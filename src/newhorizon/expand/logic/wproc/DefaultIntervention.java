package newhorizon.expand.logic.wproc;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.logic.LVar;
import mindustry.ui.Styles;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHContent;
import newhorizon.content.NHLogic;
import newhorizon.content.NHSounds;
import newhorizon.expand.game.DefaultRaidStrength;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.logic.components.action.EventInterventionAction;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;
import newhorizon.util.ui.NHUIFunc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class DefaultIntervention extends LStatement {
    private static final int[][] AUTO_TIERS = {
            {1, 2, 3},
            {2, 3, 4},
            {3, 4, 5, 6},
            {5, 6, 7, 8},
            {7, 8, 9, 10},
            {9, 10, 11, 15},
            {11, 12, 13, 16},
            {12, 13, 14, 16}
    };

    public String flag = "event-executor", timer = "event-timer", alertTime = "30", eventId = "0";

    public Vec2 target = new Vec2();

    public DefaultIntervention(String[] tokens) {
        try {
            flag = tokens[1];
            timer = tokens[2];
            alertTime = tokens[3];
            eventId = tokens[4];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.err(e);
        }
    }

    public DefaultIntervention() {
    }

    @Override
    public void build(Table table) {
        table.table(t -> {
            t.add(" Executor Flag : ");
            fields(t, flag, str -> flag = str).width(180);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Timer Name : ");
            fields(t, timer, str -> timer = str).width(180);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Alert Time: ");
            fields(t, alertTime, str -> alertTime = str);
            t.add("(s)");
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Event Id (0=auto): ");
            fields(t, eventId, str -> eventId = str).width(120);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHLogic.nhwproc;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("defaultintervention").append(" ");
        builder.append(flag).append(" ");
        builder.append(timer).append(" ");
        builder.append(alertTime).append(" ");
        builder.append(eventId).append(" ");
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultInterventionInstruction(
                builder.var(flag),
                builder.var(timer),
                builder.var(alertTime),
                builder.var(eventId)
        );
    }

    public static void showFleetAlertHud(String timerName, float time, float range, float tx, float ty) {
        Team wave = state.rules.waveTeam;

        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(wave.color);
                t2.image(NHContent.fleet).fill().color(wave.color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(wave.color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.fleet-alert") + " >>")).color(wave.color).padBottom(4).row()).growX().fillY();
        });

        NHSounds.uiAlert1.play();

        state.rules.objectives.each(mapObjective -> {
            if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timerName)) {
                obj.trigger(time * Time.toSeconds);
                for (MapObjectives.ObjectiveMarker marker : obj.markers) {
                    if (marker instanceof RaidIndicator idc) {
                        idc.init(wave.id, 2, range * tilesize, timerName)
                                .setPosition(Tmp.v2.set(tx, ty), Tmp.v3.set(tx, ty));
                    }
                }
            }
        });
    }

    public class DefaultInterventionInstruction implements LExecutor.LInstruction {
        public LVar flag, timer, alertTime, eventId;

        public float curTime;
        public boolean iconShown;
        public boolean labelShown;
        public boolean spawned;
        public newhorizon.expand.game.DefaultIntervention.FleetEvent fleet;

        public DefaultInterventionInstruction(LVar flag, LVar timer, LVar alertTime, LVar eventId) {
            this.flag = flag;
            this.timer = timer;
            this.alertTime = alertTime;
            this.eventId = eventId;
        }

        @Override
        public void run(LExecutor exec) {
            if (!state.rules.objectiveFlags.contains(flag.name)) {
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            float alert = alertTime.numf();
            if (curTime >= alert) {
                if (!spawned && RaidLogic.isLogicSide()) spawnFleet();
                reset();
            } else {
                exec.counter.numval--;
                exec.yield = true;
                curTime += Time.delta / 60f;
                if (!iconShown) showAlert();
                if (curTime > alert && !labelShown) showLabel();
            }
        }

        public void updatePosition() {
            float wx = Mathf.random(0, world.unitWidth());
            float wy = Mathf.random(0, world.unitHeight());

            AtomicReference<BlockFlag> blockFlag = new AtomicReference<>(BlockFlag.core);
            WeightedRandom.random(
                    new WeightedOption(3f, () -> blockFlag.set(BlockFlag.turret)),
                    new WeightedOption(3f, () -> blockFlag.set(BlockFlag.generator)),
                    new WeightedOption(3f, () -> blockFlag.set(BlockFlag.factory)),
                    new WeightedOption(1f, () -> blockFlag.set(BlockFlag.core))
            );
            Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(state.rules.waveTeam, blockFlag.get()));
            if (b == null) b = state.rules.defaultTeam.core();
            if (b != null) {
                target.set(b.x, b.y);
            } else {
                target.setZero();
            }
        }

        public void resolveFleet() {
            int id = eventId.numi();
            if (id > 0) {
                fleet = newhorizon.expand.game.DefaultIntervention.get(id);
            } else {
                int tier = Mathf.clamp(DefaultRaidStrength.toTier(state.rules.defaultTeam), 1, AUTO_TIERS.length);
                int[] pool = AUTO_TIERS[tier - 1];
                fleet = newhorizon.expand.game.DefaultIntervention.get(pool[Mathf.random(pool.length - 1)]);
            }
            if (fleet == null) fleet = newhorizon.expand.game.DefaultIntervention.get(1);
        }

        public void reset() {
            curTime = 0f;
            iconShown = false;
            labelShown = false;
            spawned = false;
            fleet = null;
            state.rules.objectiveFlags.remove(flag.name);
        }

        public void spawnFleet() {
            spawned = true;
            if (fleet == null) return;
            Team team = state.rules.waveTeam;
            newhorizon.expand.game.SpecialEvent special = newhorizon.expand.game.DefaultSpecialEvent.get(fleet.id);
            if (special != null) {
                special.runEffects(team, target.x, target.y, (int) (Time.time + team.id * 17), fleet.units);
                return;
            }
            EventInterventionAction.spawnJumpIn(
                    team, target.x, target.y, fleet.spawnRange, fleet.spawnReloadTime, fleet.spawnDelay,
                    fleet.status, fleet.statusDuration, fleet.flag, fleet.units, (int) (Time.time + team.id * 17)
            );
        }

        public void showLabel() {
            NHUIFunc.showToast(NHContent.fleet,
                    Core.bundle.format("nh.cutscene.event.fleet-popup", (int) (target.x / tilesize), (int) (target.y / tilesize)),
                    NHSounds.uiAlert1, state.rules.waveTeam.color);
            labelShown = true;
        }

        public void showAlert() {
            updatePosition();
            resolveFleet();

            iconShown = true;
            spawned = false;

            float range = fleet != null ? fleet.spawnRange / tilesize : 12f;
            if (!headless) {
                showFleetAlertHud(timer.name, alertTime.numf(), range, target.x, target.y);
            } else {
                state.rules.objectives.each(mapObjective -> {
                    if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timer.name)) {
                        obj.trigger(alertTime.numf() * Time.toSeconds);
                    }
                });
            }
        }
    }
}
