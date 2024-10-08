package newhorizon.content;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.GridBits;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.content.Loadouts;
import mindustry.game.Rules;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexMesher;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.Weather;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.campaign.LaunchPad;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.blocks.environment.TallBlock;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import newhorizon.NHSetting;
import newhorizon.expand.map.planet.CeitoPlanet;
import newhorizon.expand.map.planet.MidanthaPlanet;

import static mindustry.Vars.state;

public class NHPlanets{
	public static Planet midantha, sentourt, danthami, ceito;
	
	public static void load(){
		ceito = new CeitoPlanet();
		if (NHSetting.getBool(NHSetting.EXPERIMENTAL)){
			danthami = new MidanthaPlanet();
		}
		midantha = new NHPlanet("midantha", ceito, 1, 3){{
			bloom = true;
			visible = true;
			accessible = true;
			hasAtmosphere = true;
			alwaysUnlocked = true;
			iconColor = NHColor.darkEnrColor;
			meshLoader = () -> new NHModMesh(
					this, 5,
					5, 0.3, 1.7, 1.2, 1.4,
					1.1f,
					NHColor.darkEnrFront.cpy().lerp(Color.white, 0.2f),
					NHColor.darkEnrFront,
					NHColor.darkEnrColor,
					NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f).mul(1.05f),
					Pal.darkestGray.cpy().mul(0.95f),
					Pal.darkestGray.cpy().lerp(Color.white, 0.105f),
					Pal.darkestGray.cpy().lerp(Pal.gray, 0.2f),
					Pal.darkestGray
			);
			
			clearSectorOnLose = true;
			allowWaveSimulation = true;
			allowLaunchSchematics = false;
			allowLaunchLoadout = false;
			
			ruleSetter = r -> {
				r.waveTeam = Team.malis;
				r.placeRangeCheck = false;
				r.showSpawns = true;
				r.waveSpacing = 80 * Time.toSeconds;
				r.initialWaveSpacing = 8f * Time.toMinutes;
				if(r.sector != null && r.sector.preset == null)r.winWave = 150;
				r.bannedUnits.add(NHUnitTypes.guardian);
				r.coreDestroyClear = true;
				r.hideBannedBlocks = true;
				r.dropZoneRadius = 64;
				
				r.bannedBlocks.addAll(Vars.content.blocks().copy().retainAll(b -> {
					if(b instanceof SolidPump){
						SolidPump pump = (SolidPump)b;
						return pump.result == Liquids.water && pump.attribute == Attribute.water;
					}else return false;
				}));
				
				Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
				teamRule.rtsAi = false;
				teamRule.unitBuildSpeedMultiplier = 5f;
				teamRule.blockDamageMultiplier = 1.25f;
				teamRule.buildSpeedMultiplier = 3f;
				teamRule.blockHealthMultiplier = 1.25f;
				
				
				teamRule = r.teams.get(r.waveTeam);
				teamRule.infiniteAmmo = teamRule.infiniteResources = true;
			};
			
			generator = new NHPlanetGenerator();
			
			cloudMeshLoader = () -> {
				return new MultiMesh(
					new HexSkyMesh(this, 2, 0.15F, 0.14F, 5, Pal.darkerMetal.cpy().lerp(NHColor.darkEnrColor, 0.35f).a(0.55F), 2, 0.42F, 1.0F, 0.43F),
					new HexSkyMesh(this, 3, 1.26F, 0.155F, 4, Pal.darkestGray.cpy().lerp(NHColor.darkEnrColor, 0.105f).a(0.75F), 6, 0.42F, 1.32F, 0.4F));
			};
			
			defaultEnv = Env.terrestrial | Env.groundWater | Env.oxygen;
			
			icon = "midantha";
			iconColor = Color.white;
			
			landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
			atmosphereRadIn = 0.02f;
			atmosphereRadOut = 0.3f;
			startSector = 15;
		}
			public void updateBaseCoverage(){
				for(Sector sector : sectors){
					float sum = 1.25f;
					for(Sector other : sector.near()){
						if(other.generateEnemyBase){
							sum += 0.9f;
						}
					}
					
					if(sector.hasEnemyBase()){
						sum += 0.88f;
					}
					
					Rand rand = new Rand();
					rand.setSeed(sector.id);
					sector.threat = sector.preset == null ? rand.random(0.55f, 1.1f) : Mathf.clamp(sector.preset.difficulty / 10f);
				}
			}
		};


