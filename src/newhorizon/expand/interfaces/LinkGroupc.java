package newhorizon.expand.interfaces;

import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;

public interface LinkGroupc extends Linkablec{
	default Seq<Building> linkBuilds(){
		Seq<Building> buildings = new Seq<>();
		for(int pos : linkGroup().shrink()){
			Building b = Vars.world.build(pos);
			if(linkValid(b))buildings.add(b);
			else linkGroup().removeValue(pos);
		}
		return buildings;
	}
	
	IntSeq linkGroup();
	void linkGroup(IntSeq seq);
	
	@Override
	default void drawLink(){
		drawLink(linkBuilds());
	}
	
	@Override
	default boolean linkValid(){
		for(Building b : linkBuilds())if(!linkValid(b))return false;
		return true;
	}
	
	@Override default int linkPos(){return pos();}
	@Override default Building link(){return as();}
	
}
