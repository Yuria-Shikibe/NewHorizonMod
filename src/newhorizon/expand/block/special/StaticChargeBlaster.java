package newhorizon.expand.block.special;


import arc.Core;
import arc.audio.Sound;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Posc;
import mindustry.gen.Sounds;
import mindustry.gen.Unitc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.StatusEffect;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHFx;
import newhorizon.util.feature.PosLightning;

import static mindustry.Vars.tilesize;

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
	public int generateLiNum = 3, generateLiLen = 10, generateLenRand = 0;
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
	public void setStats(){
		super.setStats();
		stats.add(Stat.range, range / tilesize, StatUnit.blocks);
		stats.add(Stat.damage, damage);
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
			if(validateTarget()) {
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
				consume();
			} else {
				reload += Time.delta * efficiency();
			}
		}
		
		protected void effectFriend(){
			Units.nearby(team, x, y, range(), unit -> {
				unit.apply(positiveStatus, statusDuration);
				acceptEffect.at(unit);
				unit.heal(heal);
			});
		}
		
		protected void blast() {
			Units.nearbyEnemies(team, x, y, range, unit -> {
				unit.apply(status, statusDuration);
				unit.impulse(Tmp.v3.set(unit).sub(x, y).nor().scl(knockback * 40.0f));
				acceptEffect.at(unit);
				PosLightning.create(this, team, this, unit, lightningColor, false, damage, 0, PosLightning.WIDTH, gettingBoltNum, p -> {
					NHFx.lightningHitSmall.at(p.getX(), p.getY(), lightningColor);
				});
			});
		}
		
		protected void extra(){
			Effect.shake(blastShake, blastShake, this);
			generateEffect.at(this);
			blastSound.at(this, Mathf.random(0.9f, 1.1f));
			for (int i = 0; i < generateLiNum; i++) {
				Lightning.create(team, lightningColor, lightningDamage < 0 ? damage : lightningDamage, x, y, Mathf.range(360f), generateLiLen + Mathf.random(generateLenRand));
			}
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









