package newhorizon.expand.units.probe;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.IntSet;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.AIController;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.Block;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.logic.ThreatLevel;
import newhorizon.expand.units.ai.ProbeAI;
import newhorizon.expand.units.unitEntity.ProbeEntity;

import static mindustry.Vars.net;
import static mindustry.Vars.state;
import static mindustry.Vars.world;

public final class ProbeColonyManager {
    public static final float FACTORY_ARRIVAL_RANGE = 12f;
    private static final float MINING_STANDOFF = 3f * Vars.tilesize;
    private static final float SUPPORT_INTERVAL = 120f;
    private static final float ECONOMY_INTERVAL = 4f;
    private static final float SCANNER_COOLDOWN = 180f * Time.toSeconds;
    private static final int WORKERS_PER_FACTORY = 5;
    private static final int MAX_CANDIDATES = 36000;
    private static final int MAX_DEPLOYMENT_CANDIDATES = 512;
    private static final int TARGETS_PER_UPDATE = 2;
    private static final float DIVERSE_MINER_CHANCE = 0.3f;

    private static final Interval supportTimer = new Interval();
    private static final Interval economyTimer = new Interval();
    private static final Interval candidateTimer = new Interval();
    private static final IntMap<Seq<ProbeEntity>> teams = new IntMap<>();
    private static final Seq<ProbeEntity> probes = new Seq<>();
    private static final Seq<ProbeMiningEntity> miners = new Seq<>();
    private static final Seq<ProbeScannerEntity> scanners = new Seq<>();
    private static final IntMap<IntSet> reservedTargets = new IntMap<>();
    private static final IntMap<ProbeRtsTeam> rtsTeams = new IntMap<>();
    private static final Seq<Tile> candidates = new Seq<>();
    private static final ObjectFloatMap<Item> resourceValues = new ObjectFloatMap<>();
    private static final ObjectFloatMap<Item> targetScores = new ObjectFloatMap<>();
    private static final ObjectFloatMap<Block> recycleValues = new ObjectFloatMap<>();
    private static boolean initialized;
    private static boolean rolesDirty = true;
    private static int targetSearchBudget;
    private static int targetSearchCursor;
    private static int candidateWorldWidth = -1;
    private static int candidateWorldHeight = -1;

    private ProbeColonyManager() {
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        Events.on(EventType.ResetEvent.class, event -> clear());
        Events.on(EventType.WorldLoadBeginEvent.class, event -> clear());
    }

    public static void update() {
        if (!initialized || !state.isPlaying() || net.client() || world == null) return;

        if (rolesDirty || supportTimer.get(0, SUPPORT_INTERVAL)) refreshRoles();
        for (IntMap.Entry<ProbeRtsTeam> entry : rtsTeams) entry.value.update();
        if (probes.isEmpty()) {
            economyTimer.reset(0, 0f);
            return;
        }
        float elapsed = economyTimer.getTime(0);
        if (economyTimer.get(0, ECONOMY_INTERVAL)) updateEconomy(elapsed);
    }

    public static void invalidateRoles() {
        rolesDirty = true;
    }

    private static void refreshRoles() {
        probes.clear();
        teams.clear();
        miners.clear();
        scanners.clear();
        Groups.unit.each(unit -> {
            if (restoreFactoryExit(unit)) return;
            if (!(unit instanceof ProbeEntity) && unit.isValid() && unit.flag > 0d && unit.flag == (int) unit.flag) {
                Unit owner = Groups.unit.getByID((int) unit.flag);
                if (owner instanceof ProbeEntity && owner.team == unit.team) rtsTeam(unit.team).add(unit);
            }
            if (unit instanceof ProbeMiningEntity miner && miner.isValid()) miners.add(miner);
            if (unit instanceof ProbeScannerEntity scanner && scanner.isValid()) scanners.add(scanner);
            if (!(unit instanceof ProbeEntity probe) || !probe.isValid() || probe.team == null) return;
            probes.add(probe);
            Seq<ProbeEntity> group = teams.get(probe.team.id);
            if (group == null) {
                group = new Seq<>();
                teams.put(probe.team.id, group);
            }
            group.add(probe);
        });

        for (IntMap.Entry<Seq<ProbeEntity>> entry : teams) assignRoles(entry.value);
        for (ProbeEntity probe : probes) {
            probe.miningChildren = 0;
            probe.scannerChildren = 0;
        }
        for (ProbeMiningEntity miner : miners) {
            ProbeEntity owner = miner.owner();
            if (owner != null) owner.miningChildren++;
        }
        for (ProbeScannerEntity scanner : scanners) {
            ProbeEntity owner = scanner.owner();
            if (owner != null) owner.scannerChildren++;
        }
        rolesDirty = false;
        supportTimer.reset(0, 0f);
        for (ProbeEntity probe : probes) {
            if (probe.factory) updateFactory(probe);
            else if (probe.factoryCandidate) prepareProduction(probe);
        }
    }

