package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.util.*;
import arc.util.async.Threads;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import newhorizon.content.*;
import newhorizon.expand.vars.EventListeners;
import newhorizon.util.feature.ScreenInterferencer;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.CutsceneScript;
import newhorizon.util.feature.cutscene.EventSamples;
import newhorizon.util.func.EntityRegister;
import newhorizon.util.func.NHSetting;
import newhorizon.util.ui.Hints;
import newhorizon.util.ui.LatestFeature;
import newhorizon.util.ui.TableFunc;
import newhorizon.util.ui.Tables.LinkTable;

import java.io.IOException;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;


public class NewHorizon extends Mod{
	static{
//		Vars.experimental = true;
//		Vars.testMobile = Vars.mobile = true;
	}
//
	
	public static final String MOD_RELEASES = "https://github.com/Yuria-Shikibe/NewHorizonMod/releases";
	public static final String MOD_REPO = "Yuria-Shikibe/NewHorizonMod";
	public static final String MOD_GITHUB_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod.git";
	public static final String MOD_NAME = "new-horizon";
	public static final String MOD_NAME_BAR = "new-horizon-";
	public static final String SERVER_ADDRESS = "175.178.22.6:6666", SERVER_AUZ_NAME = "NEWHORIZON AUZ SERVER";
	
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
	
	private static LatestFeature[] getUpdateContent(){
		return new LatestFeature[]{
			new LatestFeature(NHUnitTypes.anvil),
			new LatestFeature(NHStatusEffects.weak),
			new LatestFeature("Fixes", "Fixed some bugs about cutscene", "Improvement", Icon.wrench.getRegion()),
		};
	}
	
