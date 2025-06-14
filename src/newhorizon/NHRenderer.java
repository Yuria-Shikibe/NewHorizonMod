package newhorizon;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.util.Disposable;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.expand.entities.GravityTrapField;
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

        drawGravityTrapField();

        statusRenderer.draw();

        mask.resize(graphics.getWidth(), graphics.getHeight());

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

    //todo
    public void drawGravityTrapField() {
        //mask.resize(graphics.getWidth(), graphics.getHeight());
        //Building building = Vars.control.input.config.getSelected();
        //if(control.input.block instanceof GravityWallSubstation || (building != null && (building.block instanceof GravityWell || building.block instanceof HyperSpaceWarper))){
        //	Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
        //		mask.begin(Color.clear);
        //		GravityTrapField.drawAll();
        //		mask.end();
        //		mask.blit(NHShaders.gravityTrapShader);
        //	});
        //}
    }

    public void drawGravityTrap() {
        Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
            mask.begin(Color.clear);
            GravityTrapField.drawAll();
            mask.end();
            mask.blit(NHShaders.gravityTrapShader);
        });
    }

    @Override
    public void dispose() {
        mask.dispose();
    }
}
