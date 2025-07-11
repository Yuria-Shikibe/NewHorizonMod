package newhorizon.expand.block.distribution.item;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.StackConveyor;
import mindustry.world.meta.Stat;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;

public class AdaptStackConveyor extends StackConveyor {
    public boolean onlyCarry = true;
    public TextureRegion edge2Region;
    public TextureRegion[] topRegions;

    public AdaptStackConveyor(String name) {
        super(name);

        canOverdrive = false;
        placeableLiquid = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (onlyCarry) {
            stats.remove(Stat.itemsMoved);
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        int[] bits = getTiling(plan, list);

        if (bits == null) return;

        Draw.rect(onlyCarry ? regions[0] : topRegions[plan.rotation], plan.drawx(), plan.drawy(), onlyCarry ? plan.rotation * 90 : 0);

        if (onlyCarry) {
            for (int i = 0; i < 4; i++) {
                if ((bits[3] & (1 << i)) == 0) {
                    Draw.rect(edgeRegion, plan.drawx(), plan.drawy(), (plan.rotation - i) * 90);
                }
            }
        }
    }

    @Override
    public void load() {
        super.load();
        edge2Region = Core.atlas.find(name + "-edge-d");
        topRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-top"), 32, 32, 1);
    }

    public class AdaptStackConveyorBuild extends StackConveyorBuild {
        @Override
        public void draw() {
            Draw.z(Layer.block - 0.2f);

            if (onlyCarry) {
                Draw.rect(regions[state], x, y, rotdeg());

                for (int i = 0; i < 4; i++) {
                    if ((blendprox & (1 << i)) == 0) {
                        int rot = Mathf.mod(rotation - i, 4);
                        if (rot <= 1) Draw.rect(edgeRegion, x, y, rot * 90);
                        else Draw.rect(edge2Region, x, y, rot * 90);
                    }
                }

                //draw inputs
                if (state == stateLoad) {
                    for (int i = 0; i < 4; i++) {
                        int dir = rotation - i;
                        var near = nearby(dir);
                        if ((blendprox & (1 << i)) != 0 && i != 0 && near != null && !near.block.squareSprite) {
                            Draw.rect(sliced(regions[0], SliceMode.bottom), x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, (float) (dir * 90));
                        }
                    }
                } else if (state == stateUnload) { //front unload
                    //TOOD hacky front check
                    if ((blendprox & (1)) != 0 && !front().block.squareSprite) {
                        Draw.rect(sliced(regions[0], SliceMode.top), x + Geometry.d4x(rotation) * tilesize * 0.75f, y + Geometry.d4y(rotation) * tilesize * 0.75f, rotation * 90f);
                    }
                }
            } else {
                Draw.rect(topRegions[rotation], x, y);
            }
            Draw.z(Layer.block - 0.1f);

            Tile from = world.tile(link);

            if (link == -1 || from == null || lastItem == null) return;

            //offset
            Tmp.v1.set(from.worldx(), from.worldy());
            Tmp.v2.set(x, y);
            Tmp.v1.interpolate(Tmp.v2, 1f - cooldown, Interp.linear);

            //item
            float size = itemSize * Mathf.lerp(Math.min((float) items.total() / itemCapacity, 1), 1f, 0.4f);
            Drawf.shadow(Tmp.v1.x, Tmp.v1.y, size * 1.2f);
            Draw.rect(lastItem.fullIcon, Tmp.v1.x, Tmp.v1.y, size, size, 0);

            //count
            DrawFunc.drawText(items.total() + "", Tmp.v1.x, Tmp.v1.y - 2f);
        }

        @Override
        public void onProximityUpdate() {
            if (onlyCarry) {
                super.onProximityUpdate();
                if (state == stateLoad) state = stateMove;
            } else {
                state = stateLoad;
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
        }
    }
}
