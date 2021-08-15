package newhorizon.block.turrets;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import newhorizon.func.DrawFuncs;
import newhorizon.func.NHSetting;

import static mindustry.Vars.tilesize;

public class FinalTurret extends PowerTurret{
	public float extendX = 12f, extendXmin = -4f;
	public TextureRegion leftRegion, rightRegion, leftOutlineRegion, rightOutlineRegion, middleOutlineRegion;
	public TextureRegion leftDownRegion, rightDownRegion, leftDownOutlineRegion, rightDownOutlineRegion;
	
	public float chargeY = -48f;
	public float downExtendScl = 0.35f;
	public Interp curve = Interp.circleOut;
	
	public int particles = 25;
	public float particleLife = 40f, particleRad = 7f, particleStroke = 1.1f, particleLen = 3f;
	
	public float chargeCircleFrontRad = 18;
	public float chargeCircleBackRad = 8;
	
	protected static final Vec2 trSide = new Vec2();
	protected static final int[] DRAW_KEY = {-1, 1, 0};
	protected static final boolean[] DRAW_KEY_BOOL = {true, false};
	protected static final Rand rand = new Rand();
	
	
	public FinalTurret(String name){
		super(name);
		
		outlineRadius = 0;
		outlineIcon = false;
		
	}
	
	@Override
	public void init(){
		super.init();
		
		clipSize = Math.max(clipSize, chargeCircleFrontRad * 80f);
	}
	
	@Override
	public void load(){
		super.load();
		leftRegion = Core.atlas.find(name + "-side-L");
		leftDownRegion = Core.atlas.find(name + "-side-down-L");
		rightRegion = Core.atlas.find(name + "-side-R");
		rightDownRegion = Core.atlas.find(name + "-side-down-R");
		region = Core.atlas.find(name + "-middle");
		
		leftOutlineRegion = Core.atlas.find(name + "-side-L-outline");
		leftDownOutlineRegion = Core.atlas.find(name + "-side-down-L-outline");
		rightOutlineRegion = Core.atlas.find(name + "-side-R-outline");
		rightDownOutlineRegion = Core.atlas.find(name + "-side-down-R-outline");
		middleOutlineRegion = Core.atlas.find(name + "-middle-outline");
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		super.createIcons(packer);
		
		PixmapRegion originLeft, originRight, originMiddle, baseR = Core.atlas.getPixmap(baseRegion);
		
		Pixmap
			left = Pixmaps.outline(originLeft = Core.atlas.getPixmap(leftRegion), outlineColor, outlineRadius),
			right = Pixmaps.outline(originRight = Core.atlas.getPixmap(rightRegion), outlineColor, outlineRadius),
			leftDown = Pixmaps.outline(Core.atlas.getPixmap(leftDownRegion), outlineColor, outlineRadius),
			rightDown = Pixmaps.outline(Core.atlas.getPixmap(rightDownRegion), outlineColor, outlineRadius),
			middle = Pixmaps.outline(originMiddle = Core.atlas.getPixmap(region), outlineColor, outlineRadius);
		
		packer.add(MultiPacker.PageType.main, name + "-side-L-outline", left);
		packer.add(MultiPacker.PageType.main, name + "-side-R-outline", right);
		packer.add(MultiPacker.PageType.main, name + "-side-down-L-outline", leftDown);
		packer.add(MultiPacker.PageType.main, name + "-side-down-R-outline", rightDown);
		packer.add(MultiPacker.PageType.main, name + "-middle-outline", middle);
		
		Pixmap base = new Pixmap(baseR.width, baseR.height);
		base.draw(leftDown, true);
		base.draw(rightDown, true);
		base.draw(left, true);
		base.draw(right, true);
		base.draw(middle, true);
		base.draw(originLeft.crop(), true);
		base.draw(originRight.crop(), true);
		base.draw(originMiddle.crop(), true);
		
		packer.add(MultiPacker.PageType.main, name + "-full", base);
		
		loadIcon();
	}
	
	public TextureRegion getRegion(int i, boolean outline){
		if(outline){
			switch(i){
				case 1 : return rightOutlineRegion;
				case 0 : return middleOutlineRegion;
				case -1: return leftOutlineRegion;
			}
		}else{
			switch(i){
				case 1 : return rightRegion;
				case 0 : return region;
				case -1: return leftRegion;
			}
		}
		
		return null;
	}
	
