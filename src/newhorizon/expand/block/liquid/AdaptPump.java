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
import static newhorizon.util.graphic.SpriteUtil.ATLAS_INDEX_4_4;
import static newhorizon.util.graphic.SpriteUtil.orthogonalPos;

public class AdaptPump extends Pump {
    public TextureRegion[] splits;

    public AdaptPump(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        if (size == 1) {
            splits = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 1, ATLAS_INDEX_4_4);
        } else {
            splits = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 1);
        }
    }

    public class AdaptPumpBuild extends PumpBuild {
        public boolean[] drawLink = new boolean[8];
        public int[] drawIdx = new int[4];
        public int drawIndex = 0;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            if (size == 1) {
                drawIndex = 0;
                for (int i = 0; i < orthogonalPos.length; i++) {
                    Point2 p = orthogonalPos[i];
                    if (world.build(tileX() + p.x, tileY() + p.y) instanceof AdaptPumpBuild) {
                        drawIndex += 1 << i;
                    }
                }
            } else {
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
        }

        @Override
        public void draw() {
            if (isPayload()) {
                Draw.rect(region, x, y);
            } else {
                if (size == 1) {
                    Draw.rect(splits[drawIndex], x, y);
                } else if (size == 2) {
                    Draw.rect(splits[drawIdx[0]], x + 4, y + 4);
                    Draw.rect(splits[drawIdx[1] + 4], x - 4, y + 4);
                    Draw.rect(splits[drawIdx[2] + 8], x - 4, y - 4);
                    Draw.rect(splits[drawIdx[3] + 12], x + 4, y - 4);
                }
            }

            if (liquids != null && liquidDrop != null) {
                Drawf.liquid(liquidRegion, x, y, liquids.get(liquidDrop) / liquidCapacity, liquidDrop.color);
            }
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
