package newhorizon.func;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;

public class NHUnitOutline{
	//These pieces of shit will be removed after anuke's damn update
	static{
		Team.sharded.palette[0] = Color.valueOf("ffd27e");
		Team.sharded.palette[1] = Color.valueOf("eab678");
		Team.sharded.palette[2] = Color.valueOf("d4806b");
		
		Team.crux.palette[0] = Color.valueOf("fc8e6c");
		Team.crux.palette[1] = Color.valueOf("f15454");
		Team.crux.palette[2] = Color.valueOf("a04553");
		
		Team.derelict.palette[0] = Color.valueOf("4d4e58");
		Team.derelict.palette[1] = Color.valueOf("423d44");
		Team.derelict.palette[2] = Color.valueOf("2f272c");
		
		Team.green.palette[0] = Color.valueOf("90eb86");
		Team.green.palette[1] = Color.valueOf("50cd78");
		Team.green.palette[2] = Color.valueOf("267358");
		
		Team.purple.palette[0] = Color.valueOf("e687cc");
		Team.purple.palette[1] = Color.valueOf("9257a8");
		Team.purple.palette[2] = Color.valueOf("2f2a5f");
		
		Team.blue.palette[0] = Color.valueOf("7aa3f4");
		Team.blue.palette[1] = Color.valueOf("5149e0");
		Team.blue.palette[2] = Color.valueOf("3c1e78");
	}
	
	public static final float[] v = {100, 86, 61};
	
	public static final float[] hsv = new float[3];
	
	@Deprecated
	public static void createTeamIcon(MultiPacker packer, Block block){
		if(!Vars.headless && block.teamRegion != null && block.teamRegion.found() && block.teamRegion instanceof TextureAtlas.AtlasRegion){
			ObjectMap<Team, ObjectMap<Boolf<Color>, Color>> maps = new ObjectMap<>(6);
			for(Team team : Team.baseTeams){
				ObjectMap<Boolf<Color>, Color> map = new ObjectMap<>();
				
				for(int i = 0; i < team.palette.length; i++){
					int finalI = i;
					map.put(color -> Mathf.equal(color.toHsv(hsv)[2], v[finalI] / 100f, 0.05f), team.palette[i]);
				}
				
				maps.put(team, map);
				team.hasPalette = true;
			}
			
			PixmapRegion pixmapRegion = new PixmapRegion(Core.atlas.getPixmap(block.teamRegion).crop());
			
			for(Team team : maps.keys()){
				Pixmap output = replaceColor(pixmapRegion, maps.get(team));
				packer.add(MultiPacker.PageType.main, block.name + "-team-" + team.name, output);
				if(team.id == 1)packer.add(MultiPacker.PageType.main, block.name + "-team", output);
			}
			
			block.load();
		}
	}
	
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
