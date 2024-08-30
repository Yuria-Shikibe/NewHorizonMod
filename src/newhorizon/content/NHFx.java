package newhorizon.content;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import newhorizon.NHSetting;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.struct.Vec2Seq;

import java.util.Arrays;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class NHFx{
	public static final float EFFECT_MASK = Layer.effect + 0.0001f;
	public static final float EFFECT_BOTTOM = Layer.bullet - 0.11f;
	public static final IntMap<Effect> same = new IntMap<>();
	private static final Rand rand = new Rand();
	private static final Rand rand2 = new Rand();
	private static final Vec2 v = new Vec2();
	private static final int[] oneArr = {1};
	
	public interface EffectParam{
		void draw(long id, float x, float y, float rot, float fin);
	}
	
	public static float fslope(float fin){
		return (0.5f - Math.abs(fin - 0.5f)) * 2f;
	}
	
	public static float fout(float fin, float margin){
		if(fin >= 1f - margin){
			return 1f - (fin - (1f - margin)) / margin;
		}else{
			return 1f;
		}
	}
	
	public static void drawDark(Runnable runnable){
		Draw.draw(EFFECT_BOTTOM, runnable);
		Draw.draw(EFFECT_MASK, runnable);
	}
	
	public static int hash(String m, Color c){
		return Arrays.hashCode(new int[]{m.hashCode(), c.hashCode()});
	}
	
	public static Effect get(String m, Color c, Effect effect){
		int hash = hash(m, c);
		Effect or = same.get(hash);
		if(or == null)same.put(hash, effect);
		return or == null ? effect : or;
	}
	
	
	
	public static Effect spreadOutSpark(float lifetime, float radius, int sparks, int sparkSpikes, float sparkLifetime, float sparkSize, float sparkLength, Interp spreadOutInterp){
		return new Effect(lifetime, radius * 2f, e -> {
			rand.setSeed(e.id);
			float finT = e.lifetime * e.fin(spreadOutInterp);
			
			for(int s = 0; s < sparks; s++){
				float sBegin = rand.random(e.lifetime - sparkLifetime);
				float
						fin = (finT -  sBegin) / sparkLifetime;
				
				if(fin < 0 || fin > 1)continue;
				
				
				float
						fout = 1 - fin,
						fslope = (0.5f - Math.abs(fin - 0.5f)) * 2f;
				
				rand2.setSeed(e.id + s);
				v.setToRandomDirection(rand2).scl(radius * sBegin / (e.lifetime - sparkLifetime));
				
				Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7f);
				Draw.color(Tmp.c1);
				Lines.stroke(1.2f * Mathf.curve(fout, 0, 0.22f));
				randLenVectors(e.id + s + 1, sparkSpikes, sparkSize * fin, (x, y) -> {
					lineAngle(e.x + x + v.x, e.y + y + v.y, Mathf.angle(x, y), fslope * sparkLength + 2);
					Drawf.light(e.x + x + v.x, e.y + y + v.y, fin * sparkLength * fslope * 1.3f, Tmp.c1, 0.7f);
				});
			}
		});
	}
	
	public static Effect subEffect(float lifetime, float radius, int num, float childLifetime, Interp spreadOutInterp, EffectParam drawer){
		return new Effect(lifetime, radius * 2f, e -> {
			rand.setSeed(e.id);
			float finT = e.lifetime * e.fin(spreadOutInterp);
			
			for(int s = 0; s < num; s++){
				float sBegin = rand.random(e.lifetime - childLifetime);
				float
						fin = (finT -  sBegin) / childLifetime;
				
				if(fin < 0 || fin > 1)continue;
				
				
				float
						fout = 1 - fin;
				
				rand2.setSeed(e.id + s);
				float theta = rand2.random(0f, Mathf.PI2);
				v.set(Mathf.cos(theta), Mathf.sin(theta)).scl(radius * sBegin / (e.lifetime - childLifetime));
				
				Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7f);
				Draw.color(Tmp.c1);
				drawer.draw(e.id + s + 9999, e.x + v.x, e.y + v.y, Mathf.radiansToDegrees * theta, fin);
			}
		});
	}
	
	public static Effect circleOut(float lifetime, float radius, float thick){
		return new Effect(lifetime, radius * 2f, e -> {
			Draw.color(e.color, Color.white, e.fout() * 0.7f);
			Lines.stroke(thick * e.fout());
			Lines.circle(e.x, e.y, radius * e.fin(Interp.pow3Out));
		});
	}
	
	public static Effect squareRand(Color color, float sizeMin, float sizeMax){
		return new Effect(20f, sizeMax * 2f, e -> {
			Draw.color(Color.white, color, e.fin() + 0.15f);
			if(e.id % 2 == 0){
				Lines.stroke(1.5f * e.fout(Interp.pow3Out));
				Lines.square(e.x, e.y, Mathf.randomSeed(e.id, sizeMin, sizeMax) * e.fin(Interp.pow2Out) + 3, 45);
			}else{
				Fill.square(e.x, e.y, Mathf.randomSeed(e.id, sizeMin * 0.5f, sizeMin * 0.8f) * e.fout(Interp.pow2Out), 45);
			}
		});
	}
	
	public static Effect railShoot(Color color, float length, float width, float lifetime, float spacing){
		return new Effect(lifetime, length * 2f, e -> {
			TextureRegion arrowRegion = NHContent.arrowRegion;
			
			Draw.color(color);
			
			float railF = Mathf.curve(e.fin(Interp.pow2Out), 0f, 0.25f) * Mathf.curve(e.fout(Interp.pow4Out), 0f, 0.3f) * e.fin();
			
			for(int i = 0; i <= length / spacing; i++){
				Tmp.v1.trns(e.rotation, i * spacing);
				float f = Interp.pow3Out.apply(Mathf.clamp((e.fin() * length - i * spacing) / spacing)) * (0.6f + railF * 0.4f);
				Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, arrowRegion.width * Draw.scl * f, arrowRegion.height * Draw.scl * f, e.rotation - 90);
			}
			
			Tmp.v1.trns(e.rotation, 0f, (2 - railF) * tilesize * 1.4f);
			
			Lines.stroke(railF * 2f);
			for(int i : Mathf.signs){
				Lines.lineAngle(e.x + Tmp.v1.x * i, e.y + Tmp.v1.y * i, e.rotation, length * (0.75f + railF / 4f) * Mathf.curve(e.fout(Interp.pow5Out), 0f, 0.1f));
			}
		}).followParent(true);
	}
	
	public static Effect polyTrail(Color fromColor, Color toColor, float size, float lifetime){
		return new Effect(lifetime, size * 2, e -> {
			color(fromColor, toColor, e.fin());
			Fill.poly(e.x, e.y, 6, size * e.fout(), e.rotation);
			Drawf.light(e.x, e.y, e.fout() * size, fromColor, 0.7f);
		});
	}
	
	public static Effect genericCharge(Color color, float size, float range, float lifetime){
		return new Effect(lifetime, e -> {
			color(color);
			Lines.stroke(size / 7f * e.fin());
			
			randLenVectors(e.id, 15, 3f + 60f * e.fout(), e.rotation, range, (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * size + size / 4f);
				Drawf.light(e.x + x, e.y + y, e.fout(0.25f) * size, color, 0.7f);
			});
			
			Fill.circle(e.x, e.y, size * 0.48f * Interp.pow3Out.apply(e.fin()));
		});
	}
	
	public static Effect genericChargeBegin(Color color, float size, float lifetime){
		return new Effect(lifetime, e -> {
			color(color);
			Fill.circle(e.x, e.y, e.fin() * size);
			Drawf.light(e.x, e.y, e.fin() * size, color, 0.7f);
			
			color();
			Fill.circle(e.x, e.y, e.fin() * size / 2f);
		});
	}
	
	public static Effect lightningHitSmall(Color color){
		return get("lightningHitSmall", color, new Effect(20, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 5, 18 * e.fin(), (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 6 + 2);
				Drawf.light(e.x + x, e.y + y, e.fin() * 12 * e.fout(0.25f), color, 0.7f);
			});
		}));
	}
	
	public static Effect shootCircleSmall(Color color){
		return get("shootCircleSmall", color, new Effect(30, e -> {
			color(color, Color.white, e.fout() * 0.75f);
			rand.setSeed(e.id);
			randLenVectors(e.id, 3, 3 + 23 * e.fin(), e.rotation, 22, (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * rand.random(1.5f, 3.2f));
				Drawf.light(e.x + x, e.y + y, e.fout() * 4.5f, color, 0.7f);
			});
		}));
	}
	
	public static Effect shootLineSmall(Color color){
		return get("shootLineSmall", color,new Effect(37f, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 4, 8 + 32 * e.fin(), e.rotation, 22F, (x, y) -> {
				Lines.stroke(1.25f * e.fout(0.2f));
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 6f + 3);
				Drawf.light(e.x + x, e.y + y, e.fout() * 13f + 3, color, 0.7f);
			});
		}));
	}
	
	public static Effect shootLine(float size, float angleRange){
		int num = Mathf.clamp((int)size / 6, 6, 20);
		float thick = Mathf.clamp(0.75f, 2f, size / 22f);
		
		return new Effect(37f, e -> {
			color(e.color, Color.white, e.fout() * 0.7f);
			rand.setSeed(e.id);
			DrawFunc.randLenVectors(e.id, num, 4 + (size * 1.2f) * e.fin(), size * 0.15f * e.fin(), e.rotation, angleRange, (x, y) -> {
				Lines.stroke(thick * e.fout(0.32f));
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (e.fslope() + e.fin()) * 0.5f * (size * rand.random(0.15f, 0.5f) + rand.random(2f)) + rand.random(2f));
				Drawf.light(e.x + x, e.y + y, e.fslope() * (size * 0.5f + 14f) + 3, e.color, 0.7f);
			});
		});
	}
	
	public static Effect shootSquare(float lifetime, int num, float size, float angle, float range, float angleRange){
		return new Effect(lifetime, e -> {
			color(e.color, Color.white, e.fout() * 0.7f);
			rand.setSeed(e.id);
			DrawFunc.randLenVectors(e.id, num, size + range * e.fin(), size * 0.15f * e.fin(), e.rotation, angleRange, (x, y) -> {
				float size0 = (e.fslope() + e.fout()) * 0.5f * (size * rand.random(0.25f, 1f));
				Fill.square(e.x + x, e.y + y, size0, angle);
				Drawf.light(e.x + x, e.y + y, size0 * 1.25f, e.color, 0.7f);
			});
		});
	}
	
	public static Effect laserHit(Color color){
		return get("laserHit", color, new Effect(20, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 9, 18 * e.fin(), e.rotation, 40f, (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 2);
				Drawf.light(e.x + x, e.y + y, e.fout(0.25f) * 12f, color, 0.7f);
			});
		}));
	}
	
	public static Effect lightningHitLarge(Color color){
		return get("lightningHitLarge", color, new Effect(50f, 180f, e -> {
			color(color);
			Drawf.light(e.x, e.y, e.fout() * 90f, color, 0.7f);
			e.scaled(25f, t -> {
				stroke(3f * t.fout());
				circle(e.x, e.y, 3f + t.fin(Interp.pow3Out) * 80f);
			});
			Fill.circle(e.x, e.y, e.fout() * 8f);
			randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));
			
			color(Color.gray);
			Angles.randLenVectors(e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
		}));
	}
	
	public static Effect blast(Color color, float range){
		float lifetime = Mathf.clamp(range * 1.5f, 90f, 600f);
		return new Effect(lifetime, range * 2.5f, e -> {
			color(color);
			Drawf.light(e.x, e.y, e.fout() * range, color, 0.7f);
			
			e.scaled(lifetime / 3, t -> {
				stroke(3f * t.fout());
				circle(e.x, e.y, 8f + t.fin(Interp.circleOut) * range * 1.35f);
			});
			
			e.scaled(lifetime / 2, t -> {
				Fill.circle(t.x, t.y, t.fout() * 8f);
				if(NHSetting.enableDetails())Angles.randLenVectors(t.id + 1, (int)(range / 13), 2 + range * 0.75f * t.finpow(), (x, y) -> {
					Fill.circle(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15f, 3f, 14f));
					Drawf.light(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15f, 3f, 14f), color, 0.5f);
				});
			});
			
			if(!NHSetting.enableDetails())return;
			Draw.z(Layer.bullet - 0.001f);
			color(Color.gray);
			alpha(0.85f);
			float intensity = Mathf.clamp(range / 10f, 5f, 25f);
			for(int i = 0; i < 4; i++){
				rand.setSeed(((long)e.id << 1) + i);
				float lenScl = rand.random(0.4f, 1f);
				int fi = i;
				e.scaled(e.lifetime * lenScl, eIn -> {
					randLenVectors(eIn.id + fi - 1, eIn.fin(Interp.pow10Out), (int)(intensity / 2.5f), 8f * intensity, (x, y, in, out) -> {
						float fout = eIn.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
						Fill.circle(eIn.x + x, eIn.y + y, fout * ((2f + intensity) * 1.8f));
					});
				});
			}
		});
	}

	public static Effect laserEffect(float num){
		return new Effect(26.0F, e -> {
			Draw.color(Color.white);
			float length = !(e.data instanceof Float) ? 70.0F : (Float)e.data;
			Angles.randLenVectors(e.id, (int)(length / num), length, e.rotation, 0.0F, (x, y) -> {
				Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 9.0F);
				Drawf.light(e.x + x, e.y + y, e.fout(0.25f) * 12f, Color.white, 0.7f);
			});
		});
	}

	public static Effect chargeEffectSmall(Color color, float lifetime){
		return new Effect(lifetime, 100.0F, e -> {
			Draw.color(color);
			Drawf.light(e.x, e.y, e.fin() * 55f, color, 0.7f);
			randLenVectors(e.id, 7, 3 + 50 * e.fout(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.finpow() * 3f));
			Lines.stroke(e.fin() * 1.75f);
			Lines.circle(e.x, e.y, e.fout() * 40f);
			randLenVectors(e.id + 1, 16, 3 + 70 * e.fout(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 7 + 3));
		});
	}
	
	public static Effect chargeBeginEffect(Color color, float size, float lifetime){
		return new Effect(lifetime, e -> {
			Draw.color(color);
			Drawf.light(e.x, e.y, e.fin() * size, color, 0.7f);
			Fill.circle(e.x, e.y, size * e.fin());
		});
	}
	
	public static Effect crossBlast(Color color){
		return get("crossBlast", color, crossBlast(color, 72));
	}
	
	public static Effect crossBlast(Color color, float size){
		return crossBlast(color, size, 0);
	}
	
	public static Effect crossBlast(Color color, float size, float rotate){
		return new Effect(Mathf.clamp(size / 3f, 35f, 240f), size * 2, e -> {
			color(color, Color.white, e.fout() * 0.55f);
			Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);
			
			e.scaled(10f, i -> {
				stroke(1.35f * i.fout());
				circle(e.x, e.y, size * 0.7f * i.finpow());
			});
			
			rand.setSeed(e.id);
			float sizeDiv = size / 1.5f;
			float randL = rand.random(sizeDiv);
			
			for(int i = 0; i < 4; i++){
				DrawFunc.tri(e.x, e.y, size / 20 * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + rotate);
			}
		});
	}
	
	public static Effect hyperBlast(Color color){
		return get("hyperBlast", color, new Effect(30f, e -> {
			color(color, Color.white, e.fout() * 0.75f);
			Drawf.light(e.x, e.y, e.fout() * 55f, color, 0.7f);
			stroke(1.3f * e.fslope());
			circle(e.x, e.y, 45f * e.fin());
			randLenVectors(e.id + 1, 5, 8f + 50 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 7f));
		}));
	}
	
	public static Effect instShoot(Color color, Color colorInner){
		return new Effect(24.0F, e -> {
			e.scaled(10.0F, (b) -> {
				Draw.color(Color.white, color, b.fin());
				Lines.stroke(b.fout() * 3.0F + 0.2F);
				Lines.circle(b.x, b.y, b.fin() * 50.0F);
			});
			Draw.color(color);
			
			for(int i : Mathf.signs){
				DrawFunc.tri(e.x, e.y, 8.0F * e.fout(), 85.0F, e.rotation + 90.0F * i);
				DrawFunc.tri(e.x, e.y, 8.0F * e.fout(), 50.0F, 90 + 90.0F * i);
			}
			
			if(!NHSetting.enableDetails())return;
			
			Draw.color(colorInner);
			
			for(int i : Mathf.signs){
				DrawFunc.tri(e.x, e.y, 5F * e.fout(), 48.0F, e.rotation + 90.0F * i);
				DrawFunc.tri(e.x, e.y, 5F * e.fout(), 29.0F, 90 + 90.0F * i);
			}
		});
	}
	
	public static Effect hitSpark(Color color, float lifetime, int num, float range, float stroke, float length){
		return new Effect(lifetime, e -> {
			color(color, Color.white, e.fout() * 0.3f);
			stroke(e.fout() * stroke);
			
			randLenVectors(e.id, num, e.finpow() * range, e.rotation, 360f, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * length * 0.85f + length * 0.15f);
			});
		});
	}
	
	public static Effect instBomb(Color color){
		return get("instBomb", color, instBombSize(color, 4, 80f));
	}
	
	public static Effect instBombSize(Color color, int num, float size){
		return new Effect(22.0F, size * 1.5f, e -> {
			Draw.color(color);
			Lines.stroke(e.fout() * 4.0F);
			Lines.circle(e.x, e.y, 4.0F + e.finpow() * size / 4f);
			Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);
			
			int i;
			for(i = 0; i < num; ++i) {
				DrawFunc.tri(e.x, e.y, size / 12f, size * e.fout(), (float)(i * 90 + 45));
			}
			
			Draw.color();
			
			for(i = 0; i < num; ++i) {
				DrawFunc.tri(e.x, e.y, size / 26f, size / 2.5f * e.fout(), (float)(i * 90 + 45));
			}
		});
	}
	
	public static Effect instHit(Color color){return get("instHit", color, instHit(color, 5, 50)); }
	
	public static Effect instHit(Color color, int num, float size){
		return new Effect(20.0F, size * 1.5f, e -> {
			rand.setSeed(e.id);
			
			for(int i = 0; i < 2; ++i) {
				Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.25f));
				float m = i == 0 ? 1.0F : 0.5F;
				
				
				for(int j = 0; j < num; ++j) {
					float rot = e.rotation + rand.range(size);
					float w = 15.0F * e.fout() * m;
					DrawFunc.tri(e.x, e.y, w, (size + rand.range( size * 0.6f)) * m, rot);
					DrawFunc.tri(e.x, e.y, w, size * 0.3f * m, rot + 180.0F);
				}
			}
			
			e.scaled(12.0F, (c) -> {
				Draw.color(color.cpy().lerp(Color.white, 0.25f));
				Lines.stroke(c.fout() * 2.0F + 0.2F);
				Lines.circle(e.x, e.y, c.fin() * size * 1.1f);
			});
			
			if(NHSetting.enableDetails())e.scaled(18.0F, (c) -> {
				Draw.color(color);
				Angles.randLenVectors(e.id, 25, 8.0F + e.fin() * size * 1.25f, e.rotation, 60.0F, (x, y) -> {
					Fill.square(e.x + x, e.y + y, c.fout() * 3.0F, 45.0F);
				});
			});
			
			Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);
		});
	}

	public static Effect smoothColorRect(Color out, float rad, float lifetime){
		return new Effect(lifetime, rad * 2, e -> {
			Draw.blend(Blending.additive);
			float radius = e.fin(Interp.pow3Out) * rad;
			Fill.light(e.x, e.y, 4, radius, 45f, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
			Draw.blend();
		}).layer(Layer.effect + 0.15f);
	}

	public static Effect smoothColorCircle(Color out, float rad, float lifetime){
		return new Effect(lifetime, rad * 2, e -> {
			Draw.blend(Blending.additive);
			float radius = e.fin(Interp.pow3Out) * rad;
			Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
			Drawf.light(e.x, e.y, radius * 1.3f, out, 0.7f * e.fout(0.23f));
			Draw.blend();
		}).layer(Layer.effect + 0.15f);
	}

	public static Effect smoothColorCircle(Color out, float rad, float lifetime, float alpha){
		return new Effect(lifetime, rad * 2, e -> {
			Draw.blend(Blending.additive);
			float radius = e.fin(Interp.pow3Out) * rad;
			Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out) * alpha));
			Drawf.light(e.x, e.y, radius * 1.3f, out, 0.7f * e.fout(0.23f));
			Draw.blend();
		}).layer(Layer.effect + 0.15f);
	}
	
	public static Effect instTrail(Color color, float angle, boolean random){
		return new Effect(30.0F, e -> {
			for(int j : angle == 0 ? oneArr: Mathf.signs){
				for(int i = 0; i < 2; ++i) {
					Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.15f));
					float m = i == 0 ? 1.0F : 0.5F;
					float rot = e.rotation + 180.0F;
					float w = 10.0F * e.fout() * m;
					DrawFunc.tri(e.x, e.y, w, 30.0F + (random ? Mathf.randomSeedRange(e.id, 15.0F) : 8) * m, rot + j * angle);
					if(angle == 0)DrawFunc.tri(e.x, e.y, w, 10.0F * m, rot + 180.0F + j * angle);
					else  Fill.circle(e.x, e.y, w / 2f);
				}
			}
		});
	}
	
	public static Effect lineCircleOut(Color color, float lifetime, float size, float stroke){
		return new Effect(50, e -> {
			color(color);
			stroke(e.fout() * stroke);
			Lines.circle(e.x, e.y, e.fin(Interp.pow3Out) * size);
		});
	}
	
	public static Effect lineSquareOut(Color color, float lifetime, float size, float stroke, float rotation){
		return new Effect(50, e -> {
			color(color);
			stroke(e.fout() * stroke);
			Lines.square(e.x, e.y, e.fin(Interp.pow3Out) * size, rotation);
		});
	}
	
	public static Effect polyCloud(Color color, float lifetime, float size, float range, int num){
		return (new Effect(lifetime, e -> {
			randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
				Draw.color(color, Pal.gray, e.fin() * 0.65f);
				Fill.poly(e.x + x, e.y + y, 6, size * e.fout(), e.rotation);
				Drawf.light(e.x + x, e.y + y, size * e.fout() * 2.5f, color, e.fout() * 0.65f);
				Draw.color(Color.white, Pal.gray, e.fin() * 0.65f);
				Fill.poly(e.x + x, e.y + y, 6, size * e.fout() / 2, e.rotation);
			});
		})).layer(Layer.bullet);
	}
	
	public static Effect square(Color color, float lifetime, int num, float range, float size){
		return new Effect(lifetime, e -> {
			Draw.color(color);
			rand.setSeed(e.id);
			randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
				float s = e.fout(Interp.pow3In) * (size + rand.range(size / 3f));
				Fill.square(e.x + x, e.y + y, s, 45);
				Drawf.light(e.x + x, e.y + y, s * 2.25f, color, 0.7f);
			});
		});
	}
	
	public static Effect circleSplash(Color color, float lifetime, int num, float range, float size){
		return new Effect(lifetime, e -> {
			Draw.color(color);
			rand.setSeed(e.id);
			randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
				float s = e.fout(Interp.pow3In) * (size + rand.range(size / 3f));
				Fill.circle(e.x + x, e.y + y, s);
				Drawf.light(e.x + x, e.y + y, s * 2.25f, color, 0.7f);
			});
		});
	}
	
	public static Effect circleOut(Color color, float range){
		return new Effect(Mathf.clamp(range / 2, 45f, 360f), range * 1.5f, e -> {
			rand.setSeed(e.id);
			
			Draw.color(Color.white, color, e.fin() + 0.6f);
			float circleRad = e.fin(Interp.circleOut) * range;
			Lines.stroke(Mathf.clamp(range / 24, 4, 20) * e.fout());
			Lines.circle(e.x, e.y, circleRad);
			if(NHSetting.enableDetails())for(int i = 0; i < Mathf.clamp(range / 12, 9, 60); i++){
				Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
				DrawFunc.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16, circleRad / 12) * e.fout(), rand.random(circleRad / 4, circleRad / 1.5f) * (1 + e.fin()) / 2, Tmp.v1.angle() - 180);
			}
		});
	}
	
	public static Effect sharpBlast(Color colorExternal, Color colorInternal, float lifetime, float range){
		return new Effect(lifetime, range * 2, e -> {
			Angles.randLenVectors(e.id, (int)Mathf.clamp(range / 8, 4, 18), range / 8, range * (1 + e.fout(Interp.pow2OutInverse)) / 2f, (x, y) -> {
				float angle = Mathf.angle(x, y);
				float width = e.foutpowdown() * rand.random(range / 6, range / 3) / 2 * e.fout();
				
				rand.setSeed(e.id);
				float length = rand.random(range / 2, range * 1.1f) * e.fout();
				
				Draw.color(colorExternal);
				DrawFunc.tri(e.x + x, e.y + y, width, range / 3 * e.fout(Interp.pow2In), angle - 180);
				DrawFunc.tri(e.x + x, e.y + y, width, length, angle);
				
				if(!NHSetting.enableDetails())return;
				
				Draw.color(colorInternal);
				
				width *= e.fout();
				
				DrawFunc.tri(e.x + x, e.y + y, width / 2, range / 3 * e.fout(Interp.pow2In) * 0.9f * e.fout(), angle - 180);
				DrawFunc.tri(e.x + x, e.y + y, width / 2, length / 1.5f * e.fout(), angle);
			});
		});
	}

	public static Effect sharpBlastRand(Color colorExternal, Color colorInternal, float rotation, float ranAngle, float lifetime, float range){
		return new Effect(lifetime, range * 2, e -> {
			Angles.randLenVectors(e.id, (int)Mathf.clamp(range / 8, 2, 6), (1 + e.fout(Interp.pow2OutInverse)) / 2f, rotation, ranAngle, (x, y) -> {
				float angle = Mathf.angle(x, y);
				float width = e.foutpowdown() * rand.random(range / 6, range / 3) / 2 * e.fout();

				rand.setSeed(e.id);
				float length = rand.random(range / 2, range * 1.1f) * e.fout();

				Draw.color(colorExternal);
				DrawFunc.tri(e.x + x, e.y + y, width, length, angle);
				if(!NHSetting.enableDetails())return;
				Draw.color(colorInternal);
				width *= e.fout();
				DrawFunc.tri(e.x + x, e.y + y, width / 2, length / 1.5f * e.fout(), angle);
			});
		});
	}
	
	public static final float lightningAlign = 0.5f;
	
	public static Effect
		trailFadeFast =  new Effect(600f, e -> {
			if(!(e.data instanceof Trail)) return;
			Trail trail = e.data();
			//lifetime is how many frames it takes to fade out the trail
			e.lifetime = trail.length * 1.4f;
			
			if(!state.isPaused()){
				trail.shorten();
				trail.shorten();
			}
			trail.drawCap(e.color, e.rotation * e.foutpow());
			trail.draw(e.color, e.rotation * e.foutpow());
		}),
	
		cautionOutline = new Effect(60f, e -> {
			Draw.color(e.color);
			Lines.stroke(e.fout() * 2f);
			Lines.square(e.x, e.y, e.rotation + e.rotation / 8 * e.fin(Interp.circleOut) + 1);
		}),
	
		missileShoot = new Effect(130f, 300f, e -> {
			color(e.color);
			alpha(0.67f * e.fout(0.9f));
			rand.setSeed(e.id);
			for(int i = 0; i < 35; i++){
				v.trns(e.rotation + 180f + rand.range(21f), rand.random(e.finpow() * 90f)).add(rand.range(3f), rand.range(3f));
				e.scaled(e.lifetime * rand.random(0.2f, 1f), b -> {
					Fill.circle(e.x + v.x, e.y + v.y, b.fout() * 9f + 0.3f);
				});
			}
		}),
	
		healReceiveCircle = new Effect(11.0F, (e) -> {
			Draw.color(e.color);
			Lines.stroke(e.fout() * 1.667F);
			Lines.circle(e.x, e.y, 2.0F + e.finpow() * 7.0F);
		}),
	
		healSendCircle = new Effect(22.0F, (e) -> {
			Draw.color(e.color);
			Lines.stroke(e.fout() * 2.0F);
			Lines.circle(e.x, e.y, e.finpow() * e.rotation);
		}),
	
		crossSpinBlast = (new Effect(150.0F, 600.0F, (e) -> {
			float scl = 1.0F;
			Draw.color(e.color, Color.white, e.fout() * 0.25F);
			float extend = Mathf.curve(e.fin(), 0.0F, 0.015F) * scl;
			float rot = e.fout(Interp.pow3In);
			
			for(int i : Mathf.signs){
				DrawFunc.tri(e.x, e.y, 9.0F * e.foutpowdown() * scl, 100.0F + 300.0F * extend, e.rotation + 180.0F * rot + (float)(90 * i) + 45.0F);
				DrawFunc.tri(e.x, e.y, 9.0F * e.foutpowdown() * scl, 100.0F + 300.0F * extend, e.rotation + 180.0F * rot + (float)(90 * i) - 45.0F);
			}
			
			for(int i : Mathf.signs){
				DrawFunc.tri(e.x, e.y, 6.0F * e.foutpowdown() * scl, 40.0F + 120.0F * extend, e.rotation + 270.0F * rot + (float)(90 * i) + 45.0F);
				DrawFunc.tri(e.x, e.y, 6.0F * e.foutpowdown() * scl, 40.0F + 120.0F * extend, e.rotation + 270.0F * rot + (float)(90 * i) - 45.0F);
			}
		})).followParent(true).layer(100.0F),
		
		energyUnitBlast = new Effect(150F, 1600f, e -> {
			float rad = e.rotation;
			rand.setSeed(e.id);
			
			Draw.color(Color.white, e.color, e.fin() / 5 + 0.6f);
			float circleRad = e.fin(Interp.circleOut) * rad;
			Lines.stroke(12 * e.fout());
			Lines.circle(e.x, e.y, circleRad);
			
			e.scaled(120f, i -> {
				Fill.circle(i.x, i.y, rad * i.fout() / 2);
				Lines.stroke(18 * i.fout());
				Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 1.2f);
				
				Angles.randLenVectors(i.id, (int)(rad / 4), rad / 6, rad * (1 + i.fout(Interp.circleOut)) / 2f, (x, y) -> {
					float angle = Mathf.angle(x, y);
					float width = i.foutpowdown() * rand.random(rad / 8, rad / 10);
					float length = rand.random(rad / 2, rad) * i.fout(Interp.circleOut);
					
					Draw.color(i.color);
					DrawFunc.tri(i.x + x, i.y + y, width, rad / 8 * i.fout(Interp.circleOut), angle - 180);
					DrawFunc.tri(i.x + x, i.y + y, width, length, angle);
					
					Draw.color(Color.black);
					
					width *= i.fout();
					
					DrawFunc.tri(i.x + x, i.y + y, width / 2, rad / 8 * i.fout(Interp.circleOut) * 0.9f * i.fout(), angle - 180);
					DrawFunc.tri(i.x + x, i.y + y, width / 2, length / 1.5f * i.fout(), angle);
				});
				
				Draw.color(Color.black);
				Fill.circle(i.x, i.y, rad * i.fout() * 0.375f);
			});
			
			Drawf.light(e.x, e.y, rad * e.fout() * 4f * Mathf.curve(e.fin(), 0f, 0.05f), e.color, 0.7f);
		}).layer(Layer.effect + 0.001f),
	
		absorbFix = new Effect(12, e -> {
			color(e.color);
			stroke(2f * e.fout());
			Lines.circle(e.x, e.y, 5f * e.fout());
		}),
	
		collapserBulletExplode = new Effect(300F, 1600f, e -> {
			float rad = 150f;
			rand.setSeed(e.id);
		
			Draw.color(Color.white, e.color, e.fin() + 0.6f);
			float circleRad = e.fin(Interp.circleOut) * rad * 4f;
			Lines.stroke(12 * e.fout());
			Lines.circle(e.x, e.y, circleRad);
			for(int i = 0; i < 24; i++){
				Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
				DrawFunc.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16, circleRad / 12) * e.fout(), rand.random(circleRad / 4, circleRad / 1.5f) * (1 + e.fin()) / 2, Tmp.v1.angle() - 180);
			}
			
			if(NHSetting.enableDetails()){
				Draw.blend(Blending.additive);
				Draw.z(Layer.effect + 0.1f);
				
				Fill.light(e.x, e.y, circleVertices(circleRad), circleRad, Color.clear, Tmp.c1.set(Draw.getColor()).a(e.fout(Interp.pow10Out)));
				Draw.blend();
				Draw.z(Layer.effect);
			}
			
			
			e.scaled(120f, i -> {
				Draw.color(Color.white, i.color, i.fin() + 0.4f);
				Fill.circle(i.x, i.y, rad * i.fout());
				Lines.stroke(18 * i.fout());
				Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 1.2f);
				Angles.randLenVectors(i.id, 40, rad / 3, rad * i.fin(Interp.pow2Out), (x, y) -> {
					lineAngle(i.x + x, i.y + y, Mathf.angle(x, y), i.fslope() * 25 + 10);
				});
				
				if(NHSetting.enableDetails())Angles.randLenVectors(i.id, (int)(rad / 4), rad / 6, rad * (1 + i.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
					float angle = Mathf.angle(x, y);
					float width = i.foutpowdown() * rand.random(rad / 6, rad / 3);
					float length = rand.random(rad / 2, rad * 5) * i.fout(Interp.circleOut);
					
					Draw.color(i.color);
					DrawFunc.tri(i.x + x, i.y + y, width, rad / 3 * i.fout(Interp.circleOut), angle - 180);
					DrawFunc.tri(i.x + x, i.y + y, width, length, angle);
					
					Draw.color(Color.black);
					
					width *= i.fout();
					
					DrawFunc.tri(i.x + x, i.y + y, width / 2, rad / 3 * i.fout(Interp.circleOut) * 0.9f * i.fout(), angle - 180);
					DrawFunc.tri(i.x + x, i.y + y, width / 2, length / 1.5f * i.fout(), angle);
				});
				
				Draw.color(Color.black);
				Fill.circle(i.x, i.y, rad * i.fout() * 0.75f);
			});
			
			Drawf.light(e.x, e.y, rad * e.fout(Interp.circleOut) * 4f, e.color, 0.7f);
		}).layer(Layer.effect + 0.001f),
	
		hitSpark = new Effect(45, e -> {
			color(e.color, Color.white, e.fout() * 0.3f);
			stroke(e.fout() * 1.6f);
			
			rand.setSeed(e.id);
			randLenVectors(e.id, 8, e.finpow() * 20f, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(1.95f, 4.25f) + 1f);
			});
		}),
	
		hitSparkLarge = new Effect(40, e -> {
			color(e.color, Color.white, e.fout() * 0.3f);
			stroke(e.fout() * 1.6f);
			
			rand.setSeed(e.id);
			randLenVectors(e.id, 18, e.finpow() * 27f, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(4, 8) + 2f);
			});
		}),
	
		hitSparkHuge = new Effect(70, e -> {
			color(e.color, Color.white, e.fout() * 0.3f);
			stroke(e.fout() * 1.6f);
			
			rand.setSeed(e.id);
			randLenVectors(e.id, 26, e.finpow() * 65f, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(6, 9) + 3f);
			});
		}),
	
		/**{@link Effect.EffectContainer#data}<{@link Position}> as Target */
		chainLightningFade = new Effect(220f, 500f, e -> {
			if(!(e.data instanceof Position)) return;
			Position p = e.data();
			float tx = p.getX(), ty = p.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
			Tmp.v1.set(p).sub(e.x, e.y).nor();
			
			e.lifetime = dst * 0.3f;
			float normx = Tmp.v1.x, normy = Tmp.v1.y;
			float range = e.rotation;
			int links = Mathf.ceil(dst / range);
			float spacing = dst / links;
			
			stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
			color(e.color ,Color.white, e.fout() * 0.6f);
			
			beginLine();
		
			Fill.circle(e.x, e.y, getStroke() / 2);
			linePoint(e.x, e.y);
			
			rand.setSeed(e.id);
		
			float fin = Mathf.curve(e.fin(), 0, lightningAlign);
			int i;
			float nx = e.x, ny = e.y;
			for(i = 0; i < (int)(links * fin); i++){
				if(i == links - 1){
					nx = tx;
					ny = ty;
				}else{
					float len = (i + 1) * spacing;
					Tmp.v1.setToRandomDirection(rand).scl(range/2f);
					nx = e.x + normx * len + Tmp.v1.x;
					ny = e.y + normy * len + Tmp.v1.y;
				}
				
				linePoint(nx, ny);
			}

			if(NHSetting.enableDetails() && i < links){
				float f = Mathf.clamp(fin * links % 1);
				float len = (i + 1) * spacing;
				Tmp.v1.setToRandomDirection(rand).scl(range/2f);
				Tmp.v2.set(nx, ny);
				if(i == links - 1)Tmp.v2.lerp(tx, ty, f);
				else Tmp.v2.lerp(e.x + (normx * len + Tmp.v1.x), e.y + (normy * len + Tmp.v1.y), f);
				
				linePoint(Tmp.v2.x, Tmp.v2.y);
				Fill.circle(Tmp.v2.x, Tmp.v2.y, getStroke() / 2);
			}
			
			endLine();
		}).followParent(false),
	
		/**{@link Effect.EffectContainer} as Target */
		chainLightningFadeReversed = new Effect(220f, 500f, e -> {
			if(!(e.data instanceof Position))return;
			Position p = e.data();
			float tx = e.x, ty = e.y, dst = Mathf.dst(p.getX(), p.getY(), tx, ty);
			Tmp.v1.set(e.x, e.y).sub(p).nor();
			
			e.lifetime = dst * 0.3f;
			float normx = Tmp.v1.x, normy = Tmp.v1.y;
			float range = e.rotation;
			int links = Mathf.ceil(dst / range);
			float spacing = dst / links;
			
			Lines.stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
			color(e.color ,Color.white, e.fout() * 0.6f);
			
			Lines.beginLine();
			
			Fill.circle(p.getX(), p.getY(), Lines.getStroke() / 2);
			Lines.linePoint(p);
			
			rand.setSeed(e.id);
			
			float fin = Mathf.curve(e.fin(), 0, lightningAlign);
			int i;
			float nx = p.getX(), ny = p.getY();
			for(i = 0; i < (int)(links * fin); i++){
				if(i == links - 1){
					nx = tx;
					ny = ty;
				}else{
					float len = (i + 1) * spacing;
					Tmp.v1.setToRandomDirection(rand).scl(range/2f);
					nx = p.getX() + normx * len + Tmp.v1.x;
					ny = p.getY() + normy * len + Tmp.v1.y;
				}
				
				linePoint(nx, ny);
			}
			
			if(NHSetting.enableDetails() && i < links){
				float f = Mathf.clamp(fin * links % 1);
				float len = (i + 1) * spacing;
				Tmp.v1.setToRandomDirection(rand).scl(range/2f);
				Tmp.v2.set(nx, ny);
				if(i == links - 1)Tmp.v2.lerp(tx, ty, f);
				else Tmp.v2.lerp(p.getX() + (normx * len + Tmp.v1.x), p.getY() + (normy * len + Tmp.v1.y), f);
				
				linePoint(Tmp.v2.x, Tmp.v2.y);
				Fill.circle(Tmp.v2.x, Tmp.v2.y, getStroke() / 2);
			}
			
			Lines.endLine();
		}).followParent(false),
	
		lightningHitLarge = new Effect(50f, 180f, e -> {
			color(e.color);
			Drawf.light(e.x, e.y, e.fout() * 90f, e.color, 0.7f);
			e.scaled(25f, t -> {
				stroke(3f * t.fout());
				circle(e.x, e.y, 3f + t.fin(Interp.pow3Out) * 80f);
			});
			Fill.circle(e.x, e.y, e.fout() * 8f);
			randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));
			
			color(Color.gray);
			Angles.randLenVectors(e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
		}),
	
		lightningHitSmall = new Effect(Fx.chainLightning.lifetime, e -> {
			color(Color.white, e.color, e.fin() + 0.25f);
			
			e.scaled(7f, s -> {
				stroke(0.5f + s.fout());
				Lines.circle(e.x, e.y, s.fin() * (e.rotation + 12f));
			});
			
			stroke(0.75f + e.fout());
			
			randLenVectors(e.id, 6, e.fin() * e.rotation + 7f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4 + 2f));
		
			Fill.circle(e.x, e.y, 2.5f * e.fout());
		}),
	
		lightningSpark = new Effect(Fx.chainLightning.lifetime, e -> {
			color(Color.white, e.color, e.fin() + 0.25f);
			
			stroke(0.65f + e.fout());
			
			randLenVectors(e.id, 3, e.fin() * e.rotation + 6f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4 + 2f));
			
			Fill.circle(e.x, e.y, 2.5f * e.fout());
		}),
	
		absorb = new Effect(20f, e -> {
			color(e.color);
			stroke(e.rotation * e.fout());
			Lines.poly(e.x, e.y, 6, (e.rotation + e.rotation * e.fin(Interp.pow2In) * 1.75f) / 2f);
		}),
	
		project = new Effect(60f, 1600f, e -> {
			if(!(e.data instanceof Position))return;
			Position data = e.data();
			color(e.color);
			
			Lines.stroke(e.rotation * e.fout(0.5f));
			Lines.line(e.x, e.y, data.getX(), data.getY(), false);
			Fill.circle(e.x, e.y, Lines.getStroke() * 1.25f);
			Fill.circle(data.getX(), data.getY(), Lines.getStroke() * 2f);
		}),
	
		transport = new Effect(22f, 400, e -> {
			if(!(e.data instanceof Position))return;
			Position to = e.data();
			Tmp.v1.set(e.x, e.y).interpolate(Tmp.v2.set(to), e.fin(), Interp.pow3)
					.add(Tmp.v2.sub(e.x, e.y).nor().rotate90(1).scl(Mathf.randomSeedRange(e.id, 1f) * e.fslope() * 10f));
			float x = Tmp.v1.x, y = Tmp.v1.y;
			float size = 1f;
			
			color(e.color, Color.white, e.fout() * 0.75f);
			Fill.circle(x, y, e.fslope() * e.rotation * size);
		}),
		
		attackWarningPos = new Effect(120f, 2000f, e -> {
			if(!(e.data instanceof Position))return;
			
			e.lifetime = e.rotation;
			Position pos = e.data();
			
			Draw.color(e.color);
			TextureRegion arrowRegion = NHContent.arrowRegion;
			float scl =	Mathf.curve(e.fout(), 0f, 0.1f);
			Lines.stroke(2 * scl);
			Lines.line(pos.getX(), pos.getY(), e.x, e.y);
			Fill.circle(pos.getX(), pos.getY(), Lines.getStroke());
			Fill.circle(e.x, e.y, Lines.getStroke());
			Tmp.v1.set(e.x, e.y).sub(pos).scl(e.fin(Interp.pow2In)).add(pos);
			Draw.rect(arrowRegion,  Tmp.v1.x,  Tmp.v1.y, arrowRegion.width * scl * Draw.scl, arrowRegion.height * scl * Draw.scl, pos.angleTo(e.x, e.y) - 90f);
		}),
	
		attackWarningRange = new Effect(120f, 2000f, e -> {
			Draw.color(e.color);
			Lines.stroke(2 * e.fout());
			Lines.circle(e.x, e.y, e.rotation);
			
			for(float i = 0.75f; i < 1.5f; i += 0.25f){
				Lines.spikes(e.x, e.y, e.rotation / i, e.rotation / 10f, 4, e.time);
				Lines.spikes(e.x, e.y, e.rotation / i / 1.5f, e.rotation / 12f, 4, -e.time * 1.25f);
			}
			
			TextureRegion arrowRegion = NHContent.arrowRegion;
			float scl =	Mathf.curve(e.fout(), 0f, 0.1f);
			
			for (int l = 0; l < 4; l++) {
				float angle = 90 * l;
				float regSize = e.rotation / 150f;
				for (int i = 0; i < 4; i++) {
					Tmp.v1.trns(angle, (i - 4) * tilesize * e.rotation / tilesize / 4);
					float f = (100 - (Time.time - 25 * i) % 100) / 100;
					
					Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, arrowRegion.width * regSize * f * scl, arrowRegion.height * regSize * f * scl, angle - 90);
				}
			}
			
		}),
	
		shareDamage = new Effect(45f, e-> {
			if(!(e.data instanceof Number))return;
			Draw.color(e.color);
			Draw.alpha(((Number)e.data()).floatValue() * e.fout());
			Fill.square(e.x, e.y, e.rotation);
		}),
	
		square45_4_45 = new Effect(45f, e-> {
			Draw.color(e.color);
			randLenVectors(e.id, 5, 20f * e.finpow(), (x, y) -> {
				Fill.square(e.x + x, e.y + y, 4f * e.fout(), 45);
				Drawf.light(e.x + x, e.y + y, e.fout() * 6f, e.color, 0.7f);
			});
		}),
	
		square45_6_45 = new Effect(45f, e-> {
			Draw.color(e.color, Color.white, e.fout() * 0.6f);
			randLenVectors(e.id, 6, 27f * e.finpow(), (x, y) -> {
				Fill.square(e.x + x, e.y + y, 5f * e.fout(), 45);
				Drawf.light(e.x + x, e.y + y, e.fout() * 9F, e.color, 0.7f);
			});
		}),
	
		square45_6_45_Charge = new Effect(90f, e-> {
			Draw.color(e.color, Color.white, e.fin() * 0.6f);
			randLenVectors(e.id, 12, 60 * e.fout(Interp.pow4Out), (x, y) -> {
				Fill.square(e.x + x, e.y + y, 5f * e.fin(), 45);
				Drawf.light(e.x + x, e.y + y, e.fin() * 9F, e.color, 0.7f);
			});
			
			Lines.stroke(2f * e.fin());
			Lines.circle(e.x, e.y, 80 * e.fout(Interp.pow5Out));
		}),
	
		square45_8_45 = new Effect(45f, e-> {
			Draw.color(e.color, Color.white, e.fout() * 0.6f);
			randLenVectors(e.id, 7, 34f * e.finpow(), (x, y) -> {
				Fill.square(e.x + x, e.y + y, 8f * e.fout(), 45);
				Drawf.light(e.x + x, e.y + y, e.fout() * 12f, e.color, 0.7f);
			});
		}),
	
		poly = new Effect(25f, e -> {
			Draw.color(e.color);
			Lines.stroke(e.fout() * 2.0F);
			Lines.poly(e.x, e.y, 6, 2.0F + e.finpow() * e.rotation);
		}),
	
		healEffectSky = new Effect(11.0F, e -> {
			Draw.color(NHColor.lightSkyBack);
			Lines.stroke(e.fout() * 2.0F);
			Lines.poly(e.x, e.y, 6, 2.0F + e.finpow() * 79.0F);
		}),
	
		activeEffectSky = new Effect(22.0F, e -> {
			Draw.color(NHColor.lightSkyBack);
			Lines.stroke(e.fout() * 3.0F);
			Lines.poly(e.x, e.y, 6,4.0F + e.finpow() * e.rotation);
		}),
	
		spawnGround = new Effect(60f, e -> {
			Draw.color(e.color, Pal.gray, e.fin());
			randLenVectors(e.id, (int)(e.rotation * 1.35f), e.rotation * tilesize / 1.125f * e.fin(), (x, y) -> Fill.square(e.x + x, e.y + y, e.rotation * e.fout(), 45));
		}),
		
		spawnWave = new Effect(60f, e -> {
			stroke(3 * e.fout(), e.color);
			circle(e.x, e.y, e.rotation * e.finpow());
		}),

		trailToGray = new Effect(50.0F, e -> {
			Draw.color(e.color, Color.gray, e.fin());
			randLenVectors(e.id, 2, tilesize * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.rotation * e.fout()));
		}),
	
		trailFromWhite = new Effect(50.0F, e -> {
			Draw.color(e.color, Color.white, e.fout() * 0.35f);
			randLenVectors(e.id, 2, tilesize * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.rotation * e.fout()));
		}),
	
		trailSolid = new Effect(50.0F, e -> {
			Draw.color(e.color);
			randLenVectors(e.id, 2, tilesize * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.rotation * e.fout()));
		}),
	
		boolSelector = new Effect(0, 0, e -> {}),
	
		skyTrail = new Effect(22, e -> {
			color(NHColor.lightSkyBack, Pal.gray, e.fin() * 0.6f);
			
			rand.setSeed(e.id);
			randLenVectors(e.id, 3, e.finpow() * 13, e.rotation - 180, 30f, (x, y) -> {
				Fill.poly(e.x + x, e.y + y, 6, rand.random(2, 4) * e.foutpow(), e.rotation);
			});
		}),
	
		circleOut  = new Effect(60f, 500f, e -> {
			Lines.stroke(2.5f * e.fout(), e.color);
			Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
		}),
	
		circleOutQuick  = new Effect(30f, 500f, e -> {
			Lines.stroke(2.5f * e.fout(), e.color);
			Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
		}),
	
		circleOutLong  = new Effect(120f, 500f, e -> {
			Lines.stroke(2.5f * e.fout(), e.color);
			Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
		}),
	
		shuttle = new Effect(70f, 800f, e -> {
			if(!(e.data instanceof Float))return;
			float len = e.data();
			
			color(e.color, Color.white, e.fout() * 0.3f);
			stroke(e.fout() * 2.2F);
			
			randLenVectors(e.id, (int)Mathf.clamp(len / 12, 10, 40), e.finpow() * len, e.rotation, 360f, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15f + len * 0.025f);
			});
			
			float fout = e.fout(Interp.exp10Out);
			for(int i : Mathf.signs) {
				DrawFunc.tri(e.x, e.y, len / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
			}
		}),
	
		shuttleDark = new Effect(70f, 800f, e -> {
			if(!(e.data instanceof Float))return;
			float len = e.data();
			
			color(e.color, Color.white, e.fout() * 0.3f);
			stroke(e.fout() * 2.2F);
			
			randLenVectors(e.id, (int)Mathf.clamp(len / 12, 10, 40), e.finpow() * len, e.rotation, 360f, (x, y) -> {
				float ang = Mathf.angle(x, y);
				lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15f + len * 0.025f);
			});
			
			float fout = e.fout(Interp.exp10Out);
			for(int i : Mathf.signs) {
				DrawFunc.tri(e.x, e.y, len / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
			}
		
			float len1 = len * 0.66f;
			z(EFFECT_MASK);
			color(Color.black);
			for(int i : Mathf.signs) {
				DrawFunc.tri(e.x, e.y, len1 / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len1 * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
			}
			
			z(EFFECT_BOTTOM);
			for(int i : Mathf.signs) {
				DrawFunc.tri(e.x, e.y, len1 / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len1 * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
			}
		}).layer(Layer.effect - 1f),
	
		shuttleLerp = new Effect(180f, 800f, e -> {
			if(!(e.data instanceof Float))return;
			float f = Mathf.curve(e.fin(Interp.pow5In), 0f, 0.07f) * Mathf.curve(e.fout(), 0f, 0.4f);
			float len = e.data();
			
			color(e.color);
			v.trns(e.rotation - 90, (len + Mathf.randomSeed(e.id, 0, len)) * e.fin(Interp.circleOut));
			for(int i : Mathf.signs) DrawFunc.tri(e.x + v.x, e.y + v.y, Mathf.clamp(len / 8, 8, 25) * (f + e.fout(0.2f) * 2f) / 3.5f, len * 1.75f * e.fin(Interp.circleOut), e.rotation + 90 + i * 90);
		}),
		
		line = new Effect(30f, e -> {
			color(e.color, Color.white, e.fout() * 0.75f);
			stroke(2 * e.fout());
			randLenVectors(e.id, 6, 3 + e.rotation * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 4));
		}),
		
		circle = new Effect(25f, e -> {
			color(e.color, Color.white, e.fout() * 0.65f);
			stroke(Mathf.clamp(e.rotation / 18f, 2, 6) * e.fout());
			circle(e.x, e.y, e.rotation * e.finpow());
		}),
	
		 unitLandSize = (new Effect(30.0F, e -> {
			 Draw.color(Pal.lightishGray);
			 Angles.randLenVectors(e.id, 9, 3.0F + 20.0F * e.finpow(), (x, y) -> {
				 Fill.circle(e.x + x, e.y + y, e.fout() * e.rotation + 0.4F);
			 });
		})).layer(20.0F),
	
		spawn = new Effect(100f, e -> {
			TextureRegion pointerRegion = NHContent.pointerRegion;

			Draw.color(e.color);

			for (int j = 1; j <= 3; j ++) {
				for(int i = 0; i < 4; i++) {
					float length = e.rotation * 3f + tilesize;
					float x = Angles.trnsx(i * 90, -length), y = Angles.trnsy(i * 90, -length);
					e.scaled(30 * j, k -> {
						float signSize = e.rotation / tilesize / 3f * Draw.scl * k.fout();
						Draw.rect(pointerRegion, e.x + x * k.finpow(), e.y + y * k.finpow(), pointerRegion.width * signSize, pointerRegion.height * signSize, Angles.angle(x, y) - 90);
						Drawf.light(e.x + x, e.y + y, e.fout() * signSize * pointerRegion.height, e.color, 0.7f);
					});
				}
			}
		}),

		jumpTrail = new Effect(120f, 5000, e -> {
			if (!(e.data instanceof UnitType))return;
			UnitType type = e.data();
			color(type.engineColor == null ? e.color : type.engineColor);
			
			if(type.engineLayer > 0)Draw.z(type.engineLayer);
			else Draw.z((type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) - 0.001f);
			
			for(int index = 0; index < type.engines.size; index++){
				UnitType.UnitEngine engine = type.engines.get(index);
				
				if(Angles.angleDist(engine.rotation, -90) > 75)return;
				float ang = Mathf.slerp(engine.rotation, -90, 0.75f);
				
				//noinspection SuspiciousNameCombination
				Tmp.v1.trns(e.rotation, engine.y, -engine.x);
				
				e.scaled(80, i -> {
					DrawFunc.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 1.5f * i.fout(Interp.slowFast), 3000 * engine.radius / (type.engineSize + 4), i.rotation + ang - 90);
					Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 1.5f * i.fout(Interp.slowFast));
				});
				
				randLenVectors(e.id + index, 22, 400 * engine.radius / (type.engineSize + 4), e.rotation + ang - 90, 0f, (x, y) -> lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60));
			}
			
			Draw.color();
			Draw.mixcol(e.color, 1);
			Draw.rect(type.fullIcon, e.x, e.y, type.fullIcon.width * e.fout(Interp.pow2Out) * Draw.scl * 1.2f, type.fullIcon.height * e.fout(Interp.pow2Out) * Draw.scl * 1.2f, e.rotation - 90f);
			Draw.reset();
		}),
	
		jumpTrailOut = new Effect(120f, 200, e -> {
			if (!(e.data instanceof UnitType))return;
			UnitType type = e.data();
			color(type.engineColor == null ? e.color : type.engineColor);
			
			if(type.engineLayer > 0)Draw.z(type.engineLayer);
			else Draw.z((type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) - 0.001f);
			
			Tmp.v2.trns(e.rotation, 2300);
			
			for(int index = 0; index < type.engines.size; index++){
				UnitType.UnitEngine engine = type.engines.get(index);
				
				if(Angles.angleDist(engine.rotation, -90) > 75)return;
				float ang = Mathf.slerp(engine.rotation, -90, 0.75f);
				
				//noinspection SuspiciousNameCombination
				Tmp.v1.trns(e.rotation, engine.y, -engine.x).add(Tmp.v2);
				
				rand.setSeed(e.id);
				e.scaled(80, i -> {
					DrawFunc.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3f * i.fout(Interp.slowFast), 2300 + rand.range(120), i.rotation + ang - 90);
					Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3f * i.fout(Interp.slowFast));
				});
				
				randLenVectors(e.id + index, 42, 2330, e.rotation + ang - 90, 0f, (x, y) -> lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60));
			}
		}),
	
