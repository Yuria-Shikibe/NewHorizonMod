package newhorizon.feature;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.actions.*;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.editor.MapEditorDialog;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.io.TypeIO;
import mindustry.maps.Map;
import mindustry.mod.Mods;
import mindustry.mod.Scripts;
import mindustry.net.Net;
import mindustry.net.Packet;
import mindustry.type.SectorPreset;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.func.NHFunc;
import newhorizon.func.NHInterp;
import newhorizon.func.TableFunc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static mindustry.Vars.*;
import static newhorizon.func.TableFunc.LEN;
import static newhorizon.func.TableFunc.OFFSET;

public class CutsceneScript{
	public static final String CCS_URL = "https://github.com/Yuria-Shikibe/NewHorizonMod/wiki/Cutscene-Script-Custom-Guide";
	/**
	 * Each {@link SectorPreset} can has multiple actor.
	 * <p></p>
	 * {@code updaters} Maps the actions during playing.<p>
	 * {@code initer} Maps the actions after the world is loaded.<p>
	 * {@code ender} Maps the actions after the game is over.<p>
	 * <p></p>
	 * {@code curSectorPreset} useless now.
	 * <p></p>
	 * {@code initHasRun} Used to avoid repeating loading.
	 *
	 *
	 * @author Yuria / Martix
	 */
	protected static Fi scriptDirectory;
	public static Fi currentScriptFile;
	
	public static Mods.LoadedMod mod;
	
	public static Scripts scripts;
	
	public static final String CUTSCENE_KEY = "custom-cutscene-script";
	
	public static final ObjectMap<SectorPreset, Seq<Runnable>> updaters = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Runnable>> initer = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Cons<Boolean>>> ender = new ObjectMap<>(6); // true -> win, false -> lose
	
	public static @Nullable SectorPreset curSectorPreset = null;
	
	/**
	 * {@code curUpdater} Used to storage movements that is acted every update(Do not run during pause).
	 * {@code curIniter} Used to storage movements that is acted when the world is loaded.
	 * {@code curEnder} Used to stroage movements that is acted when game over. Param `Boolean`: true -> win; false -> lose.
	 * */
	
	public static final Seq<Runnable> curUpdater = new Seq<>(), curIniter = new Seq<>();
	public static final Seq<Cons<Boolean>> curEnder = new Seq<>();
	
	public static final Seq<Runnable> taskUpdater = new Seq<>();
	
	/** Used to storage movements that will be called when a specific type of block is destroyed*/
	public static final ObjectMap<Block, Cons<Building>> blockDestroyListener = new ObjectMap<>();
	
	protected static boolean isPlayingCutscene = false;
	
	public static void addListener(Block type, Cons<Building> actor){
		blockDestroyListener.put(type, actor);
	}
	
	public static void addListener(Seq<Block> types, Cons<Building> actor){
		for(Block type : types)addListener(type, actor);
	}
	
	public static Interval timer = new Interval(6); //[0] Is Captured
	
	protected static boolean initHasRun = false;
	
	protected static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();
	
