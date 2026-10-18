package newhorizon.expand.units.probe;

import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.logic.ThreatLevel;
import newhorizon.expand.logic.UnitProductionRecipe;
import newhorizon.expand.units.unitEntity.ProbeEntity;
import newhorizon.expand.units.unitType.AncientUnitType;

public final class ProbeProductionPlan {
    public static final int MAX_LEVEL = 8;

    // Each row is appended to the previous row. Keep this table cumulative-by-construction
    // so changing the roster remains readable and deterministic.
    private static final String[][] LEVEL_ADDITIONS = {
            {"dagger", "flare", "crawler", "nova", "mono", "histone", "sharp"},
            {"mace", "horizon", "atrax", "poly", "branch", "restriction-enzyme"},
            {"fortress", "zenith", "quad", "mega", "warper", "macrophage"},
            {"scepter", "antumbra", "eclipse", "risso", "assaulter", "lymph"},
            {"reign", "oct", "vela", "aegires", "sin", "laugra"},
            {"toxopid", "corvus", "navanax", "pester", "guardian"},
            {"eradicator", "conquer", "destruction", "longinus", "nucleoid"},
            {"ancient-probe"}
    };

    @SuppressWarnings("unchecked")
    private static final Seq<UnitType>[] pools = new Seq[MAX_LEVEL];
    private static boolean initialized;

    private ProbeProductionPlan() {
    }

    public static int level(int threat) {
        return Mathf.clamp(threat, 1, MAX_LEVEL);
    }

    public static void ensureInitialized() {
        if (initialized || Vars.content == null) return;

        Seq<UnitType> cumulative = new Seq<>();
        for (int i = 0; i < MAX_LEVEL; i++) {
            for (String name : LEVEL_ADDITIONS[i]) {
                UnitType unit = find(name);
                if (unit != null && !unit.internal && !unit.hidden && !cumulative.contains(unit)) {
                    cumulative.add(unit);
                }
            }
            pools[i] = cumulative.copy();
        }
        initialized = true;
    }

    public static UnitType choose(ProbeEntity factory, int threat, UnitType preferred) {
        ensureInitialized();
        if (factory == null || factory.team == null) return null;

        int level = level(threat);
        boolean groundPath = hasGroundPath(factory);

        if (preferred != null && canProduce(preferred, factory.team, threat)
                && (preferred.flying || groundPath)) {
            return preferred;
        }

        Seq<UnitType> pool = pools[level - 1];
        UnitType selected = weightedPick(pool, factory.team, threat, groundPath, factory.id() + (int) mindustry.Vars.state.tick);
        if (selected != null) return selected;

        return weightedPick(allUnits(), factory.team, threat, groundPath, factory.id() + level * 31);
    }

    public static Seq<UnitType> options(ProbeEntity factory) {
        ensureInitialized();
        Seq<UnitType> result = new Seq<>();
        if (factory == null || factory.team == null) return result;

        int threat = level(ThreatLevel.getTeamThreat(factory.team));
        boolean groundPath = hasGroundPath(factory);
        Seq<UnitType> pool = pools[threat - 1];
        if (pool == null) return result;

        for (UnitType type : pool) {
            if (canProduce(type, factory.team, threat) && (type.flying || groundPath)) result.add(type);
        }
        return result;
    }

    public static boolean canProduce(UnitType type, Team team, int threat) {
        if (type == null || team == null || type.internal || type.hidden) return false;
        if (type == NHUnitTypes.ancientProbe && threat < MAX_LEVEL) return false;

        Team friendly = Vars.state == null ? null : Vars.state.rules.defaultTeam;
        if (friendly != null && team != friendly && !(type instanceof AncientUnitType)) return false;
        return type.constructor != null && cost(type) > 0f && buildTime(type) > 0f;
    }

    public static float cost(UnitType type) {
        return ThreatLevel.getUnitThreat(type);
    }

    public static float buildTime(UnitType type) {
        return UnitProductionRecipe.get(type).buildTime;
    }

    public static float factoryRadius(UnitType type) {
        if (type == null) return 64f;
        return Math.max(56f, type.hitSize * 2.5f + 28f);
    }

    public static boolean hasGroundPath(ProbeEntity factory) {
        if (factory == null || factory.team == null) return false;
        Building enemyCore = factory.closestEnemyCore();
        if (enemyCore == null) return false;
        if (Vars.pathfinder == null || Vars.world == null) return true;

        Pathfinder.Flowfield field = Vars.pathfinder.getField(factory.team, Pathfinder.fieldCore, Pathfinder.costGround);
        if (field == null || !field.hasTargets()) return false;

        Tile source = Vars.world.tileWorld(factory.x, factory.y);
        if (source == null) return false;
        Tile next = field.getNextTile(source);
        return next != null && (next != source || source.within(enemyCore, 24f));
    }

    private static UnitType weightedPick(Seq<UnitType> source, Team team, int threat, boolean groundPath, int seed) {
        if (source == null || source.isEmpty()) return null;

        Seq<UnitType> candidates = new Seq<>();
        for (UnitType type : source) {
            if (!canProduce(type, team, threat)) continue;
            if (!type.flying && !groundPath) continue;
            candidates.add(type);
        }
        if (candidates.isEmpty()) return null;

        float total = 0f;
        for (UnitType type : candidates) total += weight(type);
        if (total <= 0f) return candidates.first();

        Rand random = new Rand(seed);
        float value = random.random(total);
        for (UnitType type : candidates) {
            value -= weight(type);
            if (value <= 0f) return type;
        }
        return candidates.peek();
    }

    private static float weight(UnitType type) {
        float healthWeight = type.health / 5000f;
        float costWeight = cost(type) / 150f;
        return 1f / (1f + healthWeight + costWeight);
    }

    private static Seq<UnitType> allUnits() {
        ensureInitialized();
        return Vars.content == null ? new Seq<>() : Vars.content.units();
    }

    private static UnitType find(String name) {
        if (Vars.content == null) return null;
        return Vars.content.units().find(unit -> unit.name.equals(name) || unit.name.endsWith("-" + name));
    }
}
