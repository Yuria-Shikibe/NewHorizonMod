package newhorizon.feature.cutscene;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.actions.*;
import arc.scene.event.Touchable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import newhorizon.feature.cutscene.actions.*;
import newhorizon.func.NHInterp;

import static mindustry.Vars.*;
import static newhorizon.ui.TableFunc.LEN;
import static newhorizon.ui.TableFunc.OFFSET;

public class UIActions{
	public static Seq<Action[]> waitingPool = new Seq<>();
	public static Action[] currentActions;
	
	protected static boolean lockInput = false;
	protected static Element actor = new Element();
	protected static Table multiActor = new Table();
	protected static Table defaultFiller;
	protected static Table up = new Table(), down = new Table();
	protected static HUDTable eventBarTable = null;
	
	protected static Table root;
	
	public static float /*Updated*/ width_UTD = 0, height_UTD = 0;
	
	
	public static Table root(){
		return root;
	}
	
	public static void addElement(Element element){
		root.addChildAt(1, element);
	}
	
	public static Table eventTable(){
		return eventBarTable == null ? null : eventBarTable.paneTable;
	}
	
	public static void initHUD(){
		if(root == null)root = new Table(Tex.clear){
			{
				Core.scene.root.addChildAt(2, this);
			}
			
			@Override
			public void act(float delta){
				setSize(Core.graphics.getWidth(), Core.graphics.getHeight());
				setPosition(0, 0);
				
				UIActions.width_UTD = width;
				UIActions.height_UTD = height;
				
				super.act(delta);
				if(Vars.state.isMenu())remove();
			}
			
			@Override
			public boolean remove(){
				boolean b = super.remove();
				root = null;
				return b;
			}
		};
		actor = null;
		actor = new Element();
		
		if(eventBarTable != null)eventBarTable.remove();
		
		Element element = root.find("CutsceneHUD");
		if(element != null) element.remove();
		
		
		if(root.find("CutsceneHUD") == null){
			eventBarTable = new HUDTable();
			
			root.addChildAt(10, eventBarTable);
		}
		
		eventBarTable.setup();
	}
	
	public static void reset(){
		Log.info("Run Reset");
		
		if(eventBarTable == null) return;
		eventBarTable.reset();
		eventBarTable.remove();
		eventBarTable = null;
	}
	
	protected static class HUDTable extends Table{
		public Table paneTable = new Table();
		public ScrollPane pane;
		
		public HUDTable(){
			name = "CutsceneHUD";
			
			update(() -> {
				if(state.isMenu()) remove();
				visible(() -> ui.hudfrag.shown);
				
				setSize(width_UTD / 4f, height_UTD / 4f);
				setPosition(Core.settings.getInt("eventbarsoffsetx", 0) / 100f * width, Core.settings.getInt("eventbarsoffsety", 0) / 100f * height);
			});
			
			background(Tex.buttonEdge3);
			
			pane = pane(t -> {
				paneTable = t;
				
				Prov<Table> constructor = () -> {
					Cell<Table> table = t.table(i -> {
						i.image().color(Color.gray).growX().height(OFFSET / 4).pad(OFFSET / 4);
						i.add(Iconc.cancel + "  " + Core.bundle.get("nh.cutscene.empty-event")).color(Color.gray).padLeft(OFFSET).padRight(OFFSET).fill();
						i.image().color(Color.gray).growX().height(OFFSET / 4).pad(OFFSET / 4);
					}).growX().fillY().center();
					
					table.row();
					
					Table i = table.get();
					i.color.a(0);
					i.actions(Actions.fadeIn(0.1f));
					return i;
				};
				
				Table[] element = {constructor.get()};
				
				t.update(() -> {
					if(t.getChildren().size > 1) element[0].remove();
					else if(!t.hasChildren())element[0] = constructor.get();
				});
				
				t.top().row();
				t.defaults().growX().fillY();
			}).grow().pad(OFFSET).get();
			
			pane.setFadeScrollBars(true);
			pane.setupFadeScrollBars(0.15f, 0.25f);
			
			exited(() -> getScene().unfocus(this));
		}
		
