package newhorizon.util.graphic;

import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.struct.IntIntMap;

public class SpriteUtil {
    /*
          1
        4   2
          3
    */
    public static final int[] ATLAS_INDEX_4_4 =
        {
            0b00000000, 0b00000010, 0b00001010, 0b00001000,
            0b00000100, 0b00000110, 0b00001110, 0b00001100,
            0b00000101, 0b00000111, 0b00001111, 0b00001101,
            0b00000001, 0b00000011, 0b00001011, 0b00001001,
        };

    /*
        8 1 5
        4   2
        7 3 6
    */
    public static final int[] ATLAS_INDEX_4_12_RAW =
        {
            0b00000000, 0b00000010, 0b00001010, 0b00001000, /**/0b10001111, 0b00101110, 0b01001110, 0b00011111, /**/0b00100110, 0b01101111, 0b01101110, 0b01001100,
            0b00000100, 0b00000110, 0b00001110, 0b00001100, /**/0b00100111, 0b01111111, 0b11101111, 0b01001101, /**/0b00110111, 0b01011111, 0b10101111, 0b11001111,
            0b00000101, 0b00000111, 0b00001111, 0b00001101, /**/0b00010111, 0b10111111, 0b11011111, 0b10001101, /**/0b00111111, 0b11111111, 0b11110000, 0b11001101,
            0b00000001, 0b00000011, 0b00001011, 0b00001001, /**/0b01001111, 0b00011011, 0b10001011, 0b00101111, /**/0b00010011, 0b10011011, 0b10011111, 0b10001001,
        };

    public static final int[] ATLAS_INDEX_4_12 = new int[ATLAS_INDEX_4_12_RAW.length];
    public static final IntIntMap ATLAS_INDEX_4_12_MAP = new IntIntMap();

    public static final Point2[] orthogonalPos = {
        new Point2(0, 1),
        new Point2(1, 0),
        new Point2(0, -1),
        new Point2(-1, 0),
    };

    public static final Point2[][] diagonalPos = {
        new Point2[]{ new Point2(1, 0), new Point2(1, 1), new Point2(0, 1)},
        new Point2[]{ new Point2(1, 0), new Point2(1, -1), new Point2(0, -1)},
        new Point2[]{ new Point2(-1, 0), new Point2(-1, -1), new Point2(0, -1)},
        new Point2[]{ new Point2(-1, 0), new Point2(-1, 1), new Point2(0, 1)},
    };

    public static final Point2[] proximityPos = {
        new Point2(0, 1),
        new Point2(1, 0),
        new Point2(0, -1),
        new Point2(-1, 0),

        new Point2(1, 1),
        new Point2(1, -1),
        new Point2(-1, -1),
        new Point2(-1, 1),
    };

    static {
        Integer[] indices = new Integer[ATLAS_INDEX_4_12_RAW.length];
        for (int i = 0; i < ATLAS_INDEX_4_12_RAW.length; i++) {
            indices[i] = i;
        }

        for (int i = 1; i < indices.length; i++) {
            int key = indices[i];
            int keyValue = ATLAS_INDEX_4_12_RAW[key];
            int j = i - 1;

            while (j >= 0 && ATLAS_INDEX_4_12_RAW[indices[j]] > keyValue) {
                indices[j + 1] = indices[j];
                j = j - 1;
            }
            indices[j + 1] = key;
        }

        for (int i = 0; i < indices.length; i++) {
            ATLAS_INDEX_4_12[indices[i]] = i;
        }

        for (int i = 0; i < ATLAS_INDEX_4_12_RAW.length; i++) {
            ATLAS_INDEX_4_12_MAP.put(ATLAS_INDEX_4_12_RAW[i], ATLAS_INDEX_4_12[i]);
        }
    }



    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight){
        return splitRegionArray(region, tileWidth, tileHeight, 0);
    }

    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight, int pad){
        return splitRegionArray(region, tileWidth, tileHeight, pad, null);
    }

    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight, int pad, int[] indexMap){
        if(region.texture == null) return null;
        int x = region.getX();
        int y = region.getY();
        int width = region.width;
        int height = region.height;

        int pWidth = tileWidth + pad * 2;
        int pHeight = tileHeight + pad * 2;

        int sw = width / pWidth;
        int sh = height / pHeight;

        int startX = x;
        TextureRegion[] tiles = new TextureRegion[sw * sh];
        for(int cy = 0; cy < sh; cy++, y += pHeight){
            x = startX;
            for(int cx = 0; cx < sw; cx++, x += pWidth){
                int index = cx + cy * sw;
                if (indexMap != null){
                     tiles[indexMap[index]] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                }else {
                    tiles[index] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                }
            }
        }

        return tiles;
    }

}
