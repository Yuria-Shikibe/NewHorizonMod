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
	zetaFluid, quantumLiquid,
	irdryonFluid;
	
	@Override
	public void load(){
		quantumLiquid = new Liquid("quantum-liquid", NHColor.darkEnrColor){{
			heatCapacity = 1.25f;
			lightColor = NHColor.darkEnrColor;
			effect = NHStatusEffects.quantization;
		}};
		
		
		xenAlpha = new Liquid("xen-alpha", Color.valueOf("#AEDFFF")){{
			heatCapacity = 0.3f;
			explosiveness = 0.25f;
			viscosity = 0.8f;
			temperature = 2f;
		}};
		
		
		xenBeta = new Liquid("xen-beta", Color.valueOf("#CAEEFF")){{
			heatCapacity = 1.1f;
			explosiveness = 0.3f;
			viscosity = 0.5f;
			lightColor = Color.valueOf("#CAEEFF");
		}};

		xenGamma = new Liquid("xen-gamma", Color.valueOf("#CAEEFF")){{
			heatCapacity = 1.65f;
			explosiveness = 0.6f;
			viscosity = 0.5f;
			temperature = 0f;
			lightColor = Color.valueOf("#CAEEFF");
			effect = NHStatusEffects.emp2;
		}};

		zetaFluid = new Liquid("zeta-fluid", Color.valueOf("#f0ffba")){{
			heatCapacity = 0.3f;
			explosiveness = 0.75f;
			viscosity = 0.1f;
			lightColor = Color.valueOf("#f0ffba");
		}};

		irdryonFluid = new Liquid("irdryon-fluid", Color.valueOf("#F7C6B5")){{
			viscosity = 0.8f;
			temperature = 5f;
		}};
	}
}
