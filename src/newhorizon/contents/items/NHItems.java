package newhorizon.contents.items;

import arc.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class NHItems implements ContentList{
	
	//Load Mod Items
	
	public static Item 
	presstanium, seniorProcessor, juniorProcessor,
	zate, fusionEnergy, multipleSteel, irayrondPanel,
	setonAlloy, darkEnergy, upgradeSort, metalOxhydrigen,
	thermoCorePositive, thermoCoreNegative,
	emergencyReplace;
	
	@Override
	public void load(){
		emergencyReplace = new Item("emergency-replace", Color.valueOf("#BD0019"));
		thermoCorePositive = new Item("thermo-core-positive", Color.valueOf("#C0FF53")){{
			explosiveness = 5f;
		}};
		thermoCoreNegative = new Item("thermo-core-negative", Color.valueOf("#7D95B2")){{
			explosiveness = 2f;
		}};
		presstanium = new Item("presstanium", Color.valueOf("6495ED"));
		seniorProcessor = new Item("processor-senior", Color.valueOf("6495ED"));
		juniorProcessor = new Item("processor-junior", Color.valueOf("808080"));
		zate = new Item("zate", Color.valueOf("#FFAE87"));
		fusionEnergy = new Item("fusion-core-energy", Color.valueOf("ffe4b5")){{
			explosiveness = 3f;
		}};
		multipleSteel = new Item("multiple-steel", Color.valueOf("cedbe3"));
		irayrondPanel = new Item("irayrond-panel", Color.valueOf("f7f7f7"));
		setonAlloy = new Item("seton-alloy", Color.valueOf("#151C23")){{
			explosiveness = 10f;
			radioactivity = 25f;
		}};
		darkEnergy = new Item("dark-energy", Color.valueOf("#BE91FF"));
		upgradeSort = new Item("upgradeSort", Color.valueOf("1D1E23"));
		metalOxhydrigen = new Item("metal-oxhydrigen", Color.valueOf("#BFF3FF"));
	}
	
}










