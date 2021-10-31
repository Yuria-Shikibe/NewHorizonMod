package newhorizon.feature.cutscene;

import arc.Core;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Func;
import arc.func.Prov;
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
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.func.NHFunc;
import newhorizon.ui.ObjectiveSign;
import newhorizon.ui.TableFunc;

import static newhorizon.ui.TableFunc.LEN;
import static newhorizon.ui.TableFunc.OFFSET;

public class EventSamples{
	public static CutsceneEvent seekingTech(String name, Cons<CutsceneEvent> modifier, float x, float y, float seekRange, Runnable act){
		return new CutsceneEvent(name){
			final float range = seekRange;
			{
				cannotBeRemove = true;
				updatable = true;
				removeAfterTriggered = true;
				position = new Vec2(x, y);
				
				modifier.get(this);
			}
			
			@Override
			public void updateEvent(CutsceneEventEntity e){
				if(e.timer.get(160)){
					WorldActions.signalDef(position.getX(), position.getY(), range, Color.lightGray);
				}
				
				WorldActions.signalTriggered(position.getX(), position.getY(), 12, e::act);
			}
			
			@Override
			public void triggered(CutsceneEventEntity e){
				act.run();
				for(int i = 0; i < 4; i++)Time.run(15f * i, () -> Fx.spawn.at(position));
				
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
				UIActions.actionSeqMinor(UIActions.labelAct("[lightgray]@@@" + Core.bundle.get("nh.cutscene.event.signal-detected"), 0.25f, 0.75f, Interp.linear, t -> {
					t.image(Icon.tree).padRight(OFFSET);
				}));
			}
			
			@Override
			public void setupTable(CutsceneEventEntity e, Table table){
				e.infoT = new Table(Tex.sideline, t -> {
					t.table(c -> {
						c.table(t2 -> {
							t2.add("Signal Strength:").color(Pal.heal);
							t2.image().growX().height(OFFSET / 3).pad(OFFSET / 3).color(Pal.heal);
						}).growX().pad(OFFSET / 2).fillY().row();
						c.add(new Bar(
								() -> TableFunc.format((1 - Mathf.clamp(Vars.player.dst(position) / range)) * 100) + "%",
								() -> Pal.heal,
								() -> 1 - Mathf.clamp(Vars.player.dst(position) / range)
						)).growX().height(LEN / 2);
					}).padLeft(OFFSET * 2).growX().fillY();
				});
				
				table.add(e.infoT).row();
			}
		};
	}
	
	public static CutsceneEvent objective(String name, Cons<CutsceneEvent> modifier, Prov<CharSequence> info, Boolp trigger, Runnable runnable){
		return new CutsceneEvent(name){
			
			{
				cannotBeRemove = true;
				updatable = true;
				removeAfterTriggered = true;
				modifier.get(this);
			}
			
			@Override
			public void updateEvent(CutsceneEventEntity e){
				if(trigger.get())e.act();
			}
			
			@Override
			public void triggered(CutsceneEventEntity e){
				Sounds.unlock.play();
				runnable.run();
			}
			
			@Override
			public void onCall(CutsceneEventEntity e){
			
			}
			
			@Override
			public void setupTable(CutsceneEventEntity e, Table table){
				e.infoT = new Table(Tex.sideline, t -> {
					t.add(new ObjectiveSign(Color.gray, Pal.accent, 2, 4, 5, trigger)).size(LEN / 2).pad(OFFSET / 2).padLeft(OFFSET).padRight(OFFSET).left();
					t.add(info.get()).update(l -> l.setText(info.get())).growX();
				});
				
				table.add(e.infoT).row();
			}
			
			@Override
			public void removeTable(CutsceneEventEntity e, Table table){
				e.infoT.actions(Actions.delay(1.5f), Actions.alpha(0, 0.45f, Interp.fade), Actions.remove());
			}
		};
	}
	