		public void updateChildren(){
			//				childrenChanged();
		}
		
		public void addElement(Element element){
			paneTable.row();
			paneTable.add(element).row();
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
			
			paneTable.row();
		}
	}
	
	//TODO actions that will be acted while skipping a cutscene.
	public static ImportantRunnableAction necessaryRun(Runnable runnable){
		ImportantRunnableAction action = Actions.action(ImportantRunnableAction.class, ImportantRunnableAction::new);
		action.setRunnable(runnable);
		return action;
	}
	
	/**
	 * @param duration Use second format.
	 * @param x        Use *8 format.
	 * @param y        Use *8 format.
	 *
	 * @implNote ProvSet an identifier for the target point on the HUD.
	 */
	public static CautionAction cautionAt(float x, float y, float size, float duration, Color color){
		CautionAction action = Actions.action(CautionAction.class, CautionAction::new);
		action.setDuration(duration);
		action.x = x;
		action.y = y;
		action.size = size * 6;
		action.color = color;
		action.style = CautionAction.MarkStyles.defaultStyle;
		return action;
	}
	
	public static CautionAction customCautionAt(float x, float y, float size, float duration, Color color, CautionAction.MarkStyles style){
		CautionAction action = Actions.action(CautionAction.class, CautionAction::new);
		action.setDuration(duration);
		action.x = x;
		action.y = y;
		action.size = size * 6;
		action.color = color;
		action.style = style;
		return action;
	}
	
	/**
	 * @param duration Use second format.
	 *
	 * @implNote ProvSet the camera on a certain target.
	 */
	public static CameraTrackerAction track(Position target, float duration){
		CameraTrackerAction action = Actions.action(CameraTrackerAction.class, CameraTrackerAction::new);
		action.trackTarget = target;
		action.setDuration(duration);
		return action;
	}
	
	/**
	 * @param duration      Use second format.
	 * @param x             Use *8 format.
	 * @param y             Use *8 format.
	 * @param interpolation Move animation curve.
	 *
	 * @implNote Make the camera slide to a certain position.
	 */
	public static CameraMoveAction moveTo(float x, float y, float duration, Interp interpolation){
		CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
		action.setPosition(x + Mathf.random(0.01f), y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}
	
	/**
	 * @param duration Use second format.
	 * @param x        Use *8 format.
	 * @param y        Use *8 format.
	 *
	 * @implNote Make the camera fixed on a certain position.
	 */
	public static CameraMoveAction holdCamera(float x, float y, float duration){
		CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
		action.startX = action.endX = x;
		action.startY = action.endY = y;
		action.setDuration(duration);
		return action;
	}
	
	/**
	 * @param duration     The time text generating uses. Use second format.
	 * @param holdDuration The time text keeping showing. Use second format.
	 *
	 * @implNote Pop up a text dialog on your screen.
	 */
	public static LabelAction labelAct(String text, float duration, float holdDuration){
		LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
		action.setDuration(duration + holdDuration);
		action.margin = Mathf.clamp(duration / (action.getDuration()));
		action.text = text;
		return action;
	}
	
	/**
	 * @param duration     The time text generating uses. Use second format.
	 * @param holdDuration The time text keeping showing. Use second format.
	 * @param inFunc       Animation curve the dialog fade in.
	 * @param outFunc      Animation curve the dialog fade out.
	 * @param modifier     Modifies the dialog.
	 *
	 * @implNote Pop up a text dialog on your screen.
	 */
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
	 * @param duration      The time text generating uses. Use second format.
	 * @param holdDuration  The time text keeping showing. Use second format.
	 * @param interpolation Animation curve the dialog fade in.
	 *
	 * @implNote Pop up a text dialog on your screen.
	 */
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
		lockInput = true;
	}
	
