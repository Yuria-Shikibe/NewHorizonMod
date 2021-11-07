package newhorizon.util.feature.cutscene.events;

import arc.Core;
import arc.func.Func;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import newhorizon.content.NHContent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.func.NHInterp;

public class DestroyObjectiveEvent extends ObjectiveEvent{
	public Block targetBlock = null;
	public Func<CutsceneEventEntity, Seq<Building>> targets = e -> Seq.with();
	
	public DestroyObjectiveEvent(String name){
		super(name);
		
		trigger = e -> targets.get(e).isEmpty();
		info = e ->
			Core.bundle.format("nh.cutscene.event.destroy-objective",
				targetBlock == null ? Core.bundle.get("nh.cutscene.event.destroy-target") : targetBlock.localizedName,
				targets.get(e).size
			);
		
		drawable = true;
	}
	
	@Override
	public void draw(CutsceneEventEntity e){
		Draw.alpha(0.55f);
		Draw.blend(Blending.additive);
		Draw.z(Layer.legUnit + 1);
		
		for(Building b : targets.get(e)){
			Draw.tint(b.team.color);
			
			float p = Time.time % 100 / 100;
			
			for(int i = 0; i < 4; i++){
				float length = b.block.size / 2f * Vars.tilesize + 6 + Interp.smoother.apply(Interp.slope.apply(p)) * 3;
				float ang = i * 90 + 45 + 90 * NHInterp.pow10.apply(Mathf.curve(p, 0.2f, 0.8f));
				Tmp.v1.trns(ang, -length);
				Draw.rect(NHContent.arrowRegion, b.x + Tmp.v1.x, b.y + Tmp.v1.y, ang + 90);
			}
			
			Lines.stroke(Vars.tilesize / 2f);
			Lines.poly(b.x, b.y, 4, b.block.size / 2f * Vars.tilesize);
		}
		
		Draw.blend();
		Draw.reset();
	}
	
	@Override
	public void updateEvent(CutsceneEventEntity e){
		super.updateEvent(e);
		
		if(!Vars.headless)e.set(Core.camera.position);
	}
}
