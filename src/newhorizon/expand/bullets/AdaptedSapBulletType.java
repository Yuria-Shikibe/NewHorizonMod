package newhorizon.expand.bullets;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Position;
import arc.util.Tmp;
import mindustry.entities.bullet.SapBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import newhorizon.util.graphic.DrawFunc;

public class AdaptedSapBulletType extends SapBulletType{
	protected TextureRegion laser, laserEnd;
	public boolean moveTarget = false;
	
	@Override
	public void load(){
		super.load();
		
		laser = Core.atlas.find("laser-white");
		laserEnd = Core.atlas.find("laser-white-end");
	}
	
	@Override
	public void draw(Bullet b){
		if(b.data instanceof Position){
			Position data = (Position)b.data();
			if (moveTarget){
				
			}else {
				Tmp.v1.set(data).lerp(b, b.fin());
			}
			
			Draw.color(color);
			DrawFunc.laser(laser, laserEnd,
					b.x, b.y, Tmp.v1.x, Tmp.v1.y, width * b.fout());
			
			Draw.reset();
			
			Drawf.light(b.x, b.y, Tmp.v1.x, Tmp.v1.y, 15f * b.fout(), lightColor, lightOpacity);
		}
	}
}
