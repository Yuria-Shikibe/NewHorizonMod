package newhorizon.expand.eventsys.types;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Func;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.ui.ObjectiveSign;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class ObjectiveEventType extends WorldEventType{
	public Floatf<WorldEvent> ratio = f -> 0;
	public Func<WorldEvent, CharSequence> info = e -> "null";
	public Boolf<WorldEvent> trigger = e -> true;
	public Cons<WorldEvent> action = e -> {};
	
	public float checkSpacing = 30f;
	
	public ObjectiveEventType(String name){
		super(name);
		
		checkSpacing = Vars.mobile ? 60f : 30f;
		
		removeAfterTrigger = true;
		
		warnOnHUD = false;
		minimapMarkable = false;
		drawable = false;
		hasCoord = false;
	}
	
	@Override
	public void init(WorldEvent event){
		super.init(event);
		event.team = Vars.state.rules.defaultTeam;
	}
	
	@Override
	public void infoTable(Table table){
		table.label(() -> info.get(null)).margin(4f).fill();
	}
	
	@Override
	public void updateEvent(WorldEvent e){
		e.reload += Time.delta;
		if(e.reload > checkSpacing){
			e.reload = 0;
			if(trigger.get(e))trigger(e);
		}
		
	}
	
	@Override
	public float progressRatio(WorldEvent event){
		return ratio.get(event);
	}
	
	@Override
	public void trigger(WorldEvent e){
		Sounds.unlock.play();
		action.get(e);
		
		super.trigger(e);
	}
	
	
	@Override
	public void buildDebugTable(WorldEvent e, Table table){
		table.table(Tex.pane, t -> {
			t.add(name + "|" + e.id).growX().fillY().row();
			t.add(e.ui).padBottom(4f).growX().fillY();
			t.table(i -> {
				i.defaults().growX().height(LEN - OFFSET);
				i.button("COMPLETE", Icon.play, Styles.cleart, () -> {
					trigger(e);
					e.remove();
				});
			}).growX().fillY();
		}).growX().fillY();
	}
	
	@Override
	public Table buildSimpleTable(WorldEvent e){
		return new Table(Tex.sideline, t -> {
			t.add(new ObjectiveSign(Color.gray, Pal.accent, 2, 4, 5, () -> trigger.get(e))).size(LEN / 2).pad(OFFSET / 2).padLeft(OFFSET).padRight(OFFSET).left();
			t.label(() -> info.get(e)).growX().fillY();
		});
	}
	
	@Override
	public void buildTable(WorldEvent e, Table table){
		e.ui = new Table(Tex.sideline, t -> {
			t.add(new ObjectiveSign(Color.gray, Pal.accent, 2, 4, 5, () -> trigger.get(e))).size(LEN / 2).pad(OFFSET / 2).padLeft(OFFSET).padRight(OFFSET).left();
			t.label(() -> info.get(e)).growX().fillY();
		});
		
		e.ui.pack();
		
		table.add(e.ui).growX().row();
	}
}
