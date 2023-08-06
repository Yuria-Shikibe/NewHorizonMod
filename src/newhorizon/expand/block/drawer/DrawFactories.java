package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawDefault;


public class DrawFactories extends DrawDefault{
	public TextureRegion rotator, rotator2, bottom, liquid, pressor, top;
	public Color liquidColor;
	public float drawRotator;
	public float drawRotator2;
	public float[] pressorSet = new float[]{1f};
	public boolean drawTop;
	
	@Override
	public void draw(Building entity){
		if(liquidColor.a > 0.001f){
			Draw.color(liquidColor);
			Draw.alpha(entity.liquids.currentAmount() / entity.block.liquidCapacity);
			Draw.rect(liquid, entity.x, entity.y);
			Draw.reset();
		}
		if(drawRotator != 0)Draw.rect(rotator, entity.x, entity.y, drawRotator * entity.totalProgress());
		if(drawRotator2 != 0)Draw.rect(rotator2, entity.x, entity.y, drawRotator2 * entity.totalProgress());
		
		if(pressorSet.length == 4){
			for(int arm = 0; arm < 4; arm ++){
				int offest = arm - 1;
				Vec2 armVec = new Vec2();
				armVec.trns(pressorSet[2] + 90 * offest, Mathf.absin(entity.totalProgress(), pressorSet[0], pressorSet[1] * entity.warmup()) );
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
	    Seq<TextureRegion> seq = new Seq<>(TextureRegion.class);
	    seq.add(bottom);
//	    if(rotator != null && rotator.found())seq.add(rotator);
//	    if(rotator2 != null && rotator2.found())seq.add(rotator2);
	    seq.add(block.region);
//	    if(top != null && top.found())seq.add(top);
        return seq.shrink();
    }
}









