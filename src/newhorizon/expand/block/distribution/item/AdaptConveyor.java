package newhorizon.expand.block.distribution.item;

import arc.Core;
import arc.Graphics;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Conveyor;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;

public class AdaptConveyor extends Conveyor {
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

    public boolean blends(Building self, Building other) {
        if (other == null) return false;
        return blends(self.tile, self.rotation, other.tileX(), other.tileY(), other.rotation, other.block);
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
            if (check(tile.x, tile.y + 1)) drawIndex += 1;
            if (check(tile.x + 1, tile.y)) drawIndex += 2;
            if (check(tile.x, tile.y - 1)) drawIndex += 4;
            if (check(tile.x - 1, tile.y)) drawIndex += 8;
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
            Draw.alpha(0.75f);
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
    }
}
