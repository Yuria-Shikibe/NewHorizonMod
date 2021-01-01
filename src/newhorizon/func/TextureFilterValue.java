package newhorizon.func;

import arc.graphics.g2d.TextureRegion;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import mindustry.gen.Tex;
import mindustry.ui.Links;
import mindustry.world.meta.StatValue;

import static newhorizon.func.Functions.LEN;
import static newhorizon.func.Functions.OFFSET;

public class TextureFilterValue implements StatValue {
    public final TextureRegion region;
    public String description = "N/A";

    public TextureFilterValue(TextureRegion region, String description) {
        this.region = region;
        this.description = description;
    }

    @Override
    public void display(Table table) {
        table.table(t2 ->{
            t2.left();
            t2.table(t -> t.image(region).size(LEN * 1.5f).left()).size(LEN * 1.5f + OFFSET / 2f).pad(OFFSET / 2f).left();
            t2.table(t -> t.add(description).left()).pad(OFFSET / 2f).left();
        }).left().grow();
    }
}
