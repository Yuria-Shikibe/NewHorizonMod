package newhorizon.content.blocks;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHContent;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.block.unit.JumpGate;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class UnitBlock {
    public static Block jumpGateMK1, jumpGateMK2, jumpGateMk3;

    public static void load(){
        jumpGateMK1 = new JumpGate("primary-jump-gate"){{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 80,
                    NHItems.juniorProcessor, 80,
                    Items.tungsten, 80
            ));

            health = 1800;
            armor = 10f;
            size = 3;

            warmupPerSpawn = 0.2f;
            maxWarmupSpeed = 8f;

            maxRadius = 180f;
            minRadius = 40f;

            spawnList = Seq.with(UnitTypes.poly, UnitTypes.cleroi, UnitTypes.quasar, UnitTypes.zenith, UnitTypes.cyerce, NHUnitTypes.ghost, NHUnitTypes.warper, NHUnitTypes.aliotiat, NHUnitTypes.rhino, NHUnitTypes.gather);

            consumePowerCond(8, JumpGateBuild::canConsume);
        }};

        jumpGateMK2 = new JumpGate("standard-jump-gate"){{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 800,
                    NHItems.metalOxhydrigen, 300,
                    NHItems.juniorProcessor, 600,
                    NHItems.zeta, 1000
            ));

            warmupPerSpawn = 0.4f;
            maxWarmupSpeed = 4f;

            health = 10000;
            armor = 20f;
            size = 5;

            maxRadius = 240f;
            minRadius = 60f;

            spawnList = Seq.with(NHUnitTypes.naxos, NHUnitTypes.tarlidor, NHUnitTypes.zarkov, UnitTypes.eclipse, UnitTypes.disrupt, UnitTypes.corvus, UnitTypes.navanax, UnitTypes.collaris);

            consumePowerCond(30, JumpGateBuild::canConsume);

            blockDrawer = (building, valid) -> {
                TextureRegion arrowRegion = NHContent.arrowRegion;
                TextureRegion pointerRegion = NHContent.pointerRegion;

                Draw.z(Layer.bullet);

                float scl = building.warmup() * 0.125f;
                float rot = building.totalProgress();

                Draw.color(building.canConsume()? building.team.color: Pal.techBlue);
                Lines.stroke(8f * scl);
                Lines.square(building.x, building.y, building.block.size * tilesize / 2.5f, -rot);
                Lines.square(building.x, building.y, building.block.size * tilesize / 2f, rot);
                for(int i = 0; i < 4; i++){
                    float length = tilesize * building.block.size / 2f + 8f;
                    float rotation = i * 90;
                    float sin = Mathf.absin(building.totalProgress(), 16f, tilesize);
                    float signSize = 0.75f + Mathf.absin(building.totalProgress() + 8f, 8f, 0.15f);

                    Tmp.v1.trns(rotation + rot, -length);
                    Draw.rect(arrowRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, arrowRegion.width * scl, arrowRegion.height * scl, rotation + 90 + rot);
                    length = tilesize * building.block.size / 2f + 3 + sin;
                    Tmp.v1.trns(rotation, -length);
                    Draw.rect(pointerRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, pointerRegion.width * signSize * scl, pointerRegion.height * signSize * scl, rotation + 90);
                }
                Draw.color();
            };
        }};

        jumpGateMk3 = new JumpGate("hyper-jump-gate"){{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 1800,
                    NHItems.metalOxhydrigen, 800,
                    NHItems.seniorProcessor, 800,
                    NHItems.multipleSteel, 1000,
                    NHItems.irayrondPanel, 400
            ));

            warmupPerSpawn = 0.5f;
            maxWarmupSpeed = 3f;

            health = 80000;
            armor = 20f;
            size = 8;

            maxRadius = 320f;
            minRadius = 80f;

            spawnList = Seq.with(NHUnitTypes.destruction, NHUnitTypes.longinus, NHUnitTypes.annihilation, NHUnitTypes.saviour, NHUnitTypes.declining, NHUnitTypes.hurricane, NHUnitTypes.anvil, NHUnitTypes.sin, NHUnitTypes.collapser);

            consumePowerCond(60, JumpGateBuild::canConsume);
        }};
    }
}
