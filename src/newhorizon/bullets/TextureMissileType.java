package newhorizon.bullets;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.NewHorizon;
import newhorizon.content.NHFx;
import newhorizon.content.NHLoader;
import newhorizon.effects.EffectTrail;
import newhorizon.func.NHSetting;

public class TextureMissileType extends NHTrailBulletType{
	public float div = 7f;
	public float effectDelay = 5f;
	public boolean drawLightTrail;
	
	public TextureMissileType(){
		super();
	}
	
	public TextureMissileType(float speed, float damage, String name){
		super(speed, damage, name);
		NHLoader.put(name);
		String[] s = name.split("@");
		sprite = NewHorizon.configName(s[0]);
		homingPower = 0.08f;
		homingRange = 400f;
		homingDelay = 8;
		absorbable = false;
		trailEffect = NHFx.trail;
		trailChance = 0.5f;
		trailParam = 2.4f;
		height = width = 1;
		despawnEffect = new Effect(35f, e -> {
			Draw.mixcol(trailColor, 1);
			Draw.rect(backRegion, e.x, e.y, backRegion.width * Draw.scl * e.fout(), backRegion.height * Draw.scl * e.fout(), e.rotation - 90);
		});
		hitShake = 4f;
		hitSound = Sounds.explosionbig;
		hitEffect = Fx.flakExplosionBig;
		drawSize = 60f;
	}
	
	public TextureMissileType(String name){
		this(1, 1, name);
	}
	
	@Override
	public void load(){
		NHSetting.debug(() -> Log.info(sprite + Core.atlas.find(sprite)));
		if(Vars.headless)super.load();
	}
	
	@Override
	public void init(){
		if(!Vars.headless || (backRegion != null && backRegion.found())){
			backRegion = Core.atlas.find(sprite);
			if(trailLength < 0) trailLength = (int)(backRegion.height * height / 5.2f);
			if(trailWidth < 0) trailWidth = backRegion.width * width / 38f;
		}
		drawSize = Math.max(drawSize, 2.5f * trailLength * speed);
		super.init();
	}
	
	@Override
	public void init(Bullet b){
		if(killShooter && b.owner() instanceof Healthc){
			Healthc h = (Healthc)b.owner;
			h.kill();
		}
		
		if(instantDisappear){
			b.time = lifetime;
		}
		
		b.data(new EffectTrail(trailLength, trailWidth, trailColor, trailToColor));
	}
	
	@Override
	public void draw(Bullet b){
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail trail = (EffectTrail)b.data;
		Tmp.v1.trns(b.rotation(), -backRegion.height * height / div);
		float sin = Mathf.absin(Time.time, 1f, 3f);
		float f = Mathf.curve(b.fin(), 0.05f, 0.1f);
		float h = b.fslope() * Layer.block / 2f + 5;
		
		Draw.color(trailColor);
		
		Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (trail.width + sin / 2f + 0.1f) * f);
		
		if(drawLightTrail)for(int i : Mathf.signs)Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 4.5f * Mathf.curve(b.fout(), 0f, 0.1f), 23 * (1.6f + sin / 3.2f) * f, (1 + i) * 90);
		
		trail.draw();
		Draw.z(Layer.blockOver - 1f);
		Draw.color(Pal.shadow);
		Draw.rect(backRegion, b.x - h, b.y - h, backRegion.width * Draw.scl * width, backRegion.height * Draw.scl * height, b.rotation() - 90.0F);
		Draw.color();
		Draw.rect(backRegion, b.x, b.y, backRegion.width * Draw.scl * width, backRegion.height * Draw.scl * height, b.rotation() - 90.0F);
		Draw.reset();
		
	}
	
	@Override
	public void update(Bullet b){
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail trail = (EffectTrail)b.data;
		if(!Vars.headless && b.time > effectDelay){
			float x = Angles.trnsx(b.rotation(), -backRegion.height / div), y = Angles.trnsy(b.rotation(), -backRegion.height / div);
			if(b.timer(3, Mathf.clamp(1 / Time.delta, 0, 1))){
				trail.update(b.x + x, b.y + y);
			}
			
			if (trailChance > 0.0F && Mathf.chanceDelta(trailChance)) {
				trailEffect.at(b.x + x, b.y + y, trailParam, trailColor);
			}
		}
		
		if (homingPower > 1.0E-4F && b.time >= homingDelay) {
			Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange, (e) -> e.isGrounded() && collidesGround || e.isFlying() && collidesAir, (t) -> collidesGround);
			if (target != null) {
				b.vel.setAngle(Mathf.slerpDelta(b.rotation(), b.angleTo(target), homingPower));
			}
		}
		
		if (weaveMag > 0.0F) {
			b.vel.rotate(Mathf.sin(b.time + 3.1415927F * weaveScale / 2.0F, weaveScale, weaveMag * (float)(Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
		}
		
		if(!(velocityEnd <= speed))b.vel.setLength(2 * speed - velocityEnd + (Mathf.curve(b.fin(), accelerateBegin, accelerateEnd) * velocityEnd));
	}
	
	@Override
	public void despawned(Bullet b){
		super.despawned(b);
		if(Vars.headless)return;
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail trail = (EffectTrail)b.data;
		float x = Angles.trnsx(b.rotation(), -backRegion.height / div), y = Angles.trnsy(b.rotation(), -backRegion.height / div);
		trail.disappear();
		Fx.artilleryTrail.at(b.x + x, b.y + y, trail.width * 1.2f, trailColor);
	}
}
