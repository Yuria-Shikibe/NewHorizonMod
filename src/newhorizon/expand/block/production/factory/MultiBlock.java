package newhorizon.expand.block.production.factory;

import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
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

    Block mirrorBlock();

    boolean isMirror();

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
            PlaceholderBlock.PlaceholderBuild b = (PlaceholderBlock.PlaceholderBuild) t.build;
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
            LinkBlock.LinkBuild b = (LinkBlock.LinkBuild) t.build;
            b.updateLink(building);
            out.add(b);
        }
        return out;
    }

    default Seq<Tile> getLinkTiles(Tile tile, int size, int rotation) {
        Seq<Tile> out = new Seq<>();
        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);
            Tile t = world.tile(tile.x + rotated.x, tile.y + rotated.y);
            out.add(t);
        }
        return out;
    }

    default Seq<Tile> linkTiles(int x, int y, int size, int rotation) {
        Seq<Tile> tiles = new Seq<>();
        Point2 lb = leftBottomPos(size);
        for (int tx = 0; tx < size; tx++) {
            for (int ty = 0; ty < size; ty++) {
                Tile other = world.tile(x + tx + lb.x, y + ty + lb.y);
                if (other != null) tiles.add(other);
            }
        }

        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);
            Point2 lb2 = leftBottomPos(s).add(rotated);

            for (int tx = 0; tx < s; tx++) {
                for (int ty = 0; ty < s; ty++) {
                    Tile other = world.tile(x + tx + lb2.x, y + ty + lb2.y);
                    if (other != null) tiles.add(other);
                }
            }
        }

        return tiles;
    }

    default Point2 teamOverlayPos(int size, int rotation) {
        Point2 out = leftBottomPos(size);

        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);
            Point2 lb = leftBottomPos(s).add(rotated);

            if ((lb.x + lb.y) < (out.x + out.y)) out.set(lb.x, lb.y);
        }
        return out;
    }

    default Point2 statusOverlayPos(int size, int rotation) {
        Point2 out = rightBottomPos(size);

        for (int i = 0; i < linkBlockPos().size; i++) {
            Point2 p = linkBlockPos().get(i);
            int s = linkBlockSize().get(i);
            Point2 rotated = calculateRotatedPosition(p, size, s, rotation);
            Point2 rb = rightBottomPos(s).add(rotated);

            if ((rb.x - rb.y) > (out.x - out.y)) out.set(rb.x, rb.y);
        }
        return out;
    }

    default Point2 leftBottomPos(int size) {
        int shift = (size + 1) % 2;
        return new Point2(-size / 2 + shift, -size / 2 + shift);
    }

    default Point2 rightBottomPos(int size) {
        int shift = (size + 1) % 2;
        return new Point2(size / 2, -size / 2 + shift);
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
