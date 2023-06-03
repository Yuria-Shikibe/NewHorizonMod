package newhorizon.expand.entities;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.util.Time;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.Puddles;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.world.Tile;
import mindustry.world.meta.Attribute;
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.content.NHColor;
import newhorizon.content.NHStatusEffects;

import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.rect;
import static mindustry.Vars.*;

public class UltFire extends Fire{
	public static TextureRegion[] ultRegion = new TextureRegion[40];
	
	public static final Effect remove = new Effect(70f, e -> {
		alpha(e.fout());
		rect(ultRegion[((int)(e.rotation + e.fin() * Fire.frames)) % Fire.frames], e.x + Mathf.randomSeedRange((int)e.y, 2), e.y + Mathf.randomSeedRange((int)e.x, 2));
		Drawf.light(e.x, e.y, 50f + Mathf.absin(5f, 5f), NHColor.lightSkyBack, 0.6f  * e.fout());
	});
	
	public static final float baseLifetime = 1200f;
	
	public static void create(float x, float y, Team team){
		Tile tile = Vars.world.tile(World.toTile(x), World.toTile(y));
		
		if(tile != null && tile.build != null && tile.build.team != team)create(tile);
	}
	
	public static void createChance(Position pos, double chance){
		if(Mathf.chanceDelta(chance))UltFire.create(pos);
	}
	
	public static void createChance(float x, float y, float range, float chance, Team team){
		indexer.eachBlock(null, x, y, range, other -> other.team != team && Mathf.chanceDelta(chance), other -> UltFire.create(other.tile));
	}
	
	public static void createChance(Teamc teamc, float range, float chance){
		indexer.eachBlock(null, teamc.x(), teamc.y(), range, other -> other.team != teamc.team() && Mathf.chanceDelta(chance), other -> UltFire.create(other.tile));
	}
	
	public static void create(float x, float y, float range, Team team){
		indexer.eachBlock(null, x, y, range, other -> other.team != team, other -> UltFire.create(other.tile));
	}
	
	public static void create(float x, float y, float range){
		indexer.eachBlock(null, x, y, range, other -> true, other -> UltFire.create(other.tile));
	}
	
	public static void create(Teamc teamc, float range){
		indexer.eachBlock(null, teamc.x(), teamc.y(), range, other -> other.team != teamc.team(), other -> UltFire.create(other.tile));
	}
	
	public static void create(Position position){
		create(World.toTile(position.getX()), World.toTile(position.getY()));
	}
	
	public static void create(int x, int y){
		create(Vars.world.tile(x, y));
	}
	
	public static void create(Tile tile){
		if(net.client() || tile == null || !state.rules.fire) return; //not clientside.
		
		Fire fire = Fires.get(tile.x, tile.y);
		
		if(!(fire instanceof UltFire)){
			fire = UltFire.create();
			fire.tile = tile;
			fire.lifetime = baseLifetime;
			fire.set(tile.worldx(), tile.worldy());
			fire.add();
			Fires.register(fire);
		}else{
			fire.lifetime = baseLifetime;
			fire.time = 0f;
		}
	}
	
	public static void load(){
		for(int i = 0; i < 40; ++i) {
			ultRegion[i] = Core.atlas.find(NewHorizon.name("ult-fire-") + i);
		}
	}
	
	public void draw() {
		Draw.alpha(0.35f);
		Draw.alpha(Mathf.clamp(warmup / 20.0F));
		Draw.z(110.0F);
		Draw.rect(ultRegion[Math.min((int)animation, ultRegion.length - 1)], x + Mathf.randomSeedRange((long)((int)y), 2.0F), y + Mathf.randomSeedRange((long)((int)x), 2.0F));
		Draw.reset();
		Drawf.light(x, y, 50.0F + Mathf.absin(5.0F, 5.0F), NHColor.lightSkyBack, 0.6F * Mathf.clamp(warmup / 20.0F));
	}
	
	public void update() {
		animation += Time.delta / 2.25F;
		warmup += Time.delta;
		animation %= 40.0F;
		if (!Vars.headless) {
			Vars.control.sound.loop(Sounds.fire, this, 0.07F);
		}
		
		float speedMultiplier = 1.0F + Math.max(Vars.state.envAttrs.get(Attribute.water) * 10.0F, 0.0F);
		time = Mathf.clamp(time + Time.delta * speedMultiplier, 0.0F, lifetime);
		if (!Vars.net.client()) {
			if (!(time >= lifetime) && tile != null && !Float.isNaN(lifetime)) {
				Building entity = tile.build;
				boolean damage = entity != null;
				
				float flammability = puddleFlammability;
				if (!damage && flammability <= 0.0F) {
					time += Time.delta * 8.0F;
				}
				
				if (damage) {
					lifetime += Mathf.clamp(flammability / 16.0F, 0.1F, 0.5F) * Time.delta;
				}
				
				if (flammability > 1.0F && (spreadTimer += Time.delta * Mathf.clamp(flammability / 5.0F, 0.5F, 1.0F)) >= 22.0F) {
					spreadTimer = 0.0F;
					Point2 p = Geometry.d4[Mathf.random(3)];
					Tile other = Vars.world.tile(tile.x + p.x, tile.y + p.y);
					UltFire.create(other);
				}
				
				if (flammability > 0.0F && (fireballTimer += Time.delta * Mathf.clamp(flammability / 10.0F, 0.0F, 1.5F)) >= 40.0F) {
					fireballTimer = 0.0F;
					NHBullets.ultFireball.createNet(Team.derelict, x, y, Mathf.random(360.0F), 1.0F, 1.0F, 1.0F);
				}
				
				if ((damageTimer += Time.delta) >= 40.0F) {
					damageTimer = 0.0F;
					Puddlec p = Puddles.get(tile);
					puddleFlammability = p != null ? p.getFlammability() / 3.0F : 0.0F;
					if (damage) {
						entity.damage(10);
					}
					
					Damage.damageUnits(null, tile.worldx(), tile.worldy(), 8.0F, 10, (unit) -> !unit.isFlying() && !unit.isImmune(NHStatusEffects.ultFireBurn), (unit) -> {
						unit.apply(NHStatusEffects.ultFireBurn, 300.0F);
					});
				}
			} else {
				remove();
			}
		}
		
		if (Vars.net.client() && !isLocal() || isRemote()) {
			interpolate();
		}
		
		time = Math.min(time + Time.delta, lifetime);
		if (time >= lifetime) {
			remove();
		}
		
	}
	
	public static UltFire create() {
		return Pools.obtain(UltFire.class, UltFire::new);
	}
	
	@Override
	public int classId(){
		return EntityRegister.getID(getClass());
	}
	
	@Override
	public void remove() {
		if (added) {
			Groups.all.remove(this);
			Groups.sync.remove(this);
			Groups.draw.remove(this);
			Groups.fire.remove(this);
			removeEffect();
			
			if (Vars.net.client()) {
				Vars.netClient.addRemovedEntity(id());
			}
			
			added = false;
			Groups.queueFree(this);
			Fires.remove(tile);
		}
	}
	
	public void removeEffect(){
		remove.at(x, y, animation);
	}
}
