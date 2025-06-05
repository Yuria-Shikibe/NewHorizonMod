package newhorizon.content;

import mindustry.type.Planet;

public class NHPlanets{
	public static Planet midantha, sentourt, danthami, ceito;
	
	public static void load(){

	}
	
	public static class NHPlanet extends Planet{
		public NHPlanet(String name, Planet parent, float radius, int sectorSize){
			super(name, parent, radius, sectorSize);
		}
		
		public NHPlanet(String name, Planet parent, float radius){
			super(name, parent, radius);
		}
	}

	/*
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
		public float airThresh = 0.13f, airScl = 14;

		public final Seq<Rect> tmpRects = new Seq<>();
		
		Block[] terrain1 = {NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGroundQuantum, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.quantumFieldDeep, NHBlocks.metalGround, NHBlocks.conglomerateRock, NHBlocks.quantumFieldDeep};

		{
			baseSeed = 5;
			defaultLoadout = Loadouts.basicBastion;
		}
		
		public boolean allowLanding(Sector sector){
			return (sector.hasBase() || sector.near().contains(s -> s.hasBase() && s.isCaptured()));
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
							}
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
						}
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
						//floor = NHBlocks.armorAncient;
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
			state.rules.spawns = NHPostProcess.generate(difficulty, new Rand(sector.id), false, false, false);
			//state.rules.tags.put(NHInbuiltEvents.APPLY_KEY, "true");
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
	*/
	
	public interface DrawBoolf{
		boolean get(int x, int y);
	}
}
