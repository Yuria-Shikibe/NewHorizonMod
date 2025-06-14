package newhorizon.expand.logic;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;

public class NHLogicStatement extends LStatement {
    public String name = "statement";

    public Seq<String> vars = Seq.with();

    public NHLogicStatement(String[] tokens) {
        try {
            phaseToken(tokens);
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public NHLogicStatement() {
    }

    public void phaseToken(String[] tokens) {
    }

    @Override
    public void build(Table table) {

    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return null;
    }

    public String statementCode() {
        StringBuilder str = new StringBuilder();
        str.append(name).append(" ");
        for (String token : vars) {
            str.append(token).append(" ");
        }
        return str.toString();
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append(statementCode());
    }
}
