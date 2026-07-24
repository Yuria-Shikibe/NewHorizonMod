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

public class EventIntervention extends ActionLStatement {
    public String eventId = "1";
    public boolean overrideStats = false, overrideDefaultCoordinate = false;
    public String team = "@waveteam";
    public String alertTime = "30", spawnRange = "180";
    public String targetX = "0", targetY = "0";

    public EventIntervention(String[] token) {
        ParseUtil.getFirstToken(token);
        eventId = ParseUtil.getNextToken(token);
        team = ParseUtil.getNextToken(token);

        overrideStats = ParseUtil.getNextBool(token);
        if (overrideStats) {
            alertTime = ParseUtil.getNextToken(token);
            spawnRange = ParseUtil.getNextToken(token);
        }

        overrideDefaultCoordinate = ParseUtil.getNextBool(token);
        if (overrideDefaultCoordinate) {
            targetX = ParseUtil.getNextToken(token);
            targetY = ParseUtil.getNextToken(token);
        }
    }

    public EventIntervention() {
    }

    @Override
    public String getLStatementName() {
        return "interventionevent";
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
            t.add(" Fleet Event Id: ");
            fields(t, eventId, str -> eventId = str).width(120f);
        });

        buildRowTable(table, t -> {
            t.add(" From Team : ");
            fields(t, team, str -> team = str).width(180f);
        });

        buildRowTable(table, t -> t.button(b -> {
            b.label(() -> " Override Default Fleet Stats ");
            b.clicked(() -> {
                overrideStats = !overrideStats;
                b.setChecked(overrideStats);
                rebuild(table);
            });
        }, Styles.grayt, () -> {
        }).size(0, 40));

        if (overrideStats) {
            buildRowTable(table, t -> {
                t.add(" Alert Time (s): ").padLeft(20f);
                fields(t, alertTime, str -> alertTime = str);
            });
            buildRowTable(table, t -> {
                t.add(" Spawn Range: ").padLeft(20f);
                fields(t, spawnRange, str -> spawnRange = str);
            });
        }

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
        writeTokens(builder, eventId);
        writeTokens(builder, team);
        writeTokens(builder, String.valueOf(overrideStats));
        if (overrideStats) writeTokens(builder, alertTime, spawnRange);
        writeTokens(builder, String.valueOf(overrideDefaultCoordinate));
        if (overrideDefaultCoordinate) writeTokens(builder, targetX, targetY);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new EventInterventionI(
                builder.var(eventId), builder.var(team),
                overrideStats, builder.var(alertTime), builder.var(spawnRange),
                overrideDefaultCoordinate, builder.var(targetX), builder.var(targetY)
        );
    }

    public class EventInterventionI extends ActionInstruction {
        public boolean overrideStats, overrideDefaultCoordinate;
        public LVar eventId, team, alertTime, spawnRange, targetX, targetY;

        public EventInterventionI(
                LVar eventId, LVar team,
                boolean overrideStats, LVar alertTime, LVar spawnRange,
                boolean overrideDefaultCoordinate, LVar targetX, LVar targetY
        ) {
            this.eventId = eventId;
            this.team = team;
            this.overrideStats = overrideStats;
            this.alertTime = alertTime;
            this.spawnRange = spawnRange;
            this.overrideDefaultCoordinate = overrideDefaultCoordinate;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "event-intervention");
            appendExec(exec, String.valueOf((int) eventId.numf()));
            appendExec(exec, team);
            appendExec(exec, String.valueOf(overrideStats));
            if (overrideStats) appendExec(exec, alertTime, spawnRange);
            appendExec(exec, String.valueOf(overrideDefaultCoordinate));
            if (overrideDefaultCoordinate) appendExec(exec, targetX, targetY);
            endExec(exec);
        }
    }
}
