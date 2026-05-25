package newhorizon.expand.bullets;

import arc.math.Interp;
import arc.math.Mathf;
import mindustry.entities.bullet.FlakBulletType;
import mindustry.gen.Bullet;

public class DecelerateFlakBulletType extends FlakBulletType {
    public float velocityStaticTime = 60f;

    public DecelerateFlakBulletType(){
        collidesGround = true;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        b.lifetime = b.lifetime * 2;
        b.fdata = b.lifetime;
        b.lifetime += velocityStaticTime;
        b.data = b.vel.len() / speed;
    }

    @Override
    public void update(Bullet b) {
        b.vel.setLength(Interp.reverse.apply((b.time / b.lifetime)) * b.fdata / b.lifetime * speed * (b.data instanceof Float scl ? scl : 1f));
        super.update(b);
    }
}
