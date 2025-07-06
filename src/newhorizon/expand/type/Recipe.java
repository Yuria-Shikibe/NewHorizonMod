package newhorizon.expand.type;

import arc.struct.Seq;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.type.*;

public class Recipe{
    public static Recipe empty = new Recipe();
    public Seq<ItemStack> inputItem = new Seq<>();
    public Seq<LiquidStack> inputLiquid = new Seq<>();
    public Seq<PayloadStack> inputPayload = new Seq<>();

    public float boostScl = 1f;
    public int craftScl = 1;

    public Recipe(Object...objects){
        for (int i = 0; i < objects.length / 2; i++){
            if (objects[i * 2] instanceof Item item && objects[i * 2 + 1] instanceof Integer count){
                inputItem.add(new ItemStack(item, count));
            }else if (objects[i * 2] instanceof Liquid liquid && objects[i * 2 + 1] instanceof Float count){
                inputLiquid.add(new LiquidStack(liquid, count));
            }else if (objects[i * 2] instanceof UnlockableContent payload && objects[i * 2 + 1] instanceof Integer count){
                inputPayload.add(new PayloadStack(payload, count));
            }
        }

        if (objects.length % 2 != 0 && objects[objects.length - 1] instanceof Float multiplier){
            boostScl = multiplier;
        }
    }
}
