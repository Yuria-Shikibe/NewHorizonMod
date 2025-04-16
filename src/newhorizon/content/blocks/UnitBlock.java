package newhorizon.content.blocks;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.unit.JumpGate;

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

            size = 3;

            spawnList = Seq.with(UnitTypes.poly);

            //consumePowerCond(8, (JumpGateBuild e) -> e.canConsume());
        }};
    }
}
