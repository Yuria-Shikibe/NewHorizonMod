package newhorizon.expand.cutscene.event;

import arc.Core;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.ui.Styles;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.expand.bullets.raid.BasicRaidBulletType;
import newhorizon.expand.cutscene.components.WorldActionEvent;
import newhorizon.util.ui.NHUIFunc;

import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidEvent extends WorldActionEvent {
    //target position. override the targetFlag
    public float targetX = 0, targetY = 0, bulletDamage = 100, bulletInterval = 20f, inaccuracyRadius = 80;
    public int bulletCount = 3, bulletType = 0, targetFlag = 0;

    public RaidEvent(Team team, float sourceX, float sourceY, float targetX, float targetY, float bulletDamage, float bulletInterval, float inaccuracyRadius, int bulletCount, int bulletType, int targetFlag, float duration) {
        super(team, sourceX, sourceY, duration);
        this.targetX = targetX;
        this.targetY = targetY;
        this.bulletDamage = bulletDamage;
        this.bulletInterval = bulletInterval;
        this.inaccuracyRadius = inaccuracyRadius;
        this.bulletCount = bulletCount;
        this.bulletType = bulletType;
        this.targetFlag = targetFlag;
    }

    @Override
    public void activate() {
        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(team.color);
                t2.image(NHContent.raid).fill().color(team.color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(team.color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });
    }

    public BulletType bulletType() {
        //get bullet type according to bulletType
        return NHBullets.basicRaid;
    }

    @Override
    public void trigger() {
        //shift value for parallel raid
        for (int i = 0; i < bulletCount; i++) {
            Time.run(i * bulletInterval, this::createBullet);
        }
    }

    public void createBullet() {
        Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracyRadius));
        float dst = Mathf.dst(worldX, worldY, targetX, targetY);
        float ang = Angles.angle(worldX, worldY, targetX, targetY);
        float lifetimeScl = dst / (bulletType().speed * bulletType().lifetime);
        Call.createBullet(bulletType(), team, worldX + Tmp.v1.x, worldY + Tmp.v1.y, ang, bulletDamage, 1f, lifetimeScl);
    }
}
