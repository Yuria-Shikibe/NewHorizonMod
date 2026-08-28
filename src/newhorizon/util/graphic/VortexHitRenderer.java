package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.util.Disposable;
import mindustry.Vars;
import mindustry.graphics.Layer;
import newhorizon.content.NHShaders;
import newhorizon.content.NHShaders.ShieldQuantumBlurShader;
import newhorizon.expand.entities.SharedShieldField;
import newhorizon.expand.entities.SharedShieldFields;
import newhorizon.expand.entities.VortexEvent;
import arc.struct.Seq;

public class VortexHitRenderer implements Disposable {
    public static final float VORTEX_RENDER_LAYER = Layer.end + 1f;

    private final FrameBuffer mask = new FrameBuffer();
    private final FrameBuffer shield = new FrameBuffer();
    private final FrameBuffer glowNear = new FrameBuffer();
    private final FrameBuffer glowFar = new FrameBuffer();
    private final FrameBuffer blurTemp = new FrameBuffer();
    private final ShieldQuantumBlurShader blurHorizontal = new ShieldQuantumBlurShader("VFX_quantumShieldBlurH");
    private final ShieldQuantumBlurShader blurVertical = new ShieldQuantumBlurShader("VFX_quantumShieldBlurV");
    private final Seq<SharedShieldField> groups = new Seq<>(false, 16, SharedShieldField.class);
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

        Draw.draw(VORTEX_RENDER_LAYER, () -> {
            // Submit all world geometry before changing the render target.
            Draw.flush();
            mask.begin(Color.clear);
            captureMask();
            Draw.flush();
            mask.end();

            renderShield();
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
        shield.resize(w, h);
        glowNear.resize(w, h);
        glowFar.resize(w, h);
        blurTemp.resize(w, h);
        clear(shield);
        clear(glowNear);
        clear(glowFar);
        clear(blurTemp);
    }

    private void renderShield() {
        // Intermediate buffers must receive exact RGBA values without blending.
        Blending.disabled.apply();
        shield.begin(Color.clear);
        mask.blit(NHShaders.shieldQuantum);
        shield.end();

        blurHorizontal.radius = 1f;
        blurTemp.begin();
        shield.blit(blurHorizontal);
        blurTemp.end();

        blurVertical.radius = 1f;
        glowNear.begin();
        blurTemp.blit(blurVertical);
        glowNear.end();

        blurHorizontal.radius = 3f;
        blurTemp.begin();
        glowNear.blit(blurHorizontal);
        blurTemp.end();

        blurVertical.radius = 3f;
        glowFar.begin();
        blurTemp.blit(blurVertical);
        glowFar.end();

        NHShaders.shieldQuantumComposite.glowNear = glowNear.getTexture();
        NHShaders.shieldQuantumComposite.glowFar = glowFar.getTexture();

        // Composite transparently over the already-rendered world.
        Blending.normal.apply();
        shield.blit(NHShaders.shieldQuantumComposite);
    }

    private void clear(FrameBuffer buffer) {
        buffer.begin(Color.clear);
        buffer.end();
    }

    private void fillShaderState() {
        NHShaders.ShieldQuantumShader shader = NHShaders.shieldQuantum;
        groups.clear();

        for (SharedShieldField field : SharedShieldFields.all()) {
            if (!field.active()) continue;
            int group = groups.size;
            groups.add(field);

            for (var source : field.iterable()) {
                if (!(source.block instanceof newhorizon.expand.block.defence.QuantumVortexProjector projector)) continue;
                float radius = projector.displayRadius(source);
                if (radius <= 0.01f || shader.getFieldCount() >= 24) continue;

                shader.addField(source.x, source.y, radius, source.team.color, field.hit, group);
            }
        }

        for (VortexEvent event : VortexEvent.active) {
            if (event == null || shader.eventCount >= 24 || event.field == null) continue;
            int group = groups.indexOf(event.field, true);
            if (group < 0 || group >= 24) continue;
            shader.addEvent(event.x, event.y, event.time, group);
        }
    }

    @Override
    public void dispose() {
        mask.dispose();
        shield.dispose();
        glowNear.dispose();
        glowFar.dispose();
        blurTemp.dispose();
    }
}
