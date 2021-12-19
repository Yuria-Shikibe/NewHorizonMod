package newhorizon.util.feature;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.IntSeq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import newhorizon.content.NHSounds;
import newhorizon.util.func.NHFunc;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.math.Angles.randLenVectors;

public class WarpUnit{
	public static final IntSeq toBeRemoved = new IntSeq();
	
	public static final Effect warp = new Effect(70f, 50000, e -> {
		if (!(e.data instanceof UnitType))return;
		UnitType type = e.data();
		color(e.color);
		
		Draw.z((type.engineSize < 0 ? Layer.effect - 0.1f : type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) - 0.1f);
		
		float length = Mathf.sqrt(Vars.world.unitWidth() * Vars.world.unitHeight()) * 3f;
		
		Tmp.v1.trns(e.rotation, length / 3f);
		
		e.scaled(45, i -> {
			Drawf.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, type.engineSize * 1.5f * i.fout(Interp.sineIn), length, i.rotation - 180);
			for(int s : Mathf.signs){
				Drawf.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, type.engineSize * 1.5f * i.fout(Interp.sineIn), type.hitSize * 1.5f, i.rotation - 120 * s);
			}
			Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, type.engineSize * 0.75f * i.fout(Interp.sineIn));
		});
		
		randLenVectors(e.id, 15, length, e.rotation - 180, 0f, (x, y) -> lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60));
		
		Draw.reset();
	}), prepare = new Effect(180f, 300f, e -> {
		if (!(e.data instanceof Unit))return;
		Unit unit = e.data();
		color(unit.team.color);
		
		NHFunc.rand.setSeed(e.id);
		
		Tmp.v1.trns(unit.rotation, unit.hitSize());
		for(int i : Mathf.signs){
			Lines.stroke(NHFunc.rand.random(2, 4));
			randLenVectors(e.id, (int)Mathf.clamp(unit.hitSize / 2, 20, 60), Mathf.clamp(unit.hitSize * 2, 22, 200), unit.rotation + 90 * i, 37, (x, y) -> {
				lineAngle(unit.x + x + Tmp.v1.x, unit.y + y + Tmp.v1.y, Mathf.angle(x, y), (e.fin(Interp.pow4Out) * 15 + 4) * e.fout(Interp.pow4Out));
			});
		}
		
	}).followParent(true);
	
	public static void warp(Unit unit, float angle){
		unit.abilities().and(new Rotator(angle));
		toBeRemoved.add(unit.id());
		
//		prepare.at(unit.x, unit.y, unit.rotation, unit);
		
		Time.run(prepare.lifetime, () -> {
			warp.at(unit.x, unit.y, unit.rotation, unit.team.color, unit.type);
			NHSounds.jumpIn.at(unit);
			toBeRemoved.removeValue(unit.id());
			unit.remove();
		});
	}
	
	public static class Rotator extends Ability{
//		protected static final Vec2 direction = new Vec2();
		public float targetAng = 45;
		
		protected float warmup = 0;
		
		public Rotator(float targetAng){
			this.targetAng = targetAng;
		}
		
		@Override
		public void update(Unit unit){
			unit.lookAt(targetAng);
			
			warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
		}
		
		@Override
		public void draw(Unit unit){
			float z = Draw.z();
			
			int particles = (int)unit.type.hitSize;
			float particleLife = 40f, particleRad = unit.type.hitSize, particleStroke = 1.1f, particleLen = unit.hitSize / 8f;
			Rand rand = new Rand();
			float base = (Time.time / particleLife);
			rand.setSeed(unit.id);
			
			Tmp.v1.trns(unit.rotation, unit.type.engineOffset * 1.25f);
			
			Draw.z(Layer.effect);
			
			Draw.color(Color.white, warmup * 0.6f);
			
			for(int i = 0; i < particles; i++){
				float fin = (rand.random(1f) + base) % 1f * warmup, fout = 1f - fin;
				float angle = unit.rotation + rand.range(60f) - 180;
				float len = particleRad * Interp.pow2Out.apply(fin);
				Lines.lineAngle(unit.x + Angles.trnsx(angle, len) + Tmp.v1.x, unit.y + Angles.trnsy(angle, len) + Tmp.v1.y, angle, particleLen * fout * warmup);
			}
			
			Draw.z(z);
		}
	}
}
