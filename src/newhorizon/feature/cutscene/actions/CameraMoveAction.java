package newhorizon.feature.cutscene.actions;

import arc.Core;
import arc.math.Mathf;
import arc.scene.actions.TemporalAction;

public class CameraMoveAction extends TemporalAction{
	public float startX, startY;
	public float endX, endY;
	
	@Override
	protected void begin(){
		if(Mathf.equal(startX, endX) && Mathf.equal(startY, endY)) return;
		startX = Core.camera.position.x;
		startY = Core.camera.position.y;
	}
	
	@Override
	protected void update(float percent){
		Core.camera.position.set(startX + (endX - startX) * percent, startY + (endY - startY) * percent);
	}
	
	@Override
	public void reset(){
		super.reset();
	}
	
	public void setPosition(float x, float y){
		endX = x;
		endY = y;
	}
	
	public float getX(){
		return endX;
	}
	
	public void setX(float x){
		endX = x;
	}
	
	public float getY(){
		return endY;
	}
	
	public void setY(float y){
		endY = y;
	}
}
