package newhorizon.expand.block.special;

import arc.graphics.g2d.Draw;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.Stat;
import mindustry.world.modules.ItemModule;

public class RemoteStorage extends StorageBlock {
    public float unloaderEfficiency = 0.25f;
    public RemoteStorage(String name) {
        super(name);

        update = true;
        allowConfigInventory = false;

        itemCapacity = 0;
    }

    @Override
    public void setBars() {
        super.setBars();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.maxEfficiency, unloaderEfficiency);
    }

    public class EnderChestStorageBuild extends StorageBuild {
        public ItemModule tmpItem = new ItemModule();
        @Override
        public void updateTile() {
            if ((Time.time + id) % 60f < unloaderEfficiency * 60){
                if (closestCore() != null) {
                    linkedCore = closestCore();
                    items = closestCore().items;
                }
            }else {
                linkedCore = null;
                items = tmpItem;
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return super.acceptItem(source, item);
        }

        @Override
        public void draw() {
            super.draw();
        }

        @Override
        public void drawSelect() {}

        @Override
        public boolean canPickup() {
             return false;
        }
    }
}
