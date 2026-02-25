package newhorizon;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.util.graphic.StatusRenderer;

import static arc.Core.graphics;

public class NHRenderer implements Disposable {
    public static float width, height;
    public FrameBuffer mask;
    public StatusRenderer statusRenderer;

    public NHRenderer() {
        mask = new FrameBuffer();
        statusRenderer = new StatusRenderer();
    }

    public void draw() {
        width = graphics.getWidth();
        height = graphics.getHeight();

        statusRenderer.draw();

        mask.resize(graphics.getWidth(), graphics.getHeight());

        drawShader(NHShaders.gravityTrap, NHContent.GRAVITY_TRAP_LAYER);
        drawShader(NHShaders.hexShield, NHContent.HEX_SHIELD_LAYER);
    }

    public void drawShader(Shader shader, float layer) {
        if (shader != null) {
            Draw.drawRange(layer, 0.0001f, () -> mask.begin(Color.clear), () -> {
                mask.end();
                mask.blit(shader);
            });
        }
    }

    @Override
    public void dispose() {
        mask.dispose();
    }
}
