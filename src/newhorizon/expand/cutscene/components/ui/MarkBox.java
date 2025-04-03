package newhorizon.expand.cutscene.components.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import newhorizon.util.func.MathUtil;
import newhorizon.util.func.NHInterp;

import static newhorizon.NHVars.cutsceneUI;

public class MarkBox extends Table{
    protected static final Vec2 tmpVec = new Vec2();
    protected static final Color tmpColor = new Color();

    public float radius = 24f;
    public Color markColor = Color.white;
    public Interp popUpInterp = NHInterp.bounce5Out;
    public Position markPoint;
    public MarkStyle style = MarkStyle.defaultStyle;
    public int id = lastID++;

    protected static int lastID = 0;

    public float lifetime = -1;

    public MarkBox() {
        touchable = Touchable.childrenOnly;
        fillParent = true;

        update(() -> {
            if (!Vars.state.isPaused()) totalProgress += Time.delta;
            //todo add save here
            if (Vars.state.isMenu()) remove();
        });

        color.a = 0;
    }

    public void addSelf() {
        cutsceneUI.root.addChild(this);
        setZIndex(0);
    }

    public MarkBox init(float radius, Color markColor, Position markPoint, MarkStyle style) {
        this.radius = radius;
        this.markColor = markColor;
        this.markPoint = markPoint;
        this.style = style;

        return this;
    }

    public void setLife(float lifetime) {
        this.lifetime = lifetime;
    }

    protected float totalProgress = 0;

    {
        actions(Actions.alpha(1, 0.45f, popUpInterp));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (totalProgress > lifetime) {
            removeFromHUD();
        }
    }

    public void removeFromHUD() {
        actions(Actions.fadeOut(0.5f), Actions.remove());
    }

    @Override
    public void draw() {
        super.draw();

        if (Vars.headless) return;

        Vec2 screenVec = tmpVec.set(Core.camera.project(markPoint.getX(), markPoint.getY()));
        Vec2 originVec = screenVec.cpy();


        boolean outer = screenVec.x < width * 0.05f || screenVec.y < height * 0.05f || screenVec.x > width * 0.95f || screenVec.y > height * 0.95f;

        if (outer) {
            screenVec.x = Mathf.clamp(screenVec.x, width * 0.05f, width * 0.95f);
            screenVec.y = Mathf.clamp(screenVec.y, height * 0.05f, height * 0.95f);
        }

        tmpColor.set(markColor).lerp(Color.white, MathUtil.timeValue(0.0f, 0.2f, 1)).a(color.a);

        style.drawer.draw(id, totalProgress / lifetime, radius, screenVec, originVec, tmpColor, outer);
    }
}
