package newhorizon.effects;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHFx;
import newhorizon.func.NHSetting;

import static mindustry.Vars.player;

public class EffectTrail implements Cloneable{
	public static final float LIFETIME = 20f, DRAW_SIZE = 500f;
	public int length;
	public float width;
	protected final Seq<Vec3> points;
	protected float lastX = -1, lastY = -1;
	
	public Color fromColor, toColor;
	
//	public float trailWeaveScale = 1f;
//	public float trailWeaveMag = -1f;
//	public float trailOffset = 0f;
//	public int trails = 1;
//	public float sideOffset = 0f;
//	public boolean flip;

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

	public void draw() {
		if(points.isEmpty())return;
		Draw.color(fromColor);
		Fill.circle(points.peek().x, points.peek().y, width * 1.1f);
		
		Draw.reset();
		for (int i = 0; i < points.size - 1; i++) {
			Vec3 c = points.get(i);
			Vec3 n = points.get(i + 1);
			float sizeP = width / length;

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
	
	public EffectTrail cpy(){
		try{
			return (EffectTrail)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
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
		//NHSetting.debug(() -> Log.info("RUN ED"));
		//TrailDrawer drawer = Pools.obtain(TrailDrawer.class, TrailDrawer::new);
		//drawer.add();
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
	
	public class TrailDrawer implements Drawc, Pool.Poolable, Timedc{
		public boolean added;
		public transient int id = EntityGroup.nextId();
		public transient float drawSize = 500f;
		public transient float time, lifetime, initWidth;
		public transient float x, y;
		public transient EffectTrail trail;
		
		public TrailDrawer(){this(35f, width, DRAW_SIZE, EffectTrail.this.cpy());}
		
		public TrailDrawer(float lifetime, float initWidth, float drawSize, EffectTrail trail){
			//NHSetting.debug(() -> Log.info("NEW ED"));
			this.lifetime = lifetime;
			this.initWidth = initWidth;
			this.drawSize = drawSize;
			this.trail = trail;
			try{
				x = trail.points.peek().x;
				y = trail.points.peek().y;
			}catch(ArrayIndexOutOfBoundsException e){
				x = 0;
				y = 0;
			}
		}
		
		@Override public float clipSize(){return drawSize;}
		@Override public void draw(){trail.draw();}
		@Override public void set(float x, float y){
			this.x = x;
			this.y = y;
		}
		@Override public void set(Position pos){set(pos.getX(), pos.getY());}
		@Override public void trns(float x, float y){set(this.x + x, this.y + y);}
		@Override public void trns(Position pos){trns(pos.getX(), pos.getY());}
		@Override public int tileX(){return 0;}
		@Override public int tileY(){return 0; }
		@Override public Floor floorOn(){ return null; }
		@Override public Block blockOn(){ return null; }
		@Override public boolean onSolid(){ return false; }
		@Override public Tile tileOn(){ return null; }
		@Override public float getX(){ return 0; }
		@Override public float getY(){ return y; }
		@Override public float x(){ return x; }
		@Override public void x(float x){ this.x = x; }
		@Override public float y(){ return y; }
		@Override public void y(float y){ this.y = y; }
		@Override public boolean isAdded(){ return added; }
		
		@Override public void update(){
			//EffectTrail.this.update(x(), y());
			time = Math.min(time + Time.delta, lifetime);
			if (time >= lifetime) {
				remove();
			}
			Log.info("UPDATED" + time);
			trail.width = initWidth * (1 - fin());
		}
		
		@Override public float fin(){return time / LIFETIME;}
		@Override public float time(){return time;}
		@Override public void time(float time){this.time = time;}
		@Override public float lifetime(){return lifetime;}
		@Override public void lifetime(float lifetime){this.lifetime = lifetime;}
		
		@Override
		public void remove(){
			Groups.draw.remove(this);
			Groups.all.remove(this);
			Groups.queueFree(this);
			added = false;
			NHSetting.debug(() -> Log.info("Removed: " + this));
		}
		
		@Override
		public void add(){
			if(added) return;
			Groups.all.add(this);
			Groups.draw.add(this);
			added = true;
			NHSetting.debug(() -> Log.info(this));
		}
		
		@Override
		public boolean isLocal(){
			return this instanceof Unitc && ((Unitc)this).controller() == player;
		}
		
		@Override
		public boolean isRemote(){
			return this instanceof Unitc && ((Unitc)this).isPlayer() && !isLocal();
		}
		
		@Override public boolean isNull(){ return false; }
		@Override public <T extends Entityc> T self(){ return (T)this; }
		@Override public <T> T as(){ return (T)this; }
		
		@Override
		public <T> T with(Cons<T> cons) {
			cons.get((T)this);
			return (T)this;
		}
		
		@Override public int classId(){ return 1001; }
		@Override public boolean serialize(){ return false; }
		@Override public void read(Reads read){ }
		@Override public void write(Writes write){ }
		@Override public void afterRead(){ }
		@Override public int id(){return id; }
		@Override public void id(int id){ this.id = id; }
		@Override public void reset(){
			//drawSize = 0;
			//trail = null;
			//lifetime = 0;
			//initWidth = 0;
			//trail.clear();
			trail.width = initWidth;
			time = 0;
			added = false;
			id = EntityGroup.nextId();
		}
		
		@Override
		public String toString(){
			return "TrailDrawer{" + "added=" + added + ", id=" + id + ", x=" + x + ", y=" + y + ", lifetime=" + lifetime + ", initWidth=" + initWidth + '}';
		}
	}
}
