package newhorizon.util.graphic;

import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.pooling.Pool;

public class ReusableVec2 extends Vec2 implements Pool.Poolable{
	private static final Pool<ReusableVec2> pool = new Pool<ReusableVec2>(50, 2000){
		@Override
		protected ReusableVec2 newObject(){
			return new ReusableVec2();
		}
		
		@Override
		protected void reset(ReusableVec2 object){
			object.reset();
		}
	};
	
	@Override
	public void reset(){
		x = y = 0;
	}
	
	public static ReusableVec2 get(){
		return pool.obtain();
	}
	
	public static ReusableVec2 get(float x, float y){
		ReusableVec2 v = pool.obtain();
		v.set(x, y);
		return v;
	}
	
	public static void free(ReusableVec2 v){
		pool.free(v);
	}
	
	public static void freeAll(Seq<ReusableVec2> v){
		pool.freeAll(v);
	}
	
	public static void freeAll(ReusableVec2[] vs){
		for(ReusableVec2 v : vs)pool.free(v);
	}
}
