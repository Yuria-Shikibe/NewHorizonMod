package newhorizon.expand.game;

import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Prov;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Payloadc;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.payloads.BuildPayload;
import newhorizon.content.NHFx;
import newhorizon.expand.entities.Spawner;
import newhorizon.expand.logic.components.action.EventInterventionAction;
import newhorizon.util.func.NHFunc;

import static mindustry.Vars.*;

public class SpecialEvent {
    public int id;
    public float alertTime = 20f;
    public float spawnRange = 180f;
    public Prov<Team> teamProv = () -> state.rules.waveTeam;
    public boolean requireAll = true;
    public boolean disposable = true;
    public boolean looping;
    public float loopInterval;
    public final Seq<Trigger> triggers = new Seq<>();
    public final Seq<UnitSpec> units = new Seq<>();
    public final Seq<WorldChange> worldChanges = new Seq<>();

    public Team resolveTeam() {
        Team t = teamProv == null ? null : teamProv.get();
        return t != null ? t : state.rules.waveTeam;
    }

    public boolean ally() {
        return resolveTeam() == state.rules.defaultTeam;
    }

    public boolean triggersMet() {
        if (triggers.isEmpty()) return true;
        if (requireAll) {
            for (Trigger t : triggers) {
                if (!t.valid()) return false;
            }
            return true;
        }
        for (Trigger t : triggers) {
            if (t.valid()) return true;
        }
        return false;
    }

    public Seq<EventInterventionAction.UnitEntry> toUnitEntries() {
        Seq<EventInterventionAction.UnitEntry> seq = new Seq<>();
        for (UnitSpec spec : units) {
            if (spec.type != null && spec.count > 0) {
                seq.add(new EventInterventionAction.UnitEntry(spec.type, spec.count));
            }
        }
        return seq;
    }

    public void runEffects(Team team, float worldX, float worldY, int syncSeed) {
        runEffects(team, worldX, worldY, syncSeed, null);
    }

    public void runEffects(Team team, float worldX, float worldY, int syncSeed, Seq<EventInterventionAction.UnitEntry> countOverrides) {
        if (!RaidLogic.isLogicSide()) return;
        Team spawnTeam = team != null ? team : resolveTeam();

        NHFx.spawn.at(worldX, worldY, 12f, spawnTeam.color);

        long seed = syncSeed;
        Rand lifetimeRand = new Rand(syncSeed ^ 0xC0FFEE);
        for (int i = 0; i < units.size; i++) {
            UnitSpec spec = units.get(i);
            if (spec.type == null) continue;
            int count = spec.count;
            if (countOverrides != null && i < countOverrides.size && countOverrides.get(i) != null) {
                count = countOverrides.get(i).count;
            }
            if (count <= 0) continue;
            if (spawnTeam != state.rules.waveTeam) {
                count = Math.min(count, Math.max(0, Units.getCap(spawnTeam) - spawnTeam.data().countType(spec.type)));
            }
            if (count <= 0) continue;

            seed = seed * 31L + spec.type.id + 17L;
            Seq<Vec2> points = collectSpawnPoints(spec.type, worldX, worldY, spawnRange, count, seed);
            float angle = 90f;
            Building core = state.rules.defaultTeam == null ? null : state.rules.defaultTeam.core();
            if (core != null) angle = Mathf.angle(core.x - worldX, core.y - worldY);

            for (int j = 0; j < points.size; j++) {
                Spawner spawner = new Spawner();
                spawner.init(spec.type, spawnTeam, points.get(j), angle, lifetimeRand.random(4f, 10f) * Time.toSeconds);
                if (spec.primaryStatus != null && spec.primaryStatus != StatusEffects.none) {
                    spawner.setStatus(spec.primaryStatus, spec.primaryStatusDuration);
                }
                if (!Double.isNaN(spec.flag)) spawner.flagToApply = spec.flag;
                spawner.afterSpawn = spec::applyTo;
                spawner.add();
            }
        }

        for (WorldChange change : worldChanges) {
            change.apply(spawnTeam);
        }
    }

