package newhorizon.expand.entities;

import arc.math.geom.Position;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.player;

public abstract class NHBaseEntity implements Posc, Drawc{
	public float x = 0, y = 0, drawSize = 40;
	public boolean added;
	public transient int id = EntityGroup.nextId();
	
	@Override
	public float clipSize(){
		return drawSize * 2;
	}
	
	@Override
	public void remove(){
		if(!added)return;
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
		return this instanceof Unitc && ((Unitc)this).controller() == player;
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
	@Override public float getX(){ return x; }
	@Override public float getY(){ return y; }
	@Override public float x(){ return x; }
	@Override public void x(float x){ this.x = x; }
	@Override public float y(){ return y; }
	@Override public void y(float y){ this.y = y; }
	@Override public boolean isAdded(){ return added; }
	@Override public boolean serialize(){ return true; }
	@Override public void read(Reads read){
		x = read.f();
		y = read.f();
	}
	@Override public void write(Writes write){
		write.f(x);
		write.f(y);
	}
	
	@Override
	public int classId(){return 0;}
	@Override public void afterRead(){ }
	@Override public int id(){return id; }
	@Override public void id(int id){ this.id = id; }
	@Override public String toString(){
		return "CommandEntity{" + "added=" + added + ", id=" + id + ", x=" + x + ", y=" + y + '}';
	}
}
