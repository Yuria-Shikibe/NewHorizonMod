package newhorizon.feature;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
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
import mindustry.game.EventType;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.input.DesktopInput;
import mindustry.input.MobileInput;
import mindustry.type.Sector;
import mindustry.type.SectorPreset;
import mindustry.ui.Styles;
import newhorizon.content.NHStatusEffects;
import newhorizon.func.NHInterp;

public class SectorScript{
	public static long lastToast = 0;
	public static final ObjectMap<SectorPreset, Seq<Runnable>> updaters = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Runnable>> initer = new ObjectMap<>(6);
	public static final ObjectMap<SectorPreset, Seq<Cons<Boolean>>> ender = new ObjectMap<>(6); // true -> win, false -> lose
	
	public static @Nullable SectorPreset curSectorPreset = null;
	public static final Seq<Runnable> curUpdaters = new Seq<>(), curIniter = new Seq<>();
	public static final Seq<Cons<Boolean>> curEnder = new Seq<>();
	
	public static Interval timer = new Interval(6);
	
	public static boolean initHasRun = false;
	
	static{
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.from == GameState.State.playing && e.to == GameState.State.menu){
				reset();
			}
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			if(initHasRun)return;
			init(Vars.state.getSector() == null ? null : Vars.state.getSector().preset);
			Log.info(curIniter.size);
			curIniter.each(Runnable::run);
		});
		
		Events.run(EventType.Trigger.update, () -> {
			if(curSectorPreset == null || !Vars.state.isPlaying())return;
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
	}
	
	public static void reset(){
		curSectorPreset = null;
		initHasRun = false;
		curEnder.clear();
		curIniter.clear();
		curUpdaters.clear();
		timer.clear();
	}
	
	public static void init(SectorPreset sector){
		reset();
		
		curSectorPreset = sector;
		
		if(sector == null)return;
		
		if(updaters.containsKey(sector)){
			curUpdaters.addAll(updaters.get(sector));
		}
		
		if(initer.containsKey(sector)){
			curIniter.addAll(initer.get(sector));
		}
		
		if(ender.containsKey(sector)){
			curEnder.addAll(ender.get(sector));
		}
	}
	
	public static boolean canInit(Sector sector){
		boolean b = !initHasRun && sector.save == null;
		initHasRun = true;
		return b;
	}
	
	public static class UIActions{
		
		public static float countTime(Action[] actions){
			float time = 0;
			
			for(Action action : actions){
				if(!(action instanceof TemporalAction))continue;
				time += ((TemporalAction)action).getDuration();
			}
			
			return time;
		}
		
		public static void pauseCamera(){
			if(Vars.mobile && Vars.control.input instanceof MobileInput){
				MobileInput input = (MobileInput)Vars.control.input;
				Vars.player.unit().apply(NHStatusEffects.staticVel, 100000);
			}else if(Vars.control.input instanceof DesktopInput){
				DesktopInput input = (DesktopInput)Vars.control.input;
				input.panning = true;
			}
		}
		
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
		
		public static float countTime(Seq<Action> actions){
			float time = 0;
			
			for(Action action : actions){
				if(!(action instanceof TemporalAction))continue;
				time += ((TemporalAction)action).getDuration();
			}
			
			return time;
		}
		
		private static void scheduleToast(float time, Runnable run){
			long duration = Math.max((long)(time * 1000 - 1f), 0);
			long since = Time.timeSinceMillis(lastToast);
			if(since > duration){
				if(duration > 1)lastToast = Time.millis();
				run.run();
			}else{
				Time.runTask((duration - since) / 1000f * 60f, run);
				lastToast += duration;
			}
		}
		
		public static Table filler(Runnable update, boolean removeShowUI){
			return new Table(Tex.clear){{
				Core.scene.root.addChild(this);
				
				update(update);
				setFillParent(true);
				visible(UIActions::shown);
				
				keyDown(k -> {
					if(k == KeyCode.escape)remove();
				});
			}
				@Override
				public void act(float delta){
					super.act(delta);
					if(Vars.state.isMenu())remove();
				}
				
				@Override
				public boolean remove(){
					if(removeShowUI)enableVanillaUI();
					return super.remove();
				}
			};
		}
		
		public static Table filler(){
			return filler(() -> {}, false);
		}
		
		public static Table actionSeq(Action... actions){
			Table filler = filler();
			
			float time = countTime(actions);
			scheduleToast(time, () -> {
				Action[] acts = new Action[actions.length + 1];
				System.arraycopy(actions, 0, acts, 0, actions.length);
				acts[acts.length - 1] = Actions.remove();
				filler.actions(acts);
			});
			
			return filler;
		}
		
		public static Table cameraMove(float toX, float toY, float time, Interp interp){
			Table filler = filler();
			
			scheduleToast(time, () -> {
				filler.actions(Actions.run(() -> Vars.control.pause()), CameraMoveAction.moveTo(toX, toY, time, interp), Actions.run(() -> Vars.control.resume()));
				filler.actions(Actions.delay(time + 1f), Actions.remove());
			});
			
			return filler;
		}
		
		public static Table screenHold(float riseTime, float hangTime, float fallTime, Interp rise, Interp fall){
			return screenHold(riseTime, hangTime, fallTime, rise, fall, riseTime + fallTime + hangTime);
		}
		
		public static Table screenHold(float riseTime, float hangTime, float fallTime, Interp rise, Interp fall, float waitTime){
			Table filler = filler(UIActions::disableVanillaUI, true);
			
			scheduleToast(waitTime, () -> {
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
			});
			
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
		
		public static void setWait(long time){
			lastToast = time;
		}
	}
	
	public static class LabelAction extends TemporalAction{
		public static LabelAction labelAct(String text, float duration, float hold, boolean hasNext){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + hold);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.hasNext = hasNext;
			action.text = text;
			return action;
		}
		
		
		public static LabelAction labelAct(String text, float duration, float hold, boolean hasNext, Interp interpolation, Cons<Table> modifier){
			LabelAction action = Actions.action(LabelAction.class, LabelAction::new);
			action.setDuration(duration + hold);
			action.margin = Mathf.clamp(duration / (action.getDuration()));
			action.setInterpolation(interpolation);
			action.hasNext = hasNext;
			action.text = text;
			action.modifier = modifier;
			return action;
		}
		
		public boolean hasNext = false;
		public float margin = 0;
		public String text;
		public Label label;
		public Table table;
		public Cons<Table> modifier = null;
		
		@Override
		protected void begin(){
			Sounds.press.play(10);
			
			label = new Label("");
			
			table = new Table(Tex.buttonEdge3){{
				Core.scene.root.addChild(this);
				color.a = 0;
				
				update(() -> {
					setSize(Core.graphics.getWidth() / 2f, UIActions.yAxis() / 2);
					x = (Core.graphics.getWidth() - width) / 2f;
					y = UIActions.yAxis() * 1.15f;
				});
				
				if(modifier != null)modifier.get(this);
				
				add(label).center().grow();
			}};
			
			table.actions(Actions.alpha(1, 0.45f, NHInterp.bounceOut5));
			
			
		}
		
		@Override
		protected void end(){
			if(!hasNext)table.actions(Actions.fadeOut(1f), Actions.remove());
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
	
//	public static class CameraZoomAction extends TemporalAction{
//		public Rect originalZoomRect;
//		public Rect toZoom;
//
//		public static CameraZoomAction moveTo(float scl, float duration, Interp interpolation){
//			CameraZoomAction action = Actions.action(CameraZoomAction.class, CameraZoomAction::new);
//			Vars.renderer.resize();
//			action.originalZoomRect = new Rect().setSize(Core.camera.width, Core.camera.height).setCenter(Core.camera.position);
//			action.toZoom = new Rect().setSize(Core.camera.width * scl, Core.camera.height * scl).setCenter(Core.camera.position);
//
//			action.setDuration(duration);
//			action.setInterpolation(interpolation);
//			return action;
//		}
//
//
//		@Override
//		protected void update(float percent){
//			Core.camera.mat.idt().scl(currentZoom + (toZoom - currentZoom) * percent);
//		}
//	}
	
	public static class CautionAction extends TemporalAction{
		public static CautionAction at(float x, float y, float size, float duration, Color color){
			CautionAction action = Actions.action(CautionAction.class, CautionAction::new);
			action.setDuration(duration);
			action.x = x;
			action.y = y;
			action.size = size * 6;
			action.color = color;
			return action;
		}
		
		public float x, y, size;
		public Color color;
		public Table drawer;
		
		@Override
		protected void update(float percent){
		
		}
		
		@Override
		protected void begin(){
			drawer = new Table(Tex.pane){{
				Core.scene.root.addChild(this);
				
				update(() -> {
					if(Vars.state.isMenu())remove();
				});
				table().grow();
				setFillParent(true);
			}
				@Override
				public void draw(){
					Vec2 screenVec = Core.camera.project(Tmp.v1.set(CautionAction.this.x, CautionAction.this.y));
					
					Tmp.c1.set(CautionAction.this.color).lerp(Color.white, Mathf.absin(getTime() * 60f, 3f, 0.1f)).a(color.a);
					Tmp.c2.set(Pal.gray).a(color.a);
					
					float rotationS = 45 + 90 * NHInterp.pow10.apply((getTime() * 4 / getDuration()) % 1);
					Lines.stroke(9f, Tmp.c2);
					Lines.square(screenVec.x, screenVec.y, size + 3f, rotationS);
					Lines.stroke(3f, Tmp.c1);
					Lines.square(screenVec.x, screenVec.y, size + 3f, rotationS);
					
					float width = Core.graphics.getWidth(), height = Core.graphics.getHeight();
					
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
		
		public static CameraTrackerAction moveTo(Position target, float duration){
			CameraTrackerAction action = Actions.action(CameraTrackerAction.class, CameraTrackerAction::new);
			action.trackTarget = target;
			action.setDuration(duration);
			return action;
		}
		
		@Override
		protected void update(float percent){
			Core.camera.position.lerp(trackTarget.getX(), trackTarget.getY(), 0.075f);
		}
	}
	
	public static class CameraMoveAction extends TemporalAction{
		public static CameraMoveAction moveTo(float x, float y, float duration, Interp interpolation){
			CameraMoveAction action = Actions.action(CameraMoveAction.class, CameraMoveAction::new);
			action.setPosition(x, y);
			action.setDuration(duration);
			action.setInterpolation(interpolation);
			return action;
		}
		
		protected float startX, startY;
		protected float endX, endY;
		
		@Override
		protected void begin(){
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
}
