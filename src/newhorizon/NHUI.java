package newhorizon;

import arc.Core;
import arc.scene.Group;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.ArcRuntimeException;
import mindustry.Vars;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;

import static mindustry.Vars.ui;

public class NHUI{
	//references:
	//HUD stuff
	public static Table HudFragment_overlaymarker, HUD_waves, HUD_statustable, HUD_status;
	//BuildFragment stuff
	public static Table buildFrag;
	public static Image buildFragBound;
	public static Group hudGroup;

	public static int hudIndex;

	public static WidgetGroup HUD_waves_editor;

	public static Table eventSimplePane = new Table();
	
	public static NHWorldSettingDialog nhWorldSettingDialog;

	public static float getWidth(){
		return Core.graphics.getWidth();
	}
	
	public static float getHeight(){
		return Core.graphics.getHeight();
	}
	
	public static void init(){
		nhWorldSettingDialog = new NHWorldSettingDialog();

		try{
			HudFragment_overlaymarker = Vars.ui.hudGroup.find("overlaymarker");
			HUD_waves_editor = HudFragment_overlaymarker.find("waves/editor");
			HUD_waves = HUD_waves_editor.find("waves");
			HUD_statustable = HUD_waves.find("statustable");
			HUD_status = HUD_statustable.find("status");

			buildFrag = (Table) ui.hudGroup.find("inputTable").parent.parent.parent.parent.parent;
			hudGroup = buildFrag.parent;
			hudIndex = hudGroup.getChildren().indexOf(buildFrag);
		}catch(ClassCastException e){
			throw new ArcRuntimeException("Invalid UI Parameter! Check Game & Mod's Version!");
		}
		

		/*
		Table table = new Table(Tex.buttonEdge4,  t -> {

			t.row();

			Table infoT = new Table();
			infoT.touchable = Touchable.childrenOnly;
			infoT.update(() -> {
				if(Vars.state.isMenu())clear();
			});

			ImageButton b = new ImageButton(Icon.downOpen, Styles.clearNoneTogglei);
			b.clicked(() -> {
				if(b.isChecked()){
					infoT.clear();
					infoT.table().padTop(4);
					ScrollPane pane = infoT.pane(Styles.smallPane, i -> {
						i.align(Align.topLeft);
						i.margin(5f);
						i.defaults().growX().fillY().row();
						eventSimplePane = i;
					}).grow().maxHeight(NHUI.getHeight() / 2f).get();
					pane.setFadeScrollBars(false);
					pane.setForceScroll(false, true);
					infoT.exited(() -> Core.scene.unfocus(infoT));
				}else{
					Core.scene.unfocus(infoT);
				}
			});
			b.update(() -> {
				if(NHGroups.events.isEmpty())b.setChecked(false);
			});
			b.setDisabled(NHGroups.events::isEmpty);

			t.row().collapser(infoT, true, b::isChecked).growX().get().setDuration(0.1f);


		});
		table.name = "nh-event-table";

		try{
			ImageButton skip = HUD_statustable.find("skip");
			
			skip.setStyle(new ImageButton.ImageButtonStyle(){{
				over = buttonSelectTrans;
				down = whitePane;
				up = pane;
				imageUp = Icon.play;
				disabled = paneRight;
				imageDisabledColor = Color.clear;
				imageUpColor = Color.white;
			}});
			
			skip.addChild(new Table(underline){{
				touchable = Touchable.disabled;
				setSize(skip.getWidth(), skip.getHeight());
			}}.visible(() -> !(state.rules.waves && state.rules.waveSending && ((net.server() || player.admin) || !net.active()) && state.enemies == 0 && !spawner.isSpawning())));
			
			Element infoT = HUD_waves.find("infotable");
			infoT.remove();
			HUD_waves.row().add(table).left().margin(10f).growX().row();
			HUD_waves.add(infoT).width(65f * 5f + 4f).left();

		}catch(Exception e){
			Log.info(e);
		}
		 */
	}
}
