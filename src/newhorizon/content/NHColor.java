package newhorizon.content;

import arc.graphics.Color;
import mindustry.graphics.Pal;

public class NHColor{
	public static Color
		lightSkyBack = Color.valueOf("#8DB0FF"),
		lightSkyFront = lightSkyBack.cpy().lerp(Color.white, 0.35f),
		darkEnrColor = Pal.sapBullet,
		thurmixRed = Color.valueOf("#FF9492"),
		thurmixRedLight = Color.valueOf("#FFCED0"),
		thurmixRedDark = thurmixRed.cpy().lerp(Color.black, 0.9f),
		darkEnr = darkEnrColor.cpy().lerp(Color.black, 0.85f),
		darkEnrFront = darkEnrColor.cpy().lerp(Color.white, 0.2f),
		thermoPst = Color.valueOf("CFFF87").lerp(Color.white, 0.15f);
}










