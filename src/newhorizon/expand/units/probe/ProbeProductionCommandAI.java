package newhorizon.expand.units.probe;

import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import mindustry.entities.units.AIController;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public final class ProbeProductionCommandAI extends AIController {
    private boolean opened;

    @Override
    public void updateUnit() {
        if (!opened) {
            opened = true;
            if (unit instanceof ProbeEntity probe) ProbeProductionDialog.show(probe);
            if (unit.controller() instanceof CommandAI command) command.command(UnitCommand.moveCommand);
        }
    }
}
