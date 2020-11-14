package mindustry.graphics;

import mindustry.entities.Effect;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.*;

public class EffectTrail{
	public static final float LIFETIME = 20f;
    public int length;
	public float width;
	
	//No fucking sucks private
    protected final Seq<Vec3> points;
    protected float lastX = -1, lastY = -1;

    public EffectTrail(int length, float width){
    	this.width = width;
        this.length = length;
        points = new Seq<>(length);
    }

    public void clear(){
        points.clear();
    }

    public void draw(Color color){
        Draw.color(color);

        for(int i = 0; i < points.size - 1; i++){
            Vec3 c = points.get(i);
            Vec3 n = points.get(i + 1);
            float size = width * 1f / length;

            float cx = Mathf.sin(c.z) * i * size, cy = Mathf.cos(c.z) * i * size, nx = Mathf.sin(n.z) * (i + 1) * size, ny = Mathf.cos(n.z) * (i + 1) * size;
            Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
        }

        Draw.reset();
    }

    public void update(float x, float y, boolean updateLength){
		if(updateLength)length = (int)Mathf.floor(14 / Time.delta * 1.65f);
    
        if(points.size > length){
            Pools.free(points.first());
            points.remove(0);
        }

        float angle = -Angles.angle(x, y, lastX, lastY);

        points.add(Pools.obtain(Vec3.class, Vec3::new).set(x, y, (angle) * Mathf.degRad));

        lastX = x;
        lastY = y;
    }
    
	public void disappear(Color color){
		if(points.isEmpty())return;
		
		new Effect(LIFETIME, points.first().dst.(points.peek()), e -> {
			if(e.data instanceof EffectTrailData){
				EffectTrailData data = (EffectTrailData)e.data;
				Draw.color(e.color);
				for(int i = 0; i < data.points.size - 1; i++){
					Vec3 c = data.points.get(i);
					Vec3 n = data.points.get(i + 1);
					float size = data.width * e.fout() / data.length;

					float cx = Mathf.sin(c.z) * i * size, cy = Mathf.cos(c.z) * i * size, nx = Mathf.sin(n.z) * (i + 1) * size, ny = Mathf.cos(n.z) * (i + 1) * size;
					Fill.quad(c.x - cx, c.y - cy, c.x + cx, c.y + cy, n.x + nx, n.y + ny, n.x - nx, n.y - ny);
				}
			}
		}).at(points.peek().x, points.peek().y, points.first().angle(points.peek()), color, new EffectTrailData(this.points, this.length, this.width) );
		
		for(Vec3 vec : points)Pools.free(vec);
	}
    	
	protected static class EffectTrailData{
		public Seq<Vec3> points;
		public float width, length;
		
		public EffectTrailData(Seq<Vec3> points, float length, float width){
			this.length = length;
			this.points = points;
			this.width = width;
		}
	}
}
