package newhorizon;

import arc.*;
import mindustry.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.style.TextureRegionDrawable;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import newhorizon.contents.units.*;
import newhorizon.contents.items.*;
import newhorizon.contents.blocks.*;
import newhorizon.contents.blocks.special.*;
import newhorizon.contents.blocks.turrets.*;
import newhorizon.contents.effects.NHFx;
import newhorizon.contents.bullets.*;


public class NewHorizon extends Mod{
	public static final String NHNAME = "new-horizon-";
	
	private void confirm(String link){
		Dialog dialog = new Dialog("");
		dialog.cont.add("[lightgray]Are you sure jump to this link: [accent]" + link + " [lightgray]?").row();
		dialog.cont.image().fillX().pad(8).height(4f).color(Pal.accent).row();
		dialog.cont.pane(table -> {
			table.button("Yes", () -> Core.app.openURI(link)).size(120f, 60f);
			table.button("No", dialog::hide).size(120f, 60f).pad(4);
		}).size(260f, 70f);
		
		dialog.show();
	}
	
	private void links(){
		BaseDialog dialog = new BaseDialog("Links");
		dialog.cont.button(Icon.github, () -> {
			confirm("https://github.com/Yuria-Shikibe/NewHorizonMod.git");
		}).size(120f, 60f).left().row();
		
		dialog.cont.button("Back", dialog::hide).size(120f, 60f);
		dialog.show();
	}
	
    public NewHorizon(){
        Log.info("Loaded NewHorizon Mod constructor.");
        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("Welcome");
                dialog.cont.image(Core.atlas.find(NHNAME + "upgrade2")).row();
                dialog.cont.add("").row();
                dialog.cont.add("<<-Powered by NewHorizonMod->>").row();
                dialog.cont.pane(table -> {
					table.button("Dismiss", dialog::hide).size(120f, 60f);
					table.button(Icon.export, () -> {
						links();
					}).size(120f, 60f).pad(4);
				}).size(260f, 70f);
                dialog.show();
            });
        });
    }
    
	private void loadUnitEntities(){
		EntityMapping.nameMap.put(NHNAME + "tarlidor", EntityMapping.idMap[25]);
	}
	
    @Override
    public void loadContent(){
		Log.info("Loading NewHorizon Mod Objects");
		
		loadUnitEntities();
		Log.info("Unit Entities Loaded");
		new NHItems().load();
		new NHLiquids().load();
		new NHFx().load();
		new NHBullets().load();
		new NHBlocks().load();
		new NHFactories().load();
		new NHTurrets().load();
		new NHUnits().load();
		Log.info("Units Loaded");
    }
	
}
