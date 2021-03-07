package newhorizon.block.special;


import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;

import static mindustry.Vars.tilesize;
import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;

public class Debuger extends Block{
	public Debuger(){
		super("debuger");
		configurable = update = true;
		requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.with());
	}
	
	public class DebugerBuild extends Building{
		float
			x1, x2, x3, x4,
			y1, y2, y3, y4;
		
		@Override
		public void buildConfiguration(Table table){
			table.table(Tex.pane, t -> {
				t.add("X1: ").fill();
				t.slider(0, 40, 0.5f, x1, f -> x1 = f).growX().height(LEN).row();
				t.add("X2: ").fill();
				t.slider(0, 40, 0.5f, x2, f -> x2 = f).growX().height(LEN).row();
				t.add("X3: ").fill();
				t.slider(0, 40, 0.5f, x3, f -> x3 = f).growX().height(LEN).row();
				t.add("X4: ").fill();
				t.slider(0, 40, 0.5f, x4, f -> x4 = f).growX().height(LEN).row();
				t.image().growX().height(OFFSET / 4).pad(OFFSET / 2).color(Color.lightGray).row();
				t.add("Y1: ").fill();
				t.slider(0, 40, 0.5f, y1, f -> y1 = f).growX().height(LEN).row();
				t.add("Y2: ").fill();
				t.slider(0, 40, 0.5f, y2, f -> y2 = f).growX().height(LEN).row();
				t.add("Y3: ").fill();
				t.slider(0, 40, 0.5f, y3, f -> y3 = f).growX().height(LEN).row();
				t.add("Y4: ").fill();
				t.slider(0, 40, 0.5f, y4, f -> y4 = f).growX().height(LEN).row();
				
			}).grow();
		}
		
		@Override
		public void drawConfigure(){
			float x = this.x + tilesize * 4;
			Draw.color(Pal.accent);
			Fill.quad(x + x1, y + y1, x + x2, y + y2, x + x3, y + y3, x + x4, y + y4);
			Draw.color(Pal.redderDust);
			Lines.line(x + x1, y + y1, x + x2, y + y2, false);
			Lines.line(x + x2, y + y2, x + x3, y + y3, false);
			Lines.line(x + x3, y + y3, x + x4, y + y4, false);
			Lines.line(x + x4, y + y4, x + x1, y + y1, false);
			
			Fill.square(x + x1, y + y1, 1, 45);
			Fill.square(x + x2, y + y2, 1, 45);
			Fill.square(x + x3, y + y3, 1, 45);
			Fill.square(x + x4, y + y4, 1, 45);
			Draw.reset();
		}
	
	}
}
