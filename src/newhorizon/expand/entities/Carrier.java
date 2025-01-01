package newhorizon.expand.entities;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.Scaled;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.io.TypeIO;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.NHGroups;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.*;

public class Carrier extends NHBaseEntity implements Teamc, Rotc, Scaled{
	protected static final float dstPerMove = tilesize * 1.5f;
	
	public float rotation = 0;
	public float finalRot = 0;
	public Unit unit;
	public UnitPayload toCarry;
	public Team team;
	public Vec2 to;
	
	public transient long lastUpdated, updateSpacing;
	
	public transient float x_LAST_, x_TARGET_, y_LAST_, y_TARGET_;
	
	public transient Vec2 vel = new Vec2();
	
	public Trail trail = new Trail(1);
	
	public boolean dumped = false, onMove = false, contained, adjusted, intercepted = false, complete = false;
	protected float time = 0, lifetime = 540f, surviveTime = 0, surviveLifetime = 6000;
	
	public transient boolean onGoing = true;

	public static Carrier create(Unit unit, Vec2 to, float rot){
		Carrier c = Pools.obtain(Carrier.class, Carrier::new);
		c.init(unit, to, rot);
		c.set(unit);
		c.add();
		
		return c;
	}
	
	public static Carrier create(Unit unit, Vec2 to){
		Carrier c = Pools.obtain(Carrier.class, Carrier::new);
		c.init(unit, to, unit.angleTo(to));
		c.set(unit);
		c.add();
		
		return c;
	}
	
	public void init(Unit unit, Vec2 to, float rotation){
		finalRot = rotation;
		drawSize = unit.hitSize * 2f;
		this.unit = unit;
		this.to = to;
		team(unit.team());
		contained = toCarry != null;
		trail = new Trail(60);
	}
	
	@Override
	public void add(){
		super.add();
		NHSounds.hyperspace.at(this);
	}
	
	@Override
	public void draw(){
		Draw.z(Layer.effect);
		if((!complete && time > lifetime / 2) || onMove || (contained && time < lifetime / 2) && onGoing){
			trail.draw(team.color, 4f);
		}
		
		if(!onMove && team != null) Drawf.light(this, drawSize * fslope(), team.color, 0.8f);
		if(!onMove && unit != null && !unit.isNull()){
			float height = Mathf.curve(fslope() * fslope(), 0f, 0.3f) * 1.1f;
			float width = Mathf.curve(fslope() * fslope(), 0.35f, 0.75f) * 1.1f;
			
			if((contained && !Units.canCreate(team, unit.type)) || (surviveTime > 0)){
				Draw.z(Layer.bullet - 0.2f);
				Draw.color(team.color.cpy().mul(1.15f), Pal.gray, new Rand(id).random(-0.25f, 0.25f) / 4f);
				Draw.alpha(0.2f);
				Fill.rect(x, y, Draw.scl * unit.type.shadowRegion.height * width + 1f, Draw.scl * unit.type.shadowRegion.width * height, rotation);
				
				Draw.color(Pal.ammo);
				Draw.z(Layer.bullet - 0.1f);
				float size = this.drawSize / 3;
				float sin = Mathf.absin(Time.time * DrawFunc.sinScl, 8f, 2f);
				float length = size / 1.5f + sin;
				
				Draw.rect(Icon.warning.getRegion(), x, y, size / 1.5f, size / 1.5f);
				
				Draw.alpha(surviveTime / surviveLifetime);
				for(int i = 0; i < 4; i++){
					
					Tmp.v1.trns(i * 90, -length);
					Draw.rect(NHContent.pointerRegion, x + Tmp.v1.x, y + Tmp.v1.y, size, size, i * 90 - 90f);
				}
				
				Draw.reset();
			}else{
				Draw.z(Layer.effect);
				Draw.color(team.color.cpy().mul(1.15f), Pal.gray, new Rand(id).random(-0.25f, 0.25f) / 4f);
				Fill.rect(x, y, Draw.scl * unit.type.shadowRegion.height * width + 1f, Draw.scl * unit.type.shadowRegion.width * height, rotation);
			}
		}
	}
	
	protected void pickup(){
		unit.remove();
		toCarry = new UnitPayload(unit);
		Fx.unitPickup.at(unit);
		if(Vars.net.client()){
			Vars.netClient.clearRemovedEntity(unit.id);
		}
	}
	
	protected boolean drop(){
		toCarry.set(x, y, rotation);
		if(intercepted) toCarry.unit.apply(NHStatusEffects.intercepted, 480f);
		return toCarry.dump();
	}
	
