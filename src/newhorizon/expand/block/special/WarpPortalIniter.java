package newhorizon.expand.block.special;

import arc.graphics.g2d.Draw;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.entities.WarpRift;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

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
            //if(!addUnit)addUnit();
        }

        @Override
        public void buildConfiguration(Table table){
            table.button("summon", this::addUnit).growX().row();
            table.slider(0, 360, 14, 0, f -> angle = f).growX().row();
            ItemSelection.buildTable(table, content.units().select(b -> !b.isHidden()), this::type, this::configure);
        }

        public UnitType type(){
            return toSpawnType;
        }

        @Override
        public void configure(Object value) {
            toSpawnType = (UnitType) value;
        }

        @Override
        public void draw(){
            super.draw();
            Tmp.v1.trns(angle, tilesize * size * 4f);
            Drawf.arrow(x, y, x + Tmp.v1.x, y + Tmp.v1.y, size * tilesize, tilesize / 2f);
            if(toSpawnType == null) return;

            Drawf.light(x, y, tilesize * size * 3f, team.color, 0.8f);
            Draw.z(Layer.overlayUI);
            Draw.rect(toSpawnType.fullIcon, x, y, size * tilesize, size * tilesize);
        }

        public void addUnit(){
            WarpRift warpRift = new WarpRift();
            warpRift.create(team, toSpawnType, x, y, angle);
            warpRift.add();
            kill();
            addUnit = true;
        }
    }
}
