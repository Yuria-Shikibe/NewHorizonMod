package newhorizon.content;

import arc.graphics.Color;
import mindustry.content.Liquids;
import mindustry.type.Liquid;

public class NHLiquids {

    public static Liquid ammonia, hydrazine, quantumLiquid, xenFluid, zetaFluidPositive, zetaFluidNegative, irdryonFluid;

    public static Liquid water, slag, oil, cryofluid, arkycite, gallium, neoplasm, ozone, hydrogen, nitrogen, cyanogen;

    public static void load() {
        ammonia = new Liquid("ammonia", Color.valueOf("919ff0")){{
            viscosity = 0.8f;
            temperature = 0.5f;
            heatCapacity = 0.5f;
        }};

        hydrazine = new Liquid("hydrazine", Color.valueOf("f3b9ca")){{
            heatCapacity = 0.3f;
            explosiveness = 0.25f;
            viscosity = 0.8f;
            temperature = 2f;

            coolant = false;
        }};

        quantumLiquid = new Liquid("quantum-liquid", NHColor.darkEnrColor) {{
            heatCapacity = 0.5f;
            lightColor = NHColor.darkEnrColor;
            barColor = gasColor = lightColor;
        }};

        xenFluid = new Liquid("xen-fluid", Color.valueOf("#aedfff")) {{
            heatCapacity = 0.5f;
            explosiveness = 1.5f;
            viscosity = 0.5f;

            coolant = false;
        }};

        zetaFluidPositive = new Liquid("zeta-fluid-positive", Color.valueOf("#bdd68b")) {{
            heatCapacity = 0.3f;
            explosiveness = 0.75f;
            viscosity = 0.1f;
            lightColor = Color.valueOf("#dde6a1");
            coolant = false;
        }};

        zetaFluidNegative = new Liquid("zeta-fluid-negative", Color.valueOf("#bccee3")) {{
            heatCapacity = 0.3f;
            explosiveness = 0.75f;
            viscosity = 0.1f;
            lightColor = Color.valueOf("#deedff");
            coolant = false;
        }};

        irdryonFluid = new Liquid("irdryon-fluid", Color.valueOf("#F7C6B5")) {{
            viscosity = 0.8f;
            temperature = 0.2f;
            heatCapacity = 2f;
        }};

        water = Liquids.water;
        slag = Liquids.slag;
        oil = Liquids.oil;
        cryofluid = Liquids.cryofluid;
        arkycite = Liquids.arkycite;
        gallium = Liquids.gallium;
        neoplasm = Liquids.neoplasm;
        ozone = Liquids.ozone;
        hydrogen = Liquids.hydrogen;
        nitrogen = Liquids.nitrogen;
        cyanogen = Liquids.cyanogen;
    }
}
