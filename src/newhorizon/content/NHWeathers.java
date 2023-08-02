package newhorizon.content;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.util.Time;
import mindustry.gen.Sounds;
import mindustry.gen.WeatherState;
import mindustry.graphics.Pal;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;
import newhorizon.expand.weather.MatterStorm;

public class NHWeathers{
	public static Weather
			quantumField, quantumStorm, solarStorm;
	public static void load(){
		quantumStorm = new MatterStorm("quantum-storm"){{
			status = NHStatusEffects.ultFireBurn;
			statusDuration = 15f;
			rotateBullets = true;
			
			buildingEmp = 0.4f;
			
			textureColor = primaryColor = NHColor.darkEnrColor;
			secondaryColor = NHColor.lightSkyBack;
			bulletSpawnChance *= 1.5f;
		}};
		
		solarStorm = new MatterStorm("solar-storm"){{
			status = NHStatusEffects.emp2;
			statusDuration = 60f;
			
			buildingEmp = 0.125f;
			force = 4;
			noise = Sounds.fire;
			
			primaryColor = Pal.accent;
			textureColor = secondaryColor = Pal.ammo;
			
			attrs.set(Attribute.heat, 2f);
		}
			
//			@Override
//			public void init(){
//				super.init();
//
//
//
//				bulletType.fragBullet = Bullets.fireball;
//				bulletType.fragBullets = 4;
//				bulletType.damage = 15f;
//				bulletType.splashDamage = 40;
//				bulletType.splashDamageRadius = 40;
//				bulletType.lightning = 0;
//				bulletType.hitEffect = NHFx.lightningHitLarge;
//			}
		};
		
		quantumField = new ParticleWeather("quantum-weather"){{
			duration = 0.5f * Time.toMinutes;
			noiseLayers = 4;
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
			
			@Override
			public void drawUnder(WeatherState state){
				super.drawUnder(state);
			}
			
			@Override
			public void drawOver(WeatherState state){
				Draw.blend(Blending.additive);
			
				super.drawOver(state);
				
				Draw.blend();
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
