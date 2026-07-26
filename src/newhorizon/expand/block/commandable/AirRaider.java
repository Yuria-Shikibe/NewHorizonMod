package newhorizon.expand.block.commandable;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.ItemModule;
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.content.NHFx;
import newhorizon.content.NHItems;
import newhorizon.content.NHStatusEffects;
import newhorizon.content.NHTechTree;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.bullets.AccelBulletType;
import newhorizon.expand.bullets.LightningLinkerBulletType;
import newhorizon.expand.bullets.raid.BasicRaidBulletType;
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.RaidSync;
import newhorizon.expand.logic.ThreatLevel;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;
import static newhorizon.util.ui.TableFunc.dialogHeight;
import static newhorizon.util.ui.TableFunc.dialogWidth;
import static newhorizon.util.ui.TableFunc.ui;

public class AirRaider extends CommandableBlock {
    public static final int SLOT_COUNT = 4;
    public static final int WEAPON_COUNT = 6;
    public static final float MAX_ALERT_SECONDS = 240f;
    public static final float BASE_ALERT_SECONDS = 120f;
    public static final float MIN_ALERT_SECONDS = 12f;
    public static final float MAX_SPREAD = 360f;
    public static final float MIN_SPREAD = 8f;
    public static final float MAX_BULLET_SPEED = 36f;
    public static final float MIN_BULLET_SPEED = 2.5f;
    public static final float MAX_BULLET_SIZE = 48f;
    public static final float MIN_BULLET_SIZE = 8f;
    public static final float MAX_DAMAGE = 10000f;
    public static final float MAX_SPLASH = 5000f;
    public static final float MIN_SPLASH_RADIUS = 20f;
    public static final float MAX_SPLASH_RADIUS = 220f;
    public static final int MAX_ITEM_THREAT = 12;
    public static final float DAMAGE_QUALITY_REF = 5.2f;
    public static final float SPLASH_QUALITY_REF = 4.5f;
    public static final float ACCEL_POWER = 1.55f;

    private static final ObjectMap<Item, Integer> itemTechDepth = new ObjectMap<>();

    public final WeaponMode[] weapons = new WeaponMode[WEAPON_COUNT];
    public final SlotDef[] slotDefs = new SlotDef[SLOT_COUNT];
    public TextureRegion[] weaponIcons = new TextureRegion[WEAPON_COUNT];
    public TextureRegion[][] shellIcons = new TextureRegion[WEAPON_COUNT][SLOT_COUNT];

    public AirRaider(String name) {
        super(name);
        replaceable = true;
        canOverdrive = false;
        reloadTime = 600f;
        range = 999999f;
        unloadable = false;
        hasItems = false;
        itemCapacity = 0;
        configurable = true;
        saveConfig = false;
        clearOnDoubleTap = false;

        config(IntSeq.class, AirRaiderBuild::handleConfig);
        config(Boolean.class, (AirRaiderBuild b, Boolean launch) -> {
            if (launch) b.tryLaunchRaid();
            else b.cancelRaidEvent();
        });
        initDefs();
    }

    private void initDefs() {
        weapons[0] = new WeaponMode("nh.air-raid.weapon-1", RaidBullets.defaultRaidBullet1, 1f, 0.01f, 0.2f, 1f, 1f, 40f, 0.5f, 80f, 10);
        weapons[1] = new WeaponMode("nh.air-raid.weapon-2", NHBullets.arc_9000, 0.8f, 0.2f, 1f, 1.2f, 0.4f, 50f, 2f, 250f, 3);
        weapons[2] = new WeaponMode("nh.air-raid.weapon-3", RaidBullets.raidBullet_9, 0.6f, 0.2f, 0.5f, 0.5f, 2f, 80f, 1f, 100f, 4, 40);
        weapons[3] = new WeaponMode("nh.air-raid.weapon-4", NHBullets.blastEnergyNgt, 0.1f, 0.05f, 0.4f, 0.2f, 4f, 25f, 2f, 80f, 40, 120);
        weapons[4] = new WeaponMode("nh.air-raid.weapon-5", NHBullets.railGun1, 3f, 1f, 1.2f, 0.4f, 8f, 5f, 5f, 640f, 1);
        weapons[5] = new WeaponMode("nh.air-raid.weapon-6", NHBullets.airRaidBomb, 1.2f, 0.8f, 0.8f, 1.3f, 2f, 48f, 10f, 120f, 2, 20);

        slotDefs[0] = new SlotDef("nh.air-raid.slot-charge", 280, new Seq<>());
        slotDefs[1] = new SlotDef("nh.air-raid.slot-control", 240, new Seq<>());
        slotDefs[2] = new SlotDef("nh.air-raid.slot-fuel", 320, new Seq<>());
        slotDefs[3] = new SlotDef("nh.air-raid.slot-shell", 300, new Seq<>());
    }

    @Override
    public void init() {
        super.init();
        range = 999999f;
        classifyItems();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
    }

    private void classifyItems() {
        Seq<Item> charge = new Seq<>();
        Seq<Item> control = new Seq<>();
        Seq<Item> fuel = new Seq<>();
        Seq<Item> shell = new Seq<>();

        for (Item item : content.items()) {
            if (item == null || item.isHidden()) continue;

            if (item.explosiveness > 0.15f || item.charge > 0.15f) {
                charge.add(item);
            }
            if (item == Items.silicon || item.name.contains("processor")) {
                control.add(item);
            }
            if (item.flammability > 0.15f || item == Items.coal || item == Items.sporePod || item == Items.thorium || item == NHItems.zeta
                    || item == NHItems.fusionEnergy || item == NHItems.thermoCorePositive || item == NHItems.thermoCoreNegative 
                    || item == NHItems.darkEnergy) {
                fuel.add(item);
            }
            if (item == Items.copper || item == Items.lead || item == Items.metaglass
                    || item == Items.beryllium || item == Items.tungsten || item == Items.carbide
                    || item == NHItems.silicar || item == NHItems.presstanium || item == NHItems.multipleSteel
                    || item == NHItems.irayrondPanel || item == NHItems.setonAlloy || item == NHItems.nodexPlate
                    || item == NHItems.ancimembrane || item == NHItems.hadronicomp || item == Items.plastanium
                    || item == Items.surgeAlloy || item == Items.titanium || item == Items.thorium) {
                shell.add(item);
            }
        }

        slotDefs[0].allowed.set(charge);
        slotDefs[1].allowed.set(control);
        slotDefs[2].allowed.set(fuel);
        slotDefs[3].allowed.set(shell);
    }

    private static void ensureTechDepth() {
        if (itemTechDepth.size > 0) return;
        if (NHTechTree.itemProductionTree == null) return;
        for (NHTechTree.ProductionNode root : NHTechTree.itemProductionTree) {
            walkTech(root, 0);
        }
    }

    private static void walkTech(NHTechTree.ProductionNode node, int depth) {
        if (node == null || node.content == null) return;
        UnlockableContent content = node.content;
        if (content instanceof Item item) {
            int prev = itemTechDepth.get(item, -1);
            if (depth > prev) itemTechDepth.put(item, depth);
        }
        if (node.children != null) {
            for (NHTechTree.ProductionNode child : node.children) {
                walkTech(child, depth + 1);
            }
        }
    }

    public static int techDepth(Item item) {
        ensureTechDepth();
        return itemTechDepth.get(item, 0);
    }

