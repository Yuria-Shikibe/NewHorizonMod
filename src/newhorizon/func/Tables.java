package newhorizon.func;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Cicon;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.modules.ItemModule;
import newhorizon.block.special.JumpGate;

import static mindustry.Vars.state;
import static mindustry.Vars.ui;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;

public class Tables{
	public static class LinkTable extends Table{
		protected static float h = Core.graphics.isPortrait() ? 90f : 80f;
		protected static float w = Core.graphics.isPortrait() ? 330f : 600f;
		
		public static void sync(){
			h = Core.graphics.isPortrait() ? 90f : 80f;
			w = Core.graphics.isPortrait() ? 300f : 600f;
		}
		
		public LinkTable(Links.LinkEntry link){
			background(Tex.underline);
			margin(0);
			table(img -> {
				img.image().height(h - OFFSET / 2).width(LEN).color(link.color);
				img.row();
				img.image().height(OFFSET / 2).width(LEN).color(link.color.cpy().mul(0.8f, 0.8f, 0.8f, 1f));
			}).expandY();
			
			table(i -> {
				i.background(Tex.buttonEdge3);
				i.image(link.icon);
			}).size(h - OFFSET / 2, h);
			
			table(inset -> {
				inset.add("[accent]" + link.title).growX().left();
				inset.row();
				inset.labelWrap(link.description).width(w - LEN).color(Color.lightGray).growX();
			}).padLeft(OFFSET / 1.5f);
			
			button(Icon.link, () -> {
				if(!Core.app.openURI(link.link)){
					ui.showErrorMessage("@linkfail");
					Core.app.setClipboardText(link.link);
				}
			}).size(h);
		}
	}
	
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
					t2.table(Tex.clear, table2 -> TableFs.tableImageShrink(set.type.icon(Cicon.xlarge), LEN, table2)).size(LEN + OFFSET * 1.5f).left().padLeft(OFFSET);
					
					t2.pane(table2 -> {
						table2.add("[gray]Summon: [accent]" + set.type.localizedName + "[gray]; Level: [accent]" + set.level + "[gray].").left().row();
						table2.add("[gray]NeededTime: [accent]" + TableFs.format(set.costTimeVar() / 60) + " sec[gray].").row();
					}).growX().height(LEN).center();
					
					t2.table(stat).fillX().height(LEN + OFFSET).right().padRight(OFFSET);
				}).growX().fillY().padBottom(OFFSET / 2).row();
			}
		}
	}
	
	public static class ItemConsumeTable extends Table{
		public final @Nullable
		ItemModule itemModule;
		
		public ItemConsumeTable(@Nullable ItemModule itemModule){
			this.itemModule = itemModule;
			this.left();
		}
		
		public void add(ItemStack stack){
			float size = LEN - OFFSET;
			table(t -> {
				t.image(stack.item.icon(Cicon.xlarge)).size(size).left();
				t.table(n -> {
					Label l = new Label("");
					n.update(() -> {
						int amount = itemModule == null ? 0 : itemModule.get(stack.item);
						l.setText(String.valueOf(amount));
						l.setColor(amount < stack.amount ? Pal.redderDust : Color.white);
					});
					n.add(stack.item.localizedName + " ");
					n.add(l);
					n.add("/" + stack.amount);
				}).height(size).fillX().padLeft(OFFSET / 2).left();
			}).growX().height(size).left().row();
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
			cont.pane(table -> {
				table.add("[accent]" + NHSetting.modMeta.version + " [gray]Update Log:").center().row();
				addCloseListener();
				table.pane(t -> {
					t.add("@fix").color(Pal.accent).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFs.tabSpace + Core.bundle.get("update.fix")).row();
					
					t.add("@add").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFs.tabSpace + Core.bundle.get("update.add")).row();
					contentLog(t, contents);
					
					t.add("@remove").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFs.tabSpace + Core.bundle.get("update.remove")).row();
					
					t.add("@other").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
					t.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
					t.add(TableFs.tabSpace + Core.bundle.get("update.other")).row();
				}).growX().height((Core.graphics.getHeight() - LEN * 2) / (Vars.mobile ? 1.1f : 2.2f));
			}).grow().row();
			cont.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).bottom().row();
			cont.button("@back", Icon.left, Styles.cleart, this::hide).fillX().height(LEN).row();
		}
		
		public void contentLog(Table table, UnlockableContent[] contents){
			table.pane(t -> {
				int index = 0;
				for(UnlockableContent c : contents){
					if(index % 8 == 0)t.row();
					t.button(new TextureRegionDrawable(c.icon(Cicon.xlarge)), Styles.cleari, LEN, () -> {
						ContentInfoDialog dialog = new ContentInfoDialog();
						dialog.show(c);
					}).size(LEN);
					index++;
				}
			}).fillY().growX().row();
		}
	}
}
