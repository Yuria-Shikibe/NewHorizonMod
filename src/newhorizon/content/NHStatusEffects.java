package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.ctype.ContentList;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.type.StatusEffect;
import newhorizon.NewHorizon;
import newhorizon.func.NHUnitOutline;

public class NHStatusEffects implements ContentList{
    public static StatusEffect
            staticVel, emp1, emp2, emp3, invincible, quantization, accel_3, scrambler, end;
    
    @Override
    public void load(){
        end = new NHStatusEffect("end"){{
            damage = 100;
            permanent = true;
            textureColor = color = NHColor.darkEnrColor;
            effectChance = 0.1f;
            effect = new Effect(20f, 20f, e -> {
                Draw.color(Color.white, color, e.fin() + 0.35f);
                Lines.stroke(1.5f * e.fout(Interp.pow3Out));
                Lines.square(e.x, e.y, Mathf.randomSeed(e.id, 2f, 8f) * e.fin(Interp.pow2Out) + 6f, 45);
            });
        }};
        
        scrambler = new NHStatusEffect("scrambler-status"){{
            disarm = true;
            damage = 2;
            textureColor = color = NHColor.thermoPst;
            effectChance = 0.1f;
            effect = new MultiEffect(new Effect(30, e -> {
                Draw.color(color);
                float drawSize = 24f * e.fout();
                Draw.rect(NHContent.pointerRegion, e.x, e.y - e.rotation * 24f * e.finpow(), drawSize, drawSize, -180);
            }), NHFx.lightningHitSmall(color));
        }};
        
        accel_3 = new StatusEffect("accel_3"){{
            speedMultiplier = 3;
            show = false;
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
            healthMultiplier = 1000000;
        }
            @Override
            public void update(Unit unit, float time){
                if(!unit.spawnedByCore)unit.unapply(this);
            }
    
            @Override
            public void draw(Unit unit){
                if(!unit.spawnedByCore)return;
                Draw.z(Layer.effect);
                Draw.color(NHColor.lightSkyBack);
                TextureRegion area = Core.atlas.find(NewHorizon.name("upgrade"));
                Draw.rect(area, unit.x + unit.hitSize * 1.25f, unit.y, -90);
                Draw.rect(area, unit.x - unit.hitSize * 1.25f, unit.y, 90);
            }
        };
        
//        staticVel = new NHStatusEffect("static-vel") {
//            @Override
//            public void update(Unit unit, float time){
//                super.update(unit, time);
//                unit.vel = unit.vel.scl(0.65f);
//            }
//
//            {
//                this.color = Pal.gray;
//                this.speedMultiplier = 0.00001F;
//            }
//        };
        
        emp1 = new NHStatusEffect("emp-1"){{
            damage = 0.05f;
            effect = NHFx.emped;
            effectChance = 0.1f;
            reactive = false;
            speedMultiplier = 0.8f;
            reloadMultiplier = 0.8f;
            damageMultiplier = 0.8f;
        }};
    
        emp2 = new NHStatusEffect("emp-2"){{
            damage = 0.15f;
            effect = NHFx.emped;
            effectChance = 0.2f;
            reactive = false;
            speedMultiplier = 0.6f;
            reloadMultiplier = 0.65f;
            damageMultiplier = 0.7f;
        }};
    
        emp3 = new NHStatusEffect("emp-3"){{
            damage = 0.25f;
            effect = NHFx.emped;
            effectChance = 0.3f;
            reactive = false;
            speedMultiplier = 0.4f;
            reloadMultiplier = 0.5f;
            damageMultiplier = 0.6f;
        }};
    }
	
	public static class NHStatusEffect extends StatusEffect{
		public Color textureColor = null;
		
		public NHStatusEffect(String name){
			super(name);
		}
		
		@Override
		public void createIcons(MultiPacker packer){
			if((fullIcon != null && fullIcon.found() && fullIcon instanceof TextureAtlas.AtlasRegion)){
				if(textureColor != null){
					packer.add(MultiPacker.PageType.main, name + "-full", NHUnitOutline.fillColor(Core.atlas.getPixmap(fullIcon), textureColor).outline(Color.valueOf("404049"), 3));
				}else{
					packer.add(MultiPacker.PageType.main, name + "-full", Pixmaps.outline(Core.atlas.getPixmap(fullIcon), Color.valueOf("404049"), 3));
				}
			}
		}
	}
}
