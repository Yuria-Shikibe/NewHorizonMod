package newhorizon.contents.blocks.special;


import arc.*;
import arc.audio.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.scene.ui.layout.*;
import arc.math.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import mindustry.logic.*;
import mindustry.game.*;
import mindustry.ctype.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import newhorizon.contents.bullets.special.*;
import static mindustry.Vars.*;

public class Influencer extends Block {
	public TextureRegion heatRegion;
	public TextureRegion[] chargers;
	public Color heatColor = Color.valueOf("#FFBDAD");
	public float knockback = 0f;
	public float blastShake = 8f;
	public float cooldown = 0.02f;
	public float range = 160f;
	public float reloadTime = 90f;
	public float damage = 10, heal = 0;
	public float chargerOffset = tilesize * size / 4, rotateOffset = 0f;
	public boolean targetHost = true;
	public boolean targetFriendly = false;
	public float statusDuration = 60f;
	public StatusEffect status = StatusEffects.none;
	public StatusEffect positiveStatus = StatusEffects.none;
	public Effect generateEffect = Fx.none;
	public Effect acceptEffect = Fx.none;
	//public Sound blastSound = Sounds.explosionbig;
	//
	public int generateLiNum = 0, generateLiLen = 10, generateLiRand = 0;
	public float lightningDamage = 120;
	public Color lightningColor = Color.valueOf("#FFBDAD");
	//^Lightning generate settings;
	public int gettingBoltNum = 0;
	//^Point to point LightningBolt generate num;
	public Influencer(String name) {
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

	public class InfluencerBuild extends Building implements Ranged {
		public float reload, heat;
		public Posc target;

		protected void findTarget() {
			target = Units.closestTarget(team, x, y, range());
		}

		protected boolean validateTarget() {
			return !Units.invalidateTarget(target, team, x, y);
		}

		@Override
		public float range() {
			return range;
		}

		@Override
		public void updateTile() {
			if (!validateTarget()) target = null;
			heat = Mathf.lerpDelta(heat, 0f, cooldown);
			
			//if (!consValid())return;
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
				heat = 1.2f;
				reload = 0f;
			} else {
				reload += Time.delta * efficiency();
			}
		}
		
		protected void effectFriend(){
			Rect rect = new Rect();
			Units.nearby(team, x, y, range(), unit -> {
				unit.apply(positiveStatus, statusDuration);
				acceptEffect.at(unit);
				unit.heal(heal * Time.delta);
			});
		}
		
		protected void blast() {
			Rect rect = new Rect();
			if(generateLiNum > 0)NHLightningBolt.generate(this, target, team, lightningColor, NHLightningBolt.WIDTH, 2, e ->{
				Damage.damage(team, e.getX(), e.getY(), 40f, damage * 3f);
				new Effect(25, eff -> {
					Draw.color(eff.color);
						eff.scaled(12, t -> {
						Lines.stroke(3f * t.fout());
						Lines.circle(eff.x, eff.y, 3f + t.fin() * 80f);
					});
					Fill.circle(eff.x, eff.y, eff.fout() * 12f);
				}).at(e.getX(), e.getY(), lightningColor);
			});
			Units.nearbyEnemies(team, rect.setSize(range * 2).setCenter(x, y), unit -> {
				unit.apply(status, statusDuration);
				unit.impulse(Tmp.v3.set((Position)unit).sub(x, y).nor().scl(knockback * 80.0f));
				acceptEffect.at(unit);
				unit.damage(damage * Time.delta);
			});
			
		}
		
		protected void extra(){
			Effect.shake(blastShake, blastShake, this);
			generateEffect.at(this);
			//blastSound.at(this, Mathf.random(0.9f, 1.1f));
			for (int i = 0; i < generateLiNum; i++) {
				Lightning.create(team, lightningColor, lightningDamage < 0 ? damage : lightningDamage, x, y, Mathf.range(360f), generateLiLen + Mathf.random(generateLiRand));
			}
			NHLightningBolt.generateRange(this, team, range, gettingBoltNum, 2, lightningDamage < 0 ? damage : lightningDamage, lightningColor, true, NHLightningBolt.WIDTH);
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









