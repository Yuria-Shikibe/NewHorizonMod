package newhorizon.expand.block.distribution.delivery;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.content.NHStats;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CargoDeliveryDeposit extends Block {
    public float range = 80f;
    public float requestInterval = 12f;
    public Effect receiveEffect = NHFx.receive;
    public Sound receiveSound = Sounds.massdriverReceive;
    public float receiveSoundVolume = 0.5f;

    public final Seq<UnlockableContent> selectable = new Seq<>();

    public CargoDeliveryDeposit(String name) {
        super(name);
        size = 3;
        health = 1000;
        armor = 4f;
        update = true;
        solid = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        hasItems = true;
        hasLiquids = true;
        itemCapacity = 200;
        liquidCapacity = 200;
        sync = true;
        outputsLiquid = true;
        envEnabled |= Env.space;

        config(Item.class, (CargoDeliveryDepositBuild build, Item item) -> build.filter = item);
        config(Liquid.class, (CargoDeliveryDepositBuild build, Liquid liquid) -> build.filter = liquid);
        config(Integer.class, (CargoDeliveryDepositBuild build, Integer value) -> build.toggleLink(world.build(value)));
        configClear((CargoDeliveryDepositBuild build) -> build.filter = null);
    }

    @Override
    public void load() {
        super.load();
        selectable.clear();
        for (Item item : content.items()) selectable.add(item);
        for (Liquid liquid : content.liquids()) selectable.add(liquid);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(NHStats.requestInterval, table -> table.add(Strings.autoFixed(requestInterval / 60f, 2) + " " + StatUnit.seconds.localized()));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize, y * tilesize, range, Pal.accent);
    }

    public class CargoDeliveryDepositBuild extends CargoDeliveryBuild {
        public @Nullable UnlockableContent filter;
        public int incomingItems;
        public float incomingLiquids;
        public float requestTimer;

        @Override
        public void configure(Object value) {
            if (value instanceof Integer) {
                Object previous = block.lastConfig;
                super.configure(value);
                block.lastConfig = previous;
            } else {
                super.configure(value);
            }
        }

        public float totalLiquids() {
            return liquids.sum((liquid, amount) -> amount);
        }

        public boolean canRequest() {
            return requestTimer <= 0.0001f;
        }

        public void requested() {
            requestTimer = requestInterval;
        }

        public void reserveItem(int amount) {
            incomingItems += amount;
        }

        public void reserveLiquid(float amount) {
            incomingLiquids += amount;
        }

        public void release(CargoDeliveryData data) {
            if (data.item != null) {
                incomingItems = Math.max(0, incomingItems - data.itemAmount);
            }
            if (data.liquid != null) {
                incomingLiquids = Math.max(0f, incomingLiquids - data.liquidAmount);
            }
        }

        public void handlePayload(Bullet bullet, CargoDeliveryData data) {
            data.release();

            if (data.item != null && data.itemAmount > 0) {
                int maxAdd = Math.max(0, itemCapacity * 2 - items.total());
                int add = Math.min(data.itemAmount, maxAdd);
                items.add(data.item, add);
                data.itemAmount -= add;
            }

            if (data.liquid != null && data.liquidAmount > 0.001f) {
                float maxAdd = Math.max(0f, liquidCapacity * 2 - totalLiquids());
                float add = Math.min(data.liquidAmount, maxAdd);
                liquids.add(data.liquid, add);
                data.liquidAmount -= add;
            }

            Color color = data.item != null ? data.item.color : data.liquid != null ? data.liquid.color : Color.white;
            receiveEffect.at(bullet.x, bullet.y, bullet.rotation(), color);
            receiveSound.at(x, y, 1f + Mathf.range(0.2f), receiveSoundVolume);

            bullet.remove();
        }

        @Override
        public void updateTile() {
            if (requestTimer > 0f) {
                requestTimer = Math.max(0f, requestTimer - edelta());
            }

            dumpAccumulate();

            Seq<Liquid> liquidsContent = content.liquids();
            for (int i = 0; i < liquidsContent.size; i++) {
                Liquid liquid = liquidsContent.get(i);
                if (liquids.get(liquid) > 0.001f) {
                    dumpLiquid(liquid, 2f);
                }
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return false;
        }

        @Override
        public void buildConfiguration(Table table) {
            ItemSelection.buildTable(CargoDeliveryDeposit.this, table, selectable, () -> filter, this::configure);
        }

        @Override
        public Object config() {
            return filter;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            drawTeamTop();
            CargoDeliveryDraw.drawNodeLinks(this);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Drawf.circles(x, y, range);
            Draw.reset();

            drawItemSelection(filter);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(filter == null ? -1 : filter.getContentType().ordinal());
            write.s(filter == null ? -1 : filter.id);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            int type = read.b();
            int id = read.s();

            if (type < 0 || type >= ContentType.all.length || id < 0) {
                filter = null;
                return;
            }

            ContentType contentType = ContentType.all[type];
            filter = contentType == ContentType.item || contentType == ContentType.liquid
                    ? content.getByID(contentType, id) : null;
        }
    }
}
