package newhorizon.expand.logic.cutscene.letterbox;

import arc.Core;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.core.UI;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.ui.Styles;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

import static mindustry.Vars.mobile;
import static mindustry.Vars.state;

public class LetterboxText extends ActionLStatement {
    public static final String[] ALIGN_NAMES = {
            "topLeft", "top", "topRight",
            "left", "center", "right",
            "bottomLeft", "bottom", "bottomRight"
    };
    public static final int[] ALIGN_VALUES = {
            Align.topLeft, Align.top, Align.topRight,
            Align.left, Align.center, Align.right,
            Align.bottomLeft, Align.bottom, Align.bottomRight
    };

    public String duration = "2";
    public String align = "top";
    public String text = "nh.letterbox.example";

    public LetterboxText(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
        align = ParseUtil.getNextToken(token);
        if (ParseUtil.tokenIndex + 1 < token.length) {
            text = decodeLogicToken(ParseUtil.getNextToken(token));
        }
    }

    public LetterboxText() {
    }

    public static int parseAlign(String name) {
        if (name == null || name.isEmpty()) return Align.top;
        for (int i = 0; i < ALIGN_NAMES.length; i++) {
            if (ALIGN_NAMES[i].equalsIgnoreCase(name)) return ALIGN_VALUES[i];
        }
        return Align.top;
    }

    public static String encodeLogicToken(String value) {
        if (value == null || value.isEmpty()) return "-";
        return value
                .replace("\\", "\\\\")
                .replace("@", "[at]")
                .replace(" ", "[s]")
                .replace("\n", "[n]");
    }

    public static String decodeLogicToken(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) return "";
        String v = value;
        if (v.startsWith("<") && v.endsWith(">") && v.length() >= 2) {
            v = v.substring(1, v.length() - 1);
        }
        return v.replace("[n]", "\n").replace("[s]", " ").replace("[at]", "@");
    }

    public static String wrapActionText(String value) {
        String cleaned = value == null ? "" : value.replace(">", "");
        return "<" + cleaned.replace("\n", "[n]") + ">";
    }

    public static boolean hasLocalizedKey(String key) {
        if (key == null || key.isEmpty()) return false;
        if (mobile && hasExactKey(key + ".mobile")) return true;
        return hasExactKey(key);
    }

    public static boolean hasExactKey(String key) {
        if (key == null || key.isEmpty()) return false;
        if (state != null && state.mapLocales != null && state.mapLocales.containsProperty(key)) return true;
        return Core.bundle != null && Core.bundle.has(key);
    }

    public static String lookupLocalized(String key) {
        if (key == null || key.isEmpty()) return "";

        if (mobile) {
            String mobileKey = key + ".mobile";
            String mobileValue = lookupExact(mobileKey);
            if (mobileValue != null) return mobileValue;
        }

        String value = lookupExact(key);
        if (value != null) return value;

        if (state != null && state.mapLocales != null) {
            return state.mapLocales.getProperty(key);
        }
        return Core.bundle.get(key, "???" + key + "???");
    }

    private static String lookupExact(String key) {
        if (state != null && state.mapLocales != null && state.mapLocales.containsProperty(key)) {
            return state.mapLocales.getProperty(key);
        }
        if (Core.bundle != null && Core.bundle.has(key)) {
            return Core.bundle.get(key);
        }
        return null;
    }

    public static String resolveLocalized(String raw) {
        String value = UI.formatIcons(raw == null ? "" : raw);
        if (value.isEmpty()) return "";

        boolean forcedKey = value.startsWith("@");
        String key = forcedKey ? value.substring(1) : value;

        if (forcedKey) {
            return lookupLocalized(key);
        }

        if (hasLocalizedKey(key)) {
            return lookupLocalized(key);
        }

        return value;
    }

    private static String shortLabel(String name) {
        return switch (name) {
            case "topLeft" -> "TL";
            case "top" -> "T";
            case "topRight" -> "TR";
            case "left" -> "L";
            case "center" -> "C";
            case "right" -> "R";
            case "bottomLeft" -> "BL";
            case "bottom" -> "B";
            case "bottomRight" -> "BR";
            default -> name;
        };
    }

    @Override
    public String getLStatementName() {
        return "letterboxtext";
    }

    @Override
    public void build(Table table) {
        buildRowTable(table, t -> {
            t.add(" Duration: ");
            fields(t, duration, str -> duration = str);
        });

        buildRowTable(table, t -> {
            t.add(" Align: ").top().padTop(6f);
            t.table(grid -> {
                ButtonGroup<TextButton> group = new ButtonGroup<>();
                group.setMinCheckCount(1);
                group.setMaxCheckCount(1);
                for (int i = 0; i < ALIGN_NAMES.length; i++) {
                    String name = ALIGN_NAMES[i];
                    TextButton button = grid.button(shortLabel(name), Styles.togglet, () -> align = name)
                            .size(64f, 36f).pad(2f).group(group).get();
                    button.setChecked(name.equals(align));
                    if ((i + 1) % 3 == 0) grid.row();
                }
            });
        });

        buildRowTable(table, t -> {
            t.add(" Text/@key: ");
            fields(t, text, str -> text = str).width(0).growX().padLeft(3);
        });
    }

    @Override
    public LCategory category() {
        return NHLogic.actionCurtainControl;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration, align, encodeLogicToken(text));
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new LetterboxTextI(builder.var(duration));
    }

    public class LetterboxTextI extends ActionInstruction {
        public LVar duration;

        public LetterboxTextI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "letterbox_text");
            appendExec(exec, duration);
            exec.textBuffer.append(" ").append(align);
            exec.textBuffer.append(" ").append(wrapActionText(text));
            endExec(exec);
        }
    }
}