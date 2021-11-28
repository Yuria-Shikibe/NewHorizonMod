package newhorizon.expand.vars;

import arc.func.Intf;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import arc.util.async.Threads;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Building;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.production.GenericCrafter;

public class TileSortMap{
	public static final int
		HEALTH = 0,
		INDUSTRY = 1,
		DEFENCE = 2;
	
	public static ObjectMap<Team, IntMap<Integer[]>> sortMap;
	
	public static int lastUpdatedIndex = 0;
	public static boolean complete = false;
	
	public static void init(){
		Seq<Teams.TeamData> active = Vars.state.teams.getActive();
		
		sortMap = new ObjectMap<>(active.size);
		
		int size = Vars.world.tiles.width * Vars.world.tiles.height;
		
		active.each(t -> sortMap.put(t.team, new IntMap<>(size)));
		
		sortMap.each((t, im) -> {
			for(int i = 0; i < size; i++){
				im.put(i, new Integer[SortTarget.all.length]);
			}
		});
	}
	
	public static void startNewSoftUpdate(){
		complete = false;
		lastUpdatedIndex = 0;
	}
	
	public static void forceEndSoftUpdate(){
		complete = true;
		lastUpdatedIndex = size();
	}
	
	public static void softUpdateAll(){
		complete = false;
		lastUpdatedIndex = 0;
		
		softUpdate();
		softUpdate();
		softUpdate();
	}
	
	public static void continueUpdateAll(){
		softUpdate();
		softUpdate();
		softUpdate();
	}
	
	public static void softUpdateSingle(){
		Thread thread = Threads.thread(() -> {
			int index = lastUpdatedIndex++;
			
			if(index >= size()){
				complete = true;
				return;
			}
			
			sortMap.each((t, im) -> {
				Building building = Vars.world.tiles.geti(index).build;
				if(building == null)return;
				
				Integer[] integers = im.get(index);
				
				for(int j = 0; j < SortTarget.all.length; j++){
					integers[j] = SortTarget.all[j].getValue.get(building);
				}
			});
			
			Log.info( Thread.currentThread().getId() + "|" + lastUpdatedIndex + "|" + complete);
		});
	}
	
	public static void softUpdate(){
		Thread thread = Threads.daemon("MapUpdater", () -> {
			
			int index = 0;
			
			while(index < size() && !complete){
				index = lastUpdatedIndex++;
				
				if(index >= size()){
					Log.info("Break");
					break;
				}
				
				int finalIndex = index;
				sortMap.each((t, im) -> {
					Building building = Vars.world.tiles.geti(finalIndex).build;
					if(building == null)return;
					
					Integer[] integers = im.get(finalIndex);
					
					for(int j = 0; j < SortTarget.all.length; j++){
						integers[j] = SortTarget.all[j].getValue.get(building);
					}
				});
				
				Log.info( Thread.currentThread().getId() + "|" + lastUpdatedIndex + "|" + complete);
			}
			
			complete = true;
			Log.info(Thread.currentThread().getId() + "|" + lastUpdatedIndex + "|Complete" );
		});
	}
	
	public static void updateAll(){
//		Seq<Teams.TeamData> removed = Vars.state.teams.active.removeAll(t -> !t.active());
//		Seq<Teams.TeamData> active = Vars.state.teams.active;
		
//		removed.each(k -> sortMap.remove(k.team));
		
		int size = size();
		
		sortMap.each((t, im) -> {
//			Log.info(t + "|" + size);
			for(int i = 0; i < size; i++){
				Building building = Vars.world.tiles.geti(i).build;
				if(building == null)continue;
				
				Integer[] integers = im.get(i);
				
				for(int j = 0; j < SortTarget.all.length; j++){
					integers[j] = SortTarget.all[j].getValue.get(building);
//					Log.info(integers[j]);
				}
			}
		});
	}
	
	public static int size(){return Vars.world.tiles.width * Vars.world.tiles.height;}
	
	public static int x(int index){
		return Vars.world.tiles.geti(index).x;
	}
	
	public static int y(int index){
		return Vars.world.tiles.geti(index).y;
	}
	
	public static void update(SortTarget sort, Team team){
		if(!sortMap.containsKey(team))return;
		
		IntMap<Integer[]> map = sortMap.get(team);
		int size = size();
		
		for(int i = 0; i < size; i++){
			Building building = Vars.world.tiles.geti(i).build;
			if(building == null)continue;
			
			Integer[] integers = map.get(i);
			
			integers[sort.ordinal()] = sort.getValue.get(building);
		}
	}
	
	public static void update(SortTarget sort){
		Seq<Teams.TeamData> removed = Vars.state.teams.active.removeAll(t -> !t.active());;
		Seq<Teams.TeamData> active = Vars.state.teams.getActive();
		
		removed.each(k -> sortMap.remove(k.team));
		
		int size = size();
		
		sortMap.each((t, map) -> {
			for(int i = 0; i < size; i++){
				Building building = Vars.world.tiles.geti(i).build;
				if(building == null)continue;
				
				Integer[] integers = map.get(i);
				
				integers[sort.ordinal()] = sort.getValue.get(building);
			}
		});
	}
	
	public static int getMax(SortTarget sort, Team team){
		if(!sortMap.containsKey(team))return -1;
		
		//     1. Get key seq                      | 2. Sort                                 | 3. Get Max
		return sortMap.get(team).values().toArray().sortComparing(arr -> arr[sort.ordinal()]).first()[sort.ordinal()];
	}
	
	public static Pixmap asPixmap(SortTarget sort, Team team){
		Pixmap pixmap = new Pixmap(Vars.world.tiles.width, Vars.world.tiles.height);
		
		if(!sortMap.containsKey(team))return pixmap;
		
		Tmp.c1.set(team.color);
		IntMap<Integer[]> map = sortMap.get(team);
		int size = size();
		int max = getMax(sort, team);
		
		for(int i = 0; i < size; i++){
			int v = map.get(i)[sort.ordinal()];
			Tmp.c1.a(v / (float)max);
			pixmap.set(x(i), y(i), Tmp.c1);
		}
		
		return pixmap;
	}
	
	public static void show(SortTarget sort, Team team){
		new BaseDialog("SHOW"){{
			addCloseButton();
			
			cont.image(new TextureRegion(new Texture(asPixmap(sort, team)))).grow();
		}}.show();
	}
	
	public enum SortTarget{
		health(b -> (int)b.health),
		industry(b -> {
			if(b.block instanceof GenericCrafter){
				GenericCrafter block = (GenericCrafter)b.block;
				
				return (int)(block.consumes.hasPower() ? block.consumes.getPower().usage / 60f : 1) * (block.outputItems == null ? (block.outputItem == null ? 0 : 1) : block.outputItems.length);
			}else return 0;
		}),
		defence(b -> {
			if(b.block instanceof Turret){
				Turret block = (Turret)b.block;
				
				return (int)b.health * (int)(block.consumes.hasPower() ? block.consumes.getPower().usage / 60f : 1) * block.size;
			}else return 0;
		}),
		storage(b -> b.items().total() / b.block.size / b.block.size)
		;
		
		public static final SortTarget[] all = values();
		public Intf<Building> getValue;
		
		SortTarget(Intf<Building> getValue){
			this.getValue = getValue;
		}
	}
}
