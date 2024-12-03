package newhorizon;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import newhorizon.expand.block.flood.FloodGraph;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;
import newhorizon.util.ui.dialog.WorldEventDialog;

import static mindustry.Vars.*;
import static mindustry.Vars.content;
import static mindustry.gen.Tex.*;
import static newhorizon.expand.block.struct.GraphUpdater.allGraph;
import static newhorizon.expand.block.struct.GraphUpdater.xenGraphAll;

public class NHUI{
	//references:
	//HUD stuff
	public static Table HudFragment_overlaymarker, HUD_waves, HUD_statustable, HUD_status;
	//BuildFragment stuff
	public static Table inputTable, buildMenuTable, toolsTable;
	public static Table tableTool;
	public static WidgetGroup HUD_waves_editor;

	public static WorldEventDialog eventDialog;
	public static Table eventSimplePane = new Table();
	
	public static NHWorldSettingDialog nhWorldSettingDialog;
	
	public static Group root;
	
	public static float getWidth(){
		return Core.graphics.getWidth();
	}
	
	public static float getHeight(){
		return Core.graphics.getHeight();
	}
	
	public static void init(){
		root = new WidgetGroup(){{
			setFillParent(true);
			touchable = Touchable.childrenOnly;
		}};
		
		nhWorldSettingDialog = new NHWorldSettingDialog();
		
		Core.scene.root.addChildAt(0, root);
		
		try{
			HudFragment_overlaymarker = Vars.ui.hudGroup.find("overlaymarker");
			HUD_waves_editor = HudFragment_overlaymarker.find("waves/editor");
			HUD_waves = HUD_waves_editor.find("waves");
			HUD_statustable = HUD_waves.find("statustable");
			HUD_status = HUD_statustable.find("status");


			inputTable = Vars.ui.hudGroup.find("inputTable");
			buildMenuTable = (Table) inputTable.parent.parent.parent.parent;

		}catch(ClassCastException e){
			throw new ArcRuntimeException("Invalid UI Parameter! Check Game&Mod's Version!");
		}
		
		eventDialog = new WorldEventDialog();
		
		Table table = new Table(Tex.buttonEdge4,  t -> {
			t.label(() -> "Xen Graph Count: " + xenGraphAll.size).row();
			t.label(() -> "Graph Count: " + allGraph.size).row();
			//t.label(() -> {
			//	if (FloodGraph.allGraph.isEmpty()) return "null";
			//	StringBuilder out = new StringBuilder();
			//	for (FloodGraph graph: FloodGraph.allGraph){
			//		out.append(graph.allBuildings.size).append("\n");
			//		for (Building building: graph.allBuildings){
			//			String text = building.tileX() + " " + building.tileY() + " ";
			//			out.append(text);
			//		}
			//		out.append("\n");
			//	}
			//	return out;
			//}).row();

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
						eventDialog.buildAllEventsSimple(i);
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
			
			t.table(bl -> {
				bl.button("@mod.ui.world-event", Icon.info, Styles.cleart, () -> {
					eventDialog.show();
				}).growX().height(50).disabled(bt -> !Vars.state.rules.infiniteResources && NHGroups.events.isEmpty()).get().marginLeft(10f);
				bl.add(b).size(50).padLeft(10f);
			}).growX().fillY().margin(4f);
			
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
	}
	
	public static void clear(){
		root.clear();
	}
}
