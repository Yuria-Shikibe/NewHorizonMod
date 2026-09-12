package newhorizon.content.blocks;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.ConsumeItemExplode;
import mindustry.world.consumers.ConsumeItemFlammable;
import mindustry.world.draw.*;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import newhorizon.content.*;
import newhorizon.expand.block.drawer.DrawBaseRegion;
import newhorizon.expand.block.drawer.DrawRotation;
import newhorizon.expand.block.power.GravityWell;
import newhorizon.expand.block.power.MultiBlockConsumeGenerator;
import newhorizon.expand.block.special.HyperReactor;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class PowerBlock {
    public static Block
            fluxNodeMK1, fluxNodeMK2, fluxNodeLargeMK1, fluxNodeLargeMK2,
          //serpulo generators
            photothermalGenerator, geologicalPhotothermalGenerator,
          //erekir generators
            vectorCondenser, differentialReactor,
            photonPanel,xenExtractor,
            neutralizationGenerator, hydrazineGenerator, fissionReactor, fusionReactor, hyperReactor,
            armorBattery, armorBatteryLarge, armorBatteryHuge,
            gravityTrapSmall, gravityTrap;

    public static void load() {

        fluxNodeMK1 = new PowerNode("flux-node-mk1") {{
            requirements(Category.power, with(
                    NHItems.hardLight, 3,
                    NHItems.silicar, 6
            ));

            maxNodes = 12;
            laserRange = 8;
            underBullets = true;
            crushFragile = true;
            drawTeamOverlay = false;
        }};

        fluxNodeMK2 = new PowerNode("flux-node-mk2") {{
            requirements(Category.power, with(
                    NHItems.hardLight, 12,
                    NHItems.metalOxhydrigen, 6,
                    NHItems.fissileMatter, 3
            ));

            maxNodes = 16;
            laserRange = 12;
            underBullets = true;
            drawTeamOverlay = false;
        }};

        fluxNodeLargeMK1 = new PowerNode("flux-node-large-mk1") {{
            requirements(Category.power, with(
                    NHItems.hardLight, 12,
                    NHItems.titanium, 6,
                    NHItems.tungsten, 3
            ));

            size = 2;
            maxNodes = 18;
            laserRange = 18f;
            drawTeamOverlay = false;
        }};

        fluxNodeLargeMK2 = new PowerNode("flux-node-large-mk2") {{
            requirements(Category.power, with(
                    NHItems.hardLight, 24,
                    NHItems.carbide, 6,
                    NHItems.zeta, 3
            ));

            size = 2;
            maxNodes = 24;
            laserRange = 24f;
            drawTeamOverlay = false;
        }};

        photothermalGenerator = new ConsumeGenerator("photothermal-generator") {
            final float hlTime = 120f;

            {
                requirements(Category.power, with(NHItems.copper, 35, NHItems.lead, 25));
                powerProduction = 0.8f;
                itemDuration = 120f;

                ambientSound = Sounds.loopSmelter;
                ambientSoundVolume = 0.03f;
                generateEffect = Fx.generatespark;

                consume(new ConsumeItemFlammable());
                consume(new ConsumeItemExplode());

                itemDurationMultipliers.put(NHItems.pyratite, 3f);

                drawer = new DrawMulti(new DrawDefault(), new DrawWarmupRegion());

                buildType = () -> new ConsumeGeneratorBuild() {
                    public float produceTime = 0f;

                    @Override
                    public void updateTile() {
                        super.updateTile();

                        produceTime += warmup * edelta() * efficiencyMultiplier;
                        if (produceTime > hlTime) {
                            if (core() != null) core().handleItem(this, NHItems.hardLight);
                            produceTime %= hlTime;
                        }
                    }

                    @Override
                    public void write(Writes write) {
                        super.write(write);
                        write.f(produceTime);
                    }

                    @Override
                    public void read(Reads read, byte revision) {
                        super.read(read, revision);
                        produceTime = read.f();
                    }
                };
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(Stat.output, NHStatValues.itemsWithEfficiency(hlTime, ItemStack.with(NHItems.hardLight, 1)));
            }
        };

        geologicalPhotothermalGenerator = new ThermalGenerator("geological-photothermal-generator") {
            final float hlTime = 120f;

            {
                requirements(Category.power, with(
                        NHItems.lead, 200,
                        NHItems.graphite, 150,
                        NHItems.metaglass, 75,
                        NHItems.plastanium, 75,
                        NHItems.juniorProcessor, 50
                ));

                size = 3;
                floating = true;

                powerProduction = 172f / 60f;

                drawer = new DrawMulti(
                        new DrawDefault(),
                        new DrawGlowRegion() {{
                            alpha = 0.6f;
                            glowScale = 5f;
                            color = Color.valueOf("8966ff");
                        }}
                );

                ambientSound = Sounds.loopHum;
                ambientSoundVolume = 0.06f;

                buildType = () -> new ThermalGeneratorBuild() {
                    public float produceTime = 0f;

                    @Override
                    public void updateTile() {
                        super.updateTile();

                        produceTime += delta();
                        if (produceTime > hlTime) {
                            if (core() != null) core().handleItem(this, NHItems.hardLight);
                            produceTime %= hlTime;
                        }
                    }

                    @Override
                    public void write(Writes write) {
                        super.write(write);
                        write.f(produceTime);
                    }

                    @Override
                    public void read(Reads read, byte revision) {
                        super.read(read, revision);
                        produceTime = read.f();
                    }
                };
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(Stat.output, NHStatValues.itemsWithEfficiency(hlTime, ItemStack.with(NHItems.hardLight, 1)));
            }
        };

        vectorCondenser = new ThermalGenerator("vector-condenser") {
            final float hlTime = 120f;

            {
                requirements(Category.power, with(
                        NHItems.beryllium, 60,
                        NHItems.graphite, 40
                ));

                size = 3;
                displayEfficiency = false;
                fogRadius = 3;
                liquidCapacity = 30f;

                attribute = Attribute.steam;
                displayEfficiencyScale = 1f / 9f;
                minEfficiency = 9f - 0.0001f;
                powerProduction = 7.5f / 9f;

                drawer = new DrawMulti(
                        new DrawDefault(),
                        new DrawBlurSpin("-rotator", 0.8f * 9f) {{
                            blurThresh = 0.01f;
                        }}
                );

                generateEffect = Fx.turbinegenerate;
                effectChance = 0.04f;

                ambientSound = Sounds.loopHum;
                ambientSoundVolume = 0.06f;

                buildType = () -> new ThermalGeneratorBuild() {
                    public float produceTime = 0f;

                    @Override
                    public void updateTile() {
                        super.updateTile();

                        produceTime += delta();
                        if (produceTime > hlTime) {
                            if (core() != null) core().handleItem(this, NHItems.hardLight);
                            produceTime %= hlTime;
                        }
                    }

                    @Override
                    public void write(Writes write) {
                        super.write(write);
                        write.f(produceTime);
                    }

                    @Override
                    public void read(Reads read, byte revision) {
                        super.read(read, revision);
                        produceTime = read.f();
                    }
                };
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(Stat.output, NHStatValues.itemsWithEfficiency(hlTime, ItemStack.with(NHItems.hardLight, 2)));
            }
        };

        differentialReactor = new ConsumeGenerator("differential-reactor"){
            final float hlTime = 120f;
            {

                requirements(Category.power, with(
                        NHItems.graphite, 100,
                        NHItems.oxide, 100f,
                        NHItems.carbide, 100,
                        NHItems.silicon, 200
                ));
                size = 4;

                powerProduction = 2100f / 60f;
                consumeLiquids(LiquidStack.with(NHLiquids.cryofluid, 16f / 60f, NHLiquids.slag, 20f / 60f));

                /*drawer = new DrawMulti(
                        new DrawBaseRegion("-4x4"),
                        new DrawPistons(){{
                            sides = 4;
                            sinMag = 3f;
                            sinScl = 5f;
                            angleOffset = 45f;
                            lenOffset = 9f;
                        }},
                        new DrawRegion("-mid"),
                        new DrawLiquidTile(NHLiquids.slag, 40f / 4f),
                        new DrawDefault(),
                        new DrawGlowRegion(){{
                            alpha = 0.7f;
                            glowScale = 5f;
                            color = NHLiquids.cryofluid.color.cpy();
                        }}
                );*/

                ambientSound = Sounds.loopSmelter;
                ambientSoundVolume = 0.06f;

                buildType = () -> new ConsumeGeneratorBuild() {
                    public float produceTime = 0f;

                    @Override
                    public void updateTile() {
                        super.updateTile();

                        if (efficiency <= 0f) return;

                        produceTime += delta();
                        if (produceTime > hlTime) {
                            if (core() != null) core().handleItem(this, NHItems.hardLight);
                            produceTime %= hlTime;
                        }
                    }

                    @Override
                    public void write(Writes write) {
                        super.write(write);
                        write.f(produceTime);
                    }

                    @Override
                    public void read(Reads read, byte revision) {
                        super.read(read, revision);
                        produceTime = read.f();
                    }
                };
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(Stat.output, NHStatValues.itemsWithEfficiency(hlTime, ItemStack.with(NHItems.hardLight, 1)));
            }
        };

        photonPanel = new SolarGenerator("photon-panel") {
            public final float produceTime = 300f;
            public final int produceTimer = timers++;
            public TextureRegion topRegion;
            public TextureRegion[] baseRegions, reflectRegions;

            {
                requirements(Category.power, with(
                        NHItems.silicar, 20
                ));
                size = 3;
                powerProduction = 0.5f;

                buildType = () -> new SolarGeneratorBuild() {
                    boolean justCreated = true;

                    @Override
                    public void draw() {
                        Draw.rect(baseRegions[Mathf.randomSeed(id, 0, baseRegions.length - 1)], x, y);
                        Draw.rect(reflectRegions[Mathf.randomSeed(id + 123, 0, baseRegions.length - 1)], x, y);
                        Draw.rect(topRegion, x, y);
                    }

                    @Override
                    public void updateTile() {
                        super.updateTile();
                        if (core() != null && timer(produceTimer, produceTime / productionEfficiency)) {
                            if (!justCreated) {
                                core().handleItem(this, NHItems.hardLight);
                            } else {
                                justCreated = false;
                            }
                        }
                    }
                };
            }

            @Override
            public void load() {
                super.load();

                baseRegions = new TextureRegion[3];
                reflectRegions = new TextureRegion[3];

                topRegion = Core.atlas.find(name + "-top");
                for (int i = 0; i < 3; i++) {
                    baseRegions[i] = Core.atlas.find(name + "-base" + (i + 1));
                    reflectRegions[i] = Core.atlas.find(name + "-reflect" + (i + 1));
                }
            }

            @Override
            public void setStats() {
                super.setStats();
                stats.add(Stat.output, NHStatValues.itemsWithSolarMultiplier(produceTime, ItemStack.with(NHItems.hardLight, 1)));
            }
        };

        xenExtractor = new ThermalGenerator("xen-extractor") {{
            requirements(Category.production, with(NHItems.presstanium, 40, NHItems.juniorProcessor, 40));
            attribute = NHContent.quantum;
            displayEfficiencyScale = 1f / 9f;
            minEfficiency = 9f - 0.0001f;
            powerProduction = 240.0001f / 60f / 9f;
            displayEfficiency = false;
            effectChance = 0.2f;
            generateEffect = new OptionalMultiEffect(
                    NHFx.square(NHColor.lightSkyFront, 60, 6, 32, 3),
                    new Effect(40f, 80f, e -> {
                        Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin() * 0.8f);
                        Lines.stroke(2f * e.fout());
                        Lines.spikes(e.x, e.y, 12 * e.finpow(), 1.5f * e.fout() + 4 * e.fslope(), 4, 45);
                    })
            );
            effectChance = 0.04f;
            size = 3;
            squareSprite = false;

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(NHLiquids.xenFluid, 2f),
                    new DrawRegion()
            );

            hasLiquids = true;
            outputLiquid = new LiquidStack(NHLiquids.xenFluid, 12f / 60f / 9f);
            liquidCapacity = 300f;
            health = 1200;
            armor = 8;
        }};

        neutralizationGenerator = new MultiBlockConsumeGenerator("neutralization-generator") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.titanium, 20,
                    NHItems.silicon, 50,
                    NHItems.graphite, 20
            ));
            addLink(p(2, 0), p(2, 1), p(-1, 0), p(-1, 1), p(0, -1), p(1, -1));

            canMirror = true;
            rotations = new int[]{0, 3, 2, 1, 2, 1, 0, 3};

            size = 2;
            health = 450;
            hasItems = true;
            hasLiquids = true;

            consumeLiquids(LiquidStack.with(NHLiquids.ammonia, 12 / 60f));
            outputLiquid = new LiquidStack(NHLiquids.water, 12f / 60f);
            powerProduction = 6f;

            drawer = new DrawMulti(
                    new DrawRotation() {{
                        drawType = DRAW_X_MIRROR;
                        suffix = "-inner";
                    }},
                    new DrawRotation() {{
                        drawType = DRAW_Y_MIRROR;
                        suffix = "-outer";
                        xOffset = 12f;
                    }},
                    new DrawRotation() {{
                        drawType = DRAW_Y_MIRROR;
                        suffix = "-outer";
                        xOffset = 12f;
                        rotOffset = 3;
                    }},
                    new DrawRotation() {{
                        drawType = DRAW_Y_MIRROR;
                        suffix = "-outer";
                        xOffset = 12f;
                        rotOffset = 2;
                    }}
            );

            consumeEffect = generateEffect = NHFx.square(Pal.power, 60, 6, 16, 3);

            enableRotate();
        }};

        hydrazineGenerator = new ConsumeGenerator("hydrazine-generator") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.presstanium, 50,
                    NHItems.juniorProcessor, 50,
                    NHItems.tungsten, 30
            ));

            size = 3;
            hasLiquids = true;
            scaledHealth = 100f;

            consumeLiquids(LiquidStack.with(NHLiquids.hydrazine, 4 / 60f));
            powerProduction = 20f;

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawGlowRegion() {{
                        alpha = 0.6f;
                        glowScale = 5f;
                        color = Color.valueOf("f3b9ca");
                    }},
                    new DrawPlasma(),
                    new DrawLiquidRegion(NHLiquids.hydrazine),
                    new DrawDefault()

            );
        }};

        fissionReactor = new NuclearReactor("fission-reactor") {{
            requirements(Category.power, with(
                    NHItems.titanium, 200,
                    NHItems.juniorProcessor, 100,
                    NHItems.carbide, 50,
                    NHItems.metalOxhydrigen, 50,
                    NHItems.fissileMatter, 25

            ));
            size = 3;
            health = 1200;
            itemDuration = 300f;
            heating = 0.02f;
            heatOutput = 30f;
            fuelItem = NHItems.fissileMatter;

            consumeItem(NHItems.fissileMatter, 1);
            consumeLiquid(NHLiquids.cryofluid, (float) (3.5 / 60f)).update(false);
            powerProduction = 1800f / 60f;

            drawer = new DrawMulti(
                    new DrawBaseRegion("-3x3"),
                    new DrawLiquidTile(NHLiquids.cryofluid),
                    new DrawDefault()
            );

            ambientSound = Sounds.loopThoriumReactor;
            ambientSoundVolume = 0.11f;
        }};


        fusionReactor = new MultiBlockConsumeGenerator("fusion-reactor") {{
            requirements(Category.power, ItemStack.with(
                    NHItems.metalOxhydrigen, 200,
                    NHItems.carbide, 400,
                    NHItems.multipleSteel, 400,
                    NHItems.seniorProcessor, 200
            ));

            addLink(
                    p(-3, 2), p(-3,1), p(-3, 0), p(-3, -1), p(-3, -2),
                    p(-2, 3), p(-1,3), p(0, 3), p(1, 3), p(2, 3),
                    p(3, 2), p(3, 1), p(3, 0), p(3, -1), p(3, -2),
                    p(-2, -3), p(-1, -3), p(0, -3), p(1, -3), p(2, -3)
            );

            size = 5;
            hasItems = true;
            hasLiquids = true;
            itemCapacity = 60;
            liquidCapacity = 120;
            itemDuration = 240f;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(NHLiquids.irdryonFluid),
                    new DrawPlasma(),
                    new DrawDefault()
            );

            consumeItems(ItemStack.with(NHItems.fusionEnergy, 12));
            consumeLiquids(LiquidStack.with(NHLiquids.irdryonFluid, 11.6 / 60f));
            powerProduction = 22000f / 60f;
        }};

        hyperReactor = new HyperReactor("hyper-reactor") {{
            requirements(Category.power, BuildVisibility.shown, with(
                    NHItems.nodexPlate, 800,
                    NHItems.setonAlloy, 600,
                    NHItems.irayrondPanel, 400,
                    NHItems.presstanium, 1500,
                    NHItems.surgeAlloy, 250
            ));

            size = 8;
            health = 40000;
            armor = 50f;
            powerProduction = 4000f;
            updateLightning = updateLightningRand = 3;
            effectColor = NHColor.thermoPst;
            itemCapacity = 40;
            itemDuration = 180f;
            //ambientSound = Sounds.pulse;
            ambientSoundVolume = 0.1F;

            consumePower(100.0F);
//            consumeItems(ItemStack.with(NHItems.thermoCoreNegative, 6, NHItems.phaseFabric, 6)).optional(true, true);
            consumeItems(ItemStack.with(NHItems.thermoCorePositive, 4, NHItems.thermoCoreNegative, 4, NHItems.metalOxhydrigen, 3, NHItems.phaseFabric, 3));
            consumeLiquids(LiquidStack.with(NHLiquids.neutron, 6 / 60f, NHLiquids.proton, 6 / 60f));
//            consumeLiquids(new LiquidStack(NHLiquids.zetaFluidPositive, 8/60f)).optional(true, true);

        }};

        gravityTrapSmall = new GravityWell("gravity-trap-small") {{
            requirements(Category.power, BuildVisibility.shown, with(NHItems.titanium, 10, NHItems.tungsten, 8));

            size = 2;
            health = 640;
            gravityRange = 8 * tilesize;
        }};

        gravityTrap = new GravityWell("gravity-trap") {{
            requirements(Category.power, BuildVisibility.shown, with(NHItems.seniorProcessor, 15, NHItems.multipleSteel, 20));

            size = 3;
            health = 1250;
            gravityRange = 15 * tilesize;
        }};

        armorBattery = new Battery("armor-battery") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 10,
                    NHItems.juniorProcessor, 5
            ));
            size = 1;
            health = 600;
            armor = 20;
            consumePowerBuffered(10000f);
        }};

        armorBatteryLarge = new Battery("armor-battery-large") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                    NHItems.presstanium, 40,
                    NHItems.juniorProcessor, 10,
                    NHItems.zeta, 50
            ));
            size = 3;
            health = 2000;
            armor = 30;
            consumePowerBuffered(100000f);
        }};

        armorBatteryHuge = new Battery("armor-battery-huge") {{
            requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                    NHItems.multipleSteel, 60,
                    NHItems.seniorProcessor, 40,
                    NHItems.zeta, 200
            ));
            size = 5;
            health = 5000;
            armor = 50;
            consumePowerBuffered(1000000f);
        }};
    }
}
