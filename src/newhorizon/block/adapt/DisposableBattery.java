package newhorizon.block.adapt;

import arc.Core;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.Battery;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class DisposableBattery extends Battery{
	public float consumption = 3f;
	
	public DisposableBattery(String name){
		super(name);
		insulated = true;
		rebuildable = false;
	}
	
	@Override
	public void setStats(){
		super.setStats();
		this.stats.add(Stat.powerUse, consumption * 60f, StatUnit.powerSecond);
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
	
	public class DisposableBatteryBuild extends BatteryBuild{
		public float progress;
		
		@Override
		public void placed(){
			super.placed();
			power.status = 1;
		}
		
		@Override
		public void updateTile(){
			if(timer(0, Time.delta))progress += Math.max(0, (consumption + power.graph.getLastScaledPowerOut()) / consumes.getPower().capacity);
			power.status = 1 - progress;
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
	}
}
