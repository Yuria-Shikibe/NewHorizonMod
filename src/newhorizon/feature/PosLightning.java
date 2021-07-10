package newhorizon.feature;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provide methods that can generate Position to Position Lightning.<p>
 * {@code Tmp} <b>var</b> is available.<p>
 * Completely independent class.<p>
 *
 * @implNote The method implements the generation of random lightning effect <b>from point to point</b> and complete certain action at <b>target point</b> through {@link Cons}.<p>
 * @apiNote
 * <li> {@code movement} {@link Cons} used to run specific action at the target point.
 * <li> {@code WIDTH}: {@value WIDTH} used to control the stroke of the lightning.
 * <li> {@code RANGE_RAND}: {@value RANGE_RAND} used to control the base xRand range of every part of the lightning.
 * <li> {@code ROT_DST}: {@value ROT_DST} used to control the length of every part of the lightning.<p>
 *
 * @see Position
 * @see Vec2
 * @see Geometry
 * @see Cons
 *
 * @author Yuria
 */
public class PosLightning {
	
	public static final Cons<Position> none = p -> {};
	
	public static final float lifetime = Fx.chainLightning.lifetime;
	public static final float WIDTH = 3f;
	public static final float RANGE_RAND = 4.7f;
	public static final float ROT_DST = Vars.tilesize * 0.75f;
	
	private static final Vec2 tmp1 = new Vec2(), tmp2 = new Vec2(), tmp3 = new Vec2();
	
	private static final Effect posLightning = (new Effect(lifetime, 1200.0f, e -> {
		if(!(e.data instanceof Seq)) return;
		Seq<Vec2> lines = e.data();
		
		Draw.color(Color.white, e.color, e.fin());
		
		Lines.stroke(e.rotation * e.fout());
		
		Fill.circle(lines.first().x, lines.first().y, Lines.getStroke() / 2f);
		
		for(int i = 0; i < lines.size - 1; i++){
			Vec2 cur = lines.get(i);
			Vec2 next = lines.get(i + 1);
			
			Lines.line(cur.x, cur.y, next.x, next.y, false);
		}
		
		for(Vec2 p : lines){
			Fill.circle(p.x, p.y, Lines.getStroke() / 2f);
		}
	})).layer(Layer.effect - 1f);
	
	private static Tile furthest;
	private static final Rect rect = new Rect();
	//METHODS

	//create lightning to the enemies in range.
	
	//A radius create method that with a Bullet owner.
	public static void createRange(@NotNull Bullet owner, float range, int hits, Color color, boolean createLightning, float width, int boltNum, Cons<Position> movement) {
		createRange(owner, owner, owner.team, range, hits, color, createLightning, 0, 0, width, boltNum, movement);
	}
	
