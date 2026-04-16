package newhorizon.content;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.LogicIO;
import mindustry.graphics.Pal;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import newhorizon.NHSetting;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.CutsceneControl;
import newhorizon.expand.logic.components.action.*;
import newhorizon.expand.logic.cutscene.action.*;
import newhorizon.expand.logic.cutscene.actionBus.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.*;

public class NHLogic {
    public static Seq<LogicBlock.LogicBuild> processors = new Seq<>();
    public static LCategory nhwproc, nhcutscene, nhaction;
    public static LCategory actionCameraControl, actionInputControl, actionCurtainControl, actionFlowControl;

    public static void load(){
        NHLogic.nhwproc = new LCategory("nh-wproc", Pal.heal.cpy().lerp(Pal.gray, 0.2f));
        NHLogic.nhcutscene = new LCategory("nh-cutscene", Pal.remove.cpy().lerp(Pal.gray, 0.3f));

        NHLogic.nhaction = new LCategory("nh-action", Pal.surge.cpy().lerp(Pal.gray, 0.3f));

        NHLogic.actionFlowControl = new LCategory("nh-action-flow-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(0f));
        NHLogic.actionCameraControl = new LCategory("nh-action-camera-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(20f));
        NHLogic.actionInputControl = new LCategory("nh-action-input-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(40f));
        NHLogic.actionCurtainControl = new LCategory("nh-action-curtain-control", Pal.surge.cpy().lerp(Pal.gray, 0.3f).shiftHue(60f));

        loadStatements();
        loadActions();

        if (NHSetting.getBool(NHSetting.EVENT_RAID)){
            /*
            Events.on(EventType.PlayEvent.class, event -> {
                if (state.rules.mode() == Gamemode.sandbox || state.rules.mode() == Gamemode.pvp) return;
                updateWprocList();
                registerDefaultRaid();
            });

             */
        }
    }

    public static void loadStatements() {
        registerStatement(InitActons.class);
        registerStatement(SaveActions.class);
        registerStatement(GetActions.class);
        registerStatement(RunMainBus.class);
        registerStatement(RunSubBus.class);
    }

    public static void loadActions() {
        CutsceneControl.registerAction(NullAction.class);

        registerAction(Wait.class, WaitAction.class);

        registerAction(CurtainFadeIn.class, CurtainFadeInAction.class);
        registerAction(CurtainFadeOut.class, CurtainFadeOutAction.class);

        registerAction(CameraControl.class, CameraControlAction.class);
        registerAction(CameraZoom.class, CameraZoomAction.class);
        registerAction(CameraReset.class, CameraResetAction.class);

        registerAction(InputLock.class, InputLockAction.class);
        registerAction(InputUnlock.class, InputUnlockAction.class);

        registerAction(EventRaid.class, EventRaidAction.class);

        //registerAction(WarningIcon.class, WarningIconAction.class);
    }

    public static void registerAction(Class<? extends ActionLStatement> lstatement, Class<? extends Action> actionClass) {
        registerStatement(lstatement);
        CutsceneControl.registerAction(actionClass);
    }

    public static void registerStatement(Class<? extends ActionLStatement> lstatement){
        try {
            Constructor<? extends ActionLStatement> parserCons = lstatement.getDeclaredConstructor(String[].class);
            Constructor<? extends ActionLStatement> defaultCons = lstatement.getDeclaredConstructor();
            Method getNameMethod = lstatement.getDeclaredMethod("getLStatementName");

            parserCons.setAccessible(true);
            defaultCons.setAccessible(true);
            getNameMethod.setAccessible(true);

            String name;

            ActionLStatement tempInstance = defaultCons.newInstance();
            name = (String) getNameMethod.invoke(tempInstance);

            LAssembler.customParsers.put(name, (tokens) -> {
                try {
                    return parserCons.newInstance((Object) tokens);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create instance of " + lstatement.getSimpleName() + " with tokens", e);
                }
            });
            LogicIO.allStatements.addUnique(() -> {
                try {
                    return defaultCons.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to create default instance of " + lstatement.getSimpleName(), e);
                }
            });
        }catch (Exception e){
            Log.err(e);
        }
    }


    public static void registerDefaultRaid(){
        registerWproc("wait 300\n" + "setflag \"raid-trigger\" true", "raid protection period");
        registerWproc("defaultraid raid-executor raid-timer 30 5 200 1 5 20", "raid event");
    }

    public static void registerWproc(String code, String tag){
        AtomicBoolean contains = new AtomicBoolean(false);

        processors.each(logicBlock -> {
            if (Objects.equals(logicBlock.tag, tag)){
                contains.set(true);
            }
        });

        if (contains.get()){
            if (Log.level == Log.LogLevel.debug){
                Log.info("Already registered wproc: " + tag + ", Skip.");
            }
            return;
        }

        boolean foundAny = false;

        outer:
        for(int y = 0; y < Vars.world.height(); y++){
            for(int x = 0; x < Vars.world.width(); x++){
                Tile tile = Vars.world.rawTile(x, y);
                if(!tile.synthetic()){
                    foundAny = true;
                    tile.setNet(Blocks.worldProcessor, Team.sharded, 0);
                    if (tile.build instanceof LogicBlock.LogicBuild wproc){
                        wproc.updateCode(code);
                        wproc.tag = tag;
                    }
                    if (!headless && ui.editor.isShown()) updateStatic(x, y);

                    Log.info("Registered wproc: " + tag);
                    break outer;
                }
            }
        }

        if(!foundAny){
            Log.info("Failed to registered wproc: " + tag + ", no space available.");
        }
    }

    public static void updateStatic(int x, int y){
        renderer.blocks.floor.recacheTile(x, y);
        if(x > 0) renderer.blocks.floor.recacheTile(x - 1, y);
        if(y > 0) renderer.blocks.floor.recacheTile(x, y - 1);
        if(x < world.width() - 1) renderer.blocks.floor.recacheTile(x + 1, y);
        if(y < world.height() - 1) renderer.blocks.floor.recacheTile(x, y + 1);
    }

    public static void updateWprocList(){
        Vars.world.tiles.eachTile(t -> {
            if(t.isCenter() && t.block() == Blocks.worldProcessor){
                processors.add((LogicBlock.LogicBuild) t.build);
            }
        });
    }
}
