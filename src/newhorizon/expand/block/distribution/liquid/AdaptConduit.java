package newhorizon.expand.block.distribution.liquid;

import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Eachable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.DirectionLiquidBridge;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidJunction;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.player;
import static mindustry.Vars.renderer;
import static mindustry.Vars.tilesize;

public class AdaptConduit extends Conduit {
    private static final float rotatePad = 6, hpad = rotatePad / 2f / 4f;
    private static final float[][] rotateOffsets = {{hpad, hpad}, {-hpad, hpad}, {-hpad, -hpad}, {hpad, -hpad}};

    public TextureRegion[] topMaskRegions;

    public AdaptConduit(String name) {
        super(name);
        canOverdrive = false;
        placeableLiquid = true;
        config(Boolean.class, (AdaptConduitBuild build, Boolean armored) -> build.armored = armored);
    }

    @Override
    public void load() {
        super.load();
        topMaskRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-top"), 32, 32, 1);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
        if (tile.build instanceof AdaptConduitBuild && ((AdaptConduitBuild) tile.build).armored)
            return (otherblock.outputsLiquid && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock)) ||
                    (lookingAt(tile, rotation, otherx, othery, otherblock) && otherblock.hasLiquids) || otherblock instanceof LiquidJunction;
        return super.blends(tile, rotation, otherx, othery, otherrot, otherblock);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        int[] bits = getTiling(plan, list);

        if (bits == null) return;

        Draw.scl(bits[1], bits[2]);
        Draw.color(botColor);
        Draw.alpha(0.5f);
        Draw.rect(botRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.color();
        Draw.rect(topMaskRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.scl();
    }

    public class AdaptConduitBuild extends ConduitBuild {
        public boolean armored = false;

        @Override
        public void draw() {
            draw(false);
        }

        public void draw(boolean under) {
            int r = this.rotation;

            if (under) Draw.color(botColor);

            Draw.z(Layer.blockUnder);
            for (int i = 0; i < 4; i++) {
                if ((blending & (1 << i)) != 0) {
                    int dir = r - i;
                    drawAt(x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, 0, i == 0 ? r : dir, i != 0 ? SliceMode.bottom : SliceMode.top, under);
                }
            }

            Draw.z(Layer.block);

            Draw.scl(xscl, yscl);
            drawAt(x, y, blendbits, r, SliceMode.none, under);
            Draw.reset();

            if (!under) return;

            if (capped && capRegion.found()) Draw.rect(capRegion, x, y, rotdeg());
            if (backCapped && capRegion.found()) Draw.rect(capRegion, x, y, rotdeg() + 180);
        }

        @Override
        protected void drawAt(float x, float y, int bits, int rotation, SliceMode slice, boolean under) {
            float angle = rotation * 90f;
            if (under) {
                Draw.rect(sliced(botRegions[bits], slice), x, y, angle);
            } else {
                int offset = yscl == -1 ? 3 : 0;

                int frame = liquids.current().getAnimationFrame();
                int gas = liquids.current().gas ? 1 : 0;
                float ox = 0f, oy = 0f;
                int wrapRot = (rotation + offset) % 4;
                TextureRegion liquidr = bits == 1 && padCorners ? rotateRegions[wrapRot][gas][frame] : renderer.fluidFrames[gas][frame];

                if (bits == 1 && padCorners) {
                    ox = rotateOffsets[wrapRot][0];
                    oy = rotateOffsets[wrapRot][1];
                }

                float xscl = Draw.xscl, yscl = Draw.yscl;
                Draw.scl(1f, 1f);
                Drawf.liquid(sliced(liquidr, slice), x + ox, y + oy, smoothLiquid, liquids.current().color.write(Tmp.c1).a(1f));
                Draw.scl(xscl, yscl);

                Draw.rect(sliced(topMaskRegions[bits + (armored ? 5 : 0)], slice), x, y, angle);
            }
        }

        @Override
        public void tapped() {
            super.tapped();
            Fx.placeBlock.at(this, size);
            Sounds.click.at(this);
            configure(!armored);

            onProximityUpdate();
        }

        @Override
        public Graphics.Cursor getCursor() {
            return interactable(player.team()) ? Graphics.Cursor.SystemCursor.hand : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public Object config() {
            return armored;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (armored)
                return super.acceptLiquid(source, liquid) && (tile == null || source.block instanceof Conduit || source.block instanceof DirectionLiquidBridge || source.block instanceof LiquidJunction ||
                        source.tile.absoluteRelativeTo(tile.x, tile.y) == rotation || !source.proximity.contains(this));
            return super.acceptLiquid(source, liquid);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(armored);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            armored = read.bool();
        }
    }
}
