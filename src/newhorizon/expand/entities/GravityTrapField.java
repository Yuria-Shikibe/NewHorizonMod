package newhorizon.expand.entities;

import arc.func.Boolf2;
import arc.func.Boolp;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Intersector;
import arc.math.geom.Position;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Hitboxc;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import newhorizon.NHGroups;
import newhorizon.expand.block.defence.GravityWell;
import newhorizon.expand.block.special.NexusCore;
import newhorizon.util.graphic.DrawFunc;

public class GravityTrapField implements Position, QuadTree.QuadTreeObject{
	protected static final Seq<GravityTrapField> tmpSeq = new Seq<>();
	protected static final Rect tmpRect = new Rect();
	
	public static void drawAll(){
		for(GravityTrapField i : NHGroups.gravityTrapsDraw)i.draw();
	}

	public static final Boolf2<Team, Hitboxc> IntersectedAlly = (team, entity) -> {
		entity.hitbox(tmpRect);
		tmpSeq.clear();
		NHGroups.gravityTraps.intersect(tmpRect, tmpSeq);
		for(GravityTrapField f : tmpSeq){
			if(team == f.team() && f.active() && Intersector.isInsideHexagon(f.x, f.y, f.range * 2f, entity.x(), entity.y())){
				return true;
			}
		}
	
		return false;
	};
	
	public static final Boolf2<Teamc, Rect> IntersectedAllyRect = (team, rect) -> {
		tmpSeq.clear();
		NHGroups.gravityTraps.intersect(rect, tmpSeq);
		for(GravityTrapField f : tmpSeq){
			if(team.team() == f.team() && f.active()){
				return true;
			}
		}
		
		return false;
	};
	
	public static final Boolf2<Teamc, Rect> IntersectedHostileRect = (team, rect) -> {
		tmpSeq.clear();
		NHGroups.gravityTraps.intersect(rect, tmpSeq);
		for(GravityTrapField f : tmpSeq){
			if(team.team() != f.team() && f.active()){
				return true;
			}
		}
		
		return false;
	};
	
	public static final Boolf2<Team, Hitboxc> IntersectedHostile = (team, entity) -> {
		entity.hitbox(tmpRect);
		tmpSeq.clear();
		NHGroups.gravityTraps.intersect(tmpRect, tmpSeq);
		for(GravityTrapField f : tmpSeq){
			if(team != f.team() && f.active()){
				return true;
			}
		}
		
		return false;
	};
	
	public float x = 0, y = 0;
	public float range = 120;
	public Boolp activated = () -> true;
	public Prov<Team> team = () -> Team.derelict;
	public Runnable drawer = () -> {
		if(!active()) return;
		Draw.color(DrawFunc.markColor(team()));
		Fill.square(x, y, range);
	};
	
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
		NHGroups.gravityTrapsDraw.add(this);
		
		
		return this;
	}
	
	
	public void remove(){
		NHGroups.gravityTraps.remove(this);
		NHGroups.gravityTrapsDraw.remove(this);
	}
	
	/**
	 * Do Changes here if the field is not a rect.
	 *
	 * */
	/*
	public <T extends TeamHealthc> boolean checkExactHitbox(T t){
		return true;
	}*/
	
	public GravityTrapField(Unit unit, float range){
		this.range = range;
		team = unit::team;
		activated = unit::isValid;
		setPosition(unit);
	}
	
	public GravityTrapField(){
	
	}
	
	public GravityTrapField(GravityWell.GravityTrapBuild build){
		setPosition(build);
		activated = () -> build.active() && build.isValid();
		team = () -> build.team;
		range = build.range();
	}

	public GravityTrapField(NexusCore.NexusCoreBuild build){
		setPosition(build);
		activated = build::isValid;
		team = () -> build.team;
		range = build.range();
	}
	
	public Team team(){
		return team.get();
	}
	
	public void draw(){
		drawer.run();
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