    private static void assignRoles(Seq<ProbeEntity> group) {
        group.sort((left, right) -> Integer.compare(left.id(), right.id()));
        int automaticFactoryCount = Math.max(1, (group.size + WORKERS_PER_FACTORY) / (WORKERS_PER_FACTORY + 1));
        int manualCandidateCount = group.count(probe -> probe.manualFactory && !probe.factory);
        Seq<ProbeEntity> factories = new Seq<>();
        for (ProbeEntity probe : group) {
            if (probe.factory) {
                probe.factoryCandidate = false;
                factories.add(probe);
            } else {
                probe.factoryCandidate = false;
            }
        }

        int factoryCount = Math.max(automaticFactoryCount, factories.size + manualCandidateCount);
        int candidateSlots = Math.max(0, factoryCount - factories.size);
        for (ProbeEntity probe : group) {
            if (candidateSlots <= 0) break;
            if (!probe.factory && probe.manualFactory) {
                probe.factoryCandidate = true;
                candidateSlots--;
            }
        }
        for (ProbeEntity probe : group) {
            if (candidateSlots <= 0) break;
            if (probe.factory || probe.factoryCandidate) continue;
            probe.factoryCandidate = true;
            candidateSlots--;
        }

        for (ProbeEntity probe : group) {
            probe.factory = factories.contains(probe);
            if (probe.factory) {
                probe.factoryId = probe.id();
            } else {
                ProbeEntity nearest = nearestFactory(probe, factories);
                probe.factoryId = probe.factoryCandidate || nearest == null ? -1 : nearest.id();
                if (!probe.factoryCandidate && nearest == null) {
                    probe.miningTargetTile = -1;
                }
            }
        }
    }

    private static ProbeEntity nearestFactory(ProbeEntity probe, Seq<ProbeEntity> factories) {
        ProbeEntity nearest = null;
        float distance = Float.MAX_VALUE;
        for (ProbeEntity factory : factories) {
            float dst = probe.dst2(factory);
            if (dst < distance) {
                distance = dst;
                nearest = factory;
            }
        }
        return nearest;
    }

    private static ProbeEntity findFactory(ProbeEntity probe) {
        Unit unit = Groups.unit.getByID(probe.factoryId);
        return unit instanceof ProbeEntity factory && factory.isValid() && factory.factory && factory.team == probe.team ? factory : null;
    }

    private static void updateFactoryCandidate(ProbeEntity probe) {
        if (probe.selectedProduction == null) prepareProduction(probe);
        if (probe.selectedProduction == null) return;
        Tile tile = deploymentTarget(probe);
        if (tile == null || !probe.within(tile.worldx(), tile.worldy(), FACTORY_ARRIVAL_RANGE)
                || !ProbeFactoryParts.retracted(probe)) return;

        probe.set(tile.worldx(), tile.worldy());
        probe.vel.setZero();
        probe.factory = true;
        probe.factoryCandidate = false;
        probe.factoryId = probe.id();
        probe.miningTargetTile = -1;
        if (probe.controller() instanceof ProbeAI colony) colony.clearCommands();
        probe.miningWorkProgress = 0f;
        consumeFactoryItems(probe);
        Fx.teleportActivate.at(probe.x, probe.y, probe.rotation, probe.team.color);
        updateFactory(probe);
    }

    private static Tile deploymentTarget(ProbeEntity factory) {
        Tile tile = targetTile(factory.miningTargetTile);
        if (tile != null && !ProbeFactoryLayout.canPlace(factory, tile.worldx(), tile.worldy(), probes)) tile = null;
        if (tile == null) {
            factory.miningTargetTile = factory.factory ? -2 : -1;
            if (targetSearchBudget > 0 && Time.time >= factory.miningTargetRetry) {
                int target = chooseTarget(factory, factory, true);
                factory.miningTargetTile = target < 0 && factory.factory ? -2 : target;
                factory.miningTargetRetry = Time.time + SUPPORT_INTERVAL;
                tile = targetTile(factory.miningTargetTile);
            }
        }
        return tile;
    }

