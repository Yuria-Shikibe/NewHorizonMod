package newhorizon.util.graphic;

import arc.Core;
import arc.func.Cons;
import arc.func.Floatc2;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;

import static mindustry.Vars.tilesize;

public class DrawFunc{
    private static float[] vertices = new float[24];
    
    public static final Color bottomColor = Pal.gray;
    public static final float sinScl = 1f;
    public static float NOR_DISTANCE = 600f;
    
    private static final Vec2
        vec1 = new Vec2(),
        vec2 = new Vec2(),
        vec3 = new Vec2(),
        vec4 = new Vec2();

    public static final int[] oneArr = {1};
    
    private static final Seq<Position> pointPos = new Seq<>(Position.class);
    private static final Rand rand = new Rand();


    public static void laser(TextureRegion line, TextureRegion start, TextureRegion end, float x, float y, float x2, float y2, float scale){
        float scl = 8f * scale * Draw.scl, rot = Mathf.angle(x2 - x, y2 - y);
        float vx = Mathf.cosDeg(rot) * scl, vy = Mathf.sinDeg(rot) * scl;
        
        Draw.rect(start, x, y, start.width * scale * start.scl(), start.height * scale * start.scl(), rot + 180);
        Draw.rect(end, x2, y2, end.width * scale * end.scl(), end.height * scale * end.scl(), rot);
        
        Lines.stroke(12f * scale);
        Lines.line(line, x + vx, y + vy, x2 - vx, y2 - vy, false);
        Lines.stroke(1f);
    }
    
    public static void laser(TextureRegion line, TextureRegion edge, float x, float y, float x2, float y2){
        laser(line, edge, edge, x, y, x2, y2, 1f);
    }
    
    public static void laser(TextureRegion line, TextureRegion start, TextureRegion end, float x, float y, float x2, float y2){
        laser(line, start, end, x, y, x2, y2, 1f);
    }
    
    public static void laser(TextureRegion line, TextureRegion edge, float x, float y, float x2, float y2, float scale){
        laser(line, edge, edge, x, y, x2, y2, scale);
    }
    
    public static void basicLaser(float x, float y, float x2, float y2, float stroke, float circleScl){
        Lines.stroke(stroke);
        Lines.line(x, y, x2, y2, false);
        Fill.circle(x, y, stroke * circleScl);
        Fill.circle(x2, y2, stroke * circleScl);
        Lines.stroke(1f);
    }
    
    public static void basicLaser(float x, float y, float x2, float y2, float stroke){
        basicLaser(x, y, x2, y2, stroke, 0.95f);
    }
    
    public static void lineAngleLerp(float x, float y, float angle, float length, boolean cap, Color begin, Color end){
        float stroke = Lines.getStroke();
    
        vec2.trns(angle, length);
        
        float x2 = x + vec2.x;
        float y2 = y + vec2.y;
        
        float hstroke = stroke/2f;
        float len = Mathf.len(x2 - x, y2 - y);
        float diffx = (x2 - x) / len * hstroke, diffy = (y2 - y) / len * hstroke;
    
        float colorB = begin.toFloatBits();
        float colorE = end.toFloatBits();
        
        if(cap){
            Fill.quad(
                x - diffx - diffy,
                y - diffy + diffx,
                colorB,
            
                x - diffx + diffy,
                y - diffy - diffx,
                colorB,
            
                x2 + diffx + diffy,
                y2 + diffy - diffx,
                colorE,
            
                x2 + diffx - diffy,
                y2 + diffy + diffx,
                colorE
            );
        }else{
            Fill.quad(
                x - diffy,
                y + diffx,
                colorB,
            
                x + diffy,
                y - diffx,
                colorB,
            
                x2 + diffy,
                y2 - diffx,
                colorE,
            
                x2 - diffy,
                y2 + diffx,
                colorE
            );
        }
    }
    
    public static void randLenVectors(long seed, int amount, float length, float minLength, float angle, float range, Floatc2 cons){
        rand.setSeed(seed);
        for(int i = 0; i < amount; i++){
            vec1.trns(angle + rand.range(range), minLength  + rand.random(length));
            cons.get(vec1.x, vec1.y);
        }
    }

    public static void randLenVectors(long seed, float fin, int amount, float minLength, float length, Angles.ParticleConsumer cons){
        rand.setSeed(seed);
        for(int i = 0; i < amount; i++){
            float l = rand.nextFloat();
            vec1.trns(rand.random(360f), length * l * fin + minLength);
            cons.accept(vec1.x, vec1.y, fin * l, (1f - fin) * l);
        }
    }
    
