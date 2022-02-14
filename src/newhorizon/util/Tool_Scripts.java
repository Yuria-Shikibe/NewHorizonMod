package newhorizon.util;

import arc.Core;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;
import newhorizon.util.feature.cutscene.CutsceneScript;
import rhino.Context;
import rhino.ImporterTopLevel;
import rhino.NativeJavaObject;
import rhino.Undefined;

public class Tool_Scripts implements Disposable{
	public final Context context;
	public final ImporterTopLevel scope;
	
	protected boolean error;
	protected Mods.LoadedMod mod;
	protected ClassLoader arcLoader;
	
	public Tool_Scripts(){
		Time.mark();
		mod = Vars.mods.getMod(NewHorizon.class);
		
		context = Vars.platform.getScriptContext();
		scope = new ImporterTopLevel(context);
		
		if(!run(Core.files.internal("scripts/global.js").readString(), "Global", false)){
			error = true;
		}
		
		arcLoader = context.getApplicationClassLoader();
		context.setApplicationClassLoader(mod.loader);
		
		if(!run(CutsceneScript.getModGlobalJSCode(), "cutsceneLoader.js", false)){
			error = true;
		}
		
		context.setApplicationClassLoader(arcLoader);
		
		
		Log.info("[["+ mod.meta.displayName + "]Time to load cutscene script engine: @", Time.elapsed());
	}
	
	public boolean hasErrored(){
		return error;
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
		context.setApplicationClassLoader(mod.loader);
		run(code, "cutscene.js", true);
		context.setApplicationClassLoader(arcLoader);
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
