package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.PointLaserBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.*;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.AttributeCrafter;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.sandbox.PowerVoid;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.content.blocks.*;
import newhorizon.expand.block.adapt.AdaptUnloader;
import newhorizon.expand.block.adapt.AssignOverdrive;
import newhorizon.expand.block.adapt.MultiCrafter;
import newhorizon.expand.block.ancient.CaptureableTurret;
import newhorizon.expand.block.ancient.wave.WaveConveyor;
import newhorizon.expand.block.ancient.wave.WaveGenerator;
import newhorizon.expand.block.commandable.AirRaider;
import newhorizon.expand.block.commandable.BombLauncher;
import newhorizon.expand.block.defence.*;
import newhorizon.expand.block.distribution.*;
import newhorizon.expand.block.drawer.*;
import newhorizon.expand.block.env.ArmorFloor;
import newhorizon.expand.block.special.*;
import newhorizon.expand.block.turrets.MultTractorBeamTurret;
import newhorizon.expand.block.turrets.ShootMatchTurret;
import newhorizon.expand.block.turrets.SpeedupTurret;
import newhorizon.expand.block.turrets.Webber;
import newhorizon.expand.bullets.*;
import newhorizon.expand.game.NHPartProgress;
import newhorizon.expand.game.NHUnitSorts;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;

public class NHBlocks{

	//Load Mod Factories

	public static Block
		tagger,
		
		reinForcedItemSource, reinForcedLiquidSource,
		//delivery,
		oreZeta, xenMelter, hyperGenerator, fusionCollapser,
		chargeWall, chargeWallLarge,
		
		jumpGate, jumpGateJunior, jumpGatePrimary,
	
		ventExtractor, processorCompactor,
		multiplePresstaniumFactory, presstaniumFactory, seniorProcessorFactory, juniorProcessorFactory, multipleSurgeAlloyFactory,
		zetaFactoryLarge, zetaFactorySmall, fusionEnergyFactory, multipleSteelFactory, irayrondPanelFactory, reconstructPlastaniumFactory,
		setonAlloyFactory, darkEnergyFactory, upgradeSortFactory, metalOxhydrigenFactory, metalOxhydrigenFactoryLarge,
		sandCracker,
		thermoCorePositiveFactory, thermoCoreNegativeFactory, thermoCoreFactory, irdryonVault, arkyciteCompactor,
	
		//Ancient
		ancimembraneConcentrator, ancientArtillery,
	
		//Wave Energy
		waveEnergyConveyor, ancitentWaveEnergyConveyor, waveEnergyGenerator,
	
		//Turrets
		dendrite, interferon, prism, hive, concentration,
		shockWaveTurret, usualUpgrader, bloodStar, pulseShotgun, beamLaserTurret,
		blaster, endOfEra, thurmix, argmot, thermoTurret, railGun, divlusion, executor, empTurret, gravity, multipleLauncher, antibody, multipleArtillery,
		antiMatterTurret, atomSeparator, eternity, synchro,

		//Liquids
		irdryonTank,
		//Liquids factories
		irdryonFluidFactory, xenBetaFactory, xenGammaFactory, zetaFluidFactory, oilRefiner, waterInstancer,
		//walls
		insulatedWall, setonWall, setonWallLarge, heavyDefenceWall, heavyDefenceWallLarge, heavyDefenceDoor, heavyDefenceDoorLarge, laserWall, ancientLaserWall,
		//Distributions
		towardGate, rapidUnloader, liquidAndItemBridge, remoteRouter, multiArmorConveyor, multiConveyor, multiEfficientConveyor,
		multiJunction, multiRouter, multiConduit, multiSteelItemBridge, multiSteelLiquidBridge,
		//Drills
		largeWaterExtractor, //beamDrill,
		//Powers
		hydroFuelCell, ancientPowerNode,
		armorPowerNode, armorBatteryLarge, radiationGenerator, zetaGenerator, hugeBattery, heavyPowerNode,
		//Defence
		largeMendProjector, assignOverdrive, antiBulletTurret, largeShieldGenerator, fireExtinguisher, webber,
		//Special
		playerJumpGate, gravityTrap, hyperspaceWarper, bombLauncher, scrambler, airRaider, configurer, shieldProjector, unitIniter, remoteStorage,
		disposePowerVoid, gravityTrapSmall, lableSpawner,
	
		//Env
		armorClear, armorLight, armorQuantum, armorAncient,
		quantumField, quantumFieldDeep, quantumFieldDisturbing, metalWall, metalWallQuantum, metalTower, metalGround, metalGroundQuantum, metalScarp, metalVent,
		metalGroundHeat, conglomerateRock, conglomerateWall
		;
	
	private static void loadEnv(){
		metalScarp = new Prop("metal-scrap"){{
			variants = 3;
			breakEffect = new Effect(23, e -> {
				float scl = Math.max(e.rotation, 1);
				Fx.rand.setSeed(e.id);
				randLenVectors(e.id, 6, 19f * e.finpow() * scl, (x, y) -> {
					color(Tmp.c1.set(e.color).mul(1 + Fx.rand.range(0.125f)));
					Fill.square(e.x + x, e.y + y, e.fout() * 3.5f * scl + 0.3f);
				});
			}).layer(Layer.debris);
			breakSound = NHSounds.metalWalk;
		}};
		
		metalWall = new StaticWall("metal-unit-quantum"){{
			variants = 6;
		}};
		
		metalWallQuantum = new StaticWall("metal-unit"){{
			variants = 6;
		}};
		
		Cons<Floor> quantumSetter = f -> {
			f.wall = metalWall;
			f.attributes.set(Attribute.water, -1f);
			f.attributes.set(Attribute.oil, -1f);
			f.attributes.set(Attribute.heat, 0);
			f.attributes.set(Attribute.light, 0);
			f.attributes.set(Attribute.spores, -1f);
			f.walkSound = NHSounds.metalWalk;
			f.walkSoundVolume = 0.05f;
			f.speedMultiplier = 1.25f;
			f.decoration = metalScarp;
		};
		
		armorClear = new ArmorFloor("armor-clear", 0){{
			quantumSetter.get(this);
			blendGroup = this;
		}};
		
		armorAncient = new ArmorFloor("armor-ancient", 9){{
			quantumSetter.get(this);
			lightRadius = 16f;
			lightColor = NHColor.ancientLightMid.cpy().a(0.7f);
			attributes.set(Attribute.light, 1);
			attributes.set(Attribute.heat, 1);
			speedMultiplier = 2f;
			emitLight = true;
		}};
		
		armorQuantum = new ArmorFloor("armor-quantum", 2, armorClear.asFloor()){{
			quantumSetter.get(this);
			lightColor = NHColor.darkEnrColor.cpy().a(0.7f);
			lightRadius = 7f;
			emitLight = true;
		}};
		
		armorLight = new ArmorFloor("armor-light", 3, armorClear.asFloor()){{
			useDynamicLight = true;
			lightRadius = 7f;
			quantumSetter.get(this);
			emitLight = true;
		}};
		
		conglomerateWall = new StaticWall("conglomerate-wall"){{
			variants = 4;
		}};
		
		conglomerateRock = new Floor("conglomerate-rock", 3){{
			blendGroup = Blocks.stone;
		}};
		
		metalGroundHeat = new Floor("metal-ground-heat", 3){{
			wall = metalWall;
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.heat, 1.25f);
			attributes.set(Attribute.light, 1f);
			attributes.set(Attribute.spores, -1f);
			walkSound = NHSounds.metalWalk;
			walkSoundVolume = 0.05f;
			speedMultiplier = 1.25f;
			
			liquidMultiplier = 0.8f;
			liquidDrop = NHLiquids.quantumEntity;
			lightColor = NHColor.darkEnrColor;
			emitLight = true;
			lightRadius = 35f;
			
			decoration = metalScarp;
		}};
		
		quantumField = new Floor("quantum-field", 8){{
			status = NHStatusEffects.quantization;
			statusDuration = 60f;
			speedMultiplier = 1.15f;
			liquidDrop = NHLiquids.quantumEntity;
			isLiquid = true;
			cacheLayer = CacheLayer.water;
			attributes.set(Attribute.light, 2f);
			emitLight = true;
			lightRadius = 32f;
			lightColor = NHColor.darkEnrColor.cpy().lerp(Color.black, 0.1f);
			blendGroup = this;
			
			attributes.set(Attribute.heat, 0.05f);
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.spores, -1f);
			
//			cacheLayer = NHContent.quantum;
		}};
		
		quantumFieldDeep = new Floor("quantum-field-deep", 0){{
			drownTime = 180f;
			status = NHStatusEffects.quantization;
			statusDuration = 240f;
			speedMultiplier = 1.3f;
			liquidDrop = NHLiquids.quantumEntity;
			isLiquid = true;
			cacheLayer = CacheLayer.water;
			attributes.set(Attribute.light, 3f);
			emitLight = true;
			lightRadius = 40f;
			liquidMultiplier = 2f;
			lightColor = NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f);
			blendGroup = this;
			
			attributes.set(Attribute.heat, 0.15f);
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.spores, -1f);
			
