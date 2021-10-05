package newhorizon.units;

import arc.Core;
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
import newhorizon.func.DrawFunc;

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
			
			NHFx.absorbFix.at(trait.x, trait.y, 0, paramUnit.team.color);
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
		
		angle = Mathf.slerpDelta(angle, unit.angleTo(unit.aimX, unit.aimY), rotateSpeed);
		
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
		}else{
			radiusScale = 0f;
		}
	}
	
	@Override
	public void draw(Unit unit){
		checkRadius(unit);
		
		if(unit.shield > 0){
			float f = realRad / radius;
			
			Tmp.v2.trns(unit.rotation, 0, x);
			Tmp.v1.trns(unit.rotation, y);
			for(int i : x == 0 ? DrawFunc.oneArr : Mathf.signs){
				Draw.color(unit.team.color, Color.white, Mathf.clamp(alpha));
				Draw.z(Layer.shields);
				if(Core.settings.getBool("animatedshields")){
					DrawFunc.fillCirclePercent(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, unit.x, unit.y, realRad, angleDst / 360f, angle - angleDst / 2f);
				}else{
					Lines.stroke(f * 1.5f);
					Draw.alpha(0.09f);
					DrawFunc.fillCirclePercent(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, unit.x, unit.y, realRad, angleDst / 360f, angle - angleDst / 2f);
					Draw.alpha(1f);
					DrawFunc.circlePercent(unit.x, unit.y, realRad, angleDst / 360f, angle - angleDst / 2f);
					Lines.line(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, left.x, left.y, false);
					Lines.line(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, right.x, right.y, false);
				}
				Fill.circle(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 2f);
				Draw.z(Layer.effect);
				Lines.stroke(f * 0.82f);
				Fill.circle(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 4.2f);
				DrawFunc.circlePercentFlip(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 6.5f, Time.time, 20f);
				DrawFunc.circlePercentFlip(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 8f, -Time.time * 1.25f, 30f);
				Draw.color(Color.white);
				Fill.circle(unit.x + Tmp.v1.x + Tmp.v2.x * i, unit.y + Tmp.v1.y + Tmp.v2.y * i, f * 2.8f);
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
