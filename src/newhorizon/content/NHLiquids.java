package newhorizon.content;

import arc.graphics.Color;
import mindustry.type.Liquid;

public class NHLiquids{

	public static Liquid quantumEntity, xenFluid, zetaFluidPositive, zetaFluidNegative, irayrondFluid, xenAlpha, xenBeta, xenGamma, zetaFluid;
	
	public static void load(){
		quantumEntity = new Liquid("quantum-liquid", NHColor.darkEnrColor){{
			heatCapacity = 0.75f;
			lightColor = NHColor.darkEnrColor;
			gas = true;
			barColor = gasColor = lightColor;
			}
			@Override
			public void init(){
				super.init();
				coolant = true;
			}
		};

		xenFluid = new Liquid("xen-fluid", Color.valueOf("#aedfff")){{
			heatCapacity = 0.3f;
			explosiveness = 0.25f;
			viscosity = 0.8f;
			temperature = 2f;

			coolant = false;
		}};

		zetaFluidPositive = new Liquid("zeta-fluid-positive", Color.valueOf("#bdd68b")){{
			heatCapacity = 0.3f;
			explosiveness = 0.75f;
			viscosity = 0.1f;
			lightColor = Color.valueOf("#dde6a1");
		}};

		zetaFluidNegative = new Liquid("zeta-fluid-negative", Color.valueOf("#bccee3")){{
			heatCapacity = 0.3f;
			explosiveness = 0.75f;
			viscosity = 0.1f;
			lightColor = Color.valueOf("#deedff");
		}};

		irayrondFluid = new Liquid("irdryon-fluid", Color.valueOf("#F7C6B5")){{
			viscosity = 0.8f;
			temperature = 5f;
		}};

		xenAlpha = new Liquid("xen-alpha", Color.valueOf("#AEDFFF")){{
			heatCapacity = 0.3f;
			explosiveness = 0.25f;
			viscosity = 0.8f;
			temperature = 2f;

			hidden = true;
		}};
		
		xenBeta = new Liquid("xen-beta", Color.valueOf("#CAEEFF")){{
			heatCapacity = 1.1f;
			explosiveness = 0.3f;
			viscosity = 0.5f;
			lightColor = Color.valueOf("#CAEEFF");

			hidden = true;
		}};

		xenGamma = new Liquid("xen-gamma", Color.valueOf("#CAEEFF")){{
			heatCapacity = 1.65f;
			explosiveness = 0.6f;
			viscosity = 0.5f;
			temperature = 0f;
			lightColor = Color.valueOf("#CAEEFF");

			hidden = true;
		}};

		zetaFluid = new Liquid("zeta-fluid", Color.valueOf("#f0ffba")){{
			heatCapacity = 0.3f;
			explosiveness = 0.75f;
			viscosity = 0.1f;
			lightColor = Color.valueOf("#f0ffba");

			hidden = true;
		}};
	}
}
