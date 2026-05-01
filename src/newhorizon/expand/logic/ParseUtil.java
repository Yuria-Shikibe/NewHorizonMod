package newhorizon.expand.logic;

import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.logic.GlobalVars;
import mindustry.type.UnitType;

import static mindustry.Vars.state;

public class ParseUtil {
    public static int tokenIndex = 0;

    public static String getToken(String[] tokens, String defaultValue) {
        return (tokenIndex < tokens.length) ? tokens[tokenIndex] : defaultValue;
    }

    public static String getToken(String[] tokens) {
        return getToken(tokens, "0");
    }

    public static Team getTeam(String[] tokens) {
        String token = getToken(tokens);

        switch (token) {
            case "@waveteam": return state.rules.waveTeam;
            case "@derelict": return Team.derelict;
            case "@sharded": return Team.sharded;
            case "@crux": return Team.crux;
            case "@malis": return Team.malis;
            case "@green": return Team.green;
            case "@blue": return Team.blue;
        }

        try {
            int teamID = Integer.parseInt(token);
            return Team.get(teamID);
        } catch (NumberFormatException e) {
            Log.err(e);
            return Team.derelict;
        }
    }

    public static UnitType getUnitType(String[] tokens) {
        String token = getToken(tokens);
        UnitType unitType = null;
        if (token.startsWith("@")) unitType = Vars.content.unit(token.substring(1));
        if (unitType == null) unitType = UnitTypes.dagger;
        return unitType;
    }

    public static String getFirstToken(String[] tokens) {
        tokenIndex = 0;
        return getToken(tokens);
    }

    public static String getNextToken(String[] tokens) {
        tokenIndex++;
        return getToken(tokens);
    }

    public static float getFirstFloat(String[] tokens) {
        tokenIndex = 0;
        return Strings.parseFloat(getToken(tokens), 0f);
    }

    public static float getNextFloat(String[] tokens) {
        tokenIndex++;
        return Strings.parseFloat(getToken(tokens), 0f);
    }

    public static int getFirstInt(String[] tokens) {
        tokenIndex = 0;
        return Strings.parseInt(getToken(tokens), 0);
    }

    public static int getNextInt(String[] tokens) {
        tokenIndex++;
        return Strings.parseInt(getToken(tokens), 0);
    }

    public static boolean getFirstBool(String[] tokens) {
        tokenIndex = 0;
        return getToken(tokens).equals("true");
    }

    public static boolean getNextBool(String[] tokens) {
        tokenIndex++;
        return getToken(tokens).equals("true");
    }

    public static String getFirstString(String[] tokens) {
        tokenIndex = 0;
        return getToken(tokens).replace("[n]", "\n");
    }

    public static String getNextString(String[] tokens) {
        tokenIndex++;
        return getToken(tokens).replace("[n]", "\n");
    }

    public static Team getFirstTeam(String[] tokens) {
        tokenIndex = 0;
        return getTeam(tokens);
    }

    public static Team getNextTeam(String[] tokens) {
        tokenIndex++;
        return getTeam(tokens);
    }

    public static UnitType getFirstUnitType(String[] tokens) {
        tokenIndex = 0;
        return getUnitType(tokens);
    }

    public static UnitType getNextUnitType(String[] tokens) {
        tokenIndex++;
        return getUnitType(tokens);
    }
}
