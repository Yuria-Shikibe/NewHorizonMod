package newhorizon.content;

import arc.func.Cons;
import arc.func.Intc;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.game.SpawnGroup;
import mindustry.game.Waves;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.meta.*;
import newhorizon.NHSetting;
import newhorizon.content.bullets.OverrideBullets;
import newhorizon.expand.ability.passive.PassiveShield;
import newhorizon.expand.block.turrets.AdaptPowerTurret;
import newhorizon.expand.bullets.AdaptBulletType;
import newhorizon.expand.bullets.AdaptedLightningBulletType;
import newhorizon.util.func.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static mindustry.Vars.content;
import static mindustry.content.UnitTypes.*;
import static mindustry.type.ItemStack.with;

import mindustry.world.consumers.*;
import mindustry.world.blocks.power.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;




public class NHPostProcess {
	public static final Seq<SpawnGroup> modSpawnGroup = new Seq<>();
	public static final Seq<Color> validColor = new Seq<>();

	private static final Seq<TechTree.TechNode> tmpVanillaNode = new Seq<>();
	private static final Seq<TechTree.TechNode> tmpCreatedNode = new Seq<>();

	public static void load(){
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
						begin = 35;
						spacing = 3;
						unitScaling = 1;
						max = 18;
						shieldScaling = 30f;
						shields = 100;
					}},
					
					new SpawnGroup(NHUnitTypes.aliotiat){{
						begin = 40;
						spacing = 3;
						unitScaling = 1;
						max = 15;
						shieldScaling = 50f;
						shields = 150;
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
						effect = StatusEffects.overdrive;
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
						begin = 8;
						spacing = 3;
						unitAmount = 4;
						effect = StatusEffects.overdrive;
						items = new ItemStack(NHItems.thermoCorePositive, 100);
						end = 130;
						max = 30;
					}},
					
					new SpawnGroup(NHUnitTypes.warper){{
						begin = 30;
						unitAmount = 2;
						spacing = 2;
						unitScaling = 2;
						shieldScaling = 20;
					}},
					
					new SpawnGroup(NHUnitTypes.tarlidor){{
						begin = 35;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 6;
						shieldScaling = 80f;
					}},
					
					new SpawnGroup(NHUnitTypes.annihilation){{
						begin = 65;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 6;
						shields = 2000;
						shieldScaling = 800f;
					}},
					
					new SpawnGroup(NHUnitTypes.laugra){{
						begin = 75;
						unitAmount = 2;
						unitScaling = 1;
						spacing = 12;
						shieldScaling = 600f;
						effect = StatusEffects.boss;
					}},
					
					new SpawnGroup(NHUnitTypes.macrophage){{
						begin = 75;
						unitAmount = 2;
						unitScaling = 0.5f;
						spacing = 5;
						shieldScaling = 500f;
						shields = 1200;
						effect = NHStatusEffects.quantization;
					}},
					
					new SpawnGroup(NHUnitTypes.laugra){{
						begin = 105;
						unitAmount = 1;
						unitScaling = 0.7f;
						spacing = 4;
						shieldScaling = 200f;
						effect = NHStatusEffects.overphased;
					}},
					
					new SpawnGroup(NHUnitTypes.sin){{
						begin = 110;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 10;
						shieldScaling = 30f;
					}},
					
					new SpawnGroup(NHUnitTypes.hurricane){{
						begin = 105;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 10;
						shieldScaling = 300f;
					}},
					
					new SpawnGroup(NHUnitTypes.anvil){{
						begin = 70;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 15;
						shieldScaling = 30f;
						shields = 300;
					}},
					
					new SpawnGroup(NHUnitTypes.guardian){{
						begin = 75;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 20;
						shieldScaling = 30f;
						shields = 500;
					}},
					
					new SpawnGroup(NHUnitTypes.saviour){{
						begin = 70;
						unitAmount = 1;
						unitScaling = 3;
						spacing = 9;
						shieldScaling = 30f;
						shields = 3000;
					}},
					
					new SpawnGroup(NHUnitTypes.naxos){{
						begin = 75;
						unitAmount = 2;
						unitScaling = 3;
						spacing = 4;
						shields = 40f;
						shieldScaling = 30f;
					}},
					
					new SpawnGroup(NHUnitTypes.sin){{
						begin = 100;
						unitAmount = 1;
						unitScaling = 1;
						effect = StatusEffects.boss;
						spacing = 15;
						shields = 80f;
						shieldScaling = 100f;
					}},
					
					new SpawnGroup(NHUnitTypes.longinus){{
						begin = 90;
						unitAmount = 2;
						unitScaling = 3;
						spacing = 10;
						shields = 40f;
						shieldScaling = 20f;
					}},
					
					new SpawnGroup(NHUnitTypes.collapser){{
						begin = 120;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 12;
						shields = 19000;
						shieldScaling = 3500f;
					}},
					
					new SpawnGroup(NHUnitTypes.nucleoid){{
						begin = 180;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 20;
						shields = 80000;
						shieldScaling = 2000f;
					}},
					
					new SpawnGroup(NHUnitTypes.pester){{
						begin = 130;
						unitAmount = 1;
						unitScaling = 1;
						spacing = 20;
						shields = 26000;
						shieldScaling = 5000f;
					}}
			);
		}

		contentOverride();
	}

	public static void loadOptional(){
		overrideVanillaMain();

		setModContentEnv();
		overrideUnitTypeAbility();
		buffCoreUnits();

		adjustVanillaLogistic();
		adjustVanillaDrill();
		adjustVanillaFactories();
		adjustVanillaPower();
		adjustVanillaUnit();
		adjustVanillaLogic();
		adjustVanillaTurret();
	}

	public static void contentOverride(){


		//overrideStats();
	}

	public static void postProcessOverride(){
		overrideStats();
		setupAdaptBulletType();
	}

	public static void setupAdaptBulletType(){
		//replaceUnitTypeBullets(alpha, alpha.weapons.get(0).bullet, (AdaptBulletType b) -> b.setDamage(15, 15));
		//replaceUnitTypeBullets(beta, beta.weapons.get(0).bullet, (AdaptBulletType b) -> b.setDamage(20, 20));
		//replaceUnitTypeBullets(gamma, gamma.weapons.get(0).bullet, (AdaptBulletType b) -> b.setDamage(25, 25));
	}

	public static void replaceUnitTypeBullets(UnitType unitType, BulletType bulletType, Cons<AdaptBulletType> modifier){
		BulletType replacement = replaceBullet(bulletType, modifier);
		for (Weapon weapon: unitType.weapons){
			if (weapon.bullet == bulletType){
				weapon.bullet = replacement;
			}
		}
	}
	public static BulletType replaceBullet(BulletType bullet, Cons<AdaptBulletType> modifier){
		BulletType bulletType = OverrideBullets.getReplacement(bullet);
		if(bulletType instanceof AdaptBulletType replacement){
			ReflectionUtil.copyProperties(bullet, replacement);
			modifier.get(replacement);
		}
		return bulletType;
	}

	public static void setModContentEnv(){
		for(Block block: content.blocks()){
			if (block.name.startsWith("new-horizon")){
				block.shownPlanets.clear();
				block.shownPlanets.addAll(Planets.serpulo, Planets.erekir
						//, NHPlanets.midantha
				);
			}
		}
	}

	public static void overrideVanillaMain(){
		replaceVanillaVisualContent();
		replaceVanillaSpawnGroup();
	}

	public static Seq<SpawnGroup> generate(float difficulty, Rand rand, boolean attack, boolean airOnly, boolean naval){
		UnitType[][] species = {
				{NHUnitTypes.origin, NHUnitTypes.thynomo, NHUnitTypes.aliotiat, NHUnitTypes.tarlidor, NHUnitTypes.annihilation, NHUnitTypes.sin},
				{NHUnitTypes.sharp, NHUnitTypes.branch, NHUnitTypes.warper, NHUnitTypes.naxos, NHUnitTypes.hurricane},
				{flare, NHUnitTypes.assaulter, NHUnitTypes.restrictionEnzyme, NHUnitTypes.destruction, NHUnitTypes.longinus},
				{NHUnitTypes.sharp, NHUnitTypes.assaulter, NHUnitTypes.branch, NHUnitTypes.longinus, NHUnitTypes.guardian},
				{risso, minke, NHUnitTypes.ghost, NHUnitTypes.zarkov, NHUnitTypes.declining},
				{risso, oxynoe, cyerce, aegires, navanax}, //retusa intentionally left out as it cannot damage the core properly
				{NHUnitTypes.branch, zenith, rand.chance(0.5) ? NHUnitTypes.naxos : NHUnitTypes.macrophage, rand.chance(0.5) ? NHUnitTypes.longinus : NHUnitTypes.laugra, rand.chance(0.5) ? NHUnitTypes.anvil : NHUnitTypes.hurricane}
		};
		
		if(airOnly){
			species = Structs.filter(UnitType[].class, species, v -> v[0].flying);
		}
		
		if(naval){
			species = Structs.filter(UnitType[].class, species, v -> v[0].flying || v[0].naval);
		}else{
			species = Structs.filter(UnitType[].class, species, v -> !v[0].naval);
		}
		
		UnitType[][] fspec = species;
		
		//required progression:
		//- extra periodic patterns
		
		Seq<SpawnGroup> out = new Seq<>();
		
		//max reasonable wave, after which everything gets boring
		int cap = 150;
		
		final float[] shieldStart = {30};
		float shieldsPerWave = 20 + difficulty*30f;
		float[] scaling = {1f, 1.15f, 1.55f, 2.5f, 2.7f, 3f};
		
		Intc createProgression = start -> {
			//main sequence
			UnitType[] curSpecies = Structs.random(fspec);
			int curTier = 0;
			
			for(int i = start; i < cap;){
				int f = i;
				int next = rand.random(8, 16) + (int)Mathf.lerp(5f, 0f, difficulty) + curTier * 4;
				
				float shieldAmount = Math.max((i - shieldStart[0]) * shieldsPerWave, 0);
				int space = start == 0 ? 1 : rand.random(1, 2);
				int ctier = curTier;
				
				//main progression
				out.add(new SpawnGroup(curSpecies[Math.min(curTier, curSpecies.length - 1)]){{
					unitAmount = f == start ? 1 : (int)(6 / scaling[ctier]);
					begin = f;
					end = f + next >= cap ? never : f + next;
					max = 13;
					unitScaling = (difficulty < 0.4f ? rand.random(2.5f, 5f) : rand.random(1f, 4f)) * scaling[ctier];
					shields = shieldAmount;
					shieldScaling = shieldsPerWave;
					spacing = space;
				}});
				
				//extra progression that tails out, blends in
//				out.add(new SpawnGroup(curSpecies[Math.min(curTier, curSpecies.length - 1)]){{
//					unitAmount = (int)(3 / scaling[ctier]);
//					begin = f + next - 1;
//					end = f + next + rand.random(6, 10);
//					max = 6;
//					unitScaling = rand.random(2f, 4f);
//					spacing = rand.random(2, 4);
//					shields = shieldAmount/2f;
//					shieldScaling = shieldsPerWave;
//				}});
				
				i += next + 1;
				if(curTier < 3 || (rand.chance(0.05) && difficulty > 0.8)){
					curTier ++;
				}
				
				//do not spawn bosses
				curTier = Math.min(curTier, 6);
				
				//small chance to switch species
				if(rand.chance(0.3)){
					curSpecies = Structs.random(fspec);
				}
			}
		};
		
		createProgression.get(0);
		
		int step = 5 + rand.random(5);
		
		while(step <= cap){
			createProgression.get(step);
			step += (int)(rand.random(15, 30) * Mathf.lerp(1f, 0.5f, difficulty));
		}
		
		int bossWave = (int)(rand.random(90, 120) * Mathf.lerp(1f, 0.5f, difficulty));
		int bossSpacing = (int)(rand.random(25, 40) * Mathf.lerp(1f, 0.5f, difficulty));
		
		int bossTier = difficulty < 0.6 ? 3 : 4;
		
		//main boss progression
		out.add(new SpawnGroup(Structs.random(species)[bossTier]){{
			unitAmount = 1;
			begin = bossWave;
			spacing = bossSpacing;
			end = never;
			max = 16;
			unitScaling = bossSpacing;
			shieldScaling = shieldsPerWave;
			effect = StatusEffects.boss;
		}});
		
		//alt boss progression
		out.add(new SpawnGroup(Structs.random(species)[bossTier]){{
			unitAmount = 1;
			begin = bossWave + rand.random(3, 5) * bossSpacing;
			spacing = bossSpacing;
			end = never;
			max = 16;
			unitScaling = bossSpacing;
			shieldScaling = shieldsPerWave;
			effect = StatusEffects.boss;
		}});
		
		int finalBossStart = 120 + rand.random(30);
		
		//final boss waves
		out.add(new SpawnGroup(Structs.random(species)[bossTier]){{
			unitAmount = 1;
			begin = finalBossStart;
			spacing = bossSpacing/2;
			end = never;
			unitScaling = bossSpacing;
			shields = 500;
			shieldScaling = shieldsPerWave * 4;
			effect = StatusEffects.boss;
		}});
		
		//final boss waves (alt)
		out.add(new SpawnGroup(Structs.random(species)[bossTier]){{
			unitAmount = 1;
			begin = finalBossStart + 15;
			spacing = bossSpacing/2;
			end = never;
			unitScaling = bossSpacing;
			shields = 500;
			shieldScaling = shieldsPerWave * 4;
			effect = StatusEffects.boss;
		}});
		
		//add megas to heal the base.
		if(attack && difficulty >= 0.5){
			int amount = Mathf.random(1, 3 + (int)(difficulty*2));
			
			for(int i = 0; i < amount; i++){
				int wave = Mathf.random(3, 20);
				out.add(new SpawnGroup(mega){{
					unitAmount = 1;
					begin = wave;
					end = wave;
					max = 16;
				}});
			}
		}
		
		//shift back waves on higher difficulty for a harder start
		int shift = Math.max((int)(difficulty * 14 - 5), 0);
		
		for(SpawnGroup group : out){
			group.begin -= shift;
			group.end -= shift;
		}
		
		out.add(NHPostProcess.modSpawnGroup);
		
		if(difficulty > 0.9){
			out.add(new SpawnGroup(NHUnitTypes.pester){{
				effect = NHStatusEffects.overphased;
				begin = 148;
				spacing = 1;
				unitAmount = 1;
			}});
		}
		
		out.each(s -> {
			if(s.type == NHUnitTypes.longinus || s.type == NHUnitTypes.naxos){
				s.unitScaling *= 1.75f;
				s.unitAmount = (int)Math.max(1, s.unitAmount / 1.5f);
			}
		});
		
		return out;
	}

	public static void overrideStats(){
		for (Block block: content.blocks()){
			//uhh so eg compatibility, better way come later
			if (block.minfo != null && block.minfo.mod != null && Objects.equals(block.minfo.mod.name, "exogenesis")) continue;
			if (block instanceof ItemTurret itemTurret) processAmmoStat(block, itemTurret.ammoTypes);
			if (block instanceof LiquidTurret liquidTurret) processAmmoStat(block, liquidTurret.ammoTypes);
			if (block instanceof PowerTurret powerTurret) processAmmoStat(block, ObjectMap.of(powerTurret, powerTurret.shootType));
			if (block instanceof ContinuousTurret continuousTurret) processAmmoStat(block, ObjectMap.of(continuousTurret, continuousTurret.shootType));
			if (block instanceof ContinuousLiquidTurret continuousLiquidTurret) processAmmoStat(block, continuousLiquidTurret.ammoTypes);
		}

		for (UnitType unitType: content.units()){
			unitType.checkStats();
			var map = unitType.stats.toMap();
			if (map.get(StatCat.function) != null && map.get(StatCat.function).get(Stat.weapons) != null){
				unitType.stats.remove(Stat.weapons);
				unitType.stats.add(Stat.weapons, NHStatValues.weapons(unitType, unitType.weapons));
			}
		}
	}

	private static void processAmmoStat(Block block, ObjectMap<? extends UnlockableContent, BulletType> ammo){
		block.checkStats();
		var map = block.stats.toMap();
		if (map.get(StatCat.function) != null && map.get(StatCat.function).get(Stat.ammo) != null){
			block.stats.remove(Stat.ammo);
			if (block instanceof ContinuousLiquidTurret continuousLiquidTurret){
				block.stats.add(Stat.ammo, table -> {
					table.row();
					StatValues.number(continuousLiquidTurret.liquidConsumed * 60f, StatUnit.perSecond, true).display(table);
				});
			}
			block.stats.add(Stat.ammo, NHStatValues.ammo(ammo, 0, false));
		}
	}

	private static void replaceVanillaVisualContent(){
		if(NHSetting.enableDetails())Fx.trailFade.clip = 2000;
		Fx.lightning.layer(Fx.lightning.layer - 0.1f);
	}

	@SuppressWarnings("unchecked")
	private static void replaceVanillaSpawnGroup(){
		try{
			Field field;
			field = Waves.class.getDeclaredField("spawns");
			field.setAccessible(true);
			Vars.waves.get();
			Seq<SpawnGroup> spawns = (Seq<SpawnGroup>)field.get(Vars.waves);
			spawns.addAll(modSpawnGroup);
		}catch(NoSuchFieldException | IllegalAccessException e){
			Log.info(e);
		}
	}

	private static void adjustVanillaLogic(){
		adjustContent(Blocks.message, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 5));
		adjustContent(Blocks.reinforcedMessage, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 5));
		adjustContent(Blocks.switchBlock, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 5));
		adjustContent(Blocks.microProcessor, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 50, Items.titanium, 50));
		adjustContent(Blocks.logicProcessor, content -> ((Block) content).requirements = ItemStack.with(NHItems.juniorProcessor, 50, NHItems.presstanium, 50));
		adjustContent(Blocks.hyperProcessor, content -> ((Block) content).requirements = ItemStack.with(NHItems.juniorProcessor, 100, NHItems.zeta, 200, Items.surgeAlloy, 150));
		adjustContent(Blocks.memoryCell, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 30));
		adjustContent(Blocks.memoryBank, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 80, Items.phaseFabric, 40));
		adjustContent(Blocks.logicDisplay, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 50));
		adjustContent(Blocks.largeLogicDisplay, content -> ((Block) content).requirements = ItemStack.with(Items.silicon, 150, Items.phaseFabric, 75));
	}

	private static void adjustVanillaLogistic(){
		adjustContent(Blocks.rotaryPump, content -> {
			Pump pump = (Pump) content;
			pump.requirements = ItemStack.with(Items.graphite, 80, Items.metaglass, 50);
		});

		hideContent(Blocks.conveyor);
		hideContent(Blocks.titaniumConveyor);
		hideContent(Blocks.armoredConveyor);
		hideContent(Blocks.plastaniumConveyor);
		hideContent(Blocks.junction);
		//hideContent(Blocks.itemBridge);
		hideContent(Blocks.phaseConveyor);
		hideContent(Blocks.sorter);
		hideContent(Blocks.invertedSorter);
		hideContent(Blocks.overflowGate);
		hideContent(Blocks.underflowGate);
		//hideContent(Blocks.massDriver);

		hideContent(Blocks.duct);
		hideContent(Blocks.armoredDuct);
		hideContent(Blocks.ductRouter);
		hideContent(Blocks.overflowDuct);
		hideContent(Blocks.underflowDuct);
		//hideContent(Blocks.ductBridge);
		hideContent(Blocks.ductUnloader);
		hideContent(Blocks.surgeConveyor);
		hideContent(Blocks.surgeRouter);
		hideContent(Blocks.unitCargoLoader);
		hideContent(Blocks.unitCargoUnloadPoint);

		hideContent(Blocks.mechanicalPump);
		hideContent(Blocks.impulsePump);
		hideContent(Blocks.pulseConduit);
		hideContent(Blocks.platedConduit);
		hideContent(Blocks.liquidRouter);
		hideContent(Blocks.liquidJunction);
		hideContent(Blocks.bridgeConduit);
		hideContent(Blocks.phaseConduit);
		hideContent(Blocks.reinforcedConduit);
		hideContent(Blocks.reinforcedLiquidJunction);
		hideContent(Blocks.reinforcedBridgeConduit);
		hideContent(Blocks.reinforcedLiquidRouter);
	}

	private static void adjustVanillaDrill(){
		hideContent(Blocks.mechanicalDrill);
		hideContent(Blocks.laserDrill);
		hideContent(Blocks.blastDrill);
		hideContent(Blocks.eruptionDrill);

		adjustContent(Blocks.pneumaticDrill, content -> {
			Drill drill = (Drill)content;
			drill.requirements = ItemStack.with(Items.copper, 15, Items.lead, 20);
			drill.hardnessDrillMultiplier = 0;
			drill.liquidBoostIntensity = Mathf.sqrt(1.5f);
			drill.drillTime = 60f * drill.size * drill.size;
			drill.drillMultipliers.put(Items.sand, 1f);
			drill.drillMultipliers.put(Items.scrap, 1f);
			drill.drillMultipliers.put(Items.copper, 1f);
			drill.drillMultipliers.put(Items.lead, 1f);
			drill.drillMultipliers.put(Items.coal, 1f);
			drill.drillMultipliers.put(Items.titanium, 0.75f);
			drill.drillMultipliers.put(Items.beryllium, 0.75f);
		});
		adjustContent(Blocks.impactDrill, content -> {
			BurstDrill drill = (BurstDrill)content;
			drill.requirements = ItemStack.with(Items.beryllium, 60, Items.graphite, 45);
			drill.hardnessDrillMultiplier = 0;
			drill.drillTime = 90f * drill.size * drill.size / 6;
			drill.drillMultipliers.put(Items.sand, 1f);
			drill.drillMultipliers.put(Items.scrap, 1f);
			drill.drillMultipliers.put(Items.copper, 1f);
			drill.drillMultipliers.put(Items.lead, 1f);
			drill.drillMultipliers.put(Items.coal, 1f);
			drill.drillMultipliers.put(Items.titanium, 0.75f);
			drill.drillMultipliers.put(Items.beryllium, 0.75f);
			drill.drillMultipliers.put(Items.tungsten, 0.5f);
			drill.removeConsumers(consume -> consume instanceof ConsumeLiquid);
			drill.consume(new ConsumeLiquid(Liquids.water, 10 / 60f).boost());
			drill.liquidBoostIntensity = 1.5f;
		});
		adjustContent(Blocks.plasmaBore, content -> {
			BeamDrill drill = (BeamDrill)content;
			drill.drillTime = 60f;
			drill.optionalBoostIntensity = 2.5f;
		});
		adjustContent(Blocks.largePlasmaBore, content -> {
			BeamDrill drill = (BeamDrill)content;
			drill.drillTime = 30f;
			drill.optionalBoostIntensity = 2.5f;
		});
		adjustContent(Blocks.cliffCrusher, content -> {
			WallCrafter wallCrafter = (WallCrafter)content;
			wallCrafter.drillTime = 60f;
		});
		adjustContent(Blocks.largeCliffCrusher, content -> {
			WallCrafter wallCrafter = (WallCrafter)content;
			wallCrafter.drillTime = 30f;
			wallCrafter.itemBoostIntensity = 2.5f;
			wallCrafter.itemConsumer = wallCrafter.consumeItem(Items.graphite).boost();
		});
	}

	private static void adjustVanillaPower(){
		adjustContent(Blocks.turbineCondenser, content -> {
			ThermalGenerator generator = (ThermalGenerator)content;
			generator.powerProduction = 300 / 60f;
		});
		adjustContent(Blocks.differentialGenerator, content -> {
			ConsumeGenerator generator = (ConsumeGenerator) content;
			generator.itemDuration = 240f;
		});

		hideContent(Blocks.powerNode);
		hideContent(Blocks.powerNodeLarge);
		hideContent(Blocks.surgeTower);
		hideContent(Blocks.beamLink);
		hideContent(Blocks.beamNode);
		hideContent(Blocks.beamTower);
	}
	private static void adjustVanillaTurret(){
		adjustContent(Blocks.swarmer, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new MissileBulletType(){{
				damage= 40;
				rangeChange=40;
				lightningDamage= 10;
				lightning= 3;
				lightningLength = 1;
				lightningLengthRand = 4;
				speed= 3;
				lifetime= 120;
				width= 8;
				height= 8;
				ammoMultiplier= 2;
				lightningColor = hitColor = lightColor = backColor = NHItems.zeta.color;
				frontColor = Color.white;
				trailColor = Color.gray;
				trailParam = 1.8f;
				hitEffect= Fx.blastExplosion;
				shootEffect= Fx.shootSmallFlame;
				splashDamageRadius= 4;
				splashDamage= 15;
				reloadMultiplier = 0.85f;
			}});
		});
		adjustContent(Blocks.salvo, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				lightningColor = trailColor = hitColor = lightColor = backColor = NHItems.zeta.color;
				frontColor = Color.white;
				speed = 6.5f;
				damage= 20;
				rangeChange=40;
				lightningDamage= 10;
				lightning = 1;
				lightningLengthRand = 3;
				reloadMultiplier= 1.5f;
				lifetime= 30f;
				width= 7;
				height= 10;
				ammoMultiplier= 4;
				shootEffect= Fx.shootBig;
				hitEffect = despawnEffect = Fx.none;
			}});
		});
		adjustContent(Blocks.fuse, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new ShrapnelBulletType(){{
				reloadMultiplier = 1.5f;
				rangeChange = 40;
				length = 140;
				damage = 150;
				width = 20;
				lightningColor = trailColor = hitColor = lightColor = fromColor = NHItems.zeta.color;
				toColor = Color.valueOf("ffafaf");
				ammoMultiplier = 2;
				pierce = true;
				shootEffect = new ParticleEffect(){{
					particles= 5;
					line= true;
					length = 55;
					baseLength = 0;
					lifetime = 15;
					colorFrom = fromColor;
					colorTo = toColor;
					cone= 60;
				}};

				smokeEffect = shootEffect;

				fragRandomSpread = 90;
				fragBullets= 2;
				fragBullet = new AdaptedLightningBulletType(){{
					damage = 30;
					lightningColor = trailColor = hitColor = lightColor = NHItems.zeta.color;
					lightningLength = 10;
					lightningLengthRand = 15;
					collidesAir = true;
				}};

				fragOnHit = false;
			}});
		});
		adjustContent(Blocks.ripple, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new ArtilleryBulletType(){{
				damage= 40;
				rangeChange=60;
				lightningDamage= 10;
				lightning= 3;
				lightningLength= 10;
				speed= 3;
				lifetime= 180;
				width= 10;
				height= 20;
				ammoMultiplier= 2;
				lightningColor = trailColor = hitColor = lightColor = backColor = NHItems.zeta.color;
				trailParam = 2.3f;
				frontColor = Color.white;
				hitEffect= Fx.flakExplosionBig;
				shootEffect= Fx.shootSmallFlame;
				splashDamageRadius= 45;
				splashDamage= 60;
			}});
		});
		adjustContent(Blocks.cyclone, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				ammoMultiplier = 2f;
				speed = 6f;
				splashDamage = 40f * 1.5f;
				splashDamageRadius = 38f;
				rangeChange=24;
				lightning = 1;
				lightningLength = 10;
				shootEffect = Fx.shootBig;
				collidesGround = true;
				backColor = hitColor = trailColor= frontColor = NHItems.zeta.color;
				despawnEffect = Fx.hitBulletColor;
			}});
		});
		adjustContent(Blocks.spectre, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				lightningColor = trailColor = hitColor = lightColor = backColor = NHItems.zeta.color;
				frontColor = Color.white;
				speed= 10;
				lifetime= 30;
				rangeChange=40;
				knockback= 1.8f;
				width= 18;
				height= 20;
				damage= 100;
				splashDamageRadius= 20;
				reloadMultiplier = 1.2f;
				splashDamage= 35;
				shootEffect= Fx.shootBig;
				hitEffect= NHFx.hitSpark;
				ammoMultiplier= 2;
				lightningDamage= 30;
				lightning= 1;
				lightningLengthRand = 3;
				lightningLength = 4;
			}});
		});
		adjustContent(Blocks.meltdown, content -> {
			LaserTurret turret = (LaserTurret) content;
			ContinuousLaserBulletType meltDownType = (ContinuousLaserBulletType) turret.shootType;

			meltDownType.length += 80f;
			meltDownType.damage += 20f;
			meltDownType.splashDamage += 10f;
			meltDownType.splashDamageRadius += 10f;

			turret.range += 80f;
			turret.shootDuration += 60f;
		});
		adjustContent(Blocks.breach, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				width = 13f;
				height = 19f;
				hitSize = 7f;
				damage= 100;
				lifetime= 32;
				speed= 8;
				shootEffect = Fx.shootSmallFlame;
				smokeEffect = Fx.shootBigSmoke;
				ammoMultiplier = 1;
				reloadMultiplier = 1f;
				pierceCap = 3;
				pierce = true;
				pierceBuilding = true;
				hitColor = backColor = trailColor = NHItems.zeta.color;
				frontColor = Color.white;
				trailWidth = 2.2f;
				trailLength = 20;
				hitEffect = despawnEffect = Fx.hitBulletColor;
				rangeChange = 56f;
				buildingDamageMultiplier = 0.3f;
				lightningDamage= 30;
				lightning= 1;
				lightningLengthRand = 3;
				lightningLength = 4;
			}});
		});
		adjustContent(Blocks.titan, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new ArtilleryBulletType(2.5f, 500, "shell"){{
				hitEffect = new MultiEffect(Fx.titanExplosionLarge, Fx.titanSmokeLarge, Fx.smokeAoeCloud);
				despawnEffect = Fx.none;
				knockback = 2f;
				lifetime = 190f;
				height = 19f;
				width = 17f;
				reloadMultiplier = 0.65f;
				splashDamageRadius = 110f;
				rangeChange = 8f;
				splashDamage = 300f;
				scaledSplashDamage = true;
				hitColor = backColor = trailColor = NHItems.zeta.color;
				frontColor = NHItems.zeta.color;
				ammoMultiplier = 1f;
				hitSound = Sounds.titanExplosion;

				status = StatusEffects.blasted;

				trailLength = 32;
				trailWidth = 3.35f;
				trailSinScl = 2.5f;
				trailSinMag = 0.5f;
				trailEffect = Fx.vapor;
				trailInterval = 3f;
				despawnShake = 7f;

				shootEffect = Fx.shootTitan;
				smokeEffect = Fx.shootSmokeTitan;

				trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
				shrinkX = 0.2f;
				shrinkY = 0.1f;
				buildingDamageMultiplier = 0.25f;

				lightningDamage= 30;
				lightning= 4;
				lightningLengthRand = 3;
				lightningLength = 15;

				fragBullets = 4;
				fragBullet = new EmptyBulletType(){{
					lifetime = 60f * 3f;
					speed = 0.3f;
					bulletInterval = 20f;
					intervalBullet = new EmptyBulletType(){{
						splashDamage = 60f;
						collidesGround = true;
						collidesAir = false;
						collides = false;
						hitEffect = Fx.none;
						pierce = true;
						instantDisappear = true;
						splashDamageRadius = 90f;
						buildingDamageMultiplier = 0.2f;
						lightningDamage= 10;
						lightning= 2;
						lightningLengthRand = 3;
						lightningLength = 5;
					}};
				}};
			}});
		});
		adjustContent(Blocks.disperse, content -> {
			ItemTurret turret = (ItemTurret) content;
			turret.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				reloadMultiplier = 0.59f;
				damage = 80;
				rangeChange = 8f * 3f;
				lightning = 4;
				lightningLength = 6;
				lightningDamage = 24f;
				lightningLengthRand = 3;
				speed = 6f;
				width = height = 16;
				shrinkY = 0.3f;
				backSprite = "large-bomb-back";
				sprite = "mine-bullet";
				collidesGround = false;
				collidesTiles = false;
				shootEffect = Fx.shootBig2;
				smokeEffect = Fx.shootSmokeDisperse;
				frontColor = Color.white;
				backColor = trailColor = hitColor = NHItems.zeta.color;
				trailChance = 0.44f;
				ammoMultiplier = 3f;

				lifetime = 34f;
				rotationOffset = 90f;
				trailRotation = true;
				trailEffect = Fx.disperseTrail;

				hitEffect = despawnEffect = Fx.hitBulletColor;

				bulletInterval = 3f;

				intervalBullet = new BulletType(){{
					collidesGround = false;
					collidesTiles = false;
					lightningLengthRand = 2;
					lightningLength = 2;
					lightningCone = 30f;
					lightningDamage = 10f;
					lightning = 1;
					hittable = collides = false;
					instantDisappear = true;
					hitEffect = despawnEffect = Fx.none;
				}};
			}});
		});

	}

	private static void adjustVanillaFactories(){
		adjustContent(Blocks.graphitePress, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.consume(new ConsumeItems(with(Items.coal, 2)));
			crafter.outputItems = with(Items.graphite, 3);
			crafter.craftTime = 60f;
		});
		adjustContent(Blocks.multiPress, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.consume(new ConsumeItems(with(Items.coal, 2)));
			crafter.outputItems = with(Items.graphite, 5);
			crafter.craftTime = 40f;
		});
		adjustContent(Blocks.siliconSmelter, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.consume(new ConsumeItems(with(Items.sand, 3)));
			crafter.outputItems = with(Items.silicon, 2);
			crafter.craftTime = 60f;
		});
		adjustContent(Blocks.siliconCrucible, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.requirements = with(Items.titanium, 120, Items.metaglass, 80, Items.silicon, 60);
			crafter.consumeItems(with(Items.sand, 8, Items.pyratite, 2));
			crafter.outputItem = new ItemStack(Items.silicon, 15);
			crafter.craftTime = 120f;
		});
		adjustContent(Blocks.pyratiteMixer, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.consumeItems(with(Items.coal, 1, Items.sand, 2));
			crafter.outputItem = new ItemStack(Items.pyratite, 2);
			crafter.craftTime = 60f;
		});
		adjustContent(Blocks.siliconArcFurnace, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.consume(new ConsumeItems(with(Items.sand, 5)));
			crafter.outputItems = with(Items.silicon, 5);
			crafter.craftTime = 60f;
		});
		adjustContent(Blocks.cultivator, content -> {
        	        AttributeCrafter crafter = (AttributeCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeLiquid);
        		crafter.consume(new ConsumeLiquid(Liquids.water, 18f / 60f));
        		crafter.outputItems = with(Items.sporePod, 2);
   			crafter.craftTime = 60f;
        		crafter.consume(new ConsumePower(6f, 0f, false));

       		});
		adjustContent(Blocks.blastMixer, content -> {
			GenericCrafter crafter = (GenericCrafter)content;
			crafter.removeConsumers(consume -> consume instanceof ConsumeItems);
			crafter.consume(new ConsumeItems(with(Items.sporePod, 3, Items.pyratite, 3)));
			crafter.outputItems = with(Items.blastCompound, 3);
			crafter.craftTime = 90f;
		});
	}

	private static void adjustVanillaUnit(){
		for (UnitType type: content.units()){
			type.envRequired = Env.none;
			type.envDisabled = Env.none;
			type.envEnabled = Env.any;
		}

		hideContent(Blocks.payloadConveyor);
		hideContent(Blocks.payloadRouter);
		hideContent(Blocks.reinforcedPayloadConveyor);
		hideContent(Blocks.reinforcedPayloadRouter);
		hideContent(Blocks.payloadMassDriver);
		hideContent(Blocks.largePayloadMassDriver);
		hideContent(Blocks.deconstructor);
		hideContent(Blocks.smallDeconstructor);
		hideContent(Blocks.constructor);
		hideContent(Blocks.largeConstructor);
		hideContent(Blocks.payloadLoader);
		hideContent(Blocks.payloadUnloader);

		hideContent(Blocks.groundFactory);
		hideContent(Blocks.airFactory);
		hideContent(Blocks.navalFactory);
		hideContent(Blocks.additiveReconstructor);
		hideContent(Blocks.multiplicativeReconstructor);
		hideContent(Blocks.exponentialReconstructor);
		hideContent(Blocks.tetrativeReconstructor);
		hideContent(Blocks.tankFabricator);
		hideContent(Blocks.tankRefabricator);
		hideContent(Blocks.tankAssembler);
		hideContent(Blocks.mechFabricator);
		hideContent(Blocks.mechRefabricator);
		hideContent(Blocks.mechAssembler);
		hideContent(Blocks.shipFabricator);
		hideContent(Blocks.shipRefabricator);
		hideContent(Blocks.shipAssembler);
		hideContent(Blocks.primeRefabricator);
		hideContent(Blocks.basicAssemblerModule);
	}

	private static void buffCoreUnits(){

		adjustContent(Blocks.coreShard, content -> {
			CoreBlock core = (CoreBlock)content;
			core.buildVisibility = BuildVisibility.shown;
			//core.health *= 5;
			core.armor = 5;
		});

		adjustContent(Blocks.coreFoundation, content -> {
			CoreBlock core = (CoreBlock)content;
			//core.health *= 5;
			core.armor = 10;
		});

		adjustContent(Blocks.coreNucleus, content -> {
			CoreBlock core = (CoreBlock)content;
			//core.health *= 5;
			core.armor = 15;
		});

		
		adjustContent(Blocks.coreBastion, content -> {
			CoreBlock core = (CoreBlock)content;
			core.incinerateNonBuildable = false;
			core.requiresCoreZone = false;
		});
		adjustContent(Blocks.coreCitadel, content -> {
			CoreBlock core = (CoreBlock)content;
			core.incinerateNonBuildable = false;
			core.requiresCoreZone = false;
		});
		adjustContent(Blocks.coreAcropolis, content -> {
			CoreBlock core = (CoreBlock)content;
			core.incinerateNonBuildable = false;
			core.requiresCoreZone = false;
		});

		
		adjustContent(UnitTypes.alpha, content -> {
			UnitType unitType = (UnitType)content;
			unitType.mineSpeed = 8f;
			unitType.weapons.each(weapon -> Objects.equals(weapon.name, "small-basic-weapon"), weapon -> {weapon.reload = 15f;});
		});

		adjustContent(UnitTypes.beta, content -> {
			UnitType unitType = (UnitType)content;
			unitType.mineSpeed = 10f;
			unitType.weapons.each(weapon -> Objects.equals(weapon.name, "small-mount-weapon"), weapon -> {weapon.reload = 20f;});
		});

		adjustContent(UnitTypes.gamma, content -> {
			UnitType unitType = (UnitType)content;
			unitType.mineSpeed = 12.5f;
			unitType.weapons.each(weapon -> Objects.equals(weapon.name, "small-mount-weapon"), weapon -> weapon.reload = 16f);
		});
	}

	private static void overrideUnitTypeAbility(){
		for (UnitType type: content.units()){
			if (type.abilities.contains(ability -> ability instanceof PassiveShield)) continue;
			type.abilities.add(new PassiveShield(type.health));
		}
	}

	private static ItemStack[] hugeItemReq(){
		ItemStack[] out = new ItemStack[content.items().size];
		for (int i = 0; i < out.length; i++){
			out[i] = new ItemStack(content.item(i), 114514);
		}
		return out;
	}

	private static void hideContent(UnlockableContent content){
		if (content instanceof Block block){
			block.buildVisibility = BuildVisibility.hidden;
			block.envRequired = Env.none;
			block.requirements = hugeItemReq();
			block.instantDeconstruct = true;
			//todo change the tech tree
		}
	}

	private static void adjustContent(Content content, Cons<Content> modifier){
		modifier.get(content);
	}
}
