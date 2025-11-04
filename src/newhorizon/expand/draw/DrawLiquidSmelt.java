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
import mindustry.type.Liquid;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;


/**
 * DrawArcSmelt 的液体自适应版本（可旋转）。
 * 自动根据工厂液体颜色变化并平滑过渡，
 * 并随建筑旋转保持相对位置。
 */
public class DrawLiquidSmelt extends DrawArcSmelt {

    // 平滑颜色变化速度
    public float colorLerp = 0.1f;

    // 当前颜色缓存
    private final Color currentFlame = new Color();
    private final Color currentMid = new Color();

    // 固定透明度（外部可配置）
    public float fixedAlpha = 0.68f;

    @Override
    public void draw(Building build) {
        if (build == null) return;

        // ====================== 液体识别逻辑 ======================
        Liquid liquidToDraw = null;

        if (build instanceof RecipeGenericCrafter.RecipeGenericCrafterBuild crafterBuild) {
            var current = crafterBuild.getRecipe();
            if (current != null) {
                if (current.inputLiquid != null && current.inputLiquid.size > 0) {
                    liquidToDraw = current.inputLiquid.first().liquid;
                } else if (current.outputLiquid != null && current.outputLiquid.size > 0) {
                    liquidToDraw = current.outputLiquid.first().liquid;
                }
            }
        }

        if (liquidToDraw == null && build.liquids != null) {
            liquidToDraw = build.liquids.current();
        }

        // ====================== 颜色渐变 ======================
        Color target = (liquidToDraw != null) ? liquidToDraw.color : flameColor;

        currentFlame.lerp(target, colorLerp * Time.delta);
        currentMid.lerp(Tmp.c1.set(target).shiftHue(0.08f), colorLerp * Time.delta);

        this.flameColor.set(currentFlame);
        this.midColor.set(currentMid);

        // ====================== 固定透明度 ======================
        this.alpha = fixedAlpha;

        // ====================== 层级调整 ======================
        Draw.z(Layer.block + 0.05f);

        // ====================== 绘制 ======================
        super.draw(build);
    }

    @Override
    public void load(Block block) {
        super.load(block);
        currentFlame.set(flameColor);
        currentMid.set(midColor);
    }
}
