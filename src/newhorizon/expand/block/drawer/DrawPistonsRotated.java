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
    public float sinOffset = 50f;
    public float lenOffset = 0f;
    public float angleOffset = 0f;
    public float horiOffset = 0f;

    @Override
    public void load(Block block) {
        region = Core.atlas.find(block.name + "-piston", block.region);
    }

    @Override
    public void draw(Building build) {
        if(region == null) return;
        Vec2 pos = Tmp.v1.set(x, y).rotate(build.rotdeg()).add(build.x, build.y);
        for(int i = 0; i < sides; i++){
            float phase = build.totalProgress() + sinOffset + sideOffset * sinScl * i;
            float offset = Mathf.absin(phase, sinScl, sinMag) + lenOffset;
            float angle = angleOffset + i * 360f / sides + build.rotdeg();
            Tmp.v1.trns(angle, offset, -horiOffset);
            Draw.rect(region, build.x + Tmp.v1.x + x, build.y + Tmp.v1.y + y, angle);
        }
    }
}
