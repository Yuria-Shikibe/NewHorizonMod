package newhorizon.content;

import arc.graphics.Color;
import mindustry.ctype.ContentList;
import mindustry.type.Liquid;

public class NHLiquids implements ContentList{
	
	//Load Mod Liquids
	
	public static Liquid 
	xenAlpha, 
	xenBeta,
	xenGamma,
	zetaFluid,
	infinityLiquid,
	irdryonFluid;
	
	@Override
	public void load(){
		infinityLiquid = new Liquid("infinity-liquid", Color.valueOf("#B170FF")){
			@Override public boolean isHidden(){return true;}
		{
			heatCapacity = Float.MAX_VALUE / 10000f;
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
			heatCapacity = 1.65f;
			explosiveness = 100f;
			viscosity = 0.5f;
			temperature = 0f;
			lightColor = Color.valueOf("#CAEEFF");
			effect = NHStatusEffects.emp2;
		}};

		zetaFluid = new Liquid("zeta-fluid", Color.valueOf("#f0ffba")){{
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
