package newhorizon.expand.units;

import mindustry.graphics.Pal;
import newhorizon.content.NHColor;
import newhorizon.content.NHUnitTypes;

public class AncientUnit extends NHUnitTypes.NHUnitType{
	public boolean immuniseAll = true;
	
	
	public AncientUnit(String name){
		super(name);
		
		outlineColor = Pal.darkOutline;
		healColor = NHColor.ancientLightMid;
		lightColor = NHColor.ancientLightMid;
	}
	
	@Override
	public void init(){
		super.init();
		
		if(immuniseAll){
			NHUnitTypes.immunise(this);
		}
	}
}
