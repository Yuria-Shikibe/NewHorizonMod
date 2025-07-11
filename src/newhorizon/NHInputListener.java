package newhorizon;

import mindustry.Vars;
import mindustry.gen.Unit;
import newhorizon.util.annotation.HeadlessDisabled;

@HeadlessDisabled
public class NHInputListener {
    public Unit currentUnit;

    public NHInputListener() {
    }

    public static void registerModBinding() {
    }

    public void update() {
        if (Vars.player != null) updatePlayerStatus();
    }

    protected void updatePlayerStatus() {
        currentUnit = Vars.player.unit();
    }
}
