package newhorizon.expand.bullets;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;
import static newhorizon.content.NHStatValues.buildSharedBulletTypeStat;

/**Bullet with kinetic damage and energy damage*/
public class AdaptBulletType extends BasicBulletType implements TypeDamageBulletType{
    public String bundleName = "nh.bullet.desc";

    @Override
    public String bundleName() {return bundleName;}

    @Override
    public void init(Bullet b) {
        super.init(b);
        applyExtraMultiplier(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        typedHitEntity(this, b, entity, health);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        typedCreateSplash(this, b, x, y);
    }
}