		sentourt = new NHPlanet("sentourt", midantha, 0.22f, 1){{
			bloom = true;
			visible = false;
			accessible = false;
			hasAtmosphere = false;
			alwaysUnlocked = false;
			meshLoader = () -> new NHModMesh(
					this, 5,
					5, 0.3, 1.7, 1.2, 1.4,
					1.1f,
					Pal.accent.cpy(),
					Pal.accent.cpy().mul(1.25f),
					//					Pal.gray.cpy().lerp(Pal.metalGrayDark, 0.25f).lerp(NHColor.darkEnr, 0.02f),
					//					Pal.gray,
					Pal.darkerGray,
					Pal.darkerMetal,
					Pal.darkestMetal,
					Pal.darkestGray.cpy().lerp(Pal.gray, 0.2f),
					Pal.darkestGray
			);
			
			
			clearSectorOnLose = true;
			allowWaveSimulation = true;
			allowLaunchSchematics = false;
			allowLaunchLoadout = true;
			
			ruleSetter = r -> {
				r.waveTeam = Team.malis;
				r.placeRangeCheck = false;
				r.showSpawns = true;
				r.waveSpacing = 80 * Time.toSeconds;
				r.initialWaveSpacing = 8f * Time.toMinutes;
				if(r.sector.preset == null)r.winWave = 150;
				r.bannedUnits.add(NHUnitTypes.guardian);
				r.coreDestroyClear = true;
				
				Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
				teamRule.rtsAi = false;
				teamRule.unitBuildSpeedMultiplier = 5f;
				teamRule.blockDamageMultiplier = 1.25f;
				teamRule.buildSpeedMultiplier = 3f;
				teamRule.blockHealthMultiplier = 1.25f;
				
				teamRule = r.teams.get(r.waveTeam);
				teamRule.infiniteAmmo = teamRule.infiniteResources = true;
			};
			
			defaultEnv = Env.terrestrial;
			
			iconColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.23f);
			
			landCloudColor = Color.valueOf("3c1b8f");
			startSector = 0;
		}
			
			
			public void updateBaseCoverage(){
				for(Sector sector : sectors){
					float sum = 1.25f;
					for(Sector other : sector.near()){
						if(other.generateEnemyBase){
							sum += 0.9f;
						}
					}
					
					if(sector.hasEnemyBase()){
						sum += 0.88f;
					}
					
					Rand rand = new Rand();
					rand.setSeed(sector.id);
					sector.threat = sector.preset == null ? rand.random(0.55f, 1f) : Mathf.clamp(sector.preset.difficulty / 10f);
				}
			}
		};
	}
	
	public static class NHPlanet extends Planet{
		public NHPlanet(String name, Planet parent, float radius, int sectorSize){
			super(name, parent, radius, sectorSize);
		}
		
		public NHPlanet(String name, Planet parent, float radius){
			super(name, parent, radius);
		}
	}
	
	public static class NHModMesh extends HexMesh{
		public static float waterOffset = 0.05f;
		
		public NHModMesh(Planet planet, int divisions, double octaves, double persistence, double scl, double pow, double mag, float colorScale, Color... colors){
			super(planet, new HexMesher(){
				@Override
				public float getHeight(Vec3 position){
					position = Tmp.v33.set(position).scl(4f);
					float height = (Mathf.pow(Simplex.noise3d(123, 7, 0.5f, 1f/3f, position.x, position.y, position.z), 2.3f) + waterOffset) / (1f + waterOffset);
					return Math.max(height, 0.05f);
				}
				
				@Override
				public Color getColor(Vec3 position){
					double height = Math.pow(Simplex.noise3d(1, octaves, persistence, scl, position.x, position.y, position.z), pow) * mag;
					return Tmp.c1.set(colors[Mathf.clamp((int)(height * colors.length), 0, colors.length - 1)]).mul(colorScale);
				}
				
			}, divisions, Shaders.unlit);
		}
	}
	
	public static class NHPlanetGenerator extends PlanetGenerator{
		public float heightScl = 0.9f, octaves = 8, persistence = 0.7f, heightPow = 3f, heightMult = 1.6f;
		
		//TODO inline/remove
		public static float arkThresh = 0.28f, arkScl = 0.83f;
		public static int arkSeed = 7, arkOct = 2;
		public static float liqThresh = 0.64f, liqScl = 87f, redThresh = 3.1f, noArkThresh = 0.3f;
		public static int crystalSeed = 8, crystalOct = 2;
		public static float crystalScl = 0.9f, crystalMag = 0.3f;
		public static float airThresh = 0.13f, airScl = 14;
		
		public static final Seq<Rect> tmpRects = new Seq<>();
		
		Block[] terrain1 = {NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGroundQuantum, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.quantumFieldDeep, NHBlocks.metalGround, NHBlocks.conglomerateRock, NHBlocks.quantumFieldDeep};
		Block[] terrain2 = {NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.conglomerateRock, Blocks.stone, NHBlocks.metalGround};
		
		
		{
			baseSeed = 5;
			defaultLoadout = Loadouts.basicBastion;
		}
		
		public boolean allowLanding(Sector sector){
			return (sector.hasBase() || sector.near().contains(s -> s.hasBase() && s.isCaptured()))/* && NHPlanets.midantha.unlocked()*/;
		}
		
		@Override
		public void generateSector(Sector sector){
			//no bases right now
		}
		
		@Override
		public float getHeight(Vec3 position){
			return Mathf.pow(rawHeight(position), heightPow) * heightMult;
		}
		
		@Override
		public Color getColor(Vec3 position){
			Block block = getBlock(position);
			
			return Pal.gray;
		}
		
		@Override
		public float getSizeScl(){
			return 2000 * 1.07f * 6f / 3.5f;
		}
		
		float rawHeight(Vec3 position){
			return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 10f + position.x, 10f + position.y, 10f + position.z);
		}
		
		float rawTemp(Vec3 position){
			return position.dst(0, 0, 1)*2.2f - Simplex.noise3d(seed, 8, 0.54f, 1.4f, 10f + position.x, 10f + position.y, 10f + position.z) * 2.9f;
		}
		
		Block getBlock(Vec3 position){
			float ice = rawTemp(position);
			Tmp.v32.set(position);
			
			float height = rawHeight(position);
			Tmp.v31.set(position);
			height *= 1.2f;
			height = Mathf.clamp(height);
			
			return terrain1[Mathf.clamp((int)(height * terrain1.length), 0, terrain1.length - 1)];
		}
		
		@Override
		public void genTile(Vec3 position, TileGen tile){
			tile.floor = getBlock(position);
			
			if(tile.floor == NHBlocks.metalGround && rand.chance(0.01)){
				tile.floor = NHBlocks.metalGroundQuantum;
			}
			
			tile.block = tile.floor.asFloor().wall;
			
			if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, airScl) > airThresh){
				tile.block = Blocks.air;
			}
		}
		
		@Override
		protected void generate(){
			pass((x, y) -> {
				float noise = noise(x + 782, y, 7, 0.8f, 130f, 1f);
				if(noise > 0.62f){
					floor = Blocks.darksand;
					ore = Blocks.air;
				}
			});
			
			distort(10f, 12f);
			distort(5f, 7f);
			
			Pool<Rect> rectPool = Pools.get(Rect.class, Rect::new);
			rand.setSeed(seed);
			
			float difficulty = sector == null ? rand.random(0.4f, 1f) : sector.threat;
			
			for(int i = 0; i < 24; i++){
				int w = rand.random(10, width / 10);
				int h = rand.random(10, height / 10);
				int x2 = rand.random(width - w);
				int y2 = rand.random(height - h);
				tmpRects.add(rectPool.obtain().set(x2, y2, w, h));
			}
			
			for(int k = 0; k < tmpRects.size; k++){
				Rect r = tmpRects.get(k);
				for(int i = 0; i < r.width; i++){
					for(int j = 0; j < r.height; j++){
						Tile tile = tiles.get((int)(i + r.x), (int)(j + r.y));
						if(tile == null || tile.floor().isLiquid)continue;
						tile.setBlock(Blocks.air);
						if(i == 0 || i == (int)r.width - 1 || j == 0 || j == (int)r.height - 1){
							tile.setFloor(Blocks.darkPanel3.asFloor());
						}else{
							if(k % 3 == 0){
								tile.setFloor(Blocks.coreZone.asFloor());
							}else if(Mathf.chance(0.1)){
								if(k % 3 == 1){
									tile.setFloor(NHBlocks.armorQuantum.asFloor());
								}else{
									tile.setFloor(NHBlocks.armorLight.asFloor());
								}
							}else tile.setFloor(NHBlocks.armorClear.asFloor());
						}
					}
				}
			}
			
			tmpRects.clear();
			
			Block oW = Blocks.coreZone.asFloor().wall;
			Blocks.coreZone.asFloor().wall = NHBlocks.metalWall;
			
			cells(4);
			
			Blocks.coreZone.asFloor().wall = oW;
			
			float length = width/2.6f;
			Vec2 trns = Tmp.v1.trns(rand.random(360f), length);
			int
					spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f),
					endX = (int)(-trns.x + width/2f), endY = (int)(-trns.y + height/2f);
			float maxd = Mathf.dst(width/2f, height/2f);
			
			erase(spawnX, spawnY, 22);
			
			Seq<Tile> path = pathfind(spawnX, spawnY, endX, endY, tile -> (tile.solid() ? 70f : 0f) + maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan);
			
			brush(path, 8);
			erase(endX, endY, 15);
			
			median(12, 0.6, NHBlocks.quantumField);
			
			blend(NHBlocks.quantumFieldDeep, NHBlocks.quantumField, 7);
			
			scatter(NHBlocks.metalGround, NHBlocks.metalGroundQuantum, 0.075f);
			
			pass((x, y) -> {
				if(floor.asFloor().isDeep()){
					float noise = noise(x + 342, y + 541, 7, 0.8f, 120f, 1.5f);
					if(noise > 0.82f){
						floor = NHBlocks.quantumField;
					}
				}
			});
			
			inverseFloodFill(tiles.getn(spawnX, spawnY));
			
			erase(endX, endY, 6);
			
			
			pass((x, y) -> {
				if(block != Blocks.air){
					if(nearAir(x, y)){
						if(block == NHBlocks.metalWall && noise(x + 78, y, 4, 0.7f, 33f, 1f) > 0.52f){
							ore = Blocks.wallOreBeryllium;
						}/*else if(block != NHBlocks.metalWall && noise(x + 782, y, 4, 0.8f, 38f, 1f) > 0.665f){
							ore = Blocks.wallOreBeryllium;
						}*/
					}
				}else if(!nearWall(x, y)){
					if(noise(x + 150, y + x*2 + 100, 4, 3.8f, 55f, 1f) > 0.816f){
						ore = Blocks.oreTitanium;
					}
					
					if(noise(x + 134, y - 134, 5, 4f, 45f, 1f) > 0.73f){
						ore = Blocks.oreLead;
					}
					
					if(noise(x + 644, y - 538, 5.1, 2f, 125f, 1f) > 0.737f){
						ore = Blocks.oreCopper;
					}
					
					if(noise(x + 344 + y*0.35f, y - 538, 5, 6f, 45f, 1f) > 0.75f){
						ore = Blocks.oreCoal;
					}
					
					if(noise(x + 244, y - 138, 6, 3f, 35f, 1f) > 0.8f){
						ore = Blocks.oreBeryllium;
					}
					
					if(noise(x + 578, y - 238, 4, 2.08f, 85f, 1f) > 0.793f){
						ore = Blocks.oreTungsten;
					}
					
					if(noise(y - 1234, x - 938, 6, 2.28f, 15f, 1f) > 0.880383f){
						ore = NHBlocks.oreZeta;
					}
					
					if(noise(x + 999, y + 600, 4, 5.63f, 45f, 1f) > 0.8422f){
						ore = Blocks.oreThorium;
					}
				}
			});
			
