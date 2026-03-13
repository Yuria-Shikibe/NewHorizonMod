package newhorizon.expand.logic;

public class ParseUtil {
    public static int tokenIndex = 0;

    public static String getFirstToken(String[] tokens) {
        tokenIndex = 0;
        return (tokenIndex < tokens.length) ? tokens[tokenIndex] : "0";
    }

    public static String getNextToken(String[] tokens) {
        tokenIndex++;
        return (tokenIndex < tokens.length) ? tokens[tokenIndex] : "0";
    }
}
