package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Geometry;
import arc.struct.IntSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Tmp;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.modules.ItemModule;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.content;

/**
 * A storage block whose item module is shared by orthogonally adjacent blocks
 * of the same team. It deliberately does not participate in
 * StorageBlock's core merging behaviour.
 */
public class AccumulatingStorage extends StorageBlock {
    private static final String FLOOD_PIPE_REGION = "new-horizon-flood-pipe";
    private static final int CAPACITY_PER_BLOCK = 100;

    public TextureRegion[] atlasRegions;
    public TextureRegion[] innerAtlasRegions;

    public AccumulatingStorage(String name) {
        super(name);
        coreMerge = false;
        itemCapacity = CAPACITY_PER_BLOCK;
        hasItems = true;
        hasPower = true;
        conductivePower = true;
        update = true;
        solid = true;
        separateItemCapacity = true;
        drawTeamOverlay = false;
        drawCached = false;
        drawDynamic = true;

        // Mindustry power consumption is expressed per tick; 100/60 displays as 100 power/second.
        consumePower(100f / 60f);
    }

    @Override
    public void setBars() {
        super.setBars();
        // Block.setBars() places the power bar before this custom status bar.
        addBar("storage-power-status", (AccumulatingStorageBuild entity) -> new Bar(
                () -> Core.bundle.get(entity.hasStoragePower()
                        ? "bar.new-horizon-accumulating-storage-enabled"
                        : "bar.new-horizon-accumulating-storage-locked"),
                () -> entity.hasStoragePower() ? Pal.powerBar : Pal.redderDust,
                () -> 1f
        ));
    }

    @Override
    public void load() {
        super.load();
        // Use flood-pipe art and its 4x4 autotile layout until dedicated art exists.
        atlasRegions = SpriteUtil.splitRegionArray(Core.atlas.find(FLOOD_PIPE_REGION + "-tiled"), 32, 32);
        innerAtlasRegions = SpriteUtil.splitRegionArray(Core.atlas.find(FLOOD_PIPE_REGION + "-inner-tiled"), 32, 32, 0, SpriteUtil.ATLAS_INDEX_4_4_VANILLA);
        region = Core.atlas.find(FLOOD_PIPE_REGION);
    }

    @Override
    public void loadIcon() {
        uiIcon = Core.atlas.find(FLOOD_PIPE_REGION);
        fullIcon = uiIcon;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{Core.atlas.find(FLOOD_PIPE_REGION)};
    }

    public class AccumulatingStorageBuild extends StorageBuild {
        private final Seq<AccumulatingStorageBuild> component = new Seq<>();
        private final Seq<ItemModule> modules = new Seq<>();
        private final Queue<AccumulatingStorageBuild> queue = new Queue<>();
        private final IntSet visited = new IntSet();
        private int networkSize = 1;
        private int drawIndex;
        private int drawInnerIndex;
        private boolean statusLeader = true;
        private boolean storageEnabled;

        @Override
        public void created() {
            super.created();
            rebuildComponent();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            rebuildComponent();
        }

        @Override
        public void onRemoved() {
            splitFromNeighbours();
            super.onRemoved();
        }

        @Override
        public void changeTeam(mindustry.game.Team next) {
            splitFromNeighbours();
            super.changeTeam(next);
            rebuildComponent();
        }

        @Override
        public void updateTile() {
            // Keep the interaction state synchronized in the same update phase as
            // RemoteCoreStorage, but require a genuinely full power supply.
            storageEnabled = enabled && efficiency >= 0.999f;
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return Math.max(CAPACITY_PER_BLOCK, networkSize * CAPACITY_PER_BLOCK);
        }

        private boolean hasStoragePower() {
            return enabled && storageEnabled;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return hasStoragePower() && super.acceptItem(source, item);
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (hasStoragePower()) super.handleItem(source, item);
        }

        @Override
        public boolean canUnload() {
            return hasStoragePower() && super.canUnload();
        }

        @Override
        public boolean allowDeposit() {
            return hasStoragePower() && super.allowDeposit();
        }

        @Override
        public int removeStack(Item item, int amount) {
            return hasStoragePower() ? super.removeStack(item, amount) : 0;
        }

        @Override
        public int explosionItemCap() {
            return getMaximumAccepted(null);
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.itemCapacity) return getMaximumAccepted(null);
            return super.sense(sensor);
        }

        @Override
        public void draw() {
            Draw.z(mindustry.graphics.Layer.block + 1f);
            Draw.rect(atlasRegions[drawIndex], x, y);
            if (drawIndex == 13) Draw.rect(innerAtlasRegions[drawInnerIndex], x, y);
        }

        @Override
        public void drawStatus() {
            if (statusLeader) super.drawStatus();
        }

