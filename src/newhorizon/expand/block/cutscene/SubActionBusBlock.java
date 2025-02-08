package newhorizon.expand.block.cutscene;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.gen.Icon;
import mindustry.type.Category;
import mindustry.ui.Styles;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.cutscene.components.ActionControl;

import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutscene;

public class SubActionBusBlock extends MessageBlock {

    public SubActionBusBlock(String name) {
        super(name);

        maxTextLength = 5000;
        maxNewlines = 200;

        configurable = true;
        destructible = true;
        solid = true;

        category = Category.logic;
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class SubActionBusControllerBuild extends MessageBuild {

        @Override
        public void drawSelect() {}

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            table.button(Icon.play, Styles.cleari, this::playCutscene).size(40f);
        }

        public void playCutscene() {
            try{
                cutscene.addMainActionBus(ActionControl.phaseCode(message.toString()));
            }catch (Exception e){
                Log.err(e);
                ui.announce("Failed to create cutscene in block: " + tileX() + " " + tileY());
            }
        }
    }
}
