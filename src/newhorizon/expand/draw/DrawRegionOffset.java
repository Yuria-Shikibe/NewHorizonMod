package newhorizon.expand.draw;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.draw.DrawBlock;
import mindustry.graphics.Layer; // 引入 Layer

public class DrawRegionOffset extends DrawBlock {
    public String suffix = "";           // 贴图后缀
    public TextureRegion region;         // 对应贴图
    public float offsetX = 0f;           // x偏移（世界单位）
    public float offsetY = 0f;           // y偏移（世界单位）
    public boolean followRotation = true; // 是否随方块旋转
    public boolean blink = false;        // 是否闪烁
    public float blinkSpeed = 1f;        // 闪烁速度，数值越大闪烁越快
    public float layer = Layer.block + 0.05f; // 新增：可控制绘制层

    public DrawRegionOffset() {}

    public DrawRegionOffset(String suffix, float offsetX, float offsetY, boolean blink, float blinkSpeed) {
        this.suffix = suffix;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.blink = blink;
        this.blinkSpeed = blinkSpeed;
    }

    @Override
    public void load(mindustry.world.Block block) {
        super.load(block);
        region = Core.atlas.find(block.name + suffix);
    }

    @Override
    public void draw(Building build) {
        if (region == null || !region.found()) return;

        float x = build.x;
        float y = build.y;

        if (followRotation) {
            Tmp.v1.trns(build.rotdeg(), offsetX, offsetY);
            x += Tmp.v1.x;
            y += Tmp.v1.y;
        } else {
            x += offsetX;
            y += offsetY;
        }

        Draw.z(layer); // 设置绘制层

        if (blink) {
            // 计算透明度，Mathf.absin 返回 -1~1，映射到 0~1
            float alpha = (Mathf.absin(Time.time * blinkSpeed, 1f, 1f) + 1f) / 2f;
            Draw.color(1f, 1f, 1f, alpha);
        } else {
            Draw.color();
        }

        Draw.rect(region, x, y, followRotation ? build.rotdeg() : 0f);

        // 重置颜色，避免影响后续绘制
        Draw.color();
    }
}
