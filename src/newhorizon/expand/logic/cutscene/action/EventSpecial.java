package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.ui.Styles;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class EventSpecial extends ActionLStatement {
    public static final class UnitConfig {
        public String type = "@dagger";
        public String count = "1";
        public String status = "none";
        public String statusDuration = "10";
        public String item = "none";
        public String itemAmount = "0";
        public String flag = "nan";
        public String payload = "none";
    }

    public String team = "@waveteam";
    public String alertTime = "20", spawnRange = "180";
    public boolean overrideDefaultCoordinate = false;
    public String targetX = "0", targetY = "0";
    public final Seq<UnitConfig> units = new Seq<>();

    public EventSpecial(String[] token) {
        ParseUtil.getFirstToken(token);
        team = ParseUtil.getNextToken(token);
        alertTime = ParseUtil.getNextToken(token);
        spawnRange = ParseUtil.getNextToken(token);

        overrideDefaultCoordinate = ParseUtil.getNextBool(token);
        if (overrideDefaultCoordinate) {
            targetX = ParseUtil.getNextToken(token);
            targetY = ParseUtil.getNextToken(token);
        }

        units.clear();
        int count = ParseUtil.getNextInt(token);
        for (int i = 0; i < count; i++) {
            UnitConfig conf = new UnitConfig();
            conf.type = ParseUtil.getNextToken(token);
            conf.count = ParseUtil.getNextToken(token);
            conf.status = ParseUtil.getNextToken(token);
            conf.statusDuration = ParseUtil.getNextToken(token);
            conf.item = ParseUtil.getNextToken(token);
            conf.itemAmount = ParseUtil.getNextToken(token);
            conf.flag = ParseUtil.getNextToken(token);
            conf.payload = ParseUtil.getNextToken(token);
            units.add(conf);
        }
        if (units.isEmpty()) units.add(new UnitConfig());
    }

    public EventSpecial() {
        units.add(new UnitConfig());
    }

    @Override
    public String getLStatementName() {
        return "specialevent";
    }

    @Override
    public LCategory category() {
        return NHLogic.nhwproc;
    }

    @Override
    public void build(Table table) {
        rebuild(table);
    }

    public void rebuild(Table table) {
        table.clearChildren();

        buildRowTable(table, t -> {
            t.add(" From Team : ");
            fields(t, team, str -> team = str).width(180f);
        });

        buildRowTable(table, t -> {
            t.add(" Alert Time (s): ");
            fields(t, alertTime, str -> alertTime = str);
            t.add("  Spawn Range: ");
            fields(t, spawnRange, str -> spawnRange = str);
        });

        buildRowTable(table, t -> t.button(b -> {
            b.label(() -> " Override Target Position ");
            b.clicked(() -> {
                overrideDefaultCoordinate = !overrideDefaultCoordinate;
                b.setChecked(overrideDefaultCoordinate);
                rebuild(table);
            });
        }, Styles.grayt, () -> {
        }).size(0, 40));

        if (overrideDefaultCoordinate) {
            buildRowTable(table, t -> {
                t.add(" Target Position: < X: ").padLeft(20f);
                fields(t, targetX, str -> targetX = str);
                t.add(" , Y: ");
                fields(t, targetY, str -> targetY = str);
                t.add(" > ");
            });
        }

        buildRowTable(table, t -> {
            t.add(" Units: ");
            t.button("+", Styles.logict, () -> {
                if (units.size < 8) {
                    units.add(new UnitConfig());
                    rebuild(table);
                }
            }).size(40, 40);
        });

        for (int i = 0; i < units.size; i++) {
            int index = i;
            UnitConfig conf = units.get(i);

            buildRowTable(table, t -> {
                t.add(" #" + (index + 1)).padLeft(8f).width(30f);
                t.button("-", Styles.logict, () -> {
                    if (units.size > 1) {
                        units.remove(index);
                        rebuild(table);
                    }
                }).size(40, 40);
            });

            buildRowTable(table, t -> {
                t.add(" Type: ").padLeft(20f);
                fields(t, conf.type, str -> conf.type = str).width(160f);
                t.add(" Count: ");
                fields(t, conf.count, str -> conf.count = str).width(70f);
            });

            buildRowTable(table, t -> {
                t.add(" Status: ").padLeft(20f);
                fields(t, conf.status, str -> conf.status = str).width(140f);
                t.add(" Dur(s): ");
                fields(t, conf.statusDuration, str -> conf.statusDuration = str).width(70f);
            });

            buildRowTable(table, t -> {
                t.add(" Item: ").padLeft(20f);
                fields(t, conf.item, str -> conf.item = str).width(140f);
                t.add(" Amt: ");
                fields(t, conf.itemAmount, str -> conf.itemAmount = str).width(70f);
            });

            buildRowTable(table, t -> {
                t.add(" Flag: ").padLeft(20f);
                fields(t, conf.flag, str -> conf.flag = str).width(100f);
                t.add(" Payload: ");
                fields(t, conf.payload, str -> conf.payload = str).width(140f);
            });
        }
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, team, alertTime, spawnRange);
        writeTokens(builder, String.valueOf(overrideDefaultCoordinate));
        if (overrideDefaultCoordinate) writeTokens(builder, targetX, targetY);
        writeTokens(builder, String.valueOf(units.size));
        for (UnitConfig conf : units) {
            writeTokens(builder,
                    ParseUtil.contentToken(conf.type),
                    conf.count,
                    ParseUtil.contentToken(conf.status),
                    conf.statusDuration,
                    ParseUtil.contentToken(conf.item),
                    conf.itemAmount,
                    conf.flag == null || conf.flag.isEmpty() ? "nan" : conf.flag,
                    ParseUtil.contentToken(conf.payload)
            );
        }
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        Seq<UnitRuntime> runtimes = new Seq<>();
        for (UnitConfig conf : units) {
            UnitRuntime runtime = new UnitRuntime();
            runtime.type = ParseUtil.contentToken(conf.type);
            runtime.count = builder.var(conf.count);
            runtime.status = ParseUtil.contentToken(conf.status);
            runtime.statusDuration = builder.var(conf.statusDuration);
            runtime.item = ParseUtil.contentToken(conf.item);
            runtime.itemAmount = builder.var(conf.itemAmount);
            runtime.flag = conf.flag == null || conf.flag.isEmpty() ? "nan" : conf.flag;
            runtime.payload = ParseUtil.contentToken(conf.payload);
            runtimes.add(runtime);
        }

        return new EventSpecialI(
                builder.var(team),
                builder.var(alertTime),
                builder.var(spawnRange),
                overrideDefaultCoordinate,
                builder.var(targetX),
                builder.var(targetY),
                runtimes
        );
    }

    public static final class UnitRuntime {
        public String type, status, item, flag, payload;
        public LVar count, statusDuration, itemAmount;
    }

    public class EventSpecialI extends ActionInstruction {
        public boolean overrideDefaultCoordinate;
        public LVar team, alertTime, spawnRange, targetX, targetY;
        public Seq<UnitRuntime> units;

        public EventSpecialI(
                LVar team, LVar alertTime, LVar spawnRange,
                boolean overrideDefaultCoordinate, LVar targetX, LVar targetY,
                Seq<UnitRuntime> units
        ) {
            this.team = team;
            this.alertTime = alertTime;
            this.spawnRange = spawnRange;
            this.overrideDefaultCoordinate = overrideDefaultCoordinate;
            this.targetX = targetX;
            this.targetY = targetY;
            this.units = units;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "event-special");
            appendExec(exec, team);
            appendExec(exec, alertTime, spawnRange);
            appendExec(exec, String.valueOf(overrideDefaultCoordinate));
            if (overrideDefaultCoordinate) appendExec(exec, targetX, targetY);
            appendExec(exec, String.valueOf(units.size));
            for (UnitRuntime unit : units) {
                appendExec(exec, unit.type);
                appendExec(exec, unit.count);
                appendExec(exec, unit.status);
                appendExec(exec, unit.statusDuration);
                appendExec(exec, unit.item);
                appendExec(exec, unit.itemAmount);
                appendExec(exec, unit.flag);
                appendExec(exec, unit.payload);
            }
            endExec(exec);
        }
    }
}