	private static void links(){
		if(links == null)links = new Links.LinkEntry[]{
			new Links.LinkEntry("mod.ccs", "https://github.com/Yuria-Shikibe/NewHorizonMod/wiki/Cutscene-Script-Custom-Guide", Icon.settings, Pal.heal),
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
					t.button("@log", Icon.book, Styles.transt, NewHorizon::showNew).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.transt, () -> Core.app.setClipboardText(SERVER_ADDRESS)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
				}).grow();
				if(!Vars.mobile)table.table(t -> {
				
				}).grow();
			}).fill();
		}).grow();
		dialog.show();
	}
	
    public NewHorizon(){
		Log.info("Loaded NewHorizon Mod constructor.");
		
        Events.on(ClientLoadEvent.class, e -> {
	        Threads.thread(() -> {
		        Http.get(Vars.ghApi + "/repos/" + MOD_REPO + "/releases/latest", res -> {
			        Jval json = Jval.read(res.getResultAsString());
			
			        String tag = json.get("tag_name").asString();
			        String body = json.get("body").asString();
			
			        if(tag != null && body != null && !tag.equals(Core.settings.get(MOD_NAME + "-last-gh-release-tag", "0"))){
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
			
			        if(tag != null)Core.settings.put(MOD_NAME + "-last-gh-release-tag", tag);
		        }, ex -> Log.err(ex.toString()));
	        });
	        
        	Time.runTask(10f, () -> {
		
		        if(NHSetting.versionChange){
			        showNew();
		        }
		
		        if(!NHSetting.getBool("@active.hid-start-log"))startLog();
	        });
        });
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
				main.pane(t -> {
					for(int index = 0; index < getUpdateContent().length; index++){
						LatestFeature c = getUpdateContent()[index];
						t.table(Tex.pane, table -> {
							table.table(i -> {
								i.image(c.icon).fill();
							}).fill().get().pack();
							table.pane(i -> {
								i.top();
								i.add("[gray]NEW [lightgray]" + c.type.toUpperCase() + "[]: [accent]" + c.title + "[]").left().row();
								i.image().growX().height(OFFSET / 3).pad(OFFSET / 3).color(Color.lightGray).row();
								i.add("[accent]Description: []").left().row();
								i.add(c.description).padLeft(LEN).left().get().setWrap(true);
							}).grow().padLeft(OFFSET).top();
							table.button(Icon.info, Styles.clearTransi, LEN, () -> {
								ContentInfoDialog dialog = new ContentInfoDialog();
								dialog.show(c.content);
							}).growY().width(LEN).padLeft(OFFSET).disabled(b -> c.content == null);
						}).grow().row();
					}
				}).growX().top().row();
			}).grow().row();
			
			cont.table(table -> {
				table.button("@back", Icon.left, Styles.transt, this::hide).growX().height(LEN);
				table.button("@settings", Icon.settings, Styles.transt, () -> new NHSetting.SettingDialog().show()).growX().height(LEN);
			}).bottom().growX().height(LEN).padTop(OFFSET);
		}}.show();
	}
	
	public void init() {
		Vars.netServer.admins.addChatFilter((player, text) -> text.replace("jvav", "java"));
		
		
		if(Vars.headless)return;
		
		TableFunc.tableMain();
		
		NHSetting.updateSettingMenu();
		NHSetting.applySettings();
		
		ScreenInterferencer.load();
		
		NHRegister.load();
		
		Vars.ui.hints.hints.addAll(Hints.DefaultHint.all);
	}
	
	public void registerServerCommands(CommandHandler handler) {
		handler.register("events", "List all events in the map.", (args) -> {
			if (CutsceneEventEntity.events.isEmpty()) {
				Log.info("No Event Available");
			}
			
			CutsceneEventEntity.events.each(Log::info);
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
					CutsceneEventEntity event = CutsceneEventEntity.events.getByID(Integer.parseInt(args[0]));
					event.act();
					Log.info("Triggered: " + event);
				} catch (NumberFormatException var2) {
					Log.warn("[VIOLET]Failed, the ID must be a <Number>");
				}
			}
			
		});
		
		handler.register("runjs", "<Code>", "Run js codes", (args) -> {
			if (args.length == 0) {
				Log.warn("[E]Failed, pls type Code");
			}else{
				try {
					CutsceneScript.runJS(args[0]);
				} catch (Throwable var3) {
					Log.err(var3.toString());
				}
			}
		});
	}
	
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
					CutsceneEventEntity event = CutsceneEventEntity.events.getByID(Integer.parseInt(args[0]));
					event.act();
					player.sendMessage("Triggered: " + event);
				} catch (NumberFormatException var3) {
					player.sendMessage("[VIOLET]Failed, the ID must be a <Number>");
				}
			}
			
		});
		
		handler.<Player>register("events", "List all cutscene events in the map.", (args, player) -> {
			if (CutsceneEventEntity.events.isEmpty()) {
				player.sendMessage("No Event Available");
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("[accent]Events: [lightgray]\n");
				CutsceneEventEntity.events.each((e) -> {
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
					builder.append(k).append("->").append(e).append('\n');
				});
				player.sendMessage(builder.toString());
			}
		});
		
		handler.<Player>register("runjs", "<Code>", "Run js codes (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else {
				if (args.length == 0) {
					player.sendMessage("[VIOLET]Failed, pls type Code");
				}else{
					try {
						CutsceneScript.runJS(args[0]);
					} catch (Throwable var3) {
						player.sendMessage(var3.toString());
					}
				}
			}
		});
	}
	
	@Override
    public void loadContent(){
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
	    NHSounds.load();
		if(!Vars.headless)NHShaders.init();
		NHContent.loadModContent();
		
		for(ContentList contentList : content)contentList.load();
		
		if(Vars.headless || NHSetting.getBool("@active.override"))NHOverride.load();
		
		EventSamples.load();
		CutsceneScript.load();
		
		Log.info(MOD.meta.displayName + " Loaded Complete: " + MOD.meta.version + " | Cost Time: " + (Time.elapsed() / Time.toSeconds) + " sec.");
    }
}
