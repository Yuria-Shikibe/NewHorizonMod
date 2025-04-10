package newhorizon.expand.block.distribution.item;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Edges;

public class AdaptDirectionalMerger extends AdaptDirectionalRouter{
    public AdaptDirectionalMerger(String name) {
        super(name);
    }

    public class AdaptDirectionalMergerBuild extends AdaptDirectionalRouterBuild{
        @Nullable
        public Building target(){
            if (front() == null) return null;
            if(front().team == team && front().acceptItem(this, current)){
                return front();
            }
            return null;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return current == null && items.total() == 0 && (sortItem == null || item == sortItem) &&
                    (Edges.getFacingEdge(source.tile, tile).relativeTo(tile) != (rotation + 2) % 4);
        }

        @Override
        public int removeStack(Item item, int amount){
            int removed = super.removeStack(item, amount);
            if(item == current) current = null;
            return removed;
        }
    }
}
