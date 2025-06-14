package newhorizon.expand.cutscene.components;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.UnitType;
import mindustry.world.blocks.logic.MemoryBlock;
import newhorizon.expand.cutscene.action.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.*;

public class ActionControl {
    public static ActionBus parseCode(String code, Building source) {
        ActionBus bus = new ActionBus();
        parseLine(code, source).each(line -> bus.add(parseAction(line, source)));
        return bus;
    }

    public static Seq<String> parseLine(String code, Building source) {
        String[] lines = code.split("\\R");
        return Seq.with(lines);
    }

    public static Seq<String> parseToken(String line, Building source) {
        Seq<String> result = new Seq<>();
        Matcher matcher = Pattern.compile("<([^>]*)>|\\S+").matcher(line);
        while (matcher.find()) {
            result.add(matcher.group(1) != null ? matcher.group(1) : matcher.group());
        }
        return result;
    }

    public static String parseString(String token, Building source) {
        return parseString(token);
    }

    public static String parseString(String token) {
        return token.replace("[n]", "\n");
    }

    public static float parseFloat(String token, Building source) {
        if (token.startsWith("@") && source != null && world.build(source.tileX(), source.tileY() - 1).block instanceof MemoryBlock) {
            MemoryBlock.MemoryBuild memory = (MemoryBlock.MemoryBuild) world.build(source.tileX(), source.tileY() - 1);
            return (float) memory.memory[Integer.parseInt(token.replace("@", ""))];
        } else return Float.parseFloat(token);
    }

    public static Team parseTeam(String token) {
        switch (token) {
            case "derelict":
                return Team.derelict;
            case "sharded":
                return Team.sharded;
            case "crux":
                return Team.crux;
            case "malis":
                return Team.malis;
            case "green":
                return Team.green;
            case "blue":
                return Team.blue;
            case "neoplastic":
                return Team.neoplastic;
        }

        try {
            int teamID = Integer.parseInt(token);
            return Team.get(teamID);
        } catch (NumberFormatException e) {
            Log.err(e);
            return Team.derelict;
        }
    }

    public static UnitType parseUnitType(String token) {
        if (content.unit(token) != null) {
            return content.unit(token);
        }
        try {
            int id = Integer.parseInt(token);
            if (content.unit(id) != null) {
                return content.unit(id);
            }
        } catch (NumberFormatException e) {
            Log.err(e);
        }
        return UnitTypes.alpha;
    }

    public static Action parseAction(String tokens, Building source) {
        Seq<String> tokensArray = parseToken(tokens, source);
        String actionName = tokensArray.remove(0);
        String[] args = tokensArray.toArray(String.class);
        try {
            return switch (actionName) {
                case "camera_control" -> new CameraControlAction(args);
                case "camera_reset" -> new CameraResetAction(args);
                case "camera_zoom" -> new CameraZoomAction(args);

                case "curtain_draw" -> new CurtainDrawAction();
                case "curtain_raise" -> new CurtainRaiseAction();
                case "curtain_fade_in" -> new CurtainFadeInAction();
                case "curtain_fade_out" -> new CurtainFadeOutAction();

                case "info_fade_in" -> new InfoFadeInAction();
                case "info_fade_out" -> new InfoFadeOutAction();
                case "info_text" -> new InfoTextAction(args);

                case "input_lock" -> new InputLockAction();
                case "input_unlock" -> new InputUnlockAction();

                case "jump_in" -> new JumpInAction(args, source);

                case "mark_world" -> new MarkWorldAction(args, source);

                case "raid" -> new RaidAction(args, source);

                case "signal_cut_in" -> new SignalCutInAction();
                case "signal_cut_out" -> new SignalCutOutAction();
                case "signal_text" -> new SignalTextAction(args);

                case "ui_hide" -> new UIHideAction();
                case "ui_show" -> new UIShowAction();

                case "wait" -> new WaitAction(args);

                case "warning_icon" -> new WarningIconAction(args);
                case "warning_sound" -> new WarningSoundAction(args);

                default -> new NullAction();
            };
        } catch (Exception e) {
            Log.err(e);
            ui.announce("Failed to parse action: " + tokens);
            return new NullAction();
        }
    }
}
