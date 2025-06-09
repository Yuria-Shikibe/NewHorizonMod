package newhorizon;

import arc.Core;
import arc.Events;
import arc.func.Boolp;
import arc.func.Floatp;
import arc.func.Intp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.game.MapObjectives;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import newhorizon.util.ui.DelaySlideBar;
import newhorizon.util.ui.ObjectiveSign;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.underline;

public class NHUI{
	public static Table HUD_overlay, HUD_waves, HUD_statustable, HUD_status;
	public static WidgetGroup HUD_waves_editor;
	public static Element infoTable;

	public static final float maxWidth = 65f * 5f + 4f;

	public static void init(){

		Events.run(EventType.Trigger.update, () -> ui.hudfrag.toggleHudText(false));

		try{
			getReferences();
			rebuildSkipButton();
			preProcess();
			buildObjectiveTable();
			postProcess();
		}catch(Exception e){
			Log.err(e);
		}
	}

	public static void getReferences(){
		HUD_overlay = Vars.ui.hudGroup.find("overlaymarker");
		HUD_waves_editor = HUD_overlay.find("waves/editor");
		HUD_waves = HUD_waves_editor.find("waves");
		HUD_statustable = HUD_waves.find("statustable");
		HUD_status = HUD_statustable.find("status");
	}

	public static void rebuildSkipButton(){
		ImageButton skip = HUD_statustable.find("skip");

		skip.setStyle(new ImageButton.ImageButtonStyle(){{
			over = Tex.buttonSelectTrans;
			down = Tex.whitePane;
			up = Tex.pane;
			imageUp = Icon.play;
			disabled = Tex.paneRight;
			imageDisabledColor = Color.clear;
			imageUpColor = Color.white;
		}});

		skip.addChild(new Table(underline){{
			touchable = Touchable.disabled;
			setSize(skip.getWidth(), skip.getHeight());
		}}.visible(() -> !(state.rules.waves && state.rules.waveSending && ((net.server() || player.admin) || !net.active()) && state.enemies == 0 && !spawner.isSpawning())));

	}

	public static void preProcess(){
		infoTable = HUD_waves.find("infotable");
		infoTable.remove();
	}

	public static void postProcess(){
		HUD_waves.add(infoTable).width(maxWidth).left();
	}

	public static void buildObjectiveTable(){
		HUD_waves.row().add(new Table(Tex.buttonEdge4, t -> {
			Table infoT = new Table();
			infoT.touchable = Touchable.childrenOnly;

			ImageButton b = new ImageButton(Icon.downOpen, Styles.clearNoneTogglei);
			b.clicked(() -> {
				if(b.isChecked()){
					infoT.clear();
					infoT.table().padTop(4);
					ScrollPane pane = infoT.pane(Styles.smallPane, i -> {
						i.align(Align.topLeft).margin(5f).defaults().growX().fillY().row();
						state.rules.objectives.each(mapObjective -> {
							i.collapser(getObjectiveTable(mapObjective), true, mapObjective::qualified).row();
						});
					}).grow().maxHeight(NHUI.getHeight() / 2f).get();
					pane.setFadeScrollBars(false);
					pane.setForceScroll(false, true);
					infoT.exited(() -> Core.scene.unfocus(infoT));
				}else{
					Core.scene.unfocus(infoT);
				}
			});
			b.update(() -> {
				if (state.isMenu()) b.setChecked(false);
			});

			t.table(bl -> {
				bl.table(table -> table.add("Objective")).growX().height(50).marginLeft(10f);
				bl.add(b).size(50).padLeft(10f);
			}).growX().fillY().margin(4f);

			t.row().collapser(infoT, true, b::isChecked).growX().get().setDuration(0.1f);
		})).left().margin(10f).growX().row();
	}

