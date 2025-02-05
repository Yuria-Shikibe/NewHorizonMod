package newhorizon.expand.cutscene.components.ui;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.graphics.Pal;
import newhorizon.content.NHContent;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;

import static newhorizon.NHRenderer.height;
import static newhorizon.NHRenderer.width;

public enum MarkStyle {
    defaultStyle((id, time, radius, pos, color, beyond) -> {
        Tmp.c2.set(Pal.gray).a(color.a);

        float size = radius * Vars.renderer.getDisplayScale();

        float rotationS = 45 + 90 * NHInterp.pow10.apply((time / 120) % 1);
        float angle = beyond ? Angles.angle(width / 2, height / 2, pos.x, pos.y) - 90 : 0;
        Lines.stroke(9f, Tmp.c2);
        Lines.square(pos.x, pos.y, size + 3f, rotationS);
        Lines.stroke(3f, color);
        if (beyond) Draw.rect(NHContent.pointerRegion, pos, size, size, angle);
        Lines.square(pos.x, pos.y, size + 3f, rotationS);

        Lines.stroke(9f, Tmp.c2);
        for (int i : Mathf.signs) {
            Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
            Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
        }

        Lines.stroke(3f, color);
        for (int i : Mathf.signs) {
            Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
            Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
        }
    }),

    defaultNoLines((id, time, radius, pos, color, beyond) -> {
        Tmp.c2.set(Pal.gray).a(color.a);

        float size = radius * Vars.renderer.getDisplayScale();

        float rotationS = 45 + 90 * NHInterp.pow10.apply((time / 120) % 1);
        float angle = beyond ? Angles.angle(width / 2, height / 2, pos.x, pos.y) - 90 : 0;
        Lines.stroke(9f, Tmp.c2);
        Lines.square(pos.x, pos.y, size + 3f, rotationS);
        Lines.stroke(3f, color);
        if (beyond) Draw.rect(NHContent.pointerRegion, pos, size, size, angle);
        Lines.square(pos.x, pos.y, size + 3f, rotationS);
    }),

    fixed((id, time, radius, pos, color, beyond) -> {
        Tmp.c2.set(Pal.gray).a(color.a);

        float size = radius * Vars.renderer.getDisplayScale();

        float rotationS = 45;
        float angle = beyond ? Angles.angle(width / 2, height / 2, pos.x, pos.y) - 90 : 0;
        Lines.stroke(9f, Tmp.c2);
        Lines.square(pos.x, pos.y, size + 3f, rotationS);
        Lines.stroke(3f, color);
        if (beyond) Draw.rect(NHContent.pointerRegion, pos, size, size, angle);
        Lines.square(pos.x, pos.y, size + 3f, rotationS);

        Lines.stroke(9f, Tmp.c2);
        for (int i : Mathf.signs) {
            Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
            Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
        }

        Lines.stroke(3f, color);
        for (int i : Mathf.signs) {
            Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
            Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
        }
    }),

    shake((id, time, radius, pos, color, beyond) -> {
        Tmp.c2.set(Pal.gray).a(color.a);

        Rand rand = NHFunc.rand;
//			long t = (long)time;
//			t = t - (t % 60);
        rand.setSeed((long) Mathf.round(time, 9f) + id);

        Vec2 v = pos.cpy().add(rand.range(12), rand.range(12));
        float size = radius * Vars.renderer.getDisplayScale();

        float rotationS = 45;
        float angle = beyond ? Angles.angle(width / 2, height / 2, v.x, v.y) - 90 : 0;
        Lines.stroke(9f, Tmp.c2);
        Lines.square(v.x, v.y, size + 3f, rotationS);
        Lines.stroke(3f, color);
        Lines.square(v.x, v.y, size + 3f, rotationS);

        Lines.stroke(9f, Tmp.c2);
        Lines.spikes(pos.x, pos.y, size * 1.5f + 6f, size / 2, 4, 45);

        Lines.stroke(3f, color);
        Lines.spikes(pos.x, pos.y, size * 1.5f + 6f, size / 2, 4, 45);
    });

    public final DrawCaution drawer;

    MarkStyle(DrawCaution drawer) {
        this.drawer = drawer;
    }
}
