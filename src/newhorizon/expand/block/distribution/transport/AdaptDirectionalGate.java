package newhorizon.expand.block.distribution.transport;

import arc.Core;
import arc.Graphics;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.distribution.OverflowDuct;
import newhorizon.NewHorizon;

import static mindustry.Vars.*;

public class AdaptDirectionalGate extends OverflowDuct {
    public TextureRegion upperRegion, edgeRegion1, edgeRegion2, topRegion1, topRegion2;
    public AdaptConveyor cBlock;

    public AdaptDirectionalGate(String name, AdaptConveyor cBlock) {
        super(name);
        this.cBlock = cBlock;

        saveConfig = true;

        config(Boolean.class, (AdaptDirectionalGateBuild build, Boolean invert) -> build.invert = invert);
    }

    public int conveyorFrame(){return cBlock.conveyorFrame();}

    public int pulseFrame(){return cBlock.pulseFrame();}

    @Override
    public void load() {
        super.load();
        upperRegion = Core.atlas.find(name + "-upper");
        topRegion1 = Core.atlas.find(name + "-top1");
        topRegion2 = Core.atlas.find(name + "-top2");
        edgeRegion1 = Core.atlas.find(NewHorizon.name("distribution-edge1"));
        edgeRegion2 = Core.atlas.find(NewHorizon.name("distribution-edge2"));
    }

    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(upperRegion, plan.drawx(), plan.drawy());
        Draw.rect(edgeRegion1, plan.drawx(), plan.drawy());
        Draw.rect(edgeRegion1, plan.drawx(), plan.drawy(), 90);
        Draw.rect(edgeRegion2, plan.drawx(), plan.drawy(), 180);
        Draw.rect(edgeRegion2, plan.drawx(), plan.drawy(), 270);
        Draw.rect(topRegion1, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public class AdaptDirectionalGateBuild extends OverflowDuctBuild {
        boolean invert;

        public boolean invert(){
            return invert;
        }

        @Override
        public void draw() {
            Draw.blend(Blending.additive);
            Draw.color(team.color, Pal.gray, 0.35f);
            Draw.z(Layer.block - 0.25f);
            Draw.rect(cBlock.pulseRegions[3 + pulseFrame() * 5], x, y);
            Draw.blend();

            for (int i = 0; i < 4; i++){
                Building b = world.build(tileX() + Geometry.d4x(i), tileY() + Geometry.d4y(i));
                if (b instanceof AdaptConveyor.AdaptConveyorBuild){
                    Draw.color(team.color, Color.white, 0.65f);
                    Draw.z(Layer.block - 0.2f);
                    if (b.rotation != (i+2)%4){
                        Draw.rect(cBlock.arrowRegions[conveyorFrame()], x, y, i * 90);
                    }else {
                        Draw.rect(cBlock.arrowRegions[conveyorFrame() + 16], x, y, i * 90 + 180);
                    }
                }else {
                    Draw.color();
                    Draw.z(Layer.block);
                    if (i <= 1){
                        Draw.rect(edgeRegion1, x, y, i * 90);
                    }else {
                        Draw.rect(edgeRegion2, x, y, i * 90);
                    }
                }
            }

            Draw.z(Layer.block - 0.1f);
            Drawf.shadow(x, y, 12, 1.5f);

            Draw.color();
            Draw.z(Layer.block);
            Draw.rect(upperRegion, x, y);

            if (!invert()){
                Draw.rect(topRegion1, x, y, rotdeg());
            }else {
                Draw.rect(topRegion2, x, y, rotdeg());
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
