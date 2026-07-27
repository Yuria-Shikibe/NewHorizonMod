package newhorizon.expand.logic.cutscene.letterbox;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static mindustry.Vars.renderer;
import static newhorizon.NHVars.cutsceneUI;

public class FreeZoom extends LStatement {
    public String duration = "1", zoom = "2";

    public FreeZoom(String[] tokens) {
        duration = tokens.length > 1 ? tokens[1] : "1";
        zoom = tokens.length > 2 ? tokens[2] : "2";
    }

    public FreeZoom() {
    }

    @Override
    public void build(Table table) {
        table.table(t -> {
            t.add(" Duration: ");
            fields(t, duration, str -> duration = str);
        }).left().row();

        table.table(t -> {
            t.add(" Zoom: ");
            fields(t, zoom, str -> zoom = str);
            t.add(" (abs scale, <=0 releases)").padLeft(6f);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHLogic.actionCameraControl;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("freezoom ").append(duration).append(" ").append(zoom);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new FreeZoomI(builder.var(duration), builder.var(zoom));
    }

    public static class FreeZoomI implements LExecutor.LInstruction {
        public LVar duration;
        public LVar zoom;
        public float timer;
        public float startScale;
        public float targetScale;
        public boolean releasing;

        public FreeZoomI(LVar duration, LVar zoom) {
            this.duration = duration;
            this.zoom = zoom;
        }

        @Override
        public void run(LExecutor exec) {
            if (headless || cutsceneUI == null) {
                timer = 0f;
                return;
            }

            if (timer <= 0f) {
                startScale = renderer != null ? renderer.camerascale : 1f;
                if (cutsceneUI.forceCameraZoom) startScale = cutsceneUI.forceCameraZoomScale;

                float requested = zoom.numf();
                releasing = requested <= 0f;
                if (releasing) {
                    targetScale = renderer != null ? Mathf.clamp(startScale, renderer.minZoomInGame, renderer.maxZoomInGame) : 4f;
                    if (Mathf.equal(targetScale, startScale, 0.001f)) {
                        cutsceneUI.clearForcedCameraZoom();
                        control.input.logicCutscene = false;
                        timer = 0f;
                        return;
                    }
                } else {
                    targetScale = Math.max(requested, 0.01f);
                }

                cutsceneUI.setForcedCameraZoom(startScale);
                timer = Time.delta;
            } else {
                timer += Time.delta;
            }

            float dur = Math.max(duration.numf(), 0f) * Time.toSeconds;
            float progress = dur <= 0f ? 1f : Mathf.clamp(timer / dur);
            float scale = Mathf.lerp(startScale, targetScale, progress);
            cutsceneUI.setForcedCameraZoom(scale);

            if (progress < 1f) {
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            if (releasing) {
                cutsceneUI.clearForcedCameraZoom();
                control.input.logicCutscene = false;
            } else {
                cutsceneUI.setForcedCameraZoom(targetScale);
            }
            timer = 0f;
        }
    }
}