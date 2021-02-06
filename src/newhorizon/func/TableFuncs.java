package newhorizon.func;

import arc.math.Mathf;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.entities.bullet.BulletType;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

public class TableFuncs {
    public static final Seq<Dialog> debugDialogs = new Seq<>();
    
    public static final String tabSpace = "    ";
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    private static final DecimalFormat df = new DecimalFormat("######0.00");

    public static String format(float value){return df.format(value);}
    public static String getJudge(boolean value){return value ? "[green]Yes[]" : "[red]No[]";}
    public static String getPercent(float value){return Mathf.floor(value * 100) + "%";}
    
    public static void buildBulletTypeInfo(Table t, BulletType type){
        t.table(table -> {
            if(type == null)return;
            Class<?> typeClass = type.getClass();
            Field[] fields = typeClass.getFields();
            for(Field field : fields){
                try{
                    //table.add(field.getGenericType().toString()).row();
                    
                    if(field.getGenericType().toString().equals("boolean")) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": ").append(getJudge(field.getBoolean(type))).append("[]")).left().row();
                    if(field.getGenericType().toString().equals("float") && field.getFloat(type) > 0) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [accent]").append(field.getFloat(type)).append("[]")).left().row();
                    if(field.getGenericType().toString().equals("int") && field.getInt(type) > 0) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [accent]").append(field.getInt(type)).append("[]")).left().row();
    
                    if(field.getType().getSimpleName().equals("BulletType")){
                        BulletType inner = (BulletType)field.get(type);
                        if(inner == null || inner.toString().equals("bullet#0") || inner.toString().equals("bullet#1"))continue;
                        
                        Log.info(inner);
                        table.add("[gray]" + field.getName() + "{ ").left().row();
                        table.table(in -> {
                            buildBulletTypeInfo(in, inner);
                        }).padLeft(LEN).row();
                        table.add("[gray]}").left().row();
                    }
                }catch(IllegalAccessException err){
                    throw new IllegalArgumentException(err);
                }
            }
        }).row();
    }
}
