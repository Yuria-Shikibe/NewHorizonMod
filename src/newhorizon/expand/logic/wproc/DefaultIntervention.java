package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHLogic;
import newhorizon.expand.game.InterventionState;

/** Enables or disables the automatic default intervention system. */
public class DefaultIntervention extends LStatement {
    public boolean enabled = true;

    public DefaultIntervention(String[] tokens) {
        if (tokens.length > 1) enabled = Boolean.parseBoolean(tokens[1]);
    }

    public DefaultIntervention() {
    }

    @Override
    public void build(Table table) {
        table.check("Enable default interventions", enabled, value -> enabled = value).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHLogic.nhwproc;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("defaultintervention ").append(enabled);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultInterventionInstruction(enabled);
    }

    public static class DefaultInterventionInstruction implements LExecutor.LInstruction {
        private final boolean enabled;

        public DefaultInterventionInstruction(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void run(LExecutor exec) {
            InterventionState.setScale(enabled ? 1f : 0f);
        }
    }
}
