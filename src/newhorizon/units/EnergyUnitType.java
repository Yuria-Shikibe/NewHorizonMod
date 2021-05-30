package newhorizon.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.AmmoTypes;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Cicon;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;

public class EnergyUnitType extends UnitType{
	public Effect slopeEffect = NHFx.boolSelector;
	
	public float outerEyeScl = 0.25f;
	public float innerEyeScl = 0.18f;
	
	public float[][] rotator = {
		{80f, 12f, 1f},
		{60f, 10f, -0.75f}
	};
	
	public EnergyUnitType(String name){
		super(name);
		trailLength = -1;
		buildSpeed = 10f;
		crashDamageMultiplier = Mathf.clamp(hitSize / 10f, 1, 10);
		payloadCapacity = Float.MAX_VALUE;
		buildBeamOffset = 0;
		ammoType = AmmoTypes.powerHigh;
		flying = true;
		for(StatusEffect effect : Vars.content.statusEffects()){
			immunities.add(effect);
		}
	}
	
	@Override
	public TextureRegion icon(Cicon icon){
		return Icon.power.getRegion();
	}
	
	@Override
	public void init(){
		super.init();
		if(trailLength < 0)trailLength = (int)hitSize * 4;
		if(this.slopeEffect == NHFx.boolSelector)this.slopeEffect = new Effect(30, e -> {
			if(!(e.data instanceof Integer))return;
			int i = e.data();
			Draw.color(e.color);
			Angles.randLenVectors(e.id, (int)(e.rotation / 8f), e.rotation / 4f + e.rotation * 2f * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * e.rotation / 2.25f));
			Lines.stroke((i < 0 ? e.fin(Interp.pow2InInverse) : e.fout(Interp.pow2Out)) * 2f);
			Lines.circle(e.x, e.y, (i > 0 ? (e.fin(Interp.pow2InInverse) + 0.5f) : e.fout(Interp.pow2Out)) * e.rotation);
		}).layer(Layer.bullet);
		
		engineSize = hitSize / 4;
	}
	
	@Override
	public void draw(Unit unit){
		super.draw(unit);
	}
	
	
	@Override
	public void drawBody(Unit unit){
		Draw.z(Layer.effect + 0.001f);
		float sizeF = 1 + Mathf.absin(4f, 0.1f);
		Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + Mathf.clamp(unit.hitTime) / 5f * 3f);
		Draw.alpha(0.65f);
		Fill.circle(unit.x, unit.y, hitSize * sizeF * 1.1f);
		Draw.alpha(1f);
		Fill.circle(unit.x, unit.y, hitSize * sizeF);
		
		for(float[] j : rotator){
			for(int i : Mathf.signs){
				Drawf.tri(unit.x, unit.y, j[1], j[0], Time.time * j[2] + 90 + 90 * i + Mathf.randomSeed(unit.id));
			}
		}
		
		if(unit instanceof Trailc){
			Trail trail = ((Trailc)unit).trail();
			trail.draw(unit.team.color, (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f) * unit.elevation) * trailScl);
		}
		
		Draw.color(Tmp.c1.set(unit.team.color).lerp(Color.white, 0.65f));
		Fill.circle(unit.x, unit.y, hitSize * sizeF * 0.75f * unit.healthf());
		Draw.color(Color.black);
		Fill.circle(unit.x, unit.y, hitSize * sizeF * 0.7f * unit.healthf());
		
		Draw.color(unit.team.color);
		Tmp.v1.set(unit.aimX, unit.aimY).sub(unit).nor().scl(hitSize * 0.15f);
		Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, hitSize * sizeF * outerEyeScl);
		Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + 0.45f);
		Tmp.v1.setLength(hitSize * sizeF * (outerEyeScl - innerEyeScl));
		Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, hitSize * sizeF * innerEyeScl);
		Draw.reset();
	}
	
	@Override
	public void update(Unit unit){
		super.update(unit);
		if(Mathf.chanceDelta(0.1))for(int i : Mathf.signs)slopeEffect.at(unit.x + Mathf.range(hitSize), unit.y + Mathf.range(hitSize), hitSize, unit.team.color, i);
	}
	
	@Override
	public void drawCell(Unit unit){
	}
	
	@Override
	public void drawControl(Unit unit){
		Draw.z(Layer.effect + 0.001f);
		Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) +  Mathf.clamp(unit.hitTime) / 5f);
		for(int i = 0; i < 4; i++){
			float rotation = Time.time * 1.5f + i * 90;
			Tmp.v1.trns(rotation, hitSize * 1.5f).add(unit);
			Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, rotation + 90);
		}
		Draw.reset();
	}
	
	@Override
	public void drawEngine(Unit unit){
//		Draw.z(Layer.effect + 0.001f);
//		Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) +  Mathf.clamp(unit.hitTime) / 5f);
//		Tmp.v1.trns(unit.rotation - 180, hitSize);
//		Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, engineSize * (Mathf.absin(Time.time, 2f, engineSize / 4f) + 1));
//		Draw.reset();
	}
	
	@Override
	public void drawItems(Unit unit){
		super.drawItems(unit);
	}
	
	@Override
	public <T extends Unit & Legsc> void drawLegs(T unit){
	}
	
	@Override
	public void drawLight(Unit unit){
		Drawf.light(unit.team, unit.x, unit.y, hitSize * 3f, unit.team.color, lightOpacity);
	}
	
	@Override
	public void drawMech(Mechc mech){
	}
	
	@Override
	public void drawOutline(Unit unit){
	}
	
	@Override
	public <T extends Unit & Payloadc> void drawPayload(T unit){
		super.drawPayload(unit);
	}
	
	@Override
	public void drawShadow(Unit unit){
	}
	
	@Override
	public void drawShield(Unit unit){
		float alpha = unit.shieldAlpha();
		float radius = unit.hitSize() * 1.3f;
		Fill.light(unit.x, unit.y, Lines.circleVertices(radius), radius, Tmp.c1.set(Pal.shieldIn), Tmp.c2.set(unit.team.color).a(0.7f).lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f)).a(Pal.shield.a * alpha));
	}
	
	@Override
	public void drawSoftShadow(Unit unit){
	}
	
	@Override
	public void drawWeapons(Unit unit){
	}
}
