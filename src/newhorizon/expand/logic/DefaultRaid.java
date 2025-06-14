package newhorizon.expand.logic;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.logic.*;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.bullets.raid.BasicRaidBulletType;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.graphic.EffectWrapper;
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
    alertTime = "10", raidTime = "5",
            bulletDamage = "1000", bulletSpeed = "1", bulletCount = "10", inaccuracy = "0";

    public BulletType bulletType = new BasicRaidBulletType() {{
        speed = 7f;
        damage = 1000f;
        lifetime = 200f;

        trailEffect = NHFx.hugeTrail;
        trailParam = 6f;
        trailChance = 0.2f;
        trailInterval = 3;
        trailWidth = 5f;
        trailLength = 55;
        trailInterp = Interp.slope;


        splashDamage = damage;
        splashDamageRadius = 120;
        scaledSplashDamage = true;

        despawnHit = true;
        collides = false;

        shrinkY = shrinkX = 0.33f;
        width = 17f;
        height = 55f;

        despawnShake = hitShake = 12f;
        hitEffect = new MultiEffect(
                NHFx.square(hitColor, 200, 20, splashDamageRadius + 80, 10),
                NHFx.lightningHitLarge,
                NHFx.hitSpark(hitColor, 130, 85, splashDamageRadius * 1.5f, 2.2f, 10f),
                NHFx.subEffect(140, splashDamageRadius + 12, 33, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                    float fout = Interp.pow2Out.apply(1 - fin);
                    for (int s : Mathf.signs) {
                        Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(fin, 0, 0.1f) * NHFx.fout(fin, 0.25f), rot + s * 90);
                    }
                })));
        despawnEffect = NHFx.circleOut(145f, splashDamageRadius + 15f, 3f);
        shootEffect = EffectWrapper.wrap(NHFx.missileShoot, hitColor);
        smokeEffect = NHFx.instShoot(hitColor, frontColor);

        despawnSound = hitSound = Sounds.largeExplosion;
    }};

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

        public DefaultRaidInstruction() {
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
            Team player = state.rules.defaultTeam;
            Team wave = state.rules.waveTeam;

            float wx = Mathf.random(0, world.unitWidth());
            float wy = Mathf.random(0, world.unitHeight());

            float dmg = damage.numf();
            float spd = speed.numf();
            if (spd <= 0f) spd = 1f;

            Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracy.numf() * tilesize));

            Seq<Tile> spawns = spawner.getSpawns();
            if (!spawns.isEmpty()) {
                Tile t = spawns.random();
                Tmp.v2.set(t.worldx(), t.worldy());
            } else {
                Tmp.v2.setZero();
            }

            AtomicReference<BlockFlag> flag = new AtomicReference<>(BlockFlag.core);
            WeightedRandom.random(
                    new WeightedOption(1f, () -> flag.set(BlockFlag.turret)),
                    new WeightedOption(2f, () -> flag.set(BlockFlag.generator)),
                    new WeightedOption(2f, () -> flag.set(BlockFlag.factory)),
                    new WeightedOption(1f, () -> flag.set(BlockFlag.core))
            );
            Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(player, flag.get()));
            if (b != null) {
                Tmp.v3.set(b.x, b.y);
            } else {
                Tmp.v3.setZero();
            }

            float sx = Tmp.v2.x, sy = Tmp.v2.y, tx = Tmp.v3.x, ty = Tmp.v3.y;
            float dst = Mathf.dst(sx, sy, tx, ty);
            float ang = Angles.angle(sx, sy, tx, ty);
            float lifetimeScl = dst / (bulletType.speed * bulletType.lifetime * spd);
            Call.createBullet(bulletType, wave, sx + Tmp.v1.x, sy + Tmp.v1.y, ang, dmg, spd, lifetimeScl);
        }

        public void showAlert() {
            Team wave = state.rules.waveTeam;

            Call.sendMessage("/hud_raid " + wave.id);

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
                if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timer.name)) {
                    obj.trigger((alertTime.numf()) * Time.toSeconds);
                }
            });

            iconShown = true;
            raidCounter = 0;
        }
    }
}
