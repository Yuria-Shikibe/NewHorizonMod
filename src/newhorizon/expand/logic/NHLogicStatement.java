package newhorizon.expand.logic;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;

/**
 * Base class for logic statements.
 * Provides safe token parsing and automatic serialization utilities.
 */
public abstract class NHLogicStatement extends LStatement {
    public NHLogicStatement(String[] tokens) {
        try {
            parseTokens(tokens);
        } catch (Exception e) {
            Log.err("Failed to parse tokens for " + getClass().getSimpleName(), e);
        }
    }

    public NHLogicStatement() {
    }

    /**
     * Parse tokens from string array.
     * Override this method to implement custom parsing logic.
     */
    protected void parseTokens(String[] tokens) {

    }

    @Override
    public void build(Table table) {
        // Override to build UI
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        // Override to build instruction
        return null;
    }

    /**
     * Get the statement code representation.
     * Uses name and vars by default.
     */
    public String statementCode() {
        StringBuilder str = new StringBuilder();
        //str.append(name).append(" ");
        //for (String token : vars) {
        //    str.append(token).append(" ");
        //}
        return str.toString();
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append(statementCode());
    }

    // ========== Safe Token Parsing Methods ==========

    /**
     * Safely get token at index with default value.
     * Returns default if index is out of bounds.
     */
    protected String token(String[] tokens, int index, String defaultValue) {
        if (tokens == null || index < 0 || index >= tokens.length) {
            return defaultValue;
        }
        String value = tokens[index];
        return value != null && !value.isEmpty() ? value : defaultValue;
    }

    /**
     * Safely get token at index.
     * Returns empty string if index is out of bounds.
     */
    protected String token(String[] tokens, int index) {
        return token(tokens, index, "");
    }

    /**
     * Safely parse float token with default value.
     */
    protected float tokenFloat(String[] tokens, int index, float defaultValue) {
        String value = token(tokens, index);
        if (value.isEmpty()) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Log.warn("Failed to parse float token at index " + index + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Safely parse int token with default value.
     */
    protected int tokenInt(String[] tokens, int index, int defaultValue) {
        String value = token(tokens, index);
        if (value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.warn("Failed to parse int token at index " + index + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Validate that tokens array has minimum required length.
     * Throws IllegalArgumentException if validation fails.
     */
    protected void validateTokens(String[] tokens, int minLength) {
        if (tokens == null) {
            throw new IllegalArgumentException("Tokens array is null");
        }
        if (tokens.length < minLength) {
            throw new IllegalArgumentException(
                "Expected at least " + minLength + " tokens, got " + tokens.length
            );
        }
    }
}
