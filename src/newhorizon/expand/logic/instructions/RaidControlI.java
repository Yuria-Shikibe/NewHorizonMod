package newhorizon.expand.logic.instructions;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Call;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.ui.Styles;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.util.ui.NHUIFunc;

import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidControlI implements LExecutor.LInstruction {
    public LVar flag, timer, alertTime, raidTime, team, type, count, sourceX, sourceY, targetX, targetY, inaccuracy;

    public int raidCounter = 0;
    public float curTime;
    public boolean iconShown = false;

    public RaidControlI(LVar flag, LVar timer, LVar alertTime, LVar raidTime, LVar team, LVar type, LVar count, LVar sourceX, LVar sourceY, LVar targetX, LVar targetY, LVar inaccuracy) {
        this.flag = flag;
        this.timer = timer;
        this.alertTime = alertTime;
        this.raidTime = raidTime;

        this.team = team;
        this.type = type;
        this.count = count;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.inaccuracy = inaccuracy;
    }

    public RaidControlI() {
    }

    @Override
    public void run(LExecutor exec) {
        if (!state.rules.objectiveFlags.contains(flag.name)) {
            exec.counter.numval--;
            exec.yield = true;
            return;
        }

        float totalTime = alertTime.numf() + raidTime.numf();
        if (curTime >= totalTime) {
            reset();
        } else {
            exec.counter.numval--;
            exec.yield = true;
            curTime += Time.delta / 60f;
            if (!iconShown) showAlert();
            if (curTime > alertTime.numf()) {
                float raidTimer = curTime - alertTime.numf();
                int raidCount = Mathf.round((raidTimer / raidTime.numf()) * count.numi());
                int raid = raidCount - raidCounter;
                raidCounter = raidCount;

                for (int i = 0; i < raid; i++) {
                    createBullet();
                }
            }
        }
    }

    public void reset() {
        curTime = 0f;
        iconShown = false;
        state.rules.objectiveFlags.remove(flag.name);
    }

    public void createBullet() {
        Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracy.numf() * tilesize));
        float sx = sourceX.numf() * tilesize, sy = sourceY.numf() * tilesize, tx = targetX.numf() * tilesize, ty = targetY.numf() * tilesize;
        float dst = Mathf.dst(sx, sy, tx, ty);
        float ang = Angles.angle(sx, sy, tx, ty);
        float lifetimeScl = dst / (bulletType().speed * bulletType().lifetime);
        Call.createBullet(bulletType(), team.team(), sx + Tmp.v1.x, sy + Tmp.v1.y, ang, -1f, 1f, lifetimeScl);
    }

    public void showAlert() {
        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(team.team().color);
                t2.image(NHContent.raid).fill().color(team.team().color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(team.team().color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>")).color(team.team().color).padBottom(4).row()).growX().fillY();
        });

        NHSounds.alert2.play();

        state.rules.objectives.each(mapObjective -> {
            if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timer.name)) {
                obj.trigger((alertTime.numf()) * Time.toSeconds);
            }
        });

        iconShown = true;
        raidCounter = 0;
    }

    public BulletType bulletType() {
        if (type.numi() < 10000) {
            return switch (type.numi()) {
                case 1 -> RaidBullets.raidBullet_1;
                case 2 -> RaidBullets.raidBullet_2;
                case 3 -> RaidBullets.raidBullet_3;
                case 4 -> RaidBullets.raidBullet_4;
                case 5 -> RaidBullets.raidBullet_5;
                case 6 -> RaidBullets.raidBullet_6;
                case 7 -> RaidBullets.raidBullet_7;
                case 8 -> RaidBullets.raidBullet_8;
                default -> NHBullets.railGun1;
            };
        }
        if (content.bullet(type.numi() - 10000) != null) return content.bullet(type.numi() - 10000);
        return content.bullet(0);
    }
}
