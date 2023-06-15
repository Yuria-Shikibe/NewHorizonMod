package newhorizon.expand.cutscene.actions;

import arc.func.Cons;
import arc.func.Prov;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import newhorizon.expand.cutscene.NHCSS_Action;
import newhorizon.util.annotation.ClientDisabled;
import newhorizon.util.func.NHFunc;

public class InboundAction extends NHCSS_Action implements NHCSS_Action.ImportantAction{
	public UnitType type = UnitTypes.alpha;
	public Prov<Team> teamProv = () -> Vars.state.rules.waveTeam;
	public Vec2 spawnPosition = Vec2.ZERO;
	
	public int spawnNum = 1;
	public float spawnRange = 4;
	public float spawnEachDelay = 15f;
	
	public float spawnAngle = 45;
	public StatusEffect status = StatusEffects.none;
	public float statusDuration = 600f;
	
	public float spawnDelay = 60f;
	
	public double flag;
	
	public InboundAction setPosition(Position position){
		spawnPosition.set(position);
		return this;
	}
	
	public InboundAction setPosition(float x, float y){
		spawnPosition.set(x, y);
		return this;
	}
	
	public InboundAction setFlag(long bits){
		flag = Double.longBitsToDouble(bits);
		return this;
	}
	
	public InboundAction modification(Cons<InboundAction> action){
		action.get(this);
		return this;
	}
	
	public InboundAction(ActionBus bus){
		super(bus);
	}
	
	@Override
	public void setup(){
		super.setup();
		
//		spawnPosition.set(Core.camera.position);
	}
	
	@ClientDisabled
	@Override
	public void act(){
		super.act();
		
		NHFunc.spawnUnit(teamProv.get(), spawnPosition.x, spawnPosition.y, spawnAngle, spawnRange, spawnDelay, spawnEachDelay, type, spawnNum, status, statusDuration, flag);
	}
}
