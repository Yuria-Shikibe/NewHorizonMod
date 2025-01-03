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

    Seq<Point2> getLinkBlockPos();

    IntSeq getLinkBlockSize();

    /**
     * x, y, size
     * x, y are relative dst to tile()
     * */
    default void addLink(int... value){
        for(int i = 0; i < value.length; i += 3){
            getLinkBlockPos().add(new Point2(value[i], value[i + 1]));
            getLinkBlockSize().add(value[i + 2]);
        }
    }

    default boolean checkLink(Tile tile, Team team, int size, int rotation){
        for (int i = 0; i < getLinkBlockPos().size; i++){
            Point2 p = getLinkBlockPos().get(i);
            int s = getLinkBlockSize().get(i);
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
        for (int i = 0; i < getLinkBlockPos().size; i++){
            Point2 p = getLinkBlockPos().get(i);
            int s = getLinkBlockSize().get(i);
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
        for (int i = 0; i < getLinkBlockPos().size; i++){
            Point2 p = getLinkBlockPos().get(i);
            int s = getLinkBlockSize().get(i);
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

    default void removeLink(Tile tile, int size, int rotation){
        for (int i = 0; i < getLinkBlockPos().size; i++){
            Point2 p = getLinkBlockPos().get(i);
            int s = getLinkBlockSize().get(i);
            int shift = (size + 1) % 2;
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            Tile t = world.tile(tile.x + xr, tile.y + yr);
            t.remove();
        }
    }
}
