package newhorizon.expand.bullets;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.gen.Bullet;
import mindustry.gen.Teamc;

public class ThermoBulletType extends TrailFadeBulletType {
    public float angelRandOffset = 3.5f;
    public ThermoBulletType(float speed, float damage){
        super(speed, damage, "bullet");
        hitBlinkTrail = false;
        tracerStroke = 1.5f;
        tracerSpacing = 3f;
        tracerUpdateSpacing = 1.2f;
    }

    public void updateHoming(Bullet b){
        if(homingPower > 0.0001f && b.time >= homingDelay){
            float realAimX = b.aimX < 0 ? b.x : b.aimX;
            float realAimY = b.aimY < 0 ? b.y : b.aimY;

            Teamc target;
            //home in on allies if possible
            if(heals()){
                target = Units.closestTarget(null, realAimX, realAimY, homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                    t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            }else{
                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                }else{
                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                        e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                        t -> t != null && collidesGround && !b.hasCollided(t.id));
                }
            }

            if(target != null){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
            }else {
                b.vel.rotate(Mathf.random(-angelRandOffset, angelRandOffset));
            }
        }else {
            b.vel.rotate(Mathf.random(-angelRandOffset, angelRandOffset));
        }
    }
}
