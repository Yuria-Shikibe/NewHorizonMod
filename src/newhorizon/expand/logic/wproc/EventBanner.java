package newhorizon.expand.logic.wproc;

import arc.Core;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.logic.LVar;
import mindustry.ui.Styles;
import newhorizon.NewHorizon;
import newhorizon.content.NHLogic;
import newhorizon.util.ui.NHUIFunc;

import static mindustry.Vars.headless;
import static mindustry.Vars.state;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class EventBanner extends LStatement {
    public String icon = "\"new-horizon-event-default-raid-t1\"";
    public String text = "\"Notification\"";
    public String duration = "4.5";
    public String color = "@crux";

    public EventBanner(String[] tokens) {
        if (tokens.length > 1) icon = tokens[1];
        if (tokens.length > 2) text = tokens[2];
        if (tokens.length > 3) duration = tokens[3];
        if (tokens.length > 4) color = tokens[4];
    }

    public EventBanner() {
    }

    @Override
    public void build(Table table) {
        table.table(row -> {
            row.add("@nh.event-banner.icon");
            fields(row, icon, value -> icon = value).width(260f);
        }).left().row();
        table.table(row -> {
            row.add("@nh.event-banner.text");
            fields(row, text, value -> text = value).width(260f);
        }).left().row();
        table.table(row -> {
            row.add("@nh.event-banner.duration");
            fields(row, duration, value -> duration = value).width(90f);
            row.add("@nh.event-banner.color");
            fields(row, color, value -> color = value).width(140f);
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
        builder.append("eventbanner ").append(icon).append(' ').append(text).append(' ')
                .append(duration).append(' ').append(color);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        String colorToken = isHexColor(color) ? "\"" + color + "\"" : color;
        return new EventBannerInstruction(builder.var(icon), builder.var(text), builder.var(duration), builder.var(colorToken));
    }

    private static boolean isHexColor(String value) {
        String hex = value.startsWith("#") ? value.substring(1) : value;
        return hex.matches("[0-9a-fA-F]{6}");
    }

    public static class EventBannerInstruction implements LExecutor.LInstruction {
        public final LVar icon, text, duration, color;

        public EventBannerInstruction(LVar icon, LVar text, LVar duration, LVar color) {
            this.icon = icon;
            this.text = text;
            this.duration = duration;
            this.color = color;
        }

        @Override
        public void run(LExecutor exec) {
            if (headless || state.isMenu()) return;

            float seconds = duration.numf();
            if (!Float.isFinite(seconds) || seconds <= 0f) return;

            TextureRegion region = resolveIcon(icon.obj() == null ? icon.name : icon.obj());
            Color bannerColor = resolveColor(color);
            String label = "<< " + resolveText(text) + " >>";

            NHUIFunc.showLabel(Math.max(seconds, 1f), table -> {
                table.background(Styles.black5);
                table.table(row -> {
                    row.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(bannerColor);
                    row.image(region).scaling(Scaling.fit).size(192f).color(bannerColor);
                    row.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(bannerColor);
                }).growX().pad(OFFSET / 2).fillY().row();
                table.table(row -> row.add(new FLabel(label)).color(bannerColor).padBottom(4))
                        .growX().fillY();
            });
        }

        private static String resolveText(LVar value) {
            String message = value.obj() == null ? String.valueOf(value.num()) : String.valueOf(value.obj());
            if (message.startsWith("@")) {
                message = Core.bundle.get(message.substring(1), message);
            }
            return message.replace("[n]", "\n");
        }

        private static Color resolveColor(LVar value) {
            Object object = value.obj();
            if (object instanceof Team team) return team.color;
            if (object instanceof Color color) return color;
            if (object instanceof String string && isHexColor(string)) {
                return Color.valueOf(string.startsWith("#") ? string.substring(1) : string);
            }
            return Team.crux.color;
        }

        private static TextureRegion resolveIcon(Object value) {
            if (value instanceof UnlockableContent content && content.uiIcon != null) return content.uiIcon;
            if (value instanceof TextureRegion region) return region;
            if (value instanceof TextureRegionDrawable drawable) return drawable.getRegion();
            if (value instanceof String string) {
                String name = string.startsWith("@") ? string.substring(1) : string;
                TextureRegionDrawable drawable = Icon.icons.get(name);
                if (drawable != null) return drawable.getRegion();
                TextureRegion region = Core.atlas.find(name);
                if (region.found()) return region;
                region = Core.atlas.find(NewHorizon.name(name));
                if (region.found()) return region;
            }
            return Icon.warning.getRegion();
        }
    }
}