	public TextureRegion getDownRegion(int i, boolean outline){
		if(outline){
			switch(i){
				case 1 : return rightDownOutlineRegion;
				case -1: return leftDownOutlineRegion;
			}
		}else{
			switch(i){
				case 1 : return rightDownRegion;
				case -1: return leftDownRegion;
			}
		}
		
		return null;
	}
	
	@Override
	public TextureRegion[] icons(){
		return new TextureRegion[]{baseRegion, leftDownOutlineRegion, rightDownOutlineRegion, leftRegion, rightRegion, region};
	}
	
	public class FinalTurretBuild extends PowerTurretBuild{
		public transient float loadProgress = 0;
		public transient boolean isExtending = false;
		public transient long drawSeed = 0;
		
		@Override
		public void created(){
			super.created();
			drawSeed = id;
		}
		
		@Override
		public void draw(){
			Draw.rect(baseRegion, x, y);
			Draw.color();
			
			Draw.z(Layer.turret);
			
			float fin = loadProgress / reloadTime;
			
			tr2.trns(rotation, -recoil);
			trSide.trns(rotation, 0, -curve.apply(fin) * extendX - extendXmin);
			
			for(int i : Mathf.signs){
				Drawf.shadow(getDownRegion(i, true), x + tr2.x - elevation + trSide.x * i * downExtendScl, y + tr2.y - elevation + trSide.y * i * downExtendScl, rotation - 90);
			}
			
			for(int i : DRAW_KEY){
				Drawf.shadow(getRegion(i, true), x + tr2.x - elevation + trSide.x * i, y + tr2.y - elevation + trSide.y * i, rotation - 90);
			}
			
			for(int i : Mathf.signs){
				Draw.rect(getDownRegion(i, true), x + tr2.x + trSide.x * i * downExtendScl, y + tr2.y + trSide.y * i * downExtendScl, rotation - 90);
			}
			
			for(boolean bool : DRAW_KEY_BOOL){
				for(int i : DRAW_KEY){
					Draw.rect(getRegion(i, bool), x + tr2.x + trSide.x * i, y + tr2.y + trSide.y * i, rotation - 90);
				}
			}
			
			Draw.z(Layer.bullet + 1f);
			Draw.color(heatColor);
			
			tr2.trns(rotation, chargeY);
			tr.trns(rotation, shootLength * fin);
			
			Tmp.v1.set(tr).sub(tr2);
			float angle = Tmp.v1.angle();
			float length = Tmp.v1.len();
			float base = (Time.time / particleLife);
			rand.setSeed(id);
			
			fin = Mathf.curve(fin, 0.1f, 1f);
			
			for(int i = 0; i < particles; i++){
				float finCurve = (rand.random(1f) + base) % 1f * fin, foutCurve = 1f - finCurve / fin;
				float fslopeCurve = (0.5f - Math.abs(finCurve / fin - 0.5f)) * 2f;
				float angleRand = rand.random(45f) + angle;
				float len = length * Interp.pow2Out.apply(finCurve);
				
				float f = Mathf.curve(foutCurve, 0f, 0.78f) * Interp.pow3Out.apply(Mathf.curve(finCurve, 0f, 0.15f));
				
				Tmp.v2.trns(rand.range(150), length * rand.range(1.5f), length * rand.range(1.5f)).scl(f).add(tr2).add(tile);
				Tmp.v3.trns(rand.range(60), length * rand.range(1f), length * rand.random(0.75f)).scl(f).add(tr2).add(tile);
				Lines.stroke((isExtending ? f : foutCurve) * 3f - 1f);
				
				float spreadX = rand.range(chargeCircleBackRad * 0.7f), spreadY = rand.range(chargeCircleBackRad * 0.7f);
				float lerp = ((rand.random(1f) + base) % 1f * 3 - 1) / 2f;
				Lines.curve(x + tr2.x, y + tr2.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, x + (tr.x + spreadX) * lerp, y + (tr.y + spreadY) * lerp, (int)len / tilesize * (NHSetting.enableDetails() ? 8 : 4));
				Fill.circle(x + (tr.x + spreadX) * lerp, y + (tr.y + spreadY) * lerp, Lines.getStroke() * 3 * rand.random(0.5f, 1.25f) * f);
			}
			
			if(fin < 0.01f)return;
			Fill.circle(x + tr2.x, y + tr2.y, fin * chargeCircleBackRad);
			Lines.stroke(fin * 3f - 1f);
			DrawFuncs.circlePercentFlip(x + tr2.x, y + tr2.y, fin * (chargeCircleBackRad + 5), Time.time, 20f);
			Draw.color(Color.white);
			Fill.circle(x + tr2.x, y + tr2.y, fin * chargeCircleBackRad * 0.7f);
			
			float triWidth = (fin + Mathf.absin(3f, 0.4f)) * chargeCircleFrontRad / 3.75f;
			
			Draw.color(heatColor);
			for(int i : Mathf.signs){
				Fill.tri(x + tr.x, y + tr.y + triWidth, x + tr.x, y + tr.y - triWidth, x + tr.x + i * chargeCircleFrontRad * (18 + Mathf.absin(12f, 16)) * (fin + Mathf.absin(Time.time * 1.1f, 1.5f, 1) + 4), y + tr.y);
				Drawf.tri(x + tr.x, y + tr.y, (fin + 1) / 2 * chargeCircleFrontRad / 1.5f, chargeCircleFrontRad * 10 * fin, i * 90 + Time.time * 1.25f);
				Drawf.tri(x + tr.x, y + tr.y, (fin + 1) / 2 * chargeCircleFrontRad / 2f, chargeCircleFrontRad * 6.5f * fin, i * 90 - Time.time);
			}
			
			Fill.circle(x + tr.x, y + tr.y, fin * chargeCircleFrontRad);
			DrawFuncs.circlePercentFlip(x + tr.x, y + tr.y, fin * (chargeCircleFrontRad + 5), Time.time, 20f);
			Draw.color(Color.white);
			Fill.circle(x + tr.x, y + tr.y, fin * chargeCircleFrontRad * 0.7f);
			
			//			Drawf.shadow(rightOutlineRegion, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90);
//			Drawf.shadow(middleOutlineRegion, x + tr2.x - elevation, y + tr2.y - elevation, rotation - 90);
//			Draw.rect(region, tile.x + tr2.x, tile.y + tr2.y, rotation - 90);

//			if(heatRegion != Core.atlas.find("error")){
//				heatDrawer.get(this);
//			}
		}
		
