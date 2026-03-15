package newhorizon.expand.logic.components;

import arc.func.Func;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.UnitType;
import mindustry.world.blocks.logic.MemoryBlock;
import newhorizon.expand.logic.components.action.*;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.*;

public class ActionControl {
    public static ObjectMap<String, Func<String[], ? extends Action>> actionParser = new ObjectMap<>();

    public static void registerAction(Class<? extends Action> actionClass) {
        try {
            Action action = actionClass.getDeclaredConstructor().newInstance();
            actionParser.put(action.actionName(), tokens -> {
                action.parseTokens(tokens);
                return action;
            });
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
            Seq<String> tokensArray = parseToken(tokens);
            String actionName = tokensArray.remove(0);
            String[] args = tokensArray.toArray(String.class);
            return actionParser.get(actionName).get(args);
        }catch (Exception e) {
            Log.err("Error when parsing token:" + tokens);
            Log.err(e);
            return new NullAction();
        }
    }
}
