package newhorizon.util.ui.frag;

import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.type.PayloadSeq;
import mindustry.ui.fragments.BlockInventoryFragment;
import newhorizon.NHUI;
import newhorizon.content.blocks.ModuleBlock;

import static mindustry.Vars.control;

public class PayloadInventoryFragment {
    public Table table = new Table();

    public void build(Group parent){
        table.name = "inventoryPayload";
        table.setTransform(true);
        parent.setTransform(true);
        parent.addChild(table);
    }

    public void rebuild(){
        if (!table.visible) return;
        table.clear();
        table.background(Tex.inventory);
        table.touchable = Touchable.disabled;
        table.margin(4f);
        table.defaults().size(8 * 5).pad(4f);
        int row = 0;
        int cols = 3;
        Building b = getBuild();
        if(b != null && b.getPayloads() != null){
            PayloadSeq inv = b.getPayloads();
            for (UnlockableContent content: ModuleBlock.modules){
                if (!inv.contains(content)) continue;
                Element image = itemImage(content.uiIcon, () -> !b.isValid() ? "": round(inv.get(content)));
                table.add(image);
                if(row++ % cols == cols - 1) table.row();
            }
        }
        updateTablePosition();
    }

    private Building getBuild(){
        try{
            return Reflect.get(control.input.inv, "build");
        }catch (Exception e){
            Log.err(e);
            return null;
        }
    }

    private String round(float f){
        f = (int)f;
        if(f >= 1000000){
            return (int)(f / 1000000f) + "[gray]" + UI.millions;
        }else if(f >= 1000){
            return (int)(f / 1000) + UI.thousands;
        }else{
            return (int)f + "";
        }
    }

    private void updateTablePosition(){
        table.pack();
        Table t = NHUI.itemInv;
        table.setPosition(t.x, t.y + t.getPrefHeight() - 4, Align.bottomLeft);

    }

    private Element itemImage(TextureRegion region, Prov<CharSequence> text){
        Stack stack = new Stack();

        Table t = new Table().left().bottom();
        t.label(text);

        stack.add(new Image(region));
        stack.add(t);
        return stack;
    }
}
