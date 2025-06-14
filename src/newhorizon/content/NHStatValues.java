package newhorizon.content;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import mindustry.content.StatusEffects;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Icon;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.StatValues;
import newhorizon.expand.bullets.TypeDamageBulletType;

import static mindustry.Vars.content;
import static mindustry.Vars.tilesize;

public class NHStatValues {
    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent, boolean showUnit) {
        return table -> {

            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for (T t : orderedKeys) {
                boolean compact = t instanceof UnitType && !showUnit || indent > 0;

                BulletType type = map.get(t);

                if (type.spawnUnit != null && type.spawnUnit.weapons.size > 0) {
                    ammo(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), indent, false).display(table);
                    continue;
                }

                table.table(Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();
                    //no point in displaying unit icon twice
                    if (!compact && !(t instanceof Turret)) {
                        bt.table(title -> {
                            title.image(icon(t)).size(3 * 8).padRight(4).right().scaling(Scaling.fit).top();
                            title.add(t.localizedName).padRight(10).left().top();
                        });
                        bt.row();
                    }

                    if (type instanceof TypeDamageBulletType typeDamageBulletType) {
                        typeDamageBulletType.buildStat(type, t, bt, compact);
                    } else {
                        if (type.damage > 0 && (type.collides || type.splashDamage <= 0)) {
                            if (type.continuousDamage() > 0) {
                                bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
                            } else {
                                bt.add(Core.bundle.format("bullet.damage", type.damage));
                            }
                        }

                        buildSharedBulletTypeStat(type, t, bt, compact);

                        if (type.intervalBullet != null) {
                            bt.row();

                            Table ic = new Table();
                            StatValues.ammo(ObjectMap.of(t, type.intervalBullet), true, false).display(ic);
                            Collapser coll = new Collapser(ic, true);
                            coll.setDuration(0.1f);

                            bt.table(it -> {
                                it.left().defaults().left();

                                it.add(Core.bundle.format("bullet.interval", Strings.autoFixed(type.intervalBullets / type.bulletInterval * 60, 2)));
                                it.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                            });
                            bt.row();
                            bt.add(coll);
                        }

                        if (type.fragBullet != null) {
                            bt.row();

                            Table fc = new Table();
                            StatValues.ammo(ObjectMap.of(t, type.fragBullet), true, false).display(fc);
                            Collapser coll = new Collapser(fc, true);
                            coll.setDuration(0.1f);

                            bt.table(ft -> {
                                ft.left().defaults().left();

                                ft.add(Core.bundle.format("bullet.frags", type.fragBullets));
                                ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
                            });
                            bt.row();
                            bt.add(coll);
                        }
                    }

                }).padLeft(indent * 5).padTop(5).padBottom(compact ? 0 : 5).growX().margin(compact ? 0 : 10);
                table.row();
            }
        };
    }

    public static void buildSharedBulletTypeStat(BulletType type, UnlockableContent t, Table bt, boolean compact) {
        if (type.buildingDamageMultiplier != 1) {
            sep(bt, Core.bundle.format("bullet.buildingdamage", ammoStat((int) (type.buildingDamageMultiplier * 100 - 100))));
        }

        if (type.rangeChange != 0 && !compact) {
            sep(bt, Core.bundle.format("bullet.range", ammoStat(type.rangeChange / tilesize)));
        }

        if (type.splashDamage > 0) {
            sep(bt, Core.bundle.format("bullet.splashdamage", (int) type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
        }

        if (!compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier && (!(t instanceof Turret turret) || turret.displayAmmoMultiplier)) {
            sep(bt, Core.bundle.format("bullet.multiplier", (int) type.ammoMultiplier));
        }

        if (!compact && !Mathf.equal(type.reloadMultiplier, 1f)) {
            int val = (int) (type.reloadMultiplier * 100 - 100);
            sep(bt, Core.bundle.format("bullet.reload", ammoStat(val)));
        }

        if (type.knockback > 0) {
            sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
        }

        if (type.healPercent > 0f) {
            sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
        }

        if (type.healAmount > 0f) {
            sep(bt, Core.bundle.format("bullet.healamount", Strings.autoFixed(type.healAmount, 2)));
        }

        if (type.pierce || type.pierceCap != -1) {
            sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
        }

        if (type.incendAmount > 0) {
            sep(bt, "@bullet.incendiary");
        }

        if (type.homingPower > 0.01f) {
            sep(bt, "@bullet.homing");
        }

        if (type.lightning > 0) {
            sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
        }

        if (type.pierceArmor) {
            sep(bt, "@bullet.armorpierce");
        }

        if (type.maxDamageFraction > 0) {
            sep(bt, Core.bundle.format("bullet.maxdamagefraction", (int) (type.maxDamageFraction * 100)));
        }

        if (type.suppressionRange > 0) {
            sep(bt, Core.bundle.format("bullet.suppression", Strings.autoFixed(type.suppressionDuration / 60f, 2), Strings.fixed(type.suppressionRange / tilesize, 1)));
        }

        if (type.status != StatusEffects.none) {
            sep(bt, (type.status.hasEmoji() ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" +
                    ((int) (type.statusDuration / 60f)) + "[lightgray] " + Core.bundle.get("unit.seconds"))).with(c -> StatValues.withTooltip(c, type.status));
        }

        if (!type.targetMissiles) {
            sep(bt, "@bullet.notargetsmissiles");
        }

        if (!type.targetBlocks) {
            sep(bt, "@bullet.notargetsbuildings");
        }
    }

    public static StatValue boosters(float reload, float maxUsed, float multiplier, boolean baseReload, Boolf<Liquid> filter, boolean noReloadBoost) {
        return table -> {
            table.row();
            table.table(c -> {
                for (Liquid liquid : content.liquids()) {
                    if (!filter.get(liquid)) continue;

                    c.table(Styles.grayPanel, b -> {
                        b.image(liquid.uiIcon).size(40).pad(10f).left().scaling(Scaling.fit);
                        b.table(info -> {
                            info.add(liquid.localizedName).left().row();
                            info.add(Strings.autoFixed(maxUsed * 60f, 2) + StatUnit.perSecond.localized()).left().color(Color.lightGray);
                        });

                        b.table(bt -> {

                            bt.right().defaults().padRight(3).left();
                            float reloadRate = (baseReload ? 1f : 0f) + maxUsed * multiplier * liquid.heatCapacity;
                            float standardReload = baseReload ? reload : reload / (maxUsed * multiplier * 0.4f);
                            float result = standardReload / (reload / reloadRate);
                            if (!noReloadBoost)
                                bt.add(Core.bundle.format("bullet.reload", Strings.autoFixed(result * 100, 2))).pad(5).right().row();
                            bt.add(Core.bundle.format("nh.stat.speed-up-turret-coolant", Strings.autoFixed((liquid.heatCapacity + 1) * 100, 2), Strings.autoFixed((1 / (liquid.heatCapacity + 1)) * 100, 0))).pad(5);
                        }).right().grow().pad(10f).padRight(15f);
                    }).growX().pad(5).row();
                }
            }).growX().colspan(table.getColumns());
            table.row();
        };
    }

    public static StatValue weapons(UnitType unit, Seq<Weapon> weapons) {
        return table -> {
            table.row();
            for (int i = 0; i < weapons.size; i++) {
                Weapon weapon = weapons.get(i);

                if (weapon.flipSprite || !weapon.hasStats(unit)) {
                    //flipped weapons are not given stats
                    continue;
                }

                TextureRegion region = !weapon.name.isEmpty() ? Core.atlas.find(weapon.name + "-preview", weapon.region) : null;

                table.table(Styles.grayPanel, w -> {
                    w.left().top().defaults().padRight(3).left();
                    if (region != null && region.found() && weapon.showStatSprite)
                        w.image(region).size(60).scaling(Scaling.bounded).left().top();
                    w.row();

                    if (weapon.inaccuracy > 0) {
                        w.row();
                        w.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (int) weapon.inaccuracy + " " + StatUnit.degrees.localized());
                    }
                    if (!weapon.alwaysContinuous && weapon.reload > 0) {
                        w.row();
                        w.add("[lightgray]" + Stat.reload.localized() + ": " + (weapon.mirror ? "2x " : "") + "[white]" + Strings.autoFixed(60f / weapon.reload * weapon.shoot.shots, 2) + " " + StatUnit.perSecond.localized());
                    }

                    ammo(ObjectMap.of(unit, weapon.bullet), 0, false).display(w);

                }).growX().pad(5).margin(10);
                table.row();
            }
        };
    }

    //for AmmoListValue
    private static Cell<?> sep(Table table, String text) {
        table.row();
        return table.add(text);
    }

    //for AmmoListValue
    private static String ammoStat(float val) {
        return (val > 0 ? "[stat]+" : "[negstat]") + Strings.autoFixed(val, 1);
    }

    private static TextureRegion icon(UnlockableContent t) {
        return t.uiIcon;
    }
}
