package newhorizon.expand.units.ablility;

import arc.Core;
import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

public class HealFieldAbility extends Ability{
	public Color scanColor = Pal.heal;
	public float reloadTime = 60f, spawnX, spawnY;
	public static final float scanTime = 120f;
	public float range = 800f;
	public static final Interp scanInterp = Interp.pow5;
	
	public float healMount = 0.15f;
	
	protected float reload;
	protected float curStroke;
	
	public float layer = Layer.bullet - 0.001f, blinkScl = 20f;
	public float effectRadius = 6f, sectorRad = 0.08f, rotateSpeed = 0.5f;
	public int sectors = 5;
	
	public HealFieldAbility(Color scanColor, float reloadTime, float spawnX, float spawnY, float range, float healMount){
		this.scanColor = scanColor;
		this.reloadTime = reloadTime;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		this.range = range;
		this.healMount = healMount;
	}
	
	public HealFieldAbility(Color scanColor, float spawnTime, float range){
		this.scanColor = scanColor;
		this.reloadTime = spawnTime;
		this.range = range;
	}
	
	public HealFieldAbility(Color scanColor, float spawnTime, float spawnX, float spawnY, float range){
		this.scanColor = scanColor;
		this.reloadTime = spawnTime;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		this.range = range;
	}
	
	protected static Effect scan = new Effect(scanTime, 2000, e -> {
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		Draw.color(e.color);
		
		float f = Interp.pow4Out.apply(Mathf.curve(e.fin(), 0, 0.3f));
		float stroke = Mathf.clamp(e.rotation / 80, 3, 8);
		
		Lines.stroke(2 * e.fout());
		Lines.circle(e.x, e.y, e.rotation * f / 8);
		
		Lines.stroke(stroke * e.fout() + 1 * e.fout(Interp.pow5In));
		Lines.circle(e.x, e.y, e.rotation * f);
		
		
		Lines.stroke(stroke * Mathf.curve(e.fin(), 0, 0.1f) * Mathf.curve(e.fout(), 0.05f, 0.15f));
		float angle = 360 * e.fin(scanInterp);
		Lines.lineAngle(e.x, e.y, angle, e.rotation * f - Lines.getStroke() / 2);
		Lines.stroke(stroke * Mathf.curve(e.fin(), 0, 0.1f) * e.fout(0.05f));
		
		Draw.z(Layer.bullet - 1);
		DrawFunc.fillCirclePercentFade(e.x, e.y, e.x, e.y, e.rotation * f, e.fin(scanInterp), 0, Mathf.curve(e.fout(), 0.2f, 0.25f) / 1.5f, 0.6f + 0.35f * Interp.pow2InInverse.apply(Mathf.curve(e.fin(), 0, 0.8f)), 1f);
		Draw.z(Layer.effect);
		
		Angles.randLenVectors(e.id, (int)(e.rotation / 40), e.rotation * 0.85f * f, angle, 0, (x, y) -> {
			Lines.lineAngle(e.x + x, e.y + y, angle, e.rotation * rand.random(0.05f, 0.15f) * e.fout(0.15f));
		});
		
		Draw.color(e.color);
		Lines.stroke(stroke * 1.25f * e.fout(0.2f));
		Fill.circle(e.x, e.y, Lines.getStroke());
		Draw.color(Color.white, e.color, e.fin());
		Fill.circle(e.x, e.y, Lines.getStroke() / 2);
		
		Drawf.light(e.x, e.y, e.rotation * f * 1.35f * e.fout(0.15f), e.color, 0.6f);
	});
	
	protected static Effect targeted = new Effect(45f, e -> {
		Draw.color(e.color);
		Lines.stroke(4 * Mathf.curve(e.fin(), 0, 0.1f) * e.fout());
		Lines.square(e.x, e.y, e.rotation * e.fout(), 315 * e.fout(Interp.pow3In));
	}).followParent(true);
	
	public void update(Unit unit){
		
		
		reload += Time.delta;
		
		if(reload / reloadTime > 0.15f)curStroke = Mathf.lerp(curStroke, 1, 0.0075f);
		else curStroke = Mathf.lerp(curStroke, 0, 0.01f);
		
		if(reload >= reloadTime){
			reload = reloadTime;
			
			NHFunc.extinguish(unit, range, Float.MAX_VALUE / 2);
			
			//noinspection SuspiciousNameCombination
			Tmp.v1.trns(unit.rotation, spawnY, spawnX).add(unit);
			Seq<Building> toHeal = new Seq<>();
			Vars.indexer.eachBlock(unit.team, Tmp.v1.x, Tmp.v1.y, range, b -> b.damaged() && b.within(Tmp.v1, range), toHeal::add);
			if(toHeal.isEmpty())return;
			
			reload = 0;
			
			scan.at(Tmp.v1.x, Tmp.v1.y, range, scanColor);
			Seq<Building> bs = toHeal.sort((Floatf<Building>)Tmp.v1::angleTo).copy();
			
			Time.run(scanTime + 10, () -> {
				for(int i = 0; i < bs.size; i++){
					Building b = bs.get(i);
					
					Time.run(i * 90f / bs.size, () -> {
						if(!b.isValid())return;
						Fx.healBlockFull.at(b.x, b.y, b.block.size, scanColor, b.block());
						b.healFract(healMount);
					});
				}
			});
		}
	}
	
	@Override
	public void draw(Unit unit){
		//noinspection SuspiciousNameCombination
		Tmp.v1.trns(unit.rotation, spawnY, spawnX).add(unit);
		Draw.z(Layer.effect);
		
		Draw.color(scanColor);
		float rx = Tmp.v1.x, ry = Tmp.v1.y;
		float orbRadius = effectRadius * reload / reloadTime;
		
		Fill.circle(rx, ry, orbRadius * 0.9f);
		Draw.color();
		Fill.circle(rx, ry, orbRadius / 2f * 0.9f);
		
		Lines.stroke(Mathf.clamp(orbRadius / 4, 2, 4) * (Mathf.absin(blinkScl, 0.15f) + 1), scanColor);
		
		Lines.square(rx, ry, orbRadius * 1.8f, Time.time * 1.5f);
		Lines.square(rx, ry, orbRadius * 2.4f, -Time.time);
		Lines.spikes(rx, ry, orbRadius * 2.6f, orbRadius, 4, Time.time * 1.25f);
		Lines.stroke(Mathf.clamp(range / 80, 3, 8) * curStroke * reload / reloadTime / 2);
		
		if(curStroke > 0){
			for(int i = 0; i < sectors; i++){
				float rot = unit.rotation + i * 360f / sectors + Time.time * rotateSpeed;
				Lines.arc(rx, ry, range * reload / reloadTime, sectorRad, rot);
			}
		}
		
		Drawf.light(rx, ry, range * 1.5f, scanColor, curStroke * 0.8f);
		
		Draw.reset();
	}
	
	public String localized(){
		return Core.bundle.get("ability.healfield");
	}
}
