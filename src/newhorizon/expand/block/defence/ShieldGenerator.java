package newhorizon.expand.block.defence;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.ControlBlock;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class ShieldGenerator extends BaseTurret {
    public final int timerUse = timers++;

    public float shieldArc = 70f;
    public float shieldRange = 250f;

    public float shieldHealth = 150000f;
    public float recoverSpeed = 5000f/60f;

    public float powerCons = 20000/60f;

    public float elevation = -1f;
    public TextureRegion turret;

    public ShieldGenerator(String name) {
        super(name);
        configurable = true;

        saveConfig = true;
        update = true;
        solid = true;
        group = BlockGroup.projectors;
        hasPower = true;
        hasItems = true;
        envEnabled |= Env.space;
        ambientSound = NHSounds.largeBeam;
        ambientSoundVolume = 0.08f;
        canOverdrive = false;

        rotateSpeed = 0.1f;
        range = 265f;

        size = 5;
        clipSize = 600f;

        health = 12000;
        armor = 32f;

        consumePower(powerCons);
        consumeItem(NHItems.ancimembrane, 2);

        config(Float.class, (ShieldGeneratorBuild build, Float rotation) -> {
            build.targetAngel = rotation;
        });
        configClear((ShieldGeneratorBuild build) -> {
            build.targetAngel = 90;
        });
    }

    @Override
    public void init() {
        super.init();
        if(elevation < 0) elevation = size / 2f;
    }

    @Override
    public void load() {
        super.load();
        turret = Core.atlas.find(name + "-turret");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, turret};
    }

    @Override
    public void drawPlan(BuildPlan plan, Eachable<BuildPlan> list, boolean valid) {
        super.drawPlan(plan, list, valid);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("riftShield", (ShieldGeneratorBuild entity) -> new Bar("stat.shieldhealth", Pal.accent, () -> 1f - entity.buildup / (shieldHealth)).blink(Color.white));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.shieldHealth, shieldHealth);
        stats.add(Stat.cooldownTime, "10s");
    }

    public class ShieldGeneratorBuild extends BaseTurretBuild implements ControlBlock{
        public @Nullable BlockUnitc unit;

        protected Rand rand = new Rand(id);
        protected Rect rect = new Rect();

        public boolean broken = true;
        public float buildup, radscl, hit, warmup;
        public float targetAngel = 90f;

        public Seq<Bullet> absorbedBullets = new Seq<>();

        public Vec2 backCenter;
        public Vec2 rightCenter;
        public Vec2 leftCenter;

        @Override
        public void created() {
            super.created();

            backCenter = new Vec2(x - Geometry.d4x(rotation()) * size * tilesize, y - Geometry.d4y(rotation()) * size * tilesize);
            rightCenter = backCenter.cpy().add(Angles.trnsx(rotation - shieldArc/2, 250), Angles.trnsy(rotation - shieldArc/2, 250));
            leftCenter = backCenter.cpy().add(Angles.trnsx(rotation + shieldArc/2, 250), Angles.trnsy(rotation + shieldArc/2, 250));
        }

        public Float config(){
            return targetAngel;
        }


        @Override
        public void buildConfiguration(Table table) {
            if (state.isEditor()){
                table.slider(0, 360, 45, rotation, f -> rotation = targetAngel = f).growX().row();
            }else {
                table.slider(0, 360, 45, targetAngel, f -> targetAngel = f).growX().row();
            }
        }

        public float getPercentage(){
            return Mathf.pow((shieldHealth + buildup) / shieldHealth, 2);
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            Draw.color();
            Draw.z(Layer.turret);
            Drawf.shadow(turret, x - elevation, y - elevation, rotation - 90);
            Draw.rect(turret, x, y, rotation - 90);
            if (!broken & radscl > 0){
                drawShield();
            }

        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range(), team.color);
            Drawf.dashLine(team.color, x, y, leftCenter.x, leftCenter.y);
            Drawf.dashLine(team.color, x, y, rightCenter.x, rightCenter.y);
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
        }

        @Override
        public void update() {
            super.update();

            updateLerpAngel();
            updateConsume();
            updateShield();
            updateBullet();
            updateUnit();


            if (hit > 1){
                hit = 1;
                hit -= 1/60f;
            }
        }

        private void updateLerpAngel(){
            if(unit != null && unit.isShooting()){
                targetAngel = Angles.angle(x, y, unit.aimX(), unit.aimY());
            }
            rotation = Angles.moveToward(rotation, targetAngel, rotateSpeed * efficiency);

            backCenter = new Vec2(x - Angles.trnsx(rotation - 180, 16), y - Angles.trnsy(rotation - 180, 16));
            rightCenter = backCenter.cpy().add(Angles.trnsx(rotation - shieldArc/2, 250), Angles.trnsy(rotation - shieldArc/2, 250));
            leftCenter = backCenter.cpy().add(Angles.trnsx(rotation + shieldArc/2, 250), Angles.trnsy(rotation + shieldArc/2, 250));

        }
        private void updateConsume(){
            if(!broken && timer(timerUse, 60f / getPercentage()) && efficiency > 0f){
                consume();
            }
        }
        private void updateShield(){
            radscl = Mathf.lerpDelta(radscl, broken ? 0f : warmup, 0.025f);
            warmup = Mathf.lerpDelta(warmup, efficiency, 0.05f);

            if(buildup > 0){
                buildup -= delta() * recoverSpeed * (broken? 2f: 1f);
            }

            if(broken && buildup <= 0){
                broken = false;
            }

            if(buildup >= shieldHealth && !broken){
                broken = true;
                buildup = shieldHealth;
                if(team != state.rules.defaultTeam){
                    Events.fire(EventType.Trigger.forceProjectorBreak);
                }
                drawShieldBreakFx();
            }

            if(hit > 0f){
                hit -= 1f / 5f * Time.delta;
            }
        }
        private void updateBullet(){
            //todo reflect bullet from inner
            if (!broken && efficiency > 0.01f){
                Groups.bullet.intersect(
                    backCenter.x - range, backCenter.y - range, range * 2, range * 2, bullet -> {
                        float chance = (2000 - bullet.damage) / 2000 * 0.8f + 0.2f;
                        float dst = Mathf.dst(backCenter.x, backCenter.y, bullet.x, bullet.y);
                        float angel = Angles.angle(backCenter.x, backCenter.y, bullet.x, bullet.y);
                        boolean in = Angles.within(rotation, angel, shieldArc/2);
                        if (dst < 260 && dst > 220 && bullet.team != team && in) {
                            float bAng = bullet.vel.angle() + 180f;
                            float nAng = Tmp.v1.set(bullet.x - backCenter.x, bullet.y - backCenter.y).angle();
                            if (Mathf.chance(chance)){
                                bullet.vel.rotate((nAng - bAng) * 2 + 180);
                                bullet.team(team);
                            }else {
                                bullet.absorb();
                            }
                            hit += bullet.damage / recoverSpeed;
                            buildup += bullet.damage;
                            NHFx.sharpBlastRand(team.color, Color.white, nAng, 90f, 65, Mathf.clamp(bullet.damage / 1200) * 100f).at(bullet);
                        }
                    }
                );
            }
        }
        private void updateUnit(){
            if (!broken && efficiency > 0.01f){
                Units.nearbyEnemies(team, backCenter.x, backCenter.y, range, unit -> {
                    float dst = Mathf.dst(backCenter.x, backCenter.y, unit.x, unit.y);
                    float angel = Angles.angle(backCenter.x, backCenter.y, unit.x, unit.y);
                    boolean in = Angles.within(rotation, angel, shieldArc/2);
                    if (dst < 260 && dst > 220 && in) {
                        Vec2 vec2 = new Vec2(unit.x - backCenter.x, unit.y - backCenter.y);
                        unit.apply(NHStatusEffects.emp3);
                        unit.vel.setZero();
                        unit.move(vec2.setLength(0.5f));
                        hit += unit.hitSize * 4 / recoverSpeed;
                        buildup += unit.hitSize * 4;
                    }
                });
            }
        }

        private void drawShield(){
            float sin1 = (Mathf.sin(Time.time / 30f) * 0.12f + 1f) * radscl;
            float sin2 = (Mathf.sin(Time.time / 30f) * 0.18f + 1.1f) * radscl;

            float xStart = backCenter.x;
            float yStart = backCenter.y;

            Vec2 vec = new Vec2(xStart, yStart);

            if (state.isPlaying() && radscl > 0.01f){
                if (Mathf.chanceDelta(10f/60f)) {
                    PosLightning.createEffect(vec, rightCenter, team.color, 1, 2 * sin1);
                    PosLightning.createEffect(vec, leftCenter, team.color, 1, 2 * sin1);
                }
            }
            drawPulse();
            drawWave();
            drawTransfer();

            Draw.z(Layer.effect);
            Draw.color(team.color, Color.white, Mathf.clamp(hit));
            Fill.circle(xStart, yStart, 15f * sin1);
            Fill.circle(rightCenter.x, rightCenter.y, 15f * sin1);
            Fill.circle(leftCenter.x, leftCenter.y, 15f * sin1);
            Lines.stroke(16f * sin1);
            Lines.arc(backCenter.x, backCenter.y, 250, 70f/360f, rotation - 35);

            Draw.z(Layer.effect + 0.0001f);
            Draw.color(Color.black);

            Fill.circle(xStart, yStart, 9f * sin2);
            Fill.circle(rightCenter.x, rightCenter.y, 9f * sin2);
            Fill.circle(leftCenter.x, leftCenter.y, 9f * sin2);
            Lines.stroke(9f * sin2);
            Lines.arc(backCenter.x, backCenter.y, 250, 70f/360f, rotation - 35);

            Draw.color(team.color, Color.white, Mathf.clamp(hit));
            Draw.z(Layer.effect + 0.0002f);
            if (radscl > 0.01f){
                DrawFunc.surround(id, rightCenter.x, rightCenter.y, 22, 6, 4, 6, sin1);
                DrawFunc.surround(id + 1, leftCenter.x, leftCenter.y, 22, 6, 4, 6, sin1);
                DrawFunc.surround(id + 2, backCenter.x, backCenter.y, 22, 8, 3, 5, sin1);
            }

        }
        private void drawPulse(){
            Draw.z(Layer.effect);
            Draw.color(team.color, Color.white, Mathf.clamp(hit));
            Draw.alpha(0.75f);
            float base = (Time.time / 400f / (buildup / shieldHealth * 0.5f + 1f));
            for(int i = 0; i < 60; i++){
                rand.setSeed(id + i);
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;

                float dst = 250f;
                float deg = rotation + (shieldArc/2 - fin * shieldArc) * (rand.chance(0.5f)? -1f : 1f);
                float xShift = Angles.trnsx(deg, dst);
                float yShift = Angles.trnsy(deg, dst);
                Fill.circle(backCenter.x + xShift, backCenter.y + yShift, rand.random(10, 12) * (0.8f + Mathf.sin(Time.time/60 + rand.random(Mathf.PI2)) * 0.2f) * radscl * (buildup / shieldHealth * 0.5f + 1f));
            }
            Draw.alpha(1f);
        }
        private void drawWave(){
            float base = (Time.time / 120f);
            Draw.color(team.color, Color.white, Mathf.clamp(hit));
            Draw.z(Layer.effect);

            for(int i = 0; i < 10; i++){
                Rand rand = new Rand(id + i);
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;

                float deg = rand.random(360f);
                float pos = rand.random(12f);
                float len = rand.random(12, 18);


                Tmp.v1.set(Mathf.sinDeg(deg) * pos, Mathf.cosDeg(deg) * pos);
                Lines.stroke(4f * fout * radscl);
                if (rand.chance(0.5f)) {
                    Lines.circle(leftCenter.x + Tmp.v1.x, leftCenter.y + Tmp.v1.y, len * fin * radscl);
                }else {
                    Lines.circle(rightCenter.x + Tmp.v1.x, rightCenter.y + Tmp.v1.y, len * fin * radscl);
                }
            }
        }
        private void drawTransfer(){
            float base = (Time.time / 250f);
            Draw.color(team.color, Color.white, Mathf.clamp(hit));
            Draw.z(Layer.effect);

            for(int i = 0; i < 30; i++){
                rand.setSeed(id + i);
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;

                float dst = 250f;
                float deg = rotation + rand.random(-shieldArc/2, shieldArc/2);
                float xEnd = Angles.trnsx(deg, dst);
                float yEnd = Angles.trnsy(deg, dst);

                Vec2 pos = backCenter.cpy().add(xEnd, yEnd);

                float ang = Angles.angle(x, y, pos.x, pos.y);
                float start = rand.random(0.2f, 0.4f);

                Tmp.v1.set(x, y);
                Tmp.v1.lerp(pos, fin * start + (1 - start));
                Fill.poly(Tmp.v1.x, Tmp.v1.y, 6, rand.random(5f, 8f) * fin * radscl, ang + rand.random(-30,30));
            }
        }
        private void drawShieldBreakFx(){
            Effect effect1 = new Effect(135, e -> {
                float sin1 = (Mathf.sin(Time.time / 30f) * 0.12f + 1f) * radscl * e.fout();
                float sin2 = (Mathf.sin(Time.time / 30f) * 0.18f + 1.1f) * radscl * e.fout();

                Draw.color(team.color, Color.white, Mathf.clamp(hit));
                Fill.circle(x, y, 22f * sin1);
                Fill.circle(rightCenter.x, rightCenter.y, 15f * sin1);
                Fill.circle(leftCenter.x, leftCenter.y, 15f * sin1);
                Lines.stroke(16f * sin1);
                Lines.arc(backCenter.x, backCenter.y, 250, 70f/360f, rotation - 35);

                Draw.color(Color.black);
                Fill.circle(x, y, 12f * sin2);
                Fill.circle(rightCenter.x, rightCenter.y, 9f * sin2);
                Fill.circle(leftCenter.x, leftCenter.y, 9f * sin2);
                Lines.stroke(9f * sin2);
                Lines.arc(backCenter.x, backCenter.y, 250, 70f/360f, rotation - 35);

                Draw.color(team.color, Color.white, Mathf.clamp(hit));
                DrawFunc.surround(id, rightCenter.x, rightCenter.y, 22, 6, 4, 6, sin1);
                DrawFunc.surround(id + 1, leftCenter.x, leftCenter.y, 22, 6, 4, 6, sin1);
            });
            Effect effect2 = new Effect(80, e -> {
                Draw.color(team.color, Color.white, Mathf.clamp(hit));
                Angles.randLenVectors(id, 120, 250, rotation - shieldArc/2, shieldArc, (x, y) -> {
                    rand.setSeed((long) (e.id + (x + y)));
                    float dst = shieldRange;
                    float deg = rotation + rand.random(-shieldArc/2, shieldArc/2);
                    float xEnd = Angles.trnsx(deg, dst);
                    float yEnd = Angles.trnsy(deg, dst);

                    Vec2 pos = backCenter.cpy().add(xEnd, yEnd);

                    float ang = Angles.angle(x, y, pos.x, pos.y) + rand.random(-30f, 30f);
                    float start = rand.random(-0.6f, 0.6f);

                    Tmp.v1.set(x, y);
                    Tmp.v1.lerp(pos, e.fout(Interp.smooth) * start + (1 - start));
                    Fill.poly(Tmp.v1.x, Tmp.v1.y, 6, rand.random(12f, 18f) * e.fout(Interp.smooth) * radscl, ang + rand.random(-30,30));
                });
            });
            effect1.at(this);
            effect2.at(this);
        }

        @Override
        public Unit unit() {
            if(unit == null){
                unit = (BlockUnitc) UnitTypes.block.create(team);
                unit.tile(this);
            }
            return (Unit)unit;
        }

        public void write(Writes write){
            super.write(write);
            write.bool(broken);
            write.f(buildup);
            write.f(radscl);
            write.f(warmup);
            write.f(rotation);
            write.f(targetAngel);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            broken = read.bool();
            buildup = read.f();
            radscl = read.f();
            warmup = read.f();
            rotation = read.f();
            targetAngel = read.f();
        }
    }
}
