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
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.production.GenericCrafter;
import newhorizon.NewHorizon;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NHLoader implements ContentList{
	public static final int outlineStroke = 4;
	public static final ObjectMap<String, NHIconGenerator.IconSet> fullIconNeeds = new ObjectMap<>();
	public static final ObjectMap<String, TextureRegion> outlineTex = new ObjectMap<>();
	public static final ObjectMap<String, TextureRegion> needBeLoad = new ObjectMap<>();
	public static final ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
	public static final Color outlineColor = Color.valueOf("565666");
	public static NHContent content;
	public static NHIconGenerator iconGenerator;
	
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
		
		Blocks.coreFoundation.health *= 5;
		Blocks.coreNucleus.health *= 5;
		Blocks.coreShard.health *= 5;
		
//		for(Block block : Vars.content.blocks()){
//			block.health *= 1.5;
//		}
		
		addReq(Blocks.blastDrill,
				new ItemStack(NHItems.presstanium, 50),
				new ItemStack(NHItems.juniorProcessor, 40)
		);
		removeReq(Blocks.blastDrill, Items.silicon);
		
		addReq(Blocks.coreFoundation,
				new ItemStack(NHItems.presstanium, 1500),
				new ItemStack(NHItems.metalOxhydrigen, 800),
				new ItemStack(NHItems.juniorProcessor, 600)
		);
		addReq(Blocks.plastaniumCompressor,
				new ItemStack(NHItems.presstanium, 50),
				new ItemStack(NHItems.juniorProcessor, 30)
		);
		removeReq(Blocks.plastaniumCompressor, Items.silicon);
		
		addReq(Blocks.commandCenter,
				new ItemStack(NHItems.presstanium, 80),
				new ItemStack(NHItems.juniorProcessor, 200)
		);
		removeReq(Blocks.commandCenter, Items.silicon);
		
		addReq(Blocks.multiplicativeReconstructor,
				new ItemStack(NHItems.presstanium, 80)
		);
		
		addReq(Blocks.exponentialReconstructor,
				new ItemStack(NHItems.presstanium, 400),
				new ItemStack(NHItems.metalOxhydrigen, 400),
				new ItemStack(NHItems.juniorProcessor, 600)
		);
		removeReq(Blocks.exponentialReconstructor, Items.silicon);
		
		addReq(Blocks.tetrativeReconstructor,
				new ItemStack(NHItems.irayrondPanel, 200),
				new ItemStack(NHItems.multipleSteel, 400),
				new ItemStack(NHItems.seniorProcessor, 600)
		);
		removeReq(Blocks.tetrativeReconstructor, Items.silicon);
		
		addReq(Blocks.ripple,
				new ItemStack(NHItems.metalOxhydrigen, 50)
		);
		
		addReq(Blocks.fuse,
				new ItemStack(NHItems.zeta, 80)
		);
		
		addReq(Blocks.coreNucleus,
				new ItemStack(NHItems.irayrondPanel, 1500),
				new ItemStack(NHItems.multipleSteel, 800),
				new ItemStack(NHItems.seniorProcessor, 600)
		);
		addReq(Blocks.surgeSmelter,
				new ItemStack(NHItems.multipleSteel, 35),
				new ItemStack(NHItems.presstanium, 65),
				new ItemStack(NHItems.juniorProcessor, 30),
				new ItemStack(NHItems.metalOxhydrigen, 45)
		);
		
		((GenericCrafter) Blocks.surgeSmelter).craftTime += 30f;
		removeReq(Blocks.surgeSmelter, Items.silicon);
		addReq(Blocks.swarmer,
				new ItemStack(NHItems.juniorProcessor, 25),
				new ItemStack(NHItems.presstanium, 35)
		);
		removeReq(Blocks.swarmer, Items.silicon);
		addReq(Blocks.blastMixer, new ItemStack(NHItems.juniorProcessor, 10));
		addReq(Blocks.cyclone, new ItemStack(NHItems.metalOxhydrigen, 55));
		addReq(Blocks.disassembler, new ItemStack(NHItems.multipleSteel, 65), new ItemStack(NHItems.juniorProcessor, 30));
		removeReq(Blocks.disassembler, Items.silicon);
		spectre: {
			if(!(Blocks.spectre instanceof ItemTurret))break spectre;
			ItemTurret block = (ItemTurret)Blocks.spectre;
			block.range += 80;
			block.health *= 1.5f;
			addReq(Blocks.spectre,
					new ItemStack(NHItems.zeta, 220),
					new ItemStack(NHItems.seniorProcessor, 100),
					new ItemStack(NHItems.multipleSteel, 150)
			);
			removeReq(Blocks.spectre, Items.silicon);
			for(Item item : block.ammoTypes.keys()){
				BulletType type = block.ammoTypes.get(item);
				type.damage *= 1.5f;
				type.splashDamage *= 1.5f;
				type.lifetime += 10f;
			}
		}
		meltdown: {
			if(!(Blocks.meltdown instanceof LaserTurret))break meltdown;
			LaserTurret block = (LaserTurret)Blocks.meltdown;
			addReq(Blocks.meltdown, new ItemStack(NHItems.presstanium, 250), new ItemStack(NHItems.metalOxhydrigen, 175), new ItemStack(NHItems.seniorProcessor, 120));
			ContinuousLaserBulletType meltDownType = ((ContinuousLaserBulletType)block.shootType);
			meltDownType.length += 120;
			meltDownType.damage += 55f;
			meltDownType.splashDamage += 10f;
			meltDownType.splashDamageRadius += 14f;
			block.range += 120;
			block.shootDuration += 30;
		}
		
		
		removeReq(Blocks.meltdown, Items.silicon);
		
		addReq(Blocks.foreshadow,
				new ItemStack(NHItems.seniorProcessor, 220),
				new ItemStack(NHItems.multipleSteel, 180)
		);
		removeReq(Blocks.foreshadow, Items.silicon);
		
		addReq(Blocks.rtgGenerator,
				new ItemStack(NHItems.juniorProcessor, 65),
				new ItemStack(NHItems.multipleSteel, 45)
		);
		removeReq(Blocks.rtgGenerator, Items.silicon);
	}
	
	private static void addReq(Block target, ItemStack... items){
		ItemStack[] newReq = new ItemStack[items.length + target.requirements.length];
		
		int i;
		
		for(i = 0; i < target.requirements.length; i++){
			newReq[i] = target.requirements[i];
		}
		
		for(i = 0; i < items.length; i++){
			newReq[i + target.requirements.length] = items[i];
		}
		
		target.requirements = newReq;
		Arrays.sort(target.requirements, Structs.comparingInt((j) -> j.item.id));
	}
	
	private static void removeReq(Block target, Item... items){
		Seq<ItemStack> req = new Seq<>(ItemStack.class);
		req.addAll(target.requirements);
		
		for(Item item : items){
			req.each(itemReq -> itemReq.item == item, req::remove);
		}
		target.requirements = req.shrink();
	}
}
