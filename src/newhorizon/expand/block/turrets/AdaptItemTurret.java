package newhorizon.expand.block.turrets;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import newhorizon.expand.block.special.DeviceBase;

import static mindustry.Vars.world;

public class AdaptItemTurret extends ItemTurret {
    public AdaptItemTurret(String name) {
        super(name);
    }

    public class AdaptItemTurretBuild extends ItemTurretBuild implements AdaptTurret {
        public ShootPattern pattern = new ShootPattern();
        public float reloadModifier = 1f;
        public float kineticModifier = 1f;
        public float energyModifier = 1f;
        public float rangeModifier = 1f;

        public int lastDeviceBasePos;

        @Override
        public void created() {
            super.created();
            resetModifier();
        }

        public void resetModifier() {
            pattern = shoot;
            reloadModifier = 1f;
            kineticModifier = 1f;
            energyModifier = 1f;
            rangeModifier = 1f;
        }

        public void updatePattern(ShootPattern pattern) {
            if (pattern == this.pattern) return;
            this.pattern = pattern;
            barrelCounter = 0;
        }

        public void updateReloadModifier(float reloadModifier) {
            this.reloadModifier = reloadModifier;
        }

        public void updateKineticModifier(float kineticModifier) {
            this.kineticModifier = kineticModifier;
        }

        public void updateEnergyModifier(float energyModifier) {
            this.energyModifier = energyModifier;
        }


        protected void updateReload() {
            reloadCounter += delta() * ammoReloadMultiplier() * baseReloadSpeed() * reloadModifier;

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload);
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (!(world.build(lastDeviceBasePos) instanceof DeviceBase.DeviceBaseBuild)) resetModifier();
        }

        protected void shoot(BulletType type) {
            float
                    bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
                    bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            if (pattern.firstShotDelay > 0) {
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation);
            }

            pattern.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                int barrel = barrelCounter;

                if (delay > 0f) {
                    Time.run(delay, () -> {
                        //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                        int prev = barrelCounter;
                        barrelCounter = barrel;
                        bullet(type, xOffset, yOffset, angle, mover);
                        barrelCounter = prev;
                    });
                } else {
                    bullet(type, xOffset, yOffset, angle, mover);
                }
            }, () -> barrelCounter++);

            if (consumeAmmoOnce) {
                useAmmo();
            }
        }

        @Override
        public ShootPattern pattern() {
            return pattern;
        }

        @Override
        public float reloadModifier() {
            return reloadModifier;
        }

        @Override
        public float kineticModifier() {
            return kineticModifier;
        }

        @Override
        public float energyModifier() {
            return energyModifier;
        }

        @Override
        public float rangeModifier() {
            return rangeModifier;
        }
    }
}
