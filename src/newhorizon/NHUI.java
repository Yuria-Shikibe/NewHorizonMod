package newhorizon;

import arc.Core;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.ArcRuntimeException;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import newhorizon.util.ui.WorldEventDialog;

public class NHUI{
	//References:
	public static Table HudFragment_overlaymarker, HUD_waves, HUD_statustable;
	public static WidgetGroup HUD_waves_editor;
	
	public static WorldEventDialog eventDialog;
	
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
		
		Core.scene.root.addChildAt(0, root);
		
		try{
			HudFragment_overlaymarker = Vars.ui.hudGroup.find("overlaymarker");
			HUD_waves_editor = HudFragment_overlaymarker.find("waves/editor");
			HUD_waves = HUD_waves_editor.find("waves");
			HUD_statustable = HUD_waves.find("statustable");
		}catch(ClassCastException e){
			throw new ArcRuntimeException("Invalid UI Parameter! Check Game&Mod's Version!");
		}
		
		
		eventDialog = new WorldEventDialog();
		
		Table table = new Table(Tex.buttonEdge4,  t -> {
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
					infoT.pane(i -> {
						i.align(Align.topLeft);
						eventDialog.buildAllEventsSimple(i, event -> iT -> {
							iT.setBackground(Tex.pane);
							i.margin(5f);
							iT.setSize(NHUI.getWidth(), NHUI.getHeight());
							event.type.infoTable(iT);
							iT.pack();
						});
					}).grow().maxHeight(NHUI.getHeight() / 2f).get().setFadeScrollBars(false);
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
		
		HUD_statustable.row().add(table).left().fill().margin(10f).padBottom(4f);
	}
	
	public static void clear(){
		root.clear();
	}
}
