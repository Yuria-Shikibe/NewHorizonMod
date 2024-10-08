package newhorizon.expand.eventsys.types;

import arc.Core;
import arc.audio.Sound;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.core.UI;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.func.NHFunc;
import newhorizon.util.ui.NHUIFunc;
import newhorizon.util.ui.TableFunc;
import newhorizon.util.ui.display.IconNumDisplay;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class InterventionEventType extends TargetableEventType{
	public ObjectIntMap<UnitType> spawner = new ObjectIntMap<>();
	
	//	@Customizable @Parserable()
	public Sound callSound = NHSounds.alert2;
	public StatusEffect status = StatusEffects.none;
	public float statusDuration = 600;
	
	public double flag = Double.NaN;
	
	public float spawnRange = 180f;
	public float reloadTime = 600f;
	
	public void spawn(Object... items){
		spawner = new ObjectIntMap<>(items.length / 2);
		for(int i = 0; i < items.length; i += 2){
			spawner.put((UnitType)items[i], ((Number)items[i + 1]).intValue());
		}
	}
	
	public InterventionEventType(String name){
		super(name);
		
		minimapMarkable = true;
		removeAfterTrigger = true;
		drawable = true;
		hasCoord = true;
	}
	
	@Override
	public float range(WorldEvent event){
		return spawnRange;
	}
	
	@Override
	public TextureRegion icon(){
		return NHContent.fleet;
	}
	
	@Override
	public void warnHUD(WorldEvent event){
		NHUIFunc.showLabel(2.5f, t -> {
			Color color = event.team.color;
			
			if(event.team != Vars.player.team())callSound.play();
			
			t.background(Styles.black5);
			
			t.table(t2 -> {
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(color);
				t2.image(NHContent.fleet).fill().color(color);
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(color);
			}).growX().pad(OFFSET / 2).fillY().row();
			
			t.table(l -> {
				l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.fleet-alert") + " >>")).color(color).padBottom(4).row();
			}).growX().fillY();
		});
	}
	
	@Override
	public void updateEvent(WorldEvent e){
		e.reload += Time.delta;
		
		if(e.reload >= reloadTime){
			e.reload = 0;
			
			trigger(e);
		}
	}
	
	public void triggerNet(WorldEvent event){
		event.reload = reloadTime - 10f;
	}
	
	@Override
	public void warnOnTrigger(WorldEvent event){
		TableFunc.showToast(new TextureRegionDrawable(icon(), 0.2f), "[#" + event.team.color + "]" + Core.bundle.get("mod.ui.intervention") + " []" + event.coordText(), NHSounds.alert2);
	}
	
	@Override
	public void trigger(WorldEvent e){
		Team team = e.team;
		
		e.reload = 0;
		
		if(!Vars.headless && team != Vars.player.team())warnOnTrigger(e);
		float angle = source(e).angleTo(e);
		
		for(ObjectIntMap.Entry<UnitType> spawn : spawner.entries()){
			NHFunc.spawnUnit(team, e.x, e.y, angle, spawnRange, 50f, 15f, spawn.key, Math.min(spawn.value, Units.getCap(team) - team.data().countType(spawn.key)), status, statusDuration, flag);
		}
		
		if(removeAfterTrigger)e.remove();
		else e.set(target(e));
	}
	
	@Override
	public void buildTable(WorldEvent e, Table table){
		Team team = e.team;
		Color color = team.color;
		
		Table infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.table(t2 -> {
					Cell<TextButton> b = t2.button(Core.bundle.get("mod.ui.intervention"), new TextureRegionDrawable(icon()), Styles.cleart, LEN - OFFSET, () -> showAsDialog(e)).growX().padLeft(OFFSET).padRight(OFFSET / 2).left().color(color);
					b.minWidth(b.get().getWidth());
					t2.label(e::coordText).expandX();
					t2.add(e.name).expandX().left().color(Color.lightGray);
				}).growX().pad(OFFSET / 2).fillY().row();
				c.add(
					new Bar(
						() -> TableFunc.format(progressRatio(e) * 100) + "%",
						() -> color,
						() -> progressRatio(e)
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
	public float progressRatio(WorldEvent event){
		return Mathf.clamp(event.reload / reloadTime);
	}
	
	@Override
	public void infoTable(Table table){
		table.table(t -> {
			t.add('<' + Core.bundle.get("mod.ui.intervention") + '>').color(Pal.accent).center().growX().fillY().row();
			t.add(Core.bundle.get("mod.ui.fleet.description")).color(Color.lightGray).center().growX().fillY().row();
			t.image().color(Pal.accent).pad(OFFSET / 2).growX().height(OFFSET / 4).padLeft(OFFSET / 2).padRight(OFFSET / 2).row();
			
			for(ObjectIntMap.Entry<UnitType> entry : spawner.entries()){
				t.add(new IconNumDisplay(entry.key.fullIcon, entry.value, entry.key.localizedName)).left().row();
			}
		}).fill().row();
	}
}
