package newhorizon.expand.block.distribution.item.logistics;

import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.OverflowDuct;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.*;

public class AdaptDirectionalGate extends OverflowDuct {
    public TextureRegion baseRegion, overlayRegion, invertRegion;

    public AdaptDirectionalGate(String name) {
        super(name);

        saveConfig = true;
        placeableLiquid = true;
        drawTeamOverlay = false;

        config(Boolean.class, (AdaptDirectionalGateBuild build, Boolean invert) -> build.invert = invert);
    }

    @Override
    public void load() {
        super.load();
        baseRegion = Core.atlas.find(NewHorizon.name("logistics-base"));
        overlayRegion = Core.atlas.find(name + "-overlay");
        invertRegion = Core.atlas.find(name + "-invert");
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(baseRegion, plan.drawx(), plan.drawy());
        Draw.rect(overlayRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public class AdaptDirectionalGateBuild extends OverflowDuctBuild{
        public boolean invert;

        public boolean invert(){
            return invert;
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Draw.rect(invert ? invertRegion : overlayRegion, x, y, rotdeg());
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
        public void write(Writes write){
            super.write(write);
            write.bool(invert);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            invert = read.bool();
        }
    }
}
