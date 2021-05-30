package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.ui.Dialog;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.*;
import newhorizon.func.NHSetting;
import newhorizon.func.SettingDialog;
import newhorizon.func.TableFs;
import newhorizon.func.Tables;
import newhorizon.func.Tables.LinkTable;
import newhorizon.vars.EventTriggers;

import java.io.IOException;

import static newhorizon.func.TableFs.*;


public class NewHorizon extends Mod{
	public static final String MOD_NAME = "new-horizon-";
	public static Links.LinkEntry[] links;
	
	public static String configName(String name){
		return MOD_NAME + name;
	}
	
	private static final ContentList[] content = {
		new NHStatusEffects(),
		new NHItems(),
	    new NHLiquids(),
	    new NHBullets(),
		new NHUpgradeDatas(),
		new NHUnits(),
		new NHBlocks(),
		//new NHPlanets(),
	    new NHTechTree(),
		//new NHWeathers()
	};
	
	private static void logShow(){
		new Tables.LogDialog(new UnlockableContent[]{
			NHUnits.collapser
		}).show();
	}
	
	private static void links(){
		if(links == null)links = new Links.LinkEntry[]{
			new Links.LinkEntry("mod.discord", "https://discord.gg/yNmbMcuwyW", Icon.discord, Color.valueOf("7289da")),
			new Links.LinkEntry("mod.github", "https://github.com/Yuria-Shikibe/NewHorizonMod.git", Icon.github, Color.valueOf("24292e")),
			new Links.LinkEntry("mod.guide", "https://github.com/Yuria-Shikibe/NewHorizonMod#mod-guide", Icon.bookOpen, Pal.accent)
		};
		
		BaseDialog dialog = new BaseDialog("@links");
		dialog.cont.pane(table -> {
			LinkTable.sync();
			for(Links.LinkEntry entry : links){
				TableFs.link(table, entry);
			}
		}).grow().row();
		dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 4, LEN);
		dialog.addCloseListener();
		dialog.show();
	}

	public static void startLog(){
		Dialog dialog = new BaseDialog("", Styles.fullDialog);
		dialog.closeOnBack();
		dialog.cont.pane(inner -> {
			inner.pane(table -> {
				table.table(t -> t.image(Core.atlas.find(MOD_NAME + "upgrade"))).center().growX().fillY().row();
				table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("[white]<< Powered by NewHorizonMod >>", Styles.techLabel).row();
				table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
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
        	if(!NHSetting.getBool("@active.hid-start-log"))startLog();
	        if(NHSetting.getBool("@active.tool-panel*"))tableMain();
	        NHSetting.updateSettingMenu();
        }));
        
        //Vars.defaultServers.add();
    }
	
	@Override
	public void init(){
	
	}
	
	@Override
    public void loadContent(){
		if(!Vars.headless){
			try{
				NHSetting.settingFile();
				NHSetting.initSetting();
				NHSetting.initSettingList();
			}catch(IOException e){
				throw new IllegalArgumentException(e);
			}
		}
		
	    NHSounds.load();
		NHLoader loader = new NHLoader();
		loader.load();
		for(ContentList c : content){
			c.load();
		}
	    loader.loadLast();
		
		EventTriggers.load();
		
		Log.info("Loaded Complete.");
    }
}