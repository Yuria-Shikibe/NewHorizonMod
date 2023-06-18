package newhorizon.expand.units.ablility;

import arc.func.Cons;
import arc.graphics.Color;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import newhorizon.content.NHFx;

public class AdaptedHealAbility extends RepairFieldAbility{
	public AdaptedHealAbility(float amount, float reload, float range, Color applyColor){
		this(amount, reload, range);
		this.applyColor = applyColor;
	}
	
	public AdaptedHealAbility(float amount, float reload, float range){
		super(amount, reload, range);
		
		healEffect = NHFx.healReceiveCircle;
		activeEffect = NHFx.healSendCircle;
	}
	
	public AdaptedHealAbility modify(Cons<AdaptedHealAbility> modifier){
		modifier.get(this);
		return this;
	}
	
	public Color applyColor = Pal.heal;
	
	public boolean ignoreHealthMultiplier = true;
	//Percent per tick
	public float selfHealAmount = 0.0005f;
	public float selfHealReloadTime = -1;
	
	protected float lastHealth = 0;
	protected float selfHealReload = 0;
	
	public void update(Unit unit) {
		timer += Time.delta;
		
		if(timer >= reload){
			wasHealed = false;
			
			Units.nearby(unit.team, unit.x, unit.y, range, other -> {
				if(other.damaged()){
					healEffect.at(other.x, other.y, 0, applyColor, parentizeEffects ? other : null);
					wasHealed = true;
				}
				other.heal(amount);
			});
			
			if(wasHealed){
				activeEffect.at(unit.x, unit.y, range, applyColor);
			}
			
			timer = 0f;
		}
		
		if(selfHealReloadTime < 0)return;
		
		if(lastHealth <= unit.health && unit.damaged()){
			selfHealReload += Time.delta;
			
			if(selfHealReload > selfHealReloadTime){
				unit.healFract(selfHealAmount * (ignoreHealthMultiplier ? 1 : 1 / unit.healthMultiplier));
			}
		}else{
			selfHealReload = 0;
		}
		
		lastHealth = unit.health;
	}
}
