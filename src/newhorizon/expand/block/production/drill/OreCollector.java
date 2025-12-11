package newhorizon.expand.block.production.drill;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.struct.EnumSet;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
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
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHContent;
import newhorizon.expand.BasicMultiBlock;
import newhorizon.util.func.MathUtil;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.*;

public class OreCollector extends BasicMultiBlock {
    public static Seq<Tile> tmpClusters = new Seq<>();
    public static ObjectFloatMap<Item> returnCount = new ObjectFloatMap<>();

    public TextureRegion[] innerRegions, outerRegions;
    public TextureRegion baseRegion;

    public int tier = 5;
    public float mineTime = 120f;
    public float drillTime = 30;
    public float warmupSpeed = 0.075f;
    public @Nullable Item blockedItem;
    public @Nullable Seq<Item> blockedItems;
    public float optionalBoostIntensity = 2f;
    public float hardnessDrillMultiplier = 10f;

    public ObjectFloatMap<Item> drillMultipliers = new ObjectFloatMap<>();

    public float baseDrillCount = 1f;

    public int collectOffset = 5;
    public int collectSize = 7;

    public OreCollector(String name) {
        super(name);

        solid = true;
        update = true;
        rotate = true;
        hasItems = true;
        hasLiquids = true;

        clipSize = 288;

        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;

        group = BlockGroup.drills;
        flags = EnumSet.of(BlockFlag.drill);
    }

    @Override
    public void load() {
        super.load();
        innerRegions = new TextureRegion[4];
        outerRegions = new TextureRegion[4];

        baseRegion = Core.atlas.find(name + "-base");

        for (int i = 0; i < 4; i++){
            innerRegions[i] = Core.atlas.find(name + "-inner-" + i);
            outerRegions[i] = Core.atlas.find(name + "-outer-" + i);
        }
    }

