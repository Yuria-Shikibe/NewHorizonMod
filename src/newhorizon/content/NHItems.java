package newhorizon.content;

import arc.graphics.Color;
import mindustry.type.Item;

public class NHItems{
	
	//Load Mod Items
	
	public static Item
	ancimembrane,
	presstanium, seniorProcessor, juniorProcessor,
	zeta, fusionEnergy, multipleSteel, irayrondPanel,
	setonAlloy, darkEnergy, upgradeSort, metalOxhydrigen,
	thermoCorePositive, thermoCoreNegative;
	public static void load(){
		ancimembrane = new Item("ancimembrane"){{
			cost = 5;
			color = NHColor.ancient;
			charge = 5;
		}};
		
		thermoCorePositive = new Item("thermo-core-positive", Color.valueOf("CFFF87")){{
			radioactivity = 2.5f;
			explosiveness = 3f;
			charge = 15f;
		}};
		thermoCoreNegative = new Item("thermo-core-negative", Color.valueOf("#7D95B2").lerp(Color.white, 0.095f)){{
			explosiveness = 1f;
		}};
		presstanium = new Item("presstanium", Color.valueOf("6495ED")){{
			flammability = 0.35f;
			cost = 1.5f;
		}};
		seniorProcessor = new Item("processor-senior", Color.valueOf("FFFACD"));
		juniorProcessor = new Item("processor-junior", Color.valueOf("808080"));
		zeta = new Item("zeta", Color.valueOf("#FFAE87")){{
			hardness = 4;
			radioactivity = 1.5f;
			charge = 1f;
		}};
		fusionEnergy = new Item("fusion-core-energy", Color.valueOf("ffe4b5")){{
			explosiveness = 0.7f;
			charge = 1.25f;
			radioactivity = 1f;
		}};
		multipleSteel = new Item("multiple-steel", Color.valueOf("cedbe3")){{
			cost = 1.75f;
		}};
		irayrondPanel = new Item("irayrond-panel", Color.valueOf("E4F0FF")){{
			cost = 2f;
		}};
		setonAlloy = new Item("seton-alloy", Color.valueOf("#151C23")){{
			cost = 2.5f;
		}};
		darkEnergy = new Item("dark-energy", Color.valueOf("#BE91FF")){{
			radioactivity = 5f;
			explosiveness = 5f;
			charge = 25f;
		}};
		upgradeSort = new Item("upgradeSort", Color.valueOf("1D1E23")){{
			cost = 3f;
		}};
		metalOxhydrigen = new Item("metal-oxhydrigen", Color.valueOf("#BFF3FF"));
	}
	
}










