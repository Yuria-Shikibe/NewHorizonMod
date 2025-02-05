package newhorizon.expand.cutscene.components;

import mindustry.game.Team;
import newhorizon.content.NHBullets;

public class WorldActionEvent {
    public Team team = Team.derelict;
    public int worldX, worldY;
    public float duration;

    public WorldActionEvent(Team team, int worldX, int worldY, float duration) {
        this.team = team;
        this.worldX = worldX;
        this.worldY = worldY;
        this.duration = duration;
    }

    public WorldActionEvent(int worldX, int worldY, float duration) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.duration = duration;
    }

    public void draw(float progress){}

    public void activate(){}

    public void trigger(){}
}
