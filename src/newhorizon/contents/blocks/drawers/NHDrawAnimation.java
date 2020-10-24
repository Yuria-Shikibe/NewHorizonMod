package newhorizon.contents.blocks.drawers;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.world.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import mindustry.world.draw.*;

public class NHDrawAnimation extends DrawAnimation{
	public Color liquidColor;
    @Override
    public void draw(GenericCrafterBuild entity){
		Draw.rect(entity.block.region, entity.x, entity.y);
		Draw.color(Color.clear, liquidColor, entity.liquids.total() / entity.block.liquidCapacity);
		Draw.rect(liquid, entity.x, entity.y);
		Draw.color();
		Draw.rect(top, entity.x, entity.y);
		Draw.rect(
            sine ?
				frames[(int)Mathf.absin(entity.totalProgress, frameSpeed, frameCount - 0.001f)] :
				frames[(int)((entity.totalProgress / frameSpeed) % frameCount)],
		entity.x, entity.y);
	}
}