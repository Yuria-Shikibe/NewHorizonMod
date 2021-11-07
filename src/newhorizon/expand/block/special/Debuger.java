package newhorizon.expand.block.special;


import arc.graphics.g2d.Draw;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHColor;
import newhorizon.util.feature.Cube;

import static newhorizon.util.ui.TableFunc.LEN;

public class Debuger extends Block{
	public Debuger(){
		super("debuger");
		configurable = update = true;
		requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.with());
	}
	
	public class DebugerBuild extends Building{
		float rotationB, rotationC;
		
		@Override
		public void buildConfiguration(Table table){
			table.table(Tex.pane, t -> {
				t.add("rotationB: ").fill();
				t.slider(0, 360, 0.5f, rotationB, f -> rotationB = f).growX().height(LEN).row();
				t.add("rotationC: ").fill();
				t.slider(0, 360, 0.5f, rotationC, f -> rotationC = f).growX().height(LEN).row();
				
			}).grow();
		}
		
		
		@Override
		public void draw(){
			super.draw();
			//new Cube().draw2(x + 80, y + 80, rotationB, rotationC);
			Draw.z(Layer.bullet);
			new Cube(NHColor.lightSkyBack, 4f, 1f).draw(x - 80, y + 80, rotationB);
			Draw.reset();
		}
	}
}
