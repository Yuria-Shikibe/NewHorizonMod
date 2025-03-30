package newhorizon.content;

import arc.struct.ObjectMap;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import newhorizon.expand.units.unitType.NHUnitType;

import static mindustry.content.TechTree.*;

public class NHTechTree{
	public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();

	public static void load(){
		unitBuildCost.each((u, is) -> {
			if(u instanceof NHUnitType){
				((NHUnitType)u).setRequirements(is);
			}
		});
		
		TechNode root = nodeRoot("new-horizon", NHPlanets.midantha, () -> {
			node(NHSectorPresents.abandonedOutpost, ItemStack.with(), () -> {});
		});
		
		root.planet = NHPlanets.midantha;
		root.children.each(c -> c.planet = NHPlanets.midantha);
	}
}
