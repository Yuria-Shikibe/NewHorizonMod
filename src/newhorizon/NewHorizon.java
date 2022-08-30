package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.*;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.net.ServerGroup;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.*;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.ui.FeatureLog;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;


public class NewHorizon extends Mod{
	public static final boolean DEBUGGING = true;
	public static final boolean DEBUGGING_SPRITE = false;
	
	public static void debugLog(Object obj){
		if(DEBUGGING)Log.info(obj);
	}
	
	{
		Vars.mobile = Vars.testMobile = true;
	}
	
	public static boolean contentLoadComplete = false;
	
	public static final String MOD_RELEASES = "https://github.com/Yuria-Shikibe/NewHorizonMod/releases";
	public static final String MOD_REPO = "Yuria-Shikibe/NewHorizonMod";
	public static final String MOD_GITHUB_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod.git";
	public static final String MOD_NAME = "new-horizon";
	public static final String MOD_NAME_BAR = "new-horizon-";
	public static final String SERVER = "175.178.22.6:6666", SERVER_ADDRESS = "175.178.22.6", SERVER_AUZ_NAME = "NEWHORIZON AUZ SERVER";
	public static final String EU_NH_SERVER = "Emphasize.cn:12510";
	
	private static boolean showed = false;
	public static Mods.LoadedMod MOD;
	
	public static Links.LinkEntry[] links;
	
	public static String name(String name){
		return MOD_NAME + "-" + name;
	}
	
	private static FeatureLog[] getUpdateContent(){
		return new FeatureLog[]{
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
//
//		BaseDialog dialog = new BaseDialog("@links");
//		dialog.cont.pane(table -> {
//			for(Links.LinkEntry entry : links){
//				TableFunc.link(table, entry);
//			}
//		}).grow().row();
//		dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 4, LEN);
//		dialog.addCloseListener();
//		dialog.show();
	}
	
	public static void startLog(){
		if(showed)return;
		showed = true;
		
		BaseDialog dialog = new BaseDialog("");
		
		Runnable runnable = () -> {
			Core.app.post(() -> Core.app.post(() -> Core.settings.getBoolOnce("nh-first-load", () -> {
				new BaseDialog("CAUTION"){
					private float countdown = 480f;
					private boolean exitable = false;
					
					{
						update(() -> {
							countdown -= Time.delta;
							if(countdown < 0 && !exitable){
								exitable = true;
								addCloseListener();
							}
						});
						
						cont.pane(t -> {
							t.left();
							t.table().margin(LEN).row();
							t.defaults().align(Align.left).padLeft(OFFSET).row();
							t.add("").update(b -> {
								b.setText("[gray]This Dialog Can Be Closed In [accent]" + Mathf.ceil(Math.max(countdown, 0) / 60) + " [lightgray]sec." + "\n" + Core.bundle.format("startwarn.1", Mathf.ceil(Math.max(countdown, 0) / 60)));
								if(countdown < 0)b.remove();
							}).row();
							t.image().growX().height(OFFSET / 3f).pad(OFFSET).color(Pal.turretHeat).row();
							t.add("[gray]This Dialog Only Shows [lightgray]Once[] After Installation.").row();
							t.add(Core.bundle.get("startwarn.2")).row();
							t.image().growX().height(OFFSET / 3f).pad(OFFSET).color(Color.lightGray).row();
							t.add("[accent]Tips:").row();
							t.add("If the mod support your language (En, in_ID, ko, uk_UA, zh_CH) and the language of the mod doesn't fit yours, please set the language to another one, reload, set it to yours, reload again, and this should work.").padLeft(LEN).row();
							t.add(Core.bundle.get("startwarn.3")).row();
							t.add(Core.bundle.get("startwarn.4")).padLeft(LEN).row();
							t.image().growX().height(OFFSET / 3f).pad(OFFSET).color(Pal.heal).row();
							t.add("[lightgray]The warns below are special messages to Chinese players, so if you aren't Chinese, skip it.").row();
							t.add(Core.bundle.get("startwarn.5")).row();
							t.add(Core.bundle.get("startwarn.6")).padLeft(LEN).row();
						}).grow().pad(LEN * 1.5f).row();
						cont.button("", Styles.cleart, this::hide).update(b -> {
							b.setDisabled(countdown > 0);
							b.setText(countdown > 0 ? "[accent]" + Mathf.ceil(Math.max(countdown, 0) / 60) + " []sec." : Core.bundle.get("confirm"));
						}).growX().height(LEN).pad(OFFSET);
					}
				}.show();
			})));
		};
		
		dialog.closeOnBack(runnable);
		
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
					t.button("@back", Icon.left, Styles.cleart, () -> {
						dialog.hide();
						runnable.run();
					}).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
//					t.button("@links", Icon.link, Styles.cleart, NewHorizon::showAbout).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
//					t.button("@settings", Icon.settings, Styles.cleart, () -> new NHSetting.SettingDialog().show()).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
//					t.button("@log", Icon.book, Styles.cleart, NewHorizon::showNew).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.cleart, () -> Core.app.setClipboardText(SERVER)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
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
			Vars.defaultServers.add(new ServerGroup(){{
				name = "[sky]New Horizon [white]Mod [lightgray]Servers";
				addresses = new String[]{SERVER, EU_NH_SERVER};
			}});
			
			Time.runTask(15f, () -> Threads.daemon(() -> {
				Http.get(Vars.ghApi + "/repos/" + MOD_REPO + "/releases/latest", res -> {
					Jval json = Jval.read(res.getResultAsString());
					
					String tag = json.get("tag_name").asString();
					String body = json.get("body").asString();
					
					if(tag != null && body != null && !tag.equals(Core.settings.get(MOD_NAME + "-last-gh-release-tag", "0")) && !tag.equals('v' + MOD.meta.version)){
						Core.app.post(() -> {
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
									table.button("@back", Icon.left, Styles.cleart, this::hide).growX().height(LEN);
									table.button("@mods.github.open", Icon.github, Styles.cleart, () -> Core.app.openURI(MOD_RELEASES)).growX().height(LEN);
								}).bottom().growX().height(LEN).padTop(OFFSET);
								
								addCloseListener();
							}}.show();
						});
					}
					
					if(tag != null) Core.settings.put(MOD_NAME + "-last-gh-release-tag", tag);
				}, ex -> Log.err(ex.toString()));
			}));
			
			Core.app.post(Time.runTask(10f, NewHorizon::startLog));
		});
	}

	@Override
	public void init() {
		Vars.netServer.admins.addChatFilter((player, text) -> text.replace("jvav", "java"));
		Core.app.addListener(new NHModCore());
		
		if(Vars.headless)return;
		
		Vars.renderer.maxZoom = 10f;
		Vars.renderer.minZoom = 1f;
		TableFunc.tableMain();
	}
	
