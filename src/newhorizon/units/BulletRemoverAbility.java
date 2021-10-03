package newhorizon.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.content.NHFx;
import newhorizon.func.NHFunc;
import newhorizon.vars.EventTriggers;

public class BulletRemoverAbility extends Ability{
	public Color scanColor = Pal.heal;
	public float spawnTime = 60f, spawnX, spawnY;
	public static final float scanTime = 120f;
	public float range = 800f;
	public float minTriggerDamage = 1000;
	public float minTriggerNum = 5;
	public static final Interp scanInterp = Interp.pow5;
	
	protected float reload;
	protected Interval timer = new Interval(1);
	protected Seq<Bullet> bullets = new Seq<>();
	
	protected static final IntSeq targetedID = new IntSeq();
	
	static{
		EventTriggers.actBeforeLoad.add(targetedID::clear);
	}
	
	public BulletRemoverAbility(Color scanColor, float spawnTime, float range, float minTriggerDamage){
		this.scanColor = scanColor;
		this.spawnTime = spawnTime;
		this.range = range;
		this.minTriggerDamage = minTriggerDamage;
	}
	
	public BulletRemoverAbility(Color scanColor, float spawnTime, float spawnX, float spawnY, float range, float minTriggerDamage){
		this.scanColor = scanColor;
		this.spawnTime = spawnTime;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		this.range = range;
		this.minTriggerDamage = minTriggerDamage;
	}
	
	@Override
	public BulletRemoverAbility copy(){
		BulletRemoverAbility n = (BulletRemoverAbility)super.copy();
		n.timer = new Interval(1);
		n.bullets = new Seq<>();
		return n;
	}
	
	protected static Effect scan = new Effect(scanTime, 2000, e -> {
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		Draw.color(e.color);
		
		float f = Interp.pow4Out.apply(Mathf.curve(e.fin(), 0, 0.3f));
		float stroke = Mathf.clamp(e.rotation / 80, 3, 8);
		
		Lines.stroke(stroke * e.fout() + 1 * e.fout(Interp.pow5In));
		Lines.circle(e.x, e.y, e.rotation * f);
		
		Lines.stroke(stroke * Mathf.curve(e.fin(), 0, 0.1f) * Mathf.curve(e.fout(), 0.05f, 0.15f));
		float angle = 360 * e.fin(scanInterp);
		Lines.lineAngle(e.x, e.y, angle, e.rotation * f);
		Lines.stroke(stroke * Mathf.curve(e.fin(), 0, 0.1f) * e.fout(0.05f));
		Angles.randLenVectors(e.id, (int)(e.rotation / 40), e.rotation * 0.85f * f, angle, 0, (x, y) -> {
			Lines.lineAngle(e.x + x, e.y + y, angle, e.rotation * rand.random(0.05f, 0.15f) * e.fout(0.15f));
		});
		
		Fill.circle(e.x, e.y, Lines.getStroke());
		Draw.color(Color.white);
		Fill.circle(e.x, e.y, Lines.getStroke() / 2);
		
		Drawf.light(e.x, e.y, e.rotation * f * 1.35f * e.fout(0.15f), e.color, 0.6f);
	}).followParent(true);
	
	protected static Effect targeted = new Effect(45f, e -> {
		Draw.color(e.color);
		Lines.stroke(4 * Mathf.curve(e.fin(), 0, 0.1f) * e.fout());
		Lines.square(e.x, e.y, e.rotation * e.fout(), 315 * e.fout(Interp.pow3In));
	}).followParent(true);
	
	public void update(Unit unit){
		reload += Time.delta;
		
		if(reload >= spawnTime){
			bullets.clear();
			Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2, range * 2, b -> {
				if(b.team != unit.team && b.type.hittable && !targetedID.contains(b.id) && b.within(unit, range)){
					bullets.add(b);
				}
			});
			
			if(bullets.size < minTriggerNum)return;
			float[] damageTotal = new float[1];
			bullets.each(b -> damageTotal[0] += b.damage());
			
			if(damageTotal[0] < minTriggerDamage)return;
			scan.at(unit.x, unit.y, range, scanColor, unit);
			reload = 0;
			
			bullets.each(b -> {
				b.damage = 1;
				targetedID.add(b.id);
				targeted.at(b.x, b.y, Mathf.clamp(b.type.hitSize, 16, 40), scanColor, b);
				b.drag = 0.025f;
				b.lifetime = Math.max(scanTime + 75, b.lifetime - b.time) + b.time;
			});
			
			Seq<Bullet> bs = bullets.copy();
			
			Time.run(scanTime + 10, () -> {
				bs.each(b -> {
					if(b.hit)return;
					float angleT = unit.angleTo(b) / 6;
					
					Time.run(angleT, () -> {
						if(b.hit || Mathf.equal(b.x, 0) || Mathf.equal(b.y, 0))return;
						NHFx.square45_4_45.at(b.x, b.y, 0, scanColor);
						Fx.chainLightning.at(b.x, b.y, 0, scanColor, unit);
						unit.heal(Math.max(b.damage / 10, 0));
						b.time(b.lifetime());
					});
				});
			});
		}
	}
	
	@Override
	public void draw(Unit unit){
		Draw.z(Layer.effect);
		
		float fin = Mathf.clamp(reload / spawnTime);
		Draw.color(scanColor);
		Fill.circle(unit.x, unit.y, 12f * fin);
		Draw.color(Color.white);
		Fill.circle(unit.x, unit.y, 8 * fin);
	}
}
