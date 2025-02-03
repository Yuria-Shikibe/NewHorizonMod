package newhorizon.expand.block.distribution.transport;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Eachable;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import newhorizon.NewHorizon;
import newhorizon.expand.block.distribution.transport.item.AdaptConveyor;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.world;

//used for some sprites control.
public class LogisticsBlock {
    public static TextureRegion[] upperRegion, overlayRegion, edgeRegion;
    public static TextureRegion itemsRegion;

    public static void load(){
        upperRegion = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("logistics-upper")), 32, 32, 1, SpriteUtil.ATLAS_INDEX_4_4);
        overlayRegion = new TextureRegion[5];
        edgeRegion = new TextureRegion[2];
        itemsRegion = Core.atlas.find(NewHorizon.name("logistics-item"));
        for (int i = 0; i < overlayRegion.length; i++){overlayRegion[i] = Core.atlas.find(NewHorizon.name("logistics-overlay-"+i));}
        for (int i = 0; i < edgeRegion.length; i++){edgeRegion[i] = Core.atlas.find(NewHorizon.name("logistics-edge-"+(i+1)));}
    }

    public static void drawPlan(BuildPlan plan, Eachable<BuildPlan> list, int overlayIndex){
        Draw.rect(upperRegion[0], plan.drawx(), plan.drawy());
        Draw.rect(edgeRegion[0], plan.drawx(), plan.drawy());
        Draw.rect(edgeRegion[0], plan.drawx(), plan.drawy(), 90);
        Draw.rect(edgeRegion[1], plan.drawx(), plan.drawy(), 180);
        Draw.rect(edgeRegion[1], plan.drawx(), plan.drawy(), 270);
        Draw.rect(overlayRegion[overlayIndex], plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public static void draw(Building building, AdaptConveyor cBlock, int upperIndex, int overlayIndex, Item item){
        Draw.blend(Blending.additive);
        Draw.color(building.team.color, Pal.gray, 0.35f);
        Draw.z(Layer.block - 0.25f);
        Draw.rect(cBlock.pulseRegions[3 + cBlock.pulseFrame() * 5], building.x, building.y);
        Draw.blend();

        for (int i = 0; i < 4; i++){
            Building b = world.build(building.tileX() + Geometry.d4x(i), building.tileY() + Geometry.d4y(i));
            if (b instanceof AdaptConveyor.AdaptConveyorBuild &&
                    (!((AdaptConveyor.AdaptConveyorBuild)b).armored || (((AdaptConveyor.AdaptConveyorBuild)b).armored && (b.rotation == i || b.rotation == (i + 2) % 4)))){
                Draw.color(building.team.color, Color.white, 0.65f);
                Draw.z(Layer.block - 0.2f);
                if (b.rotation != (i+2)%4){
                    Draw.rect(cBlock.arrowRegions[cBlock.conveyorFrame()], building.x, building.y, i * 90);
                }else {
                    Draw.rect(cBlock.arrowRegions[cBlock.conveyorFrame() + 16], building.x, building.y, i * 90 + 180);
                }
            }else {
                Draw.color();
                Draw.z(Layer.block);
                if (i <= 1){
                    Draw.rect(edgeRegion[0], building.x, building.y, i * 90);
                }else {
                    Draw.rect(edgeRegion[1], building.x, building.y, i * 90);
                }
            }
        }

        Draw.z(Layer.block - 0.1f);
        Drawf.shadow(building.x, building.y, 12, 1.5f);

        Draw.color();
        Draw.z(Layer.block);

        Draw.rect(upperRegion[upperIndex], building.x, building.y);

        if(item != null){
            Draw.color(item.color);
            Draw.rect(itemsRegion, building.x, building.y);
            Draw.color();
        }

        Draw.rect(overlayRegion[overlayIndex], building.x, building.y, building.rotdeg());
    }

    public static int proximityUpperIndex(Building building){
        int drawIndex = 0;
        if (check(building, building.tile.x, building.tile.y + 1)) drawIndex += 1;
        if (check(building, building.tile.x + 1, building.tile.y)) drawIndex += 2;
        if (check(building, building.tile.x, building.tile.y - 1)) drawIndex += 4;
        if (check(building, building.tile.x - 1, building.tile.y)) drawIndex += 8;
        return drawIndex;
    }

    public static boolean check(Building building, int x, int y){
        Building other = Vars.world.build(x, y);
        if (building instanceof LogisticBuild && other instanceof LogisticBuild){
            return true;
            //return ((LogisticBuild)building).canSend(other) || ((LogisticBuild)other).canReceive(building);
        }
        return false;
    }
}
