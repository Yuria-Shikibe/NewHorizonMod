package newhorizon.expand.units.parts;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.part.HoverPart;

public class HoverRotationPart extends HoverPart {

    @Override
    public void draw(PartParams params){
        float z = Draw.z();
        if(layer > 0) Draw.z(layer);
        if(under && turretShading) Draw.z(z - 0.0001f);

        Draw.z(Draw.z() + layerOffset);

        int len = mirror && params.sideOverride == -1 ? 2 : 1;

        Draw.color(color);


        for(int c = 0; c < circles; c++){
            float fin = ((Time.time / phase + (float)c / circles) % 1f);
            Lines.stroke((1f-fin) * stroke + minStroke);

            for(int s = 0; s < len; s++){
                //use specific side if necessary
                int i = params.sideOverride == -1 ? s : params.sideOverride;

                float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
                Tmp.v1.set((x) * sign, y).rotate(params.rotation - 90);

                float
                        rx = params.x + Tmp.v1.x,
                        ry = params.y + Tmp.v1.y;

                Lines.poly(rx, ry, sides, radius * fin, params.rotation + rotation);
            }
        }

        Draw.reset();

        Draw.z(z);
    }

}
