package newhorizon;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.*;
import newhorizon.func.NHSetting;
import newhorizon.func.SettingDialog;

import java.io.IOException;

import static newhorizon.func.TableFuncs.*;


public class NewHorizon extends Mod{
	public static final String NHNAME = "new-horizon-";
	
	
	
	private void links(){
		BaseDialog dialog = new BaseDialog("@links");
		addLink(dialog.cont, Icon.github, "Github", "https://github.com/Yuria-Shikibe/NewHorizonMod.git");
		dialog.cont.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN).padLeft(OFFSET / 2);
		dialog.show();
	}
	
	private void addLink(Table table, TextureRegionDrawable icon, String buttonName, String link){
		table.button(buttonName, icon, Styles.cleart, () -> {
			BaseDialog dialog = new BaseDialog("@link");
			dialog.addCloseListener();
			dialog.cont.pane(t -> t.add("[gray]Are you sure jump to this link: [accent]" + link + " [gray]?")).fillX().height(LEN / 2f).row();
			dialog.cont.image().fillX().pad(8).height(4f).color(Pal.accent).row();
			dialog.cont.pane(t -> {
				t.button("@back", Icon.left, Styles.cleart, dialog::hide).size(LEN * 3, LEN);
				t.button("@confirm", Icon.link, Styles.cleart, () -> Core.app.openURI(link)).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).fillX();
			dialog.show();
		}).size(LEN * 3, LEN).left().row();
	}
	
    public NewHorizon(){
        Log.info("Loaded NewHorizon Mod constructor.");
        Events.on(ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
			BaseDialog dialog = new BaseDialog("Welcome");
			dialog.addCloseListener();
			dialog.cont.pane(table -> {
				table.image(Core.atlas.find(NHNAME + "upgrade")).row();
				table.image().width(LEN * 5).height(OFFSET / 2.5f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("[white]<< Powered by NewHorizonMod >>", Styles.techLabel).row();
				table.image().width(LEN * 5).height(OFFSET / 2.5f).pad(OFFSET / 3f).color(Color.white).row();
				table.add("").row();
			}).grow().center().row();
			dialog.cont.table(Tex.clear, table -> {
				table.button("@back", Icon.left, Styles.transt, () -> {
					dialog.hide();
					NHSetting.settingApply();
				}).size(LEN * 3, LEN);
				table.button("@links", Icon.link, Styles.transt, this::links).size(LEN * 3, LEN).padLeft(OFFSET / 2);
				table.button("@settings", Icon.book, Styles.transt, () -> new SettingDialog().show()).size(LEN * 3, LEN).padLeft(OFFSET / 2);
			}).fillX().height(LEN + OFFSET);
			dialog.show();
			tableMain();
        }));
    }
    
    @Override
    public void loadContent(){
	    try{
		    NHSetting.settingFile();
		    NHSetting.initSetting();
		    NHSetting.initSettingList();
	    }catch(IOException e){
		    throw new RuntimeException(e);
	    }
	    Log.info("Loading NewHorizon Mod Objects");
		NHSounds.load();
		NHLoader loader = new NHLoader();
		loader.load();
	    new NHItems().load();
	    new NHLiquids().load();
		new NHUpgradeDatas().load();
		new NHUnits().load();
		new NHBlocks().load();
		//new NHPlanets().load();
	    new NHTechTree().load();
	    loader.loadLast();
    }
	
	
}
