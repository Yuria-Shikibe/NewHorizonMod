package newhorizon.util.feature.cutscene.events;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.feature.cutscene.WorldActions;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidEvent extends CutsceneEvent{
	public BulletType bulletType = Bullets.artilleryDense;
	public Func<CutsceneEventEntity, Team> teamFunc;
	public Func<CutsceneEventEntity, Position> targetFunc;
	public Func<CutsceneEventEntity, Position> sourceFunc = e -> {
		Team team = teamFunc.get(e);
		CoreBlock.CoreBuild core = team.cores().firstOpt();
		
		return core == null ? new Vec2(Vars.world.unitWidth() + 500, Vars.world.unitHeight() + 500) : core;
	};
	public int number = 30;
	public float shootDelay = 6;
	public float inaccuracy = 2;
	
	public Cons<Bullet> shootModifier = b -> b.lifetime(b.lifetime() * (1 + Mathf.range(0.075f)));
	
	public RaidEvent(String name){
		super(name);
		
		drawable = true;
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
	public void draw(CutsceneEventEntity e){
		Team team = teamFunc.get(e);
		Position source = sourceFunc.get(e);
		if(source == null)return;
		
		Draw.blend(Blending.additive);
		Draw.z(Layer.legUnit + 1);
		Draw.color(team.color, Color.white, 0.075f);
		Draw.alpha(0.65f);
		
		float f = Interp.pow3Out.apply(Mathf.curve(1 - e.reload / reloadTime, 0, 0.05f));

		Draw.rect(NHContent.raid, e, NHContent.raid.width * f * Draw.scl, NHContent.raid.height * f * Draw.scl, 0);

		float ang = source.angleTo(e);

		for(int i = 0; i < 4; i++){
			float s = (1 - ((Time.time + 25 * i) % 100) / 100) * f * Draw.scl * 1.75f;
			Tmp.v1.trns(ang + 180, 36 + 12 * i).add(e);
			Draw.blend(Blending.additive);
			Draw.rect(NHContent.arrowRegion, Tmp.v1, NHContent.arrowRegion.width * s, NHContent.arrowRegion.height * s, ang - 90);
		}

		Lines.stroke(5f * f);
		Draw.blend(Blending.additive);
		
		float spread = (e.dst(source) * Mathf.sinDeg(inaccuracy) / Mathf.cosDeg(inaccuracy)) * 1.35f;
		
		Lines.circle(e.x, e.y, spread * (1 + Mathf.absin(4f, 0.055f)));
		
		for(int i = 0; i < 4; i++){
			float rot = i * 360f / 4 + Time.time * 2;
			Lines.swirl(e.x, e.y, spread * f + Lines.getStroke() * 5f, 0.11f, rot);
		}
		
		Draw.reset();
		Draw.blend();
		//				DrawFunc.arrow();
	}
	
	@Override
	public void triggered(CutsceneEventEntity e){
		if(e == null)return;
		Team team = teamFunc.get(e);
		Position source = sourceFunc.get(e);
		if(source == null)return;
		
		Vec2 vec2 = new Vec2().set(e);
		
		UIActions.actionSeqMinor(Actions.parallel(UIActions.cautionAt((e).getX(), (e).getY(), 4, number * shootDelay / 60f, team.color), Actions.run(() -> {
			NHSounds.alarm.play();
			for(int i = 0; i < number; i++){
				Time.run(i * shootDelay, WorldActions.raidPos(team.cores().firstOpt(), team, bulletType, source.getX() + Mathf.range(120), source.getY() + Mathf.range(120), vec2.x, vec2.y, b -> {
					b.vel.rotate(Mathf.range(inaccuracy));
					if(b.type.shootEffect != null)
						b.type.shootEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
					if(b.type.smokeEffect != null)
						b.type.smokeEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
					
					shootModifier.get(b);
				}));
			}
		}), UIActions.labelAct("[accent]Caution[]: @@@Raid Incoming.", 0.75f, number * shootDelay / 60f, Interp.linear, t -> {
			t.image(NHContent.raid).size(LEN).padRight(OFFSET);
		})));
		
		e.set(targetFunc.get(e));
	}
	
	@Override
	public void onCallUI(CutsceneEventEntity e){
		Color color = teamFunc.get(e).color;
		
		UIActions.showLabel(2f, t -> {
			if(teamFunc.get(e) != Vars.player.team())NHSounds.alarm.play();
			
			t.background(Styles.black5);
			
			t.table(t2 -> {
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(color);
				t2.image(NHContent.raid).fill().color(color);
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(color);
			}).growX().pad(OFFSET / 2).fillY().row();
			
			t.add("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>").color(color);
		});
	}
	
	@Override
	public void onCall(CutsceneEventEntity e){
		Position position = targetFunc.get(e);
		if(position == null)position = new Vec2().set(Vars.state.rules.defaultTeam.cores().firstOpt());
		if(position == null){
			e.set(0, 0);
		}else e.set(position);
	}
	
	@Override
	public void setupTable(CutsceneEventEntity e, Table table){
		Color color = teamFunc.get(e).color;
		
		e.infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.table(t2 -> {
					t2.image(NHContent.raid).size(LEN - OFFSET).padLeft(OFFSET).padRight(OFFSET / 2).color(color);
					t2.add("Raid").color(color);
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
						l.setText("[gray]Remain Time: " + ((remain / Time.toSeconds > 15) ? "[]" : "[accent]") + Mathf.floor(remain / Time.toMinutes) + ":" + Mathf.floor((remain % Time.toMinutes) / Time.toSeconds));
					}).left().fillY().growX().row();
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
}
