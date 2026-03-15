package newhorizon.expand.logic.components;

import arc.struct.Seq;
import arc.util.Log;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.UnitType;
import mindustry.world.blocks.logic.MemoryBlock;
import newhorizon.expand.logic.components.action.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.*;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
public class ActionControl {
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

    public static Action parseAction(String tokens) {
        Seq<String> tokensArray = parseToken(tokens);
        String actionName = tokensArray.remove(0);
        String[] args = tokensArray.toArray(String.class);
        try {
            return switch (actionName) {
                case "camera_control" -> new CameraControlAction();
                case "camera_reset" -> new CameraResetAction();
                case "camera_zoom" -> new CameraZoomAction();

                case "curtain_draw" -> new CurtainDrawAction();
                case "curtain_raise" -> new CurtainRaiseAction();
                case "curtain_fade_in" -> new CurtainFadeInAction();
                case "curtain_fade_out" -> new CurtainFadeOutAction();

                case "info_fade_in" -> new InfoFadeInAction();
                case "info_fade_out" -> new InfoFadeOutAction();
                case "info_text" -> new InfoTextAction();

                case "input_lock" -> new InputLockAction();
                case "input_unlock" -> new InputUnlockAction();

                case "jump_in" -> new JumpInAction();

                case "mark_world" -> new MarkWorldAction();

                case "raid" -> new RaidAction();

                case "signal_cut_in" -> new SignalCutInAction();
                case "signal_cut_out" -> new SignalCutOutAction();
                case "signal_text" -> new SignalTextAction();

                case "ui_hide" -> new UIHideAction();
                case "ui_show" -> new UIShowAction();

                case "wait" -> new WaitAction();

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
