package newhorizon.util.graphic;

import arc.Core;
import arc.Events;
import arc.graphics.Camera;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import mindustry.graphics.Shaders;
import newhorizon.NewHorizon;
import newhorizon.content.NHShaders;

public class ScreenShaderDrawer {
    private static final FrameBuffer pingPong1 = new FrameBuffer();
    private static final FrameBuffer pingPong2 = new FrameBuffer();

    public static boolean drawDisplaceGlitch = false;

    public static void init(){
        Events.run(EventType.Trigger.drawOver, () -> {
            Draw.draw(Layer.min, ScreenShaderDrawer::drawBegin);
            Draw.draw(Layer.max, ScreenShaderDrawer::drawEnd);
        });
    }

    public static void drawBegin(){
        pingPong1.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        pingPong2.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

        pingPong1.begin();
    }

    public static void drawEnd(Camera camera){
        FrameBuffer from = pingPong1;

        if(drawDisplaceGlitch) from = pingPong(from, NHShaders.displaceGlitch, camera);

        from.end();
        from.blit(Shaders.screenspace);
    }

    public static void drawEnd(){
        drawEnd(Core.camera);
    }

    private static FrameBuffer pingPong(FrameBuffer from, NHShaders.ModSurfaceShader shader, Camera camera){
        FrameBuffer to = from == pingPong1 ? pingPong2 : pingPong1;

        from.end();
        to.begin();
        blit(shader, from, camera);

        return to;
    }

    private static void drawScreen(FrameBuffer active, NHShaders.ModSurfaceShader shader, Camera camera){
        FrameBuffer screenBuffer = active == pingPong1 ? pingPong2 : pingPong1;

        screenBuffer.begin();
        Draw.rect();
        screenBuffer.end();

        blit(shader, screenBuffer, camera);
    }

    private static void blit(NHShaders.ModSurfaceShader shader, FrameBuffer buffer, Camera camera){
        shader.texture = buffer.getTexture();
        buffer.blit(shader);
    }
}
