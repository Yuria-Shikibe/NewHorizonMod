package newhorizon.expand.block.cutscene;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NHVars;
import newhorizon.expand.cutscene.action.*;
import newhorizon.expand.cutscene.components.ActionBus;

public class SubActionBusBlock extends Block {
    public SubActionBusBlock(String name) {
        super(name);
        configurable = true;
        destructible = true;
        solid = true;

        category = Category.logic;
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    public class SubActionBusControllerBuild extends Building {
        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            table.button("trigger", () -> {
                ActionBus bus = new ActionBus();
                bus.addAll(
                        new InputLockAction(),

                        new CurtainFadeInAction(),
                        new InfoFadeInAction(15),
                        new InfoTextAction("[accent]<DUST TO DUST>[]"),
                        new WaitAction(270/60f),
                        new InfoFadeOutAction(30),
                        new WaitAction(20/60f),
                        new CurtainFadeOutAction(),

                        new InputUnlockAction()
                );
                NHVars.cutscene.addMainActionBus(bus);
            });
        }
    }
}
