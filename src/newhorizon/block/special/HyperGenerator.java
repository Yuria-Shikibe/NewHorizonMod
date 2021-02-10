package newhorizon.block.special;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHBullets;
import newhorizon.content.NHFx;
import newhorizon.feature.PosLightning;

import static mindustry.Vars.state;


public class HyperGenerator extends PowerGenerator{
	private static final float minWarmup = 0.075f;
	
	public TextureRegion bottomRegion, armorRegion, gateRegion;
	public TextureRegion[] plasmaRegions;
	public float warmupSpeed = 0.0015f;
	public float disabledSpeed = 0.015f;
	public float itemDuration = 120f;
	
	public float structureLim = 0.3f;
	public float beginDamageScl = 0.01f;
	public float destroyedExplodeLimit = 0.5f;
	
	public int updateLightning;
	public int updateLightningRand;
	public float lightningRange = 160f;
	public int lightningLen = 4;
	public int lightningLenRand = 8;
	public float lightningDamage = 120f;
	public int subNum = 2;
	public int subNumRand = 1;
	
	public Cons<HyperGeneratorBuild> explodeAction = entity -> {};
	public Cons<Position> explodeSub = entity -> {};
	public float explosionRadius;
	public float explosionDamage;
	
	public float maxVelScl = 1.25f, minVelScl = 0.75f;
	public float maxTimeScl = 1.25f, minTimeScl = 0.75f;
	
	public float plasmaScl = 0.975f;
	public float gateSize = 5f;
	public float effectCircleSize = -1;
	public float triWidth = 6f;
	public float triLength = 83f;
	public Effect updateEffect = NHFx.circle;
	public Effect workEffect = NHFx.line;
	public float updateEffectDiv = 20f;
	public float updateEffectSize = 20f;
	public float effectSinScl = 0.75f;
	public float effectSinMag = 0.11f;
	public Color effectColor;
	
	public float workShake = 8f;
	public Sound workSound = Sounds.plasmaboom;
	
	public HyperGenerator(String name){
		super(name);
		this.hasPower = true;
		this.hasLiquids = true;
		this.liquidCapacity = 30.0F;
		this.hasItems = true;
		this.outputsPower = this.consumesPower = true;
		expanded = true;
		this.explosionRadius = 220f;
		this.explosionDamage = 14000f;
	}
	
	public void setBars() {
		super.setBars();
		this.bars.add("poweroutput", (HyperGeneratorBuild entity) -> new Bar(
			() -> Core.bundle.format("bar.poweroutput", Strings.fixed(Math.max(entity.getPowerProduction() - this.consumes.getPower().usage, 0.0F) * 60.0F * entity.timeScale, 1)),
			() -> Pal.powerBar,
			() -> entity.productionEfficiency)
		);
	}
	
	public void setStats() {
		super.setStats();
		if (this.hasItems) {
			this.stats.add(Stat.productionTime, this.itemDuration / 60.0F, StatUnit.seconds);
		}
	}
	
	@Override
	protected TextureRegion[] icons(){
		return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{this.bottomRegion, this.region, this.teamRegions[Team.sharded.id], armorRegion} : new TextureRegion[]{this.bottomRegion, this.region, armorRegion};
	}
	
	@Override
	public void init(){
		super.init();
		if(effectCircleSize < 0)effectCircleSize = size * Vars.tilesize / 6f;
	}
	
	
	@Override
	public void load(){
		super.load();
		bottomRegion = Core.atlas.find(name + "-bottom");
		armorRegion = Core.atlas.find(name + "-armor");
		gateRegion = Core.atlas.find(name + "-gate");
		plasmaRegions = new TextureRegion[4];
		
		for(int index = 0; index < 4; ++index) {
			plasmaRegions[index] = Core.atlas.find(name + "-plasma-" + index);
		}
	}
	
	public class HyperGeneratorBuild extends PowerGenerator.GeneratorBuild{
		public float progress;
		public float warmup;
		
		public Color getColor(){
			return effectColor == null ? team.color : effectColor;
		}
		
		@Override
		public void updateTile(){
			super.updateTile();
			if(consValid()){
				if (this.timer(0, itemDuration / timeScale)) {
					this.consume();
					workSound.at(this, Mathf.random(0.9f, 1.1f));
					NHFx.hyperInstall.at(x, y, effectCircleSize / 1.5f * (warmup + 0.3f), getColor());
					Effect.shake(workShake, workShake, this);
					if(warmup > destroyedExplodeLimit && Mathf.chanceDelta(warmup / 2))PosLightning.createRandomRange(state.rules.waveTeam, this, lightningRange, getColor(), true, lightningDamage * (Mathf.curve(1 - health / maxHealth(), structureLim, 1f) + beginDamageScl), lightningLen + Mathf.random(lightningLenRand), PosLightning.WIDTH, subNum + Mathf.random(subNumRand),updateLightning + Mathf.random(updateLightningRand), point -> {
						NHFx.lightningHitLarge(getColor()).at(point);
					});
				}
				progress += efficiency() * Time.delta;
				if(Mathf.equal(warmup, 1.0F, 0.0015F)){
					warmup = 1.0F;
				}else warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed);
			}else{
				if(Mathf.equal(warmup, 0F, 0.0015F)){
					warmup = 0F;
				}else warmup = Mathf.lerpDelta(warmup, 0, disabledSpeed);
			}
			productionEfficiency = Mathf.pow(warmup, 5.0F);
			
