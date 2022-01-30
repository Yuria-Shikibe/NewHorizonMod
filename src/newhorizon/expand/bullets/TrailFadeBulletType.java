package newhorizon.expand.bullets;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Bullet;
import newhorizon.content.NHFx;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHSetting;

public class TrailFadeBulletType extends SpeedUpBulletType{
	public int boltNum = 2;
	public float stroke = 2F;
	public int fadeOffset = 10;
	
	public float spacing = 8f;
	public float randX = 6f;
	
	public float updateSpacing = 0.2f;
	
	/** Whether add the spawn point of the bullet to the trail seq.*/
	public boolean addBeginPoint = false;
	public boolean hitShowTrail = false;
	
	public TrailFadeBulletType(){
		super();
	}
	
	public TrailFadeBulletType(float speed, float damage, String bulletSprite) {
		super(speed, damage, bulletSprite);
		
		impact = true;
	}
	
	public TrailFadeBulletType(float speed, float damage) {
		this(speed, damage, "bullet");
	}
	
	protected static final Vec2 v1 = new Vec2(), v2 = new Vec2();
	protected static final Rand rand = new Rand();
	
	@Override
	public void hit(Bullet b){
		super.hit(b);
		if(Vars.headless || !(b.data instanceof Seq[]))return;
		Seq<Vec2>[] pointsArr = (Seq<Vec2>[])b.data();
		for(Seq<Vec2> points : pointsArr){
			points.add(new Vec2(b.x, b.y));
			if(hitShowTrail){
				PosLightning.createBoltEffect(hitColor, stroke * 2f, points);
				Vec2 v = points.first();
				NHFx.lightningHitSmall.at(v.x, v.y, hitColor);
			}else{
				points.add(new Vec2(stroke, fadeOffset));
				NHFx.lightningFade.at(b.x, b.y, points.size * speed * 2, hitColor, points);
			}
			
		}
	}
	
	@Override
	public void init(Bullet b){
		super.init(b);
		if(Vars.headless || (!NHSetting.enableDetails() && trailLength > 0))return;
		Seq<Vec2>[] points = new Seq[boltNum];
		for(int i = 0; i < boltNum; i++){
			Seq<Vec2> p = new Seq<>();
			if(addBeginPoint)p.add(new Vec2(b.x, b.y));
			points[i] = p;
		}
		b.data = points;
	}
	
	@Override
	public void update(Bullet b){
		super.update(b);
		if(!Vars.headless && b.timer(2, updateSpacing)){
			if(!(b.data instanceof Seq[]))return;
			Seq<Vec2>[] points = (Seq<Vec2>[])b.data();
			for(Seq<Vec2> seq : points){
				v2.trns(b.rotation(), 0, rand.range(randX));
				v1.set(1, 0).setToRandomDirection(rand).scl(spacing);
				seq.add(new Vec2(b.x, b.y).add(v1).add(v2));
			}
		}
	}
	
	@Override
	public void drawTrail(Bullet b){
		super.drawTrail(b);
		
		if((b.data instanceof Seq[])){
			Seq<Vec2>[] pointsArr = (Seq<Vec2>[])b.data();
			for(Seq<Vec2> points : pointsArr){
				if(points.size < 2)return;
				Draw.color(hitColor);
				for(int i = 1; i < points.size; i++){
					Draw.alpha(((float)(i + fadeOffset) / points.size));
					Lines.stroke(Mathf.clamp((i + fadeOffset / 2f) / points.size) * stroke);
					Vec2 from = points.get(i - 1);
					Vec2 to = points.get(i);
					Lines.line(from.x, from.y, to.x, to.y, false);
					Fill.circle(from.x, from.y, Lines.getStroke() / 2);
				}
				
				Fill.circle(points.peek().x, points.peek().y, stroke);
			}
		}
	}
}
