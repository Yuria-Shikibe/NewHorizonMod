package newhorizon;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.core.GameState;
import mindustry.core.Logic;
import mindustry.editor.MapEditorDialog;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.net.Net;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.NHContent;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.expand.eventsys.types.WorldEventObjective;
import newhorizon.expand.game.NHWorldData;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;

import java.lang.reflect.Field;

import static mindustry.Vars.ui;

public class NHRegister{
	public static final Seq<Runnable> afterLoad = new Seq<>();
	
	protected static boolean worldLoaded = false;
	
	public static void postAfterLoad(Runnable runnable){
		if(!worldLoaded)afterLoad.add(runnable);
	}
	
	static{
		Net.registerPacket(LongInfoMessageCallPacket::new);
		Net.registerPacket(ActiveAbilityTriggerPacket::new);
	}
	
	public static void addTaskOnSave(Runnable runnable){
		taskOnSave.add(runnable);
	}
	public static final Seq<Runnable> taskOnSave = new Seq<>();
	
	public static boolean worldLoaded(){
		return worldLoaded;
	}
	
	public static void load(){
		Events.on(EventType.ResetEvent.class, e -> {

			NHGroups.clear();
			worldLoaded = false;
			afterLoad.clear();
			EventHandler.dispose();
			
			while(taskOnSave.any()){
				taskOnSave.pop().run();
			}
		});

		Events.on(EventType.WorldLoadBeginEvent.class, e -> {
			NHGroups.worldReset();
		});

		Events.run(EventType.Trigger.update, () -> {
			NHGroups.updateGroup();
		});

		Events.run(EventType.Trigger.draw, () -> {
			NHVars.renderer.draw();
			if (NHSetting.getBool(NHSetting.TERRAIN_MODE)){
				NHVars.control.terrainSelect();
			}
			NHGroups.draw();
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHGroups.worldInit();
			NHVars.core.worldInit();
			if(!Vars.state.isEditor()){
				EventHandler.create();
				afterLoad.each(Runnable::run);
			}
			
			Core.app.post(() -> {
				if(!Vars.state.map.tags.containsKey(NHWorldSettingDialog.SETTINGS_KEY)){
					NHWorldData data = NHVars.worldData;
					
					NHWorldSettingDialog.allSettings.each(entry -> {
						try{
							entry.dataField.set(data, entry.defData());
						}catch(IllegalAccessException ex){
							Log.info(ex);
						}
					});
				}else{
					Jval initContext = Jval.read(Vars.state.map.tags.get(NHWorldSettingDialog.SETTINGS_KEY));
					NHWorldSettingDialog.allSettings.each(entry -> entry.initWorldData(initContext));
				}
			});
			
			//Fuck erekir on the server
			if(Vars.headless){
				if(Vars.state.rules.hiddenBuildItems.equals(ObjectSet.with(Planets.erekir.hiddenItems))){
					Groups.player.each(p -> p.sendMessage("No Pure Erekir On The Server!!!"));
					
					Groups.build.each(b -> Time.run(Mathf.random(60, 600), b::kill));
					Groups.unit.each(b -> Time.run(Mathf.random(60, 600), b::kill));
					Time.run(600f, () -> {
						Logic.updateGameOver(Team.derelict);
					});
					
					Vars.maps.removeMap(Vars.state.map);
				}
			}
			
			afterLoad.clear();
			
			if(!Vars.headless && Vars.net.active() && !NHSetting.getBool(NHSetting.VANILLA_COST_OVERRIDE)){
				Core.app.post(() -> {
					Vars.ui.showConfirm("@mod.ui.requite.need-override", NHSetting::showDialog);
					Vars.net.disconnect();
				});
			}

			if(!Vars.headless && Vars.net.active() && NHSetting.getBool(NHSetting.EXPERIMENTAL)){
				Core.app.post(() -> {
					Vars.ui.showConfirm("need disable experimental content", NHSetting::showDialog);
					Vars.net.disconnect();
				});
			}


			
			Core.app.post(() -> {
				if(Vars.state.isPlaying()){
					Vars.state.rules.objectives.all.insert(0, new WorldEventObjective());
				}
				Core.app.post(() -> Core.app.post(() -> Core.app.post(() ->
					worldLoaded = true
				)));
			});
			
			if(!Vars.headless){
				NHVars.renderer.statusRenderer.clear();
			}
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu){
				worldLoaded = false;
			}
		});
		
		Events.on(EventType.ClientLoadEvent.class, e -> {
			try{
				BaseDialog menu;
				Field field = MapEditorDialog.class.getDeclaredField("menu");
				field.setAccessible(true);
				menu = (BaseDialog)field.get(ui.editor);
				
				menu.cont.row().button("@mod.ui.nh-extra-menu", new TextureRegionDrawable(NHContent.icon), 30, () -> {
					NHUI.nhWorldSettingDialog.show();
				}).size(180f * 2 + 10f, 60f);
			}catch(IllegalAccessException | NoSuchFieldException ex){
				ui.showErrorMessage(ex.toString());
			}
		});
	}
	
	
}
