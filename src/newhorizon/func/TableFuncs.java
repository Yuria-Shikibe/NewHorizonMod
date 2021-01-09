package newhorizon.func;

import java.text.DecimalFormat;

public class TableFuncs {
    public static final String tabSpace = "    ";
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    private static final DecimalFormat df = new DecimalFormat("######0.00");

    public static String format(float value){return df.format(value);}

    public static String getJudge(boolean value){return value ? "[green]Yes[]" : "[red]No[]";}
}
