package newhorizon.expand.logic;

import arc.util.Log;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;

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
            case "@waveteam":
                return state.rules.waveTeam;
            case "@derelict":
                return Team.derelict;
            case "@sharded":
                return Team.sharded;
            case "@crux":
                return Team.crux;
            case "@malis":
                return Team.malis;
            case "@green":
                return Team.green;
            case "@blue":
                return Team.blue;
        }

        try {
            return Team.get(parseIntToken(token, Team.derelict.id));
        } catch (Exception e) {
            Log.err(e);
            return Team.derelict;
        }
    }

    public static UnitType getUnitType(String[] tokens) {
        return resolveUnitType(getToken(tokens));
    }

    public static UnitType resolveUnitType(String token) {
        if (token == null || isNone(token)) return UnitTypes.dagger;
        UnitType unitType = null;
        if (token.startsWith("@")) unitType = Vars.content.unit(token.substring(1));
        if (unitType == null) unitType = Vars.content.unit(token);
        if (unitType == null) unitType = UnitTypes.dagger;
        return unitType;
    }

    public static StatusEffect getStatusEffect(String[] tokens) {
        return resolveStatusEffect(getToken(tokens));
    }

    public static StatusEffect resolveStatusEffect(String token) {
        if (token == null || isNone(token)) return StatusEffects.none;
        String name = token.startsWith("@") ? token.substring(1) : token;
        StatusEffect effect = Vars.content.statusEffect(name);
        return effect != null ? effect : StatusEffects.none;
    }

    public static Item resolveItem(String token) {
        if (token == null || isNone(token)) return null;
        String name = token.startsWith("@") ? token.substring(1) : token;
        return Vars.content.item(name);
    }

    public static Block resolveBlock(String token) {
        if (token == null || isNone(token)) return null;
        String name = token.startsWith("@") ? token.substring(1) : token;
        Block block = Vars.content.block(name);
        if (block == null || block == Blocks.air) return null;
        return block;
    }

    public static double resolveFlag(String token) {
        if (token == null || isNone(token) || token.equalsIgnoreCase("nan")) return Double.NaN;
        return Strings.parseDouble(token, Double.NaN);
    }

    public static boolean isNone(String token) {
        return token.isEmpty() || token.equals("-") || token.equalsIgnoreCase("none") || token.equals("null");
    }

    public static String contentToken(String name) {
        if (name == null || name.isEmpty() || isNone(name)) return "none";
        return name.startsWith("@") ? name : "@" + name;
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

    public static int parseIntToken(String token, int defaultValue) {
        int value = Strings.parseInt(token, Integer.MIN_VALUE);
        if (value != Integer.MIN_VALUE) return value;
        float f = Strings.parseFloat(token, Float.NaN);
        if (!Float.isNaN(f)) return (int) f;
        return defaultValue;
    }

    public static int getFirstInt(String[] tokens) {
        tokenIndex = 0;
        return parseIntToken(getToken(tokens), 0);
    }

    public static int getNextInt(String[] tokens) {
        tokenIndex++;
        return parseIntToken(getToken(tokens), 0);
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

    public static StatusEffect getNextStatusEffect(String[] tokens) {
        tokenIndex++;
        return getStatusEffect(tokens);
    }

    public static Item getNextItem(String[] tokens) {
        tokenIndex++;
        return resolveItem(getToken(tokens));
    }

    public static Block getNextBlock(String[] tokens) {
        tokenIndex++;
        return resolveBlock(getToken(tokens));
    }

    public static double getNextFlag(String[] tokens) {
        tokenIndex++;
        return resolveFlag(getToken(tokens));
    }
}
