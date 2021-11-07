package newhorizon.expand.block.adapt;

import arc.Events;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.world.blocks.power.ImpactReactor;

public class AdaptImpactReactor extends ImpactReactor{
	public float textureScl = 0.95f;
	
	public AdaptImpactReactor(String name){
		super(name);
	}
	
	public class AdaptImpactReactorBuild extends ImpactReactorBuild{
		@Override
		public void updateTile(){
			if(consValid() && power.status >= 0.99f){
				boolean prevOut = getPowerProduction() <= consumes.getPower().requestedPower(this);
				
				warmup = Mathf.lerpDelta(warmup, 1f, warmupSpeed * timeScale);
				if(Mathf.equal(warmup, 1f, 0.001f) || Vars.net.active()){
					warmup = 1f;
				}
				
				if(!prevOut && (getPowerProduction() > consumes.getPower().requestedPower(this))){
					Events.fire(EventType.Trigger.impactPower);
				}
				
				if(timer(timerUse, itemDuration / timeScale)){
					consume();
				}
			}else{
				warmup = Mathf.lerpDelta(warmup, 0f, 0.01f);
			}
			
			productionEfficiency = Mathf.pow(warmup, 5f);
		}
		
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
