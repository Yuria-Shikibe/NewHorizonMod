package newhorizon.block.drawer;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.content.Items;
import mindustry.graphics.Drawf;
import mindustry.type.Item;
import mindustry.ui.Cicon;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericCrafter.GenericCrafterBuild;
import mindustry.world.draw.DrawBlock;
import org.jetbrains.annotations.NotNull;

public class DrawPrinter extends DrawBlock {
	public DrawPrinter(@NotNull Item item){
		this.toPrint = item;
	}
	
	public DrawPrinter(){
		this.toPrint = Items.copper;
	}
	
	public Color printColor;
	public Color lightColor;
	public float moveLength = 8f;
	@NotNull public Item toPrint;
	public float time;
	public TextureRegion bottom, lightRegion;
	@Override
	public void draw(GenericCrafterBuild entity) {
		Draw.rect(bottom, entity.x, entity.y);
		Draw.color(printColor);
		Draw.alpha(entity.warmup);
		float sin = Mathf.sin(entity.totalProgress, time, moveLength);
		for (int i : Mathf.signs) {
			Lines.lineAngleCenter(entity.x + i * sin, entity.y, 90, 12);
			Lines.lineAngleCenter(entity.x, entity.y + i * sin, 0, 12);
		}
		Draw.reset();

		Draw.rect(entity.block.region, entity.x, entity.y);
		
		Draw.draw(Draw.z(), () -> Drawf.construct(entity.x, entity.y, toPrint.icon(Cicon.xlarge), lightColor, 0, entity.progress, ((GenericCrafter)entity.block()).craftTime / time, time));
		
		
		if (lightColor.a > 0.001f) {
			Draw.color(lightColor, entity.warmup);
			Draw.blend(Blending.additive);
			Draw.rect(lightRegion, entity.x, entity.y);
			Draw.blend();
			Draw.reset();
		}
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