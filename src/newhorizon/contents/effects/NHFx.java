package newhorizon.contents.effects;

import arc.*;
import arc.audio.*;
import arc.math.geom.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.struct.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.io.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

import newhorizon.contents.bullets.special.*;
import newhorizon.contents.colors.*;
import newhorizon.NewHorizon;


import static mindustry.Vars.*;

import static arc.graphics.g2d.Draw.rect;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class NHFx{
	public static final Effect
		//All effects
		darkEnergySpread = new Effect(32f, e -> {
			randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
				color(NHColor.darkEnrColor);
				Fill.circle(e.x + x, e.y + y, e.fout() * 15f);
				color(NHColor.darkEnr);
				Fill.circle(e.x + x, e.y + y, e.fout() * 9f);
			});
		}),
					
		largeDarkEnergyHitCircle = new Effect(20f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fout() * 44);
			randLenVectors(e.id, 5, 60f * e.fin(), (x,y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 8);
			});
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
			randLenVectors(e.id, 9, 7f + 60f * e.finpow(), (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 12f);
			});
			
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
		}),
		
		polyTrail = new Effect(25f, e -> {
			color(e.color, Pal.gray, e.fin());
			randLenVectors(e.id, 4, 46f * e.fin(), (x, y) -> {
				Fill.poly(e.x + x, e.y + y, 6, 5.5f * e.fslope() * e.fout());
			});
		}),
		
		darkEnergyLaserShoot = new Effect(26f, 880, e -> {
			color(Color.white, NHColor.darkEnrColor, e.fin() * 0.75f);
			float length = !(e.data instanceof Float) ? 70f : (Float)e.data;
			randLenVectors(e.id, 9, length, e.rotation, 0f, (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * (length / 14));
			});
		}),
		
		darkEnergySmoke = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 4, 60 * e.fin(), e.rotation, 30, (x,y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4);
			});
		}),
		
		darkEnergyShoot = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			for (int i : Mathf.signs){
				Drawf.tri(e.x, e.y, 2 + 2 * e.fout(), 28 * e.fout(), e.rotation + 90 * i);
			}
		}),
		
		darkEnergyCharge = new Effect(60f, e -> {
			randLenVectors(e.id, 3, 60 * Mathf.curve(e.fout(), 0.25f, 1f), (x, y) -> {
				color(NHColor.darkEnrColor);
				Fill.circle(e.x + x, e.y + y, e.fin() * 13f);
				color(NHColor.darkEnr);
				Fill.circle(e.x + x, e.y + y, e.fin() * 7f);
			});
		}),
		
		darkEnergyChargeBegin = new Effect(60f, e -> {
			color(NHColor.darkEnrColor);
			Fill.circle(e.x, e.y, e.fin() * 32);
			stroke(e.fin() * 3.7f);
			circle(e.x, e.y, e.fout() * 80);
			color(NHColor.darkEnr);
			Fill.circle(e.x, e.y, e.fin() * 20);
		}),
		
		lightningHit = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			e.scaled(12, t -> {
				stroke(3f * t.fout());
				circle(e.x, e.y, 3f + t.fin() * 80f);
			});
			Fill.circle(e.x, e.y, e.fout() * 8f);
			randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
			});
		}),
						
		upgrading = new Effect(30, e -> {
			color(e.color);
			float drawSize = e.rotation * tilesize * e.fout();
			rect(Core.atlas.find(NewHorizon.NHNAME + "upgrade"), e.x, e.y + e.rotation * tilesize * 1.35f * e.finpow(), drawSize, drawSize);
		}),
		
		darkErnExplosion = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			e.scaled(6, i -> {
				stroke(3f * i.fout());
				circle(e.x, e.y, 3f + i.fin() * 80f);
			});
			
			stroke(1f * e.fout());
			randLenVectors(e.id + 1, 8, 1f + 60f * e.finpow(), (x, y) -> {
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 3f);
			});
			
			color(Color.gray);

			randLenVectors(e.id, 5, 2f + 70 * e.finpow(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.5f);
			});
		}),
		
		lightSkyCircleSplash = new Effect(26f, e -> {
			color(NHColor.lightSky);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
			});
		}),
		
		darkEnrCircleSplash = new Effect(26f, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4.5f);
			});
		}),
		
		circleSplash = new Effect(26f, e -> {
			color(e.color);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
			});
		}),
		
		blastgenerate = new Effect(40f, 600, e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 3.7f);
			circle(e.x, e.y, e.fin() * 300 + 15);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 200 + 15);
			randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
			});
		}),
		
		blastAccept = new Effect(20f, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 3, 5 + 30 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4f);
			});
		}),
		
		emped = new Effect(20f, e -> {
			color(Color.valueOf("#F7B080"), Color.valueOf("#915923"), e.fin());
			randLenVectors(e.id, 4, 7 + 50 * e.fin(), (x, y) -> {
				stroke(e.fout() * 2.4f);
				lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 3);
			});
			
			color(Color.gray, Color.darkGray, e.fin());
			randLenVectors(e.id, 3, 5 + 30 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4f);
			});
		});
}














