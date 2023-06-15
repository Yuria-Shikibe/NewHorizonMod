package newhorizon.expand.eventsys.custom;

import mindustry.ui.dialogs.BaseDialog;
import newhorizon.expand.eventsys.types.RaidEventType;
import newhorizon.expand.eventsys.types.WorldEventType;

public class CustomUIGen extends BaseDialog{
	protected Customizer customizer;
	protected Class<? extends WorldEventType> eventType;
	
	public CustomUIGen(){
		super("UI Gen");
		
		gen();
	}
	
	public void gen(){
		customizer = Customizer.customizer.setContext(new RaidEventType("test"));
		eventType = RaidEventType.class;
		
		ParserRegistry.applyOnExit.clear();
		
		Customizer.EventEntry eventEntry = customizer.getEntry(eventType);
		cont.pane(main -> {
			main.margin(120f);
			eventEntry.buildTable(main);
		}).margin(120f).grow().row();
		
		addCloseButton();
	}
	
	@Override
	public void hide(){
		ParserRegistry.applyOnExit.each(Runnable::run);
		ParserRegistry.applyOnExit.clear();
		
		super.hide();
	}
}
