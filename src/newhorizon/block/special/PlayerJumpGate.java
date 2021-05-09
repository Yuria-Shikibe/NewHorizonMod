package newhorizon.block.special;

import arc.Core;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import newhorizon.func.NHFunc;
import newhorizon.interfaces.Linkablec;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;
import static newhorizon.func.TableFs.LEN;

public class PlayerJumpGate extends Block{
	protected float dstMax;
	
	public float reloadTime = 60f;
	public float range = 1200f;
	public float polyStroke = 2f;
	public float polyLerpSpeedScl = 0.8f;
	
	public PlayerJumpGate(String name){
		super(name);
		update = true;
		configurable = true;
		
		details = "If something wrong take place in the server, please type \"/sync\".";
		
		config(Point2.class, (Cons2<PlayerJumpGateBuild, Point2>)PlayerJumpGateBuild::linkPos);
		config(Integer.class, (PlayerJumpGateBuild tile, Integer id) -> tile.teleport(Groups.player.getByID(id)));
		config(Boolean.class, (PlayerJumpGateBuild tile, Boolean value) -> tile.locked = value);
	}
	
	@Override
	public void init(){
		dstMax = size * tilesize / 2.5f;
		super.init();
	}
	
	public void drawPlace(int x, int y, int rotation, boolean valid){
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.accent);
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
			Building target = link();
			
			NHFunc.teleportUnitNet(player.unit(), target.x, target.y, angleTo(target));
			reload = 0;
			
			Sounds.respawn.at(this, Mathf.random(0.9f, 1.1f));
			Sounds.respawn.at(target, Mathf.random(0.9f, 1.1f));
			for(int i = 0; i < 3; i++){
				Time.run(8 * i, () -> {
					Fx.spawn.at(this);
					Fx.spawn.at(target);
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
				configure(Tmp.p1.set(-1, -1));
				return false;
			}
			if (other.within(this, range()) && other.team == team && other instanceof PlayerJumpGateBuild) {
				configure(Point2.unpack(other.pos()));
				return false;
			}
			return true;
		}
		
		@Override
		public void drawConfigure(){
			Drawf.dashCircle(x, y, range, getLinkColor());
			drawLink();
			
			if(player == null)return;
			
			Drawf.square(player.x, player.y, player.unit().hitSize, 45, dst(Vars.player) > dstMax ? Pal.redderDust : Pal.heal);
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
					Lines.square(x + Tmp.v1.x, y + Tmp.v1.y, (1 - f) * size * tilesize / 2f + 1);
				}
			}
		}
		
		@Override
		public void buildConfiguration(Table table){
			table.button(Icon.lock, LEN, () -> configure(!locked)).size(LEN).update(b -> b.getStyle().imageUp = locked ? Icon.lock : Icon.lockOpen);
			table.button("Teleport", Icon.upOpen, LEN, () -> configure(Vars.player.id)).size(LEN * 4, LEN).disabled(b -> !playerValid() || !canFunction() || dst(Vars.player) > dstMax);
		}
		
		public boolean canFunction(){
			return efficiency() > 0 && reload > reloadTime && linkValid();
		}
		
		public boolean playerValid(){
			return player != null && player.unit() != null && player.unit().type != null && player.unit().isValid();
		}
	}
}
