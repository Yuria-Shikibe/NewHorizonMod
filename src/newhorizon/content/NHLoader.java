package newhorizon.content;

import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.ctype.ContentList;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.production.GenericCrafter;
import newhorizon.NewHorizon;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NHLoader implements ContentList{
	public static final ObjectMap<String, NHIconGenerator.IconSet> fullIconNeeds = new ObjectMap<>();
	public static final ObjectMap<String, TextureRegion> outlineTex = new ObjectMap<>();
	public static final ObjectMap<String, TextureRegion> needBeLoad = new ObjectMap<>();
	public static final ObjectMap<UnitType, ItemStack[]> unitBuildCost = new ObjectMap<>();
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
		addReq(Blocks.spectre,
				new ItemStack(NHItems.zeta, 220),
				new ItemStack(NHItems.seniorProcessor, 100),
				new ItemStack(NHItems.multipleSteel, 150)
		);
		removeReq(Blocks.spectre, Items.silicon);
		meltdown: {
			addReq(Blocks.meltdown, new ItemStack(NHItems.presstanium, 250), new ItemStack(NHItems.metalOxhydrigen, 175), new ItemStack(NHItems.seniorProcessor, 120));
			ContinuousLaserBulletType meltDownType = ((ContinuousLaserBulletType)((LaserTurret)Blocks.meltdown).shootType);
			meltDownType.length += 120;
			meltDownType.damage += 55f;
			meltDownType.splashDamage += 10f;
			meltDownType.splashDamageRadius += 14f;
			((LaserTurret)Blocks.meltdown).range += 120;
			((LaserTurret)Blocks.meltdown).shootDuration += 30;
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
