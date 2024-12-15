package newhorizon.expand.cutscene.actions;

import mindustry.ai.types.CommandAI;

//ALL Cutscene AI should be worldReset after a load!
public class CutsceneAI extends CommandAI{
	@Override
	public boolean isLogicControllable(){
		return false;
	}
}
