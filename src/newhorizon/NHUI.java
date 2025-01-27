package newhorizon;

import arc.Core;
import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.expand.ability.active.ActiveAbility;
import newhorizon.expand.net.NHCall;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;
import newhorizon.util.ui.dialog.WorldEventDialog;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.*;

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

			buildFrag = (Table) ui.hudGroup.find("inputTable").parent.parent.parent.parent.parent;
			hudGroup = buildFrag.parent;
			hudIndex = hudGroup.getChildren().indexOf(buildFrag);
		}catch(ClassCastException e){
			throw new ArcRuntimeException("Invalid UI Parameter! Check Game & Mod's Version!");
		}
		
		eventDialog = new WorldEventDialog();
		
		Table table = new Table(Tex.buttonEdge4,  t -> {
			//t.row().image().color(Pal.gray).height(4).pad(8, -8, 8, -8).expandX().fillX();

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

		/*
		Table abilityTable = new Table(buttonEdge2, t -> {
			Button active = new Button(Styles.cleart);
			active.table(a -> {
				a.image(NHContent.activeBoost).size(64, 64);
				a.table(l -> {
					l.label(() -> "ACTIVE BOOST").pad(0, 12, 8, 0).left().expandX().fillX().row();
					l.label(() -> "READY").pad(0, 12, 8, 0).style(Styles.techLabel).left().expandX().fillX();
				}).expandX().fillX();
			}).expandX().fillX();
			active.clicked(() -> {
				Unit unit = player.unit();
				if (unit != null){
					for (int i = 0; i < unit.abilities.length; i++){
						Ability ability = unit.abilities[i];
						if (ability instanceof ActiveAbility){
							NHCall.triggerActiveAbility(unit, i);
						}
					}
				}
			});
			t.add(active).expandX().fillX();
		});
		abilityTable.visible(() -> control.input.commandMode && ui.hudfrag.shown);
		abilityTable.align(Align.bottomRight);
		abilityTable.update(() -> {
			if (control.input.commandMode && ui.hudfrag.shown){
				buildFragBound = (Image) ((Table)(((Table)hudGroup.getChildren().get(hudIndex)).getChildren().get(0))).getChildren().get(1);
				abilityTable.setSize(buildFragBound.getWidth(), 100);
				abilityTable.setPosition(Core.scene.getWidth() - buildFragBound.getWidth(), buildFragBound.y + 4);
			}
		});

		Vars.ui.hudGroup.addChild(abilityTable);

		 */

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
