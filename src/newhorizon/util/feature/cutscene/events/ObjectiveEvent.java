package newhorizon.util.feature.cutscene.events;

import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.ui.ObjectiveSign;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class ObjectiveEvent extends CutsceneEvent{
	public Func<CutsceneEventEntity, CharSequence> info = e -> "null";
	public Func<CutsceneEventEntity, Boolean> trigger = e -> true;
	public Cons<CutsceneEventEntity> action = e -> {};
	
	public float checkSpacing = 30f;
	
	public ObjectiveEvent(String name){
		super(name);
		
		checkSpacing = Vars.mobile ? 60f : 30f;
		
		removeAfterTriggered = true;
	}
	
	@Override
	public void updateEvent(CutsceneEventEntity e){
		if(e.timer.get(checkSpacing) && trigger.get(e))e.act();
	}
	
	@Override
	public void triggered(CutsceneEventEntity e){
		Sounds.unlock.play();
		action.get(e);
	}
	
	@Override
	public void onCall(CutsceneEventEntity e){
	
	}
	
	@Override
	public void setupTable(CutsceneEventEntity e, Table table){
		e.infoT = new Table(Tex.sideline, t -> {
			t.add(new ObjectiveSign(Color.gray, Pal.accent, 2, 4, 5, () -> trigger.get(e))).size(LEN / 2).pad(OFFSET / 2).padLeft(OFFSET).padRight(OFFSET).left();
			t.label(() -> info.get(e)).growX().fillY();
		});
		
		table.add(e.infoT).growX().row();
		
	}
	
	@Override
	public void removeTable(CutsceneEventEntity e, Table table){
		e.infoT.actions(Actions.delay(1.5f), Actions.alpha(0, 0.45f, Interp.fade), Actions.remove());
	}
}
