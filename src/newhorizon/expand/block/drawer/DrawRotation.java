package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

import static mindustry.Vars.tilesize;

public class DrawRotation extends DrawBlock {
    //the type rule is applicable only to patterns, not the shade tones!
    //only one sprite with central symmetry pattern. name-suffix
    public static final int DRAW_NORMAL = 0;
    //draw only one sprite but rotate with building
    public static final int DRAW_ROTATED = 1;
    //pattern is symmetrical about the x-axis. name-suffix-0/1
    public static final int DRAW_X_MIRROR = 2;
    //pattern is symmetrical about the y-axis. name-suffix-0/1
    public static final int DRAW_Y_MIRROR = 3;
    //oblique pattern (L shape for example). name-suffix-0/1/2
    public static final int DRAW_OBLIQUE = 4;
    //sprite with full directions. name-suffix-0/1/2/3
    public static final int DRAW_FULL = 5;

    public TextureRegion[] rotRegions;
    public String suffix = "-rot";
    public float xOffset = 0, yOffset = 0, layer = -1;
    public int rotOffset = 0;
    public int drawType = DRAW_NORMAL;

    public DrawRotation() {}

    public static void drawRotation(float x, float y, int rotation, int drawType, TextureRegion[] regions) {
        if (regions == null || regions.length != 4) return;
        if (rotation < 0 || rotation > 4) return;

        int xSize = regions[rotation].width / tilesize * 2, ySize = regions[rotation].height / tilesize * 2, scl = rotation % 2 == 0? 1: -1;
        float rotDeg = rotation * 90f;

        switch (drawType) {
            case DRAW_NORMAL -> {
                switch (rotation) {
                    case 0, 2 -> Draw.rect(regions[rotation], x, y, xSize, ySize);
                    case 1, 3 -> Draw.rect(regions[rotation], x, y, xSize, ySize * scl, 90);
                }
            }
            case DRAW_ROTATED -> Draw.rect(regions[rotation], x, y, xSize, ySize, rotDeg);
            case DRAW_X_MIRROR -> Draw.rect(regions[rotation], x, y, xSize * scl, ySize, rotDeg);
            case DRAW_Y_MIRROR -> Draw.rect(regions[rotation], x, y, xSize, ySize * scl, rotDeg);
            case DRAW_OBLIQUE -> {
                if (rotation == 3){
                    Draw.rect(regions[rotation], x, y, xSize, ySize * scl);
                }else {
                    Draw.rect(regions[rotation], x, y, xSize, ySize, rotDeg);
                }
            }
            case DRAW_FULL -> Draw.rect(regions[rotation], x, y, rotDeg);
        }
    }

    @Override
    public void draw(Building build) {
        float z = Draw.z();
        if (layer > 0) Draw.z(layer);
        int rot = (build.rotation + rotOffset) % 4;
        Tmp.v1.set(xOffset, yOffset).rotate(rot * 90).add(build.x, build.y);
        drawRotation(Tmp.v1.x, Tmp.v1.y, rot, drawType, rotRegions);
        Draw.z(z);
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list) {
        int rot = (plan.rotation + rotOffset) % 4;
        Tmp.v1.set(xOffset, yOffset).rotate(rot * 90).add(plan.drawx(), plan.drawy());
        drawRotation(Tmp.v1.x, Tmp.v1.y, rot, drawType, rotRegions);
    }

    @Override
    public void load(Block block) {
        rotRegions = new TextureRegion[4];
        switch (drawType) {
            case DRAW_NORMAL, DRAW_ROTATED -> {
                for (int i = 0; i < 4; i++) {
                    rotRegions[i] = Core.atlas.find(block.name + suffix);
                }
            }
            case DRAW_X_MIRROR -> {
                rotRegions[0] = rotRegions[3] = Core.atlas.find(block.name + suffix + "-0");
                rotRegions[1] = rotRegions[2] = Core.atlas.find(block.name + suffix + "-1");
            }
            case DRAW_Y_MIRROR -> {
                rotRegions[0] = rotRegions[1] = Core.atlas.find(block.name + suffix + "-0");
                rotRegions[2] = rotRegions[3] = Core.atlas.find(block.name + suffix + "-1");
            }
            case DRAW_OBLIQUE -> {
                rotRegions[0] = Core.atlas.find(block.name + suffix + "-0");
                rotRegions[1] = rotRegions[3] = Core.atlas.find(block.name + suffix + "-1");
                rotRegions[2] = Core.atlas.find(block.name + suffix + "-2");
            }
            case DRAW_FULL -> {
                for (int i = 0; i < 4; i++) {
                    rotRegions[i] = Core.atlas.find(block.name + suffix + "-i");
                }
            }
        }
    }

}
