package newhorizon.util.ui.display;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.ui.Styles;

public class ItemImage extends Stack {
    public ItemImage(TextureRegion region, int amount){

        add(new Table(o -> {
            o.left();
            o.add(new Image(region)).size(32f).scaling(Scaling.fit);
        }));

        if(amount != 0){
            add(new Table(t -> {
                t.left().bottom();
                t.add(amount >= 1000 ? UI.formatAmount(amount) : amount + "").style(Styles.outlineLabel);
                t.pack();
            }));
        }
    }

    public ItemImage(ItemStack stack){
        this(stack.item.uiIcon, stack.amount);
    }

    public ItemImage(PayloadStack stack){
        this(stack.item.uiIcon, stack.amount);
    }
}
