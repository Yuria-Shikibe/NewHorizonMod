package newhorizon.func;

import arc.struct.Seq;
import arc.func.Cons;
import arc.util.Tmp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Rect;
import arc.graphics.Color;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.game.Team;
import mindustry.gen.Unitc;
import mindustry.gen.Bullet;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.Effect;
import mindustry.entities.Damage;
import mindustry.entities.Lightning;

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
	public static void createRange(Position owner, Team team, float range, int hits, int boltNum, Color color, float width, Cons<Position> movement) {
		Seq<Unitc> entities = new Seq<>();
		whetherAdd(team, rect.setSize(range * 2).setCenter(owner.getX(), owner.getY()), entities, hits);
		for (Unitc unit : entities) {
			create(owner, unit, team, color, width, boltNum, movement);
		}
	}
	
	//create lightning to the enemies in range.
	public static void createRange(Position owner, Team team, float range, int hits, int boltNum, float damage, Color color, boolean chance, float width) {
		Seq<Unitc> entities = new Seq<>();
		whetherAdd(team, rect.setSize(range * 2).setCenter(owner.getX(), owner.getY()), entities, hits);
		for (Unitc unit : entities) {
			for (int i = 0; i < boltNum; i ++) {
				create(owner, unit, team, damage, color, chance, width);
			}
		}
	}

	//A radius create method that with a Bullet owner.
	public static void createRange(Bullet owner, float range, int hits, int boltNum, float damage, Color color, boolean chance, float width) {
		createRange(owner, owner.team(), range, hits, boltNum, damage, color, chance, width);
	}

	//A create method that could set lightning number and extra movements to the final target.
	public static void create(Position owner, Position target, Team team, Color color, float width, int boltNum, Cons<Position> movement) {
		Position sureTarget = findInterceptedPoint(owner, target, team);
		movement.get(sureTarget);

		float dst = owner.dst(sureTarget);
		for (int i = 0; i < boltNum; i ++) {
			float multBolt = getBoltRandomRange();
			float randRange = multBolt * RANGE_RAND;

			Seq<Float> randomArray = new Seq<>();
			for (int num = 0; num < dst / (ROT_DST * multBolt) + 1; num ++) {
				randomArray.add(Mathf.range(randRange));
			}
			createBoltEffect(color, width, computeVecs(randomArray, owner, sureTarget) );
		}
	}
	
	//create position to position lightning and deals splash damage, create none target lightning.
	public static void create(Position owner, Position target, Team team, float damage, Color color, boolean createLightning, float width) {
		create(owner, target, team, color, width, 1, sureTarget ->{
			if (createLightning)Lightning.create(team, color, damage, sureTarget.getX(), sureTarget.getY(), Mathf.random(360), Mathf.random(8, 12));
			Damage.damage(team, sureTarget.getX(), sureTarget.getY(), 20f, damage);
		});
	}
	
	//A create method that with a Bullet owner.
	public static void create(Bullet owner, Position target, float damage, Color color, boolean createLightning, float width) {
		create(owner, target, owner.team(), damage, color, createLightning, width);
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
	private static void whetherAdd(Team team, Rect selectRect, Seq<Unitc> targetGroup, int hits) {
		Units.nearbyEnemies(team, selectRect, unit -> {
			if (
				targetGroup.size <= hits &&
				(
					targetGroup.isEmpty() || //Make sure add the started one.
					unit.dst(Geometry.findClosest(unit.x, unit.y, targetGroup)) > GENERATE_DST
				)
			)targetGroup.add(unit);
		});
	}

	//create lightning effect.
	private static void createBoltEffect(Color color, float width, Seq<Vec2> vecs) {
		new Effect(lifetime, vecs.first().dst(vecs.peek()) * 2, e -> {
			if(!(e.data instanceof Seq)) return;
			Seq<Vec2> lines = e.data();

			Lines.stroke(e.rotation * e.fout(), e.color);
			
			Fill.circle(vecs.first().x, vecs.first().y, Lines.getStroke() * 1.1f);

			for(int i = 0; i < lines.size - 1; i++){
				Vec2 cur = lines.get(i);
				Vec2 next = lines.get(i + 1);

				Lines.line(cur.x, cur.y, next.x, next.y, false);
			}

			for(Vec2 p : lines){
				Fill.circle(p.x, p.y, Lines.getStroke() / 2f);
			}
		}).at(vecs.first().x, vecs.first().y, width, color, vecs);
	}
	
	private static Seq<Vec2> computeVecs(Seq<Float> randomVec, Position from, Position to){
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
