package newhorizon.expand.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.ObjectSet;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.modules.ItemModule;
import newhorizon.NHGroups;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class RemoteCoreStorage extends StorageBlock{
	private static CoreBlock.CoreBuild tmpCoreBuild;
	
	public static void clear(){
		for(ObjectSet<RemoteCoreStorageBuild> set : NHGroups.placedRemoteCore){
			set.clear();
		}
	}
	
	public RemoteCoreStorage(String name){
		super(name);
		update = true;
		hasItems = true;
		itemCapacity = 0;
		configurable = true;
		replaceable = false;
	}
	
	public void drawPlace(int x, int y, int rotation, boolean valid) {
		if(maxPlaceNum(Vars.player.team()) <= NHGroups.placedRemoteCore[Vars.player.team().id].size){
			drawPlaceText("Maximum Placement Quantity Reached", x, y, false);
		}
	}
	
	public int maxPlaceNum(Team team){
		return (team == Vars.state.rules.waveTeam && !state.rules.pvp) || team.rules().cheat ? Integer.MAX_VALUE : Mathf.clamp(Vars.world.width() * Vars.world.height() / 10000, 3, 10);
	}
	
	@Override
	public boolean isAccessible(){
		return true;
	}
	
	@Override
	public void setBars(){
		super.setBars();
		addBar("maxPlace", (RemoteCoreStorageBuild entity) ->
			new Bar(
				() -> "Max Place | " + NHGroups.placedRemoteCore[entity.team.id].size + " / " + maxPlaceNum(entity.team),
				() -> NHGroups.placedRemoteCore[entity.team.id].size < maxPlaceNum(entity.team) ? Pal.accent : Pal.redderDust,
				() -> (float)NHGroups.placedRemoteCore[entity.team.id].size / maxPlaceNum(entity.team)
			)
		);
		addBar("warmup", (RemoteCoreStorageBuild entity) -> new Bar(() -> Mathf.equal(entity.warmup, 1, 0.015f) ? Core.bundle.get("done") : Core.bundle.get("research.load"), () -> Mathf.equal(entity.warmup, 1, 0.015f) ? Pal.heal : Pal.redderDust, () -> entity.warmup));
		removeBar("items");
		addBar("items", (RemoteCoreStorageBuild entity) -> new Bar(
			() -> Core.bundle.format("bar.items", entity.items.total()),
			() -> Pal.items,
			() -> (float)(entity.items.total() / ((tmpCoreBuild = entity.core()) == null ? Integer.MAX_VALUE : tmpCoreBuild.storageCapacity)))
		);
	}
	
	@Override
	public boolean canPlaceOn(Tile tile, Team team, int rotation){
		return super.canPlaceOn(tile, team, rotation) && NHGroups.placedRemoteCore[team.id].size < maxPlaceNum(team);
	}
	
	public class RemoteCoreStorageBuild extends StorageBuild{
		public float warmup = 0;
		public float progress = 0;
		
		@Override
		public void onRemoved(){
			super.onRemoved();
			NHGroups.placedRemoteCore[team.id].remove(this);
		}
		
		@Override
		public void created(){
			Core.app.post(() -> {
				NHGroups.placedRemoteCore[team.id].add(this);
			});
		}
		
		@Override
		public void updateTile(){
			if(efficiency() > 0 && core() != null && NHGroups.placedRemoteCore[team.id].size <= maxPlaceNum(team)){
				if(Mathf.equal(warmup, 1, 0.015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
			
			progress += warmup * efficiency() * Time.delta;
			
			if(Mathf.equal(warmup, 1, 0.015F)){
				if(linkedCore == null || !linkedCore.isValid() && core() != null){
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
		public boolean canPickup(){
			return false;
		}
		
		@Override
		public void drawSelect(){ }
		
		@Override
		public void drawConfigure(){
			if(core() != null) DrawFunc.posSquareLink(Mathf.equal(warmup, 1, 0.015f) ? Pal.heal : Pal.redderDust, 1, 4, true, tile, core());
		}
	}
}
