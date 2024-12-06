package newhorizon.expand.cutscene.actions;

import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Team;
import mindustry.type.UnitType;
import newhorizon.content.NHSounds;
import newhorizon.expand.cutscene.NHCSS_Action;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.cutscene.NHCSS_UI;
import newhorizon.expand.eventsys.types.WorldEventType;

import static mindustry.Vars.tilesize;

public class CSSActions{
	public static NHCSS_Action.ActionBus getContext(){
		return NHCSS_Action.getContext();
	}
	
	public static final Pool<CameraAction> cameraPool = new Pool<CameraAction>(10){
		@Override
		protected CameraAction newObject(){
			return new CameraAction(NHCSS_Action.getContext());
		}
	};
	
	public static void beginCreateAction(NHCSS_Action.ActionBus bus){
		NHCSS_Action.beginCreateAction(bus);
	}
	
	public static void beginCreateAction(){
		beginCreateAction(new NHCSS_Action.ActionBus());
	}
	
	public static void endCreateAction(){
		NHCSS_Action.endCreateAction();
	}
	
	public static NHCSS_Action.ActionBus pack(NHCSS_Action... actions){
		return (NHCSS_Action.ActionBus)getContext().addAll(actions);
	}
	
	public static NHCSS_Action control(boolean enable){
		return new RunnableAction(getContext(), () -> NHCSS_UI.cameraOverride = !enable);
	}
	
	public static NHCSS_Action text(NHCSS_UI.TextBox textBox, boolean wait){
		return new TextAction(getContext(), textBox).setDuration(textBox.duration + textBox.fadeTime);
	}
	
	public static NHCSS_Action text(String text, float duration, boolean wait){
		NHCSS_UI.TextBox box;
		return new TextAction(getContext(), box = new NHCSS_UI.TextBox(text, duration)).setDuration(duration + box.fadeTime);
	}
	
	public static NHCSS_Action text(String text, float duration){
		return text(text, duration, false);
	}
	
	public static NHCSS_Action text(String text, boolean wait){
		NHCSS_UI.TextBox box = null;
		return new TextAction(getContext(), new NHCSS_UI.TextBox(text)).setDuration(box.duration + box.fadeTime);
	}
	
	public static NHCSS_Action text(String text){
		return new TextAction(getContext(), new NHCSS_UI.TextBox(text));
	}
	
	public static NHCSS_Action pause(){
		return runnable(() -> Vars.state.set(GameState.State.paused));
	}
	
	public static NHCSS_Action resume(){
		return runnable(() -> Vars.state.set(GameState.State.playing));
	}
	
	public static NHCSS_Action pullCurtain(){
		return parallel(delay(NHCSS_UI.curtainScratchTime() + 35f), runnable(NHCSS_UI::pullCurtain));
	}
	
	public static NHCSS_Action withdrawCurtain(){
		return parallel(delay(NHCSS_UI.curtainScratchTime()), runnable(NHCSS_UI::withdrawCurtain));
	}
	
	public static void check(float x, float y){
		beginCreateAction();
		
		NHCSS_Core.core.applySubBus(
			control(false),
			cameraScl(1.5f),
			cameraMove(x, y),
			cameraSustain(30f),
			control(true)
		);
		
		endCreateAction();
	}
	
	public static NHCSS_Action alert(){
		return runnable(() -> {
			if(!Vars.headless)NHSounds.alarm.play(1);
		});
	}
	
	public static NHCSS_Action runnable(Runnable runnable){
		return new RunnableAction(getContext(), runnable);
	}
	
	public static NHCSS_Action seq(NHCSS_Action... actions){
		return new QueueAction(getContext()).initActions(actions);
	}
	
	public static NHCSS_Action mark(float x, float y, float radius, float lifetime, Color color){
		return new RunnableAction(getContext(), () -> NHCSS_UI.mark(x, y, radius, lifetime, color, () -> false));
	}
	
	public static NHCSS_Action mark(float x, float y, float radius, float lifetime, Color color, NHCSS_UI.MarkStyle style){
		return new RunnableAction(getContext(), () -> NHCSS_UI.mark(x, y, radius, lifetime, color, style, () -> false));
	}
	
	public static NHCSS_Action delay(float duration){
		return new DelayAction(getContext()).setDuration(duration);
	}
	
	public static NHCSS_Action unitInbound(UnitType type, Position position, Cons<InboundAction> modifier){
		InboundAction action = new InboundAction(getContext());
		action.type = type;
		action.setPosition(position);
		return action.modification(modifier);
	}
	
	public static NHCSS_Action unitInbound(UnitType type, float x, float y, Cons<InboundAction> modifier){
		InboundAction action = new InboundAction(getContext());
		action.type = type;
		action.setPosition(x, y);
		return action.modification(modifier);
	}
	
	
	public static NHCSS_Action triggerEvent(WorldEventType eventType, Vec2 coords, Prov<Team> teamProv){
		return new EventSetupAction(getContext(), eventType, teamProv, coords);
	}
	
	public static NHCSS_Action triggerEvent(WorldEventType eventType){
		return new EventSetupAction(getContext(), eventType);
	}
	
	public static NHCSS_Action parallel(NHCSS_Action... actions){
		return new ParallelAction(getContext(), actions);
	}
	
	public static NHCSS_Action waitUntil(Boolp trigger){
		return new WaitAction(getContext(), trigger);
	}
	
	public static NHCSS_Action cameraMove(float x, float y, float duration, float scl, Interp interp){
		if(Vars.headless)return new DelayAction(getContext()).setDuration(duration);
		CameraAction m = new CameraAction(getContext());
		m.cameraTargetScl = scl;
		m.cameraTarget.set(x, y);
		m.duration = duration;
		m.panInterp = interp;
		
		return m;
	}
	
	public static NHCSS_Action cameraMove(float x, float y){
		return cameraMove(x, y, 85f, -1, Interp.smoother);
	}
	
	public static NHCSS_Action caution(float x, float y, float radius, float lifetime, Color color){
		return new ParallelAction(getContext(), CSSActions.delay(lifetime), CSSActions.runnable(() -> {
			NHCSS_UI.mark(x, y, radius, lifetime, color, () -> false);
		}));
	}
	
	
	public static NHCSS_Action cameraMove(Position position){
		return cameraMove(position.getX(), position.getY());
	}
	
	public static NHCSS_Action cameraMoveTile(int x, int y){
		return cameraMove(x * tilesize + 4, y * tilesize + 4);
	}
	
	public static NHCSS_Action cameraSustain(float panDuration){
		return cameraMove(Float.NaN, Float.NaN, panDuration, -1, Interp.one);
	}
	
	public static NHCSS_Action cameraScl(float scl){
		return cameraMove(Float.NaN, Float.NaN, 0, scl, Interp.one);
	}
	
	public static NHCSS_Action cameraSclBy(float sclDelta){
		return cameraMove(Float.NaN, Float.NaN, 0, Vars.renderer.getScale() + sclDelta, Interp.one);
	}
	
	public static NHCSS_Action cameraReturn(float panDuration){
		return cameraMove(Float.NaN, Float.NaN, panDuration, -1, Interp.one);
	}
	
	public static NHCSS_Action cameraReturn(){
		return cameraReturn(85f);
	}
	
}
