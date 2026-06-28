package newhorizon;

import arc.ApplicationListener;
import mindustry.Vars;
import newhorizon.expand.game.DefaultRaid;

import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.renderer;

public class NHModCore implements ApplicationListener {
    public NHModCore() {
    }

    @Override
    public void update() {
        if (Vars.state.isPlaying()) {
            cutscene.update();
            DefaultRaid.update();
            NHGroups.update();
            if (!Vars.headless) {
                renderer.statusRenderer.update();
                NHSetting.update();
            }
        }
    }

    @Override
    public void dispose() {
        ApplicationListener.super.dispose();
    }

    @Override
    public void init() {
        ApplicationListener.super.init();
    }
}
