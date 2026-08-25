package newhorizon.expand.block.defence;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.type.Category;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.entities.SharedShieldField;
import newhorizon.expand.entities.SharedShieldFields;
import newhorizon.expand.entities.VortexEvent;

import static mindustry.type.ItemStack.with;

public class QuantumVortexProjector extends ForceProjector {
    public QuantumVortexProjector(String name) {
        super(name);

        requirements(Category.effect, BuildVisibility.sandboxOnly, with());
        size = 2;
        sides = 4;
        health = 1500;
        armor = 10;
        itemCapacity = 20;
        liquidCapacity = 25f;
        shieldRotation = 45f;
        radius = 120f;
        shieldHealth = 5000f;
        cooldownNormal = 12f;
        cooldownBrokenBase = 2.5f;
        phaseRadiusBoost = 60f;
        phaseShieldBoost = 2500f;
        consumePower(5f);
        itemConsumer = null;
        coolantConsumer = new ConsumeCoolant(0.1f);
        consume(coolantConsumer).boost().update(false);
    }

    public float realRadius(QuantumBuild build) {
        return (radius + build.phaseHeat * phaseRadiusBoost) * build.radscl;
    }

    public class QuantumBuild extends ForceBuild {
        public transient SharedShieldField field;

        @Override
        public void created() {
            super.created();
            field = SharedShieldFields.find(this);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (field == null) {
                field = SharedShieldFields.find(this);
                field.add(this);
            } else if (!field.hasSource(this)) {
                field.add(this);
            }

            deflectBullets();
        }

        @Override
        public void onRemoved() {
            if (field != null) field.remove(this);
            super.onRemoved();
        }

        @Override
        public void pickedUp() {
            super.pickedUp();
            radscl = warmup = 0f;
        }

        public void deflectBullets() {
            if (field == null || field.broken || !field.active()) return;

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
            drawSharedShield();
        }

        private void drawSharedShield() {
            if (field == null || field.broken || radscl <= 0.001f) return;

            Draw.color(team.color, Color.white, Mathf.clamp(field.hit));
            Draw.z(Layer.shields + 0.001f * field.hit);
            Fill.poly(x, y, sides, QuantumVortexProjector.this.realRadius(this), shieldRotation);
            Draw.reset();
        }
    }
}
