package newhorizon.expand.block.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.type.Liquid;
import mindustry.world.Edges;
import mindustry.world.blocks.production.Pump;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.world;

public class AdaptPump extends Pump {
    public TextureRegion[] splits;
    public TextureRegion topRegion;
    public AdaptPump(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        splits = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32);
        topRegion = Core.atlas.find(name + "-top");
    }

    public class AdaptPumpBuild extends PumpBuild{
        public boolean[] drawLink = new boolean[8];
        public int[] drawIdx = new int[4];

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            for (int i = 0; i < Edges.getEdges(2).length; i++) {
                Point2 p = Edges.getEdges(2)[i];
                Building b = world.build(tileX() + p.x, tileY() + p.y);
                drawLink[i] = b instanceof AdaptPumpBuild;
            }

            drawIdx[0] = Mathf.num(drawLink[1]) + Mathf.num(drawLink[2]) * 2;
            drawIdx[1] = Mathf.num(drawLink[3]) + Mathf.num(drawLink[4]) * 2;
            drawIdx[2] = Mathf.num(drawLink[5]) + Mathf.num(drawLink[6]) * 2;
            drawIdx[3] = Mathf.num(drawLink[7]) + Mathf.num(drawLink[0]) * 2;
        }

        @Override
        public void draw() {
            Draw.rect(splits[drawIdx[0]], x + 4, y + 4);
            Draw.rect(splits[drawIdx[1] + 4], x - 4, y + 4);
            Draw.rect(splits[drawIdx[2] + 8], x - 4, y - 4);
            Draw.rect(splits[drawIdx[3] + 12], x + 4, y - 4);

            Draw.rect(topRegion, x, y);
            Drawf.liquid(liquidRegion, x, y, liquids.get(liquidDrop) / liquidCapacity, liquidDrop.color);
        }

        @Override
        public void drawStatus() {
            if (drawIdx[3] == 0) super.drawStatus();
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return super.acceptLiquid(source, liquid) || (isValidPump(source) && liquidDrop == liquid);
        }

        public boolean isValidPump(Building e) {
            return e instanceof AdaptPumpBuild;
        }
    }
}
