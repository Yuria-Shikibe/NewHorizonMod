package newhorizon.content;

import mindustry.ctype.ContentList;
import mindustry.type.Weather;
import newhorizon.weather.LightningStorm;

public class NHWeathers implements ContentList{
	public static Weather
		sunStorm;
	@Override
	public void load(){
		sunStorm = new LightningStorm("sun-storm");
	}
}
