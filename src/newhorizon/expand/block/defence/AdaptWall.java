package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.content.NHStats;
import newhorizon.util.graphic.SpriteUtil;
import newhorizon.util.ui.BarExtend;

import static mindustry.Vars.*;
import static newhorizon.util.graphic.SpriteUtil.*;

public class AdaptWall extends Wall {
	public TextureRegion[] atlasRegion, topRegion;

	public float damageReduction = 0.1f;
	public float maxShareStep = 3;

	private final Seq<Building> toDamage = new Seq<>();
	private final Queue<Building> queue = new Queue<>();

	public AdaptWall(String name){
		super(name);
		size = 1;
		insulated = true;
		absorbLasers = true;
		placeableLiquid = true;
		crushDamageMultiplier = 1f;
		clipSize = tilesize * 2 + 2;
	}
	
	@Override
	public void load(){
		super.load();
		atlasRegion = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 1, ATLAS_INDEX_4_12);
		topRegion = new TextureRegion[3];
		for (int i = 0; i < 3; i++){
			topRegion[i] = Core.atlas.find(name + "-top-" + i);
		}
	}

	@Override
	public void setBars() {
		barMap.clear();
		addBar("health", e -> new BarExtend(Core.bundle.format("nh.bar.health", Strings.autoFixed(e.health(), 0), health, Strings.autoFixed(e.healthf() * 100, 0)), Pal.health, e::healthf, Iconc.add + "").blink(Color.white));
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(NHStats.damageReduction, damageReduction * 100, StatUnit.percent);
	}

	public class AdaptWallBuild extends Building{
		public Seq<AdaptWallBuild> connectedWalls = new Seq<>();
		public int drawIndex = 0;
		public int topIdx = 0;

		public void updateDrawRegion(){
			drawIndex = 0;

			for(int i = 0; i < orthogonalPos.length; i++){
				Point2 pos = orthogonalPos[i];
				Building build = Vars.world.build(tileX() + pos.x, tileY() + pos.y);
				if (checkWall(build)){
					drawIndex += 1 << i;
				}
			}
			for(int i = 0; i < diagonalPos.length; i++){
				Point2[] posArray = diagonalPos[i];
				boolean out = true;
				for (Point2 pos : posArray) {
                    Building build = Vars.world.build(tileX() + pos.x, tileY() + pos.y);
                    if (!(checkWall(build))) {
						out = false;
                        break;
                    }
                }
				if (out){
					drawIndex += 1 << i + 4;

				}
			}

			drawIndex = ATLAS_INDEX_4_12_MAP.get(drawIndex);
			updateTopIndex();
		}

		public void updateTopIndex(){
			topIdx = 0;
			if (tileX() % 2 == 0 && tileY() % 2 == 0 && validTile(1, 0) && validTile(1, 1) && validTile(0, 1)){topIdx = 1; return;}
			if (tileX() % 2 == 1 && tileY() % 2 == 0 && validTile(-1, 0) && validTile(0, 1) && validTile(-1, 1)){topIdx = 0; return;}
			if (tileX() % 2 == 1 && tileY() % 2 == 1 && validTile(-1, 0) && validTile(-1, -1) && validTile(0, -1)){topIdx = 0; return;}
			if (tileX() % 2 == 0 && tileY() % 2 == 1 && validTile(1, 0) && validTile(1, -1) && validTile(0, -1)){topIdx = 0; return;}

			topIdx = (tileX() + tileY()) % 2 == 0? 3: 4;
		}

		public void drawTop(){
			if (topIdx == 0) return;
			if (topIdx == 1) {Draw.rect(topRegion[0], x + tilesize/2f, y + tilesize/2f);}
			if (topIdx == 3) {Draw.rect(topRegion[1], x, y);}
			if (topIdx == 4) {Draw.rect(topRegion[2], x, y);}
		}

		public boolean validTile(int x, int y){
			return world.build(tileX() + x, tileY() + y) != null && world.build(tileX() + x, tileY() + y).block == block();
		}

		public void findLinkWalls(){
			toDamage.clear();
			queue.clear();

			queue.addLast(this);
			while (queue.size > 0) {
				Building wall = queue.removeFirst();
				toDamage.addUnique(wall);
				for (Building next : wall.proximity) {
					if (linkValid(next) && !toDamage.contains(next)) {
						toDamage.add(next);
						queue.addLast(next);
					}
				}
			}
		}

		public boolean linkValid(Building build){
			return checkWall(build) && Mathf.dstm(tileX(), tileY(), build.tileX(), build.tileY()) <= maxShareStep;
		}

		public boolean checkWall(Building build){
			return build != null && build.block == this.block;
		}

		@Override
		public void drawSelect() {
			super.drawSelect();
			findLinkWalls();
			for (Building wall: toDamage){
				Draw.color(team.color);
				Draw.alpha(0.5f);
				Fill.square(wall.x, wall.y, 2);
			}
		}

		public void updateProximityWall(){
			connectedWalls.clear();

			for (Point2 point : proximityPos) {
				Building other = world.build(tile.x + point.x, tile.y + point.y);
				if (other == null || other.team != team) continue;
				if (checkWall(other)) {
					connectedWalls.add((AdaptWallBuild) other);
				}
			}

			updateDrawRegion();
		}
		
		public void drawTeam() {
			Draw.color(team.color);
			Draw.alpha(0.25f);
			Draw.z(Layer.blockUnder);
			Fill.square(x, y, 5f);
			Draw.color();
		}

		@Override
		public boolean collision(Bullet other){
			if(other.type.absorbable)other.absorb();
			return super.collision(other);
		}
		
		@Override
		public float handleDamage(float amount){
			findLinkWalls();
			float shareDamage = (amount / toDamage.size) * (1 - damageReduction);
			for (Building b: toDamage){
				damageShared(b, shareDamage);
			}
			return shareDamage;
		}

		public void damageShared(Building building, float damage) {
			if (building.dead()) return;
			float dm = state.rules.blockHealth(team);
			if (Mathf.zero(dm)) {
				damage = building.health + 1;
			} else {
				damage /= dm;
			}
			if (!net.client()) {
				building.health -= damage;
			}
			healthChanged();
			if (building.health <= 0) {
				Call.buildDestroyed(building);
			}
			NHFx.shareDamage.at(building.x, building.y, building.block.size * tilesize / 2f, team.color, Mathf.clamp(damage/(block.health * 0.1f)));
		}
		
		@Override
		public void draw(){
			drawTop();
			Draw.z(Layer.block + 1f);
			Draw.rect(atlasRegion[drawIndex], x, y);
		}

		public void updateProximity() {
			super.updateProximity();

			updateProximityWall();
			for (AdaptWallBuild other : connectedWalls) {
				other.updateProximityWall();
			}
		}
		
		@Override
		public void onRemoved(){
			for (AdaptWallBuild other : connectedWalls) {
				other.updateProximityWall();
			}
			super.onRemoved();
		}
	}
}