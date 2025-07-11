package newhorizon.content;

import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import newhorizon.content.blocks.SpecialBlock;
import newhorizon.content.blocks.TurretBlock;
import newhorizon.expand.block.turrets.AdaptItemTurret;
import newhorizon.expand.type.Device;

public class NHDevices {
    public static Device rapidLoaderLight, burstShoot, device2;

    public static void load() {
        rapidLoaderLight = new Device("rapid-loader-light") {{
            installableBlocks.add(SpecialBlock.juniorModuleBeacon, SpecialBlock.seniorModuleBeacon, SpecialBlock.deviceTest);
            compatibleBlocks.add(TurretBlock.synchro, TurretBlock.argmot, TurretBlock.slavio);

            modifier = (source, target, intensity) -> {
                target.building.applyBoost(2f * intensity, 30f);
            };
        }};

        burstShoot = new Device("burst-shoot") {{
            installableBlocks.add(SpecialBlock.juniorModuleBeacon, SpecialBlock.seniorModuleBeacon, SpecialBlock.deviceTest);
            compatibleBlocks.add(TurretBlock.synchro, TurretBlock.argmot, TurretBlock.slavio);

            ShootPattern synchroBurst = new ShootMulti(
                    new ShootPattern(),
                    new ShootBarrel() {{
                        barrels = new float[]{-6.5f, 3f, 0f};
                        shots = 3;
                        shotDelay = 6f;
                    }},
                    new ShootBarrel() {{
                        barrels = new float[]{6.5f, 3f, 0f};
                        shots = 3;
                        shotDelay = 6f;
                    }}
            );

            modifier = (source, target, intensity) -> {
                if (target.building.block == TurretBlock.synchro) {
                    AdaptItemTurret.AdaptItemTurretBuild turret = (AdaptItemTurret.AdaptItemTurretBuild) target.building;
                    turret.lastDeviceBasePos = source.pos();

                    turret.updatePattern(synchroBurst);
                    turret.updateReloadModifier(1 / 3f);
                    turret.updateKineticModifier(2f);
                    turret.updateEnergyModifier(1.5f);
                }
            };
        }};

        device2 = new Device("device-2") {{
            installableBlocks.add(SpecialBlock.deviceTest);
            compatibleBlocks.add(TurretBlock.synchro, TurretBlock.argmot, TurretBlock.slavio);
        }};
    }
}
