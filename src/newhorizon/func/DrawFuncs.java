package newhorizon.func;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.gen.Buildingc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;
import mindustry.ui.Fonts;
import newhorizon.NewHorizon;

import static mindustry.Vars.tilesize;

public class DrawFuncs {
    public static final Color bottomColor = Pal.gray;
    public static final Color outlineColor = Color.valueOf("565666");
    public static final float sinScl = 1f;
    private static final Vec2
        vec21 = new Vec2(),
        vec22 = new Vec2(),
        vec23 = new Vec2();
    
    private static final Seq<Position> pointPos = new Seq<>(Position.class);
    
    public static void drawConnected(float x, float y, float size, Color color){
        Draw.reset();
        float sin = Mathf.absin(Time.time * sinScl, 8f, 1.25f);
        
        for(int i = 0; i < 4; i++){
            float length = size / 2f + 3 + sin;
            Tmp.v1.trns(i * 90, -length);
            Draw.color(Pal.gray);
            Draw.rect(NewHorizon.configName("linked-arrow-back"), x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
            Draw.color(color);
            Draw.rect(NewHorizon.configName("linked-arrow"), 	 x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
        }
        Draw.reset();
    }
    
    
    public static void overlayText(Font font, String text, float x, float y, float offset, float offsetScl, float size, Color color, boolean underline, boolean align){
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(size / Scl.scl(1.0f));
        layout.setText(font, text);
        font.setColor(color);
        
        float dy = offset + 3.0F;
        font.draw(text, x, y + layout.height / (align ? 2 : 1) + (dy + 1.0F) * offsetScl, 1);
        --dy;
    
        if(underline){
            Lines.stroke(2.0F, Color.darkGray);
            Lines.line(x - layout.width / 2.0F - 2.0F, dy + y, x + layout.width / 2.0F + 1.5F, dy + y);
            Lines.stroke(1.0F, color);
            Lines.line(x - layout.width / 2.0F - 2.0F, dy + y, x + layout.width / 2.0F + 1.5F, dy + y);
        }
    
        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1.0F);
        Draw.reset();
        Pools.free(layout);
    }
    
    public static void overlayText(String text, float x, float y, float offset, Color color, boolean underline){
        overlayText(Fonts.outline, text, x, y, offset, 1, 0.25f, color, underline, false);
    }
    
    public static Pixmap getOutline(Pixmap base, Color outlineColor){
        PixmapRegion region = new PixmapRegion(base);
        Pixmap out = new Pixmap(region.width, region.height);
        Color color = new Color();
        
        for(int x = 0; x < region.width; ++x){
            for(int y = 0; y < region.height; ++y){
                region.getPixel(x, y, color);
                out.draw(x, y, color);
                if(color.a < 1.0F){
                    boolean found = false;
                    
                    label72:
                    for(int rx = -4; rx <= 4; ++rx){
                        for(int ry = -4; ry <= 4; ++ry){
                            if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && Mathf.within((float)rx, (float)ry, 4.0F) && color.set(region.getPixel(rx + x, ry + y)).a > 0.01F){
                                found = true;
                                break label72;
                            }
                        }
                    }
                    
                    if(found){
                        out.draw(x, y, outlineColor);
                    }
                }
            }
        }
        return out;
    }
    
    public static Pixmap getOutline(TextureAtlas.AtlasRegion t, Color outlineColor){
        if(t.found()){
            return getOutline(Core.atlas.getPixmap(t).crop(), outlineColor);
        }else return new Pixmap(255, 255);
    }
    
    public static void drawWeaponPixmap(Pixmap base, Weapon w, boolean outline){
        TextureAtlas.AtlasRegion t = Core.atlas.find(w.name);
        if(!t.found())return;
        Pixmap wRegion = outline ? getOutline(t, outlineColor) : Core.atlas.getPixmap(t).crop();
        
        int startX = getCenter(base, wRegion, true, outline), startY = getCenter(base, wRegion, false, outline);
    
        if(w.mirror){
            PixmapRegion t2 = Core.atlas.getPixmap(t);
            Pixmap wRegion2 = outline ? getOutline(flipX(t2), outlineColor) : flipX(t2);
            base.drawPixmap(wRegion, startX + (int)w.x * 4, startY - (int)w.y * 4, 0, 0, wRegion.getWidth(), wRegion.getHeight());
            base.drawPixmap(wRegion2, getCenter(base, wRegion2, true, outline) - (int)w.x * 4, getCenter(base, wRegion2, false, outline) - (int)w.y * 4, 0, 0, -wRegion2.getWidth(), wRegion2.getHeight());
        }else{
            base.drawPixmap(wRegion, startX + (int)(w.x) * 4, startY - (int)(w.y) * 4);
        }
    }
    
