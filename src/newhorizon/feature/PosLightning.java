package newhorizon.feature;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Unitc;
import mindustry.world.Tile;
import newhorizon.content.NHFx;

public class PosLightning { //Provide some workable methods to create position to position lightning bolt. Powered by Yuria.

	/**
	* Main methods:
	*
	*/


	//ELEMENTS
	
	//Default effect lifetime.
	public static final float lifetime = Fx.lightning.lifetime;
	//Default lightning width.
	public static final float WIDTH = 3f;
	//Default min lightning create distance from targetA to B.
	public static final float GENERATE_DST = 16f;
	//Default randX mult prama.
	public static final float RANGE_RAND = 4f;
	//ROT_DST
	public static final float ROT_DST = Vars.tilesize * 0.75f;

	//Used in find target method.
	private static Tile furthest;
	private static final Rect rect = new Rect();
	

	//METHODS

	//create lightning to the enemies in range.
	
	//A radius create method that with a Bullet owner.
	public static void createRange(Bullet owner, float range, int hits, Color color, boolean createLightning, float width, int boltNum, Cons<Position> movement) {
		createRange(owner, owner, owner.team, range, hits, color, createLightning, 0, 0, width, boltNum, movement);
	}
	
	public static void createRange(@Nullable Bullet owner, boolean hitAir, boolean hitGround, Position from, Team team, float range, int hits, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement) {
		Seq<Unitc> entities = new Seq<>();
		whetherAdd(entities, team, rect.setSize(range * 2f).setCenter(from.getX(), from.getY()), hits, hitGround, hitAir);
		for (Position p : entities)create(owner, team, from, p, color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	
	public static void createRange(@Nullable Bullet owner, Position from, Team team, float range, int hits, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement) {
		createRange(owner, owner == null || owner.type.collidesAir, owner == null || owner.type.collidesGround, from, team, range, hits, color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	public static void createRange(Position from, Team team, float range, int hits, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement) {
		createRange(null, from, team, range, hits, color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	//create lightning to the enemies in range.
	public static void createRange(Position from, Team team, float range, int hits, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum) {
		createRange(null, from, team, range, hits, color, createLightning, damage, boltLen, width, boltNum, position -> {});
	}
	
	//A create method that could set lightning number and extra movements to the final target.
	public static void create(Position from, Position target, Team team, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement) {
		create(null, team, from, target, color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	//A create method that with a Bullet owner.
	public static void create(@Nullable Bullet owner, Team team, Position from, Position target, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement) {
		Position sureTarget = findInterceptedPoint(from, target, team);
		movement.get(sureTarget);
		
		if(createLightning){
			if(owner != null)for(int i = 0; i < owner.type.lightning; i++)Lightning.create(owner, color, owner.type.lightningDamage < 0.0F ? owner.damage : owner.type.lightningDamage, sureTarget.getX(), sureTarget.getY(), owner.rotation() + Mathf.range(owner.type.lightningCone / 2.0F) + owner.type.lightningAngle, owner.type.lightningLength + Mathf.random(owner.type.lightningLengthRand));
			else for(int i = 0; i < 3; i++)Lightning.create(team, color, damage <= 0 ? 1f : damage, sureTarget.getX(), sureTarget.getY(), Mathf.random(360f), boltLen);
		}
		
		if(target instanceof Healthc){
			Healthc h = (Healthc)target;
			if(owner == null)h.damage(damage <= 0 ? 1f : damage);
			else h.damage(owner.damage);
		}
		
		float dst = from.dst(sureTarget);
		for (int i = 0; i < boltNum; i ++) {
			float len = getBoltRandomRange();
			float randRange = len * RANGE_RAND;
			
			Seq<Float> randomArray = new Seq<>();
			for (int num = 0; num < dst / (ROT_DST * len) + 1; num ++) {
				randomArray.add(Mathf.range(randRange));
			}
			createBoltEffect(color, width, computeVectors(randomArray, from, sureTarget) );
		}
	}

	public static void createRandom(Bullet owner, Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement){
		create(owner, team, from, new Vec2().rnd(rand).scl(Mathf.random(1f)).add(from), color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	public static void createRandom(Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement){
		createRandom(null, team, from, rand, color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	public static void createRandomRange(Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, int generateNum, Cons<Position> movement){
		createRandomRange(null, team, from, rand, color, createLightning, damage, boltLen, width, boltNum, generateNum, movement);
	}
	
	public static void createRandomRange(Bullet owner, Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, int generateNum, Cons<Position> movement){
		for (int i = 0; i < generateNum; i++) {
			createRandom(owner, team, from, rand, color, createLightning, damage, boltLen, width, boltNum, movement);
		}
	}

	//Private methods and classes.
	
	//Compute the proper homologous Tile.position's x and y.
	private static int toIntTile(float pos) {return Math.round(pos / Vars.tilesize); }

	//Compute the proper hit position.
	private static Position findInterceptedPoint(Position from, Position target, Team fromTeam) {
		furthest = null;

		return 
		Vars.world.raycast(
			toIntTile(from.getX()),
			toIntTile(from.getY()),
			toIntTile(target.getX()),
			toIntTile(target.getY()),
			(x, y) -> (furthest = Vars.world.tile(x, y)) != null && furthest.team() != fromTeam && furthest.block().absorbLasers 
		) && furthest != null ? furthest : target;
	}

	//Set the range of lightning's randX.
	private static float getBoltRandomRange() {return Mathf.random(2f, 5f); }
	
	//Add proper unit into the to hit Seq.
	private static void whetherAdd(Seq<Unitc> points, Team team, Rect selectRect, int hits, boolean targetGround, boolean targetAir) {
		points.clear();
		Units.nearbyEnemies(team, selectRect, unit -> {
			if(
				points.size <= hits && unit.checkTarget(targetAir, targetGround) && (
					points.isEmpty() || unit.dst(Geometry.findClosest(unit.x, unit.y, points)) > GENERATE_DST//Make sure add the started one.
				)
			) points.add(unit);
		});
	}

	//create lightning effect.
	private static void createBoltEffect(Color color, float width, Seq<Vec2> vecs) {
		NHFx.posLightning.at(vecs.first().x, vecs.first().y, width, color, vecs);
	}
	
	private static Seq<Vec2> computeVectors(Seq<Float> randomVec, Position from, Position to){
		int param = randomVec.size;
		float angle = from.angleTo(to);
		
		Seq<Vec2> lines = new Seq<>(param);
		Tmp.v1.trns(angle, from.dst(to) / (param - 1));
		
		lines.add(new Vec2().set(from));
		for (int i = 1; i < param - 2; i ++)lines.add(new Vec2().trns(angle - 90, randomVec.get(i)).add(Tmp.v1, i).add(from.getX(), from.getY()));
		lines.add(new Vec2().set(to));
		
		return lines;
	}
}
