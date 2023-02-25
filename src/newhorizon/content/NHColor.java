package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.Colors;
import mindustry.content.Items;
import mindustry.graphics.Pal;

public class NHColor{
	public static Color
		ancient = Items.surgeAlloy.color.cpy().lerp(Pal.accent, 0.055f),
		ancientLight = ancient.cpy().lerp(Color.white, 0.225f),
		ancientHeat = NHColor.ancient.cpy().lerp(Pal.redderDust, 0.5f).mul(1.1f),
		ally = new Color(0, 0, 1, 0.15f), hostile = new Color(1, 0, 0, 0.15f),
		deeperBlue = Color.valueOf("#778ff2"),
		lightSky = Color.valueOf("#8DB0FF"),
		lightSkyBack = lightSky.cpy().lerp(Color.white, 0.2f),
		lightSkyMiddle = lightSky.cpy().lerp(Color.white, 0.6f),
		lightSkyFront = lightSky.cpy().lerp(Color.white, 0.8f),
		darkEnrColor = Pal.sapBullet,
		thurmixRed = Color.valueOf("#FF9492"),
		thurmixRedLight = Color.valueOf("#FFCED0"),
		thurmixRedDark = thurmixRed.cpy().lerp(Color.black, 0.9f),
		darkEnr = darkEnrColor.cpy().lerp(Color.black, 0.85f),
		darkEnrFront = darkEnrColor.cpy().lerp(Color.white, 0.2f),
		trail = Color.lightGray.cpy().lerp(Color.gray, 0.65f),
		thermoPst = Color.valueOf("CFFF87").lerp(Color.white, 0.15f);
	
	static{
		Colors.put("heal", Pal.heal);
		Colors.put("ancient", ancient);
	}
}










