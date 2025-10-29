package newhorizon.expand.block.drawer;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawPistonsRotated extends DrawBlock {
    public float x = 0f, y = 0f;
    public TextureRegion region;
    public int sides = 8;
    public float sinMag = 2.75f;
    public float sinScl = 5f;
    public float sideOffset = 0f;

    @Override
    public void load(Block block) {
        region = Core.atlas.find(block.name + "-piston", block.region);
    }

    @Override
    public void draw(Building build) {
        if(region == null) return;

        Vec2 pos = Tmp.v1.trns(build.rotdeg(), x, y).add(build.x, build.y);

        for(int i = 0; i < sides; i++){
            float angle = i * 360f / sides + sideOffset + build.rotdeg();
            float offset = Mathf.sin(build.totalProgress() * sinScl + i) * sinMag;

            float dx = pos.x + Mathf.cosDeg(angle) * offset;
            float dy = pos.y + Mathf.sinDeg(angle) * offset;

            Draw.rect(region, dx, dy, angle);
        }
    }
}
