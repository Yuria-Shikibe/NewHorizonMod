package newhorizon.util.ui.display;

import arc.func.Intp;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;

public class IconNumDisplay extends Table{
	public final TextureRegion icon;
	public final int amount;
	
	public IconNumDisplay(TextureRegion icon){
		this(icon, 0);
	}
	
	public IconNumDisplay(TextureRegion icon, int amount, String showName){
		add(new IconImage(icon, amount)).left();
		if(!showName.isEmpty())add(showName).padLeft(4 + amount > 99 ? 4 : 0);
		
		this.icon = icon;
		this.amount = amount;
	}
	
	public IconNumDisplay(TextureRegion icon, int amount){
		this(icon, amount, "");
	}
	
	public static class IconImage extends Stack{
		public IconImage(TextureRegion region, int amount){
			
			add(new Table(o -> {
				o.left();
				o.add(new Image(region)).scaling(Scaling.fit).size(32f);
			}));
			
			add(new Table(t -> {
				t.left().bottom();
				t.add(amount > 1000 ? UI.formatAmount(amount) : amount + "");
				t.pack();
			}));
		}
		
		public IconImage(TextureRegion region, Intp amount){
			
			add(new Table(o -> {
				o.left();
				o.add(new Image(region)).scaling(Scaling.fit).size(32f);
			}));
			
			add(new Table(t -> {
				t.left().bottom();
				t.label(() -> amount.get() > 1000 ? UI.formatAmount(amount.get()) : amount.get() + "");
				t.pack();
			}));
		}
	}
	
	
}
