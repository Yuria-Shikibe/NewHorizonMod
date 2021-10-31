package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.util.Align;
import arc.util.Http;
import arc.util.Log;
import arc.util.Time;
import arc.util.async.Threads;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.BorderImage;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import newhorizon.content.*;
import newhorizon.feature.cutscene.CutsceneScript;
import newhorizon.feature.ScreenHack;
import newhorizon.feature.cutscene.EventSamples;
import newhorizon.func.EntityRegister;
import newhorizon.func.NHSetting;
import newhorizon.ui.TableFunc;
import newhorizon.ui.Tables;
import newhorizon.ui.Tables.LinkTable;
import newhorizon.vars.EventTriggers;

import java.io.IOException;

import static newhorizon.ui.TableFunc.LEN;
import static newhorizon.ui.TableFunc.OFFSET;


public class NewHorizon extends Mod{
//	static{
//		Vars.testMobile = Vars.mobile = true;
//	}
	
	
	public static final String MOD_RELEASES = "https://github.com/Yuria-Shikibe/NewHorizonMod/releases";
	public static final String MOD_REPO = "Yuria-Shikibe/NewHorizonMod";
	public static final String MOD_GITHUB_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod.git";
	public static final String MOD_NAME = "new-horizon";
	public static final String SERVER_ADDRESS = "n2.yd.gameworldmc.cn:20074", SERVER_AUZ_NAME = "NEWHORIZON AUZ SERVER";
	public static Links.LinkEntry[] links;
	
	public static String name(String name){
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
		new NHSectorPresets(),
		new NHWeathers(),
		new NHTechTree(),
	};
	
	private static UnlockableContent[] getUpdateContent(){
		return new UnlockableContent[]{
				NHUnitTypes.sin
		};
	}
	
	private static void logShow(){
		new Tables.LogDialog(getUpdateContent()).show();
	}
	