    private static Seq<Vec2> collectSpawnPoints(UnitType type, float x, float y, float range, int count, long seed) {
        Seq<Vec2> points = new Seq<>();
        Rand rand = new Rand(seed);
        Seq<Tile> tiles = NHFunc.ableToSpawn(type, x, y, range);
        if (tiles.any()) {
            for (int i = 0; i < count; i++) {
                Tile t = tiles.get(rand.random(0, tiles.size - 1));
                points.add(new Vec2(t.worldx(), t.worldy()));
            }
            return points;
        }
        if (type.flying) {
            for (int i = 0; i < count; i++) {
                Tmp.v1.trns(rand.random(360f), rand.random(range * 0.9f)).add(x, y);
                points.add(new Vec2(Tmp.v1.x, Tmp.v1.y));
            }
            return points;
        }
        Seq<Tile> wider = NHFunc.ableToSpawn(type, x, y, range * 2.5f);
        if (wider.any()) {
            for (int i = 0; i < count; i++) {
                Tile t = wider.get(rand.random(0, wider.size - 1));
                points.add(new Vec2(t.worldx(), t.worldy()));
            }
            return points;
        }
        for (int i = 0; i < count; i++) {
            Tmp.v1.trns(rand.random(360f), rand.random(range * 0.5f)).add(x, y);
            points.add(new Vec2(Tmp.v1.x, Tmp.v1.y));
        }
        return points;
    }

    public interface Trigger {
        boolean valid();
    }

    public static class UnitSpec {
        public UnitType type;
        public int count = 1;
        public StatusEffect primaryStatus = StatusEffects.none;
        public float primaryStatusDuration;
        public double flag = Double.NaN;
        public final Seq<StatusSpec> statuses = new Seq<>();
        public final Seq<ItemStack> items = new Seq<>();
        public Block payload;

        public UnitSpec(UnitType type, int count) {
            this.type = type;
            this.count = count;
        }

        public UnitSpec status(StatusEffect effect, float duration) {
            if (primaryStatus == StatusEffects.none) {
                primaryStatus = effect;
                primaryStatusDuration = duration;
            } else {
                statuses.add(new StatusSpec(effect, duration));
            }
            return this;
        }

        public UnitSpec item(Item item, int amount) {
            items.add(new ItemStack(item, amount));
            return this;
        }

        public UnitSpec flag(double value) {
            flag = value;
            return this;
        }

        public UnitSpec payload(Block block) {
            payload = block;
            return this;
        }

        public void applyTo(Unit unit) {
            if (unit == null) return;
            for (StatusSpec s : statuses) {
                if (s.effect != null && s.effect != StatusEffects.none) {
                    unit.apply(s.effect, s.duration);
                }
            }
            for (ItemStack stack : items) {
                if (stack.item != null && stack.amount > 0) {
                    unit.addItem(stack.item, stack.amount);
                }
            }
            if (payload != null && unit instanceof Payloadc p) {
                BuildPayload pay = new BuildPayload(payload, unit.team);
                if (p.canPickupPayload(pay)) {
                    p.addPayload(pay);
                }
            }
        }
    }

    public static class StatusSpec {
        public final StatusEffect effect;
        public final float duration;

        public StatusSpec(StatusEffect effect, float duration) {
            this.effect = effect;
            this.duration = duration;
        }
    }

    public interface WorldChange {
        void apply(Team team);
    }

    public static final class Builder {
        private final SpecialEvent event = new SpecialEvent();

        public Builder alert(float seconds) {
            event.alertTime = seconds;
            return this;
        }

        public Builder spawnRange(float range) {
            event.spawnRange = range;
            return this;
        }

        public Builder team(Prov<Team> team) {
            event.teamProv = team;
            return this;
        }

        public Builder ally() {
            event.teamProv = () -> state.rules.defaultTeam;
            return this;
        }

        public Builder enemy() {
            event.teamProv = () -> state.rules.waveTeam;
            return this;
        }

        public Builder requireAll() {
            event.requireAll = true;
            return this;
        }

        public Builder requireAny() {
            event.requireAll = false;
            return this;
        }

        public Builder trigger(Trigger... triggers) {
            event.triggers.addAll(triggers);
            return this;
        }

        public Builder once() {
            event.disposable = true;
            event.looping = false;
            return this;
        }

        public Builder loop(float intervalSeconds) {
            event.looping = true;
            event.disposable = false;
            event.loopInterval = intervalSeconds;
            return this;
        }

        public Builder unit(UnitType type, int count) {
            event.units.add(new UnitSpec(type, count));
            return this;
        }

        public Builder unit(UnitType type, int count, Cons<UnitSpec> conf) {
            UnitSpec spec = new UnitSpec(type, count);
            if (conf != null) conf.get(spec);
            event.units.add(spec);
            return this;
        }

        public Builder building(int tileX, int tileY, Block block) {
            return building(tileX, tileY, block, 0);
        }

