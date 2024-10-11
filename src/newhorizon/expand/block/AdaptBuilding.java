package newhorizon.expand.block;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import newhorizon.expand.block.graph.GraphEntity;
import newhorizon.expand.block.module.XenModule;

public class AdaptBuilding extends Building {
    public GraphEntity<AdaptBuilding> graph;
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
        return (building instanceof AdaptBuilding && ((AdaptBuilding)building).getBlock() != null && ((AdaptBuilding)building).getBlock().hasXen);
    }

    public boolean checkXenModule(Building building){
        return (building instanceof AdaptBuilding && ((AdaptBuilding)building).getBlock() != null && ((AdaptBuilding)building).getBlock().hasXen);
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

        if (getBlock().isGraphEntity) {
            for (AdaptBuilding build : graph.allBuildings) {
                Draw.color(Pal.accent);
                Fill.square(build.x, build.y, 2, 45);
                Draw.reset();
            }
        }
    }

    @Override
    public void created() {
        super.created();

        if (getBlock().isGraphEntity){
            graph = new GraphEntity<>();
            graph.addBuild(this);
        }
        if (hasXen()) {
            xen = new XenModule();
            xen.graph.addBuild(this);
        }
    }

    @Override
    public void onProximityAdded() {
        super.onProximityAdded();

        if (getBlock().isGraphEntity){
            for (Building other : proximity) {
                if (other instanceof AdaptBuilding && ((AdaptBuilding)other).getBlock().isGraphEntity){
                    graph.mergeGraph(((AdaptBuilding)other).graph);
                }
            }
        }

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
        if (getBlock().isGraphEntity){
            graph.remove(this, (building) -> true);
        }
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