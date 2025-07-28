package newhorizon.content.blocks;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.CharacterOverlay;
import newhorizon.expand.block.special.CharacterDisplay;
import newhorizon.expand.block.special.IconDisplay;

public class LogicBlock {
    public static Block iconDisplay, iconDisplaySmall, characterDisplay, characterDisplaySmall;

    public static void load() {
        iconDisplay = new IconDisplay("icon-display") {{
            requirements(Category.logic, ItemStack.with(Items.silicon, 12));
        }};

        iconDisplaySmall = new IconDisplay("icon-display-small") {{
            requirements(Category.logic, ItemStack.with(Items.silicon, 4));
            size = 1;
        }};

        characterDisplay = new CharacterDisplay("character-display") {{
            requirements(Category.logic, ItemStack.with(Items.silicon, 12));
        }};

        characterDisplaySmall = new CharacterDisplay("character-display-small") {{
            requirements(Category.logic, ItemStack.with(Items.silicon, 4));
            size = 1;
        }};
    }
}
