package newhorizon.expand.block.payload;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.PayloadConveyor;
import newhorizon.expand.block.inner.LinkBlock;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class AdaptPayloadConveyor extends PayloadConveyor {
    public AdaptPayloadConveyor(String name) {
        super(name);
        size = 2;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return super.canPlaceOn(tile, team, rotation) && tile.x % 2 == 0 && tile.y % 2 == 0;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        if (!valid) {
            drawPlaceText("Need align to world grid", x, y, false);
        }

        drawWorldGrid();

        for(int i = 0; i < 4; i++){
            Building other = world.build(x + Geometry.d4x[i] * size, y + Geometry.d4y[i] * size);
            if(other != null && other.block.outputsPayload && other.block.size == size){
                Drawf.selected(other.tileX(), other.tileY(), other.block, other.team.color);
            }
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        //addBar("block", (PayloadConveyorBuild e) -> new Bar(() -> e.blocked + "", () -> Pal.accent, () -> Mathf.num(e.blocked)));
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
            Building accept = nearby(Geometry.d4(rotation).x * (size/2+1), Geometry.d4(rotation).y * (size/2+1));
            if (accept instanceof LinkBlock.LinkBuild lb && lb.linkBuild != null && lb.linkBuild.block.acceptsPayload){
                next = accept;
            }

            int ntrns = 1 + size/2;
            Tile next = tile.nearby(Geometry.d4(rotation).x * ntrns, Geometry.d4(rotation).y * ntrns);
            blocked = (next != null && next.solid() && !(next.block().outputsPayload || next.block().acceptsPayload)) || (this.next != null && this.next.payloadCheck(rotation));
        }

        @Override
        protected boolean blends(int direction){
            if(direction == rotation){
                return !blocked || next != null;
            }else {
                int idx = (direction * 2) % 8;
                Building check1 = world.build(tileX() + getEdges()[idx].x, tileY() + getEdges()[idx].y);
                Building check2 = world.build(tileX() + getEdges()[idx + 1].x, tileY() + getEdges()[idx + 1].y);
                if (checkLinkBlock(check1) && checkLinkBlock(check2)) return true;
            }

            return PayloadBlock.blends(this, direction);
        }

        public boolean checkLinkBlock(Building link){
            return link instanceof LinkBlock.LinkBuild lb1 && lb1.linkBuild != null && lb1.linkBuild.block.acceptsPayload;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (next != null) {
                Drawf.selected(next, Pal.regen);
            }
            for (int i = 0; i < 4; i++){
                if (blends(i)) {
                    Drawf.selected(tileX() + Geometry.d4x(i) * 2, tileY() + Geometry.d4y(i) * 2, this.block, Pal.regen);
                };
            }
        }
    }
}
