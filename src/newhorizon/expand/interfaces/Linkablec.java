package newhorizon.expand.interfaces;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.graphics.Drawf;
import mindustry.logic.Ranged;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.util.graphic.DrawFunc.sinScl;

public interface Linkablec extends Buildingc, Ranged{
	Seq<Building> tmpSeq = new Seq<>(1);
	
	@Override default boolean onConfigureBuildTapped(Building other){
		if (this == other || linkPos() == other.pos()) {
			configure(Tmp.p1.set(-1, -1));
			return false;
		}
		if (other.within(this, range()) && other.team == team()) {
			configure(Point2.unpack(other.pos()));
			return false;
		}
		return true;
	}
	
	default void drawLink(@Nullable Seq<Building> builds){
		Draw.reset();
		if(builds == null){
			if(linkValid(link())){
				Draw.color(getLinkColor());
				Drawf.circles(getX(), getY(), block().size / 2f * tilesize + Mathf.absin(Time.time * sinScl, 6f, 1f), getLinkColor());
				DrawFunc.link(this, link(), getLinkColor());
			}
		}else if(builds.size > 0){
			Draw.color(getLinkColor());
			Drawf.circles(getX(), getY(), block().size / 2f * tilesize + Mathf.absin(Time.time * sinScl, 6f, 1f), getLinkColor());
			
			for(Building b : builds){
				if(!linkValid(b))continue;
				DrawFunc.link(this, b, getLinkColor());
			}
		}
		
		Draw.reset();
	}
	
	default void drawLink(){
		drawLink(null);
	}
	
	default Building link(){return world.build(linkPos()); }
	default boolean linkValid(){ return linkValid(link()); }
	default boolean linkValid(Building b){ return b != null; }
	default void linkPos(Point2 point2){linkPos(point2.pack());}
	int linkPos();
	void linkPos(int value);
	Color getLinkColor();
}
