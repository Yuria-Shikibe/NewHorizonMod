package newhorizon.block.special;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.blocks.distribution.Router;

public class TowardGate extends Router{
	public TextureRegion topRegion;
	
	public TowardGate(String name){
		super(name);
		rotate = true;
	}
	
	@Override
	public void load(){
		super.load();
		topRegion = Core.atlas.find(name + "-top");
	}
	
	@Override
	protected TextureRegion[] icons(){
		return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{this.region, topRegion, this.teamRegions[Team.sharded.id]} : new TextureRegion[]{this.region, topRegion};
	}
	
	public class TowardGateBuild extends RouterBuild{
		int primaryRot = 0;
		
		public boolean acceptItem(Building source, Item item) {
			return this.team == source.team && source == back() && this.lastItem == null && this.items.total() == 0;
		}
		
		@Override
		public Building back(){
			int trns = size / 2 + 1;
			return this.nearby(Geometry.d4(primaryRot + 2).x * trns, Geometry.d4(primaryRot + 2).y * trns);
		}
		
		@Override
		public void draw(){
			Draw.rect(region, x, y);
			Draw.rect(topRegion, x, y, 90 * primaryRot);
			drawTeamTop();
		}
		
		@Override
		public void drawSelect(){
			Draw.color(Pal.accent);
			Lines.stroke(1.0F);
			Lines.square(this.x, this.y, size * Vars.tilesize / 2.0F + 1.0F);
			for(int i = -1; i <= 1; i++){
				Draw.rect("bridge-arrow", Geometry.d4(primaryRot + i).x * Vars.tilesize + x, Geometry.d4(primaryRot + i).y * Vars.tilesize + y, 90 * (Mathf.mod(primaryRot + i, 4)));
			}
			Draw.reset();
		}
		
		@Override
		public void placed(){
			super.placed();
			primaryRot = rotation;
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(primaryRot);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			primaryRot = read.i();
		}
		
		@Override
		public Integer config(){
			return primaryRot;
		}
	}
}
