package newhorizon.bullets;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import newhorizon.effects.EffectTrail;

public class NHTrailBulletType extends SpeedUpBulletType {
	public int trailLength = -1;
	public float trailWidth = -1f;
	
	public float trailWeaveScale = 0f;
	public float trailWeaveMag = -1f;
	public float trailWeaveScaleOffset = 0;
	public float trailOffset = 0f;
	public int trails = 1;
	public float sideOffset = 0f;
	public boolean flip;
	public boolean flipWhileTwin = true;
	
	public Color trailToColor = Pal.gray;
	
	public NHTrailBulletType(float speed, float damage, String bulletSprite){
		super(speed, damage, bulletSprite);
		this.despawnEffect = Fx.none;
    }
	
	public NHTrailBulletType(float speed, float damage){
		this(speed, damage, "bullet");
	}
	
	public NHTrailBulletType(){
		this(1f, 1f, "bullet");
	}
    
    @Override
	public void init(){
		super.init();
	    if(trailLength < 0)trailLength = 12;
	    drawSize = Math.max(EffectTrail.DRAW_SIZE, Math.max(drawSize, 1.5f * trailLength * (speed + velocityEnd)));
	    if(trailWidth < 0)trailWidth = width / 6f;
    }
    
    @Override
	public void despawned(Bullet b){
		despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
		Effect.shake(despawnShake, despawnShake, b);
		hit(b);
	}
	
	@Override
	public void init(Bullet b) {
		super.init(b);
		EffectTrail[] data = new EffectTrail[trails];
		
		for(int i = 0; i < data.length; i++){
			data[i] = new EffectTrail(trailLength, trailWidth, trailColor, trailToColor);
		}
		
		b.data(data);
	}

	@Override
	public void hit(Bullet b) {
		super.hit(b);
		if (!(b.data instanceof EffectTrail[]))return;
		EffectTrail[] t = (EffectTrail[])b.data();
		for(EffectTrail trail : t){
			trail.disappear();
		}
	}

	@Override
	public void draw(Bullet b) {
		drawTrail(b);
		super.draw(b);
	}
	
	public void drawTrail(Bullet b){
		if (!(b.data instanceof EffectTrail[]))return;
		EffectTrail[] t = (EffectTrail[])b.data();
		for(EffectTrail trail : t){
			trail.draw();
		}
	}

	@Override
	public void update(Bullet b) {
		if (!(b.data instanceof EffectTrail[]))return;
		EffectTrail[] trail = (EffectTrail[])b.data;
		if(!Vars.headless && b.timer(3, Mathf.clamp(1 / Time.delta, 0, 1))){
			updateTrail(b, trail);
		}
		super.update(b);
	}
	
	public void updateTrail(Bullet b, EffectTrail[] t){
		for(int i = 0; i < t.length; i++){
			int offsetParma = (i - (flipWhileTwin ? i % 2 : 0));
			Tmp.v1.trns(b.rotation(), -b.vel.len() / 2 - trailOffset - sideOffset * offsetParma, (flip ? Mathf.sign(i % 2 == 0) : 1) * Mathf.absin(b.time, trailWeaveScale + offsetParma * trailWeaveScaleOffset, trailWeaveMag)).add(b);
			t[i].update(Tmp.v1.x, Tmp.v1.y);
		}
	}
}














