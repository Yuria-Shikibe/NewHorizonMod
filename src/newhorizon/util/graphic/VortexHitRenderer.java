package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.util.Disposable;
import mindustry.Vars;
import mindustry.graphics.Layer;
import newhorizon.content.NHShaders;
import newhorizon.expand.entities.SharedShieldField;
import newhorizon.expand.entities.SharedShieldFields;
import newhorizon.expand.entities.VortexEvent;

public class VortexHitRenderer implements Disposable {
    public static final float VORTEX_RENDER_LAYER = Layer.end + 1f;

    private final FrameBuffer mask = new FrameBuffer();
    private int width = -1, height = -1;

    public void update() {
        VortexEvent.update();
    }

    public boolean hasActiveField() {
        for (SharedShieldField field : SharedShieldFields.all()) {
            if (field.active()) return true;
        }
        return false;
    }

    public void draw() {
        if (Vars.headless || NHShaders.shieldQuantum == null || !hasActiveField()) return;

        resize();
        NHShaders.shieldQuantum.resetState();
        NHShaders.shieldQuantum.texture = mask.getTexture();
        fillShaderState();

        Draw.drawRange(VORTEX_RENDER_LAYER, 0.001f,
                () -> mask.begin(Color.clear),
                () -> {
                    captureMask();
                    mask.end();
                    mask.blit(NHShaders.shieldQuantum);
                    Draw.flush();
                });
    }

    private void captureMask() {
        for (SharedShieldField field : SharedShieldFields.all()) {
            if (!field.active()) continue;

            for (var source : field.iterable()) {
                if (!(source.block instanceof newhorizon.expand.block.defence.QuantumVortexProjector projector)) continue;
                float radius = projector.displayRadius(source);
                if (radius <= 0.01f) continue;

                Draw.color(Color.white);
                Fill.poly(source.x, source.y, projector.sides, radius, projector.shieldRotation);
            }
        }
    }

    private void resize() {
        int w = Math.max(2, Core.graphics.getWidth());
        int h = Math.max(2, Core.graphics.getHeight());
        if (w == width && h == height) return;

        width = w;
        height = h;
        mask.resize(w, h);
    }

    private void fillShaderState() {
        NHShaders.ShieldQuantumShader shader = NHShaders.shieldQuantum;

        for (SharedShieldField field : SharedShieldFields.all()) {
            if (!field.active()) continue;

            for (var source : field.iterable()) {
                if (!(source.block instanceof newhorizon.expand.block.defence.QuantumVortexProjector projector)) continue;
                float radius = projector.displayRadius(source);
                if (radius <= 0.01f || shader.getFieldCount() >= 24) continue;

                shader.addField(source.x, source.y, radius, source.team.color, field.hit);
            }
        }

        for (VortexEvent event : VortexEvent.active) {
            if (event != null && shader.eventCount < 24) shader.addEvent(event.x, event.y, event.time);
        }
    }

    @Override
    public void dispose() {
        mask.dispose();
    }
}
