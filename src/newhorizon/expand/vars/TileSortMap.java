package newhorizon.expand.vars;

import arc.Core;
import arc.func.Intf;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.production.GenericCrafter;
import newhorizon.util.func.NHFunc;

import static mindustry.Vars.tilesize;

public class TileSortMap{
	public static final ObjectMap<Team, TileSortMap> maps = new ObjectMap<>();
	protected static final Color backColor = Color.darkGray;
	
	protected static final Color tmpColor = new Color();
	protected static Tile tile;
	protected static final Point2 tmpP1 = new Point2(), tmpP2 = new Point2();
	
	protected Pixmap pixmap;
	
	public boolean scanDone = false;
	public boolean analysisDone = false;
	
	public IntMap<Integer[]> sortMap = new IntMap<>();
	public int[] max = new int[ValueCalculator.all.length];
	
	public final Team team;
	public final Point2 leftDown = new Point2(), rightUp = new Point2();
	
	public Rect[] bestRange = new Rect[ValueCalculator.all.length];
	public Vec2[] bestTarget = new Vec2[ValueCalculator.all.length];
	
	/***/
	public int accuracy = 20;
	
	protected TileSortMap(Team team){
		this.team = team;
	}
	
	public static TileSortMap getTeamMap(Team team){
		return maps.get(team);
	}
	
	public static void registerTeam(Team team){
		TileSortMap map = new TileSortMap(team);
		
		map.update();
		map.init();
		
		maps.put(team, map);
	}
	
	public void init(){
		initVert();
	}
	
	public static void clear(){
		maps.clear();
	}
	
	public void extremeVert(Building b){
		leftDown.x = Math.min(leftDown.x, b.tile.x - (b.block.size - 1) / 2);
		leftDown.y = Math.min(leftDown.y, b.tile.y - (b.block.size - 1) / 2);
		
		rightUp.x = Math.max(rightUp.x, b.tile.x + b.block.size / 2);
		rightUp.y = Math.max(rightUp.y, b.tile.y + b.block.size / 2);
	}
	
	public void initVert(){
		leftDown.set(Vars.world.width(), Vars.world.height());
		rightUp.set(0, 0);
	}
	
	public void update(ValueCalculator target){
		scanDone = false;
		initVert();
		
		Core.app.post(() -> {
			Groups.build.each(b -> b.team == team && !b.isPayload(), b -> {
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
				
				extremeVert(b);
				
				max[target.ordinal()] = Math.max(max[target.ordinal()], value);
			});
			
			scanDone = true;
		});
	}
	
	public void update(){
		scanDone = false;
		initVert();
		
		Core.app.post(() -> {
			Groups.build.each(b -> b.team == team, b -> {
				int index;
				
				int[] value = new int[ValueCalculator.all.length];
				
				for(int i = 0; i < ValueCalculator.all.length; i++)value[i] = ValueCalculator.all[i].getValue.get(b);
				
				extremeVert(b);
				
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
			
			scanDone = true;
		});
	}
	
	public void analysis(){
		analysisDone = false;
		
		Point2 sub = tmpP1.set(rightUp).sub(leftDown);
		Point2 center = tmpP1.set((leftDown.x + rightUp.x) / 2, (leftDown.y + rightUp.y) / 2);
		if(Mathf.sqrt(sub.x * sub.y) < accuracy){
			Seq<Building> buildings = new Seq<>();
			NHFunc.square(center.x, center.y, sub.x / 2, sub.y / 2, ((x, y) -> {
				if(Vars.world.build(x, y) != null)buildings.add(Vars.world.build(x, y));
			}));
			
			if(buildings.isEmpty())return;
			for(int i = 0; i < ValueCalculator.all.length; i++){
				bestRange[i] = new Rect().setSize(sub.x * tilesize, sub.y * tilesize).setCenter(center.x * tilesize + tilesize / 2f,  center.y * tilesize + tilesize / 2f);
				int finalI = i;
				buildings.sortComparing(b -> ValueCalculator.all[finalI]);
				bestTarget[i] = new Vec2().set(buildings.first());
			}
		}else{
			int[] total = new int[ValueCalculator.all.length];
			
		}
	}
	
	public static Tile[] filledTiles(Building building){
		if(building.isPayload())return new Tile[]{};
		
		int radius;
		int index = 0;
		
		Tile[] tile = new Tile[Mathf.pow(building.block.size, 2)];
		
		if(building.block.size % 2 == 1){
			radius = (building.block.size - 1) / 2;
			
			for(int dx = building.tile.x - radius; dx <= building.tile.x + radius; ++dx) {
				for(int dy = building.tile.y - radius; dy <= building.tile.y + radius; ++dy) {
					tile[index++] = Vars.world.tile(dx, dy);
				}
			}
			
		}else{
			radius = building.block.size / 2 - 1;
			
			for(int dx = building.tile.x - radius; dx <= building.tile.x + radius + 1; ++dx) {
				for(int dy = building.tile.y - radius; dy <= building.tile.y + radius + 1; ++dy) {
					tile[index++] = Vars.world.tile(dx, dy);
				}
			}
			
		}
		return tile;
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
		
		pixmap.set(leftDown.x, Vars.world.tiles.height - leftDown.y, Pal.heal);
		pixmap.set(rightUp.x, Vars.world.tiles.height - rightUp.y, Pal.heal);
		Log.info(leftDown + "|" + rightUp);
		
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
