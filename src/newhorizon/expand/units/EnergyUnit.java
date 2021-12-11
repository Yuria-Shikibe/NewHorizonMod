package newhorizon.expand.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import newhorizon.content.NHFx;
import newhorizon.util.func.EntityRegister;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHSetting;

public class EnergyUnit extends UnitEntity{
	public Trail[] trails;
	
	@Override
	public int classId(){
		return EntityRegister.getID(getClass());
	}
	
	@Override
	public void destroy(){
		super.destroy();
		
		for(int i = 0; i < trails.length; i++){
			Tmp.c1.set(team.color).mul(i * 0.045f).lerp(Color.white, 0.075f * i);
			Fx.trailFade.at(x, y, type.trailScl, team.color, trails[i].copy());
		}
		
		NHFx.energyUnitBlast.at(x, y, hitSize * 4, team.color);
	}
	
	@Override
	public void setType(UnitType type){
		super.setType(type);
		
		trails = new Trail[3];
		for(int i = 0; i < trails.length; i++){
			trails[i] = new Trail(type.trailLength);
		}
	}
	
	@Override
	public void draw(){
		Draw.z(Layer.bullet);
		
		if(NHSetting.enableDetails())for(int i = 0; i < trails.length; i++){
			Tmp.c1.set(team.color).mul(1 + i * 0.045f).lerp(Color.white, 0.075f * i + Mathf.absin(4f, 0.3f) +  Mathf.clamp(hitTime) / 5f);
			trails[i].drawCap(Tmp.c1, type.trailScl);
			trails[i].draw(Tmp.c1, type.trailScl);
		}
		
		super.draw();
	}
	
	@Override
	public void update(){
		super.update();
		
		Rand rand = NHFunc.rand;
		rand.setSeed(id);
		
		if(NHSetting.enableDetails() && !Vars.headless){
			for(int i = 0; i < trails.length; i++){
				Trail trail = trails[i];
				
				float scl = rand.random(0.75f, 1.5f) * Mathf.sign(rand.range(1)) * (i + 1) / 1.25f;
				float s = rand.random(0.75f, 1.25f);
				
				Tmp.v1.trns(
					Time.time * scl * rand.random(0.5f, 1.5f) + i * 360f / trails.length + rand.random(360),
					hitSize * (1.1f + 0.35f * i) * 0.65f + Mathf.sinDeg(Time.time * scl* s) * hitSize / 3,
						Mathf.cosDeg(Time.time * scl * s) * hitSize / 3 * (i + 1) * 0.5f * rand.random(-1.5f, 1.5f)
				).add(this);
				trail.update(Tmp.v1.x, Tmp.v1.y, 1 + Mathf.absin(4f, 0.2f));
			}
		}
		
		if(Mathf.chanceDelta(0.15) && healthf() < 0.6f)NHFunc.randFadeLightningEffect(x, y, Mathf.range(hitSize, hitSize * 4), Mathf.range(hitSize / 4, hitSize / 2), team.color, Mathf.chance(0.5));
		
	}
	
	@Override
	public void damage(float amount, boolean withEffect){
		super.damage(amount, withEffect);
	}
}
