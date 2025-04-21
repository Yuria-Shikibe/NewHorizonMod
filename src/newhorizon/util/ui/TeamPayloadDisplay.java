package newhorizon.util.ui;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.ui.Styles;
import mindustry.world.Block;
import newhorizon.expand.block.inner.ModulePayload;

import static mindustry.Vars.*;
import static newhorizon.NHVars.worldData;

/**
 * @see mindustry.ui.CoreItemsDisplay
 */
public class TeamPayloadDisplay extends Table {
    public Seq<Block> payloadBlocks;
    public TeamPayloadDisplay(){
        payloadBlocks = content.blocks().select(b -> b instanceof ModulePayload);
        rebuild();
        update(this::rebuild);
    }

    void rebuild(){
        clear();
        margin(0);
        Team team = player == null? Team.derelict: player.team();
        int i = 0;
        for(UnlockableContent content: payloadBlocks){
            if(worldData.teamPayloadData.getPayload(team).get(content) > 0){
                image(content.uiIcon).size(iconSmall).padRight(3).tooltip(t -> t.background(Styles.black6).margin(4f).add(content.localizedName).style(Styles.outlineLabel));
                //TODO leaks garbage
                label(() -> UI.formatAmount(worldData.teamPayloadData.getPayload(team).get(content))).padRight(3).minWidth(52f).left().style(Styles.outlineLabel);
                if(++i % 4 == 0) row();
            }
        }
    }
}
