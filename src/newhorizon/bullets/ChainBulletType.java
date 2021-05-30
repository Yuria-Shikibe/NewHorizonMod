package newhorizon.bullets;

import arc.math.geom.Position;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import newhorizon.feature.PosLightning;
import newhorizon.func.NHFunc;
import newhorizon.vars.NHVars;

public class ChainBulletType extends BulletType{
	public int maxHit = 20;
	public float chainRange = 200f;
	public float length = 200f;
	public float thick = 2f;
	
	public int boltNum = 2;
	
	protected final IntSeq tmpSeqU = new IntSeq(maxHit), tmpSeqB = new IntSeq(maxHit);
	protected final Seq<Healthc> tmpTeamcSeq = new Seq<>();
	protected final Seq<Position> points = new Seq<>();
	
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
		Healthc target = Damage.linecast(b, b.x, b.y, b.rotation(), length);
		if(target == null)return;
		
		Units.nearbyEnemies(b.team, Tmp.r1.setSize(chainRange).setCenter(target.getX(), target.getY()), u -> {
			if(!u.checkTarget(collidesAir, collidesGround))return;
			tmpTeamcSeq.add(u);
			
			u.apply(status, statusDuration);
			
		});
		
		if(collidesGround){
			Vars.indexer.eachBlock(null, target.getX(), target.getY(), chainRange, t -> t.team != b.team, u -> {
				tmpTeamcSeq.add(u);
				u.damage(damage);
				hitEffect.at(u.x, u.y, hitColor);
			});
		}
		
		NHVars.rand.setSeed(NHFunc.seedNet());
		tmpTeamcSeq.shuffle();
		tmpTeamcSeq.truncate(maxHit);
		//Teamc closest = Geometry.findClosest(b.x, b.y, tmpTeamcSeq);
		tmpTeamcSeq.sort(e -> - target.dst(e) + target.angleTo(e) / 32f);
		
		for(Healthc t : tmpTeamcSeq){
			if(t != null){
				t.damage(damage);
				hitEffect.at(t.getX(), t.getY(), hitColor);
			}
		}
		
		points.add(b);
		points.addAll(tmpTeamcSeq);
		
		for(int i = 1; i < points.size; i++){
			Position from = points.get(i - 1), to = points.get(i);
			PosLightning.createEffect(from, to, hitColor, boltNum, thick);
		}
		
		tmpTeamcSeq.clear();
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
