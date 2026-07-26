package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class EventSpecialUnit extends ActionLStatement {
    public String type = "@dagger";
    public String count = "1";
    public String status = "none";
    public String statusDuration = "10";
    public String item = "none";
    public String itemAmount = "0";
    public String flag = "nan";
    public String payload = "none";

    public EventSpecialUnit(String[] token) {
        ParseUtil.getFirstToken(token);
        type = ParseUtil.getNextToken(token);
        count = ParseUtil.getNextToken(token);
        status = ParseUtil.getNextToken(token);
        statusDuration = ParseUtil.getNextToken(token);
        item = ParseUtil.getNextToken(token);
        itemAmount = ParseUtil.getNextToken(token);
        flag = ParseUtil.getNextToken(token);
        payload = ParseUtil.getNextToken(token);
    }

    public EventSpecialUnit() {
    }

    @Override
    public String getLStatementName() {
        return "specialunit";
    }

    @Override
    public LCategory category() {
        return NHLogic.nhwproc;
    }

    @Override
    public void build(Table table) {
        buildRowTable(table, t -> {
            t.add(" Type: ");
            fields(t, type, str -> type = str).width(160f);
            t.add(" Count: ");
            fields(t, count, str -> count = str).width(70f);
        });

        buildRowTable(table, t -> {
            t.add(" Status: ");
            fields(t, status, str -> status = str).width(140f);
            t.add(" Dur(s): ");
            fields(t, statusDuration, str -> statusDuration = str).width(70f);
        });

        buildRowTable(table, t -> {
            t.add(" Item: ");
            fields(t, item, str -> item = str).width(140f);
            t.add(" Amt: ");
            fields(t, itemAmount, str -> itemAmount = str).width(70f);
        });

        buildRowTable(table, t -> {
            t.add(" Flag: ");
            fields(t, flag, str -> flag = str).width(100f);
            t.add(" Payload: ");
            fields(t, payload, str -> payload = str).width(140f);
        });
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder,
                ParseUtil.contentToken(type),
                count,
                ParseUtil.contentToken(status),
                statusDuration,
                ParseUtil.contentToken(item),
                itemAmount,
                flag == null || flag.isEmpty() ? "nan" : flag,
                ParseUtil.contentToken(payload)
        );
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new EventSpecialUnitI(
                ParseUtil.contentToken(type),
                builder.var(count),
                ParseUtil.contentToken(status),
                builder.var(statusDuration),
                ParseUtil.contentToken(item),
                builder.var(itemAmount),
                flag == null || flag.isEmpty() ? "nan" : flag,
                ParseUtil.contentToken(payload)
        );
    }

    public class EventSpecialUnitI extends ActionInstruction {
        public String type, status, item, flag, payload;
        public LVar count, statusDuration, itemAmount;

        public EventSpecialUnitI(
                String type, LVar count, String status, LVar statusDuration,
                String item, LVar itemAmount, String flag, String payload
        ) {
            this.type = type;
            this.count = count;
            this.status = status;
            this.statusDuration = statusDuration;
            this.item = item;
            this.itemAmount = itemAmount;
            this.flag = flag;
            this.payload = payload;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "event-special-unit");
            appendExec(exec, type);
            appendExec(exec, count);
            appendExec(exec, status);
            appendExec(exec, statusDuration);
            appendExec(exec, item);
            appendExec(exec, itemAmount);
            appendExec(exec, flag);
            appendExec(exec, payload);
            endExec(exec);
        }
    }
}
