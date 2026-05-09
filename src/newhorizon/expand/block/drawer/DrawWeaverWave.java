package newhorizon.expand.block.drawer;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.draw.DrawBlock;
import newhorizon.content.NHItems;

public class DrawWeaverWave extends DrawBlock {
    public Color color = NHItems.phaseFabric.color;
    public float waveStroke = 0.8f, waveLength = 5.5f, waveSpacing = 1.75f, waveAmount = 19f, waveAngle = 90f, waveMagOffset = Mathf.PI;
    public float sinMag = 4f, sinScl = 6f, sinOffset = 50f, sideOffset = 0f, angleOffset = 90f;

    @Override
    public void draw(Building build){
        for (int i = 0; i < waveAmount; i++) {
            float offset = -(waveSpacing * (waveAmount - 1)) / 2 + i * waveSpacing;
            float waveOffset = Mathf.absin(build.totalProgress() + sinOffset + sideOffset + waveMagOffset * i, sinScl, sinMag);

            Tmp.v1.trns(0, offset + waveOffset).rotate(build.rotdeg()).add(build);
            Draw.color(color);
            Draw.alpha(build.warmup());
            Lines.stroke(waveStroke);
            Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, waveAngle + build.rotdeg(), waveLength);
            Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, waveAngle + build.rotdeg() + 180f, waveLength);
            Draw.color();
        }
    }
}
