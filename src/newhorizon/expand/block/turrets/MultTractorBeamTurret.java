package newhorizon.expand.block.turrets;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.ContentInfoDialog;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import mindustry.world.meta.Stat;

import static newhorizon.util.ui.TableFunc.OFFSET;

public class MultTractorBeamTurret extends TractorBeamTurret {
    public int maxAttract = 5;
    public StatusEffect status = StatusEffects.slow;
    public float statusDuration = 10f;

    public MultTractorBeamTurret(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.shots, maxAttract);
        if (status != null && status != StatusEffects.none && !status.isHidden()) stats.add(Stat.abilities, table -> {
            table.row().table().padLeft(OFFSET * 2).getTable().table(t -> {
                t.align(Align.topLeft);
                t.table(info -> {
                    info.left();
                    info.add("[lightgray]" + Core.bundle.get("content.status.name") + ": ").padRight(OFFSET);
                    info.button(new TextureRegionDrawable(status.uiIcon), Styles.cleari, () -> {
                        new ContentInfoDialog().show(status);
                    }).scaling(Scaling.fit);
                }).fill().row();
                t.add("Duration: " + statusDuration / 60f + ".sec").growX().fillY();
            }).fill().padBottom(OFFSET).left().row();
        });
    }

    @Override
    public void load() {
        super.load();
        laser = Core.atlas.find("parallax-laser");
        laserEnd = Core.atlas.find("parallax-laser-end");
    }

    public class MultTractorBeamBuild extends TractorBeamBuild {
        /**
         * {@link ObjectMap} {@code targets} Uses a {@link Vec3} to contain:
         * <li>.x -> {@code lastX}
         * <li>.y -> {@code lastY}
         * <li>.z -> {@code strength}
         */
        public final ObjectMap<Unit, Vec3> targets = new ObjectMap<>(maxAttract);

        @Override
        public void updateTile() {
            super.updateTile();
            var entries = targets.iterator();
            while (entries.hasNext()) {
                ObjectMap.Entry<Unit, Vec3> entry = entries.next();
                Unit unit = entry.key;
                Vec3 v = entry.value;
                if (v == null) {
                    entries.remove();
                    continue;
                }
                if (unit != null && unit.isValid() && Angles.within(rotation, angleTo(unit), shootCone) && within(unit, range + unit.hitSize / 2f) && unit.team() != team && unit.checkTarget(targetAir, targetGround)) {
                    v.x = unit.x;
                    v.y = unit.y;
                    v.z = Mathf.lerpDelta(v.z, 1f, 0.1f);
                    if (unit != target) {
                        if (damage > 0) unit.damageContinuous(damage * efficiency);
                        if (status != StatusEffects.none) unit.apply(status, statusDuration);
                        unit.impulseNet(Tmp.v1.set(this).sub(unit).limit((force + (1f - unit.dst(this) / range) * scaledForce) * edelta() * timeScale));
                    }
                } else {
                    v.z = Mathf.lerpDelta(v.z, 0, 0.1f);
                    if (Mathf.equal(v.z, 0, 0.001f)) entries.remove();
                }
            }

            if (target != null && target.within(this, range + target.hitSize / 2f) && target.team() != team && target.checkTarget(targetAir, targetGround) && efficiency > 0.02f) {
                Units.nearbyEnemies(team, Tmp.r1.setSize((range + target.hitSize / 2f) * 2).setCenter(x, y), unit -> {
                    if (targets.size < maxAttract && !targets.containsKey(unit) && Angles.within(rotation, angleTo(unit), shootCone)) {
                        targets.put(unit, new Vec3(unit.x, unit.y, 0f));
                    }
                });
            }
        }

        @Override
        public void draw() {
            Draw.rect(baseRegion, x, y);
            Drawf.shadow(region, x - (size / 2f), y - (size / 2f), rotation - 90);
            Draw.rect(region, x, y, rotation - 90);
            Draw.z(Layer.bullet);
            for (ObjectMap.Entry<Unit, Vec3> entry : targets) {
                Vec3 v = entry.value;
                if (v == null) continue;
                Draw.mixcol();
                Draw.mixcol(laserColor, Mathf.absin(4f, 0.6f));
                Tmp.v1.trns(rotation, shootLength).add(x, y);
                Drawf.laser(
                        laser, laserEnd, Tmp.v1.x, Tmp.v1.y,
                        v.x, v.y, v.z * efficiency * laserWidth
                );
            }
        }
    }
}
