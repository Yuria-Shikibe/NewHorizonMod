package newhorizon.expand.bullets.raid;

import arc.struct.Seq;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import newhorizon.content.NHBullets;
import newhorizon.expand.logic.RaidBulletUtil;

public class RandomRaidBulletType extends BasicRaidBulletType {
    public static final float proxySpeed = 10f;
    public static final float proxyLifetime = 120f;
    public static final Seq<BulletType> pool = new Seq<>();

    public RandomRaidBulletType() {
        speed = proxySpeed;
        lifetime = proxyLifetime;
        collides = collidesAir = collidesGround = collidesTiles = false;
        hittable = false;
        drawSize = 0;
    }

    public static boolean handles(BulletType type) {
        return type instanceof RandomRaidBulletType;
    }

    public static void initPool() {
        if (!pool.isEmpty()) return;
        pool.add(NHBullets.artilleryHydro, NHBullets.artilleryMulti, NHBullets.artilleryNgt, NHBullets.artilleryFusion);
        pool.add(NHBullets.artilleryPhase, NHBullets.shieldDestroyer, NHBullets.ancientArtilleryProjectile, NHBullets.ancientBall);
        pool.add(NHBullets.ancientStd, NHBullets.pesterBlackHole, NHBullets.nuBlackHole, NHBullets.laugraBullet);
        pool.add(NHBullets.collapserBullet, NHBullets.railGun1, NHBullets.railGun2, NHBullets.railGun3);
        pool.add(NHBullets.declineProjectile, NHBullets.atomSeparator, NHBullets.blastEnergyPst, NHBullets.blastEnergyNgt);
        pool.add(NHBullets.warperBullet, NHBullets.airRaidBomb, NHBullets.hyperBlastLinker, NHBullets.hyperBlast);
        pool.add(NHBullets.arc_9000, NHBullets.eternity, NHBullets.arc_9000_frag, NHBullets.synchroZeta);
        pool.add(NHBullets.synchroThermoPst, NHBullets.synchroFusionEnergy, NHBullets.synchroTitanium, NHBullets.synchroTungsten);
        pool.add(NHBullets.missileTitanium, NHBullets.missileThorium, NHBullets.missileZeta, NHBullets.missileNormal);
        pool.add(NHBullets.missileStrike, NHBullets.ultFireball, NHBullets.basicSkyFrag, NHBullets.annMissile);
        pool.add(NHBullets.guardianBullet, NHBullets.guardianBulletLightningBall, NHBullets.saviourBullet, NHBullets.basicRaid);
        pool.add(NHBullets.raidBulletType);
        pool.removeAll(type -> type == null);
    }

    public static void fire(Team team, float x, float y, float angle, float damage, float velocityScl, float dst, float aimX, float aimY) {
        if (pool.isEmpty()) initPool();
        if (pool.isEmpty()) return;
        BulletType type = pool.random();
        RaidBulletUtil.spawn(type, team, x, y, angle, damage, velocityScl, dst, aimX, aimY);
    }

    @Override
    public Bullet create(Entityc owner, Team team, float x, float y, float rotation, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY) {
        float dst = lifetimeScl * proxySpeed * proxyLifetime;
        fire(team, x, y, rotation, damage, velocityScl, dst, aimX, aimY);
        return null;
    }
}
