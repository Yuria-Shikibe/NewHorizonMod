package newhorizon.contents.items;

import arc.graphics.*;
import mindustry.ctype.*;
import mindustry.type.*;

public class NHLiquids implements ContentList{
	
	//Load Mod Liquids
	
	public static Liquid 
	xenAlpha, 
	xenBeta,
	xenGamma,
	zateFluid,
	infinityLiquid,
	irdryonFluid;
	
	@Override
	public void load(){
		infinityLiquid = new Liquid("infinity-liquid", Color.valueOf("#B170FF")){{
			heatCapacity = 10f;
			lightColor = Color.valueOf("#B170FF");
		}};
		
		
		xenAlpha = new Liquid("xen-alpha", Color.valueOf("#AEDFFF")){{
			heatCapacity = 0.3f;
			explosiveness = 1f;
			viscosity = 0.8f;
			temperature = 2f;
		}};
		
		
		xenBeta = new Liquid("xen-beta", Color.valueOf("#CAEEFF")){{
			heatCapacity = 1.1f;
			explosiveness = 10f;
			viscosity = 0.5f;
			lightColor = Color.valueOf("#CAEEFF");
		}};

		xenGamma = new Liquid("xen-gamma", Color.valueOf("#CAEEFF")){{
			heatCapacity = 1.5f;
			explosiveness = 100f;
			viscosity = 0.5f;
			temperature = 0f;
			lightColor = Color.valueOf("#CAEEFF");
		}};

		zateFluid = new Liquid("zate-fluid", Color.valueOf("#f0ffba")){{
			heatCapacity = 0.3f;
			explosiveness = 3f;
			viscosity = 0.1f;
			lightColor = Color.valueOf("#f0ffba");
		}};

		irdryonFluid = new Liquid("irdryon-fluid", Color.valueOf("#F7C6B5")){{
			viscosity = 0.8f;
			temperature = 5f;
		}};
	}
}
