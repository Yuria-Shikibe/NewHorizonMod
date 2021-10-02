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
import arc.scene.actions.Actions;
import arc.scene.actions.RunnableAction;
import arc.scene.actions.TemporalAction;
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
import mindustry.input.MobileInput;
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
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHStatusEffects;
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
	public static final Seq<Runnable> curUpdaters = new Seq<>(), curIniter = new Seq<>();
	public static final Seq<Cons<Boolean>> curEnder = new Seq<>();
	
	public static final ObjectMap<Block, Cons<Building>> blockDestroyListener = new ObjectMap<>();
	
	public static boolean isPlayingCutscene = false;
	
	public static void addListener(Block type, Cons<Building> actor){
		blockDestroyListener.put(type, actor);
	}
	
	public static void addListener(Seq<Block> types, Cons<Building> actor){
		for(Block type : types)addListener(type, actor);
	}
	
	public static Interval timer = new Interval(6);
	
	public static boolean initHasRun = false;
	
	
	private static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();
	
	static{
//		Events.on(EventType.UnitDestroyEvent.class, e -> {
//
//		});
		
//		Events.on(EventType.PlayerJoin.class, e -> {
//			Log.info("EventType.PlayerJoin");
//			handleTagData();
//		});
		
//		Events.on(EventType.ConnectionEvent.class, e -> {
//			Log.info("EventType.ConnectionEvent");
//			handleTagData();
//		});
		
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
			if(!Vars.state.isPlaying())return;
			if(timer.get(0, 900))handleTagData();
			if(curUpdaters.any())curUpdaters.each(Runnable::run);
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
	
	public static void initDirectory(){
		scriptDirectory = new Fi(scriptDirectoryPath());
		if (!scriptDirectory.exists()) {
			scriptDirectory.mkdirs();
		}
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
	
	public static void reset(){
		curSectorPreset = null;
		currentScriptFile = null;
		initHasRun = false;
		curEnder.clear();
		curIniter.clear();
		curUpdaters.clear();
		timer.clear();
		blockDestroyListener.clear();
		UIActions.waitingPool.clear();
		
		TableFunc.textArea.clearText();
	}
	
	public static Fi getModGlobalJS(){
		return mod.root.child("scripts").child("cutsceneLoader.js");
	}
	
	public static String getModGlobalJSCode(){
		return getModGlobalJS().readString();
	}
	
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
	
	public static void init(SectorPreset sector){
		reset();
		
		UIActions.init();
		
		curSectorPreset = sector;
		
		if(sector == null){
			Core.app.post(() -> {
				runJS(getScript(Vars.state.map));
				curIniter.each(Runnable::run);
			});
		}else{
			if(updaters.containsKey(sector)){
				curUpdaters.addAll(updaters.get(sector));
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
	
	public static boolean runEventOnce(String eventName, Runnable run){
		boolean hasRun = false;
		if(!state.rules.tags.containsKey(eventName) || !Boolean.parseBoolean(state.rules.tags.get(eventName))){
			run.run();
			hasRun = true;
		}
		
		state.rules.tags.put(eventName, "true");
		
		return hasRun;
	}
	
	public static boolean runEventMulti(String eventName, int times, Runnable run){
		int num = 0;
		
		boolean hasRun = false;
		
		if(state.rules.tags.containsKey(eventName)){
			num = Integer.parseInt(state.rules.tags.get(eventName));
			if(num < times){
				run.run();
				hasRun = true;
			}
		}
		
		state.rules.tags.put(eventName, String.valueOf(++num));
		Log.info(state.rules.tags.get(eventName));
		
		return hasRun;
	}
	
	public static void setReload(String eventName, float time){
		state.rules.tags.put(eventName, String.valueOf(time));
	}
	
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
	}
	
	/**
	 * Used to make a pop-up dialog with texts extending out.
	 */
	public static class LabelAction extends TemporalAction{
		public float margin = 0;
		public String text;
		public Label label;
		public Table table;
		public Cons<Table> modifier = null;
		
		public Interp inFunc = NHInterp.bounceOut5;
		
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
				actions(Actions.alpha(1, 0.45f, NHInterp.bounceOut5));
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
	
	public static class UIActions{
		public static Seq<Action[]> waitingPool = new Seq<>();
		public static Action[] currentActions;
		
		public static void init(){
			if(Core.scene.root.find("CutsceneHUD") == null){
				reloadBarTable = new HUDTable();
				
				Core.scene.root.addChildAt(1, reloadBarTable);
			}
			
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
		
		public static void reset(){
			Log.info("Run Reset");
			
			if(reloadBarTable == null)return;
			reloadBarTable.reset();
			reloadBarTable.remove();
			reloadBarTable = null;
		}
		
		protected static Table actor;
		protected static Table defaultFiller = filler();
		protected static Table up = new Table(), down = new Table();
		protected static HUDTable reloadBarTable = null;
		
		protected static class HUDTable extends Table{
			public Table paneTable = new Table();
			public ScrollPane pane;
			
			public HUDTable(){
				name = "CutsceneHUD";
				
				update(() -> {
					if(state.isMenu())remove();
					visible(() -> ui.hudfrag.shown);
					
					setSize(Core.graphics.getWidth() / 4f, Core.graphics.getHeight() / 4.45f);
					setPosition(0, 0);
				});
				
				background(Tex.buttonEdge3);
				
				pane = pane(t -> {
					paneTable = t;
					t.top();
				}).grow().pad(OFFSET).get();
			}
			
			public void updateChildren(){
//				childrenChanged();
			}
			
			public void addElement(Element element){
				paneTable.add(element);
			}
		}
		
		public static ImportantRunnableAction necessaryRun(Runnable runnable){
			ImportantRunnableAction action = Actions.action(ImportantRunnableAction.class, ImportantRunnableAction::new);
			action.setRunnable(runnable);
			return action;
		}
		
		public static CautionAction cautionAt(float x, float y, float size, float duration, Color color){
			CautionAction action = Actions.action(CautionAction.class, CautionAction::new);
			action.setDuration(duration);
			action.x = x;
			action.y = y;
			action.size = size * 6;
			action.color = color;
			return action;
		}
		
		public static CameraTrackerAction track(Position target, float duration){
			CameraTrackerAction action = Actions.action(CameraTrackerAction.class, CameraTrackerAction::new);
			action.trackTarget = target;
			action.setDuration(duration);
			return action;
		}
		
		public static CameraMoveAction moveTo(float x, float y, float duration, Interp interpolation){
			CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
			action.setPosition(x, y);
			action.setDuration(duration);
			action.setInterpolation(interpolation);
			return action;
		}
		
		public static CameraMoveAction holdCamera(float x, float y, float duration){
			CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
			action.startX = action.endX = x;
			action.startY = action.endY = y;
			action.setDuration(duration);
			return action;
		}
		public static LabelAction labelAct(String text, float duration, float holdDuration){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.text = text;
			return action;
		}
		
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
		
		public static LabelAction labelAct(String text, float duration, float holdDuration, Interp interpolation, Cons<Table> modifier){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.setInterpolation(interpolation);
			action.text = text;
			action.modifier = modifier;
			return action;
		}
		
		/** Prevent the camera following player during the script. */
		public static void pauseCamera(){
			if(headless)return;
			if(Vars.mobile && Vars.control.input instanceof MobileInput){
				MobileInput input = (MobileInput)Vars.control.input;
				actor = new Table(Tex.clear){{
					setSize(1, 1);
					update(() -> {
						if(state.isMenu())remove();
						player.unit().vel.setZero();
					});
					Core.scene.root.addChild(this);
				}};
			}else if(Vars.control.input instanceof DesktopInput){
				DesktopInput input = (DesktopInput)Vars.control.input;
				input.panning = true;
			}
		}
		
		/** Release the camera. */
		public static void resumeCamera(){
			if(headless)return;
			if(Vars.mobile && Vars.control.input instanceof MobileInput){
				MobileInput input = (MobileInput)Vars.control.input;
				Vars.player.unit().unapply(NHStatusEffects.staticVel);
				Core.camera.position.set(Vars.player);
				
				if(actor != null)Core.scene.root.removeChild(actor);
			}else if(Vars.control.input instanceof DesktopInput){
				DesktopInput input = (DesktopInput)Vars.control.input;
				input.panning = false;
			}
			
			Core.camera.update();
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
		
		/** Add ordered scripts. */
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
		
		/** Used to create black frame protruding inward. Just like the effect in Game:Homeworld */
		public static Table screenHold(float riseTime, float hangTime, float fallTime, Interp rise, Interp fall){
			return screenHold(riseTime, hangTime, fallTime, rise, fall, riseTime + fallTime + hangTime);
		}
		
		public static Table screenHold(float riseTime, float hangTime, float fallTime, Interp rise, Interp fall, float waitTime){
			Table filler = filler(null, UIActions::disableVanillaUI, true);
			
			for(int i : Mathf.signs){
				Table hold = new Table(Styles.black){{
					setPosition(0, yAxis() * i + Mathf.num(i > 0) * Core.graphics.getHeight());
					
					update(() -> {
						setSize(Core.graphics.getWidth(), yAxis());
					});
					
					filler.add(this);
					
					actions(Actions.moveTo(0, Mathf.num(i > 0) * (Core.graphics.getHeight() - yAxis()), riseTime, rise), Actions.delay(hangTime), Actions.moveTo(0, yAxis() * i + Mathf.num(i > 0) * Core.graphics.getHeight(), fallTime, fall), Actions.parallel(Actions.run(UIActions::enableVanillaUI), Actions.run(filler::remove), Actions.remove()));
				}};
			}
			
			return filler;
		}
		
		public static float yAxis(){
			return Core.graphics.getHeight() / 8f;
		}
		
		public static boolean shown(){
			return !Vars.state.isMenu();
		}
		
		public static void disableVanillaUI(){
			Vars.ui.hudfrag.shown = false;
		}
		
		public static void enableVanillaUI(){
			Vars.ui.hudfrag.shown = true;
		}
		
		public static void reloadBarDelay(String eventName, float totalTime, Color color){
			reloadBarDelay(KeyFormat.generateName(eventName, color, totalTime),totalTime, () -> eventName, () -> color);
		}
		
		public static void reloadBarDelay(String eventFullName, float totalTime, Prov<CharSequence> showName, Prov<Color> showColor){
			Time.runTask(15f, () -> reloadBar(eventFullName, totalTime, showName, showColor));
		}
		
		public static void reloadBar(String eventName, float totalTime, Prov<CharSequence> showName, Prov<Color> showColor){
			if(!state.rules.tags.containsKey(eventName))return;
			
			Table t = new Table(Tex.clear){{
				add(new Bar(
					showName, showColor, () -> Float.parseFloat(state.rules.tags.get(eventName)) / totalTime
				)).growX().height(LEN - OFFSET).padBottom(OFFSET);
				update(() -> {
					if(!state.rules.tags.containsKey(eventName)){
						actions(Actions.sizeTo(0, height, 0.4f, Interp.pow3In), Actions.remove());
						update(() -> {});
					}
				});
				
				addListener(new Tooltip(t -> {
					t.background(Tex.buttonEdge3).add("Remain Time: 00:00").update(l -> {
						float remain = totalTime - Float.parseFloat(state.rules.tags.get(eventName));
						l.setText("[gray]Remain Time: " + ((remain / Time.toSeconds > 15) ? "[]" : "[accent]") + Mathf.floor(remain / Time.toMinutes) + ":" + Mathf.floor((remain % Time.toMinutes) / Time.toSeconds));
					}).fill();
				}));
			}
				
				@Override
				public boolean remove(){
					reloadBarTable.updateChildren();
					return super.remove();
				}
			};
			
			reloadBarTable.paneTable.row();
			reloadBarTable.paneTable.add(t).growX().fillY().padRight(OFFSET);
		}
	}
	
	public static class KeyFormat{
		public static final String SHOW_PREFIX = "<@Show>";
		public static final String SPLITTER = "<@Param>";
		
		public static String generateName(String name, Color color, float time){
			return SHOW_PREFIX + name + SPLITTER + color + SPLITTER + time;
		}
		
		public static String getEventName(String key){
			String[] s = key.split(SPLITTER);
			return s[0].replaceFirst(SHOW_PREFIX, "");
		}
		
		public static Color getEventColor(String key){
			String[] s = key.split(SPLITTER);
			return s.length < 2 ? Color.white : Color.valueOf(s[1]);
		}
		
		public static String getEventColorHex(String key){
			String[] s = key.split(SPLITTER);
			return s.length < 2 ? Color.white.toString() : s[1];
		}
		
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
	
	public interface ImportantAction{
		void accept();
	}
}
