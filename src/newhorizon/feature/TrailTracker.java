package newhorizon.feature;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.gen.Bullet;
import mindustry.gen.Nulls;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.effects.EffectTrail;

public class TrailTracker{
	public int trailLength;
	public float trailWidth;
	
	public float trailWeaveScale = 0f;
	public float trailWeaveMag = -1f;
	public float trailWeaveScaleOffset = 0;
	public float trailOffset = 0f;
	public int trails = 1;
	public float sideOffset = 0f;
	public boolean flip;
	public boolean combine = false;
	public boolean flipWhileTwin = true;
	public boolean useTeamColor = false;
	
	public Color trailToColor = Pal.gray;
	
	public TrailTracker(){
	
	}
	public TrailTracker(int trailLength, float trailWidth, float trailWeaveScale, float trailWeaveMag, float trailWeaveScaleOffset, float trailOffset, int trails, float sideOffset, boolean flip, boolean combine, boolean flipWhileTwin, boolean useTeamColor, Color trailToColor){
		this.trailLength = trailLength;
		this.trailWidth = trailWidth;
		this.trailWeaveScale = trailWeaveScale;
		this.trailWeaveMag = trailWeaveMag;
		this.trailWeaveScaleOffset = trailWeaveScaleOffset;
		this.trailOffset = trailOffset;
		this.trails = trails;
		this.sideOffset = sideOffset;
		this.flip = flip;
		this.combine = combine;
		this.flipWhileTwin = flipWhileTwin;
		this.useTeamColor = useTeamColor;
		this.trailToColor = trailToColor;
	}
	
	
	
	public void create(Bullet b){
		if(Vars.headless)return;
		TrailTrackerEntity entity = Pools.obtain(TrailTrackerEntity.class, TrailTrackerEntity::new);
		entity.trails = new EffectTrail[trails];
		entity.bullet = b;
		
		for(int i = 0; i < entity.trails.length; i++){
			EffectTrail t = new EffectTrail();
			t.fromColor = useTeamColor ? b.team.color : b.type.trailColor;
			t.toColor = useTeamColor ? b.team.color : trailToColor;
			t.width = trailWidth;
			t.length = trailLength;
			entity.trails[i] = t;
		}
		
		entity.set(b);
		entity.add();
	}
	
	public class TrailTrackerEntity extends NHBaseEntity{
		public EffectTrail[] trails = new EffectTrail[1];
		public transient float lastX, lastY;
		public transient int stopped = 0;
		public transient Bullet bullet = Nulls.bullet;
		
		public transient Interval timer = new Interval(1);
		
		@Override
		public float clipSize(){
			Seq<Vec3> p = trails[0].points;
			return Mathf.dst(p.first().x, p.first().y, p.peek().x, p.peek().y) * 2f;
		}
		
		@Override
		public void draw(){
			Draw.z(Layer.bullet - 0.001f);
			for(EffectTrail trail : trails){
				trail.draw();
			}
		}
		
		@Override
		public void update(){
			if(bullet == null || bullet.isNull() || bullet.type == null || !bullet.added || !bullet.moving() || bullet.time > bullet.lifetime){
				for(EffectTrail trail : trails){
					trail.disappear();
				}
				return;
			}else{
				set(bullet);
			}
			
			if(timer.get(1f)){
				for(int i = 0; i < trails.length; i++){
					int offsetParma = (i - (flipWhileTwin ? i % 2 : 0));
					Tmp.v1.trns(
							bullet.rotation(),
							-bullet.vel.len() / 2 - trailOffset - sideOffset * offsetParma,
							(flip ? Mathf.sign(i % 2 == 0) : 1) * Mathf.absin(bullet.time, trailWeaveScale + offsetParma * trailWeaveScaleOffset, trailWeaveMag)
					).add(bullet);
					trails[i].update(Tmp.v1.x, Tmp.v1.y);
					if(useTeamColor)trails[i].fromColor = bullet.team.color;
				}
			}
		}
		
		@Override
		public int classId(){
			return -1;
		}
	}
}
