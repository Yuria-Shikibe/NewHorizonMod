package newhorizon.content;

import arc.Events;
import arc.func.Boolf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import newhorizon.expand.game.MapObjectives.ReuseObjective;
import newhorizon.expand.game.MapObjectives.TriggerObjective;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.state;
import static mindustry.Vars.ui;

public class NHLogic {
    public static Seq<LogicBlock.LogicBuild> processors = new Seq<>();

    public static void load(){
        Events.on(EventType.PlayEvent.class, event -> {
            if (state.rules.mode() == Gamemode.sandbox || state.rules.mode() == Gamemode.pvp) return;
            updateWprocList();
            registerDefaultRaid();


        });
    }

    public static void registerDefaultRaid(){
        registerWproc("wait 300\n" + "setflag \"raid-trigger\" true", "raid protection period");
        registerWproc("defaultraid raid-executor raid-timer 30 5 200 1 5 30", "raid event");

        registerReuseTimer(300f * Time.toSeconds, "raid-trigger", "raid-executor");
        registerTriggerTimer("raid-timer");
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
                    if(ui.editor.isShown()){
                        Vars.editor.renderer.updatePoint(x, y);
                    }
                    Log.info("Registered wproc: " + tag);
                    break outer;
                }
            }
        }

        if(!foundAny){
            Log.info("Failed to registered wproc: " + tag + ", no space available.");
        }
    }

    public static void updateWprocList(){
        Vars.world.tiles.eachTile(t -> {
            if(t.isCenter() && t.block() == Blocks.worldProcessor){
                processors.add((LogicBlock.LogicBuild) t.build);
            }
        });
    }

    public static void registerReuseTimer(float time, String eventTrigger, String eventExecutor){
        if (!(containObjective(obj -> obj instanceof ReuseObjective reuse &&
                Objects.equals(reuse.trigger, eventTrigger) &&
                Objects.equals(reuse.executor, eventExecutor)))){
            //not so sure if this is a safe option, anuke said not modify it directly
            objectives().all.add(new ReuseObjective(time, eventTrigger, eventExecutor));
            Log.info("Registered reuse timer: " + eventTrigger + "|" + eventExecutor);
        }else {
            if (Log.level == Log.LogLevel.debug){
                Log.info("Already registered reuse timer: " + eventTrigger + "|" + eventExecutor + ", Skip.");
            }
        }
    }

    public static void registerTriggerTimer(String eventTimer){
        if (!(containObjective(obj -> obj instanceof TriggerObjective reuse && Objects.equals(reuse.timer, eventTimer)))){
            objectives().all.add(new TriggerObjective(eventTimer));
            Log.info("Registered trigger timer: " + eventTimer);
        }else {
            if (Log.level == Log.LogLevel.debug){
                Log.info("Already registered Trigger timer: " + eventTimer + ", Skip.");
            }
        }
    }

    public static MapObjectives objectives(){
        return state.rules.objectives;
    }

    public static boolean containObjective(Boolf<MapObjectives.MapObjective> checker){
        AtomicBoolean found = new AtomicBoolean(false);
        objectives().each(objective -> {
            if (checker.get(objective)) found.set(true);
        });
        return found.get();
    }
}
