package newhorizon.content;

import arc.func.Cons;
import arc.func.Intc;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.ParticleEffect;
import mindustry.game.SpawnGroup;
import mindustry.game.Waves;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.logic.CanvasBlock;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.expand.bullets.AdaptedLightningBulletType;

import java.lang.reflect.Field;
import java.util.Arrays;

import static mindustry.content.UnitTypes.*;

public class NHOverride{
	public static final Seq<SpawnGroup> modSpawnGroup = new Seq<>();
	public static final Seq<Color> validColor = new Seq<>();
	
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
		}//Apply Mod Units
		
//		Seq<UnitType> coreUnits = new Seq<>();
//		Vars.content.blocks().each(b -> {
//			if(b instanceof CoreBlock){
//				CoreBlock c = (CoreBlock)b;
//				coreUnits.add(c.unitType);
//				c.unitType.immunities.add(NHStatusEffects.scannerDown);
//			}
//		});
	}
	
	public static void coreUnits(Cons<UnitType> modifier){
		Vars.content.blocks().each(b -> {
			if(b instanceof CoreBlock){
				CoreBlock c = (CoreBlock)b;
				modifier.get(c.unitType);
			}
		});
	}
	
	public static void loadOptional(){
		//overrideCanvas();
		overrideVanillaMain();
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
		if(NHSetting.enableDetails())Fx.trailFade.clip = 2000;
		
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
			UnitTypes.minke, UnitTypes.oxynoe, NHUnitTypes.thynomo, NHUnitTypes.branch/*, NHUnitTypes.relay*/
		);
		
		ObjectSet<UnitType> Unit_T3 = ObjectSet.with(
			UnitTypes.fortress, UnitTypes.quasar, UnitTypes.spiroct, UnitTypes.zenith, UnitTypes.mega,
			UnitTypes.bryde, UnitTypes.cyerce, NHUnitTypes.aliotiat, NHUnitTypes.warper, NHUnitTypes.ghost,
			NHUnitTypes.rhino, NHUnitTypes.gather
		);
		
		ObjectSet<UnitType> Unit_T4 = ObjectSet.with(
			UnitTypes.scepter, UnitTypes.vela, UnitTypes.arkyid, UnitTypes.antumbra, UnitTypes.quad,
			UnitTypes.sei, UnitTypes.aegires, NHUnitTypes.tarlidor, NHUnitTypes.naxos/*, NHUnitTypes.striker*/, NHUnitTypes.zarkov
		);
		
		ObjectSet<UnitType> Unit_T5 = ObjectSet.with(
			conquer,
			UnitTypes.reign, UnitTypes.toxopid, UnitTypes.eclipse, UnitTypes.oct,
			UnitTypes.omura, UnitTypes.navanax, NHUnitTypes.annihilation, NHUnitTypes.destruction, NHUnitTypes.longinus, NHUnitTypes.declining, NHUnitTypes.saviour
		);
		
		ObjectSet<UnitType> Unit_T6 = ObjectSet.with(
			NHUnitTypes.hurricane, NHUnitTypes.guardian, NHUnitTypes.anvil, NHUnitTypes.sin
		);
		
		Unit_T3.each(u -> u.immunities.addAll(NHStatusEffects.emp1));
		Unit_T4.each(u -> u.immunities.addAll(NHStatusEffects.emp1));
		Unit_T5.each(u -> u.immunities.addAll(NHStatusEffects.emp1, NHStatusEffects.ultFireBurn));
		Unit_T6.each(u -> u.immunities.addAll(NHStatusEffects.scannerDown, NHStatusEffects.scrambler));
		
		new Seq<UnitType>().addAll(Unit_T4.toSeq()).addAll(Unit_T5.toSeq()).retainAll(u -> u != null && !u.name.startsWith(NewHorizon.MOD_NAME)).each(u -> {
			u.armor += 3;
			u.health *= 1.1f;
		});
		
		
		Blocks.coreShard.buildVisibility = BuildVisibility.shown;
		Blocks.coreFoundation.health *= 5;
		Blocks.coreNucleus.health *= 5;
		Blocks.coreShard.health *= 5;
		
		Blocks.coreShard.armor = 5;
		Blocks.coreNucleus.armor = 15f;
		Blocks.coreFoundation.armor = 10f;
		
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

		removeReq(Blocks.ripple, Items.titanium);
		addReq(Blocks.ripple,
			new ItemStack(NHItems.presstanium, 50)
		);

		addReq(Blocks.fuse,
				new ItemStack(NHItems.zeta, 80)
		);
		
		addReq(Blocks.coreNucleus,
				new ItemStack(NHItems.irayrondPanel, 600),
				new ItemStack(NHItems.multipleSteel, 800),
				new ItemStack(NHItems.seniorProcessor, 600)
		);
		addReq(Blocks.surgeSmelter,
				new ItemStack(NHItems.multipleSteel, 35),
				new ItemStack(NHItems.presstanium, 65),
				new ItemStack(NHItems.juniorProcessor, 30),
				new ItemStack(NHItems.metalOxhydrigen, 45)
		);

		((GenericCrafter)Blocks.surgeSmelter).craftTime += 30f;
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
			
			block.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				lightningColor = trailColor = hitColor = lightColor = backColor = NHItems.zeta.color;
				frontColor = Color.white;
				speed= 10;
				lifetime= 30;
				knockback= 1.8f;
				width= 18;
				height= 20;
				damage= 175;
				splashDamageRadius= 38;
				reloadMultiplier = 1.2f;
				splashDamage= 35;
				shootEffect= Fx.shootBig;
				hitEffect= NHFx.hitSpark;
				ammoMultiplier= 2;
				lightningDamage= 50;
				lightning= 1;
				lightningLengthRand = 3;
				lightningLength = 3;
			}});
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
		
		salvo: {
			if(!(Blocks.salvo instanceof ItemTurret))break salvo;
			ItemTurret block = (ItemTurret)Blocks.salvo;
			
			block.ammoTypes.put(NHItems.zeta, new BasicBulletType(){{
				lightningColor = trailColor = hitColor = lightColor = backColor = NHItems.zeta.color;
				frontColor = Color.white;
				speed = 6.5f;
				damage= 20;
				lightningDamage= 10;
				lightning = 1;
				lightningLengthRand = 3;
				reloadMultiplier= 1.5f;
				lifetime= 30.76f;
				width= 7;
				height= 10;
				ammoMultiplier= 4;
				shootEffect= Fx.shootBig;
				hitEffect = despawnEffect = Fx.none;
			}});
		}
		
		fuse: {
			if(!(Blocks.fuse instanceof ItemTurret))break fuse;
			ItemTurret block = (ItemTurret)Blocks.fuse;
			
			block.ammoTypes.put(NHItems.zeta, new ShrapnelBulletType(){{
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
					lightningLength = 5;
					lightningLengthRand = 15;
					collidesAir = true;
				}};
				
				fragOnHit = false;
			}
				
				@Override
				public void despawned(Bullet b){}
				
				@Override
				public void init(Bullet b){
					super.init(b);
					
					createFrags(b, b.x, b.y);
				}
			});
		}
		
		swarmer:{
			if(!(Blocks.swarmer instanceof ItemTurret)) break swarmer;
			ItemTurret block = (ItemTurret)Blocks.swarmer;
			
			block.ammoTypes.put(NHItems.zeta, new MissileBulletType(){{
				damage= 60;
				rangeChange=40;
				lightningDamage= 15;
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
				splashDamageRadius= 5;
				splashDamage= 25;
				reloadMultiplier = 0.85f;
			}});
		}
		
		ripple:{
			if(!(Blocks.ripple instanceof ItemTurret)) break ripple;
			ItemTurret block = (ItemTurret)Blocks.ripple;
			
			block.ammoTypes.put(NHItems.zeta, new ArtilleryBulletType(){{
				damage= 60;
				rangeChange=40;
				lightningDamage= 15;
				lightning= 3;
				lightningLength= 6;
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
				splashDamage= 75;
			}});
			
		}
		
		removeReq(Blocks.meltdown, Items.silicon);

		addReq(Blocks.foreshadow,
			new ItemStack(NHItems.seniorProcessor, 80)
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
	
	public static Seq<SpawnGroup> generate(float difficulty){
		//apply power curve to make starting sectors easier
		return generate(Mathf.pow(difficulty, 1.12f), new Rand(), false);
	}
	
	public static Seq<SpawnGroup> generate(float difficulty, Rand rand, boolean attack){
		return generate(difficulty, rand, attack, false);
	}
	
	public static Seq<SpawnGroup> generate(float difficulty, Rand rand, boolean attack, boolean airOnly){
		return generate(difficulty, rand, attack, airOnly, false);
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
