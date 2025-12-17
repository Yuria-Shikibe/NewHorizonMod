package newhorizon.content;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;

import static arc.graphics.g2d.Draw.color;

public class NHStatusEffects {
    public static Rand rand = new Rand();

    public static StatusEffect
            emp1, emp2, emp3, phased, overphased, weak,
            ultFireBurn, stronghold, quantization, scrambler,
            invincible, intercepted, entangled, end,
            staticVel, scannerDown, blackWall,
            shieldFlag, accumulateFlag, executionFlag, immunityFlag;

    public static void load() {
        entangled = new NHStatusEffect("entangled") {{
                color = Color.lightGray;
                speedMultiplier = 0.95f;
                reloadMultiplier = 0.95f;

                effectChance = 0.085f;
                effect = EffectWrapper.wrap(NHFx.hitSparkLarge, NHColor.ancientLightMid);
            }
            @Override
            public void update(Unit unit, StatusEntry entry) {
                super.update(unit, entry);

                unit.shield *= 0.985f;

                if (unit.shield > 50f && Mathf.chanceDelta(0.065)) {
                    NHFx.shuttle.at(unit.x + Mathf.random(unit.hitSize * 0.75f), unit.y + Mathf.random(unit.hitSize * 0.75f), 45, NHColor.ancient, Mathf.clamp(unit.shield, 1000, 6000) / Vars.tilesize / 22f);
                    Effect.shake(3, 5, unit);
                }
            }
        };

        overphased = new NHStatusEffect("overphased") {{
                
                color = NHColor.deeperBlue;
                speedMultiplier = 1.75f;
                healthMultiplier = 3f;
                reloadMultiplier = 2f;
                damageMultiplier = 1.5f;

                effectChance = 0.3f;
                permanent = true;
                hideDetails = false;
                show = true;
            }

            @Override
            public boolean isHidden() {
                return false;
            }

            @Override
            public void update(Unit unit, StatusEntry entry) {
                super.update(unit, entry);
                if (damage > 0) {
                    unit.damageContinuousPierce(damage);
                } else if (damage < 0) { //heal unit
                    unit.heal(-1f * damage * Time.delta);
                }

                if (Mathf.chanceDelta(effectChance)) {
                    Tmp.v1.trns(unit.rotation, -unit.type.engineOffset).add(unit);
                    NHFunc.randFadeLightningEffect(Tmp.v1.x, Tmp.v1.y, unit.hitSize * 1.7f, Mathf.random(8f, 18f), unit.team.color, Mathf.chance(0.5));

                    effect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, 0, color, parentizeEffect ? unit : null);
                }
            }
        };

        stronghold = new NHStatusEffect("stronghold") {{
            color = Color.lightGray;
            speedMultiplier = 0.001f;
            healthMultiplier = 2f;
        }};

        intercepted = new NHStatusEffect("intercepted") {{
            damage = 0;

            speedMultiplier = 0.55f;
            healthMultiplier = 0.75f;
            damageMultiplier = 0.75f;

            effectChance = 0.05f;
            effect = NHFx.square45_4_45;
            color = Pal.accent;
        }};

        ultFireBurn = new NHStatusEffect("ult-fire-burn") {{
            damage = 1.5f;

            color = NHColor.lightSkyBack;
            speedMultiplier = 0.75f;
            reloadMultiplier = 0.75f;
            healthMultiplier = 0.75f;
            effect = NHFx.ultFireBurn;
        }};

        scannerDown = new NHStatusEffect("scanner-down") {{
                damageMultiplier = 0.95f;
                speedMultiplier = 0.9f;
                reloadMultiplier = 0.6f;

                effectChance = 0.2f;
                color = Pal.heal.cpy().lerp(Pal.lancerLaser, 0.5f);
                effect = new MultiEffect(NHFx.squareRand(Pal.heal, 8f, 16f), NHFx.squareRand(Pal.lancerLaser, 8f, 16f));
        }};