/*	@Override
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
	}*/
	
/*	@Override
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
				
				player.sendMessage("[accent]Events: [lightgray]\n");
				NHGroups.autoEventTrigger.each(e -> {
					String builder = "[royal]" + e.toString() + "[lightgray]" + '\n' + e.desc() + '\n' + "Meet Requirements?: " + (e.meet() ? "[heal]Yes[]" : "[#ff7b69]No[]") + "[lightgray]\n" + "Reload: " + e.getReload() + '\n' + "Spacing: " + e.getSpacing() + '\n';
					player.sendMessage(builder);
				});
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
	}*/
	
	@Override
    public void loadContent(){
		Log.info("Debug Mode: " + DEBUGGING);
		Log.info("Process Texture Mode: " + NHPixmap.isDebugging());
		
		Time.mark();
		
		MOD = Vars.mods.getMod(getClass());
		
		EntityRegister.load();
		NHEventListenerRegister.load();
		
		NHContent.loadModContent();
		
		if(!Vars.headless){
			NHSounds.load();
			NHShaders.init();
		}
		
		NHContent.loadAfterContent();
		
		{
			NHStatusEffects.load();
			NHItems.load();
			NHLiquids.load();
			NHBullets.load();
			NHUnitTypes.load();
			NHBlocks.load();
			NHWeathers.load();
			NHTechTree.load();
			
//			Vars.content.each(c -> {
//				if(c instanceof UnlockableContent && c.minfo.mod == MOD){
//					c.
//				}
//			});
		}
		
		contentLoadComplete = true;
		
		Log.info(MOD.meta.displayName + " Loaded Complete: " + MOD.meta.version + " | Cost Time: " + (Time.elapsed() / Time.toSeconds) + " sec.");
    }
}