    private static boolean updateFactoryPlacement(ProbeEntity factory) {
        if (!ProbeFactoryLayout.moving(factory) && !ProbeFactoryLayout.canStay(factory, probes)) {
            factory.miningTargetTile = -2;
            factory.miningTargetRetry = 0f;
        }
        if (!ProbeFactoryLayout.moving(factory)) return ProbeFactoryParts.stationary(factory);
        Tile tile = deploymentTarget(factory);
        if (tile == null || !factory.within(tile.worldx(), tile.worldy(), FACTORY_ARRIVAL_RANGE)
                || !ProbeFactoryParts.retracted(factory)) return false;
        factory.set(tile.worldx(), tile.worldy());
        factory.vel.setZero();
        factory.miningTargetTile = -1;
        Fx.teleportActivate.at(factory.x, factory.y, factory.rotation, factory.team.color);
        return true;
    }

    private static void updateFactory(ProbeEntity factory) {
        if (!factory.isValid()) return;
        prepareProduction(factory);
        if (!ProbeFactoryLayout.moving(factory) && !ProbeFactoryLayout.canStay(factory, probes)) {
            factory.miningTargetTile = -2;
            factory.miningTargetRetry = 0f;
        }
        if (ProbeFactoryLayout.moving(factory)) return;
        maintainMiningSupport(factory);
        updateScanner(factory);
    }

    private static ProbeRtsTeam rtsTeam(Team team) {
        ProbeRtsTeam fleet = rtsTeams.get(team.id);
        if (fleet == null) {
            fleet = new ProbeRtsTeam(team);
            rtsTeams.put(team.id, fleet);
        }
        return fleet;
    }

    private static void prepareProduction(ProbeEntity factory) {
        int threat = ProbeProductionPlan.level(ThreatLevel.getTeamThreat(factory.team));
        if (factory.selectedProduction == null
                || (!factory.manualFactory && factory.productionThreatLevel != threat)
                || !ProbeProductionPlan.canProduce(factory.selectedProduction, factory.team, threat)
                || (!factory.manualFactory && !factory.selectedProduction.flying
                && !ProbeProductionPlan.hasGroundPath(factory))) {
            UnitType selected = ProbeProductionPlan.choose(factory, threat, null);
            if (selected != factory.selectedProduction) factory.productionProgress = 0f;
            factory.selectedProduction = selected;
        }
        factory.productionThreatLevel = threat;
        if (factory.selectedProduction == null) {
            factory.productionRequirement = 0f;
            factory.productionProgress = 0f;
            return;
        }

        factory.factoryRadius = ProbeProductionPlan.factoryRadius(factory.selectedProduction);
        factory.productionRequirement = ProbeProductionPlan.cost(factory.selectedProduction) * 10f;
        updateProgress(factory);
    }

    private static void updateProduction(ProbeEntity factory, float elapsed) {
        if (factory.selectedProduction == null || factory.productionRequirement <= 0f) return;
        if (factory.productionContribution < factory.productionRequirement) {
            factory.productionProgress = 0f;
            return;
        }
        float buildTime = ProbeProductionPlan.buildTime(factory.selectedProduction);
        if (buildTime <= 0f) return;
        factory.productionProgress += elapsed / buildTime;
        updateProgress(factory);
        if (factory.productionProgress < 1f) return;
        if (!spawnProduced(factory, factory.selectedProduction)) return;

        factory.productionContribution = Math.max(0f, factory.productionContribution - factory.productionRequirement);
        factory.productionProgress = 0f;
        updateProgress(factory);
        Fx.spawn.at(factory.x + factory.factoryRadius, factory.y + factory.factoryRadius,
                factory.selectedProduction.hitSize, factory.team.color);
    }

