package newhorizon.expand.cutscene.action;

import arc.audio.Sound;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import newhorizon.content.NHSounds;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;

import static mindustry.Vars.headless;
import static mindustry.Vars.player;

public class WarningSoundAction extends Action {
    public int allySound, enemySound;
    public Team team;

    public WarningSoundAction(int allySound, int enemySound, Team team) {
        super(0);
        this.allySound = allySound;
        this.enemySound = enemySound;
        this.team = team;
    }

    public WarningSoundAction(String[] args) {
        super(0);
        allySound = Integer.parseInt(args[0]);
        enemySound = Integer.parseInt(args[1]);
        team = ActionControl.parseTeam(args[2]);
    }

    public Sound warningSound(int soundID) {
        return switch (soundID) {
            case 0 -> NHSounds.alert2;
            case 1 -> NHSounds.alarm;
            case 2 -> Sounds.unlock;
            case 3 -> Sounds.wind3;
            default -> Sounds.none;
        };
    }

    @Override
    public void begin() {
        if (headless) return;
        if (player.team() == team) {
            warningSound(allySound).play();
        } else {
            warningSound(enemySound).play();
        }
    }
}
