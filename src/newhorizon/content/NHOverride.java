package newhorizon.content;

import arc.struct.Seq;
import arc.util.Structs;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.production.GenericCrafter;

import java.util.Arrays;

public class NHOverride{
	public static void load(){
		Fx.trailFade.clip = 2000;
		
		Blocks.coreFoundation.health *= 5;
		Blocks.coreNucleus.health *= 5;
		Blocks.coreShard.health *= 5;
		
		Fx.lightning.layer(Fx.lightning.layer - 0.1f);
		
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
		
		removeReq(Blocks.ripple, Items.titanium);
		addReq(Blocks.ripple,
			new ItemStack(NHItems.presstanium, 50)
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
