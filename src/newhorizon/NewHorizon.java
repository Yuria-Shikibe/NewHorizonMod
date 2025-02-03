package newhorizon;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.*;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.ctype.ContentType;
import mindustry.game.EventType;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.net.ServerGroup;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.PlanetDialog;
import mindustry.world.modules.ItemModule;
import newhorizon.content.*;
import newhorizon.content.blocks.CraftingBlock;
import newhorizon.content.blocks.DistributionBlock;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.AutoEventTrigger;
import newhorizon.expand.eventsys.types.WorldEventType;
import newhorizon.expand.net.NHCall;
import newhorizon.util.DebugFunc;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.ui.FeatureLog;
import newhorizon.util.ui.TableFunc;
import newhorizon.util.ui.dialog.NewFeatureDialog;

import static mindustry.Vars.tilesize;
import static newhorizon.NHInputListener.registerModBinding;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;


public class NewHorizon extends Mod{
	public static boolean DEBUGGING = false;
	public static final boolean DEBUGGING_SPRITE = false;
	
	public static void debugLog(Object obj){
		if(DEBUGGING)Log.info(obj);
	}
	
	protected static boolean contentLoadComplete = false;
	
	@SuppressWarnings("FinalStaticMethod")
	public static final boolean loadedComplete(){
		return contentLoadComplete;
	}
	
	public static final String MOD_RELEASES = "https://github.com/Yuria-Shikibe/NewHorizonMod/releases";
	public static final String MOD_REPO = "Yuria-Shikibe/NewHorizonMod";
	public static final String MOD_GITHUB_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod.git";
	public static final String MOD_NAME = "new-horizon";
	public static final String SERVER = "203.135.99.51:10094", SERVER_ADDRESS = "175.178.22.6", SERVER_AUZ_NAME = "NEWHORIZON AUZ SERVER";
	public static final String EU_NH_SERVER = "Emphasize.cn:12510";
	
	private static boolean showed = false;
	public static Mods.LoadedMod MOD;
	
	public static Links.LinkEntry[] links;
	/** return "new-horizon-name" for sprite. */
	public static String name(String name){
		return MOD_NAME + "-" + name;
	}
	
