package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.draw.DrawArcSmelt;

public class DrawArcSmeltRotated extends DrawArcSmelt {
    public float flameX = 0, flameY = 0;

    @Override
    public void draw(Building build) {
        if(build.warmup() > 0f && flameColor.a > 0.001f){
            Tmp.v1.set(flameX, flameY).rotate(build.rotdeg()).add(build);

            Lines.stroke(circleStroke * build.warmup());

            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            float a = alpha * build.warmup();
            Draw.blend(blending);

            Draw.color(midColor, a);
            if(drawCenter) Fill.circle(Tmp.v1.x, Tmp.v1.y, flameRad + si);

            Draw.color(flameColor, a);
            if(drawCenter) Lines.circle(Tmp.v1.x, Tmp.v1.y, (flameRad + circleSpace + si) * build.warmup());

            Lines.stroke(particleStroke * build.warmup());

            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for(int i = 0; i < particles; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f);
                float len = particleRad * Interp.pow2Out.apply(fin);
                Lines.lineAngle(Tmp.v1.x + Angles.trnsx(angle, len), Tmp.v1.y + Angles.trnsy(angle, len), angle, particleLen * fout * build.warmup());
            }

            Draw.blend();
            Draw.reset();
        }
    }
}
