package newhorizon.expand.draw;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.util.Tmp;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.draw.DrawArcSmelt;

/**
 * DrawArcSmelt 的液体自适应版本（可旋转）。
 * 自动根据工厂液体颜色变化并平滑过渡，
 * 并随建筑旋转保持相对位置。
 */
public class DrawLiquidSmelt extends DrawArcSmelt {

    // 平滑速度
    public float colorLerp = 0.1f;

    // 当前颜色缓存
    private final Color currentFlame = new Color();
    private final Color currentMid = new Color();

    @Override
    public void draw(Building build) {
        if (build == null) return;

        // === 液体颜色获取 ===
        Color target = (build.liquids != null && build.liquids.currentAmount() > 0f)
                ? build.liquids.current().color
                : flameColor;

        // 平滑颜色渐变
        currentFlame.lerp(target, colorLerp * Time.delta);
        currentMid.lerp(Tmp.c1.set(target).shiftHue(0.08f), colorLerp * Time.delta);

        this.flameColor.set(currentFlame);
        this.midColor.set(currentMid);

        // === 偏移旋转处理 ===
        float rotation = build.rotation * 90f;
        float rx = build.x + Angles.trnsx(rotation, x, y);
        float ry = build.y + Angles.trnsy(rotation, x, y);

        // === 层级调整 ===
        Draw.z(Layer.block + 0.05f);

        // === 临时偏移注入 ===
        float oldX = this.x;
        float oldY = this.y;
        this.x = rx - build.x;
        this.y = ry - build.y;

        super.draw(build);

        // 还原偏移
        this.x = oldX;
        this.y = oldY;
    }

    @Override
    public void load(Block block) {
        super.load(block);
        currentFlame.set(flameColor);
        currentMid.set(midColor);
    }
}
