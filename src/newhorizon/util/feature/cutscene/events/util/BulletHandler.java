package newhorizon.util.feature.cutscene.events.util;

import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.gen.Bullet;

import java.lang.reflect.Field;

public class BulletHandler{
	public static final Cons<Bullet>
		spread1 = b -> b.lifetime(b.lifetime() * (1 + Mathf.range(0.075f))),
		spread2 = b -> {
			b.lifetime(b.lifetime() * (1 + Mathf.range(0.075f)));
			b.vel.scl(1 + Mathf.range(0.075f));
		}, none = b -> {};
	
	public static final Seq<Cons<Bullet>> all = new Seq<>();
	
	public static int of(Cons<Bullet> event){return all.indexOf(event);}
	
	public static Cons<Bullet> get(int id){
		return id < 0 || id >= all.size ? none : all.get(id);
	}
	
	static{
		Field[] fields = BulletHandler.class.getFields();
		
		for(Field f : fields){
			if(Cons.class.isAssignableFrom(f.getType())){
				try{
					all.add((Cons<Bullet>)f.get(null));
				}catch(IllegalAccessException e){
					e.printStackTrace();
				}
			}
		}
	}
}
