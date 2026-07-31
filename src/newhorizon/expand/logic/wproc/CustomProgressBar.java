package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.logic.LVar;
import newhorizon.NHUI;
import newhorizon.content.NHLogic;
import newhorizon.util.ui.CustomProgressBarEntry;

import static mindustry.Vars.headless;

public class CustomProgressBar extends LStatement {
    public String icon = "@copper";
    public String current = "0";
    public String maximum = "100";
    public boolean setOnComplete;
    public String result = "name";
    public String resultValue = "1";

    public CustomProgressBar(String[] tokens) {
        if (tokens.length > 1) icon = tokens[1];
        if (tokens.length > 2) current = tokens[2];
        if (tokens.length > 3) maximum = tokens[3];
        if (tokens.length > 4) setOnComplete = Boolean.parseBoolean(tokens[4]);
        if (tokens.length > 5) result = tokens[5];
        if (tokens.length > 6) resultValue = tokens[6];
    }

    public CustomProgressBar() {
    }

    @Override
    public void build(Table table) {
        table.table(row -> {
            row.add(" Icon: ");
            fields(row, icon, value -> icon = value).width(150f);
        }).left().row();

        table.table(row -> {
            row.add(" Progress: ");
            fields(row, current, value -> current = value).width(100f);
            row.add(" / ");
            fields(row, maximum, value -> maximum = value).width(100f);
        }).left().row();

        table.table(row -> {
            row.check(" Set on complete", setOnComplete, value -> setOnComplete = value);
            fields(row, result, value -> result = value).width(100f);
            row.add(" = ");
            fields(row, resultValue, value -> resultValue = value).width(100f);
        }).left();
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
        builder.append("customprogress ")
                .append(icon).append(' ')
                .append(current).append(' ')
                .append(maximum).append(' ')
                .append(setOnComplete).append(' ')
                .append(result).append(' ')
                .append(resultValue);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CustomProgressBarInstruction(
                builder.var(icon),
                builder.var(current),
                builder.var(maximum),
                setOnComplete,
                builder.var(result),
                builder.var(resultValue)
        );
    }

    public static class CustomProgressBarInstruction implements LExecutor.LInstruction {
        public final LVar icon;
        public final LVar current;
        public final LVar maximum;
        public final boolean setOnComplete;
        public final LVar result;
        public final LVar resultValue;

        private CustomProgressBarEntry entry;
        private boolean completed;

        public CustomProgressBarInstruction(LVar icon, LVar current, LVar maximum, boolean setOnComplete, LVar result, LVar resultValue) {
            this.icon = icon;
            this.current = current;
            this.maximum = maximum;
            this.setOnComplete = setOnComplete;
            this.result = result;
            this.resultValue = resultValue;
        }

        @Override
        public void run(LExecutor exec) {
            float currentValue = current.numf();
            float maximumValue = maximum.numf();
            boolean nowCompleted = Float.isFinite(currentValue)
                    && Float.isFinite(maximumValue)
                    && maximumValue > 0f
                    && currentValue >= maximumValue;

            if (nowCompleted) {
                if (!completed) {
                    completed = true;
                    if (setOnComplete) result.setnum(resultValue.num());
                    if (!headless && entry != null) {
                        entry.update(resolvedIcon(), currentValue, maximumValue);
                        NHUI.completeCustomProgressBar(entry);
                        entry = null;
                    }
                }
                return;
            }

            completed = false;
            if (headless) return;

            if (entry == null) entry = NHUI.addCustomProgressBar();
            entry.update(resolvedIcon(), currentValue, maximumValue);
        }

        private Object resolvedIcon() {
            Object value = icon.obj();
            return value == null ? icon.name : value;
        }
    }
}
