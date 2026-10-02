package newhorizon.expand.logic.wproc;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.core.World;
import mindustry.game.Team;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.logic.LVar;
import newhorizon.NHUI;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.components.ui.MarkStyle;

import java.util.Locale;

import static mindustry.Vars.headless;

/**
 * World-processor HUD marker node.
 *
 * Syntax: hudsetting x y radius lifetime(seconds) color style visable
 *
 * Style values are: 0 rotating box + crosshair, 1 rotating box only,
 * 2 fixed box + crosshair, 3 shaking signal box, 4 raid/icon ring.
 */
public class HUDsetting extends LStatement {
    public String x = "0";
    public String y = "0";
    public String radius = "24";
    public String lifetime = "60";
    public String color = "@accent";
    public String style = "0";
    public String visable = "1";

    public HUDsetting(String[] tokens) {
        if (tokens.length > 1) x = tokens[1];
        if (tokens.length > 2) y = tokens[2];
        if (tokens.length > 3) radius = tokens[3];
        if (tokens.length > 4) lifetime = tokens[4];
        if (tokens.length > 5) color = tokens[5];
        if (tokens.length > 6) style = tokens[6];
        if (tokens.length > 7) visable = tokens[7];
    }

    public HUDsetting() {
    }

    @Override
    public void build(Table table) {
        table.table(row -> {
            row.add(" X: ");
            fields(row, x, value -> x = value).width(90f);
            row.add(" Y: ");
            fields(row, y, value -> y = value).width(90f);
        }).left().row();
        table.table(row -> {
            row.add(" Radius: ");
            fields(row, radius, value -> radius = value).width(90f);
            row.add(" Lifetime(s): ");
            fields(row, lifetime, value -> lifetime = value).width(90f);
        }).left().row();
        table.table(row -> {
            row.add(" Color: ");
            fields(row, color, value -> color = value).width(110f);
            row.add(" Style: ");
            fields(row, style, value -> style = value).width(60f);
        }).left().row();
        table.table(row -> {
            row.add(" Visable: ");
            fields(row, visable, value -> visable = value).width(90f);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHLogic.nhwproc;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("hudsetting ")
                .append(x).append(' ')
                .append(y).append(' ')
                .append(radius).append(' ')
                .append(lifetime).append(' ')
                .append(color).append(' ')
                .append(style).append(' ')
                .append(visable);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new HUDsettingInstruction(
                builder.var(x), builder.var(y), builder.var(radius), builder.var(lifetime),
                builder.var(color), builder.var(style), builder.var(visable), color);
    }

    public static class HUDsettingInstruction implements LExecutor.LInstruction {
        public final LVar x, y, radius, lifetime, color, style, visable;
        private final String colorToken;
        private HudMarker marker;
        private final Color decodedColor = new Color();
        private boolean lastVisable = true;

        public HUDsettingInstruction(LVar x, LVar y, LVar radius, LVar lifetime,
                                     LVar color, LVar style, LVar visable, String colorToken) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.lifetime = lifetime;
            this.color = color;
            this.style = style;
            this.visable = visable;
            this.colorToken = colorToken;
        }

        @Override
        public void run(LExecutor exec) {
            if (headless) {
                removeMarker();
                return;
            }

            float life = Math.max(lifetime.numf(), 0.01f) * Time.toSeconds;
            boolean eventVisible = visable.numf() > 0f;
            if (marker == null || marker.isRemoving() || marker.completed()) {
                removeMarker();
                marker = new HudMarker()
                        .setMarkPosition(World.unconv(x.numf()), World.unconv(y.numf()))
                        .setRadius(Math.max(radius.numf(), 0f))
                        .setDuration(life)
                        .setStyle(resolveStyle(style.numf()))
                        .setMarkColor(resolveColor(color).cpy())
                        .setEventVisibility(eventVisible);
                marker.addMarker();
                lastVisable = eventVisible;
                NHUI.rebuildEventList();
                return;
            }

            // Keep a live marker aligned with processor values without resetting
            // its lifetime every tick.
            marker.setMarkPosition(World.unconv(x.numf()), World.unconv(y.numf()));
            marker.radius = Math.max(radius.numf(), 0f);
            marker.markColor.set(resolveColor(color));
            marker.style = resolveStyle(style.numf());
            if (eventVisible != lastVisable) {
                lastVisable = eventVisible;
                marker.setEventVisibility(eventVisible);
                NHUI.rebuildEventList();
            }
        }

        private void removeMarker() {
            if (marker != null) marker.removeMarkerNow();
            marker = null;
        }

        private static MarkStyle resolveStyle(float value) {
            return switch (Math.max(0, Math.min(4, Math.round(value)))) {
                case 1 -> MarkStyle.defaultNoLines;
                case 2 -> MarkStyle.defaultFixed;
                case 3 -> MarkStyle.signalShake;
                case 4 -> MarkStyle.iconRaid;
                default -> MarkStyle.defaultStyle;
            };
        }

        private Color resolveColor(LVar value) {
            Color tokenColor = resolveTokenColor(colorToken);
            if (tokenColor == null) tokenColor = resolveTokenColor(value.name);
            if (tokenColor != null) return tokenColor;

            Object object = value.obj();
            if (object instanceof Color c) return c;
            if (object instanceof Team team) return team.color;
            if (object instanceof String text) {
                Color named = arc.graphics.Colors.get(text);
                if (named != null) return named;
            }
            if (object instanceof Number number) {
                return resolveNumericColor(number.doubleValue());
            }
            if (!value.isobj) return resolveNumericColor(value.num());
            return mindustry.graphics.Pal.accent;
        }

        private static Color resolveTokenColor(String token) {
            if (token == null || token.isEmpty()) return null;

            String name = token;
            if (name.charAt(0) == '@') name = name.substring(1);

            if ((token.startsWith("#") || token.startsWith("%"))
                    && (token.length() == 7 || token.length() == 9)) {
                try {
                    return Color.valueOf(token.substring(1));
                } catch (RuntimeException ignored) {
                    return null;
                }
            }

            Color named = arc.graphics.Colors.get(name.toUpperCase(Locale.ROOT));
            if (named != null) return named;

            return switch (name.toLowerCase()) {
                case "white" -> Color.white;
                case "black" -> Color.black;
                case "blue" -> Color.blue;
                case "sky" -> Color.sky;
                case "cyan" -> Color.cyan;
                case "green" -> Color.green;
                case "yellow" -> Color.yellow;
                case "orange" -> Color.orange;
                case "red" -> Color.red;
                case "pink" -> Color.pink;
                case "purple" -> Color.purple;
                case "accent" -> mindustry.graphics.Pal.accent;
                case "accentback" -> mindustry.graphics.Pal.accentBack;
                default -> null;
            };
        }

        private Color resolveNumericColor(double number) {
            // Logic color constants and packcolor values are stored as raw
            // RGBA bits inside a double, not as Color objects.
            if (Double.isFinite(number) && Math.abs(number) < 1e-100) {
                return decodedColor.fromDouble(number);
            }

            return switch ((int) number) {
                case 1 -> Color.red;
                case 2 -> Color.green;
                case 3 -> Color.cyan;
                case 4 -> Color.yellow;
                case 5 -> Color.white;
                default -> mindustry.graphics.Pal.accent;
            };
        }
    }
}
