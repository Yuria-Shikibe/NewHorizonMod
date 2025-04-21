package newhorizon.expand.block.payload;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.util.Eachable;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadVoid;
import mindustry.world.blocks.units.UnitAssembler;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.util.graphic.SpriteUtil;

public class ModuleVoid extends PayloadVoid {
    public TextureRegion[] rotRegions;

    public ModuleVoid(String name) {
        super(name);
        size = 2;
    }

    @Override
    public void load() {
        super.load();
        rotRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-rot"), 64, 64, 1);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    public class ModuleVoidBuild extends PayloadVoidBuild {

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.reset();
                    Draw.rect(rotRegions[i], x, y);
                    ModuleConveyor.prepareAlpha();
                    ModuleConveyor.prepareColor(team);
                    ModuleConveyor.drawArrowIn(x, y, i * 90 + 180);
                }
            }

            Draw.z(Layer.blockOver);
            drawPayload();
            Draw.reset();
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return super.acceptPayload(source, payload) && payload.content() instanceof ModulePayload;
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            Fx.payloadDeposit.at(payload.x(), payload.y(), payload.angleTo(this), new UnitAssembler.YeetData(new Vec2(x, y), payload.content()));
        }

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return false;
        }
    }
}
