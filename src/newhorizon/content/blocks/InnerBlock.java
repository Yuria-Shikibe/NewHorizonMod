package newhorizon.content.blocks;

import newhorizon.expand.block.inner.LinkBlock;

/** blocks that are never supposed to used by player. */
public class InnerBlock {
    public static LinkBlock[] linkEntity;

    public static void load(){
        linkEntity = new LinkBlock[8];
        for (int i = 0; i < linkEntity.length; i++){
            int s = i + 1;
            linkEntity[i] = new LinkBlock("link-entity-" + s){{
                size = s;
            }};
        }
    }
}
