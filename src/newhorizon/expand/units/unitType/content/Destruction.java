package newhorizon.expand.units.unitType.content;

import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.RailBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;
import mindustry.world.meta.BlockFlag;
import newhorizon.NewHorizon;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.ability.active.RepulsionWaveAbility;
import newhorizon.expand.ability.passive.AccumulateAccelerate;
import newhorizon.expand.units.ai.SniperAI;
import newhorizon.expand.units.unitType.NHUnitType;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;

public class Destruction extends NHUnitType {
    public BulletType gauss = new RailBulletType(){{
        length = 600f;
        damage = 300f;

        hitColor = Pal.techBlue;
        hitEffect = endEffect = Fx.hitBulletColor;
        pierceDamageFactor = 0.4f;

        status = NHStatusEffects.emp1;
        statusDuration = 180f;

        smokeEffect = Fx.colorSpark;

        endEffect = new Effect(16f, e -> {
            color(e.color);
            Drawf.tri(e.x, e.y, e.fout() * 1.5f, 6f, e.rotation);
        });

        shootEffect = new Effect(16f, e -> {
            color(e.color);
            float w = 1.2f + 4 * e.fout();

            Drawf.tri(e.x, e.y, w, 30f * e.fout(), e.rotation);
            color(e.color);

            for(int i : Mathf.signs){
                Drawf.tri(e.x, e.y, w * 0.9f, 22f * e.fout(), e.rotation + i * 60f);
            }

            Drawf.tri(e.x, e.y, w, 4f * e.fout(), e.rotation + 180f);
        });

        lineEffect = new Effect(25f, 1200,e -> {
            if(!(e.data instanceof Vec2)) return;

            Vec2 v = (Vec2)e.data;

            color(e.color);
            stroke((e.fout() + 0.5f) * 2f);

            Fx.rand.setSeed(e.id);
            for(int i = 0; i < 40; i++){
                Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
            }

            e.scaled(16f, b -> {
                stroke(b.fout() * 3f);
                color(e.color);
                Lines.line(e.x, e.y, v.x, v.y);
            });
        });
    }

        @Override
        public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
            super.hitTile(b, build, x, y, initialHealth, direct);

            build.applySlowdown(0.25f, 180f);
        }
    };

    public Destruction() {
        super("destruction");

        outlineColor = OColor;

        aiController = SniperAI::new;

        weapons.addAll(
                new Weapon(NewHorizon.name("gauss-cannon")){{
                    alternate = mirror = top = rotate = true;
                    rotationLimit = 10f;

                    x = 15f;
                    y = 2f;
                    recoil = 3f;
                    shootCone = 20f;
                    reload = 25f;
                    shoot = new ShootPattern();
                    inaccuracy = 6f;
                    shake = 5f;
                    shootY = 5f;
                    ejectEffect = Fx.none;
                    predictTarget = false;
                    bullet = gauss;

                    shootSound = NHSounds.coil1;
                }},
                new Weapon(NewHorizon.name("gauss-cannon")){{
                    alternate = mirror = top = rotate = true;
                    rotationLimit = 30f;

                    x = 30f;
                    y = -5f;
                    recoil = 3f;
                    shootCone = 15f;
                    reload = 25f;
                    inaccuracy = 6f;
                    shake = 5f;
                    shootY = 5f;
                    ejectEffect = Fx.none;
                    predictTarget = false;
                    bullet = gauss;

                    shootSound = NHSounds.laser3;
                }}
        );

        armor = 15.0f;
        health = 15000.0f;
        speed = 3f;
        rotateSpeed = 1.0f;
        accel = 0.04f;
        drag = 0.02f;
        engineOffset = 13f;
        engineSize = 9f;
        hitSize = 36.0f;
        buildBeamOffset = 15f;
        ammoCapacity = 800;

        flying = true;
        drawShields = false;
        lowAltitude = true;
        singleTarget = false;

        abilities.add(
            new RepairFieldAbility(500f, 160f, 240f){{
                healEffect = NHFx.healEffectSky;
                activeEffect = NHFx.activeEffectSky;
            }},
            //new RepulsionWaveAbility()
            new AccumulateAccelerate()
        );

        trailLength = 30;
        trailScl = 0.8f;

        targetFlags = new BlockFlag[]{BlockFlag.turret, BlockFlag.factory, BlockFlag.reactor, BlockFlag.generator, BlockFlag.core, null};
    }
}
