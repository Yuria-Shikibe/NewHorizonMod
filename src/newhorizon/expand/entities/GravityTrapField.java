package newhorizon.expand.entities;

import arc.func.Boolp;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Position;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import mindustry.game.Team;
import mindustry.gen.Unit;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import org.jetbrains.annotations.NotNull;

public class GravityTrapField implements Position, QuadTree.QuadTreeObject{
	public static final Runnable DRAWER = () -> {
		for(GravityTrapField i : NHFunc.getObjects(NHGroups.gravityTraps))i.draw();
	};
	
	public float x = 0, y = 0;
	public float range = 120;
	public Boolp activated = () -> true;
	public Prov<Team> team = () -> Team.derelict;
	
	public float getRange(){
		return range;
	}
	
	public void setRange(float range){
		this.range = range;
	}
	
	public boolean active(){return activated.get();}
	
	public void setPosition(Position position){
		x = position.getX();
		y = position.getY();
	}
	
	public GravityTrapField add(){
		NHGroups.gravityTraps.insert(this);
		
		return this;
	}
	
	public void remove(){
		NHGroups.gravityTraps.remove(this);
	}
	
	public GravityTrapField(@NotNull Unit unit, float range){
		this.range = range;
		team = unit::team;
		activated = unit::isValid;
		setPosition(unit);
	}
	
	public GravityTrapField(){
	
	}
	
	public GravityTrapField(@NotNull GravityTrap.GravityTrapBuild build){
		setPosition(build);
		activated = () -> build.active() && build.isValid();
		team = () -> build.team;
		range = build.range();
	}
	
	public Team team(){
		return team.get();
	}
	
	public void draw(){
		if(!active()) return;
		Draw.color(DrawFunc.markColor(team()));
		Fill.poly(x, y, 6, range);
	}
	
	@Override
	public float getX(){
		return x;
	}
	
	@Override
	public float getY(){
		return y;
	}
	
	@Override
	public void hitbox(Rect out){
		out.setSize(range * 2).setCenter(x, y);
	}
	
	@Override
	public String toString(){
		return "GravityTrapField{" + "pos(" + x + ", " + y + ")}";
	}
}