//			ores(Seq.with(Blocks.oreCopper, Blocks.oreLead, Blocks.oreTitanium, Blocks.oreCoal, Blocks.oreCrystalThorium, Blocks.oreTungsten, NHBlocks.oreZeta));
			
			pass((x, y) -> {
				int x1 = x - x % 3 + 30;
				int y1 = y - y % 3 + 30;
				
				if((x1 % 70 == 0 || y1 % 70 == 0) && !floor.asFloor().isLiquid){
					if(noise(x + 30, y + 30, 4, 0.66f, 75f, 2f) > 0.85f || Mathf.chance(0.035)){
						floor = Blocks.metalFloor2;
					}
				}
				
				if((x % 85 == 0 || y % 85 == 0) && !floor.asFloor().isLiquid){
					if(difficulty > 0.815f){
						floor = NHBlocks.armorAncient;
					}else if(noise(x, y, 7, 0.67f, 55f, 3f) > 0.835f || Mathf.chance(0.175)){
						floor = Blocks.metalFloor5;
					}
				}
				
				if((x % 50 == 0 || y % 50 == 0) && !floor.asFloor().isLiquid){
					if(noise(x, y, 5, 0.7f, 75f, 3f) > 0.8125f || Mathf.chance(0.075)){
						floor = NHBlocks.quantumFieldDisturbing;
					}
				}
				
				if((nearWall(x, y) || floor == Blocks.metalFloor2) && Mathf.chance(0.015)){
					block = NHBlocks.metalTower;
				}
			});
			
			//remove props near ores, they're too annoying
			pass((x, y) -> {
				if(ore.asFloor().wallOre || block.itemDrop != null || (block == Blocks.air && ore != Blocks.air)){
					removeWall(x, y, 3, b -> b instanceof TallBlock);
				}
			});
			
			for(Tile tile : tiles){
				if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
					tile.setOverlay(Blocks.air);
				}
			}
			
			blend(NHBlocks.quantumFieldDisturbing, Blocks.darkPanel3, 1);
			
			path = pathfind(spawnX, spawnY, endX, endY, tile -> (tile.solid() ? 50f : 0f), Astar.manhattan);
			
			Geometry.circle(endX, endY, 12, ((x, y) -> {
				Tile tile = tiles.get(x, y);
				if(tile != null && tile.floor().isLiquid){
					tile.setFloor(NHBlocks.quantumField.asFloor());
				}
			}));
			
			continualDraw(path, NHBlocks.quantumField, 4, ((x0, y0) -> {
				Floor f = tiles.getn(x0, y0).floor();
				boolean b = f.isDeep();
				if(b && noise(x0, y0 * x0, 6, 0.7f, 25f, 3f) > 0.4125f){
					rand.setSeed((long)x0 + y0 << 8);
					if(rand.chance(0.22f))draw(x0, y0, NHBlocks.quantumField, 4, ((x1, y1) -> {
						Floor f1 = tiles.getn(x1, y1).floor();
						if(f1 == NHBlocks.quantumFieldDisturbing){
							tiles.getn(x1, y1).setFloor(NHBlocks.metalGround.asFloor());
							return false;
						}
						return f1.isDeep();
					}));
				}
				
				else if(f == NHBlocks.quantumFieldDisturbing){
					draw(x0, y0, NHBlocks.metalGround, 4, ((x1, y1) -> {
						return tiles.getn(x1, y1).floor() == NHBlocks.quantumFieldDisturbing;
					}));
				}
				
				return b;
			}));
			
			tiles.getn(endX, endY).setOverlay(Blocks.spawn);
			
			median(5, 0.46, NHBlocks.quantumField);
			
			decoration(0.017f);
			
			trimDark();
			
			int minVents = rand.random(22, 33);
			int ventCount = 0;
			
			//vents
			over: while(ventCount < minVents){
				outer:
				for(Tile tile : tiles){
					Floor floor = tile.floor();
					if((floor == NHBlocks.metalGround) && rand.chance(0.002)){
						int radius = 2;
						for(int x = -radius; x <= radius; x++){
							for(int y = -radius; y <= radius; y++){
								Tile other = tiles.get(x + tile.x, y + tile.y);
								if(other == null || (other.floor() != NHBlocks.metalGround) || other.block().solid){
									continue outer;
								}
							}
						}
						
						ventCount++;
						for(Point2 pos : SteamVent.offsets){
							Tile other = tiles.get(pos.x + tile.x + 1, pos.y + tile.y + 1);
							other.setOverlay(Blocks.air);
							other.setFloor(NHBlocks.metalVent.asFloor());
						}
						if(ventCount >= minVents)break over;
					}
				}
			}
			
			state.rules.env = sector.planet.defaultEnv;
			
			Schematics.placeLoadout(NHContent.nhBaseLoadout, spawnX, spawnY);
			for(Point2 p : Geometry.d8){
				Tile other = tiles.getn(spawnX + p.x, spawnY + p.y);
				other.setFloor(Blocks.coreZone.asFloor());
			}
			
			tiles.getn(spawnX, spawnY).setFloor(Blocks.coreZone.asFloor());
			
			state.rules.waves = true;
			state.rules.showSpawns = true;
			state.rules.onlyDepositCore = false;
			state.rules.fog = false;
			
			if(state.rules.sector.preset != null)return;
			
			state.rules.winWave = Mathf.round(150 * difficulty, 5);
			state.rules.weather.clear();
			state.rules.weather.add(new Weather.WeatherEntry(NHWeathers.quantumStorm, 3 * Time.toMinutes, 8 * Time.toMinutes, 0.25f * Time.toMinutes, 0.75f * Time.toMinutes));
			state.rules.spawns = NHOverride.generate(difficulty, new Rand(sector.id), false, false, false);
			state.rules.tags.put(NHInbuiltEvents.APPLY_KEY, "true");
			if(rawTemp(sector.tile.v) < 0.65f){
				state.rules.bannedBlocks.addAll(Vars.content.blocks().copy().retainAll(b -> b instanceof LaunchPad));
			}
		}
		
		public void continualDraw(Seq<Tile> path, Block block, int rad, DrawBoolf b){
			GridBits used = new GridBits(tiles.width, tiles.height);
			
			for(Tile t : path){
				for(int x = -rad; x <= rad; x++){
					for(int y = -rad; y <= rad; y++){
						int wx = t.x + x, wy = t.y + y;
						if(!used.get(wx, wy) && Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad)){
							used.set(wx, wy);
							if(b.get(wx, wy)){
								Tile other = tiles.getn(wx, wy);
								if(block instanceof Floor)other.setFloor(block.asFloor());
								else other.setBlock(block);
							}
						}
					}
				}
			}
		}
		
		public void draw(int cx, int cy, Block block, int rad, DrawBoolf b){
			for(int x = -rad; x <= rad; x++){
				for(int y = -rad; y <= rad; y++){
					int wx = cx + x, wy = cy + y;
					if(Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad) && b.get(wx, wy)){
						Tile other = tiles.getn(wx, wy);
						if(block instanceof Floor)other.setFloor(block.asFloor());
						else other.setBlock(block);
					}
				}
			}
		}
	}
	
	public interface DrawBoolf{
		boolean get(int x, int y);
	}
}