//			cacheLayer = NHContent.quantum;
		}};
		
		quantumFieldDisturbing = new Floor("quantum-field-disturbing", 0){{
			drownTime = 180f;
			status = NHStatusEffects.quantization;
			statusDuration = 240f;
			speedMultiplier = 1.3f;
			liquidDrop = NHLiquids.quantumEntity;
			isLiquid = true;
			attributes.set(Attribute.light, 3f);
			emitLight = true;
			lightRadius = 40f;
			liquidMultiplier = 2f;
			lightColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.2f);
			blendGroup = this;
			
			wall = NHBlocks.metalWall;
			
			attributes.set(Attribute.heat, 0.25f);
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.spores, -1f);
			
			cacheLayer = NHContent.quantumLayer;
			
			details = "Has unique shader.";
		}
			
			@Override
			public void load(){
				super.load();
				
				editorIcon = fullIcon = uiIcon = region = Core.atlas.find(NewHorizon.name("quantum-field-disturbing-icon"));
			}
		};
		
		metalTower = new StaticWall("metal-tower"){{
			variants = 3;
			layer = Layer.blockOver + 1;
		}};
		
		metalGround = new Floor("metal-ground", 6){{
			mapColor = Pal.darkerGray;
			quantumSetter.get(this);
		}};
		
		metalVent = new SteamVent("metal-vent"){{
			variants = 2;
			parent = blendGroup = NHBlocks.metalGround;
			attributes.set(NHContent.quantum, 1f);
			
			effectColor = Pal.darkerMetal;
			
			effect = new OptionalMultiEffect(new Effect(140f, e -> {
				color(e.color, NHColor.darkEnr, e.fin() * 0.86f);

				Draw.alpha(e.fslope() * 0.78f);

				float length = 3f + e.finpow() * 10f;
				Fx.rand.setSeed(e.id);
				for(int i = 0; i < Fx.rand.random(3, 5); i++){
					Fx.v.trns(Fx.rand.random(360f), Fx.rand.random(length));
					Fill.circle(e.x + Fx.v.x, e.y + Fx.v.y, Fx.rand.random(1.2f, 3.5f) + e.fslope() * 1.1f);
				}
			}).layer(Layer.darkness - 1));
		}};
		
		metalGroundQuantum = new Floor("metal-ground-quantum", 2){{
			mapColor = Pal.darkerMetal;
			wall = metalWall;
			blendGroup = metalGround;
			attributes.set(Attribute.heat, 0.2f);
			
			quantumSetter.get(this);
			wall = metalWallQuantum;
			
			emitLight = true;
			lightColor = NHColor.darkEnrColor;
			lightRadius = 4.4f;
		}
			@Override
			public void load(){
				super.load();
				region = Core.atlas.find(NewHorizon.name("metal-ground1"));
			}
		};
	}
	
	private static void loadExperiments(){
		waveEnergyGenerator = new WaveGenerator("wave-energy-generator"){{
			size = 3;
			
			requirements(Category.power, with(NHItems.presstanium, 150, NHItems.multipleSteel, 100, NHItems.juniorProcessor, 120));
			hasItems = hasLiquids = hasPower = true;
			
			itemCapacity = 20;
			liquidCapacity = 40;
			consumePower(5f);
			consumeItem(NHItems.zeta, 5);
			squareSprite = false;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
				midColor = flameColor = NHColor.darkEnrColor;
				circleStroke = 1.125f;
				circleSpace = 1.9f;
			}}, new DrawDefault());
		}};
		
		waveEnergyConveyor = new WaveConveyor("wave-conveyor"){{
			size = 1;
			
			requirements(Category.power, with(NHItems.presstanium, 150, NHItems.multipleSteel, 100, NHItems.juniorProcessor, 120));
		}};
		
		ancitentWaveEnergyConveyor = new WaveConveyor("ancient-wave-conveyor"){{
			size = 1;
			
			armored = true;
			
			requirements(Category.power, with(NHItems.presstanium, 150, NHItems.multipleSteel, 100, NHItems.juniorProcessor, 120));
		}};
	}
	
	private static void loadTurrets(){
		concentration = new ContinuousTurret("concentration"){{
			requirements(Category.turret, with(Items.carbide, 50, Items.tungsten, 200, NHItems.setonAlloy, 150, NHItems.seniorProcessor, 75));

			shootType = new PointLaserBulletType(){{
				damage = 100f;
				hitEffect = NHFx.hitSpark;
				beamEffect = Fx.none;
				beamEffectInterval = 0;
				buildingDamageMultiplier = 0.75f;
				damageInterval = 1;
				color = hitColor = Pal.techBlue;
				sprite = "laser-white";
				status = NHStatusEffects.emp3;
				statusDuration = 60;
				oscScl /= 1.77f;
				oscMag /= 1.33f;
				hitShake = 2;
				range = 75 * 8;
				
				trailLength = 8;
			}
				
				private final Color tmpColor = new Color();
				private final Color from = color, to = NHColor.darkEnrColor;
				private final static float chargeReload = 65f;
				private final float maxDamageMultiplier = 1.5f;
				
				private final static float lerpReload = 10f;
				
				private boolean charged(Bullet b){
					return b.fdata > chargeReload;
				}
				
				private Color getColor(Bullet b){
					return tmpColor.set(from).lerp(to, warmup(b));
				}
				
				private float warmup(Bullet b){
					return Mathf.curve(b.fdata, chargeReload - lerpReload / 2f, chargeReload + lerpReload / 2f);
				}
				
				@Override
				public void update(Bullet b){
					super.update(b);
					
					b.damage = damage * (1 + warmup(b) * maxDamageMultiplier);
					
					boolean cool = false;
					
					if(b.data == null)cool = true;
					else if(b.data instanceof Healthc){
						Healthc h = (Healthc)b.data;
						if(!h.isValid() || !h.within(b.aimX, b.aimY, ((Sized)h).hitSize() + 4)){
							b.data = null;
							cool = true;
						}
					}
					
					if(cool){
						b.fdata = Mathf.approachDelta(b.fdata, 0, 1);
					}else b.fdata = Math.min(b.fdata, chargeReload + lerpReload / 2f + 1f);
					
					if(charged(b)){
						if(!Vars.headless && b.timer(3, 3)){
							PosLightning.createEffect(b, Tmp.v1.set(b.aimX, b.aimY), getColor(b), 1, 2);
							if(Mathf.chance(0.25)) NHFx.hitSparkLarge.at(b.x, b.y, tmpColor);
						}
						
						if(b.timer(4, 2.5f)){
							Lightning.create(b, getColor(b), b.damage() / 2f, b.aimX, b.aimY, b.rotation() + Mathf.range(34f), Mathf.random(5, 12));
						}
					}
				}
				
				@Override
				public void draw(Bullet b){
					float darkenPartWarmup = warmup(b);
					float stroke =  b.fslope() * (1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag)) * (darkenPartWarmup + 1) * 5;
					
					if(trailLength > 0 && b.trail != null){
						float z = Draw.z();
						Draw.z(z - 0.0001f);
						b.trail.draw(getColor(b), stroke);
						Draw.z(z);
					}
					
					Draw.color(getColor(b));
					DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke);
					Draw.color(Color.white);
					DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke * 0.64f * (2 + darkenPartWarmup) / 3f);
					
					Drawf.light(b.aimX, b.aimY, b.x, b.y, stroke, tmpColor, 0.76f);
					Drawf.light(b.x, b.y, stroke * 4, tmpColor, 0.76f);
					Drawf.light(b.aimX, b.aimY, stroke * 3, tmpColor, 0.76f);
					
					Draw.color(tmpColor);
					if(charged(b)){
						float qW = Mathf.curve(warmup(b), 0.5f, 0.7f);
						
						for(int s : Mathf.signs){
							Drawf.tri(b.x, b.y, 6, 21 * qW, 90 * s + Time.time * 1.8f);
						}
						
						for(int s : Mathf.signs){
							Drawf.tri(b.x, b.y, 7.2f, 25 * qW, 90 * s + Time.time * -1.1f);
						}
					}
					
					if(NHSetting.enableDetails()){
						int particles = 44;
						float particleLife = 74f;
						float particleLen = 7.5f;
						Rand rand = NHFunc.rand(b.id);
						
						float dst = Mathf.dst(b.x, b.y, b.aimX, b.aimY);
						
						float base = (Time.time / particleLife);
						for(int i = 0; i < particles; i++){
							float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin, fslope = NHFx.fslope(fin);
							float len = rand.random(particleLen * 0.7f, particleLen * 1.3f) * Mathf.curve(fin, 0.2f, 0.9f) * (darkenPartWarmup / 2.5f + 1);
							float centerDeg = rand.random(Mathf.pi);
							
							Tmp.v1.trns(b.rotation(), Interp.pow3In.apply(fin) * rand.random(44, 77) - rand.range(11) - 8, (((rand.random(22, 35) * (fout + 1) / 2 + 2) / (3 * fin / 7 + 1.3f) - 1) + rand.range(4)) * Mathf.cos(centerDeg));
							float angle = Mathf.slerp(Tmp.v1.angle() - 180, b.rotation(), Interp.pow2Out.apply(fin));
							Tmp.v1.scl(darkenPartWarmup / 3.7f + 1);
							Tmp.v1.add(b);
							
							Draw.color(Tmp.c2.set(tmpColor), Color.white, fin * 0.7f);
							Lines.stroke(Mathf.curve(fslope, 0, 0.42f) * 1.4f * b.fslope() * Mathf.curve(fin, 0, 0.6f));
							Lines.lineAngleCenter(Tmp.v1.x, Tmp.v1.y, angle, len);
						}
					}
					
					if(darkenPartWarmup > 0.005f){
						tmpColor.lerp(Color.black, 0.86f);
						Draw.color(tmpColor);
						DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke * 0.55f * darkenPartWarmup);
						Draw.z(NHFx.EFFECT_BOTTOM);
						DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke * 0.6f * darkenPartWarmup);
						Draw.z(Layer.bullet);
					}
					
					Draw.reset();
				}
				
				@Override
				public void hit(Bullet b, float x, float y){
					if(Mathf.chance(0.4))hitEffect.at(x, y, b.rotation(), getColor(b));
					hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
					
					Effect.shake(hitShake, hitShake, b);
					
					if(fragOnHit){
						createFrags(b, x, y);
					}
				}
				
				@Override
				public void hitEntity(Bullet b, Hitboxc entity, float health){
					if(entity instanceof Healthc){
						Healthc h = (Healthc)entity;
						if(charged(b)){
							h.damagePierce(b.damage);
						}else{
							h.damage(b.damage);
						}
					}
					
					if(charged(b) && entity instanceof Unit){
						Unit unit = (Unit)entity;
						unit.apply(status, statusDuration);
					}
					
					if(entity == b.data)b.fdata += Time.delta;
					else b.fdata = 0;
					b.data = entity;
				}
				
				@Override
				public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
					super.hitTile(b, build, x, y, initialHealth, direct);
					if(build == b.data)b.fdata += Time.delta;
					else b.fdata = 0;
					b.data = build;
				}
			};
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-charger"){{
					mirror = true;
					under = true;
					moveRot = 10;
					moveX = 4.677f;
					moveY = 6.8f;
				}});
				parts.add(new RegionPart("-side"){{
					mirror = true;
					under = true;
					moveRot = 10;
					moveX = 2.75f;
					moveY = 2;
				}});
				parts.add(new RegionPart("-barrel"){{
					moveY = -7.5f;
					progress = progress.curve(Interp.pow2Out);
				}});
			}};
			
			shootSound = Sounds.none;
			loopSoundVolume = 1f;
			loopSound = NHSounds.largeBeam;
			
			shootWarmupSpeed = 0.08f;
			shootCone = 360f;
			
			aimChangeSpeed = 1.75f;
			rotateSpeed = 1.45f;
			canOverdrive = false;
			
			shootY = 16f;
			minWarmup = 0.8f;
			warmupMaintainTime = 45;
			shootWarmupSpeed /= 2;
			outlineColor = Pal.darkOutline;
			size = 5;
			range = 75 * 8f;
			scaledHealth = 300;
			armor = 10;
			
			unitSort = NHUnitSorts.slowest;
			
			consumePower(16);
			consumeLiquid(NHLiquids.xenBeta, 12f / 60f);
		}};

		multipleArtillery = new ShootMatchTurret("multiple-artillery"){{
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-mid"){{
					moveY = -3;
					
					layerOffset = 0.01f;
					
					outline = true;
					
					progress = PartProgress.recoil;
				}});
				parts.add(new RegionPart("-barrel"){{
					moveX = 5.5f;
					
					layerOffset = 0.01f;
					
					mirror = outline = true;
					
					moves.add(new PartMove(PartProgress.recoil, 0, -4, 0, 0, 0));
				}});
			}};
			
			recoil = 0.74f;
			velocityRnd = 0.088f;
			lifeRnd = 0.088f;
			
			reload = 72f;
			
			rotateSpeed = 1.22f;
			unitSort = NHUnitSorts.slowest;
			
			shoot = new ShootBarrel(){{
				barrels = new float[]{
					 5f, -3f, 0,
					-5f, -3f, 0,
					 11f, -2f, 0,
					-11f, -2f, 0,
				};
				
				shots = 4;
			}};
			
			range = 640;
			
			minWarmup = 0.9f;
			shootWarmupSpeed /= 2;
			inaccuracy = 2;
			shootY -= 5;
			shootSound = Sounds.largeCannon;
			
			ammo(
				NHItems.metalOxhydrigen, NHBullets.artilleryHydro,
				NHItems.multipleSteel, NHBullets.artilleryMulti,
				NHItems.thermoCoreNegative, NHBullets.artilleryNgt,
				NHItems.fusionEnergy, NHBullets.artilleryFusion,
				Items.phaseFabric, NHBullets.artilleryPhase
			);
			
			shooter(Items.phaseFabric, new ShootBarrel(){{
				barrels = new float[]{
						5f, -3f, 0,
						11f, -2f, 0,
						-11f, -2f, 0,
						-5f, -3f, 0,
				};
				
				barrelOffset = 1;
				shots = 2;
			}});
			
			targetAir = false;
			maxAmmo = 80;
			
			size = 4;
			health = 2600;
			outlineColor = Pal.darkOutline;
			
			squareSprite = false;
			
			coolantMultiplier /= 2;
			coolant = new ConsumeCoolant(0.6f);
			liquidCapacity = 90;
			
			requirements(Category.turret, with(NHItems.multipleSteel, 300, NHItems.seniorProcessor, 90, Items.plastanium, 120, NHItems.presstanium, 200, Items.phaseFabric, 100));
		}};
		
		hive = new ShootMatchTurret("hive"){{
			size = 4;
			health = 3200;
			armor = 10;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 300, NHItems.seniorProcessor, 60, NHItems.juniorProcessor, 120, NHItems.presstanium, 200, Items.graphite, 150));
			outlineColor = Pal.darkOutline;
			
			warmupMaintainTime = 90f;
			shootWarmupSpeed /= 2f;
			
			reload = 150;
			targetGround = false;
			
			range = 8 * 70;
			
			xRand = 6f;
			velocityRnd = 0.105f;
			
			shootY = 8;
			
			rotateSpeed = 3;
			cooldownTime = 60f;
			recoil = 1.25f;
			
			Color heatC = Pal.turretHeat.cpy().lerp(Color.red, 0.33f);
			heatColor = heatC;
			
			shake = 1.1f;
			shootSound = Sounds.missileLarge;
			
			shoot = new AdaptedShootHelix(){{
				flip = true;
				shots = 10;
				mag = 1.65f;
				scl = 6f;
				shotDelay = 3.5f;
				offset = 9.75f * Mathf.PI2;
				rotSpeedOffset = 0.015f;
				rotSpeedBegin = 0.925f;
			}};
			
			canOverdrive = true;
			
			ammo(NHItems.juniorProcessor, new AccelBulletType(5.2f, 75, NHBullets.STRIKE){{
				width = 7f;
				height = 13f;
				shrinkY = 0f;
				
				collideFloor = collidesGround = collidesTiles = false;
				ammoMultiplier = 8f;
				backColor = lightningColor = hitColor = Pal.bulletYellowBack;
				lightColor = frontColor = Pal.bulletYellow;
				splashDamageRadius = 12f;
				splashDamage = damage / 3;
				despawnEffect = Fx.smoke;
				hitEffect = NHFx.hitSpark;
				lifetime = 100f;
				lightningDamage = damage / 2;
				lightning = 2;
				lightningLength = 3;
				lightningLengthRand = 12;
				
				status = NHStatusEffects.emp2;
				statusDuration = 180f;
				
				hitSound = despawnSound = Sounds.dullExplosion;
				hitSoundVolume = 0.6f;
				hitSoundPitch -= 0.11f;
				hitShake = 1.1f;
				
				shootEffect = EffectWrapper.wrap(Fx.shootBigSmoke2, 180, true);
				smokeEffect = NHFx.hugeSmokeGray;
				lightningType = NHBullets.lightningAir;
				
				inaccuracy = 0.3f;
				
				weaveMag = 3f;
				weaveScale = 3.55f;
				homingDelay = 5f;
				homingPower = 0.25f;
				homingRange = 160f;
				
				velocityBegin = 1.4f;
				velocityIncrease = 8f;
				accelerateBegin = 0.005f;
				accelerateEnd = 0.75f;
				
				trailColor = NHColor.trail;
				trailWidth = 1f;
				trailLength = 15;
			}},
					NHItems.seniorProcessor, new AccelBulletType(5.2f, 200, NHBullets.STRIKE){{
				width = 14f;
				height = 28f;
				shrinkY = 0f;
				lightningType = NHBullets.lightningAir;
				
				collideFloor = collidesGround = collidesTiles = false;
				ammoMultiplier = 2f;
				backColor = lightningColor = hitColor = Pal.bulletYellowBack;
				lightColor = frontColor = Pal.bulletYellow;
				splashDamageRadius = 64f;
				splashDamage = damage / 5;
				scaledSplashDamage = true;
				despawnEffect = NHFx.blast(backColor, 64f);
				lifetime = 142f;
				hitEffect = NHFx.subEffect(90F, 120F, 18, 60F, Interp.pow2Out, (i, x, y, rot, fin) -> {
					float fout = 1 - fin;
					Draw.color(hitColor, Color.white, fin * 0.7f);
					float f = NHFunc.rand(i).random(5, 8) * Mathf.curve(fin, 0, 0.1f) * fout;
					Fill.circle(x, y, f);
					Drawf.light(x, y, f * 1.25f, hitColor, 0.7f);
				});
				lightningDamage = damage / 2;
				lightning = 4;
				lightningLength = 12;
				lightningLengthRand = 18;
				
				status = NHStatusEffects.emp3;
				statusDuration = 640f;
				
				rangeChange = 80;
				
				reloadMultiplier = 0.25f;
				
				hitSound = despawnSound = Sounds.largeExplosion;
				hitShake = despawnShake = 6.1f;
				
				shootEffect = EffectWrapper.wrap(NHFx.circleSplash, hitColor);
				smokeEffect = EffectWrapper.wrap(NHFx.missileShoot, Color.gray.cpy().a(0.84f));
				
				inaccuracy = 0.3f;
				
				weaveMag = 2f;
				weaveScale = 8f;
				homingDelay = 5f;
				homingPower = 0.325f;
				homingRange = 320f;
				
				velocityBegin = 0.6f;
				velocityIncrease = 6f;
				accelerateBegin = 0.005f;
				accelerateEnd = 0.75f;
				
				trailColor = NHColor.trail;
				trailWidth = 2f;
				trailLength = 18;
			}});
			ammoPerShot = 4;
			maxAmmo = 120;
			
			AdaptedShootHelix shootS = (AdaptedShootHelix)shoot.copy();
			shootS.flip = false;
			shootS.shots = 4;
			shootS.shotDelay = 6;
			shootS.mag /= 2;
			shootS.scl *= 2;
			shootS.offset = shootS.scl * Mathf.PI2;
			shooter(NHItems.juniorProcessor, shoot, NHItems.seniorProcessor, shootS);
			
			float mainMoveX = 2.5f;
			drawer = new DrawTurret("reinforced-"){{
				parts.add(new RegionPart("-barrel-main"){{
					mirror = true;
					moveX = mainMoveX;
					heatColor = heatC;
					heatLightOpacity = 0.86f;
				}});
				
				parts.add(new RegionPart("-charger"){{
					mirror = true;
					moveX = mainMoveX;
					
					moves.add(new PartMove(){{
						y = -1f;
						x = 1f;
						progress = PartProgress.warmup.compress(0.3f, 0.86f);
					}});
				}});
				
				parts.add(new RegionPart("-back"){{
					mirror = true;
					
					moveX = mainMoveX;
					
					moves.add(new PartMove(){{
						y = -1f;
						x = 1.5f;
						progress = PartProgress.warmup.compress(0.4f, 0.95f);
					}});
				}});
			}};
			
			liquidCapacity = 120;
			coolantMultiplier = 2.4f;
			coolant = new ConsumeLiquid(Liquids.water, 0.5f);
			
			squareSprite = false;
		}};
		
		ancientArtillery = new CaptureableTurret("ancient-artillery"){{
			size = 8;
			destructible = false;
			sync = true;
			
			health = 45000;
			armor = 120;
			
			lightColor = NHColor.ancientLightMid;
			clipSize = 8 * 24;
			
			outlineColor = Pal.darkOutline;
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.ancimembrane, 1200, NHItems.seniorProcessor, 300));
			
			warmupMaintainTime = 90f;
			shootWarmupSpeed /= 5f;
			minWarmup = 0.9f;
			shootCone = 15f;
			rotateSpeed = 0.325f;
			canOverdrive = false;
			ammo(NHItems.fusionEnergy, NHBullets.ancientArtilleryProjectile);
			shooter(NHItems.fusionEnergy, new ShootPattern());
			
			ammoPerShot = 12;
			maxAmmo = 180;
			
			range = 1200;
			reload = 120;
			
			unitSort = NHUnitSorts.slowest;
			
			shake = 7;
			recoil = 3;
			shootY = -13.5f;
			shootSound = NHSounds.flak;
			
			consumePowerCond(5, TurretBuild::isActive);
			
			enableDrawStatus = false;
			
			drawer = new DrawTurret(){{
				parts.addAll(
					new RegionPart("-additional"){{
						drawRegion = false;
						heatColor = Color.red;
						heatProgress = PartProgress.warmup;
						heatLightOpacity = 0.55f;
					}},
					new FlipRegionPart("-armor"){{
						outline = mirror = true;
						layerOffset = 0.2f;
						x = 2f;
						
						moveY = 16f;
						moveX = 8f;
						moveRot = -45;
						moves.add(new PartMove(PartProgress.recoil, 0, -6, 0));
					}},
					new FlipRegionPart("-back"){{
						outline = mirror = true;
						layerOffset = 0.3f;
						x = 2f;
						moveY = -2f;
						moveX = 5f;
						
						moves.add(new PartMove(PartProgress.recoil, 0, -4, 0));
					}},
					new FlipRegionPart("-cover"){{
						outline = true;
						layerOffset = 0.3f;
						turretHeatLayer = Layer.turretHeat + layerOffset;
						heatColor = Color.red;
						heatProgress = PartProgress.warmup;
						heatLightOpacity = 0.55f;
					}},
					new FlipRegionPart("-barrel"){{
						outline = mirror = true;
						layerOffset = 0.2f;
						x = 2f;
						
						turretHeatLayer = Layer.turretHeat + layerOffset;
						heatColor = Color.red;
						heatProgress = PartProgress.warmup;
						heatLightOpacity = 0.55f;
						
						moves.add(new PartMove(PartProgress.recoil, -1.75f, -8, -2.12f));
						moveY = 6f;
						moveX = 7.75f;
						moveRot = 3.6f;
					}},
					new FlipRegionPart("-tail"){{
						outline = mirror = true;
						layerOffset = 0.2f;
						x = 1f;
						moveY = -4f;
						moveX = 2f;
					}},
					new DrawArrowSequence(){{
						x = 0;
						y = 2f;
						arrows = 9;
						color = NHColor.ancientLightMid;
						colorTo = Color.red;
						colorToFinScl = 0.12f;
					}},
					new HaloPart(){{
						y = -52f;
						layer = Layer.bullet;
						color = NHColor.ancient;
						colorTo = NHColor.ancientLightMid;
						hollow = true;
						tri = false;
						
						shapes = 1;
						
						sides = 16;
						stroke = -1f;
						strokeTo = 3.4f;
						radius = 8f;
						radiusTo = 14.5f;
						
						haloRadius = 0;
						
						haloRotateSpeed = 2f;
					}},
					new HaloPart(){{
						y = -52f;
						layer = Layer.bullet;
						color = NHColor.ancient;
						colorTo = NHColor.ancientLightMid;
						tri = true;
						shapes = 2;
						radius = -1;
						radiusTo = 4.2f;
						triLength = 6;
						triLengthTo = 18;
						
						haloRadius = 14;
						haloRadiusTo = 25;
						haloRotateSpeed = 1.5f;
					}},
					new HaloPart(){{
						y = -52f;
						layer = Layer.bullet;
						color = NHColor.ancient;
						colorTo = NHColor.ancientLightMid;
						tri = true;
						shapes = 2;
						radius = -1;
						radiusTo = 4.2f;
						triLength = 0;
						triLengthTo = 4;
						
						haloRadius = 14;
						haloRadiusTo = 25;
						shapeRotation = 180;
						haloRotateSpeed = 1.5f;
					}},
					
					new HaloPart(){{
						y = -52f;
						layer = Layer.bullet;
						color = NHColor.ancient;
						colorTo = NHColor.ancientLightMid;
						tri = true;
						shapes = 2;
						radius = -1;
						radiusTo = 5f;
						triLength = 10;
						triLengthTo = 24;
						
						haloRadius = 15;
						haloRadiusTo = 28;
						haloRotateSpeed = -1f;
					}},
					new HaloPart(){{
						y = -52f;
						layer = Layer.bullet;
						color = NHColor.ancient;
						colorTo = NHColor.ancientLightMid;
						tri = true;
						shapes = 2;
						radius = -1;
						radiusTo = 5f;
						triLength = 0;
						triLengthTo = 6;
						
						haloRadius = 15;
						haloRadiusTo = 28;
						shapeRotation = 180;
						haloRotateSpeed = -1f;
					}}
				);
			}};
		}};
		
		prism = new ItemTurret("prism"){{
			size = 4;
			recoil = 2f;
			health = 6500;
			
			armor = 25;
			
			unitSort = UnitSorts.strongest;
			
			outlineColor = Pal.darkOutline;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 100, NHItems.seniorProcessor, 120, Items.tungsten, 350, NHItems.zeta, 500));
			shootY = 4f;
			
			minWarmup = 0.9f;
			warmupMaintainTime = 25f;
			shootWarmupSpeed /= 2f;
			
			shootSound = Sounds.shootSmite;
			
			rotateSpeed = 1.1f;
			
			cooldownTime = 90f;
			
			squareSprite = false;
			drawer = new DrawTurret("reinforced-"){{
				heatColor = Color.red;
				
				parts.add(new RegionPart("-top"){{
					under = turretShading = true;
					layerOffset = outlineLayerOffset = -0.0015f;
					heatLayerOffset = -0.0005f;
				}}, new RegionPart("-barrel"){{
					heatColor = Color.red;
					mirror = true;
					layerOffset = 0.001f;
					outlineLayerOffset = -0.0025f;
					moveY = 1f;
					moveX = 1.5f;
					moveRot = 8f;
					
					progress = PartProgress.warmup.blend(PartProgress.smoothReload, 0.5f);
					
					children.add(new RegionPart(""){{
						name = NewHorizon.name("prism-barrel-charger");
						mirror = true;
						layerOffset = 0.001f;
						outlineLayerOffset = -0.0025f;
						moveX = 0.75f;
						moveY = -0.75f;
						progress = PartProgress.warmup;
					}});
					
					progress = NHPartProgress.recoilWarmup;
				}});
			}};
			
			reload = 420f;
			shootCone = 5f;
			
			ammoPerShot = 16;
			maxAmmo = 80;
			
			ammo(NHItems.fusionEnergy,
					new DelayedPointBulletType(){{
				width = 15f;
				damage = 1200;
				lightningDamage = 40;
				hitColor = NHColor.lightSkyBack;
				lightColor = lightningColor = trailColor = NHColor.lightSkyMiddle;
				rangeOverride = 600;
				
				ammoMultiplier = 1;
				
				lightning = 3;
				lightningLength = lightningLengthRand = 8;
				
				despawnEffect = NHFx.lightningHitLarge;
				hitEffect = NHFx.hitSparkHuge;
				errorCorrectionRadius = 32;
				
				status = NHStatusEffects.ultFireBurn;
				statusDuration = 600f;
				
				trailEffect = new Effect(30f, 50f, e -> {
					Draw.color(e.color, Color.white, e.fout() * 0.6f);
					
					float f = e.finpow();
					Rand rand = Fx.rand;
					rand.setSeed(e.id + 1);
					Angles.randLenVectors(e.id, 6, 16, 0, 360, (x, y) -> {
						Fill.poly(e.x + x * f, e.y + y * f, 3, rand.random(3, 5) * e.fout(), rand.random(360) + rand.random(80, 220) * e.fin(Interp.pow3Out));
						Drawf.light(e.x + x * f, e.y + y * f, 8, e.color, 0.7f);
					});
				});
				
				trailSpacing *= 2f;
				
				shootEffect = EffectWrapper.wrap(NHFx.shootLine(32f, 12f), hitColor);
				smokeEffect = EffectWrapper.wrap(NHFx.square45_6_45, hitColor);
				
//				Vars.content.getByName(ContentType.block, "new-horizon-prism").reload = 12;
				
				despawnShake = hitShake = 2f;
				fragBullets = 2;
				
				fragBullet = new ChainBulletType(700){{
					length = 0;
					collidesAir = collidesGround = true;
					quietShoot = true;
					hitColor = NHColor.lightSkyBack;
					lightColor = lightningColor = trailColor = NHColor.lightSkyMiddle;
					thick = 7.3f;
					maxHit = 5;
					hitEffect = NHFx.lightningHitSmall;
					effectController = (t, f) -> {
						DelayedPointBulletType.laser.at(f.getX(), f.getY(), thick, hitColor, new Vec2().set(t));
					};
				}};
			}},
				NHItems.thermoCorePositive, new DelayedPointBulletType(){{
					width = 15f;
					damage = 500;
					splashDamage = 800;
					splashDamageRadius = 120;
					lightningDamage = 80;
					hitColor = NHColor.thermoPst;
					lightColor = lightningColor = trailColor = NHColor.thermoPst;
					rangeOverride = 600;
					
					ammoMultiplier = 1;
					lightning = 5;
					lightningLength = lightningLengthRand = 18;
					
					despawnEffect = new OptionalMultiEffect(
						NHFx.smoothColorCircle(hitColor, splashDamageRadius + 50f, 95f),
						NHFx.circleOut(95f, splashDamageRadius + 50f, 2),
						NHFx.spreadOutSpark(160f, splashDamageRadius + 40f, 72, 4, 72f, 13f, 4f, Interp.pow3Out)
					);
					hitEffect = NHFx.square45_6_45;
					
					status = NHStatusEffects.scrambler;
					statusDuration = 600f;
					
					trailEffect = NHFx.square45_4_45;
					
					trailSpacing *= 2f;
					reloadMultiplier = 0.75f;
					
					despawnShake = hitShake = 5f;
					despawnSound = hitSound = NHSounds.shock;
					hitSoundVolume = 2;
					
					fragBullets = 2;
					fragBullet = NHBullets.hyperBlastLinker;
					fragLifeMax = 1.3f;
					fragLifeMin = 0.6f;
					fragVelocityMax = 0.55f;
					fragVelocityMin = 0.2f;
					
					shootEffect = EffectWrapper.wrap(NHFx.shootLine(32f, 12f), hitColor);
					smokeEffect = EffectWrapper.wrap(NHFx.square45_6_45, hitColor);
				}}
			);
			
			shake = 4;
			hasLiquids = true;
			liquidCapacity = 80f;
			coolant = new ConsumeCoolant(0.5f);
			coolantMultiplier = 2.25f;
			range = 520f;
		}};
		
		interferon = new PowerTurret("interferon"){{
			size = 3;
			recoil = 1f;
			reload = 120f;
			health = 1800;
			armor = 8;
			
			shoot = new ShootSine(){{
				scl = 16f;
				mag = 8f;
			}};
			
			shootSound = Sounds.pulseBlast;
			outlineColor = Pal.darkOutline;
			warmupMaintainTime = 45f;
			minWarmup = 0.9f;
			shootWarmupSpeed /= 2f;
			cooldownTime = 65f;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.presstanium, 250, NHItems.juniorProcessor, 120, Items.beryllium, 90, NHItems.zeta, 300));
			shootY -= 6f;
			shootType = new LightningLinkerBulletType(1.5f, 40){{
				lifetime = 110;
				keepVelocity = false;
				
				sprite = NHBullets.MISSILE_LARGE;
				
				trailColor = lightColor = lightningColor = hitColor = NHColor.ancientLightMid;
				backColor = NHColor.ancientLightMid;
				frontColor = NHColor.ancientLight;
				
				hitSpacing = 3f;
				hitShake = 1.0F;
				lightningDamage = damage = splashDamage = 45;
				splashDamageRadius = 50f;
				
				lightning = 1;
				lightningLength = lightningLengthRand = 8;
				
				effectLightningChance /= 3f;
				effectLightningLength = 17;
				
				effectLingtning = 1;
				
				maxHit = 8;
				despawnShake = 5f;
				hitSound = despawnSound = Sounds.plasmaboom;
				statusDuration = 120f;
				status = NHStatusEffects.emp1;
				
				size = 5f;
				width = 7f;
				height = 22f;
				drawCircle = false;
				
				trailWidth = 3f;
				trailLength = 16;
				
				linkRange = 60f;
				
				scaleLife = false;
				despawnHit = true;
				
				collidesAir = collidesGround = true;
				
				shootEffect = EffectWrapper.wrap(NHFx.shootLine(33, 33), hitColor);
				smokeEffect = EffectWrapper.wrap(NHFx.hitSparkHuge, hitColor);
				
				hitEffect = NHFx.hitSpark;
				despawnEffect = NHFx.blast(hitColor, 42f);
				
				spreadEffect = EffectWrapper.wrap(NHFx.hitSpark, hitColor);
			}};
			
			squareSprite = false;
			drawer = new DrawTurret("reinforced-"){{
				parts.add(new RegionPart("-handle"){{
						x = -5f;
						moveX = 5f;
						mirror = true;
						under = turretShading = true;
					
						progress = PartProgress.warmup;
	                }}, new RegionPart("-turret-base"){{
						under = turretShading = true;
					}}, new RegionPart("-barrel"){{
						under = turretShading = true;
						moveY = -4f;
						progress = PartProgress.recoil;
					}}
				);
			}};
			
			hasLiquids = true;
			coolant = new ConsumeCoolant(0.15f);
			consumePowerCond(12f, TurretBuild::isActive);
			range = 160f;
			inaccuracy = 1.25f;
		}};
		
		beamLaserTurret = new ItemTurret("beam-laser-turret"){{
			size = 2;
			requirements(Category.turret, BuildVisibility.shown, with(Items.copper, 60, NHItems.juniorProcessor, 60, NHItems.presstanium, 60));
			recoil = 1f;
			reload = 60f;
			
			shoot = new ShootPattern(){{
				shots = 3;
				shotDelay = 1.75f;
			}};
			
			canOverdrive = false;
			squareSprite = false;
			drawer = new DrawTurret("reinforced-");
			
			heatColor = Pal.turretHeat.cpy().lerp(Pal.redderDust, 0.5f).mul(1.1f);
			cooldownTime *= 2f;
			shootSound = NHSounds.laser5;
			range = 144f;
			shootCone = 30f;
			inaccuracy = 6f;
			maxAmmo = 80;
			ammoPerShot = 5;
			shootY += 5f;
			health = 600;
			smokeEffect = Fx.shootBigSmoke2;
			consumePowerCond(2f, TurretBuild::isActive);
			ammo(
				Items.silicon, new AdaptedLaserBulletType(100){{
					colors = new Color[]{Pal.bulletYellowBack.cpy().mul(1f, 1f, 1f, 0.35f), Pal.bulletYellowBack, Color.white};
					hitColor = Pal.bulletYellow;
					length = 150f;
					lifetime = 30f;
					drawLine = false;
					width = 8f;
					lengthFalloff = 0.8f;
					sideLength = 25f;
					sideWidth = 0.7f;
					sideAngle = 30f;
					largeHit = false;
					ammoMultiplier = 1;
					shootEffect = NHFx.square(hitColor, 15f, 2, 8f, 2f);
				}}
			);
		}};
		
		antibody = new ItemTurret("antibody"){{
			requirements(Category.turret, with(NHItems.presstanium, 220, Items.tungsten, 150, NHItems.juniorProcessor, 80, Items.phaseFabric, 50));
			
			size = 3;
			health = 1800;
			armor = 10f;
			
			range = 200f;
			
			warmupMaintainTime = 22f;
			shootWarmupSpeed /= 2f;
			minWarmup = 0.9f;
			
			squareSprite = false;
			drawer = new DrawTurret("reinforced-"){{
				parts.add(new RegionPart("-top"){{
					under = turretShading = true;
					outline = true;
				}}, new RegionPart("-side"){{
					
					mirror = true;
					moveY = 2.75f;
					moveX = 5.5f;
					moveRot = -45f;
					progress = PartProgress.warmup;
				}});
			}};
			
			reload = 60f;
			
			shoot = new ShootMulti(new ShootPattern(), new ShootPattern(){{
				shots = 2;
				shotDelay = 4f;
			}}, new ShootBarrel(){{
				shots = 4;
				shotDelay = 4f;
				firstShotDelay = 8f;
				barrels = new float[]
						{5.35f, -14f, -45f, -5.35f, -14f, 45f};
			}});
			
			rotateSpeed = 3f;
			coolant = consume(new ConsumeLiquid(NHLiquids.quantumEntity, 10f / 60f));
			coolantMultiplier = 2f;
			
			inaccuracy = 3f;
			
			shootSound = NHSounds.laser3;
			outlineColor = Pal.darkOutline;
			
			ammo(NHItems.zeta, new AccelBulletType(2.85f, 40f){{
				frontColor = NHColor.ancientLight;
				backColor = lightningColor = trailColor = hitColor = lightColor = NHColor.ancient;
				lifetime = 55f;
				
				status = NHStatusEffects.emp2;
				statusDuration = 120f;
				
				width = 6f;
				height = 18f;
				
				velocityBegin = 0.35f;
				velocityIncrease = 7f;
				accelInterp = NHInterp.inOut;
				accelerateBegin = 0f;
				accelerateEnd = 0.775f;
				
				homingDelay = 2f;
				homingPower = 0.035f;
				homingRange = 120f;
				
				despawnHit = true;
				hitShake = despawnShake = 2f;
				
				hitEffect = NHFx.hitSpark;
				despawnEffect = NHFx.square45_4_45;
				shootEffect = new Effect(18f, e -> {
					color(backColor, frontColor, e.fout() * 0.75f);
					
					randLenVectors(e.id, 9, e.finpow() * 23f, e.rotation, 20f, (x, y) -> {
						DrawFunc.arrow(e.x + x, e.y + y, e.fslope() * 1.1f + e.fout() * 1.15f, e.fout() * 5.5f + 0.75f, -e.fout() * 0.6f - 0.2f, Mathf.angle(x, y));
					});
				});
				smokeEffect = Fx.none;
				
				trailEffect = NHFx.trailToGray;
				
				trailLength = 6;
				trailWidth = 2f;
				
				fragBullets = 1;
				fragBullet = new ShrapnelBulletType(){{
					width = 6;
					length = 60;
					lifetime = 22f;
					damage = 70.0F;
					status = NHStatusEffects.emp2;
					serrationLenScl = 2f;
					serrationWidth = 2f;
					serrations = 3;
					statusDuration = 120f;
					fromColor = NHColor.ancientLight;
					hitColor = lightColor = lightningColor = toColor = NHColor.ancient;
					shootEffect = NHFx.lightningHitSmall(NHColor.ancient);
					smokeEffect = NHFx.circleSplash;
				}};
			}
				public void createFrags(Bullet b, float x, float y){
					if(fragBullet != null && (fragOnAbsorb || !b.absorbed)){
						float a = b.rotation();
						for(int i = 0; i < fragBullets; i++){
							fragBullet.create(b, x, y, a);
						}
					}
				}
			});
			
			maxAmmo = 40;
		}};
		
		webber = new Webber("webber"){{
			size = 3;
			
			moveInterp = Interp.pow3In;
			status = NHStatusEffects.scrambler;
			shootLength = 22f;
			laserColor = NHColor.thermoPst;
			requirements(Category.turret, ItemStack.with(NHItems.multipleSteel, 50, Items.plastanium, 85, NHItems.seniorProcessor, 35, NHItems.presstanium, 80));
			hasPower = true;
			cal = d -> 4 * Interp.pow3Out.apply(d) - 3;
			scaledForce = 0;
			force = 45f;
			range = 280.0F;
			damage = 0.3F;
			scaledHealth = 160.0F;
			rotateSpeed = 10.0F;
			consumePower(12.0F);
		}};
		
		gravity = new MultTractorBeamTurret("gravity"){{
			size = 3;
			requirements(Category.turret, ItemStack.with(Items.metaglass, 35, NHItems.juniorProcessor, 15, Items.lead, 80, NHItems.presstanium, 45));
			health = 1020;
			maxAttract = 8;
			shootCone = 60f;
			range = 300f;
			hasPower = true;
			force = 40.0F;
			scaledForce = 8.0F;
			shootLength = size * tilesize / 2f - 3;
			damage = 0.15F;
			rotateSpeed = 6f;
			consumePowerCond(6.0F, (MultTractorBeamBuild e) -> e.target != null);
		}};
		
		argmot = new SpeedupTurret("argmot"){{
			shoot = new ShootAlternate(){{
				spread = 7f;
			}};
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart(){{
					drawRegion = false;
					mirror = true;
					moveY = -2.75f;
					progress = PartProgress.recoil;
					children.add(new RegionPart("-shooter"){{
						heatLayerOffset = 0.001f;
						heatColor = NHColor.thurmixRed.cpy().a(0.85f);
						progress = PartProgress.warmup;
						mirror = outline = true;
						moveX = 2f;
						moveY = 2f;
						moveRot = 11.25f;
					}});
				}}, new RegionPart("-up"){{
					layerOffset = 0.3f;
					
					turretHeatLayer += layerOffset + 0.1f;
					
					heatColor = NHColor.thurmixRed.cpy().a(0.85f);
					outline = false;
				}});
			}};
			
			warmupMaintainTime = 120f;
			
			rotateSpeed = 3f;
			health = 960;
			requirements(Category.turret, with(Items.phaseFabric, 150, NHItems.multipleSteel, 120, NHItems.juniorProcessor, 80, Items.plastanium, 120));
			maxSpeedupScl = 9f;
			speedupPerShoot = 0.3f;
			hasLiquids = true;
			coolant = new ConsumeCoolant(0.15f);
			consumePowerCond(35f, TurretBuild::isActive);
			size = 3;
			range = 200;
			reload = 60f;
			shootCone = 24f;
			shootSound = NHSounds.laser3;
			shootType = new PosLightningType(50f){{
				lightningColor = hitColor = NHColor.lightSkyBack;
				maxRange = rangeOverride = 250f;
				hitEffect = NHFx.hitSpark;
				smokeEffect = Fx.shootBigSmoke2;
			}};
		}};
		
		eternity = new ItemTurret("eternity"){{
			armor = 30;
			size = 16;
			outlineRadius = 7;
			range = 1200;
			heatColor = NHColor.darkEnrColor;
			unitSort = NHUnitSorts.regionalHPMaximum_All;
			
			coolant = consume(new ConsumeLiquid(NHLiquids.quantumEntity, 1));
			liquidCapacity = 120;
			coolantMultiplier = 2.5f;
			
			buildCostMultiplier *= 2;
			canOverdrive = false;
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-side"){{
					under = mirror = true;
					layerOffset = -0.1f;
					moveX = 6f;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}}, new RegionPart("-side-down"){{
					mirror = true;
					layerOffset = -0.5f;
					moveX = 10f;
					moveY = 45f;
					y = 10f;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}}, new RegionPart("-side-down"){{
					mirror = true;
					layerOffset = -0.35f;
					moveX = -9f;
					moveY = 7f;
					y = -2f;
					x = 8;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}}, new RegionPart("-side-down"){{
					under = mirror = true;
					layerOffset = -0.2f;
					moveY = -33f;
					y = -33f;
					x = 14;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}});
				
				parts.add(new ArcCharge(){{
					progress = PartProgress.smoothReload.inv().curve(Interp.pow5Out);
					color = NHColor.darkEnrColor;
					chargeY = t -> -35f;
					shootY = t -> 90 * curve.apply(1 - t.smoothReload);
				}});
				
