package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHLogic;
import newhorizon.expand.game.WeatherEventState;

/** Enables or disables the automatic weather forecast and scheduler. */
public class DefaultWeatherEvent extends LStatement {
    public boolean enabled = true;

    public DefaultWeatherEvent(String[] tokens) {
        if (tokens.length > 1) enabled = Boolean.parseBoolean(tokens[1]);
    }

    public DefaultWeatherEvent() {
    }

    @Override
    public void build(Table table) {
        table.check("@nh.default-weather-event.enable", enabled, value -> enabled = value).left();
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
        builder.append("defeatweatherevent ").append(enabled);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new DefaultWeatherEventInstruction(enabled);
    }

    public static class DefaultWeatherEventInstruction implements LExecutor.LInstruction {
        private final boolean enabled;

        public DefaultWeatherEventInstruction(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void run(LExecutor exec) {
            WeatherEventState.update(enabled);
        }
    }
}