    public static int getCenter(Pixmap base, Pixmap above, boolean WorH, boolean outline){
        return (WorH ? (base.getWidth() - above.getWidth()) / 2 : (base.getHeight() - above.getHeight()) / 2);
    }
    
    public static Pixmap flipX(PixmapRegion pixmap){
        Pixmap base = new Pixmap(pixmap.width, pixmap.height);
        Color color = new Color();
        
        if(color.a < 1.0F){
            for(int y = 0; y < pixmap.height; ++y){
                for(int x = 0; x < pixmap.width; ++x){
                    pixmap.getPixel(x, y, color);
                    base.draw(pixmap.width - x, y, color);
                }
            }
        }
        return base;
    }
    
    public static Pixmap fillColor(PixmapRegion pixmap, Color replaceColor){
        Pixmap base = new Pixmap(pixmap.width, pixmap.height);
        Color color = new Color();
        if(color.a < 1.0F){
            for(int y = 0; y < pixmap.height; ++y){
                for(int x = 0; x < pixmap.width; ++x){
                    pixmap.getPixel(x, y, color);
                    base.draw(pixmap.width - x, y, color.mul(replaceColor));
                }
            }
        }
        return base;
    }
    
    public static void drawSine(float x, float y, float x2, float y2, int phase, float mag, float scale, float offset, float distant, boolean flip){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        int dst = (int)(dstTotal / distant);
    
        if(dst < 1)return;
    
        Vec2 vec = new Vec2().trns(Angles.angle(x, y, x2, y2), distant);
    
        for(int sign : flip ? Mathf.signs : Mathf.one){
            for(int p = 0; p < phase; p++){
                Fill.circle(x, y, Lines.getStroke());
            
                for(int i = 0; i < dst; i++){
                    vec21.trns(Angles.angle(x, y, x2, y2) + 90, (Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i) * sinScl,
                            scale,
                            mag
                    ) - mag / 2) * i);
                
                    vec22.trns(Angles.angle(x, y, x2, y2) + 90, (Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i + 1) * sinScl,
                            1 * scale,
                            mag
                    ) - mag / 2) * i);
                
