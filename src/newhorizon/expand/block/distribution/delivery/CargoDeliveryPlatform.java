package newhorizon.expand.block.distribution.delivery;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.content.NHStats;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CargoDeliveryPlatform extends Block {
    protected static final Rand rand = new Rand();

    public float range = 160f;
    public int maxLinks = 4;
    public float reload = 90f;
    public int itemsPerDelivery = 20;
    public float liquidPerDelivery = 40f;
    public int packagesPerDispatch = 1;

    public float bulletSpeed = 5.5f;
    public float launchAccelTime = 30f;
    public float shake = 2f;
    public Effect launchEffect = NHFx.launch;
    public Sound shootSound = Sounds.massdriver;
    public float shootSoundVolume = 0.5f;
    public CargoDeliveryBolt bullet = new CargoDeliveryBolt();

    public float orbRadius = 4.1f, orbMidScl = 0.33f, orbSinScl = 8f, orbSinMag = 1f;
    public float particleOrbitX = 12f, particleOrbitY = 8f;
    public float particleOrbitSpeed = 0.9f, particleOrbitSpeedRand = 0.25f;
    public float particleDrift = 1.5f;
    public float orbitTrailWidth = 1.5f;
    public int orbitTrailLength = 20;

    public CargoDeliveryPlatform(String name) {
        super(name);
        size = 3;
        health = 1200;
        armor = 4f;
        update = true;
        solid = true;
        configurable = true;
        ignoreResizeConfig = true;
        hasItems = true;
        hasLiquids = true;
        hasPower = true;
        itemCapacity = 200;
        liquidCapacity = 200;
        sync = true;
        outputsLiquid = false;
        envEnabled |= Env.space;

        consumePower(2f);

        config(Integer.class, (CargoDeliveryPlatformBuild build, Integer value) -> build.toggleLink(world.build(value)));
        config(Point2[].class, (CargoDeliveryPlatformBuild build, Point2[] value) -> build.applyLinkPoints(value));
    }

    @Override
    public boolean outputsItems() {
        return false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
        stats.add(NHStats.deliveryPackages, packagesPerDispatch, StatUnit.none);
        stats.add(NHStats.deliveryItemCapacity, itemsPerDelivery, StatUnit.items);
        stats.add(NHStats.deliveryLiquidCapacity, liquidPerDelivery, StatUnit.liquidUnits);
        stats.add(NHStats.deliveryRate, packagesPerDispatch * 60f / reload, StatUnit.perSecond);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize, y * tilesize, range, Pal.accent);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region};
    }

    public class CargoDeliveryPlatformBuild extends CargoDeliveryBuild {
        public float reloadCounter;
        public float searchTimer;

        protected final ObjectMap<UnlockableContent, Seq<OrbitTrail>> orbitTrails = new ObjectMap<>();
        protected final ObjectSet<UnlockableContent> activeOrbitContents = new ObjectSet<>();
        protected final Seq<Building> pathBuffer = new Seq<>();
        protected final ObjectSet<Building> usedDeposits = new ObjectSet<>();

        @Override
        public void updateTile() {
            if (!headless) {
                updateOrbitTrails();
            }

            if (reloadCounter > 0f) {
                reloadCounter = Mathf.clamp(reloadCounter - edelta() * efficiency / reload);
            }

            if (efficiency <= 0f) return;
            if (reloadCounter > 0.0001f) return;
            if ((searchTimer -= edelta()) > 0f) return;

            if (tryFire()) {
                reloadCounter = 1f;
                searchTimer = 0f;
            } else {
                searchTimer = 10f;
            }
        }

        protected boolean tryFire() {
            if (items.total() <= 0 && totalLiquids() <= 0.001f) return false;

            CargoDeliveryGraph.ensureUpdated();
            CargoDeliveryGraph graph = CargoDeliveryGraph.get(team);
            CargoDeliveryGraph.PathResult result = graph.distances(this);

            usedDeposits.clear();
            int sent = 0;

            while (sent < packagesPerDispatch) {
                CargoDeliveryDeposit.CargoDeliveryDepositBuild bestDeposit = null;
                Item bestItem = null;
                Liquid bestLiquid = null;
                int bestItemAmount = 0;
                float bestLiquidAmount = 0f;
                float bestDist = Float.MAX_VALUE;

                for (int i = 0; i < graph.deposits.size; i++) {
                    CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit = graph.deposits.get(i);
                    if (deposit == null || deposit.dead() || usedDeposits.contains(deposit) || !deposit.canRequest()) {
                        continue;
                    }

                    float dist = result.dist.get(deposit, Float.MAX_VALUE);
                    if (dist == Float.MAX_VALUE) continue;

                    UnlockableContent filter = deposit.filter;
                    if (filter == null) continue;

                    if (filter instanceof Item item) {
                        int stock = items.get(item);
                        if (stock <= 0) continue;

                        int free = deposit.block.itemCapacity - deposit.items.total() - deposit.incomingItems;
                        if (free <= 0) continue;

                        int amount = Math.min(itemsPerDelivery, Math.min(stock, free));
                        if (amount <= 0) continue;

                        if (isBetter(dist, deposit, bestDist, bestDeposit)) {
                            bestDist = dist;
                            bestDeposit = deposit;
                            bestItem = item;
                            bestItemAmount = amount;
                            bestLiquid = null;
                            bestLiquidAmount = 0f;
                        }
                    } else if (filter instanceof Liquid liquid) {
                        if (liquids.current() != liquid) continue;

                        float stock = liquids.get(liquid);
                        if (stock <= 0.001f) continue;

                        float free = deposit.block.liquidCapacity - deposit.totalLiquids() - deposit.incomingLiquids;
                        if (free <= 0.001f) continue;

                        float amount = Math.min(liquidPerDelivery, Math.min(stock, free));
                        if (amount <= 0.001f) continue;

                        if (isBetter(dist, deposit, bestDist, bestDeposit)) {
                            bestDist = dist;
                            bestDeposit = deposit;
                            bestLiquid = liquid;
                            bestLiquidAmount = amount;
                            bestItem = null;
                            bestItemAmount = 0;
                        }
                    }
                }

                if (bestDeposit == null) break;

                usedDeposits.add(bestDeposit);

                if (!graph.pathTo(result, this, bestDeposit, pathBuffer) || pathBuffer.isEmpty()) {
                    continue;
                }

                CargoDeliveryData data = Pools.obtain(CargoDeliveryData.class, CargoDeliveryData::new);
                data.from = this;
                data.to = bestDeposit;
                data.item = bestItem;
                data.itemAmount = bestItemAmount;
                data.liquid = bestLiquid;
                data.liquidAmount = bestLiquidAmount;
                data.accelTime = launchAccelTime;
                data.reserved = true;

                for (int i = 0; i < pathBuffer.size - 1; i++) {
                    data.path.add(pathBuffer.get(i).pos());
                }

                if (bestItem != null) {
                    bestDeposit.reserveItem(bestItemAmount);
                    items.remove(bestItem, bestItemAmount);
                } else {
                    bestDeposit.reserveLiquid(bestLiquidAmount);
                    liquids.remove(bestLiquid, bestLiquidAmount);
                }

                bestDeposit.requested();

                float length = 0f;
                Building prev = this;
                for (int i = 0; i < pathBuffer.size; i++) {
                    Building step = pathBuffer.get(i);
                    length += prev.dst(step);
                    prev = step;
                }

                float life = Math.max(60f, length / Math.max(bulletSpeed, 0.01f) * 3f + launchAccelTime * 2f + 60f);
                float angle = angleTo(pathBuffer.first());

                bullet.create(this, team, x, y, angle, 0f, bulletSpeed, life, data);

                Color color = bestItem != null ? bestItem.color : bestLiquid != null ? bestLiquid.color : Color.white;
                launchEffect.at(x, y, angle, color);

                sent++;
            }

            if (sent > 0) {
                Effect.shake(shake, shake, this);
                shootSound.at(x, y, 1f + Mathf.range(0.2f), shootSoundVolume);
                return true;
            }

            return false;
        }

        protected boolean isBetter(float dist, Building deposit, float bestDist, Building bestDeposit) {
            return dist < bestDist - 0.001f
                    || (Mathf.equal(dist, bestDist, 0.001f) && (bestDeposit == null || deposit.id < bestDeposit.id));
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return Mathf.clamp(1f - reloadCounter);
            return super.sense(sensor);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return items.total() < itemCapacity;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return (liquids.current() == liquid || liquids.currentAmount() < 0.2f) && totalLiquids() < liquidCapacity;
        }

        public float totalLiquids() {
            return liquids.sum((liquid, amount) -> amount);
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            drawTeamTop();
            CargoDeliveryDraw.drawNodeLinks(this);

            Draw.z(Layer.effect);
            drawOrb();
            Draw.reset();
            Draw.z(Layer.block);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Drawf.circles(x, y, range);
            Draw.reset();
        }

        protected void drawOrb() {
            float alpha = Mathf.clamp(efficiency);
            if (alpha <= 0.001f) return;

            drawOrbitTrails(alpha);

            float rad = orbRadius + Mathf.absin(orbSinScl, orbSinMag);

            Draw.color(team.color, 0.8f * alpha);
            Lines.stroke(2f);
            Lines.circle(x, y, rad);

            Draw.color(team.color, 0.45f * alpha);
            Fill.circle(x, y, rad * orbMidScl);
        }

        protected void drawOrbitTrails(float alpha) {
            for (UnlockableContent content : orbitTrails.keys()) {
                Seq<OrbitTrail> trails = orbitTrails.get(content);
                if (trails == null) continue;

                for (OrbitTrail trail : trails) {
                    trail.draw(alpha);
                }
            }
        }

        protected void updateOrbitTrails() {
            activeOrbitContents.clear();

            if (itemsPerDelivery > 0) {
                items.each((item, amount) -> updateContentTrails(item, amount, itemsPerDelivery));
            }

            Liquid liquid = liquids.current();
            float amount = liquids.currentAmount();
            if (liquid != null && amount > 0.001f && liquidPerDelivery > 0.001f) {
                updateContentTrails(liquid, amount, liquidPerDelivery);
            }

            for (UnlockableContent content : orbitTrails.keys().toSeq()) {
                if (activeOrbitContents.contains(content)) continue;

                Seq<OrbitTrail> trails = orbitTrails.remove(content);
                if (trails != null) {
                    for (OrbitTrail trail : trails) {
                        trail.trail.clear();
                    }
                }
            }
        }

        protected void updateContentTrails(UnlockableContent content, float amount, float perDelivery) {
            int count = (int) Math.ceil(amount / perDelivery);
            if (count <= 0) return;

            activeOrbitContents.add(content);

            Seq<OrbitTrail> trails = orbitTrails.get(content, () -> new Seq<>());
            while (trails.size < count) {
                int index = trails.size;
                trails.add(new OrbitTrail(content, index, particleSizeFactor(amount, index, perDelivery)));
            }

            while (trails.size > count) {
                trails.pop().trail.clear();
            }

            for (int i = 0; i < trails.size; i++) {
                OrbitTrail trail = trails.get(i);
                trail.sizeFactor = particleSizeFactor(amount, i, perDelivery);
                trail.update(x, y);
            }
        }

        protected float particleSizeFactor(float amount, int index, float perDelivery) {
            return Math.max(0.25f, Math.min(1f, (amount - index * perDelivery) / perDelivery));
        }

        protected class OrbitTrail {
            protected final UnlockableContent content;
            protected final Trail trail;
            protected final float phase, speed, orbitX, orbitY, driftPhase, driftSpeed, driftX, driftY;
            protected float sizeFactor;

            protected OrbitTrail(UnlockableContent content, int index, float sizeFactor) {
                this.content = content;
                this.sizeFactor = sizeFactor;
                this.trail = new Trail(orbitTrailLength);

                rand.setSeed(id + block.hashCode() + content.getContentType().ordinal() * 100000L
                        + content.id * 131L + index * 17L);

                this.phase = rand.random(360f);

                float speedScl = 1f + (rand.random(1f) - 0.5f) * 2f * particleOrbitSpeedRand;
                float direction = rand.nextBoolean() ? 1f : -1f;
                this.speed = particleOrbitSpeed * speedScl * direction * (0.85f + index * 0.03f);

                this.orbitX = particleOrbitX * (0.9f + rand.random(0.2f));
                this.orbitY = particleOrbitY * (0.9f + rand.random(0.2f));
                this.driftPhase = rand.random(360f);
                this.driftSpeed = speed * (0.7f + rand.random(0.6f));
                this.driftX = particleDrift * (0.5f + rand.random(1f));
                this.driftY = particleDrift * (0.5f + rand.random(1f));
            }

            protected void update(float centerX, float centerY) {
                float angle = phase + Time.time * speed;
                float drift = Time.time * driftSpeed + driftPhase;

                trail.update(
                        centerX + Mathf.cosDeg(angle) * orbitX + Mathf.sinDeg(drift) * driftX,
                        centerY + Mathf.sinDeg(angle) * orbitY + Mathf.cosDeg(drift) * driftY,
                        1f + Mathf.absin(4f, 0.2f)
                );
            }

            protected void draw(float alpha) {
                Color color = content instanceof Item item ? item.color
                        : content instanceof Liquid liquid ? liquid.color : Color.white;
                Tmp.c1.set(color).a(alpha);

                float width = orbitTrailWidth * sizeFactor;
                trail.drawCap(Tmp.c1, width);
                trail.draw(Tmp.c1, width);
            }
        }

    }
}

