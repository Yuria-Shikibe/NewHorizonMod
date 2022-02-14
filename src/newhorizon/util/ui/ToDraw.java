package newhorizon.util.ui;

import arc.graphics.g2d.Draw;
import arc.scene.Element;
import newhorizon.util.EventListeners;

public class ToDraw extends Element{
	public Runnable drawer;
	
	public ToDraw(float layer, Runnable drawer){
		EventListeners.toDraw.add((this.drawer = () -> Draw.draw(layer, drawer)));
	}
	
	{
		width = height = 0;
		visible = false;
		cullable = false;
	}
	
	@Override
	public void setSize(float size){
		super.setSize(0);
	}
	
	@Override
	public void setSize(float width, float height){
		super.setSize(0, 0);
	}
	
	@Override
	public void act(float delta){
		super.act(delta);
		
		if(!visible)EventListeners.toDraw.remove(drawer);
	}
	
	@Override
	public boolean remove(){
		EventListeners.toDraw.remove(drawer);
		return super.remove();
	}
	
	@Override
	public void draw(){}
}
