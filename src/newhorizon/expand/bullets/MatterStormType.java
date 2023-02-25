/*
package newhorizon.expand.bullets;

import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Bullet;
import newhorizon.NHModCore;
import newhorizon.content.NHColor;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;

public class MatterStormType extends ContinuousBulletType{
	public float angleRange = 60f;
	public float length = 300f;
	public float alphaMax = 0.7f;
	public float alphaSwing = 0.2f;
	public float angSwing = 3f;
	public float angSwingScl = 1.7f;
	public Floatf<Bullet> aCurve = b -> NHInterp.laser.apply(b.fin()) * 0.3f + b.fout() * 0.8f;
	public Color srcColor = NHColor.ancient, tgtColor = Color.clear;
	
	protected static final Vec2 v1 = new Vec2(), v2 = new Vec2();
	protected static final Color c = new Color();
	
	@Override
	public void draw(Bullet b){
		super.draw(b);
		
		float src = c.set(srcColor).a(aCurve.get(b)).toFloatBits();
		float tgt = c.set(tgtColor).a(aCurve.get(b)).toFloatBits();
		
		float as = Mathf.absin(angSwingScl, angSwing);
		
		v1.trns(b.rotation() - angleRange * 0.5f - as, length);
		v2.trns(b.rotation() + angleRange * 0.5f + as, length);
		DrawFunc.quad(Draw.wrap(NHModCore.core.renderer.matterStorm),
			b.x, b.y, src,
			b.x, b.y, src,
			b.x + v1.x, b.y + v1.y, tgt,
			b.x + v2.x, b.y + v2.y, tgt);
	}
}
*/
