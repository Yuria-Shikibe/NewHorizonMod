package newhorizon.expand.logic.wproc;

import arc.Core;
import arc.flabel.FLabel;
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
import newhorizon.content.NHContent;
import newhorizon.content.NHLogic;
import newhorizon.content.NHSounds;
import newhorizon.expand.game.DefaultIntervention;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.SpecialEvent;
import newhorizon.util.ui.NHUIFunc;

import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class DefaultSpecialEvent extends LStatement {
    public String flag = "special-executor", timer = "special-timer", alertTime = "0", eventId = "100";
    public String targetX = "0", targetY = "0";

    public Vec2 target = new Vec2();

    public DefaultSpecialEvent(String[] tokens) {
        try {
            flag = tokens[1];
            timer = tokens[2];
            alertTime = tokens[3];
            eventId = tokens[4];
            if (tokens.length > 5) targetX = tokens[5];
            if (tokens.length > 6) targetY = tokens[6];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.err(e);
        }
    }

    public DefaultSpecialEvent() {
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
            t.add("(s, 0=preset)");
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Special Event Id: ");
            fields(t, eventId, str -> eventId = str).width(120);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Target X/Y (0=auto): ");
            fields(t, targetX, str -> targetX = str).width(90);
            fields(t, targetY, str -> targetY = str).width(90);
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
        builder.append("defaultspecialevent").append(" ");
        builder.append(flag).append(" ");
        builder.append(timer).append(" ");
        builder.append(alertTime).append(" ");
        builder.append(eventId).append(" ");
        builder.append(targetX).append(" ");
        builder.append(targetY).append(" ");
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultSpecialEventInstruction(
                builder.var(flag),
                builder.var(timer),
                builder.var(alertTime),
                builder.var(eventId),
                builder.var(targetX),
                builder.var(targetY)
        );
    }

    public static void showAlertHud(Team team, String timerName, float time, float range, float tx, float ty) {
        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(team.color);
                t2.image(NHContent.fleet).fill().color(team.color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(team.color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.fleet-alert") + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });

        NHSounds.uiAlert1.play();

        state.rules.objectives.each(mapObjective -> {
            if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timerName)) {
                obj.trigger(time * Time.toSeconds);
                for (MapObjectives.ObjectiveMarker marker : obj.markers) {
                    if (marker instanceof RaidIndicator idc) {
                        idc.init(team.id, 2, range * tilesize, timerName)
                                .setPosition(Tmp.v2.set(tx, ty), Tmp.v3.set(tx, ty));
                    }
                }
            }
        });
    }

    public class DefaultSpecialEventInstruction implements LExecutor.LInstruction {
        public LVar flag, timer, alertTime, eventId, targetX, targetY;

        public float curTime;
        public boolean iconShown;
        public boolean labelShown;
        public boolean spawned;
        public SpecialEvent event;
        public Team team = Team.crux;

        public DefaultSpecialEventInstruction(LVar flag, LVar timer, LVar alertTime, LVar eventId, LVar targetX, LVar targetY) {
            this.flag = flag;
            this.timer = timer;
            this.alertTime = alertTime;
            this.eventId = eventId;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        @Override
        public void run(LExecutor exec) {
            if (!state.rules.objectiveFlags.contains(flag.name)) {
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            float alert = resolveAlert();
            if (curTime >= alert) {
                if (!spawned && RaidLogic.isLogicSide()) spawnEvent();
                reset();
            } else {
                exec.counter.numval--;
                exec.yield = true;
                curTime += Time.delta / 60f;
                if (!iconShown) showAlert();
                if (curTime > alert && !labelShown) showLabel();
            }
        }

        private float resolveAlert() {
            float override = alertTime.numf();
            if (override > 0.001f) return override;
            if (event != null) return event.alertTime;
            return 20f;
        }

        public void resolveEvent() {
            event = newhorizon.expand.game.DefaultSpecialEvent.get(eventId.numi());
            if (event != null) team = event.resolveTeam();
        }

        public void updatePosition() {
            float ox = targetX.numf();
            float oy = targetY.numf();
            if (Math.abs(ox) > 0.001f || Math.abs(oy) > 0.001f) {
                target.set(ox * tilesize, oy * tilesize);
                return;
            }

            Team player = state.rules.defaultTeam;
            Team wave = state.rules.waveTeam;
            float[] tile = new float[2];
            if (event != null && event.ally()) {
                tile = DefaultIntervention.pickAllyTarget(player, (int) Time.time + event.id);
            } else {
                tile = DefaultIntervention.pickHostileTarget(wave, player, (int) Time.time + (event == null ? 0 : event.id));
            }

            if (tile[0] == 0f && tile[1] == 0f) {
                Building core = player == null ? null : player.core();
                if (core != null) target.set(core.x, core.y);
                else target.set(world.unitWidth() / 2f, world.unitHeight() / 2f);
            } else {
                target.set(tile[0] * tilesize, tile[1] * tilesize);
            }
        }

        public void reset() {
            curTime = 0f;
            iconShown = false;
            labelShown = false;
            spawned = false;
            event = null;
            state.rules.objectiveFlags.remove(flag.name);
        }

        public void spawnEvent() {
            spawned = true;
            if (event == null) return;
            newhorizon.expand.game.DefaultSpecialEvent.runAt(event, target.x, target.y, (int) (Time.time + team.id * 31));
        }

        public void showLabel() {
            NHUIFunc.showToast(NHContent.fleet,
                    Core.bundle.format("nh.cutscene.event.fleet-popup", (int) (target.x / tilesize), (int) (target.y / tilesize)),
                    NHSounds.uiAlert1, team.color);
            labelShown = true;
        }

        public void showAlert() {
            resolveEvent();
            updatePosition();

            iconShown = true;
            spawned = false;

            float alert = resolveAlert();
            float range = event != null ? event.spawnRange / tilesize : 12f;
            if (!headless) {
                showAlertHud(team, timer.name, alert, range, target.x, target.y);
            } else {
                state.rules.objectives.each(mapObjective -> {
                    if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timer.name)) {
                        obj.trigger(alert * Time.toSeconds);
                    }
                });
            }
        }
    }
}
