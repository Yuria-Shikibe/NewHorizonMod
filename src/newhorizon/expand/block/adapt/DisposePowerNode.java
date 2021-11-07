package newhorizon.expand.block.adapt;

import mindustry.world.blocks.power.PowerNode;

public class DisposePowerNode extends PowerNode{
	public DisposePowerNode(String name){
		super(name);
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
	}
	
	public class DisposePowerNodeBuild extends PowerNodeBuild{
		@Override
		public void placed(){
		}
	}
}
