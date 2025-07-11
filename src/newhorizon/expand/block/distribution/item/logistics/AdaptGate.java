package newhorizon.expand.block.distribution.item.logistics;

import arc.Core;
import arc.Graphics;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.Item;
import mindustry.world.blocks.distribution.OverflowGate;

import static mindustry.Vars.player;

public class AdaptGate extends OverflowGate {
    public TextureRegion invertRegion;

    public AdaptGate(String name) {
        super(name);

        saveConfig = true;
        placeableLiquid = true;
        drawTeamOverlay = false;

        config(Boolean.class, (AdaptGateBuild build, Boolean invert) -> build.invert = invert);
    }

    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    @Override
    public void load() {
        super.load();
        invertRegion = Core.atlas.find(name + "-invert");
    }

    public class AdaptGateBuild extends OverflowGateBuild {
        public boolean invert;

        @Override
        public void draw() {
            Draw.rect(invert ? invertRegion : region, x, y);
        }

        @Override
        public void tapped() {
            super.tapped();
            Fx.placeBlock.at(this, size);
            Sounds.click.at(this);
            configure(!invert);
        }

        @Override
        public Graphics.Cursor getCursor() {
            return interactable(player.team()) ? Graphics.Cursor.SystemCursor.hand : Graphics.Cursor.SystemCursor.arrow;
        }

        @Override
        public Object config() {
            return invert;
        }

        public @Nullable Building getTileTarget(Item item, Building src, boolean flip) {
            int from = relativeToEdge(src.tile);
            if (from == -1) return null;
            Building to = nearby((from + 2) % 4);
            boolean
                    fromInst = src.block.instantTransfer,
                    canForward = to != null && to.team == team && !(fromInst && to.block.instantTransfer) && to.acceptItem(this, item),
                    inv = invert == enabled;

            if (!canForward || inv) {
                Building a = nearby(Mathf.mod(from - 1, 4));
                Building b = nearby(Mathf.mod(from + 1, 4));
                boolean ac = a != null && !(fromInst && a.block.instantTransfer) && a.team == team && a.acceptItem(this, item);
                boolean bc = b != null && !(fromInst && b.block.instantTransfer) && b.team == team && b.acceptItem(this, item);

                if (!ac && !bc) {
                    return inv && canForward ? to : null;
                }

                if (ac && !bc) {
                    to = a;
                } else if (bc && !ac) {
                    to = b;
                } else {
                    to = (rotation & (1 << from)) == 0 ? a : b;
                    if (flip) rotation ^= (1 << from);
                }
            }

            return to;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(invert);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            invert = read.bool();
        }
    }
}
