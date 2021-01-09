package newhorizon;

import arc.*;
import arc.graphics.Color;
import mindustry.graphics.*;
import arc.scene.ui.layout.*;
import arc.scene.style.TextureRegionDrawable;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

import newhorizon.content.*;

import static newhorizon.func.TableFuncs.LEN;
import static newhorizon.func.TableFuncs.OFFSET;


public class NewHorizon extends Mod{
	public static final String NHNAME = "new-horizon-";
	
	private void links(){
		BaseDialog dialog = new BaseDialog("@links");
		addLink(dialog.cont, Icon.github, "Github", "https://github.com/Yuria-Shikibe/NewHorizonMod.git");

		//addLink(dialog.cont, Icon.host, "", "https://github.com/Yuria-Shikibe/NewHorizonMod.git");
		dialog.cont.button("@back", dialog::hide).size(120f, 60f);
		dialog.show();
	}
	
	private void addLink(Table table, TextureRegionDrawable icon, String buttonName, String link){
		table.button(buttonName, icon, () -> {
			BaseDialog dialog = new BaseDialog("LINK");
			dialog.addCloseListener();
			dialog.cont.pane(t -> t.add("[gray]Are you sure jump to this link: [accent]" + link + " [gray]?")).fillX().height(LEN / 2f).row();
			dialog.cont.image().fillX().pad(8).height(4f).color(Pal.accent).row();
			dialog.cont.pane(t -> {
				t.button("@confirm", Icon.link, () -> Core.app.openURI(link)).size(220f, 60f);
				t.button("@back", Icon.left, dialog::hide).size(220f, 60f).pad(4);
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
			}).fill().center().row();
			dialog.cont.table(Tex.clear, table -> {
				table.button("@back", Icon.left, Styles.transt, dialog::hide).size(LEN * 3, LEN);
				table.button("@links", Icon.link, Styles.transt, this::links).size(LEN * 3, LEN).pad(OFFSET / 3f);
			}).fillX().height(LEN + OFFSET);
			dialog.show();
		}));
    }

    @Override
    public void loadContent(){
		Log.info("Loading NewHorizon Mod Objects");
		new NHItems().load();
		new NHLiquids().load();
		new NHUnits().load();
		new NHBlocks().load();
		new NHTechTree().load();
		new NHPlanets().load();
    }
	
	
}
