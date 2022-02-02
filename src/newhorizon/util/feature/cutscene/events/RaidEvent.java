package newhorizon.util.feature.cutscene.events;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.core.UI;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.util.feature.cutscene.*;
import newhorizon.util.feature.cutscene.events.util.BulletHandler;
import newhorizon.util.func.NHFunc;
import newhorizon.util.ui.TableFunc;
import newhorizon.util.ui.Tables;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidEvent extends CutsceneEvent{
	public BulletType bulletType = Bullets.artilleryDense;
	
	public Func<CutsceneEventEntity, Team> attackerTeamFunc = e -> Vars.state.rules.waveTeam;
	public Func<CutsceneEventEntity, Position> targetFunc = e -> {
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		
		Building b = null;
		
		int times = 0;
		
		Seq<Building> all = new Seq<>(Groups.build.size());
		Groups.build.copy(all);
		
		
		while(b == null && times < 1024 && all.any()){
			int index = rand.random(all.size - 1);
			b = all.get(index);
			if(b.team == attackerTeamFunc.get(e) || (b.proximity().size < 3 && b.block.health < 1600)){
				all.remove(index);
				b = null;
			}
			times++;
		}
		
		if(b == null && attackerTeamFunc.get(e) != Vars.state.rules.defaultTeam)b = Vars.state.rules.defaultTeam.core();
		
		return new Vec2().set(b == null ? Vec2.ZERO : b);
	};
	public Func<CutsceneEventEntity, Position> sourceFunc = e -> {
		Team team = attackerTeamFunc.get(e);
		CoreBlock.CoreBuild core = team.cores().firstOpt();
		
		return core == null ? (team == Vars.state.rules.waveTeam && Vars.spawner.getSpawns().any()) ? Vars.spawner.getSpawns().get(Mathf.randomSeed(e.id, 0, Math.max(0, Vars.spawner.getSpawns().size - 1))) : new Vec2(Vars.world.unitWidth() + 500, Vars.world.unitHeight() + 500) : core;
	};
	
	public int number = 30;
	public float shootDelay = 6;
	public float inaccuracy = 2;
	public float sourceSpread = 140;
	
	public Cons<Bullet> shootModifier = BulletHandler.spread1;
	
	public RaidEvent(String name){
		super(name);
		
		drawable = true;
	}
	
	@Override
	public CutsceneEventEntity setup(){
		Events.fire(Triggers.raid_setup);
		return super.setup();
	}
	
	@Override
	public void updateEvent(CutsceneEventEntity e){
		e.reload += Time.delta;
		
		if(e.reload >= reloadTime){
			if(Vars.net.server())e.actNet();
			else if(!Vars.net.active())e.act();
		}
	}
	
	@Override
	public void draw(CutsceneEventEntity e){
		Team team = attackerTeamFunc.get(e);
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
		
		float spread = (sourceSpread / 2 + e.dst(source) * Mathf.sinDeg(inaccuracy) / Mathf.cosDeg(inaccuracy)) * 1.35f;
		
		Lines.circle(e.x, e.y, spread * (1 + Mathf.absin(4f, 0.055f)));
		
		for(int i = 0; i < 4; i++){
			float rot = i * 360f / 4 + Time.time * 2;
			Lines.swirl(e.x, e.y, spread * f + Lines.getStroke() * 5f, 0.11f, rot);
		}
		
		Draw.reset();
		Draw.blend();
	}
	
	@Override
	public void triggered(CutsceneEventEntity e){
		if(e == null)return;
		e.reload = 0;
		Team team = attackerTeamFunc.get(e);
		Position source = sourceFunc.get(e);
		if(source == null)return;
		
		Vec2 vec2 = new Vec2().set(e);
		
		UIActions.actionSeqMinor(Actions.parallel(UIActions.cautionAt((e).getX(), (e).getY(), 4, number * shootDelay / 60f, team.color), Actions.run(() -> {
			NHSounds.alarm.play();
			for(int i = 0; i < number; i++){
				int finalI = i;
				Time.run(i * shootDelay, WorldActions.raidPos(team.cores().firstOpt(), team, bulletType, source.getX() + Mathf.randomSeedRange(e.id + finalI, sourceSpread), source.getY() + Mathf.randomSeedRange(e.id + 100 - finalI, sourceSpread), vec2.x, vec2.y, b -> {
					b.vel.rotate(Mathf.randomSeedRange(e.id + 50 + finalI, inaccuracy));
					if(b.type.shootEffect != null)
						b.type.shootEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
					if(b.type.smokeEffect != null)
						b.type.smokeEffect.at(b.x, b.y, b.angleTo(source), b.type.hitColor);
					shootModifier.get(b);
				}));
			}
		}), UIActions.labelAct("[accent]Caution[]: Raid Incoming.", 0.75f, number * shootDelay / 60f, Interp.linear, t -> {
			t.image(NHContent.raid).size(LEN).padRight(OFFSET);
		})));
		
		e.set(targetFunc.get(e));
		
		Events.fire(Triggers.raid_launch);
	}
	
	@Override
	public void onCallUI(CutsceneEventEntity e){
		Color color = attackerTeamFunc.get(e).color;
		
		UIActions.showLabel(2f, t -> {
			if(attackerTeamFunc.get(e) != Vars.player.team())NHSounds.alarm.play();
			
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
		Color color = attackerTeamFunc.get(e).color;
		
		e.infoT = new Table(Tex.sideline, t -> {
			t.table(c -> {
				c.table(t2 -> {
					t2.button(new TextureRegionDrawable(NHContent.raid), Styles.clearPartiali, LEN - OFFSET, this::showAsDialog).size(LEN - OFFSET).padLeft(OFFSET).padRight(OFFSET / 2).color(color);
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
			t.add('<' + Core.bundle.get("mod.ui.raid") + '>').color(Pal.accent).center().growX().fillY().row();
			t.add(Core.bundle.get("mod.ui.raid.description")).color(Color.lightGray).center().growX().fillY().row();
			t.image().color(Pal.accent).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
			
			t.align(Align.topLeft);
			t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-air") + ": " + TableFunc.judge(bulletType.collidesAir && bulletType.collides)).left().row();
			t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-ground") + ": " + TableFunc.judge(bulletType.collidesGround && bulletType.collides)).left().row();
			t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-tile") + ": " + TableFunc.judge(bulletType.collidesTiles)).left().row();
			
			t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
			
			t.add("[lightgray]" + Core.bundle.get("mod.ui.absorbable") + ": " + TableFunc.judge(bulletType.absorbable)).left().row();
			t.add("[lightgray]" + Core.bundle.get("mod.ui.hittable") + ": " + TableFunc.judge(bulletType.hittable)).left().row();
			
			t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
			
			t.add("[lightgray]" + Core.bundle.get("stat.launchtime") + ": [accent]" + TableFunc.format(reloadTime / Time.toSeconds) + "[]" + Core.bundle.get("unit.seconds")).left().row();
			
			t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
			
			t.add("[lightgray]" + Core.bundle.format("mod.ui.estimated-max-damage", UI.formatAmount((long)(Tables.estimateBulletDamage(bulletType, number, true))))).left().row();
			
			t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
			
			t.table(b -> Tables.ammo(b, "[lightgray]*[accent]" + number, bulletType, NHContent.raid, 0)).row();
		}).fill().padBottom(OFFSET).left().row();
	}
}
