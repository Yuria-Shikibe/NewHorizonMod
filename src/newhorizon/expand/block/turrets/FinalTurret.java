package newhorizon.expand.block.turrets;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.func.NHSetting;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;

public class FinalTurret extends ItemTurret{
	public TextureRegion leftRegion, rightRegion, leftOutlineRegion, rightOutlineRegion, middleOutlineRegion;
	public TextureRegion leftDownRegion, rightDownRegion, leftDownOutlineRegion, rightDownOutlineRegion;
	
	public float extendX = 12f, extendXMin = -4f;
	public float extendY = 48f, extendYMin = 10f;
	public float extentYBackScl = -1.1f, extendBackXScl = 0.7f;
	public float extendFrontX = 30f;
	
	public float chargeY = -48f;
	public float downExtendScl = 0.35f;
	public Interp curve = Interp.pow3;
	
	public int particles = 25;
	public float particleLife = 40f, particleRad = 7f, particleStroke = 1.1f, particleLen = 3f;
	
	public float chargeCircleFrontRad = 18;
	public float chargeCircleBackRad = 8;
	
	public float lightningCircleInScl = 0.85f, lightningCircleOutScl = 1.1f;
	public Interp lightningCircleCurve = Interp.pow3Out;
	
	public float shootEffectSpreadX = 5 * tilesize, shootEffectSpreadY = 28 * tilesize;
	public float shootEffectMin = 6, shootEffectMax = 10;
	
	protected static final Vec2 trSide = new Vec2(), trFront = new Vec2(), trFrontSide = new Vec2();
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
		
		clipSize = Math.max(clipSize, size * tilesize * 5);
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
		
		fullIcon = uiIcon = Core.atlas.find(name + "-full");
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		super.createIcons(packer);
		
		if(NHPixmap.isDebugging()){
			PixmapRegion originLeft, originRight, originMiddle, baseR = Core.atlas.getPixmap(baseRegion);
			
			Pixmap left = Pixmaps.outline(originLeft = Core.atlas.getPixmap(leftRegion), outlineColor, outlineRadius), right = Pixmaps.outline(originRight = Core.atlas.getPixmap(rightRegion), outlineColor, outlineRadius), leftDown = Pixmaps.outline(Core.atlas.getPixmap(leftDownRegion), outlineColor, outlineRadius), rightDown = Pixmaps.outline(Core.atlas.getPixmap(rightDownRegion), outlineColor, outlineRadius), middle = Pixmaps.outline(originMiddle = Core.atlas.getPixmap(region), outlineColor, outlineRadius);
			
			NHPixmap.packAndAdd(packer, name + "-side-L-outline", left);
			NHPixmap.packAndAdd(packer, name + "-side-R-outline", right);
			NHPixmap.packAndAdd(packer, name + "-side-down-L-outline", leftDown);
			NHPixmap.packAndAdd(packer, name + "-side-down-R-outline", rightDown);
			NHPixmap.packAndAdd(packer, name + "-middle-outline", middle);
			
//			packer.add(MultiPacker.PageType.main, name + "-side-L-outline", left);
//			packer.add(MultiPacker.PageType.main, name + "-side-R-outline", right);
//			packer.add(MultiPacker.PageType.main, name + "-side-down-L-outline", leftDown);
//			packer.add(MultiPacker.PageType.main, name + "-side-down-R-outline", rightDown);
//			packer.add(MultiPacker.PageType.main, name + "-middle-outline", middle);
			
			Pixmap base = new Pixmap(baseR.width, baseR.height);
			base.draw(Core.atlas.getPixmap(baseRegion).crop());
			base.draw(leftDown, true);
			base.draw(rightDown, true);
			base.draw(left, true);
			base.draw(right, true);
			base.draw(middle, true);
			base.draw(originLeft.crop(), true);
			base.draw(originRight.crop(), true);
			base.draw(originMiddle.crop(), true);
			
			NHPixmap.packAndAdd(packer, name + "-full", base);
			
//			packer.add(MultiPacker.PageType.main, name + "-full", base);
		}
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
	
	public class FinalTurretBuild extends ItemTurretBuild{
		public transient float loadProgress = 0;
		public transient boolean isExtending = false;
		public long drawSeed = 0;
		public transient final Vec2 cir = new Vec2();
		
		@Override
		public void created(){
			super.created();
			drawSeed = id;
		}
		
