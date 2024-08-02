package newhorizon.expand.units.ablility;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;

/**
 * Copy From {@link ShieldArcAbility}
 * 
 * */
public class TurretShield extends Ability{
	private static float paramRot;
	private static Unit paramUnit;
	private static TurretShield paramField;
	private static final Vec2 paramPos = new Vec2();
	private static final Cons<Bullet> shieldConsumer = b -> {
		if(b.team != paramUnit.team && b.type.absorbable && paramField.data > 0 &&
				(paramPos.within(b, paramField.radius + paramField.width/2f) ||
				Tmp.v1.set(b).add(b.vel).within(paramPos, paramField.radius + paramField.width/2f)) &&
				Angles.within(paramPos.angleTo(b), paramRot + paramField.angleOffset, paramField.angle / 2f)){
			
			b.absorb();
			Fx.absorb.at(b);
			
			//break riftShield
			if(paramField.data <= b.damage()){
				paramField.data -= paramField.cooldown * paramField.regen;
				
				//TODO fx
			}
			
			paramField.data -= b.damage();
			paramField.alpha = 1f;
		}
	};
	
	public int weaponIndex = 0;
	/** Shield radius. */
	public float radius = 60f;
	/** Shield regen speed in damage/tick. */
	public float regen = 0.1f;
	/** Maximum riftShield. */
	public float max = 200f;
	/** Cooldown after the riftShield is broken, in ticks. */
	public float cooldown = 60f * 5;
	/** Angle of riftShield arc. */
	public float angle = 80f;
	/** Offset parameters for riftShield. */
	public float angleOffset = 0f, x = 0f, y = 0f;
	/** If true, only activates when shooting. */
	public boolean whenShooting = true;
	/** Width of riftShield line. */
	public float width = 6f, drawWidth;
	
	/** Whether to draw the arc line. */
	public boolean drawArc = true;
	/** If not null, will be drawn on top. */
	public @Nullable
	String region;
	/** If true, sprite position will be influenced by x/y. */
	public boolean offsetRegion = false;
	
	/** State. */
	protected float widthScale, alpha;
	protected WeaponMount turret;
	
	@Override
	public void update(Unit unit){
		WeaponMount mount = unit.mounts[weaponIndex];
		
		if(data < max){
			data += Time.delta * regen;
		}
		
		boolean active = data > 0 && (unit.isShooting || !whenShooting);
		alpha = Math.max(alpha - Time.delta/10f, 0f);
		
		if(active){
			widthScale = Mathf.lerpDelta(widthScale, 1f, 0.06f);
			paramRot = mount.rotation + unit.rotation;
			paramUnit = unit;
			paramField = this;
			paramPos.set(x, y).rotate(mount.rotation + unit.rotation).add(unit);
			
			Groups.bullet.intersect(unit.x - radius, unit.y - radius, radius * 2f, radius * 2f, shieldConsumer);
		}else{
			widthScale = Mathf.lerpDelta(widthScale, 0f, 0.11f);
		}
	}
	
	@Override
	public void init(UnitType type){
		data = max;
		if(weaponIndex - 1> type.weapons.size)throw new ArrayIndexOutOfBoundsException("No so many weapons");
	}
	
	@Override
	public void draw(Unit unit){
		if(widthScale > 0.001f){
			WeaponMount mount = unit.mounts[weaponIndex];
			
			Draw.z(Layer.shields);
			
			Draw.color(unit.team.color, Color.white, Mathf.clamp(alpha));
			Vec2 pos = paramPos.set(x, y).rotate(mount.rotation + unit.rotation).add(unit);
			
			if(!Vars.renderer.animateShields){
				Draw.alpha(0.4f);
			}
			
			if(region != null){
				Vec2 rp = offsetRegion ? pos : Tmp.v1.set(unit);
				Draw.yscl = widthScale;
				Draw.rect(region, rp.x, rp.y, mount.rotation + unit.rotation);
				Draw.yscl = 1f;
			}
			
			if(drawArc){
				Lines.stroke(drawWidth * widthScale);
				Lines.arc(pos.x, pos.y, radius + Math.max(0, (width - drawWidth) / 2f), angle / 360f, mount.rotation + unit.rotation + angleOffset - angle / 2f);
			}
			Draw.reset();
		}
	}
	
	@Override
	public void displayBars(Unit unit, Table bars){
		bars.add(new Bar("stat.shieldhealth", Pal.accent, () -> data / max)).row();
	}
}
