package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.draw.DrawDefault;

public class DrawPrinter extends DrawDefault{
	public DrawPrinter(Item item){
		this.toPrint = item;
	}
	
	public DrawPrinter(){
		this.toPrint = Items.copper;
	}
	
	public Color printColor;
	public Color lightColor;
	public float moveLength = 8f;
	public Item toPrint;
	public float time;
	public TextureRegion bottom, lightRegion;
	
	public void draw(Building entity) {
		Draw.rect(bottom, entity.x, entity.y);
		Draw.color(printColor);
		Draw.alpha(entity.warmup());
		float sin = Mathf.sin(entity.totalProgress(), time, moveLength);
		for (int i : Mathf.signs) {
			Lines.lineAngleCenter(entity.x + i * sin, entity.y, 90, 12);
			Lines.lineAngleCenter(entity.x, entity.y + i * sin, 0, 12);
		}
		Draw.reset();

		Draw.rect(entity.block.region, entity.x, entity.y);
		
		Draw.draw(Layer.blockOver, () -> {
			Drawf.construct(entity.x, entity.y, toPrint.fullIcon, printColor, 0, entity.progress(), entity.progress(), entity.totalProgress() * 3f);
		});
		
		if (lightColor.a > 0.001f) {
			Draw.color(lightColor, entity.warmup());
			Draw.blend(Blending.additive);
			Draw.alpha(entity.warmup() * 0.85f);
			Draw.rect(lightRegion, entity.x, entity.y);
			Draw.blend();
			Draw.reset();
		}
	}
	
	@Override
	public void drawLight(Building build){
		Drawf.light(build.x, build.y, build.warmup() * build.block.size * Vars.tilesize, lightColor, 0.7f);
	}
	
	@Override
	public void load(Block block) {
		bottom = Core.atlas.find(block.name + "-bottom");
		lightRegion = Core.atlas.find(block.name + "-light");
		//toPrintObj = Core.atlas.find(toPrint.name);
	}

	@Override
	public TextureRegion[] icons(Block block) {
		return new TextureRegion[] {bottom, block.region};
	}

}