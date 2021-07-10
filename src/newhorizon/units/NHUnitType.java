package newhorizon.units;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.func.NHSetting;

public class NHUnitType extends UnitType{
	public static final Seq<TextureRegion> test = new Seq<>();
	
	public NHUnitType(String name){
		super(name);
	}
	
	public NHUnitType(String name, Weapon... weapons){
		this(name);
		this.weapons.addAll(weapons);
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		if(!NHSetting.getBool("@active.advance-load*"))super.createIcons(packer);
		else if(!Vars.headless && region != null && region.found() && region instanceof TextureAtlas.AtlasRegion){
			TextureAtlas.AtlasRegion t = (TextureAtlas.AtlasRegion)region;
			
			test.add(region);
			PixmapRegion r = Core.atlas.getPixmap(Core.atlas.find(name));

			makeOutline(packer, t);

			Pixmap base = new Pixmap(region.width, region.height);
			base.draw(r.crop(), true);
			
			TextureAtlas.AtlasRegion tC = Core.atlas.find(name + "-cell");
			test.add(tC);
			base.draw(fillColor(Core.atlas.getPixmap(tC), Team.sharded.color), true);

			for(Weapon w : weapons){
				makeOutline(packer, w.region);
				if(w.top)continue;
				drawWeaponPixmap(base, w, false, outlineColor, outlineRadius);
			}

			base = Pixmaps.outline(new PixmapRegion(base), outlineColor, outlineRadius);

			for(Weapon w : weapons){
				makeOutline(packer, w.region);
				if(!w.top)continue;
				drawWeaponPixmap(base, w, true, outlineColor, outlineRadius);
			}

			if(Core.settings.getBool("linear")){
				Pixmaps.bleed(base);
			}

			packer.add(MultiPacker.PageType.main, name + "-full", base);
		}
	}
	
	@Override
	public void load(){
		super.load();
		test.add(outlineRegion);
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
	
	private void makeOutline(MultiPacker packer, TextureRegion region){
		if(region instanceof TextureAtlas.AtlasRegion && region.found()){
			String name = ((TextureAtlas.AtlasRegion)region).name;
			if(!packer.has(name + "-outline")){
				PixmapRegion base = Core.atlas.getPixmap(region);
				Pixmap result = Pixmaps.outline(base, outlineColor, outlineRadius);
				if(Core.settings.getBool("linear")){
					Pixmaps.bleed(result);
				}
				packer.add(MultiPacker.PageType.main, name + "-outline", result);
			}
		}
	}
	
	public static int getCenter(Pixmap base, Pixmap above, boolean WorH, boolean outline){
		return (WorH ? (base.getWidth() - above.getWidth()) / 2 : (base.getHeight() - above.getHeight()) / 2);
	}
}
