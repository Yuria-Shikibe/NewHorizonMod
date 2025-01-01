package newhorizon.content.blocks;

import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.PlaceholderBlock;

/** blocks that are never supposed to used by player. */
public class InnerBlock {
    public static LinkBlock[] linkEntity;
    public static PlaceholderBlock[] placeholderEntity;

    public static void load(){
        linkEntity = new LinkBlock[8];
        placeholderEntity = new PlaceholderBlock[8];
        for (int i = 0; i < linkEntity.length; i++){
            int s = i + 1;
            linkEntity[i] = new LinkBlock("link-entity-" + s){{
                size = s;
            }};
        }
        for (int i = 0; i < placeholderEntity.length; i++){
            int s = i + 1;
            placeholderEntity[i] = new PlaceholderBlock("placeholder-entity-" + s){{
                size = s;
            }};
        }
    }
}
