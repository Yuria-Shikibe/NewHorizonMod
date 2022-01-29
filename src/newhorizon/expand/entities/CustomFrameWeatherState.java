package newhorizon.expand.entities;

import arc.graphics.Texture;
import arc.graphics.gl.FrameBuffer;
import mindustry.gen.WeatherState;
import mindustry.type.Weather;

import static mindustry.Vars.world;

public class CustomFrameWeatherState extends WeatherState{
	public FrameBuffer buffer = new FrameBuffer();
	
	@Override
	public void init(Weather weather){
		super.init(weather);
		
		buffer.getTexture().setFilter(Texture.TextureFilter.linear);
		buffer.resize(world.width(), world.height());
	}
}
