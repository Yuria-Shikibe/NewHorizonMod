package newhorizon.expand.block.drawer;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.draw.DrawBlock;
import newhorizon.content.NHColor;
import newhorizon.util.func.MathUtil;

public class DrawParticleFlow extends DrawBlock {
    public float startX, startY, endX, endY;
    public float length = 1.5f, stroke = 0.6f;
    public float range = 4;
    public Color color = Pal.techBlue.cpy().lerp(Color.white, 0.4f);
    public float particleAlpha = 1f, particleLife = 90f;
    public int particles = 45;
    public boolean ignoreRot2_3 = false;

    @Override
    public void draw(Building build){

        if(build.warmup() > 0f && color.a > 0.001f){
            Lines.stroke(stroke * build.warmup());
            float ang = Angles.angle(startX, startY, endX, endY);
            if (ignoreRot2_3 && build.rotation > 1) ang += 180;
            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for(int i = 0; i < particles; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float shift = rand.random(-range, range);
                float shiftAng = rand.nextBoolean() ? ang + 90 : ang - 90;
                Tmp.v1.set((endX - startX) * fin, (endY - startY) * fin).add(startX, startY).add(build);
                Tmp.v2.trns(shiftAng, shift).add(Tmp.v1);
                Draw.color(color);
                Draw.alpha(particleAlpha * build.warmup());
                Lines.lineAngle(Tmp.v2.x, Tmp.v2.y, ang, length);
            }
            Draw.reset();
        }
    }
}
