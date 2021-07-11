package newhorizon.block.special;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import newhorizon.block.special.JumpGate.JumpGateBuild;

import static mindustry.Vars.tilesize;

public class Configurer extends Block{
	public Configurer(String name){
		super(name);
		solid = true;
		update = true;
		configurable = true;
		logicConfigurable = true;
		rotate = true;
		noUpdateDisabled = true;
		saveConfig = true;
		
		config(Integer.class, (ConfigurerBuild tile, Integer i) -> {
			Log.info("Configured");
			if(tile.target != null)tile.target.configure(i);
		});
		configClear( (ConfigurerBuild tile) -> {
			if(tile.target != null)tile.target.configure(-1);
		});
	}
	
	public class ConfigurerBuild extends Building{
		
		@Override
		public Integer config(){
			return 1;
		}
		
		Building target = null;
		
		public void target(){
			Building b = front();
			if(b instanceof JumpGateBuild){
				target = b;
			}else target = null;
		}
		
		
		@Override
		public void placed(){
			super.placed();
			target();
		}
		
		@Override
		public void add(){
			super.add();
			target();
		}
		
		@Override
		public void updateProximity(){
			super.updateProximity();
			target();
		}

		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			if(target == null)return;
			Draw.color(Pal.accent);
			Lines.stroke(1.0F);
			Lines.square(target.x, target.y, target.block.size * tilesize / 2.0F + 1.0F);
			Draw.reset();
		}
		
		@Override
		public void drawSelect(){
			drawConfigure();
		}
		
		@Override
		public void draw(){
			Draw.rect(block.region, x, y, 0);
			drawTeamTop();
			if(target == null)return;
			Draw.color(Pal.accent);
			Draw.z(Layer.effect);
			Lines.stroke(1.0F);
			Lines.square(x, y, block.size * tilesize / 2.0F / Mathf.sqrt2 + 1.0F, 45);
			
			Tmp.v1.set(target).sub(this).scl(Mathf.absin(12f, 1f));
			Lines.square(x + Tmp.v1.x, y + Tmp.v1.y, block.size * tilesize / 2.0F / Mathf.sqrt2 + 1.0F, 45);
		}
		
		@Override
		public byte version(){
			return 2;
		}
	}
}
