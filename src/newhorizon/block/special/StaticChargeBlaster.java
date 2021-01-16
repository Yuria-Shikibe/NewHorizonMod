package newhorizon.block.special;


import arc.*;
import arc.audio.Sound;
import arc.math.geom.*;
import arc.math.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.entities.bullet.SapBulletType;
import mindustry.game.Team;
import mindustry.logic.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;

import newhorizon.content.NHFx;
import newhorizon.func.PosLightning;

import static mindustry.Vars.*;

public class StaticChargeBlaster extends Block {
	public TextureRegion heatRegion;
	public TextureRegion[] chargers;
	public Color heatColor = Color.valueOf("#FFBDAD");
	public float knockback = 0f;
	public float blastShake = 8f;
	public float cooldown = 0.005f;
	public float range = 160f;
	public float reloadTime = 90f;
	public float damage = 10, heal = 0;
	public float chargerOffset = tilesize * size / 4f, rotateOffset = 0f;
	public boolean targetHost = true;
	public boolean targetFriendly = false;
	public float statusDuration = 60f;
	public StatusEffect status = StatusEffects.none;
	public StatusEffect positiveStatus = StatusEffects.none;
	public Effect generateEffect = Fx.none;
	public Effect acceptEffect = Fx.none;
	public Sound blastSound = Sounds.explosionbig;
	//
	public int generateLiNum = 0, generateLiLen = 10, generateLiRand = 0;
	public float lightningDamage = 120;
	public Color lightningColor = Color.valueOf("#FFBDAD");
	//^Lightning generate settings;
	public int gettingBoltNum = 0;
	//^Point to point LightningBolt generate num;
	public StaticChargeBlaster(String name) {
		super(name);
		timers = 2;
		update = true;
		//configurable = true;
		solid = true;
	}

	@Override
	public void load() {
		super.load();
		chargers = new TextureRegion[4];
		for (int i = 0; i < 4; i++) {
			chargers[i] = Core.atlas.find(name + "-charger-" + i);
		}
		heatRegion = Core.atlas.find(name + "-heat");
	}

	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
	}

	public class StaticChargeBlasterBuild extends Building implements Ranged {
		public float reload, heat;
		public Posc target;

		protected void findTarget() {
			target = Units.closestTarget(team, x, y, range());
		}

		protected boolean validateTarget() {
			return !(Units.invalidateTarget(target, team, x, y) || !(target instanceof Unitc));
		}

		@Override
		public float range() {
			return range;
		}

		@Override
		public void updateTile() {
			if (!validateTarget()) target = null;
			heat = Mathf.lerpDelta(heat, 0f, cooldown);
			
			if (!consValid())return;
			if (timer(0, 20)) {
				findTarget();
			}
			if (validateTarget()) {
				updateCharging();
			}
		}

		protected void updateCharging() {
			if (reload >= reloadTime) {
				if(targetHost)blast();
				if(targetFriendly)effectFriend();
				extra();
				heat = 1.8f;
				reload = 0f;
			} else {
				reload += Time.delta * efficiency();
			}
		}
		
		protected void effectFriend(){
			Units.nearby(team, x, y, range(), unit -> {
				unit.apply(positiveStatus, statusDuration);
				acceptEffect.at(unit);
				unit.heal(heal * Time.delta);
			});
		}
		
		protected void blast() {
			Rect rect = new Rect();
			if(generateLiNum > 0)PosLightning.create(this, target, team, lightningColor, true, damage, 4, PosLightning.WIDTH, 2, p ->{
				NHFx.lightningHitSmall(lightningColor).at(p);
			});
			Units.nearbyEnemies(team, rect.setSize(range * 2).setCenter(x, y), unit -> {
				unit.apply(status, statusDuration);
				unit.impulse(Tmp.v3.set(unit).sub(x, y).nor().scl(knockback * 80.0f));
				acceptEffect.at(unit);
				unit.damage(damage * Time.delta);
			});
			
		}
		
		protected void extra(){
			Effect.shake(blastShake, blastShake, this);
			generateEffect.at(this);
			blastSound.at(this, Mathf.random(0.9f, 1.1f));
			for (int i = 0; i < generateLiNum; i++) {
				Lightning.create(team, lightningColor, lightningDamage < 0 ? damage : lightningDamage, x, y, Mathf.range(360f), generateLiLen + Mathf.random(generateLiRand));
			}
			PosLightning.createRange(this, team, range, gettingBoltNum, lightningColor, false, 0, 0, PosLightning.WIDTH, 3, p -> NHFx.lightningHitLarge(lightningColor).at(p));
		}

		@Override
		public void drawSelect() {
			Drawf.dashCircle(x, y, range, team.color);
		}

		@Override
		public void draw() {
			Draw.rect(region, x, y);
			Draw.color(heatColor, heat);
			Draw.blend(Blending.additive);
			Draw.rect(heatRegion, x, y);
			Draw.blend();
			Draw.reset();
			for (int i = 0; i < 4; i++) {
				Vec2 vec = new Vec2();
				vec.trns(-i * 90 + rotateOffset, Mathf.pow((reload / reloadTime), 2.3f) * chargerOffset);
				Draw.rect(chargers[i], x + vec.x, y + vec.y);
			}
		}



	}
}









