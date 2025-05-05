package newhorizon.expand.block.turrets;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.world.blocks.defense.turrets.ContinuousTurret;

public class ContinuousOverheatTurret extends ContinuousTurret {
    public float overheatTime = 360f;
    public float overheatCoolAmount = 1.25f;
    public float slowDownReloadTime = 150f;

    public float maxHeatEffectChance = 0.3f;
    public Effect heatEffect = Fx.reactorsmoke;

    public ContinuousOverheatTurret(String name) {
        super(name);

        shootCone = 360f;
    }

    public class ContinuousOverheatTurretBuild extends ContinuousTurretBuild {
        public float overheat = 0;
        public float slowDownReload = 0f;

        public boolean requireCompleteCooling = false;


        @Override
        public void updateTile(){
            updateCooldown();
            if(overheat < overheatTime && !requireCompleteCooling){
                super.updateTile();
            }else{
                forceCoolDown();
            }
        }

        public void updateCooldown(){
            if(slowDownReload >= 1f){
                slowDownReload -= Time.delta;
            }else if (!requireCompleteCooling){
                coolDown();
            }

            if(overheat > overheatTime * 0.3f){
                if(Mathf.chanceDelta(maxHeatEffectChance * (requireCompleteCooling ? 1 : overheat / overheatTime))){
                    heatEffect.at(x + Mathf.range(Vars.tilesize * size / 2), y + Mathf.range(Vars.tilesize * size / 2), rotation, heatColor);
                }
            }
        }

        public void forceCoolDown(){
            coolDown();

            slowDownReload = 0;
            shootWarmup = linearWarmup? Mathf.approachDelta(shootWarmup, 0, shootWarmupSpeed): Mathf.lerpDelta(shootWarmup, 0, shootWarmupSpeed);

            unit.tile(this);
            unit.rotation(rotation);
            unit.team(team);

            curRecoil = Mathf.approachDelta(curRecoil, 0, 1 / recoilTime);
            recoilOffset.trns(rotation, -Mathf.pow(curRecoil, recoilPow) * recoil);

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(overheat <= 0){
                overheat = 0;
                requireCompleteCooling = false;
            }
        }

        @Override
        protected void updateShooting(){
            if(bullets.any()){
                return;
            }

            if(canConsume() && !charging() && shootWarmup >= minWarmup){
                shoot(peekAmmo());
            }else{
                overheat = Mathf.approachDelta(overheat, overheatTime + 0.05f, efficiency * timeScale / (1 + (liquids.current() == null ? 0 : liquids.current().heatCapacity)));
                if(overheat > overheatTime)requireCompleteCooling = true;
            }
        }

        @Override
        protected void shoot(BulletType type){
            super.shoot(type);

            slowDownReload = slowDownReloadTime;
        }

        public void coolDown(){
            if (overheat > 0){
                overheat -= overheatCoolAmount * (1 + coolantEfficiency()) * Time.delta;
            }
        }

        public float coolantEfficiency(){
            return liquids.current() == null ? 0 : liquids.current().heatCapacity;
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            write.f(overheat);
            write.f(slowDownReload);
            write.bool(requireCompleteCooling);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            overheat = read.f();
            slowDownReload = read.f();
            requireCompleteCooling = read.bool();
        }
    }
}
