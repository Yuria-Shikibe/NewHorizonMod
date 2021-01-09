package newhorizon.bullets;

import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Velc;
import mindustry.graphics.Drawf;
import newhorizon.content.NHFx;

public class DelayLaserType extends LaserBulletType {
    public float effectTime;
    public float shootShake = 2f;
    public Sound shootSound = Sounds.laserblast;
    public Sound chargeSound = Sounds.lasercharge2;
    public DelayLaserType(float damage, float effectTime){
        super(damage);
        this.effectTime = effectTime;
    }

    @Override
    public void init() {
        super.init();
        lifetime += effectTime;
        this.laserEffect = NHFx.laserEffect(15);
    }

    @Override
    public void update(Bullet b) {
        if (b.time - effectTime > 0 && b.data() instanceof Boolean && !(boolean)b.data()){
            shoot(b);
        }
    }

    @Override
    public void init(Bullet b) {
        b.data(false);
        chargeSound.at(b, Mathf.random(0.8f, 1.05f));
    }

    public void shoot(Bullet b) {
        float resultLength = Damage.collideLaser(b, this.length, this.largeHit);
        float rot = b.rotation();

        //laserEffect.at(b.x, b.y, b.rotation(), resultLength * 0.75F);
        Effect.shake(shootShake, shootShake, b);
        shootSound.at(b);

        b.data(true);

        if (this.lightningSpacing > 0.0F) {
            int idx = 0;

            for(float i = 0.0F; i <= resultLength; i += this.lightningSpacing) {
                float cx = b.x + Angles.trnsx(rot, i);
                float cy = b.y + Angles.trnsy(rot, i);
                int f = idx++;

                for (int s : Mathf.signs) {
                    Time.run((float) f * this.lightningDelay, () -> {
                        if (b.isAdded() && b.type == this) {
                            Lightning.create(b, this.lightningColor, this.lightningDamage < 0.0F ? this.damage : this.lightningDamage, cx, cy, rot + (float) (90 * s) + Mathf.range(this.lightningAngleRand), this.lightningLength + Mathf.random(this.lightningLengthRand));
                        }
                    });
                }
            }
        }
    }

    public void effectDraw(Bullet b){

    }

    public void draw(Bullet b){
        effectDraw(b);

        if(b.time - effectTime > 0) {
            float realLength = b.fdata;
            float fin = Mathf.clamp((b.time - effectTime) / (b.lifetime - effectTime));
            float fout = Mathf.clamp(1f - fin);
            float fslope = (0.5F - Math.abs(fin - 0.5F)) * 2.0F;
            float f = Mathf.curve(fin, 0.0F, 0.2F);

            float baseLen = realLength * f;
            float cwidth = this.width;
            float compound = 1.0F;
            Lines.lineAngle(b.x, b.y, b.rotation(), baseLen);

            for (Color color : this.colors) {
                Draw.color(color);
                Lines.stroke((cwidth *= this.lengthFalloff) * fout);
                Lines.lineAngle(b.x, b.y, b.rotation(), baseLen, false);
                Tmp.v1.trns(b.rotation(), baseLen);
                Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Lines.getStroke() * 1.22F, cwidth * 2.0F + this.width / 2.0F, b.rotation());
                Fill.circle(b.x, b.y, 1.0F * cwidth * fout);

                for (int i : Mathf.signs) {
                    Drawf.tri(b.x, b.y, this.sideWidth * fout * cwidth, this.sideLength * compound, b.rotation() + this.sideAngle * (float) i);
                }

                compound *= this.lengthFalloff;
            }

            Draw.reset();
            Tmp.v1.trns(b.rotation(), baseLen * 1.1F);
            Drawf.light(b.team, b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, this.width * 1.4F * b.fout(), this.colors[0], 0.6F);
        }
    }

    public void drawLight(Bullet b) {
    }
}
