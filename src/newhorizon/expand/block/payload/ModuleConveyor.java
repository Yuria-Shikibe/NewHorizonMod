package newhorizon.expand.block.payload;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.geom.Geometry;
import arc.math.geom.Intersector;
import arc.math.geom.Rect;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadConveyor;
import newhorizon.NewHorizon;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.expand.block.production.factory.MultiBlockEntity;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class ModuleConveyor extends PayloadConveyor {
    public TextureRegion edgeRegion1, edgeRegion2;
    public static TextureRegion arrowRegion;
    public ModuleConveyor(String name) {
        super(name);
        size = 2;
        moveTime = 30;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {

        return super.canPlaceOn(tile, team, rotation)
                //&& tile.x % 2 == 0 && tile.y % 2 == 0
                ;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        //if (!valid) {
        //    drawPlaceText("Need align to world grid", x, y, false);
        //}

        drawWorldGrid();

        for(int i = 0; i < 4; i++){
            Building other = world.build(x + Geometry.d4x[i] * size, y + Geometry.d4y[i] * size);
            if(other != null && other.block.outputsPayload && other.block.size == size){
                Drawf.selected(other.tileX(), other.tileY(), other.block, other.team.color);
            }
        }
    }

    @Override
    public void load() {
        super.load();
        edgeRegion1 = Core.atlas.find(name + "-edge-1");
        edgeRegion2 = Core.atlas.find(name + "-edge-2");

        arrowRegion = Core.atlas.find(NewHorizon.name("payload-arrow"));
    }

    public static void prepareColor(Team team){
        float dst = 0.8f;
        float fract = Interp.pow5.apply((Time.time % 30) / 30);
        float glow = Math.max((dst - (Math.abs(fract - 0.5f) * 2)) / dst, 0);
        Draw.mixcol(team.color, glow);
    }

    public static void prepareAlpha(){
        float fract = Interp.pow5.apply((Time.time % 30) / 30);
        Draw.alpha(1f - Interp.pow5In.apply(fract));
    }

    public static void drawArrowOut(float x, float y, float rotation){
        float fract = Interp.pow5.apply((Time.time % 30) / 30);
        float s = tilesize * 2;
        float trnext = s * fract;

        //next
        TextureRegion clipped = clipRegion(Tmp.r1.set(x, y, 16, 16), Tmp.r2.set(x, y, 16, 16).move(trnext, 0), arrowRegion);
        float widthNext = (s - clipped.width * clipped.scl()) * 0.5f;
        float heightNext = (s - clipped.height * clipped.scl()) * 0.5f;
        Tmp.v1.set(widthNext, heightNext).rotate(rotation);
        Draw.rect(clipped, x + Tmp.v1.x, y + Tmp.v1.y, rotation);
    }

    public static void drawArrowIn(float x, float y, float rotation){
        float fract = Interp.pow5.apply((Time.time % 30) / 30);
        float s = tilesize * 2;
        float trprev = s * (fract - 1);

        //next
        TextureRegion clipped = clipRegion(Tmp.r1.set(x, y, 16, 16), Tmp.r2.set(x, y, 16, 16).move(trprev, 0), arrowRegion);
        float widthPrev = (clipped.width * clipped.scl() - s) * 0.5f;
        float heightPrev = (clipped.height * clipped.scl() - s) * 0.5f;
        Tmp.v1.set(widthPrev, heightPrev).rotate(rotation);
        Draw.rect(clipped, x + Tmp.v1.x, y + Tmp.v1.y, rotation);
    }

    protected static TextureRegion clipRegion(Rect bounds, Rect sprite, TextureRegion region){
        Rect over = Tmp.r3;

        boolean overlaps = Intersector.intersectRectangles(bounds, sprite, over);

        TextureRegion out = Tmp.tr1;
        out.set(region.texture);
        out.scale = region.scale;

        if(overlaps){
            float w = region.u2 - region.u;
            float h = region.v2 - region.v;
            float x = region.u, y = region.v;
            float newX = (over.x - sprite.x) / sprite.width * w + x;
            float newY = (over.y - sprite.y) / sprite.height * h + y;
            float newW = (over.width / sprite.width) * w, newH = (over.height / sprite.height) * h;

            out.set(newX, newY, newX + newW, newY + newH);
        }else{
            out.set(0f, 0f, 0f, 0f);
        }

        return out;
    }

    @Override
    public void setBars() {
        super.setBars();
    }

    public void drawWorldGrid(){
        float leftBound = Core.camera.position.x - Core.camera.width / 2;
        float rightBound = Core.camera.position.x + Core.camera.width / 2;
        float topBound = Core.camera.position.y + Core.camera.height / 2;
        float bottomBound = Core.camera.position.y - Core.camera.height / 2;

        int leftTile = (int) (leftBound / tilesize), rightTile = (int) (rightBound / tilesize);
        int bottomTile = (int) (bottomBound / tilesize) + 1, topTile = (int) (topBound / tilesize);

        int leftStart = (leftTile / 2) * 2, bottomStart = (bottomTile / 2) * 2;

        Lines.stroke(0.5f);
        Draw.alpha(0.2f);
        for (int x = leftStart; x <= rightTile; x += 2){
            int xPos = x * tilesize - 4;
            Draw.z(Layer.blockOver);
            Draw.color(Pal.accent);
            Draw.alpha(0.2f);
            Lines.line(xPos, topBound, xPos, bottomBound);
        }
        for (int y = bottomStart; y <= topTile; y += 2){
            int yPos = y * tilesize - 4;
            Draw.z(Layer.blockOver);
            Draw.color(Pal.accent);
            Draw.alpha(0.2f);
            Lines.line(leftBound, yPos, rightBound, yPos);
        }
    }

    public class AdaptPayloadConveyorBuild extends PayloadConveyorBuild{
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();

            if (next != null) return;
            checkLinkTile();
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            prepareColor(team);
            drawArrowOut(x, y, rotdeg());
            drawArrowIn(x, y, rotdeg());

            for(int i = 0; i < 4; i++){
                if(blends(i) && i != rotation){
                    prepareAlpha();
                    drawArrowIn(x, y, i * 90 + 180);
                }
            }

            Draw.reset();

            if(!blends(0)) Draw.rect(edgeRegion1, x, y, 0);
            if(!blends(1)) Draw.rect(edgeRegion1, x, y, 90);
            if(!blends(2)) Draw.rect(edgeRegion2, x, y, 180);
            if(!blends(3)) Draw.rect(edgeRegion2, x, y, 270);

            Draw.z(Layer.blockOver);

            if(item != null){
                item.draw();
            }
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            int idx = (rotation + 2) % 4 * 2;
            Building prev1 = nearby(getEdges()[idx].x, getEdges()[idx].y);
            Building prev2 = nearby(getEdges()[idx + 1].x, getEdges()[idx + 1].y);

            Building link1 = prev1 instanceof MultiBlockEntity? prev1: prev1 instanceof LinkBlock.LinkBuild lb && lb.linkBuild != null? lb.linkBuild: null;
            Building link2 = prev2 instanceof MultiBlockEntity? prev2: prev2 instanceof LinkBlock.LinkBuild lb && lb.linkBuild != null? lb.linkBuild: null;

            return super.acceptPayload(source, payload) && (prev1 == prev2 || link1 == link2) && payload.content() instanceof ModulePayload;
        }

        public void checkLinkTile(){
            Building next1 = nearby(getEdges()[rotation * 2].x, getEdges()[rotation * 2].y);
            Building next2 = nearby(getEdges()[rotation * 2 + 1].x, getEdges()[rotation * 2 + 1].y);

            if (next1 instanceof LinkBlock.LinkBuild lb1 && lb1.linkBuild != null && lb1.linkBuild.block.acceptsPayload){
                if (next2 instanceof LinkBlock.LinkBuild lb2 && lb2.linkBuild != null && lb2.linkBuild.block.acceptsPayload){
                    if (lb1.linkBuild == lb2.linkBuild) next = lb1.linkBuild;
                }
            }

            int ntrns = 1 + size/2;
            Tile next = tile.nearby(Geometry.d4(rotation).x * ntrns, Geometry.d4(rotation).y * ntrns);
            blocked = (next != null && next.solid() && !(next.block().outputsPayload || next.block().acceptsPayload)) || (this.next != null && this.next.payloadCheck(rotation));
        }

        //sometimes sandbox cause proximity issues and i dont know why
        @Override
        public void moveFailed() {
            checkLinkTile();
        }
    }
}
