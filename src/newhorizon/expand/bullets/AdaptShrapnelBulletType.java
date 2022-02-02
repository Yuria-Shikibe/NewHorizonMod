package newhorizon.expand.bullets;

import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import newhorizon.util.graphic.DrawFunc;

public class AdaptShrapnelBulletType extends ShrapnelBulletType{
	@Override
	public void draw(Bullet b){
		float realLength = b.fdata, rot = b.rotation();
		
		Draw.color(fromColor, toColor, b.fin());
		for(int i = 0; i < (int)(serrations * realLength / length); i++){
			Tmp.v1.trns(rot, i * serrationSpacing);
			float sl = Mathf.clamp(b.fout() - serrationFadeOffset) * (serrationSpaceOffset - i * serrationLenScl);
			Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, serrationWidth, sl, b.rotation() + 90);
			Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, serrationWidth, sl, b.rotation() - 90);
		}
		DrawFunc.tri(b.x, b.y, width * b.fout() / 1.75f, (realLength + 18), b.rotation());
		DrawFunc.tri(b.x, b.y, width * b.fout() / 1.75f, 10f, b.rotation() + 180f);
		Draw.reset();
		
		Drawf.light(b.team, b.x, b.y, b.x + Angles.trnsx(rot, realLength), b.y + Angles.trnsy(rot, realLength), width * 2.5f * b.fout(), toColor, lightOpacity);
	}
}
