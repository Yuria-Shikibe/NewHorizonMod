package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.game.WeatherEventState;

/** Immediately creates one selected storm when the processor starts running. */
public class WeatherEvent extends LStatement {
    public String storm = "random";
    public String duration = "30";

    public WeatherEvent(String[] tokens) {
        if (tokens.length > 1 && ("true".equalsIgnoreCase(tokens[1]) || "false".equalsIgnoreCase(tokens[1]))) {
            if (tokens.length > 2) storm = tokens[2];
            if (tokens.length > 3) duration = tokens[3];
        } else {
            if (tokens.length > 1) storm = tokens[1];
            if (tokens.length > 2) duration = tokens[2];
        }
    }

    public WeatherEvent() {
    }

    @Override
    public void build(Table table) {
        table.add("@nh.weather-event.action").left().padBottom(4f).row();
        table.table(row -> {
            row.add("@nh.weather-event.storm").left().padRight(8f);
            fields(row, storm, value -> storm = value).width(120f).padRight(12f);
            row.add("@nh.weather-event.duration").left().padRight(8f);
            fields(row, duration, value -> duration = value).width(80f);
        }).left().row();
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
        builder.append("weatherevent ").append(storm).append(' ').append(duration);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new WeatherEventInstruction(builder.var(storm), builder.var(duration));
    }

    public static class WeatherEventInstruction implements LExecutor.LInstruction {
        private final LVar storm;
        private final LVar duration;
        private boolean triggered;

        public WeatherEventInstruction(LVar storm, LVar duration) {
            this.storm = storm;
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            if (!triggered) {
                WeatherEventState.triggerManual(resolveStorm(), duration.numf());
                triggered = true;
            }
        }

        private int resolveStorm() {
            String token = storm.name == null ? "" : storm.name.toLowerCase();
            if (token.contains("quantum")) return 0;
            if (token.contains("solar")) return 1;
            if (token.contains("random")) return -1;

            Object value = storm.obj();
            if (value != null) {
                String name = String.valueOf(value).toLowerCase();
                if (name.contains("quantum")) return 0;
                if (name.contains("solar")) return 1;
                if (name.contains("random")) return -1;
            }
            float number = storm.numf();
            return Float.isFinite(number) ? Math.round(number) : -1;
        }
    }
}
