package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.*;
import newhorizon.func.NHSetting;
import newhorizon.func.SettingDialog;
import newhorizon.func.TableFuncs;

import java.io.IOException;

import static newhorizon.func.TableFuncs.*;


public class NewHorizon extends Mod{
	public static String MOD_NAME;
	
	public static String configName(String name){
		return MOD_NAME + name;
	}
	
	private final ContentList[] content = {
		new NHItems(),
	    new NHLiquids(),
	    new NHBullets(),
		new NHUpgradeDatas(),
		new NHUnits(),
		new NHBlocks(),
		//new NHPlanets(),
	    new NHTechTree(),
	};
	
	private static void links(){
		BaseDialog dialog = new BaseDialog("@links");
		addLink(dialog.cont, Icon.github, "Github", "https://github.com/Yuria-Shikibe/NewHorizonMod.git");
		dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN).padLeft(OFFSET / 2);
		dialog.addCloseListener();
		dialog.show();
	}
	
	private static void addLink(Table table, TextureRegionDrawable icon, String buttonName, String link){
		table.button(buttonName, icon, Styles.cleart, () -> {
			BaseDialog dialog = new BaseDialog("@link");
			dialog.addCloseListener();
			dialog.cont.pane(t -> t.add("[gray]" + Core.bundle.get("confirm.link") + ": [accent]" + link + " [gray]?")).fillX().height(LEN / 2f).row();
			dialog.cont.image().fillX().pad(8).height(4f).color(Pal.accent).row();
			dialog.cont.pane(t -> {
				t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
				t.button("@confirm", Icon.link, Styles.cleart, () -> Core.app.openURI(link)).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).fillX();
			dialog.show();
		}).size(LEN * 3, LEN).left().row();
	}
	
	private static void logShow(){
		new BaseDialog("@log"){{
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
		}}.show();
	}
	
	public static void startLog(){
		Dialog dialog = new BaseDialog("", Styles.fullDialog);
		dialog.closeOnBack();
		dialog.cont.pane(inner -> {
			inner.pane(table -> {
				table.table(t -> t.image(Core.atlas.find(MOD_NAME + "upgrade"))).center().growX().fillY().row();
				table.image().growX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("[white]<< Powered by NewHorizonMod >>", Styles.techLabel).row();
				table.image().growX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("").row();
			}).grow().center().row();
			
			inner.pane(t -> {
				t.add("[gray]You can get back to here by [accent]<ModDialog>[gray] -> [accent]NewHorizonMod[gray] -> [accent]<View Content>[gray] -> ");
				t.add(NHLoader.content.localizedName).color(Pal.lancerLaser).row();
			}).fillX().height(LEN).bottom().row();
			
			inner.table(Tex.clear, table -> {
				table.button("@back", Icon.left, Styles.cleart, () -> {
					dialog.hide();
					NHSetting.settingApply();
				}).size(LEN * 2f, LEN);
				table.button("@links", Icon.link, Styles.cleart, NewHorizon::links).size(LEN * 2f, LEN).padLeft(OFFSET / 2);
				table.button("@settings", Icon.settings, Styles.cleart, () -> new SettingDialog().show()).size(LEN * 2f, LEN).padLeft(OFFSET / 2);
				table.button("@log", Icon.book, Styles.cleart, NewHorizon::logShow).size(LEN * 2f, LEN).padLeft(OFFSET / 2);
			}).fillX().height(LEN + OFFSET);
		}).grow();
		dialog.show();
	}
	
    public NewHorizon(){
        Log.info("Loaded NewHorizon Mod constructor.");
        Events.on(ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
        	startLog();
	        tableMain();
        }));
    }
	
	@Override
	public void init(){
	
	}
	
	@Override
    public void loadContent(){
		try{
			NHSetting.settingFile();
			NHSetting.initSetting();
			NHSetting.initSettingList();
		}catch(IOException e){
			throw new IllegalArgumentException(e);
		}
		MOD_NAME = NHSetting.modMeta.name + "-";
	    Log.info("Loading NewHorizon Mod Objects");
	    NHSounds.load();
		NHLoader loader = new NHLoader();
		loader.load();
		for(ContentList c : content){
			c.load();
		}
	    loader.loadLast();
    }
	
	
}
