package newhorizon.expand.units.unitType;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import newhorizon.content.NHColor;
import newhorizon.expand.units.AncientEngine;
import newhorizon.expand.units.AncientUnit;

import javax.sound.sampled.Line;

import static arc.graphics.g2d.Lines.circleVertices;

public class RadianceUnitType extends AncientUnit {

    public RadianceUnitType(String name) {
        super(name);
        constructor = RadianceEntity::new;

        outlineRadius = 4;
        lowAltitude = flying = true;

        health = 200000;
        armor = 32;
        hitSize = 100f;

        drag = 0.5f;
        rotateSpeed = 1f;
        speed = 2.25f;
        accel = 0.1f;

        engineSize = 0;
    }

    @Override
    public void draw(Unit unit) {
        //super.draw(unit);
        float innerRad = 15f;
        Color color = unit.team.color;

        float colorLerp = 0.3f + 0.1f * Mathf.sinDeg(Time.time * 4f);
        Tmp.c1.set(color).lerp(Color.white, colorLerp);
        inner: {
            Draw.z(Layer.effect);
            Draw.color(Color.black);
            Fill.circle(unit.x, unit.y, innerRad);
            Draw.color(Tmp.c1);
            Lines.stroke(4f + Mathf.sinDeg(Time.time * 4f) * 0.7f);
            Lines.circle(unit.x, unit.y, innerRad);
            Fill.light(unit.x, unit.y, circleVertices(innerRad), innerRad, Color.clear, Tmp.c1);

            Lines.stroke(4f + Mathf.sinDeg(Time.time * 4f) * 0.7f);
            Lines.circle(unit.x, unit.y, innerRad);
        }

        outer: {
            Draw.z(Layer.effect);
            Draw.color(Tmp.c1);
            Lines.stroke(6f + Mathf.sinDeg(Time.time * 4f) * 0.7f);
            for (int i = 0; i < 6; i++) {
                float outerRad = hitSize * 2;
                float spaceLen = outerRad / 4f * (Mathf.sinDeg(Time.time / 2f) * 0.2f + 1f);

                Tmp.v1.set(unit).trns(60 * i - 60, outerRad);
                float x1 = unit.x + Tmp.v1.x, y1 = unit.y + Tmp.v1.y;
                Tmp.v1.set(unit).trns(60 * i, outerRad);
                float x2 = unit.x + Tmp.v1.x, y2 = unit.y + Tmp.v1.y;
                Tmp.v1.set(unit).trns(60 * i + 60, outerRad);
                float x3 = unit.x + Tmp.v1.x, y3 = unit.y + Tmp.v1.y;

                float ang1 = Angles.angle(x2, y2, x1, y1);
                float ang2 = Angles.angle(x2, y2, x3, y3);

                Lines.lineAngle(x2, y2, ang1, spaceLen);
                Lines.lineAngle(x2, y2, ang2, spaceLen);

                Fill.poly(x2, y2, 6, 8 + 1.5f * Mathf.sinDeg(Time.time * 4f) * 0.7f);
            }
        }
        Draw.reset();

        Draw.blend(Blending.additive);
        Draw.z(Layer.effect);
        Tmp.c1.set(color).lerp(Color.white, colorLerp).a(0.75f);

        for (int i = 0; i < 25; i++){
            Mathf.rand.setSeed(id + i);
            float triHeight = Mathf.random(65, 105) * (Mathf.sinDeg(Time.time * 3f + Mathf.random(360f)) * 0.6f + 0.5f);
            float triWidth = Mathf.random(10f, 15f);
            float arcRot = Mathf.random(360f);
            float arcRotSpeed = Time.time * 0.65f * Mathf.random(-1f, 1f);
            float arcAng = arcRot + arcRotSpeed;

            Tmp.v1.trns(arcAng, innerRad);

            if (triHeight > 0){
                Draw.color(Tmp.c1);
                Drawf.tri(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, triWidth, triHeight, arcAng);
            }
        }
        Draw.blend();
        Draw.reset();
    }

