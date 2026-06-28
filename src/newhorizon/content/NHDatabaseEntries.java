package newhorizon.content;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.TextureRegion;
import mindustry.game.EventType.ClientLoadEvent;
import newhorizon.NewHorizon;

import static mindustry.Vars.headless;

public class NHDatabaseEntries {
    public static DatabaseEntry raidThreat;

    public static void load() {
        raidThreat = new DatabaseEntry("new-horizon-raid-threat");
        Events.on(ClientLoadEvent.class, e -> Core.app.post(NHDatabaseEntries::refreshIcons));
    }

    public static void refreshIcons() {
        if (headless) return;

        TextureRegion region = Core.atlas.find(NewHorizon.name("danger"));
        if (region == null || !region.found()) return;

        NHContent.danger = region;
        if (raidThreat != null) {
            raidThreat.fullIcon = raidThreat.uiIcon = region;
        }
    }
}