	/** Make camera follow player on desktop; make player follow camera on phones. */
	public static void resumeCamera(){
		lockInput = false;
	}
	
	/** Generate a table that fill the screen. */
	public static Table filler(Runnable removed, Runnable update, boolean removeShowUI){
		return new Table(Tex.clear){
			{
				root.addChild(this);
				
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
				if(Vars.state.isMenu()) remove();
			}
			
			@Override
			public boolean remove(){
				if(removed != null) removed.run();
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
	 */
	public static boolean actionSeq(Action... actions){
		boolean isPlaying = CutsceneScript.isPlayingCutscene && currentActions == null;
		
		Action[] acts = new Action[actions.length + 1];
		System.arraycopy(actions, 0, acts, 0, actions.length);
		acts[acts.length - 1] = Actions.remove();
		
		if(!isPlaying){
			CutsceneScript.isPlayingCutscene = true;
			currentActions = acts;
			actor = new Element(){
				@Override
				public void act(float delta){
					super.act(delta);
					if(Vars.state.isMenu()){
						actor.getActions().clear();
						actor.remove();
						actor = null;
						waitingPool.clear();
						actor = new Element();
					}
				}
				
				@Override
				public boolean remove(){
					currentActions = null;
					
					if(waitingPool.any()){
						Time.run(60f, () -> {
							if(!state.isPlaying())return;
							CutsceneScript.isPlayingCutscene = false;
							if(!waitingPool.isEmpty())actionSeq(waitingPool.pop());
						});
					}else CutsceneScript.isPlayingCutscene = false;
					
					return super.remove();
				}
			};
			
			actor.actions(acts);
		}else{
			waitingPool.add(actions);
		}
		
		return isPlaying;
	}
	
	public static boolean actionSeqMinor(Action... actions){
		boolean isPlaying = CutsceneScript.isPlayingCutscene;
		
		Action[] acts = new Action[actions.length + 1];
		System.arraycopy(actions, 0, acts, 0, actions.length);
		acts[acts.length - 1] = Actions.remove();
		
		if(!isPlaying){
			multiActor.add(new Element(){
				{
					actions(acts);
				}
				
				@Override
				public void act(float delta){
					super.act(delta);
					if(Vars.state.isMenu()){
						clearActions();
						clearListeners();
						remove();
					}
				}
			});
		}else{
			Time.run(30f, () -> {
				if(state.isMenu())return;
				actionSeqMinor(actions);
			});
		}
		
		return isPlaying;
	}
	
	/**
	 * Pull out the black curtain from the upper and lower sides of the screen.
	 */
	public static Action curtainIn(float time, Interp func){
		return Actions.run(() -> {
			disableVanillaUI();
			down = new Table(Styles.black){
				{
					root.addChild(this);
					
					setPosition(0, -yAxis());
					
					update(() -> setSize(Core.graphics.getWidth(), yAxis()));
					
					actions(Actions.moveTo(0, 0, time, func), Actions.run(() -> update(() -> {
						disableVanillaUI();
						setSize(Core.graphics.getWidth(), yAxis());
						setPosition(0, 0);
					})));
				}
				
				@Override
				public boolean remove(){
					
					return super.remove();
				}
				
				@Override
				public void act(float delta){
					super.act(delta);
					if(state.isMenu()) remove();
				}
			};
			
			up = new Table(Styles.black){
				{
					root.addChild(this);
					
					setPosition(0, height_UTD);
					
					update(() -> setSize(Core.graphics.getWidth(), yAxis()));
					
					actions(Actions.moveTo(0, height_UTD - yAxis(), time, func), Actions.run(() -> update(() -> {
						setSize(Core.graphics.getWidth(), yAxis());
						setPosition(0, height_UTD - yAxis());
					})));
				}
				
				@Override
				public void act(float delta){
					super.act(delta);
					if(state.isMenu()) remove();
				}
			};
		});
	}
	
	/**
	 * Remove the black curtain from the upper and lower sides of the screen.
	 */
	public static Action curtainOut(float time, Interp func){
		return Actions.run(() -> {
			if(up == null || down == null) return;
			
			down.actions(Actions.run(() -> down.update(() -> {
				disableVanillaUI();
				down.setSize(Core.graphics.getWidth(), yAxis());
			})), Actions.moveTo(0, -yAxis(), time, func), Actions.parallel(Actions.remove(), Actions.run(() -> {
				down.update(() -> {});
				enableVanillaUI();
			})));
			
			up.actions(Actions.run(() -> up.update(() -> {
				up.setSize(Core.graphics.getWidth(), yAxis());
			})), Actions.moveTo(0, height_UTD, time, func), Actions.remove());
		});
	}
	
	/**
	 * Pull out the black curtain from the upper and lower sides of the screen. Make camera stop following player on
	 * desktop; make player stop following camera on phones. Hide vanilla UI.
	 */
	public static Action startCutsceneDefault(){
		return Actions.sequence(Actions.parallel(Actions.delay(2f), UIActions.curtainIn(2f, Interp.pow2Out)), Actions.run(UIActions::pauseCamera));
	}
	
	/**
	 * Remove the black curtain from the upper and lower sides of the screen. Make camera follow player on desktop; make
	 * player follow camera on phones. Show vanilla UI.
	 */
	public static Action endCutsceneDefault(){
		return headless ? Actions.run(() -> {}) : Actions.parallel(moveTo(player.x, player.y, 0.5f, Interp.pow3), Actions.run(UIActions::resumeCamera), UIActions.curtainOut(1f, Interp.pow2In));
	}
	
	public static float yAxis(){return headless ? 0 : Core.graphics.getHeight() / 8f;}
	
	public static boolean shown(){return !Vars.state.isMenu();}
	
	/** Hide vanilla UI. */
	public static void disableVanillaUI(){if(!headless)Vars.ui.hudfrag.shown = false;}
	
	/** Show vanilla UI. */
	public static void enableVanillaUI(){if(!headless)Vars.ui.hudfrag.shown = true;}
	
	/**
	 * Add an event bar after 0.75sec.
	 *
	 * @param totalTime Uses tick format
	 */
	public static void reloadBarDelay(String eventName, float totalTime, Color color){
		reloadBarDelay(KeyFormat.generateName(eventName, color, totalTime), totalTime, () -> eventName, () -> color);
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
	 * <p>
	 * Bars will automatically remove itself if there is no data of the event. You can use method {@code
	 * setReload(String key, float time)} to set the data before calling a bar.
	 *
	 * @param totalTime Uses tick format
	 */
	public static void reloadBar(String eventName, float totalTime, Prov<CharSequence> showName, Prov<Color> showColor){
		if(headless || !state.rules.tags.containsKey(eventName)) return;
		
		Table t = new Table(Tex.clear){
			{
				add(new Bar(showName, showColor, () -> Float.parseFloat(state.rules.tags.get(eventName)) / totalTime)).growX().height(LEN - OFFSET).padBottom(OFFSET);
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
						float remain = totalTime - CutsceneScript.getFloatOrNaN(eventName);
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
	
	public static void showLabel(float duration, Cons<Table> modifier){
		Table table = new Table(){{
			touchable = Touchable.disabled;
			update(() -> {
				if(state.isMenu())remove();
				setWidth(width_UTD);
				setPosition(0, (height_UTD - height) / 2);
			});
			color.a(0);
			actions(Actions.fadeIn(0.45f, NHInterp.bounce5Out), Actions.delay(duration), Actions.fadeOut(0.5f), Actions.remove());
		}}.margin(4);
		
		modifier.get(table);
		
		table.pack();
		table.act(0f);
		//make sure it's at the back
		root.addChildAt(0, table);
	}
}