//				parts.add(new FlarePart(){{
//					layer = Layer.effect;
//					rotMove = 360;
//					rotation = 45f;
//					followRotation = true;
//					y = 70f;
//					stroke = 7;
//					radius = 0;
//					radiusTo = 68;
//					color1 = NHColor.darkEnrColor;
//					color2 = NHColor.darkEnrFront;
//					progress = DrawPart.PartProgress.smoothReload.inv().curve(Interp.pow10In).delay(0.2f);
//				}
//					@Override
//					public void draw(PartParams params){
//						float z = Draw.z();
//						if(layer > 0) Draw.z(layer);
//
//						float prog = progress.getClamp(params);
//						int i = params.sideOverride == -1 ? 0 : params.sideOverride;
//
//						float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
//						Tmp.v1.set(x * sign, y).rotate(params.rotation - 90);
//
//						float
//								rx = params.x + Tmp.v1.x,
//								ry = params.y + Tmp.v1.y,
//								rot = (followRotation ? params.rotation : 0f) + rotMove * prog + rotation + Time.time * 0.86f,
//								rad = radiusTo < 0 ? radius : Mathf.lerp(radius, radiusTo, prog);
//
//						Draw.color(color1);
//						for(int j = 0; j < sides; j++){
//							Drawf.tri(rx, ry, stroke, rad, j * 360f / sides + rot);
//						}
//
//						Draw.color(color2);
//						for(int j = 0; j < sides; j++){
//							Drawf.tri(rx, ry, stroke * innerScl, rad * innerRadScl, j * 360f / sides + rot);
//						}
//
//						Draw.color();
//						Draw.z(z);
//					}
//				});
			}};
			
			shoot = new ShootPattern();
			inaccuracy = 0;
			
			ammoPerShot = 40;
			coolantMultiplier = 0.8f;
			rotateSpeed = 0.25f;
			
			float chargeCircleFrontRad = 12f;
			
			shootEffect = new Effect(120f, 2000f, e -> {
				float scl = 1f;
				if(e.data instanceof Float)scl *= (float)e.data;
				Draw.color(heatColor, Color.white, e.fout() * 0.25f);
				
				float rand = Mathf.randomSeed(e.id, 60f);
				float extend = Mathf.curve(e.fin(Interp.pow10Out), 0.075f, 1f) * scl;
				float rot = e.fout(Interp.pow10In);
				
				for(int i : Mathf.signs){
					DrawFunc.tri(e.x, e.y, chargeCircleFrontRad * 1.2f * e.foutpowdown() * scl,200 + 500 * extend, e.rotation + (90 + rand) * rot + 90 * i - 45);
				}
				
				for(int i : Mathf.signs){
					DrawFunc.tri(e.x, e.y, chargeCircleFrontRad * 1.2f * e.foutpowdown() * scl,200 + 500 * extend, e.rotation + (90 + rand) * rot + 90 * i + 45);
				}
			});
			
			smokeEffect = new Effect(50, e -> {
				Draw.color(heatColor);
				Lines.stroke(e.fout() * 5f);
				Lines.circle(e.x, e.y, e.fin() * 300);
				Lines.stroke(e.fout() * 3f);
				Lines.circle(e.x, e.y, e.fin() * 180);
				Lines.stroke(e.fout() * 3.2f);
				Angles.randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
					lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
				});
				Draw.color(Color.white);
				Drawf.light(e.x, e.y, e.fout() * 120, heatColor, 0.7f);
			});
			
			recoil = 18f;
			shake = 80f;
			shootSound = Sounds.laserblast;
			health = 800000;
			shootCone = 5f;
			maxAmmo = 80;
			consumePowerCond(800f, TurretBuild::isActive);
			reload = 1800f;
			
			ammo(NHItems.darkEnergy, NHBullets.eternity);
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 5000, NHItems.darkEnergy, 2000));
		}};
		
		synchro = new ItemTurret("synchro"){{
			size = 3;
			health = 1420;
			reload = 12f;
			inaccuracy = 0.75f;
			
			recoil = 0.5f;
			
			coolant = consumeCoolant(0.2F);
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-shooter"){{
					under = true;
					outline = true;
					moveY = -3f;
					progress = PartProgress.recoil;
				}});
			}};
			
			coolantMultiplier = 2f;
			
			velocityRnd = 0.075f;
			unitSort = UnitSorts.weakest;
			
			range = 360f;
			
			shootSound = NHSounds.synchro;
			
			shoot = new ShootMulti(new ShootPattern(), new ShootBarrel(){{
				barrels = new float[]{-6.5f, 3f, 0f};
			}}, new ShootBarrel(){{
				barrels = new float[]{6.5f, 3f, 0f};
			}});
			
			ammo(
					NHItems.zeta, NHBullets.synchroZeta,
					NHItems.fusionEnergy, NHBullets.synchroFusion,
					NHItems.thermoCorePositive, NHBullets.synchroThermoPst,
					Items.phaseFabric, NHBullets.synchroPhase
			);
			
			ammoPerShot = 1;
			maxAmmo = 80;
			
			requirements(Category.turret, BuildVisibility.shown, with(Items.phaseFabric, 20, NHItems.metalOxhydrigen, 90, NHItems.juniorProcessor, 60, NHItems.zeta, 120, Items.plastanium, 80));
		}};
		
		antiBulletTurret = new PointDefenseTurret("anti-bullet-turret"){{
			health = 1080;
			size = 3;
			
			coolant = consumeCoolant(0.1F);
			
			color = lightColor = NHColor.lightSkyBack;
			beamEffect = Fx.chainLightning;
			hitEffect = NHFx.square45_4_45;
			shootEffect = NHFx.shootLineSmall(color);
			shootSound = NHSounds.gauss;
			
			range = 280f;
			
			hasPower = true;
			consumePowerCond(8f, (PointDefenseBuild b) -> b.target != null);
			
			shootLength = 5f;
			bulletDamage = 150f;
			reload = 6f;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 90, NHItems.juniorProcessor, 60, NHItems.presstanium, 120, NHItems.zeta, 120, Items.graphite, 80));
		}};
		
		pulseShotgun = new ItemTurret("pulse-shotgun"){{
			health = 960;
			range = 200;
			smokeEffect = Fx.shootBigSmoke;
			
			coolant = consumeCoolant(0.1F);
			
			shoot = new ShootSpread(){{
				shots = 12;
				shotDelay = 2f;
				spread = 0.55f;
			}};

			reload = 90f;
			recoil = 3f;
			shootCone = 30f;
			inaccuracy = 4f;
			size = 2;
			shootSound = Sounds.shootSnap;
			shake = 3f;
			ammo(
					Items.titanium, new BasicBulletType(5, 24){{
						width = 8f;
						height = 25f;
						hitColor = backColor = lightColor = trailColor = Items.titanium.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 12f, 2f);
						ammoMultiplier = 8;
						pierceArmor = true;
					}},
					
					Items.plastanium, new BasicBulletType(5, 26){{
						width = 8f;
						height = 25f;
						fragBullets = 4;
						fragBullet = new BasicBulletType(2, 26){{
							width = 3f;
							lifetime = 10f;
							height = 12f;
							ammoMultiplier = 12;
							hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
							frontColor = backColor.cpy().lerp(Color.white, 0.35f);
							hitEffect = NHFx.lightningHitSmall(backColor);
							shootEffect = despawnEffect = NHFx.square45_4_45;
						}};
						fragAngle = 130f;
						fragVelocityMax = 1.1f;
						fragVelocityMin = 0.5f;
						fragLifeMax = 1.25f;
						fragLifeMin = 0.25f;
						ammoMultiplier = 12;
						hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}},
					
					NHItems.zeta, new BasicBulletType(5, 18){{
						width = 8f;
						height = 25f;
						lightning = 2;
						lightningLength = 2;
						lightningLengthRand = 6;
						lightningDamage = damage;
						status = StatusEffects.shocked;
						statusDuration = 15f;
						ammoMultiplier = 12;
						lightningColor = hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}},
					
					Items.pyratite, new BasicBulletType(5, 18){{
						width = 8f;
						height = 25f;
						incendAmount = 4;
						incendChance = 0.25f;
						incendSpread = 12f;
						status = StatusEffects.burning;
						statusDuration = 15f;
						ammoMultiplier = 12;
						hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						despawnEffect = Fx.blastExplosion;
						shootEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}},
					
					Items.blastCompound, new BasicBulletType(5, 22){{
						width = 8f;
						height = 25f;
						status = StatusEffects.blasted;
						statusDuration = 15f;
						splashDamageRadius = 12f;
						splashDamage = damage;
						ammoMultiplier = 8;
						hitColor = backColor = lightColor = trailColor = Items.blastCompound.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						despawnEffect = Fx.blastExplosion;
						shootEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}}
			);
			
			limitRange();
			maxAmmo = 120;
			ammoPerShot = 12;
			
			requirements(Category.turret, with(Items.copper, 30, Items.graphite, 40, NHItems.presstanium, 50, Items.lead, 60));
		}};
		
		dendrite = new ShootMatchTurret("dendrite"){{
			health = 6000;
			armor = 15;
			range = 360f;
			
			recoil = 6.0F;
			size = 4;
			shake = 12.0F;
			reload = 60.0F;
			
			rotateSpeed = 2.4f;
			
			shootSound = NHSounds.flak2;
			
			ammo(NHItems.ancimembrane, new TrailFadeBulletType(4f, 580f, "circle-bullet"){{
				velocityBegin = 8.8f;
				velocityIncrease = -8.15f;
				accelerateBegin = 0.1f;
				accelerateEnd = 0.925f;
				accelInterp = Interp.pow2Out;
				lifetime = 92f;
				
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.ancient;
				frontColor = NHColor.ancientLight;
				
				impact = true;
				knockback = 3f;
				
				status = NHStatusEffects.entangled;
				statusDuration = 120f;
				
				hitSize = 12f;
				lightning = 2;
				lightningLengthRand = 5;
				lightningLength = 3;
				lightningDamage = damage / 10f;
				
				reloadMultiplier = 0.5f;
				ammoMultiplier = 2f;
				
				width = 155f;
				height = 7;
				shrinkX = 0.45f;
				shrinkY = -2.48f;
				shrinkInterp = Interp.reverse;
				
				pierce = true;
				pierceCap = 4;
				
				tracers = 1;
				tracerUpdateSpacing = 3;
				tracerFadeOffset = 5;
				tracerRandX = 16f;
				tracerStrokeOffset = 6;
				addBeginPoint = true;
				
				
				
				smokeEffect = NHFx.hugeSmokeGray;
				shootEffect = new EffectWrapper(NHFx.shootLine(30f, 120f), backColor);
				hitEffect = NHFx.square45_6_45;
				despawnEffect = new Effect(35f, 70f, e -> {
					Draw.color(e.color, Color.white, e.fout() * 0.7f);
					for(int i : Mathf.signs){
						
						Drawf.tri(e.x, e.y, height * 1.5f * e.fout(), width * 0.885f * e.fout(), e.rotation + i * 90);
						Drawf.tri(e.x, e.y, height * 0.8f * e.fout(), width * 0.252f * e.fout(), e.rotation + 90 + i * 90);
					}
				});
			}
				
				@Override
				public void hitEntity(Bullet b, Hitboxc entity, float health){
					super.hitEntity(b, entity, health);
					b.fdata += 1;
					
					if(b.fdata > pierceCap){
						b.hit = true;
						b.remove();
					}
				}
				
				@Override
				public void update(Bullet b){
					super.update(b);
					b.collided.clear();
				}
			}, NHItems.setonAlloy, new AccelBulletType(2.85f, 280f){{
				frontColor = NHColor.ancientLight;
				backColor = lightningColor = trailColor = hitColor = lightColor = NHColor.ancient;
				lifetime = 92f;
				knockback = 2f;
				ammoMultiplier = 6f;
				accelerateBegin = 0.1f;
				accelerateEnd = 0.85f;
				
				despawnSound = hitSound = Sounds.dullExplosion;
				
				velocityBegin = 6f;
				velocityIncrease = -4f;
				
				homingDelay = 20f;
				homingPower = 0.05f;
				homingRange = 120f;
				
				status = NHStatusEffects.entangled;
				statusDuration = 120f;
				
				despawnHit = pierceBuilding = true;
				hitShake = despawnShake = 5f;
				lightning = 1;
				lightningCone = 360;
				lightningLengthRand = 12;
				lightningLength = 4;
				width = 10f;
				height = 35f;
				pierceCap = 8;
				shrinkX = shrinkY = 0;
				
				reloadMultiplier = 1.75f;
				lightningDamage = damage * 0.8f;
				
				hitEffect = NHFx.hitSparkLarge;
				despawnEffect = NHFx.square45_6_45;
				shootEffect = new EffectWrapper(NHFx.shootLine(22, 20f), backColor);
				smokeEffect = NHFx.hugeSmokeGray;
				trailEffect = NHFx.trailToGray;
				
				trailLength = 15;
				trailWidth = 2f;
				drawSize = 300f;
			}});
			
			shooter(NHItems.ancimembrane, new ShootSpread(){{
				shots = 15;
				spread = 8;
			}}, NHItems.setonAlloy, new ShootAlternate(){{
				shots = 3;
				shotDelay = 5;
				spread = 11f;
			}});
			
			shootCone = 35f;
			ammoPerShot = 6;
			maxAmmo = 30;
			cooldownTime = 65f;
			
			squareSprite = false;
			drawer = new DrawTurret("reinforced-"){{
				parts.add(new RegionPart("-top"){{
					layerOffset = 0.1f;
					heatLayerOffset = 0;
				}}, new RegionPart("-back"){{
					mirror = true;
					layerOffset = 0.1f;
					
					moveX = 1.5f;
					moveY = -1.5f;
					progress = PartProgress.recoil;
					heatLayerOffset = 0;
				}}, new RegionPart("-panel"){{
					layerOffset = 0.1f;
					
					moveX = 0;
					moveY = 2.75f;
					
					heatProgress = PartProgress.warmup;
					progress = NHPartProgress.recoilWarmup;
					heatLayerOffset = 0;
				}});
			}};
			
			warmupMaintainTime = 45f;
			shootWarmupSpeed = 0.04f;
			minWarmup = 0.85f;
			
			outlineColor = Pal.darkOutline;
			canOverdrive = false;
			coolant = consume(new ConsumeLiquid(NHLiquids.quantumEntity, 15f / 60f));
			coolantMultiplier = 2f;
			consumePowerCond(12f, TurretBuild::isActive);
			unitSort = UnitSorts.weakest;
			requirements(Category.turret, with(NHItems.seniorProcessor, 200, NHItems.ancimembrane, 200, NHItems.zeta, 250, NHItems.metalOxhydrigen, 150));
		}};
		
		atomSeparator = new LaserTurret("atom-separator"){{
			health = 12000;
			range = 360f;
			shootEffect = NHFx.hugeSmokeGray;
			shootCone = 20.0F;
			recoil = 6.0F;
			size = 5;
			shake = 4.0F;
			reload = 60.0F;
			
			canOverdrive = false;
			accurateDelay = false;
			rotateSpeed = 3f;
			firingMoveFract = 0.15F;
			shootDuration = 200.0F;
			shootSound = Sounds.laserbig;
			loopSound = Sounds.beam;
			loopSoundVolume = 2.0F;
			shootType = NHBullets.atomSeparator;
			
			coolant = consumeCoolant(1f);
			consumePower(50f);
			unitSort = NHUnitSorts.slowest;
			requirements(Category.turret, with(NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 200, NHItems.zeta, 150, NHItems.presstanium, 250, NHItems.metalOxhydrigen, 150));
		}};
		
		bloodStar = new ItemTurret("blood-star"){{
			size = 5;
			coolant = consumeCoolant(0.2F);
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 230, NHItems.zeta, 300, NHItems.seniorProcessor, 200, NHItems.presstanium, 300, Items.thorium, 600));
			recoil = 5f;
			reload = 150f;
			range = 520f;
			unitSort = (u, x, y) -> -u.hitSize();
			shootSound = Sounds.laserblast;
			inaccuracy = 0f;
			shootCone = 15f;
			heatColor = Items.surgeAlloy.color.cpy().lerp(Color.white, 0.2f);
			consumePowerCond(12f, TurretBuild::isActive);
			coolantMultiplier = 3f;
			
			health = 8000;
			
			ammo(NHItems.thermoCorePositive,
					new BasicBulletType(4, 600, "large-bomb"){{
						lightning = 6;
						lightningCone = 360;
						lightningLengthRand = lightningLength = 12;
						splashDamageRadius = 60f;
						splashDamage = lightningDamage = 0.5f * damage;
						
						trailEffect = NHFx.hitSparkLarge;
						trailInterval = 3f;
						
						trailColor = backColor = hitColor = lightColor = lightningColor = heatColor;
						frontColor = Color.white;
						
						intervalBullets = 2;
						bulletInterval = 3f;
						intervalBullet = new AdaptedLightningBulletType(){{
							lightningColor = trailColor = hitColor = lightColor = heatColor;
							lightningLength = 4;
							lightningLengthRand = 15;
							damage = 60;
						}};
						
						status = NHStatusEffects.emp3;
						statusDuration = 90f;
						
						spin = 3f;
						trailLength = 40;
						trailWidth = 2.5f;
						lifetime = 140f;
						shrinkX = shrinkY = 0;
						hitSound = Sounds.explosionbig;
						drawSize = 60f;
						hitShake = despawnShake = 6f;
						shootEffect = NHFx.instShoot(backColor, frontColor);
						smokeEffect = Fx.shootBigSmoke2;
						hitEffect = new Effect(50, e -> {
							Draw.color(backColor);
							Fill.circle(e.x, e.y, e.fout() * height / 1.5f);
							Lines.stroke(e.fout() * 3f);
							Lines.circle(e.x, e.y, e.fin() * 80);
							Lines.stroke(e.fout() * 2f);
							Lines.circle(e.x, e.y, e.fin() * 50);
							Angles.randLenVectors(e.id, 35, 18 + 100 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 12 + 4));
							
							Draw.color(frontColor);
							Fill.circle(e.x, e.y, e.fout() * height / 2f);
						});
						despawnEffect = new OptionalMultiEffect(NHFx.crossBlast(backColor, 120f), NHFx.instHit(backColor, 3, 80f),
								NHFx.spreadOutSpark(120f, splashDamageRadius + 30f, 36, 4, 42f, 17f, 4f, Interp.pow3Out)
						);
						height = width = 40;
					}
						
						@Override
						public void draw(Bullet b){
							super.draw(b);
							
							float f = Mathf.curve(b.fout(), 0, 0.05f);
							float f2 = Mathf.curve(b.fin(), 0, 0.1f);
							Draw.color(backColor);
							
							float fi = Mathf.randomSeed(b.id, 360f);
							
							for(int i : Mathf.signs){
								DrawFunc.tri(b.x, b.y, 3 * f2 * f, 60 * f2 * f, fi + (i + 1) * 90 + Time.time * 2);
								DrawFunc.tri(b.x, b.y, 3 * f2 * f, 35 * f2 * f, fi + (i + 1) * 90 - Time.time * 2 + 90);
							}
						}
					}
			);
		}};
		
		multipleLauncher = new ItemTurret("multiple-launcher"){{
			size = 3;
			health = 1250;
			coolant = consumeCoolant(0.2F);
			requirements(Category.turret, ItemStack.with(Items.plastanium, 60, NHItems.presstanium, 45, NHItems.metalOxhydrigen, 45, NHItems.juniorProcessor, 30));
			canOverdrive = false;
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-shooter"){{
					mirror = true;
					progress = PartProgress.warmup.compress(0, 0.75f);
					moveX = 0.75f;
					moveY = -1.5f;
				}});
			}};
			
			ammo(
					Items.titanium, NHBullets.missileTitanium,
					Items.thorium, NHBullets.missileThorium,
					NHItems.zeta, NHBullets.missileZeta,
					Items.graphite, NHBullets.missileNormal,
					NHItems.presstanium, NHBullets.missileStrike
			);
			smokeEffect = Fx.shootSmallFlame;
			shootEffect = Fx.shootBig2;
			recoil = 3f;
			range = 280f;
			reload = 90f;
			
			shoot = new ShootBarrel(){{
				barrels = new float[]{
					-4, -2, 0,
					0, -3, 0,
					4, -2, 0
				};
				
				shotDelay = 2f;
				shots = 15;
			}};
			
			maxAmmo = 160;
			ammoPerShot = 15;
			ammoEjectBack = 6f;
			inaccuracy = 9f;
			
			xRand = tilesize * size / 6.5f;
			shootSound = Sounds.missile;
			coolantMultiplier = 0.85f;
		}};
		
		thermoTurret = new PowerTurret("thermo-turret"){{
			size = 1;
			health = 320;
			
			coolant = consumeCoolant(0.05F);
			requirements(Category.turret, BuildVisibility.shown, with(Items.titanium, 50, Items.copper, 50, Items.silicon, 25));
			shootType = new BasicBulletType(6.5f, 28f){{
				hitEffect = new Effect(12.0F, (e) -> {
					Draw.color(Pal.lancerLaser, Color.white, e.fout() * 0.75f);
					Lines.stroke(e.fout() * 1.5F);
					Angles.randLenVectors(e.id, 3, e.finpow() * 17.0F, e.rotation, 360.0F, (x, y) -> {
						float ang = Mathf.angle(x, y);
						Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4.0F + 1.0F);
					});
				});
				trailWidth = 1.25f;
				trailLength = 15;
				
				knockback = 0.5f;
				trailColor = backColor = hitColor = Pal.lancerLaser;
				frontColor = Color.white;
				lifetime = 50f;
				homingDelay = 1f;
				homingPower = 0.2f;
				homingRange = 120f;
				status = StatusEffects.shocked;
				collidesGround = false;
				statusDuration = 30f;
				width = 5f;
				drawSize = 120f;
				height = 22f;
			}};
			consumePower(3.5f);
			
			shoot = new ShootPattern(){{
				shots = 5;
				shotDelay = 6f;
			}};
			
			inaccuracy = 3f;
			reload = 75f;
			shootCone = 50.0F;
			rotateSpeed = 8.0F;
			targetGround = false;
			range = 200.0F;
			shootEffect = Fx.lightningShoot;
			smokeEffect = Fx.shootSmallSmoke;
			heatColor = Color.red;
			recoil = 1.0F;
			shootSound = NHSounds.thermoShoot;
		}};
		
		railGun = new ItemTurret("rail-gun"){{
			unitSort = (u, x, y) -> -u.speed();
			
			maxAmmo = 40;
			ammoPerShot = 8;
			
			squareSprite = false;
			drawer = new DrawTurret("reinforced-"){{
				parts.add(new RegionPart("-acceler"){{
					mirror = false;
//					under = turretShading = true;
					moveX = 0;
					moveY = -5f;
					
					progress = PartProgress.recoil;
				}}, new RegionPart("-top"){{
					outline = true;
					heatLight = false;
					mirror = false;
					//					under = turretShading = true;
					moveX = 0;
					moveY = 0;
				}});
			}};
			
			ammo(
					NHItems.irayrondPanel, NHBullets.railGun1,
					NHItems.setonAlloy, NHBullets.railGun2,
					NHItems.upgradeSort, NHBullets.railGun3
			);
			
			consumePowerCond(12f, TurretBuild::isActive);
			
			shoot = new ShootPattern(){{
				shots = 1;
				firstShotDelay = 90f;
			}};
			
			moveWhileCharging = false;
			shootCone = 6f;
			
			size = 4;
			health = 4550;
			armor = 15f;
			reload = 200f;
			recoil = 1f;
			shake = 8f;
			range = 620.0F;
			minRange = 160f;
			rotateSpeed = 1.5f;
			
			buildType = () -> new ItemTurretBuild(){
				@Override
				public void drawSelect(){
					super.drawSelect();
					Drawf.dashCircle(x, y, minRange, Pal.redderDust);
				}
				
				@Override
				protected boolean validateTarget(){
					return (!Units.invalidateTarget(target, canHeal() ? Team.derelict : team, x, y) && !within(target, minRange)) || isControlled() || logicControlled();
				}
			};
			
			shootSound = NHSounds.railGunBlast;
//			heatColor = NHItems.irayrondPanel.color;
			
			outlineColor = Pal.darkOutline;
			accurateDelay = true;
			
			coolantMultiplier = 0.55f;
			cooldownTime = 90f;
			coolant = consumeCoolant(0.3f);
			
			requirements(Category.turret, with(NHItems.setonAlloy, 150, Items.plastanium, 150, NHItems.seniorProcessor, 200, NHItems.zeta, 500, Items.phaseFabric, 125));
		}
			
			@Override
			public void drawPlace(int x, int y, int rotation, boolean valid){
				super.drawPlace(x, y, rotation, valid);
				
				Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, minRange, Pal.redderDust);
			}
			
			@Override
			public void setStats(){
				super.setStats();
				stats.add(Stat.shootRange, minRange / tilesize, StatUnit.blocks);
			}
		};
		
		endOfEra = new ShootMatchTurret("end-of-era"){{
			recoil = 5f;
			armor = 15;
			
			shootCone = 15f;
			squareSprite = false;
			
			unitSort = UnitSorts.strongest;
			
			warmupMaintainTime = 50f;
			coolant = consume(new ConsumeLiquid(NHLiquids.quantumEntity, 20f / 60f));
			coolantMultiplier = 2.5f;
			
			moveWhileCharging = false;
			canOverdrive = false;
			
			shootWarmupSpeed = 0.035f;
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-charger"){{
					under = mirror = true;
					layerOffset = -0.002f;
					moveX = 14f;
					moveY = -9f;
					moveRot = -45f;
					y = -4f;
					x = 16f;
					
					progress = PartProgress.warmup;
				}}, new RegionPart(){{
					drawRegion = false;
					mirror = true;
					heatColor = Color.clear;
					progress = PartProgress.recoil.min(PartProgress.warmup);
					moveY = -10f;
					children.add(new RegionPart("-wing"){{
						under = mirror = true;
						moveRot = 12.5f;
						moveY = 14f;
						moveX = 4f;
						heatColor = NHColor.darkEnrColor;
						progress = PartProgress.warmup;
					}});
				}}, new RegionPart("-shooter"){{
					outline = true;
					layerOffset = 0.001f;
					moveY = -12f;
					heatColor = NHColor.darkEnrColor;
					progress = PartProgress.warmup.blend(PartProgress.recoil, 0.5f);
				}});
			}};
			
			shoot = new ShootPattern(){{
				firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
			}};
			
			outlineColor = Pal.darkOutline;
			
			chargeSound = NHSounds.railGunCharge;

			requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 1500));
			ammo(NHItems.darkEnergy, NHBullets.arc_9000, NHItems.upgradeSort, new BulletType(){{
				hittable = false;
				hitColor = lightColor = trailColor = NHColor.darkEnrFront;
				ammoMultiplier = 1f;
				spawnUnit = NHBullets.airRaidMissile;
				chargeEffect = NHFx.railShoot(NHColor.darkEnrColor, 800f, 18, NHFx.darkEnergyChargeBegin.lifetime, 25);
				shootEffect = NHFx.instShoot(NHColor.darkEnrColor, NHColor.darkEnrFront);
				smokeEffect = new Effect(180f, 300f, b -> {
					float intensity = 2f;
					
					Rand rand = Fx.rand;
					
					color(b.color, 0.7f);
					for(int i = 0; i < 4; i++){
						rand.setSeed(b.id*2 + i);
						float lenScl = rand.random(0.5f, 1f);
						int fi = i;
						b.scaled(b.lifetime * lenScl, e -> {
							randLenVectors(e.id + fi - 1, e.fin(Interp.pow4Out), (int)(2 * intensity), 35f * intensity, e.rotation, 20, (x, y, in, out) -> {
								float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
								float rad = fout * ((2f + intensity) * 1.75f);
								
								Fill.circle(e.x + x, e.y + y, rad);
								Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
							});
						});
					}
				});
			}});
			shooter(NHItems.upgradeSort, new ShootBarrel(){{
				barrels = new float[]{
					22, -12, 25,
					-22, -12, -25,
					0, -22, 0,
				};
				firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
				shots = 3;
				shotDelay = 12f;
			}});
			
			shootCone = 12f;
			rotateSpeed = 0.75f;
			ammoPerShot = 4;
			maxAmmo = 20;
			size = 8;
			health = 28000;
			armor = 15f;
			hasItems = true;
			heatColor = NHColor.darkEnrColor;
			consumePower(30f);
			reload = 420f;
			range = 800f;
			inaccuracy = 0f;
			shootSound = Sounds.laserbig;
		}};
		
		executor = new ItemTurret("executor"){{
			size = 6;
			health = 15200;
			armor = 10;
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 650, Items.tungsten, 375, NHItems.seniorProcessor, 150, NHItems.multipleSteel, 400));
			ammo(
					NHItems.thermoCorePositive, NHBullets.blastEnergyPst, NHItems.thermoCoreNegative, NHBullets.blastEnergyNgt
			);
			
			shoot = new ShootBarrel(){{
				shots = 3;
				barrels = new float[]{
						-11, 2, 0,
						0, 2, 0,
						11, 2, 0
				};
				shotDelay = 4f;
			}};
			
			canOverdrive = false;
			maxAmmo = 80;
			ammoPerShot = 8;
			xRand = 1;
			velocityRnd = 0.08f;
			reload = 16;
			shootCone = 50.0f;
			rotateSpeed = 2.5f;
			range = 440.0F;
			inaccuracy = 1;
			heatColor = NHBullets.blastEnergyPst.lightColor;
			recoil = 4.0F;
			shootSound = NHSounds.thermoShoot;
			coolant = new ConsumeCoolant(0.25f);
		}};
	}
	
	private static void loadFactories(){
		processorCompactor = new GenericCrafter("processor-compactor"){{
			requirements(Category.crafting, with(NHItems.zeta, 120, NHItems.irayrondPanel, 80, NHItems.presstanium, 60, NHItems.seniorProcessor, 40));
			health = 1500;
			armor = 4;
			size = 4;
			
			hasItems = hasPower = hasLiquids = true;
			
			consumePower(25);
			consumeLiquid(NHLiquids.irdryonFluid, 15 / 60f);
			consumeItem(Items.silicon, 6);
			
			outputItems = ItemStack.with(NHItems.juniorProcessor, 30, NHItems.seniorProcessor, 8);
			
			liquidCapacity = 50;
			itemCapacity = 90;
			craftTime = 120;
			
			squareSprite = false;
			
			Color senior = Pal.ammo.cpy().lerp(Color.red, 0.63f).lerp(Color.white, 0.2f);
			Color junior = Pal.bulletYellowBack;
			
			updateEffectChance = 0.07f;
			updateEffect = new Effect(30f, e -> {
				Rand rand = NHFunc.rand(e.id);
				Draw.color(senior, Color.white, e.fout() * 0.66f);
				Draw.alpha(0.55f * e.fout() + 0.5f);
				Angles.randLenVectors(e.id, 2, 4f + e.finpow() * 16f, (x, y) -> {
					Fill.square(e.x + x, e.y + y, e.fout() * rand.random(2, 4));
				});
				
				Draw.color(junior, Color.white, e.fout() * 0.66f);
				Draw.alpha(0.55f * e.fout() + 0.5f);
				Angles.randLenVectors(e.id + 12, 4, 4f + e.finpow() * 16f, (x, y) -> {
					Fill.square(e.x + x, e.y + y, e.fout() * rand.random(1, 3));
				});
			});
			
			
			/*craftEffect = new Effect(30f, e -> {
				Rand rand = NHFunc.rand(e.id);
				Lines.stroke(e.fout() * 1.5f);
				Draw.color(senior, Color.white, e.fout() * 0.66f);
				Angles.randLenVectors(e.id, 3, 4f + e.fin() * 22, (x, y) -> {
					Lines.square(e.x + x, e.y + y, e.finpow() * rand.random(7, 11));
				});
				
				Draw.color(junior, Color.white, e.fout() * 0.66f);
				Angles.randLenVectors(e.id + 6, 7, 4f + e.fin() * 22, (x, y) -> {
					Lines.square(e.x + x, e.y + y, e.finpow() * rand.random(4, 7));
				});
			});*/
			
			
			drawer = new DrawMulti(new DrawRegion("-bottom"),
					new DrawScanLine(){{
						colorFrom = junior;
						lineLength = 73 / 4f;
						scanLength = 73 / 4f;
						scanScl = 15f;
						scanAngle = 90;
						lineStroke -= 0.15f;
						totalProgressMultiplier = 1.25f;
						phaseOffset = Mathf.random() * 5f;
					}},
					
					new DrawScanLine(){{
						colorFrom = junior;
						lineLength = 73 / 4f;
						scanLength = 73 / 4f;
						scanScl = 15f;
						scanAngle = 0;
						totalProgressMultiplier = 1.55f;
						phaseOffset = Mathf.random() * 5f;
					}},
					
					new DrawScanLine(){{
						colorFrom = senior;
						lineLength = 73 / 4f;
						scanLength = 73 / 4f;
						scanScl = 15f;
						scanAngle = 90;
						totalProgressMultiplier = 1.35f;
						phaseOffset = Mathf.random() * 5f;
					}},
					
					new DrawScanLine(){{
						colorFrom = senior;
						lineLength = 73 / 4f;
						scanLength = 73 / 4f;
						scanScl = 8f;
						scanAngle = 0;
						lineStroke -= 0.15f;
						totalProgressMultiplier = 1.65f;
						phaseOffset = Mathf.random() * 5f;
					}},
					
					new DrawRegion("-mid"), new DrawLiquidTile(NHLiquids.irdryonFluid, 54 / 4f), new DrawDefault(),
					new DrawGlowRegion("-glow1"){{
						color = junior;
//						alpha = 1;
					}}, new DrawGlowRegion("-glow2"){{
						color = junior;
//						alpha = 1;
				
				}}, new DrawGlowRegion("-glow3"){{
					color = junior;
//					alpha = 0.76f;
				}}
			);
		}};
		
		ventExtractor = new AttributeCrafter("vent-extractor"){{
			requirements(Category.production, with(Items.graphite, 40, Items.beryllium, 60, NHItems.presstanium, 30, NHItems.juniorProcessor, 20));
			attribute = NHContent.quantum;
			group = BlockGroup.liquids;
			minEfficiency = 1f - 0.0001f;
			boostScale = 1/9f;
			baseEfficiency = 0f;
			displayEfficiency = false;
			
			squareSprite = false;
			
			updateEffectChance -= 0.05f;
			updateEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, Liquids.nitrogen.color.cpy().a(0.45f));
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.nitrogen, 5), new DrawRegion("-mid"), new DrawLiquidTile(NHLiquids.quantumEntity, 40f / 4f), new DrawRotator(3f, true), new DrawDefault());
			craftTime = 120f;
			size = 3;
			ambientSound = Sounds.hum;
			ambientSoundVolume = 0.06f;
			hasLiquids = true;
			boostScale = 1f / 9f;
			outputLiquid = new LiquidStack(Liquids.nitrogen, 16f / 60f);
			consumePower(2f);
			consumeLiquid(NHLiquids.quantumEntity, 0.1f);
			liquidCapacity = 60f;
		}};
		
		arkyciteCompactor = new AttributeCrafter("arkycite-compactor"){{
			baseEfficiency = 1f;
			attribute = NHContent.quantum;
			boostScale = 0.25f;
			maxBoost = 2;
			
			size = 3;
			
			squareSprite = false;
			health = 560;
			
			requirements(Category.crafting, ItemStack.with(Items.beryllium, 120, Items.silicon, 60, NHItems.presstanium, 40, Items.metaglass, 60));
			hasPower = hasLiquids = true;
			
			consumeLiquids(LiquidStack.with(NHLiquids.quantumEntity, 0.15f, Liquids.oil, 0.2f));
			
			outputLiquid = new LiquidStack(Liquids.arkycite, 0.5f);
			craftTime = 30f;
			
			liquidCapacity = 180;
			itemCapacity = 16;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawPistons(){{
				sinMag = 2.75f;
				sinScl = 5f;
				sides = 4;
				lenOffset += 0.25f;
				angleOffset = 45;
				sideOffset = Mathf.PI / 2f;
			}}, new DrawLiquidTile(Liquids.arkycite, 38 / 4f), new DrawDefault(), new DrawGlowRegion(){{
				color = NHLiquids.quantumEntity.color;
				alpha = 0.721f;
			}});
		}};
		
		ancimembraneConcentrator = new AttributeCrafter("ancimembrane-concentrator"){{
			size = 3;
			
			baseEfficiency = 0;
			minEfficiency = 1f;
			boostScale = 1f / 9f;
			lightRadius /= 2f;
			
			attribute = NHContent.quantum;
			requirements(Category.crafting, ItemStack.with(NHItems.seniorProcessor, 120, NHItems.multipleSteel, 90, NHItems.zeta, 45, NHItems.setonAlloy, 60));
			
			craftTime = 120f;
			
			health = 2200;
			armor = 12;
			craftEffect = NHFx.crossBlast(NHColor.ancient, 45f, 45f);
			craftEffect.lifetime *= 1.5f;
			updateEffect = NHFx.squareRand(NHColor.ancient, 5f, 15f);
			hasPower = hasItems = hasLiquids = true;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.quantumEntity), new DrawRegion("-bottom-2"),
				new DrawCrucibleFlame(){
					{
						flameColor = NHColor.ancient;
						midColor = Color.valueOf("2e2f34");
						circleStroke = 1.05f;
						circleSpace = 2.65f;
					}
				
					@Override
					public void draw(Building build){
						if(build.warmup() > 0f && flameColor.a > 0.001f){
							Lines.stroke(circleStroke * build.warmup());
							
							float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
							float a = alpha * build.warmup();
							
							Draw.blend(Blending.additive);
							Draw.color(flameColor, a);
							
							float base = (Time.time / particleLife);
							rand.setSeed(build.id);
							for(int i = 0; i < particles; i++){
								float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
								float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
								float len = particleRad * particleInterp.apply(fout);
								Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
								Fill.square(
										build.x + Angles.trnsx(angle, len),
										build.y + Angles.trnsy(angle, len),
										particleSize * fin * build.warmup(), 45
								);
							}
							
							Draw.blend();
							
							Draw.color(midColor, build.warmup());
							Lines.square(build.x, build.y, (flameRad + circleSpace + si) * build.warmup(), 45);
							
							Draw.reset();
						}
					}
				},
				new DrawDefault(),
				new DrawGlowRegion(){{
					color = NHColor.ancient;
					layer = -1;
					glowIntensity = 1.1f;
					alpha = 1.1f;
				}},
				new DrawRotator(1f, "-top"){
					@Override
					public void draw(Building build){
						Drawf.spinSprite(rotator, build.x + x, build.y + y, DrawFunc.rotator_90(DrawFunc.cycle(build.totalProgress() * rotateSpeed, 0, craftTime), 0.15f));
					}
				}
			);
			
			itemCapacity = 60;
			liquidCapacity = 60f;
			
			consumePower(12);
			consumeItems(with(NHItems.setonAlloy, 2, Items.tungsten, 6));
			consumeLiquid(NHLiquids.quantumEntity, 0.2f);
			outputItems = with(NHItems.ancimembrane, 3);
