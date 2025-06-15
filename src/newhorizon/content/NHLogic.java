package newhorizon.content;

import arc.Events;
import arc.func.Boolf;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.LogicBlock;
import newhorizon.expand.game.MapObjectives.ReuseObjective;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static mindustry.Vars.state;
import static mindustry.Vars.ui;

public class NHLogic {
    public static Seq<LogicBlock.LogicBuild> processors = new Seq<>();

    public static void load(){
        Events.on(EventType.WorldLoadEvent.class, event -> {
            updateWprocList();

        });
    }

    public static void registerDefaultRaidTimerWproc(){

    }

    public static void registerWproc(String code, String tag){
        AtomicBoolean contains = new AtomicBoolean(false);

        processors.each(logicBlock -> {
            if (Objects.equals(logicBlock.tag, tag)){
                contains.set(true);
            }
        });

        if (contains.get()){
            Log.info("Already registered wproc: " + tag + ", Skip.");
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
                        wproc.code = code;
                        wproc.tag = tag;
                    }
                    break outer;
                }
            }
        }

        if(!foundAny){
            Log.info("Failed to registered wproc: " + tag + ", no space available.");
        }
    }

    public static void updateWprocList(){
        //scan the entire world for processor (Groups.build can be empty, indexer is probably inaccurate)
        Vars.world.tiles.eachTile(t -> {
            if(t.isCenter() && t.block() == Blocks.worldProcessor){
                processors.add((LogicBlock.LogicBuild) t.build);
            }
        });
    }

    public static void registerTimer(String eventTrigger, String eventExecutor){
        if (!containObjective(obj -> obj instanceof ReuseObjective reuse && Objects.equals(reuse.trigger, eventTrigger))){
            objectives().add(new ReuseObjective(300f, eventTrigger, eventExecutor));
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
