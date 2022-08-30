package newhorizon.expand.block.drawer;

import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import newhorizon.content.NHContent;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;

public class ArcCharge extends DrawPart{
	protected static final Rand rand = new Rand();
	public float size = 13.75f;
	public Color color;
	public PartProgress progress;
	
	public int particles = 25;
	public float particleLife = 40f, particleRad = 7f, particleStroke = 1.1f, particleLen = 3f;
	
	public float chargeCircleFrontRad = 18;
	public float chargeCircleBackRad = 8;
	
	public Interp curve = Interp.pow3;
	public float lightningCircleInScl = 0.85f, lightningCircleOutScl = 1.1f;
	public Interp lightningCircleCurve = Interp.pow3Out;
	public Floatf<PartParams> chargeY = t -> 1;
	public Floatf<PartParams> shootY = t -> 1;
	
	protected final static Vec2 tr = new Vec2(), tr2 = new Vec2();
	
	@Override
	public void draw(PartParams params){
		Draw.z(Layer.effect - 1f);
		
		Draw.color(color);
		float x = params.x, y = params.y, rotation = params.rotation;
		
		float fin = progress.getClamp(params);
		
		Lines.stroke(3f * Mathf.curve(fin, 0.1f, 0.2f));
		tr2.trns(rotation, chargeY.get(params));
		tr.trns(rotation, shootY.get(params));
		Tmp.v2.set(tr).sub(tr2);
		float length = Tmp.v2.len();
		Tmp.v2.set(tr).add(tr2);
		
		DrawFunc.circlePercent(x + Tmp.v2.x / 2, y + Tmp.v2.y / 2, length / 2f, Mathf.curve(fin, 0.1f, 1f), rotation - Mathf.curve(fin, 0.1f, 1f) * 180f - 180f);
		
		float scl = size * tilesize * lightningCircleCurve.apply(fin);
		float fin_9 = Mathf.curve(fin, 0.95f, 1f);
		float sclSign = size * tilesize * lightningCircleCurve.apply(fin_9);
		Lines.stroke(fin * lightningCircleInScl * 4.5f);
		Lines.circle(x, y, scl * lightningCircleInScl);
		for(int i = 0; i < 4; i++){
			float rot = Time.time + i * 90;
			Tmp.v1.trns(rot, sclSign * lightningCircleInScl + Lines.getStroke() * 2f).add(x, y);
			Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, NHContent.arrowRegion.width * Draw.scl * fin_9, NHContent.arrowRegion.height * Draw.scl * fin_9, rot + 90);
		}
		
		Lines.stroke(fin * lightningCircleOutScl * 4.5f);
		Lines.circle(x, y, scl * lightningCircleOutScl);
		for(int i = 0; i < 4; i++){
			float rot = -Time.time * 1.5f + i * 90;
			Tmp.v1.trns(rot, sclSign * lightningCircleOutScl + Lines.getStroke() * 3f).add(x, y);
			Draw.rect(NHContent.pointerRegion, Tmp.v1.x, Tmp.v1.y, NHContent.pointerRegion.width * Draw.scl * fin_9, NHContent.pointerRegion.height * Draw.scl * fin_9, rot + 90);
		}
		
		fin = Mathf.curve(fin, 0.25f, 1f);
		
		if(fin < 0.01f) return;
		Fill.circle(x + tr2.x, y + tr2.y, fin * chargeCircleBackRad);
		Lines.stroke(fin * 3f - 1f);
		DrawFunc.circlePercentFlip(x + tr2.x, y + tr2.y, fin * (chargeCircleBackRad + 5), Time.time, 20f);
		Draw.color(Color.white);
		Fill.circle(x + tr2.x, y + tr2.y, fin * chargeCircleBackRad * 0.7f);
		
		float cameraFin = (1 + 2 * DrawFunc.cameraDstScl(x + tr.x, y + tr.y, Vars.mobile ? 200 : 320)) / 3f;
		float triWidth = fin * chargeCircleFrontRad / 3.5f * cameraFin;
		
		Draw.color(color);
		for(int i : Mathf.signs){
			Fill.tri(x + tr.x, y + tr.y + triWidth, x + tr.x, y + tr.y - triWidth, x + tr.x + i * cameraFin * chargeCircleFrontRad * (23 + Mathf.absin(10f, 0.75f)) * (fin * 1.25f + 1f), y + tr.y);
			Drawf.tri(x + tr.x, y + tr.y, (fin + 1) / 2 * chargeCircleFrontRad / 1.5f, chargeCircleFrontRad * 10 * fin, i * 90 + Time.time * 1.25f);
			Drawf.tri(x + tr.x, y + tr.y, (fin + 1) / 2 * chargeCircleFrontRad / 2f, chargeCircleFrontRad * 6.5f * fin, i * 90 - Time.time);
		}
		
		Fill.circle(x + tr.x, y + tr.y, fin * chargeCircleFrontRad);
		DrawFunc.circlePercentFlip(x + tr.x, y + tr.y, fin * (chargeCircleFrontRad + 5), Time.time, 20f);
		Draw.color(Color.white);
		Fill.circle(x + tr.x, y + tr.y, fin * chargeCircleFrontRad * 0.7f);
	}
	
	@Override
	public void load(String name){
		
	}
}
