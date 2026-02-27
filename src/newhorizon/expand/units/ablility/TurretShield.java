package newhorizon.expand.units.ablility;

import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;

/**
 * Copy From {@link ShieldArcAbility}
 */
public class TurretShield extends Ability {
    private static Unit paramUnit;
    private static TurretShield paramField;
    private static final Vec2 paramPos = new Vec2();
    private static float paramRot;
    private static final Cons<Bullet> shieldConsumer = b -> {
        if (b.team != paramUnit.team && b.type.absorbable && paramField.data > 0 &&
                (paramPos.within(b, paramField.radius + paramField.width / 2f) ||
                        Tmp.v1.set(b).add(b.vel).within(paramPos, paramField.radius + paramField.width / 2f)) &&
                Angles.within(paramPos.angleTo(b), paramRot + paramField.angleOffset, paramField.angle / 2f)) {

            if(paramField.chanceDeflect > 0f && b.vel.len() >= 0.1f && b.type.reflectable && Mathf.chance(paramField.chanceDeflect)){

                //make sound
                paramField.deflectSound.at(paramPos, Mathf.random(0.9f, 1.1f));

                //translate bullet back to where it was upon collision
                b.trns(-b.vel.x, -b.vel.y);

                float penX = Math.abs(paramPos.x - b.x), penY = Math.abs(paramPos.y - b.y);

                if(penX > penY){
                    b.vel.x *= -1;
                }else{
                    b.vel.y *= -1;
                }

                b.owner = paramUnit;
                b.team = paramUnit.team;
                b.time += 1f;

            }else{
                b.absorb();
                Fx.absorb.at(b);
            }
            
            //break shield
            if(paramField.data <= b.damage()){
                paramField.data -= paramField.cooldown * paramField.regen;

                Fx.arcShieldBreak.at(paramPos.x, paramPos.y, 0, paramField.color == null ? paramUnit.type.shieldColor(paramUnit) : paramField.color, paramUnit);
            }

            //shieldDamage for consistency
            paramField.data -= b.type.shieldDamage(b);
            paramField.alpha = 1f;
        }
    };

    protected static final Cons<Unit> unitConsumer = unit -> {
        // ignore core units
        if(paramField.data > 0 && unit.targetable(paramUnit.team) &&
            !(unit.within(paramPos, paramField.radius - paramField.width) && paramPos.within(unit.x - unit.deltaX, unit.y - unit.deltaY, paramField.radius - paramField.width)) &&
            (Tmp.v1.set(unit).add(unit.deltaX, unit.deltaY).within(paramPos, paramField.radius + paramField.width) || unit.within(paramPos, paramField.radius + paramField.width)) &&
            (Angles.within(paramPos.angleTo(unit), paramRot + paramField.angleOffset, paramField.angle / 2f) || Angles.within(paramPos.angleTo(unit.x + unit.deltaX, unit.y + unit.deltaY), paramRot + paramField.angleOffset, paramField.angle / 2f))){
                
            if(unit.isMissile() && unit.killable() && paramField.missileUnitMultiplier >= 0f){

                unit.remove();
                unit.type.deathSound.at(unit);
                unit.type.deathExplosionEffect.at(unit);
                Fx.absorb.at(unit);
                Fx.circleColorSpark.at(unit.x, unit.y,paramUnit.team.color);
                
                // consider missile hp and gamerule to damage the shield
                paramField.data -= unit.health() * paramField.missileUnitMultiplier * Vars.state.rules.unitDamage(unit.team);
                paramField.alpha = 1f;

            }else{

                float reach = paramField.radius + paramField.width;
                float overlapDst = reach - unit.dst(paramPos.x,paramPos.y);

                if(overlapDst>0){
                    //stop
                    unit.vel.setZero();
                    // get out
                    unit.move(Tmp.v1.set(unit).sub(paramUnit).setLength(overlapDst + 0.01f));

                    if(Mathf.chanceDelta(0.5f*Time.delta)){
                        Fx.circleColorSpark.at(unit.x,unit.y,paramUnit.team.color);
                    }
                }
            }
        }
    };

