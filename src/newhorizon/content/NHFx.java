package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import newhorizon.feature.PosLightning;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;

public class NHFx{
	public static Effect genericCharge(Color color, float size, float range, float lifetime){
		return new Effect(lifetime, e -> {
			color(color);
			
			randLenVectors(e.id, 2, 1f + 20f * e.fout(), e.rotation, range, (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * size + size / 4f);
			});
		});
	}
	
	public static Effect genericChargeBegin(Color color, float size, float lifetime){
		return new Effect(lifetime, e -> {
			color(color);
			Fill.circle(e.x, e.y, e.fin() * size);
			
			color();
			Fill.circle(e.x, e.y, e.fin() * size / 2f);
		});
	}
	
	public static Effect lightningHitSmall(Color color){
		return new Effect(20, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 5, 18 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 2));
		});
	}
	
	public static Effect shootCircleSmall(Color color){
		return new Effect(30, e -> {
			color(color, Color.white, e.fout() * 0.75f);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3.22f));
		});
	}
	
	public static Effect shootLineSmall(Color color){
		return new Effect(20, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 5, 18 * e.fin(), e.rotation, 34f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 2));
		});
	}
	
	public static Effect laserHit(Color color){
		return new Effect(20, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 9, 18 * e.fin(), e.rotation, 40f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 2));
		});
	}
	
	public static Effect lightningHitLarge(Color color){
		return new Effect(25, e -> {
			color(color);
			e.scaled(12, t -> {
				stroke(3f * t.fout());
				circle(e.x, e.y, 3f + t.fin() * 80f);
			});
			Fill.circle(e.x, e.y, e.fout() * 8f);
			randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));
			
			color(Color.gray);
			Angles.randLenVectors(e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
		});
	}

	public static Effect laserEffect(float num){
		return new Effect(26.0F, (e) -> {
			Draw.color(Color.white);
			float length = !(e.data instanceof Float) ? 70.0F : (Float)e.data;
			Angles.randLenVectors(e.id, (int)(length / num), length, e.rotation, 0.0F, (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 9.0F));
		});
	}

	public static Effect chargeEffectSmall(Color color, float lifetime){
		return new Effect(lifetime, 100.0F, (e) -> {
			Draw.color(color);
			randLenVectors(e.id, 7, 3 + 50 * e.fout(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.finpow() * 3f));
			Lines.stroke(e.fin() * 1.75f);
			Lines.circle(e.x, e.y, e.fout() * 40f);
			randLenVectors(e.id + 1, 16, 3 + 70 * e.fout(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 7 + 3));
		});
	}
	
	public static Effect chargeBeginEffect(Color color, float size, float lifetime){
		return new Effect(lifetime, (e) -> {
			Draw.color(color);
			Fill.circle(e.x, e.y, size * e.fin());
		});
	}
	
	public static Effect crossBlast(Color color){
		return crossBlast(color, 117);
	}
	
	public static Effect crossBlast(Color color, float size){
		return new Effect(36f, size * 2, e -> {
			color(color, Color.white, e.fout() * 0.55f);
			stroke(2f * e.fout());
			e.scaled(16f, i -> circle(e.x, e.y, 45f * i.fin()));
			
			for(int i = 0; i < 4; i++){
				Drawf.tri(e.x, e.y, size / 16 * (e.fout() + 1) / 2, size * Mathf.curve(e.fin(), 0, 0.12f) * e.fout(), i * 90);
			}
		});
	}
	
	public static Effect hyperBlast(Color color){
		return new Effect(30f, e -> {
			color(NHItems.thermoCorePositive.color, Color.white, e.fout() * 0.75f);
			stroke(1.3f * e.fslope());
			circle(e.x, e.y, 45f * e.fin());
			randLenVectors(e.id + 1, 5, 8f + 50 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 7f));
		});
	}
	
	public static Effect instShoot(Color color){
		return new Effect(24.0F, (e) -> {
			e.scaled(10.0F, (b) -> {
				Draw.color(Color.white, color, b.fin());
				Lines.stroke(b.fout() * 3.0F + 0.2F);
				Lines.circle(b.x, b.y, b.fin() * 50.0F);
			});
			Draw.color(color);
			
			for(int i : Mathf.signs){
				Drawf.tri(e.x, e.y, 13.0F * e.fout(), 85.0F, e.rotation + 90.0F * i);
				Drawf.tri(e.x, e.y, 13.0F * e.fout(), 50.0F, e.rotation + 20.0F * i);
			}
			
		});
	}
	
	public static Effect instBomb(Color color){
		return instBombSize(color, 4, 80f);
	}
	
	public static Effect instBombSize(Color color, int num, float size){
		return new Effect(15.0F, size * 1.5f, (e) -> {
			Draw.color(color);
			Lines.stroke(e.fout() * 4.0F);
			Lines.circle(e.x, e.y, 4.0F + e.finpow() * size / 4f);
			
			int i;
			for(i = 0; i < num; ++i) {
				Drawf.tri(e.x, e.y, size / 12f, size * e.fout(), (float)(i * 90 + 45));
			}
			
			Draw.color();
			
			for(i = 0; i < num; ++i) {
				Drawf.tri(e.x, e.y, size / 26f, size / 2.5f * e.fout(), (float)(i * 90 + 45));
			}
		});
	}
	
	public static Effect instHit(Color color){return instHit(color, 5, 50); }
	
	public static Effect instHit(Color color, int num, float size){
		return new Effect(20.0F, size * 1.5f, (e) -> {
			for(int i = 0; i < 2; ++i) {
				Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.25f));
				float m = i == 0 ? 1.0F : 0.5F;
				
				for(int j = 0; j < num; ++j) {
					float rot = e.rotation + Mathf.randomSeedRange((e.id + j), size);
					float w = 23.0F * e.fout() * m;
					Drawf.tri(e.x, e.y, w, (size + Mathf.randomSeedRange((e.id + j), size * 0.8f)) * m, rot);
					Drawf.tri(e.x, e.y, w, size * 0.4f * m, rot + 180.0F);
				}
			}
			
			e.scaled(10.0F, (c) -> {
				Draw.color(color.cpy().lerp(Color.white, 0.25f));
				Lines.stroke(c.fout() * 2.0F + 0.2F);
				Lines.circle(e.x, e.y, c.fin() * size * 0.7f);
			});
			
			e.scaled(12.0F, (c) -> {
				Draw.color(color);
				Angles.randLenVectors(e.id, 25, 5.0F + e.fin() * size * 1.25f, e.rotation, 60.0F, (x, y) -> {
					Fill.square(e.x + x, e.y + y, c.fout() * 3.0F, 45.0F);
				});
			});
		});
	}
	
	public static Effect instTrail(Color color){
		return new Effect(30.0F, (e) -> {
			for(int i = 0; i < 2; ++i) {
				Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.15f));
				float m = i == 0 ? 1.0F : 0.5F;
				float rot = e.rotation + 180.0F;
				float w = 15.0F * e.fout() * m;
				Drawf.tri(e.x, e.y, w, (30.0F + Mathf.randomSeedRange(e.id, 15.0F)) * m, rot);
				Drawf.tri(e.x, e.y, w, 10.0F * m, rot + 180.0F);
			}
		});
	}
	
	public static Effect polyCloud(Color color, float lifetime, float size, float range, int num){
		return (new Effect(lifetime, (e) -> {
			randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
				Draw.color(color, Pal.gray, e.fin());
				Fill.poly(e.x + x, e.y + y, 6, size * e.fout(), e.rotation);
				Drawf.light(e.x + x, e.y + y, size * e.fout() * 2.5f, color, e.fout() * 0.65f);
				Draw.color(Color.white, Pal.gray, e.fin());
				Fill.poly(e.x + x, e.y + y, 6, size * e.fout() / 2, e.rotation);
			});
		})).layer(Layer.bullet);
	}
	
	
	public static final Effect
		poly = new Effect(25f, e -> {
			Draw.color(e.color);
			Lines.stroke(e.fout() * 2.0F);
			Lines.poly(e.x, e.y, 6, 2.0F + e.finpow() * e.rotation);
		}),
		healEffect = new Effect(11.0F, (e) -> {
			Draw.color(NHColor.lightSky);
			Lines.stroke(e.fout() * 2.0F);
			Lines.poly(e.x, e.y, 6, 2.0F + e.finpow() * 79.0F);
		}),
	
		activeEffect = new Effect(22.0F, (e) -> {
			Draw.color(NHColor.lightSky);
			Lines.stroke(e.fout() * 3.0F);
			Lines.poly(e.x, e.y, 6,4.0F + e.finpow() * e.rotation);
		}),
	
		spawnGround = new Effect(60f, e -> {
			Draw.color(e.color, Pal.gray, e.fin());
			randLenVectors(e.id, (int)(e.rotation * 1.35f), e.rotation * tilesize / 1.125f * e.fin(), (x, y) -> Fill.square(e.x + x, e.y + y, e.rotation * e.fout(), 45));
		}),
		
		spawnWave = new Effect(60f, e -> {
			stroke(3 * e.fout(), e.color);
			circle(e.x, e.y, e.rotation  / tilesize * e.finpow());
		}),
		//All effects
		trail = new Effect(50.0F, (e) -> {
			Draw.color(e.color, Color.gray, e.fin());
			randLenVectors(e.id, 2, tilesize * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.rotation * e.fout()));
		}),
	
		boolSelector = new Effect(0, 0, e -> {}),
	
		skyTrail = new Effect(22, e -> {
			color(NHColor.lightSky, Pal.gray, e.fin());
			Fill.poly(e.x, e.y, 6, 4.7f * e.fout(), e.rotation);
		}),
		
		posLightning = new Effect(PosLightning.lifetime, 800.0f, e -> {
			if(!(e.data instanceof Seq)) return;
			Seq<Vec2> lines = e.data();
			
			color(e.color, Color.white, e.fin());
			
			Lines.stroke(e.rotation * e.fout());
			
			Fill.circle(lines.first().x, lines.first().y, Lines.getStroke() * 1.1f);
			
			for(int i = 0; i < lines.size - 1; i++){
				Vec2 cur = lines.get(i);
				Vec2 next = lines.get(i + 1);
				
				Lines.line(cur.x, cur.y, next.x, next.y, false);
			}
			
			for(Vec2 p : lines){
				Fill.circle(p.x, p.y, Lines.getStroke() / 2f);
			}
		}),
	
		shuttle = new Effect(60f, 200f, e -> {
			if(!(e.data instanceof Float))return;
			float len = e.data();
			color(e.color);
			for(int i : Mathf.signs) {
				Drawf.tri(e.x, e.y, len * e.fout() * e.fslope() * 4f, len * 50f * e.fout(), e.rotation + 90 + i * 90);
			}
			Lines.stroke(e.fout() * 2.0F);
			Lines.circle(e.x, e.y, e.fin() * len * 8f);
			randLenVectors(e.id, 6, 3 + 60 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 18 + 5));
		}),
		
		line = new Effect(30f, e -> {
			color(e.color, Color.white, e.fout() * 0.75f);
			stroke(2 * e.fout());
			randLenVectors(e.id, 6, 3 + e.rotation * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 4));
		}),
		
		circle = new Effect(25f, e -> {
			color(e.color, Color.white, e.fout() * 0.75f);
			stroke(2 * e.fout());
			circle(e.x, e.y, e.rotation * e.fin());
		}),
	
		 unitLandSize = (new Effect(30.0F, (e) -> {
			 Draw.color(Pal.lightishGray);
			 Angles.randLenVectors((long)e.id, 9, 3.0F + 20.0F * e.finpow(), (x, y) -> {
				 Fill.circle(e.x + x, e.y + y, e.fout() * e.rotation + 0.4F);
			 });
		})).layer(20.0F),
	
		spawn = new Effect(100f, e -> {
			if(!(e.data() instanceof Building))return;
			Building starter = e.data();

			final TextureRegion pointerRegion = Core.atlas.find("new-horizon-jump-gate-pointer");

			Draw.color(e.color);

			for (int j = 1; j <= 3; j ++) {
				for(int i = 0; i < 4; i++) {
					float length = tilesize * starter.block().size * 1.5f + 4f;
					float x = Angles.trnsx(i * 90, -length), y = Angles.trnsy(i * 90, -length);
					e.scaled(30 * j, k -> {
						float signSize = (e.rotation / 3f + Draw.scl) * k.fout();
						Draw.rect(pointerRegion, e.x + x * k.finpow(), e.y + y * k.finpow(), pointerRegion.width * signSize, pointerRegion.height * signSize, Angles.angle(x, y) - 90);
					});
				}
			}
		}),

		jumpTrail = new Effect(70f, 5000, e -> {
			if (!(e.data instanceof Unit))return;
			Unit unit = e.data();
			UnitType type = unit.type;
			color(e.color);

			e.scaled(38, i -> Drawf.tri(e.x, e.y, type.hitSize / 2.5f * i.fout(), 2500, e.rotation - 180));

			randLenVectors(e.id, 15, 800, e.rotation - 180, 0f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 60));
			mixcol(e.color, e.fout());
			rect(type.shadowRegion, unit.x, unit.y, unit.rotation - 90f);
		}),

		darkEnergySpread = new Effect(32f, e -> randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x + x, e.y + y, e.fout() * 15f);
			color(NHColor.darkEnr);
			Fill.circle(e.x + x, e.y + y, e.fout() * 9f);
		})),
					
		largeDarkEnergyHitCircle = new Effect(20f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 44);
			randLenVectors(e.id, 5, 60f * e.fin(), (x,y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 8));
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 30);
		}),
		
		largeDarkEnergyHit = new Effect(50, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 44);
			stroke(e.fout() * 3.2f);
			circle(e.x, e.y, e.fin() * 80);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 50);
			randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
				stroke(e.fout() * 3.2f);
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
			});
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 30);
		}),
				
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
		}),
		
		darkEnergySmokeBig = new Effect(30f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 32);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fout() * 20);
		}),
		
		darkEnergyShootBig = new Effect(40f, 100, e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 3.7f);
			circle(e.x, e.y, e.fin() * 100 + 15);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 60 + 15);
			randLenVectors(e.id, 15, 7f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 16f));
		}),
		
		polyTrail = new Effect(25f, e -> {
			color(e.color, Pal.gray, e.fin());
			randLenVectors(e.id, 4, 46f * e.fin(), (x, y) -> Fill.poly(e.x + x, e.y + y, 6, 5.5f * e.fslope() * e.fout()));
		}),
		
		darkEnergyLaserShoot = new Effect(26f, 880, e -> {
			color(Color.white, NHColor.darkEnrColor, e.fin() * 0.75f);
			float length = !(e.data instanceof Float) ? 70f : (Float)e.data;
			randLenVectors(e.id, 9, length, e.rotation, 0f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * (length / 14)));
		}),
		
		darkEnergySmoke = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 4, 60 * e.fin(), e.rotation, 30, (x,y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4));
		}),
		
		darkEnergyShoot = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			for (int i : Mathf.signs){
				Drawf.tri(e.x, e.y, 2 + 2 * e.fout(), 28 * e.fout(), e.rotation + 90 * i);
			}
		}),
		
		darkEnergyCharge = new Effect(60f, e -> randLenVectors(e.id, 3, 60 * Mathf.curve(e.fout(), 0.25f, 1f), (x, y) -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x + x, e.y + y, e.fin() * 13f);
			color(NHColor.darkEnr);
			Fill.circle(e.x + x, e.y + y, e.fin() * 7f);
		})),
		
		hugeSmoke = new Effect(40f, e -> {
			Draw.color(Color.gray);
			Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2f));
			e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F)));
		}),
	
		darkEnergyChargeBegin = new Effect(60f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fin() * 32);
			stroke(e.fin() * 3.7f);
			circle(e.x, e.y, e.fout() * 80);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fin() * 20);
		}),
						
		upgrading = new Effect(30, e -> {
			color(e.color);
			float drawSize = e.rotation * tilesize * e.fout();
			rect(Core.atlas.find("new-horizon-upgrade"), e.x, e.y + e.rotation * tilesize * 1.35f * e.finpow(), drawSize, drawSize);
		}),
		
		darkErnExplosion = new Effect(40, e -> {
			color(NHColor.darkEnrColor);
			e.scaled(20, i -> {
				stroke(3f * i.fout());
				circle(e.x, e.y, 3f + i.fin() * 80f);
			});

			stroke(e.fout());
			randLenVectors(e.id + 1, 8, 1f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 3f));

			color(Color.gray);

			randLenVectors(e.id, 5, 2f + 70 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.5f));
		}),
		
		lightSkyCircleSplash = new Effect(26f, e -> {
			color(NHColor.lightSky);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
		}),
		
		darkEnrCircleSplash = new Effect(26f, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.5f));
		}),
		
		circleSplash = new Effect(26f, e -> {
			color(e.color);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
		}),
		
		blastgenerate = new Effect(40f, 600, e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 3.7f);
			circle(e.x, e.y, e.fin() * 300 + 15);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 200 + 15);
			randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));
		}),
		
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
		}),
	
		hyperCloud = new Effect(140.0F, 400.0F, (e) -> {
			randLenVectors(e.id, 20, e.finpow() * 160.0F, (x, y) -> {
				float size = e.fout() * 15.0F;
				Draw.color(e.color, Color.lightGray, e.fin());
				Fill.circle(e.x + x, e.y + y, size / 2.0F);
			});
		}),
	
		hyperExplode = new Effect(30f, e -> {
			color(e.color, Color.white, e.fout() * 0.75f);
			stroke(1.3f * e.fslope());
			circle(e.x, e.y, 45f * e.fin());
			randLenVectors(e.id + 1, 5, 8f + 60 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 7f));
		}),
	
		hyperInstall = new Effect(30f, e -> {
			color(e.color, Color.white, e.fout() * 0.55f);
			stroke(2.5f * e.fout());
			circle(e.x, e.y, 75f * e.fin());
			
			stroke(1.3f * e.fslope());
			circle(e.x, e.y, 45f * e.fin());
			
			for(int i = 0; i < 4; i++){
				Drawf.tri(e.x, e.y, e.rotation * (e.fout() + 1) / 2, e.rotation * 27f * Mathf.curve(e.fin(), 0, 0.12f) * e.fout(), i * 90);
			}
		}),

		emped = new Effect(20f, e -> {
			color(Color.valueOf("#F7B080"), Color.valueOf("#915923"), e.fin());
			
			stroke(e.fout() * 2.4f);
			randLenVectors(e.id, 4, 7 + 50 * e.fin(), (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 5 + 1);
			});
			
			color(Color.gray, Color.darkGray, e.fin());
			randLenVectors(e.id, 3, 3 + 28 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f));
		});
}














