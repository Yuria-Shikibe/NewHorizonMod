package newhorizon.expand.block.floodv2;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NewHorizon;
import newhorizon.content.NHItems;
import newhorizon.util.graphic.SpriteUtil;

public class SynthWall extends Wall {
    public TextureRegion[] atlas;
    public TextureRegion[] base;
    public int altitude;

    public SynthWall(String name, int altitude) {
        super(name);
        this.altitude = altitude;

        requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
    }

    @Override
    public void load() {
        super.load();
        region = Core.atlas.find(NewHorizon.name("synth-wall"));
        atlas = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("synth-wall-atlas")), 48, 48, 0, SpriteUtil.ATLAS_INDEX_4_4);
        base = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("synth-wall-base")), 32, 32);
    }

    public class SynthWallBuilding extends WallBuild {
        public int drawIndex = 0;
        public int baseIndex = 0;

        @Override
        public void created() {
            super.created();
            int splitTileSize = 2;
            int splitVariants = 12;

            int tx = tile.x / splitTileSize * splitTileSize;
            int ty = tile.y / splitTileSize * splitTileSize;


            int index = Mathf.randomSeed(Point2.pack(tx, ty), 0, splitVariants - 1);
            int ix = index * splitTileSize + tile.x - tx;
            int iy = splitTileSize - (tile.y - ty) - 1;

            baseIndex = ix + iy * splitTileSize * splitVariants;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            drawIndex = 0;
            if (check(tile.x, tile.y + 1)) drawIndex += 1;
            if (check(tile.x + 1, tile.y)) drawIndex += 2;
            if (check(tile.x, tile.y - 1)) drawIndex += 4;
            if (check(tile.x - 1, tile.y)) drawIndex += 8;
        }

        public boolean check(int x, int y){
            Building building = Vars.world.build(x, y);
            return (building instanceof SynthWallBuilding && ((SynthWall)building.block).altitude >= altitude);
        }

        @Override
        public void draw() {
            Draw.z(Layer.block + altitude * 0.001f);
            Draw.mixcol(Color.white, altitude * 0.05f);
            Draw.rect(base[baseIndex], x, y);
            Draw.rect(atlas[drawIndex], x, y);
        }
    }
}
