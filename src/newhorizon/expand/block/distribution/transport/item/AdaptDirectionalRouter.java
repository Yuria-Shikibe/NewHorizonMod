package newhorizon.expand.block.distribution.transport.item;

import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.DuctRouter;
import newhorizon.expand.block.distribution.transport.LogisticBuild;
import newhorizon.expand.block.distribution.transport.LogisticsBlock;

public class AdaptDirectionalRouter extends DuctRouter {
    public AdaptConveyor cBlock;

    public AdaptDirectionalRouter(String name, AdaptConveyor conveyorBlock) {
        super(name);

        this.cBlock = conveyorBlock;
        placeableLiquid = true;
        drawTeamOverlay = false;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        LogisticsBlock.drawPlan(plan, list, 2);
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class AdaptDirectionalRouterBuild extends DuctRouterBuild implements LogisticBuild {
        public int upperIndex;

        @Override
        public void draw() {
            LogisticsBlock.draw(this, cBlock, upperIndex, 2, sortItem);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            upperIndex = LogisticsBlock.proximityUpperIndex(this);
        }

        @Override
        public boolean canSend(Building target) {
            if (target instanceof LogisticBuild){
                if (target == front()) return true;
                if (target == right()) return true;
                if (target == left()) return true;

                if (target == back()) return false;
            }
            return false;
        }

        @Override
        public boolean canReceive(Building source) {
            if (source instanceof LogisticBuild){
                if (source == front()) return false;
                if (source == right()) return false;
                if (source == left()) return false;

                if (source == back()) return true;
            }
            return false;
        }
    }
}