	@Override
	public void update(){
		onGoing = true;
		
		if(!contained && unit != null && unit.isValid() && !complete){
			set(unit);
			rotation(unit.rotation);
		}
		
		if(time > lifetime / 2 && !adjusted){
			if(!contained && unit != null && unit.isValid()){
				contained = true;
				adjusted = true;
				pickup();
			}else if(toCarry != null && !dumped){
				onGoing = dumped = drop();
				contained = !dumped;
				if(surviveTime > surviveLifetime){
					if(!net.client()){
						toCarry.set(x, y, rotation);
						toCarry.unit.add();
					}
					dumped = true;
					contained = false;
				}
			}
		}
		
		if(dumped){
			complete = true;
			adjusted = true;
		}
		
		if(time > lifetime){
			if(!contained) remove();
			else if(!onMove) onMove = true;
		}
		
		
		if(!onMove && onGoing) time += Time.delta;
		else if(!onMove) surviveTime += Time.delta;
		
		if(onMove && contained && (intercepted || (Mathf.equal(x, to.getX(), 12f) && Mathf.equal(y, to.getY(), 12f)))){
			if(!intercepted) set(to);
			time = 0;
			onMove = false;
			adjusted = false;
			rotation = finalRot;
			NHSounds.hyperspace.at(this);
		}
		
		if(onMove && contained){
			if(!headless) trail.update(x, y);
			
			vel.set(to).sub(x, y).nor().scl(dstPerMove * Time.delta);
			
			x += vel.x;
			y += vel.y;
			
			NHGroups.gravityTraps.intersect(x - drawSize / 2f, y - drawSize / 2f, drawSize, drawSize, b -> {
				if(b.team() != team && b.active()){
					intercepted = true;
					toCarry.unit.damage(toCarry.unit.health * 0.3f);
					PosLightning.createEffect(b, this, b.team().color, 2, PosLightning.WIDTH);
					NHFx.square45_4_45.at(x, y, team.color);
				}
			});
		}
		
		if(!Vars.headless)trail.update(x, y, 1);
	}
	
	@Override
	public int classId(){
		return EntityRegister.getID(getClass());
	}
	
	@Override
	public void write(Writes write){
		super.write(write);
		write.bool(contained);
		write.bool(complete);
		TypeIO.writeVec2(write, to);
		write.f(time);
		write.f(surviveTime);
		write.f(rotation);
		write.f(finalRot);
		write.bool(onMove);
		write.bool(adjusted);
		write.bool(dumped);
		
		if(contained) TypeIO.writePayload(write, toCarry);
	}
	
	@Override
	public void read(Reads read){
		super.read(read);
		contained = read.bool();
		complete = read.bool();
		to = TypeIO.readVec2(read);
		time = read.f();
		surviveTime = read.f();
		rotation = read.f();
		finalRot = read.f();
		onMove = read.bool();
		adjusted = read.bool();
		dumped = read.bool();
		
		if(contained) toCarry = (UnitPayload)TypeIO.readPayload(read);
		else remove();
		
		afterRead();
	}
	
	@Override
	public void afterRead(){
		if(contained && toCarry != null && toCarry.unit != null && !toCarry.unit.isNull()){
			init(toCarry.unit, to, finalRot);
			add();
		}else{
			remove();
		}
	}
	
	public boolean inFogTo(Team viewer) {
		return false;
//		if (this.team != viewer && Vars.state.rules.fog) {
//			if (this.size <= 16.0F) {
//				return !Vars.fogControl.isVisible(viewer, this.x, this.y);
//			} else {
//				float trns = this.size / 2.0F;
//
//				for(Point2 p : Geometry.d8){
//					if(Vars.fogControl.isVisible(viewer, this.x + (float)p.x * trns, this.y + (float)p.y * trns)){
//						return false;
//					}
//				}
//
//				return true;
//			}
//		} else {
//			return false;
//		}
	}
	
	@Override
	public boolean cheating(){
		return toCarry != null && toCarry.unit.cheating();
	}
	
	@Override
	public CoreBlock.CoreBuild core(){
		return team.core();
	}
	
	@Override
	public CoreBlock.CoreBuild closestCore(){
		return state.teams.closestCore(x, y, team);
	}
	
	@Override
	public CoreBlock.CoreBuild closestEnemyCore(){
		return state.teams.closestEnemyCore(x, y, team);
	}
	
	@Override
	public Team team(){
		return team;
	}
	
	@Override
	public void team(Team team){
		this.team = team;
	}
	
	@Override
	public float rotation(){
		return rotation;
	}
	
	@Override
	public void rotation(float rotation){
		this.rotation = rotation;
	}
	
	@Override
	public float fin(){
		return Math.min(time, lifetime) / lifetime;
	}
	
	@Override
	public String toString(){
		return "Carrier{" + "rotation=" + rotation + ", unit=" + unit + ", toCarry=" + toCarry + ", team=" + team + ", to=" + to + ", intercepted=" + intercepted + ", complete=" + complete + ", onMove=" + onMove + ", contained=" + contained + ", adjusted=" + adjusted + ", time=" + time + ", lifetime=" + lifetime + '}';
	}
	
	@Override
	public Building buildOn(){
		return null;
	}
}
