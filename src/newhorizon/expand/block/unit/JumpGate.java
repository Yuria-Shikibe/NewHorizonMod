package newhorizon.expand.block.unit;

import arc.Core;
import arc.func.Cons2;
import arc.func.Cons4;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import newhorizon.content.NHContent;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.expand.block.consumer.NHConsumeShowStat;
import newhorizon.expand.entities.Spawner;
import newhorizon.util.ui.DelaySlideBar;

import static mindustry.Vars.*;
import static newhorizon.NHVars.worldData;

public class JumpGate extends Block {
    public Seq<UnitType> spawnList = Seq.with(UnitTypes.alpha);
    public float warmupPerSpawn = 0.2f;
    public float maxWarmupSpeed = 3f;

    public float maxRadius = 180f;
    public float minRadius = 40f;

    //todo duplicated code
    public Cons2<JumpGateBuild, Boolean> blockDrawer = (building, valid) -> {
        TextureRegion arrowRegion = NHContent.arrowRegion;
        TextureRegion pointerRegion = NHContent.pointerRegion;

        Draw.z(Layer.bullet);

        float scl = building.warmup() * 0.125f;
        float rot = building.totalProgress();

        Draw.color(building.team.color);
        Lines.stroke(8f * scl);
        Lines.square(building.x, building.y, building.block.size * tilesize / 2.5f, -rot);
        Lines.square(building.x, building.y, building.block.size * tilesize / 2f, rot);
        for (int i = 0; i < 4; i++) {
            float length = tilesize * building.block.size / 2f + 8f;
            float rotation = i * 90;
            float sin = Mathf.absin(building.totalProgress(), 16f, tilesize);
            float signSize = 0.75f + Mathf.absin(building.totalProgress() + 8f, 8f, 0.15f);

            Tmp.v1.trns(rotation + rot, -length);
            Draw.rect(arrowRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, arrowRegion.width * scl, arrowRegion.height * scl, rotation + 90 + rot);
            length = tilesize * building.block.size / 2f + 3 + sin;
            Tmp.v1.trns(rotation, -length);
            Draw.rect(pointerRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, pointerRegion.width * signSize * scl, pointerRegion.height * signSize * scl, rotation + 90);
        }
        Draw.color();
    };
    public Cons4<JumpGateBuild, Boolean, Float, Vec2> lockDrawer = (building, valid, progress, position) -> {
        if (!valid) return;
        float ang = Angles.angle(building.x, building.y, position.x, position.y);
        float len = Mathf.dst(building.x, building.y, position.x, position.y) - minRadius;
        float lerp = Interp.pow5In.apply(Interp.pow3.apply(progress));
        float reverse = Interp.reverse.apply(lerp);

        Draw.color(Pal.techBlue);
        Draw.z(Layer.bullet);

        Lines.stroke(2);
        Lines.arc(building.x, building.y, 32, progress);
        Draw.color(Pal.techBlue, building.team.color, lerp);

        //this part code almost same from above

        TextureRegion arrowRegion = NHContent.arrowRegion;
        TextureRegion pointerRegion = NHContent.pointerRegion;

        Draw.z(Layer.bullet);

        float scl = building.spawnWarmup * 0.125f;
        float rot = building.totalProgress();
        float size = building.block.size * tilesize;
        float alphaLerp = Interp.reverse.apply(Interp.pow10In.apply(progress));

        Draw.color(Pal.techBlue, building.team.color, lerp);
        Draw.alpha(alphaLerp);
        for (int i = 0; i < 4; i++) {
            float rotation = i * 90;
            float sin = Mathf.absin(building.totalProgress(), 16f, 8f);
            float len1 = (size / 2f + 8f) * reverse;
            float len2 = (size / 2f + 3 + sin) * reverse;
            float signSize = 0.75f + Mathf.absin(building.totalProgress() + 8f, 8f, 0.15f);
            Tmp.v2.trns(ang, (minRadius + len) * lerp);

            Tmp.v1.trns(rotation + rot, -len1).add(Tmp.v2);
            Draw.rect(arrowRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, arrowRegion.width * scl, arrowRegion.height * scl, rotation + 90 + rot);
            Tmp.v1.trns(rotation, -len2).add(Tmp.v2);
            Draw.rect(pointerRegion, building.x + Tmp.v1.x, building.y + Tmp.v1.y, pointerRegion.width * signSize * scl, pointerRegion.height * signSize * scl, rotation + 90);
        }
        Draw.color();
    };

