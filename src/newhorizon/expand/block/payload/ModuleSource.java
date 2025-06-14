package newhorizon.expand.block.payload;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.PayloadSource;
import newhorizon.expand.block.inner.ModulePayload;
import newhorizon.util.graphic.SpriteUtil;
import newhorizon.util.ui.ContentSelectionTable;

import static mindustry.Vars.content;

public class ModuleSource extends PayloadSource {
    public TextureRegion[] rotRegions;
    public TextureRegion baseRegion;

    public ModuleSource(String name) {
        super(name);
        size = 2;
    }

    @Override
    public void load() {
        super.load();
        rotRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-rot"), 64, 64, 1);
        baseRegion = Core.atlas.find(name + "-base");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        Draw.rect(region, plan.drawx(), plan.drawy());
    }

    @Override
    public boolean canProduce(Block b) {
        return b instanceof ModulePayload;
    }

    @Override
    public boolean canProduce(UnitType t) {
        return false;
    }


    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public class ModuleSourceBuild extends PayloadSourceBuild {
        @Override
        public void buildConfiguration(Table table) {
            ContentSelectionTable.buildModuleTable(ModuleSource.this, table,
                    content.blocks().select(b -> b instanceof ModulePayload),
                    () -> (Block) config(), this::configure);
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            ModuleConveyor.prepareColor(team);
            ModuleConveyor.drawArrowIn(x, y, rotdeg());
            ModuleConveyor.drawArrowOut(x, y, rotdeg());
            Draw.reset();
            Draw.rect(rotRegions[rotation], x, y);
            Draw.scl(scl);
            drawPayload();
            Draw.reset();
        }
    }
}
