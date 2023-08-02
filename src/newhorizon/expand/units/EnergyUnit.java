package newhorizon.expand.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.gen.Groups;
import mindustry.gen.PayloadUnit;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import newhorizon.NHGroups;
import newhorizon.NHSetting;
import newhorizon.content.NHFx;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.func.NHFunc;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class EnergyUnit extends PayloadUnit{
	public static Effect teleport = new Effect(90, 150, e -> {
		color(e.color, Color.white, e.fin() * 0.7f);
		Fill.circle(e.x, e.y, e.fout() * e.rotation * 1.25f);
		stroke(e.fout() * 3.2f);
		circle(e.x, e.y, e.fin() * e.rotation * 1.75f);
		stroke(e.fout() * 2.5f);
		circle(e.x, e.y, e.fin() * e.rotation * 1.5f);
		stroke(e.fout() * 3.2f);
		randLenVectors(e.id, (int)e.rotation * 2, e.rotation / 1.5f + e.rotation * e.fin() * 2.75f, (x, y) -> {
			lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
		});
		
		Fx.rand.setSeed(e.id + 100);
		
		randLenVectors(e.id + 100, (int)e.rotation, e.rotation / 2f + e.rotation * e.fin() * 3f, (x, y) -> {
			Fill.circle(e.x + x, e.y + y, Fx.rand.random(e.rotation, e.rotation * 2f) * e.foutpow());
		});
		
		color(Color.black);
		Fill.circle(e.x, e.y, e.fout() * e.rotation * 0.7f);
	});
	
	public static Effect teleportTrans = new Effect(45, 600, e -> {
		if(!(e.data instanceof Vec2))return;
		Vec2 data = e.data();
		float angle = Angles.angle(e.x, e.y, data.x, data.y);
		float dst = Mathf.dst(e.x, e.y, data.x, data.y);
		Rand rand = new Rand(e.id);
		Tmp.v1.set(data).sub(e.x, e.y).nor().scl(Vars.tilesize * 3f);
		stroke(Mathf.curve(e.fout(), 0, 0.3f) * 1.75f);
		color(e.color, Color.white, e.fout() * 0.75f);
		for(int i = 1; i < dst / Vars.tilesize / 3f; i++){
			for(int j = 0; j < (int)e.rotation / 3; j++){
				Tmp.v4.trns(angle, rand.random(e.rotation / 4, e.rotation / 2), rand.range(e.rotation));
				Tmp.v3.set(Tmp.v2.set(Tmp.v1)).scl(i).add(Tmp.v2.scl(rand.range(0.5f))).add(Tmp.v4).add(e.x, e.y);
				lineAngle(Tmp.v3.x, Tmp.v3.y, angle - 180, e.fout(Interp.pow2Out) * 18 + 8);
			}
		}
	});
	
	public float reload = 240;
	public float teleportMinRange = 180f;
	public float teleportRange = 400f;
	
	public static final float effectTriggerLen = 40f;
	
	protected transient Vec2 lastPos = new Vec2();
	protected float reloadValue = 0;
	protected float lastHealth = 0;
	protected Interval timer = new Interval(5);
	
	protected Trail[] trails = {};
	
	@Override public int classId(){return EntityRegister.getID(getClass());}
	
	@Override
	public void destroy(){
		super.destroy();
		
		for(int i = 0; i < trails.length; i++){
			Tmp.c1.set(team.color).mul(i * 0.045f).lerp(Color.white, 0.075f * i);
			Fx.trailFade.at(x, y, type.trailScl, team.color, trails[i].copy());
		}
		
		NHFx.energyUnitBlast.at(x, y, hitSize * 4, team.color);
		
		Vec2 v = new Vec2().set(this);
		
		for(int i = 0; i < NHFx.energyUnitBlast.lifetime / 6; i++){
			Time.run(i * 6, () -> {
				for(int j = 0; j < 3; j++){
					Lightning.create(team, team.color, 120f, v.x, v.y, Mathf.random(360), Mathf.random(12, 28));
					NHFunc.randFadeLightningEffect(v.x, v.y, Mathf.random(360), Mathf.random(12, 28), team.color, Mathf.chance(0.5));
				}
			});
		}
	}
	
	@Override
	public void setType(UnitType type){
		super.setType(type);
		
		if(trails.length != 3){
			trails = new Trail[3];
			for(int i = 0; i < trails.length; i++){
				trails[i] = new Trail(type.trailLength);
			}
		}
	}
	
	@Override
	public void add(){
		super.add();
		
		lastPos.set(this);
	}
	
	@Override
	public void draw(){
		Draw.z(Layer.bullet);
		
		if(NHSetting.enableDetails()){
			for(int i = 0; i < trails.length; i++){
				Tmp.c1.set(team.color).mul(1 + i * 0.005f).lerp(Color.white, 0.015f * i + Mathf.absin(4f, 0.3f) +  Mathf.clamp(hitTime) / 5f);
				trails[i].drawCap(Tmp.c1, type.trailScl);
				trails[i].draw(Tmp.c1, type.trailScl);
			}
		}
		
		super.draw();
	}
	
	protected void updateTeleport(){
		if(!hasEffect(NHStatusEffects.intercepted) && !isPlayer()){
			reloadValue += Time.delta;
			
			Teamc target = Units.closestEnemy(team, x, y, teleportRange * 2f, b -> true);
			int[] num = {0};
			float[] damage = {0};
			
			reloadValue += Math.max(lastHealth - health, 0) / 2f;
			lastHealth = health;
			
			if(timer.get(5f)) Groups.bullet.intersect(x - teleportRange, y - teleportRange, teleportRange * 2f, teleportRange * 2f, bullet -> {
				if(bullet.team == team)return;
				num[0]++;
				damage[0] += bullet.damage();
			});
			
			if(teleportValid() && (target != null || ((hitTime > 0 || num[0] > 4 || damage[0] > reload / 2))) && (!isLocal() || Vars.mobile)){
				float dst = target == null ? teleportRange + teleportMinRange : dst(target) / 2f;
				float angle = target == null ? rotation : angleTo(target);
				Tmp.v2.trns(angle + Mathf.range(1) * 45,dst * Mathf.random(1, 2), Mathf.range(0.2f) * dst).clamp(teleportMinRange, teleportRange).add(this).clamp(-Vars.finalWorldBounds, -Vars.finalWorldBounds, Vars.world.unitHeight() + Vars.finalWorldBounds, Vars.world.unitWidth() + Vars.finalWorldBounds);
				
				teleport(Tmp.v2.x, Tmp.v2.y);
			}
		}
	}
	
	public boolean teleportValid(){
		return reloadValue > reload;
	}
	
	public void teleport(float x, float y){
		NHFunc.teleportUnitNet(this, x, y, angleTo(x, y), isPlayer() ? getPlayer() : null);
		reloadValue = 0;
	}
	
	@Override
	public void update(){
		super.update();
		
		Seq<GravityTrapField> fields = new Seq<>();
		NHGroups.gravityTraps.intersect(x - hitSize / 4, y - hitSize / 4, hitSize / 2, hitSize / 2, fields);
		
		if(timer.get(1, 8))fields.each(f -> f.team() != team && f.active(), f -> {
			Vec2 target = new Vec2(x, y);
			NHFx.slidePoly.at(f.x, f.y, hitSize, f.team().color, target);
			NHFx.chainLightningFade.at(f.x, f.y, 12, f.team().color, target);
			impulseNet(Tmp.v1.set(this).sub(f).nor().scl(50 * (f.range * 0.75f - f.dst(this))));
			damage(35f, true);
			apply(NHStatusEffects.intercepted, 60f);
		});
		
		if(!Vars.headless && lastPos.dst(this) > effectTriggerLen){
			Sounds.plasmaboom.at(this);
			Sounds.plasmaboom.at(lastPos);
			
			teleport.at(x, y, hitSize / 2, team.color);
			teleport.at(lastPos.x, lastPos.y, hitSize / 2, team.color);
			teleportTrans.at(lastPos.x, lastPos.y, hitSize / 2, team.color, new Vec2().set(this));
			
			for(Trail t : trails){
				Fx.trailFade.at(lastPos.x, lastPos.y, type.trailScl, team.color, t.copy());
				t.clear();
			}
		}
		
		lastPos.set(this);
		
		Rand rand = NHFunc.rand;
		rand.setSeed(id);
		
		if(NHSetting.enableDetails() && !Vars.headless){
			for(int i = 0; i < trails.length; i++){
				Trail trail = trails[i];
				
				float scl = rand.random(0.75f, 1.5f) * Mathf.sign(rand.range(1)) * (i + 1) / 1.25f;
				float s = rand.random(0.75f, 1.25f);
				
				Tmp.v1.trns(
						Time.time * scl * rand.random(0.5f, 1.5f) + i * 360f / trails.length + rand.random(360),
						hitSize * (1.1f + 0.5f * i) * 0.75f
				).add(this).add(
						Mathf.sinDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * hitSize / 3 * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f),
						Mathf.cosDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * hitSize / 3 * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f)
				);
				trail.update(Tmp.v1.x, Tmp.v1.y, 1 + Mathf.absin(4f, 0.2f));
			}
		}
		
		if(Mathf.chanceDelta(0.15) && healthf() < 0.6f)NHFunc.randFadeLightningEffect(x, y, Mathf.range(hitSize, hitSize * 4), Mathf.range(hitSize / 4, hitSize / 2), team.color, Mathf.chance(0.5));
		
		if(!Vars.net.client() || isLocal())updateTeleport();
	}
	
	@Override
	public void damage(float amount, boolean withEffect){
		super.damage(amount, withEffect);
	}
}
