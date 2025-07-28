package newhorizon;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.net.Net;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.AlertToastPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;
import newhorizon.expand.net.packet.WarnHUDPacket;

import static newhorizon.NHVars.renderer;

public class NHRegister {
    static {
        Net.registerPacket(LongInfoMessageCallPacket::new);
        Net.registerPacket(ActiveAbilityTriggerPacket::new);
        Net.registerPacket(WarnHUDPacket::new);
        Net.registerPacket(AlertToastPacket::new);
    }

    public static void load() {
        Events.on(EventType.ResetEvent.class, e -> NHGroups.clear());

        Events.on(EventType.WorldLoadBeginEvent.class, e -> NHGroups.worldReset());

        Events.run(EventType.Trigger.draw, () -> {
            renderer.draw();
            NHGroups.draw();
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            NHGroups.worldInit();

            if (!Vars.headless && Vars.net.active()) {
                for (var setting: NHSetting.allSettings.get("override")){
                    if (!NHSetting.getBool(setting.key)){
                        Core.app.post(() -> {
                            Vars.ui.showConfirm("@mod.ui.requite.need-override", NHSetting::showDialog);
                            Vars.net.disconnect();
                        });
                        break;
                    }
                }

            }

            if (!Vars.headless) {
                renderer.statusRenderer.clear();
            }
        });
    }
}
