package newhorizon.expand.block.production.factory;

import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Log;
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
    Point2 tmp1 = new Point2(), tmp2 = new Point2();

    LinkBlock linkBlock();

    IntSeq links();

    default Point2 getRotationPos(Point2 pos, int blockSize, int rotation) {
        int shift = (blockSize + 1) % 2;
        int px = pos.x, py = pos.y;

        return switch (rotation) {
            case 1 -> tmp2.set(-py + shift, px);
            case 2 -> tmp2.set(-px + shift, -py + shift);
            case 3 -> tmp2.set(py, -px + shift);
            default -> tmp2.set(px, py); // default rotation 0
        };
    }

    default void addLink(int[]... values) {
        for (int[] value : values) {
            links().addAll(value);
        }
    }

    default int[] p(int x, int y) {
        return new int[]{x, y};
    }

    default int linkSize() {
        return links().size / 2;
    }

    default Point2 getLink(int index) {
        return tmp1.set(links().get(index * 2), links().get(index * 2 + 1));
    }

    default boolean checkLink(Tile tile, Team team, int size, int rotation) {
        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, rotation);
            if (!Build.validPlace(linkBlock(), team, tile.x + r.x, tile.y + r.y, 0, false)) {
                return false;
            }
        }
        return true;
    }

    default void createPlaceholder(Tile tile, int size) {
        if (state.rules.infiniteResources || tile == null || tile.build == null) return;

        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, tile.build.rotation);
            Tile t = world.tile(tile.x + r.x, tile.y + r.y);
            t.setBlock(InnerBlock.placeholder, tile.team(), 0);
            PlaceholderBlock.PlaceholderBuild b = (PlaceholderBlock.PlaceholderBuild) t.build;
            b.updateLink(tile);
        }
    }

    default Seq<Building> setLinkBuild(Building building, Tile tile, Team team, int size, int rotation) {
        Seq<Building> out = new Seq<>();
        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, rotation);
            Tile t = world.tile(tile.x + r.x, tile.y + r.y);

            t.setBlock(linkBlock(), team, 0);
            LinkBlock.LinkBuild b = (LinkBlock.LinkBuild) t.build;
            b.updateLink(building);
            out.add(b);
        }
        return out;
    }

    default Seq<Tile> getLinkTiles(Tile tile, int size, int rotation) {
        Seq<Tile> out = new Seq<>();
        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, rotation);
            Tile t = world.tile(tile.x + r.x, tile.y + r.y);
            out.add(t);
        }
        return out;
    }

    default Point2 teamOverlayPos(int size, int rotation) {
        Point2 out = leftBottomPos(size);

        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, rotation);
            if ((r.x + r.y) < (out.x + out.y)) out.set(r.x, r.y);
        }
        return out;
    }

    default Point2 statusOverlayPos(int size, int rotation) {
        Point2 out = rightBottomPos(size);

        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, rotation);
            if ((r.x - r.y) > (out.x - out.y)) out.set(r.x, r.y);
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

        for (int i = 0; i < linkSize(); i++) {
            Point2 p = getLink(i);
            Point2 r = getRotationPos(p, size, rotation);

            left = Math.min(left, r.x);
            right = Math.max(right, r.x);
            bot = Math.min(bot, r.y);
            top = Math.max(top, r.y);
        }

        out.set(right - left + 1, top - bot + 1);
        return out;
    }
}
