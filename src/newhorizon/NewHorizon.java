package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import newhorizon.content.*;
import newhorizon.func.*;
import newhorizon.func.Tables.LinkTable;
import newhorizon.vars.EventTriggers;

import java.io.IOException;

import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;


public class NewHorizon extends Mod{
	public static final String MOD_NAME = "new-horizon";
	public static final String SERVER_ADDRESS = "n2.yd.gameworldmc.cn:20074", SERVER_AUZ_NAME = "NEWHORIZON AUZ SERVER";
	public static Links.LinkEntry[] links;
	
	static{
		Vars.testMobile = true;
	}
	
	public static String contentName(String name){
		return MOD_NAME + "-" + name;
	}
	
	private static final ContentList[] content = {
		new NHStatusEffects(),
		new NHItems(),
	    new NHLiquids(),
	    new NHBullets(),
		new NHUpgradeDatas(),
		new NHUnitTypes(),
		new NHBlocks(),
		new NHPlanets(),
		new NHSectorPreset(),
		new NHWeathers(),
		new NHTechTree(),
	};
	
	private static UnlockableContent[] getUpdateContent(){
		return new UnlockableContent[]{
			NHBlocks.sandCracker,
			NHSectorPreset.downpour,
			NHSectorPreset.deltaOutpost,
			NHSectorPreset.hostileHQ,
			NHSectorPreset.luminariOutpost,
			NHSectorPreset.quantumCraters
		};
	}
	
	private static void logShow(){
		new Tables.LogDialog(getUpdateContent()).show();
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
				table.table(t -> t.image(Core.atlas.find(contentName("upgrade")))).center().growX().fillY().row();
				table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("[white]<< Powered by NewHorizonMod >>", Styles.techLabel).row();
				table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("").row();
			}).grow().center().row();
			
			inner.table(table -> {
				table.button("@back", Icon.left, Styles.cleart, () -> {
					dialog.hide();
					NHSetting.settingApply();
				}).size(LEN * 2f, LEN);
				table.button("@links", Icon.link, Styles.cleart, NewHorizon::links).size(LEN * 2f, LEN).padLeft(OFFSET / 2);
				table.button("@settings", Icon.settings, Styles.cleart, () -> new SettingDialog().show()).size(LEN * 2f, LEN).padLeft(OFFSET / 2);
				table.button("@log", Icon.book, Styles.cleart, NewHorizon::logShow).size(LEN * 2f, LEN).padLeft(OFFSET / 2);
				table.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.cleart, () -> Core.app.setClipboardText(SERVER_ADDRESS)).size(LEN * 4f, LEN).padLeft(OFFSET / 2);
			}).fillX().height(LEN + OFFSET);
		}).grow();
		dialog.show();
	}
	
    public NewHorizon(){
		Log.info("Loaded NewHorizon Mod constructor.");
        
        Events.on(ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
        	if(NHSetting.versionChange){
        		new BaseDialog("Detected Update"){{
        			addCloseListener();
        			
        			cont.pane(table -> {
        				table.add(NHSetting.modMeta.version + ": ").row();
        				table.image().height(OFFSET / 3).growX().color(Pal.accent).row();
        				table.add(Core.bundle.get("mod.ui.update-log"));
			        }).grow().row();
			        cont.pane(t -> {
				        int index = 0;
				        for(UnlockableContent c : getUpdateContent()){
					        if(index % 8 == 0)t.row();
					        t.button(new TextureRegionDrawable(c.fullIcon), Styles.clearTransi, LEN, () -> {
						        ContentInfoDialog dialog = new ContentInfoDialog();
						        dialog.show(c);
					        }).size(LEN);
					        index++;
				        }
			        }).growX().top().row();
        			cont.table(table -> {
				        table.button("@back", Icon.left, Styles.cleart, this::hide).growX().height(LEN);
				        table.button("@settings", Icon.settings, Styles.cleart, () -> new SettingDialog().show()).growX().height(LEN);
				        table.button("@log", Icon.add, Styles.cleart, NewHorizon::logShow).growX().height(LEN);
			        }).bottom().growX().height(LEN).padTop(OFFSET);
		        }}.show();
	        }
        	
        	if(!NHSetting.getBool("@active.hid-start-log"))startLog();
	        if(NHSetting.getBool("@active.tool-panel*"))TableFs.tableMain();
	        NHSetting.updateSettingMenu();
	        NHSetting.loadSettings();
        }));
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
		
		if(NHSetting.getBool("@active.debug-mobile*"))Vars.testMobile = true;
		
		NHReflections.load();
		EventTriggers.load();
	    NHSounds.load();
		NHContent.initLoad();
		//NHShaders.init();
		
		for(ContentList contentList : content){
			contentList.load();
		}
		
		ClassIDIniter.load();
		if(!ClassIDIniter.safe)Log.info("Detected id map conflict");
		
		if(Vars.headless || NHSetting.getBool("@active.override"))NHOverride.load();
		
		Log.info("Loaded Complete." + NHSetting.modMeta.version);
    }
}