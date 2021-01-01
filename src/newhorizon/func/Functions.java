package newhorizon.func;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.type.UnitType;
import newhorizon.blocks.special.JumpGate;
import newhorizon.bullets.EffectBulletType;
import newhorizon.content.NHFx;

import java.text.DecimalFormat;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;

public class Functions {
    private static final DecimalFormat df = new DecimalFormat("######0.00");
    public static String format(float value){return df.format(value);}
    public static String getJudge(boolean value){return value ? "[green]Yes[]" : "[red]No[]";}
    public static final String tabSpace = "    ";
    public static final float LEN = 60f, OFFSET = 12f;

    public static float regSize(UnitType type){
        return type.hitSize / tilesize / tilesize / 3.25f;
    }

    public static void spawnUnit(JumpGate.JumpGateBuild starter, float x, float y, int spawns, int level, float spawnRange, float spawnReloadTime, float spawnDelay, float inComeVelocity, UnitType type, Color spawnColor){
        final TextureRegion
                pointerRegion = Core.atlas.find("new-horizon-jump-gate-pointer"),
                arrowRegion = Core.atlas.find("new-horizon-jump-gate-arrow");

        ui.showInfoPopup("[accent]<<Caution>>[]: Level [accent]" + level + "[] fleet in coming at [" + format(x / tilesize) + ", " + format(y / tilesize) + "].", spawnReloadTime / 60f, 0, 20, 20, 20, 20);

        float angle, regSize = regSize(type);

        new Effect(60f, e -> {
            Lines.stroke(3 * e.fout(), spawnColor);
            Lines.circle(e.x, e.y, spawnRange * e.finpow());
        }).at(x, y);

        angle = starter.angleTo(x, y);

        Seq<Vec2> vecs = new Seq<>();
        randLenVectors((long)Time.time, spawns, spawnRange, (vx, vy) -> vecs.add(new Vec2(vx, vy)));

        int i = 0;
        for (Vec2 s : vecs) {
            int finalI = i;

            new EffectBulletType(spawnReloadTime + finalI * spawnDelay){
                @Override
                public void init(Bullet b){
                    new Effect(60f, e -> {
                        Lines.stroke(3 * e.fout(), spawnColor);
                        Lines.circle(e.x, e.y, spawnRange  / 8f * e.finpow());
                    }).at(b);
                }

                @Override
                public void draw(Bullet b){
                    Draw.color(spawnColor);
                    for(int i = 0; i < 4; i++){
                        float sin = Mathf.absin(Time.time, 16f, tilesize);
                        float length = (tilesize * starter.block().size / 3f + sin) * b.fout() + tilesize * 2f;
                        float signSize = regSize + 0.75f + Mathf.absin(Time.time + 8f, 8f, 0.15f);
                        Tmp.v1.trns(i * 90, -length);
                        Draw.rect(pointerRegion, b.x + Tmp.v1.x,b.y + Tmp.v1.y, pointerRegion.width * Draw.scl * signSize, pointerRegion.height * Draw.scl * signSize, i * 90 - 90);
                    }


                    for (int i = 0; i <= 8; i++) {
                        Tmp.v1.trns(angle, (i - 4) * tilesize * 2);
                        float f = (100 - (Time.time - 12.5f * i) % 100) / 100;
                        Draw.rect(arrowRegion, b.x + Tmp.v1.x, b.y + Tmp.v1.y, pointerRegion.width * (regSize / 2f + Draw.scl) * f, pointerRegion.height * (regSize / 2f + Draw.scl) * f, angle - 90);
                    }

                    Draw.reset();
                }

                @Override
                public void despawned(Bullet b) {
                    NHFx.spawn.at(b.x, b.y, regSize, spawnColor, starter);
                }

            }.create(starter, x + s.x, y + s.y, angle);

            Time.run(spawnReloadTime + finalI * spawnDelay, () -> {
                if(!Units.canCreate(starter.team(), type))return;
                if (!starter.isValid()) return;
                Unit unit = type.create(starter.team());
                    unit.set(x + s.x, y + s.y);
                unit.add();
                unit.rotation = angle;
                NHFx.jumpTrail.at(unit.x, unit.y, angle, spawnColor, unit);
                Tmp.v1.trns(angle, inComeVelocity).scl(type.drag + 1);
                unit.vel.add(Tmp.v1.x, Tmp.v1.y);
                Sounds.plasmaboom.at(unit.x, unit.y);
            });

            i++;
        }
    }
}
