package newhorizon.util.struct;

import arc.math.geom.Position;

/**
 * MetaCircle, 2D version of <a href="https://en.wikipedia.org/wiki/Metaballs">MetaBall</a>.<br>
 */
public class MetaCircle implements Position{
    public int x, y;
    public float range;
    public float maxsize;

    

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }
}
