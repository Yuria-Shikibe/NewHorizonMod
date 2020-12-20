package newhorizon.contents.blocks.special;


import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.contents.bullets.EffectBulletType;
import newhorizon.contents.effects.NHFx;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static newhorizon.contents.data.UpgradeBaseData.*;

public class JumpGate extends Block {
    public float spawnDelay = 5f;
    public float spawnReloadTime = 60f;
    public float spawnRange = 120f;
    public float range = 200f;
    public float inComeVelocity = 5f;
    public TextureRegion
            pointerRegion,
            arrowRegion,
            bottomRegion,
            armorRegion;
    public Color baseColor;
    public final Seq<UnitsSet> calls = new Seq<>();

    public float squareStroke = 2f;

    public JumpGate(String name){
        super(name);
        update = true;
        configurable = true;
        solid = true;
        hasPower = true;
    }

    public JumpGate(String name, UnitsSet... sets){
        this(name);
        addSets(sets);
    }


    public void addSets(UnitsSet... sets){
        calls.addAll(sets);
    }

    @Override
    public void init(){
        super.init();
        if(calls.isEmpty()) throw new IllegalArgumentException("Seq @calls is [red]EMPTY[].");
    }

    @Override
    public void load(){
        super.load();
        pointerRegion = Core.atlas.find(this.name + "-pointer");
        arrowRegion = Core.atlas.find(this.name + "-arrow");
        bottomRegion = Core.atlas.find(this.name + "-bottom");
        armorRegion = Core.atlas.find(this.name + "-armor");
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        Color color = baseColor == null ? Pal.accent : baseColor;
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);
    }

    @Override
    protected TextureRegion[] icons() {
        return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{this.bottomRegion, this.region, this.teamRegions[Team.sharded.id], armorRegion} : new TextureRegion[]{this.bottomRegion, this.region, armorRegion};
    }

    public class JumpGateBuild extends Building implements Ranged {
        public Color baseColor(){
            return baseColor == null ? this.team().color : baseColor;
        }
        public int spawnID = 0;
        public int spawnPOS = -1;
        public int spawns = 1;

        protected void generateEffect(float Sx, float Sy){
            new Effect(60f, e -> {
                Lines.stroke(3 * e.fout(), baseColor());
                Lines.circle(e.x, e.y, spawnRange * e.finpow());
            }).at(Sx, Sy);
        }

        protected void cautionEffect(float Sx, float Sy, float showTime){

            new EffectBulletType(showTime){
                @Override
                public void init(Bullet b){
                    new Effect(60f, e -> {
                        Lines.stroke(3 * e.fout(), baseColor());
                        Lines.circle(e.x, e.y, spawnRange  / 8f * e.finpow());
                    }).at(b);
                }

                @Override
                public void draw(Bullet b){
                    Draw.color(baseColor());
                    for(int i = 0; i < 4; i++){
                        float sin = Mathf.absin(Time.time, 16f, tilesize);
                        float length = (tilesize * block().size / 3f + sin) * b.fout() + tilesize * 2f;
                        float signSize = 0.75f + Mathf.absin(Time.time + 8f, 8f, 0.15f);
                        Tmp.v1.trns(i * 90, -length);
                        Draw.rect(pointerRegion, b.x + Tmp.v1.x,b.y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize, pointerRegion.height * Draw.scl * signSize, i * 90 - 90);
                    }
                    Draw.reset();
                }

                @Override
                public void despawned(Bullet b) {
                    new Effect(100f, e -> {
                        Draw.color(baseColor());

                        for (int j = 1; j <= 3; j ++) {
                            for(int i = 0; i < 4; i++) {
                            float length = tilesize * block().size * 1.5f + 4f;
                            Tmp.v1.trns(i * 90, -length);
                                e.scaled(30 * j, k -> {
                                    float signSize = Draw.scl * k.fout();
                                    Draw.rect(pointerRegion, e.x + Tmp.v1.x * k.finpow(), e.y + Tmp.v1.y * k.finpow(), pointerRegion.width * signSize, pointerRegion.height * signSize, Tmp.v1.angle() - 90);
                                });
                            }
                        }
                    }).at(b);
                }

            }.create(this, Sx, Sy, 0);
        }

        protected void spawnUnit(){
            float angle = 0;
            if (getSpawnPos() == null) {
                generateEffect(x, y);
            } else {
                Building target = getSpawnPos();
                generateEffect(target.x, target.y);
                angle = angleTo(target);
            }

            Seq<Vec2> vecs = new Seq<>();
            randLenVectors((long)Time.time, spawns, spawnRange, (vx, vy) -> vecs.add(new Vec2(vx, vy)));

            int i = 0;
            for (Vec2 s : vecs) {
                int finalI = i;
                if (getSpawnPos() == null) {
                    cautionEffect(x + s.x, y + s.y, spawnReloadTime + finalI * spawnDelay);
                } else {
                    Building target = getSpawnPos();
                    cautionEffect(target.x + s.x, target.y + s.y, spawnReloadTime + finalI * spawnDelay);
                }

                float finalAngle = angle;

                Time.run(spawnReloadTime + finalI * spawnDelay, () -> {
                    if (!isValid()) return;
                    UnitType type = calls.get(spawnID).type;
                    Unit unit = calls.get(spawnID).type.create(team());
                    if (getSpawnPos() == null) {
                        unit.set(x + s.x, y + s.y);
                    } else {
                        Building target = getSpawnPos();
                        unit.set(target.x + s.x, target.y + s.y);
                    }
                    unit.add();
                    unit.rotation = finalAngle;
                    NHFx.jumpTrail.at(unit.x, unit.y, finalAngle, baseColor(), unit);
                    Tmp.v1.trns(finalAngle, inComeVelocity).scl(type.drag + 1);
                    unit.vel.add(Tmp.v1.x, Tmp.v1.y);
                    Sounds.plasmaboom.at(unit.x, unit.y);
                });

                i++;
            }
        }
        
        public void setSpawnPos(int pos){
            this.spawnPOS = pos;
        }
        
        public Building getSpawnPos(){
            return Vars.world.build(spawnPOS);
        }

        @Override
        public void drawConfigure() {
            Drawf.dashCircle(x, y, range(), baseColor());
            Draw.color(baseColor());
            Lines.square(x, y, block().size * tilesize / 2f + 1.0f);
            if(getSpawnPos() != null) {
                Building target = getSpawnPos();
                Draw.alpha(0.3f);
                Fill.square(target.x, target.y, target.block.size / 2f * tilesize);
                Draw.alpha(1f);
                Drawf.dashCircle(target.x, target.y, spawnRange, baseColor());
                Draw.color(baseColor());
                Lines.square(target.x, target.y, target.block().size * tilesize / 2f + 1.0f);

                Lines.stroke(3f, Pal.gray);
                Lines.line(x, y, target.x, target.y);
                Fill.square(x, y, 4.5f, 45);
                Fill.square(target.x, target.y, 4.5f, 45);
                Lines.stroke(1f, baseColor());
                Lines.line(x, y, target.x, target.y);
                Fill.square(x, y, 3.3f, 45);
                Fill.square(target.x, target.y, 3.3f, 45);
            }
            Draw.reset();
        }

        @Override
        public boolean onConfigureTileTapped(Building other) {
            if (this == other || this.spawnPOS == other.pos()) {
                setSpawnPos(-1);
                return false;
            }
            if (other.within(this, range())) {
                setSpawnPos(other.pos());
                return false;
            }
            return true;
        }
        
        @Override
        public void buildConfiguration(Table table) {
            BaseDialog dialog = new BaseDialog("Call");
            dialog.addCloseListener();

            dialog.cont.table(t -> {
                t.table(Tex.button, t2 -> {
                    int num = 0;
                    for(UnitsSet set : calls){
                        if(set.type.isHidden())continue;
                        num++;
                        if(num % 5 == 0)t2.row().left();
                        t2.button(new TextureRegionDrawable(set.type.icon(Cicon.medium)), Styles.clearPartiali, LEN, () -> this.spawnID = calls.indexOf(set)).left();
                    }
                }).fillX().growY().row();
                t.table(Tex.button, t2 -> {
                    t2.button("Spawn01", Styles.cleart, () -> spawns = 1).size(120f, 50f);
                    t2.button("Spawn10", Styles.cleart, () -> spawns = 10).size(120f, 50f);
                    t2.button("Spawn20", Styles.cleart, () -> spawns = 20).size(120f, 50f);
                }).fillX().growY().row();
                t.button("@back", Icon.left, dialog::hide).size(210.0F, 64.0F).row();
                t.button("Spawn", this::spawnUnit).size(210.0F, 64.0F).row();

            }).fill();

            table.button("Spawn", Icon.add, dialog::show).size(LEN * 5, LEN);
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);
            super.draw();
            Draw.rect(armorRegion, x, y);
            Draw.reset();
            Draw.z(Layer.bullet);
            if(efficiency() > 0){
                Lines.stroke(squareStroke, baseColor());
                float rot = Time.time * efficiency();
                Lines.square(x, y, block.size * tilesize / 2.5f, -rot);
                Lines.square(x, y, block.size * tilesize / 2f, rot);
                for(int i = 0; i < 4; i++){
                    float length = tilesize * block().size / 2f + 8f;
                    Tmp.v1.trns(i * 90 + rot, -length);
                    Draw.rect(arrowRegion,x + Tmp.v1.x,y + Tmp.v1.y,i * 90 + 90 + rot);
                }
                for(int i = 0; i < 4; i++){
                    float sin = Mathf.absin(Time.time, 16f, tilesize);
                    float length = tilesize * block().size / 2f + 3 + sin;
                    float signSize = 0.75f + Mathf.absin(Time.time + 8f, 8f, 0.15f);
                    Tmp.v1.trns(i * 90, -length);
                    Draw.rect(pointerRegion, x + Tmp.v1.x,y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize, pointerRegion.height * Draw.scl * signSize, i * 90 + 90);
                }
                Draw.color();
            }
            Draw.reset();
        }
        @Override public float range(){return range;}
        @Override
        public void write(Writes write) {
            write.i(this.spawnID);
            write.i(this.spawnPOS);
            write.i(this.spawns);
        }
        @Override
        public void read(Reads read, byte revision) {
            this.spawnID = read.i();
            this.spawnPOS = read.i();
            this.spawns = read.i();
        }
    }

    public static class UnitsSet{
        public final int level;
        public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
        public final UnitType type;

        public UnitsSet(int level, UnitType type){
            this.type = type;
            this.level = level;
        }

        public ItemStack[] requirements(){
            return requirements.toArray();
        }
    }
}
