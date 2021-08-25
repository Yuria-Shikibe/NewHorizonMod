package newhorizon.bullets;

import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import newhorizon.feature.PosLightning;

public class ChainBulletType extends BulletType{
	public int maxHit = 20;
	public float chainRange = 200f;
	public float length = 200f;
	public float thick = 2f;
	
	public int boltNum = 2;
	
	protected final IntSeq tmpSeqU = new IntSeq(maxHit), tmpSeqB = new IntSeq(maxHit);
	protected static final Seq<Position> points = new Seq<>();
	protected static final Vec2 tmpVec = new Vec2();
	
	@Override
	public void init(){
		super.init();
		drawSize = Math.max(drawSize, (length + chainRange) * 2f);
	}
	
	public ChainBulletType(float damage){
		super(0.0001f, damage);
	}
	
	@Override
	public void init(Bullet b){
		Position target = Damage.linecast(b, b.x, b.y, b.rotation(), length);
		if(target == null)target = tmpVec.trns(b.rotation(), length).add(b);
		
		Position confirm = target;
		
		Units.nearbyEnemies(b.team, Tmp.r1.setSize(chainRange).setCenter(confirm.getX(), confirm.getY()), u -> {
			if(!u.checkTarget(collidesAir, collidesGround))return;
			points.add(u);
		});
		
		if(collidesGround){
			Vars.indexer.eachBlock(null, confirm.getX(), confirm.getY(), chainRange, t -> t.team != b.team, points::add);
		}
		
		points.shuffle();
		points.truncate(maxHit);
		points.sort(e -> - confirm.dst(e) + confirm.angleTo(e) / 32f);
		points.insert(0, b);
		points.insert(1, target);
		
		for(int i = 1; i < points.size; i++){
			Position from = points.get(i - 1), to = points.get(i);
			Position sureTarget = PosLightning.findInterceptedPoint(from, to, b.team);
			PosLightning.createEffect(from, sureTarget, hitColor, boltNum, thick);
			hitEffect.at(sureTarget.getX(), sureTarget.getY(), hitColor);
			lightningType.create(b, sureTarget.getX(), sureTarget.getY(), 0).damage(damage);
			
			if(sureTarget instanceof Unit)((Unit)sureTarget).apply(status, statusDuration);
			
			if(sureTarget != to)break;
		}
		
		points.clear();
	}
	
	@Override
	public float range(){
		return length + chainRange;
	}
	
//	@Override
//	public void draw(Bullet b){
//		if(!(b.data instanceof Seq))return;
//		Seq<Position> data = (Seq<Position>)b.data;
//		Lines.stroke(thick * b.fout(Interp.pow2Out), hitColor);
//		Fill.circle(b.getX(), b.getY(), Lines.getStroke());
//		for(int i = 1; i < data.size; i++){
//			Position from = data.get(i - 1), to = data.get(i);
//			Lines.line(from.getX(), from.getY(), to.getX(), to.getY());
//			Fill.circle(to.getX(), to.getY(), Lines.getStroke() / 2f);
//			Drawf.light(b.team, from.getX(), from.getY(), to.getX(), to.getY(), Lines.getStroke() * 3f + 0.5f, hitColor, b.fout());
//		}
//	}
	
	@Override
	public void hit(Bullet b, float x, float y){
	}
	
	@Override
	public void hit(Bullet b){
	}
	
	@Override
	public void despawned(Bullet b){
	}
	
	@Override
	public void drawLight(Bullet b){
	}
}