	public static CutsceneEvent raid(String name, Cons<CutsceneEvent> modifier, Func<CutsceneEventEntity, Position> target, BulletType type, Prov<Team> teamProv, float delay, int number, float shootDelay, float inaccuracy){
		return new CutsceneEvent(name){
			{
				
				drawable = true;
				initOnce = true;
				reloadTime = delay;
				updatable = true;
				cannotBeRemove = true;
				modifier.get(this);
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
				Position pos = e.data();
				Team team = teamProv.get();
				CoreBlock.CoreBuild core = team.cores().firstOpt();
				if(core == null)return;
				
				Draw.z(Layer.endPixeled);
				Draw.color(team.color);
				Draw.alpha(0.65f);
				Draw.blend(Blending.additive);
				
				Draw.rect(NHContent.raid, pos, 0);
				
				float ang = core.angleTo(pos);
				
				for(int i = 0; i < 4; i++){
					Draw.scl(1 - ((Time.time + 25 * i) % 100) / 100);
					Tmp.v1.trns(ang + 180, 36 + 12 * i).add(pos);
					Draw.blend(Blending.additive);
					Draw.rect(NHContent.arrowRegion, Tmp.v1, ang - 90);
				}
				
				Lines.stroke(5f);
				Draw.blend(Blending.additive);
				Lines.circle(pos.getX(), pos.getY(), (pos.dst(core) * Mathf.sinDeg(inaccuracy) / Mathf.cosDeg(inaccuracy)) * 1.35f * (1 + Mathf.absin(4f, 0.055f)));
				
				Draw.reset();
				Draw.blend();
//				DrawFunc.arrow();
			}
			
			@Override
			public void triggered(CutsceneEventEntity e){
				Position pos = e.data();
				if(pos == null)return;
				Team team = teamProv.get();
				CoreBlock.CoreBuild core = team.cores().firstOpt();
				if(core == null || !core.isValid())return;
				
				UIActions.actionSeqMinor(Actions.parallel(UIActions.cautionAt(pos.getX(), pos.getY(), 4, number * shootDelay / 60f, team.color), Actions.run(() -> {
					NHSounds.alarm.play();
					for(int i = 0; i < number; i++){
						Time.run(i * shootDelay, WorldActions.raidPos(core, team, type, core.x + Mathf.range(120), core.y + Mathf.range(120), pos.getX(), pos.getY(), b -> {
							b.lifetime(b.lifetime() * (1 + Mathf.range(0.075f)));
							b.vel.rotate(Mathf.range(inaccuracy));
							if(b.type.shootEffect != null)
								b.type.shootEffect.at(b.x, b.y, b.angleTo(core), b.type.hitColor);
							if(b.type.smokeEffect != null)
								b.type.smokeEffect.at(b.x, b.y, b.angleTo(core), b.type.hitColor);
						}));
					}
				}), UIActions.labelAct("[accent]Caution[]: @@@Raid Incoming.", 0.75f, number * shootDelay / 60f, Interp.linear, t -> {
					t.image(NHContent.raid).size(LEN).padRight(OFFSET);
				})));
				
				e.data = target.get(e);
			}
			
			@Override
			public void onCall(CutsceneEventEntity e){
				e.data = target.get(e);
				if(e.data == null)e.data = new Vec2().set(Vars.state.rules.defaultTeam.cores().firstOpt());
				if(e.data == null)e.remove();
				
				if(!Vars.headless){
					NHSounds.alarm.play();
					UIActions.showLabel(2f, t -> {
						t.background(Styles.black5);
						
						t.table(t2 -> {
							t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(Pal.redderDust);
							t2.image(NHContent.raid).fill().color(Pal.redderDust);
							t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(Pal.redderDust);
						}).growX().pad(OFFSET / 2).fillY().row();
						
						t.add("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>").color(Pal.redderDust);
					});
				}
			}
			
			@Override
			public void setupTable(CutsceneEventEntity e, Table table){
				e.infoT = new Table(Tex.sideline, t -> {
					t.table(c -> {
						c.table(t2 -> {
							t2.image(NHContent.raid).size(LEN - OFFSET).padLeft(OFFSET).padRight(OFFSET / 2).color(Pal.redderDust);
							t2.add("Raid").color(Pal.redderDust);
							t2.button("Check Target", Icon.eye, Styles.transt, () -> {
								Position pos = e.data();
								UIActions.actionSeqMinor(
										Actions.run(UIActions::pauseCamera),
										UIActions.moveTo(pos.getX(), pos.getY(), 1f, Interp.pow3),
										UIActions.holdCamera(pos.getX(), pos.getY(), 1f),
										UIActions.moveTo(Vars.player.x, Vars.player.y, 0.5f, Interp.pow3),
										Actions.run(UIActions::resumeCamera)
								);
							}).disabled(b -> UIActions.lockInput).growX().height(LEN - OFFSET * 2).padLeft(OFFSET).marginLeft(OFFSET).pad(OFFSET / 3);
						}).growX().pad(OFFSET / 2).fillY().row();
						c.add(
							new Bar(
								() -> TableFunc.format(Mathf.clamp(e.reload / reloadTime) * 100) + "%",
								() -> Pal.redderDust,
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
				e.data = TypeIO.readVec2(reads);
			}
			
			@Override
			public void write(CutsceneEventEntity e, Writes writes){
				writes.f(e.reload);
				TypeIO.writeVec2(writes, new Vec2().set((Position)e.data()));
			}
		};
	}
	
	public static CutsceneEvent jumpgateUnlock,
			jumpgateUnlockObjective, waveTeamRaid;
	
	public static void load(){
		waveTeamRaid = raid("waveTeamRaid", e -> {e.reloadTime = 60 * 60 * 4;}, e -> {
			Rand rand = NHFunc.rand;
			rand.setSeed(e.id);
			
			Building b = null;
			int times = 0;
			
			Seq<Building> all = new Seq<>();
			Groups.build.copy(all);
			
			while(b == null && times < 1024 && all.any()){
				int index = rand.random(all.size - 1);
				b = all.get(index);
				if(b.team == Vars.state.rules.waveTeam || (b.proximity().size < 3 && b.block.health < 1600)){
					all.remove(index);
					b = null;
				}
				times++;
			}
			
			return new Vec2().set(b == null ? Vec2.ZERO : b);
		}, NHBullets.airRaidMissile, () -> Vars.state.rules.waveTeam, 60 * 60 * 4, 30, 6f, 4);
		jumpgateUnlock = seekingTech("jumpgateUnlock", e -> {}, 888, 1392, 1600, () -> CutsceneScript.netUnlock(NHBlocks.jumpGatePrimary));
		jumpgateUnlockObjective = objective("jumpgateUnlockObjective", e -> {}, () -> "Find The Signal Source", () -> NHBlocks.jumpGatePrimary.unlocked(), () -> {});
	}
}
