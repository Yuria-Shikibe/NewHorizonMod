package newhorizon.expand.block.distribution.transport.item;

import arc.Graphics;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.world.blocks.distribution.OverflowDuct;
import newhorizon.expand.block.distribution.transport.LogisticBuild;
import newhorizon.expand.block.distribution.transport.LogisticsBlock;

import static mindustry.Vars.*;

public class AdaptDirectionalGate extends OverflowDuct {
    public AdaptConveyor cBlock;

    public AdaptDirectionalGate(String name, AdaptConveyor cBlock) {
        super(name);
        this.cBlock = cBlock;

        saveConfig = true;
        placeableLiquid = true;
        drawTeamOverlay = false;

        config(Boolean.class, (AdaptDirectionalGateBuild build, Boolean invert) -> build.invert = invert);
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        LogisticsBlock.drawPlan(plan, list, 0);
    }

    public class AdaptDirectionalGateBuild extends OverflowDuctBuild implements LogisticBuild {
        public boolean invert;
        public int upperIndex;


        public boolean invert(){
            return invert;
        }

        @Override
        public void draw() {
            if (!invert()){
                LogisticsBlock.draw(this, cBlock, upperIndex, 0, null);
            }else {
                LogisticsBlock.draw(this, cBlock, upperIndex, 1, null);
            }
        }

        @Override
        public void tapped() {
            super.tapped();
            Fx.placeBlock.at(this, size);
            Sounds.click.at(this);
            configure(!invert);
        }

        @Override
        public Graphics.Cursor getCursor(){
            return interactable(player.team()) ? Graphics.Cursor.SystemCursor.hand : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public Object config() {
            return invert;
        }

        @Nullable
        public Building target(){
            if(current == null) return null;

            if(invert()){ //Lots of extra code. Make separate UnderflowDuct class?
                Building l = left(), r = right();
                boolean lc = l != null && l.team == team && l.acceptItem(this, current),
                        rc = r != null && r.team == team && r.acceptItem(this, current);

                if(lc && !rc){
                    return l;
                }else if(rc && !lc){
                    return r;
                }else if(lc){
                    return cdump == 0 ? l : r;
                }
            }

            Building front = front();
            if(front != null && front.team == team && front.acceptItem(this, current)){
                return front;
            }

            if(invert()) return null;

            for(int i = -1; i <= 1; i++){
                int dir = Mathf.mod(rotation + (((i + cdump + 1) % 3) - 1), 4);
                if(dir == rotation) continue;
                Building other = nearby(dir);
                if(other != null && other.team == team && other.acceptItem(this, current)){
                    return other;
                }
            }

            return null;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            upperIndex = LogisticsBlock.proximityUpperIndex(this);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(invert);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            invert = read.bool();
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
