package newhorizon.expand.logic.cutscene.types;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import newhorizon.NewHorizon;

public enum HudIcon {
    defaultRaid("default-raid");

    public final TextureRegion icon;

    HudIcon(String name) {
        this.icon = Core.atlas.find(NewHorizon.name("hud-icon-" + name));
    }
}