    public static float cameraDstScl(float x, float y, float norDst){
        vec1.set(Core.camera.position);
        float dst = Mathf.dst(x, y, vec1.x, vec1.y);
        return 1 - Mathf.clamp(dst / norDst);
    }
    
    public static <T extends Building> void drawWhileHold(Class<T> type, Cons<T> drawer){
        Draw.z(Layer.overlayUI + 1);
        Seq<T> builds = Groups.build.copy(new Seq<>()).retainAll(b -> b.team == Vars.player.team() && type.isAssignableFrom(b.getClass())).as();
        for(T build : builds)drawer.get(build);
    }
    
    public static float cameraDstScl(float x, float y){
        return cameraDstScl(x, y, NOR_DISTANCE);
    }
    
    public static void tri(float x, float y, float width, float length, float angle){
        float wx = Angles.trnsx(angle + 90, width), wy = Angles.trnsy(angle + 90, width);
        Fill.tri(x + wx, y + wy, x - wx, y - wy, Angles.trnsx(angle, length) + x, Angles.trnsy(angle, length) + y);
    }
    
    public static void arrow(float x, float y, float width, float length, float backLength, float angle){
        float wx = Angles.trnsx(angle + 90, width), wy = Angles.trnsy(angle + 90, width);
        float ox = Angles.trnsx(angle, backLength), oy = Angles.trnsy(angle, backLength);
        float cx = Angles.trnsx(angle, length) + x, cy = Angles.trnsy(angle, length) + y;
        Fill.tri(x + ox, y + oy, x - wx, y - wy, cx, cy);
        Fill.tri(x + wx, y + wy, x + ox, y + oy, cx, cy);
    }
    
    public static void surround(long id, float x, float y, float rad, int num, float innerSize, float outerSize, float interp){
        Rand rand = NHFunc.rand;
        
        rand.setSeed(id);
        for(int i = 0; i < num; i++){
            float len = rad * rand.random(0.75f, 1.5f);
            vec1.trns(rand.random(360f) + rand.range(2f) * (1.5f - Mathf.curve(len, rad * 0.75f, rad * 1.5f)) * Time.time, len);
            float angle = vec1.angle();
            vec1.add(x, y);
            DrawFunc.tri(vec1.x, vec1.y, (interp + 1) * outerSize + rand.random(0, outerSize / 8), outerSize * (Interp.exp5In.apply(interp) + 0.25f) / 2f, angle);
            DrawFunc.tri(vec1.x, vec1.y, (interp + 1) / 2 * innerSize + rand.random(0, innerSize / 8), innerSize * (Interp.exp5In.apply(interp) + 0.5f), angle - 180);
        }
    }
    
