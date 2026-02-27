package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Liquids;
import mindustry.gen.Puddle;
import mindustry.graphics.Drawf;
import mindustry.type.Liquid;
import mindustry.world.meta.Attribute;

import static mindustry.entities.Puddles.maxLiquid;

public class NHLiquids {
    public static Seq<Liquid> streams = Seq.with();
    public static Seq<Liquid> floodLiquid = Seq.with();

    //NH Liquids
    public static Liquid ammonia, hydrazine, quantumLiquid, xenFluid, zetaFluidPositive, zetaFluidNegative, irdryonFluid;
    //streams, special kind of liquid
    public static Liquid particle, photon, neutron, zetaPositive, zetaNegative, proton, antiMatter;
    //flood liquids
    public static Liquid ploNaq, choVat, karIon;
    //vanilla contents, used for better reference
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

        particle = new Stream("particle-stream", Color.valueOf("ff8787"));
        photon = new Stream("photon-stream", Color.valueOf("ffb15b"));
        neutron = new Stream("neutron-stream", Color.valueOf("fff786"));
        zetaPositive = new Stream("zeta-positive-stream", Color.valueOf("adff93"));
        zetaNegative = new Stream("zeta-negative-stream", Color.valueOf("abf8ff"));
        proton = new Stream("proton-stream", Color.valueOf("8b96ff"));
        antiMatter = new Stream("anti-matter-stream", Color.valueOf("b479ff"));

        //Promethium + Naquium (Factorio SE item)
        ploNaq = new Liquid("plo-naq", Color.valueOf("cba3ff"));
        //Chromium + Watt
        choVat = new Liquid("cho-vat", Color.valueOf("aeebdc"));
        //Kar Ion, Kaion in Everspace2
        karIon = new Liquid("kar-ion", Color.valueOf("f1e69f"));

        floodLiquid.add(ploNaq, choVat, karIon);

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

    public static class Stream extends Liquid {
        public Stream(String name, Color color) {
            super(name, color);
            gas = true;
            coolant = false;

            streams.add(this);
        }

        public boolean willBoil(){
            return false;
        }

        public boolean canExtinguish(){
            return false;
        }
    }
}
