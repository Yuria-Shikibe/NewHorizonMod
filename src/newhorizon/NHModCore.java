package newhorizon;

import arc.ApplicationListener;
import arc.Core;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import newhorizon.expand.NHVars;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.util.func.NHFunc;

public class NHModCore implements ApplicationListener{
	public static NHModCore core;
	public static NHInputControl control;

	public NHInputListener inputListener;
	public NHRenderer renderer;
	
	public NHModCore(){
		if(NewHorizon.DEBUGGING)Log.info("NH Listener Core Constructed");
		
		if(!Core.app.isHeadless()){
			NHVars.listener = inputListener = new NHInputListener();
			NHVars.renderer = renderer = new NHRenderer();
			control = new NHInputControl();
		}
		
		NHVars.core = core = this;
	}
	
	@Override
	public void update(){
		if(Vars.state.isPlaying()){
			NHCSS_Core.core.update();
			NHGroups.update();
			if(!Vars.headless){
				if(inputListener != null)inputListener.update();
				renderer.effectDrawer.update();
				renderer.textureStretchIn.update();
				NHSetting.update();
			}
		}
	}
	
	@Override
	public void dispose(){
		if(NewHorizon.DEBUGGING)Log.info("Disposed");
	}
	
	@Override
	public void init(){
		if(NewHorizon.DEBUGGING)Log.info("Init NH Core");
	}
	
	public void initOnLoadWorld(){
		if(!Vars.headless){
			renderer.init();
		}
	}
}
