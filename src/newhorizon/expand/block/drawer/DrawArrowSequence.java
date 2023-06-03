package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.part.DrawPart;
import mindustry.graphics.Layer;
import newhorizon.content.NHContent;

public class DrawArrowSequence extends DrawPart{
	public float x, y, rotation;
	public TextureRegion arrowRegion;
	public String arrowName;
	
	public float layer = Layer.bullet, layerOffset = 0f;
	
	public Color color = Color.white;
	public Color colorTo = Color.white;
	public float colorToFinScl = 0.7f;
	public Blending blending = Blending.normal;
	public float opacity = 1f;
	public PartProgress progress = PartProgress.reload.inv().mul(PartProgress.warmup);
	public PartProgress colorProgress = PartProgress.reload.inv().mul(PartProgress.warmup).compress(0.35f, 0.75f);
	
	public float spacing = 12f;
	public int arrows = 8;
	public float scl = 12f;
	public float maintainScl = 0.125f;
//	public float
	
	public DrawArrowSequence(){
	}
	
	public DrawArrowSequence(String arrowName){
		this.arrowName = arrowName;
	}
	
	@Override
	public void draw(PartParams params){
		float z = Draw.z();
		if(layer > 0) Draw.z(layer);
		if(under && turretShading) Draw.z(z - 0.0001f);
		Draw.z(Draw.z() + layerOffset);
		
		float prevZ = Draw.z();
		float fin = progress.getClamp(params);
		float fout = 1 - fin;
		
		//noinspection SuspiciousNameCombination
		Tmp.v5.trns(params.rotation, y, x).add(params.x, params.y);
		TextureRegion arrowRegion = NHContent.arrowRegion;
		
		Draw.color(color, colorTo, colorToFinScl * colorProgress.get(params));
		Draw.blend(blending);
		Draw.alpha(opacity);
		
		float railF = Mathf.curve(Interp.pow2Out.apply(fin), 0f, 0.25f) * Mathf.curve(Interp.pow4Out.apply(fout), 0f, 0.3f) * fin;
		
		for(int i = 0; i < arrows; i++){
			Tmp.v1.trns(params.rotation + rotation, i * spacing).add(Tmp.v5);
			float f = Interp.pow3Out.apply(Mathf.clamp(fin * arrows - i)) * (0.6f + railF * 0.4f);
			Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, arrowRegion.width * Draw.scl * f, arrowRegion.height * Draw.scl * f, params.rotation - 90);
		}
		
		Draw.blend();
		Draw.reset();
		Draw.z(prevZ);
	}
	
	@Override
	public void load(String name){
		if(arrowName != null){
			arrowRegion = Core.atlas.find(arrowName);
		}else arrowRegion = NHContent.arrowRegion;
	}
	
	
}
