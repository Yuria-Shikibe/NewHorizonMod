package newhorizon.expand.block.production.drill;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import newhorizon.expand.BasicMultiBlock;
import newhorizon.expand.block.environment.OreCluster;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.indexer;
import static mindustry.Vars.tilesize;

public class OreCollector extends BasicMultiBlock {
    public static Seq<Tile> clusters = new Seq<>();
    public int tier;
    public float drillTime = 300;
    public float warmupSpeed = 0.015f;
    public @Nullable Item blockedItem;
    public @Nullable Seq<Item> blockedItems;
    public float liquidBoostIntensity = 1.6f;

    public int collectOffset = 5;
    public int collectSize = 7;

    public OreCollector(String name) {
        super(name);

        solid = true;
        update = true;
        rotate = true;
        hasItems = true;
        hasLiquids = true;
        liquidCapacity = 20f;

        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;

        group = BlockGroup.drills;
        flags = EnumSet.of(BlockFlag.drill);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        x *= tilesize;
        y *= tilesize;
        x += (int) offset;
        y += (int) offset;

        Rect rect = getRect(Tmp.r1, x, y, rotation);
        Color c = valid ? Pal.accent : Pal.remove;
        Drawf.dashRect(c, rect);

        clusters.each(tile -> {
            float z = Draw.z();
            Draw.z(Layer.effect);
            Draw.color(c);
            Draw.alpha(0.4f);

            Fill.rect(tile.worldx(), tile.worldy(), tilesize, tilesize);

            Draw.z(z);
            Draw.reset();
        });
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        //overlapping construction areas not allowed; grow by a tiny amount so edges can't overlap either.
        Rect rect = getRect(Tmp.r1, tile.worldx() + offset, tile.worldy() + offset, rotation).grow(-0.1f);
        getOreClusters(clusters, tile.x, tile.y, rotation);
        boolean hasOre = !clusters.isEmpty();
        boolean overlap = indexer.getFlagged(team, BlockFlag.drill).contains(b -> checkOverlap(rect, Tmp.r2, b.block, b));
        boolean planOverlap = team.data().getBuildings(ConstructBlock.get(size)).contains(b -> checkOverlap(rect, Tmp.r2, ((ConstructBlock.ConstructBuild)b).current, b));
        return super.canPlaceOn(tile, team, rotation) && hasOre && !overlap && !planOverlap;
    }

    public boolean checkOverlap(Rect rect1, Rect rect2, Block block, Building building) {
        if (building == null) return false;
        if (!(block instanceof OreCollector)) return false;
        return ((OreCollector)block).getRect(rect2, building.x, building.y, building.rotation).overlaps(rect1);
    }

    public Rect getRect(Rect rect, float x, float y, int rotation){
        rect.setCentered(x, y, collectSize * tilesize);
        float len = tilesize * (collectSize + size)/2f;

        rect.x += Geometry.d4x(rotation) * len;
        rect.y += Geometry.d4y(rotation) * len;

        return rect;
    }

    public void getOreClusters(Seq<Tile> out, int x, int y, int rotation) {
        out.clear();

        int cx = x + Geometry.d4x(rotation) * collectOffset;
        int cy = y + Geometry.d4y(rotation) * collectOffset;
        int offset = collectSize/2;
        for (int tx = cx - offset; tx <= cx + offset; tx++) {
            for (int ty = cy - offset; ty <= cy + offset; ty++) {
                Tile tile = Vars.world.tile(tx, ty);
                Building building = tile.build;
                if (building != null && building.block instanceof OreCluster) out.add(tile);
            }
        }
    }

    public class OreCollectorBuilding extends BasicMultiBuilding {
        public Seq<Tile> oreClusters = new Seq<>();
        public float warmup;

        @Override
        public void created() {
            super.created();
            getOreClusters(oreClusters, tileX(), tileY(), rotation);
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y, drawrot());
            Lines.rect(getRect(Tmp.r1, x, y, rotation));

            float offset1 = collectOffset * tilesize - tilesize / 2f;
            float offset2 = (collectSize - 1) * tilesize * MathUtil.timeValue(0.5f, -0.5f, 3f);
            float len1 = collectSize * tilesize / 2f;
            float len2 = size * tilesize / 2f;

            Draw.z(Layer.buildBeam);
            Draw.color(team.color);

            Tmp.v1.setZero().add(offset1, len1).add(offset2, 0).rotate(rotdeg()).add(x, y);
            Tmp.v2.setZero().add(offset1, -len1).add(offset2, 0).rotate(rotdeg()).add(x, y);
            Tmp.v3.setZero().add(len2 - 2f, len2 - 2f).rotate(rotdeg()).add(x, y);
            Tmp.v4.setZero().add(len2 - 2f, len2 + 2f).rotate(rotdeg()).add(x, y);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);
            Tmp.v3.setZero().add(len2 - 2f, -len2 - 2f).rotate(rotdeg()).add(x, y);
            Tmp.v4.setZero().add(len2 - 2f, -len2 + 2f).rotate(rotdeg()).add(x, y);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);

            Tmp.v5.setZero().add(offset1, -len1).add(offset2 + tilesize, 0).rotate(rotdeg()).add(x, y);
            Tmp.v6.setZero().add(offset1, len1).add(offset2 + tilesize, 0).rotate(rotdeg()).add(x, y);

            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v5.x, Tmp.v5.y, Tmp.v6.x, Tmp.v6.y);

            Draw.z(Layer.effect);
            Draw.alpha(0.5f);

            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v5.x, Tmp.v5.y, Tmp.v6.x, Tmp.v6.y);

            oreClusters.each(tile -> Fill.rect(tile.worldx(), tile.worldy(), tilesize, tilesize));

            Draw.reset();
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (timer(0, 300)) getOreClusters(oreClusters, tileX(), tileY(), rotation);
        }
    }
}
