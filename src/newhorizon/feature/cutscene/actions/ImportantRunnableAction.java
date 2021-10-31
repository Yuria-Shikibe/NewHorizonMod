package newhorizon.feature.cutscene.actions;

import arc.scene.actions.RunnableAction;

public class ImportantRunnableAction extends RunnableAction implements ImportantAction{
	
	@Override
	public void accept(){
		run();
	}
}
