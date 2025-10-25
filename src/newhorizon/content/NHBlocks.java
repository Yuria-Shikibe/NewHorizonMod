package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.FlakBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.*;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.MendProjector;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.Prop;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.draw.*;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.Vars;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.content.blocks.*;
import newhorizon.expand.block.ancient.CaptureableTurret;
import newhorizon.expand.block.commandable.AirRaider;
import newhorizon.expand.block.commandable.BombLauncher;
import newhorizon.expand.block.defence.FireExtinguisher;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.block.defence.ShockwaveGenerator;
import newhorizon.expand.block.drawer.ArcCharge;
import newhorizon.expand.block.drawer.DrawArrowSequence;
import newhorizon.expand.block.drawer.FlipRegionPart;
import newhorizon.expand.block.special.HyperGenerator;
import newhorizon.expand.block.special.UnitSpawner;
import newhorizon.expand.block.turrets.MultTractorBeamTurret;
import newhorizon.expand.block.turrets.ShootMatchTurret;
import newhorizon.expand.block.turrets.Webber;
import newhorizon.expand.bullets.*;
import newhorizon.expand.bullets.adapt.AdaptBulletType;
import newhorizon.expand.game.NHPartProgress;
import newhorizon.expand.game.NHUnitSorts;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;
import static mindustry.entities.part.DrawPart.PartProgress.smoothReload;
import static mindustry.type.ItemStack.with;

public class NHBlocks {
    public static Attribute quantum = new Attribute("quantum");
    
    public static Block reinForcedItemSource;
    public static Block reinForcedLiquidSource;
    public static Block hyperGenerator;


    public static Block ancientArtillery, dendrite, interferon, prism, hive, bloodStar, blaster, endOfEra,
            railGun, executor, gravity, multipleLauncher, antibody, atomSeparator, eternity;

    public static Block largeWaterExtractor;
    public static Block antiBulletTurret;
    public static Block fireExtinguisher;
    public static Block webber;
    public static Block hyperspaceWarper;
    public static Block bombLauncher;
    public static Block airRaider;
    public static Block unitIniter;

    public static Block armorClear;
    public static Block quantumField;
    public static Block quantumFieldDeep;
    public static Block quantumFieldDisturbing;
    public static Block metalWall;
    public static Block metalWallQuantum;
    public static Block metalTower;
    public static Block metalGround;
    public static Block metalGroundQuantum;
    public static Block metalScarp;
    public static Block metalVent;
    public static Block metalGroundHeat;
    public static Block conglomerateRock;
    public static Block conglomerateWall;

    private static void loadEnv() {

        metalScarp = new Prop("metal-scrap") {{
            variants = 3;
            breakEffect = new Effect(23, e -> {
                float scl = Math.max(e.rotation, 1);
                Fx.rand.setSeed(e.id);
                randLenVectors(e.id, 6, 19f * e.finpow() * scl, (x, y) -> {
                    color(Tmp.c1.set(e.color).mul(1 + Fx.rand.range(0.125f)));
                    Fill.square(e.x + x, e.y + y, e.fout() * 3.5f * scl + 0.3f);
                });
            }).layer(Layer.debris);
            breakSound = NHSounds.metalWalk;
        }};

        metalWall = new StaticWall("metal-unit-quantum") {{
            variants = 6;
        }};

        metalWallQuantum = new StaticWall("metal-unit") {{
            variants = 6;
        }};

        Cons<Floor> quantumSetter = f -> {
            f.wall = metalWall;
            f.attributes.set(Attribute.water, -1f);
            f.attributes.set(Attribute.oil, -1f);
            f.attributes.set(Attribute.heat, 0);
            f.attributes.set(Attribute.light, 0);
            f.attributes.set(Attribute.spores, -1f);
            f.walkSound = NHSounds.metalWalk;
            f.walkSoundVolume = 0.05f;
            f.speedMultiplier = 1.25f;
            f.decoration = metalScarp;
        };

        conglomerateWall = new StaticWall("conglomerate-wall") {{
            variants = 4;
        }};

        conglomerateRock = new Floor("conglomerate-rock", 3) {{
            blendGroup = Blocks.stone;
            wall = conglomerateWall;
        }};

        metalGroundHeat = new Floor("metal-ground-heat", 3) {{
            wall = metalWall;
            attributes.set(quantum, 3f);
            attributes.set(Attribute.water, -1f);
            attributes.set(Attribute.oil, -1f);
            attributes.set(Attribute.heat, 1.25f);
            attributes.set(Attribute.light, 1f);
            attributes.set(Attribute.spores, -1f);
            walkSound = NHSounds.metalWalk;
            walkSoundVolume = 0.05f;
            speedMultiplier = 1.25f;

            liquidMultiplier = 0.8f;
            liquidDrop = NHLiquids.quantumLiquid;
            lightColor = NHColor.darkEnrColor;
            emitLight = true;
            lightRadius = 35f;

            decoration = metalScarp;
        }};

        quantumField = new Floor("quantum-field", 8) {{
            status = NHStatusEffects.quantization;
            statusDuration = 60f;
            speedMultiplier = 1.15f;
            liquidDrop = NHLiquids.quantumLiquid;
            liquidMultiplier = 0.25f;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            attributes.set(Attribute.light, 2f);
            emitLight = true;
            lightRadius = 32f;
            lightColor = NHColor.darkEnrColor.cpy().lerp(Color.black, 0.1f);
            blendGroup = this;

            attributes.set(quantum, 1f);
            attributes.set(Attribute.heat, 0.05f);
            attributes.set(Attribute.water, -1f);
            attributes.set(Attribute.oil, -1f);
            attributes.set(Attribute.spores, -1f);

//			cacheLayer = NHContent.quantum;
        }};

        quantumFieldDeep = new Floor("quantum-field-deep", 0) {{
            drownTime = 180f;
            status = NHStatusEffects.quantization;
            statusDuration = 240f;
            speedMultiplier = 1.3f;
            liquidDrop = NHLiquids.quantumLiquid;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            attributes.set(Attribute.light, 3f);
            emitLight = true;
            lightRadius = 40f;
            liquidMultiplier = 0.5f;
            lightColor = NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f);
            blendGroup = this;

            attributes.set(quantum, 1.5f);
            attributes.set(Attribute.heat, 0.15f);
            attributes.set(Attribute.water, -1f);
            attributes.set(Attribute.oil, -1f);
            attributes.set(Attribute.spores, -1f);
        }};

        quantumFieldDisturbing = new Floor("quantum-field-disturbing", 0) {
            {
                drownTime = 180f;
                status = NHStatusEffects.quantization;
                statusDuration = 240f;
                speedMultiplier = 1.3f;
                liquidDrop = NHLiquids.quantumLiquid;
                isLiquid = true;
                attributes.set(Attribute.light, 3f);
                emitLight = true;
                lightRadius = 40f;
                liquidMultiplier = 0.5f;
                lightColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.2f);
                blendGroup = this;

                wall = NHBlocks.metalWall;

                attributes.set(quantum, 2f);
                attributes.set(Attribute.heat, 0.25f);
                attributes.set(Attribute.water, -1f);
                attributes.set(Attribute.oil, -1f);
                attributes.set(Attribute.spores, -1f);

                cacheLayer = NHContent.quantumLayer;

                details = "Has unique shader.";
            }

