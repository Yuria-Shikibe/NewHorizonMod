package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.ui.Styles;
import newhorizon.content.NHBullets;
import newhorizon.content.NHSounds;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.cutscene.types.RaidControllerType;
import newhorizon.util.ui.NHUIFunc;

import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidAction extends Action {
    public String raidControllerType = "defaultController";
    public String flag = "raid-executor", timer = "raid-timer";

    public float alertTime = 15f, raidTime = 5f;

    public String raidType = "PRESET_RAID_0";

    public String warningIcon = "raid";
    public String warningSound = "alarm";
    public String warningText = "default_raid_text";

    public Team team;
    public int bulletType = 0, bulletCount = 5;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0;
    public float inaccuracy = 40f;

    private int raidCounter;

    @Override
    public String actionName() {
        return "raid";
    }

    @Override
    public boolean skippable() {
        return false;
    }

    @Override
    public void parseTokens(String[] tokens) {
        RaidControllerType type = RaidControllerType.valueOf(ParseUtil.getFirstToken(tokens));

        flag = ParseUtil.getNextToken(tokens);
        timer = ParseUtil.getNextToken(tokens);

        alertTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
        raidTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;

        duration = alertTime + raidTime;

        switch (type) {
            case defaultController -> {
                raidType = ParseUtil.getNextToken(tokens);
            }
            case customController -> {
                bulletType = ParseUtil.getNextInt(tokens);
                bulletCount = ParseUtil.getNextInt(tokens);

                warningIcon = ParseUtil.getNextToken(tokens);
                warningSound = ParseUtil.getNextToken(tokens);
                warningText = ParseUtil.getNextString(tokens);
            }
            case teamDefaultController -> {
                team = ParseUtil.getNextTeam(tokens);
                raidType = ParseUtil.getNextToken(tokens);
            }
            case teamCustomController -> {
                team = ParseUtil.getNextTeam(tokens);
                bulletType = ParseUtil.getNextInt(tokens);
                bulletCount = ParseUtil.getNextInt(tokens);

                warningIcon = ParseUtil.getNextToken(tokens);
                warningSound = ParseUtil.getNextToken(tokens);
                warningText = ParseUtil.getNextString(tokens);
            }
            case coordinateDefaultController -> {
                team = ParseUtil.getNextTeam(tokens);
                raidType = ParseUtil.getNextToken(tokens);

                sourceX = ParseUtil.getFirstFloat(tokens);
                sourceY = ParseUtil.getFirstFloat(tokens);
                targetX = ParseUtil.getFirstFloat(tokens);
                targetY = ParseUtil.getFirstFloat(tokens);
            }
            case coordinateCustomController ->{
                team = ParseUtil.getNextTeam(tokens);
                bulletType = ParseUtil.getNextInt(tokens);
                bulletCount = ParseUtil.getNextInt(tokens);

                warningIcon = ParseUtil.getNextToken(tokens);
                warningSound = ParseUtil.getNextToken(tokens);
                warningText = ParseUtil.getNextString(tokens);

                sourceX = ParseUtil.getFirstFloat(tokens);
                sourceY = ParseUtil.getFirstFloat(tokens);
                targetX = ParseUtil.getFirstFloat(tokens);
                targetY = ParseUtil.getFirstFloat(tokens);
            }
        }
    }

    public BulletType bulletType() {
        return switch (bulletType) {
            case 1 -> RaidBullets.raidBullet_1;
            case 2 -> RaidBullets.raidBullet_2;
            case 3 -> RaidBullets.raidBullet_3;
            case 4 -> RaidBullets.raidBullet_4;
            case 5 -> RaidBullets.raidBullet_5;
            case 6 -> RaidBullets.raidBullet_6;
            case 7 -> RaidBullets.raidBullet_7;
            case 8 -> RaidBullets.raidBullet_8;
            default -> NHBullets.railGun1;
        };
    }

    @Override
    public void begin() {
        if (headless) return;

        NHSounds.alert2.play();
        NHUIFunc.showLabel(duration / Time.toSeconds, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.table(left -> left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, 0,-9).color(team.color).row()).pad(0).growX();
                t2.image(Core.atlas.find(warningIcon)).fill().color(team.color);
                t2.table(right -> right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, -9, 0, 0).color(team.color).row()).pad(0).growX();
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + warningText + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });

        state.rules.objectives.each(mapObjective -> {
            if (mapObjective instanceof TriggerObjective obj && Objects.equals(obj.timer, timer)) {
                obj.trigger(alertTime * Time.toSeconds);
                for (MapObjectives.ObjectiveMarker marker: obj.markers) {
                    if (marker instanceof RaidIndicator idc){
                        idc.init(team.id, 1, inaccuracy, timer)
                                .setPosition(Tmp.v2.set(sourceX, sourceY), Tmp.v3.set(targetX, targetY));
                    }
                }
            }
        });
    }

    public void end() {
        state.rules.objectiveFlags.remove(flag);
    }

    @Override
    public void act() {
        int raidCount = Mathf.round(((lifeTimer - alertTime) / raidTime) * raidCounter);
        int raid = raidCount - raidCounter;
        raidCounter = raidCount;

        for (int i = 0; i < raid; i++) {
            createBullet();
        }
    }

    public void createBullet() {
        Tmp.v1.trns(Mathf.random(360f), 1);
        float dst = Mathf.dst(sourceX, sourceY, targetX, targetY);
        float ang = Angles.angle(sourceX, sourceY, targetX, targetY);
        float lifetimeScl = dst / (bulletType().speed * bulletType().lifetime);
        Call.createBullet(bulletType(), team, sourceX + Tmp.v1.x, sourceY + Tmp.v1.y, ang, -1, 1f, lifetimeScl);
    }
}
