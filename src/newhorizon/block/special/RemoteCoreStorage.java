package newhorizon.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.modules.ItemModule;
import newhorizon.func.DrawFuncs;

import static mindustry.Vars.tilesize;

public class RemoteCoreStorage extends StorageBlock{
	public RemoteCoreStorage(String name){
		super(name);
		update = true;
		hasItems = true;
		itemCapacity = 0;
		configurable = true;
	}
	
	@Override
	public boolean isAccessible(){
		return true;
	}
	
	@Override
	public void setBars(){
		super.setBars();
		bars.add("warmup", (RemoteCoreStorageBuild entity) -> new Bar(() -> Mathf.equal(entity.warmup, 1, 0.015f) ? Core.bundle.get("done") : Core.bundle.get("research.load"), () -> Mathf.equal(entity.warmup, 1, 0.015f) ? Pal.heal : Pal.redderDust, () -> entity.warmup));
		bars.remove("items");
	}
	
	public class RemoteCoreStorageBuild extends StorageBuild{
		public float warmup = 0;
		public float progress = 0;
		
		@Override
		public void displayBars(Table table){
			super.displayBars(table);
			CoreBlock.CoreBuild b = core();
			if(b != null){
				table.add(new Bar(() -> Core.bundle.format("bar.items", b.items.total()), () -> Pal.items, () -> (float)(b.items.total() / b.storageCapacity))).growX().row();
			}
		}
		
		@Override
		public void updateTile(){
			if(efficiency() > 0 && core() != null){
				if(Mathf.equal(warmup, 1, 0.015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
			
			progress += warmup * efficiency() * Time.delta;
			
			if(Mathf.equal(warmup, 1, 0.015F)){
				if(linkedCore == null){
					linkedCore = core();
					items = linkedCore.items;
				}
			}else{
				linkedCore = null;
				items = new ItemModule();
			}
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(warmup);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			warmup = read.f();
		}
		
		@Override
		public void draw(){
			super.draw();
			Draw.z(Layer.effect - 1f);
			Draw.color(team.color);
			Building b = core();
			if(b != null){
				for (int i = 0; i < 5; i++) {
					float f = (progress - 25 * i) % 100 / 100;
					Tmp.v1.trns(angleTo(b), f * tilesize * size * 4);
					Lines.stroke(warmup * 1.5f * (1 - f));
					Lines.square(x + Tmp.v1.x, y + Tmp.v1.y, (1 - f) * 8, 45);
				}
			}
		}
		
		@Override
		public void drawSelect(){ }
		
		@Override
		public void drawConfigure(){
			if(core() != null)DrawFuncs.posSquareLink(Mathf.equal(warmup, 1, 0.015f) ? Pal.heal : Pal.redderDust, 1, 4, true, tile, core());
		}
	}
}