            @Override
            public void load() {
                super.load();

                fullIcon = uiIcon = region = Core.atlas.find(NewHorizon.name("quantum-field-disturbing-icon"));
            }
        };

        metalTower = new StaticWall("metal-tower") {{
            variants = 3;
            layer = Layer.blockOver + 1;
        }};

        metalGround = new Floor("metal-ground", 6) {{
            mapColor = Pal.darkerGray;
            quantumSetter.get(this);
        }};

        metalVent = new SteamVent("metal-vent") {{
            variants = 2;
            parent = blendGroup = NHBlocks.metalGround;
            attributes.set(NHContent.quantum, 1f);

            effectColor = Pal.darkerMetal;

            effect = new OptionalMultiEffect(new Effect(140f, e -> {
                color(e.color, NHColor.darkEnr, e.fin() * 0.86f);

                Draw.alpha(e.fslope() * 0.78f);

                float length = 3f + e.finpow() * 10f;
                Fx.rand.setSeed(e.id);
                for (int i = 0; i < Fx.rand.random(3, 5); i++) {
                    Fx.v.trns(Fx.rand.random(360f), Fx.rand.random(length));
                    Fill.circle(e.x + Fx.v.x, e.y + Fx.v.y, Fx.rand.random(1.2f, 3.5f) + e.fslope() * 1.1f);
                }
            }).layer(Layer.darkness - 1));
        }};

        metalGroundQuantum = new Floor("metal-ground-quantum", 2) {
            {
                mapColor = Pal.darkerMetal;
                wall = metalWall;
                blendGroup = metalGround;
                attributes.set(Attribute.heat, 0.2f);

                quantumSetter.get(this);
                wall = metalWallQuantum;

                emitLight = true;
                lightColor = NHColor.darkEnrColor;
                lightRadius = 4.4f;
            }

            @Override
            public void load() {
                super.load();
                region = Core.atlas.find(NewHorizon.name("metal-ground1"));
            }
        };
    }

    private static void loadTurrets() {

        hive = new ShootMatchTurret("hive") {{
            size = 4;
            health = 3200;
            armor = 10;

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 300, NHItems.seniorProcessor, 60, NHItems.juniorProcessor, 120, NHItems.presstanium, 200, Items.graphite, 150));
            outlineColor = Pal.darkOutline;

            warmupMaintainTime = 90f;
            shootWarmupSpeed /= 2f;

            reload = 150;
            targetGround = false;

            range = 8 * 70;
            trackingRange = range * 1.4f;
            xRand = 6f;
            velocityRnd = 0.105f;

            shootY = 8;

            rotateSpeed = 3;
            cooldownTime = 60f;
            recoil = 1.25f;

            Color heatC = Pal.turretHeat.cpy().lerp(Color.red, 0.33f);
            heatColor = heatC;

            shake = 1.1f;
            shootSound = Sounds.missileLarge;

            shoot = new AdaptedShootHelix() {{
                flip = true;
                shots = 10;
                mag = 1.65f;
                scl = 6f;
                shotDelay = 3.5f;
                offset = 9.75f * Mathf.PI2;
                rotSpeedOffset = 0.015f;
                rotSpeedBegin = 0.925f;
            }};

            canOverdrive = true;

            ammo(NHItems.juniorProcessor, new AccelBulletType(5.2f, 75, NHBullets.STRIKE) {{
                        width = 7f;
                        height = 13f;
                        shrinkY = 0f;

                        collideFloor = collidesGround = collidesTiles = false;
                        ammoMultiplier = 8f;
                        backColor = lightningColor = hitColor = Pal.bulletYellowBack;
                        lightColor = frontColor = Pal.bulletYellow;
                        splashDamageRadius = 12f;
                        splashDamage = damage / 3;
                        despawnEffect = Fx.smoke;
                        hitEffect = NHFx.hitSpark;
                        lifetime = 100f;
                        lightningDamage = damage / 2;
                        lightning = 2;
                        lightningLength = 3;
                        lightningLengthRand = 12;

                        status = NHStatusEffects.emp2;
                        statusDuration = 180f;

                        hitSound = despawnSound = Sounds.dullExplosion;
                        hitSoundVolume = 0.6f;
                        hitSoundPitch -= 0.11f;
                        hitShake = 1.1f;

                        shootEffect = EffectWrapper.wrap(Fx.shootBigSmoke2, 180, true);
                        smokeEffect = NHFx.hugeSmokeGray;
                        lightningType = NHBullets.lightningAir;

                        inaccuracy = 0.3f;

                        weaveMag = 3f;
                        weaveScale = 3.55f;
                        homingDelay = 5f;
                        homingPower = 0.25f;
                        homingRange = 160f;

                        velocityBegin = 1.4f;
                        velocityIncrease = 8f;
                        accelerateBegin = 0.005f;
                        accelerateEnd = 0.75f;

                        trailColor = NHColor.trail;
                        trailWidth = 1f;
                        trailLength = 15;
                    }},
                    NHItems.seniorProcessor, new AccelBulletType(5.2f, 200, NHBullets.STRIKE) {{
                        width = 14f;
                        height = 28f;
                        shrinkY = 0f;
                        lightningType = NHBullets.lightningAir;

                        collideFloor = collidesGround = collidesTiles = false;
                        ammoMultiplier = 2f;
                        backColor = lightningColor = hitColor = Pal.bulletYellowBack;
                        lightColor = frontColor = Pal.bulletYellow;
                        splashDamageRadius = 64f;
                        splashDamage = damage / 5;
                        scaledSplashDamage = true;
                        despawnEffect = NHFx.blast(backColor, 64f);
                        lifetime = 142f;
                        hitEffect = NHFx.subEffect(90F, 120F, 18, 60F, Interp.pow2Out, (i, x, y, rot, fin) -> {
                            float fout = 1 - fin;
                            Draw.color(hitColor, Color.white, fin * 0.7f);
                            float f = NHFunc.rand(i).random(5, 8) * Mathf.curve(fin, 0, 0.1f) * fout;
                            Fill.circle(x, y, f);
                            Drawf.light(x, y, f * 1.25f, hitColor, 0.7f);
                        });
                        lightningDamage = damage / 2;
                        lightning = 4;
                        lightningLength = 12;
                        lightningLengthRand = 18;

                        status = NHStatusEffects.emp3;
                        statusDuration = 640f;

                        rangeChange = 80;

                        reloadMultiplier = 0.25f;

                        hitSound = despawnSound = Sounds.largeExplosion;
                        hitShake = despawnShake = 6.1f;

                        shootEffect = EffectWrapper.wrap(NHFx.circleSplash, hitColor);
                        smokeEffect = EffectWrapper.wrap(NHFx.missileShoot, Color.gray.cpy().a(0.84f));

                        inaccuracy = 0.3f;

                        weaveMag = 2f;
                        weaveScale = 8f;
                        homingDelay = 5f;
                        homingPower = 0.325f;
                        homingRange = 320f;

                        velocityBegin = 0.6f;
                        velocityIncrease = 6f;
                        accelerateBegin = 0.005f;
                        accelerateEnd = 0.75f;

                        trailColor = NHColor.trail;
                        trailWidth = 2f;
                        trailLength = 18;
                    }});
            ammoPerShot = 4;
            maxAmmo = 120;

            AdaptedShootHelix shootS = (AdaptedShootHelix) shoot.copy();
            shootS.flip = false;
            shootS.shots = 4;
            shootS.shotDelay = 6;
            shootS.mag /= 2;
            shootS.scl *= 2;
            shootS.offset = shootS.scl * Mathf.PI2;
            shooter(NHItems.juniorProcessor, shoot, NHItems.seniorProcessor, shootS);

            float mainMoveX = 2.5f;
            drawer = new DrawTurret("reinforced-") {{
                parts.add(new RegionPart("-barrel-main") {{
                    mirror = true;
                    moveX = mainMoveX;
                    heatColor = heatC;
                    heatLightOpacity = 0.86f;
                }});

                parts.add(new RegionPart("-charger") {{
                    mirror = true;
                    moveX = mainMoveX;

                    moves.add(new PartMove() {{
                        y = -1f;
                        x = 1f;
                        progress = PartProgress.warmup.compress(0.3f, 0.86f);
                    }});
                }});

                parts.add(new RegionPart("-back") {{
                    mirror = true;

                    moveX = mainMoveX;

                    moves.add(new PartMove() {{
                        y = -1f;
                        x = 1.5f;
                        progress = PartProgress.warmup.compress(0.4f, 0.95f);
                    }});
                }});
            }};

            liquidCapacity = 120;
            coolantMultiplier = 2.4f;
            coolant = new ConsumeLiquid(Liquids.water, 0.5f);

            squareSprite = false;
        }};

        ancientArtillery = new CaptureableTurret("ancient-artillery") {{
            size = 8;
            destructible = false;
            sync = true;

            health = 45000;
            armor = 120;

            lightColor = NHColor.ancientLightMid;
            clipSize = 8 * 24;

            outlineColor = Pal.darkOutline;
            requirements(Category.turret, BuildVisibility.shown, with(NHItems.ancimembrane, 1200, NHItems.seniorProcessor, 300));

            warmupMaintainTime = 90f;
            shootWarmupSpeed /= 5f;
            minWarmup = 0.9f;
            shootCone = 15f;
            rotateSpeed = 0.325f;
            canOverdrive = false;
            ammo(NHItems.fusionEnergy, NHBullets.ancientArtilleryProjectile);
            shooter(NHItems.fusionEnergy, new ShootPattern());

            ammoPerShot = 12;
            maxAmmo = 180;

            range = 1200;
            trackingRange = range * 1.2f;
            reload = 120;

            unitSort = NHUnitSorts.slowest;

            shake = 7;
            recoil = 3;
            shootY = -13.5f;
            shootSound = NHSounds.flak;

            consumePowerCond(5, TurretBuild::isActive);

            enableDrawStatus = false;

            drawer = new DrawTurret() {{
                parts.addAll(
                        new RegionPart("-additional") {{
                            drawRegion = false;
                            heatColor = Color.red;
                            heatProgress = PartProgress.warmup;
                            heatLightOpacity = 0.55f;
                        }},
                        new FlipRegionPart("-armor") {{
                            outline = mirror = true;
                            layerOffset = 0.2f;
                            x = 2f;

                            moveY = 16f;
                            moveX = 8f;
                            moveRot = -45;
                            moves.add(new PartMove(PartProgress.recoil, 0, -6, 0));
                        }},
                        new FlipRegionPart("-back") {{
                            outline = mirror = true;
                            layerOffset = 0.3f;
                            x = 2f;
                            moveY = -2f;
                            moveX = 5f;

                            moves.add(new PartMove(PartProgress.recoil, 0, -4, 0));
                        }},
                        new FlipRegionPart("-cover") {{
                            outline = true;
                            layerOffset = 0.3f;
                            turretHeatLayer = Layer.turretHeat + layerOffset;
                            heatColor = Color.red;
                            heatProgress = PartProgress.warmup;
                            heatLightOpacity = 0.55f;
                        }},
                        new FlipRegionPart("-barrel") {{
                            outline = mirror = true;
                            layerOffset = 0.2f;
                            x = 2f;

                            turretHeatLayer = Layer.turretHeat + layerOffset;
                            heatColor = Color.red;
                            heatProgress = PartProgress.warmup;
                            heatLightOpacity = 0.55f;

                            moves.add(new PartMove(PartProgress.recoil, -1.75f, -8, -2.12f));
                            moveY = 6f;
                            moveX = 7.75f;
                            moveRot = 3.6f;
                        }},
                        new FlipRegionPart("-tail") {{
                            outline = mirror = true;
                            layerOffset = 0.2f;
                            x = 1f;
                            moveY = -4f;
                            moveX = 2f;
                        }},
                        new DrawArrowSequence() {{
                            x = 0;
                            y = 2f;
                            arrows = 9;
                            color = NHColor.ancientLightMid;
                            colorTo = Color.red;
                            colorToFinScl = 0.12f;
                        }},
                        new HaloPart() {{
                            y = -52f;
                            layer = Layer.bullet;
                            color = NHColor.ancient;
                            colorTo = NHColor.ancientLightMid;
                            hollow = true;
                            tri = false;

                            shapes = 1;

                            sides = 16;
                            stroke = -1f;
                            strokeTo = 3.4f;
                            radius = 8f;
                            radiusTo = 14.5f;

                            haloRadius = 0;

                            haloRotateSpeed = 2f;
                        }},
                        new HaloPart() {{
                            y = -52f;
                            layer = Layer.bullet;
                            color = NHColor.ancient;
                            colorTo = NHColor.ancientLightMid;
                            tri = true;
                            shapes = 2;
                            radius = -1;
                            radiusTo = 4.2f;
                            triLength = 6;
                            triLengthTo = 18;

                            haloRadius = 14;
                            haloRadiusTo = 25;
                            haloRotateSpeed = 1.5f;
                        }},
                        new HaloPart() {{
                            y = -52f;
                            layer = Layer.bullet;
                            color = NHColor.ancient;
                            colorTo = NHColor.ancientLightMid;
                            tri = true;
                            shapes = 2;
                            radius = -1;
                            radiusTo = 4.2f;
                            triLength = 0;
                            triLengthTo = 4;

                            haloRadius = 14;
                            haloRadiusTo = 25;
                            shapeRotation = 180;
                            haloRotateSpeed = 1.5f;
                        }},

                        new HaloPart() {{
                            y = -52f;
                            layer = Layer.bullet;
                            color = NHColor.ancient;
                            colorTo = NHColor.ancientLightMid;
                            tri = true;
                            shapes = 2;
                            radius = -1;
                            radiusTo = 5f;
                            triLength = 10;
                            triLengthTo = 24;

                            haloRadius = 15;
                            haloRadiusTo = 28;
                            haloRotateSpeed = -1f;
                        }},
                        new HaloPart() {{
                            y = -52f;
                            layer = Layer.bullet;
                            color = NHColor.ancient;
                            colorTo = NHColor.ancientLightMid;
                            tri = true;
                            shapes = 2;
                            radius = -1;
                            radiusTo = 5f;
                            triLength = 0;
                            triLengthTo = 6;

                            haloRadius = 15;
                            haloRadiusTo = 28;
                            shapeRotation = 180;
                            haloRotateSpeed = -1f;
                        }}
                );
            }};
        }};

        prism = new ItemTurret("prism") {{
            size = 4;
            recoil = 2f;
            health = 6500;

            armor = 25;

            unitSort = UnitSorts.strongest;

            outlineColor = Pal.darkOutline;

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 100, NHItems.seniorProcessor, 120, Items.tungsten, 350, NHItems.zeta, 500));
            shootY = 4f;

            minWarmup = 0.9f;
            warmupMaintainTime = 25f;
            shootWarmupSpeed /= 2f;

            shootSound = Sounds.shootSmite;

            rotateSpeed = 1.1f;

            cooldownTime = 90f;

            squareSprite = false;
            drawer = new DrawTurret("reinforced-") {{
                heatColor = Color.red;

                parts.add(new RegionPart("-top") {{
                    under = turretShading = true;
                    layerOffset = outlineLayerOffset = -0.0015f;
                    heatLayerOffset = -0.0005f;
                }}, new RegionPart("-barrel") {{
                    heatColor = Color.red;
                    mirror = true;
                    layerOffset = 0.001f;
                    outlineLayerOffset = -0.0025f;
                    moveY = 1f;
                    moveX = 1.5f;
                    moveRot = 8f;

                    progress = PartProgress.warmup.blend(smoothReload, 0.5f);

                    children.add(new RegionPart("") {{
                        name = NewHorizon.name("prism-barrel-charger");
                        mirror = true;
                        layerOffset = 0.001f;
                        outlineLayerOffset = -0.0025f;
                        moveX = 0.75f;
                        moveY = -0.75f;
                        progress = PartProgress.warmup;
                    }});

                    progress = NHPartProgress.recoilWarmup;
                }});
            }};

            reload = 420f;
            shootCone = 5f;

            ammoPerShot = 16;
            maxAmmo = 80;

            ammo(NHItems.fusionEnergy,
                    new DelayedPointBulletType() {{
                        width = 15f;
                        damage = 1200;
                        lightningDamage = 40;
                        hitColor = NHColor.lightSkyBack;
                        lightColor = lightningColor = trailColor = NHColor.lightSkyMiddle;
                        rangeOverride = 600;

                        ammoMultiplier = 1;

                        lightning = 3;
                        lightningLength = lightningLengthRand = 8;

                        despawnEffect = NHFx.lightningHitLarge;
                        hitEffect = NHFx.hitSparkHuge;
                        errorCorrectionRadius = 32;

                        status = NHStatusEffects.ultFireBurn;
                        statusDuration = 600f;

                        trailEffect = new Effect(30f, 50f, e -> {
                            Draw.color(e.color, Color.white, e.fout() * 0.6f);

                            float f = e.finpow();
                            Rand rand = Fx.rand;
                            rand.setSeed(e.id + 1);
                            Angles.randLenVectors(e.id, 6, 16, 0, 360, (x, y) -> {
                                Fill.poly(e.x + x * f, e.y + y * f, 3, rand.random(3, 5) * e.fout(), rand.random(360) + rand.random(80, 220) * e.fin(Interp.pow3Out));
                                Drawf.light(e.x + x * f, e.y + y * f, 8, e.color, 0.7f);
                            });
                        });

                        trailSpacing *= 2f;

                        shootEffect = EffectWrapper.wrap(NHFx.shootLine(32f, 12f), hitColor);
                        smokeEffect = EffectWrapper.wrap(NHFx.square45_6_45, hitColor);

//				Vars.content.getByName(ContentType.block, "new-horizon-prism").reload = 12;

                        despawnShake = hitShake = 2f;
                        fragBullets = 2;

                        fragBullet = new ChainBulletType(700) {{
                            length = 0;
                            collidesAir = collidesGround = true;
                            quietShoot = true;
                            hitColor = NHColor.lightSkyBack;
                            lightColor = lightningColor = trailColor = NHColor.lightSkyMiddle;
                            thick = 7.3f;
                            maxHit = 5;
                            hitEffect = NHFx.lightningHitSmall;
                            effectController = (t, f) -> {
                                DelayedPointBulletType.laser.at(f.getX(), f.getY(), thick, hitColor, new Vec2().set(t));
                            };
                        }};
                    }},
                    NHItems.thermoCorePositive, new DelayedPointBulletType() {{
                        width = 15f;
                        damage = 500;
                        splashDamage = 800;
                        splashDamageRadius = 120;
                        lightningDamage = 80;
                        hitColor = NHColor.thermoPst;
                        lightColor = lightningColor = trailColor = NHColor.thermoPst;
                        rangeOverride = 600;

                        ammoMultiplier = 1;
                        lightning = 5;
                        lightningLength = lightningLengthRand = 18;

                        despawnEffect = new OptionalMultiEffect(
                                NHFx.smoothColorCircle(hitColor, splashDamageRadius + 50f, 95f),
                                NHFx.circleOut(95f, splashDamageRadius + 50f, 2),
                                NHFx.spreadOutSpark(160f, splashDamageRadius + 40f, 72, 4, 72f, 13f, 4f, Interp.pow3Out)
                        );
                        hitEffect = NHFx.square45_6_45;

                        status = NHStatusEffects.scrambler;
                        statusDuration = 600f;

                        trailEffect = NHFx.square45_4_45;

                        trailSpacing *= 2f;
                        reloadMultiplier = 0.75f;

                        despawnShake = hitShake = 5f;
                        despawnSound = hitSound = NHSounds.shock;
                        hitSoundVolume = 2;

                        fragBullets = 2;
                        fragBullet = NHBullets.hyperBlastLinker;
                        fragLifeMax = 1.3f;
                        fragLifeMin = 0.6f;
                        fragVelocityMax = 0.55f;
                        fragVelocityMin = 0.2f;

                        shootEffect = EffectWrapper.wrap(NHFx.shootLine(32f, 12f), hitColor);
                        smokeEffect = EffectWrapper.wrap(NHFx.square45_6_45, hitColor);
                    }}
            );

            shake = 4;
            hasLiquids = true;
            liquidCapacity = 80f;
            coolant = new ConsumeCoolant(0.5f);
            coolantMultiplier = 2.25f;
            range = 520f;
            trackingRange = range * 1.4f;
        }};

        interferon = new PowerTurret("interferon") {{
            size = 3;
            recoil = 1f;
            reload = 120f;
            health = 1800;
            armor = 8;

            shoot = new ShootSine() {{
                scl = 16f;
                mag = 8f;
            }};

            shootSound = Sounds.pulseBlast;
            outlineColor = Pal.darkOutline;
            warmupMaintainTime = 45f;
            minWarmup = 0.9f;
            shootWarmupSpeed /= 2f;
            cooldownTime = 65f;

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.presstanium, 250, NHItems.juniorProcessor, 120, Items.beryllium, 90, NHItems.zeta, 300));
            shootY -= 6f;
            shootType = new LightningLinkerBulletType(1.5f, 40) {{
                lifetime = 110;
                keepVelocity = false;

                sprite = NHBullets.MISSILE_LARGE;

                trailColor = lightColor = lightningColor = hitColor = NHColor.ancientLightMid;
                backColor = NHColor.ancientLightMid;
                frontColor = NHColor.ancientLight;

                hitSpacing = 3f;
                hitShake = 1.0F;
                lightningDamage = damage = splashDamage = 45;
                splashDamageRadius = 50f;

                lightning = 1;
                lightningLength = lightningLengthRand = 8;

                effectLightningChance /= 3f;
                effectLightningLength = 17;

                effectLingtning = 1;

                maxHit = 8;
                despawnShake = 5f;
                hitSound = despawnSound = Sounds.plasmaboom;
                statusDuration = 120f;
                status = NHStatusEffects.emp1;

                size = 5f;
                width = 7f;
                height = 22f;
                drawCircle = false;

                trailWidth = 3f;
                trailLength = 16;

                linkRange = 60f;

                scaleLife = false;
                despawnHit = true;

                collidesAir = collidesGround = true;

                shootEffect = EffectWrapper.wrap(NHFx.shootLine(33, 33), hitColor);
                smokeEffect = EffectWrapper.wrap(NHFx.hitSparkHuge, hitColor);

                hitEffect = NHFx.hitSpark;
                despawnEffect = NHFx.blast(hitColor, 42f);

                spreadEffect = EffectWrapper.wrap(NHFx.hitSpark, hitColor);
            }};

            squareSprite = false;
            drawer = new DrawTurret("reinforced-") {{
                parts.add(new RegionPart("-handle") {{
                              x = -5f;
                              moveX = 5f;
                              mirror = true;
                              under = turretShading = true;

                              progress = PartProgress.warmup;
                          }}, new RegionPart("-turret-base") {{
                              under = turretShading = true;
                          }}, new RegionPart("-barrel") {{
                              under = turretShading = true;
                              moveY = -4f;
                              progress = PartProgress.recoil;
                          }}
                );
            }};

            hasLiquids = true;
            coolant = new ConsumeCoolant(0.15f);
            consumePowerCond(12f, TurretBuild::isActive);
            range = 160f;
            trackingRange = range * 1.4f;
            inaccuracy = 1.25f;
        }};

        antibody = new ItemTurret("antibody") {{
            requirements(Category.turret, with(NHItems.presstanium, 220, Items.tungsten, 150, NHItems.juniorProcessor, 80, Items.phaseFabric, 50));

            size = 3;
            health = 1800;
            armor = 10f;

            range = 200f;
            trackingRange = range * 1.4f;

            warmupMaintainTime = 22f;
            shootWarmupSpeed /= 2f;
            minWarmup = 0.9f;

            squareSprite = false;
            drawer = new DrawTurret("reinforced-") {{
                parts.add(new RegionPart("-top") {{
                    under = turretShading = true;
                    outline = true;
                }}, new RegionPart("-side") {{

                    mirror = true;
                    moveY = 2.75f;
                    moveX = 5.5f;
                    moveRot = -45f;
                    progress = PartProgress.warmup;
                }});
            }};

            reload = 60f;

            shoot = new ShootMulti(new ShootPattern(), new ShootPattern() {{
                shots = 2;
                shotDelay = 4f;
            }}, new ShootBarrel() {{
                shots = 4;
                shotDelay = 4f;
                firstShotDelay = 8f;
                barrels = new float[]
                        {5.35f, -14f, -45f, -5.35f, -14f, 45f};
            }});

            rotateSpeed = 3f;
            coolant = consume(new ConsumeLiquid(NHLiquids.quantumLiquid, 10f / 60f));
            coolantMultiplier = 2f;

            inaccuracy = 3f;

            shootSound = NHSounds.laser3;
            outlineColor = Pal.darkOutline;

            ammo(NHItems.zeta, new AccelBulletType(2.85f, 40f) {
                {
                    frontColor = NHColor.ancientLight;
                    backColor = lightningColor = trailColor = hitColor = lightColor = NHColor.ancient;
                    lifetime = 55f;

                    status = NHStatusEffects.emp2;
                    statusDuration = 120f;

                    width = 6f;
                    height = 18f;

                    velocityBegin = 0.35f;
                    velocityIncrease = 7f;
                    accelInterp = NHInterp.inOut;
                    accelerateBegin = 0f;
                    accelerateEnd = 0.775f;

                    homingDelay = 2f;
                    homingPower = 0.035f;
                    homingRange = 120f;

                    despawnHit = true;
                    hitShake = despawnShake = 2f;

                    hitEffect = NHFx.hitSpark;
                    despawnEffect = NHFx.square45_4_45;
                    shootEffect = new Effect(18f, e -> {
                        color(backColor, frontColor, e.fout() * 0.75f);

                        randLenVectors(e.id, 9, e.finpow() * 23f, e.rotation, 20f, (x, y) -> {
                            DrawFunc.arrow(e.x + x, e.y + y, e.fslope() * 1.1f + e.fout() * 1.15f, e.fout() * 5.5f + 0.75f, -e.fout() * 0.6f - 0.2f, Mathf.angle(x, y));
                        });
                    });
                    smokeEffect = Fx.none;

                    trailEffect = NHFx.trailToGray;

                    trailLength = 6;
                    trailWidth = 2f;

                    fragBullets = 1;
                    fragBullet = new ShrapnelBulletType() {{
                        width = 6;
                        length = 60;
                        lifetime = 22f;
                        damage = 70.0F;
                        status = NHStatusEffects.emp2;
                        serrationLenScl = 2f;
                        serrationWidth = 2f;
                        serrations = 3;
                        statusDuration = 120f;
                        fromColor = NHColor.ancientLight;
                        hitColor = lightColor = lightningColor = toColor = NHColor.ancient;
                        shootEffect = NHFx.lightningHitSmall(NHColor.ancient);
                        smokeEffect = NHFx.circleSplash;
                    }};
                }

                public void createFrags(Bullet b, float x, float y) {
                    if (fragBullet != null && (fragOnAbsorb || !b.absorbed)) {
                        float a = b.rotation();
                        for (int i = 0; i < fragBullets; i++) {
                            fragBullet.create(b, x, y, a);
                        }
                    }
                }
            });

            maxAmmo = 40;
        }};

        webber = new Webber("webber") {{
            size = 3;

            moveInterp = Interp.pow3In;
            status = NHStatusEffects.scrambler;
            shootLength = 22f;
            laserColor = NHColor.thermoPst;
            requirements(Category.turret, ItemStack.with(NHItems.multipleSteel, 50, Items.plastanium, 85, NHItems.seniorProcessor, 35, NHItems.presstanium, 80));
            hasPower = true;
            cal = d -> 4 * Interp.pow3Out.apply(d) - 3;
            scaledForce = 0;
            force = 45f;
            range = 280.0F;
            damage = 0.3F;
            scaledHealth = 160.0F;
            rotateSpeed = 10.0F;
            consumePower(12.0F);
        }};

        gravity = new MultTractorBeamTurret("gravity") {{
            size = 3;
            requirements(Category.turret, ItemStack.with(Items.metaglass, 35, NHItems.juniorProcessor, 15, Items.lead, 80, NHItems.presstanium, 45));
            health = 1020;
            maxAttract = 8;
            shootCone = 60f;
            range = 300f;
            hasPower = true;
            force = 40.0F;
            scaledForce = 8.0F;
            shootLength = size * tilesize / 2f - 3;
            damage = 0.15F;
            rotateSpeed = 6f;
            consumePowerCond(6.0F, (MultTractorBeamBuild e) -> e.target != null);
        }};

        eternity = new ItemTurret("eternity") {{
            armor = 30;
            size = 16;
            outlineRadius = 7;
            range = 1200;
            heatColor = NHColor.darkEnrColor;
            unitSort = NHUnitSorts.regionalHPMaximum_All;

            coolant = consume(new ConsumeLiquid(NHLiquids.quantumLiquid, 1));
            liquidCapacity = 120;
            coolantMultiplier = 2.5f;

            buildCostMultiplier *= 2;
            canOverdrive = false;
            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-side") {{
                    under = mirror = true;
                    layerOffset = -0.1f;
                    moveX = 6f;
                    progress = smoothReload.inv().curve(Interp.pow3Out);
                }}, new RegionPart("-side-down") {{
                    mirror = true;
                    layerOffset = -0.5f;
                    moveX = 10f;
                    moveY = 45f;
                    y = 10f;
                    progress = smoothReload.inv().curve(Interp.pow3Out);
                }}, new RegionPart("-side-down") {{
                    mirror = true;
                    layerOffset = -0.35f;
                    moveX = -9f;
                    moveY = 7f;
                    y = -2f;
                    x = 8;
                    progress = smoothReload.inv().curve(Interp.pow3Out);
                }}, new RegionPart("-side-down") {{
                    under = mirror = true;
                    layerOffset = -0.2f;
                    moveY = -33f;
                    y = -33f;
                    x = 14;
                    progress = smoothReload.inv().curve(Interp.pow3Out);
                }});

                parts.add(new ArcCharge() {{
                    progress = smoothReload.inv().curve(Interp.pow5Out);
                    color = NHColor.darkEnrColor;
                    chargeY = t -> -35f;
                    shootY = t -> 90 * curve.apply(1 - t.smoothReload);
                }});
            }};
            buildType = () -> new ItemTurretBuild(){

                // -------------------- 基础字段 --------------------
                private final Vec2 energyOrb1 = new Vec2();   // 炮口能量球起点位置
                private final Vec2 energyOrb2 = new Vec2();   // 炮口能量球终点位置（根据reload进度移动）
                private float orbMoveProgress = 0f;           // 能量球移动进度 (0~1)
                private final float orbMaxDist = size * tilesize * 0.98f; // 能量球最大移动距离

                // 曲线/能量球相关参数
                private static final float CURVE_STRENGTH = 8f; // 贝塞尔曲线偏移强度
                private static final float ORB_UP_OFFSET = 0f;  // 起点上偏移（一般设为0）
                private static final float SHRINK_SPEED = 2.4f; // 曲线收缩速度

                // -------------------- 内部类：移动能量球 --------------------
                private class MovingOrb{
                    private final Vec2 start;       // 起点位置
                    private final Vec2 end;         // 终点位置
                    private final long spawnTime;   // 生成时间
                    private final float lifespan = 800f; // 存活时长（毫秒）
                    private final float baseSize;   // 初始半径大小

                    public MovingOrb(Vec2 start, Vec2 end){
                        this.start = start.cpy().add(0f, -ORB_UP_OFFSET); // 起点可微调高度
                        this.end = end.cpy();
                        this.spawnTime = Time.millis();
                        this.baseSize = 2.5f + Mathf.random() * 2f;       // 初始大小随机化
                    }

                    // 判断能量球是否过期
                    public boolean isExpired(){ return Time.timeSinceMillis(spawnTime) > lifespan; }

                    // 返回进度 0~1
                    public float fin(){ return Mathf.clamp(Time.timeSinceMillis(spawnTime)/lifespan); }

                    // 根据进度插值计算当前位置
                    public Vec2 pos(){ return start.cpy().lerp(end, fin()); }

                    // 绘制能量球
                    public void draw(){
                        float f = fin();
                        Vec2 p = pos();

                        Draw.z(Layer.effect - 1.01f); // 设置绘制层级
                        float size = baseSize * (1f - f * 0.4f); // 随着时间缩小
                        float glow = size * 2.2f;                // 外发光大小

                        // 半透明发光层
                        Draw.color(heatColor, 0.45f);
                        Fill.circle(p.x, p.y, glow);

                        // 主体颜色
                        Draw.color(heatColor);
                        Fill.circle(p.x, p.y, size);

                        // 中心白点
                        Draw.color(Color.white, 0.7f * (1f - f));
                        Fill.circle(p.x, p.y, size * 0.55f);
                    }
                }

                // -------------------- 内部类：贝塞尔曲线 --------------------
                private class BezierCurve{
                    private final MovingOrb target;  // 曲线目标（移动能量球）
                    private final long spawnTime;    // 曲线生成时间
                    private final Color color;       // 曲线颜色
                    private final Vec2 ctrlOffset;   // 控制点偏移量
                    private final float baseAmp;     // 偏移基准幅度

                    public BezierCurve(MovingOrb target, Color color){
                        this.target = target;
                        this.color = color;
                        this.spawnTime = Time.millis();

                        // 偏移强度与距离成比例，最低保证 5
                        float rawAmp = target.start.dst(target.end) * 0.25f + Mathf.random() * 10f;
                        this.baseAmp = Math.max(rawAmp, 5f) * CURVE_STRENGTH;

                        // 随机生成控制点方向与大小
                        float angle = Mathf.random(360f);
                        float mag = Mathf.clamp(Mathf.random() * baseAmp, 5f, baseAmp);
                        this.ctrlOffset = new Vec2(Angles.trnsx(angle, mag), Angles.trnsy(angle, mag));
                    }

                    // 曲线是否过期：能量球过期 或 曲线存在时间 > 1600ms
                    public boolean isExpired(){ return target.isExpired() || (Time.timeSinceMillis(spawnTime) > 1600f); }

                    // 曲线生命周期进度
                    public float lifeProgress(){ return Mathf.clamp(Time.timeSinceMillis(spawnTime)/1600f); }

                    // 获取当前控制点（随时间收缩）
                    public Vec2 getControlPoint(){
                        Vec2 start = energyOrb1.cpy();
                        Vec2 end = target.pos().cpy();
                        float lp = lifeProgress();

                        // 收缩公式，速度由 SHRINK_SPEED 控制
                        float shrinkFactor = Mathf.pow(0.08f, lp * SHRINK_SPEED);

                        Vec2 offset = ctrlOffset.cpy().scl(shrinkFactor);

                        // 限制偏移长度（避免太离谱）
                        float maxOffset = start.dst(end) * 1.8f;
                        if(offset.len() > maxOffset) offset.setLength(maxOffset);

                        // 控制点位于中点偏移位置
                        return start.cpy().lerp(end, 0.5f).add(offset);
                    }

                    // 绘制曲线
                    public void draw(){
                        //  改动点：不再 return，而是保证暂停时也绘制
                        Vec2 start = energyOrb1.cpy();
                        Vec2 end = target.pos().cpy();
                        Vec2 ctrl = getControlPoint();

                        Draw.z(Layer.effect - 1.01f);
                        float alpha = Mathf.lerp(1f, 0.18f, lifeProgress());
                        Draw.color(color.cpy().a(alpha));

                        // 贝塞尔曲线绘制
                        Lines.stroke(2f * (1f - lifeProgress() * 0.5f), color.cpy().a(alpha));
                        Lines.curve(
                                start.x, start.y,
                                ctrl.x, ctrl.y,
                                ctrl.x, ctrl.y,
                                end.x, end.y,
                                20 // 曲线分段数
                        );
                    }
                }

                // -------------------- 容器 --------------------
                private final Seq<MovingOrb> movingOrbs = new Seq<>();    // 能量球容器
                private final Seq<BezierCurve> activeCurves = new Seq<>();// 曲线容器

                // 能量球生成几率参数
                private static final float MOVING_ORB_SPAWN_MIN = 0.01f;
                private static final float MOVING_ORB_SPAWN_MAX = 0.24f;

                // -------------------- 逻辑更新 --------------------
                @Override
                public void update(){
                    super.update();
                    if(Vars.state.isPaused()) return; //  update 在暂停时停止，但 draw 仍会绘制已有对象

                    float upAngle = Mathf.mod(rotation, 360f); // 炮塔朝向角度
                    float baseOffset = 35f;

                    // 计算能量球起点位置（炮口后方）
                    energyOrb1.set(
                            x + Angles.trnsx(upAngle + 180f, baseOffset),
                            y + Angles.trnsy(upAngle + 180f, baseOffset)
                    );

                    // 计算能量球终点位置（炮口前方，根据装填进度）
                    orbMoveProgress = Mathf.clamp(reloadCounter / reload, 0f, 1f);
                    float currentOrbDist = orbMaxDist * orbMoveProgress;

                    energyOrb2.set(
                            energyOrb1.x + Angles.trnsx(upAngle, currentOrbDist),
                            energyOrb1.y + Angles.trnsy(upAngle, currentOrbDist)
                    );

                    // 清理过期能量球与曲线
                    movingOrbs.removeAll(MovingOrb::isExpired);
                    activeCurves.removeAll(BezierCurve::isExpired);

                    // 动态生成新能量球与曲线
                    float fin = reloadCounter / reload;
                    float spawnChance = Mathf.lerp(MOVING_ORB_SPAWN_MIN, MOVING_ORB_SPAWN_MAX, fin);

                    if(Mathf.chanceDelta(spawnChance)){
                        Vec2 start = energyOrb1.cpy().add(0f, -ORB_UP_OFFSET);
                        MovingOrb mo = new MovingOrb(start, energyOrb2.cpy());
                        movingOrbs.add(mo);
                        activeCurves.add(new BezierCurve(mo, heatColor));
                    }
                }

                // -------------------- 绘制 --------------------
                @Override
                public void draw(){
                    super.draw();
                    if(Vars.state.isPaused()){
                        //  保证暂停时仍然绘制已有能量球和曲线
                        for(MovingOrb mo : movingOrbs) mo.draw();
                        for(BezierCurve curve : activeCurves) curve.draw();
                        return;
                    }

                    float fin = reloadCounter / reload;
                    if(fin <= 0.01f) return;

                    // 绘制起点和终点能量球
                    drawEnergyOrb(energyOrb1, fin);
                    drawEnergyOrb(energyOrb2, fin);

                    // 绘制能量球与曲线
                    for(MovingOrb mo : movingOrbs) mo.draw();
                    for(BezierCurve curve : activeCurves) curve.draw();

                    // 半径计算（用于闪电效果）
                    float innerRadius = size * tilesize * 0.74f * Interp.circleOut.apply(fin);
                    float outerRadius = size * tilesize * 0.96f * Interp.circleOut.apply(fin);

                    // 内环随机闪电
                    if(Mathf.chanceDelta(0.12f * fin)){
                        float ang = Mathf.random(360f);
                        Vec2 inner = Tmp.v1.trns(ang, innerRadius).add(x, y).cpy();
                        NHFx.chainLightningFade.at(energyOrb2.x, energyOrb2.y, 12f, heatColor, new Vec2(inner));
                    }

                    // 外环随机闪电
                    if(Mathf.chanceDelta(0.08f * fin)){
                        float ang = Mathf.random(360f);
                        Vec2 outer = Tmp.v1.trns(ang, outerRadius).add(x, y).cpy();
                        NHFx.chainLightningFade.at(energyOrb2.x, energyOrb2.y, 18f, heatColor, new Vec2(outer));
                    }
                }

                // -------------------- 工具方法：绘制能量球 --------------------
                private void drawEnergyOrb(Vec2 pos, float fin){
                    Draw.z(Layer.effect-1.01f);

                    // 外层发光
                    float glowSize = 8f * fin + Mathf.absin(Time.time, 2f, 2f * fin);
                    Draw.color(heatColor, 0.3f);
                    Fill.circle(pos.x, pos.y, glowSize);

                    // 核心层
                    float coreSize = 4f * fin;
                    Draw.color(heatColor);
                    Fill.circle(pos.x, pos.y, coreSize);

                    // 中心高亮
                    Draw.color(Color.white, 0.6f);
                    Fill.circle(pos.x, pos.y, coreSize * 0.6f);
                }
            };


            shoot = new ShootPattern();
            inaccuracy = 0;

            ammoPerShot = 40;
            coolantMultiplier = 0.8f;
            rotateSpeed = 0.25f;

            float chargeCircleFrontRad = 12f;

            shootEffect = new Effect(120f, 2000f, e -> {
                float scl = 1f;
                if (e.data instanceof Float) scl *= (float) e.data;
                Draw.color(heatColor, Color.white, e.fout() * 0.25f);

                float rand = Mathf.randomSeed(e.id, 60f);
                float extend = Mathf.curve(e.fin(Interp.pow10Out), 0.075f, 1f) * scl;
                float rot = e.fout(Interp.pow10In);

                for (int i : Mathf.signs) {
                    DrawFunc.tri(e.x, e.y, chargeCircleFrontRad * 1.2f * e.foutpowdown() * scl, 200 + 500 * extend, e.rotation + (90 + rand) * rot + 90 * i - 45);
                }

                for (int i : Mathf.signs) {
                    DrawFunc.tri(e.x, e.y, chargeCircleFrontRad * 1.2f * e.foutpowdown() * scl, 200 + 500 * extend, e.rotation + (90 + rand) * rot + 90 * i + 45);
                }
            });

            smokeEffect = new Effect(50, e -> {
                Draw.color(heatColor);
                Lines.stroke(e.fout() * 5f);
                Lines.circle(e.x, e.y, e.fin() * 300);
                Lines.stroke(e.fout() * 3f);
                Lines.circle(e.x, e.y, e.fin() * 180);
                Lines.stroke(e.fout() * 3.2f);
                Angles.randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
                    lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
                });
                Draw.color(Color.white);
                Drawf.light(e.x, e.y, e.fout() * 120, heatColor, 0.7f);
            });

            recoil = 18f;
            shake = 80f;
            shootSound = Sounds.laserblast;
            health = 800000;
            shootCone = 5f;
            maxAmmo = 80;
            consumePowerCond(800f, TurretBuild::isActive);
            reload = 1800f;

            ammo(NHItems.darkEnergy, NHBullets.eternity);

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.nodexPlate, 5000, NHItems.darkEnergy, 2000));
        }};

        antiBulletTurret = new PointDefenseTurret("anti-bullet-turret") {{
            health = 1080;
            size = 3;

            coolant = consumeCoolant(0.1F);

            color = lightColor = NHColor.lightSkyBack;
            beamEffect = Fx.chainLightning;
            hitEffect = NHFx.square45_4_45;
            shootEffect = NHFx.shootLineSmall(color);
            shootSound = NHSounds.gauss;

            range = 280f;

            hasPower = true;
            consumePowerCond(8f, (PointDefenseBuild b) -> b.target != null);

            shootLength = 5f;
            bulletDamage = 150f;
            reload = 6f;

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 90, NHItems.juniorProcessor, 60, NHItems.presstanium, 120, NHItems.zeta, 120, Items.graphite, 80));
        }};

        dendrite = new ShootMatchTurret("dendrite") {{
            health = 6000;
            armor = 15;
            range = 360f;
            trackingRange = range * 1.4f;

            recoil = 6.0F;
            size = 4;
            shake = 12.0F;
            reload = 60.0F;

            rotateSpeed = 2.4f;

            shootSound = NHSounds.flak2;

            ammo(NHItems.ancimembrane, new TrailFadeBulletType(4f, 580f, "circle-bullet") {
                {
                    velocityBegin = 8.8f;
                    velocityIncrease = -8.15f;
                    accelerateBegin = 0.1f;
                    accelerateEnd = 0.925f;
                    accelInterp = Interp.pow2Out;
                    lifetime = 92f;

                    backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.ancient;
                    frontColor = NHColor.ancientLight;

                    impact = true;
                    knockback = 3f;

                    status = NHStatusEffects.entangled;
                    statusDuration = 120f;

                    hitSize = 12f;
                    lightning = 2;
                    lightningLengthRand = 5;
                    lightningLength = 3;
                    lightningDamage = damage / 10f;

                    reloadMultiplier = 0.5f;
                    ammoMultiplier = 2f;

                    width = 155f;
                    height = 7;
                    shrinkX = 0.45f;
                    shrinkY = -2.48f;
                    shrinkInterp = Interp.reverse;

                    pierce = true;
                    pierceCap = 4;

                    tracers = 1;
                    tracerUpdateSpacing = 3;
                    tracerFadeOffset = 5;
                    tracerRandX = 16f;
                    tracerStrokeOffset = 6;
                    addBeginPoint = true;


                    smokeEffect = NHFx.hugeSmokeGray;
                    shootEffect = new EffectWrapper(NHFx.shootLine(30f, 120f), backColor);
                    hitEffect = NHFx.square45_6_45;
                    despawnEffect = new Effect(35f, 70f, e -> {
                        Draw.color(e.color, Color.white, e.fout() * 0.7f);
                        for (int i : Mathf.signs) {

                            Drawf.tri(e.x, e.y, height * 1.5f * e.fout(), width * 0.885f * e.fout(), e.rotation + i * 90);
                            Drawf.tri(e.x, e.y, height * 0.8f * e.fout(), width * 0.252f * e.fout(), e.rotation + 90 + i * 90);
                        }
                    });
                }

                @Override
                public void hitEntity(Bullet b, Hitboxc entity, float health) {
                    super.hitEntity(b, entity, health);
                    b.fdata += 1;

                    if (b.fdata > pierceCap) {
                        b.hit = true;
                        b.remove();
                    }
                }

                @Override
                public void update(Bullet b) {
                    super.update(b);
                    b.collided.clear();
                }
            }, NHItems.setonAlloy, new AccelBulletType(2.85f, 280f) {{
                frontColor = NHColor.ancientLight;
                backColor = lightningColor = trailColor = hitColor = lightColor = NHColor.ancient;
                lifetime = 92f;
                knockback = 2f;
                ammoMultiplier = 6f;
                accelerateBegin = 0.1f;
                accelerateEnd = 0.85f;

                despawnSound = hitSound = Sounds.dullExplosion;

                velocityBegin = 6f;
                velocityIncrease = -4f;

                homingDelay = 20f;
                homingPower = 0.05f;
                homingRange = 120f;

                status = NHStatusEffects.entangled;
                statusDuration = 120f;

                despawnHit = pierceBuilding = true;
                hitShake = despawnShake = 5f;
                lightning = 1;
                lightningCone = 360;
                lightningLengthRand = 12;
                lightningLength = 4;
                width = 10f;
                height = 35f;
                pierceCap = 8;
                shrinkX = shrinkY = 0;

                reloadMultiplier = 1.75f;
                lightningDamage = damage * 0.8f;

                hitEffect = NHFx.hitSparkLarge;
                despawnEffect = NHFx.square45_6_45;
                shootEffect = new EffectWrapper(NHFx.shootLine(22, 20f), backColor);
                smokeEffect = NHFx.hugeSmokeGray;
                trailEffect = NHFx.trailToGray;

                trailLength = 15;
                trailWidth = 2f;
                drawSize = 300f;
            }});

            shooter(NHItems.ancimembrane, new ShootSpread() {{
                shots = 15;
                spread = 8;
            }}, NHItems.setonAlloy, new ShootAlternate() {{
                shots = 3;
                shotDelay = 5;
                spread = 11f;
            }});

            shootCone = 35f;
            ammoPerShot = 6;
            maxAmmo = 30;
            cooldownTime = 65f;

            squareSprite = false;
            drawer = new DrawTurret("reinforced-") {{
                parts.add(new RegionPart("-top") {{
                    layerOffset = 0.1f;
                    heatLayerOffset = 0;
                }}, new RegionPart("-back") {{
                    mirror = true;
                    layerOffset = 0.1f;

                    moveX = 1.5f;
                    moveY = -1.5f;
                    progress = PartProgress.recoil;
                    heatLayerOffset = 0;
                }}, new RegionPart("-panel") {{
                    layerOffset = 0.1f;

                    moveX = 0;
                    moveY = 2.75f;

                    heatProgress = PartProgress.warmup;
                    progress = NHPartProgress.recoilWarmup;
                    heatLayerOffset = 0;
                }});
            }};

            warmupMaintainTime = 45f;
            shootWarmupSpeed = 0.04f;
            minWarmup = 0.85f;

            outlineColor = Pal.darkOutline;
            canOverdrive = false;
            coolant = consume(new ConsumeLiquid(NHLiquids.quantumLiquid, 15f / 60f));
            coolantMultiplier = 2f;
            consumePowerCond(12f, TurretBuild::isActive);
            unitSort = UnitSorts.weakest;
            requirements(Category.turret, with(NHItems.seniorProcessor, 200, NHItems.ancimembrane, 200, NHItems.zeta, 250, NHItems.metalOxhydrigen, 150));
        }};

        atomSeparator = new LaserTurret("atom-separator") {{
            health = 12000;
            range = 360f;
            shootEffect = NHFx.hugeSmokeGray;
            shootCone = 20.0F;
            recoil = 6.0F;
            size = 5;
            shake = 4.0F;
            reload = 60.0F;

            canOverdrive = false;
            accurateDelay = false;
            rotateSpeed = 3f;
            firingMoveFract = 0.15F;
            shootDuration = 200.0F;
            shootSound = Sounds.laserbig;
            loopSound = Sounds.beam;
            loopSoundVolume = 2.0F;
            shootType = NHBullets.atomSeparator;

            coolant = consumeCoolant(1f);
            consumePower(50f);
            unitSort = NHUnitSorts.slowest;
            requirements(Category.turret, with(NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 200, NHItems.zeta, 150, NHItems.presstanium, 250, NHItems.metalOxhydrigen, 150));
        }};

        bloodStar = new ShootMatchTurret("blood-star") {{
            size = 5;
            coolant = consumeCoolant(0.2F);
            requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 230, NHItems.zeta, 300, NHItems.seniorProcessor, 200, NHItems.presstanium, 300));
            recoil = 5f;
            reload = 150f;
            range = 520f;
            unitSort = (u, x, y) -> -u.hitSize();
            shootSound = Sounds.laserblast;
            inaccuracy = 0f;
            //shoot = new ShootPattern() {{
            //    firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
            //}};

            heatColor = Items.surgeAlloy.color.cpy().lerp(Color.white, 0.2f);
            consumePowerCond(12f, TurretBuild::isActive);
            coolantMultiplier = 3f;

            health = 8000;

            ammo(NHItems.thermoCorePositive,
                    new BasicBulletType(4, 600, "large-bomb") {

                        {

                            lightning = 6;
                            lightningCone = 360;
                            lightningLengthRand = lightningLength = 12;
                            splashDamageRadius = 60f;
                            splashDamage = lightningDamage = 0.5f * damage;

                            trailEffect = NHFx.hitSparkLarge;
                            trailInterval = 3f;

                            trailColor = backColor = hitColor = lightColor = lightningColor = heatColor;
                            frontColor = Color.white;

                            homingRange = 100f;
                            homingPower = 0.08f;

                            intervalBullets = 2;
                            bulletInterval = 3f;
                            intervalBullet = new AdaptedLightningBulletType() {{
                                lightningColor = trailColor = hitColor = lightColor = heatColor;
                                lightningLength = 4;
                                lightningLengthRand = 15;
                                damage = 60;
                            }};

                            status = NHStatusEffects.emp3;
                            statusDuration = 90f;

                            spin = 3f;
                            trailLength = 40;
                            trailWidth = 2.5f;
                            lifetime = 140f;
                            shrinkX = shrinkY = 0;
                            hitSound = Sounds.explosionbig;
                            drawSize = 60f;
                            hitShake = despawnShake = 6f;
                            shootEffect = NHFx.instShoot(backColor, frontColor);
                            smokeEffect = Fx.shootBigSmoke2;
                            hitEffect = new Effect(50, e -> {
                                Draw.color(backColor);
                                Fill.circle(e.x, e.y, e.fout() * height / 1.5f);
                                Lines.stroke(e.fout() * 3f);
                                Lines.circle(e.x, e.y, e.fin() * 80);
                                Lines.stroke(e.fout() * 2f);
                                Lines.circle(e.x, e.y, e.fin() * 50);
                                Angles.randLenVectors(e.id, 35, 18 + 100 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 12 + 4));

                                Draw.color(frontColor);
                                Fill.circle(e.x, e.y, e.fout() * height / 2f);
                            });
                            despawnEffect = new OptionalMultiEffect(NHFx.crossBlast(backColor, 120f), NHFx.instHit(backColor, 3, 80f),
                                    NHFx.spreadOutSpark(120f, splashDamageRadius + 30f, 36, 4, 42f, 17f, 4f, Interp.pow3Out)
                            );
                            height = width = 40;
                        }

                        @Override
                        public void draw(Bullet b) {
                            super.draw(b);

                            float f = Mathf.curve(b.fout(), 0, 0.05f);
                            float f2 = Mathf.curve(b.fin(), 0, 0.1f);
                            Draw.color(backColor);

                            float fi = Mathf.randomSeed(b.id, 360f);

                            for (int i : Mathf.signs) {
                                DrawFunc.tri(b.x, b.y, 3 * f2 * f, 60 * f2 * f, fi + (i + 1) * 90 + Time.time * 2);
                                DrawFunc.tri(b.x, b.y, 3 * f2 * f, 35 * f2 * f, fi + (i + 1) * 90 - Time.time * 2 + 90);
                            }
                        }
                    }

            );
            shooter(
                    NHItems.thermoCorePositive, new ShootSpread() {{
                        firstShotDelay = 90f;
                        shots = 3;
                        shootCone = 30f;
                        shotDelay = 15f;
                    }}
            );


        }};

        multipleLauncher = new ItemTurret("multiple-launcher") {{
            size = 3;
            health = 1250;
            coolant = consumeCoolant(0.2F);
            requirements(Category.turret, ItemStack.with(NHItems.presstanium, 45, NHItems.metalOxhydrigen, 45, NHItems.juniorProcessor, 30));
            canOverdrive = false;
            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-shooter") {{
                    mirror = true;
                    progress = PartProgress.warmup.compress(0, 0.75f);
                    moveX = 0.75f;
                    moveY = -1.5f;
                }});
            }};

            ammo(
                    Items.titanium, NHBullets.missileTitanium,
                    Items.thorium, NHBullets.missileThorium,
                    NHItems.zeta, NHBullets.missileZeta,
                    Items.graphite, NHBullets.missileNormal,
                    NHItems.presstanium, NHBullets.missileStrike
            );
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = Fx.shootBig2;
            recoil = 3f;
            range = 280f;
            reload = 90f;

            shoot = new ShootBarrel() {{
                barrels = new float[]{
                        -4, -2, 0,
                        0, -3, 0,
                        4, -2, 0
                };

                shotDelay = 2f;
                shots = 15;
            }};

            maxAmmo = 160;
            ammoPerShot = 15;
            ammoEjectBack = 6f;
            inaccuracy = 9f;

            xRand = tilesize * size / 6.5f;
            shootSound = Sounds.missile;
            coolantMultiplier = 0.85f;
        }};

        railGun = new ItemTurret("rail-gun") {
            {
                unitSort = (u, x, y) -> -u.speed();

                maxAmmo = 40;
                ammoPerShot = 8;

                squareSprite = false;
                drawer = new DrawTurret("reinforced-") {{
                    parts.add(new RegionPart("-acceler") {{
                        mirror = false;
//					under = turretShading = true;
                        moveX = 0;
                        moveY = -5f;

                        progress = PartProgress.recoil;
                    }}, new RegionPart("-top") {{
                        outline = true;
                        heatLight = false;
                        mirror = false;
                        //					under = turretShading = true;
                        moveX = 0;
                        moveY = 0;
                    }});
                }};

                ammo(
                        NHItems.irayrondPanel, NHBullets.railGun1,
                        NHItems.setonAlloy, NHBullets.railGun2,
                        NHItems.nodexPlate, NHBullets.railGun3
                );

                consumePowerCond(12f, TurretBuild::isActive);

                shoot = new ShootPattern() {{
                    shots = 1;
                    firstShotDelay = 90f;
                }};

                moveWhileCharging = false;
                shootCone = 6f;

                size = 4;
                health = 4550;
                armor = 15f;
                reload = 200f;
                recoil = 1f;
                shake = 8f;
                range = 620.0F;
                trackingRange = range * 1.4f;
                minRange = 160f;
                rotateSpeed = 1.5f;

                buildType = () -> new ItemTurretBuild() {
                    @Override
                    public void drawSelect() {
                        super.drawSelect();
                        Drawf.dashCircle(x, y, minRange, Pal.redderDust);
                    }

                    @Override
                    protected boolean validateTarget() {
                        return (!Units.invalidateTarget(target, canHeal() ? Team.derelict : team, x, y) && !within(target, minRange)) || isControlled() || logicControlled();
                    }
                };

                shootSound = NHSounds.railGunBlast;
//			heatColor = NHItems.irayrondPanel.color;

                outlineColor = Pal.darkOutline;
                accurateDelay = true;

                coolantMultiplier = 0.55f;
                cooldownTime = 90f;
                coolant = consumeCoolant(0.3f);

                requirements(Category.turret, with(NHItems.setonAlloy, 150, NHItems.seniorProcessor, 200, NHItems.zeta, 500, Items.phaseFabric, 125));
            }

            @Override
            public void drawPlace(int x, int y, int rotation, boolean valid) {
                super.drawPlace(x, y, rotation, valid);

                Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, minRange, Pal.redderDust);
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(Stat.shootRange, minRange / tilesize, StatUnit.blocks);
            }
        };

        endOfEra = new ShootMatchTurret("end-of-era") {{
            recoil = 5f;
            armor = 15;

            shootCone = 15f;
            squareSprite = false;

            unitSort = UnitSorts.strongest;

            warmupMaintainTime = 50f;
            coolant = consume(new ConsumeLiquid(NHLiquids.quantumLiquid, 20f / 60f));
            coolantMultiplier = 2.5f;

            moveWhileCharging = false;
            canOverdrive = false;

            shootWarmupSpeed = 0.035f;

            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-charger") {{
                    under = mirror = true;
                    layerOffset = -0.002f;
                    moveX = 14f;
                    moveY = -9f;
                    moveRot = -45f;
                    y = -4f;
                    x = 16f;

                    progress = PartProgress.warmup;
                }}, new RegionPart() {{
                    drawRegion = false;
                    mirror = true;
                    heatColor = Color.clear;
                    progress = PartProgress.recoil.min(PartProgress.warmup);
                    moveY = -10f;
                    children.add(new RegionPart("-wing") {{
                        under = mirror = true;
                        moveRot = 12.5f;
                        moveY = 14f;
                        moveX = 4f;
                        heatColor = NHColor.darkEnrColor;
                        progress = PartProgress.warmup;
                    }});
                }}, new RegionPart("-shooter") {{
                    outline = true;
                    layerOffset = 0.001f;
                    moveY = -12f;
                    heatColor = NHColor.darkEnrColor;
                    progress = PartProgress.warmup.blend(PartProgress.recoil, 0.5f);
                }});
            }};

            shoot = new ShootPattern() {{
                firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
            }};

            outlineColor = Pal.darkOutline;

            chargeSound = NHSounds.railGunCharge;

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.nodexPlate, 1500));
            ammo(NHItems.darkEnergy, NHBullets.arc_9000, NHItems.nodexPlate, new BulletType() {{
                hittable = false;
                hitColor = lightColor = trailColor = NHColor.darkEnrFront;
                ammoMultiplier = 1f;
                spawnUnit = NHBullets.airRaidMissile;
                chargeEffect = NHFx.railShoot(NHColor.darkEnrColor, 800f, 18, NHFx.darkEnergyChargeBegin.lifetime, 25);
                shootEffect = NHFx.instShoot(NHColor.darkEnrColor, NHColor.darkEnrFront);
                smokeEffect = new Effect(180f, 300f, b -> {
                    float intensity = 2f;

                    Rand rand = Fx.rand;

                    color(b.color, 0.7f);
                    for (int i = 0; i < 4; i++) {
                        rand.setSeed(b.id * 2 + i);
                        float lenScl = rand.random(0.5f, 1f);
                        int fi = i;
                        b.scaled(b.lifetime * lenScl, e -> {
                            randLenVectors(e.id + fi - 1, e.fin(Interp.pow4Out), (int) (2 * intensity), 35f * intensity, e.rotation, 20, (x, y, in, out) -> {
                                float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                                float rad = fout * ((2f + intensity) * 1.75f);

                                Fill.circle(e.x + x, e.y + y, rad);
                                Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
                            });
                        });
                    }
                });
            }});
            shooter(NHItems.nodexPlate, new ShootBarrel() {{
                barrels = new float[]{
                        22, -12, 25,
                        -22, -12, -25,
                        0, -22, 0,
                };
                firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
                shots = 3;
                shotDelay = 12f;
            }});

            shootCone = 12f;
            rotateSpeed = 0.75f;
            ammoPerShot = 4;
            maxAmmo = 20;
            size = 8;
            health = 28000;
            armor = 15f;
            hasItems = true;
            heatColor = NHColor.darkEnrColor;
            consumePower(30f);
            reload = 420f;
            range = 800f;
            trackingRange = range * 1.4f;
            inaccuracy = 0f;
            shootSound = Sounds.laserbig;
        }};

        executor = new ItemTurret("executor") {{
            size = 6;
            health = 15200;
            armor = 10;
            requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 650, Items.tungsten, 375, NHItems.seniorProcessor, 150, NHItems.multipleSteel, 400));
            ammo(
                    NHItems.thermoCorePositive, NHBullets.blastEnergyPst, NHItems.thermoCoreNegative, NHBullets.blastEnergyNgt
            );

            shoot = new ShootBarrel() {{
                shots = 3;
                barrels = new float[]{
                        -11, 2, 0,
                        0, 2, 0,
                        11, 2, 0
                };
                shotDelay = 4f;
            }};

            canOverdrive = false;
            maxAmmo = 80;
            ammoPerShot = 8;
            xRand = 1;
            velocityRnd = 0.08f;
            reload = 16;
            shootCone = 50.0f;
            rotateSpeed = 2.5f;
            range = 440.0F;
            inaccuracy = 1;
            heatColor = NHBullets.blastEnergyPst.lightColor;
            recoil = 4.0F;
            shootSound = NHSounds.thermoShoot;
            coolant = new ConsumeCoolant(0.25f);
        }};
    }

    private static void loadPowers() {
        hyperGenerator = new HyperGenerator("hyper-generator") {{
            size = 8;
            health = 40000;
            armor = 50f;
            powerProduction = 4000f;
            updateLightning = updateLightningRand = 3;
            effectColor = NHColor.thermoPst;
            itemCapacity = 40;
            itemDuration = 180f;
            ambientSound = Sounds.pulse;
            ambientSoundVolume = 0.1F;

            consumePower(100.0F);
            consumeItems(ItemStack.with(NHItems.thermoCoreNegative, 6, Items.phaseFabric, 6)).optional(true, true);
            consumeItems(new ItemStack(NHItems.thermoCorePositive, 6), new ItemStack(NHItems.metalOxhydrigen, 6));
            consumeLiquid(NHLiquids.zetaFluidNegative, 8/60f);
            consumeLiquids(new LiquidStack(NHLiquids.zetaFluidPositive, 8/60f)).optional(true, true);
            requirements(Category.power, BuildVisibility.shown, with(NHItems.nodexPlate, 800, NHItems.setonAlloy, 600, NHItems.irayrondPanel, 400, NHItems.presstanium, 1500, Items.surgeAlloy, 250));
        }};
    }

    public static void load() {
        blaster = new ShockwaveGenerator("blaster") {{
            requirements(Category.defense, with(NHItems.presstanium, 120, NHItems.juniorProcessor, 120, NHItems.multipleSteel, 30));

            squareSprite = false;
            size = 3;
            chargerOffset = 5.65f;
            rotateOffset = -45f;
            damage = 150;
            lightningDamage = 200;
            generateLiNum = 3;
            generateLiLen = 12;
            generateLenRand = 20;
            gettingBoltNum = 1;
            lightningColor = NHColor.darkEnrColor;
            generateEffect = NHFx.blastgenerate;
            acceptEffect = NHFx.blastAccept;
            blastSound = Sounds.explosionbig;
            status = NHStatusEffects.emp2;
            range = 240;
            health = 1200;
            knockback = 10f;
            consumePower(8f);
            itemCapacity = 30;
            consumeItem(NHItems.zeta, 3);

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt() {{
                midColor = flameColor = NHColor.darkEnrColor;
                circleStroke = 1.125f;
                circleSpace = 1.9f;
            }}, new DrawDefault());
        }};

        reinForcedLiquidSource = new LiquidSource("reinforced-liquid-source") {{
            size = 1;
            health = 800;
            armor = 10;
            buildVisibility = BuildVisibility.sandboxOnly;

            category = Category.liquid;

            buildType = () -> new LiquidSourceBuild() {
                @Override
                public boolean canPickup() {
                    return false;
                }

                @Override
                public void write(Writes write) {
                    write.str(source == null ? "null-liquid" : source.name);
                }

                @Override
                public void read(Reads read, byte revision) {
                    source = content.liquid(read.str());
                }
            };
        }};

        reinForcedItemSource = new ItemSource("reinforced-item-source") {
            {
                size = 1;
                health = 800;
                armor = 10;
                buildVisibility = BuildVisibility.sandboxOnly;

                buildType = () -> new ItemSourceBuild() {
                    @Override
                    public boolean canPickup() {
                        return false;
                    }

                    @Override
                    public void draw() {
                        if (this.block.variants != 0 && this.block.variantRegions != null) {
                            Draw.rect(this.block.variantRegions[Mathf.randomSeed(this.tile.pos(), 0, Math.max(0, this.block.variantRegions.length - 1))], this.x, this.y, this.drawrot());
                        } else {
                            Draw.rect(this.block.region, this.x, this.y, this.drawrot());
                        }

                        this.drawTeamTop();

                        if (outputItem == null) {
                            Draw.rect(NHContent.crossRegion, x, y);
                        } else {
                            Draw.color(outputItem.color);
                            Draw.rect(NHContent.sourceCenter, x, y);
                            Draw.color();
                        }
                    }

                    @Override
                    public void write(Writes write) {
                        write.str(outputItem == null ? "null-item" : outputItem.name);
                    }

                    @Override
                    public void read(Reads read, byte revision) {
                        outputItem = content.item(read.str());
                    }
                };
            }

            @Override
            public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
                drawPlanConfigCenter(plan, plan.config, NewHorizon.name("source-center"), true);
            }
        };

        fireExtinguisher = new FireExtinguisher("fire-extinguisher") {{
            size = 3;
            health = 920;
            intensity = 1600;

            consumeItem(NHItems.metalOxhydrigen, 2);
            consumePowerCond(3f, FireExtinguisherBuild::isActive);

            requirements(Category.defense, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 120));
        }};

        airRaider = new AirRaider("air-raider") {{
            requirements(Category.defense, with(NHItems.nodexPlate, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 120, NHItems.juniorProcessor, 100, Items.phaseFabric, 150));

            shoot = new ShootSummon(0, 0, 120, 0) {{
                shots = 4;
                shotDelay = 8f;
            }};

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame() {{
                alpha = 0.375f;
                particles = 20;
                particleSize = 2.6f;
                particleRad = 7f;
                flameColor = NHColor.darkEnrColor;
                midColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.1f);
            }}, new DrawDefault());

            size = 3;
            consumePowerCond(6f, AirRaiderBuild::isCharging);
            consumeItem(NHItems.darkEnergy, 4);
            itemCapacity = 16;
            health = 4500;

            triggeredEffect = new Effect(45f, e -> {
                Draw.color(NHColor.darkEnrColor);
                Lines.stroke(e.fout() * 2f);
                Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
            });

            bullet = NHBullets.airRaidBomb;
        }};


        unitIniter = new UnitSpawner("unit-initer");

        bombLauncher = new BombLauncher("bomb-launcher") {{
            requirements(Category.defense, with(NHItems.multipleSteel, 100, NHItems.presstanium, 300, NHItems.juniorProcessor, 200, Items.surgeAlloy, 75));
            //NHTechTree.add(Blocks.massDriver, this);
            size = 3;
            storage = 2;

            bullet = new LightningLinkerBulletType(0f, 200) {
                {
                    trailWidth = 4.5f;
                    trailLength = 66;

                    spreadEffect = slopeEffect = Fx.none;
                    trailEffect = NHFx.hitSparkHuge;
                    trailInterval = 5;

                    backColor = trailColor = hitColor = lightColor = lightningColor = NHColor.thurmixRed;
                    frontColor = NHColor.thurmixRed;
                    randomGenerateRange = 240f;
                    randomLightningNum = 1;
                    linkRange = 120f;
                    range = 200f;

                    drawSize = 20f;

                    drag = 0.0035f;
                    fragLifeMin = 0.3f;
                    fragLifeMax = 1f;
                    fragVelocityMin = 0.3f;
                    fragVelocityMax = 1.25f;
                    fragBullets = 3;
                    fragBullet = new FlakBulletType(3.75f, 50) {
                        {
                            trailColor = lightColor = lightningColor = NHColor.thurmixRed;
                            backColor = NHColor.thurmixRed;
                            frontColor = NHColor.thurmixRed;

                            trailLength = 14;
                            trailWidth = 2.7f;
                            trailRotation = true;
                            trailInterval = 3;

                            trailEffect = NHFx.polyTrail(backColor, frontColor, 4.65f, 22f);
                            trailChance = 0f;
                            knockback = 12f;
                            lifetime = 40f;
                            width = 17f;
                            height = 42f;
                            collidesTiles = false;
                            splashDamageRadius = 60f;
                            splashDamage = damage * 0.6f;
                            lightning = 3;
                            lightningLength = 8;
                            smokeEffect = Fx.shootBigSmoke2;
                            hitShake = 8f;
                            hitSound = Sounds.plasmaboom;
                            status = StatusEffects.sapped;

                            statusDuration = 60f * 10;
                        }
                    };
                    hitSound = Sounds.explosionbig;
                    splashDamageRadius = 120f;
                    splashDamage = 200;
                    lightningDamage = 40f;

                    collidesTiles = true;
                    pierce = false;
                    collides = false;
                    lifetime = 10;
                    despawnEffect = new OptionalMultiEffect(
                            NHFx.crossBlast(hitColor, splashDamageRadius * 0.8f),
                            NHFx.blast(hitColor, splashDamageRadius * 0.8f),
                            NHFx.circleOut(hitColor, splashDamageRadius * 0.8f)
                    );
                }

                @Override
                public void update(Bullet b) {
                    super.update(b);

                    if (NHSetting.enableDetails() && b.timer(1, 6)) for (int j = 0; j < 2; j++) {
                        NHFunc.randFadeLightningEffect(b.x, b.y, Mathf.random(360), Mathf.random(7, 12), backColor, Mathf.chance(0.5));
                    }
                }

                @Override
                public void draw(Bullet b) {
                    Draw.color(backColor);
                    DrawFunc.surround(b.id, b.x, b.y, size * 1.45f, 14, 7, 11, (b.fin(NHInterp.parabola4Reversed) + 1f) / 2 * b.fout(0.1f));

                    drawTrail(b);

                    color(backColor);
                    Fill.circle(b.x, b.y, size);

                    Draw.z(NHFx.EFFECT_MASK);
                    color(frontColor);
                    Fill.circle(b.x, b.y, size * 0.62f);
                    Draw.z(NHFx.EFFECT_BOTTOM);
                    color(frontColor);
                    Fill.circle(b.x, b.y, size * 0.66f);
                    Draw.z(Layer.bullet);

                    Drawf.light(b.x, b.y, size * 1.85f, backColor, 0.7f);
                }
            };

            reloadTime = 300f;

            consumePowerCond(6f, BombLauncherBuild::isCharging);
            consumeItem(NHItems.fusionEnergy, 2);
            itemCapacity = 16;
            health = 1200;
        }};

        hyperspaceWarper = new HyperSpaceWarper("hyper-space-warper") {{
            size = 4;
            health = 2250;
            squareSprite = false;

            completeEffect = NHFx.square45_4_45;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawPlasma() {{
                plasma1 = NHColor.darkEnrColor;
                plasma2 = NHColor.darkEnr;
            }}, new DrawDefault());

            hasPower = hasItems = hasLiquids = true;
            itemCapacity = 20;
            liquidCapacity = 120;
            consumeItem(NHItems.fusionEnergy, 5);
            consumePower(12f);
            consumeLiquid(NHLiquids.quantumLiquid, 0.5f);

            requirements(Category.units, BuildVisibility.shown, with(NHItems.ancimembrane, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 450, NHItems.zeta, 200));
        }};

        largeWaterExtractor = new SolidPump("large-water-extractor") {{
            size = 3;
            pumpAmount = 0.31f;
            requirements(Category.production, ItemStack.with(NHItems.presstanium, 50, NHItems.juniorProcessor, 45, Items.tungsten, 30, Items.titanium, 30));
            result = Liquids.water;
            liquidCapacity = 60.0F;
            rotateSpeed = 1.4F;
            attribute = Attribute.water;
            consumePower(4f);
        }};

        loadTurrets();
        loadEnv();
        loadPowers();

        InnerBlock.load();
        ModuleBlock.load();
        ProductionBlock.load();
        TurretBlock.load();
        DefenseBlock.load();
        EnvironmentBlock.load();
        SpecialBlock.load();
        CraftingBlock.load();
        PowerBlock.load();
        LogicBlock.load();
        LiquidBlock.load();
        UnitBlock.load();
        DistributionBlock.load();
        PayloadBlock.load();
    }
}