    public static int itemThreat(Item item) {
        if (item == null) return 0;
        int threat = 0;
        for (var entry : ThreatLevel.threatMap) {
            if (entry.value.contains(item)) {
                threat = Math.max(threat, entry.key);
            }
        }
        if (threat > 0) return Mathf.clamp(threat, 0, MAX_ITEM_THREAT);
        return Mathf.clamp(techDepth(item), 0, MAX_ITEM_THREAT);
    }

    public static float accel01(float x) {
        return Mathf.pow(Mathf.clamp(x, 0f, 1f), ACCEL_POWER);
    }

    public static float utilAccel01(float x) {
        x = Mathf.clamp(x, 0f, 1f);
        return Mathf.lerp(x, x * x, 0.38f);
    }

    public static float threatFactor(Item item) {
        return accel01(itemThreat(item) / (float) MAX_ITEM_THREAT);
    }

    public static float utilThreatFactor(Item item) {
        return utilAccel01(itemThreat(item) / (float) MAX_ITEM_THREAT);
    }

    public static float techMul(Item item) {
        return threatFactor(item);
    }

    @Override
    public void load() {
        super.load();
        for (int w = 0; w < WEAPON_COUNT; w++) {
            weaponIcons[w] = Core.atlas.find(NewHorizon.name("w" + (w + 1)));
            for (int s = 0; s < SLOT_COUNT; s++) {
                shellIcons[w][s] = Core.atlas.find(NewHorizon.name("w" + (w + 1) + "-s" + (s + 1)), Core.atlas.find(NewHorizon.name("s" + (s + 1))));
            }
        }
    }

    public class AirRaiderBuild extends CommandableBlockBuild {
        public int weaponIndex = -1;
        public int selectedSlot = 0;
        public final ItemModule[] slots = new ItemModule[SLOT_COUNT];
        public transient ActionBus raidBus;
        public transient EventRaidAction raidAction;
        public transient boolean raidActive;
        public transient float raidExpectEnd;

        public AirRaiderBuild() {
            for (int i = 0; i < SLOT_COUNT; i++) {
                slots[i] = new ItemModule();
            }
        }

        @Override
        public boolean isCharging() {
            return false;
        }

        @Override
        public boolean shouldCharge() {
            return false;
        }

        @Override
        public BlockStatus status() {
            if (raidActive) return BlockStatus.active;
            if (!isPowered()) return BlockStatus.noInput;
            if (weaponIndex < 0) return BlockStatus.noInput;
            if (!hasPayload() || !canAffordPayload()) return BlockStatus.noInput;
            return BlockStatus.noOutput;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (!raidActive) return;
            if (!RaidLogic.isLogicSide()) {
                if (Time.time >= raidExpectEnd) clearRaidRefs();
                return;
            }
            if (raidBus == null || raidBus.complete()) {
                clearRaidRefs();
            }
        }

        @Override
        public void command(Vec2 pos) {
            lastConfirmedTarget.set(pos);
            targetVec.set(pos);
            target = Point2.pack(World.toTile(pos.x), World.toTile(pos.y));
        }

        @Override
        public void commandAll(Vec2 pos) {
            if (canCommand(pos)) command(pos);
        }

        @Override
        public void setTarget(Point2 point2) {
            super.setTarget(point2);
            lastConfirmedTarget.set(World.unconv(point2.x), World.unconv(point2.y));
        }

        @Override
        public boolean canCommand(Vec2 target) {
            return !raidActive && isPowered() && weaponIndex >= 0 && hasPayload() && canAffordPayload() && !target.epsilonEquals(x, y, 0.1f);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawTargetPreview();
        }

        @Override
        public void drawConfigure() {
            drawTargetPreview();
        }

        private void drawTargetPreview() {
            if (!hasTarget()) return;
            float tx = lastConfirmedTarget.x;
            float ty = lastConfirmedTarget.y;
            float spread = previewSpread();

            Draw.z(Layer.effect);
            DrawFunc.posSquareLink(Pal.accent, 1.25f, 2.5f, true, x, y, tx, ty);
            DrawFunc.drawConnected(tx, ty, 12f, Pal.accent);
            Drawf.dashCircle(tx, ty, spread, team.color);
            Drawf.circles(tx, ty, Math.min(spread * 0.22f, 18f), Pal.accent);
            Draw.reset();
        }

        private float previewSpread() {
            if (weaponIndex < 0 || weaponIndex >= WEAPON_COUNT) {
                return 40f;
            }
            return Math.max(calcStats().inaccuracy, 8f);
        }

        public boolean hasPayload() {
            for (ItemModule slot : slots) {
                if (slot.total() <= 0) return false;
            }
            return true;
        }

        public boolean isPowered() {
            return efficiency > 0.001f;
        }

        public boolean canAffordPayload() {
            if (!hasPayload()) return false;
            if (state.rules.infiniteResources || team.rules().cheat) return true;
            Building core = team.core();
            if (core == null) return false;
            ItemModule need = new ItemModule();
            for (ItemModule slot : slots) {
                slot.each(need::add);
            }
            boolean[] ok = {true};
            need.each((item, amount) -> {
                if (amount > 0 && !core.items.has(item, amount)) ok[0] = false;
            });
            return ok[0];
        }

        public boolean consumePayloadFromCore() {
            if (!canAffordPayload()) return false;
            if (state.rules.infiniteResources || team.rules().cheat) {
                for (ItemModule slot : slots) slot.clear();
                return true;
            }
            if (!net.client()) {
                Building core = team.core();
                if (core == null) return false;
                for (ItemModule slot : slots) {
                    slot.each((item, amount) -> {
                        if (amount > 0) core.items.remove(item, amount);
                    });
                }
            }
            for (ItemModule slot : slots) slot.clear();
            return true;
        }

        public boolean hasTarget() {
            return target >= 0 && !lastConfirmedTarget.epsilonEquals(x, y, 0.1f) && !targetVec.epsilonEquals(x, y, 0.1f);
        }

        public void handleConfig(IntSeq seq) {
            if (seq == null || seq.size < 1) return;
            int mode = seq.get(0);
            switch (mode) {
                case 0 -> {
                    if (seq.size < 2) return;
                    int idx = seq.get(1);
                    if (idx >= -1 && idx < WEAPON_COUNT) {
                        weaponIndex = idx;
                        selectedSlot = 0;
                        clampSlotsToCapacity();
                    }
                }
                case 1 -> {
                    if (seq.size < 4) return;
                    setSlotAmount(seq.get(1), seq.get(2), seq.get(3));
                }
                case 2 -> tryLaunchRaid();
                case 3 -> cancelRaidEvent();
                case 4 -> {
                    if (seq.size < 2) return;
                    int slot = seq.get(1);
                    if (slot >= 0 && slot < SLOT_COUNT) {
                        slots[slot].clear();
                    }
                }
                case 5 -> {
                    if (seq.size < 2) return;
                    selectedSlot = Mathf.clamp(seq.get(1), 0, SLOT_COUNT - 1);
                }
            }
        }

