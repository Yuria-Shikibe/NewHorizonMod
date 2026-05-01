package newhorizon;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.net.Net;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;

public class NHRegister {
    static {
        Net.registerPacket(LongInfoMessageCallPacket::new);
        Net.registerPacket(ActiveAbilityTriggerPacket::new);
    }

    public static void load() {
        Events.on(EventType.ResetEvent.class, e -> NHGroups.clear());
        Events.on(EventType.WorldLoadBeginEvent.class, e -> NHGroups.worldReset());

        Events.run(EventType.Trigger.draw, () -> {
            NHVars.renderer.draw();
            NHGroups.draw();
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            NHGroups.worldInit();
            if (!Vars.headless) NHVars.renderer.statusRenderer.clear();
        });
    }
}
