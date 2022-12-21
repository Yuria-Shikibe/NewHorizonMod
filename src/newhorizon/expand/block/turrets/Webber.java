package newhorizon.expand.block.turrets;

import arc.func.FloatFloatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import newhorizon.content.NHFx;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.*;

public class Webber extends TractorBeamTurret{
	public float circleRad = 12f;
	public float num = 3;
	public float scale = 6f;
	public float mag = 8f;
	public float wavelength = 6f;
	public float stroke = 1.45f;
	public float scaleSpeedPerMove = 0.75f;
	public float rotateSpeedScl = 0.85f;
	public float weaveSpeedScl = 0.3f;
	public float zScl = 0.25f;
	
	public int circleNum = 2;
	public float strokeScl = 0.65f;
	public Interp moveInterp = Interp.pow3In;
	public float moveTime = 60f;
	
	public float effectChance = 0.13f;
	public Effect affectedEffect = NHFx.hitSpark;
	public FloatFloatf cal = d -> 1;
	
	public Webber(String name){
		super(name);
		
		status = StatusEffects.unmoving;
		statusDuration = 30f;
	}
	
	public class WebberBuild extends TractorBeamBuild{
		@Override
		public void updateTile(){
			float eff = efficiency * coolantMultiplier, edelta = eff * delta();
			
			//retarget
			if(timer(timerTarget, retargetTime)){
				target = Units.bestEnemy(team, x, y, range, u -> u.checkTarget(targetAir, targetGround), (u, x, y) -> -u.type.speed * (u.speedMultiplier() + 1) * u.type.accel + u.health * 0.00005f);
			}
			
			//consume coolant
			if(target != null && coolant != null){
				float maxUsed = coolant.amount;
				
				Liquid liquid = liquids.current();
				
				float used = Math.min(Math.min(liquids.get(liquid), maxUsed * Time.delta), Math.max(0, (1f / coolantMultiplier) / liquid.heatCapacity));
				
				liquids.remove(liquid, used);
				
				if(Mathf.chance(0.06 * used)){
					coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
				}
				
				coolantMultiplier = 1f + (used * liquid.heatCapacity * coolantMultiplier);
			}
			
			any = false;
			
			//look at target
			if(target != null && target.within(this, range + target.hitSize/2f) && target.team() != team && target.checkTarget(targetAir, targetGround) && efficiency > 0.02f){
				if(!headless){
					if(Mathf.chanceDelta(effectChance)){
						affectedEffect.at(target.x + Mathf.range(target.hitSize), target.y + Mathf.range(target.hitSize), laserColor);
					}
					control.sound.loop(shootSound, this, shootSoundVolume);
				}
				
				float dst = dst(target);
				float dest = angleTo(target);
				rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta);
				lastX = target.x;
				lastY = target.y;
				strength = Mathf.lerpDelta(strength, 1f, 0.1f);
				
				//shoot when possible
				if(Angles.within(rotation, dest, shootCone)){
					if(damage > 0){
						target.damageContinuous(damage * eff);
					}
					
					if(status != StatusEffects.none){
						target.apply(status, statusDuration);
					}
					
					any = true;
					target.impulseNet(Tmp.v1.set(this).sub(target).limit((force + (1f - dst / range) * scaledForce) * edelta).scl(cal.get(dst / range)));
				}
			}else{
				strength = Mathf.lerpDelta(strength, 0, 0.1f);
			}
		}
		
		@Override
		public void draw() {
			Draw.rect(baseRegion, x, y);
			Drawf.shadow(region, x - (float)size / 2.0F, y - (float)size / 2.0F, rotation - 90.0F);
			Draw.rect(region, x, y, rotation - 90.0F);
			
			
			if (any) {
				Draw.color(laserColor, Color.white, Mathf.absin(4, 0.15f));
				Draw.z(Layer.bullet + 5);
				float ang = angleTo(lastX, lastY);
				
				float phaseOffset = 360 / num;
				
				Lines.stroke(stroke * strength);
				
				Tmp.v2.trns(ang, shootLength);
				
				Lines.circle(x + Angles.trnsx(ang, shootLength), y + Angles.trnsy(ang, shootLength), circleRad);
				
				for(int i = 0; i < num; i++){
					float a = phaseOffset * i + Time.time * rotateSpeedScl;
					Lines.stroke(stroke * strength * (1 + (Angles.angleDist(a, rotation - 180) / 90 - 1f) * zScl));
					Tmp.v1.trns(a, circleRad);
					DrawFunc.drawSine2Modifier(x + Tmp.v2.x + Tmp.v1.x, y + Tmp.v2.y + Tmp.v1.y, lastX, lastY, Time.time * weaveSpeedScl, scale, scaleSpeedPerMove, phaseOffset * Mathf.degreesToRadians, mag, wavelength, ((x1, y1) -> {
						Fill.circle(x1, y1, Lines.getStroke() * 2f);
					}));
					Fill.circle(lastX, lastY, Lines.getStroke() * 1.35f);
				}
				
				
				float timeDelta = moveTime / circleNum;
				float targetCircleRad = (circleRad + target.hitSize) * 0.75f;
				for(int i = 0; i < circleNum; i++){
					float f = moveInterp.apply(DrawFunc.cycle(i * timeDelta, moveTime));
					Lines.stroke(stroke * strength * (1 - strokeScl * (1 - f)));
					Lines.circle(Mathf.lerp(lastX, x + Tmp.v2.x, f), Mathf.lerp(lastY, y + Tmp.v2.y, f), Mathf.lerp(targetCircleRad, circleRad, f));
				}
				
				Lines.stroke(stroke * strength);
				Lines.circle(lastX, lastY, targetCircleRad);
			}
			
		}
	}
}
