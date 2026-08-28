package newhorizon.expand.block.defence;

import arc.graphics.Color;
import arc.Core;
import arc.util.Strings;
import arc.math.Mathf;
import mindustry.entities.Effect;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import arc.math.geom.Intersector;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.logic.LAccess;
import arc.util.io.Reads;
import arc.util.io.Writes;
import newhorizon.expand.entities.SharedShieldField;
import newhorizon.expand.entities.SharedShieldFields;

public class QuantumVortexProjector extends ForceProjector {
    /** Assigned by DefenseBlock during content registration. */
    public Effect shieldActivateEffect;

    public QuantumVortexProjector(String name) {
        super(name);
    }

    public float realRadius(QuantumBuild build) {
        return radius * visualScale(build);
    }

    public float displayRadius(Building build) {
        if (!(build instanceof QuantumBuild quantum)) return 0f;
        return radius * visualScale(quantum);
    }

    /** The shared field owns the visible shield scale; inherited ForceBuild
     * state is retained only for the base class update contract. */
    private float visualScale(QuantumBuild build) {
        if (build.efficiency <= 0.01f) return 0f;
        if (build.field == null || !build.field.hasSource(build) || !build.field.sameTeam(build)) return 0f;
        if (build.field.broken) return 0f;
        return Mathf.clamp(Math.max(build.field.radscl, build.field.warmup));
    }

    public class QuantumBuild extends ForceBuild {
        public transient SharedShieldField field;

        @Override
        public float realRadius() {
            // ForceBuild exposes this value to range queries and effects. Keep
            // those callers on the same shared-field scale as rendering and
            // bullet interception.
            return QuantumVortexProjector.this.realRadius(this);
        }

        @Override
        public void created() {
            super.created();
            field = SharedShieldFields.find(this);
        }

        @Override
        public void updateTile() {
            super.updateTile();

            // A projector without power is not an active source: it neither
            // contributes capacity/recovery nor keeps a range connection.
            if (efficiency <= 0.01f) {
                if (field != null) {
                    field.remove(this);
                    field = null;
                }
                radscl = warmup = 0f;
                buildup = 0f;
                broken = false;
                return;
            }

            if (field == null) {
                field = SharedShieldFields.find(this);
            } else if (!field.hasSource(this) || !field.sameTeam(this)) {
                // Re-resolve through the registry. The old field may have been
                // removed during a world reset or topology rebuild.
                field.remove(this);
                field = SharedShieldFields.find(this);
            }

            // ForceProjector only updates coolant while its private shield has
            // buildup.  Shared shields keep buildup in SharedShieldField, so
            // explicitly update the coolant consumer while recovering.
            if (field != null && (field.broken || field.buildup > 0f) && coolantConsumer != null) {
                coolantConsumer.update(this);
            }

            // The shared field is authoritative. Keep only its visual warmup
            // mirrored to the inherited projector; copying shared buildup into
            // ForceBuild would make a multi-projector field look broken as soon
            // as it exceeded one projector's 5000-point private capacity.
            if (field != null) {
                float scale = visualScale(this);
                radscl = scale;
                warmup = field.broken ? 0f : field.warmup;

                // Do not let ForceBuild's private 5000-point shield trigger a
                // second break or keep the inherited scale collapsed. Shared
                // ShieldField is the sole authority for damage and recovery.
                buildup = 0f;
                broken = false;
            }

            // ForceBuild.updateTile() dispatches to this class's deflectBullets()
            // once. Calling it again here would scan every nearby bullet twice.
        }

        @Override
        public void onRemoved() {
            if (field != null) {
                field.remove(this);
                field = null;
            }
            super.onRemoved();
        }

        @Override
        public void pickedUp() {
            if (field != null) {
                field.remove(this);
                field = null;
            }
            super.pickedUp();
            radscl = warmup = 0f;
        }

        public void deflectBullets() {
            // ForceBuild.updateTile() dispatches here before this override can
            // detach an unpowered source. Gate the local interaction as well,
            // otherwise a stale field reference could absorb for a powered
            // neighbour during the power-loss tick.
            if (efficiency <= 0.01f || field == null || !field.hasSource(this) || !field.sameTeam(this)
                    || field.broken || !field.active()) return;

            float radius = QuantumVortexProjector.this.realRadius(this);
            float maxRadius = field.maxRadius();
            float searchRadius = Math.max(radius, maxRadius);
            if (searchRadius <= 0f) return;

            float useRadius = Math.max(searchRadius, 1f);

            Groups.bullet.intersect(x - useRadius, y - useRadius, useRadius * 2f, useRadius * 2f,
                    (Bullet bullet) -> intercept(field, bullet));
        }