    public JumpGate(String name) {
        super(name);
        solid = true;
        sync = true;
        breakable = true;
        update = true;
        commandable = true;
        configurable = true;
        saveConfig = true;
        canOverdrive = false;
        logicConfigurable = true;
        clearOnDoubleTap = true;

        config(Integer.class, JumpGateBuild::changePlan);
        config(UnitType.class, (JumpGateBuild e, UnitType unitType) -> e.changePlan(e.getPlanId(unitType)));
        configClear((JumpGateBuild e) -> e.unitType = null);
        consume(new NHConsumeShowStat(e -> null, e -> null, e -> null, b -> worldData.teamPayloadData.getPayload(b.team)));
    }

    @Override
    public void init() {
        super.init();
        clipSize = maxRadius;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress", (JumpGateBuild e) -> new Bar("bar.progress", Pal.ammo, e::progress));
        addBar("efficiency", (JumpGateBuild e) -> new Bar(() -> Core.bundle.format("bar.efficiency", Strings.autoFixed(e.speedMultiplier * 100f, 0)), () -> Pal.techBlue, () -> e.speedMultiplier / maxWarmupSpeed));
        addBar("units", (JumpGateBuild e) -> new Bar(
                () -> e.unitType == null ? "[lightgray]" + Iconc.cancel :
                        Core.bundle.format("bar.unitcap",
                                Fonts.getUnicodeStr(e.unitType.name),
                                e.team.data().countType(e.unitType),
                                e.unitType == null ? Units.getStringCap(e.team) : (e.unitType.useUnitCap ? Units.getStringCap(e.team) : "∞")
                        ),
                () -> Pal.power,
                () -> e.unitType == null ? 0f : (e.unitType.useUnitCap ? (float) e.team.data().countType(e.unitType) / Units.getCap(e.team) : 1f)
        ));
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.output, table -> {
            table.row();

            for (UnitType plan : spawnList) {
                ModuleBlock.UnitCost cost = ModuleBlock.unitCosts.get(plan);
                if (cost == null) continue;
                table.table(Styles.grayPanel, t -> {

                    if (plan.isBanned()) {
                        t.image(Icon.cancel).color(Pal.remove).size(40);
                        return;
                    }

                    if (plan.unlockedNow()) {
                        t.image(plan.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit).with(i -> StatValues.withTooltip(i, plan));
                        t.table(info -> {
                            info.add(plan.localizedName).left();
                            info.row();
                            info.add(Strings.autoFixed(cost.craftTime / 60f, 1) + " " + Core.bundle.get("unit.seconds")).color(Color.lightGray);
                        }).left();

                        t.table(req -> {
                            req.right();
                            for (int i = 0; i < cost.payloadSeq.size; i++) {
                                if (i % 6 == 0) {
                                    req.row();
                                }

                                PayloadStack stack = cost.payloadSeq.get(i);
                                req.add(StatValues.stack(stack.item, stack.amount, true)).pad(5);
                            }
                        }).right().grow().pad(10f);
                    } else {
                        t.image(Icon.lock).color(Pal.darkerGray).size(40);
                    }
                }).growX().pad(5);
                table.row();
            }
        });
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class JumpGateBuild extends Building {
        public float speedMultiplier = 1f;
        public float progress;
        public float warmup;
        public float spawnWarmup;
        public @Nullable UnitType lastUnitType;
        public @Nullable UnitType unitType;
        public @Nullable Vec2 vec2;
        public @Nullable Vec2 spawn;

        @Override
        public Vec2 getCommandPosition() {
            return vec2;
        }

        public void getRealSpawnPosition() {
            if (vec2 != null) {
                Tmp.v1.set(vec2).sub(this);
                float len = Mathf.clamp(Tmp.v1.len(), minRadius, maxRadius);
                spawn.trns(Tmp.v1.angle(), len).add(this);
            }
        }

        @Override
        public void created() {
            super.created();
            spawn = new Vec2(x, y);
        }

        @Override
        public void onCommand(Vec2 target) {
            vec2 = target;
            getRealSpawnPosition();
        }

        public ModuleBlock.UnitCost cost() {
            if (ModuleBlock.unitCosts.get(unitType) == null) return new ModuleBlock.UnitCost();
            return ModuleBlock.unitCosts.get(unitType);
        }

        public boolean canSpawn() {
            PayloadSeq teamPayload = worldData.teamPayloadData.getPayload(team);
            for (PayloadStack stack : cost().payloadSeq) {
                if (teamPayload.get(stack.item) < stack.amount * state.rules.unitCost(team)) return false;
            }
            return true;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, maxRadius, team.color);
            Drawf.dashCircle(x, y, minRadius, team.color);

            if (unitType == null) return;
            Lines.stroke(3f, Pal.gray);
            Lines.square(spawn.x, spawn.y, 8f, 45f);
            Lines.stroke(1f, team.color);
            Lines.square(spawn.x, spawn.y, 8f, 45f);
            Draw.reset();
        }

