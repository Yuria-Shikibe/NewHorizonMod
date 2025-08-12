package newhorizon;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;
import arc.util.viewport.Viewport;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.graphic.StatusRenderer;

import static arc.Core.camera;
import static arc.Core.graphics;

public class NHRenderer implements Disposable {
    public static float width, height;
    public FrameBuffer mask;
    public StatusRenderer statusRenderer;
    FrameBuffer captureBuffer = new FrameBuffer();

    public NHRenderer() {
        mask = new FrameBuffer();
        statusRenderer = new StatusRenderer();
    }

    public void draw() {
        width = graphics.getWidth();
        height = graphics.getHeight();

        statusRenderer.draw();

        mask.resize(graphics.getWidth(), graphics.getHeight());
        drawShader(NHShaders.displaceGlitch, 90.334f);
        drawShader(NHShaders.powerArea, NHContent.POWER_AREA);
        drawShader(NHShaders.powerDynamicArea, NHContent.POWER_DYNAMIC);
    }

    public void drawShader(Shader shader, float layer) {
        if (shader != null) {
            Draw.drawRange(layer, 0.0001f, () -> mask.begin(Color.clear), () -> {
                mask.end();
                mask.blit(shader);
            });
        }
    }

    public void drawGravityTrap() {
        Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
            mask.begin(Color.clear);
            GravityTrapField.drawAll();
            mask.end();
            mask.blit(NHShaders.gravityTrapShader);
        });
    }


    void captureViewport(Viewport viewport, Runnable drawCall){
        captureBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

        captureBuffer.begin();
        Core.graphics.clear(Color.clear);

        Draw.proj(viewport.getCamera());
        drawCall.run();
        Draw.flush();

        captureBuffer.end();
        Draw.proj(camera);
    }

    @Override
    public void dispose() {
        mask.dispose();
    }
}
