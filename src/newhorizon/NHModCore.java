package newhorizon;

import arc.ApplicationListener;
import mindustry.Vars;
import newhorizon.expand.cutscene.NHCSS_Core;

import static newhorizon.NHVars.renderer;

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

	public void worldInit(){
		if(!Vars.headless){
			renderer.init();
		}
	}
}
