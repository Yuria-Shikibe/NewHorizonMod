package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NHGroups;
import newhorizon.content.NHContent;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class GravityWell extends Block{
	private static Tile tmpTile;
	
	public int range = 35;
	
	public GravityWell(String name){
		super(name);
		solid = true;
		configurable = true;
		update = true;
		hasPower = true;
		canOverdrive = false;
		sync = true;
		noUpdateDisabled = true;
		
		replaceable = true;
		group = BlockGroup.projectors;
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.range, range, StatUnit.blocks);
		stats.add(Stat.output, (t) -> {
			t.row().left();
			t.add("").row();
			t.table(i -> {
				i.image().size(LEN).color(Pal.lancerLaser).left();
				i.add(Core.bundle.get("mod.ui.gravity-trap-field-friendly")).growX().padLeft(OFFSET / 2).row();
			}).padTop(OFFSET).growX().fillY().row();
			t.table(i -> {
				i.image().size(LEN).color(Pal.redderDust).left();
				i.add(Core.bundle.get("mod.ui.gravity-trap-field-hostile")).growX().padLeft(OFFSET / 2).row();
			}).padTop(OFFSET).growX().fillY().row();
		});
		stats.add(Stat.abilities, t -> {
			t.table(table -> {
				table.left();
				table.defaults().fill().pad(OFFSET / 3).left();
				table.add("- " + Core.bundle.get("mod.ui.gravity-trap.ability-1")).row();
				table.add("- " + Core.bundle.get("mod.ui.gravity-trap.ability-2")).row();
				table.add("- " + Core.bundle.get("mod.ui.gravity-trap.ability-3")).row();
			}).fill();
		});
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		
		Lines.stroke(3, Pal.gray);
		Lines.square(x * tilesize + offset, y * tilesize + offset, range * tilesize + 1);
		
		Lines.stroke(1, Vars.player.team().color);
		Lines.square(x * tilesize + offset, y * tilesize + offset, range * tilesize);
	}
	
	public class GravityTrapBuild extends Building implements Ranged{
		public float warmup;
		public transient GravityTrapField field;
		
		@Override
		public float warmup(){
			return warmup;
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
		public void updateTile(){
			if(efficiency() > 0){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
			
			if(field != null)field.setPosition(self());
		}
		
		@Override
		public void afterPickedUp(){
			super.afterPickedUp();
			warmup = 0;
			NHGroups.gravityTraps.remove(field);
		}
		
		@Override
		public boolean canPickup(){
			return false;
		}
		
		@Override
		public void drawConfigure(){
			Lines.stroke(3, Pal.gray);
			Lines.square(x, y, range() + 1);
			
			Lines.stroke(1, team.color);
			Lines.square(x, y, range());
		}
		
		public boolean active(){
			return warmup() > 0.75f;
		}
		
		@Override
		public void remove(){
			if(added)NHGroups.gravityTraps.remove(field);
			
			super.remove();
		}
		
		@Override
		public void draw(){
			super.draw();
			
			Draw.reset();
			float sin = Mathf.absin(Time.time, 8f, size / 2f);
			
			Draw.z(Layer.bullet + 1f);
			Draw.color(team.color);
			float length = tilesize * size / 4f + sin;
			
			TextureRegion region = NHContent.linkArrow;
			for(int i = 0; i < 4; i++){
				Tmp.v1.trns(i * 90, -length);
				Draw.rect(region, x + Tmp.v1.x, y + Tmp.v1.y, region.width * warmup * Draw.scl, region.height * warmup * Draw.scl, i * 90);
			}
			
			Lines.stroke(warmup * (1 + sin / 2f));
			Lines.spikes(x, y, sin * 4 + size, warmup * (tilesize + sin), 4, 45 + DrawFunc.rotator_90());
			
			Draw.reset();
			
			
		}
		
		@Override
		public float range(){
			return range * tilesize;
		}
		
		@Override
		public void add(){
			if(added)return;
			
			Groups.all.add(this);
			Groups.build.add(this);
			this.added = true;
			
			if(field == null)field = new GravityTrapField(this);
			
			field.add();
		}
	}
	
}
