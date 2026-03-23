package newhorizon.expand.logic.components.action;

import arc.flabel.FLabel;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.headless;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class WarningIconAction extends Action {
    public int icon;
    public Team team;
    public String text;

    @Override
    public String actionName() {
        return "warning_icon";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = Math.max(ParseUtil.getFirstFloat(tokens), 1f) * Time.toSeconds;
        icon = ParseUtil.getNextInt(tokens);
        team = ParseUtil.getNextTeam(tokens);
        text = ParseUtil.getNextString(tokens);
    }


    public TextureRegion warningIcon() {
        return switch (icon) {
            case 0 -> NHContent.raid;
            case 1 -> NHContent.fleet;
            case 2 -> NHContent.capture;
            default -> NHContent.objective;
        };
    }

    @Override
    public void begin() {
        if (headless) return;

        NHUIFunc.showLabel(duration / Time.toSeconds, t -> {
            t.background(Styles.black5);
            t.table(t2 -> {
                t2.table(left -> {
                    left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(-42, 0, 0, -40).color(team.color).row();
                    left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, 0, -42, -40).color(team.color).row();

                    //left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padBottom(54f).padRight(-12).color(team.color).row();
                    //left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(team.color).row();
                    //left.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padTop(54f).padRight(-32).color(team.color).row();
                }).pad(0).growX();
                t2.image(warningIcon()).fill().color(team.color);
                t2.table(right -> {
                    right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(-42, -40, 0, 0).color(team.color).row();
                    right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).pad(0, -40, -42, 0).color(team.color).row();

                    //right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padBottom(54f).padLeft(-32).color(team.color).row();
                    //right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(team.color).row();
                    //right.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padTop(54f).padLeft(-12).color(team.color).row();
                }).pad(0).growX();
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + text + " >>")).color(team.color).padBottom(4).row()).growX().fillY();
        });
    }
}
