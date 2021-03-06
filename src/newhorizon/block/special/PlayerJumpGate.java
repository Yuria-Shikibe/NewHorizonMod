package newhorizon.block.special;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import newhorizon.content.NHFx;
import newhorizon.func.NHSetting;
import newhorizon.interfaces.Linkablec;

import static mindustry.Vars.*;
import static newhorizon.func.TableFuncs.LEN;

public class PlayerJumpGate extends Block{
	public float reloadTime = 60f;
	public float range = 1200f;
	public float polyStroke = 2f;
	public float polyLerpSpeedScl = 0.8f;
	
	
	public PlayerJumpGate(String name){
		super(name);
		update = true;
		configurable = true;
		solid = true;
		config(Integer.class, (Cons2<PlayerJumpGateBuild, Integer>)PlayerJumpGateBuild::linkPos);
		config(Long.class, (PlayerJumpGateBuild tile, Long id) -> tile.teleport(Groups.player.getByID(id.intValue())));
		config(Boolean.class, (PlayerJumpGateBuild tile, Boolean value) -> tile.locked = value);
	}
	
	public boolean canPlaceOn(Tile tile, Team team) {return !Vars.net.client();}
	
	public void drawPlace(int x, int y, int rotation, boolean valid){
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
		if (Vars.world.tile(x, y) != null) {
			if (!canPlaceOn(null, null)) {
				drawPlaceText("Broken in server.\nWhy? Because the fucking anti teleport plugin fucked up everything.", x, y, valid);
			}
		}
	}
	
	@Override
	public void setBars() {
		super.setBars();
		bars.add("progress",
			(PlayerJumpGateBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.power,
				() -> entity.reload / reloadTime
			)
		);
	}
	
	public class PlayerJumpGateBuild extends Building implements Linkablec{
		public int link = -1;
		public float warmup;
		public float reload;
		public transient float progress;
		public boolean locked = false;
		
		@Override
		public int linkPos(){
			return link;
		}
		
		@Override
		public void linkPos(int value){
			if(!locked)link = value;
		}
		
		@Override
		public Color getLinkColor(){
			return team.color;
		}
		
		@Override
		public float range(){
			return range;
		}
		
		@Override
		public boolean linkValid(){
			return link() != null && link() instanceof PlayerJumpGateBuild && link().team == team;
		}
		
		public void teleport(Player player){
			if(!canFunction())return;
			
			Tmp.v3.set(link()).sub(player).scl((1 + player.unit().type.drag) / tilesize * 1.575f);
			
			player.unit().lookAt(link());
			player.unit().vel().set(Tmp.v3);
			
			float time = dst(link()) / (Tmp.v3.len() * (1f - player.unit().type.drag));
			for(int i = 0; i < 30; i++)Time.run(time * 2 / 30 * i, () -> {
				NHFx.poly.at(player.unit().x, player.unit().y, player.unit().hitSize * 1.1f, player.team().color);
				NHSetting.debug(() -> Log.info(player.x + " | " + player.y));
				NHSetting.debug(() -> Log.info("[U]" + player.unit().x + " | " + player.unit().y));
			});
			Time.run(time, () -> player.unit().vel.trns(angleTo(link()), player.unit().type.speed));
			
			if(mobile)Core.camera.position.set(link());
			reload = 0;
			Sounds.plasmaboom.at(this, Mathf.random(0.9f, 1.1f));
			for(int i = 0; i < 3; i++){
				Time.run(8 * i, () -> {
					Fx.spawn.at(this);
				});
			}
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(warmup);
			write.f(reload);
			write.i(link);
			write.bool(locked);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			warmup = read.f();
			reload = read.f();
			link = read.i();
			locked = read.bool();
		}
		
		@Override
		public boolean onConfigureTileTapped(Building other){
			if (this == other || link == other.pos()) {
				configure(-1);
				return false;
			}
			if (other.within(this, range()) && other.team == team && other instanceof PlayerJumpGateBuild) {
				configure(other.pos());
				return false;
			}
			return true;
		}
		
		@Override
		public void drawConfigure(){
			Drawf.dashCircle(x, y, range, getLinkColor());
			drawLink();
		}
		
		@Override
		public void updateTile(){
			reload += efficiency() * Time.delta;
			progress += (efficiency() + warmup) * Time.delta * polyLerpSpeedScl;
			if(canFunction()){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
		}
		
		@Override
		public void draw(){
			super.draw();
			Draw.z(Layer.effect - 1f);
			Draw.color(getLinkColor());
			if(canFunction()){
				for (int i = 0; i < 5; i++) {
					float f = (progress - 25 * i) % 100 / 100;
					Tmp.v1.trns(angleTo(link()), f * tilesize * size * 4);
					Lines.stroke(warmup * polyStroke * (1 - f));
					Lines.poly(x + Tmp.v1.x, y + Tmp.v1.y, 6, (1 - f) * size * tilesize / 1.25f);
				}
			}
		}
		
		@Override
		public void buildConfiguration(Table table){
			final float dstMax = size * tilesize / 2.5f;
			table.button(Icon.lock, LEN, () -> configure(!locked)).size(LEN).update(b -> b.getStyle().imageUp = locked ? Icon.lock : Icon.lockOpen);
			table.button("Teleport", Icon.upOpen, LEN, () -> configure((long)Vars.player.id)).size(LEN * 4, LEN).disabled(b -> !playerValid() || !canFunction() || dst(Vars.player) > dstMax);
		}
		
		public boolean canFunction(){
			return efficiency() > 0 && reload > reloadTime && linkValid();
		}
		
		public boolean playerValid(){
			return player != null && player.unit() != null && player.unit().type != null && player.unit().type.flying && player.unit().isValid();
		}
	}
}
