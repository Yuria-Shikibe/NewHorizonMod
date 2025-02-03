package newhorizon.expand.block.distribution.transport.item;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.type.Item;
import mindustry.world.ItemBuffer;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.*;

public class AdaptItemBridge extends ItemBridge {
    public AdaptConveyor cBlock;
    public TextureRegion topRegion;

    public final int timerAccept = timers++;

    public float speed = 40f;
    public int bufferCapacity = 50;

    public AdaptItemBridge(String name, AdaptConveyor cBlock) {
        super(name);

        range = 6;
        placeableLiquid = true;
        drawTeamOverlay = false;
        this.cBlock = cBlock;
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
        Draw.reset();
    }

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
        stats.add(Stat.speed, 15, StatUnit.itemsSecond);
    }

    public class AdaptItemBridgeBuild extends ItemBridgeBuild {

        ItemBuffer buffer = new ItemBuffer(bufferCapacity);

        @Override
        public void updateTransport(Building other){
            if(buffer.accepts() && items.total() > 0){
                buffer.accept(items.take());
            }

            Item item = buffer.poll(speed / timeScale);
            if(timer(timerAccept, 3.6f / timeScale) && item != null && other.acceptItem(this, item)){
                moved = true;
                other.handleItem(this, item);
                buffer.remove();
            }
        }

        @Override
        public void doDump(){
            dump();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            buffer.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buffer.read(read);
        }

        private void drawInput(Tile other){
            if(!linkValid(tile, other, false)) return;
            boolean linked = other.pos() == link;

            Tmp.v2.trns(tile.angleTo(other), 2f);
            float tx = tile.drawx(), ty = tile.drawy();
            float ox = other.drawx(), oy = other.drawy();
            float alpha = Math.abs((linked ? 100 : 0)-(Time.time * 2f) % 100f) / 100f;
            float x = Mathf.lerp(ox, tx, alpha);
            float y = Mathf.lerp(oy, ty, alpha);

            //draw "background"
            Draw.color(Pal.gray);
            Lines.stroke(2.5f);
            Lines.square(ox, oy, 2f, 45f);
            Lines.stroke(2.5f);
            Lines.line(tx + Tmp.v2.x, ty + Tmp.v2.y, ox - Tmp.v2.x, oy - Tmp.v2.y);

            //draw foreground colors
            Draw.color(linked ? Pal.place : Pal.accent);
            Lines.stroke(1f);
            Lines.line(tx + Tmp.v2.x, ty + Tmp.v2.y, ox - Tmp.v2.x, oy - Tmp.v2.y);

            Lines.square(ox, oy, 2f, 45f);
            Draw.mixcol(Draw.getColor(), 1f);
            Draw.color();
            Fill.square(x, y, 2, Angles.angle(tile.x, tile.y, other.x, other.y) + 45);
            Draw.mixcol();
        }

        @Override
        public void drawSelect(){

            if(linkValid(tile, world.tile(link))){
                drawInput(world.tile(link));
            }

            incoming.each(pos -> drawInput(world.tile(pos)));

            Draw.reset();
        }

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
                if(idx > 1){
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

            Lines.stroke(4.5f);
            Draw.blend(Blending.additive);
            Draw.color(team.color, Pal.gray, 0.45f);
            Draw.alpha(Renderer.bridgeOpacity * 0.75f);
            Lines.line(cBlock.pulseRegions[cBlock.pulseFrame() * 5], x, y, other.worldx(), other.worldy(), false);
            Draw.blend(Blending.normal);

            Draw.color(team.color, Color.white, 0.4f);
            Draw.alpha(Renderer.bridgeOpacity * 0.75f);
            Lines.line(cBlock.edgeRegions[0], x, y, other.worldx(), other.worldy(), false);

            float dst = Mathf.dst(x, y, other.worldx(), other.worldy()) - tilesize/4f;
            float ang = Angles.angle(x, y, other.worldx(), other.worldy());
            int seg = Mathf.round(dst / tilesize);

            if (seg == 0) return;
            for (int i = 0; i < seg; i++) {
                Tmp.v1.trns(ang, (dst/seg) * i + tilesize/8f).add(this);
                Tmp.v2.trns(ang, dst/seg).add(Tmp.v1);
                Draw.color(team.color, Color.white, 0.7f);
                Draw.alpha(Renderer.bridgeOpacity * 0.75f);
                Lines.stroke(6f);
                Lines.line(cBlock.arrowRegions[cBlock.conveyorFrame()], Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
                Lines.line(cBlock.arrowRegions[cBlock.conveyorFrame() + 16], Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
            }
            Draw.color();

            Draw.reset();
        }
    }
}
