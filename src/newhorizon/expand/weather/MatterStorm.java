package newhorizon.expand.weather;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.gen.WeatherState;
import mindustry.graphics.Drawf;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Shaders;
import mindustry.type.Weather;
import newhorizon.NHGroups;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.content.NHShaders;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.bullets.TrailFadeBulletType;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.func.NHPixmap;

/**
 * Matter storm weather, including the legacy environmental effects and arc
 * projectile gameplay used by the quantum and solar storms.
 */
public class MatterStorm extends Weather {
    public Color textureColor;
    public Color secondaryColor = mindustry.graphics.Pal.ammo;
    public Color primaryColor = mindustry.graphics.Pal.redderDust;
    public float alphaMin = 0.075f, alphaMax = 0.28f;
    public float alphaScl = 32f;
    public float colorScl = 22f, colorMag = 0.9f;

    /** Gameplay parameters restored from the legacy matter-storm implementation. */
    public boolean rotateBullets;
    public float buildingEmp = -1f;
    public float force = 7f, overload = 1.2f;
    public float sparkEffectChance = 0.125f;
    public Sound noise = NHSounds.shock;
    public float noiseChance = 0.0225f;
    public Effect sparkEffect2 = NHFx.hitSparkLarge;
    public Effect sparkEffect = new Effect(45f, e -> {
        if (!(e.data instanceof Number number)) return;
        float data = number.floatValue();
        Draw.color(e.color, Color.white, e.fout() * 0.53f);
        Lines.stroke(e.fout() * 3f);
        float len = Mathf.clamp(data / 16f, 4f, 20f);
        Rand rand = NHFunc.rand;
        rand.setSeed(e.id);
        Tmp.v1.trns(e.rotation - 180f, data * 1.25f).add(e.x, e.y);
        Angles.randLenVectors(e.id, (int)Mathf.clamp(data / 24f, 4f, 30f),
                e.fin(Interp.pow3Out) * data * 3f, e.rotation, 85f, (x, y) -> {
                    float angle = Mathf.angle(x, y);
                    Lines.lineAngle(Tmp.v1.x + x, Tmp.v1.y + y, angle,
                            e.fin(NHInterp.parabola4Reversed) * len * 0.85f
                                    * rand.random(0.8f, 1.2f) + len * 0.35f * e.fout());
                });
    });

    public float bulletDamage = 120f;
    public float bulletVelocityMin = 0.6f, bulletVelocityMax = 1.4f;
    public float bulletLifeMin = 0.8f, bulletLifeMax = 2f;
    public float bulletSpawnChance = 0.075f;
    public float bulletSpawnNum = 2f;
    public float empScale = 0.75f;
    public BulletType bulletType;

    public MatterStorm(String name) {
        super(name);
        opacityMultiplier = 3f;
        duration = 0.4f * Time.toMinutes;
        alwaysUnlocked = true;
    }

    @Override
    public void init() {
        super.init();
        if (bulletType != null) return;

        bulletType = new TrailFadeBulletType(18f, bulletDamage) {{
            disableAccel();
            width = height = 0f;
            trailRotation = true;
            pierce = true;
            pierceCap = 3;
            hitBlinkTrail = false;
            collidesTiles = false;
            tracerStroke = 4f;
            drawSize = 1600f;
            tracerUpdateSpacing = 1f;
            tracerRandX = 12f;
            tracerSpacing = 12f;
            tracers = 1;
            weaveMag = 2f;
            weaveScale = 12f;
            lifetime = 40f;
            hitColor = lightningColor = frontColor = backColor = trailColor = lightColor =
                    secondaryColor.cpy().lerp(primaryColor, 0.4f);
            lightning = 4;
            lightningLength = 8;
            lightningLengthRand = 13;
            shootEffect = NHFx.instShoot(hitColor, frontColor);
            hitEffect = NHFx.hitSpark(hitColor, 45f, 20, 50f, 2.8f, 12);
            smokeEffect = Fx.smokeCloud;
            trailEffect = Fx.none;
            despawnEffect = NHFx.square45_8_45;
            lightningDamage = damage / 5f;
            hitShake = 8f;
            knockback = 6f;
            addBeginPoint = despawnHit = true;
            hitSound = Sounds.explosion;
            despawnSound = Sounds.explosion;
            hitSoundVolume = 0.2f;
        }

            @Override
            public void hit(Bullet bullet, float x, float y) {
                super.hit(bullet, x, y);
                UltFire.createChance(x, y, splashDamageRadius, 0.2f, bullet.team);
            }

            @Override
            public void draw(Bullet bullet) {
                drawTrail(bullet);
            }

            @Override
            public void init(Bullet bullet) {
                super.init(bullet);
                despawnEffect.at(bullet.x, bullet.y, 0f, hitColor);
                Sounds.shootArc.at(bullet);
            }
        };
    }

