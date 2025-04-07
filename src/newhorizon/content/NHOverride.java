package newhorizon.content;

import arc.func.Cons;
import arc.func.Intc;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.game.SpawnGroup;
import mindustry.game.Team;
import mindustry.game.Waves;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.production.BurstDrill;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import newhorizon.NHSetting;
import newhorizon.content.bullets.VanillaOverrideBullets;
import newhorizon.expand.ability.passive.PassiveShield;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static mindustry.Vars.content;
import static mindustry.Vars.defaultEnv;
import static mindustry.content.UnitTypes.*;

public class NHOverride{
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
	}

	public static void contentOverride(){
		setModContentEnv();
		overrideUnitTypeAbility();
		balanceDrill();
		buffCoreUnits();
		adjustUnit();

		overrideStats();
	}

	public static void setModContentEnv(){
		//Events.on(EventType.WorldLoadEvent.class, e -> {
		//	state.rules.bannedBlocks.each(b -> {
		//		if (b.name.startsWith("new-horizon")) {
		//			state.rules.bannedBlocks.remove(b);
		//		}
		//	});
		//});
	}

	public static void overrideVanillaMain(){
		replaceVanillaVisualContent();
		replaceVanillaSpawnGroup();

		//Icon.icons.put("nhIcon0", new TextureRegionDrawable(NHContent.icon));
		//todo set the icon
		//Team.blue.name = "ancient";
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
		
		out.add(NHOverride.modSpawnGroup);
		
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
			if (block instanceof ItemTurret itemTurret){
				block.checkStats();
				var map = block.stats.toMap();
				if (map.get(StatCat.function) != null && map.get(StatCat.function).get(Stat.ammo) != null){
					block.stats.remove(Stat.ammo);
					block.stats.add(Stat.ammo, NHStatValues.ammo(itemTurret.ammoTypes, 0, false));
				}
			}
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

	private static void hideVanillaContent(){
		hideContent(Blocks.mechanicalDrill);
		hideContent(Blocks.laserDrill);
		hideContent(Blocks.blastDrill);
		hideContent(Blocks.mechanicalPump);
		hideContent(Blocks.impulsePump);
	}

	public static void balanceDrill(){
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
			drill.drillTime = 60f * drill.size * drill.size / 6;
			drill.drillMultipliers.put(Items.sand, 1f);
			drill.drillMultipliers.put(Items.scrap, 1f);
			drill.drillMultipliers.put(Items.copper, 1f);
			drill.drillMultipliers.put(Items.lead, 1f);
			drill.drillMultipliers.put(Items.coal, 1f);
			drill.drillMultipliers.put(Items.titanium, 0.75f);
			drill.drillMultipliers.put(Items.beryllium, 0.75f);
			drill.drillMultipliers.put(Items.tungsten, 0.5f);
		});

	}

	public static void buffCoreUnits(){

		adjustContent(Blocks.coreShard, content -> {
			CoreBlock core = (CoreBlock)content;
			core.buildVisibility = BuildVisibility.shown;
			//core.health *= 5;
			core.armor = 5;
		});

		adjustContent(UnitTypes.alpha, content -> {
			UnitType unitType = (UnitType)content;
			unitType.mineSpeed = 8f;
			unitType.weapons.each(weapon -> Objects.equals(weapon.name, "small-basic-weapon"), weapon -> {
				weapon.reload = 15f;
				weapon.bullet = VanillaOverrideBullets.alpha0;
			});
		});

		adjustContent(Blocks.coreFoundation, content -> {
			CoreBlock core = (CoreBlock)content;
			//core.health *= 5;
			core.armor = 10;
		});

		adjustContent(UnitTypes.beta, content -> {
			UnitType unitType = (UnitType)content;
			unitType.mineSpeed = 10f;
			unitType.weapons.each(weapon -> Objects.equals(weapon.name, "small-mount-weapon"), weapon -> {
				weapon.reload = 20f;
				weapon.bullet = VanillaOverrideBullets.beta0;
			});
		});

		adjustContent(Blocks.coreNucleus, content -> {
			CoreBlock core = (CoreBlock)content;
			//core.health *= 5;
			core.armor = 15;
		});

		adjustContent(UnitTypes.gamma, content -> {
			UnitType unitType = (UnitType)content;
			unitType.mineSpeed = 12.5f;
			unitType.weapons.each(weapon -> Objects.equals(weapon.name, "small-mount-weapon"), weapon -> {
				weapon.reload = 16f;
				weapon.bullet = VanillaOverrideBullets.gamma0;
			});
		});
	}

	public static void balanceVanillaTurret(){

	}

	private static void overrideUnitTypeAbility(){
		for (UnitType type: content.units()){
			if (type.abilities.contains(ability -> ability instanceof PassiveShield)) continue;
			type.abilities.add(new PassiveShield(type.health));
		}
	}

	private static void adjustUnit(){
		for (UnitType type: content.units()){
			type.envRequired = Env.none;
			type.envDisabled = Env.none;
			type.envEnabled = Env.any;
		}

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
			block.envRequired = Env.any;
			block.requirements = hugeItemReq();
			block.instantDeconstruct = true;
			//todo change the tech tree
		}
	}

	private static void adjustContent(Content content, Cons<Content> modifier){
		modifier.get(content);
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
