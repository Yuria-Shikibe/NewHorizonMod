package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;

public class SignalText extends LStatement {
    public String cutscene = "css", text = "\"<anuke likes frog>\"";

    public SignalText(String[] token){
        cutscene = token[1];
        text = token[2];
    }

    public SignalText() {}

    @Override
    public void build(Table table) {
        table.table(t -> {
            t.add(" Cutscene Name: ");
            fields(t, cutscene, str -> cutscene = str);
            t.left();
        }).growX();

        table.row();
        table.table(t -> {
            field(t, text, str -> text = str).width(0f).growX().padRight(3).maxTextLength(200);
        }).growX();
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new SignalTextI(builder.var(cutscene), builder.var(text));
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("signaltext");
        builder.append(" ");
        builder.append(cutscene);
        builder.append(" ");
        builder.append(text);
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class SignalTextI implements LExecutor.LInstruction {
        public int cutscene, text;
        public SignalTextI(int cutscene, int text){
            this.cutscene = cutscene;
            this.text = text;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) exec.obj(cutscene);
            exec.setobj(cutscene, css + "signal_text" + " " + exec.obj(text) + "\n");
        }
    }
}
