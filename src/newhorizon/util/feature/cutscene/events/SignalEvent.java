package newhorizon.util.feature.cutscene.events;

import arc.Core;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.feature.cutscene.WorldActions;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class SignalEvent extends CutsceneEvent{
	public static final float proximateDst = 12f;
	
	public float range = 1800f;
	public Runnable action = () -> {};
	
	public SignalEvent(String name){
		super(name);
		position = new Vec2(0, 0);
		reloadTime = 150f;
		
		removeAfterTriggered = true;
	}
	
	@Override
	public void updateEvent(CutsceneEventEntity e){
		if(e.timer.get(160)){
			WorldActions.signalDef(e.x, e.y, range, Color.lightGray);
		}
		
		if(WorldActions.signalNearby(e.x, e.y, proximateDst)){
			e.reload += Time.delta;
			if(e.timer.get(1, 15)){
				Fx.spawn.at(e);
			}
			
			if(Mathf.chanceDelta(0.22))NHFx.dataTransport.at(e.x, e.y, Mathf.random(0.15f, 0.55f), Pal.accent);
			if(e.reload >= reloadTime)e.act();
		}else{
			e.reload = Mathf.lerpDelta(e.reload, 0, 0.04f);
		}
	}
	
	@Override
	public void triggered(CutsceneEventEntity e){
		action.run();
		for(int i = 0; i < 4; i++) Time.run(15f * i, () -> Fx.spawn.at(position));
		
		if(!Vars.headless){
			UIActions.showLabel(2f, t -> {
				t.background(Styles.black5);
				
				t.table(t2 -> {
					t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(Color.lightGray);
					t2.image(NHContent.objective).fill().color(Color.lightGray);
					t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(Color.lightGray);
				}).growX().pad(OFFSET / 2).fillY().row();
				
				t.add("[accent]<< []" + Core.bundle.get("nh.cutscene.event.signal-found") + "[accent] >>[]");
			});
		}
	}
	
	@Override
	public void onCall(CutsceneEventEntity e){
		e.set(position);
		
		UIActions.actionSeqMinor(UIActions.labelAct("[lightgray]@@@" + Core.bundle.get("nh.cutscene.event.signal-detected"), 0.25f, 0.75f, Interp.linear, t -> {
			t.image(Icon.tree).padRight(OFFSET);
		}));
	}
	
	@Override
	public void setupTable(CutsceneEventEntity e, Table table){
		e.infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.table(t2 -> {
					t2.add("Signal Strength:").update(l -> l.setColor(e.within(Vars.player, proximateDst) ? Pal.accent : Pal.heal));
					t2.image().growX().height(OFFSET / 3).pad(OFFSET / 3).update(l -> l.setColor(e.within(Vars.player, proximateDst) ? Pal.accent : Pal.heal));
				}).growX().pad(OFFSET / 2).fillY().row();
				c.add(new Bar(
						() -> e.within(Vars.player, proximateDst) ? Core.bundle.get("nh.cutscene.event.signal-receive") : (Core.bundle.get("nh.cutscene.event.signal-strength") + " ") + TableFunc.format((1 - Mathf.clamp(Vars.player.dst(position) / range)) * 100) + "%",
						() -> e.within(Vars.player, proximateDst) ? Pal.accent : Pal.heal,
						() -> e.within(Vars.player, proximateDst) ? (e.reload / reloadTime) : (1 - Mathf.clamp(Vars.player.dst(position) / range))
				)).growX().height(LEN / 2);
			}).padLeft(OFFSET * 2).growX().fillY();
		});
		
		table.add(e.infoT).row();
	}
}
