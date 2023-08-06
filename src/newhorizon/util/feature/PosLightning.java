package newhorizon.util.feature;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Healthc;
import mindustry.graphics.Layer;
import newhorizon.NHSetting;
import newhorizon.content.NHFx;
import newhorizon.expand.bullets.EffectBulletType;
import newhorizon.util.struct.Vec2Seq;

/**
 * Provide methods that can generate Position to Position Lightning.<p>
 * {@code Tmp} <b>var</b> is available.<p>
 * Completely independent class.<p>
 *
 * @implNote The method implements the generation of random lightning effect <b>from point to point</b> and complete certain action at <b>target point</b> through {@link Cons}.<p>
 * @apiNote
 * <li> {@code hitPointMovement} {@link Cons} used to run specific action at the target point.
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
	public static final BulletType hitter = new EffectBulletType(5f){{
		absorbable = true;
		collides = collidesAir = collidesGround = collidesTiles = true;
		status = StatusEffects.shocked;
		statusDuration = 10f;
		hittable = false;
	}};
	
	/**Spawns Nothing at the hit point.*/
	public static final Cons<Position> none = p -> {};
	/**Lighting Effect Lifetime.*/
	public static final float lifetime = Fx.chainLightning.lifetime;
	/**Lighting Effect Default Width, apply it manually.*/
	public static final float WIDTH = 2.5f;
	/**Lighting Effect X-Rand.*/
	public static final float RANGE_RAND = 5f;
	/**Lighting Effect Length Between Nodes.*/
	public static final float ROT_DST = Vars.tilesize * 0.6f;
	/**Used for range spawn, make the lightning more random and has smoother spacing.*/
	public static float trueHitChance = 1;
	
	
	/**(0, 1]*/
	public static void setHitChance(float f){
		trueHitChance = f;
	}
	
	/**Must Hit*/
	public static void setHitChanceDef(){
		trueHitChance = 1;
	}
	
	//Lightning's randX. Modify it if needed.
	private static float getBoltRandomRange() {return Mathf.random(1f, 7f); }
	
	
	public static final Effect posLightning = (new Effect(lifetime, 1200.0f, e -> {
		if(!(e.data instanceof Vec2Seq)) return;
		Vec2Seq lines = e.data();
		
		Draw.color(e.color, Color.white, e.fout() * 0.6f);
		
		Lines.stroke(e.rotation * e.fout());
		
		Fill.circle(lines.firstTmp().x, lines.firstTmp().y, Lines.getStroke() / 2f);
		
		for(int i = 0; i < lines.size() - 1; i++){
			Vec2 cur = lines.setVec2(i, Tmp.v1);
			Vec2 next = lines.setVec2(i + 1, Tmp.v2);
			
			Lines.line(cur.x, cur.y, next.x, next.y, false);
			Fill.circle(next.x, next.y, Lines.getStroke() / 2f);
		}
	})).layer(Layer.effect - 0.001f);
	
	private static Building furthest;
	private static final Rect rect = new Rect();
	private static final Rand rand = new Rand();
	private static final FloatSeq floatSeq = new FloatSeq();
	private static final Vec2 tmp1 = new Vec2(), tmp2 = new Vec2(), tmp3 = new Vec2();
	//METHODS

	//create lightning to the enemies in range.
	
	//A radius create method that with a Bullet owner.
	public static void createRange(Bullet owner, float range, int maxHit, Color color, boolean createSubLightning, float width, int lightningNum, Cons<Position> hitPointMovement) {
		createRange(owner, owner, owner.team, range, maxHit, color, createSubLightning, 0, 0, width, lightningNum, hitPointMovement);
	}
	
	public static void createRange(Bullet owner, boolean hitAir, boolean hitGround, Position from, Team team, float range, int maxHit, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement) {
		Seq<Healthc> entities = new Seq<>();
		whetherAdd(entities, team, rect.setSize(range * 2f).setCenter(from.getX(), from.getY()), maxHit, hitGround, hitAir);
		for (Healthc p : entities)create(owner, team, from, p, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
	}
	
	
	public static void createRange(Bullet owner, Position from, Team team, float range, int maxHit, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement) {
		createRange(owner, owner == null || owner.type.collidesAir, owner == null || owner.type.collidesGround, from, team, range, maxHit, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
	}
	
	public static void createLength(Bullet owner, Team team, Position from, float length, float angle, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement){
		create(owner, team, from, tmp2.trns(angle, length).add(from), color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
	}
	
	//A create method that with a Bullet owner.
	public static void create(Entityc owner, Team team, Position from, Position target, Color color, boolean createSubLightning, float damage, int subLightningLength, float lightningWidth, int lightningNum, Cons<Position> hitPointMovement) {
		if(!Mathf.chance(trueHitChance))return;
		Position sureTarget = findInterceptedPoint(from, target, team);
		hitPointMovement.get(sureTarget);
		
		if(createSubLightning){
			if(owner instanceof Bullet){
				Bullet b = (Bullet)owner;
				for(int i = 0; i < b.type.lightning; i++)Lightning.create(b, color, b.type.lightningDamage < 0.0F ? b.damage : b.type.lightningDamage, sureTarget.getX(), sureTarget.getY(), b.rotation() + Mathf.range(b.type.lightningCone / 2.0F) + b.type.lightningAngle, b.type.lightningLength + Mathf.random(b.type.lightningLengthRand));
			}
			else for(int i = 0; i < 3; i++)Lightning.create(team, color, damage <= 0 ? 1f : damage, sureTarget.getX(), sureTarget.getY(), Mathf.random(360f), subLightningLength);
		}
	
		float realDamage = damage;
		
		if(realDamage <= 0){
			if(owner instanceof Bullet){
				Bullet b = (Bullet)owner;
				realDamage = b.damage > 0 ? b.damage : 1;
			}else realDamage = 1;
		}
		
		hitter.create(owner, team, sureTarget.getX(), sureTarget.getY(), 1).damage(realDamage);
		
		createEffect(from, sureTarget, color, lightningNum, lightningWidth);
	}

	public static void createRandom(Bullet owner, Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement){
		create(owner, team, from, tmp2.rnd(rand).scl(Mathf.random(1f)).add(from), color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
	}
	
	public static void createRandom(Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement){
		createRandom(null, team, from, rand, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
	}
	
	public static void createRandomRange(Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, int generateNum, Cons<Position> hitPointMovement){
		createRandomRange(null, team, from, rand, color, createSubLightning, damage, subLightningLength, width, lightningNum, generateNum, hitPointMovement);
	}
	
	public static void createRandomRange(Bullet owner, float rand, Color color, boolean createSubLightning, float damage, float width, int lightningNum, int generateNum, Cons<Position> hitPointMovement){
		createRandomRange(owner, owner.team, owner, rand, color, createSubLightning, damage, owner.type.lightningLength + Mathf.random(owner.type.lightningLengthRand), width, lightningNum, generateNum, hitPointMovement);
	}
	
	public static void createRandomRange(Bullet owner, Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, int generateNum, Cons<Position> hitPointMovement){
		for (int i = 0; i < generateNum; i++) {
			createRandom(owner, team, from, rand, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
		}
	}
	
	public static void createEffect(Position from, float length, float angle, Color color, int lightningNum, float width){
		if(Vars.headless)return;
		createEffect(from, tmp2.trns(angle, length).add(from), color, lightningNum, width);
	}
	
	public static void createEffect(Position from, Position to, Color color, int lightningNum, float width){
		if(Vars.headless)return;
		
		if(lightningNum < 1){
			Fx.chainLightning.at(from.getX(), from.getY(), 0, color, new Vec2().set(to));
		}else{
			float dst = from.dst(to);
			
			for(int i = 0; i < lightningNum; i++){
				float len = getBoltRandomRange();
				float randRange = len * RANGE_RAND;
				
				floatSeq.clear();
				FloatSeq randomArray = floatSeq;
				for(int num = 0; num < dst / (ROT_DST * len) + 1; num++){
					randomArray.add(Mathf.range(randRange) / (num * 0.025f + 1));
				}
				createBoltEffect(color, width, computeVectors(randomArray, from, to));
			}
		}
		
	}
	
	//Compute the proper hit position.
	public static Position findInterceptedPoint(Position from, Position target, Team fromTeam) {
		furthest = null;
		return Geometry.raycast(
			World.toTile(from.getX()),
			World.toTile(from.getY()),
			World.toTile(target.getX()),
			World.toTile(target.getY()),
			(x, y) -> (furthest = Vars.world.build(x, y)) != null && furthest.team() != fromTeam && furthest.block().insulated
		) && furthest != null ? furthest : target;
	}
	
	//Add proper unit into the to hit Seq.
	private static void whetherAdd(Seq<Healthc> points, Team team, Rect selectRect, int maxHit, boolean targetGround, boolean targetAir) {
		Units.nearbyEnemies(team, selectRect, unit -> {
			if(unit.checkTarget(targetAir, targetGround))points.add(unit);
		});
		
		if(targetGround){
			selectRect.getCenter(tmp3);
			Units.nearbyBuildings(tmp3.x, tmp3.y, selectRect.getHeight() / 2, b -> {
				if(b.team != team && b.isValid())points.add(b);
			});
		}
		
		points.shuffle();
		points.truncate(maxHit);
	}

	//create lightning effect.
	public static void createBoltEffect(Color color, float width, Vec2Seq vets) {
		if(NHSetting.enableDetails()){
			vets.each(((x, y) -> {
				if(Mathf.chance(0.0855))NHFx.lightningSpark.at(x, y, rand.random(2f + width, 4f + width), color);
			}));
		}
		posLightning.at((vets.firstTmp().x + vets.peekTmp().x) / 2f, (vets.firstTmp().y + vets.peekTmp().y) / 2f, width, color, vets);
	}
	
	private static Vec2Seq computeVectors(FloatSeq randomVec, Position from, Position to){
		int param = randomVec.size;
		float angle = from.angleTo(to);
		
		Vec2Seq lines = new Vec2Seq(param);
		tmp1.trns(angle, from.dst(to) / (param - 1));
		
		lines.add(from);
		for (int i = 1; i < param - 2; i ++)lines.add(tmp3.trns(angle - 90, randomVec.get(i)).add(tmp1, i).add(from.getX(), from.getY()));
		lines.add(to);
		
		return lines;
	}
}
