package newhorizon.expand.block.drawer;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.draw.DrawCrucibleFlame;

public class DrawCrucibleFlameRotated extends DrawCrucibleFlame {
    public float flameX, flameY;
    @Override
    public void draw(Building build){

        if(build.warmup() > 0f && flameColor.a > 0.001f){
            Tmp.v1.set(flameX, flameY).rotate(build.rotdeg()).add(build.x, build.y);

            Lines.stroke(circleStroke * build.warmup());

            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            float a = alpha * build.warmup();
            Draw.blend(Blending.additive);

            Draw.color(midColor, a);
            Fill.circle(Tmp.v1.x, Tmp.v1.y, flameRad + si);

            Draw.color(flameColor, a);
            Lines.circle(Tmp.v1.x, Tmp.v1.y, (flameRad + circleSpace + si) * build.warmup());

            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for(int i = 0; i < particles; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                float len = particleRad * particleInterp.apply(fout);
                Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                Fill.circle(
                        Tmp.v1.x + Angles.trnsx(angle, len),
                        Tmp.v1.y + Angles.trnsy(angle, len),
                        particleSize * fin * build.warmup()
                );
            }

            Draw.blend();
            Draw.reset();
        }
    }
}
