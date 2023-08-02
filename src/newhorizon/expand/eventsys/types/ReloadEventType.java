package newhorizon.expand.eventsys.types;

import arc.Core;
import arc.audio.Sound;
import arc.flabel.FLabel;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.core.UI;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import newhorizon.content.NHSounds;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.ui.NHUIFunc;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class ReloadEventType extends WorldEventType{
	public float reloadTime = 600f;
	public Func<WorldEvent, Color> colorFunc = e -> Color.white;
	public Floatf<WorldEvent> barRatio = e -> e.type.progressRatio(e);
	public Func<WorldEvent, CharSequence> info = WorldEvent::info;
	public Cons<WorldEvent> act = e -> {};
	
	public Sound actSound = NHSounds.alert2;
	
	public ReloadEventType(String name){
		super(name);
		
		removeAfterTrigger = true;
		drawable = false;
		hasCoord = false;
	}
	
	@Override
	public float progressRatio(WorldEvent event){
		return Mathf.clamp(event.reload / reloadTime);
	}
	
	@Override
	public void updateEvent(WorldEvent e){
		e.reload += Time.delta;
		
		if(e.reload >= reloadTime){
			e.reload = 0;
			
			trigger(e);
		}
	}
	
	@Override
	public void trigger(WorldEvent e){
		actSound.play();
		act.get(e);
		
		super.trigger(e);
	}
	
	@Override
	public void buildTable(WorldEvent e, Table table){
		Team team = e.team;
		Color color = team.color;
		
		Table infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.table(t2 -> {
					Cell<TextButton> b = t2.button(Core.bundle.get("nh.dialog-event"), new TextureRegionDrawable(icon(), 0.5f), Styles.cleart, LEN - OFFSET, () -> showAsDialog(e)).growX().padLeft(OFFSET).padRight(OFFSET / 2).left().color(color);
					b.minWidth(b.get().getWidth());
					t2.label(e::coordText).expandX();
					t2.add(e.name).expandX().left().color(Color.lightGray);
				}).growX().pad(OFFSET / 2).fillY().row();
				c.add(
						new Bar(
								() -> TableFunc.format(progressRatio(e) * 100) + "%",
								() -> color,
								() -> barRatio.get(e)
						)
				).growX().height(LEN / 2);
				c.addListener(new Tooltip(t2 -> {
					t2.background(Tex.bar);
					t2.color.set(Color.black);
					t2.color.a = 0.35f;
					t2.add("Remain Time: 00:00 ").update(l -> {
						float remain = reloadTime - e.reload;
						l.setText("[gray]Remain Time: " + ((remain / Time.toSeconds > 15) ? "[]" : "[accent]") + UI.formatTime(remain));
					}).left().fillY().growX().row();
					t2.table().fillX();
				}));
			}).padLeft(OFFSET * 2).growX().fillY().row();
			
		});
		
		infoT.pack();
		
		e.ui = infoT;
		
		table.add(infoT).growX().fillY();
	}
	
	@Override
	public void warnHUD(WorldEvent event){
		NHUIFunc.showLabel(2.5f, t -> {
			String color = colorFunc.get(event).toString();
			
			NHSounds.alert2.play();
			
			t.background(Styles.black5);
			
			t.table(t2 -> {
				t2.image(icon()).fill();
			}).growX().pad(OFFSET / 2).margin(12f).fillY().row();
			
			t.table(l -> {
				l.add(new FLabel("[#" + color + "]<<[] " + info.get(event) + "[#" + color + "] >>[]")).padBottom(4).row();
			}).growX().fillY();
		});
	}
	
	@Override
	public void warnOnTrigger(WorldEvent event){
//		TableFunc.showToast(new TextureRegionDrawable(icon(), 0.2f), "[#" + event.team.color + "]" + Core.bundle.get("mod.ui.raid") + " []" + event.coordText(), NHSounds.alert2);
	}
	
	public void triggerNet(WorldEvent event){
		event.reload = reloadTime * 0.95f;
	}
	
	@Override
	public void infoTable(Table table){
		table.table(this::buildSpeInfo).fill();
	}
	
	public void buildSpeInfo(Table table){
		table.add("[gray]N/A");
	}
}
