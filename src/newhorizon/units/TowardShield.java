package newhorizon.units;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.Turret;
import newhorizon.content.NHFx;
import newhorizon.func.DrawFuncs;

@SuppressWarnings("SuspiciousNameCombination")
public class TowardShield extends Ability{
	public float radius = 60f;
	/** Shield regen speed in damage/tick. */
	public float regen = 0.1f;
	/** Maximum shield. */
	public float max = 200f;
	/** Cooldown after the shield is broken, in ticks. */
	public float cooldown = 60f * 5;
	
	public float angleDst = 120f;
	
	public float rotateSpeed = 0.025f;
	
	public float x, y;
	
	protected float radiusScale, alpha, angle;
	protected Vec2 left = new Vec2(), right = new Vec2();
	
	private static float realRad;
	private static Unit paramUnit;
	private static TowardShield paramField;
	private static final Cons<Bullet> shieldConsumer = trait -> {
		if(trait.team != paramUnit.team && trait.type.absorbable && Angles.within(paramUnit.angleTo(trait), paramField.angle, paramField.angleDst / 2) && paramUnit.shield > 0){
			trait.absorb();
			Fx.absorb.at(trait);
			
			Tmp.v2.trns(paramUnit.rotation, 0, paramField.x);
			Tmp.v1.trns(paramUnit.rotation, paramField.y).add(paramUnit);
			Fx.chainLightning.at(trait.x, trait.y, 0, paramUnit.team.color, Tmp.v1.cpy().add(Tmp.v2));
			if(paramField.x != 0)Fx.chainLightning.at(trait.x, trait.y, 0, paramUnit.team.color, Tmp.v1.cpy().add(Tmp.v2.inv()));
			//break shield
			if(paramUnit.shield <= trait.damage()){
				paramUnit.shield -= paramField.cooldown * paramField.regen;
				
				NHFx.circleShieldBreak.at(paramUnit.x, paramUnit.y, realRad, paramUnit.team.color, paramUnit);
			}
			
			paramUnit.shield -= trait.damage();
			paramField.alpha = 1f;
		}
	};
	
	@Override
	public TowardShield copy(){
		Ability out = super.copy();
		if(!(out instanceof TowardShield))return null;
		TowardShield o = (TowardShield)out;
		o.left = new Vec2();
		o.right = new Vec2();
		return o;
	}
	
	public TowardShield(float radius, float regen, float max, float cooldown, float angleDst, float rotateSpeed, float x, float y){
		this.radius = radius;
		this.regen = regen;
		this.max = max;
		this.cooldown = cooldown;
		this.angleDst = angleDst;
		this.rotateSpeed = rotateSpeed;
		this.x = x;
		this.y = y;
	}
	
	public TowardShield(float radius, float regen, float max, float cooldown){
		this.radius = radius;
		this.regen = regen;
		this.max = max;
		this.cooldown = cooldown;
	}
	
	TowardShield(){}
	
	@Override
	public void update(Unit unit){
		Teamc target = Units.bestTarget(unit.team, unit.x, unit.y, radius * 5, u -> true, t -> t instanceof Turret.TurretBuild, (u, x, y) -> -unit.dst(u));
		
		if(target == null)angle = Mathf.slerpDelta(angle, unit.rotation, rotateSpeed);
		else angle = Mathf.slerpDelta(angle, unit.angleTo(target), rotateSpeed);
		
		if(unit.shield < max){
			unit.shield += Time.delta * regen;
		}
		
		alpha = Math.max(alpha - Time.delta/10f, 0f);
		
		if(unit.shield > 0){
			radiusScale = Mathf.lerpDelta(radiusScale, 1f, 0.06f);
			paramUnit = unit;
			paramField = this;
			checkRadius(unit);
			
			Groups.bullet.intersect(unit.x - realRad, unit.y - realRad, realRad * 1.85f, realRad * 1.85f, shieldConsumer);
			this.left.trns(angle - angleDst / 2f, realRad).add(unit);
			this.right.trns(angle + angleDst / 2f, realRad).add(unit);
			Tmp.v2.trns(unit.rotation, 0, x);
			Tmp.v1.trns(unit.rotation, y);
			if(Mathf.chanceDelta(0.2))Fx.chainLightning.at(unit.x + Tmp.v1.x + Tmp.v2.x, unit.y + Tmp.v1.y + Tmp.v2.y, unit.rotation, unit.team.color, right);
			if(Mathf.chanceDelta(0.2))Fx.chainLightning.at(unit.x + Tmp.v1.x - Tmp.v2.x, unit.y + Tmp.v1.y - Tmp.v2.y, unit.rotation, unit.team.color, left);
		}else{
			radiusScale = 0f;
		}
	}
	
	@Override
	public void draw(Unit unit){
		checkRadius(unit);
		
		if(unit.shield > 0){
			Draw.z(Layer.effect);
			
			Draw.color(unit.team.color, Color.white, Mathf.clamp(alpha));
			
			float f = realRad / radius;
			Lines.stroke(f * 3f);
			DrawFuncs.circlePercent(unit.x, unit.y, realRad, angleDst / 360f, angle - angleDst / 2f);
			
			Lines.stroke(f * 0.82f);
			Tmp.v2.trns(unit.rotation, 0, x);
			Tmp.v1.trns(unit.rotation, y);
			for(int i : x == 0 ? DrawFuncs.oneArr : Mathf.signs){
				Fill.circle(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 4.2f);
				DrawFuncs.circlePercentFlip(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 6.5f, Time.time, 20f);
				DrawFuncs.circlePercentFlip(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 8f, -Time.time * 1.25f, 30f);
			}
		}
	}
	
	@Override
	public void displayBars(Unit unit, Table bars){
		bars.add(new Bar("stat.shieldhealth", Pal.accent, () -> unit.shield / max)).row();
	}
	
	public void checkRadius(Unit unit){
		//timer2 is used to store radius scale as an effect
		realRad = radiusScale * radius;
	}
	
}