		@Override
		public void draw(){
			Draw.rect(baseRegion, x, y);
			Draw.color();
			
			Draw.z(Layer.power + 0.1f);
			
			float fin = loadProgress / reloadTime;
			
			tr2.trns(rotation, -recoil);
			trSide.trns(rotation, 0, -curve.apply(fin) * extendX - extendXMin);
			trFront.trns(rotation, curve.apply(fin) * extendY + extendYMin);
			trFrontSide.trns(rotation, 0, -extendFrontX * curve.apply(fin));
			//Layer: Shadow
			for(int i : Mathf.signs){
				Drawf.shadow(getDownRegion(i, true), x + tr2.x - elevation + trSide.x * i * downExtendScl, y + tr2.y - elevation + trSide.y * i * downExtendScl, rotation - 90);
				Drawf.shadow(getDownRegion(i, true), x + tr2.x + trFront.x - elevation + (trSide.x + trFrontSide.x) * i * downExtendScl, y + tr2.y + trFront.y - elevation + (trSide.y + trFrontSide.y) * i * downExtendScl, rotation - 90);
				Drawf.shadow(getDownRegion(i, true), x + tr2.x + trFront.x * extentYBackScl - elevation + (trSide.x + trFrontSide.x * extendBackXScl) * i * downExtendScl, y + tr2.y + trFront.y * extentYBackScl - elevation + (trSide.y + trFrontSide.y * extendBackXScl) * i * downExtendScl, rotation - 90);
			}
			for(int i : DRAW_KEY){
				Drawf.shadow(getRegion(i, true), x + tr2.x - elevation + trSide.x * i, y + tr2.y - elevation + trSide.y * i, rotation - 90);
			}
			
			//Layer: DownRegion
			for(int i : Mathf.signs){
				Draw.rect(getDownRegion(i, true), x + tr2.x + trSide.x * i * downExtendScl, y + tr2.y + trSide.y * i * downExtendScl, rotation - 90);
			}
			for(int i : Mathf.signs){
				Draw.rect(getDownRegion(i, true), x + tr2.x + trFront.x + (trSide.x + trFrontSide.x) * i * downExtendScl, y + tr2.y + trFront.y + (trSide.y + trFrontSide.y) * i * downExtendScl, rotation - 90);
				Draw.rect(getDownRegion(i, true), x + tr2.x + trFront.x * extentYBackScl + (trSide.x + trFrontSide.x * extendBackXScl) * i * downExtendScl, y + tr2.y + trFront.y * extentYBackScl + (trSide.y + trFrontSide.y * extendBackXScl) * i * downExtendScl, rotation - 90);
			}
			
			//Layer: UpRegion
			for(boolean bool : DRAW_KEY_BOOL){
				for(int i : DRAW_KEY){
					Draw.rect(getRegion(i, bool), x + tr2.x + trSide.x * i, y + tr2.y + trSide.y * i, rotation - 90);
				}
			}
			
			Draw.z(Layer.bullet + 1f);
			Draw.color(heatColor);
			
			float shootY = shootLength * curve.apply(fin);
			if(isLocal()) DrawFunc.drawRail(x, y, rotation, shootY + curve.apply(fin) * extendY + extendYMin + size * tilesize / 4f, reload / reloadTime, range(), size * tilesize / 4f, range() / size, Mathf.clamp(size / 6f, 1f, 4f), NHContent.arrowRegion);
			
			Lines.stroke(3f * Mathf.curve(fin, 0.1f, 0.2f));
			tr2.trns(rotation, chargeY);
			tr.trns(rotation, shootY);
			Tmp.v2.set(tr).sub(tr2);
			float length = Tmp.v2.len();
			Tmp.v2.set(tr).add(tr2);
			
			DrawFunc.circlePercent(x + Tmp.v2.x / 2, y + Tmp.v2.y / 2, length / 2f, Mathf.curve(fin, 0.1f, 1f), rotation - Mathf.curve(fin, 0.1f, 1f) * 180f - 180f);
			
			boolean enableDetials = NHSetting.enableDetails();
			
			if(enableDetials){
				float scl = size * tilesize * lightningCircleCurve.apply(fin);
				float fin_9 = Mathf.curve(fin, 0.95f, 1f);
				float sclSign = size * tilesize * lightningCircleCurve.apply(fin_9);
				Lines.stroke(fin * lightningCircleInScl * 4.5f);
				Lines.circle(x, y, scl * lightningCircleInScl);
				for(int i = 0; i < 4; i++){
					float rotation = Time.time + i * 90;
					Tmp.v1.trns(rotation, sclSign * lightningCircleInScl + Lines.getStroke() * 2f).add(tile);
					Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, NHContent.arrowRegion.width * Draw.scl * fin_9, NHContent.arrowRegion.height * Draw.scl * fin_9, rotation + 90);
				}
				
				Lines.stroke(fin * lightningCircleOutScl * 4.5f);
				Lines.circle(x, y, scl * lightningCircleOutScl);
				for(int i = 0; i < 4; i++){
					float rotation = -Time.time * 1.5f + i * 90;
					Tmp.v1.trns(rotation, sclSign * lightningCircleOutScl + Lines.getStroke() * 3f).add(tile);
					Draw.rect(NHContent.pointerRegion, Tmp.v1.x, Tmp.v1.y, NHContent.pointerRegion.width * Draw.scl * fin_9, NHContent.pointerRegion.height * Draw.scl * fin_9, rotation + 90);
				}
				
				float base = (Time.time / particleLife);
				rand.setSeed(drawSeed);
				
				fin = Mathf.curve(fin, 0.25f, 1f);
				
				if(fin > 0.05f)for(int i = 0; i < particles; i++){
					float finCurve = (rand.random(1f) + base) % 1f, foutCurve = 1f - finCurve;
					float fslopeCurve = (0.5f - Math.abs(finCurve - 0.5f)) * 2f;
					float angleRand = rand.random(45f) + rotation;
					float len = length * Interp.pow2Out.apply(finCurve);
					
					float f = Mathf.curve(foutCurve, 0f, 0.78f) * Interp.pow3Out.apply(Mathf.curve(finCurve, 0f, 0.15f)) * Mathf.curve(fin, 0f, 0.1f);
					
					Tmp.v2.trns(rand.range(150), length * rand.range(1.5f), length * rand.range(1.5f)).scl(f).add(tr2).add(tile);
					Tmp.v3.trns(rand.range(60), length * rand.range(1f), length * rand.random(0.75f)).scl(f).add(tr2).add(tile);
					Lines.stroke((isExtending ? f : foutCurve) * 3f - 1f);
					
					float spreadX = rand.range(chargeCircleBackRad * 0.7f), spreadY = rand.range(chargeCircleBackRad * 0.7f);
					float lerp = ((rand.random(1f) + base) % 1f * 3 - 1) / 2f;
					Lines.curve(x + tr2.x, y + tr2.y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, x + (tr.x + spreadX) * lerp, y + (tr.y + spreadY) * lerp, (int)len / tilesize * (NHSetting.enableDetails() ? 8 : 4));
					Fill.circle(x + (tr.x + spreadX) * lerp, y + (tr.y + spreadY) * lerp, Lines.getStroke() * 3 * rand.random(0.5f, 1.25f) * f);
				}
				
				if(fin < 0.01f) return;
				Fill.circle(x + tr2.x, y + tr2.y, fin * chargeCircleBackRad);
				Lines.stroke(fin * 3f - 1f);
				DrawFunc.circlePercentFlip(x + tr2.x, y + tr2.y, fin * (chargeCircleBackRad + 5), Time.time, 20f);
				Draw.color(Color.white);
				Fill.circle(x + tr2.x, y + tr2.y, fin * chargeCircleBackRad * 0.7f);
			}
			
