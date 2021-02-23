package newhorizon.effects;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.pooling.Pools;
import mindustry.entities.Effect;

public class EffectTrail {
	public static final float LIFETIME = 20f;
	public int length;
	public float width;
	protected final Seq<Vec3> points;
	protected float lastX = -1, lastY = -1;
	
	private final Effect disappearEffect = new Effect(LIFETIME, 500, e -> {
		if (!(e.data instanceof Seq))return;
		Seq<Vec3> data = e.data();
		
		Draw.color(e.color);
		Fill.circle(e.x, e.y, width * 1.1f * e.fout());
		for (int i = 0; i < data.size - 1; i++) {
			Vec3 c = data.get(i);
			Vec3 n = data.get(i + 1);
			float sizeP = width * e.fout() / e.rotation;
			
			float cx = Mathf.sin(c.z) * i * sizeP, cy = Mathf.cos(c.z) * i * sizeP, nx = Mathf.sin(n.z) * (i + 1) * sizeP, ny = Mathf.cos(n.z) * (i + 1) * sizeP;
			Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
		}
	});

	public EffectTrail() {
		this(1, 1);
	}

	public EffectTrail(int length, float width) {
		this.length = length;
		this.width = width;
		points = new Seq<>(length);
		
	}

	public EffectTrail clear() {
		points.clear();
		return this;
	}

	public void draw(Color color) {
		Draw.color(color);

		for (int i = 0; i < points.size - 1; i++) {
			Vec3 c = points.get(i);
			Vec3 n = points.get(i + 1);
			float sizeP = width/ length;

			float cx = Mathf.sin(c.z) * i * sizeP, cy = Mathf.cos(c.z) * i * sizeP, nx = Mathf.sin(n.z) * (i + 1) * sizeP, ny = Mathf.cos(n.z) * (i + 1) * sizeP;
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

	public void disappear(Color color) {
		if (points.isEmpty())return;
		disappearEffect.at(this.points.peek().x, this.points.peek().y, this.length, color, this.points);
	}

}
