package newhorizon.func;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Circle;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.graphics.Pal;

public class DrawFuncs {
    public static final Color bottomColor = Pal.gray;

    private static final Vec2
        vec21 = new Vec2(),
        vec22 = new Vec2(),
        vec23 = new Vec2();
    
    private static final Seq<Position> pointPos = new Seq<>(Position.class);
    
    
    /**
     * @draw sin(@scale * x + @offset);
     * */
    public static void drawSine(float x, float y, float x2, float y2, int phase, float mag, float scale, float offset, float distant){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        int dst = (int)(dstTotal / distant);
    
        if(dst < 1)return;
    
        Vec2 vec = new Vec2().trns(Angles.angle(x, y, x2, y2), distant);
    
        //for(int sign = 0; sign < 1; sign++){
        for(int p = 0; p < phase; p++){
            Fill.circle(x, y, Lines.getStroke());
        
            for(int i = 0; i < dst; i++){
                vec21.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                        (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i),
                        1 * scale,
                        mag
                ) - scale / 2);
            
                vec22.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                        (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i + 1),
                        1 * scale,
                        mag
                ) - scale / 2);
            
                Vec2 from = vec.cpy().scl(i).add(vec21).add(x, y), to = vec.cpy().scl(i + 1).add(vec22).add(x, y);
            
                Lines.line(from.x, from.y, to.x, to.y, false);
                Fill.circle(from.x, from.y, Lines.getStroke() / 2f);
                Fill.circle(to.x, to.y, Lines.getStroke() / 2f);
            }
        }
        //}
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
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag),
                            1 * scale,
                            mag
                    ) - scale / 2);
    
                    vec22.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i),
                            1 * scale,
                            mag
                    ) - scale / 2);
                    
                    Vec2 from = vec.cpy().scl(i).add(vec21).add(x, y), to = vec.cpy().scl(i + 1).add(vec22).add(x, y);
                    
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2f);
                    Fill.circle(to.x, to.y, Lines.getStroke() / 2f);
                }
            }
        //}
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
