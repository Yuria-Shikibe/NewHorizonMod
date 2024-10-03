package newhorizon.expand.block;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import newhorizon.expand.block.graph.GraphEntity;
import newhorizon.expand.block.module.XenModule;
import newhorizon.util.graphic.DrawUtil;

public class AdaptBuilding extends Building {
    public GraphEntity<AdaptBuilding> graph;
    public Seq<AdaptBuilding> proximityNH;
    public XenModule xen;

    /** get the adapt block. */
    public AdaptBlock getBlock(){
        return (block instanceof AdaptBlock ? (AdaptBlock) block: null);
    }

    public String getXenText(){
        return xen.getXenText();
    }

    public Color getXenColor(){
        return xen.getXenColor();
    }

    public Color getXenSmoothColor(){
        return xen.getXenSmoothColor();
    }

    public float getXenFrac(){
        return xen.getXenFrac();
    }

    public boolean checkXenModule(int x, int y){
        Building building = Vars.world.build(x, y);
        if (building instanceof AdaptBuilding){
            AdaptBuilding b = (AdaptBuilding) building;
            return b.getBlock() != null && b.getBlock().hasXen;
        }
        return false;
    }

    public boolean checkXenModule(Building building){
        return (building instanceof AdaptBuilding b && b.getBlock() != null && b.getBlock().hasXen);
    }

    public boolean hasXen(){
        return getBlock() != null && getBlock().hasXen;
    }

    public float xenArea(){
        if (hasXen()){
            return getBlock().xenArea;
        }
        return 0;
    }

    @Override
    public void drawSelect() {
        /*
        if (hasXen()) {
            for (AdaptBuilding build : xen.graph.allBuildings) {
                Draw.color(Pal.accent);
                Fill.square(build.x, build.y, 2, 45);
                Draw.reset();
            }
            String xenInfo = "Graph ID: " + xen.graph.graphID + "\nGraph Area: " + xen.graph.area + "\nGraph Height: " + xen.graph.height + "\nXen Level: " + xen.getXenText();
            DrawUtil.drawText(xenInfo, x, y - 8);
        }

         */
    }

    @Override
    public void created() {
        super.created();

        if (hasXen()) {
            xen = new XenModule();
            xen.graph.addBuild(this);
        }
    }

    @Override
    public void onProximityAdded() {
        super.onProximityAdded();

        if (hasXen()) {
            for (Building other : proximity) {
                if (checkXenModule(other)){
                    AdaptBuilding b = (AdaptBuilding)other;
                    xen.graph.mergeGraph(b.xen.graph);
                }
            }
        }
    }

    @Override
    public void onProximityRemoved() {
        super.onProximityRemoved();
        if (hasXen()) {
            xen.graph.remove(this);
        }
    }

    @Override
    public void onProximityUpdate() {
        super.onProximityUpdate();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();

    }
}
