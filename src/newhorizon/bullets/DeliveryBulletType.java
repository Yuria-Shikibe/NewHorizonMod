package newhorizon.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import newhorizon.block.special.Delivery;
import newhorizon.content.NHFx;
import newhorizon.effects.EffectTrail;
import newhorizon.func.NHSetting;

public class DeliveryBulletType extends BulletType{
	private static final float div = 8f;
	
	public static float rotateSpeed = 0.15f;
	protected TextureRegion region;
	public DeliveryBulletType(TextureRegion region){
		super();
		this.speed = 4.6f;
		this.region = region;
		this.lifetime = 60f;
		this.despawnEffect = NHFx.boolSelector;
		trailColor = null;
		trailEffect = NHFx.trail;
		trailChance = 0.7f;
		trailParam = 2.1f;
		homingDelay = 3f;
		collides = collidesGround = collidesTeam = collidesTiles = absorbable = false;
	}
	
	private Color getTrailColor(Bullet b){
		return trailColor == null ? b.team.color : trailColor;
	}
	
	@Override
	public void init(){
		super.init();
		if(despawnEffect == NHFx.boolSelector)despawnEffect = new Effect(35f, e -> {
			Draw.mixcol(e.color, 1);
			Draw.rect(region, e.x, e.y, region.width * Draw.scl * e.fout(), region.height * Draw.scl * e.fout(), e.rotation - 90);
		});
	}
	
	@Override
	public void init(Bullet b){
		super.init(b);
		if(!(b.data instanceof Delivery.DeliveryData))b.remove();
		Delivery.DeliveryData data = (Delivery.DeliveryData)b.data();
		if(data.t == null)data.t = new EffectTrail(region.height / 6, (region.width / 40f)).clear();
		if(data.to == null)despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
		if(data.needRotate){
			b.lifetime += 180 / (rotateSpeed * 50f);
			b.vel.setLength(0.001f);
		}
	}
	
	public void rotateBullet(Bullet b, @Nullable Position target, boolean addLife){
		if(target != null)b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), rotateSpeed * Time.delta * 50f));
	}
	
	@Override
	public void draw(Bullet b){
		if(!(b.data instanceof Delivery.DeliveryData))b.remove();
		Delivery.DeliveryData data = (Delivery.DeliveryData)b.data();
		Tmp.v1.trns(b.rotation(), -region.height / div);
		float sin = Mathf.absin(Time.time, 1f, 3);
		float scl = (1 + b.fslope() * 0.7f);
		float f = Mathf.curve(b.fin(), 0.05f, 0.1f) * scl;
		float h = b.fslope() * Layer.block / 2f + 5;
		
		
		Draw.color(getTrailColor(b));
		
		Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (data.t.width * 1.2f + sin / 2f) * f);
		for(int i : Mathf.signs){
			Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 4.5f * Mathf.curve(b.fout(), 0f, 0.1f), 23 * (1.6f + sin / 3.2f) * f, (1 + i) * 90);
		}
		data.t.draw(getTrailColor(b));
		Draw.z(Layer.blockOver);
		Draw.rect(region, b.x, b.y, region.width * Draw.scl * scl, region.height * Draw.scl * scl, b.rotation() - 90.0F);
		Draw.reset();
		
		Drawf.shadow(region, b.x - h + Tmp.v1.x, b.y - h + Tmp.v1.y, b.rotation() - 90f);
	}
	
	@Override
	public void update(Bullet b){
		if(!(b.data instanceof Delivery.DeliveryData)){
			b.remove();
			return;
		}
		Delivery.DeliveryData data = (Delivery.DeliveryData)b.data();
		if(!data.needRotate || Angles.angleDist(b.rotation(), data.from.angleTo(data.to)) < 5f){
			b.vel.setLength(speed * (Mathf.curve(b.fin(), 0f, 0.05f) + 0.001f));
			rotateBullet(b, data.to, false);
			if(b.x < 0 || b.x > Vars.world.unitWidth() || b.y < 0 || b.y > Vars.world.unitHeight() || (data.to == null || (b.dst(data.to) < Vars.tilesize * 1.25f && data.needRotate))){
				b.time(b.lifetime());
			}
		}else rotateBullet(b, data.to, true);
		
		if(data.needRotate || b.time > homingDelay){
			Tmp.v1.trns(b.rotation(), -region.height / div);
			data.t.update(b.x + Tmp.v1.x, b.y + Tmp.v1.y);
			if(trailChance > 0.0F && Mathf.chanceDelta(trailChance)){
				trailEffect.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, trailParam, getTrailColor(b));
			}
		}
	}
	
	@Override
	public void despawned(Bullet b){
		if(!(b.data instanceof Delivery.DeliveryData)){
			b.remove();
			return;
		}
		Delivery.DeliveryData data = (Delivery.DeliveryData)b.data();
		if(data.to != null){
			if(!data.transportBack){
				for(int i = 0; i < Vars.content.items().size; ++i){
					Call.transferItemTo(null, Vars.content.item(i), Mathf.clamp(data.items[i], 0, data.to.getMaximumAccepted(Vars.content.item(i)) - data.to.items.get(i)), b.x, b.y, data.to);
				}
			}else{
				for(int i = 0; i < Vars.content.items().size; ++i){
					if(data.items[i] > 0){
						int num = Mathf.clamp(data.to.items.get(i), 0, data.from.getMaximumAccepted(Vars.content.item(i)) );
						Fx.itemTransfer.at(data.to.x, data.to.y, num, Vars.content.item(i).color, new Vec2().set(b));
						data.to.items.remove(Vars.content.item(i), num);
						data.items[i] = num;
						NHSetting.debug(() -> Log.info(data.to + " | " + num));
					}
				}
			}
		}
		Tmp.v1.trns(b.rotation(), -region.height / div);
		if(data.needRotate || !data.transportBack){
			despawnEffect.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), b.team.color);
			data.t.disappear(getTrailColor(b));
		}else{
			float lifeScl = data.to.dst(data.from) / range();
			Delivery.DeliveryData dataAdapt = new Delivery.DeliveryData(data, true);
			dataAdapt.t = data.t;
			NHSetting.debug(() -> Log.info(dataAdapt));
			create(b, b.team, b.x, b.y, b.rotation(), 1, 1, lifeScl, dataAdapt);
		}
	}
}
