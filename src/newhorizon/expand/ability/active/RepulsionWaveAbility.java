package newhorizon.expand.ability.active;

import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.Vars.tilesize;

public class RepulsionWaveAbility extends ActiveAbility{
    public float radius = 300;
    public float damage = 10;

    public float time = 30;
    public float cooldown = 300;

    public BulletType b = new DOTBulletType(){{
        DOTDamage = damage = 40f;
        DOTRadius = 30f;
        radIncrease = 0.25f;
        lightningColor = Pal.techBlue;
        fx = NHFx.triSpark1;
    }};

    public Effect createEffect = new OptionalMultiEffect(
            NHFx.smoothColorCircle(Pal.techBlue, 165f, 150f, 0.6f),
            NHFx.circleOut(145f, 160f, 2)
    );

    @Override
    public void trigger(Unit unit) {
        NHSounds.shock.at(unit);
        unit.apply(NHStatusEffects.intercepted, 120f);
        createEffect.at(unit);
        b.create(unit, unit.x, unit.y, unit.rotation);
    }
}