        private void intercept(SharedShieldField field, Bullet bullet) {
            if (bullet.team == team || !bullet.type.absorbable || bullet.absorbed) return;

            boolean insideOwnField = QuantumVortexProjector.this.realRadius(this) > 0f && Intersector.isInRegularPolygon(sides, x, y,
                    QuantumVortexProjector.this.realRadius(this), shieldRotation, bullet.x, bullet.y);
            boolean insideMergedField = insideOwnField ||
                    field.contains(bullet.x, bullet.y);
            if (!insideMergedField) return;

            bullet.absorb();
            hitSound.at(bullet.x, bullet.y, 1f + Mathf.range(0.1f), hitSoundVolume);
            absorbEffect.at(bullet);
            field.damage(bullet.type.shieldDamage(bullet), bullet.x, bullet.y);
        }

        @Override
        public void draw() {
            super.draw();
            drawShield();
        }

        @Override
        public void drawShield() {
        }

        @Override
        public boolean absorbExplosion(float ex, float ey, float damage) {
            if (efficiency <= 0.01f || field == null || !field.hasSource(this) || !field.sameTeam(this)
                    || field.broken || !field.active()) return false;
            if (!Intersector.isInRegularPolygon(sides, x, y, QuantumVortexProjector.this.realRadius(this), shieldRotation, ex, ey)) return false;
            field.damage(damage * crashDamageMultiplier, ex, ey);
            return true;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.shield) {
                if (efficiency <= 0.01f || field == null || !field.hasSource(this) || !field.sameTeam(this) || field.broken) return 0d;
                return Math.max(field.capacity() - field.buildup, 0f);
            }
            return super.sense(sensor);
        }

        @Override
        public void setProp(LAccess prop, double value) {
            if (prop == LAccess.shield) {
                if (efficiency <= 0.01f || field == null || !field.hasSource(this) || !field.sameTeam(this)) return;
                float capacity = field.capacity();
                field.buildup = Mathf.clamp(capacity - (float)value, 0f, capacity);
                if (field.buildup >= capacity) field.broken = true;
                return;
            }
            super.setProp(prop, value);
        }

        @Override
        public void writeSync(Writes write) {
            super.writeSync(write);
            SharedShieldField shared = efficiency > 0.01f && field != null && field.hasSource(this) && field.sameTeam(this) ? field : null;
            write.f(shared == null ? 0f : shared.buildup);
            write.bool(shared != null && shared.broken);
            write.f(shared == null ? 0f : shared.cooldownProgress());
        }

        @Override
        public void readSync(Reads read, byte revision) {
            super.readSync(read, revision);
            float syncedBuildup = read.f();
            boolean syncedBroken = read.bool();
            float syncedCooldown = read.f();
            if (efficiency > 0.01f && field == null) field = SharedShieldFields.find(this);
            if (efficiency > 0.01f && field != null && field.hasSource(this) && field.sameTeam(this)) {
                field.buildup = syncedBuildup;
                field.broken = syncedBroken;
                field.setCooldownProgress(syncedCooldown);
            }
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("shield");
        addBar("sharedShield", (QuantumBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.new-horizon-shared-shield", Strings.autoFixed(Math.max(entity.field == null ? 0f : entity.field.capacity() - entity.field.buildup, 0f), 0)),
                () -> Pal.accent,
                () -> {
                    if (entity.field == null) return 0f;
                    float capacity = entity.field.capacity();
                    return capacity <= 0f ? 0f : Mathf.clamp((capacity - entity.field.buildup) / capacity);
                }
        ).blink(Color.white));
        addBar("sharedShieldCooldown", (QuantumBuild entity) -> new Bar(
                () -> Core.bundle.get("stat.cooldowntime"),
                () -> entity.field != null && entity.field.broken ? Pal.redderDust : Pal.accent,
                () -> entity.field == null ? 0f : entity.field.cooldownProgressRatio()
        ));
    }
}
