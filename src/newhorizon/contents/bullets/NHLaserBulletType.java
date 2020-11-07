package newhorizon.contents.bullets;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.entities.*;
import mindustry.entities.bullet.LaserBulletType;

public class NHLaserBulletType extends LaserBulletType {
	public NHLaserBulletType(float damage){
		super(damage);
	}
	
	public NHLaserBulletType(){
        this(1f);
    }

	@Override
    public void draw(Bullet b){
		float realLength = b.fdata;

		float f = Mathf.curve(b.fin(), 0f, 0.2f);
		float baseLen = realLength * f;
		float cwidth = width;
		float compound = 1f;

		for(Color color : colors){
			Draw.color(color);
            Lines.stroke((cwidth *= lengthFalloff) * b.fout());
            Lines.lineAngle(b.x, b.y, b.rotation(), baseLen, false);
            Tmp.v1.trns(b.rotation(), baseLen);
            Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Lines.getStroke() * 1.22f, cwidth * 2f + width / 2f, b.rotation());

            Fill.circle(b.x, b.y, 1f * cwidth * b.fout());
            for(int i : Mathf.signs){
                Drawf.tri(b.x, b.y, sideWidth * b.fout() * cwidth, sideLength * compound, b.rotation() + sideAngle * i);
            }

            compound *= lengthFalloff;
        }
        Draw.reset();

        Tmp.v1.trns(b.rotation(), baseLen * 1.1f);
        Drawf.light(b.team, b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.4f * b.fout(), colors[0], 0.6f);
    }
}














