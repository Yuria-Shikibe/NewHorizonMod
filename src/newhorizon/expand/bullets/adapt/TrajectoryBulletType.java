package newhorizon.expand.bullets.adapt;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.BSpline;
import arc.math.geom.Bezier;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import newhorizon.content.NHColor;

public class TrajectoryBulletType extends AdaptBulletType{
    public static final Rand rand = new Rand();

    public Interp expIn = new Interp.ExpIn(2, 2);

    public float pointEffectSpace = 20f;
    public float length = 300f;

    public Effect trajectoryEffect = new Effect(15, e -> {
        if (e.data instanceof Float len){
            rand.setSeed(e.id);
            Tmp.v3.setZero().trns(e.rotation, len).add(e.x, e.y);
            Tmp.v1.setZero().trns(e.rotation + rand.random(-25, 25), len * rand.random(0.15f, 0.25f)).add(e.x, e.y);

            /*
            Tmp.v3.setZero().trns(e.rotation, len).add(e.x, e.y);
            Tmp.v2.setZero().trns(e.rotation + rand.random(-25, 25) - 180, len * rand.random(0.15f, 0.25f)).add(Tmp.v3.x, Tmp.v3.y);
            Lines.curve(e.x, e.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, (int) (len / 4f));
             */
            Draw.color(e.color);
            Lines.stroke(1.5f);

            Tmp.v2.set(Tmp.v3).sub(e.x, e.y).nor().rotate90(1).scl(Mathf.randomSeedRange(e.id, 1f) * 50f);
            Tmp.bz2.set(Tmp.v3, Tmp.v2.add(e.x, e.y), Tmp.v1.set(e.x, e.y));
            Tmp.bz2.valueAt(Tmp.v4, e.fout(expIn));
            Tmp.bz2.valueAt(Tmp.v5, e.fout());
            Lines.line(Tmp.v4.x, Tmp.v4.y, Tmp.v5.x, Tmp.v5.y);
        }
    });

    public TrajectoryBulletType(){
        lifetime = 1f;
        delayFrags = true;
        keepVelocity = false;

        hitEffect = Fx.none;
        despawnEffect = Fx.none;
    }

    @Override
    protected float calculateRange(){
        return length;
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        float hitLength = Math.min(length, Mathf.dst(b.x, b.y, b.aimX, b.aimY));
        float maxLength = Damage.findPierceLength(b, 1, true, hitLength);

        for (int i = 0; i < 2; i++){
            trajectoryEffect.at(b.x, b.y, b.rotation(), NHColor.lightSky, maxLength);
        }

        b.remove();
        b.vel.setZero();
    }

}
