package newhorizon.content.blocks;

import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.PlaceholderBlock;

/**
 * blocks that are never supposed to used by player.
 */
public class InnerBlock {
    public static final int maxsize = 4;
    public static LinkBlock[] linkEntity, linkEntityLiquid;
    public static PlaceholderBlock[] placeholderEntity;

    public static void load() {
        linkEntity = new LinkBlock[maxsize];
        linkEntityLiquid = new LinkBlock[maxsize];
        placeholderEntity = new PlaceholderBlock[maxsize];
        for (int i = 0; i < maxsize; i++) {
            int s = i + 1;
            linkEntity[i] = new LinkBlock("link-entity-" + s) {{
                size = s;
            }};
            linkEntityLiquid[i] = new LinkBlock("link-entity-liquid-" + s) {{
                size = s;
                outputsLiquid = true;
            }};
            placeholderEntity[i] = new PlaceholderBlock("placeholder-entity-" + s) {{
                size = s;
            }};
        }
    }
}
