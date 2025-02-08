package newhorizon.expand.cutscene.components.ui;

import arc.func.Cons;
import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import newhorizon.expand.cutscene.action.InputLockAction;
import newhorizon.expand.cutscene.action.InputUnlockAction;
import newhorizon.expand.cutscene.action.NullAction;
import newhorizon.expand.cutscene.action.WaitAction;
import newhorizon.expand.cutscene.components.Action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mindustry.Vars.ui;

public class ActionControl {
    //"wait_action 123 567 <wait action>" -> ["wait_action" "123" "567" "wait action"]
    public static Seq<String> parseString(String input) {
        Seq<String> result = new Seq<>();
        Matcher matcher = Pattern.compile("<([^>]*)>|\\S+").matcher(input);
        while (matcher.find()) {
            result.add(matcher.group(1) != null ? matcher.group(1) : matcher.group());
        }
        return result;
    }

    public static Action phaseAction (String tokens){
        Seq<String> tokensArray = parseString(tokens);
        String actionName = tokensArray.remove(0);
        String[] args = tokensArray.toArray(String.class);
        try{
            switch (actionName){
                case "input_lock": return new InputLockAction();
                case "input_unlock": return new InputUnlockAction();
                case "wait_action": return new WaitAction(args);
            }
        }catch(Exception e){
            Log.err(e);
            ui.announce("Failed to parse action: " + tokens);
            return new NullAction();
        }
        return new NullAction();
    }
}
