package newhorizon.func;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Position;
import mindustry.graphics.Pal;


public class DrawFuncs {
    public static void posSquareLinkBottom(float x, float y, float x2, float y2, Color color, float stroke, float size){
        posSquareLink(x, y, x2, y2, Pal.gray, stroke + 2f, size + 2f);
        posSquareLink(x, y, x2, y2, color, stroke, size);
    }

    public static void posSquareLinkBottom(Position from, Position to, Color color, float stroke, float size){
        posSquareLink(from.getX(), from.getY(), to.getX(), to.getY(), Pal.gray, stroke + 2f, size + 2f);
        posSquareLink(from.getX(), from.getY(), to.getX(), to.getY(), color, stroke, size);
    }

    public static void posSquareLink(float x, float y, float x2, float y2, Color color, float stroke, float size){
        Lines.stroke(stroke, color);
        Lines.line(x, y, x2, y2);
        Fill.square(x, y, size, 45);
        Fill.square(x2, y2, size, 45);
        Draw.reset();
    }

    public static void posSquareLink(Position from, Position to, Color color, float stroke, float size){
        Lines.stroke(stroke, color);
        Lines.line(from.getX(), from.getY(), to.getX(), to.getY());
        Fill.square(from.getX(), from.getY(), size, 45);
        Fill.square(to.getX(), to.getY(), size, 45);
        Draw.reset();
    }
}