        public void setSlotAmount(int slot, int itemId, int amount) {
            if (slot < 0 || slot >= SLOT_COUNT) return;
            Item item = content.item(itemId);
            if (item == null || !slotDefs[slot].allows(item)) return;

            ItemModule module = slots[slot];
            int current = module.get(item);
            int others = module.total() - current;
            int maxForItem = Math.max(0, slotCapacity(slot) - others);
            boolean infinite = state.rules.infiniteResources || team.rules().cheat;
            if (!infinite) {
                Building core = team.core();
                int have = core == null ? 0 : core.items.get(item);
                maxForItem = Math.min(maxForItem, have);
            }
            int targetAmt = Mathf.clamp(amount, 0, maxForItem);
            if (targetAmt == current) return;
            module.set(item, targetAmt);
        }

        public void clampSlotsToCapacity() {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                int cap = slotCapacity(slot);
                ItemModule module = slots[slot];
                if (module.total() <= cap) continue;
                int[] remain = {cap};
                ItemModule next = new ItemModule();
                module.each((item, amount) -> {
                    int keep = Math.min(amount, Math.max(remain[0], 0));
                    if (keep > 0) {
                        next.add(item, keep);
                        remain[0] -= keep;
                    }
                });
                module.clear();
                next.each(module::add);
            }
        }

        public void clearAllSlots() {
            for (ItemModule slot : slots) slot.clear();
        }

        private float currentCostMul() {
            if (weaponIndex < 0 || weaponIndex >= WEAPON_COUNT) return 1f;
            return Math.max(weapons[weaponIndex].costMul, 0.0001f);
        }

        private int slotCapacity(int slot) {
            return Math.max(1, Mathf.round(slotDefs[slot].capacity * currentCostMul()));
        }

        public RaidStats calcStats() {
            RaidStats stats = new RaidStats();
            if (weaponIndex < 0 || weaponIndex >= WEAPON_COUNT) return stats;
            WeaponMode mode = weapons[weaponIndex];

            float chargePower = slotContribution(0, (item, amt) ->
                    itemWeight(item, amt, 0) * (
                            0.4f
                                    + softAttr(item.explosiveness, 1.45f) * 0.9f
                                    + softAttr(item.flammability, 1.6f) * 0.25f
                                    + softAttr(item.charge, 3.2f) * 0.38f
                    ));
            float controlPower = slotContribution(1, (item, amt) ->
                    itemWeight(item, amt, 1) * (
                            0.45f
                                    + softAttr(item.charge, 3.2f) * 0.28f
                                    + softAttr(item.radioactivity, 3f) * 0.35f
                                    + itemThreat(item) * 0.04f
                    ));
            float fuelPower = slotContribution(2, (item, amt) ->
                    itemWeight(item, amt, 2) * (
                            0.38f
                                    + softAttr(item.flammability, 1.6f) * 0.95f
                                    + softAttr(item.explosiveness, 1.45f) * 0.22f
                                    + softAttr(item.charge, 3.2f) * 0.2f
                    ));
            float shellPower = slotContribution(3, (item, amt) ->
                    itemWeight(item, amt, 3) * (
                            0.32f + softAttr((float) Math.log1p(Math.max(item.hardness, 0)), 3.8f) * 0.95f
                    ));

            float chargeUtil = slotContribution(0, (item, amt) ->
                    utilWeight(item, amt, 0) * (
                            0.45f
                                    + softAttr(item.explosiveness, 1.45f) * 0.85f
                                    + softAttr(item.flammability, 1.6f) * 0.3f
                                    + softAttr(item.charge, 3.2f) * 0.4f
                    ));
            float controlUtil = slotContribution(1, (item, amt) ->
                    utilWeight(item, amt, 1) * (
                            0.5f
                                    + softAttr(item.charge, 3.2f) * 0.32f
                                    + softAttr(item.radioactivity, 3f) * 0.38f
                                    + itemThreat(item) * 0.05f
                    ));
            float fuelUtil = slotContribution(2, (item, amt) ->
                    utilWeight(item, amt, 2) * (
                            0.42f
                                    + softAttr(item.flammability, 1.6f) * 1.05f
                                    + softAttr(item.explosiveness, 1.45f) * 0.25f
                                    + softAttr(item.charge, 3.2f) * 0.22f
                    ));
            float shellUtil = slotContribution(3, (item, amt) ->
                    utilWeight(item, amt, 3) * (
                            0.4f + softAttr((float) Math.log1p(Math.max(item.hardness, 0)), 3.8f) * 1.05f
                    ));

            float chargeExplosiveness = slotContribution(0, (item, amt) ->
                    itemWeight(item, amt, 0) * softAttr(item.explosiveness, 1.45f));
            float chargeExplosivenessUtil = slotContribution(0, (item, amt) ->
                    utilWeight(item, amt, 0) * softAttr(item.explosiveness, 1.45f));
            float chargeElectric = slotContribution(0, (item, amt) ->
                    itemWeight(item, amt, 0) * softAttr(item.charge, 3.2f));
            float chargeFlame = slotContribution(0, (item, amt) ->
                    itemWeight(item, amt, 0) * softAttr(item.flammability, 1.6f));

            stats.chargeScore = chargePower;
            stats.controlScore = controlPower;
            stats.fuelScore = fuelPower;
            stats.shellScore = shellPower;
            stats.explosiveness = chargeExplosiveness;

            float damageScore = chargePower * 0.22f + shellPower * 0.14f + fuelPower * 0.08f + controlPower * 0.07f;
            float sizeScore = shellUtil * 0.62f + chargeUtil * 0.28f;
            float speedScore = fuelUtil * 0.72f + controlUtil * 0.32f;
            float sizeT = Mathf.clamp(sizeScore / 3.2f, 0f, 1f);
            float speedT = Mathf.clamp(speedScore / 3.0f, 0f, 1f);
            float sizeMax = MAX_BULLET_SIZE * Math.max(mode.sizeMul, 0.0001f);
            stats.size = Mathf.lerp(MIN_BULLET_SIZE, sizeMax, sizeT);
            float speedMax = MAX_BULLET_SPEED * Math.max(mode.speedMul, 0.0001f);
            stats.speed = Mathf.lerp(MIN_BULLET_SPEED, speedMax, speedT);
            stats.shotCount = scaledShotCount(mode);
            float shellFill = Mathf.clamp(slots[3].total() / (float) Math.max(slotCapacity(3), 1), 0f, 1f);
            stats.pierce = Mathf.clamp(Math.round(maxThreatInSlot(3) / (float) MAX_ITEM_THREAT * 8f * shellFill), 0, 8);

            float splashScore = 3.5f + chargePower * 0.08f + chargeExplosiveness * 0.12f + shellPower * 0.015f;
            float spreadScore = chargeExplosivenessUtil * 1.15f + chargeUtil * 0.35f + shellUtil * 0.12f;
            float spreadT = Mathf.clamp(spreadScore / 3.0f, 0f, 1f);
            float spreadMul = Math.max(mode.inaccuracy, 0.0001f) / 40f;
            float spreadMax = MAX_SPREAD * spreadMul;
            stats.inaccuracy = Mathf.lerp(MIN_SPREAD, spreadMax, spreadT);

            float tierGate = accel01(maxThreatLoaded() / (float) MAX_ITEM_THREAT);
            float amountGate = accel01(payloadFill());
            float damageQuality = accel01(Mathf.clamp(damageScore / DAMAGE_QUALITY_REF, 0f, 1f));
            float splashQuality = accel01(Mathf.clamp(splashScore / SPLASH_QUALITY_REF, 0f, 1f));
            float damageScale = tierGate * amountGate * Mathf.lerp(0.48f, 1f, damageQuality);
            float splashScale = tierGate * amountGate * Mathf.lerp(0.48f, 1f, splashQuality);

            float radiusT = Mathf.clamp(tierGate * 0.55f + amountGate * 0.45f, 0f, 1f) * Mathf.lerp(0.55f, 1f, splashQuality);
            float radiusMul = Math.max(mode.splashRangeMul, 0.0001f);
            float radiusMax = MAX_SPLASH_RADIUS * radiusMul;
            float radiusMin = MIN_SPLASH_RADIUS * radiusMul;
            stats.splashRadius = Mathf.lerp(radiusMin, radiusMax, radiusT);

            stats.damage = mode.damageMul * MAX_DAMAGE * damageScale;
            stats.splash = mode.splashDamageMul * MAX_SPLASH * splashScale;

            float electric01 = accel01(Mathf.clamp(chargeElectric / 2.8f, 0f, 1f));
            float lightningScale = Mathf.clamp(tierGate * 0.4f + amountGate * 0.4f + electric01 * 0.2f, 0f, 1f);
            stats.lightningDamage = mode.maxLightningDmg * lightningScale;

            if (mode.bullet instanceof LightningLinkerBulletType) {
                BulletType src = mode.bullet;
                float radiusCap = Math.max(src.splashDamageRadius, MIN_SPLASH_RADIUS) * radiusMul;
                stats.splashRadius = Math.min(stats.splashRadius, radiusCap);
                stats.lightning = src.lightning;
                stats.lightningLength = src.lightningLength;
            } else {
                stats.lightning = Mathf.clamp(Mathf.round(riseToMaxSoft(chargeElectric * 0.18f, 6f, 1.6f)), 0, 6);
                stats.lightningLength = Mathf.clamp(Mathf.round(riseToMaxSoft(chargeElectric * 0.25f, 10f, 1.8f)), 0, 10);
            }

            stats.flamePercent = riseToMaxSoft(chargeFlame * 3.2f, 300f, 5.5f);
            stats.plasmaFlame = stats.flamePercent >= 150f;

            stats.ammoColor.set(dominantChargeColor());

            float alertDown = Mathf.clamp(controlUtil / 2.6f, 0f, 1f);
            float alertUp = Mathf.clamp((chargeUtil * 0.45f + fuelUtil * 0.35f + shellUtil * 0.25f) / 2.8f, 0f, 1f);
            float alertT = Mathf.clamp(0.5f - alertDown * 0.5f + alertUp * 0.5f, 0f, 1f);
            stats.alertSeconds = Mathf.clamp(Mathf.lerp(MIN_ALERT_SECONDS, MAX_ALERT_SECONDS, alertT), MIN_ALERT_SECONDS, MAX_ALERT_SECONDS);
            float shotInterval = 0.065f;
            stats.raidSeconds = Math.max(stats.shotCount * shotInterval, 0.4f);
            stats.raidScale = stats.shotCount / stats.raidSeconds;
            return stats;
        }