	public static void createRange(@Nullable Bullet owner, boolean hitAir, boolean hitGround, Position from, Team team, float range, int hits, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement) {
		Seq<Healthc> entities = new Seq<>();
		whetherAdd(entities, team, rect.setSize(range * 2f).setCenter(from.getX(), from.getY()), hits, hitGround, hitAir);
		for (Healthc p : entities)create(owner, team, from, p, color, createLightning, damage, boltLen, width, boltNum, movement);
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
	
	public static void createLength(@Nullable Bullet owner, Team team, Position from, float length, float angle, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement){
		create(owner, team, from, tmp2.trns(angle, length).add(from), color, createLightning, damage, boltLen, width, boltNum, movement);
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
		
		if(sureTarget instanceof Healthc){
			Healthc h = (Healthc)sureTarget;
			if(owner == null)h.damage(damage <= 0 ? 1f : damage);
			else h.damage(owner.damage);
		}
		
		createEffect(from, sureTarget, color, boltNum, width);
	}

	public static void createRandom(Bullet owner, Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement){
		create(owner, team, from, tmp2.rnd(rand).scl(Mathf.random(1f)).add(from), color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	public static void createRandom(Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, Cons<Position> movement){
		createRandom(null, team, from, rand, color, createLightning, damage, boltLen, width, boltNum, movement);
	}
	
	public static void createRandomRange(Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, int generateNum, Cons<Position> movement){
		createRandomRange(null, team, from, rand, color, createLightning, damage, boltLen, width, boltNum, generateNum, movement);
	}
	
	public static void createRandomRange(@NotNull Bullet owner, float rand, Color color, boolean createLightning, float damage, float width, int boltNum, int generateNum, Cons<Position> movement){
		createRandomRange(owner, owner.team, owner, rand, color, createLightning, damage, owner.type.lightningLength + Mathf.random(owner.type.lightningLengthRand), width, boltNum, generateNum, movement);
	}
	
	public static void createRandomRange(Bullet owner, Team team, Position from, float rand, Color color, boolean createLightning, float damage, int boltLen, float width, int boltNum, int generateNum, Cons<Position> movement){
		for (int i = 0; i < generateNum; i++) {
			createRandom(owner, team, from, rand, color, createLightning, damage, boltLen, width, boltNum, movement);
		}
	}
	
	public static void createEffect(Position from, float length, float angle, Color color, int boltNum, float width){
		createEffect(from, tmp2.trns(angle, length).add(from), color, boltNum, width);
	}
	
	public static void createEffect(Position from, Position to, Color color, int boltNum, float width){
		if(boltNum < 1)return;
		
		float dst = from.dst(to);
		
		Seq<Vec2> p = null;
		
		for (int i = 0; i < boltNum; i ++) {
			float len = getBoltRandomRange();
			float randRange = len * RANGE_RAND;
			
			FloatSeq randomArray = new FloatSeq();
			for (int num = 0; num < dst / (ROT_DST * len) + 1; num ++) {
				randomArray.add(Mathf.range(randRange) / (num * 0.025f + 1));
			}
			createBoltEffect(color, width, p = computeVectors(randomArray, from, to));
		}
		
		Fx.chainLightning.at(p.first().x, p.first().y, 0, color, p.peek());
	}
	
	//Private methods and classes.

	//Compute the proper hit position.
	private static Position findInterceptedPoint(Position from, Position target, Team fromTeam) {
		furthest = null;
		return Geometry.raycast(
			World.toTile(from.getX()),
			World.toTile(from.getY()),
			World.toTile(target.getX()),
			World.toTile(target.getY()),
			(x, y) -> (furthest = Vars.world.tile(x, y)) != null && furthest.team() != fromTeam && furthest.block().absorbLasers
		) && furthest != null ? furthest : target;
	}

	//Set the range of lightning's randX.
	private static float getBoltRandomRange() {return Mathf.random(2f, 7f); }
	
	//Add proper unit into the to hit Seq.
	private static void whetherAdd(Seq<Healthc> points, Team team, Rect selectRect, int hits, boolean targetGround, boolean targetAir) {
		Units.nearbyEnemies(team, selectRect, unit -> {
			if(unit.checkTarget(targetAir, targetGround))points.add(unit);
		});
		
		if(targetGround){
			selectRect.getCenter(tmp3);
			Vars.indexer.eachBlock(null, tmp3.x, tmp3.y, selectRect.getHeight() / 4, b -> b.team != team && b.isValid(), points::add);
		}
		
		points.shuffle();
		points.truncate(hits);
	}

	//create lightning effect.
	private static void createBoltEffect(Color color, float width, Seq<Vec2> vets) {
		posLightning.at((vets.first().x + vets.peek().x) / 2f, (vets.first().y + vets.peek().y) / 2f, width, color, vets);
	}
	
	private static Seq<Vec2> computeVectors(FloatSeq randomVec, Position from, Position to){
		int param = randomVec.size;
		float angle = from.angleTo(to);
		
		Seq<Vec2> lines = new Seq<>(param);
		tmp1.trns(angle, from.dst(to) / (param - 1));
		
		lines.add(new Vec2().set(from));
		for (int i = 1; i < param - 2; i ++)lines.add(new Vec2().trns(angle - 90, randomVec.get(i)).add(tmp1, i).add(from.getX(), from.getY()));
		lines.add(new Vec2().set(to));
		
		return lines;
	}
}
