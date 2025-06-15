package newhorizon.expand.logic;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.logic.*;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.net.NHCall;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;
import newhorizon.util.ui.NHUIFunc;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class DefaultRaid extends LStatement {
    public String
            //objective flag this statement using
            flag = "event-executor", timer = "event-timer",
            //alert time - objective timer
            //raid time - the time raid lasts
            alertTime = "30", raidTime = "5",
            bulletDamage = "500", bulletSpeed = "1", bulletCount = "10", inaccuracy = "30";

    public Vec2 source = new Vec2(), target = new Vec2();

    public DefaultRaid(String[] tokens) {
        try {
            flag = tokens[1];
            timer = tokens[2];
            alertTime = tokens[3];
            raidTime = tokens[4];
            bulletDamage = tokens[5];
            bulletSpeed = tokens[6];
            bulletCount = tokens[7];
            inaccuracy = tokens[8];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.err(e);
        }
    }

    public DefaultRaid() {
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
            t.add("(s), Raid Time: ");
            fields(t, raidTime, str -> raidTime = str);
            t.add("(s)");
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Bullet Damage  : ");
            fields(t, bulletDamage, str -> bulletDamage = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Bullet Speed  : ");
            fields(t, bulletSpeed, str -> bulletSpeed = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Raid Count: ");
            fields(t, bulletCount, str -> bulletCount = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Inaccuracy Radius: ");
            fields(t, inaccuracy, str -> inaccuracy = str);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHContent.nhwproc;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("defaultraid").append(" ");
        builder.append(flag).append(" ");
        builder.append(timer).append(" ");
        builder.append(alertTime).append(" ");
        builder.append(raidTime).append(" ");
        builder.append(bulletDamage).append(" ");
        builder.append(bulletSpeed).append(" ");
        builder.append(bulletCount).append(" ");
        builder.append(inaccuracy).append(" ");
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultRaidInstruction(
                builder.var(flag),
                builder.var(timer),
                builder.var(alertTime),
                builder.var(raidTime),
                builder.var(bulletDamage),
                builder.var(bulletSpeed),
                builder.var(bulletCount),
                builder.var(inaccuracy)
        );
    }

    public class DefaultRaidInstruction implements LExecutor.LInstruction {
        public LVar flag, timer, alertTime, raidTime, damage, speed, count, inaccuracy;

        public int raidCounter = 0;
        public float curTime;
        public boolean iconShown = false;
        public boolean labelShown = false;
        public int threatLevel = 0;


        public DefaultRaidInstruction(LVar flag, LVar timer, LVar alertTime, LVar raidTime, LVar damage, LVar speed, LVar count, LVar inaccuracy) {
            this.flag = flag;
            this.timer = timer;
            this.alertTime = alertTime;
            this.raidTime = raidTime;

            this.damage = damage;
            this.speed = speed;
            this.count = count;
            this.inaccuracy = inaccuracy;
        }

        public DefaultRaidInstruction() {}

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
                    if (!labelShown) showLabel();

                    float raidTimer = curTime - alertTime.numf();
                    int raidCount = Mathf.round((raidTimer / raidTime.numf()) * count.numi() * threatScl());
                    int raid = raidCount - raidCounter;
                    raidCounter = raidCount;

                    for (int i = 0; i < raid; i++) {
                        createBullet();
                    }
                }
            }
        }
        
        public void updatePosition(){
            float wx = Mathf.random(0, world.unitWidth());
            float wy = Mathf.random(0, world.unitHeight());

            Seq<Tile> spawns = spawner.getSpawns();
            if (!spawns.isEmpty()) {
                Tile t = spawns.random();
                source.set(t.worldx(), t.worldy());
            } else {
                source.setZero();
            }

            AtomicReference<BlockFlag> flag = new AtomicReference<>(BlockFlag.core);
            WeightedRandom.random(
                    new WeightedOption(3f, () -> flag.set(BlockFlag.turret)),
                    new WeightedOption(3f, () -> flag.set(BlockFlag.generator)),
                    new WeightedOption(3f, () -> flag.set(BlockFlag.factory)),
                    new WeightedOption(1f, () -> flag.set(BlockFlag.core))
            );
            Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(state.rules.waveTeam, flag.get()));
            if (b == null) b = state.rules.defaultTeam.core();
            if (b != null) {
                target.set(b.x, b.y);
            } else {
                target.setZero();
            }

            threatLevel = Math.max(ThreatLevel.getTeamThreat(state.rules.defaultTeam), 1);
        }

        public void reset() {
            curTime = 0f;
            iconShown = false;
            labelShown = false;
            state.rules.objectiveFlags.remove(flag.name);
        }

        public float threatScl(){
            return Mathf.sqrt(threatLevel);
        }

        public void createBullet() {
            BulletType bulletType = NHBullets.raidBulletType;
            
            float dmg = damage.numf() * threatScl();
            float spd = speed.numf();
            if (spd <= 0f) spd = 1f;

            Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracy.numf() * tilesize));
            
            float sx = source.x, sy = source.y, tx = target.x, ty = target.y;
            float dst = Mathf.dst(sx, sy, tx, ty);
            float ang = Angles.angle(sx, sy, tx, ty);
            float lifetimeScl = dst / (bulletType.speed * bulletType.lifetime * spd);
            Call.createBullet(bulletType, state.rules.waveTeam, sx + Tmp.v1.x, sy + Tmp.v1.y, ang, dmg, spd, lifetimeScl);
        }

        public void showLabel(){
            NHCall.alertToastTable(1, 1, "[#ff7b69]Raid: []" + "<" + (int)(target.x / tilesize) + "," + (int)(target.y / tilesize) + ">");

            labelShown = true;
        }

        public void showAlert() {
            updatePosition();

            iconShown = true;
            raidCounter = 0;

            NHCall.warnHudPacket(timer.name, alertTime.numf(), inaccuracy.numf(), source.x, source.y, target.x, target.y);
        }
    }

    public static void clientAlertHud(String timerName, float time, float range, float sx, float sy, float tx, float ty) {
        Team wave = state.rules.waveTeam;

        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(wave.color);
                t2.image(NHContent.raid).fill().color(wave.color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(wave.color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>")).color(wave.color).padBottom(4).row()).growX().fillY();
        });

        NHSounds.alert2.play();

        state.rules.objectives.each(mapObjective -> {
            if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timerName)) {
                obj.trigger(time * Time.toSeconds);
                for (MapObjectives.ObjectiveMarker marker: obj.markers) {
                    if (marker instanceof RaidIndicator idc){
                        idc.init(wave.id, 1, range * tilesize, timerName)
                                .setPosition(Tmp.v2.set(sx, sy), Tmp.v3.set(tx, ty));
                    }
                }
            }
        });
    }
}
