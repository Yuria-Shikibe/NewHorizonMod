package newhorizon.util.ui;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.world.Block;
import newhorizon.expand.block.inner.ModulePayload;

import static mindustry.Vars.control;
import static mindustry.Vars.state;

public class ContentSelectionTable {
    public static void buildModuleTable(@Nullable Block block, Table table, Seq<Block> items, Prov<Block> holder, Cons<Block> consumer){
        ButtonGroup<ImageButton> group = new ButtonGroup<>();
        group.setMinCheckCount(0);

        Table cont = new Table().top();
        cont.defaults().size(240, 48);

        Runnable rebuild = () -> {
            group.clear();
            cont.clearChildren();

            for(Block item : items){
                if(!item.unlockedNow()) continue;

                ImageButton button = cont.button(Tex.whiteui, Styles.clearNoneTogglei, 48f, () -> control.input.config.hideConfig()).group(group).get();
                button.table(t -> t.label(() -> item.localizedName).size(180, 0)).padLeft(6).padRight(6);

                button.changed(() -> consumer.get(button.isChecked() ? item : null));
                button.getStyle().imageUp = new TextureRegionDrawable(item.uiIcon);
                button.update(() -> button.setChecked(holder.get() == item));

                cont.row();
            }
        };

        rebuild.run();

        Table main = new Table().background(Styles.black6);

        ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
        pane.setScrollingDisabled(true, false);
        pane.exited(() -> {
            if(pane.hasScroll()){
                Core.scene.setScrollFocus(null);
            }
        });

        if(block != null){
            pane.setScrollYForce(block.selectScroll);
            pane.update(() -> block.selectScroll = pane.getScrollY());
        }

        pane.setOverscroll(false, false);
        main.add(pane).maxHeight(192);
        table.top().add(main);
    }
}