                    Vec2 from = vec.cpy().scl(i).add(vec21).add(x, y), to = vec.cpy().scl(i + 1).add(vec22).add(x, y);
                
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2f);
                    Fill.circle(to.x, to.y, Lines.getStroke() / 2f);
                }
            }
        }
    }
    
    public static void drawSineLerp(float x, float y, float x2, float y2, int phase, float mag, float scale, float offset, float distant){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        int dst = (int)(dstTotal / distant);
        
        if(dst < 1)return;
        
        Vec2 vec = new Vec2().trns(Angles.angle(x, y, x2, y2), distant);
        
        //for(int sign = 0; sign < 1; sign++){
            for(int p = 0; p < phase; p++){
                Fill.circle(x, y, Lines.getStroke());
                
                for(int i = 0; i < dst; i++){
                    vec21.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag) * sinScl,
                            scale,
                            mag
                    ) - mag / 2);
    
                    vec22.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i) * sinScl,
                            scale,
                            mag
                    ) - mag / 2);
                    
                    Vec2 from = vec.cpy().scl(i).add(vec21).add(x, y), to = vec.cpy().scl(i + 1).add(vec22).add(x, y);
                    
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2f);
                    Fill.circle(to.x, to.y, Lines.getStroke() / 2f);
                }
            }
        //}
    }
    
    public static void link(Buildingc from, Buildingc to, Color color){
        float
                sin = Mathf.absin(Time.time * sinScl, 6f, 1f),
                r1 = from.block().size / 2f * tilesize + sin,
                x1 = from.getX(), x2 = to.getX(), y1 = from.getY(), y2 = to.getY(),
                r2 = to.block().size / 2f * tilesize + sin;
        
        Draw.color(color);
    
        Lines.square(x2, y2, to.block().size * tilesize / 2f + 1.0f);
    
        Tmp.v1.trns(from.angleTo(to), r1);
        Tmp.v2.trns(to.angleTo(from), r2);
        int signs = (int)(from.dst(to) / tilesize);
    
        Lines.stroke(4, Pal.gray);
        Lines.dashLine(x1 + Tmp.v1.x, y1 + Tmp.v1.y, x2 + Tmp.v2.x, y2 + Tmp.v2.y, signs);
        Lines.stroke(2, color);
        Lines.dashLine(x1 + Tmp.v1.x, y1 + Tmp.v1.y, x2 + Tmp.v2.x, y2 + Tmp.v2.y, signs);
    
        Drawf.arrow(x1, y1, x2, y2, from.block().size * tilesize / 2f + sin, 4 + sin, color);
    
        Drawf.circles(x2, y2, r2, color);
    }
    
    public static void arrow(TextureRegion arrow, float x, float y, float sizeScl, float angle, Color color){
        Draw.color(color);
        Draw.rect(arrow, x, y, arrow.width * sizeScl * Draw.scl, arrow.height * sizeScl * Draw.scl, angle);
    }

    public static void arrow(String arrow, float x, float y, float sizeScl, float angle, Color color){
        arrow(Core.atlas.find(arrow, "bridge-arrow"), x, y, sizeScl, angle, color);
    }

    public static void arrowLine(TextureRegion arrow, float sizeScl, Color color, float dst, float maxDst, int offset, Position from, Position to){
        float dstF = dst <= 0 ? Vars.tilesize * 2f : dst;
        int num = (int)(from.dst(to) / dstF);
        if(num - 2 * offset < 0)return;
        float maxDstF = maxDst <= 0 ? Float.MAX_VALUE : maxDst;
        float angle = from.angleTo(to);
        Vec2 vec = new Vec2().trns(angle, dstF);
        for (int i = offset; i <= num - offset; i++) {
            if(vec.len() > maxDstF)break;
            arrow(arrow, from.getX() + vec.cpy().scl(i).x, from.getY() + vec.cpy().scl(i).y, sizeScl, angle, color);
        }
    }

    public static void arrowLine(String arrow, float sizeScl, Color color, float dst, float maxDst, int offset, Position from, Position to){
        arrowLine(Core.atlas.find(arrow, "bridge-arrow"), sizeScl, color, dst, maxDst, offset, from, to);
    }

    public static void posSquare(Color color, float stroke, float size, float x1, float y1, float x2, float y2){

    }

    public static void posSquareLinkArr(Color color, float stroke, float size, boolean drawBottom, boolean linkLine, Position... pos){
        if(pos.length < 2 || (!linkLine && pos[0] == null))return;

        for (int c : drawBottom ? Mathf.signs : Mathf.one) {
            for (int i = 1; i < pos.length; i++) {
                if (pos[i] == null)continue;
                Position p1 = pos[i - 1], p2 = pos[i];
                Lines.stroke(stroke + 1 - c, c == 1 ? color : bottomColor);
                if(linkLine) {
                    if(p1 == null)continue;
                    Lines.line(p2.getX(), p2.getY(), p1.getX(), p1.getY());
                }else{
                    Lines.line(p2.getX(), p2.getY(), pos[0].getX(), pos[0].getY());
                }
                Draw.reset();
            }

            for (Position p : pos) {
                if (p == null)continue;
                Draw.color(c == 1 ? color : bottomColor);
                Fill.square(p.getX(), p.getY(), size + 1 -c / 1.5f, 45);
                Draw.reset();
            }
        }
    }

    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, float x, float y, float x2, float y2){
        posSquareLink(color, stroke, size, drawBottom, vec21.set(x, y), vec22.set(x2, y2));
    }

    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, Position from, Position to){
        posSquareLinkArr(color, stroke, size, drawBottom, false, from, to);
    }
}