		@Override
		public void updateTile(){
			if(!validateTarget()) target = null;
			
			boolean cooled = Mathf.equal(heat, 0, 0.001f);
			wasShooting = false;
			
			if(isExtending)loadProgress = Mathf.lerpDelta(loadProgress, reload, 0.5f);
			else loadProgress = Mathf.lerpDelta(loadProgress, 0f, restitution * 5f);
			
			recoil = Mathf.lerpDelta(recoil, 0f, restitution);
			heat = Mathf.lerpDelta(heat, 0f, cooldown);
			
			if(unit != null){
				unit.health(health);
				unit.rotation(rotation);
				unit.team(team);
				unit.set(x, y);
			}
			
			if(logicControlTime > 0){
				logicControlTime -= Time.delta;
			}
			
			if(hasAmmo()){
				
				if(timer(timerTarget, targetInterval)){
					findTarget();
				}
				
				if(validateTarget()){
					boolean canShoot = true;
					
					if(isControlled()){ //player behavior
						targetPos.set(unit.aimX(), unit.aimY());
						canShoot = unit.isShooting();
					}else if(logicControlled()){ //logic behavior
						canShoot = logicShooting;
					}else{ //default AI behavior
						targetPosition(target);
						
						if(Float.isNaN(rotation)){
							rotation = 0;
						}
					}
					
					float targetRot = angleTo(targetPos);
					
					if(shouldTurn()){
						turnToTarget(targetRot);
					}
					
					if(Angles.angleDist(rotation, targetRot) < shootCone && canShoot && cooled){
						wasShooting = isExtending = true;
						updateShooting();
					}
				}
			}
			
			if(acceptCoolant){
				updateCooling();
			}
		}
		
		@Override
		protected void shoot(BulletType type){
			super.shoot(type);
			isExtending = false;
			drawSeed++;
		}
	}
}
