package newhorizon.util.feature.cutscene.events;

import arc.Core;
import arc.func.Func;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.CutsceneScript;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.func.NHFunc;
import newhorizon.util.ui.IconNumDisplay;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class FleetEvent extends CutsceneEvent{
	public ObjectMap<UnitType, Number> unitTypeMap = ObjectMap.of();
	public Func<CutsceneEventEntity, Team> teamFunc = e -> Vars.state.rules.waveTeam;
	
	public Func<CutsceneEventEntity, Position> sourceFunc = e -> {
		Team team = teamFunc.get(e);
		CoreBlock.CoreBuild coreBuild = team.core();
		if(coreBuild == null){
			if(team == Vars.state.rules.waveTeam){
				return Vars.state.hasSpawns() ? new Vec2().set(Vars.spawner.getFirstSpawn()) : new Vec2(0, 0);
			}else return new Vec2(-120, -120);
		}
		
		return Geometry.findFurthest(coreBuild.x, coreBuild.y, Vars.state.rules.waveTeam.cores());
	};
	
	public Func<CutsceneEventEntity, Position> targetFunc = e -> {
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		
		Building b = null;
		
		int times = 0;
		
		Team team = teamFunc.get(e);
		Position source = sourceFunc.get(e);
		
		Seq<Building> all = new Seq<>(Groups.build.size());
		Groups.build.copy(all);
		all.remove(bi -> bi.team == team);
		
		while(b == null && all.any()){
			int index = rand.random(all.size - 1);
			b = all.get(index);
			
			if(GravityTrapField.IntersectedAllyRect.get(b, Tmp.r1.setSize(4).setCenter(b.x, b.y))){
				all.remove(index);
				b = null;
			}
		}
		
		Vec2 t = new Vec2();
		if(b != null)t.set(b);
		if(all.isEmpty())t.set(source);
		
		return t;
	};
	
	public Func<CutsceneEventEntity, Float> angle = e -> {
		Position source = sourceFunc.get(e), target = targetFunc.get(e);
		if(source == null || target == null)return 45f;
		else return target.angleTo(source) - 180;
	};
	
	public float spawnDelay = 30f;
	public float delay = 600f;
	public float range = 120f;
	
	public FleetEvent(String name){
		super(name);
		
		reloadTime = 900f;
		drawable = true;
		removeAfterTriggered = false;
	}
	
	@Override
	public void updateEvent(CutsceneEventEntity e){
		e.reload += Time.delta;
		
		if(e.reload >= reloadTime && !CutsceneScript.isPlayingCutscene){
			e.act();
			e.reload = 0;
		}
	}
	
	@Override
	public void draw(CutsceneEventEntity e){
		Team team = teamFunc.get(e);
		
		
		
		Draw.blend(Blending.additive);
		Draw.z(Layer.legUnit + 1);
		Draw.color(team.color, Color.white, 0.075f);
		Draw.alpha(0.65f);
		
		float f = Interp.pow3Out.apply(Mathf.curve(1 - e.reload / reloadTime, 0, 0.05f));
		
		Draw.rect(NHContent.fleet, e, NHContent.fleet.width * f * Draw.scl, NHContent.fleet.height * f * Draw.scl, 0);
		
		float ang = angle.get(e);
		
		for(int i = 0; i < 4; i++){
			float s = (1 - ((Time.time + 25 * i) % 100) / 100) * f * Draw.scl * 1.75f;
			Tmp.v1.trns(ang + 180, 36 + 12 * i).add(e);
			Draw.blend(Blending.additive);
			Draw.rect(NHContent.arrowRegion, Tmp.v1, NHContent.arrowRegion.width * s, NHContent.arrowRegion.height * s, ang - 90);
		}
		
		
		Lines.stroke(5f * f);
		Draw.blend(Blending.additive);
		Lines.circle(e.x, e.y, range * (1 + Mathf.absin(4f, 0.055f)));
		
		Draw.reset();
		Draw.blend();
	}
	
	@Override
	public void triggered(CutsceneEventEntity e){
		Team team = teamFunc.get(e);
		
		if(!UIActions.disabled() && teamFunc.get(e) != Vars.player.team())NHSounds.alarm.play();
		unitTypeMap.each((u, i) -> {
			NHFunc.spawnUnit(team, e.x, e.y, angle.get(e), range, delay, spawnDelay, u, Math.min(i.intValue(), Units.getCap(team) - team.data().countType(u)));
		});
		
		UIActions.actionSeqMinor(Actions.parallel(
			UIActions.cautionAt((e).getX(), (e).getY(), 4, 3, team.color),
			UIActions.labelAct("[accent]Caution[]: Fleet Incoming.", 0.75f, 2.25f, Interp.linear, t -> {
			t.image(NHContent.fleet).size(LEN).padRight(OFFSET);
		})));
		
		e.set(targetFunc.get(e));
	}
	
	@Override
	public void onCallUI(CutsceneEventEntity e){
		UIActions.showLabel(2f, t -> {
			Color color = teamFunc.get(e).color;
			
			if(teamFunc.get(e) != Vars.player.team())NHSounds.alarm.play();
			
			t.background(Styles.black5);
			
			t.table(t2 -> {
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(color);
				t2.image(NHContent.fleet).fill().color(color);
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(color);
			}).growX().pad(OFFSET / 2).fillY().row();
			
			t.add("<< " + Core.bundle.get("nh.cutscene.event.fleet-alert") + " >>").color(color);
		});
	}
	
	@Override
	public void onCall(CutsceneEventEntity e){
		Position position = targetFunc.get(e);
		if(position == null)position = new Vec2().set(teamFunc.get(e).cores().firstOpt());
		if(position == null){
			e.set(0, 0);
		}else e.set(position);
	}
	
	@Override
	public void setupTable(CutsceneEventEntity e, Table table){
		Team team = teamFunc.get(e);
		Color color = team.color;
		
		e.infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.table(t2 -> {
					t2.button(new TextureRegionDrawable(NHContent.fleet), Styles.clearPartiali, LEN - OFFSET, this::showAsDialog).size(LEN - OFFSET).padLeft(OFFSET).padRight(OFFSET / 2).color(color);
					t2.add("INTERVENE").color(color);
					t2.button("Check Target", Icon.eye, Styles.transt, () -> {
						UIActions.checkPosition(e);
					}).disabled(b -> UIActions.lockingInput()).growX().height(LEN - OFFSET * 2).padLeft(OFFSET).marginLeft(OFFSET).pad(OFFSET / 3);
				}).growX().pad(OFFSET / 2).fillY().row();
				c.add(
					new Bar(
						() -> TableFunc.format(Mathf.clamp(e.reload / reloadTime) * 100) + "%",
						() -> color,
						() -> e.reload / reloadTime
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
		
		e.infoT.pack();
		
		table.add(e.infoT).row();
	}
	
	@Override
	public void read(CutsceneEventEntity e, Reads reads){
		e.reload = reads.f();
	}
	
	@Override
	public void write(CutsceneEventEntity e, Writes writes){
		writes.f(e.reload);
	}
	
	@Override
	public void display(Table table){
		table.table(t -> {
			t.add('<' + Core.bundle.get("mod.ui.fleet") + '>').color(Pal.accent).center().growX().fillY().row();
			t.add(Core.bundle.get("mod.ui.fleet.description")).color(Color.lightGray).center().growX().fillY().row();
			t.image().color(Pal.accent).pad(OFFSET / 2).growX().height(OFFSET / 4).padLeft(OFFSET / 2).padRight(OFFSET / 2).row();
			
			for(ObjectMap.Entry<UnitType, Number> entry : unitTypeMap.entries()){
				t.add(new IconNumDisplay(entry.key.fullIcon, entry.value.intValue(), entry.key.localizedName)).left().row();
			}
		}).fill().row();
		
	}
}
