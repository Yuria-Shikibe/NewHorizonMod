package newhorizon.util.graphic;

import arc.Events;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Disposable;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.g3d.MeshBuilder;
import newhorizon.NHSetting;
import newhorizon.content.NHShaders;

/** Renders procedural nebula volumes in solar-system space without post-processing the scene or UI. */
public class GalaxyNebulaRenderer implements Disposable {
    private static final Nebula[] nebulas = {
            new Nebula(-24f, -7f, -32f, 20f, 9f, 13f, 11f, 0.165f, 0.02f),
            new Nebula(27f, 10f, -38f, 18f, 12f, 10f, 29f, 0.150f, 0.16f),
            new Nebula(-34f, 13f, 11f, 22f, 10f, 13f, 47f, 0.145f, 0.31f),
            new Nebula(35f, -12f, 17f, 19f, 13f, 9f, 71f, 0.150f, 0.49f),
            new Nebula(4f, 22f, -45f, 26f, 8f, 16f, 101f, 0.135f, 0.68f),
            new Nebula(-8f, -24f, 36f, 23f, 9f, 15f, 131f, 0.140f, 0.84f),
            new Nebula(49f, 18f, -13f, 17f, 8f, 22f, 157f, 0.125f, 0.12f),
            new Nebula(-53f, -16f, -18f, 25f, 12f, 11f, 181f, 0.135f, 0.27f),
            new Nebula(14f, -34f, -29f, 18f, 16f, 9f, 211f, 0.145f, 0.43f),
            new Nebula(-17f, 32f, 26f, 21f, 8f, 18f, 239f, 0.130f, 0.57f),
            new Nebula(52f, -5f, 35f, 28f, 10f, 14f, 269f, 0.120f, 0.73f),
            new Nebula(-46f, 27f, 39f, 18f, 14f, 21f, 307f, 0.125f, 0.91f),
            new Nebula(9f, 43f, 13f, 15f, 22f, 10f, 337f, 0.135f, 0.38f),
            new Nebula(38f, 31f, 47f, 24f, 11f, 18f, 373f, 0.115f, 0.62f),
            new Nebula(-29f, -39f, -48f, 27f, 13f, 19f, 401f, 0.120f, 0.79f),
            new Nebula(3f, -45f, 52f, 20f, 17f, 11f, 431f, 0.115f, 0.97f)
    };

    private final Mat3D transform = new Mat3D();
    private final Vec3 position = new Vec3();
    private Mesh sphere;

    public GalaxyNebulaRenderer() {
        sphere = MeshBuilder.buildHex(Color.white, 3, 1f);
        Events.run(EventType.Trigger.universeDraw, this::draw);
    }

    private void draw() {
        if (!NHSetting.getBool(NHSetting.DEBUG_GALAXY_DISTORTION) || sphere == null || sphere.isDisposed()) return;

        var camera = Vars.renderer.planets.cam;
        var shader = NHShaders.galaxyNebula;

        Gl.depthMask(false);
        Gl.disable(Gl.cullFace);
        Blending.additive.apply();

        shader.bind();
        shader.setUniformMatrix4("u_proj", camera.combined.val);
        shader.cameraX = camera.position.x;
        shader.cameraY = camera.position.y;
        shader.cameraZ = camera.position.z;

        for (Nebula nebula : nebulas) {
            position.set(nebula.x, nebula.y, nebula.z);
            float clipRadius = Math.max(nebula.scaleX, Math.max(nebula.scaleY, nebula.scaleZ));
            if (!camera.frustum.containsSphere(position, clipRadius)) continue;

            renderLayer(nebula, 1.10f, nebula.alpha * 0.65f, nebula.seed, nebula.palette, 0.27f, 0);
            renderLayer(nebula, 0.78f, nebula.alpha * 0.90f, nebula.seed + 19.7f, nebula.palette + 0.11f, 0.34f, 1);
            renderLayer(nebula, 0.49f, nebula.alpha * 1.18f, nebula.seed + 43.1f, nebula.palette + 0.23f, 0.42f, 2);
        }

        Blending.normal.apply();
        Gl.enable(Gl.cullFace);
        Gl.cullFace(Gl.back);
        Gl.depthMask(true);
    }

    private void renderLayer(Nebula nebula, float scale, float alpha, float seed, float palette, float warp, int layer) {
        float rotation = Time.globalTime * (0.018f + seed % 7f * 0.002f) + seed * 13.7f;
        float offsetScl = layer * 0.065f;
        float offsetX = (float)Math.sin(seed * 1.37f) * nebula.scaleX * offsetScl;
        float offsetY = (float)Math.cos(seed * 0.91f) * nebula.scaleY * offsetScl;
        float offsetZ = (float)Math.sin(seed * 0.63f + 1.7f) * nebula.scaleZ * offsetScl;
        float stretchX = 1f + (float)Math.sin(seed * 0.77f) * 0.16f;
        float stretchY = 1f + (float)Math.cos(seed * 1.13f) * 0.18f;
        float stretchZ = 1f + (float)Math.sin(seed * 1.51f) * 0.15f;
        transform.setToTranslationAndScaling(
                nebula.x + offsetX, nebula.y + offsetY, nebula.z + offsetZ,
                nebula.scaleX * scale * stretchX,
                nebula.scaleY * scale * stretchY,
                nebula.scaleZ * scale * stretchZ
        ).rotate(Vec3.Y, rotation).rotate(Vec3.X, seed * 2.1f).rotate(Vec3.Z, seed * 0.83f);

        var shader = NHShaders.galaxyNebula;
        shader.seed = seed;
        shader.alpha = alpha;
        shader.warp = warp;
        shader.palette = palette - (float)Math.floor(palette);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.apply();
        sphere.render(shader, Gl.triangles);
    }

    @Override
    public void dispose() {
        if (sphere != null) {
            sphere.dispose();
            sphere = null;
        }
    }

    private static class Nebula {
        final float x, y, z;
        final float scaleX, scaleY, scaleZ;
        final float seed, alpha, palette;

        Nebula(float x, float y, float z, float scaleX, float scaleY, float scaleZ, float seed, float alpha, float palette) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
            this.seed = seed;
            this.alpha = alpha;
            this.palette = palette;
        }
    }
}
