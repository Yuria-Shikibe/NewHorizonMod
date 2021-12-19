package newhorizon.util.ui;

import arc.Core;
import arc.func.Boolp;
import arc.util.Nullable;
import arc.util.Structs;
import mindustry.Vars;
import mindustry.ui.fragments.HintsFragment;
import newhorizon.expand.vars.EventListeners;

public class Hints{
	
	public enum DefaultHint implements HintsFragment.Hint{
		raid_setup(visibleAll, () -> EventListeners.raid_setup)
		;
		
		public static boolean[] toBeTrigger = new boolean[values().length];
		@Nullable
		String text;
		int visibility = visibleAll;
		HintsFragment.Hint[] dependencies = {};
		boolean finished, cached;
		Boolp complete, shown = () -> true;
		
		public static final DefaultHint[] all = values();
		
		DefaultHint(Boolp complete){
			this.complete = complete;
		}
		
		DefaultHint(int visiblity, Boolp complete){
			this(complete);
			this.visibility = visiblity;
		}
		
		DefaultHint(Boolp shown, Boolp complete){
			this(complete);
			this.shown = shown;
		}
		
		DefaultHint(int visiblity, Boolp shown, Boolp complete){
			this(complete);
			this.shown = shown;
			this.visibility = visiblity;
		}
		
		@Override
		public boolean finished(){
			if(!cached){
				cached = true;
				finished = Core.settings.getBool(name() + "-hint-done", false);
			}
			return finished;
		}
		
		@Override
		public void finish(){
			Core.settings.put(name() + "-hint-done", finished = true);
		}
		
		@Override
		public String text(){
			if(text == null){
				text = Vars.mobile && Core.bundle.has("hint." + name() + ".mobile") ? Core.bundle.get("hint." + name() + ".mobile") : Core.bundle.get("hint." + name());
				if(!Vars.mobile) text = text.replace("tap", "click").replace("Tap", "Click");
			}
			return text;
		}
		
		@Override
		public boolean complete(){
			return complete.get();
		}
		
		@Override
		public boolean show(){
			return shown.get() && (dependencies.length == 0 || !Structs.contains(dependencies, d -> !d.finished()));
		}
		
		@Override
		public int order(){
			return ordinal();
		}
		
		@Override
		public boolean valid(){
			return (Vars.mobile && (visibility & visibleMobile) != 0) || (!Vars.mobile && (visibility & visibleDesktop) != 0);
		}
	}
}
