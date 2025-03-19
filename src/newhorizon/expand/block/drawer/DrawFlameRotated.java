package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.draw.DrawFlame;

public class DrawFlameRotated extends DrawFlame {
    public String suffix = "";
    public float x, y;
    public float flameX, flameY;

    public DrawFlameRotated(float x, float y, Color flameColor) {
        super(flameColor);
        this.x = x;
        this.y = y;
    }

    public DrawFlameRotated(Color flameColor) {
        super(flameColor);
    }

    public DrawFlameRotated() {
    }

    @Override
    public void load(Block block) {
        top = Core.atlas.find(block.name + suffix);
        block.clipSize = Math.max(block.clipSize, (lightRadius + lightSinMag) * 2f * block.size);
    }

    @Override
    public void draw(Building build) {
        if (build.warmup() > 0f && flameColor.a > 0.001f) {
            float g = 0.3f;
            float r = 0.06f;
            float cr = Mathf.random(0.1f);

            Tmp.v1.set(x, y).rotate(build.rotdeg());

            Draw.z(Layer.block + 0.01f);

            Draw.alpha(build.warmup());
            Draw.rect(top, build.x + Tmp.v1.x, build.y + Tmp.v1.y, build.rotdeg());

            Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * build.warmup());

            Tmp.v1.set(flameX, flameY).rotate(build.rotdeg());
            Draw.tint(flameColor);
            Fill.circle(build.x + Tmp.v1.x, build.y + Tmp.v1.y, flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr);
            Draw.color(1f, 1f, 1f, build.warmup());
            Fill.circle(build.x + Tmp.v1.x, build.y + Tmp.v1.y, flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr);

            Draw.color();
        }
    }
}
