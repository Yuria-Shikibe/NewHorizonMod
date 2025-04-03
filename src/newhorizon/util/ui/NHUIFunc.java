package newhorizon.util.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.Stats;
import newhorizon.NHUI;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.util.annotation.HeadlessDisabled;
import newhorizon.util.func.NHInterp;
import newhorizon.util.ui.display.ItemImage;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static newhorizon.NHVars.cutsceneUI;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class NHUIFunc{
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
			
			cutsceneUI.overlay.addChild(table);
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
}
