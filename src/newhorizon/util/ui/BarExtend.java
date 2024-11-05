package newhorizon.util.ui;

import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.util.pooling.Pools;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

public class BarExtend extends Bar {
    public String icon;

    public BarExtend(String name, Color color, Floatp fraction, String icon){
        super(name, color, fraction);
        this.icon = icon;
    }

    public BarExtend(Prov<CharSequence> name, Prov<Color> color, Floatp fraction, String icon){
        super(name, color, fraction);
        this.icon = icon;
    }

    //public BarExtend(Prov<String> name, Color color, Floatp fraction, String icon){
    //    update(() -> set(name, fraction, color));
    //    this.icon = icon;
    //}

    @Override
    public void draw(){
        super.draw();
        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, icon);

        font.setColor(1f, 1f, 1f, 1f);
        font.getCache().clear();
        font.getCache().addText(icon, x + 10f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);
        font.getCache().draw(parentAlpha);

        Pools.free(lay);
    }
}
