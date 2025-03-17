package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;

public class InfoText extends LStatement {
    public String cutscene = "css", text = "\"<''DUST TO DUST''{wait}{wait}{wait}[n]January 21st - 22:14:50{wait}{wait}{wait}[n]Captain Price{wait}{wait}{wait}[n]Task Force 141{wait}{wait}{wait}[n]Arabian Peninsula>\"";

    public InfoText(String[] token){
        cutscene = token[1];
        text = token[2];
    }

    public InfoText() {}

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
        return new InfoTextI(builder.var(cutscene), builder.var(text));
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
        builder.append("infotext");
        builder.append(" ");
        builder.append(cutscene);
        builder.append(" ");
        builder.append(text);
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class InfoTextI implements LExecutor.LInstruction {
        public int cutscene, text;
        public InfoTextI(int cutscene, int text){
            this.cutscene = cutscene;
            this.text = text;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) exec.obj(cutscene);
            exec.setobj(cutscene, css + "info_text" + " " + exec.obj(text) + "\n");
        }
    }
}