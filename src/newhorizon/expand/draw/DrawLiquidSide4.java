package newhorizon.expand.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawBlock;

/** Draws non-output sides for crafters using 4 side textures: block.name + "-0..3". */
public class DrawLiquidSide4 extends DrawBlock{
    public TextureRegion[] liquidSideRegions;

    @Override
    public void draw(Building build){
        GenericCrafter crafter = (GenericCrafter)build.block;
        drawSides(crafter, build.x, build.y, build.rotation);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        GenericCrafter crafter = (GenericCrafter)block;
        drawSides(crafter, plan.drawx(), plan.drawy(), plan.rotation);
    }

    @Override
    public void load(Block block){
        var crafter = expectCrafter(block);
        if(crafter.outputLiquids == null) return;

        liquidSideRegions = new TextureRegion[4];
        for(int side = 0; side < 4; side++){
            liquidSideRegions[side] = Core.atlas.find(block.name + "-side-" + side);
        }
    }

    protected void drawSides(GenericCrafter crafter, float x, float y, int rotation){
        if(crafter.outputLiquids == null) return;

        int outputMask = outputMask(crafter);
        for(int side = 0; side < 4; side++){
            if((outputMask & (1 << side)) != 0) continue;
            int offset=rotation==1||rotation==3?1:3;
            int worldSide = (side + rotation + offset) % 4;
            Draw.rect(liquidSideRegions[worldSide], x, y);
        }
    }

    protected int outputMask(GenericCrafter crafter){
        if(crafter.liquidOutputDirections == null) return 0b1111;

        int mask = 0;
        for(int i = 0; i < crafter.outputLiquids.length; i++){
            int side = i < crafter.liquidOutputDirections.length ? crafter.liquidOutputDirections[i] : -1;
            if(side == -1) return 0b1111;
            if(side >= 0 && side < 4){
                mask |= 1 << side;
            }
        }
        return mask;
    }
}
