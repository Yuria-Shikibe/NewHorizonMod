package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.graphics.Drawf;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHContent;

import static mindustry.Vars.world;

public class ArmorFloor extends Floor{
	public Floor solidReact;
	public boolean useDynamicLight = false;
	public Color[] colors;
	
	public TextureRegion large;
	public TextureRegion[][] split;
	
	protected static final int scanStep = 5;
	protected static final Color tmpColor = new Color();
	protected static final Seq<Color> collectedColors = new Seq<>();
	
	public ArmorFloor(String name, int variants, Floor solidReact){
		super(name, variants);
		this.solidReact = solidReact;
		
		cacheLayer = NHContent.armorLayer;
		oreDefault = false;
		needsSurface = false;
	}
	
	public ArmorFloor(String name){
		this(name, 0, null);
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
		
		if(drawLiquidLight && !Vars.headless){
			colors = new Color[variants];
			
			for(int i = 0; i < variants; i++){
				collectedColors.clear();
				
				PixmapRegion image = Core.atlas.getPixmap(variantRegions[i]);
				for(int x = 1; x < image.width; x += scanStep){
					for(int y = 1; y < image.height; y += scanStep){
						tmpColor.set(image.get(x, y));
						float bright = 0.2126f * tmpColor.r + 0.7152f * tmpColor.g + 0.0722f * tmpColor.b;
						if(bright < 0.6f)continue;
						collectedColors.add(tmpColor.cpy().a(bright));
					}
				}
				
				colors[i] = new Color(
						collectedColors.sumf(c -> c.r),
						collectedColors.sumf(c -> c.g),
						collectedColors.sumf(c -> c.b),
						collectedColors.sumf(c -> Mathf.curve(c.a, 0.2f, 1.23f))
				);
			}
		}
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
	public void drawEnvironmentLight(Tile tile){
		if(!useDynamicLight)super.drawEnvironmentLight(tile);
		else{
			Color color = lightColor(tile);
			Drawf.light(tile.worldx(), tile.worldy(), lightRadius, color, color.a);
		}
	}
	
	public Color lightColor(Tile tile){
		return colors[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))];
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
