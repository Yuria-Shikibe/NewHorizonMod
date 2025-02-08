package newhorizon.expand.cutscene.components.ui;

import arc.struct.Seq;
import arc.util.Log;
import newhorizon.expand.cutscene.action.*;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.ui;

public class ActionControl {
    public static ActionBus phaseCode(String code){
        ActionBus bus = new ActionBus();
        phaseLine(code).each(line -> {
            bus.add(phaseAction(line));
        });
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

    public static Action phaseAction (String tokens){
        Seq<String> tokensArray = parseString(tokens);
        String actionName = tokensArray.remove(0);
        String[] args = tokensArray.toArray(String.class);
        try{
            switch (actionName){
                case "camera_control" : return new CameraControlAction(args);
                case "camera_reset" : return new CameraResetAction(args);

                case "curtain_draw" : return new CurtainDrawAction();
                case "curtain_raise" : return new CurtainRaiseAction();

                case "curtain_fade_in" : return new CurtainFadeInAction();
                case "curtain_fade_out" : return new CurtainFadeOutAction();

                case "info_fade_in" : return new InfoFadeInAction();
                case "info_fade_out" : return new InfoFadeOutAction();
                case "info_text" : return new InfoTextAction(args);

                case "input_lock" : return new InputLockAction();
                case "input_unlock" : return new InputUnlockAction();

                case "signal_cut_in" : return new SignalCutInAction();
                case "signal_cut_out" : return new SignalCutOutAction();
                case "signal_text" : return new SignalTextAction(args);

                case "wait" : return new WaitAction(args);

                case "" : return new NullAction();
            }
        }catch(Exception e){
            Log.err(e);
            ui.announce("Failed to parse action: " + tokens);
            return new NullAction();
        }
        return new NullAction();
    }
}
