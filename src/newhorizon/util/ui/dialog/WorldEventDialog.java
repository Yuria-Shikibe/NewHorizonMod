package newhorizon.util.ui.dialog;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Color;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Align;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NHGroups;
import newhorizon.NHUI;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.expand.eventsys.types.WorldEventType;
import newhorizon.util.ui.display.IconNumDisplay;

import static newhorizon.util.ui.TableFunc.*;

public class WorldEventDialog extends BaseDialog{
	public Table currentShow, consoleTable, scrollShow;
	public boolean buildDebug = false;
	
	public Boolf<WorldEvent> currentFilter = e -> true;
	public Func<WorldEvent, Cons<Table>> tooltipModifier = event -> iT -> {
		iT.setBackground(Tex.pane);
		iT.setSize(NHUI.getWidth(), NHUI.getHeight());
		event.type.infoTable(iT);
		iT.pack();
		iT.visible(iT::hasChildren);
	};
	
//	public ObjectMap<WorldEvent, Table> allToShow = new ObjectMap<>();
	
	public WorldEventDialog(){
		super(Core.bundle.get("nh.dialog-event"));
		
		addCloseListener();
		
		//Top Info Panel
		cont.margin(6f);
		cont.table(Tex.button, t -> {
			t.defaults().growX().height(40f).pad(4f);
			t.button("summon", Styles.cleart, () -> {
				new BaseDialog("summon"){{
					addCloseButton();
					cont.margin(6f);
					cont.pane(t -> {
						t.defaults().growX().height(50f).pad(4);
						int num = 0;
						for(ObjectMap.Entry<String, WorldEventType> entry : WorldEventType.allTypes.entries()){
							buildDebug = true;
							WorldEventType type = entry.value;
							t.button(entry.key, type::create);
							num++;
							if(num % 2 == 0)t.row();
							buildDebug = false;
						}
					}).grow();
				}}.show();
			}).visible(() -> Vars.state.rules.infiniteResources);
			t.button("triggers", Styles.cleart, () -> {
				triggers().show();
			}).visible(() -> Vars.state.rules.infiniteResources && !Vars.net.client());
		}).growX().height(120f).padBottom(6f).top().row();
		
		currentShow = new Table(Tex.button){{
			pane(scrollShow = new Table()).grow();
			scrollShow.align(Align.topLeft);
		}};
		
		consoleTable = new Table(Tex.button){{
			update(() -> {
				setWidth(NHUI.getWidth() / 4);
				changed(() -> {
					parent.layout();
				});
			});
		}};
		
		cont.table(lt -> {
//			lt.add(consoleTable).width(NHUI.getWidth() / 4).growY().left();
//			lt.image().width(4f).color(Color.gray).left().pad(8f).growY();
			lt.add(currentShow).left().grow();
		}).grow().padBottom(6f).row();
		
		cont.button("@back", Icon.left, this::hide).growX().height(60).get().marginLeft(20f);
	}
	
	@Override
	public Dialog show(){
		buildDebug = true;
		buildAllEvents();
		buildDebug = false;
		return super.show();
	}
	
	public void buildAllEvents(){
		buildAllEvents(scrollShow);
	}
	
	public void buildAllEvents(Table table){
		scrollShow.clear();
		
		NHGroups.events.each(e -> buildEvent(e, table));
	}
	
	public Table buildSimpleTable(WorldEvent e){
		return e.type.buildSimpleTable(e);
	}
	
	public void buildAllEventsSimple(Table table){
		NHGroups.events.each(e -> table.add(buildSimpleTable(e)).row());
	}
	
	public void buildEvent(WorldEvent event){
		buildEvent(event, scrollShow);
	}
	
	public void buildEvent(WorldEvent event, Table target){
		Table table = new Table();
		table.margin(6f);
		target.add(table).growX().fillY().row();
		event.buildTable(table);
		if(Vars.state.rules.infiniteResources && !Vars.net.client() && buildDebug){
			event.buildDebugTable(table);
			table.setBackground(Tex.pane);
		}
	}
	
	public static BaseDialog triggers(){
		return new BaseDialog(""){{
			addCloseButton();
			
			cont.pane(t -> {
				t.margin(120f);
				NHGroups.autoEventTrigger.each(trigger -> {
					t.table(Tex.sideline, info -> {
						info.defaults().left().growX().fillY().pad(OFFSET / 2f);
						info.add(trigger.toString()).row();
						if(!EventHandler.inValidEvent(trigger.eventType))info.table(Tex.buttonEdge3, show -> {
							show.left();
							trigger.eventType.infoTable(show);
						}).row();
						info.table(Tex.pane, show -> {
							show.defaults().growX().fillY().pad(OFFSET / 2f).pad(6f).left();
							show.table(d -> {
								d.left();
								d.defaults().left().growX().height(60);
								d.add("REQUIREMENTS: ").color(Color.lightGray).row();
								if(trigger.items.any())d.pane(s -> {
									trigger.items.each(p -> {
										s.left();
										s.add(new IconNumDisplay(p.item.fullIcon, p.value, p.item.localizedName)).left().padLeft(6f);
									});
								}).row();
								if(trigger.units.any())d.pane(s -> {
									s.left();
									trigger.units.each(p -> {
										s.add(new IconNumDisplay(p.item.fullIcon, p.value, p.item.localizedName)).left().padLeft(6f);
									});
								}).row();
								if(trigger.buildings.any())d.pane(s -> {
									s.left();
									trigger.buildings.each(p -> {
										s.add(new IconNumDisplay(p.item.fullIcon, p.value, p.item.localizedName)).left().padLeft(6f);
									});
								}).row();
							}).left().growX().fill().row();
							show.add("[lightgray]Min Spawn Wave: [accent]" + trigger.minTriggerWave).row();
							show.add("[lightgray]BaseSpacing: [accent]" + trigger.spacingBase / Time.toMinutes).row();
							show.add("[lightgray]RandSpacing: [accent]" + trigger.spacingRand / Time.toMinutes).row();
							show.add("[lightgray]Disposable: " + judge(trigger.disposable)).row();
							show.label(() -> "[lightgray]Reload: [accent]" + format(trigger.getReload() / Time.toMinutes)).row();
							show.label(() -> "[lightgray]Spacing: [accent]" + format(trigger.getSpacing() / Time.toMinutes)).row();
							show.add(new Bar("Ratio", Pal.accent, () -> trigger.getReload() / trigger.getSpacing())).height(48).row();
							show.label(() -> "[lightgray]Meet: [accent]" + judge(trigger.meet())).row();
						}).row();
					}).growX().fillY().pad(OFFSET).row();
				});
			}).grow();
			
		}};
	}
}
