package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.ctype.ContentType;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BuildVisibility;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

public class UnitSpawner extends Block{
	public float spawnRange = 4;
	
	protected static String divKey = "@@@";
	
	public UnitSpawner(String name){
		super(name);
		
		alwaysUnlocked = true;
		destroySound = ambientSound = breakSound = Sounds.none;
		size = 1;
		update = true;
		outputsPayload = true;
		hasPower = false;
		configurable = true;
		clipSize = 120;
		saveConfig = true;
		rebuildable = false;
		solid = solidifes = false;
		requirements = ItemStack.empty;
		category = Category.units;
		destroyEffect = Fx.none;
		buildVisibility = BuildVisibility.sandboxOnly;
		config(UnitType.class, (UnitIniterBuild build, UnitType unit) -> build.toSpawnType = unit);
		config(String.class, (UnitIniterBuild build, String unit) -> {
			String[] s = unit.split(divKey);
			if(s.length < 3)return;
			build.angle = Float.parseFloat(s[1]);
			build.delay = Float.parseFloat(s[2]);
			build.toSpawnType = content.getByName(ContentType.unit, s[0]);
		});
		configClear((UnitIniterBuild tile) -> {
			tile.toSpawnType = UnitTypes.alpha;
			tile.angle = 0;
			tile.delay = 30;
		});
	}
	
	@Override
	public boolean isHidden(){
		return !Vars.state.rules.editor;
	}
	
	@Override
	public boolean canBeBuilt(){
		return Vars.state.rules.editor;
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		if(!canBeBuilt())drawPlaceText(Core.bundle.get("mod.ui.cautions.unit-initer"), x, y, valid);
	}
	
	public class UnitIniterBuild extends Building{
		public UnitType toSpawnType = UnitTypes.alpha;
		public float angle;
		public float delay = 6 * 60;
		public transient boolean addUnit = false;
		
		
		@Override public void onDestroyed(){}
		
		@Override public void afterDestroyed(){}
		
		@Override
		public void buildConfiguration(Table table){
			table.slider(5, 20, 1, 0, f -> delay = f * 60f).growX().row();
			table.slider(0, 360, 45, 0, f -> angle = f).growX().row();
			ItemSelection.buildTable(table, content.units().select(b -> !b.isHidden()), this::type, this::configure);
		}
		
		public UnitType type(){
			return content.getByName(ContentType.unit, config().split(divKey)[0]);
		}
		
		@Override
		public String config(){
			return toSpawnType.name + divKey + angle + divKey + delay;
		}
		
		@Override
		public void control(LAccess type, Object p1, double p2, double p3, double p4){
			super.control(type, p1, p2, p3, p4);
			
			if(type == LAccess.shootp && p1 instanceof String){
				toSpawnType = content.unit((String)p1);
				angle = (float)p2;
				delay = 30;
			}
		}
		
		@Override
		public void control(LAccess type, double p1, double p2, double p3, double p4){
			super.control(type, p1, p2, p3, p4);
		}
		
		@Override public void updateTile(){
			if(!addUnit)addUnit();
		}
		
		@Override public void drawConfigure(){}
		
		@Override
		public void draw(){
			if(Vars.state.isEditor()){
				super.draw();
				Tmp.v1.trns(angle, tilesize * size * 1.5f);
				Drawf.arrow(x, y, x + Tmp.v1.x, y + Tmp.v1.y, size * tilesize, tilesize / 2f);
				if(toSpawnType == null) return;
				
				Drawf.light(x, y, tilesize * size * 3f, team.color, 0.8f);
				Draw.z(Layer.overlayUI);
				Draw.rect(toSpawnType.fullIcon, x, y, size * tilesize, size * tilesize);
				DrawFunc.overlayText(delay / 60 + "s", x - Tmp.v1.x, y - Tmp.v1.y, -4f, Pal.accent, true);
			}
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(angle);
			write.f(delay);
			TypeIO.writeUnitType(write, toSpawnType);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			angle = read.f();
			delay = read.f();
			toSpawnType = TypeIO.readUnitType(read);
		}
		
		public void addUnit(){
			NHFunc.spawnUnit(team, x, y, angle, spawnRange, delay, 0, toSpawnType, 1);
			kill();
			addUnit = true;
		}
	}
}
