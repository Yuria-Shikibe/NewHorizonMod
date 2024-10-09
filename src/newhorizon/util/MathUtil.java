package newhorizon.util;

import arc.math.Mathf;
import arc.util.Time;

public class MathUtil {

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
}
