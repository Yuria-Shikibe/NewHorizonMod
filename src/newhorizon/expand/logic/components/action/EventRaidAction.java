package newhorizon.expand.logic.components.action;

import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.ui.Styles;
import newhorizon.content.NHBullets;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.game.MapMarker.RaidIndicator;
import newhorizon.expand.game.MapObjectives.TriggerObjective;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.cutscene.types.AlertType;
import newhorizon.expand.logic.cutscene.types.HudIcon;
import newhorizon.expand.logic.cutscene.types.RaidControllerType;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.util.ui.NHUIFunc;

import java.util.Objects;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventRaidAction extends Action {
    public RaidPreset raidType = RaidPreset.valueOf("PRESET_RAID_0");

    public String flag = "raid-executor", timer = "raid-timer";

    public boolean overrideDefaultTeam = false;
    public Team team = Team.crux;

    public float alertTime = 15f, raidTime = 5f;
    public float raidScale = 1, inaccuracy = 40f;

    public boolean overrideDefaultCoordinate = false;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0;

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

        alertTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
        raidTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;

        duration = alertTime + raidTime;

        sourceX = ParseUtil.getNextFloat(tokens);
        sourceY = ParseUtil.getNextFloat(tokens);
        targetX = ParseUtil.getNextFloat(tokens);
        targetY = ParseUtil.getNextFloat(tokens);
    }

    public BulletType bulletType() {
        return RaidBullets.raidBullet_1;
    }

    @Override
    public void begin() {
        if (headless) return;

        /*
        warningSound.sound.play();
        NHUIFunc.showLabel(duration / Time.toSeconds, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.table(left -> left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, 0,-9).color(team.color).row()).pad(0).growX();
                t2.image(hudIcon.icon).fill().color(team.color);
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

         */
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
