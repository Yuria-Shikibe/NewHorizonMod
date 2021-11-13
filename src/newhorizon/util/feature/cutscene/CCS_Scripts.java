package newhorizon.util.feature.cutscene;

import arc.Core;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;
import rhino.*;
import rhino.module.RequireBuilder;
import rhino.module.provider.ModuleSource;
import rhino.module.provider.SoftCachingModuleScriptProvider;
import rhino.module.provider.UrlModuleSourceProvider;

import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class CCS_Scripts implements Disposable{
	public final Context context;
	public final ImporterTopLevel scope;
	
	protected ClassLoader formalLoader = null;
	
	protected boolean errored;
	protected static Mods.LoadedMod mod;
	
	public CCS_Scripts(){
		Time.mark();
		mod = Vars.mods.getMod(NewHorizon.class);
		
		context = Vars.platform.getScriptContext();
		scope = new ImporterTopLevel(context);
		
		formalLoader = context.getApplicationClassLoader();
		
		context.setApplicationClassLoader(mod.loader);
		
		new RequireBuilder()
				.setModuleScriptProvider(new SoftCachingModuleScriptProvider(new CCS_ScriptModuleProvider()))
				.setSandboxed(true).createRequire(context, scope).install(scope);
		
		if(!run(Core.files.internal("scripts/global.js").readString() + CutsceneScript.getModGlobalJSCode(), "CCS_Importer", false)){
			errored = true;
		}
		
		Log.debug("Time to load cutscene script engine: @", Time.elapsed());
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
	
	protected class CCS_ScriptModuleProvider extends UrlModuleSourceProvider{
		protected final Pattern directory = Pattern.compile("^(.+?)/(.+)");
		
		public CCS_ScriptModuleProvider(){
			super(null, null);
		}
		
		@Override
		public ModuleSource loadSource(String moduleId, Scriptable paths, Object validator) throws URISyntaxException{
			return null;
		}
	}
	
	public static class CCS_ContextFactory extends ContextFactory{
		
		@Override
		protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args){
			return super.doTopCall(callable, cx, scope, thisObj, args);
		}
		
		@Override
		protected Context makeContext(){
			return new CCS_Context(this);
		}
		
		public static class CCS_Context extends Context{
			public CCS_Context(ContextFactory factory){
				super(factory);
				setApplicationClassLoader(mod.loader);
			}
		}
	}
}