	static{
		Events.on(EventType.BlockDestroyEvent.class, e -> {
			if(e.tile.build == null || !blockDestroyListener.containsKey(e.tile.build.block()))return;
			
			blockDestroyListener.get(e.tile.build.block).get(e.tile.build);
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if((e.from == GameState.State.playing && e.to == GameState.State.menu)){
				reset();
				UIActions.reset();
			}
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(initHasRun)return;
			init(Vars.state.getSector() == null ? null : Vars.state.getSector().preset);
		});
		
		Events.run(EventType.Trigger.update, () -> {
			if(!state.isMenu()){
				if(UIActions.cameraActor != null)UIActions.cameraActor.run();
				taskUpdater.each(Runnable::run);
			}
			if(!Vars.state.isPlaying())return;
			if(timer.get(0, 900))handleTagData();
			if(curUpdater.any()) curUpdater.each(Runnable::run);
		});
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			curEnder.each(c -> {
				c.get(true);
			});
		});
		
		Events.on(EventType.SectorLoseEvent.class, e -> {
			curEnder.each(c -> {
				c.get(false);
			});
		});
		
		Events.on(EventType.ClientLoadEvent.class, e -> {
			try{
				BaseDialog menu;
				Field field = MapEditorDialog.class.getDeclaredField("menu");
				field.setAccessible(true);
				menu = (BaseDialog)field.get(ui.editor);
				
				menu.cont.row().button("Cutscene Scripts", Icon.eye, () -> {
					new BaseDialog(""){{
						addCloseButton();
						
						cont.table(t -> {
							t.defaults().size(LEN * 6, LEN).padBottom(OFFSET);
							
							t.button("Guide", Icon.info, () -> {
								Core.app.openURI(CCS_URL);
							}).row();
							t.button("Package Scripts", Icon.download, () -> {
								platform.showMultiFileChooser(file -> {
									editor.tags.put(CUTSCENE_KEY,
										file.readString()
									);
								}, "js");
							}).row();
							t.button("Delete Scripts", Icon.trash, () -> {
								ui.showConfirm("Are you sure you want to delete it?", () -> {
									if(editor.tags.remove(CUTSCENE_KEY) != null){
										ui.showText("OPERATION STATE", "Delete Successfully");
									}else ui.showErrorMessage("Script is null");
								});
							}).disabled(b -> !editor.tags.containsKey(CUTSCENE_KEY)).row();
							t.button("Read Scripts", Icon.bookOpen, () -> {
								new BaseDialog(""){{
									addCloseButton();
									
									cont.pane(t -> {
										Label rootScript = new Label(mod.root.child("scripts").child("cutsceneLoader.js").readString().trim());
										rootScript.setWrap(false);
										Label label = new Label(editor.tags.get(CUTSCENE_KEY).trim());
										label.setWrap(false);
										
										t.left();
										
										t.image().height(OFFSET / 3).growX().color(Pal.heal).padTop(OFFSET / 2).row();
										t.add("[heal]//Package Importer: ").pad(OFFSET / 2).row();
										t.image().height(OFFSET / 3).growX().color(Pal.heal).padBottom(OFFSET / 2).row();
										
										t.add(rootScript).color(Color.gray).growX().padLeft(LEN * 3).row();
										
										t.image().height(OFFSET / 3).growX().color(Pal.heal).padTop(OFFSET / 2).row();
										t.add("[heal]//Custom Cutscene Script: ").pad(OFFSET / 2).row();
										t.image().height(OFFSET / 3).growX().color(Pal.heal).padBottom(OFFSET / 2).row();
										
										t.add(label).growX().padLeft(LEN * 3);
										
									}).grow();
								}}.show();
							}).disabled(b -> !editor.tags.containsKey(CUTSCENE_KEY)).row();
						}).grow();
					}}.show();
				}).size(180f * 2 + 10f, 60f);
			}catch(IllegalAccessException | NoSuchFieldException ex){
				ui.showErrorMessage(ex.toString());
			}
		});
	}
	
	public static String scriptDirectoryPath(){
		return Vars.customMapDirectory + "/custom-cutscene";
	}
	
	public static void load(){
		initDirectory();
		scripts = platform.createScripts();
		mod = mods.getMod(NewHorizon.class);
		
		Net.registerPacket(TagPacket::new);
	}
	
	protected static void initDirectory(){
		scriptDirectory = new Fi(scriptDirectoryPath());
		if (!scriptDirectory.exists()) {
			scriptDirectory.mkdirs();
		}
	}
	
	public static boolean hasScript(){
		if(state.getSector() != null && state.getSector().preset != null){
			SectorPreset sp = state.getSector().preset;
			return initer.containsKey(sp) || updaters.containsKey(sp) || ender.containsKey(sp);
		}else if(state.map != null){
			return state.map.hasTag(CUTSCENE_KEY) || (state.map.name().contains("(@HC)") && scriptDirectory.child(state.map.name() + "-cutscene.js").exists());
		}else return false;
	}
	
	public static String getScript(Map map){
		if(map.tags.containsKey(CUTSCENE_KEY)){
			return map.tag(CUTSCENE_KEY);
		}else if(map.name().contains("(@HC)")){
			currentScriptFile = scriptDirectory.child(map.name() + "-cutscene.js");
			if(!currentScriptFile.exists()){
				if(map.tags.containsKey(CUTSCENE_KEY)){
					return map.tag(CUTSCENE_KEY);
				}else try{
					Log.info("Tried Create Script File: " + currentScriptFile.file().createNewFile() + " | " + currentScriptFile.file().getAbsolutePath());
				}catch(IOException e){
					Vars.ui.showErrorMessage(e.toString());
				}
			}
			String code = currentScriptFile.readString();
			TableFunc.textArea.setText(code);
			return code;
		}else return null;
	}
	
	protected static void reset(){
		curSectorPreset = null;
		currentScriptFile = null;
		initHasRun = false;
		curEnder.clear();
		curIniter.clear();
		curUpdater.clear();
		timer.clear();
		blockDestroyListener.clear();
		UIActions.waitingPool.clear();
		taskUpdater.clear();
		UIActions.cameraActor = null;
		
		TableFunc.textArea.clearText();
	}
	
	public static Fi getModGlobalJS(){
		return mod.root.child("scripts").child("cutsceneLoader.js");
	}
	
	public static String getModGlobalJSCode(){
		return getModGlobalJS().readString();
	}
	
	/**
	 * Run js codes.
	 *
	 * */
	public static void runJS(String js){
		if(js == null || js.isEmpty())return;
		
		try{
			Class<? extends Scripts> scriptsClass = scripts.getClass();
			
			Method method = scriptsClass.getDeclaredMethod("run", String.class, String.class, boolean.class);
			Field field = scriptsClass.getDeclaredField("currentMod");
			
			method.setAccessible(true);
			field.setAccessible(true);
			
			field.set(scripts, mod);
			method.invoke(scripts, mod.root.child("scripts").child("cutsceneLoader.js").readString() + js, state.map.name(), true);
		}catch(Exception e){
			Vars.ui.showErrorMessage(e.toString());
		}
	}
	
	/**
	 * Run js codes with try, catch statements.
	 *
	 * */
	public static void tryRunJS(String js){
		if(js == null || js.isEmpty())return;
		
		try{
			Class<? extends Scripts> scriptsClass = scripts.getClass();
			
			Method method = scriptsClass.getDeclaredMethod("run", String.class, String.class, boolean.class);
			Field field = scriptsClass.getDeclaredField("currentMod");
			
			method.setAccessible(true);
			field.setAccessible(true);
			
			field.set(scripts, mod);
			method.invoke(scripts,
					mod.root.child("scripts").child("cutsceneLoader.js").readString() + "\ntry{" + js + "\n}catch(e){Vars.ui.showErrorMessage(e.toString());}",
			state.map.name(), true);
		}catch(Exception e){
			Vars.ui.showErrorMessage(e.toString());
		}
	}
	
	protected static void init(SectorPreset sector){
		reset();
		
		UIActions.forceInit();
		
		curSectorPreset = sector;
		
		if(sector == null){
			Core.app.post(() -> {
				runJS(getScript(Vars.state.map));
				curIniter.each(Runnable::run);
			});
		}else{
			if(updaters.containsKey(sector)){
				curUpdater.addAll(updaters.get(sector));
			}
			
			if(initer.containsKey(sector)){
				curIniter.addAll(initer.get(sector));
			}
			
			if(ender.containsKey(sector)){
				curEnder.addAll(ender.get(sector));
			}
			
			curIniter.each(Runnable::run);
		}
	}
	
	public static boolean canInit(){
		boolean b = !state.rules.tags.containsKey("inited") || !Boolean.parseBoolean(state.rules.tags.get("inited"));
		state.rules.tags.put("inited", "true");
		initHasRun = true;
		return b;
	}
	
	public static boolean eventHasData(String key){
		return state.rules.tags.containsKey(key);
	}
	
	public static void run(String key, Boolf<String> boolf, Runnable run){
		if(state.rules.tags.containsKey(key) && boolf.get(state.rules.tags.get(key))){
			run.run();
		}
	}
	
	public static boolean getBool(String key){
		return state.rules.tags.containsKey(key) && Boolean.parseBoolean(state.rules.tags.get(key));
	}
	
	public static float getFloat(String key){
		return Float.parseFloat(state.rules.tags.get(key));
	}
	
	public static float getFloatOrNaN(String key){
		float f = Float.NaN;
		try{
			f = Float.parseFloat(state.rules.tags.get(key));
		}catch(Exception ignore){
		
		}
		return f;
	}
	
	/**
	 * Run an event that only happens once.
	 *
	 * */
	public static boolean runEventOnce(String eventName, Runnable run){
		boolean hasRun = false;
		if(!state.rules.tags.containsKey(eventName) || !Boolean.parseBoolean(state.rules.tags.get(eventName))){
			run.run();
			hasRun = true;
		}
		
		state.rules.tags.put(eventName, "true");
		
		return hasRun;
	}
	
	/**
	 * Run an event that can happens times.
	 *
	 * */
	public static boolean runEventMulti(String eventName, int maxTimes, Runnable run){
		int num = 0;
		
		boolean hasRun = false;
		
		if(state.rules.tags.containsKey(eventName)){
			num = Integer.parseInt(state.rules.tags.get(eventName));
			if(num < maxTimes){
				run.run();
				hasRun = true;
			}
		}
		
		state.rules.tags.put(eventName, String.valueOf(++num));
		Log.info(state.rules.tags.get(eventName));
		
		return hasRun;
	}
	
	/**
	 * Set the progress of a certain event.
	 * @param time Use tick format
	 * */
	public static void setReload(String eventName, float time){
		state.rules.tags.put(eventName, String.valueOf(time));
	}
	
	/**
	 * Set the total reload time of a certain event.
	 *
	 * @param reloadTime Use tick format
	 * @param speed Usually {@link Time}{@code .delta} or its multiple.
	 * @param exist If false, the data of the event will be removed.
	 * @param canContinue Used to check whether the progress of the event should continue.
	 * @param run Actions
	 *
	 * @see Prov
	 * @see Runnable
	 *
	 * */
	public static void reload(String eventName, float speed, float reloadTime, Prov<Boolean> exist, Prov<Boolean> canContinue, Runnable run){
		if(exist.get()){
			if(canContinue.get()){
				if(state.rules.tags.containsKey(eventName)){
					float time = Float.parseFloat(state.rules.tags.get(eventName));
					time += speed;
					if(time > reloadTime){
						setReload(eventName, 0);
						run.run();
					}else setReload(eventName, time);
				}else setReload(eventName, 0);
			}
		}else state.rules.tags.remove(eventName);
	}
	
	public static class WorldActions{
		
		/**
		 * Use a certain {@link BulletType} bullet to attack a certain place.
		 * The {@code lifetime} of the bullet will be scaled.
		 *
		 * @param type The bullet type to be shot.
		 * @param owner The owner of the bullet. Usually use a {@link mindustry.world.blocks.storage.CoreBlock.CoreBuild} core of a certain team.
		 * @param team The team of the owner of the bullet.
		 * @param modifier A {@link Cons} modifier that used to modify the bullet entity {@link Bullet}.
		 * @param x Use *8 format.
		 * @param y Use *8 format.
		 * @param toX Use *8 format.
		 * @param toY Use *8 format.
		 *
		 * @see mindustry.content.Bullets
		 * @see newhorizon.content.NHBullets
		 * @see BulletType
		 * @see Cons
		 * @see Entityc
		 * @see Bullet
		 * @see Team
		 *
		 * @return Returns a {@link Runnable} runnable function. Use {@code run()} to act it.
		 * */
		public static Runnable raidPos(Entityc owner, Team team, BulletType type, float x, float y, float toX, float toY, Cons<Bullet> modifier){
			float scl = NHFunc.scaleBulletLifetime(type, x, y, toX, toY);
			
			return () -> {
				modifier.get(type.create(owner, team, x, y, Angles.angle(x, y, toX, toY), 1, scl));
			};
		}
		
		public static Runnable raidPos(Entityc owner, Team team, BulletType type, float x, float y, Position target, Cons<Bullet> modifier){
			float scl = NHFunc.scaleBulletLifetime(type, x, y, target.getX(), target.getY());
			
			return () -> {
				modifier.get(type.create(owner, team, x, y, Angles.angle(x, y, target.getX(), target.getY()), 1, scl));
			};
		}
		
		public static Runnable raidDirection(Entityc owner, Team team, BulletType type, float x, float y, float angle, float distance, Cons<Bullet> modifier){
			return raidPos(owner, team, type, x, y, v1.trns(angle, distance).add(x, y), modifier);
		}
		
		public static void raidFromCoreToCore(Team from, Team target, BulletType ammo, int number, float shootDelay, float randP, float inaccuracy){
			CoreBlock.CoreBuild coreFrom = from.cores().firstOpt();
			CoreBlock.CoreBuild coreTarget = state.teams.closestCore(coreFrom.x, coreFrom.y, target);
			
			UIActions.actionSeq(
				Actions.parallel(
					UIActions.cautionAt(coreTarget.x, coreTarget.y, coreTarget.block.size * tilesize / 3.5f, number * shootDelay / 60f, coreTarget.team.color),
					Actions.run(() -> {
						NHSounds.alarm.play();
						for(int i = 0; i < number; i++){
							Time.run(i * shootDelay, CutsceneScript.WorldActions.raidPos(coreFrom, coreFrom.team, ammo, coreFrom.x + Mathf.range(randP), coreFrom.y + Mathf.range(randP), coreTarget, b -> {
								b.vel.rotate(Mathf.range(inaccuracy));
								if(b.type.shootEffect != null)b.type.shootEffect.at(b.x, b.y, b.angleTo(coreTarget), b.type.hitColor);
								if(b.type.smokeEffect != null)b.type.smokeEffect.at(b.x, b.y, b.angleTo(coreTarget), b.type.hitColor);
							}));
						}
					}),
					UIActions.labelAct(
						"[accent]Caution[]: @@@Raid Incoming."
						, 0.75f, number * shootDelay / 60f, Interp.linear, t -> {
							t.image(Icon.warning).padRight(OFFSET);
						}
					)
				)
			);
		}
		
		public static void raidFromCoreToCoreDefault(BulletType ammo, int number, float shootDelay, float randP, float inaccuracy){
			raidFromCoreToCore(state.rules.waveTeam, state.rules.defaultTeam, ammo, number, shootDelay, randP, inaccuracy);
		}
	}
	
	public static class UIActions{
		public static Seq<Action[]> waitingPool = new Seq<>();
		public static Action[] currentActions;
		
		public static void forceInit(){
			if(eventBarTable != null) eventBarTable.remove();
			
			Element element = Core.scene.root.find("CutsceneHUD");
			if(element != null)element.remove();
			
			if(Core.scene.root.find("CutsceneHUD") == null){
				
				eventBarTable = new HUDTable();
				
				Core.scene.root.addChildAt(1, eventBarTable);
			}
			
			eventBarTable.setup();
		}
		
		public static void init(){
			if(eventBarTable != null) eventBarTable.remove();
			
			Element element = Core.scene.root.find("CutsceneHUD");
			if(element != null)element.remove();
			
			if(!hasScript())return;
			
			if(Core.scene.root.find("CutsceneHUD") == null){
				eventBarTable = new HUDTable();
				
				Core.scene.root.addChildAt(1, eventBarTable);
			}
			
			eventBarTable.setup();
		}
		
		public static void reset(){
			Log.info("Run Reset");
			
			if(eventBarTable == null)return;
			eventBarTable.reset();
			eventBarTable.remove();
			eventBarTable = null;
		}
		
		
		protected static Table actor;
		protected static Table defaultFiller = filler();
		protected static Table up = new Table(), down = new Table();
		protected static HUDTable eventBarTable = null;
		
		protected static Runnable cameraActor = null;
		
		protected static class HUDTable extends Table{
			public Table paneTable = new Table();
			public ScrollPane pane;
			
			public HUDTable(){
				name = "CutsceneHUD";
				
				update(() -> {
					if(state.isMenu())remove();
					visible(() -> ui.hudfrag.shown);
					
					setSize(Core.graphics.getWidth() / 4f, Core.graphics.getHeight() / 4.45f);
					setPosition(Core.settings.getInt("eventbarsoffsetx", 0) / 100f * Core.graphics.getWidth(), Core.settings.getInt("eventbarsoffsety", 0) / 100f * Core.graphics.getHeight());
				});
				
				background(Tex.buttonEdge3);
				
				pane = pane(t -> {
					paneTable = t;
					t.top();
				}).grow().pad(OFFSET).get();
				
				exited(() -> {
					getScene().unfocus(this);
				});
			}
			
			public void updateChildren(){
//				childrenChanged();
			}
			
			public void addElement(Element element){
				paneTable.add(element);
			}
			
			public void setup(){
				paneTable.clear();
				state.rules.tags.each((k, v) -> {
					if(k.startsWith(KeyFormat.SHOW_PREFIX)){
						try{
							String[] s = k.split(KeyFormat.SPLITTER);
							float time = Float.parseFloat(s[s.length - 1]);
							reloadBar(k, KeyFormat.getEventTotalTime(k), () -> KeyFormat.getEventName(k), () -> KeyFormat.getEventColor(k));
						}catch(Exception e){
							ui.showException(e);
						}
					}
				});
			}
		}
		
		//TODO actions that will be acted while skipping a cutscene.
		public static ImportantRunnableAction necessaryRun(Runnable runnable){
			ImportantRunnableAction action = Actions.action(ImportantRunnableAction.class, ImportantRunnableAction::new);
			action.setRunnable(runnable);
			return action;
		}
		
		/**
		 * @implNote Set an identifier for the target point on the HUD.
		 *
		 * @param duration Use second format.
		 * @param x Use *8 format.
		 * @param y Use *8 format.
		 * */
		public static CautionAction cautionAt(float x, float y, float size, float duration, Color color){
			CautionAction action = Actions.action(CautionAction.class, CautionAction::new);
			action.setDuration(duration);
			action.x = x;
			action.y = y;
			action.size = size * 6;
			action.color = color;
			return action;
		}
		
		/**
		 * @implNote Set the camera on a certain target.
		 *
		 * @param duration Use second format.
		 * */
		public static CameraTrackerAction track(Position target, float duration){
			CameraTrackerAction action = Actions.action(CameraTrackerAction.class, CameraTrackerAction::new);
			action.trackTarget = target;
			action.setDuration(duration);
			return action;
		}
		
		/**
		 * @implNote Make the camera slide to a certain position.
		 *
		 * @param duration Use second format.
		 * @param x Use *8 format.
		 * @param y Use *8 format.
		 * @param interpolation Move animation curve.
		 * */
		public static CameraMoveAction moveTo(float x, float y, float duration, Interp interpolation){
			CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
			action.setPosition(x, y);
			action.setDuration(duration);
			action.setInterpolation(interpolation);
			return action;
		}
		
		/**
		 * @implNote Make the camera fixed on a certain position.
		 *
		 * @param duration Use second format.
		 * @param x Use *8 format.
		 * @param y Use *8 format.
		 * */
		public static CameraMoveAction holdCamera(float x, float y, float duration){
			CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
			action.startX = action.endX = x;
			action.startY = action.endY = y;
			action.setDuration(duration);
			return action;
		}
		
		/**
		 * @implNote Pop up a text dialog on your screen.
		 *
		 * @param duration The time text generating uses. Use second format.
		 * @param holdDuration The time text keeping showing. Use second format.
		 * */
		public static LabelAction labelAct(String text, float duration, float holdDuration){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.text = text;
			return action;
		}
		
		/**
		 * @implNote Pop up a text dialog on your screen.
		 *
		 * @param duration The time text generating uses. Use second format.
		 * @param holdDuration The time text keeping showing. Use second format.
		 * @param inFunc Animation curve the dialog fade in.
		 * @param outFunc Animation curve the dialog fade out.
		 * @param modifier Modifies the dialog.
		 * */
		public static LabelAction labelActFull(String text, float duration, float holdDuration, Interp outFunc, Interp inFunc, Cons<Table> modifier){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.setInterpolation(outFunc);
			action.inFunc = inFunc;
			action.text = text;
			action.modifier = modifier;
			return action;
		}
		
		/**
		 * @implNote Pop up a text dialog on your screen.
		 *
		 * @param duration The time text generating uses. Use second format.
		 * @param holdDuration The time text keeping showing. Use second format.
		 * @param interpolation Animation curve the dialog fade in.
		 * */
		public static LabelAction labelAct(String text, float duration, float holdDuration, Interp interpolation, Cons<Table> modifier){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.setInterpolation(interpolation);
			action.text = text;
			action.modifier = modifier;
			return action;
		}
		
		/** Make camera stop following player on desktop; make player stop following camera on phones. */
		public static void pauseCamera(){
			if(Vars.mobile)cameraActor = () -> player.unit().vel.setZero();
			else if(Vars.control.input instanceof DesktopInput)cameraActor = () -> ((DesktopInput)Vars.control.input).panning = true;
		}
		
		/** Make camera follow player on desktop; make player follow camera on phones. */
		public static void resumeCamera(){
			if(Vars.mobile)Core.camera.position.set(Vars.player);
			else if(Vars.control.input instanceof DesktopInput)((DesktopInput)Vars.control.input).panning = false;
			cameraActor = null;
		}
		
		/** Generate a table that fill the screen. */
		public static Table filler(Runnable removed, Runnable update, boolean removeShowUI){
			return new Table(Tex.clear){
				{
					Core.scene.root.addChild(this);
					
					update(update);
					setFillParent(true);
					visible(UIActions::shown);
					
					keyDown(k -> {
						if(k == KeyCode.escape) remove();
					});
				}
				
				@Override
				public void act(float delta){
					super.act(delta);
					if(Vars.state.isMenu())remove();
				}
				
				@Override
				public boolean remove(){
					if(removed != null)removed.run();
					if(removeShowUI) enableVanillaUI();
					return super.remove();
				}
			};
		}
		
		public static Table filler(){
			return filler(null, () -> {}, false);
		}
		
		/**
		 * Add ordered scripts.
		 *
		 * @see DelayAction
		 * @see ParallelAction
		 * @see SequenceAction
		 * @see RunnableAction
		 * @see ImportantRunnableAction
		 * @see LabelAction
		 * @see CameraMoveAction
		 * @see CameraTrackerAction
		 * @see CautionAction
		 * @see AddAction
		 * @see AddListenerAction
		 * @see RemoveListenerAction
		 * @see AfterAction
		 * @see IntAction
		 * @see FloatAction
		 * @see TimeScaleAction
		 * @see RepeatAction
		 *
		 */
		public static boolean actionSeq(Action... actions){
			boolean isPlaying = isPlayingCutscene;
			
			Action[] acts = new Action[actions.length + 1];
			System.arraycopy(actions, 0, acts, 0, actions.length);
			acts[acts.length - 1] = Actions.parallel(Actions.remove(), Actions.run(() -> currentActions = null));
			
			if(!isPlaying){
				isPlayingCutscene = true;
				currentActions = acts;
				Table filler = new Table(Tex.clear){
					{
						Core.scene.root.addChild(this);
						
						setFillParent(true);
						visible(UIActions::shown);
						
						keyDown(k -> {
							if(k == KeyCode.escape) remove();
						});
					}
					
					@Override
					public void act(float delta){
						super.act(delta);
						if(Vars.state.isMenu()) remove();
					}
					
					@Override
					public boolean remove(){
						enableVanillaUI();
						
						if(waitingPool.any()){
							Time.run(60f, () -> {
								isPlayingCutscene = false;
								actionSeq(waitingPool.pop());
							});
						}else isPlayingCutscene = false;
						
						return super.remove();
					}
				};
				
				filler.actions(acts);
			}else{
				waitingPool.add(acts);
			}
			
			return isPlaying;
		}
		
		/**
		 * Pull out the black curtain from the upper and lower sides of the screen.
		 * */
		public static Action curtainIn(float time, Interp func){
			return Actions.run(() -> {
				disableVanillaUI();
				down = new Table(Styles.black){{
					Core.scene.root.addChild(this);
					
					setPosition(0, -yAxis());
					
					update(() -> {
						setSize(Core.graphics.getWidth(), yAxis());
					});
					
					actions(Actions.moveTo(0, 0, time, func), Actions.run(() -> {
						update(() -> {
							disableVanillaUI();
							setSize(Core.graphics.getWidth(), yAxis());
							setPosition(0, 0);
						});
					}));
				}
					
					@Override
					public boolean remove(){
						
						return super.remove();
					}
					
					@Override
					public void act(float delta){
						super.act(delta);
						if(state.isMenu())remove();
					}
				};
				
				up = new Table(Styles.black){{
					Core.scene.root.addChild(this);
					
					setPosition(0, Core.graphics.getHeight());
					
					update(() -> {
						setSize(Core.graphics.getWidth(), yAxis());
					});
					
					actions(Actions.moveTo(0, Core.graphics.getHeight() - yAxis(), time, func), Actions.run(() -> {
						update(() -> {
							setSize(Core.graphics.getWidth(), yAxis());
							setPosition(0, Core.graphics.getHeight() - yAxis());
						});
					}));
				}
					@Override
					public void act(float delta){
						super.act(delta);
						if(state.isMenu())remove();
					}
				};
			});
		}
		
		/**
		 * Remove the black curtain from the upper and lower sides of the screen.
		 * */
		public static Action curtainOut(float time, Interp func){
			return Actions.run(() -> {
				if(up == null || down == null)return;
				
				down.actions(Actions.run(() -> {
					down.update(() -> {
						disableVanillaUI();
						down.setSize(Core.graphics.getWidth(), yAxis());
					});
				}), Actions.moveTo(0, -yAxis(), time, func), Actions.parallel(Actions.remove(), Actions.run(() -> {
					down.update(() -> {});
					enableVanillaUI();
				})));
				
				up.actions(Actions.run(() -> {
					up.update(() -> {
						up.setSize(Core.graphics.getWidth(), yAxis());
					});
				}), Actions.moveTo(0, Core.graphics.getHeight(), time, func), Actions.remove());
			});
		}
		
		/**
		 * Pull out the black curtain from the upper and lower sides of the screen.
		 * Make camera stop following player on desktop; make player stop following camera on phones.
		 * Hide vanilla UI.
		 * */
		public static Action startCutsceneDefault(){
			return Actions.sequence(Actions.parallel(Actions.delay(2f), UIActions.curtainIn(2f, Interp.pow2Out)), Actions.run(UIActions::pauseCamera));
		}
		
		/**
		 * Remove the black curtain from the upper and lower sides of the screen.
		 * Make camera follow player on desktop; make player follow camera on phones.
		 * Show vanilla UI.
		 * */
		public static Action endCutsceneDefault(){
			return Actions.parallel(Actions.run(UIActions::resumeCamera), UIActions.curtainOut(1f, Interp.pow2In));
		}
		
		protected static float yAxis(){return Core.graphics.getHeight() / 8f;}
		protected static boolean shown(){return !Vars.state.isMenu();}
		/** Hide vanilla UI.*/public static void disableVanillaUI(){Vars.ui.hudfrag.shown = false;}
		/** Show vanilla UI.*/public static void enableVanillaUI(){Vars.ui.hudfrag.shown = true;}
		
		/**
		 * Add an event bar after 0.75sec.
		 *
		 * @param totalTime Uses tick format
		 */
		public static void reloadBarDelay(String eventName, float totalTime, Color color){
			reloadBarDelay(KeyFormat.generateName(eventName, color, totalTime),totalTime, () -> eventName, () -> color);
		}
		
		/**
		 * Add an event bar after 0.75sec.
		 *
		 * @param totalTime Uses tick format
		 */
		public static void reloadBarDelay(String eventFullName, float totalTime, Prov<CharSequence> showName, Prov<Color> showColor){
			Time.runTask(45f, () -> reloadBar(eventFullName, totalTime, showName, showColor));
		}
		
		/**
		 * Add an event bar immediately.
		 *
		 * Bars will automatically remove itself if there is no data of the event.
		 * You can use method {@code setReload(String key, float time)} to set the data before calling a bar.
		 *
		 * @param totalTime Uses tick format
		 */
		public static void reloadBar(String eventName, float totalTime, Prov<CharSequence> showName, Prov<Color> showColor){
			if(!state.rules.tags.containsKey(eventName))return;
			
			Table t = new Table(Tex.clear){{
				add(new Bar(
					showName, showColor, () -> Float.parseFloat(state.rules.tags.get(eventName)) / totalTime
				)).growX().height(LEN - OFFSET).padBottom(OFFSET);
				update(() -> {
					if(!state.rules.tags.containsKey(eventName)){
						actions(Actions.moveBy(-width, 0, 0.4f, Interp.pow3In), Actions.remove());
						update(() -> {});
					}
				});
				
				addListener(new Tooltip(t -> {
					t.background(Tex.bar);
					t.color.set(Color.black);
					t.color.a = 0.35f;
					t.add("Remain Time: 00:00 ").update(l -> {
						float remain = totalTime - getFloatOrNaN(eventName);
						l.setText("[gray]Remain Time: " + ((remain / Time.toSeconds > 15) ? "[]" : "[accent]") + Mathf.floor(remain / Time.toMinutes) + ":" + Mathf.floor((remain % Time.toMinutes) / Time.toSeconds));
					}).left().fill();
				}));
			}
				
				@Override
				public boolean remove(){
					eventBarTable.updateChildren();
					return super.remove();
				}
			};
			
			eventBarTable.paneTable.row();
			eventBarTable.paneTable.add(t).growX().fillY().padRight(OFFSET);
		}
	}
	
	/**
	 * Search what is Key-Value pair map on the Internet if you cannot understand these things.
	 *
	 * @implNote Make the key to the value(event's data) more formatted.
	 *
	 */
	public static class KeyFormat{
		public static final String SHOW_PREFIX = "<@Show>";
		public static final String SPLITTER = "<@Param>";
		
		/**
		 * Generate a formatted event name for bars saving.
		 *
		 * @param time Use tick format.
		 * */
		public static String generateName(String name, Color color, float time){
			return SHOW_PREFIX + name + SPLITTER + color + SPLITTER + time;
		}
		
		/**
		 * Get the shorted name from a formatted event name.
		 * */
		public static String getEventName(String key){
			String[] s = key.split(SPLITTER);
			return s[0].replaceFirst(SHOW_PREFIX, "");
		}
		
		/**
		 * Get the bar color from a formatted event name.
		 * */
		public static Color getEventColor(String key){
			String[] s = key.split(SPLITTER);
			return s.length < 2 ? Color.white : Color.valueOf(s[1]);
		}
		
		/**
		 * Get the bar color(Hex format) from a formatted event name.
		 * */
		public static String getEventColorHex(String key){
			String[] s = key.split(SPLITTER);
			return s.length < 2 ? Color.white.toString() : s[1];
		}
		
		/**
		 * Get the total time(tick format) from a formatted event name.
		 * */
		public static float getEventTotalTime(String key){
			try{
				String[] s = key.split(SPLITTER);
				return Float.parseFloat(s[s.length - 1]);
			}catch(Exception e){
				return 0;
			}
		}
		
		public static final String
			ENEMY_CORE_DESTROYED_EVENT = "EnemyCoreDestroyedEvent",
			FLEET_RAID_EVENT_00 = "FleetRaidEvent00",
			FLEET_RAID_EVENT_01 = "FleetRaidEvent01",
			FLEET_RAID_EVENT_02 = "FleetRaidEvent02",
			FLEET_RAID_EVENT_03 = "FleetRaidEvent03";
	}
	
	public static void handleTagData(){
		if(state.rules.tags == null || state.rules.tags.isEmpty())return;
		
		if(net.server()){
			TagPacket packet = new TagPacket();
			packet.tags = state.rules.tags;
			Vars.net.send(packet, true);
		}
	}
	
	public static class TagPacket extends Packet{
		private byte[] DATA;
		public StringMap tags;
		
		public TagPacket() {
			this.DATA = NODATA;
		}
		
		public void write(Writes WRITE) {
			String[][] strings = new String[tags.size][2];
			
			int i = 0;
			for(ObjectMap.Entry<String, String> entry : tags.entries()){
				strings[i][0] = entry.key;
				strings[i][1] = entry.value;
				i++;
			}
			
			TypeIO.writeStrings(WRITE, strings);
		}
		
		public void read(Reads READ, int LENGTH) {
			this.DATA = READ.b(LENGTH);
		}
		
		public void handled() {
			BAIS.setBytes(this.DATA);
			
			String[][] strings = TypeIO.readStrings(READ);

			tags = new StringMap();
			for(String[] kv : strings){
				tags.put(kv[0], kv[1]);
			}
		}
		
		public int getPriority(){
			return priorityLow;
		}
		
		public void handleClient(){
			state.rules.tags = tags;
		}
	}
	
	//TODO make events can be skipped but acting necessary parts
	public interface ImportantAction{
		void accept();
	}
	
	/**
	 * Used to make a pop-up dialog with texts extending out.
	 * Use {@code "@@@"} to split the text. The text before it will always be shown, while the latter will have a fade in effect.
	 */
	public static class LabelAction extends TemporalAction{
		public float margin = 0;
		public String text;
		public Label label;
		public Table table;
		public Cons<Table> modifier = null;
		
		public Interp inFunc = NHInterp.bounce5Out;
		
		@Override
		protected void begin(){
			Sounds.press.play(10);
			
			label = new Label("");
			label.setWrap(true);
			
			table = new Table(Tex.buttonEdge3){{
				Core.scene.root.addChild(this);
				color.a = 0;
				
				if(Vars.mobile){
					setSize(Core.graphics.getWidth() / 1.25f, Core.graphics.getHeight() / 3f);
					setPosition(0, 0);
				}else{
					setSize(Core.graphics.getWidth() / 2f, UIActions.yAxis() / 2);
					x = (Core.graphics.getWidth() - width) / 2f;
					y = UIActions.yAxis() * 1.15f;
				}
				
				update(() -> {
					if(state.isMenu())remove();
					if(Vars.mobile){
						setSize(Core.graphics.getWidth() / 1.25f, Core.graphics.getHeight() / 3f);
						setPosition(0, 0);
					}else{
						setSize(Core.graphics.getWidth() / 2f, UIActions.yAxis() / 2);
						x = (Core.graphics.getWidth() - width) / 2f;
						y = UIActions.yAxis() * 1.15f;
					}
				});
				
				table(inner -> {
					if(Vars.mobile){
						inner.table(table1 -> {
							table1.left();
							if(modifier != null)modifier.get(table1);
						}).fillY().growX().row();
						inner.pane(t -> {
							t.add(label).left().top().growX().fillY().pad(OFFSET).padRight(OFFSET).row();
							t.table(c -> {}).grow();
						}).grow().top();
					}else{
						if(modifier != null)modifier.get(inner);
						inner.add(label).center().grow();
					}
				}).grow().pad(OFFSET);
			}};
			
			table.actions(Actions.alpha(1, 0.45f, inFunc));
		}
		
		@Override
		protected void end(){
			table.actions(Actions.fadeOut(0.45f), Actions.remove());
		}
		
		@Override
		protected void update(float percent){
			String[] s = text.split("@@@");
			String speaker = s[0];
			StringBuilder saying = new StringBuilder();
			for(int i = 1; i < s.length; i++)saying.append(s[i]);
			
			label.setText(speaker + saying.substring(0, (int)(saying.length() * Mathf.curve(percent, 0, margin))));
		}
	}
	
	public static class CautionAction extends TemporalAction{
		public float x, y, size;
		public Color color;
		public Table drawer;
		
		@Override
		protected void update(float percent){
		
		}
		
		@Override
		protected void begin(){
			drawer = new Table(Tex.pane){{
				Core.scene.root.addChildAt(1, this);
				
				update(() -> {
					if(Vars.state.isMenu())remove();
				});
				table().grow();
				setFillParent(true);
				
				color.a = 0;
				actions(Actions.alpha(1, 0.45f, NHInterp.bounce5Out));
			}
				@Override
				public void draw(){
					float width = Core.graphics.getWidth(), height = Core.graphics.getHeight();
					
					Vec2 screenVec = Core.camera.project(Tmp.v1.set(CautionAction.this.x, CautionAction.this.y));
					
					boolean outer = screenVec.x < width * 0.05f || screenVec.y < height * 0.05f || screenVec.x > width * 0.95f || screenVec.y > height * 0.95f;
					
					if(outer)screenVec.clamp(width * 0.05f, height * 0.05f, height * 0.95f, width * 0.95f);
					
					Tmp.c1.set(CautionAction.this.color).lerp(Color.white, Mathf.absin(getTime() * 60f, 5f, 0.4f)).a(color.a);
					Tmp.c2.set(Pal.gray).a(color.a);
					
					float rotationS = 45 + 90 * NHInterp.pow10.apply((getTime() * 4 / getDuration()) % 1);
					float angle = outer ? Angles.angle(width / 2, height / 2, screenVec.x, screenVec.y) - 90 : 0;
					Lines.stroke(9f, Tmp.c2);
					Lines.square(screenVec.x, screenVec.y, size + 3f, rotationS);
					Lines.stroke(3f, Tmp.c1);
					if(outer)Draw.rect(NHContent.pointerRegion, screenVec, size, size, angle);
					Lines.square(screenVec.x, screenVec.y, size + 3f, rotationS);
					
					Lines.stroke(9f, Tmp.c2);
					for(int i : Mathf.signs){
						Lines.line(Math.max(0, i) * width, screenVec.y, screenVec.x + size * i * 2, screenVec.y);
						Lines.line(screenVec.x, Math.max(0, i) * height, screenVec.x, screenVec.y + size * i * 2);
					}
					
					Lines.stroke(3f, Tmp.c1);
					for(int i : Mathf.signs){
						Lines.line(Math.max(0, i) * width, screenVec.y, screenVec.x + size * i * 2, screenVec.y);
						Lines.line(screenVec.x, Math.max(0, i) * height, screenVec.x, screenVec.y + size * i * 2);
					}
					
					
				}
			};
		}
		
		@Override
		protected void end(){
			drawer.actions(Actions.fadeOut(1f), Actions.remove());
		}
	}
	
	public static class CameraTrackerAction extends CameraMoveAction{
		public Position trackTarget;
		
		@Override
		protected void update(float percent){
			Core.camera.position.lerp(trackTarget.getX(), trackTarget.getY(), 0.075f);
		}
	}
	
	public static class CameraMoveAction extends TemporalAction{
		protected float startX, startY;
		protected float endX, endY;
		
		@Override
		protected void begin(){
			if(Mathf.equal(startX, endX) && Mathf.equal(startY, endY))return;
			startX = Core.camera.position.x;
			startY = Core.camera.position.y;
		}
		
		@Override
		protected void update(float percent){
			Core.camera.position.set(startX + (endX - startX) * percent, startY + (endY - startY) * percent);
		}
		
		@Override
		public void reset(){
			super.reset();
		}
		
		public void setPosition(float x, float y){
			endX = x;
			endY = y;
		}
		
		public float getX(){
			return endX;
		}
		
		public void setX(float x){
			endX = x;
		}
		
		public float getY(){
			return endY;
		}
		
		public void setY(float y){
			endY = y;
		}
	}
	
	public static class ImportantRunnableAction extends RunnableAction implements ImportantAction{
		
		@Override
		public void accept(){
			run();
		}
	}
}
