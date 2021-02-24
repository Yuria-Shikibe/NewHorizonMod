package newhorizon.func;

import arc.Core;
import arc.func.Cons;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.block.special.JumpGate;

import static mindustry.Vars.state;
import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class Tables{
	public static class UnitSetTable extends Table{
		public UnitSetTable(JumpGate.UnitSet set, Cons<Table> stat){
			super();
			if(set.type.locked() && !state.rules.infiniteResources && state.isCampaign()){
				table(Tex.clear, t2 -> {
					t2.table(Tex.clear, table2 -> table2.image(Icon.lock).size(LEN).center()).left().fill().padLeft(OFFSET);
					
					t2.pane(table2 -> table2.add("[gray]Need to be researched.").left().row()).size(LEN * 6f, LEN).center();
				}).growX().fillY().padBottom(OFFSET / 2).row();
			}else{
				table(Tex.clear, t2 -> {
					t2.table(Tex.clear, table2 -> TableFuncs.tableImageShrink(set.type.icon(Cicon.xlarge), LEN, table2)).size(LEN + OFFSET * 1.5f).left().padLeft(OFFSET);
					
					t2.pane(table2 -> {
						table2.add("[lightgray]Summon: [accent]" + set.type.localizedName + "[lightgray]; Level: [accent]" + set.level + "[].").left().row();
						table2.add("[lightgray]NeededTime: [accent]" + TableFuncs.format(set.costTime() / 60) + "[lightgray] sec[].").row();
					}).growX().height(LEN).center();
					
					t2.table(stat).fillX().height(LEN + OFFSET).right().padRight(OFFSET);
				}).growX().fillY().padBottom(OFFSET / 2).row();
			}
		}
	}
	
	public static class ItemSelectTable extends Table{
		public boolean[] selects = new boolean[Vars.content.items().size];
		public ItemSelectTable(){
			background(Tex.button);
			pane(table -> {
				int i = 0;
				for(Item item : Vars.content.items()){
					if(i % 8 == 0)table.row();
					table.table(Tex.clear, t -> {
						t.button(new TextureRegionDrawable(item.icon(Cicon.xlarge)), Styles.clearTogglei, LEN - OFFSET, () -> {
							selects[Vars.content.items().indexOf(item)] = !selects[Vars.content.items().indexOf(item)];
						}).update(b -> b.setChecked(selects[Vars.content.items().indexOf(item)])).size(LEN);
					}).fill();
					i++;
				}
			}).grow();
		}
		public Seq<Item> getItems(Seq<Item> items){
			items.clear();
			for(Item item : Vars.content.items()){
				if(selects[Vars.content.items().indexOf(item)])items.add(item);
			}
			return items;
		}
		
		public Seq<Item> getItems(){
			Seq<Item> items = new Seq<>(Item.class);
			for(Item item : Vars.content.items()){
				if(selects[Vars.content.items().indexOf(item)])items.add(item);
			}
			return items;
		}
		
		
		public void write(Writes write){
			for(boolean b : selects){
				write.bool(b);
			}
		}
		
		public void read(Reads read, byte revision) {
			for(int i = 0; i < selects.length; i++){
				selects[i] = read.bool();
			}
		}
	}
	
	public static class LogDialog extends BaseDialog{
		public LogDialog(UnlockableContent[] contents){
			super("@log");
			cont.table(Tex.buttonEdge3, table -> {
				table.add("[accent]" + NHSetting.modMeta.version + " [gray]Update Log:").center().row();
				addCloseListener();
				table.pane(t -> {
					t.add("@fix").color(Pal.accent).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFuncs.tabSpace + Core.bundle.get("update.fix")).row();
					
					t.add("@add").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFuncs.tabSpace + Core.bundle.get("update.add")).row();
					contentLog(t, contents);
					
					t.add("@remove").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFuncs.tabSpace + Core.bundle.get("update.remove")).row();
					
					t.add("@other").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFuncs.tabSpace + Core.bundle.get("update.other")).row();
				}).growX().height((Core.graphics.getHeight() - LEN * 2) / (Vars.mobile ? 1.1f : 2.2f));
			}).growX().fillY().row();
			cont.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).bottom().row();
			cont.button("@back", Icon.left, Styles.cleart, this::hide).fillX().height(LEN).row();
		}
		
		public void contentLog(Table table, UnlockableContent[] contents){
			table.pane(t -> {
				for(UnlockableContent c : contents){
					c.display(t);
					t.row();
				}
			}).grow().row();
		}
	}
}
