package newhorizon.expand.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Flyingc;
import mindustry.gen.Physicsc;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Layer;
import newhorizon.content.NHFx;

public class EnergyUnitII extends UnitEntity{
	protected Seq<Clamper> clamps = new Seq<>(12);

	@Override
	public void add(){
		super.add();

		for(int i = 0; i < 12; i++){
			clamps.add(new Clamper());
		}
	}
	
	@Override
	public void draw(){
		for(StatusEntry e : this.statuses){
			e.effect.draw(this, e.time);
		}
		
		Draw.color(Color.black);
		Draw.z(Layer.bullet - 1);
		Fill.circle(x, y, hitSize);
		Draw.z(Layer.effect - 5);
		
		Fill.circle(x, y, hitSize);
		Lines.stroke(12, team.color);
		Lines.circle(x, y, hitSize);
		
		Lines.stroke(3, team.color);
		clamps.each(Clamper::draw);
	}
	
	@Override
	public void update(){
		super.update();
		
		clamps.each(c -> {
			c.target = Units.bestEnemy(team, x, y, 800, Flyingc::isFlying, UnitSorts.strongest);
			c.update();
		});
	}
	
	public class Clamper{
		public Physicsc target;
		public float Cx, Cy;
		public float strength;
		
		public void draw(){
			Tmp.v1.set(Cx, Cy).sub(x, y).scl(0.33f);
			float ang = Tmp.v1.angle();
			float len = Mathf.dst(Cx, Cy, x, y);
			Tmp.v2.trns(ang + 90,strength * len / 4).add(Tmp.v1);
			Tmp.v3.trns(ang + 90,strength * len / 8).add(Tmp.v1.scl(2));
			Lines.curve(x, y, Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y, Cx, Cy, (int)(len / 10));
		}
		
		public void update(){
			if(Mathf.chance(0.1)){
				NHFx.hitSpark.at(Cx, Cy, team.color);
			}
			
			if(target != null){
				Cx = target.getX();
				Cy = target.getY();
				
				Tmp.v1.set(Cx, Cy).sub(x, y).setLength(strength);
				
				target.impulse(Tmp.v1);
			}else{
				Cx = Mathf.lerpDelta(Cx, x, 0.0075f);
				Cy = Mathf.lerpDelta(Cy, x, 0.0075f);
			}
		}
	}
}
