package newhorizon.util.func;

import arc.Core;
import arc.files.Fi;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Log;
import mindustry.Vars;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;

import java.io.IOException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class NHPixmap{
	private static final boolean DEBUGGING_SPRITE = true;
	
	public static final String PCD_SUFFIX = "-processed";
	
	public static boolean isDebugging(){return DEBUGGING_SPRITE;}
	
	public static ObjectMap<String, Pixmap> processed = new ObjectMap<>();
	
	public static Pixmap addProcessed(String name, Pixmap pixmap){
		processed.put(name, pixmap);
		
		return pixmap;
	}
	
	public static Pixmap outLineAndAdd(String name, PixmapRegion pixmap, Color color, int radius){
		return addProcessed(name, Pixmaps.outline(pixmap, color, radius));
	}
	
	public static Pixmap outLineAndAdd(String name, TextureRegion pixmap, Color color, int radius){
		return outLineAndAdd(name, Core.atlas.getPixmap(pixmap), color, radius);
	}
	
	public static void packOutlineAndAdd(MultiPacker packer, String name, TextureRegion pixmap, Color color, int radius){
		packer.add(MultiPacker.PageType.main, name, outLineAndAdd(name, pixmap, color, radius));
	}
	
	public static void packAndAdd(MultiPacker packer, String name, Pixmap pixmap){
		packer.add(MultiPacker.PageType.main, name, addProcessed(name, pixmap));
	}
	
	public static final Color OUTLINE_COLOR = Color.valueOf("565666");
	
	public static void outlineLegs(MultiPacker packer, UnitType type){
		if(NHSetting.getBool("@active.advance-load*") && !Vars.headless){
			Color color = Color.valueOf("565666");
			if(type.legRegion.found())packer.add(MultiPacker.PageType.main, type.name + "-leg", Pixmaps.outline(Core.atlas.getPixmap(type.legRegion), color, type.outlineRadius));
			if(type.jointRegion.found())packer.add(MultiPacker.PageType.main, type.name + "-joint", Pixmaps.outline(Core.atlas.getPixmap(type.jointRegion), color, type.outlineRadius));
			if(type.baseJointRegion.found())packer.add(MultiPacker.PageType.main, type.name + "-joint-base", Pixmaps.outline(Core.atlas.getPixmap(type.baseJointRegion), color, type.outlineRadius));
			if(type.footRegion.found())packer.add(MultiPacker.PageType.main, type.name + "-foot", Pixmaps.outline(Core.atlas.getPixmap(type.footRegion), color, type.outlineRadius));
			if(type.legBaseRegion.found())packer.add(MultiPacker.PageType.main, type.name + "-leg-base", Pixmaps.outline(Core.atlas.getPixmap(type.legBaseRegion), color, type.outlineRadius));
			if(type.baseRegion.found())packer.add(MultiPacker.PageType.main, type.name + "-base", Pixmaps.outline(Core.atlas.getPixmap(type.baseRegion), color, type.outlineRadius));
		}
	}
	
	public static void createIcons(MultiPacker packer, UnitType type){
		if(!Vars.headless && DEBUGGING_SPRITE){
			TextureAtlas.AtlasRegion t = (TextureAtlas.AtlasRegion)type.region;
			
			PixmapRegion r = Core.atlas.getPixmap(Core.atlas.find(type.name));
			
			Pixmap base = new Pixmap(type.region.width, type.region.height);
			base.draw(r.crop(), true);
			
			base.draw(replaceColor(Core.atlas.getPixmap(type.cellRegion), ObjectMap.of((Boolf<Color>) c -> c.equals(Color.white), Color.valueOf("ffa664"), (Boolf<Color>) c -> c.equals(Color.valueOf("dcc6c6")), Color.valueOf("dc804e"))), 0, 0, true);

			for(Weapon w : type.weapons){
				if(w.top)continue;
				drawWeaponPixmap(base, w, false, OUTLINE_COLOR, type.outlineRadius);
			}

			base = Pixmaps.outline(new PixmapRegion(base), OUTLINE_COLOR, type.outlineRadius);

			for(Weapon w : type.weapons){
				if(!w.top)continue;
				drawWeaponPixmap(base, w, true, OUTLINE_COLOR, type.outlineRadius);
			}

			if(Core.settings.getBool("linear")){
				Pixmaps.bleed(base);
			}
			
			//used to debug
			
			packAndAdd(packer, type.name + "-full", base);
		}
	}
	
	public static Pixmap replaceColor(PixmapRegion pixmap, ObjectMap<Boolf<Color>, Color> map){
		Pixmap base = new Pixmap(pixmap.width, pixmap.height);
		Color color = new Color();
		
		for(int y = 0; y < pixmap.height; ++y){
			for(int x = 0; x < pixmap.width; ++x){
				pixmap.get(x, y, color);
				if(Mathf.zero(color.a))continue;
				for(Boolf<Color> filter : map.keys()){
					if(filter.get(color)){
						base.set(x, y, map.get(filter));
						break;
					}
				}
			}
		}
		
		return base;
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
	
	@Deprecated
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
	
	public static Fi processedDir(){
		Fi dic = new Fi("E:/Java_Projects/MDT_Mod_Project/NewHorizonMod/assets/sprites/pre-processed");
		if(!dic.exists())dic.mkdirs();
		return dic;
	}
	
	public static Fi processedPng(String fileName, String suffix){
		Fi fi = new Fi("E:/Java_Projects/MDT_Mod_Project/NewHorizonMod/assets/sprites/pre-processed/" + fileName.replaceAll(NewHorizon.MOD_NAME + "-", "") + suffix + ".png");
		if(!fi.exists()){
			try{
				fi.file().createNewFile();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return fi;
	}
	
	public static void saveUnitPixmap(Pixmap pixmap, UnitType type){
		Fi dic = new Fi("E:/Java_Projects/MDT_Mod_Project/NewHorizonMod/assets/sprites/pre-processed");
		if(!dic.exists())dic.mkdirs();
		if(dic.exists()){
			Fi n = processedPng(type.name, "-full");
			if(!n.exists())try{n.file().createNewFile();}catch(IOException e){Log.err(e);}
			PixmapIO.writePng(n, pixmap);
			Log.info("Created Icon: " + type.localizedName);
		}
	}
	
	public static void saveAddProcessed(){
		processedDir();
		
		for(ObjectMap.Entry<String, Pixmap> entry : processed.entries()){
			Fi n = processedPng(entry.key, "");
			PixmapIO.writePng(n, entry.value);
			Log.info("Created Icon: " + entry.key);
		}
		
	}
}
