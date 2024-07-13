package newhorizon.expand.block.distribution;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.BufferItem;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.type.Item;
import mindustry.world.DirectionalItemBuffer;
import mindustry.world.blocks.liquid.LiquidJunction;
import mindustry.world.meta.BlockGroup;
import newhorizon.util.graphic.FloatPlatformDrawer;

import static mindustry.Vars.content;
import static mindustry.Vars.world;

//junction which allow both liquid and item to go through
public class FloatMultiJunction extends LiquidJunction {
    public float speed = 26; //frames taken to go through this junction
    public int capacity = 6;

    public FloatMultiJunction(String name) {
        super(name);
        placeableLiquid = true;
        update = true;
        solid = false;
        underBullets = true;
        group = BlockGroup.transportation;
        unloadable = false;
        floating = true;
        noUpdateDisabled = true;
    }

    @Override
    public boolean outputsItems(){
        return true;
    }

    public class FloatMultiJunctionBuild extends LiquidJunctionBuild {
        public DirectionalItemBuffer buffer = new DirectionalItemBuffer(capacity);

        public Seq<Building> proximityBuild = new Seq<>();
        public boolean ab;
        public boolean[] edge, corner;

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return 0;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            for(int i = 0; i < 4; i++){
                if(buffer.indexes[i] > 0){
                    if(buffer.indexes[i] > capacity) buffer.indexes[i] = capacity;
                    long l = buffer.buffers[i][0];
                    float time = BufferItem.time(l);

                    if(Time.time >= time + speed / timeScale || Time.time < time){

                        Item item = content.item(BufferItem.item(l));
                        Building dest = nearby(i);

                        //skip blocks that don't want the item, keep waiting until they do
                        if(item == null || dest == null || !dest.acceptItem(this, item) || dest.team != team){
                            continue;
                        }

                        dest.handleItem(this, item);
                        System.arraycopy(buffer.buffers[i], 1, buffer.buffers[i], 0, buffer.indexes[i] - 1);
                        buffer.indexes[i] --;
                    }
                }
            }
        }

        @Override
        public void handleItem(Building source, Item item){
            int relative = source.relativeTo(tile);
            buffer.accept(relative, item);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            int relative = source.relativeTo(tile);

            if(relative == -1 || !buffer.accepts(relative)) return false;
            Building to = nearby(relative);
            return to != null && to.team == team;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            buffer.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buffer.read(read);
        }

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
                if (other instanceof FloatConveyor.FloatConveyorBuild) {
                    tmpTiles.add(other);
                }
            }
            for (Building tile : tmpTiles) {
                proximityBuild.add(tile);
            }

            updateDrawRegion();
        }

        public void updateProximity() {
            super.updateProximity();

            updateProximityBuild();
            for (Building other : proximityBuild) {
                if (other instanceof FloatConveyor.FloatConveyorBuild) {
                    FloatConveyor.FloatConveyorBuild build = (FloatConveyor.FloatConveyorBuild) other;
                    build.updateProximityBuild();
                }
            }
        }

        @Override
        public void onRemoved(){
            for (Building other : proximityBuild) {
                if (other instanceof FloatConveyor.FloatConveyorBuild) {
                    FloatConveyor.FloatConveyorBuild build = (FloatConveyor.FloatConveyorBuild) other;
                    build.updateProximityBuild();
                }
            }
            super.onRemoved();
        }
    }
}
