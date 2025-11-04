package newhorizon.expand.draw;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.draw.DrawPistons;

/**
 * DrawPistonsOffset - 带整体偏移，随方块旋转的活塞动画绘制类
 */
public class DrawPistonsOffset extends DrawPistons {

    /** 整体偏移，单位 world units（会随方块旋转） */
    public float offsetX = 0f;
    public float offsetY = 0f;

    /** 活塞初相位 */
    public float phaseOffset = 0f;

    @Override
    public void draw(Building build) {
        if (region1 == null && region2 == null && regiont == null) return;

        // 先把整体偏移旋转到方块朝向
        float baseX = build.x;
        float baseY = build.y;
        if (offsetX != 0f || offsetY != 0f) {
            Tmp.v3.trns(build.rotdeg(), offsetX, offsetY); // 将偏移旋转到方块方向
            baseX += Tmp.v3.x;
            baseY += Tmp.v3.y;
        }

        for (int i = 0; i < sides; i++) {
            // 计算活塞动画长度
            float len = Mathf.absin(build.totalProgress() + sinOffset + sideOffset * sinScl * i + phaseOffset, sinScl, sinMag) + lenOffset;

            // 计算当前活塞角度
            float angle = angleOffset + i * 360f / sides;

            // 选择贴图
            TextureRegion reg =
                    regiont.found() && (Mathf.equal(angle, 315) || Mathf.equal(angle, 135)) ? regiont :
                            angle >= 135 && angle < 315 ? region2 : region1;

            // 镜像 Y 轴
            if (Mathf.equal(angle, 315)) Draw.yscl = -1f;

            // 活塞偏移（旋转到方块朝向）
            Tmp.v1.trns(angle + build.rotdeg(), len, -horiOffset); // 关键：加上 build.rotdeg()
            Draw.rect(reg, baseX + Tmp.v1.x, baseY + Tmp.v1.y, angle + build.rotdeg());

            Draw.yscl = 1f;
        }

        Draw.color();
    }
}