    public static boolean selectProduction(ProbeEntity factory, UnitType type) {
        if (factory == null || !factory.isValid() || type == null || factory.team == null) return false;

        refreshRoles();

        int threat = ProbeProductionPlan.level(ThreatLevel.getTeamThreat(factory.team));
        if (!ProbeProductionPlan.canProduce(type, factory.team, threat)
                || (!type.flying && !ProbeProductionPlan.hasGroundPath(factory))) return false;

        boolean previousManualFactory = factory.manualFactory;
        boolean previousFactoryCandidate = factory.factoryCandidate;
        UnitType previousProduction = factory.selectedProduction;
        float previousProgress = factory.productionProgress;
        int previousThreat = factory.productionThreatLevel;
        factory.manualFactory = true;
        if (!factory.factory) factory.factoryCandidate = true;
        factory.selectedProduction = type;
        if (previousProduction != type) factory.productionProgress = 0f;
        factory.productionThreatLevel = threat;
        refreshRoles();
        if (factory.factory || factory.factoryCandidate) return true;

        factory.manualFactory = previousManualFactory;
        factory.factoryCandidate = previousFactoryCandidate;
        factory.selectedProduction = previousProduction;
        factory.productionProgress = previousProgress;
        factory.productionThreatLevel = previousThreat;
        refreshRoles();
        return false;
    }

    private static boolean spawnProduced(ProbeEntity factory, UnitType type) {
        int current = 0;
        for (Unit unit : Groups.unit) {
            if (unit.team == factory.team && (unit.flag == factory.id() || unit.flag == -(factory.id() + 1d))) current++;
        }

        int max = 20;
        if (current >= max) return false;
        float centerX = factory.x + factory.factoryRadius;
        float centerY = factory.y + factory.factoryRadius;
        Unit unit = type.create(factory.team);
        unit.set(centerX, centerY);
        unit.rotation = Mathf.random(360f);
        unit.flag = -(factory.id() + 1d);
        boolean ignoreUnitCap = factory.team.ignoreUnitCap;
        factory.team.ignoreUnitCap = true;
        try {
            unit.add();
        } finally {
            factory.team.ignoreUnitCap = ignoreUnitCap;
        }
        if (!unit.isValid()) return false;
        unit.controller(new ProbeFactoryExitAI(factory));
        factory.lastProducedThreat = ThreatLevel.getTeamThreat(factory.team);
        return true;
    }

    private static boolean restoreFactoryExit(Unit unit) {
        if (!unit.isValid() || unit.flag >= 0d || unit.flag != (int) unit.flag) return false;
        Unit owner = Groups.unit.getByID((int) -unit.flag - 1);
        if (!(owner instanceof ProbeEntity factory) || !factory.isValid() || !factory.factory || factory.team != unit.team) return false;
        if (!(unit.controller() instanceof ProbeFactoryExitAI) && !unit.isPlayer()) unit.controller(new ProbeFactoryExitAI(factory));
        return true;
    }

    static void finishFactoryExit(Unit unit, int factoryId) {
        unit.flag = factoryId;
        unit.vel.setZero();
        if (unit instanceof ProbeEntity) {
            unit.controller(new ProbeAI());
            invalidateRoles();
        } else {
            rtsTeam(unit.team).add(unit);
        }
    }

    static boolean canExitAt(Unit unit, float x, float y) {
        float margin = unit.hitSize / 2f + Vars.tilesize;
        for (ProbeEntity factory : probes) {
            if (!factory.isValid() || !factory.factory || ProbeFactoryLayout.moving(factory)) continue;
            float extent = factory.factoryRadius * 2f;
            if (x >= factory.x - margin && x <= factory.x + extent + margin
                    && y >= factory.y - margin && y <= factory.y + extent + margin) return false;
        }
        return true;
    }

    private static void updateScanner(ProbeEntity factory) {
        Seq<ProbeEntity> group = teams.get(factory.team.id);
        int total = group == null ? 1 : group.size;
        int active = 0;
        for (ProbeScannerEntity scanner : scanners) if (scanner.isValid() && scanner.team == factory.team) active++;
        int max = Math.max(1, total / 10);
        if (active >= max || factory.scannerChildren > 0 || Time.time < factory.scannerCooldownUntil) return;

        Building target = findScanTarget(factory);
        if (target == null || NHUnitTypes.probeScannerPhantom == null) return;

        ProbeScannerEntity scanner = (ProbeScannerEntity) NHUnitTypes.probeScannerPhantom.create(factory.team);
        scanner.ownerId = factory.id();
        scanner.targetTile = target.tile.array();
        scanner.set(factory.x, factory.y);
        scanner.add();
        scanners.add(scanner);
        factory.scannerChildren++;
        Fx.spawn.at(scanner.x, scanner.y, scanner.hitSize, factory.team.color);
    }

