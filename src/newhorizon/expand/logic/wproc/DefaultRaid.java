package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHLogic;
import newhorizon.expand.game.RaidState;

/** Enables or disables the automatic default raid system. */
public class DefaultRaid extends LStatement {
    public boolean enabled = true;

    public DefaultRaid(String[] tokens) {
        if (tokens.length > 1) enabled = Boolean.parseBoolean(tokens[1]);
    }

    public DefaultRaid() {
    }

    @Override
    public void build(Table table) {
        table.check("Enable default air raids", enabled, value -> enabled = value).left();
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
        builder.append("defaultraid ").append(enabled);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultRaidInstruction(enabled);
    }

    public static class DefaultRaidInstruction implements LExecutor.LInstruction {
        private final boolean enabled;

        public DefaultRaidInstruction(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void run(LExecutor exec) {
            RaidState.setScale(enabled ? 1f : 0f);
        }
    }
}
