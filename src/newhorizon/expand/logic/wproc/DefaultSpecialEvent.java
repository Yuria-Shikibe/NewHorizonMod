package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHLogic;
import newhorizon.expand.game.SpecialEventState;

/** Enables or disables automatic default special events. */
public class DefaultSpecialEvent extends LStatement {
    public boolean enabled = true;

    public DefaultSpecialEvent(String[] tokens) {
        if (tokens.length > 1) enabled = Boolean.parseBoolean(tokens[1]);
    }

    public DefaultSpecialEvent() {
    }

    @Override
    public void build(Table table) {
        table.check("Enable default special events", enabled, value -> enabled = value).left();
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
        builder.append("defaultspecialevent ").append(enabled);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultSpecialEventInstruction(enabled);
    }

    public static class DefaultSpecialEventInstruction implements LExecutor.LInstruction {
        private final boolean enabled;

        public DefaultSpecialEventInstruction(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void run(LExecutor exec) {
            SpecialEventState.setEnabled(enabled);
        }
    }
}
