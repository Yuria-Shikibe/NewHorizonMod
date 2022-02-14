package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.*;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.net.ServerGroup;
import mindustry.type.Item;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.modules.ItemModule;
import newhorizon.content.*;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.entities.NHGroups;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.EventListeners;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.CutsceneScript;
import newhorizon.util.feature.cutscene.EventSamples;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.func.NHSetting;
import newhorizon.util.ui.*;
import newhorizon.util.ui.NHUI.LinkTable;

import java.io.IOException;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;


public class NewHorizon extends Mod{
	public static final boolean DEBUGGING = false;
	public static final boolean DEBUGGING_SPRITE = false;
	
//	{
//		Vars.mobile = Vars.testMobile = true;
//	}
	
	public static boolean contentLoadComplete = false;
	
	public static final String MOD_RELEASES = "https://github.com/Yuria-Shikibe/NewHorizonMod/releases";
	public static final String MOD_REPO = "Yuria-Shikibe/NewHorizonMod";
	public static final String MOD_GITHUB_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod.git";
	public static final String MOD_NAME = "new-horizon";
	public static final String MOD_NAME_BAR = "new-horizon-";
	public static final String SERVER = "175.178.22.6:6666", SERVER_ADDRESS = "175.178.22.6", SERVER_AUZ_NAME = "NEWHORIZON AUZ SERVER";
	public static final String EU_NH_SERVER = "Emphasize.cn:12510";
	
	public static Mods.LoadedMod MOD;
	
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
	
	private static FeatureLog[] getUpdateContent(){
		return new FeatureLog[]{
			new FeatureLog(NHBlocks.beacon),
			new FeatureLog("[[[accent]Since 1.11.0[]]Territory Capture Game Mode", "Added a completely new game mode.\nEnable it in Editor -> Menu -> New Horizon Extra Settings -> Beacon Capture\n(WIP)", FeatureLog.NEW_FEATURE, NHContent.capture){{
				important = true;
			}},
			new FeatureLog("[[[accent]Since 1.11.0[]]New Map Settings", "Added some new mod map settings for custom maps.\n1. Jump Gates use items from core, default disabled, just like the old version. Added for some quick battle maps.\n2. Enemy's Jump Gates consumes items.\nSet them in Editor -> Menu -> New Horizon Extra Settings", FeatureLog.NEW_FEATURE, Icon.settings.getRegion())
//			new FeatureLog("Customizable Event Trigger", "Enter the menu from the <Editor Menu> -> <Cutscene Menu>, has ease UI.", FeatureLog.NEW_FEATURE + FeatureLog.IMPORTANT, NHContent.objective),
//			new FeatureLog("Sprite Adjust", "Slightly adjustments so some sprites.", FeatureLog.IMPROVE, Icon.wrench.getRegion()),
		};
	}
	