    private static Building findScanTarget(ProbeEntity factory) {
        Team enemy = enemyTeam(factory.team);
        if (enemy == null) return null;
        factory.targetTeam = enemy;
        return Vars.indexer.findTile(enemy, factory.x, factory.y, factory.scanRange,
                building -> building.block.priority >= TargetPriority.base);
    }

    private static void updateEconomy(float elapsed) {
        if (candidateTimer.get(0, 600f)
                || candidateWorldWidth != world.width() || candidateWorldHeight != world.height()) rebuildCandidates();
        targetSearchBudget = TARGETS_PER_UPDATE;
        rebuildReservations();

        for (ProbeEntity probe : probes) {
            if (!probe.isValid()) continue;
            if (probe.factoryCandidate) {
                updateFactoryCandidate(probe);
                continue;
            }
            if (probe.factory) {
                consumeFactoryItems(probe);
                if (updateFactoryPlacement(probe)) updateProduction(probe, elapsed);
                continue;
            }
            ProbeEntity factory = findFactory(probe);
            if (factory != null) updateMiningProbe(probe, factory, elapsed);
        }

        for (ProbeMiningEntity miner : miners) {
            if (!miner.isValid()) continue;
            ProbeEntity factory = miner.owner();
            if (factory != null) updateMiningPhantom(miner, factory, elapsed);
        }
    }

    private static void updateMiningProbe(ProbeEntity miner, ProbeEntity factory, float elapsed) {
        if (factory == null) return;
        if (shouldDeliver(miner, miner.miningTargetTile)) {
            if (miner.within(factory, deliveryRange(miner, factory))) {
                deliverItems(miner, factory);
                miner.miningTargetTile = -1;
                miner.miningWorkProgress = 0f;
                miner.miningTargetRetry = 0f;
            }
            return;
        }

        updateMiningTarget(miner, factory);
        Tile tile = targetTile(miner.miningTargetTile);
        if (!canHarvest(miner, tile) || !withinMiningRange(miner, tile)) return;
        miner.miningWorkProgress += miningIncrement(miner, tile, elapsed);
        if (miner.miningWorkProgress >= 1f) {
            int amount = (int) miner.miningWorkProgress;
            miner.miningWorkProgress -= amount;
            collect(miner, tile, factory, amount);
        }
    }

    private static void collect(Unit miner, Tile tile, ProbeEntity factory, int amount) {
        if (tile.build != null && tile.build.team == Team.derelict) {
            collect(factory, tile);
            return;
        }

        Item item = miner.getMineResult(tile);
        if (item == null) return;
        int collected = Math.min(amount, Math.max(0, miner.itemCapacity() - miner.stack.amount));
        if (collected <= 0) return;
        miner.addItem(item, collected);
        showItemTransfer(item, collected, tile.worldx(), tile.worldy(), miner);
    }

    private static void updateMiningPhantom(ProbeMiningEntity miner, ProbeEntity factory, float elapsed) {
        if (shouldDeliver(miner, miner.targetTile)) {
            if (miner.within(factory, deliveryRange(miner, factory))) {
                deliverItems(miner, factory);
                miner.targetTile = -1;
                miner.workProgress = 0f;
                miner.targetRetry = 0f;
            }
            return;
        }

        if (!canHarvest(miner, targetTile(miner.targetTile)) && targetSearchBudget > 0 && Time.time >= miner.targetRetry) {
            miner.targetTile = chooseTarget(factory, miner, false);
            miner.workProgress = 0f;
            miner.targetRetry = Time.time + SUPPORT_INTERVAL;
        }
        Tile tile = targetTile(miner.targetTile);
        if (!canHarvest(miner, tile) || !withinMiningRange(miner, tile)) return;
        miner.workProgress += miningIncrement(miner, tile, elapsed);
        if (miner.workProgress >= 1f) {
            int amount = (int) miner.workProgress;
            miner.workProgress -= amount;
            collect(miner, tile, factory, amount);
        }
    }

    private static void updateMiningTarget(ProbeEntity miner, ProbeEntity factory) {
        if (canHarvest(miner, targetTile(miner.miningTargetTile)) || targetSearchBudget <= 0 || Time.time < miner.miningTargetRetry) return;
        miner.miningTargetTile = chooseTarget(factory, miner, false);
        miner.miningWorkProgress = 0f;
        miner.miningTargetRetry = Time.time + SUPPORT_INTERVAL;
    }

