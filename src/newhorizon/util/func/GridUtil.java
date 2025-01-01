package newhorizon.util.func;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import mindustry.type.UnitType;
import newhorizon.util.struct.GridData;

import static mindustry.Vars.*;

public class GridUtil {
    public static final int GRID_LEN = 16;
    public static final int PX_LEN = GRID_LEN / tilesize * 2;

    public static final Point2[] samplingPixels = {new Point2(2, 2), new Point2(2, 13), new Point2(13, 13), new Point2(13, 2)};

    //key: unit inner name, value: unit grid
    public static ObjectMap<String, GridData> unitGridsMap;

    public static void init(){

        if (headless) return;
        unitGridsMap = new ObjectMap<>(content.units().size);
        for (UnitType unit : content.units()) {
            unitGridsMap.put(unit.name, GridUtil.getGridData(unit.fullIcon));
        }
    }


    public static GridData getGridData(TextureRegion region){

        int widthStep = Mathf.ceil((float) region.width / GRID_LEN);
        int heightStep = Mathf.ceil((float) region.height / GRID_LEN);

        int width = widthStep * GRID_LEN;
        int height = heightStep * GRID_LEN;

        Pixmap pixmap = Core.atlas.getPixmap(region).crop();

        float padLeft = (width - pixmap.width) / 2f, padBot = (height - pixmap.height) / 2f;
        int startLeft = -(int) padLeft, startBot = -(int) padBot;

        IntSeq tmpPoints = new IntSeq();
        tmpPoints.setSize(width * height);

        for (int x = 0; x < widthStep; x++){
            for (int y = 0; y < heightStep; y++){
                int xCoord = startLeft + x * GRID_LEN;
                int yCoord = startBot + y * GRID_LEN;

                int sampleCount = 0;
                for (Point2 point2: samplingPixels){
                    int pixel = pixmap.get(xCoord + point2.x, yCoord + point2.y);
                    //check for empty pixels, +1 if true
                    if ((pixel & 0x000000ff) == 0) sampleCount++;
                }

                if (sampleCount == 4){tmpPoints.set(y * widthStep + x, 0);}else {tmpPoints.set(y * widthStep + x, 1);}
            }
        }
        pixmap.dispose();

        return new GridData(tmpPoints, widthStep, heightStep, padLeft, padBot);
    }
}
