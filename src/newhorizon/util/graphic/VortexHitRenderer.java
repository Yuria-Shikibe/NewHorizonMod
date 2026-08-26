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
import newhorizon.content.NHShaders.SimpleSurfaceShader;
import newhorizon.expand.entities.SharedShieldField;
import newhorizon.expand.entities.SharedShieldFields;
import newhorizon.expand.entities.VortexEvent;

public class VortexHitRenderer implements Disposable {
    public static final float VORTEX_RENDER_LAYER = Layer.end + 1f;

    private final FrameBuffer mask = new FrameBuffer();
    private final FrameBuffer simulationA = new FrameBuffer();
    private final FrameBuffer simulationB = new FrameBuffer();
    private final FrameBuffer blurred = new FrameBuffer();
    private final FrameBuffer blurTemp = new FrameBuffer();
    private final SimpleSurfaceShader blurHorizontal = new SimpleSurfaceShader("VFX_quantumShieldBlurH");
    private final SimpleSurfaceShader blurVertical = new SimpleSurfaceShader("VFX_quantumShieldBlurV");
    private boolean usingFirstSimulation;
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
                    updateSimulation();
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
        simulationA.resize(w, h);
        simulationB.resize(w, h);
        blurred.resize(w, h);
        blurTemp.resize(w, h);
        clear(simulationA);
        clear(simulationB);
        clear(blurred);
        clear(blurTemp);
    }

    private void updateSimulation() {
        FrameBuffer previous = usingFirstSimulation ? simulationA : simulationB;
        FrameBuffer next = usingFirstSimulation ? simulationB : simulationA;

        blurTemp.begin();
        previous.blit(blurHorizontal);
        blurTemp.end();

        blurred.begin();
        blurTemp.blit(blurVertical);
        blurred.end();

        NHShaders.shieldQuantumSimulation.blurTexture = blurred.getTexture();
        next.begin();
        previous.blit(NHShaders.shieldQuantumSimulation);
        next.end();

        NHShaders.shieldQuantumSimulation.frame++;
        NHShaders.shieldQuantum.simulationTexture = next.getTexture();
        usingFirstSimulation = !usingFirstSimulation;
    }

    private void clear(FrameBuffer buffer) {
        buffer.begin(Color.clear);
        buffer.end();
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
        simulationA.dispose();
        simulationB.dispose();
        blurred.dispose();
        blurTemp.dispose();
    }
}
