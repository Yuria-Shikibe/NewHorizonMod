package newhorizon.content;

import arc.graphics.Color;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.game.SpawnGroup;
import mindustry.game.Waves;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.logic.CanvasBlock;
import mindustry.world.blocks.production.GenericCrafter;
import newhorizon.NewHorizon;

import java.lang.reflect.Field;
import java.util.Arrays;

public class NHOverride{
	public static final Seq<SpawnGroup> modSpawnGroup = new Seq<>();
	public static final Seq<Color> validColor = new Seq<>();
	
	public static void load(){
		overrideCanvas();
	}
	
	public static void overrideCanvas(){
		if(Blocks.canvas instanceof CanvasBlock){
			CanvasBlock canvas = (CanvasBlock)Blocks.canvas;
			
			for(int i : canvas.palette){
				validColor.add(Color.valueOf(Integer.toHexString(i)));
			}
			
			validColor.addAll(Color.white, Color.lightGray, Color.gray, Pal.accent);
			
			canvas.palette = validColor.mapInt(Color::rgba).toArray();
		}
	}
	
	public static void overrideVanillaMain(){
		Fx.trailFade.clip = 2000;
		
		{
		modSpawnGroup.addAll(
				new SpawnGroup(NHUnitTypes.origin){{
                     end = 12;
                     unitScaling = 2f;
                     max = 30;
                 }},
				
				new SpawnGroup(NHUnitTypes.assaulter){{
					begin = 4;
					end = 13;
					unitAmount = 1;
					unitScaling = 1.25f;
				}},
				
				new SpawnGroup(NHUnitTypes.sharp){{
					begin = 6;
					end = 16;
					unitScaling = 1f;
				}},
				
				new SpawnGroup(NHUnitTypes.origin){{
					begin = 11;
					unitScaling = 1.7f;
					spacing = 2;
					max = 4;
					shieldScaling = 25f;
				}},
				
				new SpawnGroup(NHUnitTypes.thynomo){{
					begin = 16;
					spacing = 3;
					unitScaling = 0.5f;
					max = 25;
				}},
				
				new SpawnGroup(NHUnitTypes.origin){{
					begin = 12;
					unitScaling = 1;
					unitAmount = 4;
					spacing = 2;
					shieldScaling = 20f;
					max = 14;
				}},
				
				new SpawnGroup(NHUnitTypes.thynomo){{
					begin = 28;
					spacing = 3;
					unitScaling = 1;
					end = 40;
					shieldScaling = 20f;
				}},
				
				new SpawnGroup(NHUnitTypes.aliotiat){{
					begin = 45;
					spacing = 3;
					unitScaling = 1;
					max = 10;
					shieldScaling = 30f;
					shields = 100;
					effect = StatusEffects.overdrive;
				}},
				
				new SpawnGroup(NHUnitTypes.sharp){{
					begin = 16;
					unitScaling = 1;
					spacing = 2;
					shieldScaling = 20f;
					max = 20;
				}},
				
				new SpawnGroup(NHUnitTypes.warper){{
					begin = 40;
					end = 80;
					spacing = 5;
					unitAmount = 2;
					unitScaling = 2;
					max = 20;
					shieldScaling = 30;
				}},
				
				new SpawnGroup(NHUnitTypes.branch){{
					begin = 35;
					spacing = 3;
					unitAmount = 4;
					effect = StatusEffects.overdrive;
					items = new ItemStack(NHItems.thermoCorePositive, 60);
					end = 60;
				}},
				
				new SpawnGroup(NHUnitTypes.branch){{
					begin = 42;
					spacing = 3;
					unitAmount = 4;
					effect = StatusEffects.overdrive;
					items = new ItemStack(NHItems.thermoCorePositive, 100);
					end = 130;
					max = 30;
				}},
				
				new SpawnGroup(NHUnitTypes.warper){{
					begin = 55;
					unitAmount = 2;
					spacing = 2;
					unitScaling = 2;
					shieldScaling = 20;
				}},
				
				new SpawnGroup(NHUnitTypes.tarlidor){{
					begin = 53;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 6;
					shieldScaling = 30f;
				}},
				
				new SpawnGroup(NHUnitTypes.annihilation){{
					begin = 81;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 8;
					shieldScaling = 30f;
				}},
				
				new SpawnGroup(NHUnitTypes.sin){{
					begin = 120;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 10;
					shieldScaling = 30f;
				}},
				
				new SpawnGroup(NHUnitTypes.hurricane){{
					begin = 140;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 10;
					shieldScaling = 300f;
				}},
				
				new SpawnGroup(NHUnitTypes.anvil){{
					begin = 145;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 15;
					shieldScaling = 30f;
					shields = 300;
				}},
				
				new SpawnGroup(NHUnitTypes.guardian){{
					begin = 125;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 20;
					shieldScaling = 30f;
					shields = 500;
				}},
				
				new SpawnGroup(NHUnitTypes.saviour){{
					begin = 105;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 12;
					shieldScaling = 30f;
					shields = 3000;
					
					payloads = Seq.with(NHUnitTypes.aliotiat, NHUnitTypes.aliotiat);
				}},
				
				new SpawnGroup(NHUnitTypes.striker){{
					begin = 75;
					unitAmount = 2;
					unitScaling = 3;
					spacing = 4;
					shields = 40f;
					shieldScaling = 30f;
				}},
				
				new SpawnGroup(NHUnitTypes.destruction){{
					begin = 90;
					unitAmount = 2;
					unitScaling = 3;
					spacing = 10;
					shields = 40f;
					shieldScaling = 100f;
				}},
				
				new SpawnGroup(NHUnitTypes.collapser){{
					begin = 180;
					unitAmount = 1;
					unitScaling = 1;
					spacing = 25;
					shields = 1000;
					shieldScaling = 350f;
				}}
		);
		}//Apply Mod Units
		
		try{
			Field field;
			field = Waves.class.getDeclaredField("spawns");
			field.setAccessible(true);
			Vars.waves.get();
			Seq<SpawnGroup> spawns = (Seq<SpawnGroup>)field.get(Vars.waves);
			spawns.addAll(modSpawnGroup);
		}catch(NoSuchFieldException | IllegalAccessException e){
			e.printStackTrace();
		}
		
		
		ObjectSet<UnitType> Unit_T1 = ObjectSet.with(
			UnitTypes.dagger, UnitTypes.nova, UnitTypes.crawler, UnitTypes.flare, UnitTypes.mono,
			UnitTypes.risso, UnitTypes.retusa, NHUnitTypes.origin, NHUnitTypes.sharp, NHUnitTypes.assaulter
		);
		
		ObjectSet<UnitType> Unit_T2 = ObjectSet.with(
			UnitTypes.mace, UnitTypes.pulsar, UnitTypes.atrax, UnitTypes.horizon, UnitTypes.poly,
			UnitTypes.minke, UnitTypes.oxynoe, NHUnitTypes.thynomo, NHUnitTypes.branch, NHUnitTypes.relay
		);
		
		ObjectSet<UnitType> Unit_T3 = ObjectSet.with(
			UnitTypes.fortress, UnitTypes.quasar, UnitTypes.spiroct, UnitTypes.zenith, UnitTypes.mega,
			UnitTypes.bryde, UnitTypes.cyerce, NHUnitTypes.aliotiat, NHUnitTypes.warper, NHUnitTypes.ghost,
			NHUnitTypes.rhino, NHUnitTypes.gather
		);
		
		ObjectSet<UnitType> Unit_T4 = ObjectSet.with(
			UnitTypes.scepter, UnitTypes.vela, UnitTypes.arkyid, UnitTypes.antumbra, UnitTypes.quad,
			UnitTypes.sei, UnitTypes.aegires, NHUnitTypes.tarlidor, NHUnitTypes.naxos, NHUnitTypes.striker, NHUnitTypes.zarkov
		);
		
		ObjectSet<UnitType> Unit_T5 = ObjectSet.with(
			UnitTypes.reign, UnitTypes.toxopid, UnitTypes.eclipse, UnitTypes.oct,
			UnitTypes.omura, UnitTypes.navanax, NHUnitTypes.annihilation, NHUnitTypes.destruction, NHUnitTypes.longinus, NHUnitTypes.declining, NHUnitTypes.saviour
		);
		
		ObjectSet<UnitType> Unit_T6 = ObjectSet.with(
			NHUnitTypes.hurricane, NHUnitTypes.guardian, NHUnitTypes.anvil
		);
		
		ObjectSet<UnitType> Unit_T7 = ObjectSet.with(
			NHUnitTypes.collapser
		);
		
		Unit_T3.each(u -> u.immunities.addAll(NHStatusEffects.emp1));
		Unit_T4.each(u -> u.immunities.addAll(NHStatusEffects.emp1));
		Unit_T5.each(u -> u.immunities.addAll(NHStatusEffects.emp1, NHStatusEffects.emp2, NHStatusEffects.ultFireBurn));
		Unit_T6.each(u -> u.immunities.addAll(NHStatusEffects.scannerDown, NHStatusEffects.scrambler));
		
		new Seq<UnitType>().addAll(Unit_T4.toSeq()).addAll(Unit_T5.toSeq()).filter(u -> u != null && !u.name.startsWith(NewHorizon.MOD_NAME)).each(u -> {
			u.armor += 3;
			u.health *= 1.1;
		});
		
		Blocks.coreFoundation.health *= 5;
		Blocks.coreNucleus.health *= 5;
		Blocks.coreShard.health *= 5;
		
		Fx.lightning.layer(Fx.lightning.layer - 0.1f);
		
//		Team.purple.name = "Luminari";
		
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
				new ItemStack(NHItems.seniorProcessor, 100)
			);
			removeReq(Blocks.spectre, Items.silicon, Items.surgeAlloy, Items.graphite);
			for(Item item : block.ammoTypes.keys()){
				BulletType type = block.ammoTypes.get(item);
				type.damage *= 2f;
				type.pierceCap *= 1.5f;
				type.lifetime += 8f;
			}
		}
		
		meltdown: {
			if(!(Blocks.meltdown instanceof LaserTurret))break meltdown;
			LaserTurret block = (LaserTurret)Blocks.meltdown;
			addReq(Blocks.meltdown, new ItemStack(NHItems.presstanium, 350), new ItemStack(NHItems.metalOxhydrigen, 175), new ItemStack(NHItems.seniorProcessor, 120));
			removeReq(Blocks.meltdown, Items.surgeAlloy, Items.lead);
			ContinuousLaserBulletType meltDownType = ((ContinuousLaserBulletType)block.shootType);
			meltDownType.length += 120;
			meltDownType.damage += 60f;
			meltDownType.splashDamage += 10f;
			meltDownType.splashDamageRadius += 14f;
			block.range += 120;
			block.shootDuration += 60;
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
		
		addReq(Blocks.logicProcessor, ItemStack.with(NHItems.juniorProcessor, 80));
		removeReq(Blocks.logicProcessor, Items.silicon);
		
		addReq(Blocks.hyperProcessor, ItemStack.with(NHItems.seniorProcessor, 80));
		removeReq(Blocks.hyperProcessor, Items.silicon);
	}
	
	
	private static void addReq(Block target, ItemStack... items){
		ItemStack[] newReq = new ItemStack[items.length + target.requirements.length];
		
		System.arraycopy(target.requirements, 0, newReq, 0, target.requirements.length);
		System.arraycopy(items, 0, newReq, target.requirements.length, items.length);
		
		target.requirements = newReq;
		Arrays.sort(target.requirements, Structs.comparingInt((j) -> j.item.id));
	}
	
	private static void removeReq(Block target, Item... items){
		Seq<ItemStack> req = new Seq<>(ItemStack.class);
		req.addAll(target.requirements);
		
		for(Item item : items)req.each(itemReq -> itemReq.item == item, req::remove);
		
		target.requirements = req.shrink();
	}
}
