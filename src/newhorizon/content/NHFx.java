package newhorizon.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.*;

import mindustry.type.UnitType;

import static mindustry.Vars.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class NHFx{
	public static Effect lightningHitSmall(Color color){
		return new Effect(20, e -> {
			color(color, Color.white, e.fout() * 0.7f);
			randLenVectors(e.id, 5, 18 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 2));
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
			Angles.randLenVectors((long)e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
			});
		});
	}

	public static Effect laserEffect(float num){
		return new Effect(26.0F, (e) -> {
			Draw.color(Color.white);
			float length = !(e.data instanceof Float) ? 70.0F : (Float)e.data;
			Angles.randLenVectors((long)e.id, (int)(length / num), length, e.rotation, 0.0F, (x, y) -> {
				Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 9.0F);
			});
		});
	}

	public static Effect chargeEffectSmall(Color color){
		return new Effect(60.0F, 100.0F, (e) -> {
			Draw.color(color);
			randLenVectors(e.id, 6, 3 + 50 * e.fout(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.finpow() * 5f));
			Lines.stroke(e.fslope() * 2.0F);
			Lines.circle(e.x, e.y, e.fout() * 40f);
			randLenVectors(e.id + 1, 16, 3 + 70 * e.fout(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 18 + 5));
		});
	}

	public static final Effect
		//All effects
		
		boolSelector = new Effect(0, 0, e -> {}),
	
		skyTrail = new Effect(22, e -> {
			color(NHColor.lightSky, Pal.gray, e.fin());
			Fill.poly(e.x, e.y, 6, 4.7f * e.fout(), e.rotation);
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

		spawn = new Effect(100f, e -> {
			if(!(e.data() instanceof Building))return;
			Building starter = e.data();

			final TextureRegion pointerRegion = Core.atlas.find("new-horizon-jump-gate-pointer");

			Draw.color(e.color);

			for (int j = 1; j <= 3; j ++) {
				for(int i = 0; i < 4; i++) {
					float length = tilesize * starter.block().size * 1.5f + 4f;
					Tmp.v1.trns(i * 90, -length);
					e.scaled(30 * j, k -> {
						float signSize = (e.rotation / 3f + Draw.scl) * k.fout();
						Draw.rect(pointerRegion, e.x + Tmp.v1.x * k.finpow(), e.y + Tmp.v1.y * k.finpow(), pointerRegion.width * signSize, pointerRegion.height * signSize, Tmp.v1.angle() - 90);
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
			rect(type.region, unit.x, unit.y, unit.rotation - 90f);
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
		
		darkErnExplosion = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			e.scaled(6, i -> {
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
		
		emped = new Effect(20f, e -> {
			color(Color.valueOf("#F7B080"), Color.valueOf("#915923"), e.fin());
			randLenVectors(e.id, 4, 7 + 50 * e.fin(), (x, y) -> {
				stroke(e.fout() * 2.4f);
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 3);
			});
			
			color(Color.gray, Color.darkGray, e.fin());
			randLenVectors(e.id, 3, 5 + 30 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f));
		});
}














