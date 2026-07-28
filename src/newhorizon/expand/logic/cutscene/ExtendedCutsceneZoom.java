package newhorizon.expand.logic.cutscene;

import arc.Events;
import arc.math.Mathf;
import arc.scene.ui.layout.Scl;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.logic.CutsceneAction;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.blocks.logic.LogicBlock;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static mindustry.Vars.renderer;

/** Extends the vanilla cutscene zoom instruction while keeping its statement syntax unchanged. */
public final class ExtendedCutsceneZoom {
    public static final float maxZoomLevel = 100f;

    private static final ObjectMap<LExecutor, LExecutor.LInstruction[]> patchedExecutors = new ObjectMap<>();
    private static final ObjectMap<LogicBlock.LogicBuild, Runnable> wrappedLoadBlocks = new ObjectMap<>();
    private static boolean loaded;

    private static float savedMinZoom = -1f;
    private static float savedMaxZoom = -1f;
    private static float savedMinZoomInGame = -1f;
    private static float savedMaxZoomInGame = -1f;

    private ExtendedCutsceneZoom() {
    }

    public static void load() {
        if (loaded) return;
        loaded = true;

        Events.run(EventType.Trigger.update, ExtendedCutsceneZoom::patchProcessors);
        Events.on(EventType.WorldLoadEvent.class, event -> {
            restoreZoomBounds();
            patchedExecutors.clear();
            wrappedLoadBlocks.clear();
        });
    }

    private static void patchProcessors() {
        Groups.build.each(build -> {
            if (!(build instanceof LogicBlock.LogicBuild logic)) return;

            patchExecutor(logic.executor);

            Runnable pendingLoad = logic.loadBlock;
            if (pendingLoad != null && wrappedLoadBlocks.get(logic) != pendingLoad) {
                Runnable wrapped = () -> {
                    pendingLoad.run();
                    patchExecutor(logic.executor);
                };
                wrappedLoadBlocks.put(logic, wrapped);
                logic.loadBlock = wrapped;
            }
        });
    }

    private static void patchExecutor(LExecutor executor) {
        if (executor == null || patchedExecutors.get(executor) == executor.instructions) return;

        for (int i = 0; i < executor.instructions.length; i++) {
            if (executor.instructions[i] instanceof LExecutor.CutsceneI instruction) {
                executor.instructions[i] = new ExtendedCutsceneI(
                        instruction.action,
                        instruction.p1,
                        instruction.p2,
                        instruction.p3,
                        instruction.p4
                );
            }
        }

        patchedExecutors.put(executor, executor.instructions);
    }

    private static void extendZoomBounds(float targetScale) {
        if (renderer == null) return;

        if (savedMinZoom < 0f) {
            savedMinZoom = renderer.minZoom;
            savedMaxZoom = renderer.maxZoom;
            savedMinZoomInGame = renderer.minZoomInGame;
            savedMaxZoomInGame = renderer.maxZoomInGame;
        }

        float uiScale = Math.max(Scl.scl(1f), 0.0001f);
        float requiredMaxZoom = Math.max(targetScale, (float)Math.ceil(targetScale) / uiScale);
        renderer.maxZoom = Math.max(renderer.maxZoom, requiredMaxZoom);
        renderer.maxZoomInGame = Math.max(renderer.maxZoomInGame, requiredMaxZoom);
    }

    private static void restoreZoomBounds() {
        if (renderer != null && savedMinZoom >= 0f) {
            renderer.minZoom = savedMinZoom;
            renderer.maxZoom = savedMaxZoom;
            renderer.minZoomInGame = savedMinZoomInGame;
            renderer.maxZoomInGame = savedMaxZoomInGame;
        }

        savedMinZoom = -1f;
        savedMaxZoom = -1f;
        savedMinZoomInGame = -1f;
        savedMaxZoomInGame = -1f;
    }

    public static class ExtendedCutsceneI implements LExecutor.LInstruction {
        public CutsceneAction action;
        public LVar p1, p2, p3, p4;

        public ExtendedCutsceneI(CutsceneAction action, LVar p1, LVar p2, LVar p3, LVar p4) {
            this.action = action;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.p4 = p4;
        }

        @Override
        public void run(LExecutor exec) {
            if (headless) return;

            switch (action) {
                case pan -> {
                    control.input.logicCutscene = true;
                    control.input.logicCamPan.set(Vars.world.unconv(p1.numf()), Vars.world.unconv(p2.numf()));
                    control.input.logicCamSpeed = p3.numf();
                }
                case zoom -> {
                    control.input.logicCutscene = true;

                    float level = p1.numf();
                    if (level <= 1f) {
                        restoreZoomBounds();
                        control.input.logicCutsceneZoom = Mathf.clamp(level);
                    } else if (renderer != null) {
                        float targetScale = Mathf.clamp(level, 0f, maxZoomLevel);
                        extendZoomBounds(targetScale);

                        float span = renderer.maxZoom - renderer.minZoom;
                        control.input.logicCutsceneZoom = span <= 0.0001f
                                ? 0f
                                : Mathf.clamp((targetScale - renderer.minZoom) / span);
                    }
                }
                case stop -> {
                    restoreZoomBounds();
                    control.input.logicCutscene = false;
                }
            }
        }
    }
}
