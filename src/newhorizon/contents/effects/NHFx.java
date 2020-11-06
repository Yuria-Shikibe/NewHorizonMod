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
import mindustry.ctype.*;
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

public class NHFx implements ContentList {
	public static
	Effect 
	circleSplash, darkErnExplosion, upgrading, lightningHit,
	blastgenerate, blastAccept, emped, lightSkyCircleSplash;

	@Override
	public void load() {
		lightningHit = new Effect(25, e -> {
			color(NHColor.darkEnrColor);
			e.scaled(12, t -> {
				stroke(3f * t.fout());
				circle(e.x, e.y, 3f + t.fin() * 80f);
			});
			Fill.circle(e.x, e.y, e.fout() * 8f);
			randLenVectors(e.id + 1, 8, 1f + 60f * e.finpow(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
			});
		})
						
		upgrading = new Effect(30, e -> {
			color(e.color);
			float drawSize = e.rotation * tilesize * e.fout();
			rect(Core.atlas.find(NewHorizon.NHNAME + "upgrade"), e.x, e.y + e.rotation * tilesize * 1.35f * e.finpow(), drawSize, drawSize);
		});
		
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
		});
		
		lightSkyCircleSplash = new Effect(26f, e -> {
			color(NHColor.lightSky);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
			});
		});
		
		circleSplash = new Effect(26f, e -> {
			color(e.color);
			randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
			});
		});
		
		blastgenerate = new Effect(40f, 600, e -> {
			color(NHColor.darkEnrColor);
			stroke(e.fout() * 3.7f);
			circle(e.x, e.y, e.fin() * 300 + 15);
			stroke(e.fout() * 2.5f);
			circle(e.x, e.y, e.fin() * 200 + 15);
			randLenVectors(e.id, 10, 5 + 55 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
			});
		});
		
		blastAccept = new Effect(20f, e -> {
			color(NHColor.darkEnrColor);
			randLenVectors(e.id, 3, 5 + 30 * e.fin(), (x, y) -> {
				Fill.circle(e.x + x, e.y + y, e.fout() * 4f);
			});
		});
		
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
}














