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
import newhorizon.expand.logic.cutscene.types.RaidPreset;

public class EventRaid extends ActionLStatement {
    public RaidPreset raidType = RaidPreset.valueOf("PRESET_RAID_1");

    public boolean overrideRaidStats = false, overrideDefaultCoordinate = false;

    public String team = "@waveteam";
    public String alertTime = "15", raidTime = "5", raidScale = "1", inaccuracy = "40";
    public String sourceX = "0", sourceY = "0", targetX = "0", targetY = "0";

    public EventRaid(String[] token) {
        ParseUtil.getFirstToken(token);

        raidType = RaidPreset.valueOf(ParseUtil.getNextToken(token));
        team = ParseUtil.getNextToken(token);

        overrideRaidStats = ParseUtil.getNextBool(token);
        if (overrideRaidStats) {
            alertTime = ParseUtil.getNextToken(token);
            raidTime = ParseUtil.getNextToken(token);
            raidScale = ParseUtil.getNextToken(token);
            inaccuracy = ParseUtil.getNextToken(token);
        }

        overrideDefaultCoordinate = ParseUtil.getNextBool(token);
        if (overrideDefaultCoordinate) {
            sourceX = ParseUtil.getNextToken(token);
            sourceY = ParseUtil.getNextToken(token);
            targetX = ParseUtil.getNextToken(token);
            targetY = ParseUtil.getNextToken(token);
        }
    }

    public EventRaid() {}

    @Override
    public String getLStatementName() {
        return "raidevent";
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
            t.add(" Raid Preset Type: ");
            t.button(b -> {
                b.label(() -> raidType.name());
                b.clicked(() -> showSelect(b, RaidPreset.all, raidType, cType -> raidType = cType, 2, cell -> cell.size(240, 50)));
            }, Styles.logict, () -> {}).size(240, 40).color(table.color).left().padLeft(2);
        });

        buildRowTable(table, t -> {
            t.add(" From Team : ");
            fields(t, team, str -> team = str).width(180f);
        });

        buildRowTable(table, t -> t.button(b -> {
            b.label(() -> " Override Default Raid Stats ");
            b.clicked(() -> {
                overrideRaidStats = !overrideRaidStats;
                b.setChecked(overrideRaidStats);
                rebuild(table);
            });
        }, Styles.grayt, () -> {}).size(0, 40));

        if (overrideRaidStats) {
            buildRowTable(table, t -> {
                t.add(" Duration: < Alert: ").padLeft(20f);
                fields(t, alertTime, str -> alertTime = str);
                t.add(" , Raid: ");
                fields(t, raidTime, str -> raidTime = str);
                t.add(" > ");
            });

            buildRowTable(table, t -> {
                t.add(" Raid Scale (bullet/s): ").padLeft(20f);
                fields(t, raidScale, str -> raidScale = str);
            });

            buildRowTable(table, t -> {
                t.add(" Inaccuracy Radius (tiles): ").padLeft(20f);
                fields(t, inaccuracy, str -> inaccuracy = str);
            });
        }

        buildRowTable(table, t -> t.button(b -> {
            b.label(() -> " Override Default Raid Coordinate ");
            b.clicked(() -> {
                overrideDefaultCoordinate = !overrideDefaultCoordinate;
                b.setChecked(overrideDefaultCoordinate);
                rebuild(table);
            });
        }, Styles.grayt, () -> {}).size(0, 40));

        if (overrideDefaultCoordinate) {
            buildRowTable(table, t -> {
                t.add(" Source Position: < X: ").padLeft(20f);
                fields(t, sourceX, str -> sourceX = str);
                t.add(" , Y: ");
                fields(t, sourceY, str -> sourceY = str);
                t.add(" > ");
            });

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
        writeTokens(builder, raidType.name(), team);
        writeTokens(builder, String.valueOf(overrideRaidStats));
        if (overrideRaidStats) writeTokens(builder, alertTime, raidTime, raidScale, inaccuracy);
        writeTokens(builder, String.valueOf(overrideDefaultCoordinate));
        if (overrideDefaultCoordinate) writeTokens(builder, sourceX, sourceY, targetX, targetY);

    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new EventRaidI(
                raidType, builder.var(team),
                overrideRaidStats, builder.var(alertTime), builder.var(raidTime), builder.var(raidScale), builder.var(inaccuracy),
                overrideDefaultCoordinate, builder.var(sourceX), builder.var(sourceY), builder.var(targetX), builder.var(targetY)
        );
    }

    public class EventRaidI extends ActionInstruction {
        public RaidPreset raidType;

        public boolean overrideRaidStats, overrideDefaultCoordinate;

        public LVar team;
        public LVar alertTime, raidTime, raidScale, inaccuracy ;
        public LVar sourceX, sourceY, targetX, targetY;

        public EventRaidI(
                RaidPreset raidType, LVar team,
                boolean overrideRaidStats, LVar alertTime, LVar raidTime, LVar raidScale, LVar inaccuracy,
                boolean overrideDefaultCoordinate, LVar sourceX, LVar sourceY, LVar targetX, LVar targetY
        ) {
            this.raidType = raidType;
            this.team = team;

            this.overrideRaidStats = overrideRaidStats;
            this.alertTime = alertTime;
            this.raidTime = raidTime;
            this.raidScale = raidScale;
            this.inaccuracy = inaccuracy;

            this.overrideDefaultCoordinate = overrideDefaultCoordinate;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "event-raid");
            appendExec(exec, raidType.name());
            appendExec(exec, team);
            appendExec(exec, String.valueOf(overrideRaidStats));
            if (overrideRaidStats) appendExec(exec, alertTime, raidTime, raidScale, inaccuracy);
            appendExec(exec, String.valueOf(overrideDefaultCoordinate));
            if (overrideDefaultCoordinate) appendExec(exec, sourceX, sourceY, targetX, targetY);
            endExec(exec);
        }
    }
}
