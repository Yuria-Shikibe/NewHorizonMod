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
import newhorizon.NHGroups;
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
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.components.ui.RaidMarker;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class AirRaider extends CommandableBlock {
    public static final int SLOT_COUNT = 4;
    public static final int WEAPON_COUNT = 6;
    public static final float MAX_ALERT_SECONDS = 240f;
    public static final float BASE_ALERT_SECONDS = 120f;
    public static final float MAX_SPREAD = 240f;
    public static final float MAX_DAMAGE = 10000f;
    public static final float MAX_SPLASH = 5000f;

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
        weapons[0] = new WeaponMode("nh.air-raid.weapon-1", RaidBullets.defaultRaidBullet1, 1f, 1f, 1f, 40f, 10);
        weapons[1] = new WeaponMode("nh.air-raid.weapon-2", NHBullets.arc_9000, 0.8f, 0.9f, 0.4f, 50f, 3);
        weapons[2] = new WeaponMode("nh.air-raid.weapon-3", RaidBullets.raidBullet_9, 0.4f, 0.5f, 2f, 80f, 4,20);
        weapons[3] = new WeaponMode("nh.air-raid.weapon-4", NHBullets.blastEnergyNgt, 0.1f, 0.2f, 4f, 25f, 40,120);
        weapons[4] = new WeaponMode("nh.air-raid.weapon-5", NHBullets.railGun1, 3f, 0.4f, 8f, 5f, 1);
        weapons[5] = new WeaponMode("nh.air-raid.weapon-6", NHBullets.airRaidBomb, 1.2f, 1.3f, 2f, 48f, 2, 20);

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

    public static float techMul(Item item) {
        return (float) Math.pow(1.36f, techDepth(item));
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
            if (raidActive && (raidBus == null || raidBus.complete())) {
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
            for (CommandableBlockBuild build : NHGroups.commandableBuilds) {
                if (build.team == team && sameGroup(build.block) && build.canCommand(pos)) {
                    build.command(pos);
                }
            }
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
            Seq<CommandableBlockBuild> builds = new Seq<>();
            for (CommandableBlockBuild build : NHGroups.commandableBuilds) {
                if (build != this && build != null && build.team == team && sameGroup(build.block) && build.canCommand(targetVec)) {
                    builds.add(build);
                    DrawFunc.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, targetVec.x, targetVec.y);
                }
            }
            for (CommandableBlockBuild build : builds) {
                DrawFunc.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, targetVec.x, targetVec.y);
            }
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
            int maxForItem = Math.max(0, slotDefs[slot].capacity - others);
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

        public void clearAllSlots() {
            for (ItemModule slot : slots) slot.clear();
        }

        public RaidStats calcStats() {
            RaidStats stats = new RaidStats();
            if (weaponIndex < 0 || weaponIndex >= WEAPON_COUNT) return stats;
            WeaponMode mode = weapons[weaponIndex];

            float chargePower = slotContribution(0, (item, amt) ->
                    amountFactor(amt) * techMul(item) * (
                            0.4f
                                    + softAttr(item.explosiveness, 1.45f) * 0.9f
                                    + softAttr(item.flammability, 1.6f) * 0.25f
                                    + softAttr(item.charge, 3.2f) * 0.38f
                    ));
            float controlPower = slotContribution(1, (item, amt) ->
                    amountFactor(amt) * techMul(item) * (
                            0.45f
                                    + softAttr(item.charge, 3.2f) * 0.28f
                                    + softAttr(item.radioactivity, 3f) * 0.35f
                                    + techDepth(item) * 0.08f
                    ));
            float fuelPower = slotContribution(2, (item, amt) ->
                    amountFactor(amt) * techMul(item) * (
                            0.38f
                                    + softAttr(item.flammability, 1.6f) * 0.95f
                                    + softAttr(item.explosiveness, 1.45f) * 0.22f
                                    + softAttr(item.charge, 3.2f) * 0.2f
                    ));
            float shellPower = slotContribution(3, (item, amt) ->
                    amountFactor(amt) * techMul(item) * (
                            0.32f + softAttr((float) Math.log1p(Math.max(item.hardness, 0)), 3.8f) * 0.95f
                    ));

            float chargeExplosiveness = slotContribution(0, (item, amt) ->
                    amountFactor(amt) * techMul(item) * softAttr(item.explosiveness, 1.45f));
            float chargeElectric = slotContribution(0, (item, amt) ->
                    amountFactor(amt) * techMul(item) * softAttr(item.charge, 3.2f));
            float chargeFlame = slotContribution(0, (item, amt) ->
                    amountFactor(amt) * techMul(item) * softAttr(item.flammability, 1.6f));

            float piercePower = slotContribution(3, (item, amt) -> {
                float hard = softAttr((float) Math.log1p(Math.max(item.hardness - 4f, 0f)), 3.2f);
                return amountFactor(amt) * hard * (0.35f + techDepth(item) * 0.12f);
            });

            stats.chargeScore = chargePower;
            stats.controlScore = controlPower;
            stats.fuelScore = fuelPower;
            stats.shellScore = shellPower;
            stats.explosiveness = chargeExplosiveness;

            float damageScore = chargePower * 0.22f + shellPower * 0.14f + fuelPower * 0.08f + controlPower * 0.07f;
            float sizeScore = shellPower * 0.12f + chargePower * 0.035f;
            float speedScore = fuelPower * 0.14f + controlPower * 0.05f;
            stats.size = Mathf.clamp(mode.sizeMul * (10f + approachMax(sizeScore, 54f, 28f)), 8f, 64f);
            stats.speed = Mathf.clamp(mode.speedMul * (3.2f + approachMax(speedScore, 26f, 22f)), 2.5f, 30f);
            stats.shotCount = scaledShotCount(mode);
            stats.pierce = Mathf.clamp((int) Math.floor(approachMax(piercePower, 10f, 14f)), 0, 10);

            float splashScore = 3.5f + chargePower * 0.08f + chargeExplosiveness * 0.12f + shellPower * 0.015f;
            float spreadFromExpl = chargeExplosiveness * 1.15f;
            stats.inaccuracy = Mathf.clamp(mode.inaccuracy + spreadFromExpl, 8f, MAX_SPREAD);

            if (mode.bullet instanceof LightningLinkerBulletType) {
                float load = Mathf.clamp(0.4f + payloadFill() * 0.9f, 0.4f, 1.35f);
                float mul = mode.damageMul * load;
                BulletType src = mode.bullet;
                stats.damage = src.damage * mul;
                stats.splash = src.splashDamage * mul;
                stats.splashRadius = Math.max(src.splashDamageRadius, Mathf.clamp(40f + stats.size * 1.55f + chargeExplosiveness * 0.55f, 40f, 220f) * 0.65f);
                stats.lightning = src.lightning;
                stats.lightningLength = src.lightningLength;
                stats.lightningDamage = (src.lightningDamage > 0f ? src.lightningDamage : src.damage) * mul;
            } else {
                stats.damage = mode.damageMul * approachMax(damageScore, MAX_DAMAGE, 85f);
                stats.splash = mode.damageMul * approachMax(splashScore, MAX_SPLASH, 18f);
                stats.splashRadius = Mathf.clamp(40f + stats.size * 1.55f + chargeExplosiveness * 0.55f, 40f, 220f);
                stats.lightning = Mathf.clamp(Mathf.round(approachMax(chargeElectric * 0.12f, 6f, 9f)), 0, 6);
                stats.lightningLength = Mathf.clamp(Mathf.round(approachMax(chargeElectric * 0.18f, 10f, 11f)), 0, 10);
                stats.lightningDamage = Mathf.clamp(approachMax(chargeElectric * 1.4f, 100f, 55f), 0f, 100f);
            }

            stats.flamePercent = approachMax(chargeFlame * 3.2f, 300f, 55f);
            stats.plasmaFlame = stats.flamePercent >= 150f;

            stats.ammoColor.set(dominantChargeColor());

            float alertDelta =
                    -controlPower * 0.55f
                            + chargePower * 0.12f
                            + fuelPower * 0.1f
                            + shellPower * 0.08f;
            stats.alertSeconds = Mathf.clamp(BASE_ALERT_SECONDS + alertDelta, 12f, MAX_ALERT_SECONDS);
            float shotInterval = 0.065f;
            stats.raidSeconds = Math.max(stats.shotCount * shotInterval, 0.4f);
            stats.raidScale = stats.shotCount / stats.raidSeconds;
            return stats;
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

        private float amountFactor(float amount) {
            return softAttr(amount * 0.1f, 36f);
        }

        private float softAttr(float value, float soft) {
            return soft * (1f - (float) Math.exp(-Math.max(value, 0f) / soft));
        }

        private float approachMax(float score, float max, float halfLife) {
            if (halfLife <= 0.001f) return 0f;
            return max * (1f - (float) Math.exp(-Math.max(score, 0f) / halfLife));
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
                float load = Mathf.clamp(0.4f + payloadFill() * 0.9f, 0.4f, 1.35f);
                float mul = mode.damageMul * load;
                BulletType src = mode.bullet;
                copy.damage = src.damage * mul;
                copy.splashDamage = src.splashDamage * mul;
                copy.splashDamageRadius = Math.max(src.splashDamageRadius, stats.splashRadius * 0.65f);
                float srcLightning = src.lightningDamage > 0f ? src.lightningDamage : src.damage;
                copy.lightningDamage = srcLightning * mul;
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

            if (copy instanceof AccelBulletType accel) {
                accel.velocityBegin = copy.speed;
                accel.velocityIncrease = 0f;
                accel.disableAccel();
                copy.homingPower = 0f;
                copy.homingRange = 0f;
                copy.despawnHit = true;
                copy.scaleLife = true;
            }

            float maxDist = Math.max(Math.min(range, world.unitWidth() + world.unitHeight()), 64f);
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
                int cap = Math.max(slotDefs[i].capacity, 1);
                sum += Mathf.clamp(slots[i].total() / (float) cap);
            }
            return sum / SLOT_COUNT;
        }

        private int scaledShotCount(WeaponMode mode) {
            if (mode.shotCountMin >= mode.shotCountMax) {
                return mode.shotCountMax;
            }
            float fill = payloadFill();
            float t = 1f - (float) Math.exp(-fill * 2.35f);
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
            if (player != null && player.team() != team) return;
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

            EventRaidAction action = new EventRaidAction();
            action.raidType = RaidPreset.CUSTOM_RAID;
            action.customBullet = raidBullet;
            action.keyBullet = weapons[weaponIndex].bullet;
            action.team = team;
            action.overrideRaidStats = true;
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
            raidActive = true;
        }

        public void cancelRaidEvent() {
            if (!raidActive && raidBus == null && raidAction == null) return;
            if (raidAction != null) {
                raidAction.lifeTimer = raidAction.duration;
                removeRaidMarkers(raidAction);
            }
            if (raidBus != null) {
                raidBus.skip();
                cutscene.subBuses.remove(raidBus);
            }
            clearRaidRefs();
        }

        private void removeRaidMarkers(EventRaidAction action) {
            if (headless || cutsceneUI == null) return;
            for (int i = cutsceneUI.markers.size - 1; i >= 0; i--) {
                HudMarker marker = cutsceneUI.markers.get(i);
                if (marker instanceof RaidMarker && Mathf.dst(marker.markPoint.x, marker.markPoint.y, action.targetX, action.targetY) < 8f) {
                    marker.remove();
                }
            }
        }

        private void clearRaidRefs() {
            raidBus = null;
            raidAction = null;
            raidActive = false;
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
            table.table(Tex.paneSolid, t -> {
                t.button("@mod.ui.air-raid-settings", Icon.modeAttack, Styles.cleart, LEN, this::showRaidDialog)
                        .size(LEN * 4, LEN).row();
                t.button("@mod.ui.air-raid-select-pos", Icon.move, Styles.cleart, LEN, () ->
                        TableFunc.selectPos(t, p -> {
                            configure(p);
                            command(new Vec2(World.unconv(p.x), World.unconv(p.y)));
                        })
                ).size(LEN * 4, LEN).row();
            }).fill();
        }

        public void showRaidDialog() {
            if (player == null || player.team() != team) return;
            BaseDialog dialog = new BaseDialog("@mod.ui.air-raid-settings");
            dialog.addCloseListener();

            float dialogW = Math.min(Core.graphics.getWidth() * 0.92f, 1280f);
            float dialogH = Math.min(Core.graphics.getHeight() * 0.88f, 820f);
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

            dialog.cont.table(main -> {
                main.table(top -> {
                    top.table(Styles.black3, tl -> {
                        tl.top().left().defaults().pad(4f);
                        tl.add("@nh.air-raid.select-weapon").color(Pal.accent).left().pad(6f).padBottom(4f).growX().wrap().row();
                        tl.pane(Styles.noBarPane, body -> {
                            body.top();
                            body.defaults().pad(4f);
                            if (weaponIndex < 0) {
                                body.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(12f);
                            } else {
                                Image icon = new Image(weaponIcons[weaponIndex]);
                                icon.setScaling(Scaling.fit);
                                body.add(icon).size(72f).padBottom(6f).row();
                                body.add(Core.bundle.get(weapons[weaponIndex].bundleKey + ".name")).padBottom(4f).row();
                                body.add(Core.bundle.get(weapons[weaponIndex].bundleKey + ".desc"))
                                        .wrap().growX().pad(4f).padBottom(8f).color(Pal.lightishGray).labelAlign(Align.left);
                            }
                        }).grow().pad(6f).padBottom(2f);
                        tl.row();
                        tl.button("@nh.air-raid.change-weapon", Icon.pencil, Styles.cleart, () -> showWeaponPicker(rebuild))
                                .growX().height(LEN - 4f).pad(6f);
                    }).size(leftW, topH).pad(2f);

                    top.table(Styles.black3, tr -> {
                        tr.top().left().margin(8f);
                        tr.add("@nh.air-raid.select-shell").color(Pal.accent).left().padBottom(8f).row();
                        if (weaponIndex < 0) {
                            tr.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(16f);
                        } else {
                            // 弹药部件预览：s1–s4 画在同一 192x96 坐标系，必须同位置叠放。
                            // 手工调参只改下面这几项即可。
                            tr.table(missile -> {
                                missile.top().center();

                                // ---- 手工调参开始 ----
                                float partH = Math.min(168f, topH - 110f); // 整弹显示高度
                                float partW = partH * (192f / 96f);          // 保持贴图宽高比 2:1
                                // 若以后改成“横移拼接”而不是叠放：stepRatio>0，每段相对画布宽度的步进
                                // 0 = 完全重叠（当前正确做法）；0.2 = 每段向右挪 20% 画布宽
                                float stepRatio = 0f;
                                // 点击热区相对宽度（对应 s1..s4 不透明区域大致占比，可改）
                                float[] hitWeights = {50f, 26f, 68f, 104f};
                                // ---- 手工调参结束 ----

                                float step = partW * stepRatio;
                                float assembleW = partW + (SLOT_COUNT - 1) * step;

                                if (stepRatio <= 0.0001f) {
                                    Stack stack = new Stack();
                                    for (int s = 0; s < SLOT_COUNT; s++) {
                                        int slot = s;
                                        Image icon = new Image(shellIcons[weaponIndex][slot]);
                                        icon.setScaling(Scaling.fit);
                                        icon.update(() -> icon.setColor(selectedSlot == slot ? Pal.accent : Color.white));
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
                                        zone.clicked(() -> {
                                            uiSlot[0] = slot;
                                            selectedSlot = slot;
                                            rebuild.run();
                                        });
                                        hitOverlay.add(zone).growY().width(partW * (hitWeights[s] / weightSum));
                                    }
                                    stack.add(hitOverlay);
                                    missile.add(stack).size(partW, partH).padBottom(10f).row();
                                } else {
                                    missile.table(icons -> {
                                        icons.left().top().defaults().pad(0f);
                                        for (int s = 0; s < SLOT_COUNT; s++) {
                                            int slot = s;
                                            Table hit = new Table();
                                            hit.addListener(new HandCursorListener());
                                            hit.clicked(() -> {
                                                uiSlot[0] = slot;
                                                selectedSlot = slot;
                                                rebuild.run();
                                            });
                                            Image icon = new Image(shellIcons[weaponIndex][slot]);
                                            icon.setScaling(Scaling.fit);
                                            icon.update(() -> icon.setColor(selectedSlot == slot ? Pal.accent : Color.white));
                                            hit.add(icon).size(partW, partH).scaling(Scaling.fit);
                                            var cell = icons.add(hit).size(partW, partH);
                                            if (s > 0) cell.padLeft(-(partW - step));
                                        }
                                    }).width(assembleW).left().padBottom(10f).row();
                                }

                                missile.table(labels -> {
                                    labels.defaults().pad(0f);
                                    for (int s = 0; s < SLOT_COUNT; s++) {
                                        int slot = s;
                                        boolean selected = selectedSlot == slot;
                                        Table hit = new Table();
                                        hit.addListener(new HandCursorListener());
                                        hit.clicked(() -> {
                                            uiSlot[0] = slot;
                                            selectedSlot = slot;
                                            rebuild.run();
                                        });
                                        Label name = new Label(Core.bundle.get(slotDefs[slot].bundleKey));
                                        name.setAlignment(Align.center);
                                        name.setColor(selected ? Pal.accent : Color.white);
                                        name.setWrap(true);
                                        hit.add(name).growX().labelAlign(Align.center);
                                        labels.add(hit).growX().uniformX().top();
                                    }
                                }).width(Math.max(assembleW, partW));
                            }).grow().center();
                        }
                    }).size(rightW, topH).pad(2f);
                }).growX().height(topH).row();

                main.table(bottom -> {
                    bottom.table(Styles.black3, bl -> {
                        bl.top().left().margin(8f);
                        bl.add("@nh.air-raid.loaded").color(Pal.accent).padBottom(6f).left().row();
                        bl.pane(list -> {
                            list.top().left();
                            if (weaponIndex < 0) {
                                list.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(8f);
                            } else {
                                ItemModule module = slots[selectedSlot];
                                boolean[] any = {false};
                                module.each((item, amount) -> {
                                    any[0] = true;
                                    list.table(row -> {
                                        row.left();
                                        row.image(item.uiIcon).size(32f).scaling(Scaling.fit).pad(4f);
                                        row.add(item.localizedName + " x" + amount).growX().left().padLeft(6f);
                                        row.button(Icon.cancel, Styles.clearNonei, () -> {
                                            configure(IntSeq.with(1, selectedSlot, item.id, 0));
                                            rebuild.run();
                                        }).size(28f);
                                    }).growX().pad(2f).row();
                                });
                                if (!any[0]) {
                                    list.add("@nh.air-raid.empty-slot").color(Pal.lightishGray).pad(8f);
                                }
                            }
                        }).grow().pad(2f);
                        bl.row();
                        bl.button("@nh.air-raid.clear-slot", Icon.trash, Styles.cleart, () -> {
                            configure(IntSeq.with(4, selectedSlot));
                            rebuild.run();
                        }).growX().height(LEN - 8f).padTop(4f).disabled(b -> weaponIndex < 0);
                    }).size(leftW, bottomH).pad(2f);

                    bottom.table(Styles.black3, br -> {
                        br.top().left().margin(8f);
                        br.add("@nh.air-raid.select-items").color(Pal.accent).padBottom(6f).left().row();
                        if (weaponIndex < 0) {
                            br.add("@nh.air-raid.no-weapon").color(Pal.lightishGray).pad(16f);
                        } else {
                            SlotDef def = slotDefs[selectedSlot];
                            Building core = team.core();
                            boolean infinite = state.rules.infiniteResources || team.rules().cheat;

                            br.pane(list -> {
                                list.top().left();
                                for (Item item : def.allowed) {
                                    int have = core == null ? 0 : core.items.get(item);
                                    int loaded = slots[selectedSlot].get(item);
                                    int others = slots[selectedSlot].total() - loaded;
                                    int maxForItem = Math.max(0, def.capacity - others);
                                    if (!infinite) maxForItem = Math.min(maxForItem, have);

                                    int max = Math.max(maxForItem, loaded);
                                    int[] val = {loaded};
                                    int[] sent = {loaded};
                                    Label amountLabel = new Label(loaded + "/" + max);
                                    amountLabel.setAlignment(Align.right);
                                    Slider slider = new Slider(0, Math.max(max, 1), 1, false);
                                    slider.setValue(loaded);
                                    slider.setDisabled(max <= 0 && loaded <= 0);
                                    slider.moved(v -> {
                                        int nv = (int) v;
                                        val[0] = nv;
                                        amountLabel.setText(nv + "/" + max);
                                        if (nv != sent[0]) {
                                            sent[0] = nv;
                                            configure(IntSeq.with(1, selectedSlot, item.id, nv));
                                        }
                                    });
                                    slider.addListener(new InputListener() {
                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                                            if (val[0] != slots[selectedSlot].get(item)) {
                                                configure(IntSeq.with(1, selectedSlot, item.id, val[0]));
                                            }
                                            Core.app.post(rebuild);
                                        }
                                    });

                                    list.table(row -> {
                                        row.left();
                                        row.image(item.uiIcon).size(32f).scaling(Scaling.fit).padRight(4f);
                                        row.add(item.localizedName).left().width(88f).padRight(4f).ellipsis(true);
                                        row.add(slider).growX().height(36f).padRight(4f);
                                        row.add(amountLabel).minWidth(88f).right().padRight(2f);
                                    }).growX().pad(2f).row();
                                }
                            }).grow().pad(2f);
                        }
                    }).size(rightW, bottomH).pad(2f);
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
                    }).growX().left().pad(6f);
                    statsBar.table(actions -> {
                        actions.defaults().pad(3f);
                        actions.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 2f, LEN);
                        actions.button("@nh.air-raid.cancel-raid", Icon.cancel, Styles.cleart, () -> {
                            configure(false);
                            rebuild.run();
                        }).size(LEN * 2.4f, LEN).disabled(b -> !raidActive);
                        actions.button("@nh.air-raid.launch", Icon.ok, Styles.cleart, () -> {
                            configure(true);
                            Core.app.post(() -> {
                                if (raidActive) dialog.hide();
                                else rebuild.run();
                            });
                        }).size(LEN * 2.6f, LEN).disabled(b -> raidActive || !isPowered() || weaponIndex < 0 || !hasPayload() || !canAffordPayload() || !hasTarget());
                    }).right().pad(4f);
                }).width(dialogW).minHeight(LEN * 2.2f).padTop(4f);
            });
        }

        private void showWeaponPicker(Runnable parentRebuild) {
            BaseDialog picker = new BaseDialog("@nh.air-raid.select-weapon");
            picker.addCloseListener();

            Table cards = new Table();
            cards.left().top();
            for (int i = 0; i < WEAPON_COUNT; i++) {
                int idx = i;
                WeaponMode mode = weapons[idx];
                cards.table(Tex.pane, card -> {
                    card.top().margin(8f);
                    card.addListener(new HandCursorListener());
                    card.clicked(() -> {
                        configure(IntSeq.with(0, idx));
                        picker.hide();
                        parentRebuild.run();
                    });
                    Image icon = new Image(weaponIcons[idx]);
                    icon.setScaling(Scaling.fit);
                    card.add(icon).size(100f).padTop(8f).padBottom(8f).row();
                    card.add(Core.bundle.get(mode.bundleKey + ".name")).padBottom(6f).wrap().width(210f).labelAlign(Align.center).row();
                    card.pane(Styles.noBarPane, desc -> {
                        desc.top();
                        desc.add(Core.bundle.get(mode.bundleKey + ".desc"))
                                .wrap().width(200f).color(Pal.lightishGray).labelAlign(Align.left);
                    }).size(210f, 120f).pad(6f);
                    if (weaponIndex == idx) {
                        card.setColor(Pal.accent);
                    }
                }).size(240f, 320f).pad(10f);
            }

            ScrollPane pane = new ScrollPane(cards);
            pane.setScrollingDisabledY(true);
            pane.setFadeScrollBars(false);
            picker.cont.top();
            picker.cont.add(pane).grow().pad(12f);
            picker.buttons.button("@back", Icon.left, picker::hide).size(210f, 64f);
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
        public final float damageMul, sizeMul, speedMul, inaccuracy;
        public final int shotCountMin, shotCountMax;

        public WeaponMode(String bundleKey, BulletType bullet, float damageMul, float sizeMul, float speedMul, float inaccuracy, int shotCount) {
            this(bundleKey, bullet, damageMul, sizeMul, speedMul, inaccuracy, shotCount, shotCount);
        }

        public WeaponMode(String bundleKey, BulletType bullet, float damageMul, float sizeMul, float speedMul, float inaccuracy, int shotCountMin, int shotCountMax) {
            this.bundleKey = bundleKey;
            this.bullet = bullet;
            this.damageMul = damageMul;
            this.sizeMul = sizeMul;
            this.speedMul = speedMul;
            this.inaccuracy = inaccuracy;
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

























