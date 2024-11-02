package newhorizon.util.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.meta.*;
import mindustry.world.modules.ItemModule;
import newhorizon.NHUI;
import newhorizon.NewHorizon;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.util.annotation.HeadlessDisabled;
import newhorizon.util.func.NHInterp;
import newhorizon.util.ui.display.ItemImage;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class NHUIFunc{
	private static float damage = 0;
	private static long lastToast, waiting;
	
	public static Table coreInfo;
	
	public static void init(){
		coreInfo = ui.hudGroup.find("coreinfo");
	}
	
	public static void show(Table p, UnlockableContent content){
		p.clear();
		
		Table table = new Table();
		table.margin(10);
		
		//initialize stats if they haven't been yet
		content.checkStats();
		
		table.table(title1 -> {
			title1.image(content.uiIcon).size(iconXLarge).scaling(Scaling.fit);
			title1.add("[accent]" + content.localizedName + (settings.getBool("console") ? "\n[gray]" + content.name : "")).padLeft(5);
		});
		
		table.row();
		
		if(content.description != null){
			boolean any = content.stats.toMap().size > 0;
			
			if(any){
				table.add("@category.purpose").color(Pal.accent).fillX().padTop(10);
				table.row();
			}
			
			table.add("[lightgray]" + content.displayDescription()).wrap().fillX().padLeft(any ? 10 : 0).width(500f).padTop(any ? 0 : 10).left();
			table.row();
			
			if(!content.stats.useCategories && any){
				table.add("@category.general").fillX().color(Pal.accent);
				table.row();
			}
		}
		
		Stats stats = content.stats;
		
		for(StatCat cat : stats.toMap().keys()){
			OrderedMap<Stat, Seq<StatValue>> map = stats.toMap().get(cat);
			
			if(map.size == 0) continue;
			
			if(stats.useCategories){
				table.add("@category." + cat.name).color(Pal.accent).fillX();
				table.row();
			}
			
			for(Stat stat : map.keys()){
				table.table(inset -> {
					inset.left();
					inset.add("[lightgray]" + stat.localized() + ":[] ").left().top();
					Seq<StatValue> arr = map.get(stat);
					for(StatValue value : arr){
						value.display(inset);
						inset.add().size(10f);
					}
					
				}).fillX().padLeft(10);
				table.row();
			}
		}
		
		if(content.details != null){
			table.add("[gray]" + (content.unlocked() || !content.hideDetails ? content.details : Iconc.lock + " " + Core.bundle.get("unlock.incampaign"))).pad(6).padTop(20).width(400f).wrap().fillX();
			table.row();
		}
		
		content.displayExtra(table);
		
		ScrollPane pane = new ScrollPane(table);
		p.add(pane);
	}
	
	@HeadlessDisabled
	public static void showLabel(float duration, Cons<Table> modifier){
		if(state.isMenu() || headless)return;
		
		scheduleToast(duration, () -> {
			if(state.isMenu())return;
			
			Table table = new Table(){{
				touchable = Touchable.disabled;
				update(() -> {
					if(state.isMenu())remove();
					setWidth(NHUI.getWidth());
					setPosition(0, (NHUI.getHeight() - height) / 2);
				});
				color.a(0);
				actions(Actions.fadeIn(0.45f, NHInterp.bounce5Out), Actions.delay(duration), Actions.fadeOut(0.5f), Actions.remove());
			}}.margin(4);
			
			modifier.get(table);
			
			table.pack();
			table.act(0f);
			
			NHUI.root.addChild(table);
		});
	}
	
	@HeadlessDisabled
	private static void scheduleToast(float time, Runnable run){
		if(waiting > 5)return;
		long duration = (int)((time + 1.25f) * 1000);
		long since = Time.timeSinceMillis(lastToast);
		if(since > duration){
			lastToast = Time.millis();
			run.run();
		}else{
			waiting++;
			Time.runTask((duration - since) / 1000f * 60f, () -> {
				waiting--;
				run.run();
			});
			lastToast += duration;
		}
	}
	
	public static int getTotalShots(ShootPattern pattern){
		int total = 0;
		
		if(pattern instanceof ShootMulti){
			ShootMulti multi = (ShootMulti)pattern;
			for(ShootPattern p : multi.dest)total += getTotalShots(p);
			total *= multi.source.shots;
		}else total = pattern.shots;
		
		return total;
	}
	
	public static float estimateBulletDamage(BulletType type, int num, boolean init){
		if(init)damage = 0;
		
		damage += type.damage * num / 1.8f * Mathf.num(type.collides && (type.collidesGround || type.collidesTiles || type.collidesAir));
		if(type.splashDamage > 0 && type.splashDamageRadius > 0)damage += type.splashDamage * Mathf.sqrt(type.splashDamageRadius) / tilesize / 3.0f;
		damage += type.lightningDamage * type.lightning * (type.lightningLength + (type.lightningLengthRand + 1) / 3f) / 3f;
		
		
		if(type.fragBullet != null)damage += estimateBulletDamage(type.fragBullet, type.fragBullets, false);
		if(type.intervalBullet != null)damage += estimateBulletDamage(type.intervalBullet, type.intervalBullets, false) * type.lifetime / type.bulletInterval;
		
		return damage;
	}
	
	public static void ammo(Table table, String name, BulletType type, TextureRegion icon){
		table.row();
		
		table.table().padTop(OFFSET);
		table.image(icon).size(3 * 8).padRight(4).right().top();
		if(!name.isEmpty())table.add(name).padRight(10).left().top();
		
		table.table(bt -> {
			bt.left().defaults().padRight(3).left();
			
			StatValues.ammo(ObjectMap.of(UnitTypes.block, type), 0, false).display(bt);
		}).padTop(-9).left().get().background(Tex.underline);
		
		table.row();
	}
	
	protected static void sep(Table table, String text){
		table.row();
		table.add(text);
	}
	
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
			if(state.rules.bannedUnits.contains(set.type)){
				table(Styles.grayPanel, t2 -> {
					t2.margin(6f);
					t2.defaults().left().padRight(OFFSET);
					t2.table(Tex.clear, table2 -> {
						TableFunc.tableImageShrink(set.type.fullIcon, LEN, table2, i -> i.color.set(Pal.gray));
						table2.image(Icon.cancel).size(LEN + OFFSET * 1.5f).color(Color.scarlet).padLeft(OFFSET / 2f);
					}).left().padLeft(OFFSET * 2f);
					
					t2.pane(table2 -> table2.add(Core.bundle.get("banned")));
				}).growX().fillY().padBottom(OFFSET / 2).row();
			}else if(set.type.locked() && !state.rules.infiniteResources && state.isCampaign()){
				table(Styles.grayPanel, t2 -> {
					t2.margin(6f);
					t2.defaults().left().padRight(OFFSET);
					t2.table(Tex.clear, table2 -> table2.image(Icon.lock).size(LEN + OFFSET * 1.5f)).left().padLeft(OFFSET / 2f);
					
					t2.pane(table2 -> table2.add("[gray]Need to be researched.").left().row()).grow();
				}).growX().fillY().padBottom(OFFSET / 2).row();
			}else{
				table(Styles.grayPanel, t2 -> {
					t2.margin(6f);
					t2.defaults().left().padRight(OFFSET);
					t2.image(set.type.fullIcon).size(LEN + OFFSET).scaling(Scaling.fit).left().padLeft(OFFSET / 2f);
					
					t2.pane(table2 -> {
						table2.left().marginLeft(12f);
						table2.add("[lightgray]" + Core.bundle.get("editor.spawn") + ": [accent]" + set.type.localizedName + "[lightgray] | Tier: [accent]" + set.sortIndex[1]).left().row();
						table2.add("[lightgray]" + Core.bundle.get("stat.buildtime") + ": [accent]" + TableFunc.format(set.costTimeVar() / 60) + "[lightgray] " + Core.bundle.get("unit.seconds")).row();
					}).growX().height(LEN).center();
					
					t2.pack();
					
					t2.pane(items -> {
						items.right();
						for(ItemStack stack : set.baseRequirements()){
							items.add(new ItemImage(stack.item.fullIcon, stack.amount)).padRight(OFFSET / 2).left();
						}
					}).growX().height(LEN).center();
					
					t2.table(stat).fillX().height(LEN + OFFSET).right();
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
				t.image(stack.item.fullIcon).size(size).left();
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
						t.button(new TextureRegionDrawable(item.fullIcon), Styles.clearTogglei, LEN - OFFSET, () -> {
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
			super("v" + NewHorizon.MOD.meta.version + " Update Log:");
			addCloseListener();
			cont.pane(table -> {
				table.add("@fix").color(Pal.accent).left().row();
				table.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
				table.add(TableFunc.tabSpace + Core.bundle.get("update.fix")).row();
				
				table.add("@add").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
				table.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
				table.add(TableFunc.tabSpace + Core.bundle.get("update.add")).row();
				contentLog(table, contents);
				
				table.add("@remove").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
				table.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
				table.add(TableFunc.tabSpace + Core.bundle.get("update.remove")).row();
				
				table.add("@other").color(Pal.accent).padTop(OFFSET * 1.5f).left().row();
				table.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).row();
				table.add(TableFunc.tabSpace + Core.bundle.get("update.other")).row();
			}).grow().row();
			cont.image().color(Pal.accent).fillX().height(OFFSET / 4).pad(OFFSET / 3).bottom().row();
			cont.button("@back", Icon.left, Styles.cleart, this::hide).fillX().height(LEN).row();
		}
		
		public void contentLog(Table table, UnlockableContent[] contents){
			table.pane(t -> {
				int index = 0;
				for(UnlockableContent c : contents){
					if(index % 8 == 0)t.row();
					t.button(new TextureRegionDrawable(c.fullIcon), Styles.cleari, LEN, () -> {
						ContentInfoDialog dialog = new ContentInfoDialog();
						dialog.show(c);
					}).size(LEN);
					index++;
				}
			}).fillY().growX().row();
		}
	}
}
