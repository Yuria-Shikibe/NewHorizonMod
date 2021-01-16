package newhorizon.block.drawer;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;


public class DrawFactories extends DrawBlock{
	public TextureRegion rotator, rotator2, bottom, liquid, pressor, top;
	public Color liquidColor;
	public float drawRotator;
	public float drawRotator2;
	public float[] pressorSet = new float[]{1f};
	public boolean drawTop;
	
	@Override
	public void draw(GenericCrafter.GenericCrafterBuild entity){
		Draw.rect(bottom, entity.x, entity.y);
		
		if(liquidColor.a > 0.001f){
			Draw.color(liquidColor);
			Draw.alpha(entity.liquids.total() / entity.block.liquidCapacity);
			Draw.rect(liquid, entity.x, entity.y);
			Draw.reset();
		}
		if(drawRotator != 0)Draw.rect(rotator, entity.x, entity.y, drawRotator * entity.totalProgress);
		if(drawRotator2 != 0)Draw.rect(rotator2, entity.x, entity.y, drawRotator2 * entity.totalProgress);
		
		if(pressorSet.length == 4){
			for(int arm = 0; arm < 4; arm ++){
				int offest = arm - 1;
				Vec2 armVec = new Vec2();
				armVec.trns(pressorSet[2] + 90 * offest, Mathf.absin(entity.totalProgress, pressorSet[0], pressorSet[1] * entity.warmup) );
				Draw.rect(pressor, entity.x + armVec.x, entity.y + armVec.y, 90 * offest + pressorSet[3]);
			}
		}
		
        Draw.rect(entity.block.region, entity.x, entity.y);
		if(drawTop)Draw.rect(top, entity.x, entity.y);
    }
	
    @Override
    public void load(Block block){
        rotator = Core.atlas.find(block.name + "-rotator");
        rotator2 = Core.atlas.find(block.name + "-rotator2");
        bottom = Core.atlas.find(block.name + "-bottom");
        liquid = Core.atlas.find(block.name + "-liquid");
        pressor = Core.atlas.find(block.name + "-pressor");
        top = Core.atlas.find(block.name + "-top");
    }

    @Override
    public TextureRegion[] icons(Block block){
        return new TextureRegion[]{bottom, block.region}; 
    }
}









