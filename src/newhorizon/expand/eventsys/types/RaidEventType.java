package newhorizon.expand.eventsys.types;

import arc.Core;
import arc.audio.Sound;
import arc.flabel.FLabel;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.func.NHFunc;
import newhorizon.util.ui.NHUIFunc;
import newhorizon.util.ui.TableFunc;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidEventType extends TargetableEventType{
	protected static final Seq<Building> tmpSeq = new Seq<>();
	
	public float radius = 180f;
	public float reloadTime = 600f;
	public float inaccuracy = 3f;
	public float velocityRnd = 0.075f;
	
	public ObjectMap<BulletType, ShootPattern> projectiles = new ObjectMap<>();
	
	public Sound callSound = NHSounds.alert2;
	
	public void ammo(Object... objects){
		projectiles = ObjectMap.of(objects);
	}
	
	public RaidEventType(String name){
		super(name);
		
		minimapMarkable = true;
		removeAfterTrigger = true;
		drawable = true;
		hasCoord = true;
	}
	
	@Override
	public Position target(WorldEvent e){
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		
		Building b = null;
		Team team = e.team;
		
		int times = 0;
		
		tmpSeq.clear();
		Groups.build.copy(tmpSeq);
		Seq<Building> all = tmpSeq;
		
		all.remove(bu -> bu.team == team);
		
		while(b == null && times < 1024 && all.any()){
			int index = rand.random(all.size - 1);
			b = all.get(index);
			if(b.proximity().size < 3 || b.block.health < 1600 || b.isPayload()){
				all.remove(index);
				b = null;
			}
			times++;
		}
		
		if(b == null)b = team.core();
		
		return new Vec2().set(b == null ? Vec2.ZERO : b);
	}
	
	public void drawArrow(WorldEvent e){
		float f = Interp.pow3Out.apply(Mathf.curve(1 - progressRatio(e), 0, 0.05f));
		
		float ang = source(e).angleTo(e);
		
		Draw.color(e.team.color, Color.white, 0.075f);
		Draw.blend(Blending.additive);
		
		for(int i = 0; i < 4; i++){
			float s = (1 - ((Time.time + 25 * i) % 100) / 100) * f * Draw.scl * 1.75f;
			Tmp.v1.trns(ang + 180, 36 + 12 * i).add(e);
			Draw.rect(NHContent.arrowRegion, Tmp.v1, NHContent.arrowRegion.width * s, NHContent.arrowRegion.height * s, ang - 90);
		}
		
		Draw.blend();
	}
	
	@Override
	public void draw(WorldEvent e){
		super.draw(e);
		
		drawArrow(e);
	}
	
	@Override
	public float progressRatio(WorldEvent event){
		return Mathf.clamp(event.reload / reloadTime);
	}
	
	@Override
	public float range(WorldEvent event){
		return radius;
	}
	
	@Override
	public TextureRegion icon(){
		return NHContent.raid;
	}
	
	@Override
	public void updateEvent(WorldEvent e){
		e.reload += Time.delta;
		
		if(e.reload >= reloadTime){
			e.reload = 0;
			
			trigger(e);
		}
	}
	
	protected void bullet(WorldEvent e, Team team, BulletType bullet, Position source, Position target, Mover mover){
		if(Vars.state.isMenu())return;
		
		Tmp.v6.rnd(range(e)).add(target);
		
		float
			bulletX = source.getX() + Mathf.range(range(e)) * 0.5f,
			bulletY = source.getY() + Mathf.range(range(e)) * 0.5f,
			aimAngle = Tmp.v6.angleTo(bulletX, bulletY) - 180,
			//force scale life
			lifeScl = Math.max(0, Mathf.dst(bulletX, bulletY, Tmp.v6.x, Tmp.v6.y) / bullet.range),
			angle = aimAngle + Mathf.range(inaccuracy);
		
		Bullet shootBullet = bullet.create(e, team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, Tmp.v6.x, Tmp.v6.y);
		
		bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor);
		bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor);
	}
	
	@Override
	public void trigger(WorldEvent e){
		Team team = e.team;
		Position source = source(e);
		if(source == null)return;
		if(!Vars.headless && team != Vars.player.team())warnOnTrigger(e);
		
		Vec2 sr = new Vec2().set(source.getX(), source.getY());
		Vec2 t = new Vec2().set(e.x, e.y);
		
		projectiles.each((b, s) -> {
			e.intData = 0;
			s.shoot(e.intData, (xOffset, yOffset, angle, delay, mover) -> {
				if(delay > 0f){
					Time.run(delay, () -> bullet(e, team, b, sr, t, mover));
				}else{
					bullet(e, team, b, sr, t, mover);
				}
			}, () -> e.intData++);
		});
		
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
					Cell<TextButton> b = t2.button(Core.bundle.get("mod.ui.raid"), new TextureRegionDrawable(icon()), Styles.cleart, LEN - OFFSET, () -> showAsDialog(e)).growX().padLeft(OFFSET).padRight(OFFSET / 2).left().color(color);
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
	public void warnHUD(WorldEvent event){
		NHUIFunc.showLabel(2.5f, t -> {
			Color color = event.team.color;
			
			if(event.team != Vars.player.team())callSound.play();
			
			t.background(Styles.black5);
			
			t.table(t2 -> {
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(color);
				t2.image(icon()).fill().color(color);
				t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(color);
			}).growX().pad(OFFSET / 2).fillY().row();
			
			t.table(l -> {
				l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>")).color(color).padBottom(4).row();
			}).growX().fillY();
		});
	}
	
	@Override
	public void warnOnTrigger(WorldEvent event){
		TableFunc.showToast(new TextureRegionDrawable(icon(), 0.2f), "[#" + event.team.color + "]" + Core.bundle.get("mod.ui.raid") + " []" + event.coordText(), callSound);
	}
	
	public void triggerNet(WorldEvent event){
		event.reload = reloadTime * 0.95f;
	}
	
	@Override
	public void infoTable(Table table){
		table.table(i -> {
			i.table(Tex.underline, t -> {
				t.align(Align.topLeft);
				t.add('<' + Core.bundle.get("mod.ui.raid") + '>').color(Pal.accent).center().growX().fillY().row();
				t.add(Core.bundle.get("mod.ui.raid.description")).color(Color.lightGray).center().growX().fillY().row();
				t.image().color(Pal.accent).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
				t.add("[lightgray]" + Core.bundle.get("stat.launchtime") + ": [accent]" + TableFunc.format(reloadTime / Time.toSeconds) + "[]" + Core.bundle.get("unit.seconds")).left().row();
			}).grow().padBottom(6).row();
			projectiles.each((bulletType, s) -> {
				i.table(Tex.sideline, t -> {
					int shots = NHUIFunc.getTotalShots(s);
					t.align(Align.topLeft);
					t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-air") + ": " + TableFunc.judge(bulletType.collidesAir && bulletType.collides)).left().row();
					t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-ground") + ": " + TableFunc.judge(bulletType.collidesGround && bulletType.collides)).left().row();
					t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-tile") + ": " + TableFunc.judge(bulletType.collidesTiles)).left().row();
					
					t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
					
					t.add("[lightgray]" + Core.bundle.get("mod.ui.absorbable") + ": " + TableFunc.judge(bulletType.absorbable)).left().row();
					t.add("[lightgray]" + Core.bundle.get("mod.ui.hittable") + ": " + TableFunc.judge(bulletType.hittable)).left().row();
					
					t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
					
					t.add("[lightgray]" + Core.bundle.format("mod.ui.estimated-max-damage", UI.formatAmount((long)(NHUIFunc.estimateBulletDamage(bulletType, shots, true))))).left().row();
					
					t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).row();
					
					t.table(b -> NHUIFunc.ammo(b, "[lightgray]*[accent]" + shots, bulletType, NHContent.raid)).row();
				}).fill().padBottom(12f).row();
			});
		}).fill();
	}
	
	//	@Override
//	public void infoTable(Table table){
//		table.table(t -> {
//			t.add('<' + Core.bundle.get("mod.ui.intervention") + '>').color(Pal.accent).center().growX().fillY().row();
//			t.add(Core.bundle.get("mod.ui.fleet.description")).color(Color.lightGray).center().growX().fillY().row();
//			t.image().color(Pal.accent).pad(OFFSET / 2).growX().height(OFFSET / 4).padLeft(OFFSET / 2).padRight(OFFSET / 2).row();
//
//			for(ObjectIntMap.Entry<UnitType> entry : spawner.entries()){
//				t.add(new IconNumDisplay(entry.key.fullIcon, entry.value, entry.key.localizedName)).left().row();
//			}
//		}).fill().row();
//	}
}
