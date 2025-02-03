package newhorizon.expand.block.distribution.transport.item;

import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.Junction;
import newhorizon.expand.block.distribution.transport.LogisticBuild;
import newhorizon.expand.block.distribution.transport.LogisticsBlock;

public class AdaptJunction extends Junction {
    public AdaptConveyor cBlock;

    public AdaptJunction(String name, AdaptConveyor conveyorBlock) {
        super(name);

        this.cBlock = conveyorBlock;
        placeableLiquid = true;
        drawTeamOverlay = false;
    }


    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        LogisticsBlock.drawPlan(plan, list, 3);
    }

    public class AdaptJunctionBuild extends JunctionBuild implements LogisticBuild {
        public int upperIndex;

        @Override
        public void draw() {
            LogisticsBlock.draw(this, cBlock, upperIndex, 3, null);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            upperIndex = LogisticsBlock.proximityUpperIndex(this);
        }

        @Override
        public boolean canSend(Building target) {
            return true;
        }

        @Override
        public boolean canReceive(Building source) {
            return true;
        }
    }
}
