package newhorizon.util.feature.cutscene;

import arc.Core;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;
import rhino.Context;
import rhino.ImporterTopLevel;
import rhino.NativeJavaObject;
import rhino.Undefined;

public class CCS_Scripts implements Disposable{
	public final Context context;
	public final ImporterTopLevel scope;
	
	protected boolean errored;
	protected Mods.LoadedMod mod;
	
	public CCS_Scripts(){
		Time.mark();
		mod = Vars.mods.getMod(NewHorizon.class);
		
		context = Vars.platform.getScriptContext();
		scope = new ImporterTopLevel(context);
		
		context.setApplicationClassLoader(mod.loader);
		
		if(!run(Core.files.internal("scripts/global.js").readString(), "Global", false)){
			errored = true;
		}
		
		if(!run(CutsceneScript.getModGlobalJSCode(), "cutsceneLoader.js", false)){
			errored = true;
		}
		
		Log.info("[["+ mod.meta.displayName + "]Time to load cutscene script engine: @", Time.elapsed());
	}
	
	public boolean hasErrored(){
		return errored;
	}
	
	public String runConsole(String text){
		try{
			Object o = context.evaluateString(scope, text, "console.js", 1);
			if(o instanceof NativeJavaObject)o = ((NativeJavaObject)o).unwrap();
			if(o instanceof Undefined)o = "undefined";
			return String.valueOf(o);
		}catch(Throwable t){
			return getError(t, false);
		}
	}
	
	private String getError(Throwable t, boolean log){
		if(log) Log.err(t);
		return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
	}
	
	public void log(String source, String message){
		log(Log.LogLevel.info, source, message);
	}
	
	public void log(Log.LogLevel level, String source, String message){
		Log.log(level, "[@]: @", source, message);
	}
	
	public void run(Mods.LoadedMod mod, String code){
		run(code, "cutscene.js", true);
	}
	
	private boolean run(String script, String file, boolean wrap){
		try{
			context.evaluateString(scope,
					wrap ? "(function(){'use strict';" + script + "\n})();" : script,
					file, 0);
			return true;
		}catch(Throwable t){
			if(mod != null){
				file = mod.name + "/" + file;
			}
			log(Log.LogLevel.err, file, "" + getError(t, true));
			return false;
		}
	}
	
	@Override
	public void dispose(){
		Context.exit();
	}
}
