package newhorizon.expand.block.defence;

import arc.Core;
import arc.func.Cons2;
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
import mindustry.Vars;
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
import newhorizon.expand.vars.EventTriggers;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.feature.PosLightning;
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
		
		for(TrapField bi : seq){
			GravityTrapBuild b = bi.build;
			Draw.color(Pal.gray);
			Lines.stroke(3);
			Lines.poly(b.x, b.y, 6, b.range());
		}
		
		for(TrapField bi : seq){
			GravityTrapBuild b = bi.build;
			Draw.color(b.team == Vars.player.team() ? Pal.lancerLaser : Pal.redderDust);
			Draw.alpha(b.warmup / 12f);
			Lines.stroke(1);
			Fill.poly(b.x, b.y, 6, b.range());
			Draw.alpha(1);
			Lines.poly(b.x, b.y, 6, b.range());
		}
		
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
			super.remove();
			NHVars.world.gravityTraps.remove(field);
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
			EventTriggers.actBeforeLoad.add(() -> NHVars.world.gravityTraps.insert(field));
		}
	}
	
	public static class TrapField implements Position, QuadTree.QuadTreeObject{
		public final @NotNull GravityTrapBuild build;
		
		public TrapField(@NotNull GravityTrapBuild build){
			this.build = build;
		}
		
		@Override
		public float getX(){
			return build.x;
		}
		
		@Override
		public float getY(){
			return build.y;
		}
		
		@Override
		public void hitbox(Rect out){
			out.setSize(build.range() * 2).setCenter(build.x, build.y);
		}
	}
}
