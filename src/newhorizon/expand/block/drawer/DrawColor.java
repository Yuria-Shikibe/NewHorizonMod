package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Log;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawColor extends DrawBlock{
	public Color color = Color.valueOf("4d4e58");
	
	protected TextureAtlas.AtlasRegion textureRegion;
	
	@Override
	public void draw(Building build){
		Draw.color(color);
		Fill.rect(build.x, build.y, build.block.size * Vars.tilesize, build.block.size * Vars.tilesize);
	}
	
	@Override
	public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
		Draw.color(color);
		Fill.rect(plan.drawx(), plan.drawy(), block.size * Vars.tilesize, block.size * Vars.tilesize);
	}
	
	@Override
	public TextureRegion[] icons(Block block){
		return new TextureRegion[]{textureRegion};
	}
	
	@Override
	public void load(Block block){
		Pixmap pixmap = new Pixmap(block.size * 4 * Vars.tilesize, block.size * 4 * Vars.tilesize);
		pixmap.fill(color);
		
		Log.info(pixmap);
		
		textureRegion = new TextureAtlas.AtlasRegion(new TextureRegion(new Texture(pixmap)));
		textureRegion.name = block.name + "-draw-" + color.toString();
		
		Core.atlas.addRegion(textureRegion.name, textureRegion);
	}
}