    private static int chooseTarget(ProbeEntity factory, Unit miner, boolean deployment) {
        targetSearchBudget--;
        if (candidates.isEmpty()) return -1;
        int seed = miner.id();
        Tile selected = null;
        float best = Float.NEGATIVE_INFINITY;
        boolean diverse = !deployment && Mathf.randomSeed(seed * 523L) < DIVERSE_MINER_CHANCE;
        float preferredValue = Mathf.randomSeed(seed * 953L, 1f, 3f);
        targetScores.clear();
        IntSet used = reservations(factory.team);
        Team enemy = enemyTeam(factory.team);
        Building enemyCore = enemy == null ? null : enemy.core();
        int limit = Math.min(deployment ? MAX_DEPLOYMENT_CANDIDATES : MAX_CANDIDATES, candidates.size);
        int start = targetSearchCursor % candidates.size;
        targetSearchCursor = (start + 1) % candidates.size;
        for (int checked = 0; checked < limit; checked++) {
            Tile candidate = candidates.get((int) ((start + (long) checked * candidates.size / limit) % candidates.size));
            if (!validCandidate(candidate) || used.contains(candidate.array())) continue;
            if (!deployment && !canHarvest(miner, candidate)) continue;
            Item item = candidate.build != null ? null : deployment
                    ? (candidate.drop() != null ? candidate.drop() : candidate.wallDrop()) : miner.getMineResult(candidate);
            float value = item == null ? buildingValue(candidate.build) : resourceValue(item);
            float resourceScore = value * 100f;
            if (diverse) {
                resourceScore = item == null ? 0f : targetScores.get(item, -1f);
                if (resourceScore < 0f) {
                    resourceScore = 80f / (1f + Math.abs(value - preferredValue))
                            + Mathf.randomSeed(seed * 31L + item.id * 8191L, 20f);
                    targetScores.put(item, resourceScore);
                }
            }
            float distance = enemyCore == null ? 0f : candidate.dst(enemyCore);
            float local = miner.dst(candidate.worldx(), candidate.worldy());
            float score = resourceScore + distance * 0.025f - local * (diverse ? 0.05f : 0.02f);
            if (score > best) {
                Tile target = deployment ? ProbeFactoryLayout.findAnchor(factory, candidate, probes) : candidate;
                if (target == null) continue;
                best = score;
                selected = target;
            }
        }
        if (selected == null) return -1;
        used.add(selected.array());
        return selected.array();
    }

    private static IntSet reservations(Team team) {
        IntSet used = reservedTargets.get(team.id);
        if (used == null) {
            used = new IntSet();
            reservedTargets.put(team.id, used);
        }
        return used;
    }

    private static void rebuildReservations() {
        for (IntMap.Entry<IntSet> entry : reservedTargets) entry.value.clear();
        for (ProbeEntity probe : probes) {
            if (probe.isValid() && !probe.factory && probe.miningTargetTile >= 0) reservations(probe.team).add(probe.miningTargetTile);
        }
        for (ProbeMiningEntity miner : miners) {
            if (miner.isValid() && miner.targetTile >= 0) reservations(miner.team).add(miner.targetTile);
        }
    }

    public static boolean shouldDeliver(Unit miner, int target) {
        if (!miner.hasItem()) return false;
        Tile tile = targetTile(target);
        if (miner.stack.amount >= miner.itemCapacity() || !canHarvest(miner, tile)) return true;
        Item item = miner.getMineResult(tile);
        return item != miner.item();
    }

    public static boolean canHarvest(Unit miner, Tile tile) {
        return validCandidate(tile) && miner.canMine() && (tile.build != null || miner.validMine(tile, false));
    }

    public static boolean withinMiningRange(Unit miner, Tile tile) {
        if (tile == null || !miner.within(tile.worldx(), tile.worldy(), miner.type.mineRange)) return false;
        float minimum = miningMinimumRange(miner);
        return miner.dst2(tile.worldx(), tile.worldy()) >= minimum * minimum;
    }

    private static float miningMinimumRange(Unit miner) {
        return Math.min(MINING_STANDOFF, miner.type.mineRange * 0.8f);
    }