//		fellowTrail = new Effect(90f, 5000, e -> {
//			if(!(e.data instanceof Vec2))return;
//			Vec2 data = e.data();
//
//			color(e.color);
//		}),
	
		dataTransport = new Effect(60f, 100f, e -> {
			rand.setSeed(e.id);
			int i = rand.random(8, 10000);
			
			Vec2 vec2 = Tmp.v1.setToRandomDirection(rand).scl(rand.random(36f, 80f) * e.rotation * e.fout(Interp.pow2Out)).add(e.x, e.y);
			
			String text = Integer.toBinaryString(i).substring(1);
			
			Font font = Fonts.tech;
			GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
			boolean ints = font.usesIntegerPositions();
			font.setUseIntegerPositions(false);
			font.getData().setScale(Mathf.curve(e.fin(), 0, 0.1f) * e.rotation * (e.fout(Interp.pow2In) * 3 + 1) / 4 / Scl.scl(1.0f) + 0.01f);
			layout.setText(font, text);
			font.setColor(e.color);
			
			font.draw(text, vec2.x, vec2.y, 1);
			
			font.setUseIntegerPositions(ints);
			font.setColor(Color.white);
			font.getData().setScale(1.0F);
			Draw.reset();
			Pools.free(layout);
		}),

		darkEnergySpread = new Effect(32f, e -> randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x + x, e.y + y, e.fout() * 15f);
			color(NHColor.darkEnr);
			Fill.circle(e.x + x, e.y + y, e.fout() * 9f);
			Drawf.light(e.x + x, e.y + y, e.fout() * 25f, NHColor.darkEnrColor, 0.7f);
		})),
		
		largeDarkEnergyHitCircle = new Effect(20f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 44);
			randLenVectors(e.id, 5, 60f * e.fin(), (x,y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 8));
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 30);
			Drawf.light(e.x, e.y, e.fout() * 55f, NHColor.darkEnrColor, 0.7f);
		}),
		
		largeDarkEnergyHit = new Effect(50, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 44);
			stroke(e.fout() * 3.2f);
			circle(e.x, e.y, e.fin() * 80);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 50);
			stroke(e.fout() * 3.2f);
			randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
			});
			
			Draw.z(EFFECT_MASK);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 30);
			Drawf.light(e.x, e.y, e.fout() * 80f, NHColor.darkEnrColor, 0.7f);
			
			Draw.z(EFFECT_BOTTOM);
			Fill.circle(e.x, e.y, e.fout() * 31);
			Draw.z(Layer.effect - 0.0001f);
		}).layer(Layer.effect - 0.0001f),
		
		mediumDarkEnergyHit = new Effect(23, e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 2.8f);
			circle(e.x, e.y, e.fin() * 60);
			stroke(e.fout() * 2.12f);
			circle(e.x, e.y, e.fin() * 35);
			
			stroke(e.fout() * 2.25f);
			randLenVectors(e.id, 9, 7f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 12f));
			
			Fill.circle(e.x, e.y, e.fout() * 22);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 14);
			Drawf.light(e.x, e.y, e.fout() * 80f, NHColor.darkEnrColor, 0.7f);
		}),
		
		darkEnergySmokeBig = new Effect(30f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 32);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 20);
			Drawf.light(e.x, e.y, e.fout() * 36f, NHColor.darkEnrColor, 0.7f);
		}),
		
		darkEnergyShootBig = new Effect(40f, 100, e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 3.7f);
			circle(e.x, e.y, e.fin() * 100 + 15);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 60 + 15);
			randLenVectors(e.id, 15, 7f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 16f));
			Drawf.light(e.x, e.y, e.fout() * 120f, NHColor.darkEnrColor, 0.7f);
		}),
		
		polyTrail = new Effect(25f, e -> {
			color(e.color, Pal.gray, e.fin());
			randLenVectors(e.id, 4, 46f * e.fin(), (x, y) -> {
				Fill.poly(e.x + x, e.y + y, 6, e.rotation * e.fslope() * e.fout());
				Drawf.light(e.x + x, e.y + y, e.fout() * e.rotation * 1.15f, e.color, 0.7f);
			});
		}),
		
		darkEnergyLaserShoot = new Effect(26f, 880, e -> {
			color(Color.white, NHColor.darkEnrColor, e.fin() * 0.75f);
			float length = !(e.data instanceof Float) ? 70f : (Float)e.data;
			randLenVectors(e.id, 9, length, e.rotation, 0f, (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * (length / 14));
				Drawf.light(e.x + x, e.y + y, e.fout() * (length / 12), NHColor.darkEnrColor, 0.7f);
			});
		}),
	
		circleShieldBreak = new Effect(30f, 600f, e -> {
			color(e.color);
			stroke(3f * e.fout());
			Lines.circle(e.x, e.y, e.rotation + e.fin() * 5f);
		}).followParent(true),
		
		darkEnergySmoke = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 4, 60 * e.fin(), e.rotation, 30, (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4);
				Drawf.light(e.x + x, e.y + y, e.fout() * 4.5f, NHColor.darkEnrColor, 0.7f);
			});
		}),
		
		darkEnergyShoot = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			for (int i : Mathf.signs){
				DrawFunc.tri(e.x, e.y, 2 + 2 * e.fout(), 28 * e.fout(), e.rotation + 90 * i);
			}
		}),
		
		darkEnergyCharge = new Effect(130f, e -> {
			rand.setSeed(e.id);
			randLenVectors(e.id, 12, 140f * e.fout(Interp.pow3Out), (x, y) -> {
				color(NHColor.darkEnrColor);
				float rad = rand.random(9f, 18f);
				float scl = rand.random(0.6f, 1f);
				float dx = e.x + scl * x, dy = e.y + scl * y;
				Fill.circle(dx, dy, e.fin() * rad);
				color(NHColor.darkEnr);
				Draw.z(EFFECT_MASK);
				Fill.circle(dx, dy, e.fin() * rad / 1.8f);
				Draw.z(EFFECT_BOTTOM);
				Fill.circle(dx, dy, e.fin() * rad / 1.8f);
				Draw.z(Layer.effect);
				Drawf.light(dx, dy, e.fin() * rad * 1.5f, NHColor.darkEnrColor, 0.7f);
			});
		}),
	
		hugeTrail = new Effect(40f, e -> {
			Draw.color(e.color);
			Draw.alpha(e.fout(0.85f) * 0.85f);
			Angles.randLenVectors(e.id, 6, 2.0F + e.rotation * 5f * e.finpow(), (x, y) -> {
				Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout(Interp.pow3Out) * e.rotation);
			});
		}),
	
		hugeSmokeGray = new Effect(40f, e -> {
			Draw.color(Color.gray, Color.darkGray, e.fin());
			Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2f));
			e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F)));
		}),
	
		hugeSmoke = new Effect(40f, e -> {
			Draw.color(e.color);
			Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2f));
			e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F)));
		}),
	
		hugeSmokeLong = new Effect(120f, e -> {
			Draw.color(e.color);
			Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2f));
			e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F)));
		}),
	
		darkEnergyChargeBegin = new Effect(130f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fin() * 32);
			stroke(e.fin() * 3.7f);
			circle(e.x, e.y, e.fout() * 80);
			Draw.z(EFFECT_MASK);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fin() * 20);
			
			Draw.z(EFFECT_BOTTOM);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fin() * 22);
			Drawf.light(e.x, e.y, e.fin() * 35f, NHColor.darkEnrColor, 0.7f);
		}),
		
		upgrading = new Effect(30, e -> {
			color(e.color);
			float drawSize = e.rotation * tilesize * e.fout();
			rect(Core.atlas.find("new-horizon-upgrade"), e.x, e.y + e.rotation * tilesize * 1.35f * e.finpow(), drawSize, drawSize);
		}),
		
		darkErnExplosion = new Effect(40, e -> {
			color(NHColor.darkEnrColor);
			e.scaled(20, i -> {
				stroke(3f * i.foutpow());
				circle(e.x, e.y, 3f + i.finpow() * 80f);
			});

			stroke(e.fout());
			randLenVectors(e.id + 1, 8, 1f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 3f));

			color(Color.gray);

			randLenVectors(e.id, 5, 2f + 70 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.5f));
			
			Drawf.light(e.x, e.y, e.fout(Interp.pow2Out) * 100f, NHColor.darkEnrColor, 0.7f);
		}),
		
		lightSkyCircleSplash = new Effect(26f, e -> {
			color(NHColor.lightSkyBack);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
				Drawf.light(e.x + x, e.y + y, e.fout() * 3.5f, NHColor.lightSkyBack, 0.7f);
			});
		}),
		
		darkEnrCircleSplash = new Effect(26f, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4.5f);
				Drawf.light(e.x + x, e.y + y, e.fout() * 5f, NHColor.darkEnrColor, 0.7f);
			});
		}),
		
		circleSplash = new Effect(26f, e -> {
			color(Color.white, e.color, e.fin() + 0.15f);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
				Drawf.light(e.x + x, e.y + y, e.fout() * 3.5f, e.color, 0.7f);
			});
		}),
		
		blastgenerate = new MultiEffect(new Effect(40f, 600,e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 3.7f);
			circle(e.x, e.y, e.fin(Interp.pow3Out) * 240 + 15);
			rand.setSeed(e.id);
			randLenVectors(e.id, 12, 8 + 60 * e.fin(Interp.pow5Out), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout(Interp.circleIn) * (6f + rand.random(6f))));
			Drawf.light(e.x, e.y, e.fout() * 320f, NHColor.darkEnrColor, 0.7f);
		}), circleOut(NHColor.darkEnrColor, 120f)),
		
		blastAccept = new Effect(20f, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 3, 5 + 30 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f));
		}),
	
		thurmixHit = new Effect(35f, 80f, e -> {
			color(NHColor.thurmixRedLight, Color.white, e.fin());
			stroke(3 * e.fout());
			circle(e.x, e.y, 75f * e.fin());
			
			stroke(1.3f * e.fslope());
			e.scaled(20f, i-> randLenVectors(e.id, 11, 1f + 60f * i.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 5f + i.fslope() * 8f)));
			
			color(Color.gray);
			randLenVectors(e.id + 1, 7, 8f + 70 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 6f + 0.5f));
			Drawf.light(e.x, e.y, e.fout() * 80f, NHColor.thurmixRedLight, 0.7f);
		}),
	
		hyperCloud = new Effect(140.0F, 400.0F, e -> {
			randLenVectors(e.id, 20, e.finpow() * 160.0F, (x, y) -> {
				float size = e.fout() * 15.0F;
				Draw.color(e.color, Color.lightGray, e.fin());
				Fill.circle(e.x + x, e.y + y, size / 2.0F);
				Drawf.light(e.x + x, e.y + y, e.fout() * size, e.color, 0.7f);
			});
		}),
	
		hyperExplode = new Effect(30f, e -> {
			color(e.color, Color.white, e.fout() * 0.75f);
			stroke(1.3f * e.fslope());
			circle(e.x, e.y, 45f * e.fin());
			randLenVectors(e.id + 1, 5, 8f + 60 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 7f));
			Drawf.light(e.x, e.y, e.fout() * 70f, e.color, 0.7f);
		}),
	
		hyperInstall = new Effect(30f, e -> {
			color(e.color, Color.white, e.fout() * 0.55f);
			stroke(2.5f * e.fout());
			circle(e.x, e.y, 75f * e.fin());
			
			stroke(1.3f * e.fslope());
			circle(e.x, e.y, 45f * e.fin());
			
			for(int i = 0; i < 4; i++){
				DrawFunc.tri(e.x, e.y, e.rotation * (e.fout() + 1) / 3, e.rotation * 27f * Mathf.curve(e.fin(), 0, 0.12f) * e.fout(), i * 90);
			}
			Drawf.light(e.x, e.y, e.fout() * 80f, e.color, 0.7f);
		}),

		emped = new Effect(20f, e -> {
			color(Color.valueOf("#F7B080"), Color.valueOf("#915923"), e.fin());
			
			stroke(e.fout() * 2.4f);
			randLenVectors(e.id, 4, 7 + 50 * e.fin(), (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 5 + 1);
			});
			
			color(Color.gray, Color.darkGray, e.fin());
			randLenVectors(e.id, 3, 3 + 28 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f));
		}),
	
		hyperSpace = new Effect(600f, 300f, e -> {
			color(e.color);
			final float step = 0.2475f;
			float finX = Mathf.curve(e.fslope(), 0, step);
			float finY = Mathf.curve(e.fslope(), step + 0.05f, step * 2);
			
			Fill.rect(e.x, e.y, e.rotation * finX, (e.rotation - 1) * finY + 1);
		}),
	
		triTrail =  new Effect(28f, 50f, e -> {
			Rand rand = NHFunc.rand;
			rand.setSeed(e.id);
			Draw.color(e.color, Color.white, e.fout() * 0.6f);
			for(int i : Mathf.signs){
				float ang = e.rotation - 180 + rand.random(10, 45) * i;
				Tmp.v1.trns(ang, rand.random(4, 14) * ( 0.75f + e.fin() * 1.5f)).scl(0.3f + e.fin(Interp.pow3Out)).add(e.x, e.y);
				DrawFunc.arrow(Tmp.v1.x, Tmp.v1.y, rand.random(3, 6.5f) * e.fout(), rand.random(17, 28) * e.fout(Interp.pow3Out) * Mathf.curve(e.fin(), 0, 0.05f), rand.random(-4, -8) * e.fout(), ang);
			}
		}),
	
		ultFireBurn = new Effect(25f, e -> {
			color(NHColor.lightSkyBack, Color.gray, e.fin() * 0.75f);
			
			randLenVectors(e.id, 2, 2f + e.fin() * 7f, (x, y) -> {
				Fill.square(e.x + x, e.y + y, 0.2f + e.fout() * 1.5f, 45);
			});
		}).layer(Layer.bullet + 1),
	
		slidePoly = new Effect(60f, 600f, e -> {
			if(!(e.data instanceof Position))return;
			Position data = e.data();
			
			e.lifetime = data.dst(e.x, e.y) / 7f;
			
			Draw.color(e.color, Color.white, e.fout() * 0.6f);
			Lines.stroke(4f * e.fout());
			Lines.poly(Mathf.lerp(e.x, data.getX(), e.fin(Interp.pow2Out)), Mathf.lerp(e.y, data.getY(), e.fin(Interp.pow2Out)), 6, Math.max(10, e.rotation - 18) * e.fin() + 18);
		}),
	
		lightningFade = (new Effect(PosLightning.lifetime, 1200.0f, e -> {
			if(!(e.data instanceof Vec2Seq)) return;
			Vec2Seq points = e.data();
			
			e.lifetime = points.size() < 2 ? 0 : 1000;
			int strokeOffset = (int)e.rotation;
			
			
			if(points.size() > strokeOffset + 1 && strokeOffset > 0 && points.size() > 2){
				points.removeRange(0, points.size() - strokeOffset - 1);
			}
			
			if(!state.isPaused() && points.any()){
				points.remove(0);
			}
			
			if(points.size() < 2)return;
			
			Vec2 data = points.peekTmp(); //x -> stroke, y -> fadeOffset;
			float stroke = data.x;
			float fadeOffset = data.y;
			
			Draw.color(e.color);
			for(int i = 1; i < points.size() - 1; i++){
//				Draw.alpha(Mathf.clamp((float)(i + fadeOffset - e.time) / points.size()));
				Lines.stroke(Mathf.clamp((i + fadeOffset / 2f) / points.size() * (strokeOffset - (points.size() - i)) / strokeOffset) * stroke);
				Vec2 from = points.setVec2(i - 1, Tmp.v1);
				Vec2 to = points.setVec2(i, Tmp.v2);
				Lines.line(from.x, from.y, to.x, to.y, false);
				Fill.circle(from.x, from.y, Lines.getStroke() / 2);
			}
			
			Vec2 last = points.tmpVec2(points.size() - 2);
			Fill.circle(last.x, last.y, Lines.getStroke() / 2);
		})).layer(Layer.effect - 0.001f),
	
		crossBlastArrow45 = new Effect(65, 140, e -> {
			color(e.color, Color.white, e.fout() * 0.55f);
			Drawf.light(e.x, e.y, e.fout() * 70, e.color, 0.7f);
			
			e.scaled(10f, i -> {
				stroke(1.35f * i.fout());
				circle(e.x, e.y, 49 * i.finpow());
			});
			
			rand.setSeed(e.id);
			float sizeDiv = 138;
			float randL = rand.random(sizeDiv);
			
			float f = Mathf.curve(e.fin(), 0, 0.05f);
			
			for(int i = 0; i < 4; i++){
				Tmp.v1.trns(45 + i * 90, 66);
				DrawFunc.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 27.5f * (e.fout() * 3f + 1) / 4 * e.fout(Interp.pow3In), (sizeDiv + randL) * f * e.fout(Interp.pow3), -randL / 6f * f, i * 90 + 45);
			}
		}),
	
		crossBlast = new Effect(35, 140, e -> {
			color(e.color, Color.white, e.fout() * 0.55f);
			Drawf.light(e.x, e.y, e.fout() * 70, e.color, 0.7f);
			
			e.scaled(10f, i -> {
				stroke(1.35f * i.fout());
				circle(e.x, e.y, 49 * i.finpow());
			});
			
			rand.setSeed(e.id);
			float sizeDiv = 35;
			float randL = rand.random(sizeDiv);
			
			for(int i = 0; i < 4; i++){
				DrawFunc.tri(e.x, e.y, 3.5f * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90);
			}
		}),
	
		crossBlast_45 = new Effect(35, 140, e -> {
			color(e.color, Color.white, e.fout() * 0.55f);
			Drawf.light(e.x, e.y, e.fout() * 70, e.color, 0.7f);
			
			e.scaled(10f, i -> {
				stroke(1.35f * i.fout());
				circle(e.x, e.y, 55 * i.finpow());
			});
			
			rand.setSeed(e.id);
			float sizeDiv = 60;
			float randL = rand.random(sizeDiv);
			
			for(int i = 0; i < 4; i++){
				DrawFunc.tri(e.x, e.y, 5.85f * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + 45);
			}
		}),

	    triSpark1 = new Effect(26, e -> {
			rand.setSeed(e.id);
	    	Draw.color(Pal.techBlue, Color.white, e.fin());
	    	randLenVectors(e.id, 3, 3f + 24f * e.fin(), 5f, (x, y) -> {
	    		float randN = rand.random(120f);
	    		Fill.poly(e.x + x, e.y + y, 3, e.fout() * 8f * rand.random(0.8f, 1.2f), e.rotation + randN * e.fin());
	    	});
	    }),

		triSpark2 = new Effect(26, e -> {
			rand.setSeed(e.id);
			Draw.color(NHColor.ancient, Color.white, e.fin());
			randLenVectors(e.id, 3, 3f + 24f * e.fin(), 5f, (x, y) -> {
				float randN = rand.random(120f);
				Fill.poly(e.x + x, e.y + y, 3, e.fout() * 8f * rand.random(0.8f, 1.2f), e.rotation + randN * e.fin());
			});
		});

}














