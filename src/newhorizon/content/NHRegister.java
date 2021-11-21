package newhorizon.content;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.ai.BaseRegistry;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NewHorizon;

import java.io.IOException;

import static mindustry.Vars.tilesize;

public class NHRegister{
	public static void load(){
//		NewHorizon.MOD.root.child("schematics-bases").findAll().each(f -> {
//			try{
//				Schematic s = Schematics.read(f);
//				Vars.schematics.add(s);
//			}catch(IOException e){
//				e.printStackTrace();
//			}
//		});
		
		registerBase();
		
//		Vars.bases.parts.each(e -> Vars.schematics.add(e.schematic));
	}
	
	public static void registerBase(){
		String[] names = NewHorizon.MOD.root.child("schematics-bases").child("to-register.txt").readString().split("\n");
		
		for(String name : names){
			if(name == null || name.isEmpty())continue;
			try{
				Schematic schem = Schematics.read(NewHorizon.MOD.root.child("schematics-bases").child(name + ".msch"));
				
				BaseRegistry.BasePart part = new BaseRegistry.BasePart(schem);
				Tmp.v1.setZero();
				int drills = 0;
				
				for(Schematic.Stile tile : schem.tiles){
					//keep track of core type
					if(tile.block instanceof CoreBlock){
						part.core = tile.block;
					}
					
					//save the required resource based on item source - multiple sources are not allowed
					if(tile.block instanceof ItemSource){
						Item config = (Item)tile.config;
						if(config != null) part.required = config;
					}
					
					//same for liquids - this is not used yet
					if(tile.block instanceof LiquidSource){
						Liquid config = (Liquid)tile.config;
						if(config != null) part.required = config;
					}
					
					//calculate averages
					if(tile.block instanceof Drill || tile.block instanceof Pump){
						Tmp.v1.add(tile.x*tilesize + tile.block.offset, tile.y*tilesize + tile.block.offset);
						drills ++;
					}
				}
				schem.tiles.removeAll(s -> s.block.buildVisibility == BuildVisibility.sandboxOnly);
				
				part.tier = schem.tiles.sumf(s -> Mathf.pow(s.block.buildCost / s.block.buildCostMultiplier, 1.4f));
				
				if(part.core != null){
					Vars.bases.cores.add(part);
				}else if(part.required == null){
					Vars.bases.parts.add(part);
				}
				
				if(drills > 0){
					Tmp.v1.scl(1f / drills).scl(1f / tilesize);
					part.centerX = (int)Tmp.v1.x;
					part.centerY = (int)Tmp.v1.y;
				}else{
					part.centerX = part.schematic.width/2;
					part.centerY = part.schematic.height/2;
				}
				
				if(part.required != null && part.core == null){
					Vars.bases.reqParts.get(part.required, Seq::new).add(part);
				}
				
			}catch(IOException e){
				Log.err(e);
			}
		}
		
		Vars.bases.cores.sort(b -> b.tier);
		Vars.bases.parts.sort();
		Vars.bases.reqParts.each((key, arr) -> arr.sort());
	}
}
