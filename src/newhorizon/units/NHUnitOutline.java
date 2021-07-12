package newhorizon.units;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.func.NHSetting;

public class NHUnitOutline{
	public static void createIcons(MultiPacker packer, UnitType type){
		if(NHSetting.getBool("@active.advance-load*") && !Vars.headless && type.region != null && type.region.found() && type.region instanceof TextureAtlas.AtlasRegion){
			TextureAtlas.AtlasRegion t = (TextureAtlas.AtlasRegion)type.region;
			
			PixmapRegion r = Core.atlas.getPixmap(Core.atlas.find(type.name));
			
			Pixmap base = new Pixmap(type.region.width, type.region.height);
			base.draw(r.crop(), true);
			
			TextureAtlas.AtlasRegion tC = Core.atlas.find(type.name + "-cell");
			//base.draw(fillColor(Core.atlas.getPixmap(tC), Team.sharded.color), 0, 0, true);
			base.draw(fillColor(Core.atlas.getPixmap(tC), Team.sharded.color), -1, 0, true);

			for(Weapon w : type.weapons){
				if(w.top)continue;
				drawWeaponPixmap(base, w, false, type.outlineColor, type.outlineRadius);
			}

			base = Pixmaps.outline(new PixmapRegion(base), type.outlineColor, type.outlineRadius);

			for(Weapon w : type.weapons){
				if(!w.top)continue;
				drawWeaponPixmap(base, w, true, type.outlineColor, type.outlineRadius);
			}

			if(Core.settings.getBool("linear")){
				Pixmaps.bleed(base);
			}

			packer.add(MultiPacker.PageType.main, type.name + "-full", base);
		}
	}
	
	public static Pixmap fillColor(PixmapRegion pixmap, Color replaceColor){
		Pixmap base = new Pixmap(pixmap.width, pixmap.height);
		Color color = new Color();
		if(color.a < 1.0F){
			for(int y = 0; y < pixmap.height; ++y){
				for(int x = 0; x < pixmap.width; ++x){
					pixmap.get(x, y, color);
					base.set(pixmap.width - x, y, color.mul(replaceColor));
				}
			}
		}
		return base;
	}
	
	public static void drawWeaponPixmap(Pixmap base, Weapon w, boolean outline, Color outlineColor, int radius){
		if(w.region != null && w.region.found() && w.region instanceof TextureAtlas.AtlasRegion){
			TextureAtlas.AtlasRegion t = (TextureAtlas.AtlasRegion)w.region;
			if(!t.found())return;
			
			Pixmap wRegion = outline ? Pixmaps.outline(Core.atlas.getPixmap(t), outlineColor, radius) : Core.atlas.getPixmap(t).crop();
			
			if(w.mirror){
				Pixmap wRegion2 = wRegion.flipX();
				base.draw(wRegion, getCenter(base, wRegion, true, outline) + (int)(w.x * 4), getCenter(base, wRegion, false, outline) - (int)(w.y * 4), true);
				base.draw(wRegion2, getCenter(base, wRegion2, true, outline) - (int)(w.x * 4), getCenter(base, wRegion2, false, outline) - (int)(w.y * 4), true);
			}else{
				base.draw(wRegion, getCenter(base, wRegion, true, outline) + (int)(w.x * 4), getCenter(base, wRegion, false, outline) - (int)(w.y * 4), true);
			}
		}
	}
	
	public static int getCenter(Pixmap base, Pixmap above, boolean WorH, boolean outline){
		return (WorH ? (base.getWidth() - above.getWidth()) / 2 : (base.getHeight() - above.getHeight()) / 2);
	}
}
