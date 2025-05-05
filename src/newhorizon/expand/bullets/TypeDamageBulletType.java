package newhorizon.expand.bullets;

import arc.scene.ui.layout.Table;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;

public interface TypeDamageBulletType {
    EventType.UnitDamageEvent bulletDamageEvent = new EventType.UnitDamageEvent();

    void setDamage(float kineticDamage, float energyDamage);

    void setSplash(float kineticDamage, float energyDamage, float splashRadius, int maxTarget);

    void setDescription(String key);

    float continuousKineticDamage();

    float continuousEnergyDamage();

    void buildStat(UnlockableContent t, Table bt, boolean compact);
}
