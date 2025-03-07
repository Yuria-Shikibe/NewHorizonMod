package newhorizon.expand.block.production.factory;

import arc.func.Boolf2;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Edges;
import mindustry.world.Tile;
import newhorizon.content.blocks.InnerBlock;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.PlaceholderBlock;

import static mindustry.Vars.state;
import static mindustry.Vars.world;

public interface MultiBlock {

    Seq<Point2> linkBlockPos();

    IntSeq linkBlockSize();

    default Point2 calculateRotatedPosition(Point2 pos, int blockSize, int linkSize, int rotation) {
        int shift = (blockSize + 1) % 2;
        int offset = (linkSize + 1) % 2;
        int px = pos.x, py = pos.y;

        return switch (rotation) {
            case 1 -> new Point2(-py + shift - offset, px);
            case 2 -> new Point2(-px + shift - offset, -py + shift - offset);
            case 3 -> new Point2(py, -px + shift - offset);
            default -> new Point2(px, py); // default rotation 0
        };
    }

    default void addLink(int... values) {
        for (int i = 0; i < values.length; i += 3) {
            linkBlockPos().add(new Point2(values[i], values[i + 1]));
            linkBlockSize().add(values[i + 2]);
        }
    }

    default boolean checkLink(Tile tile, Team team, int size, int rotation) {
        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);
            if (!Build.validPlace(InnerBlock.linkEntity[s - 1], team, tile.x + rotated.x, tile.y + rotated.y, 0, false)) {
                return false;
            }
        }
        return true;
    }

    default void createPlaceholder(Tile tile, int size) {
        if (state.rules.infiniteResources || tile == null || tile.build == null) return;

        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, tile.build.rotation);
            Tile t = world.tile(tile.x + rotated.x, tile.y + rotated.y);
            t.setBlock(InnerBlock.placeholderEntity[s - 1], tile.team(), 0);
            PlaceholderBlock.PlaceholderBuild b = (PlaceholderBlock.PlaceholderBuild)t.build;
            b.updateLink(tile);
        }
    }

    default Seq<Building> setLinkBuild(Building building, Block block, Tile tile, Team team, int size, int rotation) {
        Seq<Building> out = new Seq<>();
        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);
            Tile t = world.tile(tile.x + rotated.x, tile.y + rotated.y);

            if (!block.outputsLiquid) {
                t.setBlock(InnerBlock.linkEntity[s - 1], team, 0);
            } else {
                t.setBlock(InnerBlock.linkEntityLiquid[s - 1], team, 0);
            }
            LinkBlock.LinkBuild b = (LinkBlock.LinkBuild)t.build;
            b.updateLink(building);
            out.add(b);
        }
        return out;
    }

    default Point2 teamOverlayPos(int size, int rotation){
        int shift = (size + 1) % 2;
        int value = -size/2 + shift;

        Point2 out = new Point2(value, value);

        for (int i = 0; i < linkBlockPos().size; i++){
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);

            if ((rotated.x + rotated.y) < (value + value)) out.set(rotated.x, rotated.y);
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
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);

            if ((rotated.x + rotated.y) > (value1 - value2)) out.set(rotated.x, rotated.y);
        }
        return out;
    }

    default Point2 getMaxSize(int size, int rotation) {
        int shift = (size + 1) % 2;
        int left = -size / 2 + shift, bot = -size / 2 + shift, right = size / 2, top = size / 2;

        Point2 out = new Point2(size, size);

        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);

            left = Math.min(left, rotated.x);
            right = Math.max(right, rotated.x);
            bot = Math.min(bot, rotated.y);
            top = Math.max(top, rotated.y);
        }

        out.set(right - left + 1, top - bot + 1);
        return out;
    }
}
