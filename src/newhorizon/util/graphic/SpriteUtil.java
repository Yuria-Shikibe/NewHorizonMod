package newhorizon.util.graphic;

import arc.graphics.g2d.TextureRegion;

public class SpriteUtil {
    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight){
        if(region.texture == null) return null;
        int x = region.getX();
        int y = region.getY();
        int width = region.width;
        int height = region.height;

        int sw = width / tileWidth;
        int sh = height / tileHeight;

        int startX = x;
        TextureRegion[] tiles = new TextureRegion[sw * sh];
        for(int cy = 0; cy < sh; cy++, y += tileHeight){
            x = startX;
            for(int cx = 0; cx < sw; cx++, x += tileWidth){
                tiles[cx + cy * sw] = new TextureRegion(region.texture, x, y, tileWidth, tileHeight);
            }
        }

        return tiles;
    }

    public static TextureRegion[] splitRegionArray(TextureRegion region, int tileWidth, int tileHeight, int pad){
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
                tiles[cx + cy * sw] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
            }
        }

        return tiles;
    }
}
