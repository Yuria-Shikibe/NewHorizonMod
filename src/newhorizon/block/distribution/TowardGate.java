package newhorizon.block.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Router;
import mindustry.world.meta.BlockGroup;

public class TowardGate extends Router{
	public TextureRegion topRegion;
	
	public TowardGate(String name){
		super(name);
		rotate = true;
		group = BlockGroup.transportation;
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
		
		public Building getTileTarget(Item item, Tile from, boolean set){
			if(unit != null && isControlled()){
				unit.health(health);
				unit.ammo(unit.type().ammoCapacity * (items.total() > 0 ? 1f : 0f));
				unit.team(team);
				unit.set(x, y);
				
				int angle = Mathf.mod((int)((angleTo(unit.aimX(), unit.aimY()) + 45) / 90), 4);
				
				if(unit.isShooting()){
					Building other = nearby(angle);
					if(other != null && other.acceptItem(this, item)){
						return other;
					}
				}
				
				return null;
			}
			
			int counter = primaryRot;
			for(int i = 0; i < proximity.size; i++){
				Building other = proximity.get((i + counter) % proximity.size);
				if(set) primaryRot = ((byte)((primaryRot + 1) % proximity.size));
				if(other.tile == from && from.block() == Blocks.overflowGate) continue;
				if(other.acceptItem(this, item)){
					return other;
				}
			}
			return null;
		}
		
		
		public boolean acceptItem(Building source, Item item) {
			return this.team == source.team && source == back() && this.lastItem == null && this.items.total() == 0;
		}
		
		@Override
		public void draw(){
			Draw.rect(region, x, y);
			Draw.rect(topRegion, x, y, 90 * rotation);
			drawTeamTop();
		}
		
		@Override
		public void drawSelect(){
			Draw.color(Pal.accent);
			Lines.stroke(1.0F);
			Lines.square(this.x, this.y, size * Vars.tilesize / 2.0F + 1.0F);
			for(int i = -1; i <= 1; i++){
				Draw.rect("bridge-arrow", Geometry.d4(rotation + i).x * Vars.tilesize + x, Geometry.d4(rotation + i).y * Vars.tilesize + y, 90 * (Mathf.mod(rotation + i, 4)));
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
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
		}
	}
}
