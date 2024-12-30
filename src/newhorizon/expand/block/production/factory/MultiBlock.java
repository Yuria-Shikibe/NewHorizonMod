package newhorizon.expand.block.production.factory;

import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.world.Build;
import mindustry.world.Tile;
import newhorizon.content.blocks.InnerBlock;
import newhorizon.expand.block.inner.LinkBlock;

import static mindustry.Vars.world;

public interface MultiBlock {
    Seq<Point2> linkPos = new Seq<>();
    IntSeq linkSize = new IntSeq();

    default Seq<Point2> getLinkBlockPos(){
        return linkPos;
    }

    default IntSeq getLinkBlockSize(){
        return linkSize;
    }

    /**
     * x, y, size
     * x, y are relative dst to tile()
     * */
    default void addLink(int... value){
        for(int i = 0; i < value.length; i += 3){
            linkPos.add(new Point2(value[i], value[i + 1]));
            linkSize.add(value[i + 2]);
        }
    }

    default boolean checkLink(Tile tile, Team team, int size, int rotation){
        for (int i = 0; i < linkPos.size; i++){
            Point2 p = linkPos.get(i);
            int s = linkSize.get(i);
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

    default void setLinkBuild(Building building, Tile tile, Team team, int size, int rotation){
        for (int i = 0; i < linkPos.size; i++){
            Point2 p = linkPos.get(i);
            int s = linkSize.get(i);
            int shift = (size + 1) % 2;
            int offset = (s + 1) % 2;
            int xr = p.x, yr = p.y;

            switch(rotation){
                case 1: xr = -p.y + shift - offset; yr = p.x; break;
                case 2: xr = -p.x + shift - offset; yr = -p.y + shift - offset; break;
                case 3: xr = p.y; yr = -p.x + shift - offset; break;
            }

            Tile t = world.tile(tile.x + xr, tile.y + yr);
            Call.setTile(t, InnerBlock.linkEntity[s - 1], team, 0);
            ((LinkBlock.LinkBuild)t.build).linkBuild = building;
        }
    }
}
