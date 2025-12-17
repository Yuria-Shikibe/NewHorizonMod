package newhorizon.content.blocks;

import arc.math.geom.Geometry;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadConveyor;
import mindustry.world.blocks.payloads.PayloadRouter;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import newhorizon.content.NHItems;
import newhorizon.expand.block.inner.LinkBlock;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.expand.block.production.factory.AdaptCrafter;

public class PayloadBlock {
    public static Block payloadRail, payloadRouter, payloadTeleport;

    public static void load() {
        payloadRail = new PayloadConveyor("module-rail") {{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 10));
            size = 1;
            moveTime = 30;
            buildType = () -> new PayloadConveyorBuild(){
                @Override
                public boolean acceptPayload(Building source, Payload payload) {
                    if (front() == source) return false;
                    return super.acceptPayload(source, payload) && payload.content() instanceof ModulePayload;
                }

                @Override
                protected boolean blends(int direction){
                    if(direction == rotation){
                        return !blocked || next != null;
                    }

                    Building accept = nearby(Geometry.d4(direction).x, Geometry.d4(direction).y);
                    if (accept instanceof AdaptCrafter.AdaptCrafterBuild) return true;
                    if (accept instanceof LinkBlock.LinkBuild) return true;
                    return mindustry.world.blocks.payloads.PayloadBlock.blends(this, direction);
                }
            };
        }
            @Override
            public void setStats() {
                super.setStats();
                stats.remove(Stat.payloadCapacity);
            }
        };

        payloadRouter = new PayloadRouter("module-router") {{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 10));
            size = 1;
            moveTime = 30;
            buildType = () -> new PayloadRouterBuild(){
                @Override
                public boolean acceptPayload(Building source, Payload payload) {
                    if (front() == source) return false;
                    return super.acceptPayload(source, payload) && payload.content() instanceof ModulePayload;
                }
            };
        }
            @Override
            public boolean canSort(Block b){
                return ModuleBlock.modules.contains(modulePayload -> modulePayload == b);
            }

            @Override
            public boolean canSort(UnitType t) {
                return false;
            }
        };
    }
}