        private void updateDrawRegion() {
            int mask = 0;
            for (int i = 0; i < 8; i++) {
                Tile other = tile.nearby(Geometry.d8[i]);
                if (sameBlock(other)) {
                    mask |= 1 << i;
                }
            }
            drawIndex = TileBitmask.values[mask];
            drawInnerIndex = 0;
            if (drawIndex == 13) {
                for (int i = 0; i < 4; i++) {
                    Tile other1 = tile.nearby(Geometry.d4[i]);
                    Tile other2 = tile.nearby(Tmp.p1.set(Geometry.d4[i]).add(Geometry.d4[i]));
                    if (sameBlock(other1) && sameBlock(other2)) {
                        drawInnerIndex |= 1 << i;
                    }
                }
            }
        }

        private boolean sameBlock(Tile other) {
            return other != null && other.build != null && other.build.block == block;
        }

        /** Save shared inventory once per connected component, avoiding load-time duplication. */
        @Override
        public void writeAll(Writes write) {
            ItemModule original = items;
            if (!isLeader()) items = new ItemModule();
            super.writeAll(write);
            items = original;
        }

        private boolean isLeader() {
            rebuildComponent();
            int position = tile.pos();
            for (AccumulatingStorageBuild build : component) {
                if (build.tile.pos() < position) return false;
            }
            return true;
        }

        private boolean valid(AccumulatingStorageBuild build, AccumulatingStorageBuild excluded) {
            return build != null && build != excluded && build.block == block && build.team == team && build.isValid();
        }

        private void collect(AccumulatingStorageBuild start, AccumulatingStorageBuild excluded, Seq<AccumulatingStorageBuild> out) {
            queue.clear();
            visited.clear();
            queue.addLast(start);
            while (queue.size > 0) {
                AccumulatingStorageBuild current = queue.removeFirst();
                if (!valid(current, excluded) || !visited.add(current.tile.pos())) continue;
                out.add(current);
                for (Building next : current.proximity) {
                    if (next instanceof AccumulatingStorageBuild build && valid(build, excluded) && !visited.contains(build.tile.pos())) {
                        queue.addLast(build);
                    }
                }
            }
        }

        private void rebuildComponent() {
            if (!isValid()) return;
            component.clear();
            collect(this, null, component);
            if (component.size == 0) return;

            modules.clear();
            for (AccumulatingStorageBuild build : component) {
                if (build.items != null && !modules.contains(build.items, true)) modules.add(build.items);
            }
            ItemModule shared = modules.size == 0 ? new ItemModule() : modules.first();
            for (int i = 1; i < modules.size; i++) shared.add(modules.get(i));
            AccumulatingStorageBuild leader = component.first();
            for (AccumulatingStorageBuild build : component) {
                if (build.tile.pos() < leader.tile.pos()) leader = build;
            }
            for (AccumulatingStorageBuild build : component) {
                build.items = shared;
                build.networkSize = component.size;
                build.statusLeader = build == leader;
                build.updateDrawRegion();
            }
        }

        /** Reassign a shared inventory before a block leaves its current component. */
        private void splitFromNeighbours() {
            if (items == null) return;

            Seq<Seq<AccumulatingStorageBuild>> parts = new Seq<>();
            IntSet claimed = new IntSet();
            for (Building prox : proximity) {
                if (!(prox instanceof AccumulatingStorageBuild neighbour) || !valid(neighbour, this) || claimed.contains(neighbour.tile.pos())) continue;
                Seq<AccumulatingStorageBuild> part = new Seq<>();
                collect(neighbour, this, part);
                if (part.isEmpty()) continue;
                for (AccumulatingStorageBuild build : part) claimed.add(build.tile.pos());
                parts.add(part);
            }

            ItemModule old = items;
            ItemModule selfModule = new ItemModule();
            int survivorCapacity = 0;
            for (Seq<AccumulatingStorageBuild> part : parts) survivorCapacity += part.size * CAPACITY_PER_BLOCK;

            for (Item item : content.items()) {
                int amount = old.get(item);
                int remaining = Math.min(amount, survivorCapacity);
                int remainingCapacity = survivorCapacity;
                for (Seq<AccumulatingStorageBuild> part : parts) {
                    int capacity = part.size * CAPACITY_PER_BLOCK;
                    int give = remainingCapacity == 0 ? 0 : Math.min(capacity, (remaining * capacity + remainingCapacity - 1) / remainingCapacity);
                    ItemModule target = new ItemModule();
                    if (part.get(0).items == null || part.get(0).items == old) part.get(0).items = target;
                    else target = part.get(0).items;
                    target.set(item, give);
                    for (AccumulatingStorageBuild build : part) {
                        build.items = target;
                    }
                    remaining -= give;
                    remainingCapacity -= capacity;
                }
                selfModule.set(item, Math.min(CAPACITY_PER_BLOCK, amount - Math.min(amount, survivorCapacity)));
            }
            for (Seq<AccumulatingStorageBuild> part : parts) {
                AccumulatingStorageBuild leader = part.first();
                for (AccumulatingStorageBuild build : part) {
                    if (build.tile.pos() < leader.tile.pos()) leader = build;
                }
                for (AccumulatingStorageBuild build : part) {
                    build.networkSize = part.size;
                    build.statusLeader = build == leader;
                    build.updateDrawRegion();
                }
            }
            networkSize = 1;
            statusLeader = true;
            items = selfModule;
        }
    }
}