    public static void approachMine(AIController controller, Unit miner, Tile tile) {
        if (withinMiningRange(miner, tile)) {
            miner.vel.setZero();
        } else {
            float minimum = miningMinimumRange(miner);
            float distance = (minimum + miner.type.mineRange) / 2f;
            if (miner.dst2(tile.worldx(), tile.worldy()) < minimum * minimum) {
                Tmp.v1.set(miner.x - tile.worldx(), miner.y - tile.worldy());
                if (Tmp.v1.isZero()) Tmp.v1.trns(Mathf.randomSeed(miner.id(), 360f), 1f);
                Tmp.v1.setLength(distance).add(tile.worldx(), tile.worldy());
                controller.moveTo(Tmp.v1, 0f, 8f, false, null);
            } else {
                controller.moveTo(Tmp.v1.set(tile.worldx(), tile.worldy()), distance, 8f, false, null);
            }
        }
        miner.lookAt(tile.worldx(), tile.worldy());
    }

    public static float deliveryRange(Unit miner, ProbeEntity factory) {
        return (miner.hitSize + factory.hitSize) * 0.5f + Vars.tilesize;
    }

    private static float miningIncrement(Unit miner, Tile tile, float elapsed) {
        Item item = miner.getMineResult(tile);
        float duration = 50f + (miner.type.mineHardnessScaling && item != null ? item.hardness * 15f : 15f);
        return elapsed * miner.type.mineSpeed * state.rules.unitMineSpeed(miner.team) / duration;
    }

    private static void collect(ProbeEntity factory, Tile tile) {
        if (tile.build != null && tile.build.team == Team.derelict) {
            ItemStack[] requirements = tile.build.block.requirements;
            for (ItemStack stack : requirements) {
                if (stack.item != null && stack.amount > 0) deliver(factory, stack.item, Math.max(1, stack.amount / 4));
            }
            tile.build.kill();
            return;
        }

        Item item = tile.drop() != null ? tile.drop() : tile.wallDrop();
        if (item == null) return;
        deliver(factory, item, 1);
    }

    private static void deliverItems(Unit miner, ProbeEntity factory) {
        Item item = miner.item();
        int amount = miner.stack.amount;
        deliver(factory, item, amount);
        miner.clearItem();
        showItemTransfer(item, amount, miner.x, miner.y, factory);
    }

    private static void showItemTransfer(Item item, int amount, float x, float y, Unit to) {
        if (item == null || amount <= 0) return;
        Call.transferItemEffect(item, x, y, to);
        int particles = Mathf.clamp(amount / 3, 1, 8);
        for (int particle = 1; particle < particles; particle++) {
            Time.run(particle * 3f, () -> {
                if (to.isValid()) Call.transferItemEffect(item, x, y, to);
            });
        }
    }

    private static void deliver(ProbeEntity factory, Item item, int amount) {
        if (item == null || amount <= 0) return;
        factory.productionContribution += amount * ThreatLevel.getItemWeight(item);
        updateProgress(factory);
    }

    private static void consumeFactoryItems(ProbeEntity factory) {
        if (!factory.hasItem()) return;
        Item item = factory.item();
        int amount = factory.stack.amount;
        factory.clearItem();
        deliver(factory, item, amount);
    }

    private static void updateProgress(ProbeEntity factory) {
        factory.productionProgress = factory.productionRequirement <= 0f
                || factory.productionContribution < factory.productionRequirement ? 0f : Mathf.clamp(factory.productionProgress);
    }

    public static float resourceProgress(ProbeEntity factory) {
        return factory.productionRequirement <= 0f ? 0f
                : Mathf.clamp(factory.productionContribution / factory.productionRequirement);
    }

    private static boolean validCandidate(Tile tile) {
        if (tile == null) return false;
        if (tile.build != null) return tile.build.team == Team.derelict && tile.build.block.requirements.length > 0;
        return tile.drop() != null || tile.wallDrop() != null;
    }

    private static float buildingValue(Building build) {
        if (build == null || build.block.requirements == null) return 0f;
        float cached = recycleValues.get(build.block, -1f);
        if (cached >= 0f) return cached;
        float value = 0f;
        for (ItemStack stack : build.block.requirements) value += stack.amount * resourceValue(stack.item);
        recycleValues.put(build.block, value);
        return value;
    }

    private static float resourceValue(Item item) {
        if (item == null) return 0f;
        float cached = resourceValues.get(item, -1f);
        if (cached >= 0f) return cached;
        float value = ThreatLevel.getItemWeight(item);
        resourceValues.put(item, value);
        return value;
    }

    public static Tile targetTile(int array) {
        return array < 0 || world == null || array >= (long) world.width() * world.height() ? null : world.tiles.geti(array);
    }

