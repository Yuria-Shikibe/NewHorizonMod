package newhorizon.content;

import arc.util.Time;
import mindustry.ctype.ContentList;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;

public class NHWeathers implements ContentList{
	public static Weather quantumField;
	@Override
	public void load(){
		quantumField = new ParticleWeather("quantum-weather"){{
			duration = 4 * Time.toMinutes;
			noiseLayers = 4;
			noiseLayerSclM = 0.8f;
			noiseLayerAlphaM = 0.7f;
			noiseLayerSpeedM = 2f;
			noiseLayerSclM = 0.6f;
			baseSpeed = 0.1f;
			noiseColor = NHColor.darkEnrFront;
			color = NHColor.darkEnrColor;
			noiseScale = 1100f;
			noisePath = "fog";
			drawParticles = false;
			drawNoise = true;
			useWindVector = true;
			xspeed = 1f;
			yspeed = 0.01f;
			
			statusAir = statusGround = true;
			status = NHStatusEffects.quantization;
			statusDuration = 60f;
			attrs.set(Attribute.light, -0.3f);
			opacityMultiplier = 0.47f;
		}
		
			
//			@Override
//			public void load(){
//				region = Core.atlas.find(particleRegion);
//
//				//load noise texture
//				if(drawNoise && Core.assets != null){
//					NHLoader.loadSprite(noisePath);
//				}
//			}
		};
	}
}
