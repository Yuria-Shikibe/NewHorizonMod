package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.ui.Styles;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class EventSpecial extends ActionLStatement {
    public String team = "@waveteam";
    public String alertTime = "20", spawnRange = "180";
    public boolean overrideDefaultCoordinate = false;
    public String targetX = "0", targetY = "0";

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
    }

    public EventSpecial() {
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
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, team, alertTime, spawnRange);
        writeTokens(builder, String.valueOf(overrideDefaultCoordinate));
        if (overrideDefaultCoordinate) writeTokens(builder, targetX, targetY);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new EventSpecialI(
                builder.var(team),
                builder.var(alertTime),
                builder.var(spawnRange),
                overrideDefaultCoordinate,
                builder.var(targetX),
                builder.var(targetY)
        );
    }

    public class EventSpecialI extends ActionInstruction {
        public boolean overrideDefaultCoordinate;
        public LVar team, alertTime, spawnRange, targetX, targetY;

        public EventSpecialI(
                LVar team, LVar alertTime, LVar spawnRange,
                boolean overrideDefaultCoordinate, LVar targetX, LVar targetY
        ) {
            this.team = team;
            this.alertTime = alertTime;
            this.spawnRange = spawnRange;
            this.overrideDefaultCoordinate = overrideDefaultCoordinate;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "event-special");
            appendExec(exec, team);
            appendExec(exec, alertTime, spawnRange);
            appendExec(exec, String.valueOf(overrideDefaultCoordinate));
            if (overrideDefaultCoordinate) appendExec(exec, targetX, targetY);
            endExec(exec);
        }
    }
}
