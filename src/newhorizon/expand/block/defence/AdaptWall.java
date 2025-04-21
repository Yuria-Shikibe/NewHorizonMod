package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Queue;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.content.NHStats;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.*;
import static newhorizon.util.graphic.SpriteUtil.*;

public class AdaptWall extends Wall {
	public TextureRegion[] atlasRegion;

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
	}

	@Override
	public void setStats() {
		super.setStats();
		stats.add(NHStats.damageReduction, damageReduction * 100, StatUnit.percent);
	}

	public class AdaptWallBuild extends Building{
		public Seq<AdaptWallBuild> connectedWalls = new Seq<>();
		public int drawIndex = 0;

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
		public void damage(Bullet bullet, Team source, float damage) {
			super.damage(bullet, source, damage);
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

		//todo healthChanged sometimes not trigger properly
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
			if (damaged()){
				healthChanged();
			}
			if (building.health <= 0) {
				Call.buildDestroyed(building);
			}
			NHFx.shareDamage.at(building.x, building.y, building.block.size * tilesize / 2f, team.color, Mathf.clamp(damage/(block.health * 0.1f)));
		}
		
		@Override
		public void draw(){
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