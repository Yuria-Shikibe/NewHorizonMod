package newhorizon.data;

import arc.scene.ui.layout.Table;
import mindustry.type.ItemStack;

public abstract class UpgradeLevelData extends UpgradeData{
    public int level;

    public abstract void buildTableComplete(Table table);

    public UpgradeLevelData(){
        super();
    }

    public UpgradeLevelData(
            String name,
            String description,
            float costTime,
            ItemStack... items
    ) {
        super(name, description, costTime, items);
        unlockLevel = 0;
        this.id = -1;
    }
}
