package newhorizon.expand.logic.components.action;

import arc.audio.Sound;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import newhorizon.content.NHSounds;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.player;

public class WarningSoundAction extends Action {
    public int allySound, enemySound;
    public Team team;

    @Override
    public String actionName() {
        return "warning_sound";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        allySound = ParseUtil.getNextInt(tokens);
        enemySound = ParseUtil.getNextInt(tokens);
        team = ParseUtil.getNextTeam(tokens);
    }


    public Sound warningSound(int soundID) {
        return switch (soundID) {
            case 0 -> NHSounds.alert2;
            case 1 -> NHSounds.alarm;
            case 2 -> Sounds.uiUnlock;
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
