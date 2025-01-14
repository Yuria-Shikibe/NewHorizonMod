package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import newhorizon.expand.cutscene.stateoverride.UnitOverride;
import newhorizon.expand.units.status.BoostStatusEffect;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.EffectWrapper;

public class NHStatusEffects{
    public static StatusEffect
            boost,
            quiet, marker, healthLocker, forceWeak,
            reinforcements,
            entangled,
            ultFireBurn, stronghold, overphased,
            staticVel, emp1, emp2, emp3, invincible, quantization, scrambler, end, phased, weak, scannerDown, intercepted;
    
    public static void load(){
        boost = new BoostStatusEffect("boost"){{
            hideDetails = true;
            show = false;
        }};

        forceWeak = new NHStatusEffect("force-slow"){{
            hideDetails = true;
            show = false;
            speedMultiplier = 0.855f;
            reloadMultiplier = 0.75f;
            damage = 0.45f;
        }};
        
        healthLocker = new NHStatusEffect("health-locker"){{
            hideDetails = true;
            show = false;
            permanent = true;
            healthMultiplier = 8;
        }
    
            @Override
            public void update(Unit unit, float time){
                super.update(unit, time);
            
                if(unit.healthf() < 0.25f){
                    unit.health = unit.maxHealth() / 4f;
                }
            }
        };
        
        marker = new NHStatusEffect("marker"){{
            hideDetails = true;
            show = false;
            permanent = true;
        }
    
            @Override
            public void update(Unit unit, float time){
                UnitOverride.marked.put(Double.doubleToLongBits(unit.flag), unit);
            }
        };
        
        quiet = new NHStatusEffect("quiet"){{
            disarm = true;
            dragMultiplier = 10;
            speedMultiplier = 0;
            
            hideDetails = true;
            show = false;
        }};
        
        reinforcements = new NHStatusEffect("reinforcements"){{
            show = false;
            hideDetails = true;
        }
    
            @Override
            public void update(Unit unit, float time){
                if(time < 60f || unit.healthf() < 0.1f){
                    unit.clearStatuses();
                    Effect.shake(unit.hitSize / 10f, unit.hitSize / 8f, unit.x, unit.y);
                    NHFx.circleOut.at(unit.x, unit.y, unit.hitSize, unit.team.color);
                    NHFx.jumpTrailOut.at(unit.x, unit.y, unit.rotation, unit.team.color, unit.type);
                    NHSounds.jumpIn.at(unit.x, unit.y, 1, 3);
                    
                    unit.remove();
                    if(Vars.net.client())Vars.netClient.clearRemovedEntity(unit.id);
                }
            }
        };
        
        entangled = new NHStatusEffect("entangled"){{
            color = textureColor = Color.lightGray;
            speedMultiplier = 0.95f;
            reloadMultiplier = 0.95f;
            outline = true;
            
            effectChance = 0.085f;
            effect = EffectWrapper.wrap(NHFx.hitSparkLarge, NHColor.ancientLightMid);
        }
        
            @Override
            public void update(Unit unit, float time){
                super.update(unit, time);
                
                unit.shield *= 0.985f;
                
                if(unit.shield > 50f && Mathf.chanceDelta(0.065)){
                    NHFx.shuttle.at(unit.x + Mathf.random(unit.hitSize * 0.75f), unit.y + Mathf.random(unit.hitSize * 0.75f), 45, NHColor.ancient, Mathf.clamp(unit.shield, 1000, 6000) / Vars.tilesize / 22f);
                    Effect.shake(3, 5, unit);
                }
            }
        };
        
        overphased = new NHStatusEffect("overphased"){{
            outline = true;
            color = textureColor = NHColor.deeperBlue;
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
            public boolean isHidden(){
                return false;
            }
    
            @Override
            public void update(Unit unit, float time){
                if(damage > 0){
                    unit.damageContinuousPierce(damage);
                }else if(damage < 0){ //heal unit
                    unit.heal(-1f * damage * Time.delta);
                }
    
                if(Mathf.chanceDelta(effectChance)){
                    Tmp.v1.trns(unit.rotation, -unit.type.engineOffset).add(unit);
                    NHFunc.randFadeLightningEffect(Tmp.v1.x, Tmp.v1.y, unit.hitSize * 1.7f, Mathf.random(8f, 18f), unit.team.color, Mathf.chance(0.5));
                    
                    effect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, 0, color, parentizeEffect ? unit : null);
                }
            }
        };
        
        stronghold = new NHStatusEffect("stronghold"){{
            color = textureColor = Color.lightGray;
            speedMultiplier = 0.001f;
            healthMultiplier = 2f;
        }};
        
        intercepted = new NHStatusEffect("intercepted"){{
           damage = 0;
           
           speedMultiplier = 0.55f;
           healthMultiplier = 0.75f;
           damageMultiplier = 0.75f;
           
           effectChance = 0.05f;
           effect = NHFx.square45_4_45;
           color = textureColor = Pal.accent;
        }};
        
        ultFireBurn = new NHStatusEffect("ult-fire-burn"){{
            damage = 1.5f;
            
            color = textureColor = NHColor.lightSkyBack;
            speedMultiplier = 1.2f;
            effect = NHFx.ultFireBurn;
        }};
        
        scannerDown = new NHStatusEffect("scanner-down"){{
            damage = 2;
            
            damageMultiplier = 0.95f;
            speedMultiplier = 0.9f;
            reloadMultiplier = 0.6f;
            
            effectChance = 0.2f;
            color = Pal.heal.cpy().lerp(Pal.lancerLaser, 0.5f);
            effect = new MultiEffect(NHFx.squareRand(Pal.heal, 8f, 16f), NHFx.squareRand(Pal.lancerLaser, 8f, 16f));
        }
    
            @Override
            public void update(Unit unit, float time){
                super.update(unit, time);
                
//                if(unit.controller() instanceof AIController){
//                    AIController controller = (AIController)unit.controller();
//                    unit.mounts[0].weapon.inaccuracy
//                }
            }
    
            //            @Override
//            public void update(Unit unit, float time){
//               super.update(unit, time);
//
//                if(unit.isLocal()){
//                    ScreenInterferencer.continueGenerate();
//                }
//            }
        };
        
        weak = new NHStatusEffect("weak"){{
            speedMultiplier = 0.75f;
            damageMultiplier = 0.8f;
            reloadMultiplier = 0.9f;
            
            
            textureColor = color = NHColor.thurmixRed;
    
            effectChance = 0.25f;
            effect = new MultiEffect(new Effect(30, e -> {
                Draw.color(color);
                float drawSize = 24f * e.fout();
                Draw.rect(NHContent.pointerRegion, e.x, e.y - e.rotation * 24f * e.finpow(), drawSize, drawSize, -180);
            }), NHFx.crossBlast(color, 30, 45));
        }};
        
        phased = new NHStatusEffect("phased"){{
            damage = -10f;
            speedMultiplier = 1.5f;
            damageMultiplier = 1.25f;
            healthMultiplier = 1.5f;
            
    
            textureColor = color = NHColor.lightSkyBack;
            
            effectChance = 0.25f;
            effect = NHFx.squareRand(color, 8f, 16f);
        }};
        
        end = new NHStatusEffect("end"){{
            damage = 200;
            textureColor = color = NHColor.darkEnrColor;
            
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
            public void update(Unit unit, float time){
                unit.damage(120, true);
            
                if(!Vars.headless && Mathf.chanceDelta(0.1)){
                    Tmp.v1.rnd(Mathf.random(unit.hitSize() / 3.5f, unit.hitSize()) * 2f);
                    NHFx.shuttleLerp.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, Tmp.v1.angle(), color, Tmp.v1.len());
                }
            }
        };
        
        scrambler = new NHStatusEffect("scrambler-status"){{
            reloadMultiplier = 0.35f;
            damage = 0.35f;
            speedMultiplier = 0.125f;
            textureColor = color = NHColor.thermoPst;
            effectChance = 0.1f;
            effect = new MultiEffect(new Effect(30, e -> {
                Draw.color(color);
                float drawSize = 24f * e.fout();
                Draw.rect(NHContent.pointerRegion, e.x, e.y - e.rotation * 24f * e.finpow(), drawSize, drawSize, -180);
            }), NHFx.lightningHitSmall(color));
        }};
        
        quantization = new NHStatusEffect("quantization"){{
            textureColor = color = NHColor.darkEnrColor;
            effectChance = 0.1f;
            damage = -2f;
            effect = NHFx.squareRand(color, 5f, 13f);
            buildSpeedMultiplier = speedMultiplier = damageMultiplier = reloadMultiplier = 1.25f;
            healthMultiplier = 0.75f;
        }};
        
        invincible = new NHStatusEffect("invincible"){{
            healthMultiplier = 3;
        }
            @Override
            public void draw(Unit unit, float time){
                Draw.z(Layer.effect);
                Draw.color(NHColor.lightSkyBack);
                
                float size = Mathf.clamp(time / 30f) * NHContent.upgrade.height * Draw.scl;
    
                for(int i : Mathf.signs){
                    Tmp.v1.trns(unit.rotation + 90 * i,unit.hitSize * 1.5f).add(unit);
                    Draw.rect(NHContent.upgrade, Tmp.v1.x, Tmp.v1.y, size, size, unit.rotation + 90 * i - 90);
                }
            }
        };
        
        staticVel = new NHStatusEffect("static-vel") {
            @Override
            public void update(Unit unit, float time){
                super.update(unit, time);
                unit.vel = unit.vel.scl(0.05f);
            }

            {
                permanent = true;
                this.color = Pal.gray;
                this.speedMultiplier = 0.00001F;
            }
        };
        
        emp1 = new NHStatusEffect("emp-1"){{
            damage = 0.05f;
            effect = NHFx.emped;
            effectChance = 0.1f;
            reactive = false;
            speedMultiplier = 0.8f;
            reloadMultiplier = 0.8f;
            damageMultiplier = 0.8f;
    
            transitionDamage = 40;
    
            affinity(StatusEffects.shocked, (unit, status, time) -> {
                if(Mathf.chance(0.085))NHFunc.randFadeLightningEffect(unit.x + Mathf.range(unit.hitSize), unit.y + Mathf.range(unit.hitSize), unit.hitSize * Mathf.random(1f, 1.6f) + 14f, 6f, Tmp.c1.set(Pal.powerLight).mul(Mathf.random(0.12f) + 1f), false);
            });
        }
        
        };
    
        emp2 = new NHStatusEffect("emp-2"){{
            damage = 0.15f;
            effect = NHFx.emped;
            effectChance = 0.2f;
            reactive = false;
            speedMultiplier = 0.6f;
            reloadMultiplier = 0.65f;
            damageMultiplier = 0.7f;
    
            transitionDamage = 80;
    
            affinity(StatusEffects.shocked, (unit, status, time) -> {
                if(Mathf.chance(0.125))NHFunc.randFadeLightningEffect(unit.x + Mathf.range(unit.hitSize), unit.y + Mathf.range(unit.hitSize), unit.hitSize * Mathf.random(1.25f, 2f) + 22f, 7f, Tmp.c1.set(Pal.powerLight).mul(Mathf.random(0.16f) + 1f), false);
            });
        }};
    
        emp3 = new NHStatusEffect("emp-3"){{
            damage = 0.25f;
            effect = NHFx.emped;
            effectChance = 0.3f;
            reactive = false;
            speedMultiplier = 0.4f;
            reloadMultiplier = 0.5f;
            damageMultiplier = 0.6f;
    
            transitionDamage = 120;
    
            affinity(StatusEffects.shocked, (unit, status, time) -> {
                if(Mathf.chance(0.155))NHFunc.randFadeLightningEffect(unit.x + Mathf.range(unit.hitSize), unit.y + Mathf.range(unit.hitSize), unit.hitSize * Mathf.random(1.4f, 2.2f) + 28f, 8f, Tmp.c1.set(Pal.powerLight).mul(Mathf.random(0.16f) + 1f), false);
            });
        }};
    }
	
	public static class NHStatusEffect extends StatusEffect{
		public Color textureColor = null;
		
		public NHStatusEffect(String name){
			super(name);
		}
        
        @Override
        public void load(){
            super.load();
        }
	}
}
