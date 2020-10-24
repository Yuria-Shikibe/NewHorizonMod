package newhorizon.contents.blocks.drawers;

import arc.*;
import arc.math.geom.*;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ctype.*;
import mindustry.content.*;

import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class DrawPrinter extends DrawBlock {
	public Color printColor;
	public Color lightColor;
	public float moveLength = 8f;
	//public Item toPrint;
	public float time;
	public TextureRegion bottom, toPrintObj, lightRegion;
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


		/*
		Shaders.build.region = toPrintObj;
		Shaders.build.progress = entity.progress / 1;
		Shaders.build.color.a = entity.warmup;
		Shaders.build.color.set(lightColor);
		Shaders.build.time = 12f;

		Draw.shader(Shaders.build);
		Draw.rect(Core.atlas.find(toPrint.name), entity.x, entity.y);
		Draw.shader();
		*/
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