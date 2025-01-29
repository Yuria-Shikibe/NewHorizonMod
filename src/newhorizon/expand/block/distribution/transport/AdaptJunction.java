package newhorizon.expand.block.distribution.transport;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.distribution.Junction;
import newhorizon.NewHorizon;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class AdaptJunction extends Junction {
    public TextureRegion upperRegion, edgeRegion1, edgeRegion2;
    public AdaptConveyor cBlock;

    public AdaptJunction(String name, AdaptConveyor conveyorBlock) {
        super(name);

        this.cBlock = conveyorBlock;
        placeableLiquid = true;
        drawTeamOverlay = false;
    }


    public int conveyorFrame(){return cBlock.conveyorFrame();}

    public int pulseFrame(){return cBlock.pulseFrame();}

    @Override
    public void load() {
        super.load();
        upperRegion = Core.atlas.find(name + "-upper");
        edgeRegion1 = Core.atlas.find(NewHorizon.name("distribution-edge1"));
        edgeRegion2 = Core.atlas.find(NewHorizon.name("distribution-edge2"));
    }

    public class AdaptJunctionBuild extends JunctionBuild {
        @Override
        public void draw() {
            Draw.blend(Blending.additive);
            Draw.color(team.color, Pal.gray, 0.35f);
            Draw.z(Layer.block - 0.25f);
            Draw.rect(cBlock.pulseRegions[3 + pulseFrame() * 5], x, y);
            Draw.blend();

            for (int i = 0; i < 4; i++){
                Building b = world.build(tileX() + Geometry.d4x(i), tileY() + Geometry.d4y(i));
                if (b instanceof AdaptConveyor.AdaptConveyorBuild){
                    Draw.color(team.color, Color.white, 0.65f);
                    Draw.z(Layer.block - 0.2f);
                    if (b.rotation != (i+2)%4){
                        Draw.rect(cBlock.arrowRegions[conveyorFrame()], x, y, i * 90);
                    }else {
                        Draw.rect(cBlock.arrowRegions[conveyorFrame() + 16], x, y, i * 90 + 180);
                    }
                }else {
                    Draw.color();
                    Draw.z(Layer.block);
                    if (i <= 1){
                        Draw.rect(edgeRegion1, x, y, i * 90);
                    }else {
                        Draw.rect(edgeRegion2, x, y, i * 90);
                    }
                }
            }

            Draw.z(Layer.block - 0.1f);
            Drawf.shadow(x, y, 12, 1.5f);

            Draw.color();
            Draw.z(Layer.block);
            Draw.rect(upperRegion, x, y);
        }
    }
}
