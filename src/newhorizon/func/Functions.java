package newhorizon.func;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import newhorizon.bullets.EffectBulletType;
import newhorizon.content.NHFx;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.ui;

public class Functions {
    public static Color getColor(Color defaultColor, Team team){
        return defaultColor == null ? team.color : defaultColor;
    }
    
    public static void spawnUnit(UnitType type, Team team, int spawnNum, float x, float y){
        for(int spawned = 0; spawned < spawnNum; spawned++){
            Time.run(spawned * Time.delta, () -> {
                Unit unit = type.create(team);
                if(unit != null){
                    unit.set(x, y);
                    unit.add();
                }else Log.info("Unit == null");
            });
        }
    }
    
    public static float regSize(UnitType type){
        return type.hitSize / tilesize / tilesize / 3.25f;
    }

    public static boolean spawnUnit(Building starter, float x, float y, int spawns, int level, float spawnRange, float spawnReloadTime, float spawnDelay, float inComeVelocity, UnitType type, Color spawnColor){
        Seq<Vec2> vecs = new Seq<>();
        
        int steps = 0;
        
        if(!type.flying){
            while(vecs.size < spawns){
                if(steps > 10)return false;
                Vec2 p = new Vec2().rnd(spawnRange).scl(Mathf.random(1f));
                Building building = Units.findAllyTile(starter.team, p.x + x, p.y + y, type.hitSize * 2f, b -> b.block().solid);
                Log.info(building);
                if(building != null){
                    steps++;
                    continue;
                }
                vecs.add(p);
            }
        }else{
            randLenVectors((long)Time.time, spawns, spawnRange, (sx, sy) -> vecs.add(new Vec2(sx, sy)));
        }
        
        float angle, regSize = regSize(type);
        final TextureRegion pointerRegion = Core.atlas.find("new-horizon-jump-gate-pointer"), arrowRegion = Core.atlas.find("new-horizon-jump-gate-arrow");
        angle = starter.angleTo(x, y);
    
        ui.showInfoPopup("[accent]<<Caution>>[]: Level [accent]" + level + "[] fleet in coming at [" + TableFuncs.format(x / tilesize) + ", " + TableFuncs.format(y / tilesize) + "].", spawnReloadTime / 60f, 0, 20, 20, 20, 20);
        
        new Effect(60f, e -> {
            Lines.stroke(3 * e.fout(), spawnColor);
            Lines.circle(e.x, e.y, spawnRange * e.finpow());
        }).at(x, y);
        
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
                if(type.flying){
                    NHFx.jumpTrail.at(unit.x, unit.y, angle, spawnColor, unit);
                    Tmp.v1.trns(angle, inComeVelocity).scl(type.drag + 1);
                    unit.vel.add(Tmp.v1.x, Tmp.v1.y);
                }else{
                    for(int j = 0; j < 3; j++){
                        Time.run(j * 8, () -> Fx.spawn.at(unit));
                    }
                    NHFx.circle.at(unit.x, unit.y, type.hitSize * 2, spawnColor);
                }
                Sounds.plasmaboom.at(unit.x, unit.y);
            });

            i++;
        }
        return true;
    }
}
