package newhorizon.expand.units;

import arc.Settings;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.SettingsMenuDialog;

import static arc.Core.settings;
import static arc.graphics.g2d.Lines.circleVertices;

public class TrailEngine extends UnitType.UnitEngine{
    protected Trail trail;
    public float width;
    public TrailEngine(float x, float y, float radius, float rotation, int trailLength, float trailWidth){
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.rotation = rotation;
        trail = new Trail(trailLength);
        width = trailWidth;
    }

    @Override
    public void draw(Unit unit){
        UnitType type = unit.type;
        float rot = unit.rotation - 90;
        Color color = type.engineColor == null ? unit.team.color : type.engineColor;

        Tmp.v1.set(x, y).rotate(rot);
        float ex = Tmp.v1.x, ey = Tmp.v1.y;
        trail.update(unit.x + ex, unit.y + ey);

        Draw.z(Layer.effect);

        if (unit.vel.len2() > 0.001f) {
            //Tmp.c1.set(color).shiftHue(Time.time * 3);
            trail.draw(color, width);
        }
        super.draw(unit);
        if (settings.getBool("bloom")){
            Draw.color(Color.black);
            Draw.rect(type.region, unit.x, unit.y, unit.rotation - 90);
            Draw.color();
        }
    }
}
