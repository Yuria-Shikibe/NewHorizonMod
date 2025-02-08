package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import arc.graphics.g2d.TextureRegion;
import mindustry.game.Team;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;
import newhorizon.util.ui.NHUIFunc;

import static newhorizon.util.ui.TableFunc.OFFSET;

public class WarningIconAction extends Action {
    public int icon;
    public Team team;
    public String message;

    public WarningIconAction(int icon, Team team, String message) {
        super(0);
        this.icon = icon;
        this.team = team;
        this.message = message;
    }

    public WarningIconAction(String[] args) {
        super(0);
        icon = Integer.parseInt(args[0]);
        team = ActionControl.phaseTeam(args[1]);
        message = ActionControl.phaseString(args[2]);
    }

    public TextureRegion warningIcon(){
        return switch (icon) {
            case 0 -> NHContent.raid;
            case 1 -> NHContent.fleet;
            case 2 -> NHContent.capture;
            default -> NHContent.objective;
        };
    }

    @Override
    public void begin() {
        NHUIFunc.showLabel(2.5f, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(team.color);
                t2.image(warningIcon()).fill().color(team.color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(team.color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + message + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });
    }
}
