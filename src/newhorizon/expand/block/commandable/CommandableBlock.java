package newhorizon.expand.block.commandable;

import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Building;
import mindustry.io.TypeIO;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import newhorizon.NHGroups;

public abstract class CommandableBlock extends Block{
	protected static final Seq<CommandableBlockBuild> participantsTmp = new Seq<>();
	protected int commandPos = -1;
	
	public DrawBlock drawer = new DrawDefault();
	public float warmupSpeed = 0.02f;
	public float warmupFallSpeed = 0.0075f;
	public float range;
	public float reloadTime = 60;
	public float configureChargeTime = 60;
	
	public CommandableBlock(String name){
		super(name);
		timers = 4;
		update = sync = configurable = solid = true;
		
		config(Vec2.class, CommandableBlockBuild::commandAll);
		config(Point2.class, (CommandableBlockBuild b, Point2 p) -> {
			b.setTarget(p);
			commandPos = p.pack();
		});
	}
	
	@Override
	public void load(){
		super.load();
		
		drawer.load(this);
	}
	
	
	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
		drawer.drawPlan(this, plan, list);
	}
	
	@Override
	public TextureRegion[] icons(){
		return drawer.finalIcons(this);
	}
	
	@Override
	public void getRegionsToOutline(Seq<TextureRegion> out){
		drawer.getRegionsToOutline(this, out);
	}
	
	public boolean sameGroup(Block other){
		return equals(other);
	}
	
	public abstract class CommandableBlockBuild extends Building implements Ranged{
		protected final Vec2 targetVec = new Vec2().set(this);
		protected final Vec2 lastConfirmedTarget = new Vec2();
		
		public boolean initiateConfigure = false;
		public float configureChargeProgress = 0;
		
		public float reload;
		public float warmup;
		public float totalProgress;
		public BlockUnitc unit;
		
		public int target = -1;
		public float logicControlTime = -1;
		
		public void setTarget(Point2 point2){
			target = point2.pack();
			target();
		}
		
		public int getTarget(){
			return target;
		}
		
		public abstract void command(Vec2 pos);
		public abstract void commandAll(Vec2 pos);
		
		public abstract boolean canCommand(Vec2 target);
		public abstract boolean isCharging();
		public abstract boolean shouldCharge();
		
		public boolean isChargingConfigure(){
			return initiateConfigure;
		}
		
		public boolean shouldChargeConfigure(){
			return configureChargeProgress < configureChargeTime && initiateConfigure;
		}
		
		public boolean configureChargeComplete(){
			return configureChargeProgress >= configureChargeTime && initiateConfigure;
		}
		
		public Vec2 target(){
			Tile tile = Vars.world.tile(target);
			if(tile != null){
				return targetVec.set(tile);
			}else return targetVec.set((Position)self());
		}
		
		@Override
		public void updateTile(){
			if(unit != null){
				unit.health(health);
				unit.rotation(rotation);
				unit.team(team);
				unit.set(x, y);
			}
			
			if(shouldCharge()){
				reload += edelta() * warmup;
			}
			
//			if(isControlled() && timer.get(4, 10)){ //player behavior
//				if(unit.isShooting())updateShoot();
//				updateControl();
//			}

			if(efficiency > 0){
				warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed);
				totalProgress += warmup * edelta();
			}else warmup = Mathf.lerpDelta(warmup, 0, warmupFallSpeed);
		}
		
		public void updateShoot(){
		
		}
		
		public void updateControl(){
		
		}
		
		@Override
		public void created(){
			unit = (BlockUnitc)UnitTypes.block.create(team);
			unit.tile(this);
		}
		
		@Override
		public void read(Reads read, byte v){
			warmup = read.f();
			reload = read.f();
			TypeIO.readVec2(read, targetVec);
			
			super.read(read);
		}
		
		@Override
		public void write(Writes write){
			write.f(warmup);
			write.f(reload);
			TypeIO.writeVec2(write, targetVec);
		}
		
//		@Override
//		public Unit unit(){
//			if(unit == null){
//				unit = (BlockUnitc)UnitTypes.block.create(team);
//				unit.tile(this);
//			}
//			return (Unit)unit;
//		}
		
		@Override
		public float range(){
			return range;
		}
		
		@Override
		public void draw(){
			drawer.draw(this);
		}
		
		@Override
		public float warmup(){
			return warmup;
		}
		
		@Override
		public float totalProgress(){
			return totalProgress;
		}
		
		@Override
		public void add(){
			if(!added)NHGroups.commandableBuilds.add(this);
			
			super.add();
		}
		
		@Override
		public void remove(){
			if(added)NHGroups.commandableBuilds.remove(this);
			
			super.remove();
		}
	}
}
