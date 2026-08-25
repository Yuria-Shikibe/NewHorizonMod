package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.util.Disposable;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.ForceProjector;
import newhorizon.content.NHShaders;
import newhorizon.expand.entities.VortexEvent;

public class VortexHitRenderer implements Disposable {
    public static final float VORTEX_RENDER_LAYER = Layer.space + 0.021f;

    @Override
    public void dispose() {
    }

    public void update() {
        VortexEvent.update();
    }

    public void draw() {
        if (NHShaders.vortexHit == null || !hasEvents()) return;

        fillShaderData();
        Draw.drawRange(VORTEX_RENDER_LAYER, 0.0001f,
                () -> Vars.renderer.effectBuffer.begin(Color.clear),
                () -> {
                    Vars.renderer.effectBuffer.end();
                    Vars.renderer.effectBuffer.blit(NHShaders.vortexHit);
                });
    }

    private boolean hasEvents() {
        for (VortexEvent event : VortexEvent.active) {
            if (event != null) return true;
        }
        return false;
    }

    private void fillShaderData() {
        float[] positions = NHShaders.vortexHit.hitPositions;
        float[] angles = NHShaders.vortexHit.hitAngles;
        float cameraLeft = Core.camera.position.x - Core.camera.width / 2f;
        float cameraBottom = Core.camera.position.y - Core.camera.height / 2f;
        int count = 0;

        for (int i = 0; i < VortexEvent.active.length && count < 24; i++) {
            VortexEvent event = VortexEvent.active[i];
            if (event == null) continue;

            float x = (event.x - cameraLeft) / Core.camera.width;
            float y = (event.y - cameraBottom) / Core.camera.height;
            positions[count * 2] = x;
            positions[count * 2 + 1] = y;
            angles[count * 2] = event.angle;
            angles[count * 2 + 1] = event.time;
            count++;
        }

        for (int i = count * 2; i < positions.length; i++) {
            positions[i] = 0f;
            angles[i] = 0f;
        }
    }
}
