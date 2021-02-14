package newhorizon.block.adapt;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.world.blocks.power.ImpactReactor;

public class AdaptImpactReactor extends ImpactReactor{
	public float textureScl = 0.95f;
	
	public AdaptImpactReactor(String name){
		super(name);
	}
	
	public class AdaptImpactReactorBuild extends ImpactReactorBuild{
		@Override
		public void draw(){
			Draw.rect(bottomRegion, x, y);
			
			for(int i = 0; i < plasmaRegions.length; ++i) {
				float r = textureScl * (size * 8) - 3.0F + Mathf.absin(Time.time, 2.0F + i * 1.0F, 5.0F - i * 0.5F);
				Draw.color(plasma1, plasma2, (float)i / plasmaRegions.length);
				Draw.alpha((0.3F + Mathf.absin(Time.time, 2.0F + i * 2.0F, 0.3F + i * 0.05F)) * warmup);
				Draw.blend(Blending.additive);
				Draw.rect(plasmaRegions[i], x, y, r, r, Time.time * (12.0F + i * 6.0F) * warmup);
				Draw.blend();
			}
			
			Draw.color();
			Draw.rect(region, x, y);
			Draw.color();
		}
	}
}
