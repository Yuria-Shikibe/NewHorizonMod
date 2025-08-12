package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.ui.Bar;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.StatUnit;
import newhorizon.NHGroups;
import newhorizon.content.NHStats;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.util.graphic.DrawFunc;

import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class AssignedBeacon extends AdaptOverdriveProjector {
    public int maxLink = 4;
    public AssignedBeacon(String name) {
        super(name);
        solid = true;
        update = true;
        configurable = true;
        saveConfig = true;
        copyConfig = true;

        canOverdrive = false;
        schematicPriority = -30;

        size = 3;

        config(Integer.class, AssignedBeaconBuild::handleLink);
        config(Point2[].class, (AssignedBeaconBuild entity, Point2[] data) -> {
            for (Point2 p : data) {
                entity.handleLink(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
            }
        });
        configClear((AssignedBeaconBuild entity) -> entity.linkBuilds.clear());
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("boost");
        addBar("productivity", (AssignedBeaconBuild e) -> new Bar(
                () -> Core.bundle.format("nh.bar.productivity-boost", Strings.autoFixed((e.realBoost()) * 100, 0)),
                () -> Pal.ammo,
                () -> 1f
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        drawPotentialLinks(x, y);
        drawOverlay(x * tilesize + offset, y * tilesize + offset, rotation);
        Drawf.dashRect(baseColor, x * tilesize + offset - range/2f, y * tilesize + offset - range/2f, range, range);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.maxLinks, maxLink, StatUnit.none);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class AssignedBeaconBuild extends AdaptOverdriveProjectorBuild{
        public ObjectFloatMap<Building> linkBuilds = new ObjectFloatMap<>();
        public float[] buffer;

        @Override
        public void updateTile() {
            if (buffer != null) {
                for (int i = 0; i < buffer.length; i += 2) {
                    Building b = world.build((int) buffer[i]);
                    if (b != null) linkBuilds.put(b, buffer[i + 1]);
                }
                buffer = null;
            }
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            heat = Mathf.lerpDelta(heat, efficiency > 0 ? 1f : 0f, 0.08f);
            if(hasBoost) phaseHeat = Mathf.lerpDelta(phaseHeat, optionalEfficiency, 0.1f);
            if(efficiency > 0) useProgress += delta();
            if(useProgress >= useTime){
                consume();
                useProgress %= useTime;
            }
            checkLinks();
            updateLink();
        }

        public Seq<Building> linkBuilds() {
            return linkBuilds.keys().toArray();
        }

        public void updateLink(){
            linkBuilds().each(b -> {
                float progress = linkBuilds.get(b, 0f);
                if (b.block instanceof GenericCrafter crafter && b instanceof GenericCrafter.GenericCrafterBuild build){
                    progress += getProgressIncrease(crafter.craftTime / realBoost() / efficiency) * build.efficiency * build.timeScale();
                    if (progress >= 1f){
                        if (b.block instanceof RecipeGenericCrafter crafter1 && b instanceof RecipeGenericCrafter.RecipeGenericCrafterBuild build1){
                            if (build1.getRecipe() != null){
                                if (crafter1.outputItems != null){
                                    for (ItemStack stack : crafter1.outputItems) {
                                        build.items.add(stack.item, stack.amount);
                                    }
                                }
                                if (crafter1.outputPayloads != null){
                                    for (PayloadStack stack : crafter1.outputPayloads) {
                                        build.getPayloads().add(stack.item, stack.amount);
                                    }
                                }
                            }
                        }else {
                            if (crafter.outputItems != null){
                                for (ItemStack stack : crafter.outputItems) {
                                    build.items.add(stack.item, stack.amount);
                                }
                            }
                        }
                        progress %= 1f;
                    }
                    linkBuilds.put(b, progress);
                }
            });
        }

        @Override
        public Point2[] config() {
            Point2[] out = new Point2[linkBuilds.size];
            for (int i = 0; i < linkBuilds.size; i++){
                out[i] = new Point2(linkBuilds().get(i).tileX() - tileX(), linkBuilds().get(i).tileY() - tileY());
            }
            return out;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (other != null && linkValid(other) && !containBuild(other)) {
                configure(other.pos());
                return false;
            }
            return true;
        }

        public boolean linkValid(Building b) {
            return b != null && b.isValid() && Math.abs(b.x - x) <= range()/2f && Math.abs(b.y - y) <= range()/2f && b.team == team && b.block.canOverdrive;
        }

        @SuppressWarnings("all")
        public boolean containBuild(Building b) {
            AtomicBoolean contains = new AtomicBoolean(false);
            NHGroups.beaconBoostLinks.each((source, targets) -> {
                if (source != this && targets.contains(target -> target == b)) {
                    contains.set(true);
                }
            });
            return contains.get();
        }

        public void handleLink(int value) {
            Building other = Vars.world.build(value);
            if (other != null && linkValid(other) && !containBuild(other)) {
                if (linkBuilds().contains(other)) {
                    linkBuilds.remove(other, 0f);
                }else {
                    if (linkBuilds.size >= maxLink) return;
                    linkBuilds.put(other, 0f);
                }
            }
        }

        @Override
        public void drawConfigure() {
            Drawf.dashSquare(Pal.accent, x, y, range());

            NHGroups.beaconBoostLinks.keys().toSeq().each(b -> {
                if (b instanceof AssignedBeaconBuild build) {
                    build.drawConnect();
                }
            });
        }

        public void drawConnect(){
            Draw.z(Layer.blockOver);
            Draw.color(baseColor);
            Draw.alpha(0.3f);
            Fill.square(x, y, size * tilesize / 2f);

            Draw.z(Layer.blockOver + 0.01f);
            Draw.color(baseColor);
            Lines.stroke(1.5f);
            Lines.square(x, y, size * tilesize / 2f + 0.5f);

            for (Building b : linkBuilds()) {
                if (b == null) continue;
                Tmp.v1.set(Geometry.raycastRect(b.x, b.y, x, y, Tmp.r1.setCentered(x, y, size * tilesize)));
                Tmp.v2.set(Geometry.raycastRect(x, y, b.x, b.y, Tmp.r1.setCentered(b.x, b.y, b.block.size * tilesize)));

                Draw.z(Layer.blockOver);
                Draw.color(baseColor);
                Draw.alpha(0.3f);
                Fill.square(b.x, b.y, b.block.size * tilesize / 2f);

                Draw.z(Layer.blockOver + 0.01f);
                Draw.color(baseColor);
                Lines.stroke(1f);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 0.5f);
                Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);

                float progress = linkBuilds.get(b, 0f);
                if (progress > 0.001f){
                    DrawFunc.circlePercent(b.x, b.y, b.block.size * tilesize * 0.375f, linkBuilds.get(b, 0f), 0);
                }
            }
        }

        //check all links and remove invalid one
        public void checkLinks() {
            while (linkBuilds.size > maxLink) {
                linkBuilds.remove(linkBuilds().pop(), 0f);
            }
            for (Building b : linkBuilds()) {
                if (!linkValid(b)) {
                    linkBuilds.remove(b, 0f);
                }
            }
            NHGroups.beaconBoostLinks.put(this, linkBuilds());
        }

        @Override
        public void remove() {
            super.remove();
            NHGroups.beaconBoostLinks.remove(this);
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(linkBuilds.size);
            linkBuilds.each(buildingEntry -> {
                write.i(buildingEntry.key.pos());
                write.f(buildingEntry.value);
            });
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision == 2){
                int size = read.i();
                buffer = new float[size * 2];
                for (int i = 0; i < size; i++) {
                    int pos = read.i();
                    float value = read.f();
                    buffer[i * 2] = pos;
                    buffer[i * 2 + 1] = value;
                }
            }
        }
    }
}
