package newhorizon.block.special;

import arc.func.Boolf2;
import arc.func.Cons;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.EntityGroup;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.vars.NHVars;
import newhorizon.vars.NHWorldVars;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static mindustry.Vars.*;

public abstract class CommandableBlock extends Block{
	public Boolf2<CommandableBlockBuild, CommandableBlockBuild> groupBoolf = null;
	protected static final Vec2 tmpVec = new Vec2();
	
	public CommandableBlock(String name){
		super(name);
		update = configurable = solid = true;
	}
	
	@Override
	public void init(){
		super.init();
		if(groupBoolf == null)groupBoolf = (b1, b2) -> b1.block == b2.block;
	}
	
	public abstract class CommandableBlockBuild extends Building implements BeforeLoadc, Ranged{
		public transient int lastTarget = -1;
		
		@NotNull public abstract CommandableBlockType getType();
		
		public abstract void setTarget(Point2 point2);
		public abstract int getTarget();
		
		public abstract void command(Integer point2);
		public abstract void commandAll(Integer pos);
		
		public abstract boolean canCommand();
		public abstract boolean overlap();
		public abstract boolean isCharging();
		public abstract boolean isPreparing();
		public abstract void setPreparing();
		public abstract float delayTime();
		public abstract float spread();
		
		public Table actionTable(){return new Table();}
		
		public  @Nullable Vec2 target(){
			Tile t = world.tile(getTarget());
			if(t != null || getTarget() >= 0) return tmpVec.set(t);
			else return null;
		}
		
		@Override
		public void created(){
			NHVars.world.commandables.add(this);
		}
		
		@Override
		public void remove(){
			super.remove();
			NHWorldVars.advancedLoad.remove(this);
			NHVars.world.commandables.remove(this);
		}
		
		@Override
		public void add(){
			super.add();
			NHWorldVars.advancedLoad.add(this);
			beforeLoad();
		}
		
		@Override
		public void beforeLoad(){NHVars.world.commandables.add(this); }
	}
	
	public enum CommandableBlockType{
		defender, attacker
	}
	
	public static class CommandEntity implements Drawc, Timedc, Teamc{
		public Cons<Teamc> act;
		
		public boolean added;
		public transient int id = EntityGroup.nextId();
		public transient float time, lifetime;
		public transient float x, y;
		public transient Team team;
		
		@Override public float clipSize(){return 500f;}
		
		@Override public void draw(){}
		
		@Override public void update(){
			time = Math.min(time + Time.delta, lifetime);
			if (time >= lifetime) {
				remove();
			}
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
			return this instanceof Unitc && ((Unitc)this).controller() == player;
		}
		@Override public boolean isRemote(){
			return this instanceof Unitc && ((Unitc)this).isPlayer() && !isLocal();
		}
		@Override public float fin(){return time / lifetime;}
		@Override public float time(){return time;}
		@Override public void time(float time){this.time = time;}
		@Override public float lifetime(){return lifetime;}
		@Override public void lifetime(float lifetime){this.lifetime = lifetime;}
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
		@Override public int classId(){ return 1001; }
		@Override public boolean serialize(){ return false; }
		@Override public void read(Reads read){ }
		@Override public void write(Writes write){ }
		@Override public void afterRead(){ }
		@Override public int id(){return id; }
		@Override public void id(int id){ this.id = id; }
		@Override public String toString(){
			return "CommandEntity{" + "added=" + added + ", id=" + id + ", x=" + x + ", y=" + y + ", lifetime=" + lifetime + '}';
		}
		@Override public boolean cheating(){
			return team.rules().cheat;
		}
		@Override public CoreBlock.CoreBuild core(){
			return team.core();
		}
		@Override public CoreBlock.CoreBuild closestCore(){
			return team.core();
		}
		@Override public CoreBlock.CoreBuild closestEnemyCore(){
			return state.teams.closestEnemyCore(x, y, team);
		}
		@Override public Team team(){
			return team;
		}
		@Override public void team(Team team){
			this.team = team;
		}
	}
}