        weak = new NHStatusEffect("weak") {{
            speedMultiplier = 0.75f;
            damageMultiplier = 0.8f;
            reloadMultiplier = 0.9f;


            color = NHColor.thurmixRed;

            effectChance = 0.25f;
            effect = new MultiEffect(new Effect(30, e -> {
                Draw.color(color);
                float drawSize = 24f * e.fout();
                Draw.rect(NHContent.pointerRegion, e.x, e.y - e.rotation * 24f * e.finpow(), drawSize, drawSize, -180);
            }), NHFx.crossBlast(color, 30, 45));
        }};

        phased = new NHStatusEffect("phased") {{
            damage = -10f;
            speedMultiplier = 1.5f;
            damageMultiplier = 1.25f;
            healthMultiplier = 1.5f;


            color = NHColor.lightSkyBack;

            effectChance = 0.25f;
            effect = NHFx.squareRand(color, 8f, 16f);
        }};

        end = new NHStatusEffect("end") {
            {
                damage = 200;
                color = NHColor.darkEnrColor;

                damageMultiplier = 0.5f;
                reloadMultiplier = 0.5f;
                speedMultiplier = 0.5f;

                effectChance = 0.075f;
                effect = new Effect(20f, 20f, e -> {
                    Draw.color(Color.white, color, e.fin() + 0.35f);
                    Lines.stroke(1.5f * e.fout(Interp.pow3Out));
                    Lines.square(e.x, e.y, Mathf.randomSeed(e.id, 2f, 8f) * e.fin(Interp.pow2Out) + 6f, 45);
                });
            }

            @Override
            public void update(Unit unit, StatusEntry entry) {
                super.update(unit, entry);
                unit.damage(120, true);

                if (!Vars.headless && Mathf.chanceDelta(0.1)) {
                    Tmp.v1.rnd(Mathf.random(unit.hitSize() / 3.5f, unit.hitSize()) * 2f);
                    NHFx.shuttleLerp.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, Tmp.v1.angle(), color, Tmp.v1.len());
                }
            }
        };

        scrambler = new NHStatusEffect("scrambler-status") {{
            reloadMultiplier = 0.35f;
            damage = 0.35f;
            speedMultiplier = 0.125f;
            color = NHColor.thermoPst;
            effectChance = 0.1f;
            effect = new MultiEffect(new Effect(30, e -> {
                Draw.color(color);
                float drawSize = 24f * e.fout();
                Draw.rect(NHContent.pointerRegion, e.x, e.y - e.rotation * 24f * e.finpow(), drawSize, drawSize, -180);
            }), NHFx.lightningHitSmall(color));
        }};

        quantization = new NHStatusEffect("quantization") {{
            color = NHColor.darkEnrColor;
            effectChance = 0.1f;
            damage = -2f;
            effect = NHFx.squareRand(color, 5f, 13f);
            buildSpeedMultiplier = speedMultiplier = damageMultiplier = reloadMultiplier = 1.25f;
            healthMultiplier = 0.75f;
        }};

        invincible = new NHStatusEffect("invincible") {{
                healthMultiplier = 3;
            }

            @Override
            public void draw(Unit unit, float time) {
                Draw.z(Layer.effect);
                Draw.color(NHColor.lightSkyBack);

                float size = Mathf.clamp(time / 30f) * NHContent.upgrade.height * Draw.scl;

                for (int i : Mathf.signs) {
                    Tmp.v1.trns(unit.rotation + 90 * i, unit.hitSize * 1.5f).add(unit);
                    Draw.rect(NHContent.upgrade, Tmp.v1.x, Tmp.v1.y, size, size, unit.rotation + 90 * i - 90);
                }
            }
        };

        staticVel = new NHStatusEffect("static-vel") {{
                permanent = true;
                this.color = Pal.gray;
                this.speedMultiplier = 0.00001F;
            }

            @Override
            public void update(Unit unit, StatusEntry entry) {
                super.update(unit, entry);
                unit.vel = unit.vel.scl(0.05f);
            }
        };

        emp1 = new NHStatusEffect("emp-1") {{
            effect = NHFx.emped;
            effectChance = 0.1f;
            reactive = false;
            speedMultiplier = 0.8f;
            reloadMultiplier = 0.8f;
            damageMultiplier = 0.8f;
        }};

        emp2 = new NHStatusEffect("emp-2") {{
            effect = NHFx.emped;
            effectChance = 0.2f;
            reactive = false;
            speedMultiplier = 0.5f;
            reloadMultiplier = 0.5f;
            damageMultiplier = 0.5f;

            init(() -> override.add(emp1));
        }};

        emp3 = new NHStatusEffect("emp-3") {{
            effect = NHFx.emped;
            effectChance = 0.3f;
            reactive = false;
            speedMultiplier = 0.2f;
            reloadMultiplier = 0.2f;
            damageMultiplier = 0.2f;

            init(() -> override.add(emp1, emp2));
        }};

        immunityFlag = new StatusEffect("immunity-flag");

        executionFlag = new NHStatusEffect("execution-flag") {{
            speedMultiplier = 0f;
        }
            @Override
            public void onRemoved(Unit unit) {
                super.onRemoved(unit);
                float damagePercent = 0.25f;
                float killThreshold = 0.5f;

                boolean shouldKill = unit.health() < unit.maxHealth() * killThreshold;
                float size = unit.type.hitSize * (shouldKill? 9f: 3f);
                Effect eff = new Effect(120f, size * 2, e -> {
                    color(NHColor.darkEnrFront, Color.white, e.fout() * 0.55f);
                    for (int i = 0; i < 4; i++) {
                        DrawFunc.tri(e.x, e.y, size / 20 * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, size * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + 45);
                    }
                });
                for (int i = 0; i < 4; i++){
                    eff.at(unit.x + Geometry.d4x(i) * unit.type.hitSize / 2f, unit.y + Geometry.d4y(i) * unit.type.hitSize / 2f);
                }
                eff.at(unit.x, unit.y);

                if (shouldKill) {
                    unit.kill();
                }else {
                    unit.damagePierce(unit.maxHealth() * damagePercent);
                }
            }
        };

        accumulateFlag = new NHStatusEffect("accumulate-flag") {
            @Override
            public void draw(Unit unit, float time) {
                super.draw(unit, time);

                float scl = time / getTimeThreshold(unit);

                Draw.z(Layer.effect);
                Draw.color(NHColor.darkEnrFront);
                Lines.stroke(Mathf.clamp(unit.hitSize / 40, 0.5f, 2.5f));

                Lines.arc(unit.x, unit.y, unit.hitSize * 1.50f, scl);
                Lines.arc(unit.x, unit.y, unit.hitSize * 2.00f, scl);

                Lines.stroke(Mathf.clamp(unit.hitSize / 20, 1f, 5f) * scl);
                Lines.circle(unit.x, unit.y, unit.hitSize * 1.75f);

                int step = (int) Mathf.clamp(unit.hitSize / 4, 8, 20);
                for (int i = 0; i < step; i++) {
                    float rot = i * (360f / step);
                    Tmp.v1.set(unit.hitSize * 1.75f, 0).rotate(rot).add(unit);

                    Drawf.tri(Tmp.v1.x, Tmp.v1.y, unit.hitSize * 0.1f, unit.hitSize * 0.50f * scl, rot);
                    Drawf.tri(Tmp.v1.x, Tmp.v1.y, unit.hitSize * 0.1f, unit.hitSize * 0.50f * scl, rot + 180);
                }
            }

            @Override
            public void update(Unit unit, StatusEntry entry) {
                super.update(unit, entry);
                if (unit.getDuration(accumulateFlag) >= getTimeThreshold(unit)) {
                    //for (int i = 0; i < 4; i++){
                    //    execution(unit.type.hitSize * 6f).at(unit.x, unit.y, 45 + 90 * i, NHColor.darkEnrFront);
                    //}
                    Effect eff = new Effect(60, e -> {
                        Draw.color(NHColor.darkEnrFront);
                        Lines.stroke(Mathf.clamp(unit.hitSize / 40, 0.5f, 2.5f));

                        float scl = e.fout(Interp.pow3Out);

                        Lines.circle(unit.x, unit.y, unit.hitSize * 1.50f * scl);
                        Lines.circle(unit.x, unit.y, unit.hitSize * 2.00f * scl);

                        Lines.stroke(Mathf.clamp(unit.hitSize / 20, 1f, 5f));
                        Lines.circle(unit.x, unit.y, unit.hitSize * 1.75f * scl);

                        int step = (int) Mathf.clamp(unit.hitSize / 4, 8, 20);
                        for (int i = 0; i < step; i++) {
                            float rot = i * (360f / step);
                            Tmp.v1.set(unit.hitSize * scl * 1.75f, 0).rotate(rot).add(unit);

                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, unit.hitSize * 0.1f, unit.hitSize * 0.50f * scl, rot);
                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, unit.hitSize * 0.1f, unit.hitSize * 0.50f * scl, rot + 180);
                        }
                    });
                    eff.at(unit);
                    unit.apply(executionFlag, 60f);
                    unit.apply(immunityFlag, 90);
                    unit.unapply(accumulateFlag);
                }
            }

            public float getTimeThreshold(Unit unit) {
                return Mathf.sqrt(unit.hitSize) * 100;
            }
        };

        shieldFlag = new NHStatusEffect("shield-flag") {{
            color = Pal.techBlue;
            permanent = true;
            show = false;
        }};

        blackWall = new NHStatusEffect("black-wall") {
            @Override
            public void applied(Unit unit, float time, boolean extend) {
                super.applied(unit, time, extend);
                if (!unit.hasEffect(immunityFlag)){
                    unit.apply(accumulateFlag, unit.getDuration(accumulateFlag) + unit.getDuration(blackWall));
                }
                unit.unapply(blackWall);
            }
        };
    }

    public static class NHStatusEffect extends StatusEffect {
        public Seq<StatusEffect> override = new Seq<>();
        public NHStatusEffect(String name) {
            super(name);
            outline = false;
        }

        @Override
        public void update(Unit unit, StatusEntry entry) {
            super.update(unit, entry);
            override.each(unit::unapply);
        }

        @Override
        public void setStats() {
            super.setStats();
            if(!override.isEmpty()) {
                for(var e : override){
                    stats.add(NHStats.overrides, e.emoji() + e);
                }
            }
        }
    }

    public static Effect execution(float scale) {
        float xDst = scale * 1.25f;
        float yDst = scale / 5f;
        float step = scale / 100f;
        return new Effect(75, e -> {
            float alpha = e.fout(Interp.reverse);
            Draw.color(NHColor.darkEnrFront);
            rand.setSeed(e.id);
            Lines.stroke(step / 1.5f);
            for (float i = 0; i < xDst; i += step){
                float x = NHInterp.upThenFastDown.apply(rand.random(1f)) * xDst;
                float y = rand.random(-yDst, yDst);
                float xScl = Interp.reverse.apply(x / xDst);
                float yScl = Interp.reverse.apply(Math.abs(y / yDst));

                Draw.alpha(Interp.pow10Out.apply(alpha * xScl * yScl) * 0.5f);

                Tmp.v1.set(x * Interp.reverse.apply(e.fin(Interp.pow2Out)) - rand.random(6, 10), y).rotate(e.rotation).add(e.x, e.y);
                Lines.lineAngle(Tmp.v1.x, Tmp.v1.y, e.rotation, step * 15f + rand.random(step * 0.5f) * e.fout());
            }
        });
    }

    public static Effect blast(float scale) {
        float xDst = scale * 1.25f;

        Effect rect = new Effect(100, xDst * 1.25f, e -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * xDst / 1.6f;
            Fill.light(e.x, e.y, 4, radius, Tmp.c1.set(e.color).a(e.fout(Interp.pow5Out)), Color.clear);
            Draw.blend();
        }).layer(Layer.effect + 0.15f);

        Effect rectOut = new Effect(90, xDst * 1.25f, e -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * xDst / 1.6f;
            Fill.light(e.x, e.y, 4, radius, Color.clear, Tmp.c1.set(e.color).a(e.fout(Interp.pow5Out)));
            Draw.blend();
        }).layer(Layer.effect + 0.15f);
        //Effect spark = NHFx.spreadOutSpark(120f, xDst + 40f, (int) (xDst / 2f), 4, 72f, 13f, 4f, Interp.pow3Out).startDelay(60);

        return new MultiEffect(rect, rectOut);
    }
}
