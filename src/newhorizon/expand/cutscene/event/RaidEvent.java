package newhorizon.expand.cutscene.event;

import arc.Core;
import arc.flabel.FLabel;
import arc.graphics.Color;
import mindustry.game.Team;
import mindustry.ui.Styles;
import newhorizon.content.NHBullets;
import newhorizon.content.NHContent;
import newhorizon.expand.cutscene.components.WorldActionEvent;
import newhorizon.util.ui.NHUIFunc;

import static newhorizon.util.ui.TableFunc.OFFSET;

public class RaidEvent extends WorldActionEvent {
    public RaidEvent(Team team, int worldX, int worldY, float duration) {
        super(team, worldX, worldY, duration);
    }

    @Override
    public void activate() {
        NHUIFunc.showLabel(2.5f, t -> {
            Color color = team.color;

            t.background(Styles.black5);

            t.table(t2 -> {
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(color);
                t2.image(NHContent.raid).fill().color(color);
                t2.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(color);
            }).growX().pad(OFFSET / 2).fillY().row();

            t.table(l -> l.add(new FLabel("<< " + Core.bundle.get("nh.cutscene.event.raid-alert") + " >>")).color(color).padBottom(4).row()).growX().fillY();
        });
    }

    @Override
    public void trigger() {
        NHBullets.airRaidBomb.create(null, team, worldX, worldY, 225, 100, 0.5f, 1, null, null, 0, 0);
    }
}
