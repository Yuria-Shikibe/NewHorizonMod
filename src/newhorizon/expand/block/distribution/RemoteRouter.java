package newhorizon.expand.block.distribution;

import arc.Core;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.indexer;
import static mindustry.Vars.tilesize;

public class RemoteRouter extends Block{
	public float range = 144f;
	public float reloadTime = 45f;
	public int maxDeliverPer = 45;
	
	public float loss = 0.1f;
	
	public RemoteRouter(String name){
		super(name);
		
		solid = true;
		update = true;
		destructible = true;
		flags = EnumSet.of(BlockFlag.storage);
		hasItems = true;
		itemCapacity = -1;
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.output, t -> {
			t.add(Core.bundle.format("mod.ui.loss", (loss / (1 + loss)) * 100));
		});
		stats.add(Stat.reload, 60f / reloadTime, StatUnit.perSecond);
		stats.add(Stat.range, range / tilesize, StatUnit.blocks);
	}
	
	@Override
	public void init(){
		if(itemCapacity < 0)itemCapacity = maxDeliverPer * 2;
		
		super.init();
	}
	
	@Override
	public void setBars(){
		super.setBars();
		bars.add("Burden", (RemoteRouterBuild entity) ->
			new Bar(
				() -> "Burden: " + entity.lastDelivered + "/" + maxDeliverPer,
				() -> Pal.items,
				() -> (float)entity.lastDelivered / maxDeliverPer
			)
		);
	}
	
	public class RemoteRouterBuild extends Building implements Ranged{
		public float reload = 0;
		public transient int lastDelivered = 0;
		
		@Override
		public BlockStatus status(){
			if(items.total() > 0 && efficiency() > 0)return BlockStatus.active;
			if(efficiency() == 0)return BlockStatus.noInput;
			if(items.total() == 0)return BlockStatus.noOutput;
			return BlockStatus.noInput;
		}
		
		@Override
		public boolean acceptItem(Building source, Item item){
			return items.get(item) < getMaximumAccepted(item);
		}
		
		@Override
		public void drawSelect(){
			float realRange = range();
			
			indexer.eachBlock(this, realRange, b -> b.block.consumes.has(ConsumeType.item) && b.block.consumes.get(ConsumeType.item) instanceof ConsumeItems, other -> Drawf.selected(other, Tmp.c1.set(Pal.accent).a(Mathf.absin(4f, 1f))));
			
			Drawf.dashCircle(x, y, realRange, Pal.accent);
		}
		
		@Override
		public void update(){
			if(items.total() > 0)reload += edelta();
			
			if(reload >= reloadTime){
				reload = 0;
				lastDelivered = 0;
				
				Vars.indexer.eachBlock(this, range(), b -> b.block.consumes.has(ConsumeType.item) && b.isValid() && b.enabled(), b -> {
					int toDeliver;
					if(b.block.consumes.get(ConsumeType.item) instanceof ConsumeItems)for(ItemStack stack : b.block.consumes.getItem().items){
						if(lastDelivered >= maxDeliverPer)return;
						if(b.acceptItem(this, stack.item) && items.has(stack.item)){
							b.handleStack(stack.item, (toDeliver = Math.min(Math.min(
								b.getMaximumAccepted(stack.item) - b.items.get(stack.item),
								Mathf.floor(items.get(stack.item) / (1 + loss))),
								maxDeliverPer - lastDelivered))
							, this);
							removeStack(stack.item, Mathf.ceil(toDeliver * (1 + loss)));
							Fx.itemTransfer.at(x, y, toDeliver, stack.item.color, b);
							lastDelivered += toDeliver;
						}
					}
					
					
//					TODO make ConsumeItemDynamic can be applied
//					if(b.block.consumes.get(ConsumeType.item) instanceof ConsumeItemDynamic){
//
//					}
				});
			}
		}
		
		@Override
		public float range(){
			return range;
		}
	}
}