        public void changePlan(int idx) {
            if (idx == -1) return;
            idx = Mathf.clamp(idx, 0, spawnList.size - 1);
            if (unitType == spawnList.get(idx)) {
                lastUnitType = unitType;
                unitType = null;
            } else {
                lastUnitType = unitType;
                unitType = spawnList.get(idx);
            }
            progress = 0f;
            speedMultiplier = 1f;
        }

        public UnitType getUnitType(int idx) {
            if (idx < 0 || idx > spawnList.size - 1) return null;
            return spawnList.get(idx);
        }

        public void spawnUnit() {
            if (unitType == null) return;

            if (!net.client()) {
                float rot = core() == null ? Angles.angle(x, y, spawn.x, spawn.y) : Angles.angle(core().x, core().y, x, y);
                Spawner spawner = new Spawner();
                Tmp.v1.setToRandomDirection().add(spawn);
                spawner.init(unitType, team, Tmp.v1, rot, Mathf.clamp(unitTime(unitType) / maxWarmupSpeed, 5f * 60, 15f * 60));
                if (vec2 != null) {
                    spawner.commandPos.set(vec2.cpy());
                }
                spawner.add();
            }

            progress = 0f;
            spawnWarmup = 0f;
            if (unitType == lastUnitType) {
                speedMultiplier = Mathf.clamp(speedMultiplier + warmupPerSpawn, 1, maxWarmupSpeed);
            } else {
                speedMultiplier = 1f;
            }
            lastUnitType = unitType;

            PayloadSeq teamPayload = worldData.teamPayloadData.getPayload(team);
            for (PayloadStack stack : cost().payloadSeq) {
                teamPayload.remove(stack.item, (int) (stack.amount * state.rules.unitCost(team)));
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            warmup = Mathf.lerp(warmup, efficiency, 0.01f);
            spawnWarmup = Mathf.lerp(spawnWarmup, efficiency, 0.01f);
            if (unitType == null || spawn == null) {
                progress = 0f;
                return;
            }
            if (canSpawn() && Units.canCreate(team, unitType)) {
                progress += getProgressIncrease(unitTime(unitType)) * speedMultiplier;
            }
            if (progress >= 1) spawnUnit();
        }

        public boolean canConsume() {
            return !(unitType == null || spawn == null) && canSpawn() && Units.canCreate(team, unitType);
        }

        public float unitTime(UnitType unitType) {
            if (ModuleBlock.unitCosts.get(unitType) == null) return unitType.health / 100;
            return ModuleBlock.unitCosts.get(unitType).craftTime;
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(inner -> {
                inner.background(Tex.paneSolid);
                inner.pane(selectionTable -> {
                    for (UnitType type : spawnList) {
                        selectionTable.button(button -> {
                            button.table(selection -> {
                                selection.stack(
                                        new DelaySlideBar(
                                                () -> Pal.techBlue,
                                                () -> "          " + Core.bundle.format("bar.unitcap",
                                                        type.localizedName,
                                                        team.data().countType(type),
                                                        type.useUnitCap ? Units.getStringCap(team) : "∞"),
                                                () -> type.useUnitCap ? (float) team.data().countType(type) / Units.getCap(team) : 1f),
                                        new Table(image -> image.image(type.uiIcon).scaling(Scaling.fit).size(48, 48).padTop(6f).padBottom(6f).padLeft(8f)).left()
                                ).expandX().fillX();
                            }).growX();
                            button.update(() -> {
                                button.setChecked(unitType == type);
                            });
                        }, Styles.underlineb, () -> configure(getPlanId(type))).expandX().fillX().margin(0).pad(4);
                        selectionTable.row();
                    }
                }).width(342).maxHeight(400).padRight(2);
            }).width(360).maxHeight(364);
        }

        public int getPlanId(UnitType unitType) {
            if (unitType == null) return -1;
            return spawnList.indexOf(unitType);
        }

        @Override
        public UnitType config() {
            return unitType;
        }

        @Override
        public float progress() {
            return progress;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public void draw() {
            super.draw();
            blockDrawer.get(this, unitType != null && getCommandPosition() != null);
            //lockDrawer.get(this, unitType != null && getCommandPosition() != null, progress(), spawn);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(speedMultiplier);
            write.f(progress);
            write.i(getPlanId(lastUnitType));
            write.i(getPlanId(unitType));
            TypeIO.writeVecNullable(write, vec2);
            TypeIO.writeVecNullable(write, spawn);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            speedMultiplier = read.f();
            progress = read.f();
            lastUnitType = getUnitType(read.i());
            unitType = getUnitType(read.i());
            vec2 = TypeIO.readVecNullable(read);
            spawn = TypeIO.readVecNullable(read);
        }
    }
}
