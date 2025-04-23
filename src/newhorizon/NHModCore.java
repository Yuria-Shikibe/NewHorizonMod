package newhorizon;

import arc.ApplicationListener;
import mindustry.Vars;

import static newhorizon.NHVars.*;

public class NHModCore implements ApplicationListener{
	public NHModCore(){}
	
	@Override
	public void update(){
		if(Vars.state.isPlaying()){
			cutscene.update();
			NHGroups.update();
			if(!Vars.headless){
				if(listener != null)listener.update();
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
}
