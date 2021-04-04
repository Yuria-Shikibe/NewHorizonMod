package newhorizon.feature;

import arc.Core;
import mindustry.type.StatusEffect;
import newhorizon.NewHorizon;
import newhorizon.content.NHLoader;

public class NHStatusEffect extends StatusEffect{
	public NHStatusEffect(String name){
		super(name);
		NHLoader.put(name + "@@404049");
		
		localizedName = Core.bundle.get("status." + NewHorizon.configName(name) + ".name");
	}
}