    private static void rebuildCandidates() {
        candidates.clear();
        resourceValues.clear();
        targetScores.clear();
        recycleValues.clear();
        candidateWorldWidth = world.width();
        candidateWorldHeight = world.height();
        for (Tile tile : world.tiles) {
            if (validCandidate(tile) && (tile.build == null || tile == tile.build.tile)) candidates.add(tile);
        }
        targetSearchCursor = 0;
        candidateTimer.reset(0, 0f);
    }

    private static void spawnMiningPhantom(ProbeEntity factory) {
        if (NHUnitTypes.probeMiningPhantom == null) return;
        ProbeMiningEntity miner = (ProbeMiningEntity) NHUnitTypes.probeMiningPhantom.create(factory.team);
        miner.ownerId = factory.id();
        miner.set(factory.x + Mathf.random(-16f, 16f), factory.y + Mathf.random(-16f, 16f));
        miner.health = Math.max(1f, factory.maxHealth * 0.1f);
        miner.add();
        miners.add(miner);
        factory.miningChildren++;
        Fx.spawn.at(miner.x, miner.y, miner.hitSize, factory.team.color);
    }

    private static void maintainMiningSupport(ProbeEntity factory) {
        if (factory == null || !factory.factory || NHUnitTypes.probeMiningPhantom == null) return;
        Seq<ProbeEntity> group = teams.get(factory.team.id);
        int workers = group == null ? 0 : group.count(probe -> !probe.factory && !probe.factoryCandidate && probe.factoryId == factory.id());
        if (workers > 0) {
            for (ProbeMiningEntity miner : miners) {
                if (miner.isValid() && miner.ownerId == factory.id() && !miner.hasItem()) miner.kill();
            }
        } else if (factory.miningChildren == 0) {
            spawnMiningPhantom(factory);
        }
    }

    public static void onMiningRemoved(ProbeMiningEntity miner) {
        Unit unit = Groups.unit.getByID(miner.ownerId);
        if (unit instanceof ProbeEntity factory) factory.miningChildren = Math.max(0, factory.miningChildren - 1);
    }

    public static void onScannerRemoved(int ownerId) {
        Unit unit = Groups.unit.getByID(ownerId);
        if (unit instanceof ProbeEntity factory) {
            factory.scannerChildren = Math.max(0, factory.scannerChildren - 1);
            factory.scannerCooldownUntil = Time.time + SCANNER_COOLDOWN;
        }
    }

    public static void drawMining(Unit miner, int target, float progress) {
        Tile tile = targetTile(target);
        if (!canHarvest(miner, tile) || shouldDeliver(miner, target) || !withinMiningRange(miner, tile)) return;
        float z = Draw.z();
        Draw.z(Layer.effect);
        Draw.color(miner.team.color);
        Lines.stroke(0.7f);
        Lines.line(miner.x, miner.y, tile.worldx(), tile.worldy());
        Lines.circle(tile.worldx(), tile.worldy(), 2f + progress * 2f);
        Draw.reset();
        Draw.z(z);
    }

    public static void drawFactory(ProbeEntity factory) {
        if (factory != null) ProbeFactoryRenderer.draw(factory);
    }

    private static Team enemyTeam(Team team) {
        if (state.rules.defaultTeam != null && state.rules.defaultTeam != team) return state.rules.defaultTeam;
        if (state.rules.waveTeam != null && state.rules.waveTeam != team) return state.rules.waveTeam;
        Team selected = null;
        float distance = Float.MAX_VALUE;
        Building ownCore = team.core();
        for (Team candidate : Team.all) {
            if (candidate == team || !candidate.active() || candidate.core() == null) continue;
            float dst = ownCore == null ? 0f : candidate.core().dst2(ownCore);
            if (dst < distance) {
                distance = dst;
                selected = candidate;
            }
        }
        return selected;
    }

    private static void clear() {
        probes.clear();
        teams.clear();
        miners.clear();
        scanners.clear();
        reservedTargets.clear();
        rtsTeams.clear();
        candidates.clear();
        resourceValues.clear();
        targetScores.clear();
        recycleValues.clear();
        supportTimer.reset(0, SUPPORT_INTERVAL);
        economyTimer.reset(0, 0f);
        candidateTimer.reset(0, 600f);
        candidateWorldWidth = candidateWorldHeight = -1;
        targetSearchCursor = 0;
        rolesDirty = true;
    }
}
