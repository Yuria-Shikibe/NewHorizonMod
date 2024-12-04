package newhorizon;

import arc.ApplicationListener;
import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import newhorizon.expand.NHVars;
import newhorizon.expand.cutscene.NHCSS_Core;

import static newhorizon.expand.NHVars.renderer;

public class NHModCore implements ApplicationListener{
	public NHModCore(){}
	
	@Override
	public void update(){
		if(Vars.state.isPlaying()){
			NHCSS_Core.core.update();
			NHGroups.update();
			if(!Vars.headless){
				if(NHVars.listener != null)NHVars.listener.update();
				renderer.statusRenderer.update();
				NHSetting.update();
			}
		}
	}

	@Override
	public void dispose() {
        ApplicationListener.super.dispose();
    }
	
	@Override
	public void init() {
        ApplicationListener.super.init();
    }

	public void initOnLoadWorld(){
		if(!Vars.headless){
			renderer.init();
		}
	}
}
