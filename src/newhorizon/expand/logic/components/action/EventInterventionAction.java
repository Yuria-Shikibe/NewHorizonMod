package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.entities.Spawner;
import newhorizon.expand.game.DefaultIntervention;
import newhorizon.expand.game.DefaultSpecialEvent;
import newhorizon.expand.game.InterventionState;
import newhorizon.expand.game.InterventionSync;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.SpecialEvent;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.components.ui.RaidMarker;
import newhorizon.expand.net.NHCall;
import newhorizon.util.func.NHFunc;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.headless;
import static mindustry.Vars.net;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventInterventionAction extends Action {
    public static final class UnitEntry {
        public UnitType type;
        public int count;

        public UnitEntry(UnitType type, int count) {
            this.type = type;
            this.count = count;
        }
    }

    public int eventId;
    public Team team = Team.crux;
    public float alertTime = 30f * Time.toSeconds;
    public float spawnRange = 180f;
    public float spawnReloadTime = 50f;
    public float spawnDelay = 15f;
    public float targetX, targetY;
    public boolean overrideStats, overrideDefaultCoordinate;
    public boolean presentationOnly;
    public StatusEffect status = StatusEffects.none;
    public float statusDuration = 600f;
    public double flag = Double.NaN;
    public int syncSeed;
    public final Seq<UnitEntry> units = new Seq<>();

    private boolean spawned;
    private boolean popupDisplayed;

    @Override
    public String actionName() {
        return "event-intervention";
    }

    @Override
    public boolean skippable() {
        return false;
    }

    @Override
    public void parseTokens(String[] tokens) {
        eventId = ParseUtil.getFirstInt(tokens);
        team = ParseUtil.getNextTeam(tokens);

        overrideStats = ParseUtil.getNextBool(tokens);
        if (overrideStats) {
            alertTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
            spawnRange = ParseUtil.getNextFloat(tokens);
        }

        overrideDefaultCoordinate = ParseUtil.getNextBool(tokens);
        if (overrideDefaultCoordinate) {
            targetX = ParseUtil.getNextFloat(tokens) * tilesize;
            targetY = ParseUtil.getNextFloat(tokens) * tilesize;
        }

        DefaultIntervention.FleetEvent preset = DefaultIntervention.get(eventId);
        SpecialEvent special = DefaultSpecialEvent.get(eventId);
        if (special != null) {
            applyPreset(special);
        } else if (preset == null && eventId <= 0) {
            applyPreset(DefaultIntervention.get(1));
        } else {
            applyPreset(preset);
        }
    }

    public void applyPreset(SpecialEvent event) {
        if (event == null) return;
        eventId = event.id;
        spawnReloadTime = 50f;
        spawnDelay = 15f;
        status = StatusEffects.none;
        statusDuration = 0f;
        flag = Double.NaN;
        if (event.units.any()) {
            SpecialEvent.UnitSpec first = event.units.first();
            status = first.primaryStatus;
            statusDuration = first.primaryStatusDuration;
            flag = first.flag;
        }
        if (!overrideStats) {
            alertTime = event.alertTime * Time.toSeconds;
            spawnRange = event.spawnRange;
        }
        team = event.resolveTeam();
        units.clear();
        for (UnitEntry entry : event.toUnitEntries()) {
            units.add(new UnitEntry(entry.type, entry.count));
        }
    }

    public void applyPreset(DefaultIntervention.FleetEvent event) {
        if (event == null) return;
        eventId = event.id;
        spawnReloadTime = event.spawnReloadTime;
        spawnDelay = event.spawnDelay;
        status = event.status;
        statusDuration = event.statusDuration;
        flag = event.flag;
        if (!overrideStats) {
            alertTime = event.alertTime * Time.toSeconds;
            spawnRange = event.spawnRange;
        }
        units.clear();
        for (UnitEntry entry : event.units) {
            units.add(new UnitEntry(entry.type, entry.count));
        }
    }

    @Override
    public void postInit() {
        super.postInit();
        duration = alertTime + 10f * Time.toSeconds + 30f;
        applyScale(InterventionState.scale());
    }

    public void applyScale(float scale) {
        if (scale >= 0.999f || scale <= 0.001f) return;
        for (UnitEntry entry : units) {
            if (entry.count <= 0) continue;
            entry.count = Math.max(1, Mathf.round(entry.count * scale));
        }
    }

    @Override
    public void begin() {
        if (syncSeed == 0) syncSeed = InterventionSync.nextSyncSeed();
        // World processors also run on remote clients. CSS-created interventions must not
        // present locally there — the server alert packet owns client UI (seeds would differ).
        if (RaidLogic.isRemoteClient() && !presentationOnly) {
            lifeTimer = duration;
            spawned = true;
            return;
        }
        if (!headless && !spawned && lifeTimer < alertTime
                && !InterventionSync.hasMarker(syncSeed)
                && !InterventionSync.hasMarkerAt(targetX, targetY)) {
            showPresentation();
        }
        if (!presentationOnly && net.server() && net.active()) {
            NHCall.syncInterventionAlert(this);
        }
    }

    @Override
    public void end() {
        InterventionSync.removeInterventionMarkers(this);
        if (!presentationOnly && !spawned && RaidLogic.isLogicSide()) {
            spawnUnits();
        }
    }

    public void applyNetworkState(float lifeTimer, boolean spawned) {
        this.lifeTimer = lifeTimer;
        this.spawned = spawned;
        if (lifeTimer > alertTime) popupDisplayed = true;
    }

    public boolean spawned() {
        return spawned;
    }

    private float approachAngle() {
        Building core = state.rules.defaultTeam == null ? null : state.rules.defaultTeam.core();
        if (core != null) return Angles.angle(targetX, targetY, core.x, core.y);
        return 90f;
    }

    private void showPresentation() {
        showToast();
        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(team.color);
                t2.image(NHContent.fleet).fill().color(team.color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(team.color);
            }).growX().pad(OFFSET / 2).fillY().row();
            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.fleet-alert") + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });

        RaidMarker marker = new RaidMarker();
        marker.setKind(HudMarker.Kind.INTERVENTION);
        marker.setSyncSeed(syncSeed);
        marker.setMarkPosition(targetX, targetY)
                .setDuration(alertTime)
                .bindLifeTimer(() -> this.lifeTimer);
        marker.bindAlertTime(() -> this.alertTime);
        marker.setMarkColor(team.color)
                .setRadius(spawnRange)
                .setAngle(approachAngle())
                .setIcon(NHContent.fleet)
                .addMarker();
    }

    @Override
    public void act() {
        updatePopup();
        if (presentationOnly || RaidLogic.isRemoteClient()) return;
        if (spawned || lifeTimer < alertTime) return;
        spawnUnits();
    }

    private void updatePopup() {
        if (headless) return;
        if (lifeTimer > alertTime && !popupDisplayed) {
            popupDisplayed = true;
            showToast();
        }
    }

    public void spawnUnits() {
        if (spawned) return;
        spawned = true;
        InterventionSync.finishAlert(this);
        if (presentationOnly || RaidLogic.isRemoteClient()) return;

        if (units.isEmpty()) {
            SpecialEvent specialPreset = DefaultSpecialEvent.get(eventId);
            if (specialPreset != null) applyPreset(specialPreset);
            else applyPreset(DefaultIntervention.get(eventId));
        }

        SpecialEvent special = DefaultSpecialEvent.get(eventId);
        if (special != null) {
            special.runEffects(team, targetX, targetY, syncSeed, units);
            return;
        }

        spawnJumpIn(team, targetX, targetY, spawnRange, spawnReloadTime, spawnDelay, status, statusDuration, flag, units, syncSeed);
    }

    public static void spawnJumpIn(Team team, float x, float y, float spawnRange, float spawnReloadTime, float spawnDelay,
                                   StatusEffect status, float statusDuration, double flag, Seq<UnitEntry> units, int syncSeed) {
        if (team == null) return;
        if (!RaidLogic.isLogicSide()) return;

        float angle = 90f;
        Building core = state.rules.defaultTeam == null ? null : state.rules.defaultTeam.core();
        if (core != null) angle = Angles.angle(x, y, core.x, core.y);

        NHFx.spawn.at(x, y, 12f, team.color);

        if (units == null || units.isEmpty()) return;

        long seed = syncSeed;
        Rand lifetimeRand = new Rand(syncSeed ^ 0xC0FFEE);
        for (UnitEntry entry : units) {
            if (entry.type == null || entry.count <= 0) continue;

            int count = entry.count;
            if (team != state.rules.waveTeam) {
                count = Math.min(count, Math.max(0, Units.getCap(team) - team.data().countType(entry.type)));
            }
            if (count <= 0) continue;

            seed = seed * 31L + entry.type.id + 17L;
            Seq<Vec2> points = collectSpawnPoints(entry.type, x, y, spawnRange, count, seed);
            for (int i = 0; i < points.size; i++) {
                float lifetime = lifetimeRand.random(4f, 10f) * Time.toSeconds;
                Spawner spawner = new Spawner();
                spawner.init(entry.type, team, points.get(i), angle, lifetime);
                if (status != null && status != StatusEffects.none) {
                    spawner.setStatus(status, statusDuration);
                }
                if (!Double.isNaN(flag)) spawner.flagToApply = flag;
                spawner.add();
            }
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

    public void showToast() {
        NHUIFunc.showToast(
                NHContent.fleet,
                Core.bundle.format("nh.cutscene.event.fleet-popup", Strings.fixed(targetX / tilesize, 1), Strings.fixed(targetY / tilesize, 1)),
                NHSounds.uiAlert1,
                team.color
        );
    }
}