	public static FeatureLog[] getUpdateContent(){
		return new FeatureLog[]{
				new FeatureLog(DistributionBlock.conveyor),
				new FeatureLog(DistributionBlock.conveyorJunction),
				new FeatureLog(DistributionBlock.conveyorRouter),
				new FeatureLog(DistributionBlock.conveyorGate),
				new FeatureLog(DistributionBlock.conveyorBridge),
				new FeatureLog(DistributionBlock.conveyorUnloader),
				new FeatureLog(DistributionBlock.liquidBridge),
				new FeatureLog(DistributionBlock.liquidUnloader),
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
			for(Links.LinkEntry entry : links){
				TableFunc.link(table, entry);
			}
		}).grow().row();
		dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 4, LEN);
		dialog.addCloseListener();
		dialog.show();
	}
	
	public static void showNew(){
		NewFeatureDialog newFeatureDialog = new NewFeatureDialog();
		newFeatureDialog.show();
	}
	
	public static void startLog(){
		if(showed)return;
		showed = true;

		
		BaseDialog dialog = new BaseDialog(""){
			@Override
			public void hide(){
				super.hide();
			}
		};

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
					t.button("@back", Icon.left, Styles.cleart, dialog::hide).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@links", Icon.link, Styles.cleart, NewHorizon::showAbout).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@hide-setting", Icon.settings, Styles.cleart, () -> Core.settings.put("nh_hide_starting_log", true)).disabled(b -> Core.settings.getBool("nh_hide_starting_log", false)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button("@log", Icon.book, Styles.cleart, NewHorizon::showNew).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
					t.button(Core.bundle.get("servers.remote") + "\n(" + Core.bundle.get("waves.copy") + ")", Icon.host, Styles.cleart, () -> Core.app.setClipboardText(SERVER)).growX().height(LEN).padLeft(OFFSET).padRight(OFFSET).row();
				}).grow();
				if(!Vars.mobile)table.table(t -> {

				}).grow();
			}).fill();
		}).grow();
		dialog.show();
	}
	
	public NewHorizon(){
		DEBUGGING = NHSetting.getBool(NHSetting.DEBUGGING);
		debugFunctions();

		registerModBinding();
		Events.on(ClientLoadEvent.class, e -> {
			Core.app.post(NHUI::init);
			updateServer();
			fetchNewRelease();
			showNewDialog();
			showStartLog();
			Time.run(10f, () -> {
				//DebugFunc.outputAtlas();
			});
		});
	}

	@Override
	public void init() {
		Vars.netServer.admins.addChatFilter((player, text) -> text.replace("jvav", "java"));

		NHVars.init();
		/*
		Time.mark();
		RectSpiller ls = new RectSpiller();
		int size = 128;
		ls.init(size, size);
		Pixmap pixmap = new Pixmap(size, size);
		ls.each((x, y) -> {
			if (ls.getPos(x, y) == 0){
				Tmp.c1.set(Color.white);
			}else {
				Tmp.c1.set(Color.black);
			}
			pixmap.set(x, y, Tmp.c1);
		});
		Fi fi = new Fi(NH_DEBUG_GRAPHIC_FOLDER + "value.png");
		PixmapIO.writePng(fi, pixmap);
		pixmap.dispose();
		Log.info("generated time: " + Time.elapsed());

		 */
	}

	@Override
	public void registerClientCommands(CommandHandler handler) {
		handler.<Player>register("applystatus", "Apply a status to player's unit", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0 || args[0].isEmpty()) {
				for(StatusEffect s : Vars.content.statusEffects()){
					player.sendMessage(s.name + "|" + s.id);
				}
			} else {
				try {
					player.unit().apply(Vars.content.getByID(ContentType.status, Integer.parseInt(args[0])), 120 * Time.toSeconds);
				} catch (NumberFormatException var3) {
					player.sendMessage("[VIOLET]Failed, the param must be a <Number>");
				}
			}
		});
		
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
					WorldEvent event = NHGroups.events.getByID(Integer.parseInt(args[0]));
					event.type.triggerNet(event);
					player.sendMessage("Triggered: " + event);
				} catch (NumberFormatException var3) {
					player.sendMessage("[VIOLET]Failed, the ID must be a <Number>");
				}
			}
		});
		
		handler.<Player>register("setupevent", "<name> [team] [x] [y]", "Setup Event (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0) {
				player.sendMessage("[VIOLET]Failed, pls type ID");
			} else {
				try {
					Team team;
					
					WorldEventType event = WorldEventType.getStdType(args[0]);
					
					WorldEvent e = event.eventProv.get();
					e.type = event;
					e.init();
					e.add();
					
					if(event.initPos != -1 && event.hasCoord){
						Tmp.p1.set(Point2.unpack(event.initPos));
						e.set(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
					}
					
					if(args.length >= 2){
						e.team = Team.get(Integer.parseInt(args[1]));
						if(args.length >= 4){
							e.set(Integer.parseInt(args[2]) * 8 + 4, Integer.parseInt(args[3]) * 8 + 4);
						}
					}
					
					if(args.length >= 2)Core.app.post(() -> e.team = Team.get(Integer.parseInt(args[1])));
					
					player.sendMessage("Setup: " + event + " | " + e.team + " | (" + World.toTile(e.x) + ", " + World.toTile(e.y) + ")");
				} catch (NumberFormatException var3) {
					player.sendMessage("[VIOLET]Undefined<Number>");
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
		
		handler.<Player>register("killBelow", "<health>", "Trigger Event (Admin Only)", (args, player) -> {
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0 || args[0].isEmpty()) {
				Groups.unit.each(b -> b.type.health < 800, b -> Time.run(Mathf.random(60, 300), b::kill));
			} else {
				Groups.unit.each(b -> b.type.health < Integer.parseInt(args[0]), b -> Time.run(Mathf.random(60, 300), b::kill));
			}
		});
		
		handler.<Player>register("killteam", "<id>", "Destroy The Team (Admin Only)", (args, player) -> {
			Cons<Team> destroyer = t -> {
				Groups.build.each(b -> b.team == t, b -> Time.run(Mathf.random(60, 300), b::kill));
				Groups.unit.each(b -> b.team == t, b -> Time.run(Mathf.random(60, 300), b::kill));
			};
			
			if (!player.admin()) {
				player.sendMessage("[VIOLET]Admin Only");
			} else if (args.length == 0 || args[0].isEmpty()) {
				destroyer.get(player.team());
			} else {
				destroyer.get(Team.get(Integer.parseInt(args[0])));
				player.sendMessage("Killed: [accent]" + Team.get(Integer.parseInt(args[0])));
			}
		});

		handler.<Player>register("events", "List all cutscene events in the map.", (args, player) -> {
			if (NHGroups.events.isEmpty()) {
				player.sendMessage("No Event Available");
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("[accent]Events: [lightgray]\n");
				NHGroups.events.each((e) -> {
					builder.append(e).append('\n');
				});
				player.sendMessage(builder.toString());
			}
		});

		handler.<Player>register("eventtypes", "List all cutscene event types in the map.", (args, player) -> {
			if (WorldEventType.allTypes.isEmpty()) {
				player.sendMessage("No EventTypes Available");
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("[accent]Events: [lightgray]\n");
				WorldEventType.allTypes.each((k, e) -> {
					builder.append(e.getClass().getSuperclass().getSimpleName()).append("->").append(k).append('\n');
				});
				player.sendMessage(builder.toString());
			}
		});
		
		handler.<Player>register("eventtriggers", "List all event triggers in the map.", (args, player) -> {
			if (NHGroups.autoEventTrigger.isEmpty()) {
				player.sendMessage("No Trigger Available");
			} else {
				StringBuilder builder = new StringBuilder();
				NHGroups.autoEventTrigger.each(e -> {
					builder.append("\n======================\n");
					builder.append("[royal]").append(e.toString()).append(" | ").append(e.eventType.name).append(" | [lightgray]").append('\n').append(e.desc()).append('\n').append("Meet Requirements?: ").append((e.meet() ? "[heal]Yes[]" : "[#ff7b69]No[]")).append("[lightgray]\n").append("Reload: ").append(e.getReload()).append("\nSpacing: ").append(e.getSpacing()).append('\n');
					builder.append("Percentage: [accent]").append((int)(e.getReload() / e.getSpacing() * 100)).append("[]%\n");
					builder.append("======================\n");
				});
				NHCall.infoDialog(builder.toString(), player.con);
			}
		});
		
		handler.<Player>register("getscale", "Check Auto Event Trigger Time Scale", (args, player) -> {
			player.sendMessage("Scale: " + AutoEventTrigger.timeScale);
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
		contentLoadComplete = false;
		
		Log.info("Debug Mode: " + DEBUGGING);
		Log.info("Process Texture Mode: " + NHPixmap.isDebugging());
		
		Time.mark();
		
		MOD = Vars.mods.getMod(getClass());
		
		EntityRegister.load();
		NHRegister.load();
		
		NHContent.loadPriority();
		
		NHSounds.load();
		
		if(!Vars.headless){
			NHShaders.init();
		}
		
		NHContent.loadBeforeContentLoad();
		
		{
			NHStatusEffects.load();
			NHItems.load();
			NHLiquids.load();
			NHBullets.load();
			NHUnitTypes.load();
			Recipes.load();
			NHBlocks.load();
			NHWeathers.load();
			NHPlanets.load();
			NHSectorPresents.load();
			NHTechTree.load();
			NHInbuiltEvents.load();
		}
		
		NHSetting.load();
		
		NHOverride.load();
		if(Vars.headless || NHSetting.getBool(NHSetting.VANILLA_COST_OVERRIDE))NHOverride.loadOptional();
		
		NHContent.loadLast();
		
		contentLoadComplete = true;
		
		Log.info(MOD.meta.displayName + " Loaded Complete: " + MOD.meta.version + " | Cost Time: " + (Time.elapsed() / Time.toSeconds) + " sec.");
    }

	private void debugFunctions(){
		if (DEBUGGING){
			PlanetDialog.debugSelect = true;
			Events.run(EventType.Trigger.universeDrawEnd, DebugFunc::renderSectorId);
		}
	}

	private void updateServer(){
		Vars.defaultServers.add(new ServerGroup(){{
			name = "[sky]New Horizon [white]Mod [lightgray]Servers";
			addresses = new String[]{SERVER};
		}});
	}

	private void fetchNewRelease(){
		if(!DEBUGGING)Time.runTask(15f, () -> Threads.daemon(() -> {
			Http.get(Vars.ghApi + "/repos/" + MOD_REPO + "/releases/latest", res -> {
				Jval json = Jval.read(res.getResultAsString());

				String tag = json.get("tag_name").asString();
				String body = json.get("body").asString();

				if(tag != null && body != null && !tag.equals(Core.settings.get(MOD_NAME + "-last-gh-release-tag", "0")) && !tag.equals('v' + MOD.meta.version)){
					Core.app.post(() -> {
						new BaseDialog(Core.bundle.get("mod.ui.has-new-update") + ": " + tag){{
							cont.table(t -> {
								t.add(new WarningBar()).growX().height(LEN / 2).padLeft(-LEN).padRight(-LEN).padTop(LEN).expandX().row();
								t.image(NHContent.icon2).center().pad(OFFSET).color(Pal.accent).scaling(Scaling.bounded).row();
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
	}

	private void showNewDialog(){
		Time.runTask(10f, () -> {
			if(!Core.settings.get("nh-lastver", -1).equals(MOD.meta.version)){showNew();}
			Core.settings.put("nh-lastver", MOD.meta.version);
		});
	}

	private void showStartLog(){
		if(!Core.settings.getBool(NHSetting.START_LOG))Core.app.post(Time.runTask(10f, NewHorizon::startLog));
	}
}
