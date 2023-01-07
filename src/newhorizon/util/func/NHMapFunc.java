package newhorizon.util.func;

import mindustry.Vars;
import mindustry.type.Sector;

public class NHMapFunc{
	public static void sectorToNormal(){
		Sector sector = Vars.state.rules.sector;
		if(sector == null){
			Vars.ui.showInfoToast("Null Sector", 1);
		}else{
			Vars.state.rules.sector = null;
			Vars.ui.showInfoToast("Swap Succeed", 1);
		}
	}
}
