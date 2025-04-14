package newhorizon;

import arc.Core;
import arc.scene.Group;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.ui.CoreItemsDisplay;
import newhorizon.util.ui.TeamPayloadDisplay;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;

import static mindustry.Vars.ui;

public class NHUI{
	public static Table coreInfo;

	public static TeamPayloadDisplay payloadDisplay;
	public static NHWorldSettingDialog nhWorldSettingDialog;
	
	public static void init(){
		nhWorldSettingDialog = new NHWorldSettingDialog();

		try{
			coreInfo = ui.hudGroup.find("coreinfo");
			payloadDisplay = new TeamPayloadDisplay();
			//coreInfo.top().add(payloadDisplay);
		}catch (Exception e){
			Log.err(e);
		}
	}

	public static float getWidth(){
		return Core.graphics.getWidth();
	}

	public static float getHeight(){
		return Core.graphics.getHeight();
	}
}
