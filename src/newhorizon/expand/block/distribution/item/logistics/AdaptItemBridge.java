package newhorizon.expand.block.distribution.item.logistics;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.*;

public class AdaptItemBridge extends ItemBridge {
    public TextureRegion topRegion;
    public static final int maxLinks = 3;

    public AdaptItemBridge(String name) {
        super(name);

        range = 6;
        placeableLiquid = true;
        drawTeamOverlay = false;
        allowDiagonal = true;
    }

    @Override
    public void load() {
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    public void drawBridge(BuildPlan req, float ox, float oy, float flip){
        if(Mathf.zero(Renderer.bridgeOpacity)) return;
        Draw.alpha(Renderer.bridgeOpacity);

        Lines.stroke(bridgeWidth);

        Tmp.v1.set(ox, oy).sub(req.drawx(), req.drawy()).setLength(tilesize/4f);

        Lines.line(
                bridgeRegion,
                req.drawx() + Tmp.v1.x,
                req.drawy() + Tmp.v1.y,
                ox - Tmp.v1.x,
                oy - Tmp.v1.y, false
        );

        Draw.rect(arrowRegion, (req.drawx() + ox) / 2f, (req.drawy() + oy) / 2f,
                Angles.angle(req.drawx(), req.drawy(), ox, oy) + flip);

        Draw.reset();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Tile link = findLink(x, y);

        Drawf.dashCircle(x * tilesize, y * tilesize, range * tilesize, Pal.placing);

        Draw.reset();
        Draw.color(Pal.placing);
        Lines.stroke(1f);
        if(link != null && Math.abs(link.x - x) + Math.abs(link.y - y) > 1){
            Lines.line(x * tilesize, y * tilesize, link.x * tilesize, link.y * tilesize);
            Draw.rect("bridge-arrow", (x * tilesize + link.x * tilesize)/2f, (y * tilesize + link.y * tilesize)/2f, Angles.angle(link.x, link.y, x, y));
        }
        Draw.reset();}

    public boolean linkValid(Tile tile, Tile other, boolean checkDouble){
        if(other == null || tile == null || !positionsValid(tile.x, tile.y, other.x, other.y)) return false;

        return ((other.block() == tile.block() && tile.block() == this) || (!(tile.block() instanceof ItemBridge) && other.block() == this))
                && (other.team() == tile.team() || tile.block() != this)
                && (!checkDouble || ((ItemBridgeBuild)other.build).link != tile.pos());
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> MathUtil.dst(point, other) <= range);
    }

    @Override
    public boolean positionsValid(int x1, int y1, int x2, int y2){
        return Mathf.dst(x1, y1, x2, y2) <= range;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range, StatUnit.blocks);
    }

    public class AdaptItemBridgeBuild extends ItemBridgeBuild {
        @Override
        public void drawConfigure(){
            Drawf.dashCircle(x, y, range * tilesize, Pal.placing);
            Drawf.select(x, y, tile.block().size * tilesize / 2f + 2f, Pal.accent);
        }

        @Override
        public void checkIncoming() {
            super.checkIncoming();
            int idx = 0;
            while(idx < incoming.size){
                int i = incoming.items[idx];
                Tile other = world.tile(i);
                if(idx > maxLinks - 1){
                    other.build.configure(-1);
                    incoming.removeIndex(idx);
                    idx --;
                }
                idx ++;
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.z(Layer.power + 0.1f);
            Draw.rect(topRegion, x, y);

            Draw.z(Layer.power);

            Tile other = world.tile(link);
            if(!linkValid(tile, other)) return;
            if(Mathf.zero(Renderer.bridgeOpacity)) return;

            Lines.stroke(bridgeWidth);
            Lines.line(bridgeRegion, x, y, other.worldx(), other.worldy(), false);

            float dst = Mathf.dst(x, y, other.worldx(), other.worldy()) - tilesize/4f;
            float ang = Angles.angle(x, y, other.worldx(), other.worldy());
            int seg = Mathf.round(dst / tilesize);

            if (seg == 0) return;
            for (int i = 0; i < seg; i++) {
                Tmp.v1.trns(ang, (dst/seg) * i + tilesize/8f).add(this);
                Draw.alpha(Mathf.absin(i - time / arrowTimeScl, arrowPeriod, 1f) * warmup * Renderer.bridgeOpacity);
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, ang);
            }
            Draw.color();
            Draw.reset();
        }
    }
}
