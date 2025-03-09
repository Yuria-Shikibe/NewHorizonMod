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

import newhorizon.expand.game.NHWorldData;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;

import java.lang.reflect.Field;

import static mindustry.Vars.ui;
import static newhorizon.NHVars.renderer;

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

			while(taskOnSave.any()){
				taskOnSave.pop().run();
			}
		});

		Events.on(EventType.WorldLoadBeginEvent.class, e -> {
			NHGroups.worldReset();
		});

		Events.run(EventType.Trigger.draw, () -> {
			renderer.draw();
			NHGroups.draw();
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHGroups.worldInit();
			NHVars.core.worldInit();
			if(!Vars.state.isEditor()){
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
				}
				Core.app.post(() -> Core.app.post(() -> Core.app.post(() ->
					worldLoaded = true
				)));
			});
			
			if(!Vars.headless){
				renderer.statusRenderer.clear();
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
