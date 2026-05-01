package newhorizon.expand.logic.components;

import arc.Events;
import arc.func.Func;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import newhorizon.expand.logic.components.action.NullAction;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static newhorizon.NHVars.cutsceneUI;

/**
 * Controls the execution of cutscene action buses.
 * Manages main bus queue, sub buses, and waiting periods between cutscenes.
 */
public class CutsceneControl {

    public static final String CSS_ACTION = "[CSS_ACTION]";
    public static ObjectMap<String, Func<String[], ? extends Action>> actionParser = new ObjectMap<>();

    // Whether currently waiting between cutscenes
    public boolean waiting = false;
    // Time spacing between cutscenes in ticks
    public float waitSpacing = 60f;
    // Current wait timer
    public float waitTimer = 0f;

    public ActionBus mainBus;
    public Seq<ActionBus> subBuses = new Seq<>();
    public Queue<ActionBus> waitingBuses = new Queue<>();

    public CutsceneControl() {
        Events.on(EventType.WorldLoadEvent.class, event -> clear());
    }

    public static void registerAction(Class<? extends Action> actionClass) {
        try {
            Action actionInstance = actionClass.getDeclaredConstructor().newInstance();
            actionParser.put(actionInstance.actionName(), tokens -> {
                try {
                    Action action = actionClass.getDeclaredConstructor().newInstance();
                    action.parseTokens(tokens);
                    action.postInit();
                    return action;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ActionBus parseCode(String code) {
        ActionBus bus = new ActionBus();
        parseLine(code).each(line -> bus.add(parseAction(line)));
        return bus;
    }

    public static Seq<String> parseLine(String code) {
        String[] lines = code.split("\\R");
        return Seq.with(lines);
    }

    public static Seq<String> parseToken(String line) {
        Seq<String> result = new Seq<>();
        Matcher matcher = Pattern.compile("<([^>]*)>|\\S+").matcher(line);
        while (matcher.find()) {
            result.add(matcher.group(1) != null ? matcher.group(1) : matcher.group());
        }
        return result;
    }

    public static Action parseAction(String tokens) {
        try {
            Log.info("Parsing String: " + tokens);
            Seq<String> tokensArray = parseToken(tokens);
            String actionName = tokensArray.remove(0);
            String[] args = tokensArray.toArray(String.class);
            return actionParser.get(actionName).get(args);
        }catch (Exception e) {
            Log.err("Error when parsing token: " + tokens);
            Log.err(e);
            return new NullAction();
        }
    }

    public void update() {
        updateMainBus();
        updateWaiting();
        startNextMainBus();
        updateSubBuses();
        cutsceneUI.update();
    }

    private void updateMainBus() {
        if (mainBus == null) return;

        mainBus.update();
        if (mainBus.complete()) {
            Log.info("MainBus has been completed");
            mainBus = null;
            waiting = true;
            cutsceneUI.reset();
        }
    }

    private void updateWaiting() {
        if (!waiting) return;

        waitTimer += Time.delta;
        if (waitTimer >= waitSpacing) {
            waitTimer = 0f;
            waiting = false;
        }
    }

    private void startNextMainBus() {
        if (mainBus == null && !waiting && !waitingBuses.isEmpty()) {
            mainBus = waitingBuses.removeLast();
        }
    }

    private void updateSubBuses() {
        for (int i = subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = subBuses.get(i);
            bus.update();
            if (bus.complete()) {
                subBuses.remove(i);
            }
        }
    }

    public void clear() {
        waiting = false;
        waitTimer = 0f;
        mainBus = null;
        waitingBuses.clear();
        subBuses.clear();
    }

    public static void saveActionBus(String name, String action) {
        Vars.state.rules.tags.put(CSS_ACTION + name, action);
    }

    public static String getActionBus(String name) {
        return Vars.state.rules.tags.get(CSS_ACTION + name, "");
    }

    //Add a main action bus to the queue. If no main bus is running, starts immediately; otherwise queues it.
    public void addMainActionBus(ActionBus bus) {
        if (bus == null) return;

        if (mainBus == null) {
            mainBus = bus;
        } else {
            waitingBuses.add(bus);
        }
    }

    //Add a sub action bus that runs in parallel.
    public void addSubActionBus(ActionBus bus) {
        if (bus != null) {
            subBuses.add(bus);
        }
    }
}
