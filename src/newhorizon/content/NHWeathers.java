package newhorizon.content;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.ctype.ContentList;
import mindustry.gen.WeatherState;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;
import newhorizon.expand.weather.EventGenerator;
import newhorizon.expand.weather.MatterStorm;
import newhorizon.util.feature.cutscene.events.FleetEvent;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;
import newhorizon.util.feature.cutscene.events.util.PreMadeRaids;
import newhorizon.util.feature.cutscene.events.util.TriggerGenerator;
import newhorizon.util.func.OV_Pair;

public class NHWeathers implements ContentList{
	public static Weather
			raid1, raid2, heavyRaid1,
			
			intervention1,
	
	
			quantumField, quantumStorm;
	@Override
	public void load(){
		raid1 = new EventGenerator("standard-raid-1", () -> Vars.state.rules.defaultTeam, new AutoEventTrigger().setEvent(PreMadeRaids.standardRaid1));
		
		raid2 = new EventGenerator("standard-raid-2", () -> Vars.state.rules.defaultTeam, TriggerGenerator.Unit_4Destruction__Block_1SeniorJumpGate().setEvent(PreMadeRaids.deadlyRaid1));
		
		heavyRaid1 = new EventGenerator("heavy-raid-1", () -> Vars.state.rules.defaultTeam, TriggerGenerator.Item_50SurgeAlloy().setEvent(PreMadeRaids.deadlyRaid2)){{
			beginWave = 15;
		}};
		
		intervention1 = new EventGenerator("intervention-1", () -> Vars.state.rules.defaultTeam, new AutoEventTrigger().modify(t -> {
			t.items = OV_Pair.with(Items.surgeAlloy, 50, NHItems.zeta, 150);
			t.buildings = OV_Pair.with(NHBlocks.jumpGatePrimary, 1);
			t.eventType = new FleetEvent("random-intervention"){{
				cannotBeRemove = removeAfterTriggered = removeAfterVictory = true;
				
				reloadTime = 3600f;
				
				unitTypeMap = ObjectMap.of(NHUnitTypes.thynomo, 2, NHUnitTypes.sharp, 5, NHUnitTypes.branch, 3);
			}};
		}));
		
		quantumStorm = new MatterStorm("quantum-storm"){{
			duration = 4 * Time.toMinutes;
			
			statusAir = statusGround = true;
			status = NHStatusEffects.quantization;
			statusDuration = 60f;
			attrs.set(Attribute.light, -0.3f);
			opacityMultiplier = 0.47f;
		}};
		
		quantumField = new ParticleWeather("quantum-weather"){{
			duration = 4 * Time.toMinutes;
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
