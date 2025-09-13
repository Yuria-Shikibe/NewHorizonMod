package newhorizon.expand.bullets.adapt;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.graphics.Drawf;
import newhorizon.expand.bullets.TypeDamageBulletType;
import newhorizon.util.feature.PosLightning;

public class AdaptLaserBulletType extends LaserBulletType implements TypeDamageBulletType {
    public String bundleName = "bullet-name";

    public boolean drawLightning = false;
    public int boltNum = 2;
    public float liWidth = PosLightning.WIDTH - 1f;

    @Override
    public String bundleName() {
        return bundleName;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        typedHitEntity(this, b, entity, health);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        typedCreateSplash(this, b, x, y);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        applyExtraMultiplier(b);
        if (drawLightning) PosLightning.createEffect(b, b.fdata * 0.95f, b.rotation(), hitColor, boltNum, liWidth);
    }

    //same with LaserBulletType, removed Lines.lineAngle(b.x, b.y, b.rotation(), baseLen);
    @Override
    public void draw(Bullet b) {
        float realLength = b.fdata;

        float f = Mathf.curve(b.fin(), 0f, 0.2f);
        float baseLen = realLength * f;
        float cwidth = width;
        float compound = 1f;

        if (!drawLightning) Lines.lineAngle(b.x, b.y, b.rotation(), baseLen);
        for (Color color : colors) {
            Draw.color(color);
            Lines.stroke((cwidth *= lengthFalloff) * b.fout());
            Lines.lineAngle(b.x, b.y, b.rotation(), baseLen, false);
            Tmp.v1.trns(b.rotation(), baseLen);
            Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Lines.getStroke(), cwidth * 2f + width / 2f, b.rotation());

            Fill.circle(b.x, b.y, 1f * cwidth * b.fout());
            for(int i : Mathf.signs){
                Drawf.tri(b.x, b.y, sideWidth * b.fout() * cwidth, sideLength * compound, b.rotation() + sideAngle * i);
            }

            compound *= lengthFalloff;
        }
        Draw.reset();

        Tmp.v1.trns(b.rotation(), baseLen * 1.1f);
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.4f * b.fout(), colors[0], 0.6f);
    }
}