    public static void drawConnected(float x, float y, float size, Color color){
        Draw.reset();
        float sin = Mathf.absin(Time.time * sinScl, 8f, 1.25f);
        
        for(int i = 0; i < 4; i++){
            float length = size / 2f + 3 + sin;
            Tmp.v1.trns(i * 90, -length);
            Draw.color(Pal.gray);
            Draw.rect(NewHorizon.name("linked-arrow-back"), x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
            Draw.color(color);
            Draw.rect(NewHorizon.name("linked-arrow"), 	 x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
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
            Draw.color();
        }
    
        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1.0F);
        Draw.reset();
        Pools.free(layout);
    }
    
    public static void drawRail(float x, float y, float rotation, float shootY, float fin, float length, float width, float spacing, float scl, TextureRegion arrowRegion){
        float railF = Mathf.curve(Interp.pow2Out.apply(fin), 0f, 0.25f) * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f) * fin;
    
        for(int i = 0; i <= length / spacing; i++){
            Tmp.v1.trns(rotation, i * spacing * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f) + shootY);
            float f = Interp.pow3Out.apply(Mathf.clamp((fin * length - i * spacing) / spacing)) * (0.6f + railF * 0.4f) * 0.8f * scl;
            Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * Draw.scl * f, arrowRegion.height * Draw.scl * f, rotation - 90f);
        }
    
        Tmp.v1.trns(rotation, 0f, (2 - railF) * width);
        Tmp.v2.trns(rotation, shootY);
        Lines.stroke(railF * 2f * scl);
        for(int i : Mathf.signs){
            Lines.lineAngle(x + Tmp.v1.x * i + Tmp.v2.x, y + Tmp.v1.y * i + Tmp.v2.y, rotation, length * (0.75f + railF / 4f) * Mathf.curve(Interp.pow5Out.apply(1 - fin) * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f), 0f, 0.1f));
        }
    }
    
    public static void circlePercentFlip(float x, float y, float rad, float in, float scl){
        boolean monoIncr = in % (scl * 4) < scl * 2;
        float f = Mathf.cos(in % (scl * 3f), scl, 1.1f);
        circlePercent(x, y, rad, f > 0 ? f : -f, in + -90 * Mathf.sign(f));
    }
    
    public static void fillCirclePercent(float centerX, float centerY, float x, float y, float rad, float percent, float angle){
        float p = Mathf.clamp(percent);
    
        int sides = Lines.circleVertices(rad);
    
        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
    
        int i;
    
        vec1.trns(angle, rad);
    
        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2, y + rad * sin2, centerX, centerY);
        }
    
        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        vec1.trns(a, 0, len * (f - 1));
        Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2 + vec1.x, y + rad * sin2 + vec1.y, centerX, centerY);
    }
    
    public static void fillCirclePercentFade(float centerX, float centerY, float x, float y, float rad, float percent, float angle, float aScl, float start , float end){
        float p = Mathf.clamp(percent);
        
        int sides = Lines.circleVertices(rad);
        
        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
        
        int i;
        
        vec1.trns(angle, rad);
        
        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Draw.alpha(Mathf.curve(i / (sides * p), start, end) * aScl);
            Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2, y + rad * sin2, centerX, centerY);
        }
    
        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        vec1.trns(a, 0, len * (f - 1));
        Draw.alpha(aScl);
        Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2 + vec1.x, y + rad * sin2 + vec1.y, centerX, centerY);
    }
    
    public static void circlePercent(float x, float y, float rad, float percent, float angle) {
//        Lines.swirl(x, y, rad, 360 * percent, angle);
        float p = Mathf.clamp(percent);
        
        int sides = Lines.circleVertices(rad);
        
        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
        float hstep = Lines.getStroke() / 2.0F / Mathf.cosDeg(space / 2.0F);
        float r1 = rad - hstep;
        float r2 = rad + hstep;
        
        int i;
        
        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2, y + r1 * sin2, x + r2 * cos2, y + r2 * sin2, x + r2 * cos, y + r2 * sin);
        }
    
        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        vec1.trns(a, 0, len * (f - 1));
        Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2 + vec1.x, y + r1 * sin2 + vec1.y, x + r2 * cos2 + vec1.x, y + r2 * sin2 + vec1.y, x + r2 * cos, y + r2 * sin);
    }
    
    public static void overlayText(String text, float x, float y, float offset, Color color, boolean underline){
        overlayText(Fonts.outline, text, x, y, offset, 1, 0.25f, color, underline, false);
    }
    
    public static void drawSine2(float x, float y, float x2, float y2, float in, float scale, float scaleSpeed, float scaleOffset, float mag, float wavelength){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        float ang = Angles.angle(x, y, x2, y2);
        int dst = (int)(dstTotal / wavelength);
    
//        Log.info(dst);
        
        Lines.beginLine();
//        Lines.linePoint(x, y);
        Lines.linePoint(x, y);
        
        for(int i = 0; i < dst; i++){
            vec1.trns(ang, i * wavelength, Mathf.sin(in + scale * (scaleSpeed * i + scaleOffset), scale, mag)).add(x, y);
            Lines.linePoint(vec1);
        }
    
        Lines.linePoint(x2, y2);
//        Lines.linePoint(x2, y2);
        Lines.endLine(false);
        
    }
    
    public static void drawSine2Modifier(float x, float y, float x2, float y2, float in, float scale, float scaleSpeed, float scaleOffset, float mag, float wavelength, Floatc2 f){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        float ang = Angles.angle(x, y, x2, y2);
        int dst = (int)(dstTotal / wavelength);
        
        //        Log.info(dst);
        
        Lines.beginLine();
        //        Lines.linePoint(x, y);
    
        vec1.trns(ang, 0, Mathf.sin(in + scale * scaleOffset, scale, mag)).add(x, y);
        Lines.linePoint(vec1);
        f.get(vec1.x, vec1.y);
        
        for(int i = 1; i < dst; i++){
            vec1.trns(ang, i * wavelength, Mathf.sin(in + scale * (scaleSpeed * i + scaleOffset), scale, mag)).add(x, y);
            Lines.linePoint(vec1);
        }
        
        Lines.linePoint(x2, y2);
        //        Lines.linePoint(x2, y2);
        Lines.endLine(false);
        
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
                    vec1.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag) * sinScl,
                            scale,
                            mag
                    ) - mag / 2);
    
                    vec2.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i) * sinScl,
                            scale,
                            mag
                    ) - mag / 2);
                    
                    Vec2 from = vec.cpy().scl(i).add(vec1).add(x, y), to = vec.cpy().scl(i + 1).add(vec2).add(x, y);
                    
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
        
        for (int c : drawBottom ? Mathf.signs : oneArr) {
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

    public static Color markColor(Team target){
        return target == Vars.player.team() ? NHColor.ally : NHColor.hostile;
    }
    
    public static Color markColor(Teamc target){
        return target.team() == Vars.player.team() ? NHColor.ally : NHColor.hostile;
    }
    
    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, float x, float y, float x2, float y2){
        posSquareLink(color, stroke, size, drawBottom, vec1.set(x, y), vec2.set(x2, y2));
    }

    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, Position from, Position to){
        posSquareLinkArr(color, stroke, size, drawBottom, false, from, to);
    }
    
    public static void quad(TextureRegion region, float x1, float y1, float c1, float x2, float y2, float c2, float x3, float y3, float c3, float x4, float y4, float c4){
        float mcolor = Draw.getMixColor().toFloatBits();
        float u = region.u;
        float v = region.v;
        
        vertices[0] = x1;
        vertices[1] = y1;
        vertices[2] = c1;
        vertices[3] = u;
        vertices[4] = v;
        vertices[5] = mcolor;
        
        vertices[6] = x2;
        vertices[7] = y2;
        vertices[8] = c2;
        vertices[9] = u;
        vertices[10] = v;
        vertices[11] = mcolor;
        
        vertices[12] = x3;
        vertices[13] = y3;
        vertices[14] = c3;
        vertices[15] = u;
        vertices[16] = v;
        vertices[17] = mcolor;
        
        vertices[18] = x4;
        vertices[19] = y4;
        vertices[20] = c4;
        vertices[21] = u;
        vertices[22] = v;
        vertices[23] = mcolor;
        
        Draw.vert(region.texture, vertices, 0, vertices.length);
    }
    
    public static float rotator_90(float in /*(0, 1)*/, float margin){
        return 90 * NHInterp.pow10.apply(Mathf.curve(in, margin, 1 - margin));
    }
    
    public static float rotator_120(float in /*(0, 1)*/, float margin){
        return 120 * NHInterp.pow10.apply(Mathf.curve(in, margin, 1 - margin));
    }
    
    public static float rotator_90(){return 90 * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15f, 0.85f));}
    
    public static float rotator_180(){return 180 * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15f, 0.85f));}
    
    public static float rotator_360(){return 360 * Interp.pow5.apply(Mathf.curve(cycle(0, 270), 0.15f, 0.85f));}
    
    public static float cycle_100(){return Time.time % 100 / 100;}
    /** @return an interpolation in [0, 1)*/
    public static float cycle(float phaseOffset, float T){
        return (Time.time + phaseOffset) % T / T;
    }
    
    public static float cycle(float in, float phaseOffset, float T){
        return (in + phaseOffset) % T / T;
    }

    public static void gradient(float startX, float startY, float endX, float endY, float width, Color colorFrom, Color colorEnd){
        float angle = Angles.angle(startX, startY, endX, endY);
        float c1 = colorFrom.toFloatBits(), c2 = colorEnd.toFloatBits();

        vec1.trns(angle - 90, width).add(startX, startY);
        vec2.trns(angle + 90, width).add(startX, startY);
        vec3.trns(angle + 90, width).add(endX, endY);
        vec4.trns(angle - 90, width).add(endX, endY);

        Fill.quad(
            vec1.x, vec1.y, c1,
            vec2.x, vec2.y, c1,
            vec3.x, vec3.y, c2,
            vec4.x, vec4.y, c2
        );
    }

    public static void gradient(Vec2 start, Vec2 end, float width, Color colorFrom, Color colorEnd){
        gradient(start.x, start.y, end.x, end.y, width, colorFrom, colorEnd);
    }

    public static void gradient(Vec2 start, Vec2 end, float width, Color colorFrom){
        gradient(start.x, start.y, end.x, end.y, width, colorFrom, Color.clear);
    }

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
