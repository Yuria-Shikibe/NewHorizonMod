package newhorizon.content.blocks;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.ForceProjector;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.defence.AdaptRegenProjector;
import newhorizon.expand.block.defence.AdaptWall;
import newhorizon.expand.block.defence.ShieldGenerator;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static mindustry.Vars.renderer;
import static mindustry.type.ItemStack.with;

public class DefenseBlock {
    public static Block
            presstaniumWall, refactoringMultiWall, setonPhasedWall, shapedWall,
            standardRegenProjector, heavyRegenProjector, standardForceProjector, largeShieldGenerator, riftShield;

    public static void load() {
        presstaniumWall = new AdaptWall("presstanium-wall") {{
            health = 1200;
            armor = 2f;
            maxShareStep = 1;
            damageReduction = 0.1f;

            requirements(Category.defense, with(NHItems.presstanium, 10, NHItems.juniorProcessor, 6));
        }};

        refactoringMultiWall = new AdaptWall("refactoring-multi-wall") {{
            health = 1800;
            armor = 4f;
            maxShareStep = 2;
            damageReduction = 0.2f;

            requirements(Category.defense, with(NHItems.metalOxhydrigen, 8, Items.carbide, 12));
        }};

        setonPhasedWall = new AdaptWall("seton-phased-wall") {{
            health = 2400;
            armor = 8f;
            maxShareStep = 2;
            damageReduction = 0.3f;

            requirements(Category.defense, with(NHItems.setonAlloy, 6, NHItems.irayrondPanel, 8));
        }};

        shapedWall = new AdaptWall("shaped-wall") {{
            health = 3000;
            armor = 10f;
            damageReduction = 0.5f;

            crushDamageMultiplier = 0.5f;

            requirements(Category.defense, with(NHItems.nodexPlate, 8, NHItems.ancimembrane, 8));
        }};

        standardRegenProjector = new AdaptRegenProjector("standard-regen-projector") {{
            requirements(Category.effect, with(
                    NHItems.presstanium, 60,
                    NHItems.juniorProcessor, 50

            ));
            size = 2;
            health = 1200;
            armor = 8;
            range = 25;
            healPercent = (100 / 15f) / 60f;
            baseHeal = 50f / 60f;

            consumePower(2f);
            consumeLiquid(NHLiquids.xenFluid, 12f / 60f);
            consumeItem(NHItems.phaseFabric).boost();
        }};

        heavyRegenProjector = new AdaptRegenProjector("heavy-regen-projector") {{
            requirements(Category.effect, with(
                    NHItems.multipleSteel, 60,
                    NHItems.seniorProcessor, 50
            ));
            size = 3;
            health = 3000;
            armor = 20;
            range = 40;
            healPercent = (100 / 6f) / 60f;
            baseHeal = 200f / 60f;
            optionalMultiplier = 4f;

            consumePower(5f);
            consumeLiquid(NHLiquids.irdryonFluid, 6f / 60f);
            consumeItem(NHItems.ancimembrane).boost();
        }};


        Effect forceShrink = new Effect(20, e -> {
            color(e.color, e.fout());
            if(renderer.animateShields){
                Fill.poly(e.x, e.y, 4, e.rotation * e.fout(), 45f);
            }else{
                stroke(1.5f);
                Draw.alpha(0.09f);
                Fill.poly(e.x, e.y, 4, e.rotation * e.fout(), 45f);
                Draw.alpha(1f);
                Lines.poly(e.x, e.y, 4, e.rotation * e.fout(), 45f);
            }
        }).layer(Layer.shields);

        standardForceProjector = new ForceProjector("standard-shield-generator") {{
            requirements(Category.effect, with(NHItems.juniorProcessor, 150, NHItems.presstanium, 150, Items.carbide, 50));

            size = 2;
            sides = 4;
            health = 1500;
            armor = 10;
            itemCapacity = 20;
            liquidCapacity = 25f;
            shieldRotation = 45f;
            radius = 140f;
            shieldHealth = 2500f;
            cooldownNormal = 10f;
            cooldownBrokenBase = shieldHealth / (5f * 60);
            phaseRadiusBoost = 80f;
            phaseShieldBoost = 2500f;
            itemConsumer = consumeItem(NHItems.zeta).boost();
            consumePower(5f);

            shieldBreakEffect = new Effect(40, e -> {
                color(e.color);
                stroke(3f * e.fout());
                Lines.poly(e.x, e.y, 4, e.rotation + e.fin(), 45f);
            }).followParent(true);

            buildType = () -> new ForceBuild(){
                @Override
                public void onRemoved(){
                    float radius = realRadius();
                    if(!broken && radius > 1f) forceShrink.at(x, y, radius, team.color);
                }
            };
        }};

        largeShieldGenerator = new ForceProjector("large-shield-generator") {{
            requirements(Category.effect, with(NHItems.seniorProcessor, 200, NHItems.presstanium, 200, Items.phaseFabric, 150, NHItems.multipleSteel, 100));

            size = 4;
            sides = 4;
            health = 3000;
            armor = 20;
            itemCapacity = 20;
            liquidCapacity = 50f;
            shieldRotation = 45f;
            radius = 240f;
            shieldHealth = 25000f;
            cooldownNormal = 25f;
            cooldownBrokenBase = shieldHealth / (20f * 60);
            phaseRadiusBoost = 120f;
            phaseShieldBoost = 25000f;
            itemConsumer = consumeItem(NHItems.fusionEnergy).boost();
            consumePower(25f);

            shieldBreakEffect = new Effect(40, e -> {
                color(e.color);
                stroke(3f * e.fout());
                Lines.poly(e.x, e.y, 4, e.rotation + e.fin(), 45f);
            }).followParent(true);

            buildType = () -> new ForceBuild(){
                @Override
                public void onRemoved(){
                    float radius = realRadius();
                    if(!broken && radius > 1f) forceShrink.at(x, y, radius, team.color);
                }
            };
        }};

        riftShield = new ShieldGenerator("rift-shield") {{
            requirements(Category.effect, with(NHItems.setonAlloy, 300, NHItems.ancimembrane, 350, NHItems.seniorProcessor, 400, NHItems.nodexPlate, 300));
        }};

    }
}
