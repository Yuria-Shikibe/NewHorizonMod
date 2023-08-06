//import arc.func.Boolf;
//import arc.func.Cons;
//import arc.func.Floatc2;
//import arc.graphics.Color;
//import arc.graphics.g2d.Draw;
//import arc.graphics.g2d.Fill;
//import arc.graphics.g2d.Lines;
//import arc.math.Mathf;
//import arc.math.Rand;
//import arc.math.geom.Geometry;
//import arc.math.geom.Position;
//import arc.math.geom.Rect;
//import arc.math.geom.Vec2;
//import arc.struct.FloatSeq;
//import arc.struct.Seq;
//import arc.util.Eachable;
//import arc.util.Tmp;
//import mindustry.Vars;
//import mindustry.content.Fx;
//import mindustry.content.StatusEffects;
//import mindustry.core.World;
//import mindustry.entities.Effect;
//import mindustry.entities.Lightning;
//import mindustry.entities.Units;
//import mindustry.entities.bullet.BulletType;
//import mindustry.game.Team;
//import mindustry.gen.Building;
//import mindustry.gen.Bullet;
//import mindustry.gen.Entityc;
//import mindustry.gen.Healthc;
//import mindustry.graphics.Layer;
//import newhorizon.content.NHFx;
//
//import java.util.Iterator;
//
//import static arc.graphics.g2d.Draw.color;
//import static arc.graphics.g2d.Lines.lineAngle;
//import static arc.graphics.g2d.Lines.stroke;
//import static arc.math.Angles.randLenVectors;
//
///**
// * Provide methods that can generate Position to Position Lightning.<p>
// * {@code Tmp} <b>var</b> is available.<p>
// * Completely independent class.<p>
// *
// * @implNote The method implements the generation of random lightning effect <b>from point to point</b> and complete certain action at <b>target point</b> through {@link Cons}.<p>
// * @apiNote
// * <li> {@code movement} {@link Cons} used to run specific action at the target point.
// * <li> {@code WIDTH}: {@value WIDTH} used to control the stroke of the lightning.
// * <li> {@code RANGE_RAND}: {@value RANGE_RAND} used to control the base xRand range of every part of the lightning.
// * <li> {@code ROT_DST}: {@value ROT_DST} used to control the length of every part of the lightning.<p>
// *
// * @see Position
// * @see Vec2
// * @see Geometry
// * @see Cons
// *
// * @author Yuria
// */
//public class PositionLightning {
//	public static final BulletType hitter = new BulletType(){{
//		status = StatusEffects.shocked;
//		statusDuration = 10f;
//		lifetime = 5f;
//
//		hittable = false;
//		despawnEffect = hitEffect = shootEffect = smokeEffect = trailEffect = Fx.none;
//		absorbable = collides = collidesAir = collidesGround = collidesTeam = collidesTiles = collideFloor = collideTerrain = false;
//		hitSize = 0;
//		speed = 0.0001f;
//		drawSize = 120f;
//	}};
//
//	public static final Effect lightningSpark = new Effect(Fx.chainLightning.lifetime, e -> {
//		color(Color.white, e.color, e.fin() + 0.25f);
//
//		stroke(0.65f + e.fout());
//
//		randLenVectors(e.id, 3, e.fin() * e.rotation + 6f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4 + 2f));
//
//		Fill.circle(e.x, e.y, 2.5f * e.fout());
//	});
//
//	/**Spawns Nothing at the hit point.*/
//	public static final Cons<Position> none = p -> {};
//	/**Lighting Effect Lifetime.*/
//	public static final float lifetime = Fx.chainLightning.lifetime;
//	/**Lighting Effect Default Width, apply it manually.*/
//	public static final float WIDTH = 2.5f;
//	/**Lighting Effect X-Rand.*/
//	public static final float RANGE_RAND = 5f;
//	/**Lighting Effect Length Between Nodes.*/
//	public static final float ROT_DST = Vars.tilesize * 0.6f;
//	/**Used for range spawn, make the lightning more random and has smoother spacing.*/
//	public static float trueHitChance = 1;
//	/**Whether to spawn a small spark at the nodes of a lightning.*/
//	public static boolean cornerSpark;
//
//
//	/**(0, 1]*/
//	public static void setHitChance(float f){
//		trueHitChance = f;
//	}
//
//	/**Must Hit*/
//	public static void setHitChanceDef(){
//		trueHitChance = 1;
//	}
//
//	//Lightning's randX. Modify it if needed.
//	private static float getBoltRandomRange() {return Mathf.random(1f, 7f); }
//
//
//	public static final Effect posLightning = (new Effect(lifetime, 1200.0f, e -> {
//		if(!(e.data instanceof newhorizon.util.struct.Vec2Seq)) return;
//		newhorizon.util.struct.Vec2Seq lines = e.data();
//
//		Draw.color(e.color, Color.white, e.fout() * 0.6f);
//
//		Lines.stroke(e.rotation * e.fout());
//
//		Fill.circle(lines.firstTmp().x, lines.firstTmp().y, Lines.getStroke() / 2f);
//
//		for(int i = 0; i < lines.size() - 1; i++){
//			Vec2 cur = lines.setVec2(i, Tmp.v1);
//			Vec2 next = lines.setVec2(i + 1, Tmp.v2);
//
//			Lines.line(cur.x, cur.y, next.x, next.y, false);
//			Fill.circle(next.x, next.y, Lines.getStroke() / 2f);
//		}
//	})).layer(Layer.effect - 0.001f);
//
//	private static Building furthest;
//	private static final Rect rect = new Rect();
//	private static final Rand rand = new Rand();
//	private static final FloatSeq floatSeq = new FloatSeq();
//	private static final Vec2 tmp1 = new Vec2(), tmp2 = new Vec2(), tmp3 = new Vec2();
//	//METHODS
//
//	//create lightning to the enemies in range.
//
//	//A radius create method that with a Bullet owner.
//	public static void createRange(Bullet owner, float range, int maxHit, Color color, boolean createSubLightning, float width, int lightningNum, Cons<Position> hitPointMovement) {
//		createRange(owner, owner, owner.team, range, maxHit, color, createSubLightning, 0, 0, width, lightningNum, hitPointMovement);
//	}
//
//	public static void createRange(Bullet owner, boolean hitAir, boolean hitGround, Position from, Team team, float range, int maxHit, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement) {
//		Seq<Healthc> entities = new Seq<>();
//		whetherAdd(entities, team, rect.setSize(range * 2f).setCenter(from.getX(), from.getY()), maxHit, hitGround, hitAir);
//		for (Healthc p : entities)create(owner, team, from, p, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
//	}
//
//
//	public static void createRange(Bullet owner, Position from, Team team, float range, int maxHit, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement) {
//		createRange(owner, owner == null || owner.type.collidesAir, owner == null || owner.type.collidesGround, from, team, range, maxHit, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
//	}
//
//	public static void createLength(Bullet owner, Team team, Position from, float length, float angle, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement){
//		create(owner, team, from, tmp2.trns(angle, length).add(from), color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
//	}
//
//	//A create method that with a Bullet owner.
//	public static void create(Entityc owner, Team team, Position from, Position target, Color color, boolean createSubLightning, float damage, int subLightningLength, float lightningWidth, int lightningNum, Cons<Position> hitPointMovement) {
//		if(!Mathf.chance(trueHitChance))return;
//		Position sureTarget = findInterceptedPoint(from, target, team);
//		hitPointMovement.get(sureTarget);
//
//		if(createSubLightning){
//			if(owner instanceof Bullet){
//				Bullet b = (Bullet)owner;
//				for(int i = 0; i < b.type.lightning; i++)Lightning.create(b, color, b.type.lightningDamage < 0.0F ? b.damage : b.type.lightningDamage, sureTarget.getX(), sureTarget.getY(), b.rotation() + Mathf.range(b.type.lightningCone / 2.0F) + b.type.lightningAngle, b.type.lightningLength + Mathf.random(b.type.lightningLengthRand));
//			}
//			else for(int i = 0; i < 3; i++)Lightning.create(team, color, damage <= 0 ? 1f : damage, sureTarget.getX(), sureTarget.getY(), Mathf.random(360f), subLightningLength);
//		}
//
//		float realDamage = damage;
//
//		if(realDamage <= 0){
//			if(owner instanceof Bullet){
//				Bullet b = (Bullet)owner;
//				realDamage = b.damage > 0 ? b.damage : 1;
//			}else realDamage = 1;
//		}
//
//		hitter.create(owner, team, sureTarget.getX(), sureTarget.getY(), 1).damage(realDamage);
//
//		createEffect(from, sureTarget, color, lightningNum, lightningWidth);
//	}
//
//	public static void createRandom(Bullet owner, Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement){
//		create(owner, team, from, tmp2.rnd(rand).scl(Mathf.random(1f)).add(from), color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
//	}
//
//	public static void createRandom(Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, Cons<Position> hitPointMovement){
//		createRandom(null, team, from, rand, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
//	}
//
//	public static void createRandomRange(Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, int generateNum, Cons<Position> hitPointMovement){
//		createRandomRange(null, team, from, rand, color, createSubLightning, damage, subLightningLength, width, lightningNum, generateNum, hitPointMovement);
//	}
//
//	public static void createRandomRange(Bullet owner, float rand, Color color, boolean createSubLightning, float damage, float width, int lightningNum, int generateNum, Cons<Position> hitPointMovement){
//		createRandomRange(owner, owner.team, owner, rand, color, createSubLightning, damage, owner.type.lightningLength + Mathf.random(owner.type.lightningLengthRand), width, lightningNum, generateNum, hitPointMovement);
//	}
//
//	public static void createRandomRange(Bullet owner, Team team, Position from, float rand, Color color, boolean createSubLightning, float damage, int subLightningLength, float width, int lightningNum, int generateNum, Cons<Position> hitPointMovement){
//		for (int i = 0; i < generateNum; i++) {
//			createRandom(owner, team, from, rand, color, createSubLightning, damage, subLightningLength, width, lightningNum, hitPointMovement);
//		}
//	}
//
//	public static void createEffect(Position from, float length, float angle, Color color, int lightningNum, float width){
//		if(Vars.headless)return;
//		createEffect(from, tmp2.trns(angle, length).add(from), color, lightningNum, width);
//	}
//
//	public static void createEffect(Position from, Position to, Color color, int lightningNum, float width){
//		if(Vars.headless)return;
//
//		if(lightningNum < 1){
//			Fx.chainLightning.at(from.getX(), from.getY(), 0, color, new Vec2().set(to));
//		}else{
//			float dst = from.dst(to);
//
//			for(int i = 0; i < lightningNum; i++){
//				float len = getBoltRandomRange();
//				float randRange = len * RANGE_RAND;
//
//				floatSeq.clear();
//				FloatSeq randomArray = floatSeq;
//				for(int num = 0; num < dst / (ROT_DST * len) + 1; num++){
//					randomArray.add(Mathf.range(randRange) / (num * 0.025f + 1));
//				}
//				createBoltEffect(color, width, computeVectors(randomArray, from, to));
//			}
//		}
//
//	}
//
//	//Compute the proper hit position.
//	public static Position findInterceptedPoint(Position from, Position target, Team fromTeam) {
//		furthest = null;
//		return Geometry.raycast(
//				World.toTile(from.getX()),
//				World.toTile(from.getY()),
//				World.toTile(target.getX()),
//				World.toTile(target.getY()),
//				(x, y) -> (furthest = Vars.world.build(x, y)) != null && furthest.team() != fromTeam && furthest.block().insulated
//		) && furthest != null ? furthest : target;
//	}
//
//	//Add proper unit into the to hit Seq.
//	private static void whetherAdd(Seq<Healthc> points, Team team, Rect selectRect, int maxHit, boolean targetGround, boolean targetAir) {
//		Units.nearbyEnemies(team, selectRect, unit -> {
//			if(unit.checkTarget(targetAir, targetGround))points.add(unit);
//		});
//
//		if(targetGround){
//			selectRect.getCenter(tmp3);
//			Units.nearbyBuildings(tmp3.x, tmp3.y, selectRect.getHeight() / 2, b -> {
//				if(b.team != team && b.isValid())points.add(b);
//			});
//		}
//
//		points.shuffle();
//		points.truncate(maxHit);
//	}
//
//	//create lightning effect.
//	public static void createBoltEffect(Color color, float width, newhorizon.util.struct.Vec2Seq vets) {
//		if(cornerSpark){
//			vets.each(((x, y) -> {
//				if(Mathf.chance(0.0855)) NHFx.lightningSpark.at(x, y, rand.random(2f + width, 4f + width), color);
//			}));
//		}
//		posLightning.at((vets.firstTmp().x + vets.peekTmp().x) / 2f, (vets.firstTmp().y + vets.peekTmp().y) / 2f, width, color, vets);
//	}
//
//	private static newhorizon.util.struct.Vec2Seq computeVectors(FloatSeq randomVec, Position from, Position to){
//		int param = randomVec.size;
//		float angle = from.angleTo(to);
//
//		newhorizon.util.struct.Vec2Seq lines = new newhorizon.util.struct.Vec2Seq(param);
//		tmp1.trns(angle, from.dst(to) / (param - 1));
//
//		lines.add(from);
//		for (int i = 1; i < param - 2; i ++)lines.add(tmp3.trns(angle - 90, randomVec.get(i)).add(tmp1, i).add(from.getX(), from.getY()));
//		lines.add(to);
//
//		return lines;
//	}
//
//	public static class Vec2Seq implements Iterable<Vec2>, Eachable<Vec2>{
//		private final FloatSeq coordinates;
//
//		private final Vec2 tmp = new Vec2();
//
//		public Vec2Seq(){
//			coordinates = new FloatSeq(true, 8);
//		}
//
//		public Vec2Seq(int length){
//			coordinates = new FloatSeq(true, length * 2);
//		}
//
//		public Vec2Seq(Seq<Vec2> vec2s){
//			this(vec2s.size / 2);
//			addAll(vec2s);
//		}
//
//		public Vec2Seq(FloatSeq vec2s){
//			coordinates = FloatSeq.with(vec2s.items.clone());
//			if(coordinates.size % 2 == 1) coordinates.pop();
//		}
//
//		public Vec2Seq(float[] vec2s){
//			this(vec2s.length / 2);
//			coordinates.items = vec2s;
//			coordinates.size = vec2s.length;
//		}
//
//		public int size(){
//			return coordinates.size / 2;
//		}
//
//		public FloatSeq getCoordinates(){
//			return coordinates;
//		}
//
//		public FloatSeq coordinates(){
//			return coordinates;
//		}
//
//		public boolean any(){
//			return size() > 0;
//		}
//
//		public void add(float x, float y){
//			coordinates.add(x, y);
//		}
//
//		public void add(Vec2 vec2){
//			add(vec2.x, vec2.y);
//		}
//
//		public void add(Position vec2){
//			add(vec2.getX(), vec2.getY());
//		}
//
//		public void addAll(FloatSeq arr){
//			for(int i = 0; i < arr.size / 2; i++){
//				add(arr.get(i), arr.get(i + 1));
//			}
//		}
//
//		public void addAll(float[] arr){
//			for(int i = 0; i < arr.length / 2; i++){
//				add(arr[i], arr[i + 1]);
//			}
//		}
//
//		public void addAll(Iterable<Vec2> vecs){
//			for(Vec2 v : vecs){
//				add(v);
//			}
//		}
//
//
//		//TODO should be faster.
//		public void addAll(Vec2[] vec2s){
//			for(Vec2 vec2 : vec2s){
//				add(vec2);
//			}
//		}
//
//		public Vec2Seq addAll(Vec2Seq vec2s){
//			vec2s.each(((Floatc2)this::add));
//			return this;
//		}
//
//		public Vec2Seq copy(){
//			return new Vec2Seq(coordinates.items.clone());
//		}
//
//		public int count(Boolf<Vec2> bf){
//			int i = 0;
//			Vec2 vec2 = new Vec2();
//			for(int j = 0; j < size(); j++){
//				if(bf.get(setVec2(j, vec2)))i++;
//			}
//
//			return i;
//		}
//
//		public boolean contains(Boolf<Vec2> bf){
//			Vec2 vec2 = new Vec2();
//			for(int j = 0; j < size(); j++){
//				setVec2(j, vec2);
//				if(bf.get(vec2))return true;
//			}
//
//			return false;
//		}
//
//		public Seq<Vec2> asSeq(){
//			Seq<Vec2> seq = new Seq<>(true, size());
//			for(int j = 0; j < size(); j++){
//				seq.add(newVec2(j));
//			}
//			return seq;
//		}
//
//		public Vec2 currentTmp(){
//			return tmp;
//		}
//
//		public Vec2 tmpVec2(int index){
//			return tmp.set(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
//		}
//
//		public Vec2 setVec2(int index, Vec2 vec2){
//			return vec2.set(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
//		}
//
//		public Vec2 newVec2(int index){
//			return new Vec2(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
//		}
//
//		public void get(int index, Floatc2 operator){
//			operator.get(coordinates.items[index * 2], coordinates.items[index * 2 + 1]);
//		}
//
//		public void remove(int index){
//			coordinates.removeRange(index * 2, index * 2 + 1);
//		}
//
//		public void removeRange(int start, int end){
//			coordinates.removeRange(start * 2, end * 2 + 1);
//		}
//
//		public void removeFirst(){
//			remove(0);
//		}
//
//		public void removeLast(){
//			remove(size() - 1);
//		}
//
//		public Vec2 firstTmp(){
//			return tmpVec2(0);
//		}
//
//		public Vec2 peekTmp(){
//			return tmpVec2(size() - 1);
//		}
//
//		public Vec2 popTmp(){
//			peekTmp();
//
//			removeLast();
//
//			return tmp;
//		}
//
//		public void each(Floatc2 f){
//			for(int i = 0; i < size(); i++){
//				f.get(coordinates.items[i * 2], coordinates.items[i * 2 + 1]);
//			}
//		}
//
//		public boolean check(){
//			return coordinates.size % 2 == 0;
//		}
//
//		@Override
//		public void each(Cons<? super Vec2> cons){
//			for(int i = 0; i < size(); i++){
//				cons.get(tmpVec2(i));
//			}
//		}
//
//
//
//		/**
//		 * Low Performance
//		 *
//		 * Returns an iterator over elements of type {@code T}.
//		 *
//		 * @return an Iterator.
//		 */
//		@Override
//		public Iterator<Vec2> iterator(){
//			return asSeq().iterator();
//		}
//	}
//}
//
