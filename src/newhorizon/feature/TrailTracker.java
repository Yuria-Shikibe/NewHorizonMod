package newhorizon.feature;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.geom.Position;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.effects.EffectTrail;

public class TrailTracker implements Drawc, Posc{
	public EffectTrail trail = new EffectTrail(10);
	public float x, y;
	public transient float lastX, lastY;
	public transient int stopped = 0;
	public Teamc trackTarget = Nulls.team;
	public boolean added;
	public transient int id = EntityGroup.nextId();
	
	@Override public float clipSize(){return 500f;}
	
	@Override public void draw(){
		trail.draw(fout());
	}
	
	@Override public void update(){
		if(trackTarget == null || stopped == trail.length){
			remove();
			return;
		}
		if(lastX == x && lastY == y)stopped ++;
		
		lastX = x;
		lastY = y;
		set(trackTarget);
		trail.update(x, y);
	}
	
	public float fout(){
		if(trail == null)return 1;
		return (trail.length - stopped) / (float)trail.length;
	}
	
	public void setTrail(Teamc owner, int length, float width, Color fromColor, Color toColor){
		trail = new EffectTrail(length, width, fromColor, toColor);
		trackTarget = owner;
	}
	
	@Override
	public void remove(){
		Groups.draw.remove(this);
		Groups.all.remove(this);
		added = false;
	}
	
	@Override
	public void add(){
		if(added)return;
		Groups.all.add(this);
		Groups.draw.add(this);
		added = true;
	}
	
	@Override public boolean isLocal(){
		return this instanceof Unitc && ((Unitc)this).controller() == Vars.player;
	}
	@Override public boolean isRemote(){
		return this instanceof Unitc && ((Unitc)this).isPlayer() && !isLocal();
	}
	
	@Override public boolean isNull(){ return false; }
	@Override public <T extends Entityc> T self(){ return (T)this; }
	@Override public <T> T as(){ return (T)this; }
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
	@Override public <T> T with(Cons<T> cons) {
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
	@Override public String toString(){
		return "CommandEntity{" + "added=" + added + ", id=" + id + ", x=" + x + ", y=" + y + '}';
	}
}
