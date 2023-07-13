package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;

public class StrafeLaser extends BulletType{
	public float strafeAngle = 70;
	public float width = 18f;
	public float computeTick = 5f;
	public float fallScl = 0.125f;
	public boolean dataRot = true;
	public float lerpWhiteSine = 0.3f;
	
	public float sideTriRootLength = 20, sideTriLength = 50, sideTriWidth = 6;
	public float sideLengthDifference = 10;
	
	public StrafeLaser(float damage){
		this.damage = damage;
		
		lifetime = 160f;
		
		speed = 0;
		hitEffect = new Effect(20, e -> {
			color(e.color, Color.white, e.fout() * 0.6f + 0.1f);
			stroke(e.fout() * 2f);
			
			randLenVectors(e.id, 3, e.finpow() * 48, e.rotation, 35, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * 8 + 2f);
			});
		});
		
		shootEffect = Fx.none;
		smokeEffect = Fx.none;
		maxRange = 600f;
		
		hitShake = 4f;
		
		reflectable = false;
		despawnEffect = Fx.none;
		impact = true;
		keepVelocity = false;
		collides = false;
		pierce = true;
		hittable = false;
		absorbable = false;
		
		hitColor = null;
	}
	
	@Override
	public void init(){
		super.init();
		
		drawSize = Math.max(drawSize, maxRange * 2);
	}
	
	@Override
	public void init(Bullet b){
		super.init(b);
		
		if(dataRot && b.owner instanceof Unit){
			Unit u = (Unit)b.owner;
			b.fdata = b.angleTo(u.aimX, u.aimY());
		}
	}
	
	public float getRotation(Bullet b){
		return -strafeAngle / 2 + strafeAngle * b.fin(Interp.pow3);
	}
	
	@Override
	public float estimateDPS(){
		return continuousDamage();
	}
	
	public Color getColor(Bullet b){
		return hitColor == null ? b.team.color : hitColor;
	}
	
	@Override
	public void draw(Bullet b){
		float rotation = dataRot ? b.fdata : b.rotation() + getRotation(b);
		
		float fout = b.fout(fallScl) * Mathf.curve(b.fin(), 0, fallScl);
		float maxRange = this.maxRange * fout;
		float realLength = NHFunc.findLaserLength(b, rotation, maxRange);
		
		Tmp.v1.trns(rotation, realLength);
		
		Tmp.v2.trns(rotation, 0, width / 2 * fout);
		
		Tmp.v3.setZero();
		if(realLength < maxRange){
			Tmp.v3.set(Tmp.v2).scl((maxRange - realLength) / maxRange);
		}
		
		if(lerpWhiteSine != 0){
			Tmp.c1.set(getColor(b)).lerp(Color.white, Mathf.absin(4f, lerpWhiteSine));
		}else Tmp.c1.set(getColor(b));
		
		
		Draw.z(Layer.bullet + 0.01f);
		Draw.color(Tmp.c1);
		Fill.circle(b.x, b.y, width / 1.225f * fout);
		
		for(int i : Mathf.signs){
			DrawFunc.tri(b.x, b.y, sideTriWidth * fout,sideTriRootLength - sideLengthDifference + (sideTriLength - sideLengthDifference) * fout, Time.time * 1.5f + 90 * i);
			DrawFunc.tri(b.x, b.y, sideTriWidth * fout,sideTriRootLength + sideTriLength * fout, Time.time * -1f + 90 * i);
		}
		
		Draw.color(Tmp.c1, Color.white, 0.55f);
		Fill.circle(b.x, b.y, width / 1.85f * fout);
		Draw.color(Color.white);
		Fill.circle(b.x, b.y, width / 2.125f * fout);
		
		Draw.z(Layer.bullet);
		
		Draw.reset();
		
		Tmp.v2.scl(1.1f);
		Tmp.v3.scl(1.1f);
		Draw.alpha(0.5f);
		Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
		if(realLength < maxRange)Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
		
		Draw.color(Tmp.c1);
		Tmp.v2.scl(0.85f);
		Tmp.v3.scl(0.85f);
		Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
		if(realLength < maxRange)Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
		
		Draw.alpha(1f);
		Draw.color(Tmp.c2.set(Tmp.c1).lerp(Color.white, 0.3f));
		Tmp.v2.scl(0.5f);
		Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + (Tmp.v1.x + Tmp.v3.x) / 3, b.y + (Tmp.v1.y + Tmp.v3.y) / 3, b.x + (Tmp.v1.x - Tmp.v3.x) / 3, b.y + (Tmp.v1.y - Tmp.v3.y) / 3);
		
		Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.5f, getColor(b), 0.7f);
		Draw.reset();
	}
	
	@Override
	public float continuousDamage(){
		return damage / computeTick * 60f;
	}
	
	@Override
	public void update(Bullet b){
		if(b.timer.get(1, computeTick)){
			float maxRange = this.maxRange * b.fout(fallScl) * Mathf.curve(b.fin(), 0, fallScl);
			NHFunc.collideLine(b, b.team, Fx.none, b.x, b.y, dataRot ? b.fdata : b.rotation() + getRotation(b), maxRange, true, true);
		}
		
		if(dataRot && b.owner instanceof Unit){
			Unit u = (Unit)b.owner;
			if(Vars.net.active()){
				if(u.isPlayer()){
					Player player = (Player)u.controller();
					b.fdata = Angles.moveToward(b.fdata, b.angleTo(player.mouseX, player.mouseY), 1f);
				}else{
					Teamc target = Units.bestTarget(b.team, b.x, b.y, range, un -> un.type.canAttack, Building::isValid, UnitSorts.strongest);
					if(target != null)b.fdata = Angles.moveToward(b.fdata, b.angleTo(target), 1f);
				}
			}else b.fdata = Angles.moveToward(b.fdata, b.angleTo(u.aimX, u.aimY), 1f);
		}
		
		if(hitShake > 0){
			Effect.shake(hitShake, hitShake, b);
		}
	}
	
	@Override
	public void hit(Bullet b, float x, float y){
		hitEffect.at(x, y, b.rotation() + getRotation(b), getColor(b));
	}
}
