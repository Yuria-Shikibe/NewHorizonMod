package newhorizon.expand.weather;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Prov;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.Team;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.gen.WeatherState;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.type.Weather;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.Attributes;
import mindustry.world.meta.Stat;
import newhorizon.NewHorizon;
import newhorizon.util.feature.cutscene.events.FleetEvent;
import newhorizon.util.feature.cutscene.events.RaidEvent;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;
import newhorizon.util.func.OV_Pair;
import newhorizon.util.ui.IconNumDisplay;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventGenerator extends Weather{
	public AutoEventTrigger trigger = new AutoEventTrigger();
	public Prov<Team> team = () -> Vars.state.rules.defaultTeam;
	public Boolf<GameState> whenTo = s -> true;
	
	public int beginWave = 8;
	
	public boolean remain = false;
	
	public EventGenerator(String name, Prov<Team> team, AutoEventTrigger trigger){
		this(name);
		this.trigger = trigger;
		this.team = team;
	}
	
	public EventGenerator(String name, Prov<Team> team){
		this(name);
		this.team = team;
	}
	
	@Override
	public void init(){
		if(!Core.bundle.has(getContentType() + "." + this.name + ".name")){
			localizedName = "[gray]" + trigger.eventType.type() + "|[lightgray]" + id;
		}
	}
	
	public EventGenerator(String name){
		super(name);
		
		statusAir = statusGround = false;
		duration = 2f * Time.toMinutes;
		
		alwaysUnlocked = true;
		
		details = Core.bundle.get("nh.help-event-generator");
		description = Core.bundle.get("nh.help-event");
		
		attrs = new Attributes();
	}
	
	@Override
	public boolean isHidden(){
		return false;
	}
	
	@Override
	public void load(){
		if(trigger.eventType instanceof RaidEvent)fullIcon = uiIcon = Core.atlas.find(NewHorizon.name("raid"));
		else if(trigger.eventType instanceof FleetEvent)fullIcon = uiIcon = Core.atlas.find(NewHorizon.name("fleet"));
		else fullIcon = uiIcon = Core.atlas.find(NewHorizon.name("objective"));
	}
	
	@Override
	public void setStats(){
		super.setStats();
		
		stats.add(Stat.input, t -> t.table(Tex.clear, table -> {
			table.margin(OFFSET * 2f);
			
			Cons<Table> none = i -> i.add(Core.bundle.get("none") + "  " + Iconc.cancel, Styles.outlineLabel).color(Pal.redderDust).left();
			
			table.table(i -> {
				i.add("@mod.ui.event-trigger-requirements").color(Color.gray).center().row();
				i.image().height(OFFSET / 4).color(Color.gray).growX().row();
			}).growX().fillY().row();
			
			table.table(i -> {
				i.add(Core.bundle.format("mod.ui.wait-waves", beginWave)).row();
				i.image().height(OFFSET / 4).color(Color.gray).growX().row();
			}).growX().fillY().padTop(OFFSET).row();
			
			table.table().padTop(OFFSET).row();
			table.add("@category.items").left().padLeft(OFFSET * 1.5f).color(Color.lightGray).row();
			table.pane(cons -> {
				cons.left();
				if(trigger.items.length == 0)none.get(cons);
				else for(OV_Pair<Item> pair : trigger.items)cons.table(i -> i.add(new IconNumDisplay(pair.item.fullIcon, pair.value, pair.item.localizedName))).fill().row();
			}).fillY().growX().padLeft(LEN).row();
			
			table.table().padTop(OFFSET).row();
			table.add("@rules.title.unit").left().padLeft(OFFSET * 1.5f).color(Color.lightGray).row();
			table.pane(cons -> {
				cons.left();
				if(trigger.units.length == 0)none.get(cons);
				else for(OV_Pair<UnitType> pair : trigger.units)cons.table(i -> i.add(new IconNumDisplay(pair.item.fullIcon, pair.value, pair.item.localizedName))).fill().row();
			}).fillY().growX().padLeft(LEN).row();
			
			table.table().padTop(OFFSET).row();
			table.add("@filter.option.block").left().padLeft(OFFSET * 1.5f).color(Color.lightGray).row();
			table.pane(cons -> {
				cons.left();
				if(trigger.buildings.length == 0)none.get(cons);
				else for(OV_Pair<Block> pair : trigger.buildings)cons.table(i -> i.add(new IconNumDisplay(pair.item.fullIcon, pair.value, pair.item.localizedName))).fill().row();
			}).fillY().growX().padLeft(LEN).row();
		}).fill());
		stats.add(Stat.abilities, t -> {
			t.table().padLeft(LEN + OFFSET);
			trigger.eventType.display(t);
		});
	}
	
	@Override
	public WeatherState create(float intensity, float duration){
		if(!Vars.net.client() && (!Vars.state.rules.waves || beginWave <= Vars.state.wave) && whenTo.get(Vars.state)){
			if(trigger.meet(team.get())) trigger.eventType.setup();
			else if(remain){
				AutoEventTrigger t = trigger.copy();
				t.team = team.get();
				t.add();
			}
		}
		
		WeatherState state = type.get();
		state.init(this);
		state.intensity(5);
		state.life(5);
		
		return state;
	}
	
	@Override
	public void drawOver(WeatherState state){
	
	}
	
	@Override
	public void drawUnder(WeatherState state){
	
	}
	
	public static class Starter extends WeatherState{
		public static Starter create(){
			return Pools.obtain(Starter.class, Starter::new);
		}
	}
}
