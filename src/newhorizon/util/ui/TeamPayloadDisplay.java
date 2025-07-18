package newhorizon.util.ui;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.CoreItemsDisplay;
import mindustry.ui.Styles;
import mindustry.world.Block;
import newhorizon.expand.block.inner.ModulePayload;

import static mindustry.Vars.*;
import static newhorizon.NHVars.worldData;

/**
 * @see mindustry.ui.CoreItemsDisplay
 */
public class TeamPayloadDisplay extends CoreItemsDisplay {
    public Seq<Block> payloadBlocks;

    public TeamPayloadDisplay() {
        payloadBlocks = content.blocks().select(b -> b instanceof ModulePayload);
    }

    public void rebuild() {
        clear();
        update(() -> {});
        Team team = player == null ? Team.derelict : player.team();

        table(itemDisplay -> {
            int i = 0;
            for(Item item : content.items()){
                if(team.items().has(item)){
                    itemDisplay.image(item.uiIcon).size(iconSmall).padRight(3).tooltip(t -> t.background(Styles.black6).margin(4f).add(item.localizedName).style(Styles.outlineLabel));
                    itemDisplay.label(() -> UI.formatAmount(team.items().get(item))).padRight(3).minWidth(52f).left().tooltip(t -> t.background(Styles.black6).margin(4f).label(() -> team.items().get(item) + "").style(Styles.outlineLabel));
                    if(++i % 4 == 0) row();
                }
            }
        }).width(82 * 4);

        image().color(team.color).size(80 * 4, 4);

        table(payloadDisplay -> {
            int i = 0;
            for (UnlockableContent content : payloadBlocks) {
                if (worldData.teamPayloadData.getPayload(team).get(content) > 0) {
                    payloadDisplay.image(content.uiIcon).size(iconSmall).padRight(3).tooltip(t -> t.background(Styles.black6).margin(4f).add(content.localizedName).style(Styles.outlineLabel));
                    payloadDisplay.label(() -> UI.formatAmount(worldData.teamPayloadData.getPayload(team).get(content))).padRight(3).minWidth(52f).left().style(Styles.outlineLabel);
                    if (++i % 4 == 0) row();
                }
            }
        }).width(82 * 4);
    }
}
