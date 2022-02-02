package newhorizon.util.graphic;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.graphics.Pal;
import newhorizon.content.NHShaders;
import newhorizon.util.func.NHSetting;

public class ShadowProcessor{
	private static final ObjectMap<Float, Seq<Runnable>> toPost = new ObjectMap<>();
	
	public static void clear(){
		toPost.clear();
	}
	
	public static void add(float z, Runnable drawer){
		if(!NHSetting.enableDetails()){
			Draw.color(Pal.shadow);
			drawer.run();
			Draw.color();
			return;
		}
		
		if(!toPost.containsKey(z))toPost.put(z, Seq.with(drawer));
		else toPost.get(z).add(drawer);
	}
	
	public static void post(){
		for(ObjectMap.Entry<Float, Seq<Runnable>> entry : toPost.entries()){
			Draw.draw(entry.key, () -> {
				Vars.renderer.effectBuffer.begin(Color.clear);
				
				entry.value.each(Runnable::run);
				
				
				Vars.renderer.effectBuffer.end();
				Vars.renderer.effectBuffer.blit(NHShaders.shadowShader.set(Pal.shadow));
			});
		}
	}
}
