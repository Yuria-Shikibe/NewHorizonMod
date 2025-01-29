package newhorizon.expand.block.distribution.transport;

import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.world.blocks.distribution.DuctRouter;

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

    public class AdaptDirectionalBuild extends DuctRouterBuild implements LogisticBuild {
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
    }
}
