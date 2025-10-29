package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawPistonsRotated extends DrawBlock {
    public float x = 0f, y = 0f;
    public TextureRegion region;
    public int sides = 4;
    public float sinMag = 4f;
    public float sinScl = 6f;
    public float sideOffset = 0f;

    @Override
    public void load(Block block) {
        region = Core.atlas.find(block.name + "-piston", block.region);
    }

    @Override
    public void draw(Building build) {
        if(region == null) return;

        Vec2 pos = Tmp.v1.trns(build.rotdeg(), x, y).add(build.x, build.y);

        float progress = Time.time * sinScl;

        for(int i = 0; i < sides; i++){
            float angle = i * 360f / sides + sideOffset + build.rotdeg();
            float offset = Mathf.sin(build.totalProgress() * sinScl + i) * sinMag;

            float dx = pos.x + Mathf.cosDeg(angle) * offset;
            float dy = pos.y + Mathf.sinDeg(angle) * offset;

            Draw.rect(region, dx, dy, angle);
        }
    }
}
