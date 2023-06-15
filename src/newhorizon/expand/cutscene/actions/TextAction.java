package newhorizon.expand.cutscene.actions;

import newhorizon.expand.cutscene.NHCSS_Action;
import newhorizon.expand.cutscene.NHCSS_UI;

public class TextAction extends NHCSS_Action{
	public final NHCSS_UI.TextBox textBox;
	
	public TextAction setWait(){
		duration = textBox.duration;
		return this;
	}
	
	public TextAction(ActionBus bus, NHCSS_UI.TextBox textBox){
		super(bus);
		this.textBox = textBox;
	}
	
	@Override
	public void setup(){
		super.setup();
		
		NHCSS_UI.postText(textBox);
	}
}
