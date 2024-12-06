package newhorizon.expand.units.unitEntity;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Layer;
import newhorizon.content.NHContent;
import newhorizon.content.NHStatusEffects;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

public class NucleoidEntity extends UnitEntity{
	public static float maxOnceDamage = 3000;
	
	public static final float MaxDamagedPerSec = 30000;
	public static final float RecentDamageResume = MaxDamagedPerSec / 60f;
	
	public float recentDamage = MaxDamagedPerSec;
	
	public float reinforcementsReload = REINFORCEMENTS_SPACING;
	public static float REINFORCEMENTS_SPACING = Time.toMinutes * 2;
	
	@Override
	public int classId(){
		return EntityRegister.getID(NucleoidEntity.class);
	}
	
	@Override
	public float mass(){
		return 8000000;
	}
	
	@Override
	public void update(){
		super.update();
		
		recentDamage += RecentDamageResume * Time.delta;
		if(recentDamage >= MaxDamagedPerSec){
			recentDamage = MaxDamagedPerSec;
		}
		
		reinforcementsReload += Time.delta;
		if(healthf() < 0.3f && reinforcementsReload >= REINFORCEMENTS_SPACING){
			reinforcementsReload = 0;
			for(int i : Mathf.signs){
				Tmp.v1.trns(rotation + 60 * i, -hitSize * 1.85f).add(x, y);
				
				NHFunc.spawnUnit(team, Tmp.v1.x, Tmp.v1.y, rotation, 8f, 120, 0, NHUnitTypes.pester, 1, NHStatusEffects.reinforcements, Time.toMinutes);
			}
		}
	}
	
	@Override
	public void draw(){
		super.draw();
		
		float z = Draw.z();
		Draw.z(Layer.bullet);
		
		Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4f, 0.15f));
		Draw.color(Tmp.c1);
		Lines.stroke(3f);
		DrawFunc.circlePercent(x, y, hitSize * 1.15f, reinforcementsReload / REINFORCEMENTS_SPACING, 0);
		
		float scl = Interp.pow3Out.apply(Mathf.curve(reinforcementsReload / REINFORCEMENTS_SPACING, 0.96f, 1f));
		TextureRegion arrowRegion = NHContent.arrowRegion;
		
		for (int l : Mathf.signs) {
			float angle = 90 + 90 * l;
			for (int i = 0; i < 4; i++) {
				Tmp.v1.trns(angle, i * 50 + hitSize * 1.32f);
				float f = (100 - (Time.time + 25 * i) % 100) / 100;
				
				Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * f * scl, arrowRegion.height * f * scl, angle + 90);
			}
		}
		
		Draw.z(z);
	}
	
	public void rawDamage(float amount) {
		boolean hadShields = this.shield > 1.0E-4F;
		if (hadShields) {
			this.shieldAlpha = 1.0F;
		}
		
		amount = Math.min(amount, maxOnceDamage);
		
		float shieldDamage = Math.min(Math.max(this.shield, 0.0F), amount);
		this.shield -= shieldDamage;
		this.hitTime = 1.0F;
		
		amount -= shieldDamage;
		amount = Math.min(recentDamage / healthMultiplier, amount);
		recentDamage -= amount * 1.5f * healthMultiplier;
		
		if (amount > 0.0F && this.type.killable) {
			this.health -= amount;
			if (this.health <= 0.0F && !this.dead) {
				this.kill();
			}
			
			if (hadShields && this.shield <= 1.0E-4F) {
				Fx.unitShieldBreak.at(this.x, this.y, 0.0F, this.team.color, this);
			}
		}
		
	}
	
	@Override
	public void read(Reads read){
		reinforcementsReload = read.f();
		
		super.read(read);
	}
	
	@Override
	public void write(Writes write){
		write.f(reinforcementsReload);
		
		super.write(write);
	}
	
	@Override
	public void readSync(Reads read){
		super.readSync(read);
		
		if (!isLocal()) {
			reinforcementsReload = read.f();
		} else {
			read.f();
		}
	}
	
	@Override
	public void writeSync(Writes write){
		super.writeSync(write);
		
		write.f(reinforcementsReload);
	}
}
