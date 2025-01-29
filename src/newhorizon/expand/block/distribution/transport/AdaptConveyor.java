package newhorizon.expand.block.distribution.transport;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.blocks.distribution.Conveyor;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;
import static mindustry.Vars.itemSize;

public class AdaptConveyor extends Conveyor {
    public TextureRegion[] edgeRegions, arrowRegions, pulseRegions;
    public float framePeriod = 8f;

    public AdaptConveyor(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;
    }

    @Override
    public void load() {
        super.load();
        edgeRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 32, 32, 1);
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1);
        pulseRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-pulse"), 32, 32, 1);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);

        if(bits == null) return;

        Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public boolean blends(Building self, Building other){
        if (other == null) return false;
        return blends(self.tile(), self.rotation, other.tileX(), other.tileY(), other.rotation, other.block);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public int conveyorFrame(){return (int)((((Time.time) % framePeriod) / framePeriod) * 16);}

    public int pulseFrame(){
        int value = (int) ((Time.time/4f) % 4f);
        if (value == 0) return 0;
        if (value == 1) return 1;
        if (value == 2) return 2;
        if (value == 3) return 1;
        return 0;
    }


    public class AdaptConveyorBuild extends ConveyorBuild {
        public int drawIndex = 0;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            drawIndex = 0;
            if (check(tile.x, tile.y + 1)) drawIndex += 1;
            if (check(tile.x + 1, tile.y)) drawIndex += 2;
            if (check(tile.x, tile.y - 1)) drawIndex += 4;
            if (check(tile.x - 1, tile.y)) drawIndex += 8;
        }

        public boolean check(int x, int y){
            Building other = Vars.world.build(x, y);
            return blends(this, other);
        }

        @Override
        public void draw(){
            //draw extra conveyors facing this one for non-square tiling purposes
            /*
            Draw.z(Layer.blockUnder);
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = rotation - i;
                    float rot = i == 0 ? rotation * 90 : (dir)*90;

                    Draw.rect(sliced(regions[0][frame], i != 0 ? SliceMode.bottom : SliceMode.top), x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, rot);
                }
            }

             */

            Draw.blend(Blending.additive);
            Draw.color(team.color, Pal.gray, 0.35f);
            Draw.z(Layer.block - 0.25f);
            Draw.rect(pulseRegions[blendbits + pulseFrame() * 5], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);
            Draw.blend();

            Draw.color(team.color, Color.white, 0.65f);
            Draw.z(Layer.block - 0.2f);
            Draw.rect(arrowRegions[conveyorFrame()], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            boolean backDraw = true;
            if (blends(this, right())) {Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() + 90);backDraw = false;}
            if (blends(this, back())) {Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());backDraw = false;}
            if (blends(this, left())) {Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() - 90);backDraw = false;}
            if (backDraw){Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());}

            Draw.z(Layer.block - 0.15f);
            Draw.color(team.color, Color.white, 0.3f);
            Draw.rect(edgeRegions[blendbits], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);
            Draw.color();

            Draw.z(Layer.block - 0.1f);
            float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;

            for(int i = 0; i < len; i++){
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, tilesize, 0);
                Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

                float
                        ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x),
                        iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
            }
        }
    }
}
