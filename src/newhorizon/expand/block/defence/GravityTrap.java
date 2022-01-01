package newhorizon.expand.block.defence;

import arc.Core;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.expand.vars.EventListeners;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.DrawFunc;
import newhorizon.util.func.NHFunc;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class GravityTrap extends Block{
	protected Cons2<HyperSpaceWarper.Carrier, GravityTrapBuild> act = (c, b) -> {
		c.intercepted = true;
		PosLightning.createEffect(c, b , NHColor.darkEnrColor, 2, PosLightning.WIDTH * 1.5f);
	};
	
	private static Tile tmpTile;
	
	public int range = 35;
	
	public GravityTrap(String name){
		super(name);
		solid = true;
		configurable = true;
		update = true;
		hasPower = true;
		canOverdrive = false;
		sync = true;
		noUpdateDisabled = true;
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		Seq<TrapField> seq = NHFunc.getObjects(NHVars.world.gravityTraps);
		
		Draw.z(Layer.light + 5);
		for(TrapField bi : seq){
			bi.draw();
		}
		Draw.z(Layer.overlayUI);
		
		Draw.color(Pal.gray);
		Lines.stroke(3);
		Lines.poly(x * tilesize + offset, y * tilesize + offset, 6, range * tilesize);
		Draw.color(Pal.place);
		Draw.alpha(0.125f);
		Lines.stroke(1);
		Fill.poly(x * tilesize + offset, y * tilesize + offset, 6, range * tilesize);
		Draw.alpha(1f);
		Lines.poly(x * tilesize + offset, y * tilesize + offset, 6, range * tilesize);
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.range, range, StatUnit.blocks);
		stats.add(Stat.output, (t) -> {
			t.table(i -> {
				i.row().add("[gray]Legends:").left().pad(OFFSET).growX().height(LEN).row();
			}).growX().fillY().row();
			t.table(i -> {
				i.image().size(LEN).color(Pal.lancerLaser).left();
				i.add(Core.bundle.get("mod.ui.gravity-trap-field-friendly")).growX().padLeft(OFFSET / 2).row();
			}).padTop(OFFSET).growX().fillY().row();
			t.table(i -> {
				i.image().size(LEN).color(Pal.redderDust).left();
				i.add(Core.bundle.get("mod.ui.gravity-trap-field-hostile")).growX().padLeft(OFFSET / 2).row();
			}).padTop(OFFSET).growX().fillY().row();
		});
	}
	
	public class GravityTrapBuild extends Building implements Ranged{
		public float warmup;
		public transient TrapField field;
		
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
		public void updateTile(){
			if(efficiency() > 0){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
		}
		
		public boolean active(){
			return warmup > 0.5f;
		}
		
		@Override
		public void remove(){
			NHVars.world.gravityTraps.remove(field);
			
			super.remove();
		}
		
		@Override
		public void onRemoved(){
			NHVars.world.gravityTraps.remove(field);
			
			super.onRemoved();
		}
		
		@Override
		public void draw(){
			super.draw();
			
			Draw.reset();
			float sin = Mathf.absin(Time.time, 8f, size / 2f);
			
			Draw.z(Layer.bullet + 1f);
			Draw.color(team.color);
			float length = tilesize * size / 4f + sin;
			
			TextureRegion region = Core.atlas.find(NewHorizon.name("linked-arrow"));
			for(int i = 0; i < 4; i++){
				Tmp.v1.trns(i * 90, -length);
				
				Draw.rect(region, x + Tmp.v1.x, y + Tmp.v1.y, region.width * warmup * Draw.scl, region.height * warmup * Draw.scl, i * 90);
			}
			Draw.reset();
		}
		
		@Override
		public float range(){
			return range * tilesize;
		}
		
		@Override
		public void add(){
			super.add();
			
			field = new TrapField(this);
			
			NHVars.world.gravityTraps.insert(field);
			EventListeners.actAfterLoad.add(() -> NHVars.world.gravityTraps.insert(field));
		}
	}
	
	public static class TrapField implements Position, QuadTree.QuadTreeObject{
		public Cons<TrapField> drawer = e -> {};
		public float x = 0, y = 0;
		public float range = 120;
		public Boolp activated = () -> true;
		public Prov<Team> team = () -> Team.derelict;
		
		public boolean active(){return activated.get();}
		
		public void setPosition(Position position){
			x = position.getX();
			y = position.getY();
		}
		
		public TrapField(){
		
		}
		
		public TrapField(@NotNull GravityTrapBuild build){
			setPosition(build);
			activated = () -> build.active() && build.isValid();
			team = () -> build.team;
			range = build.range();
		}
		
		public Team team(){
			return team.get();
		}
		
		public void draw(){
			if(!active())return;
			Draw.color(DrawFunc.markColor(team()));
			Fill.poly(x, y, 6, range);
		}
		
		@Override
		public float getX(){
			return x;
		}
		
		@Override
		public float getY(){
			return y;
		}
		
		@Override
		public void hitbox(Rect out){
			out.setSize(range * 3).setCenter(x, y);
		}
		
		@Override
		public String toString(){
			return "TrapField{" + "pos(" + x + ", " + y + ")}";
		}
	}
}
