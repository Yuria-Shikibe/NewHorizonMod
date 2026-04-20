package newhorizon.expand.bullets.raid;

import arc.graphics.g2d.Draw;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.graphics.Trail;

import static mindustry.Vars.headless;

public class RailRaidBulletType extends BasicRaidBulletType{
    public int railCount = 2;
    public int railLength = 15;
    public float railWidth = 4f;
    public float railSpacing = 12f;


    @Override
    public void init(Bullet b) {
        Trail[] sideTrails = new Trail[railCount];
        for (int i = 0; i < railCount; i++) {
            sideTrails[i] = new Trail(railLength);
        }
        b.data = sideTrails;
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);
        if (!(b.data() instanceof Trail[])) return;
        Trail[] sideTrails = (Trail[]) b.data;
        for (int i = 0; i < railCount; i++) {
            Fx.trailFade.at(b.x, b.y, railWidth, b.team.color, sideTrails[i].copy());
        }
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        updateSideTrail(b);
    }

    @Override
    public void drawTrail(Bullet b) {
        super.drawTrail(b);
        drawSideTrail(b);
    }

    public void drawSideTrail(Bullet b) {
        if (!(b.data() instanceof Trail[])) return;
        Trail[] sideTrails = (Trail[]) b.data;
        float z = Draw.z();
        Draw.z(z - 0.0001f);
        for (int i = 0; i < railCount; i++) {
            if (railLength > 0 && sideTrails[i] != null) {
                sideTrails[i].draw(b.team.color, railWidth);
            }
        }
        Draw.z(z);
    }

    public void updateSideTrail(Bullet b) {
        if (!(b.data() instanceof Trail[])) return;
        if (headless) return;
        Trail[] sideTrails = (Trail[]) b.data;
        for (int i = 0; i < railCount; i++) {
            if (railLength > 0) {
                if (sideTrails[i] != null) {
                    Tmp.v1.trns(90, ((railCount - 1) * railSpacing) / 2f - railSpacing * i).rotate(b.rotation()).add(b);
                    sideTrails[i].update(Tmp.v1.x, Tmp.v1.y, trailInterp.apply(b.fin()));
                }else {
                    sideTrails[i] = new Trail(railLength);
                }
            }
        }
    }
}