        private int maxThreatLoaded() {
            int best = 0;
            for (int s = 0; s < SLOT_COUNT; s++) {
                best = Math.max(best, maxThreatInSlot(s));
            }
            return best;
        }

        private int maxThreatInSlot(int slot) {
            int[] best = {0};
            slots[slot].each((item, amount) -> {
                if (amount > 0) best[0] = Math.max(best[0], itemThreat(item));
            });
            return best[0];
        }

        private float itemWeight(Item item, float amount, int slot) {
            return threatFactor(item) * amountFactor(amount, slot);
        }

        private float utilWeight(Item item, float amount, int slot) {
            float cap = Math.max(slotCapacity(slot), 1f);
            float fill = Mathf.clamp(amount / cap, 0f, 1f);
            return utilThreatFactor(item) * utilAccel01(fill) / currentCostMul();
        }

        private Color dominantChargeColor() {
            Item[] best = {null};
            int[] bestAmt = {-1};
            slots[0].each((item, amount) -> {
                if (amount > bestAmt[0]) {
                    bestAmt[0] = amount;
                    best[0] = item;
                }
            });
            return best[0] != null ? best[0].color : Color.white;
        }

        private float amountFactor(float amount, int slot) {
            float cap = Math.max(slotCapacity(slot), 1f);
            return accel01(Mathf.clamp(amount / cap, 0f, 1f)) / currentCostMul();
        }

        private float softAttr(float value, float soft) {
            return soft * (1f - (float) Math.exp(-Math.max(value, 0f) / soft));
        }

        private float riseToMax(float score, float max, float refScore) {
            if (refScore <= 0.001f || max <= 0f) return 0f;
            return max * accel01(score / refScore);
        }

        private float riseToMaxSoft(float score, float max, float refScore) {
            if (refScore <= 0.001f || max <= 0f) return 0f;
            return max * utilAccel01(score / refScore);
        }

        private float slotContribution(int slot, ItemScore score) {
            float[] total = {0f};
            slots[slot].each((item, amount) -> total[0] += score.get(item, amount));
            return total[0];
        }

