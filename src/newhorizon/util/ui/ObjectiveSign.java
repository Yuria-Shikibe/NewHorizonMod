package newhorizon.util.ui;

import arc.func.Boolp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.actions.Actions;
import mindustry.graphics.Pal;

public class ObjectiveSign extends Element{
	public Color outerColor = Color.lightGray;
	public Color innerColor = Pal.accent;
	
	public float margin = 6, outerStroke = 6, pad = 7;
	
	public Boolp trigger = () -> false;
	public boolean finished = false;
	
	protected float lerpAlpha = 1;
	protected float outerSize = 12, innerSize = 6;
	
	public ObjectiveSign(){
	
	}
	
	public ObjectiveSign(Color outerColor, Color innerColor, float margin, float outerStroke, float pad, Boolp trigger){
		this.outerColor = outerColor;
		this.innerColor = innerColor;
		this.margin = margin;
		this.outerStroke = outerStroke;
		this.pad = pad;
		this.trigger = trigger;
	}
	
	public Element setTrigger(Boolp boolp){
		this.trigger = boolp;
		return this;
	}
	
	public void setFinished(boolean b){
		finished = b;
	}
	
	public void setMargin(float margin){
		this.margin = margin;
	}
	
	@Override
	public void draw(){
		super.draw();
		
		Draw.color(innerColor);
		Draw.alpha(lerpAlpha * color.a);
		Fill.square(x + width / 2, y + height / 2, innerSize * (3 - lerpAlpha * 2), 45);
		
		Lines.stroke(outerStroke);
		Draw.color(innerColor, outerColor, lerpAlpha);
		Draw.alpha(color.a);
		Lines.square(x + width / 2, y + height / 2, (outerSize - outerStroke / 2) * color.a, 45);
		
		Draw.color();
	}
	
	@Override
	public void act(float delta){
		super.act(delta);
		
		if(!finished)finished = trigger.get();
		else{
			lerpAlpha = Mathf.approachDelta(lerpAlpha, 0, 0.075f);
		}
		
		if(hasParent())color.a(parent.color.a);
		
		outerSize = Math.max(Math.min(width, height) - margin * 2, 0) / 2 * Mathf.sqrt2;
		innerSize = Math.max(outerSize - pad * 2, 0) / 2 * Mathf.sqrt2;
	}
	
	public void fadeOut(){
		actions(Actions.delay(1.5f), Actions.alpha(0, 0.45f, Interp.fade), Actions.remove());
	}
}
