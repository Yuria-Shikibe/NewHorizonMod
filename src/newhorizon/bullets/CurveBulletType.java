package newhorizon.bullets;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;
import newhorizon.interfaces.Curve;

public class CurveBulletType extends BulletType{
	public int innerNum = 2, outerNum = 1;
	public float stroke = 5f;
	public float length = 60f;
	public float nodeDst_1 = 1 / 3f, nodeDst_2 = 2 / 3f;
	
	public Curve<Bullet> getLength = b -> Mathf.curve(b.fslope(), 0.1f, 0.8f) * length + b.fin() * length;
	
	public CurveBulletType(float speed, float damage){
		super(speed, damage);
		collides = false;
	}
	
	public CurveBulletType(){
		super();
		collides = false;
	}
	
	protected static class Data{
		float x, y;
		int id;
		
		public Data(float x, float y, int id){
			this.x = x;
			this.y = y;
			this.id = id;
		}
	}
	
	@Override
	public void init(){
		super.init();
		trailDespawn = new Effect(100, 1000, e -> {
			if(!(e.data instanceof Data))return;
			Data data = e.data();
			
			Draw.color(e.color);
			Lines.stroke(stroke * e.fout(Interp.pow2Out));
			
			float len = e.rotation;
			
			float fromX = data.x, fromY = data.y;
			
			Tmp.v3.set(e.x, e.y).sub(fromX, fromY);
			Vec2 vec1 = Tmp.v1.set(Tmp.v3).scl(nodeDst_1), vec2 = Tmp.v2.set(Tmp.v3).scl(nodeDst_2);
			
			Angles.randLenVectors(data.id, innerNum, len, (x, y) -> {
				Angles.randLenVectors(data.id - 1, outerNum, len, (x2, y2) -> {
					Lines.curve(fromX, fromY, fromX + vec1.x + x, fromY + vec1.y + y, fromX + vec2.x + x2, fromY + vec2.y + y2, e.x, e.y, 16);
				});
			});
			Fill.circle(fromX, fromY, 2.5f * Lines.getStroke() * e.fout(Interp.pow2Out));
			Fill.circle(e.x, e.y, 2.5f * Lines.getStroke() * e.fout(Interp.pow2Out));
			Draw.reset();
		}).layer(Layer.bullet);
	}
	
	@Override
	public void init(Bullet b){
		super.init(b);
		b.data(new Vec2(b.x, b.y));
	}
	
	@Override
	public void draw(Bullet b){
		if(!(b.data instanceof Vec2))return;
		Vec2 from = (Vec2)b.data();
		
		Tmp.v3.set(b).sub(from);
		Vec2 vec1 = Tmp.v1.set(Tmp.v3).scl(nodeDst_1), vec2 = Tmp.v2.set(Tmp.v3).scl(nodeDst_2);
		
		Draw.color(lightColor);
		Lines.stroke(stroke);
		
		float len = getLength.get(b);
		Angles.randLenVectors(b.id, innerNum, len, (x, y) -> {
			Angles.randLenVectors(b.id - 1, outerNum, len, (x2, y2) -> {
				Lines.curve(from.x, from.y, from.x + vec1.x + x, from.y + vec1.y + y, from.x + vec2.x + x2, from.y + vec2.y + y2, b.x, b.y, 16);
			});
		});
		Fill.circle(from.x, from.y, 2.5f * Lines.getStroke());
		Fill.circle(b.x, b.y, 2.5f * Lines.getStroke());
		Draw.reset();
	}
	
	@Override
	public void despawned(Bullet b){
		super.despawned(b);
		if(!(b.data instanceof Vec2))return;
		Vec2 v = (Vec2)b.data();
		trailDespawn.at(b.x, b.y, getLength.get(b), lightColor, new Data(v.x, v.y, b.id));
	}
	
	protected Effect trailDespawn = Fx.none;
}