        public BulletType buildRaidBullet(RaidStats stats) {
            WeaponMode mode = weapons[weaponIndex];
            BulletType copy = mode.bullet.copy();
            Color ammoColor = stats.ammoColor;
            boolean linker = copy instanceof LightningLinkerBulletType;

            if (linker) {
                BulletType src = mode.bullet;
                copy.damage = stats.damage;
                copy.splashDamage = stats.splash;
                copy.splashDamageRadius = stats.splashRadius;
                copy.lightningDamage = stats.lightningDamage;
                copy.lightning = src.lightning;
                copy.lightningLength = src.lightningLength;
                copy.lightningLengthRand = src.lightningLengthRand;
                copy.lightningCone = src.lightningCone;
                copy.buildingDamageMultiplier = 1f;

                Color back = ammoColor.cpy();
                Color front = ammoColor.cpy().lerp(Color.white, 0.45f);
                if (copy instanceof BasicBulletType basic) {
                    basic.backColor = back;
                    basic.frontColor = front;
                }
                copy.trailColor = back.cpy();
                copy.hitColor = back.cpy();
                copy.lightColor = back.cpy();
                copy.lightningColor = back.cpy();
                copy.despawnEffect = NHFx.energyCircleOut(copy.splashDamageRadius * 1.5f);
                copy.hitEffect = NHFx.largeDarkEnergyHit;
                copy.shootEffect = NHFx.darkEnergyShootBig;
                copy.smokeEffect = NHFx.darkEnergySmokeBig;

                if (copy.fragBullet != null) {
                    BulletType frag = copy.fragBullet.copy();
                    boolean shareInterval = copy.intervalBullet == copy.fragBullet;
                    if (frag instanceof BasicBulletType fragBasic) {
                        fragBasic.backColor = back.cpy();
                        fragBasic.frontColor = front.cpy();
                    }
                    frag.trailColor = back.cpy();
                    frag.hitColor = back.cpy();
                    frag.lightColor = back.cpy();
                    frag.lightningColor = back.cpy();
                    frag.trailEffect = NHFx.polyTrail(4.65f, 22f);
                    frag.hitEffect = frag.despawnEffect = NHFx.darkErnExplosion;
                    copy.fragBullet = frag;
                    if (shareInterval) {
                        copy.intervalBullet = frag;
                    }
                }
            } else {
                copy.damage = stats.damage;
                copy.splashDamage = stats.splash;
                copy.splashDamageRadius = stats.splashRadius;
                copy.buildingDamageMultiplier = 1f;
                if (copy instanceof BasicRaidBulletType) {
                    copy.damage = Math.max(stats.damage, stats.splash);
                }
                copy.lightning = stats.lightning;
                copy.lightningLength = stats.lightningLength;
                copy.lightningLengthRand = Math.max(0, stats.lightningLength / 3);
                copy.lightningDamage = stats.lightningDamage;
                copy.lightningCone = 360f;
            }

            copy.speed = Math.max(stats.speed, 2.5f);
            copy.drag = 0f;
            copy.keepVelocity = false;
            copy.scaleLife = !linker;
            copy.scaledSplashDamage = false;
            copy.splashDamagePierce = true;
            if (linker) {
                copy.collidesTiles = false;
                copy.collideFloor = false;
            } else {
                copy.collides = true;
                copy.collidesAir = true;
                copy.collidesGround = true;
                copy.collidesTiles = true;
                copy.collideFloor = false;
            }

            if (copy instanceof AccelBulletType accel) {
                accel.velocityBegin = copy.speed;
                accel.velocityIncrease = 0f;
                accel.disableAccel();
                copy.homingPower = 0f;
                copy.homingRange = 0f;
                copy.despawnHit = true;
                copy.scaleLife = true;
            }

            float maxDist = Math.max(world.unitWidth() + world.unitHeight(), 64f);
            copy.lifetime = maxDist / copy.speed + 45f;
            if (!(copy instanceof LightningLinkerBulletType)) {
                copy.range = copy.speed * copy.lifetime;
            }

            if (copy instanceof BasicBulletType basic && !(copy instanceof LightningLinkerBulletType)) {
                basic.width = stats.size;
                basic.height = stats.size * 3.1f;
                basic.frontColor = ammoColor.cpy().lerp(Color.white, 0.25f);
                basic.backColor = ammoColor.cpy();
            }
            if (!linker) {
                copy.trailWidth = Math.max(2f, stats.size * 0.18f);
                copy.trailColor = ammoColor.cpy();
                copy.hitColor = ammoColor.cpy();
                copy.lightColor = ammoColor.cpy();
                copy.lightningColor = ammoColor.cpy();
            }

            if (copy instanceof AccelBulletType) {
                Color fx = ammoColor.cpy();
                float blastSize = Math.max(72f, copy.splashDamageRadius * 0.65f);
                if (mode.bullet == NHBullets.blastEnergyPst) {
                    copy.hitEffect = NHFx.crossBlast(fx, blastSize);
                    copy.despawnEffect = NHFx.hyperBlast(fx);
                } else {
                    copy.hitEffect = NHFx.lightningHitLarge(fx);
                    copy.despawnEffect = NHFx.crossBlast(fx, blastSize);
                }
                copy.shootEffect = NHFx.shootCircleSmall(fx);
            }

            if (!linker) {
                if (stats.pierce > 0) {
                    copy.pierce = true;
                    copy.pierceBuilding = true;
                    copy.pierceCap = stats.pierce;
                } else {
                    copy.pierce = false;
                    copy.pierceBuilding = false;
                    copy.pierceCap = -1;
                }
            }

            if (stats.flamePercent > 1f) {
                float chance = Mathf.clamp(stats.flamePercent / 200f, 0.05f, 1f);
                if (stats.plasmaFlame) {
                    copy.status = NHStatusEffects.ultFireBurn;
                    copy.statusDuration = 60f * 8f;
                    copy.incendChance = 0f;
                } else {
                    copy.status = StatusEffects.burning;
                    copy.statusDuration = 60f * (2f + stats.flamePercent / 50f);
                    copy.incendChance = chance;
                    copy.incendAmount = Mathf.clamp(Mathf.round(stats.flamePercent / 35f), 1, 10);
                    copy.incendSpread = 3f + stats.flamePercent * 0.02f;
                }
                Effect flameFx = makeFlameEffect(stats.flamePercent, Math.max(copy.splashDamageRadius * 0.55f, 28f), stats.plasmaFlame);
                if (copy.hitEffect != null && copy.hitEffect != Fx.none) {
                    copy.hitEffect = new OptionalMultiEffect(copy.hitEffect, flameFx);
                } else {
                    copy.hitEffect = flameFx;
                }
                if (copy.despawnEffect != null && copy.despawnEffect != Fx.none) {
                    copy.despawnEffect = new OptionalMultiEffect(copy.despawnEffect, flameFx);
                } else {
                    copy.despawnEffect = flameFx;
                }
            }

            if (copy instanceof BasicRaidBulletType) {
                copy.hitShake = copy.despawnShake = 8f + stats.size * 0.15f;
            }
            return copy;
        }

        private float payloadFill() {
            float sum = 0f;
            for (int i = 0; i < SLOT_COUNT; i++) {
                int cap = Math.max(slotCapacity(i), 1);
                sum += Mathf.clamp(slots[i].total() / (float) cap);
            }
            return sum / SLOT_COUNT;
        }

        private int scaledShotCount(WeaponMode mode) {
            if (mode.shotCountMin >= mode.shotCountMax) {
                return mode.shotCountMax;
            }
            float t = accel01(payloadFill());
            return Mathf.clamp(Math.round(Mathf.lerp(mode.shotCountMin, mode.shotCountMax, t)), mode.shotCountMin, mode.shotCountMax);
        }

        private Effect makeFlameEffect(float flamePercent, float radius, boolean plasma) {
            return new Effect(1f, e -> {
            }) {
                @Override
                public void create(float x, float y, float rotation, Color color, Object data) {
                    if (flamePercent <= 1f) return;
                    float chance = Mathf.clamp(flamePercent / 200f, 0.05f, 1f);
                    if (plasma) {
                        if (data instanceof Bullet b) {
                            UltFire.createChance(x, y, radius, chance, b.team);
                        } else {
                            UltFire.create(x, y, radius);
                        }
                    } else if (Mathf.chance(chance)) {
                        Tile tile = world.tileWorld(x, y);
                        if (tile != null) Fires.create(tile);
                        for (int i = 0; i < 4; i++) {
                            if (!Mathf.chance(chance * 0.45f)) continue;
                            Tile around = world.tileWorld(x + Mathf.range(radius * 0.35f), y + Mathf.range(radius * 0.35f));
                            if (around != null) Fires.create(around);
                        }
                    }
                }
            };
        }

        public void tryLaunchRaid() {
            if (raidActive) return;
            if (!isPowered()) return;
            if (weaponIndex < 0 || !hasPayload()) return;
            if (!canAffordPayload()) return;
            if (!hasTarget()) {
                target();
                if (!hasTarget()) return;
            }

            RaidStats stats = calcStats();
            BulletType raidBullet = buildRaidBullet(stats);
            if (!consumePayloadFromCore()) return;

            raidActive = true;
            raidExpectEnd = Time.time + (stats.alertSeconds + stats.raidSeconds) * Time.toSeconds;

            if (!RaidLogic.isLogicSide()) return;

            EventRaidAction action = new EventRaidAction();
            action.raidType = RaidPreset.CUSTOM_RAID;
            action.customBullet = raidBullet;
            action.keyBullet = weapons[weaponIndex].bullet;
            action.team = team;
            action.overrideRaidStats = true;
            action.gatedByRaidState = false;
            action.spawnBullets = true;
            action.alertTime = stats.alertSeconds * Time.toSeconds;
            action.raidTime = stats.raidSeconds * Time.toSeconds;
            action.raidScale = stats.raidScale;
            action.inaccuracy = stats.inaccuracy;
            action.overrideDefaultCoordinate = true;
            action.sourceX = x;
            action.sourceY = y;
            action.targetX = lastConfirmedTarget.x;
            action.targetY = lastConfirmedTarget.y;
            action.syncSeed = tileX() * 31 + tileY() * 17 + (int) Time.time;
            action.postInit();

            raidAction = action;
            raidBus = new ActionBus();
            raidBus.add(action);
            cutscene.addSubActionBus(raidBus);
            RaidSync.registerLogicAction(action);
        }

