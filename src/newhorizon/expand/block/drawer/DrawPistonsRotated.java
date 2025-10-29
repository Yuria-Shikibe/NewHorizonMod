package newhorizon.expand.block.drawer;

import arc.math.geom.Vec2;
import arc.graphics.g2d.Draw;
import mindustry.gen.Building;
import mindustry.world.draw.DrawPistons;
import mindustry.graphics.TextureRegion;

public class DrawPistonsRotated extends DrawPistons {
    public float x = 0f, y = 0f;
    public TextureRegion region;

    public DrawPistonsRotated() {
        super();
    }

    @Override
    public void draw(Building build) {
        Vec2 pos = new Vec2(x, y).rotate(build.rotdeg()).add(build.x, build.y);

        for (int i = 0; i < sides; i++) {
            float angle = ((float) i / sides) * 360f + sideOffset + build.rotdeg();
            float offset = (float) Math.sin((build.totalProgress() * sinScl) + i) * sinMag;

            float dx = pos.x + (float) Math.cos(Math.toRadians(angle)) * offset;
            float dy = pos.y + (float) Math.sin(Math.toRadians(angle)) * offset;

            if(region != null){
                Draw.rect(region, dx, dy, angle);
            }
        }
    }
}