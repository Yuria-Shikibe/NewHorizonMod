package newhorizon.util.feature;

import arc.func.Intc2;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Point2;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.pooling.Pool;

public class RectSpiller {
    Rand rand = new Rand();
    Pool<QuadRect> rectPool = new Pool<QuadRect>() {@Override protected QuadRect newObject() {return new QuadRect();}};
    Pool<Point2> pointPool = new Pool<Point2>() {@Override protected Point2 newObject() {return new Point2();}};
    Seq<QuadRect> tmpRects = new Seq<>();

    int width, height;
    int[] values;

    public void init(int w, int h){
        width = w;
        height = h;

        values = new int[w * h];

        //leave 2 unit for bound
        QuadRect bound = rect(0, 0, w, h);
        tmpRects.add(bound);
        //quadTree = new QuadTree<>(bound);
        //quadTree.insert(bound);

        rand.setSeed(Point2.pack(w, h));
        while (true){
            //tmpRects = quadTree.objects;
            QuadRect target = tmpRects.find(r -> r.width > 3 || r.height > 3);
            if (target == null) return;
            recursiveSplit(target);
        }
    }

    public void recursiveSplit(QuadRect rect){
        int sizeThreshold = 1;
        float verticalChance = Mathf.maxZero(rect.width - sizeThreshold * 2);
        float horizontalChance = Mathf.maxZero(rect.height - sizeThreshold * 2);

        if (verticalChance == 0 && horizontalChance == 0) return;

        boolean vertical = Mathf.chance(verticalChance / (verticalChance + horizontalChance));

        if (vertical){
            int xSplit = rand.random(sizeThreshold, (int)rect.width - sizeThreshold);

            tmpRects.remove(rect);
            for (int y = 0; y < rect.height + 1; y++){
                setPos((int) (rect.x) + xSplit, (int) (rect.y) + y, 1);
            }

            tmpRects.add(rect(rect.x, rect.y, xSplit - 1, rect.height));
            tmpRects.add(rect(rect.x + xSplit + 1, rect.y, rect.width - xSplit - 1, rect.height));
        }else{
            int ySplit = rand.random(sizeThreshold, (int)rect.height - sizeThreshold);

            tmpRects.remove(rect);
            for (int x = 0; x < rect.width + 1; x++){
                setPos((int) (rect.x) + x, (int) (rect.y) + ySplit, 1);
            }
            tmpRects.add(rect(rect.x, rect.y, rect.width, ySplit - 1));
            tmpRects.add(rect(rect.x, rect.y + ySplit + 1, rect.width, rect.height - ySplit - 1));
        }
    }

    public Point2 point(int x, int y){
        return pointPool.obtain().set(x, y);
    }

    public QuadRect rect(float x, float y, float w, float h){
        return (QuadRect) rectPool.obtain().set(x, y, w, h);
    }

    public void setPos(int x, int y, int value){
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        values[x + y * width] = value;
    }

    public int getPos(int x, int y){
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return values[x + y * width];
    }

    public void each(Intc2 r){
        for (int x = 0; x < width; x++){
            for (int y = 0; y < width; y++){
                r.get(x, y);
            }
        }
    }

    public class QuadRect extends Rect implements QuadTree.QuadTreeObject {
        @Override
        public void hitbox(Rect out) {
            out.set(this);
        }
    }
}
