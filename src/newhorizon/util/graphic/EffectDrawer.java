package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.struct.Bits;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import newhorizon.util.annotation.HeadlessDisabled;

@HeadlessDisabled
public class EffectDrawer{
	public static EffectDrawer drawer = new EffectDrawer();
	public static final EffectRenderer none = ((effect, unit) -> {});
	
	public Seq<StatusEffect> registered = new Seq<>();
	public ObjectMap<StatusEffect, EffectRenderer> drawers = new ObjectMap<>();
	public ObjectIntMap<StatusEffect> zOrder = new ObjectIntMap<>();
	
	protected Seq<Runnable> drawTask = new Seq<>();
	protected Bits lastStatus = new Bits();
	
	public void register(StatusEffect effect, int order, EffectRenderer e){
		registered.add(effect);
		drawers.put(effect, e);
		zOrder.get(effect, order);
	}
	
	public void sort(){
		registered.sortComparing(this::getZ);
	}
	
	public int getZ(StatusEffect statusEffect){
		return zOrder.get(statusEffect, -1);
	}
	
	public EffectRenderer getRenderer(StatusEffect statusEffect){
		return drawers.get(statusEffect, none);
	}
	
	public interface EffectRenderer{
		void draw(StatusEffect effect, Unit unit);
	}
	
	public void draw(){
		if(Vars.player.unit() == null)return;
		if(!Vars.player.unit().statusBits().equals(lastStatus)){
			drawTask.clear();
			
			registered.each(effect -> {
				if(Vars.player.unit().statusBits().get(effect.id)){
					drawTask.add(() -> getRenderer(effect).draw(effect, Vars.player.unit()));
				}
			});
		}
		
		lastStatus = Vars.player.unit().statusBits();
		Draw.trans(Core.camera.mat);
		drawTask.each(Runnable::run);
	}
}
