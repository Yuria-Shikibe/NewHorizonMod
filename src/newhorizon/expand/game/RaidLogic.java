package newhorizon.expand.game;

import mindustry.Vars;

public final class RaidLogic {
    private RaidLogic() {
    }

    /** Singleplayer, dedicated server, or host — anywhere game logic may run. */
    public static boolean isLogicSide() {
        return !Vars.net.client() || Vars.net.server();
    }

    /** Remote multiplayer client only. */
    public static boolean isRemoteClient() {
        return Vars.net.client() && !Vars.net.server();
    }
}
