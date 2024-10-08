package newhorizon.util.graphic;

import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;

public class SpriteUtil {

    public static final int[] ATLAS_INDEX =
        {
            0, 2, 10, 8,
            4, 6, 14, 12,
            5, 7, 15, 13,
            1, 3, 11, 9
        };

    public static final Point2[] CLOCKWISE_POS = {
        new Point2(0, 1),
        new Point2(1, 0),
        new Point2(0, -1),
        new Point2(-1, 0),
    };

    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight){
        return splitRegionArray(region, tileWidth, tileHeight, 0);
    }

    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight, int pad){
        return splitRegionArray(region, tileWidth, tileHeight, pad, false);
    }

    public static TextureRegion[] split11Region(TextureRegion region, int pad){
        return splitRegionArray(region, 32, 32, pad, true);
    }

    public static TextureRegion[] split11Region(TextureRegion region){
        return splitRegionArray(region, 32, 32, 0, true);
    }

    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight, int pad, boolean useIndex){
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
                if (useIndex){
                    tiles[ATLAS_INDEX[index]] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                }else {
                    tiles[index] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                }
            }
        }

        return tiles;
    }

}
