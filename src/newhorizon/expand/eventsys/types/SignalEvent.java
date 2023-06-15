package newhorizon.expand.eventsys.types;

import arc.func.Boolf;
import arc.func.Prov;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import newhorizon.expand.cutscene.NHCSS_UI;
import newhorizon.expand.cutscene.stateoverride.WorldOverride;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.annotation.HeadlessDisabled;

@HeadlessDisabled
public class SignalEvent extends WorldEventType{
	public static Boolf<WorldEvent> buildingInFOV(Team t, int x, int y){
		Team team = t == null ? Vars.state.rules.defaultTeam : t;
		
		return (e) -> {
			Building building = Vars.world.build(x, y);
			if(building == null || building.team == team)return true;
			if(Vars.fogControl.isVisible(team, x * 8, y * 8))return false;
			return false;
		};
	}
	
	public static Boolf<WorldEvent> buildingInFOV(){
		Team team = Vars.state.rules.defaultTeam;
		
		return (e) -> {
			Building building = Vars.world.build(World.toTile(e.x), World.toTile(e.y));
			if(building == null || building.team == team)return true;
			return WorldOverride.visible(team, building);
		};
	}
	
	public SignalEvent(String name){
		super(name);
		
		hasCoord = true;
		drawable = warnOnHUD = minimapMarkable = removeAfterTrigger = false;
	}
	
	public Prov<CharSequence> info = () -> "Signal Detected";
	public Boolf<WorldEvent> removeCondition = buildingInFOV();
	public Color markColor = Color.lightGray;
	public float maxDst = 1200;
	
	@Override
	public String coordText(WorldEvent event){
		return "";
	}
	
	@Override
	public void updateEvent(WorldEvent event){
		super.updateEvent(event);
		
		if(Vars.headless)return;
		if(event.timer.get(180f)){
			NHCSS_UI.forceMarkSignal(event.x, event.y, maxDst, markColor);
		}
		
		if(removeCondition.get(event))event.remove();
	}
	
	@Override
	public Table buildSimpleTable(WorldEvent e){
		return new Table(t -> {
			t.margin(6f);
			t.add(new Bar(info, () -> markColor, () -> progressRatio(e))).height(35f).padLeft(5f).padTop(5f).growX().row();
		});
	}
	
	@Override
	public float progressRatio(WorldEvent event){
		return Vars.player == null ? -1 : 1 - Vars.player.dst(event.x, event.y) / maxDst;
	}
}
