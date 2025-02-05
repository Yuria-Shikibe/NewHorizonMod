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
                        new CurtainDrawAction(120),
                        new WaitAction(20),

                        new CurtainFadeInAction(),
                        new WaitAction(120),
                        new InfoFadeInAction(15),
                        new InfoTextAction("[accent]<DUST TO DUST>[]{WAIT}{WAIT}{WAIT}\n\n2/5 16:09:10{WAIT}\nMIDANTHA BASE{WAIT}\nSECTOR 161"),
                        new WaitAction(270),
                        new InfoFadeOutAction(30),
                        new WaitAction(20),
                        new CurtainFadeOutAction(),

                        new CameraControlAction(100, 600, 600),
                        new WaitAction(40),

                        new SignalCutInAction(20),
                        new SignalTextAction("yes you see some amongus words."),
                        new WaitAction(300),
                        new SignalTextAction("this is part of new horizon's cutscene."),
                        new WaitAction(300),
                        new SignalTextAction("lets return to your unit."),
                        new WaitAction(300),
                        new SignalCutOutAction(20),

                        new CameraResetAction(150),

                        new WaitAction(45),
                        new SignalCutInAction(20),
                        new SignalTextAction("yes this is you of course."),
                        new WaitAction(300),
                        new SignalTextAction("this cutscene is part of new horizon's current cutscene and im still expanding it."),
                        new WaitAction(450),
                        new SignalTextAction("now lets finish the cutscene."),
                        new WaitAction(240),
                        new SignalCutOutAction(20),

                        new WaitAction(60),
                        new CurtainRaiseAction(40),
                        new InputUnlockAction()
                );
                NHVars.cutscene.addMainActionBus(bus);
            });
        }
    }
}
