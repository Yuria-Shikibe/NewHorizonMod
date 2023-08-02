package newhorizon.expand.block.drawer;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.RegionPart;
import mindustry.graphics.Drawf;

public class ForceHeatDrawer extends RegionPart{
	public ForceHeatDrawer(String region){
		super(region);
	}
	
	@Override
	public void draw(PartParams params){
		float z = Draw.z();
		if(layer > 0) Draw.z(layer);
		//TODO 'under' should not be special cased like this...
		if(under && turretShading) Draw.z(z - 0.0001f);
		Draw.z(Draw.z() + layerOffset);
		
		float prevZ = Draw.z();
		float prog = progress.getClamp(params), sclProg = growProgress.getClamp(params);
		float mx = moveX * prog, my = moveY * prog, mr = moveRot * prog + rotation,
				gx = growX * sclProg, gy = growY * sclProg;
		
		if(moves.size > 0){
			for(int i = 0; i < moves.size; i++){
				PartMove move = moves.get(i);
				float p = move.progress.getClamp(params);
				mx += move.x * p;
				my += move.y * p;
				mr += move.rot * p;
				gx += move.gx * p;
				gy += move.gy * p;
			}
		}
		
		int len = mirror && params.sideOverride == -1 ? 2 : 1;
		float preXscl = Draw.xscl, preYscl = Draw.yscl;
		Draw.xscl *= xScl + gx;
		Draw.yscl *= yScl + gy;
		
		for(int s = 0; s < len; s++){
			//use specific side if necessary
			int i = params.sideOverride == -1 ? s : params.sideOverride;
			
			//can be null
			TextureRegion region = drawRegion ? regions[Math.min(i, regions.length - 1)] : null;
			float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
			Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);
			
			float
					rx = params.x + Tmp.v1.x,
					ry = params.y + Tmp.v1.y,
					rot = mr * sign + params.rotation - 90;
			
			Draw.xscl *= sign;
			
			if(outline && drawRegion){
				Draw.z(prevZ + outlineLayerOffset);
				Draw.rect(outlines[Math.min(i, regions.length - 1)], rx, ry, rot);
				Draw.z(prevZ);
			}
			
			if(drawRegion && region.found()){
				if(color != null && colorTo != null){
					Draw.color(color, colorTo, prog);
				}else if(color != null){
					Draw.color(color);
				}
				
				if(mixColor != null && mixColorTo != null){
					Draw.mixcol(mixColor, mixColorTo, prog);
				}else if(mixColor != null){
					Draw.mixcol(mixColor, mixColor.a);
				}
				
				Draw.blend(blending);
				Draw.rect(region, rx, ry, rot);
				Draw.blend();
				if(color != null) Draw.color();
			}
			
			if(heat.found()){
				float hprog = heatProgress.getClamp(params);
				heatColor.write(Tmp.c1).a(hprog * heatColor.a);
				Drawf.additive(heat, Tmp.c1, rx, ry, rot, Draw.z() + heatLayerOffset);
				if(heatLight) Drawf.light(rx, ry, heat, rot, Tmp.c1, heatLightOpacity * hprog);
			}
			
			Draw.xscl *= sign;
		}
		
		Draw.color();
		Draw.mixcol();
		
		Draw.z(z);
		
		//draw child, if applicable - only at the end
		//TODO lots of copy-paste here
		if(children.size > 0){
			for(int s = 0; s < len; s++){
				int i = (params.sideOverride == -1 ? s : params.sideOverride);
				float sign = (i == 1 ? -1 : 1) * params.sideMultiplier;
				Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);
				
				childParam.set(params.warmup, params.reload, params.smoothReload, params.heat, params.recoil, params.charge, params.x + Tmp.v1.x, params.y + Tmp.v1.y, i * sign + mr * sign + params.rotation);
				childParam.sideMultiplier = params.sideMultiplier;
				childParam.life = params.life;
				childParam.sideOverride = i;
				for(DrawPart child : children){
					child.draw(childParam);
				}
			}
		}
		
		Draw.scl(preXscl, preYscl);
	}
}
