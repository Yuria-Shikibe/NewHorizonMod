package newhorizon.expand.block.defence;

import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NHRegister;
import newhorizon.NHSetting;
import newhorizon.content.NHColor;
import newhorizon.expand.interfaces.Linkablec;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.control;
import static mindustry.Vars.tilesize;

public class LaserWallBlock extends Block{
	public float range = 480f;
	public float warmupSpeed = 0.075f;
	public float minActivate = 0.3f;
	
	public DrawBlock drawer = new DrawDefault();
	
	public Shooter generateType = new Shooter(100f); //Should be continuous.
	
	public LaserWallBlock(String name){
		super(name);
		config(Integer.class, (Cons2<LaserWallBuild, Integer>)Linkablec::linkPos);
		
		update = true;
		configurable = true;
		solid = true;
		hasPower = true;
		hasShadow = true;
		
		ambientSound = loopSound = Sounds.pulse;
	}
	
	@Override
	public void load(){
		super.load();
		
		drawer.load(this);
	}
	
	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
		drawer.drawPlan(this, plan, list);
	}
	
	@Override
	public TextureRegion[] icons(){
		return drawer.finalIcons(this);
	}
	
	@Override
	public void getRegionsToOutline(Seq<TextureRegion> out){
		drawer.getRegionsToOutline(this, out);
	}
	
	@Override
	public void init(){
		super.init();
		
		generateType.drawSize = Math.max(generateType.drawSize, range * 2);
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.damage, generateType.estimateDPS(), StatUnit.perSecond);
		stats.add(Stat.range, (int)(range / tilesize), StatUnit.blocks);
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		
		float xB = x * tilesize + offset, yB = y * tilesize + offset;
		
		Color color = Vars.player.team().color;
		
		Drawf.dashCircle(xB, yB, range, color);
		
		if(!control.input.config.isShown()) return;
		Building selected = control.input.config.getSelected();
		if(selected == null || !(selected.block instanceof LaserWallBlock) || !(selected.within(xB, yB, range))) return;
		
		float sin = Mathf.absin(Time.time, 6f, 1f);
		Tmp.v1.set(xB, yB).sub(selected.x, selected.y).limit((size / 2f + 1) * tilesize + sin + 0.5f);
		float x2 = xB - Tmp.v1.x, y2 = yB - Tmp.v1.y,
				x1 = selected.x + Tmp.v1.x, y1 = selected.y + Tmp.v1.y;
		int segs = (int)(selected.dst(xB, yB) / tilesize);
		
		Lines.stroke(4f, Pal.gray);
		
		Lines.dashLine(x1, y1, x2, y2, segs);
		Lines.stroke(2f, color);
		Lines.dashLine(x1, y1, x2, y2, segs);
		
		x1 = selected.x;
		y1 = selected.y;
		x2 = xB;
		y2 = yB;
		
		Drawf.circles(x1, y1, size * tilesize / 2f + sin * 2f, color);
		Drawf.circles(x2, y2, selected.block.size * tilesize / 2f + sin * 2f, color);
		Drawf.arrow(x1, y1, x2, y2, size * tilesize / 2f + sin, 4 + sin, color);
		Draw.reset();
	}
	
	public class LaserWallBuild extends Building implements Linkablec{
		protected transient LaserWallBuild target;
		protected int linkPos = -1;
		protected Bullet shooter;
		
		public float warmup;
		
		@Override
		public void updateTile(){
			if(!linkValid()){
				target = null;
				linkPos = -1;
			}
			
			if(power.status > 0.5f && canActivate())warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed);
			else                                    warmup = Mathf.lerpDelta(warmup, 0, warmupSpeed);
			
			if(warmup > minActivate && canActivate()){
				if(shooter == null)shooter = generateType.create(this, x, y, angleTo(target));
				shooter.data(target);
				shooter.damage = generateType.damage * warmup;
				shooter.time(0);
			}else shooter = null;
			
			if(shooter != null)shooter.fdata = warmup;
		}
		
		@Override
		public boolean onConfigureBuildTapped(Building other){
			if (this == other || linkPos() == other.pos()) {
				configure(-1);
				return false;
			}
			if (other.within(this, range())) {
				configure(other.pos());
				return false;
			}
			return true;
		}
		
		
		
		@Override
		public void drawConfigure(){
			Color color = getLinkColor();
			
			Drawf.dashCircle(x, y, range(), color);
			
			if(target != null){
				float fin = Interp.smoother.apply(DrawFunc.cycle_100());
				Drawf.square(Mathf.lerp(x, target.x, fin), Mathf.lerp(y, target.y, fin), size * tilesize / 6f, DrawFunc.rotator_90(DrawFunc.cycle_100(), 0) + 45, color);
				
				DrawFunc.posSquareLink(color, 1f, tilesize * size / 6f, true, this, target);
				Drawf.square(target.x, target.y, target.block.size * tilesize / 2f, color);
			}
			
			Drawf.square(x, y, size * tilesize / 2f, color);
		}
		
		@Override
		public void draw(){
			drawer.draw(this);
		}
		
		@Override
		public void drawLight(){
			super.drawLight();
			drawer.drawLight(this);
		}
		
		@Override
		public void read(Reads read, byte revision){
			linkPos = read.i();
			warmup = read.f();
		}
		
		@Override
		public void created(){
			linkPos(linkPos);
			NHRegister.postAfterLoad(() -> linkPos(linkPos));
		}
		
		@Override
		public void write(Writes write){
			write.i(linkPos);
			write.f(warmup);
		}
		
		@Override
		public boolean linkValid(Building b){
			return b instanceof LaserWallBuild && b.team == team && b.isValid() && ((LaserWallBuild)b).link() != this;
		}
		
		public boolean canActivate(){return target != null;}
		
		@Override
		public Building link(){
			return target;
		}
		
		@Override
		public int linkPos(){
			return linkPos;
		}
		
		@Override
		public void linkPos(int value){
			linkPos = value;
			if(linkValid(Vars.world.build(linkPos))){
				target = (LaserWallBuild)Vars.world.build(linkPos);
			}else{
				target = null;
				linkPos = -1;
			}
		}
		
		@Override
		public Color getLinkColor(){
			return team.color;
		}
		
		@Override
		public float range(){
			return range;
		}
		
		@Override
		public void afterDestroyed(){
			super.afterDestroyed();
			
			if(canActivate() && warmup > minActivate)for(int i = 0; i < 8; i++){
				Time.run(i * 5, () -> {
					for(int j = 0; j < 3; j++){
						Lightning.create(Team.derelict, generateType.lightningColor, generateType.lightningDamage, x, y, Mathf.random(360), generateType.lightningLength + Mathf.random(generateType.lightningLengthRand));
					}
				});
			}
		}
		
		@Override
		public float warmup(){
			return warmup;
		}
	}
	
	public static class Shooter extends BulletType{
		public Color[] colors = {NHColor.lightSkyBack.cpy().mul(0.9f, 0.95f, 0.95f, 0.3f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.6f), NHColor.lightSkyBack, Color.white};
		public float[] strokes = {1.25f, 1.05f, 0.65f, 0.3f};
		public float width = 6f, oscScl = 1.25f, oscMag = 0.85f;
		
		public Shooter(float damage){
			super(0, damage);
			
			hitEffect = Fx.hitBeam;
			despawnEffect = Fx.none;
			hitSize = 4;
			drawSize = 420f;
			lifetime = 36f;
			
			incendAmount = 3;
			incendSpread = 8;
			incendChance = 0.6f;
			hitColor = lightColor = NHColor.lightSkyBack;
			impact = true;
			keepVelocity = false;
			collides = false;
			pierce = true;
			hittable = false;
			absorbable = false;
			
			status = StatusEffects.shocked;
			statusDuration = 300f;
			
			lightningColor = null;
			
			lightning = 0;
			lightningDamage = 120f;
			lightningLength = 12;
			lightningLengthRand = 12;
			
			hitShake = 0.25f;
		}
		
		@Override
		public void init(){
			super.init();
			
			if(lightningColor == null)lightningColor = NHColor.lightSkyBack;
		}
		
		@Override
		public float estimateDPS(){
			return damage * 100f / 5f * 3f;
		}
		
		@Override
		public void update(Bullet b){
			if(!(b.data instanceof Building))return;
			Building build = (Building)b.data();
			
			//damage every 5 ticks
			if(b.timer(1, 5f)){
				Damage.collideLine(b, b.team, hitEffect, b.x, b.y, b.rotation(), b.dst(build), true, false);
			}
			
			if(hitShake > 0){
				Effect.shake(hitShake, hitShake, b);
			}
			
			if(Vars.headless || !NHSetting.enableDetails())return;
			
			if(b.timer(1, 18f) || Mathf.chanceDelta(0.02)){
				PosLightning.createEffect(b, build, lightningColor, 2, Mathf.random(1.25f, 2.25f));
			}
			
			if(Mathf.chanceDelta(0.075)){
				PosLightning.createEffect(b, build, lightningColor, 0, 0);
			}
		}
		
		@Override
		public void draw(Bullet b){
			if(!(b.data instanceof LaserWallBuild))return;
			LaserWallBuild build = (LaserWallBuild)b.data();
			
			float strokeScl = b.fdata;
			
			for(int s = 0; s < colors.length; s++){
				Draw.color(Tmp.c1.set(colors[s]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
				Draw.z(Layer.bullet);
				Lines.stroke((width + Mathf.absin(Time.time, oscScl, oscMag)) * b.fdata * b.fout() * strokes[s]);
				Lines.line(b.x, b.y, build.x, build.y, false);
				
				Draw.z(Layer.bullet + 0.1f);
				Fill.circle(b.x, b.y, Lines.getStroke() * 0.75f);
				Fill.circle(build.x, build.y, Lines.getStroke() * 0.75f);
			}
			
			Drawf.light(b.x, b.y, build.x, build.y, width * strokes[0] * 1.5f, lightColor, 0.7f);
			Draw.reset();
		}
		
		@Override
		public void drawLight(Bullet b){}
	}
}
