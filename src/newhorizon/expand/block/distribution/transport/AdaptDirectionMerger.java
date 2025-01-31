package newhorizon.expand.block.distribution.transport;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Edges;

public class AdaptDirectionMerger extends AdaptDirectionalRouter{
    public AdaptDirectionMerger(String name, AdaptConveyor conveyorBlock) {
        super(name, conveyorBlock);
    }

    public class AdaptDirectionalMergerBuild extends AdaptDirectionalRouterBuild{

        public void draw() {
            LogisticsBlock.draw(this, cBlock, upperIndex, 4, sortItem);
        }

        @Nullable
        public Building target(){
            if(front().team == team && front().acceptItem(this, current)){
                return front();
            }

            return null;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return current == null && items.total() == 0 && (item == sortItem || sortItem == null) &&
                    (Edges.getFacingEdge(source.tile(), tile).relativeTo(tile) != (rotation + 2) % 4);
        }

        @Override
        public int removeStack(Item item, int amount){
            int removed = super.removeStack(item, amount);
            if(item == current) current = null;
            return removed;
        }
    }
}
