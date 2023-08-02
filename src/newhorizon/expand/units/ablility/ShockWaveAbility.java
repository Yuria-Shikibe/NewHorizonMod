package newhorizon.expand.units.ablility;

import arc.audio.Sound;
import arc.func.Cons;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.util.feature.PosLightning;

public class ShockWaveAbility extends Ability{
	protected static final Seq<Unit> all = new Seq<>();
	
	public ObjectFloatMap<StatusEffect> status = new ObjectFloatMap<>();
	
	public boolean targetGround = true, targetAir = true;
	public float x, y;
	
	public float reload = 500f;
	public float range = 400f;
	public float damage = 400f;
	
	public float knockback = 20f;
	public float rotKnock = 10f;
	
	public Color hitColor = NHColor.ancientLightMid;
	
	public Sound shootSound = NHSounds.shock;
	
	public Effect shootEffect = NHFx.circleOut;
	public Effect hitEffect = NHFx.hitSparkLarge;
	
	public float maxSpeed = -1;
	
	public int boltNum = 2;
	public float boltWidth = 2;
	
	public ShockWaveAbility(float reload, float range, float damage, Color hitColor){
		this.reload = reload;
		this.range = range;
		this.damage = damage;
		this.hitColor = hitColor;
	}
	
	public Cons2<Position, Position> effect = (from, to) -> {
		PosLightning.createEffect(from, to, hitColor, boltNum, boltWidth);
	};
	
	public ShockWaveAbility modify(Cons<ShockWaveAbility> m){
		m.get(this);
		
		return this;
	}
	
	public ShockWaveAbility status(Object... values){
		for(int i = 0; i < values.length / 2; i++){
			status.put((StatusEffect)values[i * 2], (Float)values[i * 2 + 1]);
		}
		
		return this;
	}
	
	@Override
	public void init(UnitType type){
		super.init(type);
		if(maxSpeed > 0)maxSpeed = maxSpeed * maxSpeed;
	}
	
	protected float timer = 0;
	
	@Override
	public void update(Unit unit){
		if(unit.disarmed)return;
		
		timer += Time.delta * unit.reloadMultiplier;
		
		if(maxSpeed > 0 && unit.vel().len2() > maxSpeed){
			timer = 0;
		}else if(timer > reload){
			all.clear();
			
			Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
			float rx = Tmp.v1.x, ry = Tmp.v1.y;
			
			Units.nearby(null, rx, ry, range, other -> {
				if(other.team != unit.team && other.checkTarget(targetAir, targetGround) && other.targetable(unit.team)){
					all.add(other);
				}
			});
			
			if(all.any()){
				timer = 0;
				shootSound.at(rx, ry, 1 + Mathf.range(0.15f), 3);
				
				shootEffect.at(rx, ry, range, hitColor);
				for(Unit u : all){
					for(ObjectFloatMap.Entry<StatusEffect> s : status.entries()){
						u.apply(s.key, s.value);
					}
					
					Tmp.v3.set(unit).sub(Tmp.v1).nor().scl(knockback * 80f);
					u.impulse(Tmp.v3);
					u.damage(damage);
					hitEffect.at(u.x, u.y, hitColor);
					effect.get(Tmp.v1, u);
				}
			}
		}
	}
	
	@Override
	public void draw(Unit unit){
		super.draw(unit);
	}
	
	@Override
	public String localized(){
		return super.localized();
	}
}
