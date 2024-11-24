package newhorizon.expand.block.special;

import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.entities.WarpRift;
import newhorizon.util.func.NHFunc;

public class WarpPortalIniter extends Block {

    public WarpPortalIniter() {
        super("warp-portal-initer");

        alwaysUnlocked = true;
        destroySound = ambientSound = breakSound = Sounds.none;
        size = 1;
        update = true;
        outputsPayload = true;
        hasPower = false;
        configurable = true;
        clipSize = 120;
        saveConfig = true;
        rebuildable = false;
        solid = solidifes = false;
        requirements = ItemStack.empty;
        category = Category.units;
        destroyEffect = Fx.none;
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    public class WarpPortalIniterBuilding extends Building{
        public UnitType toSpawnType = UnitTypes.alpha;
        public float angle;
        public float delay = 6 * 60;
        public transient boolean addUnit = false;


        @Override
        public void onDestroyed(){}

        @Override
        public void afterDestroyed(){}

        @Override
        public void updateTile(){
            if(!addUnit)addUnit();
        }

        public void addUnit(){
            WarpRift warpRift = new WarpRift();
            warpRift.create(team, NHUnitTypes.anvil, x, y, 45);
            warpRift.add();
            kill();
            addUnit = true;
        }
    }
}
