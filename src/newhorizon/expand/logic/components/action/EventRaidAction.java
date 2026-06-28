package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.ui.Styles;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.RaidState;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.RaidBulletUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ui.RaidMarker;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.expand.net.NHCall;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.headless;
import static mindustry.Vars.net;
import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventRaidAction extends Action {
    public RaidPreset raidType = RaidPreset.valueOf("PRESET_RAID_1");

    public int customBulletType = 1;
    public BulletType customBullet;

    public boolean overrideRaidStats = false, overrideDefaultCoordinate = false;

    public Team team = Team.crux;
    public float alertTime = 15f, raidTime = 5f, raidScale = 1, inaccuracy = 40f;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0;
    public int syncSeed;

    private boolean popupDisplayed;
    private int raidCounter;

    @Override
    public String actionName() {
        return "event-raid";
    }

    @Override
    public boolean skippable() {
        return false;
    }

    @Override
    public void parseTokens(String[] tokens) {
        raidType = RaidPreset.valueOf(ParseUtil.getFirstToken(tokens));
        if (raidType == RaidPreset.CUSTOM_RAID) {
            customBulletType = ParseUtil.getNextInt(tokens);
        }
        team = ParseUtil.getNextTeam(tokens);

        overrideRaidStats = ParseUtil.getNextBool(tokens);
        if (overrideRaidStats) {
            alertTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
            raidTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
            raidScale = ParseUtil.getNextFloat(tokens);
            inaccuracy = ParseUtil.getNextFloat(tokens);
        }

        overrideDefaultCoordinate = ParseUtil.getNextBool(tokens);
        if (overrideDefaultCoordinate) {
            sourceX = ParseUtil.getNextFloat(tokens) * tilesize;
            sourceY = ParseUtil.getNextFloat(tokens) * tilesize;
            targetX = ParseUtil.getNextFloat(tokens) * tilesize;
            targetY = ParseUtil.getNextFloat(tokens) * tilesize;
        }

    }

    @Override
    public void postInit() {
        super.postInit();

        if (!overrideRaidStats) {
            alertTime = raidType.alertTime * Time.toSeconds;
            raidTime = raidType.raidTime * Time.toSeconds;
            raidScale = raidType.raidScale;
            inaccuracy = raidType.inaccuracy;
        }

        duration = alertTime + raidTime;
        raidScale *= RaidState.scale();
    }

    @Override
    public void begin() {
        if (!headless) {
            showRaidPresentation();
        }
        if (syncSeed == 0) syncSeed = (int) Time.time;
        if (net.server() && net.active()) {
            NHCall.syncRaidAlert(this);
        }
    }

    public void applyNetworkState(float lifeTimer, int raidCounter) {
        this.lifeTimer = lifeTimer;
        this.raidCounter = raidCounter;
        if (lifeTimer > alertTime) popupDisplayed = true;
    }

    public int raidCounter() {
        return raidCounter;
    }

    private void showRaidPresentation() {
        showRaidToast();
        NHUIFunc.showLabel(4.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                var icon = Core.atlas.find(warningIconName());
                if (icon.width == 192) {
                    t2.table(left -> left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, 0, -9).color(team.color).row()).pad(0).growX();
                    t2.image(icon).fill().color(team.color);
                    t2.table(right -> right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, -9, 0, 0).color(team.color).row()).pad(0).growX();
                } else if (icon.width == 288) {
                    t2.table(left -> {
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(-42, 0, 0, -17).color(team.color).row();
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, -42, -17).color(team.color).row();
                    }).pad(0).growX();
                    t2.image(icon).fill().color(team.color);
                    t2.table(right -> {
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(-42, -17, 0, 0).color(team.color).row();
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, -17, -42, 0).color(team.color).row();
                    }).pad(0).growX();
                } else if (icon.width == 384) {
                    t2.table(left -> {
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padBottom(25f).padRight(-14).color(team.color).row();
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-52).color(team.color).row();
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padTop(25f).padRight(-14).color(team.color).row();
                    }).pad(0).growX();
                    t2.image(icon).fill().color(team.color);
                    t2.table(right -> {
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padBottom(25f).padLeft(-14).color(team.color).row();
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-52).color(team.color).row();
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padTop(25f).padLeft(-14).color(team.color).row();
                    }).pad(0).growX();
                } else {
                    t2.image(icon).fill().color(team.color);
                }
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get(alertBundleKey()) + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });

        RaidMarker marker = new RaidMarker();
        marker.setMarkPosition(targetX, targetY)
                .setDuration(alertTime)
                .bindLifeTimer(() -> this.lifeTimer);
        marker.bindAlertTime(() -> this.alertTime);
        marker.setMarkColor(team.color)
                .setRadius(inaccuracy)
                .setAngle(Angles.angle(sourceX, sourceY, targetX, targetY))
                .setIcon(Core.atlas.find(warningIconName()))
                .addMarker();
    }

    @Override
    public void act() {
        updateRaidPopup();

        if (RaidLogic.isRemoteClient()) return;
        if (!RaidState.enabled()) return;

        int raidCount = Mathf.round(Mathf.maxZero(lifeTimer - alertTime) / Time.toSeconds * raidScale);
        int raid = raidCount - raidCounter;
        raidCounter = raidCount;

        Rand rand = new Rand(syncSeed);
        int baseIndex = raidCounter - raid;
        for (int i = 0; i < raid; i++) {
            createBullet(rand, baseIndex + i);
        }
    }

    private void updateRaidPopup() {
        if (headless) return;
        if (lifeTimer > alertTime && !popupDisplayed) {
            popupDisplayed = true;
            showRaidToast();
        }
    }

    public BulletType bulletType() {
        if (customBullet != null) return customBullet;
        if (raidType == RaidPreset.CUSTOM_RAID) return RaidBulletUtil.resolve(customBulletType);
        return raidType.bulletType;
    }

    public void createBullet(Rand rand, int index) {
        BulletType bt = bulletType();
        rand.setSeed(syncSeed + index * 7919);
        float spread = inaccuracy;
        Tmp.v1.trns(rand.random(360f), rand.random(spread));
        float tx = targetX + Tmp.v1.x;
        float ty = targetY + Tmp.v1.y;
        float dst = Mathf.dst(sourceX, sourceY, tx, ty);
        float ang = Angles.angle(sourceX, sourceY, tx, ty);
        RaidBulletUtil.spawn(bt, team, sourceX, sourceY, ang, -1, 1f, dst, tx, ty);
    }

    public String alertBundleKey() {
        if (customBullet != null) return RaidBulletUtil.alertKey(customBullet);
        if (raidType == RaidPreset.CUSTOM_RAID) return RaidBulletUtil.alertKey(customBulletType);
        return "css-raid." + raidType.name().replace("_", "-").toLowerCase() + ".alert";
    }

    public String popupBundleKey() {
        if (customBullet != null) return RaidBulletUtil.popupKey(customBullet);
        if (raidType == RaidPreset.CUSTOM_RAID) return RaidBulletUtil.popupKey(customBulletType);
        return "css-raid." + raidType.name().replace("_", "-").toLowerCase() + ".popup";
    }

    public String warningIconName() {
        if (customBullet != null) return RaidBulletUtil.warningIcon(customBullet);
        if (raidType == RaidPreset.CUSTOM_RAID) return RaidBulletUtil.warningIcon(customBulletType);
        return raidType.warningIcon;
    }

    public void showRaidToast() {
        NHUIFunc.showToast(
                Core.atlas.find(warningIconName()),
                Core.bundle.format(popupBundleKey(), Strings.fixed(targetX / tilesize, 1), Strings.fixed(targetY / tilesize, 1)),
                raidType.raidAlarmSound,
                team.color
        );
    }
}
