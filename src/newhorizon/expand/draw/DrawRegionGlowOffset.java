package newhorizon.expand.draw;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.draw.DrawBlock;

public class DrawRegionGlowOffset extends DrawBlock {
    public TextureRegion region;
    public float offsetX = 0f, offsetY = 0f;
    public float scale = 1f;
    public boolean rotateWithBlock = true;

    public DrawRegionGlowOffset() {}

    public DrawRegionGlowOffset(TextureRegion region, float offsetX, float offsetY, float scale) {
        this.region = region;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scale = scale;
    }

    @Override
    public void draw(Building build) {
        if (region == null) return;

        // 计算偏移向量
        float drawX = build.x;
        float drawY = build.y;
        if (offsetX != 0f || offsetY != 0f) {
            if (rotateWithBlock) {
                Tmp.v1.trns(build.rotdeg(), offsetX, offsetY);
                drawX += Tmp.v1.x;
                drawY += Tmp.v1.y;
            } else {
                drawX += offsetX;
                drawY += offsetY;
            }
        }

        Draw.z(Layer.block + 0.01f);

        Draw.rect(region, drawX, drawY, rotateWithBlock ? build.rotdeg() : 0f, scale, scale);

        Draw.color();
    }
}
