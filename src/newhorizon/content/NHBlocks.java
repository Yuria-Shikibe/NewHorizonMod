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
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.*;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.MendProjector;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.*;
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
import newhorizon.expand.block.env.ArmorFloor;
import newhorizon.expand.block.special.HyperGenerator;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.expand.block.special.UnitSpawner;
import newhorizon.expand.block.turrets.MultTractorBeamTurret;
import newhorizon.expand.block.turrets.ShootMatchTurret;
import newhorizon.expand.block.turrets.Webber;
import newhorizon.expand.bullets.*;
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
import static mindustry.type.ItemStack.with;

public class NHBlocks {
    public static Block reinForcedItemSource;
    public static Block reinForcedLiquidSource;
    public static Block oreZeta;
    public static Block hyperGenerator;


    public static Block ancientArtillery, dendrite, interferon, prism, hive, bloodStar, pulseShotgun, beamLaserTurret, blaster, endOfEra,
            thermoTurret, railGun, executor, gravity, multipleLauncher, antibody, multipleArtillery, atomSeparator, eternity;

    public static Block largeWaterExtractor;
    public static Block hydroFuelCell;
    public static Block armorBatteryLarge;
    public static Block hugeBattery;
    public static Block largeMendProjector;
    public static Block antiBulletTurret;
    public static Block fireExtinguisher;
    public static Block webber;
    public static Block hyperspaceWarper;
    public static Block bombLauncher;
    public static Block airRaider;
    public static Block unitIniter;
    public static Block remoteStorage;

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
        oreZeta = new OreBlock("ore-zeta") {{
            oreDefault = true;
            variants = 3;
            oreThreshold = 0.95F;
            oreScale = 20.380953F;
            itemDrop = NHItems.zeta;
            localizedName = itemDrop.localizedName;
            mapColor.set(itemDrop.color);
            useColor = true;
        }};

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

        armorClear = new ArmorFloor("armor-clear", 0) {{
            quantumSetter.get(this);
            blendGroup = this;
        }};

        conglomerateWall = new StaticWall("conglomerate-wall") {{
            variants = 4;
        }};

        conglomerateRock = new Floor("conglomerate-rock", 3) {{
            blendGroup = Blocks.stone;
        }};

