package newhorizon.expand.block.distribution;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.content.Blocks;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.meta.BlockGroup;
import newhorizon.util.graphic.FloatPlatformDrawer;

import static mindustry.Vars.world;

public class FloatMultiRouter extends LiquidRouter {
    public float speed = 8f;

    public FloatMultiRouter(String name) {
        super(name);
        placeableLiquid = true;
        solid = false;
        underBullets = true;
        update = true;
        hasItems = true;
        itemCapacity = 1;
        group = BlockGroup.transportation;
        unloadable = false;
        noUpdateDisabled = true;
    }

    public class FloatMultiRouterBuild extends LiquidRouterBuild {
        public Item lastItem;
        public Tile lastInput;
        public float time;
        public @Nullable BlockUnitc unit;

        @Override
        public void updateTile(){
            super.updateTile();

            if(lastItem == null && items.any()){
                lastItem = items.first();
            }

            if(lastItem != null){
                time += 1f / speed * delta();
                Building target = getTileTarget(lastItem, lastInput, false);

                if(target != null && (time >= 1f || !(target.block instanceof Router || target.block.instantTransfer))){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                }
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return team == source.team && lastItem == null && items.total() == 0;
        }

        @Override
        public void handleItem(Building source, Item item){
            items.add(item, 1);
            lastItem = item;
            time = 0f;
            lastInput = source.tile();
        }

        @Override
        public int removeStack(Item item, int amount){
            int result = super.removeStack(item, amount);
            if(result != 0 && item == lastItem){
                lastItem = null;
            }
            return result;
        }

        public Building getTileTarget(Item item, Tile from, boolean set){
            if(unit != null){
                unit.health(health);
                unit.ammo(unit.type().ammoCapacity * (items.total() > 0 ? 1f : 0f));
                unit.team(team);
                unit.set(x, y);

                int angle = Mathf.mod((int)((angleTo(unit.aimX(), unit.aimY()) + 45) / 90), 4);

                if(unit.isShooting()){
                    Building other = nearby(rotation = angle);
                    if(other != null && other.acceptItem(this, item)){
                        return other;
                    }
                }

                return null;
            }

            int counter = rotation;
            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + counter) % proximity.size);
                if(set) rotation = ((byte)((rotation + 1) % proximity.size));
                if(other.tile == from && from.block() == Blocks.overflowGate) continue;
                if(other.acceptItem(this, item)){
                    return other;
                }
            }
            return null;
        }

        //draw part

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
