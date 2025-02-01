package newhorizon.util.func;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.gen.Posc;

public class MathUtil {
    private static final Vec2 tmp0 = new Vec2(), tmp1 = new Vec2(), tmp2 = new Vec2();

    /**@return sin value based on time.
     * @param min minimum value for sin vale
     * @param max maximum value for max value
     * */
    public static float timeValue(float min, float max){
        return timeValue(min, max, 1, 0);
    }

    /**@return sin value based on time.
     * @param min minimum value for sin vale
     * @param max maximum value for max value
     * @param period time for a full circle. in seconds
     * */
    public static float timeValue(float min, float max, float period){
        return timeValue(min, max, period, 0);
    }

    /**@return sin value based on time.
     * @param min minimum value for sin vale
     * @param max maximum value for max value
     * @param period time for a full circle. in seconds
     * @param angle shift angle for start, 0 - 360.
     * */
    public static float timeValue(float min, float max, float period, float angle){
        float time = (Time.time / (period / 6f)) + angle;
        float sin = Mathf.sinDeg(time);
        float scale = (max - min) / 2;
        float start = (max + min) / 2;

        return sin * scale + start;
    }

    public static float dst(Posc a, Posc b){
        return Mathf.dst(a.x(), a.y(), b.x(), b.y());
    }

    public static float dst(Point2 a, Point2 b){
        return Mathf.dst(a.x, a.y, b.x, b.y);
    }

    public static float dst(Vec2 a, Vec2 b){
        return Mathf.dst(a.x, a.y, b.x, b.y);
    }

    public static float angle(Posc start, Posc end){
        return Angles.angle(start.x(), start.y(), end.x(), end.y());
    }

    public static float angle(Point2 start, Point2 end){
        return Angles.angle(start.x, start.y, end.x, end.y);
    }

    public static float angle(Vec2 start, Vec2 end){
        return Angles.angle(start.x, start.y, end.x, end.y);
    }

    public static float angleBisector(float a, float b) {
        a = Mathf.mod(a, 360f);
        b = Mathf.mod(b, 360f);

        float delta = Math.abs(a - b);

        return (delta > 180? (a + b) / 2f + 180: (a + b) / 2f) % 360;
    }

    public static float angelDistance(float start, float end){
        start = Mathf.mod(start, 360f);
        end = Mathf.mod(end, 360f);

        return (end + 360 - start) % 360;
    }

    /** Angel move from start to end, the distance is in 180 degrees */
    public static float angleRot(float start, float end, float progress){
        tmp0.trns(start, 1);
        tmp1.trns(end, 1);
        return tmp2.set(tmp0).lerp(tmp1, progress).angle();
    }
}
