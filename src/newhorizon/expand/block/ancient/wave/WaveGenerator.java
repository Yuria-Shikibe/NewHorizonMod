package newhorizon.expand.block.ancient.wave;

import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.meta.BlockFlag;
import newhorizon.expand.game.wave.SineWaveEnergySource;
import newhorizon.expand.game.wave.WaveEnergyBuilding;
import newhorizon.expand.game.wave.WaveEnergyState;
import newhorizon.util.graphic.DrawFunc;

public class WaveGenerator extends Block implements WaveEnergyBlock{
	public DrawBlock drawer;
	
	public float warmupSpeed = 0.0075f;
	public float coolSpeed = 0.05f;
	
	public float ProgressScl = 120f;
	
	public Prov<WaveEnergyState> energyGen = () -> {
		return new SineWaveEnergySource().init(20, 0.0075f);
	};
	
	public WaveGenerator(String name){
		super(name);
		
		flags = EnumSet.of(BlockFlag.generator);
		
		update = true;
		solid = true;
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
	
	@Override
	public void load(){
		super.load();
		
		drawer.load(this);
	}
	
	@Override
	public boolean acceptWave(){
		return false;
	}
	
	@Override
	public boolean outputWave(){
		return true;
	}
	
	@Override
	public void setBars(){
		super.setBars();
		addBar("wave-energy", (WaveGeneratorBuild b) -> new Bar(
				() -> "Wave Energy",
				() -> Pal.lancerLaser,
				() -> b.energy.getCurrentEnergy() / b.energy.getMaxEnergy()
		));
	}
	
	public class WaveGeneratorBuild extends Building implements WaveEnergyBuilding{
		public WaveEnergyState energy;
		
		public float progress;
		public float totalProgress;
		public float warmup;
		
		@Override
		public WaveEnergyState getWave(){
			return null;
		}
		
		@Override
		public void draw(){
			drawer.draw(this);
		}
		
		@Override
		public void created(){
			energy = energyGen.get();
			energy.init(this);
		}
		
		@Override
		public void updateTile(){
			super.updateTile();
			
			energy.update();
			energy.updateTrans();
			
			if(efficiency > 0.05f){
				if(warmup > 0.9975f){
					warmup = 1;
				}else warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed);
			}else{
				if(warmup < 0.0025f){
					warmup = 0;
				}else warmup = Mathf.lerpDelta(warmup, 0, coolSpeed);
			}
			
			progress += edelta();
			totalProgress += edelta() * energy.getAngularVelocity();
			
			if(progress >= ProgressScl){
				progress = 0;
				consume();
			}
		}
		
		@Override
		public void drawSelect(){
			super.drawSelect();
			
			DrawFunc.overlayText(energy.getCurrentEnergy() + "", x, y - 48, 0, team.color, false);
		}
		
		@Override
		public float progress(){
			return progress / ProgressScl;
		}
		
		@Override
		public float totalProgress(){
			return totalProgress;
		}
		
		@Override
		public float warmup(){
			return warmup;
		}
	}
}