//			outputLiquid = new LiquidStack(Liquids.water, 0.5f);
		}};
		
		sandCracker = new MultiCrafter("sand-cracker"){{
			size = 2;
			requirements(Category.crafting, ItemStack.with(Items.lead, 40, Items.copper, 60, Items.graphite, 45));
			health = 320;
			craftTime = 45f;
			itemCapacity = 20;
			hasPower = hasItems = true;
			craftEffect = NHFx.hugeSmokeGray;
			updateEffect = new Effect(80f, e -> {
				Fx.rand.setSeed(e.id);
				Draw.color(Color.lightGray, Color.gray, e.fin());
				Angles.randLenVectors(e.id, 4, 2.0F + 12.0F * e.fin(Interp.pow3Out), (x, y) -> {
					Fill.circle(e.x + x, e.y + y, e.fout() * Fx.rand.random(1, 2.5f));
				});
			}).layer(Layer.blockOver + 1);
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawFrames(), new DrawArcSmelt(), new DrawDefault());
			consumePower(1.5f);
			setExchangeMap(Items.copper, 1, 1, Items.lead, 1, 1, Items.titanium, 1, 2, Items.thorium, 1, 3, Items.scrap, 2, 5, Items.beryllium, 2, 3, Items.tungsten, 1, 2);
			setOutput(Items.sand);
		}};
		
		multiplePresstaniumFactory = new GenericCrafter("multiple-presstanium-factory"){{
			size = 3;
			health = 540;
			requirements(Category.crafting, ItemStack.with(NHItems.presstanium, 80, NHItems.juniorProcessor, 60, Items.thorium, 80));
			craftTime = 60f;
			consumePower(5);
			consumeItems(with(Items.titanium, 6, Items.graphite, 2));
			consumeLiquid(NHLiquids.zetaFluid, 0.125f);
			outputItems = with(NHItems.presstanium, 8, Items.scrap, 1);
			
			itemCapacity = 30;
			liquidCapacity = 30;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.zetaFluid), new DrawRotator(), new DrawRegion("-middle"), new DrawCrucibleFlame(){{
				alpha = 0.375f;
				particles = 20;
				particleSize = 2f;
				particleRad = 6f;
				flameColor = NHLiquids.zetaFluid.color;
				midColor = NHLiquids.zetaFluid.color.cpy().lerp(Color.white, 0.1f);
			}}, new DrawDefault());
		}};
		
		oilRefiner = new GenericCrafter("oil-refiner"){{
			size = 2;
			requirements(Category.production, ItemStack.with(Items.metaglass, 30, NHItems.juniorProcessor, 20, Items.copper, 60, NHItems.metalOxhydrigen, 45));
			//NHTechTree.add(Blocks.oilExtractor, this);
			health = 200;
			craftTime = 90f;
			liquidCapacity = 60f;
			itemCapacity = 20;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.oil), new DrawDefault());
			consumePower(5f);
			consumeItems(new ItemStack(Items.sand, 5));
			outputLiquid = new LiquidStack(Liquids.oil, 15f / 60f);
		}};
		
		waterInstancer = new GenericCrafter("water-instancer"){{
			size = 1;
			updateEffect = new Effect(15, e -> {
				randLenVectors(e.id, 6, 4f + e.fin() * 5f, (x, y) -> {
					color(NHColor.darkEnrColor, NHColor.deeperBlue, e.fin());
					Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
				});
			});
			consumePower(0.5f);
			consumeLiquid(NHLiquids.quantumEntity, 0.1f);
			outputLiquid = new LiquidStack(Liquids.water, 12f / 60f);
			craftTime = 30f;
			requirements(Category.crafting, BuildVisibility.shown, with(Items.metaglass, 15, Items.copper, 30, NHItems.presstanium, 20));
			//NHTechTree.add(Blocks.mechanicalPump, this);
		}};
		
		xenMelter = new GenericCrafter("xen-melter"){{
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			itemCapacity = 12;
			liquidCapacity = 24;
			craftTime = 60f;
			drawer = new DrawMulti(new DrawDefault(), new DrawLiquidTile(NHLiquids.xenAlpha), new DrawRegion("-top"));
			
			craftEffect = NHFx.lightSkyCircleSplash;
			updateEffect = Fx.smeltsmoke;
			requirements(Category.crafting, BuildVisibility.shown, with(NHItems.juniorProcessor, 35, NHItems.metalOxhydrigen, 50, Items.thorium, 30, NHItems.presstanium, 25));
			consumePower(3f);
			consumeItems(new ItemStack(NHItems.metalOxhydrigen, 4), new ItemStack(NHItems.zeta, 4));
			outputLiquid = new LiquidStack(NHLiquids.xenAlpha, 12f / 60f);
		}};
		
		oreZeta = new OreBlock("ore-zeta"){{
			oreDefault = true;
			variants = 3;
			oreThreshold = 0.95F;
			oreScale = 20.380953F;
			itemDrop = NHItems.zeta;
			localizedName = itemDrop.localizedName;
			mapColor.set(itemDrop.color);
			useColor = true;
		}};
		
		
		presstaniumFactory = new GenericCrafter("presstanium-factory") {
			{
				Color color = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
				
				drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
					midColor = flameColor = color;
					flameRad /= 1.585f;
					particleStroke /= 1.35f;
					particleLen /= 1.25f;
				}}, new DrawDefault());
				
				requirements(Category.crafting, with(Items.silicon, 45, Items.lead, 115, Items.graphite, 25, Items.titanium, 100));
				hasItems = hasPower = true;
				craftTime = 60f;
				outputItem = new ItemStack(NHItems.presstanium, 2);
				size = 2;
				health = 320;
				updateEffect = Fx.smeltsmoke;
				craftEffect = NHFx.square(color, 32f, 5, 22f, 4);
				
				consumePower(3f);
				consumeItems(new ItemStack(Items.titanium, 2), new ItemStack(Items.graphite, 1));
			}
		};
		
		zetaFactorySmall = new GenericCrafter("small-zeta-crystal-factory") {
			{
				requirements(Category.crafting, with(Items.silicon, 15, Items.lead, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 75f;
				outputItem = new ItemStack(NHItems.zeta, 2);
				size = 1;
				health = 60;
				craftEffect = new Effect(15, e -> {
					randLenVectors(e.id, 6, 4f + e.fin() * 5f, (x, y) -> {
						color(NHItems.zeta.color, Color.white, e.fout() * 0.7f + 0.1f);
						Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
					});
				});
				
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.zeta.color){{
					flameRadius = 1.7f;
					flameRadiusMag = 0.2f;
					flameRadiusInMag = 0.125f;
					flameRadiusIn = 0.775f;
				}});
				
				consumePower(1.5f);
				consumeItem(Items.thorium, 3);
			}
		};
		
		thermoCorePositiveFactory = new GenericCrafter("thermo-core-positive-factory") {
			{
				requirements(Category.crafting, with(NHItems.seniorProcessor, 15, NHItems.presstanium, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 60f;
				outputItem = new ItemStack(NHItems.thermoCorePositive, 1);
				size = 1;
				health = 60;
				craftEffect = Fx.formsmoke;
				consumePower(3f);
				consumeItem(NHItems.thermoCoreNegative, 1);
			}
		};
		
		thermoCoreNegativeFactory = new GenericCrafter("thermo-core-negative-factory") {
			{
				requirements(Category.crafting, with(NHItems.seniorProcessor, 15, NHItems.presstanium, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 60f;
				outputItem = new ItemStack(NHItems.thermoCoreNegative, 1);
				size = 1;
				health = 60;
				craftEffect = Fx.formsmoke;
				consumePower(3f);
				consumeItem(NHItems.thermoCorePositive, 1);
			}
		};
		
		darkEnergyFactory = new GenericCrafter("dark-energy-factory") {
			{
				requirements(Category.crafting, with(NHItems.irayrondPanel, 60, NHItems.setonAlloy, 30, NHItems.seniorProcessor, 60));
				craftEffect = Fx.smeltsmoke;
				outputItem = new ItemStack(NHItems.darkEnergy, 2);
				craftTime = 90f;
				size = 2;
				loopSound = Sounds.electricHum;
				hasPower = hasItems = true;
				drawer = new DrawMulti(
						new DrawRegion("-bottom"),
						new DrawCrucibleFlame(){{
							flameColor = NHColor.darkEnrColor;
							midColor = NHColor.darkEnr;
							particleRad += 0.25f;
							circleSpace = 1f;
							alpha = 0.3f;
							particles = 18;
						}},
						new DrawDefault()
				);
				
				updateEffectChance = 0.1f;
				lightColor = NHColor.darkEnrColor;
				updateEffect = NHFx.squareRand(lightColor, 4f, 8f);
				craftEffect = new Effect(90f, e -> {
					Draw.color(lightColor, Color.white, e.fin() * 0.7f);
					Lines.stroke(e.fout() * 1.12f);
					Lines.square(e.x, e.y, e.finpow() * 12f, 45f);
				});
				
				consumeItems(new ItemStack(NHItems.thermoCoreNegative, 1), new ItemStack(NHItems.thermoCorePositive, 1));
				consumePower(20f);
			}
		};
		
		fusionEnergyFactory = new GenericCrafter("fusion-core-energy-factory") {
			{
				requirements(Category.crafting, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 50, Items.thorium, 60, Items.graphite, 30));
				craftEffect = Fx.smeltsmoke;
				outputItem = new ItemStack(NHItems.fusionEnergy, 3);
				craftTime = 90f;
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				loopSound = Sounds.laserbeam;
				loopSoundVolume /= 3f;
				drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawCrucibleFlame(){{
						particles = 45;
						particleRad = 11f;
					}},
					new DrawDefault(),
					new DrawLiquidTile(Liquids.water, 37 / 4f),
					new DrawRotator(true){{
						rotateSpeed = 4;
					}},
					new DrawRegion("-top")
				);
				
				updateEffectChance = 0.075f;
				updateEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, NHItems.fusionEnergy.color.cpy().a(0.53f));
				
				consumeLiquid(Liquids.water, 0.3f);
				consumeItems(new ItemStack(NHItems.presstanium, 2), new ItemStack(NHItems.zeta, 6));
				consumePower(6f);
			}
		};
		
		irayrondPanelFactory = new GenericCrafter("irayrond-panel-factory") {
			{
				itemCapacity = 40;
				requirements(Category.crafting, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 50, Items.plastanium, 60, Items.surgeAlloy, 75, Items.graphite, 30));
				craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 7, 4f + e.fin() * 18f, (x, y) -> {
					Draw.color(NHItems.irayrondPanel.color);
					Fill.square(e.x + x, e.y + y, e.fout() * 3f, 45);
				}));
				outputItem = new ItemStack(NHItems.irayrondPanel, 4);
				craftTime = 120f;
				health = 800;
				size = 4;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.irayrondPanel.color));
				consumeLiquid(NHLiquids.xenAlpha, 0.1f);
				consumeItems(new ItemStack(NHItems.presstanium, 4), new ItemStack(Items.surgeAlloy, 2));
				consumePower(2f);
			}
		};
		
		juniorProcessorFactory = new GenericCrafter("processor-junior-factory") {
			{
				requirements(Category.crafting, with(Items.silicon, 40, NHItems.presstanium, 30, Items.copper, 25, Items.lead, 25));
				craftEffect = Fx.none;
				outputItem = new ItemStack(NHItems.juniorProcessor, 3);
				craftTime = 180f;
				size = 2;
				hasPower = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.fusionEnergy.color));
				consumeItems(new ItemStack(Items.silicon, 2), new ItemStack(Items.copper, 4));
				consumePower(3f);
			}
		};
		
		seniorProcessorFactory = new GenericCrafter("processor-senior-factory") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 75, NHItems.juniorProcessor, 50, NHItems.presstanium, 25, Items.plastanium, 45));
				craftEffect = Fx.none;
				outputItem = new ItemStack(NHItems.seniorProcessor, 2);
				craftTime = 180f;
				size = 2;
				hasPower = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.fusionEnergy.color));
				consumeItems(new ItemStack(Items.surgeAlloy, 2), new ItemStack(NHItems.juniorProcessor, 4));
				consumePower(6f);
			}
		};
		
		irdryonFluidFactory = new GenericCrafter("irdryon-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 20, NHItems.seniorProcessor, 50, NHItems.presstanium, 80, NHItems.irayrondPanel, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.irdryonFluid, 8f / 60f);
				craftTime = 60;
				size = 2;
				drawer = new DrawMulti(new DrawDefault(), new DrawLiquidRegion(NHLiquids.irdryonFluid), new DrawFrames(){{
					frames = 5;
					sine = true;
				}}, new DrawRegion("-top"));
				itemCapacity = 20;
				hasPower = hasLiquids = hasItems = true;
				consumeLiquid(NHLiquids.xenBeta, 3/60f);
				consumeItems(new ItemStack(NHItems.irayrondPanel, 2));
				consumePower(4f);
			}
		};
		
		zetaFluidFactory = new AttributeCrafter("zeta-fluid-factory") {
			{
				squareSprite = false;
				baseEfficiency = 1f;
				maxBoost = 3f;
				attribute = NHContent.quantum;
				boostScale = 2f/9f;
				requirements(Category.crafting, with(Items.plastanium, 50, NHItems.juniorProcessor, 35, NHItems.presstanium, 80, Items.graphite, 65));
				craftEffect = Fx.formsmoke;
				outputLiquid = new LiquidStack(NHLiquids.zetaFluid, 15f / 60f);
				craftTime = 60f;
				health = 550;
				drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.zetaFluid, 2.5f), new DrawDefault());
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				consumeLiquid(Liquids.water, 0.1f);
				consumeItem(NHItems.zeta, 2);
				consumePower(8f);
			}
		};
		
		metalOxhydrigenFactory = new GenericCrafter("metal-oxhydrigen-factory") {
			{
				requirements(Category.crafting, with(Items.copper, 60, NHItems.juniorProcessor, 30, NHItems.presstanium, 25, Items.thorium, 25));
				craftEffect = NHFx.square(NHColor.lightSkyFront, 38, 3, 24, 3.2f);
				outputItem = new ItemStack(NHItems.metalOxhydrigen, 4);
				craftTime = 120f;
				size = 2;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawFactories() {
					{
						liquidColor = Liquids.water.color;
						drawRotator = 1.5f;
						drawTop = true;
					}
				};
				consumeLiquid(Liquids.water, 0.1f);
				consumeItem(Items.lead, 2);
				consumePower(2f);
			}
		};
		
		metalOxhydrigenFactoryLarge = new GenericCrafter("metal-oxhydrigen-factory-large"){{
			health = 450;
			size = 3;
			
			requirements(Category.crafting, with(NHItems.multipleSteel, 50, NHItems.juniorProcessor, 60, NHItems.presstanium, 55, NHItems.zeta, 85));
			
			itemCapacity = 40;
			liquidCapacity = 80;
			
			updateEffect = new Effect(40f, 80f, e -> {
				Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin() * 0.8f);
				Lines.stroke(2f * e.fout());
				Lines.spikes(e.x, e.y, 12 * e.finpow(), 1.5f * e.fout() + 4 * e.fslope(), 4, 45);
			});
			craftEffect = NHFx.square(NHColor.lightSkyFront, 38, 5, 34, 5);
			outputItem = new ItemStack(NHItems.metalOxhydrigen, 8);
			craftTime = 60f;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
				flameRad = 0.45f;
				circleStroke = 0.8f;
				flameRadiusMag = 0.15f;
				flameColor = NHColor.lightSkyFront;
				midColor = NHColor.lightSkyBack;
			}}, new DrawDefault(), new DrawRegion("-top"));
			consumeLiquid(Liquids.water, 0.25f);
			consumeItem(Items.lead, 3);
			consumePower(5f);
		}};
		
		thermoCoreFactory = new GenericCrafter("thermo-core-factory") {
			{
				requirements(Category.crafting, with(NHItems.irayrondPanel, 150, NHItems.seniorProcessor, 80, NHItems.presstanium, 250, Items.plastanium, 80));
				craftEffect = Fx.plasticExplosionFlak;
				outputItem = new ItemStack(NHItems.thermoCorePositive, 4);
				craftTime = 90f;
				itemCapacity = 30;
				health = 1500;
				size = 5;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawFactories() {
					{
						liquidColor = NHLiquids.zetaFluid.color;
						drawRotator = 1f;
						drawTop = false;
						pressorSet = new float[] {(craftTime / 6f), -4.2f, 45, 0};
					}
				};
				consumeLiquid(NHLiquids.zetaFluid, 0.2f);
				consumeItems(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.fusionEnergy, 4), new ItemStack(NHItems.metalOxhydrigen, 2));
				consumePower(5f);
			}
		};
		
		upgradeSortFactory = new GenericCrafter("upgradeSort-factory") {{
			requirements(Category.crafting, with(NHItems.setonAlloy, 160, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.thorium, 200));
			updateEffect = NHStatusEffects.quantization.effect;
			craftEffect = new Effect(25f, e -> {
				Draw.color(NHColor.darkEnrColor);
				Angles.randLenVectors(e.id, 4, 24 * e.fout() * e.fout(), (x, y) -> {
					Lines.stroke(e.fout() * 1.7f);
					Lines.square(e.x + x, e.y + y, 2f + e.fout() * 6f);
				});
				
			});
			outputItem = new ItemStack(NHItems.upgradeSort, 3);
			craftTime = 150f;
			size = 3;
			hasPower = hasItems = true;
			drawer = new DrawPrinter(outputItem.item) {{
				printColor = NHColor.darkEnrColor;
				lightColor = Color.valueOf("#E1BAFF");
				moveLength = 4.2f;
				time = 25f;
			}};
			clipSize = size * tilesize * 2f;
			consumeItems(new ItemStack(NHItems.setonAlloy, 4), new ItemStack(NHItems.seniorProcessor, 4));
			consumePower(10f);
		}};
		
		zetaFactoryLarge = new GenericCrafter("large-zeta-factory") {{
			itemCapacity = 40;
			requirements(Category.crafting, with(Items.plastanium, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25));
			outputItem = new ItemStack(NHItems.zeta, 6);
			craftTime = 60f;
			size = 2;
			craftEffect = Fx.formsmoke;
			updateEffect = NHFx.trailToGray;
			hasPower = hasItems = hasLiquids = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCultivator(){{
				bottomColor = NHLiquids.zetaFluid.color.cpy().lerp(Color.gray, 0.1f);
				plantColor = NHLiquids.zetaFluid.color;
				plantColorLight = NHLiquids.zetaFluid.color.cpy().lerp(Color.white, 0.1f);
			}}, new DrawDefault());
			
			consumeItem(Items.thorium, 6);
			consumePower(7f);
			consumeLiquid(Liquids.water, 0.075f);
		}};
		
		multipleSteelFactory = new GenericCrafter("multiple-steel-factory") {
			{
				requirements(Category.crafting, with(Items.graphite, 65, NHItems.juniorProcessor, 65, NHItems.presstanium, 100, Items.metaglass, 30));
				lightColor = NHItems.multipleSteel.color;
				updateEffect = EffectWrapper.wrap(Fx.smeltsmoke, lightColor);
				craftEffect = EffectWrapper.wrap(NHFx.square45_6_45, lightColor);;
				outputItem = new ItemStack(NHItems.multipleSteel, 4);
				craftTime = 60f;
				itemCapacity = 20;
				health = 600;
				size = 3;
				hasPower = hasItems = true;
				drawer = new DrawFactories() {
					{
						liquidColor = NHLiquids.xenAlpha.color;
						drawTop = false;
						pressorSet = new float[] {(craftTime / 4f), 3.8f, 0, 90};
					}
				};
				consumeLiquid(NHLiquids.xenAlpha, 0.2f);
				consumeItems(new ItemStack(Items.metaglass, 4), new ItemStack(Items.titanium, 2));
				consumePower(3f);
			}
		};
		
		reconstructPlastaniumFactory = new GenericCrafter("reconstruct-plastanium-factory"){{
			requirements(Category.crafting, with(NHItems.multipleSteel, 55, NHItems.juniorProcessor, 35, NHItems.presstanium, 60, Items.plastanium, 40, NHItems.zeta, 40));
			
			Color c = NHLiquids.zetaFluid.color.cpy();
			
			squareSprite = false;
			updateEffect = Fx.formsmoke;
			
			craftEffect = new Effect(30f, e -> {
				Draw.color(c);
				Lines.stroke(e.fslope() * 1.5f);
				Lines.square(e.x, e.y, size * tilesize * 0.5f * e.finpow() + 6, 45);
			});
			
			outputItem = new ItemStack(Items.plastanium, 5);
			craftTime = 120f;
			itemCapacity = 24;
			liquidCapacity = 20f;
			health = 1200;
			armor = 5;
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.zetaFluid, 2.5f), new DrawPressor(), new DrawDefault());
			consumeLiquid(NHLiquids.zetaFluid, 0.05f);
			consumeItems(new ItemStack(NHItems.presstanium, 2), new ItemStack(Items.beryllium, 5));
			consumePower(10f);
		}};
		
		multipleSurgeAlloyFactory = new GenericCrafter("multiple-surge-alloy-factory"){{
			requirements(Category.crafting, BuildVisibility.shown, with(NHItems.irayrondPanel, 80, NHItems.seniorProcessor, 60, Items.plastanium, 40, NHItems.presstanium, 100, Items.surgeAlloy, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(Items.surgeAlloy.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 3f, 45);
			}));
			outputItem = new ItemStack(Items.surgeAlloy, 6);
			craftTime = 90f;
			itemCapacity = 30;
			liquidCapacity = 20f;
			health = 500;
			size = 3;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.zetaFluid), new DrawDefault());
			consumeLiquid(NHLiquids.zetaFluid, 0.1f);
			consumeItems(new ItemStack(Items.thorium, 6), new ItemStack(NHItems.fusionEnergy, 1));
			consumePower(20f);
		}};
		
		setonAlloyFactory = new GenericCrafter("seton-alloy-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 80, NHItems.seniorProcessor, 60, NHItems.presstanium, 100, Items.surgeAlloy, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 4f + e.finpow() * 14f, (x, y) -> {
				Draw.color(NHLiquids.irdryonFluid.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 3f);
			}));
			outputItem = new ItemStack(NHItems.setonAlloy, 2);
			craftTime = 180f;
			itemCapacity = 24;
			liquidCapacity = 20f;
			health = 500;
			size = 3;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawPistons(){{
						sides = 4;
						sinMag = 3.9f;
						lenOffset = -1.785f;
						angleOffset = 45;
					}}, new DrawRegion("-shade"), new DrawDefault(), new DrawLiquidTile(NHLiquids.irdryonFluid, 39 / 4f), new DrawRegion("-top"));
		
			consumeLiquid(NHLiquids.irdryonFluid, 0.12f);
			consumeItems(new ItemStack(Items.plastanium, 4), new ItemStack(Items.graphite, 6));
			consumePower(12f);
		}};
		
		xenBetaFactory = new GenericCrafter("xen-beta-factory"){{
			requirements(Category.crafting, with(NHItems.metalOxhydrigen, 35, NHItems.juniorProcessor, 60, Items.plastanium, 20, NHItems.presstanium, 80, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenBeta.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenBeta, 6f / 60f);
			craftTime = 60f;
			itemCapacity = 12;
			liquidCapacity = 20f;
			health = 260;
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.xenBeta), new DrawDefault());
			consumeLiquid(NHLiquids.xenAlpha, 0.1f);
			consumeItem(NHItems.zeta, 2);
			consumePower(3f);
		}};
		
		xenGammaFactory = new GenericCrafter("xen-gamma-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 70, NHItems.seniorProcessor, 60, Items.surgeAlloy, 20, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenGamma.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenGamma, 12f / 60f);
			craftTime = 60f;
			itemCapacity = 12;
			liquidCapacity = 20f;
			health = 260;
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.xenGamma), new DrawDefault());
			consumeLiquid(NHLiquids.xenBeta, 0.2f);
			consumeItems(new ItemStack(Items.phaseFabric, 2));
			consumePower(8f);
		}};
	}
	
	private static void loadPowers(){
		ancientPowerNode = new PowerNode("ancient-power-node"){{
			size = 1;
			health = 3000;
			armor = 30;
			absorbLasers = true;
			autolink = false;
			
			maxNodes = 20;
			laserRange = 120;
			timers++;
			
			lightColor = laserColor1 = NHColor.ancientLight;
			laserColor2 = NHColor.ancient;
			update = true; //Worth it?
			
			buildType = () -> new PowerNodeBuild(){
				@Override
				public void updateTile(){
					if(damaged() && power.graph.getSatisfaction() > 0.5f){
						if(timer.get(90f)){
							Fx.healBlockFull.at(x, y, 0, NHColor.ancientLightMid, this.block);
							healFract(5 * power.graph.getSatisfaction());
						}
					}
				}
			};
			
			requirements(Category.power,ItemStack.with(NHItems.ancimembrane, 50, NHItems.seniorProcessor, 15));
		}};
		
		hydroFuelCell = new ConsumeGenerator("hydro-fuel-cell"){{
			size = 2;
			requirements(Category.power,ItemStack.with(NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 45, Items.copper, 200, Items.graphite, 80, Items.metaglass, 60));
			
			lightColor = Pal.techBlue;
			consumeEffect = EffectWrapper.wrap(NHFx.hugeSmokeLong, Liquids.hydrogen.color.cpy().lerp(Liquids.nitrogen.color, 0.4f).a(0.56f));
			generateEffect = new Effect(45f, e -> {
				Draw.color(lightColor, Color.white, e.fin() * 0.66f);
				Lines.stroke(e.fout() * 1.375f);
				Lines.spikes(e.x, e.y, 0.45f + 5 * e.finpow(), 5.5f * e.fout(), 4, 45);
			});
			//			//NHTechTree.add(Blocks.thoriumReactor,this);
			powerProduction = 1720f / 60f;
			loopSound = Sounds.flux;
			loopSoundVolume = 0.14F;
			itemCapacity = 40;
			liquidCapacity = 30;
			itemDuration = 240f;
			consumeItem(NHItems.metalOxhydrigen, 4);
			consumeLiquid(Liquids.nitrogen,2 / 60f);
			
			squareSprite = false;
			hasLiquids = hasItems = true;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.nitrogen, 2f), new DrawDefault(), new DrawGlowRegion(){{
				color = Liquids.hydrogen.color;
			}});
		}};
		
		zetaGenerator = new ConsumeGenerator("zeta-generator"){{
			requirements(Category.power,ItemStack.with(NHItems.metalOxhydrigen, 120, NHItems.juniorProcessor, 80, Items.plastanium, 80, NHItems.zeta,100, Items.copper, 150, Items.metaglass, 60));
			
			size = 3;
			
			powerProduction = 70f;
			loopSound = Sounds.electricHum;
			loopSoundVolume = 0.24F;
			itemCapacity = 30;
			liquidCapacity = 30;
			itemDuration = 2.55f * 60f;
			consumeItem(NHItems.zeta, 3);
			consumeLiquid(Liquids.cryofluid, 5.9f/60f);
			
			hasLiquids = hasItems = true;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.cryofluid), new DrawDefault(), new DrawGlowRegion(){{
				color = NHItems.zeta.color;
			}});
			
			lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
			generateEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
			//			explodeEffect = NHFx.lightningHitLarge(heatColor);
		}};
		
		hugeBattery = new Battery("huge-battery"){{
			size = 5;
			health = 1600;
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(Items.phaseFabric, 40, NHItems.juniorProcessor, 20, NHItems.zeta, 80, NHItems.presstanium, 35, Items.graphite, 50));
			//			//NHTechTree.add(Blocks.batteryLarge, this);
			consumePowerBuffered(750_000);
		}};
		
		armorBatteryLarge = new Battery("large-armor-battery"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 40, NHItems.juniorProcessor, 10, Items.lead, 40));
			size = 3;
			health = 3000;
			armor = 15;
			consumePowerBuffered(45000.0F);
		}};
		
		heavyPowerNode = new PowerNode("heavy-power-node"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.multipleSteel, 25, NHItems.juniorProcessor, 15, NHItems.zeta, 45, NHItems.presstanium, 40));
			//			//NHTechTree.add(Blocks.powerNodeLarge, this);
			size = 3;
			maxNodes = 30;
			laserRange = 20F;
			health = 1200;
			armor = 3;
			
			//			consumePowerBuffered(30000f);
		}};
		
		hyperGenerator = new HyperGenerator("hyper-generator"){{
			size = 8;
			health = 12500;
			powerProduction = 2000f;
			updateLightning = updateLightningRand = 3;
			effectColor = NHColor.thermoPst;
			itemCapacity = 40;
			itemDuration = 180f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.1F;
			toApplyStatus.add(NHStatusEffects.phased, StatusEffects.overclock);
			
			consumePower(100.0F);
			consumeItems(ItemStack.with(NHItems.thermoCoreNegative, 2, Items.phaseFabric, 4)).optional(true, true);
			consumeItems(new ItemStack(NHItems.metalOxhydrigen, 8), new ItemStack(NHItems.thermoCorePositive, 4));
			consumeLiquid(NHLiquids.zetaFluid, 0.25F);
			requirements(Category.power, BuildVisibility.shown, with(NHItems.upgradeSort, 1000, NHItems.setonAlloy, 600, NHItems.irayrondPanel, 400, NHItems.presstanium, 1500, Items.surgeAlloy, 250, Items.metaglass, 250));
			//			//NHTechTree.add(fusionCollapser, this);
		}};
	}
	
	private static void loadWalls(){
		final int healthMult2 = 4, healthMult3 = 9;
		
		ancientLaserWall = new LaserWallBlock("ancient-laser-wall"){{
			size = 2;
			consumePowerCond(80f, LaserWallBuild::canActivate);
			health = 8000;
			range = 800;
			
			armor = 20f;
			crushDamageMultiplier = 0.025f;
			
			generateType = new Shooter(300){{
				Color c = NHColor.ancient;
				colors = new Color[]{c.cpy().mul(0.9f, 0.9f, 0.9f, 0.3f), c.cpy().mul(1f, 1f, 1f, 0.6f), c, Color.white};
				hitColor = lightColor = lightningColor = c;
				width = 4.5f;
				oscMag = 0.5f;
				
				status = NHStatusEffects.entangled;
				statusDuration = 60f;
				lightningDamage = 200;
			}
				
				@Override
				public void hit(Bullet b, float x, float y){
					super.hit(b, x, y);
					
					for(int i = 0; i < 2; i++){
						Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, x, y, Mathf.range(180), lightningLength + Mathf.random(lightningLengthRand));
					}
				}
			};
			
			requirements(Category.defense, with(NHItems.seniorProcessor, 120, NHItems.ancimembrane, 200, NHItems.zeta, 800));
		}};
		
		laserWall = new LaserWallBlock("laser-wall"){{
			size = 3;
			consumePowerCond(30f, LaserWallBuild::canActivate);
			health = 4000;
			
			armor = 10f;
			
			requirements(Category.defense, with(NHItems.juniorProcessor, 120, Items.copper, 350, NHItems.multipleSteel, 80, NHItems.zeta, 180, Items.graphite, 80));
			//			NHTechTree.add(Blocks.forceProjector, this);
		}};
		
		setonWall = new Wall("seton-wall"){{
			armor = 15f;
			
			insulated = true;
			crushDamageMultiplier = 0.15f;
			
			size = 1;
			health = 1250;
			chanceDeflect = 15.0F;
			flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5, NHItems.irayrondPanel, 10, Items.silicon, 15, NHItems.presstanium, 15));

			buildVisibility = BuildVisibility.editorOnly;
		}};
		
		setonWallLarge = new Wall("seton-wall-large"){{
			armor = 15f;
			
			insulated = true;
			crushDamageMultiplier = 0.15f;
			
			size = 2;
			health = 1250 * healthMult2;
			chanceDeflect = 15.0F;
			flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5 * healthMult2, NHItems.irayrondPanel, 10 * healthMult2, Items.silicon, 15 * healthMult2, NHItems.presstanium, 15 * healthMult2));

			buildVisibility = BuildVisibility.editorOnly;
		}};

		
		heavyDefenceWall = new ShieldWall("heavy-defence-wall"){{
			shieldHealth = 1000f;
			breakCooldown = 30f * 10f;
			regenSpeed = 5f;
			glowColor = NHColor.darkEnrColor.cpy().lerp(NHColor.lightSkyFront, 0.3f).a(0.5f);
			consumePower(8f / 60f);
			crushDamageMultiplier = 0.05f;
			
			outputsPower = false;
			hasPower = true;
			consumesPower = true;
			conductivePower = true;
			
			armor = 30f;
			
			size = 1;
			health = 1750;
			absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20));
			buildVisibility = BuildVisibility.editorOnly;

			buildType = () -> new ShieldWallBuild(){
				@Override
				public void draw(){
					Draw.rect(block.region, x, y);
					
					if(shieldRadius > 0){
						float radius = shieldRadius * tilesize / 2;
						
						Draw.z(Layer.shields);
						
						Draw.color(team.color, Color.white, Mathf.clamp(hit));
						
						if(renderer.animateShields){
							Fill.square(x, y, radius);
						}else{
							Lines.stroke(1.5f);
							Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
							Fill.square(x, y, radius);
							Draw.alpha(1f);
							Lines.poly(x, y, 4, radius, 45f);
							Draw.reset();
						}
						
						Draw.reset();
						
						Drawf.additive(glowRegion, glowColor, (1f - glowMag + Mathf.absin(glowScl, glowMag)) * shieldRadius, x, y, 0f, Layer.blockAdditive);
					}
				}
			};
		}};
		
		heavyDefenceWallLarge = new ShieldWall("heavy-defence-wall-large"){{
			shieldHealth = 1200f * healthMult2;
			breakCooldown = 30f * 10f;
			regenSpeed = 5f * healthMult2;
			glowColor = NHColor.darkEnrColor.cpy().lerp(NHColor.lightSkyFront, 0.3f).a(0.5f);
			consumePower(8f / 60f * healthMult2);
			
			armor = 30f;
			crushDamageMultiplier = 0.05f;
			
			outputsPower = false;
			hasPower = true;
			consumesPower = true;
			conductivePower = true;
			
			size = 2;
			health = 1750 * healthMult2;
			absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10 * healthMult2, NHItems.presstanium, 20 * healthMult2));

			buildVisibility = BuildVisibility.editorOnly;

		}};
		
		heavyDefenceDoor = new AutoDoor("heavy-defence-door"){{
			size = 1;
			health = 1750;
			armor = 30;
			crushDamageMultiplier = 0.05f;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20, NHItems.juniorProcessor, 5));
			buildVisibility = BuildVisibility.editorOnly;
		}};
		
		heavyDefenceDoorLarge = new AutoDoor("heavy-defence-door-large"){{
			size = 2;
			health = 1750 * healthMult2;
			armor = 30;
			openfx = Fx.dooropenlarge;
			closefx = Fx.doorcloselarge;
			crushDamageMultiplier = 0.05f;
			requirements(Category.defense, with(NHItems.setonAlloy, 10 * healthMult2, NHItems.presstanium, 20 * healthMult2, NHItems.juniorProcessor, 5 * healthMult2));

			buildVisibility = BuildVisibility.editorOnly;
		}};
		
		
		chargeWall = new ChargeWall("charge-wall"){{
			requirements(Category.defense, with(NHItems.seniorProcessor, 5, NHItems.upgradeSort, 5));
			size = 1;
			absorbLasers = true;
			range = 120;
			health = 2000;
			effectColor = NHColor.lightSkyBack;
			crushDamageMultiplier = 0.025f;

			armor = 32f;
			buildVisibility = BuildVisibility.editorOnly;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.defense, ItemStack.mult(chargeWall.requirements, healthMult2));
			size = 2;
			absorbLasers = true;
			range = 200;
			health = 2000 * healthMult2;
			effectColor = NHColor.lightSkyBack;
			crushDamageMultiplier = 0.025f;


			armor = 32f;
			buildVisibility = BuildVisibility.editorOnly;
		}};
		
		insulatedWall = new Wall("insulated-wall"){{
			size = 1;
			health = 300;
			buildCostMultiplier = 0.4f;
			requirements(Category.defense, with(Items.titanium, 10, Items.copper, 5));
			insulated = true;
			absorbLasers = true;

			buildVisibility = BuildVisibility.editorOnly;
		}};
		
	}
	
	public static void load() {
		blaster = new ShockwaveGenerator("blaster"){{
			requirements(Category.defense, with(NHItems.presstanium, 80, Items.graphite, 100,Items.thorium, 100, NHItems.juniorProcessor, 60, NHItems.multipleSteel, 30));
			
			squareSprite = false;
			size = 3;
			chargerOffset = 5.65f;
			rotateOffset = -45f;
			damage = 150;
			lightningDamage = 200;
			generateLiNum = 3;
			generateLiLen = 12;
			generateLenRand = 20;
			gettingBoltNum = 1;
			lightningColor = NHColor.darkEnrColor;
			generateEffect = NHFx.blastgenerate;
			acceptEffect = NHFx.blastAccept;
			blastSound = Sounds.explosionbig;
			status = NHStatusEffects.emp2;
			range = 240;
			health = 1200;
			knockback = 10f;
			consumePower(8f);
			itemCapacity = 30;
			consumeItem(NHItems.zeta, 3);
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
				midColor = flameColor = NHColor.darkEnrColor;
				circleStroke = 1.125f;
				circleSpace = 1.9f;
			}}, new DrawDefault());
		}};
		
		reinForcedLiquidSource = new LiquidSource("reinforced-liquid-source"){{
			size = 1;
			health = 800;
			armor = 10;
			buildVisibility = BuildVisibility.sandboxOnly;
			
			category = Category.liquid;
			
			buildType = () -> new LiquidSourceBuild(){
				@Override
				public boolean canPickup(){
					return false;
				}
				
				@Override
				public void write(Writes write){
					write.str(source == null ? "null-liquid" : source.name);
				}
				
				@Override
				public void read(Reads read, byte revision){
					source = content.liquid(read.str());
				}
			};
		}};
		
		reinForcedItemSource = new ItemSource("reinforced-item-source"){{
			size = 1;
			health = 800;
			armor = 10;
			buildVisibility = BuildVisibility.sandboxOnly;
			
			buildType = () -> new ItemSourceBuild(){
				@Override
				public boolean canPickup(){
					return false;
				}
				
				@Override
				public void draw(){
					if (this.block.variants != 0 && this.block.variantRegions != null) {
						Draw.rect(this.block.variantRegions[Mathf.randomSeed(this.tile.pos(), 0, Math.max(0, this.block.variantRegions.length - 1))], this.x, this.y, this.drawrot());
					} else {
						Draw.rect(this.block.region, this.x, this.y, this.drawrot());
					}
					
					this.drawTeamTop();
					
					if(outputItem == null){
						Draw.rect(NHContent.crossRegion, x, y);
					}else{
						Draw.color(outputItem.color);
						Draw.rect(NHContent.sourceCenter, x, y);
						Draw.color();
					}
				}
				
				@Override
				public void write(Writes write){
					write.str(outputItem == null ? "null-item" : outputItem.name);
				}
				
				@Override
				public void read(Reads read, byte revision){
					outputItem = content.item(read.str());
				}
			};
		}
			@Override
			public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
				drawPlanConfigCenter(plan, plan.config, NewHorizon.name("source-center"), true);
			}
		};
		
		multiSteelItemBridge = new FloatItemBridge("multi-steel-item-bridge"){{
			health = 560;
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.zeta, 5, Items.graphite, 10));
			
			fadeIn = moveArrows = true;
			hasPower = false;
			range = 8;
			arrowSpacing = 8f;
			speed = 40;
			bufferCapacity = 20;
		}};
		
		multiSteelLiquidBridge = new FloatLiquidBridge("multi-steel-liquid-bridge"){{
			health = 560;
			requirements(Category.liquid, with(NHItems.multipleSteel, 5, NHItems.zeta, 5, Items.metaglass, 10));

			fadeIn = moveArrows = true;
			arrowSpacing = 8;
			range = 8;
			hasPower = false;
		}};
		
		fireExtinguisher = new FireExtinguisher("fire-extinguisher"){{
			size = 3;
			health = 920;
			intensity = 1600;
			
			consumeItem(NHItems.metalOxhydrigen, 2);
			consumePowerCond(3f, FireExtinguisherBuild::isActive);
			
			requirements(Category.defense, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 120, Items.copper, 80, Items.graphite, 60));
//			//NHTechTree.add(Blocks.tsunami, this);
		}};
		
		multiConduit = new FloatConduit("multi-conduit"){{
			size = 1;
			health = 560;
			liquidCapacity = 20.0F;
			liquidPressure = 1.1f;
			leaks = false;

			junctionReplacement = multiJunction;
			bridgeReplacement = rotBridgeReplacement = multiSteelLiquidBridge;

			
			requirements(Category.liquid, with(NHItems.multipleSteel, 1, Items.copper, 2, Items.metaglass, 1));
//			//NHTechTree.add(Blocks.pulseConduit, this);
		}};
		
		multiRouter = new FloatMultiRouter("multi-router"){{
			size = 1;
			health = 560;
			speed = 2f;
			
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.lead, 5));
//			//NHTechTree.add(Blocks.router, this);
		}};
		
		multiJunction = new FloatMultiJunction("multi-junction"){{
			size = 1;
			health = 560;
			speed = 12f;
			capacity = 12;
			
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.copper, 5));
//			//NHTechTree.add(Blocks.junction, this);
		}};

		/*
		beamDrill = new LaserBeamDrill("beam-drill"){{
			size = 4;
			health = 960;
			tier = 6;
			drillTime = 150f;
			liquidBoostIntensity = 1.65f;
			warmupSpeed = 0.001f;
			consumePower(6);
			consumeLiquid(Liquids.water, 0.1f).optional(true, true);
			requirements(Category.production, BuildVisibility.shown, with(NHItems.juniorProcessor, 60, NHItems.multipleSteel, 45, NHItems.zeta, 60, NHItems.presstanium, 40, Items.lead, 80));
			//NHTechTree.add(Blocks.blastDrill, this);
		}};

		 */
		
		airRaider = new AirRaider("air-raider"){{
			requirements(Category.defense, with(NHItems.upgradeSort, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 120, NHItems.juniorProcessor, 100, Items.phaseFabric, 150));
			
			shoot = new ShootSummon(0, 0, 120, 0){{
				shots = 4;
				shotDelay = 8f;
			}};
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame(){{
				alpha = 0.375f;
				particles = 20;
				particleSize = 2.6f;
				particleRad = 7f;
				flameColor = NHColor.darkEnrColor;
				midColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.1f);
			}}, new DrawDefault());
			
			size = 3;
			consumePowerCond(6f, AirRaiderBuild::isCharging);
			consumeItem(NHItems.darkEnergy, 4);
			itemCapacity = 16;
			health = 4500;
			
			triggeredEffect = new Effect(45f, e -> {
				Draw.color(NHColor.darkEnrColor);
				Lines.stroke(e.fout() * 2f);
				Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
			});
			
			bullet = NHBullets.airRaidBomb;
		}};
		
		remoteStorage = new RemoteCoreStorage("remote-vault"){{
			size = 3;
			health = 960;
			consumePower(10);
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 150, NHItems.multipleSteel, 120));
		}};
		
		unitIniter = new UnitSpawner("unit-initer");