        public Builder building(int tileX, int tileY, Block block, int rotation) {
            event.worldChanges.add(team -> {
                Tile tile = world.tile(tileX, tileY);
                if (tile == null || block == null) return;
                tile.setNet(block, team, rotation);
            });
            return this;
        }

        public Builder building(int tileX, int tileY, Block block, Prov<Team> team) {
            event.worldChanges.add(ignored -> {
                Tile tile = world.tile(tileX, tileY);
                if (tile == null || block == null) return;
                Team t = team == null ? null : team.get();
                if (t == null) t = state.rules.defaultTeam;
                tile.setNet(block, t, 0);
            });
            return this;
        }

        public Builder floor(int tileX, int tileY, Floor floor) {
            event.worldChanges.add(team -> {
                Tile tile = world.tile(tileX, tileY);
                if (tile == null || floor == null) return;
                tile.setFloorNet(floor);
            });
            return this;
        }

        public Builder overlay(int tileX, int tileY, Block overlay) {
            event.worldChanges.add(team -> {
                Tile tile = world.tile(tileX, tileY);
                if (tile == null) return;
                tile.setOverlayNet(overlay);
            });
            return this;
        }

        public Builder clearBlock(int tileX, int tileY) {
            event.worldChanges.add(team -> {
                Tile tile = world.tile(tileX, tileY);
                if (tile == null) return;
                tile.setNet(Blocks.air);
            });
            return this;
        }

        public Builder world(Cons<Team> action) {
            event.worldChanges.add(action::get);
            return this;
        }

        public SpecialEvent build() {
            return event;
        }
    }

    public static final class Triggers {
        private Triggers() {
        }

        public static Trigger custom(Boolp check) {
            return check::get;
        }

        public static Trigger coreItems(Item item, int amount) {
            return coreItems(() -> state.rules.defaultTeam, item, amount);
        }

        public static Trigger coreItems(Prov<Team> team, Item item, int amount) {
            return () -> {
                Team t = team.get();
                if (t == null || item == null) return false;
                int[] total = {0};
                t.cores().each(c -> total[0] += c.items.get(item));
                return total[0] >= amount;
            };
        }

        public static Trigger coreItemsAll(Object... itemAmountPairs) {
            return coreItemsAll(() -> state.rules.defaultTeam, itemAmountPairs);
        }

        public static Trigger coreItemsAll(Prov<Team> team, Object... itemAmountPairs) {
            return () -> {
                Team t = team.get();
                if (t == null) return false;
                for (int i = 0; i < itemAmountPairs.length; i += 2) {
                    Item item = (Item) itemAmountPairs[i];
                    int amount = ((Number) itemAmountPairs[i + 1]).intValue();
                    int[] total = {0};
                    t.cores().each(c -> total[0] += c.items.get(item));
                    if (total[0] < amount) return false;
                }
                return true;
            };
        }

        public static Trigger teamUnits(UnitType type, int count) {
            return teamUnits(() -> state.rules.defaultTeam, type, count);
        }

        public static Trigger teamUnits(Prov<Team> team, UnitType type, int count) {
            return () -> {
                Team t = team.get();
                if (t == null || type == null) return false;
                return t.data().countType(type) >= count;
            };
        }

        public static Trigger teamUnitTotal(int count) {
            return teamUnitTotal(() -> state.rules.defaultTeam, count);
        }

        public static Trigger teamUnitTotal(Prov<Team> team, int count) {
            return () -> {
                Team t = team.get();
                if (t == null) return false;
                int[] total = {0};
                Groups.unit.each(u -> {
                    if (u.team == t) total[0]++;
                });
                return total[0] >= count;
            };
        }

        public static Trigger afterSeconds(float seconds) {
            return () -> state.tick >= seconds * Time.toSeconds;
        }

        public static Trigger afterMinutes(float minutes) {
            return afterSeconds(minutes * 60f);
        }

        public static Trigger waveAtLeast(int wave) {
            return () -> state.wave >= wave;
        }

        public static Trigger waveExactly(int wave) {
            return () -> state.wave == wave;
        }

        public static Trigger and(Trigger... triggers) {
            return () -> {
                for (Trigger t : triggers) {
                    if (!t.valid()) return false;
                }
                return true;
            };
        }

        public static Trigger or(Trigger... triggers) {
            return () -> {
                for (Trigger t : triggers) {
                    if (t.valid()) return true;
                }
                return false;
            };
        }

        public static Trigger not(Trigger trigger) {
            return () -> !trigger.valid();
        }
    }
}
