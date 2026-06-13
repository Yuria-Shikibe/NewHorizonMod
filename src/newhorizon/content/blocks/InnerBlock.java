package newhorizon.content.blocks;

import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.PlaceholderBlock;

/**
 * blocks that are never supposed to used by player.
 */
public class InnerBlock {
    public static PlaceholderBlock placeholder;

    public static void load() {
        placeholder = new PlaceholderBlock("construction-placeholder");
    }
}
