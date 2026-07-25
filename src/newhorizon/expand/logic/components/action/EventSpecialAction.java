package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.content.StatusEffects;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.game.InterventionSync;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.SpecialEvent;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.components.ui.RaidMarker;
import newhorizon.expand.net.NHCall;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.headless;
import static mindustry.Vars.net;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventSpecialAction extends Action {
    public Team team = Team.crux;
    public float alertTime = 20f * Time.toSeconds;
    public float spawnRange = 180f;
    public float targetX, targetY;
    public boolean overrideDefaultCoordinate;
    public int syncSeed;
    public final Seq<SpecialEvent.UnitSpec> units = new Seq<>();

    private boolean spawned;
    private boolean popupDisplayed;

    @Override
    public String actionName() {
        return "event-special";
    }

    @Override
    public boolean skippable() {
        return false;
    }

    @Override
    public void parseTokens(String[] tokens) {
        team = ParseUtil.getFirstTeam(tokens);
        alertTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
        spawnRange = ParseUtil.getNextFloat(tokens);

        overrideDefaultCoordinate = ParseUtil.getNextBool(tokens);
        if (overrideDefaultCoordinate) {
            targetX = ParseUtil.getNextFloat(tokens) * tilesize;
            targetY = ParseUtil.getNextFloat(tokens) * tilesize;
        }

        units.clear();
        int count = ParseUtil.getNextInt(tokens);
        for (int i = 0; i < count; i++) {
            UnitType type = ParseUtil.getNextUnitType(tokens);
            int amount = ParseUtil.getNextInt(tokens);
            StatusEffect status = ParseUtil.getNextStatusEffect(tokens);
            float statusDuration = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
            Item item = ParseUtil.getNextItem(tokens);
            int itemAmount = ParseUtil.getNextInt(tokens);
            double flag = ParseUtil.getNextFlag(tokens);
            Block payload = ParseUtil.getNextBlock(tokens);

            SpecialEvent.UnitSpec spec = new SpecialEvent.UnitSpec(type, Math.max(1, amount));
            if (status != null && status != StatusEffects.none) {
                spec.status(status, statusDuration);
            }
            if (item != null && itemAmount > 0) {
                spec.item(item, itemAmount);
            }
            if (!Double.isNaN(flag)) {
                spec.flag(flag);
            }
            if (payload != null) {
                spec.payload(payload);
            }
            units.add(spec);
        }
    }

    @Override
    public void postInit() {
        super.postInit();
        duration = alertTime + 10f * Time.toSeconds + 30f;
        if (!overrideDefaultCoordinate) {
            Building core = state.rules.defaultTeam == null ? null : state.rules.defaultTeam.core();
            if (core != null) {
                targetX = core.x;
                targetY = core.y;
            }
        }
    }

    @Override
    public void begin() {
        if (syncSeed == 0) syncSeed = InterventionSync.nextSyncSeed();
        if (RaidLogic.isRemoteClient()) {
            lifeTimer = duration;
            spawned = true;
            return;
        }
        if (!headless && !InterventionSync.hasMarker(syncSeed)) {
            showPresentation();
        }
        if (net.server() && net.active()) {
            NHCall.syncInterventionAlert(toInterventionProxy());
        }
    }

    @Override
    public void end() {
        if (!spawned && RaidLogic.isLogicSide()) {
            spawnUnits();
        }
    }

    @Override
    public void act() {
        updatePopup();
        if (RaidLogic.isRemoteClient()) return;
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
        if (RaidLogic.isRemoteClient()) return;

        SpecialEvent event = new SpecialEvent();
        event.alertTime = alertTime / Time.toSeconds;
        event.spawnRange = spawnRange;
        event.teamProv = () -> team;
        event.units.addAll(units);
        event.runEffects(team, targetX, targetY, syncSeed);
    }

    private EventInterventionAction toInterventionProxy() {
        EventInterventionAction proxy = new EventInterventionAction();
        proxy.syncSeed = syncSeed;
        proxy.team = team;
        proxy.alertTime = alertTime;
        proxy.spawnRange = spawnRange;
        proxy.targetX = targetX;
        proxy.targetY = targetY;
        proxy.overrideStats = true;
        proxy.overrideDefaultCoordinate = true;
        proxy.duration = duration;
        proxy.applyNetworkState(lifeTimer, spawned);
        for (SpecialEvent.UnitSpec spec : units) {
            if (spec.type != null && spec.count > 0) {
                proxy.units.add(new EventInterventionAction.UnitEntry(spec.type, spec.count));
            }
        }
        return proxy;
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
        marker.setKind(HudMarker.Kind.SPECIAL);
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

    public void showToast() {
        NHUIFunc.showToast(
                NHContent.fleet,
                Core.bundle.format("nh.cutscene.event.fleet-popup", Strings.fixed(targetX / tilesize, 1), Strings.fixed(targetY / tilesize, 1)),
                NHSounds.uiAlert1,
                team.color
        );
    }
}