	private static void showAbout(){
		if(links == null)links = new Links.LinkEntry[]{
			new Links.LinkEntry("mod.ccs", "https://github.com/Yuria-Shikibe/NewHorizonMod/wiki/Cutscene-Script-Custom-Guide", Icon.settings, Pal.heal),
			new Links.LinkEntry("mod.discord", "https://discord.gg/yNmbMcuwyW", Icon.discord, Color.valueOf("7289da")),
			new Links.LinkEntry("mod.github", MOD_GITHUB_URL, Icon.github, Color.valueOf("24292e")),
			new Links.LinkEntry("mod.guide", "https://github.com/Yuria-Shikibe/NewHorizonMod#mod-guide", Icon.bookOpen, Pal.accent),
			new Links.LinkEntry("yuria.plugin", "https://github.com/Yuria-Shikibe/RangeImager", Icon.export, NHColor.thurmixRed)
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
					t.button("@links", Icon.link, Styles.transt, NewHorizon::showAbout).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@settings", Icon.settings, Styles.transt, () -> new NHSetting.SettingDialog().show()).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@log", Icon.book, Styles.transt, NewHorizon::showNew).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.transt, () -> Core.app.setClipboardText(SERVER)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
				}).grow();
				if(!Vars.mobile)table.table(t -> {
				
				}).grow();
			}).fill();
		}).grow();
		dialog.show();
	}
	
	public static void showNew(){
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
				main.table(t -> {
					t.add("@yuria.plugin.ad").fillY().growX().row();
					t.button("@openlink", Icon.export, () -> {
						Core.app.openURI("https://github.com/Yuria-Shikibe/RangeImager");
					}).pad(OFFSET / 2f).growX();
				}).growX().fillY().padBottom(OFFSET).row();
				main.image().growX().height(4).pad(6).color(Color.lightGray).row();
				main.pane(t -> {
					for(int index = 0; index < getUpdateContent().length; index++){
						FeatureLog c = getUpdateContent()[index];
						Table info = new Table(Tex.pane, table -> {
							if(c.important || c.content != null){
								table.background(Tex.whitePane);
								if(c.important)table.color.set(Pal.accent);
							}
							
							table.table(i -> {
								i.image(c.icon).fill();
							}).fill().get().pack();
							table.pane(i -> {
								i.top();
								i.add("[gray]NEW [lightgray]" + c.type.toUpperCase() + "[]: [accent]" + c.title + "[]").left().row();
								i.image().growX().height(OFFSET / 3).pad(OFFSET / 3).color(Color.lightGray).row();
								i.add("[accent]Description: []").left().row();
								i.add(c.description).padLeft(LEN).left().get().setWrap(true);
								if(c.modifier != null)c.modifier.get(i);
							}).grow().padLeft(OFFSET).top();
							table.button(Icon.info, Styles.clearTransi, LEN, () -> {
								ContentInfoDialog dialog = new ContentInfoDialog();
								dialog.show(c.content);
							}).growY().width(LEN).padLeft(OFFSET).disabled(b -> c.content == null);
						});
						if(!c.important)t.add(info).grow().row();
						else{
							Label label = new Label("IMPORTANT", Styles.techLabel);
							label.setFontScale(1.25f);
							
							
							t.stack(new Table(table -> table.margin(OFFSET * 2).add(label)).bottom(), new Table(Styles.black6){{setFillParent(true);}}, info).grow().row();
						}
					}
				}).growX().top().row();
			}).grow().row();
			
			cont.table(table -> {
				table.button("@back", Icon.left, Styles.transt, this::hide).growX().height(LEN);
				table.button("@settings", Icon.settings, Styles.transt, () -> new NHSetting.SettingDialog().show()).growX().height(LEN);
			}).bottom().growX().height(LEN).padTop(OFFSET);
		}}.show();
	}
	
	
	
	public NewHorizon(){
		Log.info("Loaded NewHorizon Mod constructor.");
		
		Events.on(ClientLoadEvent.class, e -> {
			NHUI.init();
			
			Core.app.post(NHSetting::updateSettingMenu);
			
			Vars.defaultServers.add(new ServerGroup(){{
				name = "[sky]New Horizon [white]Mod [lightgray]Servers";
				addresses = new String[]{SERVER, EU_NH_SERVER};
			}});
			
			Time.runTask(15f, () -> Core.app.post(() -> {
				Http.get(Vars.ghApi + "/repos/" + MOD_REPO + "/releases/latest", res -> {
					Jval json = Jval.read(res.getResultAsString());
					
					String tag = json.get("tag_name").asString();
					String body = json.get("body").asString();
					
					if(tag != null && body != null && !tag.equals(Core.settings.get(MOD_NAME + "-last-gh-release-tag", "0")) && !tag.equals('v' + MOD.meta.version)){
						new BaseDialog(Core.bundle.get("mod.ui.has-new-update") + ": " + tag){{
							cont.table(t -> {
								t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padTop(LEN).expandX().row();
								t.image(NHContent.icon2).center().pad(OFFSET).color(Pal.accent).row();
								t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padBottom(LEN).expandX().row();
								t.add("\t[lightgray]Version: [accent]" + tag).left().row();
								t.image().growX().height(OFFSET / 3).pad(OFFSET / 3).row();
								t.pane(c -> {
									c.align(Align.topLeft).margin(OFFSET);
									c.add("[accent]Description: \n[]" + body).left();
								}).grow();
							}).grow().padBottom(OFFSET).row();
							
							
							cont.table(table -> {
								table.button("@back", Icon.left, Styles.transt, this::hide).growX().height(LEN);
								table.button("@mods.github.open", Icon.github, Styles.transt, () -> Core.app.openURI(MOD_RELEASES)).growX().height(LEN);
							}).bottom().growX().height(LEN).padTop(OFFSET);
							
							addCloseListener();
						}}.show();
					}
					
					if(tag != null) Core.settings.put(MOD_NAME + "-last-gh-release-tag", tag);
				}, ex -> Log.err(ex.toString()));
			}));
			
			Time.runTask(10f, () -> {
				
				if(NHSetting.versionChange){
					showNew();
				}
				
				if(!NHSetting.getBool("@active.hid-start-log"))startLog();
			});
		});
	}

	@Override
	public void init() {
		Vars.netServer.admins.addChatFilter((player, text) -> text.replace("jvav", "java"));
		NHVars.init();
		
		if(Vars.headless)return;
		
		TableFunc.tableMain();
		
		NHSetting.applySettings();
		
		ScreenInterferencer.load();
		
		NHRegister.load();
		
		Vars.ui.hints.hints.addAll(Hints.DefaultHint.all);
	}
	
	@Override
	public void registerServerCommands(CommandHandler handler) {
		handler.register("events", "List all events in the map.", (args) -> {
			if (NHGroups.event.isEmpty()) {
				Log.info("No Event Available");
			}
			
			NHGroups.event.each(Log::info);
		});
		
		handler.register("eventtypes", "List all event types in the map.", (args) -> {
			if (CutsceneEvent.cutsceneEvents.isEmpty()) {
				Log.info("No Event Available");
			}
			
			CutsceneEvent.cutsceneEvents.each(Log::info);
		});
		
		handler.register("runevent", "<id>", "Trigger Event (Admin Only)", (args) -> {
			if (args.length == 0) {
				Log.warn("[VIOLET]Failed, pls type ID");
			} else {
				try {
					CutsceneEventEntity event = NHGroups.event.getByID(Integer.parseInt(args[0]));
					event.act();
					Log.info("Triggered: " + event);
				} catch (NumberFormatException var2) {
					Log.warn("[VIOLET]Failed, the ID must be a <Number>");
				}
			}
			
		});
		
		handler.register("runjs", "<Code>", "Run js codes", (args) -> {
			StringBuilder sb = new StringBuilder();
			for(String s : args){
				sb.append(s);
				sb.append(' ');
			}
			CutsceneScript.runJS(sb.toString());
		});
	}
	
	@Override
	public void registerClientCommands(CommandHandler handler) {
		handler.<Player>register("runwave", "<num>", "Run Wave (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0 || args[0].isEmpty()) {
				Vars.logic.runWave();
			} else {
				try {
					for(int i = 0; i < Integer.parseInt(args[0]); ++i) {
						Time.run(i * 90.0F, Vars.logic::runWave);
					}
				} catch (NumberFormatException var3) {
					player.sendMessage("[VIOLET]Failed, the param must be a <Number>");
				}
			}
		});
		
		handler.<Player>register("runevent", "<id>", "Trigger Event (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0) {
				player.sendMessage("[VIOLET]Failed, pls type ID");
			} else {
				try {
					CutsceneEventEntity event = NHGroups.event.getByID(Integer.parseInt(args[0]));
					event.act();
					player.sendMessage("Triggered: " + event);
				} catch (NumberFormatException var3) {
					player.sendMessage("[VIOLET]Failed, the ID must be a <Number>");
				}
			}
			
		});
		
		handler.<Player>register("fill", "<id>", "Trigger Event (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0 || args[0].isEmpty()) {
				ItemModule module = player.team().core().items;
				for(Item i : Vars.content.items())module.set(i, 1000000);
			} else {
				ItemModule module = Team.get(Integer.parseInt(args[0])).core().items;
				for(Item i : Vars.content.items())module.set(i, 1000000);
			}
		});
		
		handler.<Player>register("events", "List all cutscene events in the map.", (args, player) -> {
			if (NHGroups.event.isEmpty()) {
				player.sendMessage("No Event Available");
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("[accent]Events: [lightgray]\n");
				NHGroups.event.each((e) -> {
					builder.append(e).append('\n');
				});
				player.sendMessage(builder.toString());
			}
		});
		
		handler.<Player>register("eventtypes", "List all cutscene event types in the map.", (args, player) -> {
			if (CutsceneEvent.cutsceneEvents.isEmpty()) {
				player.sendMessage("No EventTypes Available");
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("[accent]Events: [lightgray]\n");
				CutsceneEvent.cutsceneEvents.each((k, e) -> {
					builder.append(k).append("->").append(e.getClass().getSuperclass().getSimpleName()).append('\n');
				});
				player.sendMessage(builder.toString());
			}
		});
		
		handler.<Player>register("eventtriggers", "List all event triggers in the map.", (args, player) -> {
			if (NHGroups.autoEventTrigger.isEmpty()) {
				player.sendMessage("No Trigger Available");
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("[accent]Events: [lightgray]\n");
				NHGroups.autoEventTrigger.each(e -> {
					builder.append("[royal]").append(e.toString()).append("[lightgray]").append('\n').append(e.desc()).append('\n').append("Meet Requirements?: ").append(e.meet() ? "[heal]Yes[]" : "[#ff7b69]No[]").append("[lightgray]\n");
					builder.append("Reload: ").append(e.getReload()).append('\n').append("Spacing: ").append(e.getSpacing()).append('\n');
				});
				player.sendMessage(builder.toString());
			}
		});
		
		handler.<Player>register("js", "<Code>", "Run js codes (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			}else{
				StringBuilder sb = new StringBuilder();
				for(String s : args){
					sb.append(s);
					sb.append(' ');
				}
				CutsceneScript.runJS(sb.toString());
			}
		});
		
		handler.<Player>register("setscale", "<Scale>", "Set Auto Event Trigger Time Scale (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else {
				if (args.length == 0){
					AutoEventTrigger.setScale(0.8f);
					player.sendMessage("Set to: [accent]0.8");
				}else{
					try {
						AutoEventTrigger.setScale(Float.parseFloat(args[0]));
						player.sendMessage("Set to: [accent]" + Float.parseFloat(args[0]));
					} catch (NumberFormatException var3) {
						player.sendMessage(var3.toString());
					}
				}
			}
		});
	}
	
	@Override
    public void loadContent(){
		Log.info("Debug Mode: " + DEBUGGING);
		Log.info("Process Texture Mode: " + NHPixmap.isDebugging());
		
		Time.mark();
		
		MOD = Vars.mods.getMod(getClass());
		
		if(!Vars.headless){
			try{
				NHSetting.settingFile();
				NHSetting.initSetting();
				NHSetting.initSettingList();
				NHSetting.loadSettings();
			}catch(IOException e){
				throw new IllegalArgumentException(e);
			}
		}
		
		EntityRegister.load();
		EventListeners.load();
		
		NHContent.loadModContent();
		
		if(!Vars.headless){
			NHSounds.load();
			NHShaders.init();
		}
		
		NHContent.loadAfterContent();
		
		for(ContentList contentList : content)contentList.load();
		if(Vars.headless || NHSetting.getBool("@active.override"))NHOverride.load();
		
		EventSamples.load();
		CutsceneScript.load();
		EventListeners.loadAfterContent();
		
		contentLoadComplete = true;
		
		Log.info(MOD.meta.displayName + " Loaded Complete: " + MOD.meta.version + " | Cost Time: " + (Time.elapsed() / Time.toSeconds) + " sec.");
    }
}