    @Override
    public void updateEffect(WeatherState state) {
        float speed = force * state.intensity * Time.delta;
        if (speed > 0.001f && state.effectTimer <= 0f) {
            state.effectTimer = Math.max(statusDuration - 5f, 1f);
            float angle = state.windVector.angle();

            if (!Vars.headless) Vars.renderer.shake(force / 3f, force);
            for (Unit unit : Groups.unit) {
                if (!unit.checkTarget(statusAir, statusGround)) continue;
                unit.hitbox(Tmp.r2);
                if (NHGroups.gravityFields.any(Tmp.r2.x, Tmp.r2.y, Tmp.r2.width, Tmp.r2.height)) continue;
                if (status != null && status != StatusEffects.none) unit.apply(status, statusDuration);
                unit.impulse(Tmp.v1.set(state.windVector)
                        .scl(speed * (unit.isFlying() ? 1f : 0.4f)));
                unit.reloadMultiplier(overload);
                if (Mathf.chanceDelta(sparkEffectChance * Time.delta)) {
                    sparkEffect.at(unit.x, unit.y, angle, getColor(), unit.hitSize);
                }
            }

            if (rotateBullets) {
                for (Bullet bullet : Groups.bullet) {
                    if (!bullet.type.absorbable) continue;
                    bullet.vel().setAngle(Angles.moveToward(
                            bullet.vel().angle(), angle,
                            speed / 500f * bullet.vel().len()));
                    bullet.vel().add(Tmp.v1.set(state.windVector).scl(speed / 220f));
                }
            }

            if (buildingEmp > 0f) {
                Groups.build.each(Building::isValid, building -> {
                    if (building.block.hasPower) {
                        building.applySlowdown(buildingEmp, statusDuration * 5f);
                    }
                });
            }
        } else {
            state.effectTimer -= Time.delta;
        }

        if (!Vars.net.client() && bulletType != null
                && Mathf.chanceDelta(bulletSpawnChance * state.intensity * 1.25f)) {
            // The legacy implementation emits four arc projectiles per spawn roll.
            for (int i = 0; i < 4; i++) spawnArcBullet(state);
        }

        if (!Vars.headless && noise != null && Mathf.chanceDelta(noiseChance)) {
            noise.at(Mathf.random(Vars.world.unitWidth()), Mathf.random(Vars.world.unitHeight()),
                    Mathf.random(0.9f, 1.1f), Mathf.sqrt(state.intensity) + 1f);
        }
    }

    private void spawnArcBullet(WeatherState state) {
        float x = Mathf.random(Vars.world.unitWidth());
        float y = Mathf.random(Vars.world.unitHeight());
        float angle = state.windVector.angle();
        float maxRange = bulletLifeMax * bulletType.range;
        float life = bulletLifeMax;

        Vars.world.getQuadBounds(Tmp.r1);
        if (!Tmp.r1.contains(Tmp.v1.trns(angle, maxRange).add(x, y))) {
            float edge = y > Vars.world.unitHeight() / 2f
                    ? Vars.world.unitHeight() - y : y;
            life = (edge + Vars.finalWorldBounds)
                    / Math.max(Math.abs(Mathf.sinDeg(angle)), 0.001f)
                    / Math.max(bulletType.range, 0.001f);
        }

        life = Mathf.clamp(life, Math.min(bulletLifeMin, bulletLifeMax), bulletLifeMax);
        bulletType.createNet(Team.derelict, x, y, angle,
                bulletType.damage * (state.intensity + 1f),
                Mathf.random(bulletVelocityMin, bulletVelocityMax),
                Mathf.random(Math.min(bulletLifeMin, life), life));
    }

    @Override
    public void createIcons(MultiPacker packer) {
        TextureRegion region = Core.atlas.find(name, NewHorizon.name("weather-icon"));
        if (NHPixmap.isDebugging() && region != null && region.found()) {
            if (textureColor != null) {
                NHPixmap.addProcessed(name + "-full",
                        NHPixmap.fillColor(Core.atlas.getPixmap(region), textureColor)
                                .outline(Color.valueOf("404049"), 3));
            } else {
                NHPixmap.addProcessed(name + "-full",
                        Pixmaps.outline(Core.atlas.getPixmap(region), Color.valueOf("404049"), 3));
            }
        } else {
            super.createIcons(packer);
        }
    }

    public Color getColor() {
        return Tmp.c1.set(primaryColor).lerp(secondaryColor, Mathf.absin(colorScl, colorMag));
    }

    @Override
    public void drawUnder(WeatherState state) {
        // The storm is an overlay; there is intentionally no under-pass.
    }

    @Override
    public void drawOver(WeatherState state) {
        if (Vars.mobile || Vars.renderer == null || NHShaders.matterStorm == null) return;

        Drawf.light(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f,
                1_000_000f, getColor(), state.opacity);

        Draw.blend();
        float currentAlpha = Draw.getColor().a;
        Draw.color(primaryColor,
                Tmp.c2.set(primaryColor).lerp(secondaryColor, 0.5f).lerp(Color.white, 0.25f),
                secondaryColor, Mathf.absin(colorScl, colorMag));
        Vars.renderer.effectBuffer.begin(Tmp.c1.set(Draw.getColor()).a(
                (alphaMin + Mathf.absin(alphaScl, alphaMax - alphaMin))
                        * currentAlpha * state.opacity));
        Vars.renderer.effectBuffer.end();
        Vars.renderer.effectBuffer.blit(Shaders.screenspace);

        if (!NHSetting.enableDetails()) {
            Draw.reset();
            Draw.blend();
            return;
        }

        Draw.blend();
        Vars.renderer.effectBuffer.begin(Color.clear);
        Vars.renderer.effectBuffer.end();

        NHShaders.matterStorm.primaryColor.set(Tmp.c1.set(primaryColor).a(state.opacity));
        NHShaders.matterStorm.applyDirection(state.windVector, state.intensity);
        NHShaders.matterStorm.secondaryColor.set(Tmp.c1.set(secondaryColor)
                .lerp(Color.white, Mathf.absin(8f, 0.4f)).a(state.opacity * currentAlpha));
        Vars.renderer.effectBuffer.blit(NHShaders.matterStorm);
        Draw.reset();
        Draw.blend();
    }
}