			float cameraFin = (1 + 2 * DrawFunc.cameraDstScl(x + tr.x, y + tr.y, Vars.mobile ? 200 : 320)) / 3f;
			if(fin < 0.01f)return;
			float triWidth = fin * chargeCircleFrontRad / 3.5f * cameraFin;
			
			Draw.color(heatColor);
			for(int i : Mathf.signs){
				if(enableDetials)Fill.tri(x + tr.x, y + tr.y + triWidth, x + tr.x, y + tr.y - triWidth, x + tr.x + i * cameraFin * chargeCircleFrontRad * (23 + Mathf.absin(10f, 0.75f)) * (fin * 1.25f + 1f), y + tr.y);
				Drawf.tri(x + tr.x, y + tr.y, (fin + 1) / 2 * chargeCircleFrontRad / 1.5f, chargeCircleFrontRad * 10 * fin, i * 90 + Time.time * 1.25f);
				Drawf.tri(x + tr.x, y + tr.y, (fin + 1) / 2 * chargeCircleFrontRad / 2f, chargeCircleFrontRad * 6.5f * fin, i * 90 - Time.time);
			}
			
			Fill.circle(x + tr.x, y + tr.y, fin * chargeCircleFrontRad);
			DrawFunc.circlePercentFlip(x + tr.x, y + tr.y, fin * (chargeCircleFrontRad + 5), Time.time, 20f);
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
		public boolean isLocal(){
			return unit != null && unit().controller() == Vars.player;
		}
		
