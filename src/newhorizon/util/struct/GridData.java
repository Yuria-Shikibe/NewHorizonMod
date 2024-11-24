package newhorizon.util.struct;

import arc.struct.IntSeq;

public class GridData {
    //start from left bottom and ends at top right
    public IntSeq grids;
    //draw coord shift for units.
    public float xShift, yShift;
    public int width, height;

    public GridData(IntSeq grids, int width, int height, float xShift, float yShift){
        this.grids = grids;

        this.width = width;
        this.height = height;

        this.xShift = xShift;
        this.yShift = yShift;
    }

    public int getGrid(int x, int y){
        if (x < 0 || y < 0 || x >= width || y >= height) return 0;
        return grids.get(y * width + x);
    }

    public int getGridBottomLeft(int x, int y){
        if (x < 0 || y < 0 || x >= width || y >= height) return 0;
        int ry = height - y - 1;
        return grids.get(ry * width + x);
    }
}