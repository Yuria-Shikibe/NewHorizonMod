package newhorizon.expand.weather;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.entities.Lightning;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.gen.WeatherState;
import mindustry.graphics.Pal;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;
import newhorizon.content.NHColor;


public class LightningStorm extends ParticleWeather{
	public float lightningChance = 0.05f;
	public int lightningScl = 20;
	public float lightningDamage = 12f;
	public int lightningLength = 6;
	public int lightningLengthRand = 14;
	public float angleRand = 30f;
	public Color lightningColor = NHColor.lightSkyBack.cpy().lerp(arc.graphics.Color.white, 0.025f);
	
	public LightningStorm(String name){
		super(name);
		status = StatusEffects.burning;
		color = Pal.power;
		noiseColor = Pal.bulletYellow;
		drawNoise = true;
		useWindVector = true;
		sizeMax = 140.0F;
		sizeMin = 70.0F;
		minAlpha = 0.0F;
		maxAlpha = 0.2F;
		density = 1500.0F;
		baseSpeed = 5.4F;
		attrs.set(Attribute.light, -0.1F);
		attrs.set(Attribute.water, -0.1F);
		opacityMultiplier = 0.35F;
		force = 0.6F;
		sound = Sounds.wind;
		soundVol = 0.8F;
		duration = 6000.0F;
	}
	
	@Override
	public void update(WeatherState state){
		super.update(state);
		
		float speed = force * state.intensity;
		if(speed > 0.001f){
			for(int i = 0; i < Math.sqrt(Vars.world.height() * Vars.world.width()) / lightningScl; i++){
				Lightning.create(Team.derelict, lightningColor, lightningDamage, Mathf.random(Vars.world.unitWidth()), Mathf.random(Vars.world.unitHeight()), state.windVector.angle() + Mathf.range(angleRand), lightningLength + Mathf.random(lightningLengthRand));
			}
		}
	}
	
	@Override
	public void drawOver(WeatherState state){
		super.drawOver(state);
	}
}