			if(warmup > minWarmup){
				for(int i : Mathf.signs){
					Drawf.tri(x, y, triWidth * warmup, triLength, (i + 1) * 90);
					if(Mathf.chance(warmup / updateEffectDiv)) updateEffect.at(x + i * Mathf.random(effectCircleSize), y + i * Mathf.random(effectCircleSize), updateEffectSize * warmup, getColor());
				}
				if(Mathf.chance( Mathf.curve(1 - health / maxHealth(), structureLim, 1f) / 25f))PosLightning.createRandomRange(state.rules.waveTeam, this, lightningRange, getColor(), true, lightningDamage * (Mathf.curve(1 - health / maxHealth(), structureLim, 1f) + beginDamageScl), lightningLen + Mathf.random(lightningLenRand), PosLightning.WIDTH, subNum + Mathf.random(subNumRand),updateLightning + Mathf.random(updateLightningRand), point -> {
					NHFx.lightningHitLarge(getColor()).at(point);
				});
				if(Mathf.chance(warmup / updateEffectDiv * 1.5f)) workEffect.at(x, y, updateEffectSize * 3f * warmup, getColor());
			}
		}
		
		@Override
		public void draw(){
			super.draw();
			Color drawColor = Tmp.c1.set(getColor()).lerp(Color.white, Mathf.absin(Time.time * 1.3f, 1.0F, 0.08F));
			
			Draw.rect(bottomRegion, x, y);
			
			for(int i = 0; i < plasmaRegions.length; ++i) {
				float r = (size * Vars.tilesize - 3.0F + Mathf.absin(Time.time, 2.0F + i, 5.0F - i * 0.5F)) * plasmaScl;
				Draw.color(getColor(), drawColor, (float)(i / plasmaRegions.length));
				Draw.alpha((0.25F + Mathf.absin(Time.time, 2.0F + i * 2.0F, 0.3F + i * 0.05F)) * warmup);
				Draw.blend(Blending.additive);
				Draw.rect(plasmaRegions[i], x, y, r, r, Time.time * (12.0F + i * 6.0F) * warmup);
				Draw.blend();
			}
			Draw.color();
			
			for(int i = 0; i < 4; i++){
				for(int j : Mathf.signs){
					Tmp.v1.trns(90 * i + 45, -(1 - j) * size * Vars.tilesize * Draw.scl * Mathf.sqrt2);
					Tmp.v2.trns(90 * i - 45 , -gateSize * j * warmup).add(Tmp.v1);
					TextureRegion gate = new TextureRegion(gateRegion);
					gate.flip(j < 0, j < 0);
					Draw.rect(gate, x + Tmp.v2.x, y + Tmp.v2.y, i * 90);
				}
			}
			
			Draw.rect(region, x, y);
			Draw.rect(armorRegion, x, y);
			
			if(warmup > minWarmup){
				float drawSin = (1 + Mathf.absin(progress, effectSinScl * 2f, effectSinMag * 1.1f)) * warmup;
				float drawSin2 = (1 + Mathf.absin(progress, effectSinScl, effectSinMag)) * warmup;
				float drawSin3 = (1 + Mathf.absin(progress + effectSinScl / 2, effectSinScl, effectSinMag)) * warmup;
				
				Draw.z(Layer.effect - 1f);
				Draw.color(drawColor);
				
				Fill.circle(x, y, effectCircleSize * drawSin);
				for(int i : Mathf.signs){
					Drawf.tri(x, y, triWidth * warmup, triLength * drawSin2, (i + 1) * 90 + progress);
					Drawf.tri(x, y, triWidth * warmup, triLength * drawSin3, (i + 1) * 90 - progress + 90);
				}
			}
			Draw.reset();
		}
		
		@Override
		public void onDestroyed(){
			super.onDestroyed();
			if(warmup < destroyedExplodeLimit)return;
			explodeAction.get(this);
			int i;
			
			for(i = 0; i < 30; i++){
				Time.run(Mathf.random(80f), () -> {
					explodeSub.get(this);
					Sounds.bang.at(this);
					Sounds.explosionbig.at(this);
					NHBullets.hyperBlast.create(this, state.rules.waveTeam, x, y, Mathf.random(360), NHBullets.hyperBlast.damage * baseExplosiveness, Mathf.random(minVelScl, maxVelScl), Mathf.random(minTimeScl, maxTimeScl), new Object());
				});
			}
			
			for(i = 0; i < 10; i++){
				Time.run(i * (2 + Mathf.random(2f)), () -> {
					explodeSub.get(this);
					Sounds.explosionbig.at(this);
					PosLightning.createRandomRange(state.rules.waveTeam, this, lightningRange, getColor(), true, lightningDamage, lightningLen + Mathf.random(lightningLenRand), PosLightning.WIDTH, subNum + Mathf.random(subNumRand),updateLightning + Mathf.random(updateLightningRand), point -> {
						NHFx.lightningHitLarge(getColor()).at(point);
					});
				});
			}
			
			Sounds.explosionbig.at(this);
			Effect.shake(6.0F, 16.0F, x, y);
			
			for(i = 0; i < 7; ++i) {
				Time.run((float)Mathf.random(80), () -> {
					NHFx.hyperExplode.at(x + Mathf.range(size * Vars.tilesize), y + Mathf.range(size * Vars.tilesize), getColor());
					NHFx.hyperCloud.at(x + Mathf.range(size * Vars.tilesize), y + Mathf.range(size * Vars.tilesize), getColor());
					NHFx.circle.at(x + Mathf.range(size * Vars.tilesize), y + Mathf.range(size * Vars.tilesize), explosionRadius, getColor());
				});
			}
			
			Damage.damage(x, y, explosionRadius, explosionDamage);
		}
		
		public float ambientVolume() {
			return this.warmup;
		}
		
		@Override
		public void drawLight(){
			Drawf.light(this.team, this.x, this.y, (110.0F + Mathf.absin(5.0F, 5.0F)) * this.warmup, getColor(), 0.8F * this.warmup);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			this.warmup = read.f();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(warmup);
		}
	}
}
