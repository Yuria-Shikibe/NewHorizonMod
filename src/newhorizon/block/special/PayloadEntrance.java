package newhorizon.block.special;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.production.PayloadAcceptor;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;
import static newhorizon.func.TableFs.LEN;

public class PayloadEntrance extends PayloadAcceptor{
	protected float dstMax;
	
	public PayloadEntrance(String name){
		super(name);
		configurable = true;
		rotate = true;
		outputsPayload = true;
		config(Integer.class, (PayloadEntranceBuild tile, Integer id) -> tile.enter(Groups.player.getByID(id)));
	}
	
	@Override
	public void init(){
		dstMax = size * tilesize / 2.5f;
		super.init();
	}
	
	public class PayloadEntranceBuild extends PayloadAcceptorBuild<UnitPayload>{
		public void enter(Player player){
			if(payload == null){
				payload = new UnitPayload(player.unit());
				player.unit().remove();
			}else{
				payload.dump();
			}
			
		}
		
		@Override
		public void draw(){
			super.draw();
			drawPayload();
		}
		
		@Override
		public void updateTile(){
			super.updateTile();
			moveOutPayload();
		}
		
		@Override
		public void drawConfigure(){
			if(player == null)return;
			
			Drawf.square(player.x, player.y, player.unit().hitSize, 45, dst(Vars.player) > dstMax ? Pal.redderDust : Color.green);
		}
		
		@Override
		public void buildConfiguration(Table table){
			table.button("Teleport", Icon.upOpen, LEN, () -> configure(Vars.player.id)).size(LEN * 4, LEN).disabled(b ->  player.unit() == null || dst(Vars.player) > dstMax);
		}
	}
}
