package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;

import static mindustry.Vars.headless;

public class HelixTrailBulletType extends BasicBulletType {
    public int sideTrailNum = 3;
    public int sideTrailLength = 30;
    public float sideTrailWidth = 3f;
    public float sideTrailSpace = 15f;
    public float sideTrailTime = 4f;

    public HelixTrailBulletType(float speed, float damage) {
        super(4f, damage);
        trailEffect = Fx.none;
        despawnEffect = Fx.none;
        hitEffect = Fx.none;

        lifetime = 240f;
        trailColor = Pal.techBlue;
    }

    public HelixTrailBulletType() {
        super(6f, 1);
        trailEffect = Fx.none;
        despawnEffect = Fx.none;
        hitEffect = Fx.none;

        lifetime = 120f;
        trailColor = Pal.techBlue;
    }

    @Override
    public void init(Bullet b){
        Trail[] sideTrails = new Trail[sideTrailNum];
        for (int i = 0; i < sideTrailNum; i++){
            sideTrails[i] = new Trail(sideTrailLength);
        }
        b.data = sideTrails;
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);
        if (!(b.data() instanceof Trail[]))return;
        Trail[] sideTrails = (Trail[]) b.data;

        if(sideTrailLength > 0){
            for (Trail trail: sideTrails){
                if (trail != null && trail.size() > 0){
                    Fx.trailFade.at(b.x, b.y, sideTrailWidth, trailColor, trail.copy());
                }
            }
        }
    }

    @Override
    public void draw(Bullet b) {
        Draw.color(Pal.techBlue);
        Fill.circle(b.x, b.y, sideTrailSpace + 2f);

        Draw.color(Color.black);
        Fill.circle(b.x, b.y, sideTrailSpace - 4f);

        Draw.color(Pal.techBlue);
        Fill.circle(b.x, b.y, sideTrailSpace - 8f);

        drawSideTrail(b);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        updateSideTrail(b);
    }

    public void drawSideTrail(Bullet b){
        if (!(b.data() instanceof Trail[]))return;
        Trail[] sideTrails = (Trail[]) b.data;
        float z = Draw.z();
        Draw.z(z - 0.0001f);
        for (Trail trail: sideTrails){
            if(sideTrailLength > 0){
                if(trail != null){
                    trail.draw(trailColor, sideTrailWidth);
                }
            }
        }
        Draw.z(z);
    }

    public void updateSideTrail(Bullet b){
        if (!(b.data() instanceof Trail[]))return;
        Trail[] sideTrails = (Trail[]) b.data;
        if(!headless && sideTrailLength > 0){
            for (int i = 0; i < sideTrailNum; i++){
                if(sideTrails[i] == null){
                    sideTrails[i] = new Trail(sideTrailLength);
                }
                sideTrails[i].length = sideTrailLength;
                sideTrails[i].update(
                    b.x + Mathf.sinDeg(Time.time * sideTrailTime + i * (360f / sideTrailNum)) * sideTrailSpace,
                    b.y + Mathf.cosDeg(Time.time * sideTrailTime + i * (360f / sideTrailNum)) * sideTrailSpace,
                    trailInterp.apply(b.fin()));
            }
        }
    }

}
