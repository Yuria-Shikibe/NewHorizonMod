package newhorizon.expand.draw;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidBlock;
import mindustry.world.draw.DrawLiquidTile;
import mindustry.world.Block;
import mindustry.graphics.Layer;

public class DrawLiquidAnimatedOffset extends DrawLiquidTile {
    public float bubbleChance = 0.05f;      // 每帧生成气泡概率
    public float bubbleLifetime = 40f;      // 气泡生存时间
    public float bubbleRadius = 1.5f;       // 气泡初始半径
    public float bubbleRise = 6f;           // 气泡上升高度
    public Color bubbleColor = Color.white; // 气泡颜色（默认自动取液体颜色）

    // --- 新增部分 ---
    public float offsetX = 0f;       // 固定偏移X（相对建筑中心）
    public float offsetY = 0f;       // 固定偏移Y
    public boolean glow = false;     // 是否发光
    public float glowRadius = 20f;   // 发光半径
    public float glowAlpha = 0.4f;   // 发光强度
    // ----------------

    public static final Effect liquidBubble = new Effect(40f, e -> {
        Draw.color(e.color, Color.white, e.fin());
        Fill.circle(e.x, e.y + e.fin() * 6f, 1.5f - e.fin() * 1.2f);
        Draw.color();
    });

    public DrawLiquidAnimatedOffset() {
        super();
    }

    @Override
    public void draw(Building build) {
        Liquid liquidToDraw = drawLiquid != null ? drawLiquid : build.liquids.current();
        if (liquidToDraw == null) return;

        float fullness = build.liquids.get(liquidToDraw) / build.block.liquidCapacity;
        if (fullness <= 0.001f) return;

        Vec2 offset = Tmp.v1.set(offsetX, offsetY).rotate(build.rotation * 90f);

        // ======= 液体绘制（tile 风格） =======
        Draw.z(Layer.block-0.1f);
        // 调整液体层
        LiquidBlock.drawTiledFrames(
                build.block.size,
                build.x + offset.x,
                build.y + offset.y,
                this.padLeft,
                this.padRight,
                this.padTop,
                this.padBottom,
                liquidToDraw,
                fullness * this.alpha
        );

        // ======= 发光 =======
        if (glow) {
            mindustry.graphics.Drawf.light(
                    build.x + offset.x,
                    build.y + offset.y,
                    glowRadius,
                    liquidToDraw.lightColor,
                    glowAlpha * fullness
            );
        }

        // ======= 冒泡逻辑 =======
        if (Mathf.chanceDelta(bubbleChance * fullness)) {
            Color c = (bubbleColor == Color.white) ? liquidToDraw.color : bubbleColor;
            float bx = build.x + Mathf.range(build.block.size * 2f);
            float by = build.y + Mathf.range(build.block.size * 2f);
            liquidBubble.at(bx, by, 0f, c);
        }
    }

    @Override
    public void load(Block block) {
        super.load(block);
        if (this.padLeft < 0) this.padLeft = 0;
        if (this.padRight < 0) this.padRight = 0;
        if (this.padTop < 0) this.padTop = 0;
        if (this.padBottom < 0) this.padBottom = 0;
    }
}
