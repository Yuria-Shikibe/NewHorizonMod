package newhorizon.expand.block.distribution;

import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.meta.BlockGroup;

public class LiquidAndItemBridge extends ItemBridge{
	
	
	public LiquidAndItemBridge(String name){
		super(name);
		hasItems = true;
		hasLiquids = true;
		outputsLiquid = true;
		canOverdrive = false;
		group = BlockGroup.transportation;
	}
	
//	public class LiquidAndItemBridgeBuild extends ItemBridgeBuild{
//		@Override
//		public void updateTile(){
//			super.updateTile();
//
//			Building other = world.build(link);
//			if(other == null || !linkValid(tile, other.tile())){
//				dumpLiquid(liquids.current(), 1f);
//			}else{
//				((ItemBridgeBuild)other).incoming.add(tile.pos());
//
//				if(consValid()){
//					float alpha = 0.04f;
//					if(hasPower){
//						alpha *= efficiency(); // Exceed boot time unless power is at max.
//					}
//					uptime = Mathf.lerpDelta(uptime, 1f, alpha);
//				}else{
//					uptime = Mathf.lerpDelta(uptime, 0f, 0.02f);
//				}
//
//				if(uptime >= 0.5f){
//
//					if(moveLiquid(other, liquids.current()) > 0.1f){
//						cycleSpeed = Mathf.lerpDelta(cycleSpeed, 4f, 0.05f);
//					}else{
//						cycleSpeed = Mathf.lerpDelta(cycleSpeed, 1f, 0.01f);
//					}
//				}
//			}
//		}
//	}
}
