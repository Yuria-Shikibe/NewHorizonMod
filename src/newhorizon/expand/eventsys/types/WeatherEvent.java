package newhorizon.expand.eventsys.types;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.type.Weather;
import newhorizon.util.ui.NHUIFunc;

public class WeatherEvent extends ReloadEventType{
	public Weather weather;
	public WeatherEvent(String name, Weather weather, Color color){
		super(name);
		this.weather = weather;
		
		hasCoord = false;
		
		colorFunc = e -> color;
		info = e -> Core.bundle.format("nh.cutscene.event.incoming", weather.localizedName);
		act = e -> {
			if(!Vars.net.client())weather.create();
		};
	}
	
	@Override
	public TextureRegion icon(){
		return weather.fullIcon;
	}
	
	@Override
	public void buildSpeInfo(Table table){
		NHUIFunc.show(table, weather);
	}
}
