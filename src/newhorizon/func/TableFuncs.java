package newhorizon.func;

import arc.scene.ui.Dialog;
import arc.struct.Seq;

import java.text.DecimalFormat;

public class TableFuncs {
    public static final Seq<Dialog> debugDialogs = new Seq<>();
    
    public static final String tabSpace = "    ";
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    private static final DecimalFormat df = new DecimalFormat("######0.00");

    public static String format(float value){return df.format(value);}

    public static String getJudge(boolean value){return value ? "[green]Yes[]" : "[red]No[]";}
}
