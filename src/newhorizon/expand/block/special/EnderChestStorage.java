package newhorizon.expand.block.special;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.Stat;
import newhorizon.content.NHColor;
import newhorizon.util.graphic.DrawFunc;

import java.util.Arrays;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

public class EnderChestStorage extends StorageBlock {
    public static IntMap<Seq<EnderChestStorageBuild>> storage = new IntMap<>();
    public static final float updateTime = 60f;

    public int itemPerSecond = 15;
    public EnderChestStorage(String name) {
        super(name);

        configurable = true;
        update = true;
        coreMerge = false;

        config(Integer.class, (EnderChestStorageBuild e, Integer num) -> {
            e.removeChannel();
            int idx = Mathf.clamp(num, 0, 8);
            e.flags[idx] = !e.flags[idx];
            e.addChannel();
        });

        config(Item.class, (EnderChestStorageBuild e, Item item) -> {
            e.selected = item;
        });
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("channel", (EnderChestStorageBuild e) -> new Bar(
                () -> "Channel: " + e.channel(),
                () -> e.receiver()? Pal.techBlue: NHColor.thurmixRed,
                () -> 1f
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.itemsMoved, itemPerSecond);
    }

    public class EnderChestStorageBuild extends StorageBuild {
        public boolean[] flags;
        public float progress;
        public Item selected;

        @Override
        public void created() {
            super.created();
            flags = new boolean[9];
            addChannel();
        }

        public int channel() {
            int out = 0;
            for (int i = 0; i < 8; i++) {
                out += Mathf.num(flags[i]) << i;
            }
            return out;
        }

        public void removeChannel(){
            storage.get(channel(), new Seq<>()).remove(this);
        }

        public void addChannel(){
            storage.put(channel(), storage.get(channel(), new Seq<>()).add(this));
        }

        public Seq<EnderChestStorageBuild> getChannel(){
            return storage.get(channel(), new Seq<>());
        }

        public boolean receiver(){
            return flags[8];
        }

        @Override
        public boolean[] config() {
            return flags;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (selected == null) return;
            if (progress >= updateTime) {
                int count = 0;
                for (EnderChestStorageBuild build : getChannel()) {
                    if (build.team != team) break;
                    if (count >= itemPerSecond) break;
                    if (build.receiver()) break;
                    if (items.get(selected) > getMaximumAccepted(selected)) break;
                    while (count < itemPerSecond && build.items.has(selected) && items.get(selected) < getMaximumAccepted(selected)) {
                        build.items.remove(selected, 1);
                        items.add(selected, 1);
                        count++;
                    }
                }
                progress %= updateTime;
            }else {
                progress += edelta();
            }
        }

        @Override
        public void draw() {
            super.draw();
            Color color = receiver()? Pal.techBlue: NHColor.thurmixRed;

            Draw.color(Pal.gray);
            Fill.square(x, y, 6.5f);

            Draw.color(color);
            Lines.stroke(1f);
            Lines.square(x, y, 5.5f);

            Draw.color();

            if (selected != null) drawItemSelection(selected);

            Draw.color(color);
            Draw.rect((receiver()? Icon.download.getRegion() : Icon.upload.getRegion()), x, y);
            DrawFunc.drawText(channel() + "", x + 4, y + 5);
            Draw.reset();
        }

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            table.setBackground(Tex.paneSolid);
            table.table(t -> {
                t.top();
                buildButton(t, 0);
                buildButton(t, 1);
                buildButton(t, 2);
                t.row();
                buildButton(t, 3);
                buildButton(t, 8);
                buildButton(t, 4);
                t.row();
                buildButton(t, 5);
                buildButton(t, 6);
                buildButton(t, 7);
            }).top();

            ItemSelection.buildTable(block, table, content.items(), () -> selected, this::configure, false);
        }

        private void buildButton(Table table, int idx) {
            table.button(button -> {
                button.image().expand().fill().update(image -> {
                    if (idx == 8){
                        image.setDrawable(flags[idx] ? Icon.download : Icon.upload);
                    }else {
                        image.setDrawable(flags[idx] ? Icon.effect : Icon.none);
                    }
                }).size(64).expand().fill();
            }, Styles.clearNonei, () -> configure(idx)).margin(0);
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            removeChannel();
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return super.acceptItem(source, item) && !receiver();
        }
    }
}
