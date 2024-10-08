package newhorizon.expand.block.energy;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;

public class XenConduit extends AdaptBlock {
    public TextureRegion[] conduitRegion;
    public TextureRegion baseRegion, lightRegion;
    public XenConduit(String name) {
        super(name);

        hasXen = true;
        xenArea = 10f;
        update = true;
        configurable = true;
    }

    @Override
    public void load() {
        super.load();
        conduitRegion = new TextureRegion[16];
        for (int i = 0; i < 16; i++){
            conduitRegion[i] = Core.atlas.find(name + "-" + i);
        }
        baseRegion = Core.atlas.find(name + "-base");
        lightRegion = Core.atlas.find(name + "-light");
    }

    public class XenConduitBuild extends AdaptBuilding {
        private int index = 0;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            getRegion();
        }

        @Override
        public void created() {
            super.created();
            getRegion();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
        }

        public void getRegion() {
            index = 0;
            if (checkXenModule(tile.x, tile.y + 1)) index += 1;
            if (checkXenModule(tile.x + 1, tile.y)) index += 2;
            if (checkXenModule(tile.x, tile.y - 1)) index += 4;
            if (checkXenModule(tile.x - 1, tile.y)) index += 8;
        }

        @Override
        public void draw() {
            Draw.z(Layer.blockUnder);
            Draw.rect(baseRegion, x, y);
            Draw.color(getXenSmoothColor());
            Draw.z(NHContent.XEN_LAYER);
            Draw.rect(lightRegion, x, y);
            Draw.z(Layer.block);
            Draw.color();
            Draw.rect(conduitRegion[index], x, y);
        }

        public void buildConfiguration(Table table){
            table.button(Icon.up, Styles.defaulti, () -> xen.graph.addXen(100, 300)).size(48f);
            table.button(Icon.down, Styles.defaulti, () -> xen.graph.removeXen(100, 0)).size(48f);
        }
    }
}
