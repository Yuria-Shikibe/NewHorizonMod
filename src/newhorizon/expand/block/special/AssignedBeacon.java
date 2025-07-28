package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.IntFloatMap;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.Ranged;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NHGroups;
import newhorizon.content.NHStats;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.util.graphic.DrawFunc;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.*;

public class AssignedBeacon extends Block {
    public float range = 60f;

    public float maxBoostScl = 2f;
    public float maxProductivity = 0.5f;

    public int maxLink = 4;

    public Color overdriveColor = Color.valueOf("feb380");

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
            for (int i = 0; i < data.length - 1; i++) {
                Point2 p = data[i];
                entity.handleLink(Point2.pack(p.x + entity.tileX(), p.y + entity.tileY()));
            }
            entity.handleScale(data[data.length - 1].x / 10f);
        });
        config(Float.class, AssignedBeaconBuild::handleScale);

        configClear((AssignedBeaconBuild entity) -> entity.linkBuilds.clear());
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("speedMul", (AssignedBeaconBuild e) -> new Bar(
                () -> Core.bundle.format("nh.bar.speed-boost", "+" + Strings.autoFixed(((1 - e.balance) * maxBoostScl) * 100, 0)),
                () -> Pal.techBlue,
                () -> 1f
        ));
        addBar("productivityMul", (AssignedBeaconBuild e) -> new Bar(
                () -> Core.bundle.format("nh.bar.productivity-boost", Strings.autoFixed((e.balance * maxProductivity) * 100, 0)),
                () -> Pal.ammo,
                () -> 1f
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.maxLinks, maxLink, StatUnit.none);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class AssignedBeaconBuild extends Building implements Ranged {
        public ObjectFloatMap<Building> linkBuilds = new ObjectFloatMap<>();
        public IntFloatMap buffer;
        public float balance = 0f;

        @Override
        public void updateTile() {
            if (buffer != null) {
                buffer.forEach(buffer -> {
                    Building b = world.build(buffer.key);
                    if (b != null) {
                        linkBuilds.put(b, buffer.value);
                    }
                });
                buffer = null;
            }
            super.updateTile();
            checkLinks();
            NHGroups.beaconBoostLinks.put(this, linkBuilds());
            updateLink();
        }

        public Seq<Building> linkBuilds() {
            return linkBuilds.keys().toArray();
        }

        public void updateLink(){
            float boost = (1 - balance) * maxBoostScl;
            float prod = balance * maxProductivity;
            linkBuilds().each(b -> {
                b.applyBoost(boost * efficiency + 1, 10f);
                if (prod == 0f) return;
                float progress = linkBuilds.get(b, 0f);
                if (b.block instanceof GenericCrafter crafter && b instanceof GenericCrafter.GenericCrafterBuild build){
                    progress += getProgressIncrease(crafter.craftTime / prod / efficiency) * build.efficiency;
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
            Point2[] out = new Point2[linkBuilds.size + 1];
            for (int i = 0; i < linkBuilds.size; i++){
                out[i] = new Point2(linkBuilds().get(i).tileX() - tileX(), linkBuilds().get(i).tileY() - tileY());
            }
            out[linkBuilds.size] = new Point2((int)(balance * 10), 0);
            return out;
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(inner -> {
                inner.background(Tex.paneSolid);
                inner.label(() -> "Boost Scale:+" + Strings.autoFixed(((1 - balance) * maxBoostScl) * 100, 0) + "%").left().growX().row();
                inner.label(() -> "Productivity:" + Strings.autoFixed(balance * maxProductivity * 100, 0) + "%").left().growX().row();
                inner.slider(0f, 1f, 0.1f, balance, this::configure).growX();
            }).width(320f);

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
            return b != null && b.isValid() && Math.abs(b.x - x) <= range() && Math.abs(b.y - y) <= range() && b.team == team && b.block.canOverdrive;
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

        public void handleScale(float value) {
            balance = Mathf.clamp(value);
        }

        @Override
        public void draw() {
            super.draw();

            float f = 1f - (Time.time / 100f) % 1f;

            Draw.color(overdriveColor);
            Draw.alpha(efficiency * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.1f) * efficiency);

            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            Lines.beginLine();
            for(int i = 0; i < 4; i++){
                Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if(f < 0.5f) Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);

            Draw.reset();
        }

        @Override
        public void drawConfigure() {
            Drawf.dashSquare(Pal.accent, x, y, range() * 2);

            NHGroups.beaconBoostLinks.keys().toSeq().each(b -> {
                if (b instanceof AssignedBeaconBuild build) {
                    build.drawConnect();
                }
            });
        }

        public void drawConnect(){
            Draw.z(Layer.blockOver);
            Draw.color(overdriveColor);
            Draw.alpha(0.3f);
            Fill.square(x, y, size * tilesize / 2f);

            Draw.z(Layer.blockOver + 0.01f);
            Draw.color(overdriveColor);
            Lines.stroke(1.5f);
            Lines.square(x, y, size * tilesize / 2f + 0.5f);

            for (Building b : linkBuilds()) {
                if (b == null) continue;
                Tmp.v1.set(Geometry.raycastRect(b.x, b.y, x, y, Tmp.r1.setCentered(x, y, size * tilesize)));
                Tmp.v2.set(Geometry.raycastRect(x, y, b.x, b.y, Tmp.r1.setCentered(b.x, b.y, b.block.size * tilesize)));

                Draw.z(Layer.blockOver);
                Draw.color(overdriveColor);
                Draw.alpha(0.3f);
                Fill.square(b.x, b.y, b.block.size * tilesize / 2f);

                Draw.z(Layer.blockOver + 0.01f);
                Draw.color(overdriveColor);
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
        }

        @Override
        public void remove() {
            super.remove();
            NHGroups.beaconBoostLinks.remove(this);
        }

        @Override
        public float range() {
            return range;
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(balance);
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
                balance = read.f();
                int size = read.i();
                buffer = new IntFloatMap(size);
                for (int i = 0; i < size; i++) {
                    int pos = read.i();
                    float value = read.f();
                    buffer.put(pos, value);
                }
            }
        }
    }
}
