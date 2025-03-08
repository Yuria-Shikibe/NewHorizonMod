package newhorizon.expand.cutscene.components;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.UnitType;
import newhorizon.expand.cutscene.action.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.content;
import static mindustry.Vars.ui;

public class ActionControl {
    public static ActionBus phaseCode(String code, Building source){
        ActionBus bus = new ActionBus();
        phaseLine(code).each(line -> bus.add(phaseAction(line, source)));
        return bus;
    }

    public static Seq<String> phaseLine(String code){
        String[] lines = code.split("\\R");
        return Seq.with(lines);
    }

    public static Seq<String> parseString(String line) {
        Seq<String> result = new Seq<>();
        Matcher matcher = Pattern.compile("<([^>]*)>|\\S+").matcher(line);
        while (matcher.find()) {
            result.add(matcher.group(1) != null ? matcher.group(1) : matcher.group());
        }
        return result;
    }

    public static String phaseString(String token) {
        return token.replace("[n]", "\n");
    }

    public static Team phaseTeam(String token){
        switch (token){
            case "derelict": return Team.derelict;
            case "sharded": return Team.sharded;
            case "crux": return Team.crux;
            case "malis": return Team.malis;
            case "green": return Team.green;
            case "blue": return Team.blue;
            case "neoplastic": return Team.neoplastic;
        }

        try {
            int teamID = Integer.parseInt(token);
            return Team.get(teamID);
        }catch (NumberFormatException e){
            Log.err(e);
            return Team.derelict;
        }
    }

    public static UnitType phaseUnitType(String token){
        if (content.unit(token) != null){
            return content.unit(token);
        }
        try {
            int id = Integer.parseInt(token);
            if (content.unit(id) != null){
                return content.unit(id);
            }
        }catch (NumberFormatException e){
            Log.err(e);
        }
        return UnitTypes.alpha;
    }

    public static Action phaseAction (String tokens, Building source){
        Seq<String> tokensArray = parseString(tokens);
        String actionName = tokensArray.remove(0);
        String[] args = tokensArray.toArray(String.class);
        try{
            return switch (actionName) {
                case "camera_control" -> new CameraControlAction(args);
                case "camera_pan" -> new CameraPanAction(args);
                case "camera_reset" -> new CameraResetAction(args);
                case "camera_set" -> new CameraSetAction(args);

                case "curtain_draw" -> new CurtainDrawAction();
                case "curtain_raise" -> new CurtainRaiseAction();
                case "curtain_fade_in" -> new CurtainFadeInAction();
                case "curtain_fade_out" -> new CurtainFadeOutAction();

                case "info_fade_in" -> new InfoFadeInAction();
                case "info_fade_out" -> new InfoFadeOutAction();
                case "info_text" -> new InfoTextAction(args);

                case "input_lock" -> new InputLockAction();
                case "input_unlock" -> new InputUnlockAction();

                case "jump_in" -> new JumpInAction(args);

                case "mark_world" -> new MarkWorldAction(args);

                case "raid" -> new RaidAction(args);

                case "signal_cut_in" -> new SignalCutInAction();
                case "signal_cut_out" -> new SignalCutOutAction();
                case "signal_text" -> new SignalTextAction(args);

                case "trigger_activate" -> new TriggerActivateAction(args, source);

                case "wait" -> new WaitAction(args);

                case "warning_icon" -> new WarningIconAction(args);
                case "warning_sound" -> new WarningSoundAction(args);

                default -> new NullAction();
            };
        }catch(Exception e){
            Log.err(e);
            ui.announce("Failed to parse action: " + tokens);
            return new NullAction();
        }
    }
}
