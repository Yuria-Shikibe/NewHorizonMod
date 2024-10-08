package newhorizon.expand.block.distribution;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.world.blocks.distribution.Conveyor;
import newhorizon.util.graphic.FloatPlatformDrawer;

import static mindustry.Vars.world;

public class FloatConveyor extends Conveyor {
    public FloatConveyor(String name) {
        super(name);
        placeableLiquid = true;
    }

    public class FloatConveyorBuild extends ConveyorBuild {
        public Seq<Building> proximityBuild = new Seq<>();
        public boolean ab;
        public boolean[] edge, corner;

        @Override
        public void draw(){
            super.draw();
            if (tile.floor().isLiquid){
                FloatPlatformDrawer.drawPlatform(this, ab, edge, corner);
            }
        }

        public void updateDrawRegion(){
            ab = FloatPlatformDrawer.getEdgeAB(this);
            edge = FloatPlatformDrawer.getEdge(this);
            corner = FloatPlatformDrawer.getCorner(this);
        }

        public void updateProximityBuild(){
            tmpTiles.clear();
            proximityBuild.clear();

            for (Point2 point : Geometry.d8) {
                Building other = world.build(tile.x + point.x, tile.y + point.y);
                if (other == null || other.team != team) continue;
                if (other instanceof FloatConveyorBuild) {
                    tmpTiles.add(other);
                }
            }
            for (Building tile : tmpTiles) {
                proximityBuild.add(tile);
            }

            updateDrawRegion();
        }

        @Override
        public void updateProximity() {
            super.updateProximity();

            updateProximityBuild();
            for (Building other : proximityBuild) {
                if (other instanceof FloatConveyorBuild) {
                    FloatConveyorBuild build = (FloatConveyorBuild) other;
                    build.updateProximityBuild();
                }
            }
        }

        @Override
        public void onRemoved(){
            for (Building other : proximityBuild) {
                if (other instanceof FloatConveyorBuild) {
                    FloatConveyorBuild build = (FloatConveyorBuild) other;
                    build.updateProximityBuild();
                }
            }
            super.onRemoved();
        }
    }
}
