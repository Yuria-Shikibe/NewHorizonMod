package newhorizon;

import arc.ApplicationListener;
import arc.Core;
import arc.util.Log;
import mindustry.Vars;
import newhorizon.expand.entities.NHGroups;
import newhorizon.expand.vars.NHVars;

import java.io.IOException;

public class NHApplicationCore implements ApplicationListener{
	public NHApplicationCore(){
		if(NewHorizon.DEBUGGING)Log.info("NH Listener Core Constructed");
	}
	
	@Override
	public void update(){
		if(Vars.state.isPlaying()){
			NHGroups.update();
			NHVars.update();
		}
	}
	
	@Override
	public void dispose(){
		if(NewHorizon.DEBUGGING)Log.info("Disposed");
		
		if(Core.app.isHeadless()){
			try{
				Runtime.getRuntime().exec("cmd /k start java -Xmx1024M -Xms1024M -jar server-release.jar");
				Log.info("Restarting...");
			}catch(IOException e){
				Log.err("Restarting IO Failed");
				Log.err(e);
			}
		}
	}
	
	@Override
	public void init(){
	
	}
}
