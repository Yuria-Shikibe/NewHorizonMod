package newhorizon.expand.block.distribution.transport.item;

import arc.util.Eachable;
import arc.util.Nullable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Edges;
import newhorizon.expand.block.distribution.transport.LogisticsBlock;

public class AdaptDirectionalMerger extends AdaptDirectionalRouter{
    public AdaptDirectionalMerger(String name, AdaptConveyor conveyorBlock) {
        super(name, conveyorBlock);
    }

    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        LogisticsBlock.drawPlan(plan, list, 4);
    }

    public class AdaptDirectionalMergerBuild extends AdaptDirectionalRouterBuild{

        public void draw() {
            LogisticsBlock.draw(this, cBlock, upperIndex, 4, sortItem);
        }

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
