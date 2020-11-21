package newhorizon.contents.effects;

import mindustry.entities.Effect;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;

public class EffectTrail {
	public static final float LIFETIME = 20f;
	public int length = 1;
	public int staticLength = 1;
	public float width, size;

	//No fucking sucks private
	protected final Seq<Vec3> points;
	protected float lastX = -1, lastY = -1;

	public EffectTrail() {
		this(1, 1, 1);
	}

	public EffectTrail(int length, float width, float size) {
		this.size = size;
		this.width = width;
		this.staticLength = this.length = length;
		points = new Seq<>(length);
	}

	public void clear() {
		points.clear();
	}

	public void draw(Color color) {
		Draw.color(color);

		for (int i = 0; i < points.size - 1; i++) {
			Vec3 c = points.get(i);
			Vec3 n = points.get(i + 1);
			float sizeP = width * 1f / length;

			float cx = Mathf.sin(c.z) * i * sizeP, cy = Mathf.cos(c.z) * i * sizeP, nx = Mathf.sin(n.z) * (i + 1) * sizeP, ny = Mathf.cos(n.z) * (i + 1) * sizeP;
			Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
		}

		Draw.reset();
	}

	public void update(float x, float y, boolean updateLength) {
		if (updateLength)length = (int)Mathf.floor(staticLength / Time.delta * 1.65f);

		if (points.size > length) {
			Pools.free(points.first());
			points.remove(0);
		}

		float angle = -Angles.angle(x, y, lastX, lastY);

		points.add(Pools.obtain(Vec3.class, Vec3::new).set(x, y, (angle) * Mathf.degRad));

		lastX = x;
		lastY = y;
	}

	public void disappear(Color color, float disappearWidth) {
		if (points.isEmpty())return;

		new Effect(LIFETIME, size, e -> {
			if (!(e.data instanceof Seq))return;
			Seq<Vec3> data = e.data();

			Draw.color(e.color);
			for (int i = 0; i < data.size - 1; i++) {
				Vec3 c = data.get(i);
				Vec3 n = data.get(i + 1);
				float sizeP = disappearWidth * e.fout() / e.rotation;

				float cx = Mathf.sin(c.z) * i * sizeP, cy = Mathf.cos(c.z) * i * sizeP, nx = Mathf.sin(n.z) * (i + 1) * sizeP, ny = Mathf.cos(n.z) * (i + 1) * sizeP;
				Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
			}

		}).at(this.points.peek().x, this.points.peek().y, this.length, color, this.points);

		//for (Vec3 vec : this.points)Pools.free(vec);
	}

}
