package newhorizon.expand.cutscene.actions;

import arc.func.Prov;
import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.game.Team;
import newhorizon.expand.cutscene.NHCSS_Action;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.types.WorldEventType;
import newhorizon.util.annotation.ClientDisabled;

public class EventSetupAction extends NHCSS_Action implements NHCSS_Action.ImportantAction{
	public WorldEventType toSetup = WorldEventType.NULL;
	public Prov<Team> setupTeam = () -> Vars.state.rules.waveTeam;
	public Vec2 setupCoords = Vec2.ZERO;
	
	public EventSetupAction init(Vec2 setupCoords, Prov<Team> setupTeam){
		this.setupTeam = setupTeam;
		this.setupCoords = setupCoords;
		
		return this;
	}
	
	public EventSetupAction(ActionBus bus, WorldEventType toSetup, Prov<Team> setupTeam, Vec2 setupCoords){
		super(bus);
		this.toSetup = toSetup;
		this.setupTeam = setupTeam;
		this.setupCoords = setupCoords;
	}
	
	public EventSetupAction(ActionBus bus, WorldEventType toSetup){
		super(bus);
		this.toSetup = toSetup;
	}
	
	public EventSetupAction(ActionBus bus){
		super(bus);
	}
	
	@ClientDisabled
	@Override
	public void act(){
		super.act();
		
		if(!Vars.net.client()){
			WorldEvent worldEvent = toSetup.create();
			worldEvent.team(setupTeam.get());
			worldEvent.set(setupCoords);
		}
	}
}
