package newhorizon.util.feature;

import arc.math.Mathf;
import arc.struct.FloatSeq;

public class ManhattanVoronoi {
    public FloatSeq points = new FloatSeq();
    public FloatSeq distance = new FloatSeq();

    public int width, height;

    public ManhattanVoronoi(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean addPoint(float x, float y, float threshold) {
        float t = threshold / ((width + height) / 2f);
        for (int i = 0; i < points.size; i += 2) {
            if (Mathf.dstm(points.get(i), points.get(i + 1), x, y) <= t) {
                return false;
            }
        }
        points.add(x, y);
        return true;
    }

    public void clear(){
        points.clear();
        distance.clear();
    }

    public float getPoint(int x, int y) {
        float rx = (Mathf.clamp(x, 0, width - 1) + 1) / (float)width;
        float ry = (Mathf.clamp(y, 0, height - 1) + 1) / (float)height;
        return getTopTwoDistanceDiff(rx, ry);
    }

    public boolean getPointInThreshold(int x, int y, float threshold) {
        float t = threshold / ((width + height) / 2f);
        float dst = getPoint(x, y);
        return dst <= t;
    }

    public float getTopTwoDistanceDiff(float x, float y) {
        distance.clear();
        for (int i = 0; i < points.size; i += 2) {
            distance.add(Mathf.dstm(points.get(i), points.get(i + 1), x, y));
        }
        distance.sort();
        if (distance.size < 2) return 0;
        return distance.get(1) - distance.get(0);
    }
}
