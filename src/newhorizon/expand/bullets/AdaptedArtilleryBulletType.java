package newhorizon.expand.bullets;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.Bullet;

public class AdaptedArtilleryBulletType extends ArtilleryBulletType{
	public AdaptedArtilleryBulletType(float speed, float damage, String bulletSprite){
		super(speed, damage, bulletSprite);
	}
	
	public AdaptedArtilleryBulletType(float speed, float damage){
		this(speed, damage, "shell");
	}
	
	public AdaptedArtilleryBulletType(){
		this(1f, 1f, "shell");
	}
	
	@Override
	public void draw(Bullet b){
		drawTrail(b);
		float baseScale = 0.7f;
		float scale = (baseScale + b.fslope() * (1f - baseScale));
		float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);
		float height = this.height * ((1f - shrinkY) + shrinkY * b.fout());
		
		Draw.color(backColor);
		Draw.rect(backRegion, b.x, b.y, width * scale, height * scale, b.rotation() - 90 + offset);
		Draw.color(frontColor);
		Draw.rect(frontRegion, b.x, b.y, width * scale, height * scale, b.rotation() - 90 + offset);
		Draw.color();
	}
}