    public int weaponIndex = 0;
    /**
     * Shield radius.
     */
    public float radius = 60f;
    /**
     * Shield regen speed in damage/tick.
     */
    public float regen = 0.1f;
    /**
     * Maximum shield.
     */
    public float max = 200f;
    /**
     * Cooldown after the shield is broken, in ticks.
     */
    public float cooldown = 60f * 5;
    /**
     * Angle of shield arc.
     */
    public float angle = 80f;
    /**
     * Offset parameters for shield.
     */
    public float angleOffset = 0f, x = 0f, y = 0f;
    /**
     * If true, only activates when shooting.
     */
    public boolean whenShooting = true;
    /**
     * Width of shield line.
     */
    public float width = 6f, drawWidth;
    /**
     * Bullet deflection chance. -1 to disable
     */
    public float chanceDeflect = -1f;
    /**
     * Deflection sound.
     */
    public Sound deflectSound = Sounds.none;
    /**
     * Multiplier for shield damage taken from missile units.
     */
    public float missileUnitMultiplier = 2f;

    /**
     * Whether to draw the arc line.
     */
    public boolean drawArc = true;
    /**
     * If not null, will be drawn on top.
     */
    public @Nullable
    String region;
    /**
     * Color override of the shield. Uses unit shield colour by default.
     */
    public @Nullable
    Color color;
    /**
     * If true, sprite position will be influenced by x/y.
     */
    public boolean offsetRegion = false;

    /**
     * State.
     */
    protected float widthScale, alpha;
    protected WeaponMount turret;

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("shield", Strings.autoFixed(max, 2)));
        t.row();
        t.add(abilityStat("repairspeed", Strings.autoFixed(regen * 60f, 2)));
        t.row();
        t.add(abilityStat("cooldown", Strings.autoFixed(cooldown / 60f, 2)));
        t.row();
        t.add(abilityStat("deflectchance", Strings.autoFixed(chanceDeflect *100f, 2)));
    }

    @Override
    public void update(Unit unit) {
        WeaponMount mount = unit.mounts[weaponIndex];

        if (data < max) {
            data += Time.delta * regen;
        }

        boolean active = data > 0 && (unit.isShooting || !whenShooting);
        alpha = Math.max(alpha - Time.delta / 10f, 0f);

        if (active) {
            widthScale = Mathf.lerpDelta(widthScale, 1f, 0.06f);
            paramRot = mount.rotation + unit.rotation;
            paramUnit = unit;
            paramField = this;
            paramPos.set(x, y).rotate(mount.rotation + unit.rotation).add(unit);

            float reach = radius + width;
            Groups.bullet.intersect(paramPos.x - reach, paramPos.y - reach, reach * 2f, reach * 2f, shieldConsumer);
            Units.nearbyEnemies(paramUnit.team, paramPos.x - reach, paramPos.y - reach, reach * 2f, reach * 2f, unitConsumer);
        } else {
            widthScale = Mathf.lerpDelta(widthScale, 0f, 0.11f);
        }
    }

    @Override
    public void init(UnitType type) {
        data = max;
        if (weaponIndex >= type.weapons.size) {
            throw new ArrayIndexOutOfBoundsException("Weapon index " + weaponIndex + " exceeds available weapons count: " + type.weapons.size);
        }
    }

    @Override
    public void draw(Unit unit) {
        if (widthScale > 0.001f) {
            WeaponMount mount = unit.mounts[weaponIndex];

            Draw.z(Layer.shields);

            Draw.color(color == null ? unit.team.color : color, Color.white, Mathf.clamp(alpha));
            Vec2 pos = paramPos.set(x, y).rotate(mount.rotation + unit.rotation).add(unit);

            if (!Vars.renderer.animateShields) {
                Draw.alpha(0.4f);
            }

            if (region != null) {
                Vec2 rp = offsetRegion ? pos : Tmp.v1.set(unit);
                Draw.yscl = widthScale;
                Draw.rect(region, rp.x, rp.y, mount.rotation + unit.rotation);
                Draw.yscl = 1f;
            }

            if (drawArc) {
                float strokeWidth = (drawWidth > 0 ? drawWidth : width) * widthScale;
                Lines.stroke(strokeWidth);
                Lines.arc(pos.x, pos.y, radius + Math.max(0, (width - strokeWidth) / 2f), angle / 360f, mount.rotation + unit.rotation + angleOffset - angle / 2f);
            }
            Draw.reset();
        }
    }

    @Override
    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar("stat.shieldhealth", Pal.accent, () -> data / max)).row();
    }
}
