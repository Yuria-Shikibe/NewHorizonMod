package newhorizon.expand.block.turrets;

import arc.func.Cons;
import mindustry.gen.Bullet;
import mindustry.world.blocks.defense.turrets.PowerTurret;

public class AdaptPowerTurret extends PowerTurret {
    public AdaptPowerTurret(String name) {
        super(name);
    }

    public class AdaptPowerTurretBuild extends PowerTurretBuild {
        public Cons<Bullet> modifier = bullet -> {
        };

        @Override
        protected void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset) {
            if (modifier != null && bullet != null) modifier.get(bullet);
        }
    }
}
