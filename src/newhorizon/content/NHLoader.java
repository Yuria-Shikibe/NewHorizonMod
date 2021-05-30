package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Structs;
import mindustry.ctype.ContentList;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NHLoader implements ContentList{
	public static final int outlineStroke = 4;
	public static ObjectMap<String, NHIconGenerator.IconSet> fullIconNeeds = new ObjectMap<>();
	public static ObjectMap<String, TextureRegion> outlineTex = new ObjectMap<>();
	public static ObjectMap<String, TextureRegion> needBeLoad = new ObjectMap<>();
	public static ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
	public static final Color outlineColor = Color.valueOf("565666");
	public static NHContent content;
	public static NHIconGenerator iconGenerator;
	
	public static void free(){
		fullIconNeeds = null;
		outlineTex = null;
		needBeLoad = null;
		unitBuildCost = null;
	}
	
	public static void putNeedLoad(String name, TextureRegion textureRegion){
		needBeLoad.put(name, textureRegion);
	}
	
	public static void put(String name, @NotNull NHIconGenerator.IconSet set){
		NHLoader.fullIconNeeds.put(name, set);
	}
	
	public static void put(String name){
		NHLoader.outlineTex.put(NewHorizon.configName(name), null);
	}
	
	public static void put(String... args){
		for(String name : args)put(name);
	}
	
	public static Pixmap getOutline(Pixmap base, Color outlineColor){
	    PixmapRegion region = new PixmapRegion(base);
	    Pixmap out = new Pixmap(region.width, region.height);
	    Color color = new Color();
	    
	    for(int x = 0; x < region.width; ++x){
	        for(int y = 0; y < region.height; ++y){
	            region.getPixel(x, y, color);
	            out.draw(x, y, color);
	            if(color.a < 1.0F){
	                boolean found = false;
	                
	                loop:
	                for(int rx = -outlineStroke; rx <= outlineStroke; ++rx){
	                    for(int ry = -outlineStroke; ry <= outlineStroke; ++ry){
	                        if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && Mathf.within((float)rx, (float)ry, outlineStroke) && color.set(region.getPixel(rx + x, ry + y)).a > 0.01F){
	                            found = true;
	                            break loop;
	                        }
	                    }
	                }
	                
	                if(found){
	                    out.draw(x, y, outlineColor);
	                }
	            }
	        }
	    }
	    return out;
	}
	
	public static Pixmap getOutline(TextureAtlas.AtlasRegion t, Color outlineColor){
	    if(t.found()){
	        return getOutline(Core.atlas.getPixmap(t).crop(), outlineColor);
	    }else return new Pixmap(255, 255);
	}
	
	public static void drawWeaponPixmap(Pixmap base, Weapon w, boolean outline){
	    TextureAtlas.AtlasRegion t = Core.atlas.find(w.name);
	    if(!t.found())return;
	    Pixmap wRegion = outline ? getOutline(t, outlineColor) : Core.atlas.getPixmap(t).crop();
	    
	    int startX = getCenter(base, wRegion, true, outline), startY = getCenter(base, wRegion, false, outline);
	
	    if(w.mirror){
	        PixmapRegion t2 = Core.atlas.getPixmap(t);
	        Pixmap wRegion2 = outline ? getOutline(flipX(t2), outlineColor) : flipX(t2);
	        base.drawPixmap(wRegion, startX + (int)w.x * 4, startY - (int)w.y * 4, 0, 0, wRegion.getWidth(), wRegion.getHeight());
	        base.drawPixmap(wRegion2, getCenter(base, wRegion2, true, outline) - (int)w.x * 4, getCenter(base, wRegion2, false, outline) - (int)w.y * 4, 0, 0, -wRegion2.getWidth(), wRegion2.getHeight());
	    }else{
	        base.drawPixmap(wRegion, startX + (int)(w.x) * 4, startY - (int)(w.y) * 4);
	    }
	}
	
	public static int getCenter(Pixmap base, Pixmap above, boolean WorH, boolean outline){
	    return (WorH ? (base.getWidth() - above.getWidth()) / 2 : (base.getHeight() - above.getHeight()) / 2);
	}
	
	public static Pixmap flipX(PixmapRegion pixmap){
	    Pixmap base = new Pixmap(pixmap.width, pixmap.height);
	    Color color = new Color();
	    
	    if(color.a < 1.0F){
	        for(int y = 0; y < pixmap.height; ++y){
	            for(int x = 0; x < pixmap.width; ++x){
	                pixmap.getPixel(x, y, color);
	                base.draw(pixmap.width - x, y, color);
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
	                pixmap.getPixel(x, y, color);
	                base.draw(pixmap.width - x, y, color.mul(replaceColor));
	            }
	        }
	    }
	    return base;
	}
	
	@Override
	public void load(){
		content = new NHContent();
	}
	
	public void loadLast(){
		iconGenerator = new NHIconGenerator();
	}

}
