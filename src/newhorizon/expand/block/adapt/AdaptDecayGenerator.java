package newhorizon.expand.block.adapt;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.power.DecayGenerator;

public class AdaptDecayGenerator extends DecayGenerator{
	public AdaptDecayGenerator(String name){
		super(name);
	}
	
	public class AdaptDecayGeneratorBuild extends ItemLiquidGeneratorBuild{
		@Override
		public void draw(){
			super.draw();
			
			if(hasItems){
				Draw.blend(Blending.additive);
				Draw.color(heatColor.cpy().lerp(Color.white, Mathf.absin(Time.time, 8f, 0.3f) * heat));
				Draw.alpha(heat * (0.25f + Mathf.absin(Time.time, 8f, 0.35f)));
				Draw.rect(topRegion, x, y);
				Draw.reset();
				Draw.blend();
			}
			
			if(hasLiquids){
				Drawf.liquid(liquidRegion, x, y, liquids.total() / liquidCapacity, liquids.current().color);
			}
		}
	}
}
