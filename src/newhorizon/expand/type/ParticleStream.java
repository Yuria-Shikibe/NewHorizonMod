package newhorizon.expand.type;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;

import static mindustry.ctype.ContentType.loadout_UNUSED;

public class ParticleStream extends UnlockableContent {
    public static final Seq<ParticleStream> all = new Seq<>();

    public final Color color;

    public ParticleStream(String name, Color color) {
        super(name);
        this.color = color;
        localizedName = Core.bundle.get("particle-stream." + this.name + ".name");
        description = Core.bundle.getOrNull("particle-stream." + this.name + ".description");
        details = Core.bundle.getOrNull("particle-stream." + this.name + ".details");
        all.add(this);
    }

    @Override
    public ContentType getContentType() {
        return loadout_UNUSED;
    }

    @Override
    public void load() {
        super.load();
        TextureRegion icon = Core.atlas.find(name);
        fullIcon = uiIcon = icon;
    }
}
