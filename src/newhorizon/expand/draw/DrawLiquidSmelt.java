package newhorizon.expand.draw;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.draw.DrawArcSmelt;
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

        // ====================== 层级与绘制（基于父类实现，但加入旋转） ======================
        if (build.warmup() > 0f && this.flameColor.a > 0.001f) {
            Draw.z(Layer.block + 0.05f);

            Lines.stroke(this.circleStroke * build.warmup());
            float si = Mathf.absin(this.flameRadiusScl, this.flameRadiusMag);
            float a = this.alpha * build.warmup();

            Draw.blend(this.blending);

            // 计算旋转后的偏移（将 this.x,this.y 围绕原点按建筑角度旋转）
            float angleDeg = build.rotdeg(); // 建筑实际角度（度）
            float cos = Mathf.cosDeg(angleDeg);
            float sin = Mathf.sinDeg(angleDeg);
            float rx = this.x * cos - this.y * sin; // 旋转后的 x 偏移
            float ry = this.x * sin + this.y * cos; // 旋转后的 y 偏移

            Draw.color(this.midColor, a);
            if (this.drawCenter) {
                Fill.circle(build.x + rx, build.y + ry, this.flameRad + si);
            }

            Draw.color(this.flameColor, a);
            if (this.drawCenter) {
                Lines.circle(build.x + rx, build.y + ry, (this.flameRad + this.circleSpace + si) * build.warmup());
            }

            Lines.stroke(this.particleStroke * build.warmup());
            float base = Time.time / this.particleLife;
            rand.setSeed((long) build.id);

            for (int i = 0; i < this.particles; ++i) {
                float fin = (rand.random(1.0f) + base) % 1.0f;
                float fout = 1.0f - fin;
                float angle = rand.random(360.0f);

                // 使粒子角度随建筑旋转（加入 angleDeg 偏移）
                float particleAngle = angle + angleDeg;

                float len = this.particleRad * Interp.pow2Out.apply(fin);
                float px = Angles.trnsx(particleAngle, len) + build.x + rx;
                float py = Angles.trnsy(particleAngle, len) + build.y + ry;

                Lines.lineAngle(px, py, particleAngle, this.particleLen * fout * build.warmup());
            }

            Draw.blend();
            Draw.reset();
        }
    }

    @Override
    public void load(Block block) {
        super.load(block);
        currentFlame.set(flameColor);
        currentMid.set(midColor);
    }
}
