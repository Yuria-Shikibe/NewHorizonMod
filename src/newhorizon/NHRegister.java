package newhorizon;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.net.Net;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;

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

	public static void load(){
		Events.on(EventType.ResetEvent.class, e -> {

			NHGroups.clear();
			worldLoaded = false;
			afterLoad.clear();
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
			if(!Vars.state.isEditor()){
				afterLoad.each(Runnable::run);
			}

			afterLoad.clear();
			
			if(!Vars.headless && Vars.net.active() && !NHSetting.getBool(NHSetting.VANILLA_COST_OVERRIDE)){
				Core.app.post(() -> {
					Vars.ui.showConfirm("@mod.ui.requite.need-override", NHSetting::showDialog);
					Vars.net.disconnect();
				});
			}

			Core.app.post(() -> {
                Vars.state.isPlaying();
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
	}
	
	
}
