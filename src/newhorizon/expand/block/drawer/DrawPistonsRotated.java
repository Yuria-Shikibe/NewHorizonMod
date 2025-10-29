package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.math.geom.Vec2;
import mindustry.gen.Building;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.blocks.distribution.DrawBlock;

public class DrawPistonsRotated extends DrawBlock {
    public float x = 0f, y = 0f;
    public TextureRegion region;
    public int sides = 8;
    public float sinMag = 2.75f;
    public float sinScl = 5f;
    public float sideOffset = 0f;

    public DrawPistonsRotated(TextureRegion region) {
        this.region = region;
    }

    @Override
    public void draw(Building build) {
        if(region == null) return;

        Vec2 pos = new Vec2(x, y).rotate(build.rotdeg()).add(build.x, build.y);

        for (int i = 0; i < sides; i++) {
            float angle = ((float) i / sides) * 360f + sideOffset + build.rotdeg();
            float offset = (float) Math.sin((build.totalProgress() * sinScl) + i) * sinMag;

            float dx = pos.x + (float) Math.cos(Math.toRadians(angle)) * offset;
            float dy = pos.y + (float) Math.sin(Math.toRadians(angle)) * offset;

            Draw.rect(region, dx, dy, angle);
        }
    }
}
