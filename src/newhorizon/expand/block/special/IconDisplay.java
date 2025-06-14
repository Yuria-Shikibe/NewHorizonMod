package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Scaling;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.content;

public class IconDisplay extends Block {
    public TextureRegion maskRegion;

    public IconDisplay(String name) {
        super(name);
        size = 2;
        sync = true;
        update = true;
        rotate = false;
        saveConfig = true;
        configurable = true;
        logicConfigurable = true;
        selectionRows = selectionColumns = 8;
        clipSize = 200;

        config(UnlockableContent.class, (IconDisplayBuild build, UnlockableContent content) -> build.displayContent = content);

        configClear((IconDisplayBuild build) -> build.displayContent = null);
    }

    @Override
    public void load() {
        super.load();
        maskRegion = Core.atlas.find(name + "-mask");
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
        if (plan.config instanceof UnlockableContent content) {
            Tmp.v1.set(Scaling.bounded.apply(content.uiIcon.width, content.uiIcon.height, 12f, 12f));
            Draw.rect(content.uiIcon, plan.drawx(), plan.drawy(), Tmp.v1.x, Tmp.v1.y);
        }
    }

    public class IconDisplayBuild extends Building {
        public UnlockableContent displayContent;
        public Seq<UnlockableContent> tmpSeq = new Seq<>();

        public Seq<UnlockableContent> displayContents() {
            tmpSeq.clear();
            tmpSeq.add(content.items().select(i -> !i.isHidden()));
            tmpSeq.add(content.liquids().select(l -> !l.isHidden()));
            tmpSeq.add(content.units().select(u -> !u.isHidden()));
            tmpSeq.add(content.blocks().select(b -> !b.isHidden()));
            tmpSeq.add(content.planets().select(p -> !p.accessible));
            tmpSeq.add(content.statusEffects().select(e -> !e.isHidden()));
            tmpSeq.add(content.sectors().select(s -> !s.isHidden()));
            return tmpSeq.as();
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(IconDisplay.this, table, displayContents(),
                    this::config, this::configure, selectionRows, selectionColumns);
        }

        @Override
        public UnlockableContent config() {
            return displayContent;
        }

        @Override
        public void configure(Object value) {
            super.configure(value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void configured(Unit builder, Object value) {
            Class<?> type = value == null ? void.class : value.getClass().isAnonymousClass() ? value.getClass().getSuperclass() : value.getClass();
            UnlockableContent cont = null;
            if (value instanceof UnlockableContent) {
                type = UnlockableContent.class;
                cont = (UnlockableContent) value;
            }
            if (value instanceof Long num) {
                int typeID = Point2.unpack(Math.toIntExact(num)).x;
                int contID = Point2.unpack(Math.toIntExact(num)).y;
                if (typeID < content.getContentMap().length && content.getContentMap()[typeID].get(contID) != null) {
                    type = UnlockableContent.class;
                    cont = (UnlockableContent) content.getContentMap()[typeID].get(contID);
                }
            }
            if (builder != null && builder.isPlayer()) {
                lastAccessed = builder.getPlayer().coloredName();
            }
            if (block.configurations.containsKey(type) && content != null) {
                block.configurations.get(type).get(this, cont);
            }
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.config && displayContent != null)
                return Point2.pack(displayContent.getContentType().ordinal(), displayContent.id);
            else return super.sense(sensor);
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);

            if (displayContent != null) {
                Draw.z(Layer.blockOver);
                Draw.rect(maskRegion, x, y);
                Tmp.v1.set(Scaling.bounded.apply(displayContent.uiIcon.width, displayContent.uiIcon.height, 12f, 12f));
                Draw.rect(displayContent.uiIcon, x, y, Tmp.v1.x, Tmp.v1.y);
                DrawFunc.drawText(displayContent.localizedName, x, y + 8);
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(displayContent != null);
            if (displayContent != null) TypeIO.writeContent(write, displayContent);

        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (read.bool()) displayContent = (UnlockableContent) TypeIO.readContent(read);
        }
    }
}
