package newhorizon.util.ui;

import arc.func.Intp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.modules.ItemModule;

public class ItemImageDynamic extends Stack{
	public ItemImageDynamic(TextureRegion region, Intp amountp, Prov<Color> colorProv){
		
		add(new Table(o -> {
			o.left();
			o.add(new Image(region)).size(32f).scaling(Scaling.fit);
		}));
		
		add(new Table(t -> {
			t.left().bottom();
			t.label(() -> {
				int amount = amountp.get();
				return amount >= 1000 ? UI.formatAmount(amount) : amount + "";
			}).style(Styles.outlineLabel).color(colorProv.get());
			t.pack();
		}));
	}
	
	public ItemImageDynamic(Item item, Intp amountp){
		this(item.uiIcon, amountp, () -> Color.lightGray);
	}
	
	public ItemImageDynamic(Item item, Intp amountp, ItemModule module){
		this(item.uiIcon, amountp, () -> module.has(item, amountp.get()) ? Color.white : Pal.redderDust);
	}
}
