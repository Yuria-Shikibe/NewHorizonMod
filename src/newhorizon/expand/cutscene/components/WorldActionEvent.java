package newhorizon.expand.cutscene.components;

import mindustry.game.Team;
import newhorizon.content.NHBullets;

public class WorldActionEvent {
    public Team team = Team.derelict;
    public float worldX, worldY;
    public float waitDuration;

    public WorldActionEvent(Team team, float worldX, float worldY, float waitDuration) {
        this.team = team;
        this.worldX = worldX;
        this.worldY = worldY;
        this.waitDuration = waitDuration;
    }

    public WorldActionEvent(float worldX, float worldY, float waitDuration) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.waitDuration = waitDuration;
    }

    public void draw(float progress){}

    public void activate(){}

    public void trigger(){}
}
