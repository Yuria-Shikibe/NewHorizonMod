package newhorizon.util.graphic;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.scene.ui.layout.Scl;
import arc.util.Align;
import arc.util.pooling.Pools;
import mindustry.ui.Fonts;

public class DrawUtil {
    public static void drawText(String text, float x, float y) {
        drawText(text, x, y, 1f);
    }

    public static void drawText(String text, float x, float y, float size) {
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(size / 6f / Scl.scl(1f));

        layout.setText(font, text);
        font.draw(text, x, y, Align.center);

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1f);
        Draw.reset();
        Pools.free(layout);
    }
}
