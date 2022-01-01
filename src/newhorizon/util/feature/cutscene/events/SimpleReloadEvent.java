package newhorizon.util.feature.cutscene.events;

import arc.func.Prov;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class SimpleReloadEvent extends CutsceneEvent{
	public Runnable action = () -> {};
	public Prov<CharSequence> info = () -> "Null";
	public Prov<Color> color = () -> Pal.accent;
	
	public SimpleReloadEvent(String name, Runnable action){
		super(name);
		this.action = action;
	}
	
	public SimpleReloadEvent(String name){
		super(name);
		
		isHidden = false;
	}
	
	@Override
	public void updateEvent(CutsceneEventEntity e){
		e.reload += Time.delta;
		
		if(e.reload >= reloadTime){
			e.act();
			e.reload = 0;
		}
	}
	
	@Override
	public void setupTable(CutsceneEventEntity e, Table table){
		e.infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.add(new Bar(
						info,
						color,
						() -> e.reload / reloadTime
				)).growX().height(LEN / 1.5f);
			}).padLeft(OFFSET * 2).growX().fillY();
		});
		TableFunc.countdown(e.infoT, () -> reloadTime - e.reload);
		
		table.add(e.infoT).row();
	}
	
	@Override
	public void triggered(CutsceneEventEntity e){
		action.run();
	}
	
	@Override
	public void read(CutsceneEventEntity e, Reads reads){
		e.reload = reads.f();
	}
	
	@Override
	public void write(CutsceneEventEntity e, Writes writes){
		writes.f(e.reload);
	}
}
