package newhorizon.expand.block.environment;

import arc.Core;
import arc.util.Strings;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;

public class OreVein extends OreBlock {
    public float density;
    public OreVein(String name, Item ore, float density) {
        super(name, ore);
        this.density = density;
    }

    public void setup(Item ore){
        super.setup(ore);
        this.localizedName = Core.bundle.get(getContentType() + "." + this.name + ".name", this.name);
    }

    public String getDisplayName(Tile tile){
        return Strings.fixed(density, 1) + "x " + itemDrop.localizedName;
    }
}
