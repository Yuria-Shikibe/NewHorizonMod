package newhorizon.feature;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.func.Boolf;
import arc.func.Cons;
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
import arc.scene.actions.Actions;
import arc.scene.actions.TemporalAction;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.editor.MapEditorDialog;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.input.MobileInput;
import mindustry.maps.Map;
import mindustry.mod.Mods;
import mindustry.mod.Scripts;
import mindustry.type.Sector;
import mindustry.type.SectorPreset;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHStatusEffects;
import newhorizon.func.NHInterp;

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
	
	static{
//		Events.on(EventType.UnitDestroyEvent.class, e -> {
//
//		});
		
		Events.on(EventType.BlockDestroyEvent.class, e -> {
			if(e.tile.build == null || !blockDestroyListener.containsKey(e.tile.build.block()))return;
			
			blockDestroyListener.get(e.tile.build.block).get(e.tile.build);
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if((e.from == GameState.State.playing && e.to == GameState.State.menu)){
				reset();
			}
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(initHasRun)return;
			init(Vars.state.getSector() == null ? null : Vars.state.getSector().preset);
		});
		
		Events.run(EventType.Trigger.update, () -> {
			if(!Vars.state.isPlaying())return;
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
			Fi js = scriptDirectory.child(map.name() + "-cutscene.js");
			if(!js.exists()){
				try{
					Log.info("Tried Create Script File: " + js.file().createNewFile() + " | " + js.file().getAbsolutePath());
				}catch(IOException e){
					Vars.ui.showErrorMessage(e.toString());
				}
			}
			return js.readString();
		}else return null;
	}
	
	public static void reset(){
		curSectorPreset = null;
		initHasRun = false;
		curEnder.clear();
		curIniter.clear();
		curUpdaters.clear();
		timer.clear();
		blockDestroyListener.clear();
		UIActions.waitingPool.clear();
	}
	
	public static void runJs(String js){
		Core.app.post(() -> {
			if(js == null || js.isEmpty())return;
			
			try{
				Class<? extends Scripts> scriptsClass = scripts.getClass();
				
				Method method = scriptsClass.getDeclaredMethod("run", String.class, String.class, boolean.class);
				Field field = scriptsClass.getDeclaredField("currentMod");
				
				method.setAccessible(true);
				field.setAccessible(true);
				
				field.set(scripts, mod);
				method.invoke(scripts, mod.root.child("scripts").child("cutsceneLoader.js").readString() + js, state.map.name(), true);
				
				curIniter.each(Runnable::run);
			}catch(Exception e){
				Vars.ui.showErrorMessage(e.toString());
			}
		});
	}
	
	public static void init(SectorPreset sector){
		reset();
		
		if(Vars.net.active())return;
		
		curSectorPreset = sector;
		
		if(sector == null){
			runJs(getScript(Vars.state.map));
			//			scripts.run(Vars.mods.getMod(NewHorizon.class), js);
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
	
	public static boolean canInit(Sector sector){
		boolean b = !initHasRun && sector.save == null;
		initHasRun = true;
		return b;
	}
	
	public static boolean canInit(){
		boolean b = !state.rules.tags.containsKey("inited") || !Boolean.parseBoolean(state.rules.tags.get("inited"));
		state.rules.tags.put("inited", "true");
		initHasRun = true;
		return b;
	}
	
	public static void run(String key, Boolf<String> boolf, Runnable run){
		if(state.rules.tags.containsKey(key) && boolf.get(state.rules.tags.get(key))){
			run.run();
		}
	}
	
	public static void runEventOnce(String eventName, Runnable run){
		if(!state.rules.tags.containsKey(eventName) || !Boolean.parseBoolean(state.rules.tags.get(eventName))){
			run.run();
		}
		
		state.rules.tags.put(eventName, "true");
	}
	
	public static void runEventMulti(String eventName, int times, Runnable run){
		int num = 0;
		
		if(state.rules.tags.containsKey(eventName)){
			num = Integer.parseInt(state.rules.tags.get(eventName));
			if(num < times)run.run();
		}
		
		state.rules.tags.put(eventName, String.valueOf(++num));
		Log.info(state.rules.tags.get(eventName));
	}
	
	public static void setReload(String eventName, float time){
		state.rules.tags.put(eventName, String.valueOf(time));
	}
	
	public static void reload(String eventName, float speed, float reloadTime, Runnable run){
		if(state.rules.tags.containsKey(eventName)){
			float time = Float.parseFloat(state.rules.tags.get(eventName));
			time += speed;
			if(time > reloadTime){
				setReload(eventName, 0);
				run.run();
			}else setReload(eventName, time);
		}else setReload(eventName, 0);
	}
	
	/**
	 * Used to make a pop-up dialog with texts extending out.
	 */
	public static class LabelAction extends TemporalAction{
		public boolean hasNext = false;
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
					setPosition(0, UIActions.yAxis());
				}else{
					setSize(Core.graphics.getWidth() / 2f, UIActions.yAxis() / 2);
					x = (Core.graphics.getWidth() - width) / 2f;
					y = UIActions.yAxis() * 1.15f;
				}
				
				update(() -> {
					if(state.isMenu())remove();
					if(Vars.mobile){
						setSize(Core.graphics.getWidth() / 1.25f, Core.graphics.getHeight() / 3f);
						setPosition(0, UIActions.yAxis());
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
			if(!hasNext)table.actions(Actions.fadeOut(0.45f), Actions.remove());
			else table.addAction(Actions.remove());
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
	
	public static class UIActions{
		public static Seq<Action[]> waitingPool = new Seq<>();
		
		protected static Table defaultFiller = filler();
		protected static Table up = new Table(), down = new Table();
		
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
		public static LabelAction labelAct(String text, float duration, float holdDuration, boolean hasNext){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.hasNext = hasNext;
			action.text = text;
			return action;
		}
		
		public static LabelAction labelAct(String text, float duration, float holdDuration, boolean hasNext, Interp outFunc, Interp inFunc, Cons<Table> modifier){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.setInterpolation(outFunc);
			action.inFunc = inFunc;
			action.hasNext = hasNext;
			action.text = text;
			action.modifier = modifier;
			return action;
		}
		
		public static LabelAction labelAct(String text, float duration, float holdDuration, boolean hasNext, Interp interpolation, Cons<Table> modifier){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + holdDuration);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.setInterpolation(interpolation);
			action.hasNext = hasNext;
			action.text = text;
			action.modifier = modifier;
			return action;
		}
		
		public static float countTime(Action[] actions){
			float time = 0;
			
			for(Action action : actions){
				if(!(action instanceof TemporalAction)) continue;
				time += ((TemporalAction)action).getDuration();
			}
			
			return time;
		}
		
		/** Prevent the camera following player during the script. */
		public static void pauseCamera(){
			if(Vars.mobile && Vars.control.input instanceof MobileInput){
				MobileInput input = (MobileInput)Vars.control.input;
				Vars.player.unit().apply(NHStatusEffects.staticVel, 100000);
			}else if(Vars.control.input instanceof DesktopInput){
				DesktopInput input = (DesktopInput)Vars.control.input;
				input.panning = true;
			}
		}
		
		/** Release the camera. */
		public static void resumeCamera(){
			if(Vars.mobile && Vars.control.input instanceof MobileInput){
				MobileInput input = (MobileInput)Vars.control.input;
				Vars.player.unit().unapply(NHStatusEffects.staticVel);
				Core.camera.position.set(Vars.player);
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
			acts[acts.length - 1] = Actions.remove();
			
			if(!isPlaying){
				isPlayingCutscene = true;
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
				}};
				
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
				}};
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
	}
	
	public static class CommonEventNames{
		public static final String
				ENEMY_CORE_DESTROYED_EVENT = "EnemyCoreDestroyedEvent";
	}
}