        metalGroundHeat = new Floor("metal-ground-heat", 3) {{
            wall = metalWall;
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

                editorIcon = fullIcon = uiIcon = region = Core.atlas.find(NewHorizon.name("quantum-field-disturbing-icon"));
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
        multipleArtillery = new ShootMatchTurret("multiple-artillery") {{
            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-mid") {{
                    moveY = -3;

                    layerOffset = 0.01f;

                    outline = true;

                    progress = PartProgress.recoil;
                }});
                parts.add(new RegionPart("-barrel") {{
                    moveX = 5.5f;

                    layerOffset = 0.01f;

                    mirror = outline = true;

                    moves.add(new PartMove(PartProgress.recoil, 0, -4, 0, 0, 0));
                }});
            }};

            recoil = 0.74f;
            velocityRnd = 0.088f;
            lifeRnd = 0.088f;

            reload = 72f;

            rotateSpeed = 1.22f;
            unitSort = NHUnitSorts.slowest;

            shoot = new ShootBarrel() {{
                barrels = new float[]{
                        5f, -3f, 0,
                        -5f, -3f, 0,
                        11f, -2f, 0,
                        -11f, -2f, 0,
                };

                shots = 4;
            }};

            range = 640;
            trackingRange = range * 1.4f;

            minWarmup = 0.9f;
            shootWarmupSpeed /= 2;
            inaccuracy = 2;
            shootY -= 5;
            shootSound = Sounds.largeCannon;

            ammo(
                    NHItems.metalOxhydrigen, NHBullets.artilleryHydro,
                    NHItems.multipleSteel, NHBullets.artilleryMulti,
                    NHItems.thermoCoreNegative, NHBullets.artilleryNgt,
                    NHItems.fusionEnergy, NHBullets.artilleryFusion,
                    Items.phaseFabric, NHBullets.artilleryPhase
            );

            shooter(Items.phaseFabric, new ShootBarrel() {{
                barrels = new float[]{
                        5f, -3f, 0,
                        11f, -2f, 0,
                        -11f, -2f, 0,
                        -5f, -3f, 0,
                };

                barrelOffset = 1;
                shots = 2;
            }});

            targetAir = false;
            maxAmmo = 80;

            size = 4;
            health = 2600;
            outlineColor = Pal.darkOutline;

            squareSprite = false;

            coolantMultiplier /= 2;
            coolant = new ConsumeCoolant(0.6f);
            liquidCapacity = 90;

            requirements(Category.turret, with(NHItems.multipleSteel, 300, NHItems.seniorProcessor, 90, Items.plastanium, 120, NHItems.presstanium, 200, Items.phaseFabric, 100));
        }};

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

                    progress = PartProgress.warmup.blend(PartProgress.smoothReload, 0.5f);

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

        beamLaserTurret = new ItemTurret("beam-laser-turret") {{
            size = 2;
            requirements(Category.turret, BuildVisibility.shown, with(Items.copper, 60, NHItems.juniorProcessor, 60, NHItems.presstanium, 60));
            recoil = 1f;
            reload = 60f;

            shoot = new ShootPattern() {{
                shots = 3;
                shotDelay = 1.75f;
            }};

            canOverdrive = false;
            squareSprite = false;
            drawer = new DrawTurret("reinforced-");

            heatColor = Pal.turretHeat.cpy().lerp(Pal.redderDust, 0.5f).mul(1.1f);
            cooldownTime *= 2f;
            shootSound = NHSounds.laser5;
            range = 144f;
            shootCone = 30f;
            inaccuracy = 6f;
            maxAmmo = 80;
            ammoPerShot = 5;
            shootY += 5f;
            health = 600;
            smokeEffect = Fx.shootBigSmoke2;
            consumePowerCond(2f, TurretBuild::isActive);
            ammo(
                    Items.silicon, new AdaptedLaserBulletType(100) {{
                        colors = new Color[]{Pal.bulletYellowBack.cpy().mul(1f, 1f, 1f, 0.35f), Pal.bulletYellowBack, Color.white};
                        hitColor = Pal.bulletYellow;
                        length = 150f;
                        lifetime = 30f;
                        drawLine = false;
                        width = 8f;
                        lengthFalloff = 0.8f;
                        sideLength = 25f;
                        sideWidth = 0.7f;
                        sideAngle = 30f;
                        largeHit = false;
                        ammoMultiplier = 1;
                        shootEffect = NHFx.square(hitColor, 15f, 2, 8f, 2f);
                    }}
            );
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
                    progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
                }}, new RegionPart("-side-down") {{
                    mirror = true;
                    layerOffset = -0.5f;
                    moveX = 10f;
                    moveY = 45f;
                    y = 10f;
                    progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
                }}, new RegionPart("-side-down") {{
                    mirror = true;
                    layerOffset = -0.35f;
                    moveX = -9f;
                    moveY = 7f;
                    y = -2f;
                    x = 8;
                    progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
                }}, new RegionPart("-side-down") {{
                    under = mirror = true;
                    layerOffset = -0.2f;
                    moveY = -33f;
                    y = -33f;
                    x = 14;
                    progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
                }});

                parts.add(new ArcCharge() {{
                    progress = PartProgress.smoothReload.inv().curve(Interp.pow5Out);
                    color = NHColor.darkEnrColor;
                    chargeY = t -> -35f;
                    shootY = t -> 90 * curve.apply(1 - t.smoothReload);
                }});