    public class RadianceEntity extends UnitEntity {

    }

    public class RadianceEngine extends UnitEngine{

        public RadianceEngine(){
            x = 0;
            y = 42;
            radius = 16;
        }

        public void draw(Unit unit){
            UnitType type = unit.type;
            float rot = unit.rotation - 90;
            Color color = type.engineColor == null ? unit.team.color : type.engineColor;
            Tmp.v1.set(x, y).rotate(rot);
            float ex = unit.x + Tmp.v1.x, ey = unit.y + Tmp.v1.y;

            float radShift = Mathf.absin(Time.time / 4f, 2f, radius / 4f);
            float innerRad = radius + radShift;
            float colorLerp = 0.3f + 0.1f * Mathf.sinDeg(Time.time * 4f);

            float a = 0.88f * (0.88f + Mathf.absin(Time.time * 1.3f - Mathf.random(5), 0.825f, 0.13f));
            Draw.blend(Blending.additive);
            Draw.alpha(a);
            Draw.z(Layer.effect);
            Tmp.c1.set(color).lerp(Color.white, colorLerp).a(a);

            for (int i = 0; i < 45; i++){
                Mathf.rand.setSeed(id + i);
                float triHeight = Mathf.random(25, 105) * (Mathf.sinDeg(Time.time * 3f + Mathf.random(360f)) * 0.6f + 0.5f);
                float triWidth = Mathf.random(10f, 15f);
                float arcRot = Mathf.random(360f);
                float arcRotSpeed = Time.time * 0.65f * Mathf.random(-1f, 1f);
                float arcAng = arcRot + arcRotSpeed;

                Tmp.v1.set(x, y).trns(arcAng, innerRad);

                if (triHeight > 0){
                    Draw.color(Tmp.c1);
                    Drawf.tri(ex + Tmp.v1.x, ey + Tmp.v1.y, triWidth, triHeight, arcAng);
                }
            }
            Draw.blend();
            Draw.reset();

            Tmp.c1.set(color).lerp(Color.white, colorLerp);

            Draw.z(Layer.effect);
            Draw.color(Tmp.c1);
            Lines.stroke(4f + Mathf.sinDeg(Time.time * 4f) * 0.7f);
            Lines.circle(ex, ey, innerRad);
            Fill.light(ex, ey, circleVertices(innerRad), innerRad, Color.clear, Tmp.c1);
            Fill.light(ex, ey, circleVertices(innerRad/3), innerRad/3, Tmp.c1, Color.clear);


            Lines.stroke(6f + Mathf.sinDeg(Time.time * 4f) * 0.7f);
            for (int i = 0; i < 6; i++){
                float outerRad = hitSize * 2;
                Tmp.v1.set(x, y).trns(60 * i - 60, outerRad);
                float x1 = Tmp.v1.x, y1 = Tmp.v1.y;
                Tmp.v1.set(x, y).trns(60 * i, outerRad);
                float x2 = Tmp.v1.x, y2 = Tmp.v1.y;
                Tmp.v1.set(x, y).trns(60 * i + 60, outerRad);
                float x3 = Tmp.v1.x, y3 = Tmp.v1.y;


                float ang1 = Angles.angle(x1, y1, x2, y2);
                float ang2 = Angles.angle(x1, y1, x3, y3);

                Lines.lineAngle(ex + Tmp.v1.x, ey + Tmp.v1.y, ang1, hitSize/2f);
                Lines.lineAngle(ex + Tmp.v2.x, ey + Tmp.v2.y, ang2, hitSize/2f);

                Fill.poly(ex + Tmp.v2.x, ey + Tmp.v2.y, 6, 8 + 1.5f * Mathf.sinDeg(Time.time * 4f) * 0.7f);
            }
        }
    }
}