	private static void links(){
		if(links == null)links = new Links.LinkEntry[]{
			new Links.LinkEntry("Custom Cutscene Guide", "https://github.com/Yuria-Shikibe/NewHorizonMod/wiki/Cutscene-Script-Custom-Guide", Icon.settings, Pal.heal),
			new Links.LinkEntry("mod.discord", "https://discord.gg/yNmbMcuwyW", Icon.discord, Color.valueOf("7289da")),
			new Links.LinkEntry("mod.github", MOD_GITHUB_URL, Icon.github, Color.valueOf("24292e")),
			new Links.LinkEntry("mod.guide", "https://github.com/Yuria-Shikibe/NewHorizonMod#mod-guide", Icon.bookOpen, Pal.accent)
		};
		
		BaseDialog dialog = new BaseDialog("@links");
		dialog.cont.pane(table -> {
			LinkTable.sync();
			for(Links.LinkEntry entry : links){
				TableFunc.link(table, entry);
			}
		}).grow().row();
		dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 4, LEN);
		dialog.addCloseListener();
		dialog.show();
	}

	public static void startLog(){
		BaseDialog dialog = new BaseDialog("");
		dialog.closeOnBack();
		dialog.cont.pane(inner -> {
			inner.pane(table -> {
				table.table(t -> t.image(NHContent.icon2).fill()).center().growX().fillY().row();
				table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.pane(p -> {
					p.add("[white]<< Powered by New Horizon Mod >>", Styles.techLabel).row();
				}).fillY().growX().row();
				table.image().fillX().height(OFFSET / 2.75f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("").row();
			}).growX().center().row();
			
			inner.table(table -> {
				if(!Vars.mobile)table.table(t -> {
				
				}).grow();
				table.table(t -> {
					t.button("@back", Icon.left, Styles.transt, () -> {
						dialog.hide();
						NHSetting.applySettings();
					}).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@links", Icon.link, Styles.transt, NewHorizon::links).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@settings", Icon.settings, Styles.transt, () -> new NHSetting.SettingDialog().show()).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@log", Icon.book, Styles.transt, NewHorizon::logShow).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
//					t.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.transt, () -> Core.app.setClipboardText(SERVER_ADDRESS)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
				}).grow();
				if(!Vars.mobile)table.table(t -> {
				
				}).grow();
			}).fill();
		}).grow();
		dialog.show();
	}
	
    public NewHorizon(){
		Log.info("Loaded NewHorizon Mod constructor.");
		
        Events.on(ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
	        Threads.thread(() -> {
		        Http.get(Vars.ghApi + "/repos/" + MOD_REPO + "/releases/latest", res -> {
			        Jval json = Jval.read(res.getResultAsString());
			        
			        //Log.info(json);
			        
			        String tag = json.get("tag_name").asString();
			        String body = json.get("body").asString();
			        if(!tag.equals(Core.settings.get(MOD_NAME + "-last-gh-release-tag", "0"))){
			        	new BaseDialog(Core.bundle.get("mod.ui.has-new-update") + ": " + tag){{
			        		cont.table(t -> {
			        			t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padTop(LEN).expandX().row();
			        			t.image(NHContent.icon2).center().pad(OFFSET).color(Pal.accent).row();
						        t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padBottom(LEN).expandX().row();
						        t.add("\t[lightgray]Version: [accent]" + tag).left().row();
						        t.image().growX().height(OFFSET / 3).pad(OFFSET / 3).row();
						        t.pane(c -> {
							        c.add("[accent]Description: \n[]" + body).left();
						        }).grow();
					        }).grow().padBottom(OFFSET).row();
					
					        cont.table(table -> {
						        table.button("@back", Icon.left, Styles.cleart, this::hide).growX().height(LEN);
						        table.button("@mods.github.open", Icon.github, Styles.cleart, () -> Core.app.openURI(MOD_RELEASES)).growX().height(LEN);
					        }).bottom().growX().height(LEN).padTop(OFFSET);
					        
					        addCloseListener();
				        }}.show();
			        }
			        Core.settings.put(MOD_NAME + "-last-gh-release-tag", tag);
		        }, ex -> Log.err(ex.toString()));
	        });
        	
        	if(NHSetting.versionChange){
        		new BaseDialog("Detected Update"){{
        			addCloseListener();
        			
        			cont.pane(main -> {
        				main.top();
				        main.pane(table -> {
				        	table.align(Align.topLeft);
					        table.add(NHSetting.modMeta.version + ": ").row();
					        table.image().height(OFFSET / 3).growX().color(Pal.accent).row();
					        table.add(Core.bundle.get("mod.ui.update-log")).left();
				        }).growX().fillY().padBottom(LEN).row();
				        main.pane(t -> {
					        for(int index = 0; index < getUpdateContent().length; index++){
					        	UnlockableContent c = getUpdateContent()[index];
					        	t.table(Tex.pane, table -> {
					        		table.add(new BorderImage(c.fullIcon, OFFSET / 2).border(Pal.accent)).fill();
					        		table.pane(i -> {
								        i.top();
					        			i.add("[gray]NEW [lightgray]" + c.getContentType().toString().toUpperCase() + "[]: [accent]" + c.localizedName + "[]").left().row();
					        			i.image().growX().height(OFFSET / 3).pad(OFFSET / 3).color(Color.lightGray).row();
					        			i.add("[accent]Description: []").left().row();
					        			i.add(c.description).padLeft(LEN).left().get().setWrap(true);
							        }).grow().padLeft(OFFSET).top();
							        table.button(Icon.info, Styles.clearTransi, LEN, () -> {
								        ContentInfoDialog dialog = new ContentInfoDialog();
								        dialog.show(c);
							        }).growY().width(LEN).padLeft(OFFSET);
						        }).grow().row();
					        }
				        }).growX().top().row();
			        }).grow().row();
        			
        			cont.table(table -> {
				        table.button("@back", Icon.left, Styles.cleart, this::hide).growX().height(LEN);
				        table.button("@settings", Icon.settings, Styles.cleart, () -> new NHSetting.SettingDialog().show()).growX().height(LEN);
				        table.button("@log", Icon.add, Styles.cleart, NewHorizon::logShow).growX().height(LEN);
			        }).bottom().growX().height(LEN).padTop(OFFSET);
		        }}.show();
	        }
        	
        	if(!NHSetting.getBool("@active.hid-start-log"))startLog();
	        TableFunc.tableMain();
	        NHSetting.updateSettingMenu();
	        NHSetting.applySettings();
	        ScreenHack.load();
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
				NHSetting.loadSettings();
			}catch(IOException e){
				throw new IllegalArgumentException(e);
			}
		}else NHSetting.modMeta = Vars.mods.getMod(getClass()).meta;
		
		EventTriggers.load();
	    NHSounds.load();
		NHContent.initLoad();
//		NHShaders.init();
		
		for(ContentList contentList : content){
			contentList.load();
		}
		
		EntityRegister.load();
		if(!EntityRegister.safe)Log.info("Detected id map conflict");
		
		if(Vars.headless || NHSetting.getBool("@active.override"))NHOverride.load();
		
		CutsceneScript.load();
		EventSamples.load();
		
		Log.info("Loaded Complete." + NHSetting.modMeta.version);
    }
}
