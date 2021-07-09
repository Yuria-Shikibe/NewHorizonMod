package newhorizon.content;

import arc.graphics.Color;
import mindustry.graphics.Pal;

public class NHColor{
	public static Color
		lightSkyBack = Color.valueOf("#8DB0FF"),
		lightSkyFront = lightSkyBack.cpy().lerp(Color.white, 0.35f),
		darkEnrColor = Pal.sapBullet,
		none = new Color(0, 0, 0, 0),
		thurmixRed = Color.valueOf("#FF9492"),
		thurmixRedLight = Color.valueOf("#FFCED0"),
		darkEnr = darkEnrColor.cpy().lerp(Color.black, 0.85f);
}










