package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.flabel.FLabel;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.ui.Styles;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventRaidAction extends Action {
    public RaidPreset raidType = RaidPreset.valueOf("PRESET_RAID_0");

    public String flag = "raid-executor", timer = "raid-timer";

    public boolean overrideRaidStats = false, overrideDefaultCoordinate = false;

    public Team team = Team.crux;
    public float alertTime = 15f, raidTime = 5f, raidScale = 1, inaccuracy = 40f;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0;

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

        flag = ParseUtil.getNextToken(tokens);
        timer = ParseUtil.getNextToken(tokens);
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
    }

    @Override
    public void begin() {
        if (headless) return;
        NHUIFunc.showToast(Core.atlas.find(raidType.warningIcon), "[#ff7b69]Caution: []Attack " + targetX + "," + targetY, raidType.raidAlarmSound);
        NHUIFunc.showLabel(4.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                var icon = Core.atlas.find(raidType.warningIcon);
                if (icon.width == 192) {
                    t2.table(left -> left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, 0,-9).color(team.color).row()).pad(0).growX();
                    t2.image(icon).fill().color(team.color);
                    t2.table(right -> right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, -9, 0, 0).color(team.color).row()).pad(0).growX();
                }else if (icon.width == 288) {
                    t2.table(left -> {
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(-42, 0, 0, -17).color(team.color).row();
                        left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, -42, -17).color(team.color).row();
                    }).pad(0).growX();
                    t2.image(icon).fill().color(team.color);
                    t2.table(right -> {
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(-42, -17, 0, 0).color(team.color).row();
                        right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, -17, -42, 0).color(team.color).row();
                    }).pad(0).growX();
                }else if (icon.width == 384) {
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
                }else {
                    t2.image(icon).fill().color(team.color);
                }
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("css-raid." + raidType.name()) + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });

        new HudMarker().setMarkPosition(targetX, targetY).setDuration(alertTime).setMarkColor(team.color).setRadius(inaccuracy).addMarker();
    }

    @Override
    public void act() {
        int raidCount = Mathf.round(Mathf.maxZero(lifeTimer - alertTime) / Time.toSeconds * raidScale);
        int raid = raidCount - raidCounter;
        raidCounter = raidCount;

        if (lifeTimer > alertTime && !popupDisplayed) {
            popupDisplayed = true;
            NHUIFunc.showToast(Core.atlas.find(raidType.warningIcon), "[#ff7b69]Caution: []Attack " + targetX + "," + targetY, raidType.raidAlarmSound);
        }

        for (int i = 0; i < raid; i++) {
            createBullet();
        }
    }

    public void createBullet() {
        Tmp.v1.trns(Mathf.random(360f), inaccuracy);
        float dst = Mathf.dst(sourceX, sourceY, targetX, targetY);
        float ang = Angles.angle(sourceX, sourceY, targetX, targetY);
        float lifetimeScl = dst / (raidType.bulletType.speed * raidType.bulletType.lifetime);
        Call.createBullet(raidType.bulletType, team, sourceX + Tmp.v1.x, sourceY + Tmp.v1.y, ang, -1, 1f, lifetimeScl);
    }
}