    @Override
    public TextureRegion getPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        return baseRegion;
    }

    @Override
    public void setBars() {
        super.setBars();
    }

    @Override
    public boolean outputsItems(){
        return true;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        getOreOutput(tmpClusters, x, y, rotation);

        int i = 0;
        for (var entry: returnCount.entries()){
            Tmp.v1.setZero().add(collectOffset, 0).rotate(rotation * 90).add(0, (float) (collectSize - size) / 2).add(x, y);
            if (rotation == 3) Tmp.v1.set(x, y);
            drawPlaceText("[white]" + entry.key.emoji() + "[] " + entry.key.localizedName + " " +
                    Strings.autoFixed(entry.value / (mineTime / Time.toSeconds), 2) + StatUnit.perSecond.localized(), (int) Tmp.v1.x, (int) (Tmp.v1.y) + i, valid);
            i++;
        }

        x *= tilesize;
        y *= tilesize;
        x += (int) offset;
        y += (int) offset;

        Rect rect = getRect(Tmp.r1, x, y, rotation);
        Color c = valid ? Pal.accent : Pal.remove;
        Drawf.dashRect(c, rect);
        Draw.color(Pal.accent);
        Draw.alpha(0.5f);

        tmpClusters.each(tile -> Fill.square(tile.worldx(), tile.worldy(), tilesize / 2f));

        Draw.reset();
    }

    public float getDrillTime(Item item){
        return (drillTime + hardnessDrillMultiplier * item.hardness) / drillMultipliers.get(item, 1f);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        //overlapping construction areas not allowed; grow by a tiny amount so edges can't overlap either.
        Rect rect = getRect(Tmp.r1, tile.worldx() + offset, tile.worldy() + offset, rotation).grow(-0.1f);
        getOreClusters(tmpClusters, tile.x, tile.y, rotation);
        boolean hasOre = !tmpClusters.isEmpty();
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
                if (tile != null && tile.block() != null && tile.block().itemDrop != null) out.add(tile);
            }
        }
    }

    public void getOreOutput(Seq<Tile> out, int x, int y, int rotation){
        getOreClusters(out, x, y, rotation);
        returnCount.clear();
        out.each(tile -> {
            if (tile.block().itemDrop == null) return;
            Item drops = tile.block().itemDrop;
            if (drops != null && drops.hardness <= tier && (blockedItems == null || !blockedItems.contains(drops))){
                returnCount.increment(drops, 0, 60 / getDrillTime(drops) * tile.block().attributes.get(NHContent.density));
            }
        });
    }

    public void drawBlockBase(float x, float y, int rotation){
        Draw.rect(innerRegions[rotation], x, y);
        Tmp.v1.set(4, 20).rotate(rotation * 90);
        Tmp.v2.set(4, -20).rotate(rotation * 90);
        Draw.rect(outerRegions[(rotation + 3) % 4], x + Tmp.v1.x, y + Tmp.v1.y);
        Draw.rect(outerRegions[(rotation + 1) % 4], x + Tmp.v2.x, y + Tmp.v2.y);
    }

    public class OreCollectorBuilding extends BasicMultiBuilding {
        public Seq<Tile> oreClusters = new Seq<>();
        public float progress;
        public float warmup;

        @Override
        public void created() {
            super.created();
            getOreClusters(oreClusters, tileX(), tileY(), rotation);
        }

        @Override
        public void draw() {
            drawBlockBase(x, y, rotation) ;
            if (warmup > 0.01f) drawScanner();
        }

        private void drawScanner() {
            float len1 = collectSize * tilesize / 2f;
            float len2 = size * tilesize / 2f;
            float len3 = tilesize * collectSize;
            float len4 = tilesize;

            float outlineAlpha = warmup * MathUtil.timeValue(0.65f, 0.8f, 3f);
            float innerAlpha = warmup * MathUtil.timeValue(0.20f, 0.25f, 3f);

            float ang = DrawFunc.rotator_90(DrawFunc.cycle(Time.time / 4f, 0, 45), 0.15f);
            float scl = MathUtil.timeValue(0.75f, 1.25f, 3f);

            Draw.z(Layer.blockOver);

            Draw.color(team.color);
            Tmp.c1.set(team.color).lerp(Color.white, 0.8f).a(innerAlpha);
            float shift1 = 2f;
            float shift2 = MathUtil.timeValue(6f, 8f, 3f);

            Draw.alpha(innerAlpha);
            Rect rect = getRect(Tmp.r1, x, y, rotation);
            Fill.rect(rect);

            Tmp.v1.setZero().add(len2 - 1f, len1 - 1f).rotate(rotdeg()).add(x, y);
            Tmp.v2.setZero().add(len2 - 1f, -len1 + 1f).rotate(rotdeg()).add(x, y);
            Tmp.v3.setZero().add(len2 - len4, -len1 + len4).rotate(rotdeg()).add(x, y);
            Tmp.v4.setZero().add(len2 - len4, len1 - len4).rotate(rotdeg()).add(x, y);

            Lines.stroke(2f);

            Draw.alpha(outlineAlpha);
            Draw.z(Layer.effect);

            Fill.circle(Tmp.v1.x, Tmp.v1.y, 1.25f);
            Fill.circle(Tmp.v2.x, Tmp.v2.y, 1.25f);

            Fill.circle(Tmp.v3.x, Tmp.v3.y, 1.25f);
            Fill.circle(Tmp.v4.x, Tmp.v4.y, 1.25f);

            Tmp.v1.setZero().add(len2 - 1.5f, len1 - 1.5f).rotate(rotdeg()).add(x, y);
            Tmp.v2.setZero().add(len2 - 1.5f, -len1 + 1.5f).rotate(rotdeg()).add(x, y);

            Lines.line(Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y);
            Lines.line(Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);
            Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v4.x, Tmp.v4.y);

            Tmp.v1.setZero().add(len2, len1 - 2f).rotate(rotdeg()).add(x, y);
            Tmp.v2.setZero().add(len2, -len1 + 2f).rotate(rotdeg()).add(x, y);

            Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);

            Tmp.v1.setZero().add(len2, len1).rotate(rotdeg()).add(x, y);
            Tmp.v2.setZero().add(len2, -len1).rotate(rotdeg()).add(x, y);

            Draw.alpha(innerAlpha);
            Draw.z(Layer.blockOver);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);

            Tmp.v1.setZero().add(len2, len1).rotate(rotdeg()).add(x, y);
            Tmp.v2.setZero().add(len2 + len3, len1).rotate(rotdeg()).add(x, y);
            Tmp.v3.setZero().add(len2 + len3 - shift1, len1 - shift1).rotate(rotdeg()).add(x, y);
            Tmp.v4.setZero().add(len2, len1 - shift1).rotate(rotdeg()).add(x, y);
            Tmp.v5.setZero().add(len2, len1 - shift2).rotate(rotdeg()).add(x, y);
            Tmp.v6.setZero().add(len2 + len3 - shift2, len1 - shift2).rotate(rotdeg()).add(x, y);

            Draw.alpha(outlineAlpha);
            Draw.z(Layer.effect);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);
            Draw.alpha(innerAlpha);
            Draw.z(Layer.blockOver);
            Fill.quad(
                    Tmp.v3.x, Tmp.v3.y, Tmp.c1.toFloatBits(), Tmp.v4.x, Tmp.v4.y, Tmp.c1.toFloatBits(),
                    Tmp.v5.x, Tmp.v5.y, 0, Tmp.v6.x, Tmp.v6.y, 0
            );

            Tmp.v1.setZero().add(len2 + len3, -len1).rotate(rotdeg()).add(x, y);
            Tmp.v4.setZero().add(len2 + len3 - shift1, -len1 + shift1).rotate(rotdeg()).add(x, y);
            Tmp.v5.setZero().add(len2 + len3 - shift2, -len1 + shift2).rotate(rotdeg()).add(x, y);

            Draw.alpha(outlineAlpha);
            Draw.z(Layer.effect);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);
            Draw.alpha(innerAlpha);
            Draw.z(Layer.blockOver);
            Fill.quad(
                    Tmp.v3.x, Tmp.v3.y, Tmp.c1.toFloatBits(), Tmp.v4.x, Tmp.v4.y, Tmp.c1.toFloatBits(),
                    Tmp.v5.x, Tmp.v5.y, 0, Tmp.v6.x, Tmp.v6.y, 0
            );

            Tmp.v2.setZero().add(len2, -len1).rotate(rotdeg()).add(x, y);
            Tmp.v3.setZero().add(len2, -len1 + shift1).rotate(rotdeg()).add(x, y);
            Tmp.v6.setZero().add(len2, -len1 + shift2).rotate(rotdeg()).add(x, y);

            Draw.alpha(outlineAlpha);
            Draw.z(Layer.effect);
            Fill.quad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);
            Draw.alpha(innerAlpha);
            Draw.z(Layer.blockOver);
            Fill.quad(
                    Tmp.v3.x, Tmp.v3.y, Tmp.c1.toFloatBits(), Tmp.v4.x, Tmp.v4.y, Tmp.c1.toFloatBits(),
                    Tmp.v5.x, Tmp.v5.y, 0, Tmp.v6.x, Tmp.v6.y, 0
            );

            Draw.alpha(warmup);
            Lines.stroke(0.5f);

            oreClusters.each(tile -> {
                if (tile.block().itemDrop != null) {
                    Draw.color(tile.block().itemDrop.color);
                    Draw.z(Layer.buildBeam);
                    Fill.rect(tile.worldx(), tile.worldy(), tilesize * scl, tilesize * scl, ang);
                    Draw.z(Layer.effect);

                    for (int i = 0; i < 4; i++){
                        Tmp.v1.set(2 * scl, 6 * scl).rotate(ang + i * 90).add(tile.worldx(), tile.worldy());
                        Tmp.v2.set(6 * scl, 6 * scl).rotate(ang + i * 90).add(tile.worldx(), tile.worldy());
                        Tmp.v3.set(6 * scl, 2 * scl).rotate(ang + i * 90).add(tile.worldx(), tile.worldy());

                        Lines.beginLine();
                        Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                        Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                        Lines.linePoint(Tmp.v3.x, Tmp.v3.y);
                        Lines.endLine();

                        Tmp.v1.set(2 * scl, 4 * scl).rotate(ang + i * 90).add(tile.worldx(), tile.worldy());
                        Tmp.v2.set(4 * scl, 4 * scl).rotate(ang + i * 90).add(tile.worldx(), tile.worldy());
                        Tmp.v3.set(4 * scl, 2 * scl).rotate(ang + i * 90).add(tile.worldx(), tile.worldy());

                        Lines.beginLine();
                        Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                        Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                        Lines.linePoint(Tmp.v3.x, Tmp.v3.y);
                        Lines.endLine();
                    }
                }
            });

            Draw.z(Layer.effect);
            Draw.color(Tmp.c1.set(team.color).lerp(Color.white, 0.5f));

            for (int tx = 0; tx < 8; tx++) {
                for (int ty = 0; ty < 8; ty++) {
                    float rx = tx * (60f / 8f) + x + collectOffset * tilesize * Geometry.d4x(rotation) - 26.5f;
                    float ry = ty * (60f / 8f) + y + collectOffset * tilesize * Geometry.d4y(rotation) - 26.5f;
                    float a = warmup * MathUtil.timeValue(0.20f, 0.65f, 1f, Mathf.randomSeed(Point2.pack((int) rx, (int) ry), 0f, 360f));
                    Draw.alpha(a);
                    if (tx < 7) Lines.lineAngle(rx, ry, 0, 3);
                    if (ty < 7) Lines.lineAngle(rx, ry, 90, 3);
                    if (tx > 0) Lines.lineAngle(rx, ry, 180, 3);
                    if (ty > 0) Lines.lineAngle(rx, ry, 270, 3);
                }
            }
            Draw.reset();
        }

        @Override
        public boolean shouldConsume(){
            return items.total() < itemCapacity && enabled && !oreClusters.isEmpty();
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (timer(timerDump, dumpTime / timeScale)) dump();

            if(items.total() < itemCapacity && efficiency > 0){
                float multiplier = Mathf.lerp(1f, optionalBoostIntensity, optionalEfficiency);
                warmup = Mathf.approachDelta(warmup, 1, warmupSpeed);
                progress += edelta() * warmup * multiplier;
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            if (progress >= mineTime) {
                getOreOutput(oreClusters, tileX(), tileY(), rotation);
                oreClusters.each(tile -> {
                    if (tile.block() != null && tile.block().itemDrop != null) {
                        Fx.mineHuge.at(tile.worldx(), tile.worldy(), tile.block().itemDrop.color);
                        Fx.itemTransfer.at(tile.worldx(), tile.worldy(), 0, tile.block().itemDrop.color, this);
                    }
                });


                returnCount.each(entry -> addItem(entry.key, entry.value));
                progress %= mineTime;
            }
        }

        public void addItem(Item item, float value){
            float chance = value % 1f;
            items.add(item, Math.min(getMaximumAccepted(item) - items.get(item), (int)value));
            if (getMaximumAccepted(item) > items.get(item) && Mathf.chance(chance)) items.add(item, 1);
        }

        @Override
        public void drawStatus() {
            super.drawStatus();
        }
    }
}
