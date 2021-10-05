package newhorizon.units;

import arc.graphics.Color;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import newhorizon.func.NHFunc;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class PhaseAbility extends Ability{
	public static Effect teleport = new Effect(45, 150, e -> {
		color(e.color, Color.white, e.fin() * 0.7f);
		Fill.circle(e.x, e.y, e.fout() * e.rotation * 1.25f);
		stroke(e.fout() * 3.2f);
		circle(e.x, e.y, e.fin() * e.rotation * 1.75f);
		stroke(e.fout() * 2.5f);
		circle(e.x, e.y, e.fin() * e.rotation * 1.5f);
		stroke(e.fout() * 3.2f);
		randLenVectors(e.id, (int)e.rotation, e.rotation / 4 + e.rotation * e.fin(), (x, y) -> {
			lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
		});
		color(Color.black);
		Fill.circle(e.x, e.y, e.fout() * e.rotation * 0.7f);
	});
	public static Effect teleportTrans = new Effect(45f, 300, e -> {
		if(!(e.data instanceof Vec2))return;
		Vec2 data = e.data();
		float angle = Angles.angle(e.x, e.y, data.x, data.y);
		float dst = Mathf.dst(e.x, e.y, data.x, data.y);
		Rand rand = new Rand(e.id);
		Tmp.v1.set(data).sub(e.x, e.y).nor().scl(Vars.tilesize * 3f);
		stroke(Mathf.curve(e.fout(), 0, 0.3f) * 1.75f);
		color(e.color, Color.white, e.fout() * 0.75f);
		for(int i = 1; i < dst / Vars.tilesize / 3f; i++){
			for(int j = 0; j < (int)e.rotation / 3; j++){
				Tmp.v4.trns(angle, rand.random(e.rotation / 4, e.rotation / 2), rand.range(e.rotation));
				Tmp.v3.set(Tmp.v2.set(Tmp.v1)).scl(i).add(Tmp.v2.scl(rand.range(0.5f))).add(Tmp.v4).add(e.x, e.y);
				lineAngle(Tmp.v3.x, Tmp.v3.y, angle - 180, e.fout(Interp.pow2Out) * 18 + 8);
			}
		}
	});
	
	public float reload = 300;
	public float teleportMinRange = 120f;
	public float teleportRange = 240f;
	
	public PhaseAbility(){
		
	}
	
	public PhaseAbility(float reload, float teleportMinRange, float teleportRange){
		this.reload = reload;
		this.teleportMinRange = teleportMinRange;
		this.teleportRange = teleportRange;
	}
	
	protected float reloadValue = 0;
	protected float lastHealth = 0;
	
	@Override
	public void update(Unit unit){
		for(WeaponMount wm : unit.mounts()){
			if(wm.bullet != null)return;
		}
		if(unit.controller() instanceof Player)return;
		reloadValue += Time.delta;
		
		Teamc target = Units.closestEnemy(unit.team, unit.x, unit.y, teleportRange * 2f, b -> true);
		int[] num = {0};
		float[] damage = {0};
		
		reloadValue += Math.max(lastHealth - unit.health, 0) / 2f;
		lastHealth = unit.health;
		
		Groups.bullet.intersect(unit.x - teleportRange, unit.y - teleportRange, teleportRange * 2f, teleportRange * 2f, bullet -> {
			if(bullet.team == unit.team)return;
			num[0]++;
			damage[0] += bullet.damage();
		});
		
		if(reloadValue > reload && (target != null || ((unit.hitTime > 0 || num[0] > 4 || damage[0] > reload / 2)))){
			float dst = target == null ? teleportRange + teleportMinRange : unit.dst(target) / 2f;
			float angle = target == null ? unit.rotation : unit.angleTo(target);
			Tmp.v1.set(unit);
			Tmp.v2.trns(angle + Mathf.sign(Mathf.randomSeedRange((long)Float.floatToIntBits(unit.lastX) + Float.floatToIntBits(unit.lastY) << 8, 1)) * 90,dst / 2f, Mathf.randomSeedRange((long)Float.floatToIntBits(unit.lastX) << 8 + Float.floatToIntBits(unit.lastY), 0.15f) * dst).clamp(teleportMinRange, teleportRange).add(Tmp.v1).clamp(0, 0, Vars.world.unitWidth(), Vars.world.unitHeight());
			Tmp.c1.set(unit.team.color);
			Sounds.plasmaboom.at(unit);
			Sounds.plasmaboom.at(Tmp.v2);
			
			teleport.at(unit.x, unit.y, unit.hitSize, Tmp.c1);
			NHFunc.teleportUnitNet(unit, Tmp.v2.x, Tmp.v2.y, unit.angleTo(Tmp.v2.x, Tmp.v2.y), null);
			reloadValue = 0;
			teleport.at(Tmp.v2.x, Tmp.v2.y, unit.hitSize, Tmp.c1);
			teleportTrans.at(Tmp.v1.x, Tmp.v1.y, unit.hitSize, Tmp.c1, new Vec2().set(Tmp.v2));
		}
	}
}