/*		shieldProjector = new ShieldProjector("shield-projector"){{
			consumePower(1f);
			consumePowerCond(8f, ShieldProjectorBuild::isCharging);
			size = 3;
			itemCapacity = 20;
			consumeItem(NHItems.fusionEnergy, 5);
			requirements(Category.defense, BuildVisibility.shown, with(Items.copper, 300, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.plastanium, 75, NHItems.multipleSteel, 120));
			//NHTechTree.add(Blocks.forceProjector, this);
		}};
		
		scrambler = new AirRaider("scrambler"){{
			requirements(Category.effect, with(NHItems.multipleSteel, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 100, Items.plastanium, 100, Items.phaseFabric, 150));
			
			range =  720f;
			
			size = 3;
			consumePowerCond(8f, AirRaiderBuild::isCharging);
			consumeItems(with(NHItems.juniorProcessor, 2, Items.phaseFabric, 1, NHItems.metalOxhydrigen, 1));
			itemCapacity = 12;
			burstSpacing = 30f;
			salvos = 2;
			health = 2500;
			
			lightColor = NHColor.thermoPst;
			
			triggeredEffect = new Effect(45f, e -> {
				Draw.color(lightColor);
				Lines.stroke(e.fout() * 2f);
				Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
			});
			
			bulletHitter = new EmpBulletType(){{
				speed = 6f;
				damage = 100f;
				sprite = "missile-large";
				
				status = NHStatusEffects.scrambler;
				statusDuration = 480f;
				
				trailLength = 14;
				trailWidth = 1.5f;
				
				hitColor = trailColor = backColor = lightColor = lightningColor = NHColor.thermoPst;
				frontColor = Color.white;
				
				homingRange = 300f;
				homingDelay = 6f;
				homingPower = 0f; //Custome Homing
				
				hitSound = Sounds.explosionbig;
				trailChance = 0.075f;
				trailEffect = NHFx.polyTrail;
				drawSize = 120f;
				
				hitPowerEffect = applyEffect = NHFx.lightningHitSmall(hitColor);
				
				collides = false;
				scaleVelocity = true;
				hitShake = despawnShake = 16f;
				lightning = 3;
				lightningCone = 360;
				lightningLengthRand = lightningLength = 20;
				shootEffect = NHFx.instShoot(backColor);
				smokeEffect = NHFx.square(backColor, 50f, 3, 80f, 5f);
				shrinkX = shrinkY = 0;
				radius = 100f;
				splashDamageRadius = 60f;
				splashDamage = lightningDamage = damage;
				height = 22f;
				width = 8f;
				lifetime = 120f;
				
				despawnEffect = new Effect(60f, 150f, e -> {
					Draw.color(Color.white, hitColor, 0.3f + e.fin());
					Lines.stroke(2f * e.fout());
					
					float rad = radius * 1.25f * e.fin(Interp.pow4Out);
					Lines.circle(e.x, e.y, rad);
					
					Fill.circle(e.x, e.y, radius / 5f * e.fout());
					Drawf.light(e.x, e.y, rad * 1.2f, hitColor, 0.7f);
				});
				
				hitEffect = new OptionalMultiEffect(NHFx.square(hitColor, 100f, 3, 80f, 8f), NHFx.blast(hitColor, radius));
			}
				
				@Override
				public void update(Bullet b){
					super.update(b);
					
					TeamHealthc target = Units.closestEnemy(b.team, b.x, b.y, homingRange * 2f, Unitc::isPlayer);
					
					if(target != null){
						if(b.within(target, radius / 4))b.time(b.lifetime());
						b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), Time.delta * 7));
					}
				}
				
				@Override
				public void despawned(Bullet b){
					super.despawned(b);
					
					Units.nearbyEnemies(b.team, b.x, b.y, radius, u -> {
						if(u.isLocal()) ScreenInterferencer.generate(600);
					});
					
					for(int i = 0; i < 6; i++){
						Vec2 v = Tmp.v6.rnd(radius + Mathf.random(radius)).add(b).cpy();
						NHFx.chainLightningFade.at(b.x, b.y, 12f, hitColor, v);
						Time.run(NHFx.chainLightningFade.lifetime * NHFx.lightningAlign, () -> {
							NHFx.lightningHitSmall.at(v.x, v.y, 20f, hitColor);
						});
					}
				}
			};
		}};
		*/
		bombLauncher = new BombLauncher("bomb-launcher"){{
			requirements(Category.defense, with(NHItems.multipleSteel, 100, NHItems.presstanium, 260, NHItems.juniorProcessor, 120, Items.thorium, 500, Items.surgeAlloy, 75));
			//NHTechTree.add(Blocks.massDriver, this);
			size = 3;
			storage = 1;
			
			bullet = new EffectBulletType(15f){{
				trailChance = 0.25f;
				trailEffect = NHFx.trailToGray;
				trailParam = 1.5f;
				
				smokeEffect = NHFx.hugeSmoke;
				shootEffect = NHFx.boolSelector;
				
				scaledSplashDamage = true;
				collidesTiles = collidesGround = collides = true;
				damage = 100;
				splashDamage = 800f;
				lightningDamage = 400f;
				lightColor = lightningColor = trailColor = hitColor = NHColor.thurmixRed;
				lightning = 3;
				lightningLength = 8;
				lightningLengthRand = 16;
				splashDamageRadius = 120f;
				hitShake = despawnShake = 20f;
				hitSound = despawnSound = Sounds.explosionbig;
				hitEffect = despawnEffect = new OptionalMultiEffect(NHFx.crossBlast(hitColor, splashDamageRadius * 1.25f), NHFx.blast(hitColor, splashDamageRadius * 1.5f));
			}};
			
			reloadTime = 300f;
			
			consumePowerCond(6f, BombLauncherBuild::isCharging);
			consumeItem(NHItems.fusionEnergy, 2);
			itemCapacity = 16;
			health = 1200;
		}};
		
		lableSpawner = new LabelSpawner("label-spawner");
		
		hyperspaceWarper = new HyperSpaceWarper("hyper-space-warper"){{
			size = 4;
			health = 2250;
			squareSprite = false;
			
			completeEffect = NHFx.square45_4_45;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawPlasma(){{
				plasma1 = NHColor.darkEnrColor;
				plasma2 = NHColor.darkEnr;
			}}, new DrawDefault());
			
			hasPower = hasItems = hasLiquids = true;
			itemCapacity = 20;
			liquidCapacity = 120;
			consumeItem(NHItems.fusionEnergy, 5);
			consumePower(12f);
			consumeLiquid(NHLiquids.quantumEntity, 0.5f);
			
			requirements(Category.units, BuildVisibility.shown, with(NHItems.ancimembrane, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 450, NHItems.zeta, 200));
		}};
		
		gravityTrapSmall = new GravityWell("gravity-trap-small"){{
			size = 2;
			health = 640;
			range = 16;
			
			consumePower(5f);
			requirements(Category.units, BuildVisibility.shown, with(Items.titanium, 60, NHItems.metalOxhydrigen, 80, NHItems.juniorProcessor, 50, Items.copper, 200, NHItems.zeta, 50));
			//NHTechTree.add(hyperspaceWarper, this);
		}};
		
		gravityTrap = new GravityWell("gravity-gully"){{
			size = 3;
			health = 1250;
			
			consumePower(8f);
			requirements(Category.units, BuildVisibility.shown, with(Items.plastanium, 80, NHItems.multipleSteel, 80, NHItems.juniorProcessor, 80, Items.copper, 200));
			//NHTechTree.add(gravityTrapSmall, this);
		}};
		
		irdryonTank = new LiquidRouter("irdryon-tank"){{
			requirements(Category.liquid, with(NHItems.metalOxhydrigen, 25, NHItems.multipleSteel, 40, Items.metaglass, 25));
//			//NHTechTree.add(Blocks.liquidTank, this);
			size = 3;
			underBullets = true;
			liquidCapacity = 4500;
			armor = 12f;
			health = 3200;
		}};
		
		assignOverdrive = new AssignOverdrive("assign-overdrive"){{
			requirements(Category.effect, with(NHItems.irayrondPanel, 120, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.plastanium, 80, Items.surgeAlloy, 75));
			//NHTechTree.add(Blocks.overdriveProjector, this);
			consumePower(14.0F);
			size = 3;
			range = 240.0F;
			speedBoost = 4f;
			useTime = 300.0F;
			hasBoost = true;
			squareSprite = false;
			strokeOffset = -0.05f;
			strokeClamp = 0.06f;
			schematicPriority = -20;
			consumeItem(Items.phaseFabric).boost();
			consumeLiquid(NHLiquids.xenBeta, 0.1f);
		}};
		
		largeMendProjector = new MendProjector("large-mend-projector"){{
			size = 3;
			reload = 180f;
			useTime = 600f;
			healPercent = 15;
			requirements(Category.effect, with(NHItems.presstanium, 60, NHItems.juniorProcessor, 50, Items.plastanium, 40, Items.thorium, 80));
//			//NHTechTree.add(Blocks.mendProjector, this);
			consumePower(2F);
			range = 160.0F;
			phaseBoost = 12f;
			phaseRangeBoost = 60.0F;
			health = 980;
			consumeItem(NHItems.juniorProcessor).boost();
		}};
		
		armorPowerNode = new PowerNode("armor-power-node"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 25, NHItems.juniorProcessor, 5, Items.lead, 25));
