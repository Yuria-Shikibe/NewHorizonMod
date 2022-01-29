package newhorizon.util.feature.cutscene.events.util;

import arc.func.Cons;
import arc.util.Interval;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.io.TypeIO;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.expand.entities.NHGroups;
import newhorizon.util.annotation.ClientDisabled;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.func.EntityRegister;
import newhorizon.util.func.OV_Pair;

@ClientDisabled
public class AutoEventTrigger implements Entityc, Cloneable{
	public boolean added;
	public transient int id = EntityGroup.nextId();
	
	public Team team;
	public OV_Pair<Item>[] items = new OV_Pair[0];
	public OV_Pair<UnitType>[] units = new OV_Pair[0];
	public OV_Pair<Block>[] buildings = new OV_Pair[0];
	
	public CutsceneEvent eventType;
	
	protected final Interval timer = new Interval();
	public float checkSpacing = 180f;
	
	public AutoEventTrigger(){
		team = Team.derelict;
		eventType = CutsceneEvent.NULL_EVENT;
	}
	
	public AutoEventTrigger setEvent(CutsceneEvent event){
		eventType = event;
		return this;
	}
	
	
	public AutoEventTrigger modify(Cons<AutoEventTrigger> modifier){
		modifier.get(this);
		return this;
	}
	
	public AutoEventTrigger(Team team, CutsceneEvent eventType){
		this.team = team;
		this.eventType = eventType;
	}
	
	public AutoEventTrigger(Team team, CutsceneEvent eventType, OV_Pair<Item>[] items, OV_Pair<UnitType>[] units, OV_Pair<Block>[] buildings){
		this.team = team;
		this.items = items;
		this.units = units;
		this.buildings = buildings;
		this.eventType = eventType;
	}
	
	public AutoEventTrigger(Team team, CutsceneEvent eventType, Cons<AutoEventTrigger> modifier){
		this.team = team;
		this.eventType = eventType;
		modifier.get(this);
	}
	
	public boolean meet(Team team){
		Teams.TeamData data = Vars.state.teams.get(team);
		if(!Vars.state.teams.isActive(team) || data.noCores())return false;
		
		CoreBlock.CoreBuild core = team.core();
		for(OV_Pair<Item> stack : items)if(stack.value > core.items().get(stack.item))return false;
		
		for(OV_Pair<UnitType> pair : units)if(pair.value > data.countType(pair.item))return false;
		
		for(OV_Pair<Block> pair : buildings)if(pair.value > Groups.build.count(b -> b.team == team && b.block == pair.item))return false;
		
		return true;
	}
	
	public boolean meet(){
		return meet(team);
	}
	
	public void check(){
		if(!Vars.net.client() && timer.get(checkSpacing)){
			if(team.cores().isEmpty()){
				remove();
				return;
			}
			if(meet()){
				eventType.setup();
				remove();
			}
		}
	}
	
	@Override public void read(Reads read){
		team = TypeIO.readTeam(read);
		eventType = CutsceneEvent.readEvent(read);
		items = OV_Pair.readArr(read);
		units = OV_Pair.readArr(read);
		buildings = OV_Pair.readArr(read);
	}
	
	@Override public void write(Writes write){
		TypeIO.writeTeam(write, team);
		CutsceneEvent.writeEvent(eventType, write);
		OV_Pair.writeArr(items, write);
		OV_Pair.writeArr(units, write);
		OV_Pair.writeArr(buildings, write);
	}
	
	public void remove(){
		Groups.all.remove(this);
		NHGroups.autoEventTriggers.remove(this);
		
		added = false;
	}
	
	@Override
	@ClientDisabled
	public void add(){
		if(!Vars.net.client() && !added){
			Groups.all.add(this);
			NHGroups.autoEventTriggers.add(this);
			
			added = false;
		}
	}
	
	@Override public boolean isLocal(){
		return false;
	}
	@Override public boolean isRemote(){
		return true;
	}
	@Override public boolean isNull(){ return false; }
	@Override public <T extends Entityc> T self(){ return (T)this; }
	@Override public <T> T as(){ return (T)this; }

	@Override public boolean isAdded(){ return added; }
	
	@Override
	public void update(){
		check();
	}
	
	@Override public boolean serialize(){ return true; }
	
	@Override public int classId(){return EntityRegister.getID(getClass());}
	@Override public void afterRead(){ }
	@Override public int id(){return id; }
	@Override public void id(int id){ this.id = id; }
	
	@Override
	public String toString(){
		return "AutoEventTrigger{" + "id=" + id + ", event=" + eventType + '}';
	}
	
	public AutoEventTrigger copy(){
		try{
			AutoEventTrigger clone = (AutoEventTrigger)super.clone();
			
			clone.added = false;
			clone.id = EntityGroup.nextId();
			
			return clone;
		}catch(CloneNotSupportedException ignored){return null;}
	}
	
	public String desc(){
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("[accent]");
		stringBuilder.append(eventType.type());
		stringBuilder.append('\n');
		
		if(items.length == 0 && units.length == 0 && buildings.length == 0){
			stringBuilder.append("No Requirements");
		}else{
			stringBuilder.append("Requirements:\n");
			
			if(items.length != 0)stringBuilder.append("[gray]<Items>\n");
			
			for(OV_Pair<Item> item : items){
				stringBuilder.append("    [lightgray]|  ");
				stringBuilder.append(item.item.localizedName);
				stringBuilder.append(" *[accent]");
				stringBuilder.append(item.value);
				stringBuilder.append('\n');
			}
			
			if(units.length != 0)stringBuilder.append("[gray]<Units>\n");
			
			for(OV_Pair<UnitType> unit : units){
				stringBuilder.append("    [lightgray]|  ");
				stringBuilder.append(unit.item.localizedName);
				stringBuilder.append(" *[accent]");
				stringBuilder.append(unit.value);
				stringBuilder.append('\n');
			}
			
			if(buildings.length != 0)stringBuilder.append("[gray]<Blocks>:\n");
			
			for(OV_Pair<Block> block : buildings){
				stringBuilder.append("    [lightgray]|  ");
				stringBuilder.append(block.item.localizedName);
				stringBuilder.append(" *[accent]");
				stringBuilder.append(block.value);
				stringBuilder.append('\n');
			}
		}
		
		return stringBuilder.toString();
	}
	
}
