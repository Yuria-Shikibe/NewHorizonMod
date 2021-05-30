package newhorizon.effects;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.pooling.Pools;
import newhorizon.content.NHFx;

public class EffectTrail{
	public static final float LIFETIME = 50f, DRAW_SIZE = 500f;
	public int length;
	public float width;
	public final Seq<Vec3> points;
	protected float lastX = -1, lastY = -1;
	
	public Color fromColor, toColor;

	public EffectTrail(int length){
		this(length, 1, Color.white, Color.white);
	}
	
	public EffectTrail() {
		this(1, 1, Color.white, Color.white);
	}

	public EffectTrail(int length, float width, Color fromColor, Color toColor) {
		this.length = length;
		this.width = width;
		this.fromColor = fromColor;
		this.toColor = toColor;
		points = new Seq<>(length);
	}
	
	public EffectTrail clear() {
		points.clear();
		return this;
	}

	public void draw(){
		draw(1.0f);
	}
	
	public void draw(float f) {
		if(points.isEmpty())return;
		Draw.color(fromColor);
		
		if(points.size >= 1){
			Vec3 c = points.peek();
			float sizeP = width * f / length;
			
			float
					cx = Mathf.sin(c.z) * points.size * sizeP,
					cy = Mathf.cos(c.z) * points.size * sizeP;

			Fill.circle(points.peek().x, points.peek().y, Mathf.dst(c.x - cx, c.y - cy, c.x, c.y));
		}
		
		
		Draw.reset();
		for (int i = 0; i < points.size - 1; i++) {
			Vec3 c = points.get(i);
			Vec3 n = points.get(i + 1);
			float sizeP = width * f / length;

			float
				cx = Mathf.sin(c.z) * i * sizeP,
				cy = Mathf.cos(c.z) * i * sizeP,
				nx = Mathf.sin(n.z) * (i + 1) * sizeP,
				ny = Mathf.cos(n.z) * (i + 1) * sizeP;
			
			Draw.color(fromColor, toColor, (float)(i / points.size));
			Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
		}

		Draw.reset();
	}
	
	public void update(float x, float y){
		if (points.size > length) {
			Pools.free(points.first());
			points.remove(0);
		}

		float angle = -Angles.angle(x, y, lastX, lastY);

		points.add(Pools.obtain(Vec3.class, Vec3::new).set(x, y, (angle) * Mathf.degRad));

		lastX = x;
		lastY = y;
	}
	
	public void disappear() {
		if (points.isEmpty())return;
		NHFx.disappearEffect.at(points.peek().x, points.peek().y, length, fromColor, new EffectTrailData(points, width, toColor));
	}
	
	public static class EffectTrailData{
		public final Seq<Vec3> points;
		public float width;
		public Color toColor;
		
		public EffectTrailData(Seq<Vec3> points, float width, Color toColor){
			this.points = points;
			this.width = width;
			this.toColor = toColor;
		}
	}
}
