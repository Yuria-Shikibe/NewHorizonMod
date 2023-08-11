package newhorizon.expand.weather;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.noise.Noise;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.type.Weather;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.blocks.Attributes;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Stat;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.content.NHFx;
import newhorizon.content.NHShaders;
import newhorizon.content.NHSounds;
import newhorizon.expand.bullets.TrailFadeBulletType;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class MatterStorm extends Weather{
	public Color textureColor = null;
	public Color secondaryColor = Pal.ammo;
	public Color primaryColor = Pal.redderDust;
	
	public boolean rotateBullets = false;
	
	/** Default to be disabled, set it above 0 to activate it.*/
	public float buildingEmp = -1;
	
	public float alphaMin = 0.075f, alphaMax = 0.28f;
	public float alphaScl = 32f;
	
	public float colorScl = 22f, colorMag = 0.9f;
	
	public float force = 7;
	public float overload = 1.2f;
	
	public float sparkEffectChance = 0.125f;
	
	public Sound noise = NHSounds.shock;
	public float noiseChance = 0.0225f;
	
	public Effect sparkEffect2 = NHFx.hitSparkLarge;
	public Effect sparkEffect = new Effect(45f, e -> {
		if(!(e.data instanceof Number))return;
		float data = ((Number)e.data()).floatValue();
		
		Draw.color(e.color, Color.white, e.fout() * 0.53f);
		Lines.stroke(e.fout() * 3);
		
		float len = Mathf.clamp(data / 16, 4, 20);
		
		Rand rand = NHFunc.rand;
		rand.setSeed(e.id);
		
		Tmp.v1.trns(e.rotation - 180, data * 1.25f).add(e.x, e.y);
		
		Angles.randLenVectors(e.id, (int)Mathf.clamp(data / 24, 4, 30), e.fin(Interp.pow3Out) * data * 3, e.rotation, 85f, (x, y) -> {
			float ang = Mathf.angle(x, y);
			Lines.lineAngle(Tmp.v1.x + x, Tmp.v1.y + y, ang, e.fin(NHInterp.parabola4Reversed) * len * 0.85f * rand.random(0.8f, 1.2f) + len * 0.35f * e.fout());
		});
	});
	
	public float bulletDamage = 120f;
	public float bulletVelocityMin = 0.6f, bulletVelocityMax = 1.4f, bulletLifeMin = 0.8f, bulletLifeMax = 2f;
	public float bulletSpawnChance = 0.075f;
	public float bulletSpawnNum = 2;
	public float empScale = 0.75f;
	public BulletType bulletType = null;
	
	public MatterStorm(String name){
		super(name, AdaptedWeatherState::new);
		
		opacityMultiplier = 3;
		sound = Sounds.pulse;
		duration = 0.4f * Time.toMinutes;
		
		attrs = new Attributes();
		attrs.set(Attribute.light, 6f);
		
		alwaysUnlocked = true;
		details = Core.bundle.get("mod.ui.weather-matter-storm.detail");
	}
	
	@Override
	public boolean isHidden(){
		return false;
	}

	@Override
	public void setStats(){
		super.setStats();

		stats.add(Stat.abilities, table -> {
			table.row().table().padLeft(OFFSET * 2).getTable().table(t -> {
				t.align(Align.topLeft);
				t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-air") + ": " + TableFunc.judge(bulletType.collidesAir && bulletType.collides)).left().row();
				t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-ground") + ": " + TableFunc.judge(bulletType.collidesGround && bulletType.collides)).left().row();
				t.add("[lightgray]" + Core.bundle.get("mod.ui.collide-tile") + ": " + TableFunc.judge(bulletType.collidesTiles)).left().row();
				t.add("[lightgray]" + Core.bundle.get("matter-storm.turn-bullets") + ": " + TableFunc.judge(rotateBullets)).left().row();
				t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).padLeft(-OFFSET * 2).padRight(-OFFSET * 2).row();
				
				t.add("[lightgray]" + Core.bundle.get("mod.ui.absorbable") + ": " + TableFunc.judge(bulletType.absorbable)).left().row();
				t.add("[lightgray]" + Core.bundle.get("mod.ui.hittable") + ": " + TableFunc.judge(bulletType.hittable)).left().row();
				
				t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).padLeft(-OFFSET * 2).padRight(-OFFSET * 2).row();
				
				if(status != null && status != StatusEffects.none && !status.isHidden())t.table(info -> {
					info.left();
					info.add("[lightgray]" + Core.bundle.get("content.status.name") + ": ").padRight(OFFSET);
					info.button(new TextureRegionDrawable(status.uiIcon), Styles.cleari, () -> {
						new ContentInfoDialog().show(status);
					}).scaling(Scaling.fit);
				}).fill().row();
				
				t.image().color(Color.gray).pad(OFFSET / 2).growX().height(OFFSET / 4).padLeft(-OFFSET * 2).padRight(-OFFSET * 2).row();
			}).fill().padBottom(OFFSET).left().row();
		});
	}

	@Override
	public void createIcons(MultiPacker packer){
		TextureRegion region = Core.atlas.find(name, NewHorizon.name("weather-icon"));

		if(NHPixmap.isDebugging() && region != null && region.found()){
			if(textureColor != null)NHPixmap.addProcessed(name + "-full", NHPixmap.fillColor(Core.atlas.getPixmap(region), textureColor).outline(Color.valueOf("404049"), 3));
			else NHPixmap.addProcessed(name + "-full", Pixmaps.outline(Core.atlas.getPixmap(region), Color.valueOf("404049"), 3));
		}else super.createIcons(packer);
	}

	@Override
	public void init(){
		super.init();

		if(bulletType == null){
			bulletType = new TrailFadeBulletType(18, bulletDamage){{
				disableAccel();
				width = 0;
				height = 0;
				trailRotation = true;
				pierce = true;
				pierceCap = 3;
				hitBlinkTrail = false;
				collidesTiles = false;
				tracerStroke = 4;
				drawSize = 1600;
				tracerUpdateSpacing = 1;
				tracerRandX = 12;
				tracerSpacing = 12;
				tracers = 1;
				weaveMag = 2;
				weaveScale = 12;
				lifetime = 40f;
				hitColor = lightningColor = frontColor = backColor = trailColor = lightColor = secondaryColor.cpy().lerp(primaryColor, 0.4f);
				lightning = 4;
				lightningLength = 8;
				lightningLengthRand = 13;
				shootEffect = NHFx.instShoot(hitColor, frontColor);
				hitEffect = NHFx.hitSpark(hitColor, 45f, 20, 50f, 2.8f, 12);
				smokeEffect = Fx.smokeCloud;
				trailEffect = Fx.none;
				despawnEffect = NHFx.square45_8_45;
				lightningDamage = damage / 5;
				hitShake = 8f;
				knockback = 6f;
				addBeginPoint = despawnHit = true;
				
				
				hitSound = Sounds.plasmaboom;
				despawnSound = Sounds.plasmaboom;
				hitSoundVolume = 0.2f;
			}

				@Override
				public void hit(Bullet b, float x, float y){
					super.hit(b, x, y);
					UltFire.createChance(x, y, splashDamageRadius, 0.2f, b.team);
				}

				@Override
				public void draw(Bullet b){
					drawTrail(b);
				}

				@Override
				public void init(Bullet b){
					super.init(b);
					despawnEffect.at(b.x, b.y, 0, hitColor);
					Sounds.spark.at(b);
				}
			};
		}
	}

	@Override
	public void updateEffect(WeatherState state){
		{
			float speed = force * state.intensity * Time.delta;
			
			if(speed > 0.001f && state.effectTimer <= 0){
				state.effectTimer = statusDuration - 5f;
				float ang = state.windVector.angle();
				
				if(!Vars.headless)Vars.renderer.shake(force / 3, force);
				
				for(Unit entity : Groups.unit){
					if(entity.checkTarget(statusAir, statusGround) && !GravityTrapField.IntersectedAlly.get(entity.team, entity)){
						if(status != StatusEffects.none)entity.apply(status, statusDuration);
						entity.impulse(Tmp.v1.set(state.windVector).scl(speed * (entity.isFlying() ? 1 : 0.4f)));
						entity.reloadMultiplier(overload);
						if(Mathf.chanceDelta(sparkEffectChance * Time.delta))sparkEffect.at(entity.x, entity.y, ang, getColor(), entity.hitSize);
					}
				}
				
				if(rotateBullets)for(Bullet entity : Groups.bullet){
					if(entity.type.absorbable && !GravityTrapField.IntersectedAlly.get(entity.team, entity)){
						entity.vel().setAngle(Angles.moveToward(entity.vel.angle(), ang, speed / 500 * entity.vel.len()));
						entity.vel().add(Tmp.v1.set(state.windVector).scl(speed / 220));
					}
				}
				
				if(buildingEmp > 0){
					Groups.build.each(b -> b.isValid() && b.block.hasPower , b -> {
						b.applySlowdown(buildingEmp, statusDuration * 5f);
					});
				}
				
			}else{
				state.effectTimer -= Time.delta;
			}
		}
		
		if(!headless && sound != Sounds.none){
			float noise = soundVolOscMag > 0 ? (float)Math.abs(Noise.rawNoise(Time.time / soundVolOscScl)) * soundVolOscMag : 0;
			control.sound.loop(sound, Math.max((soundVol + noise) * state.opacity, soundVolMin));
		}
		
		if(!Vars.net.client() && Mathf.chanceDelta(bulletSpawnChance * state.intensity * 1.25f))for(int i = 0; i < 4; i++){
			float randX = Mathf.random(Vars.world.unitWidth()), randY = Mathf.random(Vars.world.unitHeight());
			float maxRange = bulletLifeMax * bulletType.range;
			float ang = state.windVector.angle();
			float maxLife = bulletLifeMax;

			Vars.world.getQuadBounds(Tmp.r1);
			if(!Tmp.r1.contains(Tmp.v1.trns(ang, maxRange).add(randX, randY))){
				maxLife = ((randY > Vars.world.unitHeight() / 2f ? Vars.world.unitHeight() - randY : randY) + Vars.finalWorldBounds) / Math.abs(Mathf.sinDeg(ang)) / bulletType.range;
			}

			bulletType.createNet(Team.derelict, randX, randY, ang, bulletType.damage * (state.intensity + 1), Mathf.random(bulletVelocityMin, bulletVelocityMax), Mathf.random(Math.min(bulletLifeMin, maxLife), maxLife));
		}
		
		if(!Vars.headless && Mathf.chanceDelta(noiseChance)){
			float randX = Mathf.random(Vars.world.unitWidth()), randY = Mathf.random(Vars.world.unitHeight());
			noise.at(randX, randY, Mathf.random(0.9f, 1.1f), Mathf.sqrt(state.intensity) + 1f);
		}
	}
	
	@Override
	public void drawUnder(WeatherState state){

	}

	//Tmp!
	public Color getColor(){
		return Tmp.c1.set(primaryColor).lerp(secondaryColor, Mathf.absin(colorScl, colorMag));
	}

	@Override
	public void drawOver(WeatherState state){
		//TODO mobile fix
		if(Vars.mobile)return;
		Drawf.light(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f, 1000000f, getColor(), state.opacity);

		Draw.blend();

		float a = Draw.getColor().a;

		Draw.color(primaryColor, Tmp.c2.set(primaryColor).lerp(secondaryColor, 0.5f).lerp(Color.white, 0.25f), secondaryColor, Mathf.absin(colorScl, colorMag));
		Vars.renderer.effectBuffer.begin(Tmp.c1.set(Draw.getColor()).a(((alphaMin + Mathf.absin(alphaScl, alphaMax - alphaMin)) * a) * state.opacity));
		Vars.renderer.effectBuffer.end();
		Vars.renderer.effectBuffer.blit(Shaders.screenspace);

		if(!NHSetting.enableDetails())return;

		Draw.blend();
		Vars.renderer.effectBuffer.begin(Color.clear);
		Vars.renderer.effectBuffer.end();

		NHShaders.matterStorm.primaryColor.set(Tmp.c1.set(primaryColor).a(state.opacity));
		NHShaders.matterStorm.applyDirection(state.windVector, state.intensity);
		NHShaders.matterStorm.secondaryColor.set(Tmp.c1.set(secondaryColor).lerp(Color.white, Mathf.absin(8f, 0.4f)).a(state.opacity * a));

		Vars.renderer.effectBuffer.blit(NHShaders.matterStorm);
	}
	
	@Override
	public WeatherState create(float intensity, float duration){
		return super.create(intensity, duration);
	}
	
	public static class AdaptedWeatherState extends WeatherState{
		public float prepareReload = 450f;
		
		
		@Override
		public void init(Weather weather){
			super.init(weather);
			
			prepareReload = Mathf.clamp(300 + (float)Math.log(intensity) * 60f, 300, 1500);
		}
		
		@Override
		public void update(){
			prepareReload -= Time.delta;
			
			if(prepareReload < 0){
				super.update();
			}
			
			if(prepareReload > -180)NHSounds.alertLoop();
		}
		
		public static AdaptedWeatherState create(){
			return Pools.obtain(AdaptedWeatherState.class, AdaptedWeatherState::new);
		}
		
		@Override
		public void read(Reads read){
			super.read(read);
			
			prepareReload = read.f();
		}
		
		@Override
		public void readSync(Reads read){
			super.readSync(read);
			
			prepareReload = read.f();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			
			write.f(prepareReload);
		}
		
		@Override
		public void writeSync(Writes write){
			super.writeSync(write);
			
			write.f(prepareReload);
		}
		
		@Override
		public int classId(){
			return EntityRegister.getID(AdaptedWeatherState.class);
		}
	}
}
