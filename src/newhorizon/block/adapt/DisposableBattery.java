package newhorizon.block.adapt;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerDistributor;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.func.NHSetting;

import static mindustry.Vars.tilesize;

public class DisposableBattery extends PowerDistributor{
	public float consumption = 3f;
	
	public TextureRegion topRegion;
	
	public Color emptyLightColor = Color.valueOf("f8c266");
	public Color fullLightColor = Color.valueOf("fb9567");
	
	public DisposableBattery(String name){
		super(name);
		insulated = true;
		rebuildable = false;
		consumesPower = true;
		breakable = false;
		details = "Cannot be broken after placed. It can only be destroyed after power run out or be damaged";
	}
	
	@Override
	public boolean canBreak(Tile tile){
		return false;
	}
	
	@Override
	public void setStats(){
		super.setStats();
		this.stats.add(Stat.powerUse, consumption * 60f, StatUnit.powerSecond);
	}
	
	@Override
	public void load(){
		super.load();
		topRegion = Core.atlas.find(name + "-top");
	}
	
	@Override
	public void setBars(){
		super.setBars();
		bars.remove("power");
		if(hasPower && consumes.hasPower()){
			ConsumePower cons = consumes.getPower();
			boolean buffered = cons.buffered;
			float capacity = cons.capacity;
			
			bars.add("power", (DisposableBatteryBuild entity) -> new Bar(
					() -> buffered ? Core.bundle.format("bar.poweramount", Float.isNaN((1 - entity.progress) * capacity) ? "<ERROR>" : (int)((1 - entity.progress) * capacity)) : Core.bundle.get("bar.power"),
					() -> Pal.powerBar,
					() -> 1 - entity.progress
			));
		}
	}
	
	public class DisposableBatteryBuild extends Building{
		public float progress;
		
		@Override
		public void placed(){
			super.placed();
			power.status = 0;
		}
		
		@Override
		public void updateTile(){
			if(timer(0, Time.delta))progress += Math.max(0, (consumption + power.graph.getLastScaledPowerOut()) / consumes.getPower().capacity);
			power.status = power.graph.getLastScaledPowerOut() / consumes.getPower().capacity * 1.125f;
			if(!Vars.headless)NHSetting.debug(() -> Log.info(power.graph.getPowerNeeded() + " | " + power.graph.getSatisfaction()));
			if(progress > 1)kill();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(progress);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			progress = read.f();
		}
		
		@Override
		public void draw(){
			Draw.color(emptyLightColor, fullLightColor, power.status);
			Fill.square(x, y, tilesize * size / 2f - 1);
			Draw.color();
			
			Draw.rect(topRegion, x, y);
		}
		
		@Override
		public void overwrote(Seq<Building> previous){
			for(Building other : previous){
				if(other.power != null && other.block.consumes.hasPower() && other.block.consumes.getPower().buffered){
					float amount = other.block.consumes.getPower().capacity * other.power.status;
					power.status = Mathf.clamp(power.status + amount / block.consumes.getPower().capacity);
				}
			}
		}
		
		@Override
		public BlockStatus status(){
			return BlockStatus.active;
		}
	}
}
