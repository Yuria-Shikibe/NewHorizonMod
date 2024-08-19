package newhorizon.util.ui;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.util.pooling.Pools;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

public class NHBar extends Bar {
    //public float[] diving;
    public Floatp NHFraction;
    public Color color;
    public float value;
    public CharSequence text = "";
    public NHBar(Prov<CharSequence> name, Prov<Color> color, Floatp fraction){
        super(name, color, fraction);
        NHFraction = fraction;
        this.color = color.get();
        text = name.get();
    }

    @Override
    public void draw() {
        super.draw();

        Draw.alpha(parentAlpha);
        Draw.color(Pal.darkerGray);
        Fill.rect(x + width/10 - 5, y + height/2, 5, height);
        Fill.rect(x + width/10 * 4 - 5, y + height/2, 5, height);
        Fill.rect(x + width/10 * 7 - 5, y + height/2, 5, height);
        Draw.alpha(1f);
        Draw.alpha(parentAlpha);

        float computed = Mathf.clamp(NHFraction.get());
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable top = Tex.barTop;
        float topWidth = width * value;
        Draw.color(color);
        top.draw(x, y, topWidth, height);

        Draw.color();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, text);

        font.setColor(1f, 1f, 1f, 1f);
        font.getCache().clear();
        font.getCache().addText(text, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);
        font.getCache().draw(parentAlpha);

        Pools.free(lay);
    }
}
