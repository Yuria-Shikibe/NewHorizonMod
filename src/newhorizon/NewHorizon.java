package newhorizon;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.style.TextureRegionDrawable;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

//import newhorizon.contents.units.*;
import newhorizon.contents.items.*;
import newhorizon.contents.blocks.*;
import newhorizon.contents.blocks.special.*;
import newhorizon.contents.blocks.turrets.*;
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.bullets.*;


public class NewHorizon extends Mod{
	public static final String NHNAME = "new-horizon-";
	
    public NewHorizon(){
        Log.info("Loaded NewHorizon Mod constructor.");
        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("Welcome");
                dialog.cont.image(Core.atlas.find(NHNAME + "upgrade")).row();
                dialog.cont.add("").row();
                dialog.cont.add("<<-Powered by NewHorizonMod->>").row();
                dialog.cont.button("Dismiss", dialog::hide).size(120f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
		Log.info("Loading NewHorizon Mod Objects");
		new NHItems().load();
		new NHLiquids().load();
		new NHFx().load();
		new NHBullets().load();
		new NHBlocks().load();
		new NHFactories().load();
		new NHTurrets().load();
		//new NHUnits().load();
    }
	
}
