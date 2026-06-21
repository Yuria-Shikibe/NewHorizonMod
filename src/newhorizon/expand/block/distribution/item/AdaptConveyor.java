package newhorizon.expand.block.distribution.item;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.storage.Unloader;
import newhorizon.util.graphic.SpriteUtil;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;

import java.sql.Blob;

import static mindustry.Vars.*;

public class AdaptConveyor extends ArmoredConveyor {
    public TextureRegion[] edgeRegions, lightRegions, pulseRegions, arrowRegions;
    public float framePeriod = 8f;

    public AdaptConveyor(String name) {
        super(name);

        placeableLiquid = true;
        drawTeamOverlay = false;

        emitLight = true;
        lightRadius = 20f;
    }

    @Override
    public void load() {
        super.load();

        edgeRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-edge"), 32, 32, 0, SpriteUtil.ATLAS_INDEX_4_4);
        lightRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-light"), 32, 32, 0, SpriteUtil.ATLAS_INDEX_4_4);
        pulseRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-pulse"), 32, 32, 0, SpriteUtil.ATLAS_INDEX_4_4);
        arrowRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-arrow"), 32, 32, 1);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        int[] bits = getTiling(plan, list);

        if (bits == null) return;
        Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public boolean isLogisticComponentDirectional(Building source) {
        Block b = source.block;
        return b instanceof DirectionBridge ||
                b instanceof Duct ||
                b instanceof Conveyor ||
                b instanceof DirectionalUnloader;
    }

    /*public boolean isLogisticComponentDirectional(Block block) {
        return block instanceof DirectionBridge ||
                block instanceof Duct;
    }*/

    public boolean isLogisticComponentOmni(Building source) {
        Block b = source.block;
        return b instanceof Junction ||
                b instanceof OverflowGate ||
                b instanceof OverflowDuct ||
                b instanceof Router ||
                b instanceof Sorter ||
                b instanceof DuctRouter||
                b instanceof Unloader ||
                b instanceof ItemBridge;
    }
    /*public boolean isLogisticComponentOmni(Block block) {
        return block instanceof Junction || block instanceof OverflowGate || block instanceof OverflowDuct || block instanceof Router || block instanceof Sorter || block instanceof DuctRouter;
    }*/
    public boolean rotatedOutputFaces(Tile tile, int otherx, int othery, int otherrot, Block otherblock) {
        return otherblock.rotatedOutput(otherx, othery, tile) &&
                Point2.equals(
                        otherx + Geometry.d4x(otherrot),
                        othery + Geometry.d4y(otherrot),
                        tile.x,
                        tile.y
                );
    }

    public boolean rotatedOutputFaces(Building source, Building self) {
        return source.block.rotatedOutput(source.tileX(), source.tileY(), self.tile) &&
                source.front() == self;
    }

    public boolean blends(Building self, Building other) {
        if (other == null) return false;
        return blends(self.tile, self.rotation, other.tileX(), other.tileY(), other.rotation, other.block) || isLogisticComponentOmni(other);
    }


    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return (otherblock.outputsItems() && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock)) ||
                (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock.hasItems) ||
                //isLogisticComponentDirectional(otherblock) ||
                rotatedOutputFaces(tile, otherx, othery, otherrot, otherblock);
    }


    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public int conveyorFrame() {
        return (int) ((((Time.time) % framePeriod) / framePeriod) * 16);
    }

    public class AdaptConveyorBuild extends ConveyorBuild {
        public int drawIndex = 0;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            drawIndex = 0;
            if (check(tile.x, tile.y + 1) || rotation == 1) drawIndex += 1;
            if (check(tile.x + 1, tile.y) || rotation == 0) drawIndex += 2;
            if (check(tile.x, tile.y - 1) || rotation == 3) drawIndex += 4;
            if (check(tile.x - 1, tile.y) || rotation == 2) drawIndex += 8;
        }

        public boolean check(int x, int y) {
            return blends(this, Vars.world.build(x, y));
        }

        @Override
        public void draw() {

            Draw.z(Layer.block - 0.25f);
            Draw.mixcol(team.color, Color.clear, 0.75f);
            Draw.rect(pulseRegions[drawIndex], x, y);

            Draw.z(Layer.block - 0.2f);
            Draw.rect(arrowRegions[conveyorFrame()], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            boolean backDraw = true;
            if (blends(this, right())) {
                Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() + 90);
                backDraw = false;
            }
            if (blends(this, back())) {
                Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());
                backDraw = false;
            }
            if (blends(this, left())) {
                Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg() - 90);
                backDraw = false;
            }
            if (backDraw) {
                Draw.rect(arrowRegions[conveyorFrame() + 16], x, y, rotdeg());
            }

            Draw.color();
            Draw.reset();
            Draw.rect(edgeRegions[drawIndex], x, y);

            Draw.color(team.color, Color.white, 0.25f);
            Draw.alpha(0.4f);
            Draw.rect(lightRegions[drawIndex], x, y);
            Draw.alpha(1f);
            Draw.color();

            Draw.reset();
            Draw.z(Layer.block - 0.05f);
            float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;

            for (int i = 0; i < len; i++) {
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, tilesize, 0);
                Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

                float
                        ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x),
                        iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
            }
        }

        @Override
        public void drawLight() {
            Drawf.light(x, y, lightRadius, Tmp.c1.set(team.color).lerp(Color.white, 0.5f), 0.5f);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(source.block.rotatedOutput(source.tileX(), source.tileY(), tile)){
                return super.acceptItem(source, item) && rotatedOutputFaces(source, this);
            }

            return super.acceptItem(source, item) && (
                    isLogisticComponentOmni(source) ||
                            Edges.getFacingEdge(source.tile, tile).relativeTo(tile) == rotation ||
                            source.front() == this && isLogisticComponentDirectional(source)
            );
        }
    }
}
