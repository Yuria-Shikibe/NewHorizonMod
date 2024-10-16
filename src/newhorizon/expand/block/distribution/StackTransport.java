package newhorizon.expand.block.distribution;

import mindustry.gen.Building;
import mindustry.type.Item;

public interface StackTransport {

    default boolean isStacker(Building building){
        return building instanceof StackTransport;
    }

    default int acceptStacker(StackTransport source, Item item, int count){
        return 0;
    }
    default void handleStacker(StackTransport source, Item item, int count){}
}
