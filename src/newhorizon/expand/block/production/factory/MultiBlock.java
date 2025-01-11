package newhorizon.expand.block.production.factory;

import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Build;
import mindustry.world.Tile;
import newhorizon.content.blocks.InnerBlock;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.PlaceholderBlock;

import static mindustry.Vars.state;
import static mindustry.Vars.world;

public interface MultiBlock {

    Seq<Point2> linkBlockPos();

    IntSeq linkBlockSize();

    /**
     * x, y, size
     * x, y are relative dst to tile()
     * */
    default void addLink(int... value){
        for(int i = 0; i < value.length; i += 3){
            linkBlockPos().add(new Point2(value[i], value[i + 1]));
            linkBlockSize().add(value[i + 2]);
        }
    }

    default int linkRotX(Point2 pos, int blockSize, int linkSize, int rotation){
        int shift = (blockSize + 1) % 2;
        int offset = (linkSize + 1) % 2;
        int px = pos.x, py = pos.y;
        switch(rotation){
            case 1: return -py + shift - offset;
            case 2: return -px + shift - offset;
            case 3: return py;
            default: return 0;
        }
    }

    default int linkRotY(Point2 pos, int blockSize, int linkSize, int rotation){
        int shift = (blockSize + 1) % 2;
        int offset = (linkSize + 1) % 2;
        int px = pos.x, py = pos.y;
        switch(rotation){
            case 1: return px;
            case 2: return -py + shift - offset;
            case 3: return -px + shift - offset;
            default: return 0;
        }
    }

    default Tile linkTile(int idx, Building building){
        return world.tile(
                building.tileX() + linkRotX(linkBlockPos().get(idx), building.block().size, linkBlockSize().get(idx), building.rotation()),
                building.tileY() + linkRotY(linkBlockPos().get(idx), building.block().size, linkBlockSize().get(idx), building.rotation()));
    }

    default Tile linkTile(int idx, int size, int rotation, Tile tile){
        return world.tile(
                tile.x + linkRotX(linkBlockPos().get(idx), size, linkBlockSize().get(idx), rotation),
                tile.y + linkRotY(linkBlockPos().get(idx), size, linkBlockSize().get(idx), rotation));
    }

    default boolean checkLink(Tile tile, Team team, int size, int rotation){
        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            int shift = (size + 1) % 2;
            //rotated link size offset
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            if (!Build.validPlace(InnerBlock.linkEntity[s - 1], team, tile.x + xr, tile.y + yr, 0, false)) return false;
        }
        return true;
    }

    default void createPlaceholder(Tile tile, int size){
        if (state.rules.infiniteResources) return;
        if (tile == null || tile.build == null) return;
        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            int shift = (size + 1) % 2;
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(tile.build.rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            Tile t = world.tile(tile.x + xr, tile.y + yr);
            t.setBlock(InnerBlock.placeholderEntity[s - 1], tile.team(), 0);
            PlaceholderBlock.PlaceholderBuild b = (PlaceholderBlock.PlaceholderBuild)t.build;
            b.updateLink(tile);
        }
    }

    default Seq<Building> setLinkBuild(Building building, Tile tile, Team team, int size, int rotation){
        Seq<Building> out = new Seq<>();
        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            int shift = (size + 1) % 2;
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            Tile t = world.tile(tile.x + xr, tile.y + yr);
            t.setBlock(InnerBlock.linkEntity[s - 1], team, 0);
            LinkBlock.LinkBuild b = (LinkBlock.LinkBuild)t.build;
            b.updateLink(building);
            out.add(b);
        }

        return out;
    }

    default void removeLink(Seq<Building> links){
        links.each(Building::kill);
    }

    default Point2 teamOverlayPos(int size, int rotation){
        int shift = (size + 1) % 2;
        int value = -size/2 + shift;

        Point2 out = new Point2(value, value);

        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            if ((xr + yr) < (value + value)) out.set(xr, yr);
        }
        return out;
    }

    default Point2 statusOverlayPos(int size, int rotation){
        int shift = (size + 1) % 2;
        int value1 = size/2, value2 = -size/2 + shift;

        Point2 out = new Point2(value1, value2);

        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            if ((xr - yr) > (value1 - value2)) out.set(xr, yr);
        }
        return out;
    }

    default Point2 getMaxSize(int size, int rotation){
        int shift = (size + 1) % 2;
        int left = -size/2 + shift, bot = -size/2 + shift, right = size/2, top = size/2;

        Point2 out = new Point2(size, size);

        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            if (xr < left) left = xr;
            if (xr > right) right = xr;
            if (yr < bot) bot = yr;
            if (yr > top) top = yr;
        }

        out.set(right - left + 1, top - bot + 1);

        return out;
    }
}
