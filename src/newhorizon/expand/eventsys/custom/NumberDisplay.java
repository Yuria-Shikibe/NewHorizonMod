package newhorizon.expand.eventsys.custom;

import arc.Core;
import arc.func.Func;
import mindustry.Vars;
import mindustry.core.UI;

public enum NumberDisplay{
	def(String::valueOf), time(f -> UI.formatTime(f.floatValue())), tileRange(f -> Core.bundle.format("bullet.range", f.floatValue() / Vars.tilesize));
	
	NumberDisplay(Func<Number, CharSequence> toDisplay){
		this.toDisplay = toDisplay;
	}
	
	public Func<Number, CharSequence> toDisplay;
}
