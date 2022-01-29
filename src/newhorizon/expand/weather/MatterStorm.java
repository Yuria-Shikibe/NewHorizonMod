package newhorizon.expand.weather;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import mindustry.Vars;
import mindustry.gen.WeatherState;
import mindustry.type.Weather;
import newhorizon.content.NHColor;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;

public class MatterStorm extends Weather{
	public MatterStorm(String name){
		super(name);
	}
	
	@Override
	public void drawOver(WeatherState state){
		Draw.draw(NHContent.MATTER_STORM_LAYER, () -> {
			Vars.renderer.effectBuffer.begin(NHColor.darkEnrColor);
			
			Vars.renderer.effectBuffer.end();
			
			NHShaders.matterStorm.direction.set(state.windVector);
			
			Draw.blend(Blending.additive);
			Vars.renderer.effectBuffer.blit(NHShaders.matterStorm);
			Draw.blend();
		});
	}
}