        public void cancelRaidEvent() {
            if (!raidActive && raidBus == null && raidAction == null) return;
            if (!RaidLogic.isLogicSide()) {
                clearRaidRefs();
                return;
            }
            if (raidAction != null) {
                raidAction.lifeTimer = raidAction.duration;
                removeRaidMarkers(raidAction);
                RaidSync.unregisterLogicAction(raidAction);
            }
            if (raidBus != null) {
                raidBus.skip();
                cutscene.subBuses.remove(raidBus);
            }
            clearRaidRefs();
            if (net.server() && net.active()) {
                RaidSync.broadcastState();
            }
        }

        private void removeRaidMarkers(EventRaidAction action) {
            if (headless || cutsceneUI == null) return;
            for (int i = cutsceneUI.markers.size - 1; i >= 0; i--) {
                HudMarker marker = cutsceneUI.markers.get(i);
                if (marker.kind != HudMarker.Kind.RAID) continue;
                if (Mathf.dst(marker.markPoint.x, marker.markPoint.y, action.targetX, action.targetY) < 8f) {
                    marker.remove();
                }
            }
        }

        private void clearRaidRefs() {
            if (raidAction != null) {
                RaidSync.unregisterLogicAction(raidAction);
            }
            raidBus = null;
            raidAction = null;
            raidActive = false;
            raidExpectEnd = 0f;
        }

        @Override
        public void remove() {
            cancelRaidEvent();
            clearAllSlots();
            super.remove();
        }

        @Override
        public boolean interactable(Team team) {
            return team == this.team;
        }

        @Override
        public boolean configTapped() {
            return player != null && player.team() == team && super.configTapped();
        }

        @Override
        public void buildConfiguration(Table table) {
            if (player == null || player.team() != team) {
                deselect();
                return;
            }
            control.input.selectedBlock();
            float len = ui(LEN);
            table.table(Tex.paneSolid, t -> {
                t.button("@mod.ui.air-raid-settings", Icon.modeAttack, Styles.cleart, len, this::showRaidDialog)
                        .size(len * 4, len).row();
                t.button("@mod.ui.air-raid-select-pos", Icon.move, Styles.cleart, len, () ->
                        TableFunc.selectPos(t, p -> {
                            configure(p);
                            command(new Vec2(World.unconv(p.x), World.unconv(p.y)));
                        })
                ).size(len * 4, len).row();
            }).fill();
        }

        public void showRaidDialog() {
            if (player == null || player.team() != team) return;
            BaseDialog dialog = new BaseDialog("@mod.ui.air-raid-settings");
            dialog.addCloseListener();

            float dialogW = dialogWidth(1080f, 0.84f);
            float dialogH = dialogHeight(680f, 0.78f);
            float leftW = dialogW / 3f;
            float rightW = dialogW - leftW;
            float topH = dialogH / 3f;
            float bottomH = dialogH - topH;
            float[] uiSlot = {selectedSlot};

            Runnable[] rebuildHold = {null};
            Runnable rebuild = () -> {
                if (rebuildHold[0] != null) rebuildHold[0].run();
            };
            rebuildHold[0] = () -> {
                dialog.cont.clearChildren();
                buildRaidContent(dialog, dialogW, leftW, rightW, topH, bottomH, uiSlot, rebuild);
            };
            rebuild.run();
            dialog.show();
        }

