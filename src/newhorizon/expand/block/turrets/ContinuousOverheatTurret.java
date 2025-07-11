package newhorizon.expand.block.turrets;

import arc.Core;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.ContinuousTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHColor;
import newhorizon.content.NHStatValues;
import newhorizon.content.NHStats;

public class ContinuousOverheatTurret extends ContinuousTurret {
    public float overheatTime = 15 * 60f;
    public float overheatCoolAmount = 3f;
    public float slowDownReloadTime = 150f;

    public float chargeTime = 300f;

    public float maxHeatEffectChance = 0.3f;
    public Effect heatEffect = Fx.reactorsmoke;

    public ContinuousOverheatTurret(String name) {
        super(name);

        shootCone = 360f;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.overheatCooldown, overheatTime / overheatCoolAmount / Time.toSeconds, StatUnit.seconds);

        if (coolant != null) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster, NHStatValues.boosters(reload, coolant.amount, coolantMultiplier, true, l -> l.coolant && consumesLiquid(l), true));
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("charge",
                (ContinuousOverheatTurretBuild entity) -> new Bar(
                        () -> Core.bundle.format("nh.bar.charge", Strings.autoFixed(Mathf.clamp(Mathf.clamp(entity.chargeProgress / chargeTime), 0, chargeTime) * 100, 0)),
                        () -> Tmp.c1.set(NHColor.lightSky).lerp(NHColor.darkEnrFront, Mathf.clamp(entity.chargeProgress / chargeTime)),
                        () -> entity.chargeProgress / chargeTime
                )
        );
        addBar("overheat",
                (ContinuousOverheatTurretBuild entity) -> new Bar(
                        () -> Core.bundle.format("nh.bar.overheat", Strings.autoFixed(Mathf.clamp(entity.overheat / overheatTime, 0, overheatTime) * 100, 0)),
                        () -> entity.requireCompleteCooling ? Pal.redderDust : Pal.powerLight,
                        () -> entity.overheat / overheatTime
                )
        );
    }

    public class ContinuousOverheatTurretBuild extends ContinuousTurretBuild {
        public float overheat = 0;
        public float slowDownReload = 0f;
        public float chargeProgress = 0f;

        public boolean requireCompleteCooling = false;

        @Override
        public void updateTile() {
            updateCooldown();
            if (overheat < overheatTime && !requireCompleteCooling) {
                super.updateTile();
                chargeProgress += edelta();
            } else {
                forceCoolDown();
            }
        }

        public void updateCooldown() {
            if (slowDownReload >= 1f) {
                slowDownReload -= Time.delta;
            } else {
                chargeProgress = Mathf.lerpDelta(chargeProgress, 0f, 0.2f);
                if (!requireCompleteCooling) {
                    coolDown();
                }
            }

            if (overheat > overheatTime * 0.3f) {
                if (Mathf.chanceDelta(maxHeatEffectChance * (requireCompleteCooling ? 1 : overheat / overheatTime))) {
                    heatEffect.at(x + Mathf.range(Vars.tilesize * size / 2), y + Mathf.range(Vars.tilesize * size / 2), rotation, heatColor);
                }
            }
        }

        public void forceCoolDown() {
            coolDown();

            if (soundLoop != null) soundLoop.update(x, y, shouldActiveSound(), activeSoundVolume());

            slowDownReload = 0;
            chargeProgress = 0;
            shootWarmup = linearWarmup ? Mathf.approachDelta(shootWarmup, 0, shootWarmupSpeed) : Mathf.lerpDelta(shootWarmup, 0, shootWarmupSpeed);

            unit.tile(this);
            unit.rotation(rotation);
            unit.team(team);

            curRecoil = Mathf.approachDelta(curRecoil, 0, 1 / recoilTime);
            recoilOffset.trns(rotation, -Mathf.pow(curRecoil, recoilPow) * recoil);

            if (logicControlTime > 0) {
                logicControlTime -= Time.delta;
            }

            if (overheat <= 0) {
                overheat = 0;
                requireCompleteCooling = false;
            }
        }

        @Override
        public float activeSoundVolume() {
            return shootWarmup;
        }

        public void coolDown() {
            if (overheat > 0) {
                overheat -= overheatCoolAmount * (1 + coolantEfficiency()) * Time.delta;
            }
        }

        @Override
        protected void updateBullet(BulletEntry entry) {
            super.updateBullet(entry);
            entry.bullet.fdata = chargeProgress;

            slowDownReload = slowDownReloadTime;
            overheat = Mathf.approachDelta(overheat, overheatTime + 0.05f, efficiency * timeScale / (1 + (liquids.current() == null ? 0 : liquids.current().heatCapacity)));
            if (overheat > overheatTime) requireCompleteCooling = true;
        }

        public float coolantEfficiency() {
            return liquids.current() == null ? 0 : liquids.current().heatCapacity;
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            write.f(overheat);
            write.f(slowDownReload);
            write.f(chargeProgress);
            write.bool(requireCompleteCooling);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            overheat = read.f();
            slowDownReload = read.f();
            chargeProgress = read.f();
            requireCompleteCooling = read.bool();
        }
    }
}
