package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHContent;

import static mindustry.Vars.world;

public class ArmorFloor extends Floor{
	public Floor solidReact;
	
	public TextureRegion large;
	public TextureRegion[][] split;
	
	public ArmorFloor(String name, int variants, Floor solidReact){
		super(name, variants);
		this.solidReact = solidReact;
		
		cacheLayer = NHContent.armorLayer;
		oreDefault = false;
		needsSurface = false;
	}

	public ArmorFloor(String name, int variants){
		this(name, variants, null);
	}
	
	protected boolean doEdge(Tile tile, Tile otherTile, Floor other){
		return (solidReact == null || other.blendGroup != solidReact) && (other.realBlendId(otherTile) > realBlendId(tile) || edges() == null);
	}
	
	@Override
	public void init(){
		if(solidReact != null)blendGroup = solidReact;
		
		super.init();
	}
	
	@Override
	public void drawBase(Tile tile){
		Mathf.rand.setSeed(tile.pos());
		Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx(), tile.worldy());
		
		int rx = tile.x / 2 * 2;
		int ry = tile.y / 2 * 2;
		
		if(Core.atlas.isFound(large) && eq(rx, ry) && Mathf.randomSeed(Point2.pack(rx, ry)) < 0.5){
			Draw.rect(split[tile.x % 2][1 - tile.y % 2], tile.worldx(), tile.worldy());
		}else{
			Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.worldx(), tile.worldy());
		}
		
		Draw.alpha(1f);
		drawEdges(tile);
		drawOverlay(tile);
	}
	
	@Override
	public void load(){
		super.load();
		large = Core.atlas.find(name + "-large");
		split = large.split(32, 32);
	}
	
	boolean eq(int rx, int ry){
		return rx < world.width() - 1 && ry < world.height() - 1
				&& world.tile(rx + 1, ry).floor() == this
				&& world.tile(rx, ry + 1).floor() == this
				&& world.tile(rx, ry).floor() == this
				&& world.tile(rx + 1, ry + 1).floor() == this;
	}
}
