package newhorizon.expand.block.drawer;

import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.draw.DrawBlock;

public class DrawScanLine extends DrawBlock implements Cloneable{
	public float lineLength = 12;
	public float lineStroke = 0.786f;
	public float scanLength = 16f;
	public float scanAngle = 0;
	public float phaseOffset = 0;
	
	public float strokeScl = 4;
	public float strokePlusScl = 0.25f;
	
	public float totalProgressMultiplier = 1;
	
	public float scanScl = 6;
	public Blending blending = Blending.additive;
	
	public float alpha = 0.67f;
	public Color colorFrom = Pal.accent, colorTo = Color.white;
	public float colorLerpRatio = 0.53f;
	public float colorLerpScl = 3.3f;
	
	public float x, y;
	
	@Override
	public void draw(Building build){
		Draw.blend(blending);
		Draw.color(colorFrom, colorTo, Mathf.absin(build.totalProgress() * totalProgressMultiplier + phaseOffset, colorLerpScl, colorLerpRatio));
		Draw.alpha(alpha * build.warmup());
		
		float stroke = lineStroke * (1 + Mathf.absin(build.totalProgress() * totalProgressMultiplier, strokeScl, strokePlusScl)) * build.warmup();
		
		Lines.stroke(stroke);
		
		Tmp.v1.trns(scanAngle, Mathf.sin(build.totalProgress() * totalProgressMultiplier + phaseOffset, scanScl, (scanLength - stroke / 2f) / 2f)).add(x, y).add(build.x, build.y);
		Lines.lineAngleCenter(Tmp.v1.x, Tmp.v1.y, scanAngle + 90, scanLength * build.warmup() - stroke);
		
		Draw.reset();
		Draw.blend();
	}
	
	public DrawScanLine copyAnd(Cons<DrawScanLine> modifier){
		DrawScanLine n = copy();
		modifier.get(n);
		return n;
	}
	
	public DrawScanLine copy(){
		try{
			return (DrawScanLine)super.clone();
		}catch(CloneNotSupportedException e){
			throw new AssertionError();
		}
	}
}