//			//NHTechTree.add(Blocks.powerNodeLarge, this);
			size = 2;
			maxNodes = 12;
			laserRange = 8.5F;
			health = 1650;
			armor = 5;
		}};
		
		largeWaterExtractor = new SolidPump("large-water-extractor"){{
			size = 3;
			pumpAmount = 0.3f;
			requirements(Category.production, ItemStack.with(NHItems.presstanium, 50, NHItems.juniorProcessor, 45, Items.thorium, 60, Items.metaglass, 30));
//			//NHTechTree.add(Blocks.waterExtractor, this);
			result = Liquids.water;
			liquidCapacity = 60.0F;
			rotateSpeed = 1.4F;
			attribute = Attribute.water;
			consumePower(4f);
		}};
		
		rapidUnloader = new AdaptUnloader("rapid-unloader"){{
			speed = 0.5f;
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.presstanium, 20, Items.lead, 15, NHItems.juniorProcessor, 25));
//			//NHTechTree.add(Blocks.unloader, this);
		}};
		
		multiEfficientConveyor = new FloatConveyor("multi-efficient-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2,NHItems.multipleSteel, 1));
//			//NHTechTree.add(Blocks.titaniumConveyor, this);
			speed = 0.12f;
			displayedSpeed = 18f;
			health = 560;
			armor = 3;
			junctionReplacement = multiJunction;
			bridgeReplacement = multiSteelItemBridge;
		}};
		
		multiArmorConveyor = new FloatArmoredConveyor("multi-armor-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2, NHItems.multipleSteel, 2, Items.thorium, 1));
//			//NHTechTree.add(Blocks.armoredConveyor, this);
			speed = 0.12f;
			displayedSpeed = 18f;
			health = 800;
			armor = 5;
			junctionReplacement = multiJunction;
		}};

		multiConveyor = new FloatStackConveyor("multi-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2,NHItems.irayrondPanel, 2, NHItems.juniorProcessor, 1));
//			//NHTechTree.add(Blocks.plastaniumConveyor, this);
			speed = 0.125f;
			health = 720;
			armor = 6;
			itemCapacity = 20;
			recharge = 1f;
			
			loadEffect = unloadEffect = new Effect(30f, e -> {
				Lines.stroke(1.5f * e.fout(Interp.pow2Out), NHItems.multipleSteel.color);
				Lines.square(e.x, e.y, tilesize / 8f * Mathf.sqrt2 * (e.fin(Interp.pow2Out) * 3 + 1f), 45f);
			});
		}};
		
		largeShieldGenerator = new ForceProjector("large-shield-generator") {{
			size = 4;
			radius = 220f;
			shieldHealth = 20000f;
			cooldownNormal = 12f;
			cooldownLiquid = 6f;
			cooldownBrokenBase = 9f;
			itemConsumer = consumeItem(NHItems.fusionEnergy).boost();
			phaseUseTime = 5 * 60f;
			phaseRadiusBoost = 15 * 8f;
			phaseShieldBoost = 20000f;
			consumePower(15F);
			requirements(Category.effect, with(NHItems.seniorProcessor, 120, Items.lead, 250, Items.graphite, 180, NHItems.presstanium, 150, Items.phaseFabric, 120, NHItems.multipleSteel, 50));
		}};
		
		irdryonVault = new StorageBlock("irdryon-vault"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            health = 3500;
            itemCapacity = 2500;
        }};
		
		jumpGatePrimary = new JumpGate("jump-gate-primary"){{
			size = 3;
			atlasSizeScl = 0.55f;
			squareStroke = 1.75f;
			health = 1800;
			spawnDelay = 90f;
			spawnReloadTime = 750f;
			range = 160f;
			
			armor = 5f;
			buildSpeedMultiplierCoefficient = 0.5f;
			
			itemCapacity = 500;
			
			consumePowerCond(8, JumpGateBuild::isCalling);
			
			requirements(Category.units, BuildVisibility.shown, with(
					Items.copper, 250,
					Items.lead, 200,
					Items.titanium, 80,
					Items.silicon, 80
			));
			
			addSets(
				new UnitSet(UnitTypes.poly, new byte[]{NHUnitTypes.OTHERS, 2}, 45 * 60f,
						with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
				),
				new UnitSet(NHUnitTypes.assaulter, new byte[]{NHUnitTypes.AIR_LINE_2, 1}, 15 * 60f,
					with(Items.silicon, 16, Items.copper, 30, NHItems.zeta, 20)
				),
				new UnitSet(NHUnitTypes.sharp, new byte[]{NHUnitTypes.AIR_LINE_1, 1}, 15 * 60f,
					with(Items.titanium, 30, Items.silicon, 15)
				),
				new UnitSet(NHUnitTypes.branch, new byte[]{NHUnitTypes.AIR_LINE_1, 2}, 30 * 60f,
					with(Items.titanium, 60, Items.silicon, 45, Items.graphite, 30)
				),
				new UnitSet(NHUnitTypes.origin, new byte[]{NHUnitTypes.GROUND_LINE_1, 1}, 20 * 60f,
					with(Items.lead, 15, Items.silicon, 10 ,Items.copper, 10)
				),
				new UnitSet(NHUnitTypes.thynomo, new byte[]{NHUnitTypes.GROUND_LINE_1, 2}, 35 * 60f,
					with(Items.lead, 30, Items.titanium, 60, Items.graphite, 45, Items.silicon, 30)
				),
				new UnitSet(NHUnitTypes.ghost, new byte[]{NHUnitTypes.NAVY_LINE_1, 3}, 60 * 60f,
					ItemStack.with(NHItems.presstanium, 60, NHItems.multipleSteel, 50, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.warper, new byte[]{NHUnitTypes.AIR_LINE_1, 3}, 65 * 60f,
					with(Items.thorium, 90, Items.graphite, 50, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.aliotiat, new byte[]{NHUnitTypes.GROUND_LINE_1, 3}, 55 * 60f,
					with(Items.copper, 120, NHItems.multipleSteel, 50, NHItems.presstanium, 60, NHItems.juniorProcessor, 45)
				),
				new UnitSet(NHUnitTypes.rhino, new byte[]{NHUnitTypes.OTHERS, 3}, 60f * 60f,
					with(Items.lead, 80, Items.graphite, 60, NHItems.presstanium, 60, NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 60)
				),
				new UnitSet(UnitTypes.mega, new byte[]{NHUnitTypes.OTHERS, 2}, 45 * 60f,
					with(Items.copper, 80, Items.metaglass, 30, NHItems.presstanium, 40, Items.graphite, 40, NHItems.juniorProcessor, 35)
				),
				new UnitSet(NHUnitTypes.gather, new byte[]{NHUnitTypes.OTHERS, 3}, 60f * 60f,
					with(Items.thorium, 80, Items.metaglass, 30, NHItems.presstanium, 80, NHItems.zeta, 120, NHItems.juniorProcessor, 80)
				)
			);
		}};
		
		jumpGateJunior = new JumpGate("jump-gate-junior"){{
			size = 5;
			atlasSizeScl = 0.75f;
			squareStroke = 2f;
			health = 6000;
			spawnDelay = 60f;
			spawnReloadTime = 600f;
			range = 300f;
			
			buildSpeedMultiplierCoefficient = 0.75f;
			
			adaptBase = jumpGatePrimary;
			adaptable = true;
			consumePowerCond(30, JumpGateBuild::isCalling);
			
			requirements(Category.units, BuildVisibility.shown, with(
					NHItems.presstanium, 800,
					NHItems.metalOxhydrigen, 300,
					NHItems.juniorProcessor, 600,
					Items.plastanium, 350,
					Items.metaglass, 300,
					Items.thorium, 1000
			));
			
			armor = 10f;
			
			itemCapacity = 1200;
			
			addSets(
				new UnitSet(NHUnitTypes.naxos, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 120 * 60f,
					with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, Items.surgeAlloy, 50, NHItems.metalOxhydrigen, 120)
				),
				new UnitSet(NHUnitTypes.restrictionEnzyme, new byte[]{NHUnitTypes.ANCIENT_GROUND, 3}, 75f * 60f,
						with(Items.tungsten, 200, Items.plastanium, 100, NHItems.presstanium, 100, NHItems.zeta, 60, NHItems.juniorProcessor, 80)
				),
				new UnitSet(NHUnitTypes.aliotiat, new byte[]{NHUnitTypes.GROUND_LINE_1, 3}, 55 * 60f,
					with(Items.copper, 120, NHItems.multipleSteel, 50, NHItems.presstanium, 60, NHItems.juniorProcessor, 45)
				),
				new UnitSet(NHUnitTypes.tarlidor, new byte[]{NHUnitTypes.GROUND_LINE_1, 4}, 130 * 60f,
					ItemStack.with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, NHItems.zeta, 250)
				),
				new UnitSet(NHUnitTypes.ghost, new byte[]{NHUnitTypes.NAVY_LINE_1, 3}, 60 * 60f,
					ItemStack.with(NHItems.presstanium, 60, NHItems.multipleSteel, 50, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.warper, new byte[]{NHUnitTypes.AIR_LINE_1, 3}, 65 * 60f,
					with(Items.thorium, 90, Items.graphite, 50, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.macrophage, new byte[]{NHUnitTypes.ANCIENT_AIR, 4}, 180 * 60f,
					with(Items.phaseFabric, 100, NHItems.irayrondPanel, 150, NHItems.presstanium, 320, Items.tungsten, 400, NHItems.seniorProcessor, 100)
				),
				new UnitSet(NHUnitTypes.zarkov, new byte[]{NHUnitTypes.NAVY_LINE_1, 4}, 140 * 60f,
						ItemStack.with(NHItems.multipleSteel, 400, NHItems.juniorProcessor, 300, NHItems.presstanium, 400, NHItems.metalOxhydrigen, 200)
				)/*,
				new UnitSet(NHUnitTypes.striker, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 150 * 60f,
						ItemStack.with(Items.phaseFabric, 200, NHItems.juniorProcessor, 300, NHItems.presstanium, 350, NHItems.seniorProcessor, 75)
				)*/
			);
		}};
		
		jumpGate = new JumpGate("jump-gate"){{
			consumePowerCond(60, JumpGateBuild::isCalling);
			health = 80000;
			spawnDelay = 30f;
			spawnReloadTime = 300f;
			range = 600f;
			squareStroke = 2.35f;
			size = 8;
			adaptable = false;
			adaptBase = jumpGateJunior;
			
			armor = 20f;
			
			buildSpeedMultiplierCoefficient = 1f;
			
			itemCapacity = 6000;
			
			requirements(Category.units, BuildVisibility.shown, with(
				NHItems.presstanium, 1800,
				NHItems.metalOxhydrigen, 800,
				NHItems.seniorProcessor, 800,
				NHItems.multipleSteel, 1000,
				Items.thorium, 2000,
				Items.titanium, 1500,
				Items.phaseFabric, 600,
				NHItems.irayrondPanel, 400
			));
			
			addSets(
				new UnitSet(NHUnitTypes.longinus, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 400 * 60f,
					with(NHItems.setonAlloy, 300, Items.surgeAlloy, 150, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 250)
				),
				new UnitSet(NHUnitTypes.saviour, new byte[]{NHUnitTypes.OTHERS, 5}, 300 * 60f,
					with(NHItems.setonAlloy, 250, Items.surgeAlloy, 400, NHItems.seniorProcessor, 350, NHItems.thermoCoreNegative, 150, Items.plastanium, 400, NHItems.zeta, 500)
				),
				new UnitSet(NHUnitTypes.declining, new byte[]{NHUnitTypes.NAVY_LINE_1, 6}, 420 * 60f,
					with(NHItems.upgradeSort, 500, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 300, NHItems.thermoCoreNegative, 300, Items.tungsten, 1200)
				),
				new UnitSet(NHUnitTypes.guardian, new byte[]{NHUnitTypes.OTHERS, 5}, 9600f,
						new ItemStack(NHItems.darkEnergy, 1500)
				),
				new UnitSet(NHUnitTypes.sin, new byte[]{NHUnitTypes.GROUND_LINE_1, 6}, 480 * 60f,
					with(NHItems.setonAlloy, 600, NHItems.upgradeSort, 750, NHItems.seniorProcessor, 300, NHItems.thermoCorePositive, 500, NHItems.presstanium, 1500)
				),
				new UnitSet(NHUnitTypes.anvil, new byte[]{NHUnitTypes.AIR_LINE_2, 6}, 540 * 60f,
					with(NHItems.multipleSteel, 1000, NHItems.setonAlloy, 800, NHItems.upgradeSort, 600, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 750)
				),
				new UnitSet(NHUnitTypes.hurricane, new byte[]{NHUnitTypes.AIR_LINE_1, 6}, 600 * 60f,
					with(NHItems.setonAlloy, 800, NHItems.upgradeSort, 900, NHItems.seniorProcessor, 1200, NHItems.thermoCoreNegative, 500)
				),
				new UnitSet(NHUnitTypes.annihilation, new byte[]{NHUnitTypes.GROUND_LINE_1, 5}, 320 * 60f,
					with(NHItems.setonAlloy, 250, NHItems.multipleSteel, 400, NHItems.seniorProcessor, 400, NHItems.fusionEnergy, 400)
				),
				new UnitSet(NHUnitTypes.destruction, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 360 * 60f,
						with(NHItems.setonAlloy, 350, NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 300, NHItems.fusionEnergy, 250)
				),
				new UnitSet(NHUnitTypes.collapser, new byte[]{NHUnitTypes.AIR_LINE_2, 7}, 900 * 60f,
						new ItemStack(NHItems.thermoCorePositive, 2500),
						new ItemStack(NHItems.thermoCoreNegative, 2500),
						new ItemStack(NHItems.upgradeSort, 4500)
				),
				new UnitSet(NHUnitTypes.pester, new byte[]{NHUnitTypes.ANCIENT_AIR, 7}, 1200 * 60f,
						new ItemStack(NHItems.ancimembrane, 3500),
						new ItemStack(NHItems.upgradeSort, itemCapacity / 2)
				),
				new UnitSet(NHUnitTypes.nucleoid, new byte[]{NHUnitTypes.ANCIENT_AIR, 8}, 3600 * 60f * 2,
						with(NHItems.ancimembrane, itemCapacity,
								NHItems.upgradeSort, itemCapacity,
								NHItems.darkEnergy, itemCapacity
						)
				),
				new UnitSet(NHUnitTypes.laugra, new byte[]{NHUnitTypes.ANCIENT_GROUND, 5}, 480 * 60f,
						with(NHItems.setonAlloy, 300, NHItems.seniorProcessor, 300, Items.surgeAlloy, 200)
				)
			);
		}};

		loadFactories();
		loadTurrets();
		loadEnv();
		loadWalls();
		loadPowers();

		InnerBlock.load();
		ProductionBlocks.load();
		TurretBlock.load();
		DefenseBlock.load();
		EnvironmentBlock.load();
		SpecialBlock.load();
		CraftingBlock.load();
		if(NHSetting.getBool(NHSetting.EXPERIMENTAL)){
			//FloodContentBlock.load();
			TestBlock.load();
		}

		DistributionBlock.load();
		//EndFieldBlock.load();

		disposePowerVoid = new PowerVoid("dispose-power-void"){{
			size = 1;
			rebuildable = false;
			requirements(Category.power, BuildVisibility.sandboxOnly, with());
			alwaysUnlocked = true;
		}};
		
		tagger = new MessageBlock("tagger"){{
			forceDark = true;
			maxTextLength = 100000;
			requirements(Category.logic, BuildVisibility.editorOnly, with());
			
			targetable = false;
			privileged = true;
		}};
	}
}