		@Override
		protected void effects(){
			Effect fshootEffect = shootEffect == Fx.none ? peekAmmo().shootEffect : shootEffect;
			Effect fsmokeEffect = smokeEffect == Fx.none ? peekAmmo().smokeEffect : smokeEffect;
			
			fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
			shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));
			
			if(shootShake > 0){
				Effect.shake(shootShake, shootShake, this);
			}
			
			recoil = recoilAmount;
		}
		
		@Override
		public void updateTile(){
			unit.ammo((float)unit.type().ammoCapacity * totalAmmo / maxAmmo);
			
			if(!validateTarget()) target = null;
			
			boolean cooled = Mathf.equal(heat, 0, 0.001f);
			wasShooting = false;
			
//			if(!Vars.headless && timer.get(4, 30f) && tile.dst(Vars.player) > tilesize * size) drawSeed -= 2;
			
			if(isExtending) loadProgress = Mathf.lerpDelta(loadProgress, reload, 0.5f);
			else{
				loadProgress = Mathf.lerpDelta(loadProgress, 0f, restitution * 2f);
			}
			
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
				
				if(cooled && !charging){
					wasShooting = isExtending = true;
					updateShooting();
				}
				
				if(isControlled()){ //player behavior
					targetPos.set(unit.aimX(), unit.aimY());
				}else{ //default AI behavior
					targetPosition(target);
					if(Float.isNaN(rotation)){
						rotation = 0;
					}
				}
				
				if(shouldTurn() && validateTarget()){
					turnToTarget(angleTo(targetPos));
				}
				
				if(acceptCoolant){
					updateCooling();
				}
			}
			
			
			
			if(!NHSetting.enableDetails())return;
			float fin = loadProgress / reloadTime;
			tr.trns(rotation, shootLength * curve.apply(fin));
			
			cir.set(tile).add(tr);
			if(Mathf.chanceDelta(0.3f * fin)){
				Tmp.v1.rnd(size * tilesize * lightningCircleCurve.apply(fin) * lightningCircleInScl).add(tile);
				NHFx.chainLightningFade.at(Tmp.v1.x, Tmp.v1.y, 12f, heatColor, cir);
			}
			if(Mathf.chanceDelta(0.3f * fin)){
				Tmp.v1.rnd(size * tilesize * lightningCircleCurve.apply(fin) * lightningCircleOutScl).add(tile);
				NHFx.chainLightningFadeReversed.at(Tmp.v1.x, Tmp.v1.y, 12f, heatColor, cir);
			}
		}
		
		@Override
		protected void updateShooting(){
			reload = Mathf.approach(reload, reloadTime, delta() * peekAmmo().reloadMultiplier * baseReloadSpeed());
			
			if(reload >= reloadTime && validateTarget() && !charging){
				boolean canShoot = true;
				
				if(isControlled()){ //player behavior
					canShoot = unit.isShooting();
				}else if(logicControlled()){ //logic behavior
					canShoot = logicShooting;
				}
				
				float targetRot = angleTo(targetPos);
				
				if(Angles.angleDist(rotation, targetRot) < shootCone && canShoot){
					BulletType type = peekAmmo();
					
					shoot(type);
					
					reload %= reloadTime;
				}
			}
		}
		
		@Override
		protected void shoot(BulletType type){
			super.shoot(type);
			isExtending = false;
			
			for(int i = 0; i < Mathf.random(shootEffectMin, shootEffectMax); i++){
				Tmp.v1.trns(rotation, Mathf.range(shootEffectSpreadX), Mathf.range(shootEffectSpreadY));
				shootEffect.at(x + tr.x + Tmp.v1.x, y + tr.y + Tmp.v1.y, rotation, heatColor, Mathf.range(0.6f, 0.75f));
			}
		}
	}
}
