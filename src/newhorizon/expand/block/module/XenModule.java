package newhorizon.expand.block.module;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import newhorizon.content.NHColor;
import newhorizon.expand.block.struct.XenGraph;

public class XenModule extends BlockModule {
    public XenGraph graph;

    public XenModule(){
        graph = new XenGraph();
    }

    public void setGraph(XenGraph graph){
        this.graph = graph;
    }

    public float getXenFrequency(){
        if (graph != null){
            return graph.height + 250f;
        }
        return 0f;
    }

    public String getXenLevel(){
        if (graph != null){
            if (graph.height >= 350) return Core.bundle.get("xen.level-3");
            if (graph.height >= 200) return Core.bundle.get("xen.level-2");
            if (graph.height >= 50) return Core.bundle.get("xen.level-1");
        }
        return Core.bundle.get("xen.level-0");
    }

    public Color getXenColor(){
        if (graph != null){
            if (graph.height >= 350) return NHColor.xenGamma;
            if (graph.height >= 200) return NHColor.xenBeta;
            if (graph.height >= 50) return NHColor.xenAlpha;
        }
        return NHColor.xenEmpty;
    }

    public Color getXenSmoothColor(){
        if (graph != null){
            if (graph.height >= 350) return Tmp.c1.set(NHColor.xenBeta).lerp(NHColor.xenGamma, (graph.height - 350f) / 150f);
            if (graph.height >= 200) return Tmp.c1.set(NHColor.xenAlpha).lerp(NHColor.xenBeta, (graph.height - 200f) / 150f);
            if (graph.height >= 50) return Tmp.c1.set(NHColor.xenEmpty).lerp(NHColor.xenAlpha, (graph.height - 50f) / 150f);
        }
        return NHColor.xenEmpty;
    }

    public String getXenText(){
        return "[lightgray]" + getXenLevel() + "[] " + Mathf.round(getXenFrequency()) + " [lightgray]" + Core.bundle.get("xen.frequency") + "[]";
    }

    public float getXenFrac(){
        if (graph != null){
            return graph.height / 500f;
        }
        return 0f;
    }

    @Override
    public void write(Writes write) {

    }
}