        private void buildRaidContent(BaseDialog dialog, float dialogW, float leftW, float rightW, float topH, float bottomH, float[] uiSlot, Runnable rebuild) {
            selectedSlot = (int) uiSlot[0];
            float dialogH = topH + bottomH;
            float len = Mathf.clamp(ui(LEN), 42f, dialogH * 0.085f);
            float pad = ui(4f);
            float iconSm = Mathf.clamp(ui(28f), 22f, dialogH * 0.045f);

            dialog.cont.table(main -> {
                main.table(top -> {
                    top.table(Styles.black3, tl -> {
                        tl.top().left().defaults().pad(pad);
                        tl.add("@nh.air-raid.select-weapon").color(Pal.accent).left().pad(ui(6f)).padBottom(pad).growX().wrap().row();
                        tl.pane(Styles.noBarPane, body -> {
                            body.top();
                            body.defaults().pad(pad);
                            if (weaponIndex < 0) {
                                body.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(ui(12f));
                            } else {
                                Image icon = new Image(weaponIcons[weaponIndex]);
                                icon.setScaling(Scaling.fit);
                                body.add(icon).size(ui(72f)).padBottom(ui(6f)).row();
                                body.add(Core.bundle.get(weapons[weaponIndex].bundleKey + ".name")).padBottom(pad).row();
                                body.add(Core.bundle.get(weapons[weaponIndex].bundleKey + ".desc"))
                                        .wrap().growX().pad(pad).padBottom(ui(8f)).color(Pal.lightishGray).labelAlign(Align.left);
                            }
                        }).grow().pad(ui(6f)).padBottom(ui(2f));
                        tl.row();
                        tl.button("@nh.air-raid.change-weapon", Icon.pencil, Styles.cleart, () -> showWeaponPicker(uiSlot, rebuild))
                                .growX().height(len - ui(4f)).pad(ui(6f));
                    }).size(leftW, topH).pad(ui(2f));

                    top.table(Styles.black3, tr -> {
                        tr.top().left().margin(ui(8f));
                        tr.add("@nh.air-raid.select-shell").color(Pal.accent).left().padBottom(ui(8f)).row();
                        if (weaponIndex < 0) {
                            tr.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(ui(16f));
                        } else {
                            tr.table(missile -> {
                                missile.top().center();

                                float partH = Math.min(Mathf.clamp(ui(140f), 64f, topH * 0.62f), Math.max(ui(64f), topH - ui(100f)));
                                float partW = partH * (192f / 96f);
                                float stepRatio = 0f;
                                float[] hitWeights = {50f, 26f, 68f, 104f};

                                float step = partW * stepRatio;
                                float assembleW = partW + (SLOT_COUNT - 1) * step;

                                if (stepRatio <= 0.0001f) {
                                    Stack stack = new Stack();
                                    for (int s = 0; s < SLOT_COUNT; s++) {
                                        int slot = s;
                                        Image icon = new Image(shellIcons[weaponIndex][slot]);
                                        icon.setScaling(Scaling.fit);
                                        icon.update(() -> icon.setColor((int) uiSlot[0] == slot ? Pal.accent : Color.white));
                                        stack.add(icon);
                                    }
                                    Table hitOverlay = new Table();
                                    hitOverlay.setFillParent(true);
                                    float weightSum = 0f;
                                    for (float w : hitWeights) weightSum += w;
                                    for (int s = 0; s < SLOT_COUNT; s++) {
                                        int slot = s;
                                        Table zone = new Table();
                                        zone.addListener(new HandCursorListener());
                                        zone.clicked(() -> selectRaidSlot(uiSlot, slot, rebuild));
                                        hitOverlay.add(zone).growY().width(partW * (hitWeights[s] / weightSum));
                                    }
                                    stack.add(hitOverlay);
                                    missile.add(stack).size(partW, partH).padBottom(ui(10f)).row();
                                } else {
                                    missile.table(icons -> {
                                        icons.left().top().defaults().pad(0f);
                                        for (int s = 0; s < SLOT_COUNT; s++) {
                                            int slot = s;
                                            Table hit = new Table();
                                            hit.addListener(new HandCursorListener());
                                            hit.clicked(() -> selectRaidSlot(uiSlot, slot, rebuild));
                                            Image icon = new Image(shellIcons[weaponIndex][slot]);
                                            icon.setScaling(Scaling.fit);
                                            icon.update(() -> icon.setColor((int) uiSlot[0] == slot ? Pal.accent : Color.white));
                                            hit.add(icon).size(partW, partH).scaling(Scaling.fit);
                                            var cell = icons.add(hit).size(partW, partH);
                                            if (s > 0) cell.padLeft(-(partW - step));
                                        }
                                    }).width(assembleW).left().padBottom(ui(10f)).row();
                                }

                                missile.table(labels -> {
                                    labels.defaults().pad(ui(6f)).uniformX().growX().height(len - ui(8f));
                                    for (int s = 0; s < SLOT_COUNT; s++) {
                                        int slot = s;
                                        labels.button(Core.bundle.get(slotDefs[slot].bundleKey), Styles.togglet, () -> selectRaidSlot(uiSlot, slot, rebuild))
                                                .checked(b -> (int) uiSlot[0] == slot).pad(ui(6f));
                                    }
                                }).growX().padTop(ui(8f));
                            }).grow().center();
                        }
                    }).size(rightW, topH).pad(ui(2f));
                }).growX().height(topH).row();

                main.table(bottom -> {
                    bottom.table(Styles.black3, bl -> {
                        bl.top().left().margin(ui(8f));
                        bl.add("@nh.air-raid.loaded").color(Pal.accent).padBottom(ui(6f)).left().row();
                        bl.pane(list -> {
                            list.top().left();
                            if (weaponIndex < 0) {
                                list.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(ui(8f));
                            } else {
                                ItemModule module = slots[selectedSlot];
                                boolean[] any = {false};
                                module.each((item, amount) -> {
                                    any[0] = true;
                                    list.table(row -> {
                                        row.left();
                                        row.image(item.uiIcon).size(iconSm).scaling(Scaling.fit).pad(pad);
                                        row.add(item.localizedName + " x" + amount).growX().left().padLeft(ui(6f));
                                        row.button(Icon.cancel, Styles.clearNonei, () -> {
                                            configure(IntSeq.with(1, selectedSlot, item.id, 0));
                                            rebuild.run();
                                        }).size(ui(28f));
                                    }).growX().pad(ui(2f)).row();
                                });
                                if (!any[0]) {
                                    list.add("@nh.air-raid.empty-slot").color(Pal.lightishGray).pad(ui(8f));
                                }
                            }
                        }).grow().pad(ui(2f));
                        bl.row();
                        bl.button("@nh.air-raid.clear-slot", Icon.trash, Styles.cleart, () -> {
                            configure(IntSeq.with(4, selectedSlot));
                            rebuild.run();
                        }).growX().height(len - ui(8f)).padTop(pad).disabled(b -> weaponIndex < 0);
                    }).size(leftW, bottomH).pad(ui(2f));

                    bottom.table(Styles.black3, br -> {
                        br.top().left().margin(ui(8f));
                        br.add("@nh.air-raid.select-items").color(Pal.accent).padBottom(ui(6f)).left().row();
                        if (weaponIndex < 0) {
                            br.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(ui(16f));
                        } else {
                            SlotDef def = slotDefs[selectedSlot];
                            Building core = team.core();
                            boolean infinite = state.rules.infiniteResources || team.rules().cheat;
                            int capacity = slotCapacity(selectedSlot);

                            br.pane(list -> {
                                list.top().left();
                                for (Item item : def.allowed) {
                                    int have = core == null ? 0 : core.items.get(item);
                                    int loaded = slots[selectedSlot].get(item);
                                    int others = slots[selectedSlot].total() - loaded;
                                    int maxForItem = Math.max(0, capacity - others);
                                    if (!infinite) maxForItem = Math.min(maxForItem, have);

                                    int max = Math.max(maxForItem, loaded);
                                    int[] val = {loaded};
                                    Label amountLabel = new Label(loaded + "/" + max);
                                    amountLabel.setAlignment(Align.right);
                                    Slider slider = new Slider(0, Math.max(max, 1), 1, false);
                                    slider.setValue(loaded);
                                    slider.setDisabled(max <= 0 && loaded <= 0);
                                    Runnable commit = () -> {
                                        int nv = Mathf.clamp(val[0], 0, max);
                                        val[0] = nv;
                                        amountLabel.setText(nv + "/" + max);
                                        if (Math.abs(slider.getValue() - nv) > 0.01f) slider.setValue(nv);
                                        if (nv != slots[selectedSlot].get(item)) {
                                            configure(IntSeq.with(1, selectedSlot, item.id, nv));
                                            Core.app.post(rebuild);
                                        }
                                    };
                                    slider.moved(v -> {
                                        int nv = (int) v;
                                        val[0] = nv;
                                        amountLabel.setText(nv + "/" + max);
                                    });
                                    slider.addListener(new InputListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                                            commit.run();
                                        }
                                    });

                                    list.table(row -> {
                                        row.left();
                                        row.image(item.uiIcon).size(iconSm).scaling(Scaling.fit).padRight(pad);
                                        row.add(item.localizedName).left().width(ui(88f)).padRight(pad).ellipsis(true);
                                        row.button("-", Styles.cleart, () -> {
                                            val[0] = Math.max(0, val[0] - 1);
                                            commit.run();
                                        }).size(ui(28f)).padRight(ui(2f)).disabled(b -> max <= 0 && loaded <= 0);
                                        row.add(slider).growX().height(ui(36f)).padRight(ui(2f));
                                        row.button("+", Styles.cleart, () -> {
                                            val[0] = Math.min(max, val[0] + 1);
                                            commit.run();
                                        }).size(ui(28f)).padRight(pad).disabled(b -> max <= 0 && loaded <= 0);
                                        row.add(amountLabel).minWidth(ui(72f)).right().padRight(ui(2f));
                                    }).growX().pad(ui(2f)).row();
                                }
                            }).grow().pad(ui(2f));
                        }
                    }).size(rightW, bottomH).pad(ui(2f));
                }).growX().height(bottomH).row();

                main.table(Tex.pane, statsBar -> {
                    statsBar.top().left();
                    statsBar.table(info -> {
                        info.left().top();
                        info.label(() -> {
                            RaidStats stats = calcStats();
                            return Core.bundle.format("nh.air-raid.stats",
                                    Strings.fixed(stats.damage, 0),
                                    Strings.fixed(stats.size, 1),
                                    Strings.fixed(stats.speed, 1),
                                    Strings.fixed(stats.alertSeconds, 0),
                                    stats.shotCount,
                                    stats.pierce,
                                    Strings.fixed(stats.inaccuracy, 0),
                                    stats.lightning,
                                    stats.lightningLength,
                                    Strings.fixed(stats.lightningDamage, 0),
                                    Strings.fixed(stats.splashRadius / tilesize, 1),
                                    Strings.fixed(stats.splash, 0)
                            );
                        }).left().growX().wrap().labelAlign(Align.left);
                    }).growX().left().pad(ui(6f));
                    statsBar.table(actions -> {
                        actions.defaults().pad(ui(3f));
                        actions.button("@back", Icon.left, Styles.cleart, dialog::hide).size(len * 2f, len);
                        actions.button("@nh.air-raid.cancel-raid", Icon.cancel, Styles.cleart, () -> {
                            configure(false);
                            rebuild.run();
                        }).size(len * 2.4f, len).disabled(b -> !raidActive);
                        actions.button("@nh.air-raid.launch", Icon.ok, Styles.cleart, () -> {
                            configure(true);
                            Core.app.post(() -> {
                                if (raidActive) dialog.hide();
                                else rebuild.run();
                            });
                        }).size(len * 2.6f, len).disabled(b -> raidActive || !isPowered() || weaponIndex < 0 || !hasPayload() || !canAffordPayload() || !hasTarget());
                    }).right().pad(pad);
                }).width(dialogW).minHeight(len * 2.2f).padTop(pad);
            });
        }

        private void selectRaidSlot(float[] uiSlot, int slot, Runnable rebuild) {
            int next = Mathf.clamp(slot, 0, SLOT_COUNT - 1);
            if ((int) uiSlot[0] == next && selectedSlot == next) return;
            uiSlot[0] = next;
            selectedSlot = next;
            configure(IntSeq.with(5, next));
            rebuild.run();
        }

        private void showWeaponPicker(float[] uiSlot, Runnable parentRebuild) {
            BaseDialog picker = new BaseDialog("@nh.air-raid.select-weapon");
            picker.addCloseListener();

            float cardW = Mathf.clamp(ui(210f), 160f, Core.graphics.getWidth() * 0.2f);
            float cardH = Mathf.clamp(ui(280f), 210f, Core.graphics.getHeight() * 0.42f);
            float iconSize = Mathf.clamp(ui(84f), 56f, cardH * 0.28f);
            float textW = cardW - ui(30f);
            float descH = cardH * 0.36f;

            Table cards = new Table();
            cards.left().top();
            for (int i = 0; i < WEAPON_COUNT; i++) {
                int idx = i;
                WeaponMode mode = weapons[idx];
                cards.table(Tex.pane, card -> {
                    card.top().margin(ui(8f));
                    card.addListener(new HandCursorListener());
                    card.clicked(() -> {
                        uiSlot[0] = 0;
                        configure(IntSeq.with(0, idx));
                        picker.hide();
                        parentRebuild.run();
                    });
                    Image icon = new Image(weaponIcons[idx]);
                    icon.setScaling(Scaling.fit);
                    card.add(icon).size(iconSize).padTop(ui(8f)).padBottom(ui(8f)).row();
                    card.add(Core.bundle.get(mode.bundleKey + ".name")).padBottom(ui(6f)).wrap().width(textW).labelAlign(Align.center).row();
                    card.pane(Styles.noBarPane, desc -> {
                        desc.top();
                        desc.add(Core.bundle.get(mode.bundleKey + ".desc"))
                                .wrap().width(textW - ui(10f)).color(Pal.lightishGray).labelAlign(Align.left);
                    }).size(textW, descH).pad(ui(6f));
                    if (weaponIndex == idx) {
                        card.setColor(Pal.accent);
                    }
                }).size(cardW, cardH).pad(ui(10f));
            }

            ScrollPane pane = new ScrollPane(cards);
            pane.setScrollingDisabledY(true);
            pane.setFadeScrollBars(false);
            picker.cont.top();
            picker.cont.add(pane).grow().pad(ui(12f));
            picker.buttons.button("@back", Icon.left, picker::hide).size(Math.min(ui(200f), Core.graphics.getWidth() * 0.25f), ui(52f));
            picker.show();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(target);
            TypeIO.writeVec2(write, lastConfirmedTarget);
            write.b((byte) weaponIndex);
            write.b((byte) selectedSlot);
            write.bool(raidActive);
            for (ItemModule slot : slots) {
                slot.write(write);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            target = read.i();
            TypeIO.readVec2(read, lastConfirmedTarget);
            if (!lastConfirmedTarget.isZero()) {
                targetVec.set(lastConfirmedTarget);
            }
            weaponIndex = read.b();
            selectedSlot = Mathf.clamp(read.b(), 0, SLOT_COUNT - 1);
            raidActive = read.bool();
            for (ItemModule slot : slots) {
                slot.read(read);
            }
            if (raidActive) {
                raidActive = false;
            }
        }
    }

    public static class WeaponMode {
        public final String bundleKey;
        public final BulletType bullet;
        public final float damageMul, splashDamageMul, splashRangeMul, sizeMul, speedMul, inaccuracy, costMul, maxLightningDmg;
        public final int shotCountMin, shotCountMax;

        public WeaponMode(String bundleKey, BulletType bullet, float damageMul, float splashDamageMul, float splashRangeMul, float sizeMul, float speedMul, float inaccuracy, float costMul, float maxLightningDmg, int shotCount) {
            this(bundleKey, bullet, damageMul, splashDamageMul, splashRangeMul, sizeMul, speedMul, inaccuracy, costMul, maxLightningDmg, shotCount, shotCount);
        }

        public WeaponMode(String bundleKey, BulletType bullet, float damageMul, float splashDamageMul, float splashRangeMul, float sizeMul, float speedMul, float inaccuracy, float costMul, float maxLightningDmg, int shotCountMin, int shotCountMax) {
            this.bundleKey = bundleKey;
            this.bullet = bullet;
            this.damageMul = damageMul;
            this.splashDamageMul = splashDamageMul;
            this.splashRangeMul = splashRangeMul;
            this.sizeMul = sizeMul;
            this.speedMul = speedMul;
            this.inaccuracy = inaccuracy;
            this.costMul = Math.max(costMul, 0.0001f);
            this.maxLightningDmg = Math.max(maxLightningDmg, 0f);
            this.shotCountMin = Math.min(shotCountMin, shotCountMax);
            this.shotCountMax = Math.max(shotCountMin, shotCountMax);
        }
    }

    public static class SlotDef {
        public final String bundleKey;
        public final int capacity;
        public final Seq<Item> allowed;

        public SlotDef(String bundleKey, int capacity, Seq<Item> allowed) {
            this.bundleKey = bundleKey;
            this.capacity = capacity;
            this.allowed = allowed;
        }

        public boolean allows(Item item) {
            return allowed.contains(item);
        }
    }

    public static class RaidStats {
        public float damage, size, speed, alertSeconds, raidSeconds, raidScale, inaccuracy, splash, splashRadius, explosiveness;
        public float chargeScore, controlScore, fuelScore, shellScore;
        public float lightningDamage, flamePercent;
        public int pierce, shotCount, lightning, lightningLength;
        public boolean plasmaFlame;
        public final Color ammoColor = new Color(Color.white);
    }

    private interface ItemScore {
        float get(Item item, float amount);
    }
}

