//				parts.add(new FlarePart(){{
//					layer = Layer.effect;
//					rotMove = 360;
//					rotation = 45f;
//					followRotation = true;
//					y = 70f;
//					stroke = 7;
//					radius = 0;
//					radiusTo = 68;
//					color1 = NHColor.darkEnrColor;
//					color2 = NHColor.darkEnrFront;
//					progress = DrawPart.PartProgress.smoothReload.inv().curve(Interp.pow10In).delay(0.2f);
//				}
//					@Override
//					public void draw(PartParams params){
//						float z = Draw.z();
//						if(layer > 0) Draw.z(layer);
//
//						float prog = progress.getClamp(params);
//						int i = params.sideOverride == -1 ? 0 : params.sideOverride;
//
//						float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
//						Tmp.v1.set(x * sign, y).rotate(params.rotation - 90);
//
//						float
//								rx = params.x + Tmp.v1.x,
//								ry = params.y + Tmp.v1.y,
//								rot = (followRotation ? params.rotation : 0f) + rotMove * prog + rotation + Time.time * 0.86f,
//								rad = radiusTo < 0 ? radius : Mathf.lerp(radius, radiusTo, prog);
//
//						Draw.color(color1);
//						for(int j = 0; j < sides; j++){
//							Drawf.tri(rx, ry, stroke, rad, j * 360f / sides + rot);
//						}
//
//						Draw.color(color2);
//						for(int j = 0; j < sides; j++){
//							Drawf.tri(rx, ry, stroke * innerScl, rad * innerRadScl, j * 360f / sides + rot);
//						}
//
//						Draw.color();
//						Draw.z(z);
//					}
//				});
            }};

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

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 5000, NHItems.darkEnergy, 2000));
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

        pulseShotgun = new ItemTurret("pulse-shotgun") {{
            health = 960;
            range = 200;
            smokeEffect = Fx.shootBigSmoke;

            coolant = consumeCoolant(0.1F);

            shoot = new ShootSpread() {{
                shots = 12;
                shotDelay = 2f;
                spread = 0.55f;
            }};

            reload = 90f;
            recoil = 3f;
            shootCone = 30f;
            inaccuracy = 4f;
            size = 2;
            shootSound = Sounds.shootSnap;
            shake = 3f;
            ammo(
                    Items.titanium, new BasicBulletType(5, 24) {{
                        width = 8f;
                        height = 25f;
                        hitColor = backColor = lightColor = trailColor = Items.titanium.color.cpy().lerp(Color.white, 0.1f);
                        frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                        hitEffect = NHFx.crossBlast(hitColor, height + width);
                        shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 12f, 2f);
                        ammoMultiplier = 8;
                        pierceArmor = true;
                    }},

                    Items.plastanium, new BasicBulletType(5, 26) {{
                        width = 8f;
                        height = 25f;
                        fragBullets = 4;
                        fragBullet = new BasicBulletType(2, 26) {{
                            width = 3f;
                            lifetime = 10f;
                            height = 12f;
                            ammoMultiplier = 12;
                            hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
                            frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                            hitEffect = NHFx.lightningHitSmall(backColor);
                            shootEffect = despawnEffect = NHFx.square45_4_45;
                        }};
                        fragAngle = 130f;
                        fragVelocityMax = 1.1f;
                        fragVelocityMin = 0.5f;
                        fragLifeMax = 1.25f;
                        fragLifeMin = 0.25f;
                        ammoMultiplier = 12;
                        hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
                        frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                        hitEffect = NHFx.crossBlast(hitColor, height + width);
                        shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
                    }},

                    NHItems.zeta, new BasicBulletType(5, 18) {{
                        width = 8f;
                        height = 25f;
                        lightning = 2;
                        lightningLength = 2;
                        lightningLengthRand = 6;
                        lightningDamage = damage;
                        status = StatusEffects.shocked;
                        statusDuration = 15f;
                        ammoMultiplier = 12;
                        lightningColor = hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
                        frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                        hitEffect = NHFx.crossBlast(hitColor, height + width);
                        shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
                    }},

                    Items.pyratite, new BasicBulletType(5, 18) {{
                        width = 8f;
                        height = 25f;
                        incendAmount = 4;
                        incendChance = 0.25f;
                        incendSpread = 12f;
                        status = StatusEffects.burning;
                        statusDuration = 15f;
                        ammoMultiplier = 12;
                        hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
                        frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                        hitEffect = NHFx.crossBlast(hitColor, height + width);
                        despawnEffect = Fx.blastExplosion;
                        shootEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
                    }},

                    Items.blastCompound, new BasicBulletType(5, 22) {{
                        width = 8f;
                        height = 25f;
                        status = StatusEffects.blasted;
                        statusDuration = 15f;
                        splashDamageRadius = 12f;
                        splashDamage = damage;
                        ammoMultiplier = 8;
                        hitColor = backColor = lightColor = trailColor = Items.blastCompound.color.cpy().lerp(Color.white, 0.1f);
                        frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                        hitEffect = NHFx.crossBlast(hitColor, height + width);
                        despawnEffect = Fx.blastExplosion;
                        shootEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
                    }}
            );

            limitRange();
            maxAmmo = 120;
            ammoPerShot = 12;

            requirements(Category.turret, with(Items.copper, 30, Items.graphite, 40, NHItems.presstanium, 50, Items.lead, 60));
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

        bloodStar = new ItemTurret("blood-star") {{
            size = 5;
            coolant = consumeCoolant(0.2F);
            requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 230, NHItems.zeta, 300, NHItems.seniorProcessor, 200, NHItems.presstanium, 300, Items.thorium, 600));
            recoil = 5f;
            reload = 150f;
            range = 520f;
            unitSort = (u, x, y) -> -u.hitSize();
            shootSound = Sounds.laserblast;
            inaccuracy = 0f;
            shootCone = 15f;
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
        }};

        multipleLauncher = new ItemTurret("multiple-launcher") {{
            size = 3;
            health = 1250;
            coolant = consumeCoolant(0.2F);
            requirements(Category.turret, ItemStack.with(Items.plastanium, 60, NHItems.presstanium, 45, NHItems.metalOxhydrigen, 45, NHItems.juniorProcessor, 30));
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

        thermoTurret = new PowerTurret("thermo-turret") {{
            size = 1;
            health = 320;

            coolant = consumeCoolant(0.05F);
            requirements(Category.turret, BuildVisibility.shown, with(Items.titanium, 50, Items.copper, 50, Items.silicon, 25));
            shootType = new BasicBulletType(6.5f, 28f) {{
                hitEffect = new Effect(12.0F, (e) -> {
                    Draw.color(Pal.lancerLaser, Color.white, e.fout() * 0.75f);
                    Lines.stroke(e.fout() * 1.5F);
                    Angles.randLenVectors(e.id, 3, e.finpow() * 17.0F, e.rotation, 360.0F, (x, y) -> {
                        float ang = Mathf.angle(x, y);
                        Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4.0F + 1.0F);
                    });
                });
                trailWidth = 1.25f;
                trailLength = 15;

                knockback = 0.5f;
                trailColor = backColor = hitColor = Pal.lancerLaser;
                frontColor = Color.white;
                lifetime = 50f;
                homingDelay = 1f;
                homingPower = 0.2f;
                homingRange = 120f;
                status = StatusEffects.shocked;
                collidesGround = false;
                statusDuration = 30f;
                width = 5f;
                drawSize = 120f;
                height = 22f;
            }};
            consumePower(3.5f);

            shoot = new ShootPattern() {{
                shots = 5;
                shotDelay = 6f;
            }};

            inaccuracy = 3f;
            reload = 75f;
            shootCone = 50.0F;
            rotateSpeed = 8.0F;
            targetGround = false;
            range = 200.0F;
            shootEffect = Fx.lightningShoot;
            smokeEffect = Fx.shootSmallSmoke;
            heatColor = Color.red;
            recoil = 1.0F;
            shootSound = NHSounds.thermoShoot;
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
                        NHItems.upgradeSort, NHBullets.railGun3
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

                requirements(Category.turret, with(NHItems.setonAlloy, 150, Items.plastanium, 150, NHItems.seniorProcessor, 200, NHItems.zeta, 500, Items.phaseFabric, 125));
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

            requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 1500));
            ammo(NHItems.darkEnergy, NHBullets.arc_9000, NHItems.upgradeSort, new BulletType() {{
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
            shooter(NHItems.upgradeSort, new ShootBarrel() {{
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
        hydroFuelCell = new ConsumeGenerator("hydro-fuel-cell") {{
            size = 2;
            requirements(Category.power, ItemStack.with(NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 45, Items.graphite, 80, NHItems.presstanium, 60));

            lightColor = Pal.techBlue;
            consumeEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, Liquids.hydrogen.color.cpy().lerp(Liquids.nitrogen.color, 0.4f).a(0.56f));
            generateEffect = new Effect(45f, e -> {
                Draw.color(lightColor, Color.white, e.fin() * 0.66f);
                Lines.stroke(e.fout() * 1.375f);
                Lines.spikes(e.x, e.y, 0.45f + 5 * e.finpow(), 5.5f * e.fout(), 4, 45);
            });
            //			//NHTechTree.add(Blocks.thoriumReactor,this);
            powerProduction = 1800f / 60f;
            itemCapacity = 40;
            liquidCapacity = 30;
            itemDuration = 240f;
            consumeItem(NHItems.metalOxhydrigen, 4);
            consumeLiquid(Liquids.nitrogen, 1 / 60f);

            squareSprite = false;
            hasLiquids = hasItems = true;

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.nitrogen, 2f), new DrawDefault(), new DrawGlowRegion() {{
                color = Liquids.hydrogen.color;
            }});
        }};

        hugeBattery = new Battery("huge-battery") {{
            size = 5;
            health = 1600;
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(Items.phaseFabric, 40, NHItems.juniorProcessor, 20, NHItems.zeta, 80, NHItems.presstanium, 35, Items.graphite, 50));
            //			//NHTechTree.add(Blocks.batteryLarge, this);
            consumePowerBuffered(750_000);
        }};

        armorBatteryLarge = new Battery("large-armor-battery") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 40, NHItems.juniorProcessor, 10, Items.lead, 40));
            size = 3;
            health = 3000;
            armor = 15;
            consumePowerBuffered(45000.0F);
        }};

        hyperGenerator = new HyperGenerator("hyper-generator") {{
            size = 8;
            health = 12500;
            powerProduction = 2000f;
            updateLightning = updateLightningRand = 3;
            effectColor = NHColor.thermoPst;
            itemCapacity = 40;
            itemDuration = 180f;
            ambientSound = Sounds.pulse;
            ambientSoundVolume = 0.1F;
            toApplyStatus.add(NHStatusEffects.phased, StatusEffects.overclock);

            consumePower(100.0F);
            consumeItems(ItemStack.with(NHItems.thermoCoreNegative, 2, Items.phaseFabric, 4)).optional(true, true);
            consumeItems(new ItemStack(NHItems.metalOxhydrigen, 8), new ItemStack(NHItems.thermoCorePositive, 4));
            consumeLiquid(NHLiquids.zetaFluidNegative, 0.25F);
            requirements(Category.power, BuildVisibility.shown, with(NHItems.upgradeSort, 1000, NHItems.setonAlloy, 600, NHItems.irayrondPanel, 400, NHItems.presstanium, 1500, Items.surgeAlloy, 250, Items.metaglass, 250));
        }};
    }

    public static void load() {
        blaster = new ShockwaveGenerator("blaster") {{
            requirements(Category.defense, with(NHItems.presstanium, 80, Items.graphite, 100, Items.thorium, 100, NHItems.juniorProcessor, 60, NHItems.multipleSteel, 30));

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

            requirements(Category.defense, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 120, Items.copper, 80, Items.graphite, 60));
        }};

        airRaider = new AirRaider("air-raider") {{
            requirements(Category.defense, with(NHItems.upgradeSort, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 120, NHItems.juniorProcessor, 100, Items.phaseFabric, 150));

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

        remoteStorage = new RemoteCoreStorage("remote-vault") {{
            size = 3;
            health = 960;
            consumePower(20);
            requirements(Category.effect, BuildVisibility.shown, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 150, NHItems.multipleSteel, 120));
        }};

        unitIniter = new UnitSpawner("unit-initer");

        bombLauncher = new BombLauncher("bomb-launcher") {{
            requirements(Category.defense, with(NHItems.multipleSteel, 100, NHItems.presstanium, 260, NHItems.juniorProcessor, 120, Items.thorium, 500, Items.surgeAlloy, 75));
            //NHTechTree.add(Blocks.massDriver, this);
            size = 3;
            storage = 1;

            bullet = new BasicBulletType() {{
                trailChance = 0.25f;
                trailEffect = NHFx.trailToGray;
                trailParam = 1.5f;
                lifetime = 15f;

                smokeEffect = NHFx.hugeSmoke;
                shootEffect = NHFx.boolSelector;

                scaledSplashDamage = true;
                collidesTiles = collidesGround = collides = true;
                absorbable=true;
                damage = 100;
                splashDamage = 800f;
                lightningDamage = 400f;
                lightColor = lightningColor = trailColor = hitColor = NHColor.thurmixRed;
                lightning = 3;
                lightningLength = 8;
                lightningLengthRand = 16;
                splashDamageRadius = 120f;
                hitShake = despawnShake = 20f;
                hitSound = despawnSound = Sounds.explosionbig;
                hitEffect = despawnEffect = new OptionalMultiEffect(NHFx.crossBlast(hitColor, splashDamageRadius * 1.25f), NHFx.blast(hitColor, splashDamageRadius * 1.5f));
            }};

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

        largeMendProjector = new MendProjector("large-mend-projector") {{
            size = 3;
            reload = 180f;
            useTime = 600f;
            healPercent = 15;
            requirements(Category.effect, with(NHItems.presstanium, 60, NHItems.juniorProcessor, 50, Items.plastanium, 40, Items.thorium, 80));
//			//NHTechTree.add(Blocks.mendProjector, this);
            consumePower(2F);
            range = 160.0F;
            phaseBoost = 12f;
            phaseRangeBoost = 60.0F;
            health = 980;
            consumeItem(NHItems.juniorProcessor).boost();
        }};

        largeWaterExtractor = new SolidPump("large-water-extractor") {{
            size = 3;
            pumpAmount = 0.31f;
            requirements(Category.production, ItemStack.with(NHItems.presstanium, 50, NHItems.juniorProcessor, 45, Items.thorium, 60, Items.metaglass, 30));
//			//NHTechTree.add(Blocks.waterExtractor, this);
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