	public static Table getObjectiveTable(MapObjectives.MapObjective e){
		return new Table(t -> {
			t.defaults().growX().fillY().padBottom(6f).pad(6f);

			if (e instanceof MapObjectives.ResearchObjective obj){
				t.add(objectiveTable(
						obj.content.fullIcon,
						() -> Mathf.num(obj.isCompleted()),
						() -> 1,
						() -> "Research:",
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.ItemObjective obj){
				t.add(objectiveTable(
						obj.item.fullIcon,
						() -> state.rules.defaultTeam.items().get(obj.item),
						() -> obj.amount,
						() -> "Obtain:",
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.CoreItemObjective obj){
				t.add(objectiveTable(
						obj.item.fullIcon,
						() -> state.stats.coreItemCount.get(obj.item),
						() -> obj.amount,
						() -> "Collect:",
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.BuildCountObjective obj){
				t.add(objectiveTable(
						obj.block.fullIcon,
						() -> state.stats.placedBlockCount.get(obj.block, 0),
						() -> obj.count,
						() -> "Build:",
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.UnitCountObjective obj){
				t.add(objectiveTable(
						obj.unit.fullIcon,
						() -> state.rules.defaultTeam.data().countType(obj.unit),
						() -> obj.count,
						() -> "Build:",
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.DestroyUnitsObjective obj){
				t.add(objectiveTable(
						Icon.units.getRegion(),
						() -> state.stats.enemyUnitsDestroyed,
						() -> obj.count,
						() -> "Destroy:",
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.TimerObjective obj){
				t.add(objectiveTable(
						Icon.refresh.getRegion(),
						() -> (int) obj.duration,
						() -> (int) obj.duration,
                        () -> UI.formatTime(obj.duration),
						obj::isCompleted,
						false
				));
			}

			if (e instanceof MapObjectives.DestroyBlockObjective obj){
				t.add(objectiveTable(
						obj.block.fullIcon,
						() -> Mathf.num(obj.isCompleted()),
						() -> 1,
						() -> "Destroy:" + obj.block.localizedName,
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.DestroyBlocksObjective obj){
				t.add(objectiveTable(
						obj.block.fullIcon,
                        obj::progress,
						() -> obj.positions.length,
						() -> "Destroy:" + obj.block.localizedName,
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.CommandModeObjective obj){
				t.add(objectiveTable(
						Icon.units.getRegion(),
						() -> Mathf.num(obj.isCompleted()),
						() -> 1,
                        obj::text,
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.FlagObjective obj){
				t.add(objectiveTable(
						Icon.info.getRegion(),
						() -> Mathf.num(obj.isCompleted()),
						() -> 1,
						obj::text,
						obj::isCompleted
				));
			}

			if (e instanceof MapObjectives.DestroyCoreObjective obj){
				t.add(objectiveTable(
						Icon.effect.getRegion(),
						() -> Mathf.num(obj.isCompleted()),
						() -> 1,
						obj::text,
						obj::isCompleted
				));
			}
		});
	}

	public static Stack objectiveTable(TextureRegion region, Intp value, Intp target, Prov<CharSequence> info, Boolp checked, boolean text){
		return new Stack(
				new Table(table -> table.add(new DelaySlideBar(
						() -> Pal.accent,
						() -> "            " + info.get() + (text? (value.get() + "/" + target.get()): ""),
						() -> Mathf.clamp((float) value.get() / target.get())
				)).padLeft(20f).height(40).expandX().fillX()),
				new Table(image -> image.image(region).scaling(Scaling.fit).size(32).padTop(4f).padBottom(4f).padLeft(56f).padRight(8)).left(),
				new Table(table -> table.add(new ObjectiveSign(Pal.gray, Pal.accent, 2, 4, 5, checked)).size(40).expandX().left())
		);
	}

	public static Stack objectiveTable(TextureRegion region, Intp value, Intp target, Prov<CharSequence> info, Boolp checked){
		return objectiveTable(region, value, target, info, checked, true);
	}


	public static float getWidth(){
		return Core.graphics.getWidth();
	}

	public static float getHeight(){
		return Core.graphics.getHeight();
	}
}
