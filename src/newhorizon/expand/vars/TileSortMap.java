package newhorizon.expand.vars;

import arc.Core;
import arc.func.Intf;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.production.GenericCrafter;

public class TileSortMap{
	public static final ObjectMap<Team, TileSortMap> maps = new ObjectMap<>();
	protected static final Color backColor = Color.darkGray;
	
	protected static final Color tmpColor = new Color();
	protected static Tile tile;
	
	protected Pixmap pixmap;
	
	public static final int
		HEALTH = 0,
		INDUSTRY = 1,
		DEFENCE = 2;
	
	public final Team team;
	public IntMap<Integer[]> sortMap = new IntMap<>();
	public int[] max = new int[ValueCalculator.all.length];
	
	public int lastUpdatedIndex = 0;
	public boolean complete = false;
	
	protected TileSortMap(Team team){
		this.team = team;
	}
	
	public static TileSortMap getTeamMap(Team team){
		return maps.get(team);
	}
	
	public static void registerTeam(Team team){
		TileSortMap map = new TileSortMap(team);
		
		Core.app.post(map::update);
		
		maps.put(team, map);
	}
	
	public static void clear(){
		maps.clear();
	}
	
	public void update(ValueCalculator target){
		Groups.build.each(b -> b.team == team, b -> {
			int index;
			
			int value = target.getValue.get(b);
			
			for(Tile t : filledTiles(b)){
				index = t.array();
				
				Integer[] intArr = sortMap.get(index);
				
				if(intArr == null){
					intArr = new Integer[ValueCalculator.all.length];
					intArr[target.ordinal()] = value;
					sortMap.put(index, intArr);
				}else{
					intArr[target.ordinal()] = value;
				}
			}
			
			max[target.ordinal()] = Math.max(max[target.ordinal()], value);
		});
	}
	
	public void update(){
		Groups.build.each(b -> b.team == team, b -> {
			int index;
			
			int[] value = new int[ValueCalculator.all.length];
			
			for(int i = 0; i < ValueCalculator.all.length; i++)value[i] = ValueCalculator.all[i].getValue.get(b);
			
			for(Tile t : filledTiles(b)){
				index = t.array();
				
				Integer[] intArr = sortMap.get(index);
				
				if(intArr == null){
					intArr = new Integer[ValueCalculator.all.length];
					
					for(int i = 0; i < ValueCalculator.all.length; i++){
						int v = value[i];
						intArr[i] = v;
						max[i] = Math.max(max[i], v);
					}
					
					sortMap.put(index, intArr);
				}else for(int i = 0; i < ValueCalculator.all.length; i++){
					int v = value[i];
					intArr[i] = v;
					max[i] = Math.max(max[i], v);
				}
			}
		});
	}
	
	public static Tile[] filledTiles(Building building){
		if(building.isPayload())return new Tile[]{};
		
		int radius = 0;
		int index = 0;
		
		Tile[] tile = new Tile[Mathf.pow(building.block.size, 2)];
		
		if(building.block.size % 2 == 1){
			radius = (building.block.size - 1) / 2;
			
			for(int dx = building.tile.x - radius; dx <= building.tile.x + radius; ++dx) {
				for(int dy = building.tile.y - radius; dy <= building.tile.y + radius; ++dy) {
					tile[index++] = Vars.world.tile(dx, dy);
				}
			}
			
			return tile;
		}else{
			radius = building.block.size / 2 - 1;
			
			for(int dx = building.tile.x - radius; dx <= building.tile.x + radius + 1; ++dx) {
				for(int dy = building.tile.y - radius; dy <= building.tile.y + radius + 1; ++dy) {
					tile[index++] = Vars.world.tile(dx, dy);
				}
			}
			
			return tile;
		}
	}
	
	public static int size(){return Vars.world.tiles.width * Vars.world.tiles.height;}
	
	public static int x(int index){
		return Vars.world.tiles.geti(index).x;
	}
	
	public static int y(int index){
		return Vars.world.tiles.geti(index).y;
	}
	
	public static int index(Tile tile){
		return tile.array();
	}
	
	public TextureRegion outputAsPixmap(ValueCalculator target){
		int index = target.ordinal();
		
		tmpColor.set(team.color);
		
		Pixmap pixmap = new Pixmap(Vars.world.tiles.width, Vars.world.tiles.height);
		
		pixmap.fill(Tmp.c1.set(backColor).a(0.25f));
		pixmap.outline(backColor, 3);
		
		for(IntMap.Entry<Integer[]> entry : sortMap.entries()){
			tile = Vars.world.tiles.geti(entry.key);
			
			float lerp = entry.value[index] / (float)max[index];
			
			tmpColor.set(team.color);
			pixmap.set(tile.x, Vars.world.tiles.height - tile.y, tmpColor.a(Mathf.clamp(lerp, 0.15f, 1f)).lerp(Color.white, Mathf.clamp(lerp, 0f, 0.55f)));
		}
		
		this.pixmap = pixmap;
		
		Texture texture = new Texture(pixmap);
		return new TextureRegion(texture);
	}
	
	public void showAsDialog(ValueCalculator target){
		new BaseDialog(""){{
			cont.pane(t -> t.image(outputAsPixmap(target)).fill()).grow();
			addCloseButton();
		}}.show();
	}
	
	public enum ValueCalculator{
		healthLinear(b -> (int)b.health),
		healthSqrt(b -> (int)(Mathf.sqrt(b.health))),
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
		storage(b -> b.items() == null ? 0 : b.items().total() / b.block.size / b.block.size),
		power(b -> {
			if(b.block instanceof PowerGenerator){
				PowerGenerator generator = (PowerGenerator)b.block;
				
				return (int)(Mathf.log(Mathf.E, generator.powerProduction * 60 * b.power.status));
			}else if(b.block.consumes.hasPower()){
				if(b.block.consumes.getPower().buffered){
					return (int)(Mathf.log(60, b.block.consumes.getPower().capacity * 60 * b.power.status));
				}else return (int)(Mathf.log(40, b.block.consumes.getPower().usage * 60 * b.power.status));
			}return 0;
		})
		;
		
		public static final ValueCalculator[] all = values();
		public Intf<Building> getValue;
		
		ValueCalculator(Intf<Building> getValue){
			this.getValue = getValue;
		}
	}
}
