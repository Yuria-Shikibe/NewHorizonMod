package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;

public class DrawPressor extends DrawBlock{
	public TextureRegion pressor;
	
	public float initAngleDst = 0;
	public float moveScl = 10;
	public float moveDst = 3.8f;
	public float spriteRot = 0;
	
	@Override
	public void draw(Building entity){
		for(int arm = 0; arm < 4; arm ++){
			int offest = arm - 1;
			Vec2 armVec = new Vec2();
			armVec.trns(initAngleDst + 90 * offest, Mathf.absin(entity.totalProgress(), moveScl, moveDst * entity.warmup()) );
			Draw.rect(pressor, entity.x + armVec.x, entity.y + armVec.y, 90 * offest + spriteRot);
		}
	}
	
	
	
	@Override
	public void load(Block block){
		pressor = Core.atlas.find(block.name + "-pressor");
	}
	
}
