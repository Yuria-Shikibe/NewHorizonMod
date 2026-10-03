package newhorizon.expand.game;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.flabel.FLabel;
import arc.math.Rand;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.WeatherState;
import mindustry.graphics.Pal;
import mindustry.net.NetConnection;
import mindustry.type.Weather;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import newhorizon.NHUI;
import newhorizon.content.NHWeathers;
import newhorizon.expand.net.packet.WeatherEventAlertPacket;
import newhorizon.expand.net.packet.WeatherEventSyncPacket;
import newhorizon.expand.net.packet.WeatherEventSyncRequestPacket;
import newhorizon.expand.weather.MatterStorm;
import newhorizon.util.ui.NHUIFunc;
import newhorizon.util.ui.WeatherForecastBar;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.headless;
import static mindustry.Vars.net;
import static mindustry.Vars.state;
import static newhorizon.util.ui.TableFunc.OFFSET;

/** Automatic five-minute storm scheduling with an optional world-processor override. */
public final class WeatherEventState {
    public static final float INTERVAL = 5f * Time.toMinutes;
    public static final float SCHEDULE_HORIZON = 60f * Time.toMinutes;
    public static final float MIN_DURATION = 15f * Time.toSeconds;
    public static final float MAX_DURATION = 45f * Time.toSeconds;

    private static final int FORECAST_SIZE = 16;
    private static final int FIRST_STORM_SLOT = 1;
    private static final int STATE_VERSION = 3;
    private static final long UNSET_SEED = Long.MIN_VALUE;
    private static final Rand random = new Rand();
    private static final Seq<Forecast> forecast = new Seq<>();
    private static boolean systemEnabled;
    private static boolean processorEnabled;
    private static boolean clientEnabled;
    private static boolean restored;
    private static boolean lastDisplayed;
    private static Forecast active;
    private static long scheduleSeed = UNSET_SEED;
    private static int generatedThroughSlot = FIRST_STORM_SLOT - 1;

    private WeatherEventState() {
    }

    public static void load() {
        Events.on(EventType.WorldLoadBeginEvent.class, event -> reset());
        Events.run(EventType.Trigger.update, WeatherEventState::tick);
        Events.on(EventType.WorldLoadEvent.class, event -> {
            if (RaidLogic.isRemoteClient() && net.active()) {
                net.send(new WeatherEventSyncRequestPacket(), true);
            }
            if (RaidLogic.isLogicSide()) tick();
            if (!headless) NHUI.rebuildEventList();
        });
        Events.on(EventType.PlayerConnect.class, event -> {
            if (net.server() && net.active() && event.player != null) {
                pushStateTo(event.player);
            }
        });
    }

    public static void reset() {
        systemEnabled = true;
        processorEnabled = false;
        clientEnabled = false;
        restored = false;
        forecast.clear();
        active = null;
        lastDisplayed = false;
        scheduleSeed = UNSET_SEED;
        generatedThroughSlot = FIRST_STORM_SLOT - 1;
    }

    /** Applies the world-processor override and immediately updates the scheduler. */
    public static void update(boolean enabled) {
        systemEnabled = enabled;
        tick();
    }

    /** Advances automatic weather scheduling once per game tick. */
    private static void tick() {
        if (!RaidLogic.isLogicSide() || !state.isGame()) return;

        if (!SpecialEventState.enabled()) {
            if (processorEnabled || !forecast.isEmpty() || active != null) {
                processorEnabled = false;
                forecast.clear();
                active = null;
                restored = false;
                NHWeathers.quantumStorm.remove();
                NHWeathers.solarStorm.remove();
                syncState();
                refreshUi();
            }
            return;
        }

        if (active != null) {
            float remaining = (float) (active.startTick + active.duration - state.tick);
            if (remaining <= 0f) {
                active = null;
                refreshUi();
            } else if (!weather(active.storm).isActive()) {
                startStorm(active.storm, remaining, false);
            }
        }

        if (SpecialEventState.defaultEventsExempt()) {
            if (processorEnabled || !forecast.isEmpty()) {
                processorEnabled = false;
                forecast.clear();
                restored = false;
                syncState();
                refreshUi();
            }
            return;
        }

        if (!systemEnabled) {
            if (processorEnabled || !forecast.isEmpty()) {
                processorEnabled = false;
                forecast.clear();
                restored = false;
                syncState();
                refreshUi();
            }
            return;
        }

        if (!processorEnabled) {
            processorEnabled = true;
            ensureForecast();
            syncState();
            refreshUi();
        }

        ensureForecast();
        boolean changed = false;
        while (forecast.any() && state.tick >= forecast.first().startTick) {
            Forecast event = forecast.remove(0);
            trigger(event);
            changed = true;
        }
        ensureForecast();
        if (changed) {
            syncState();
            refreshUi();
        }
    }

    public static boolean active() {
        return RaidLogic.isRemoteClient()
                ? clientEnabled
                : SpecialEventState.enabled() && !SpecialEventState.defaultEventsExempt() && (processorEnabled || active != null);
    }

    public static Seq<Forecast> forecast() {
        return forecast;
    }

    public static Forecast activeForecast() {
        return active;
    }

    private static void ensureForecast() {
        long seed = mapSeed();
        if (scheduleSeed != seed) {
            scheduleSeed = seed;
            if (!restored || forecast.isEmpty()) {
                generatedThroughSlot = Math.max(FIRST_STORM_SLOT - 1, (int) Math.floor(state.tick / INTERVAL));
                forecast.clear();
            } else {
                generatedThroughSlot = FIRST_STORM_SLOT - 1;
                for (Forecast event : forecast) {
                    generatedThroughSlot = Math.max(generatedThroughSlot,
                            (int) Math.floor(event.startTick / INTERVAL));
                }
            }
        }

        int targetSlot = (int) Math.floor((state.tick + SCHEDULE_HORIZON) / INTERVAL);
        int firstSlot = Math.max(FIRST_STORM_SLOT, generatedThroughSlot + 1);
        for (int slot = firstSlot; slot <= targetSlot; slot++) {
            forecast.add(createForecast(slot));
            generatedThroughSlot = slot;
        }

        forecast.sort((left, right) -> Float.compare(left.startTick, right.startTick));
        restored = false;
    }

    private static Forecast createForecast(int slot) {
        random.setSeed(scheduleSeed ^ (0x9E3779B97F4A7C15L * slot));
        int storm = random.random(0, 1);
        float duration = random.random(MIN_DURATION, MAX_DURATION);
        float maxOffset = Math.max(0f, INTERVAL - duration);
        float offset = random.random(0f, maxOffset);
        return new Forecast(slot * INTERVAL + offset, storm, duration);
    }

    private static long mapSeed() {
        long seed = 0x4E485745415448L;
        if (state.map != null) {
            seed = seed * 31L + state.map.name().hashCode();
            seed = seed * 31L + state.map.width;
            seed = seed * 31L + state.map.height;
            if (state.map.file != null) seed = seed * 31L + state.map.file.path().hashCode();
        }
        if (state.rules != null) {
            for (var entry : state.rules.tags.entries()) {
                seed = seed * 31L + entry.key.hashCode();
                seed = seed * 31L + (entry.value == null ? 0 : entry.value.hashCode());
            }
        }
        seed = seed * 31L + (state.rules == null ? 0 : state.rules.mode().ordinal());
        return seed;
    }

    private static void trigger(Forecast event) {
        Weather weather = weather(event.storm);
        if (weather == null || !SpecialEventState.enabled()) return;

        if (startStorm(event.storm, event.duration, true)) active = event;
    }

    public static void triggerManual(int storm, float durationSeconds) {
        if (!RaidLogic.isLogicSide() || !SpecialEventState.enabled() || !Float.isFinite(durationSeconds)) return;
        if (storm < 0) storm = random.random(0, 1);
        storm = Math.max(0, Math.min(1, storm));
        float duration = Math.max(1f, durationSeconds) * Time.toSeconds;
        if (startStorm(storm, duration, true)) {
            active = new Forecast((float) state.tick, storm, duration);
            syncState();
            refreshUi();
        }
    }

    private static boolean startStorm(int storm, float duration, boolean announce) {
        Weather weather = weather(storm);
        if (weather == null || (net.client() && !net.server())) return false;

        Tmp.v1.setToRandomDirection();
        if (net.server() && net.active()) {
            WeatherState weatherState = weather.create(1f, duration);
            weatherState.windVector.set(Tmp.v1);
            Call.createWeather(weather, 1f, duration, Tmp.v1.x, Tmp.v1.y);
        } else if (!net.active()) {
            WeatherState weatherState = weather.create(1f, duration);
            weatherState.windVector.set(Tmp.v1);
        } else {
            return false;
        }
        if (announce) showStormAlert(storm);
        if (announce && net.server() && net.active()) {
            net.send(new WeatherEventAlertPacket(storm), true);
        }
        return weather.isActive();
    }

    public static Weather weather(int storm) {
        return storm == 0 ? NHWeathers.quantumStorm : NHWeathers.solarStorm;
    }

    public static Color weatherColor(int storm) {
        Weather weather = weather(storm);
        if (weather instanceof MatterStorm matterStorm) return matterStorm.primaryColor;
        return Pal.accent;
    }

    public static String weatherName(int storm) {
        Weather weather = weather(storm);
        return weather == null ? Core.bundle.get("mod.ui.weather-forecast-unknown") : weather.localizedName;
    }

    public static void showStormAlert(int storm) {
        if (headless || weather(storm) == null) return;

        Weather weather = weather(storm);
        Color color = weatherColor(storm);
        TextureRegion icon = weather.uiIcon;
        String text = "<< " + Core.bundle.format("mod.ui.weather-event-alert", weatherName(storm)) + " >>";
        NHUIFunc.showLabel(2.5f, table -> {
            table.background(Styles.black5);
            table.table(row -> {
                row.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padRight(-9).color(color);
                row.image(new TextureRegionDrawable(icon)).size(192f).color(color);
                row.image().growX().height(OFFSET / 2).pad(OFFSET / 3).padLeft(-9).color(color);
            }).growX().pad(OFFSET / 2).fillY().row();
            table.table(row -> row.add(new FLabel(text)).color(color).padBottom(4)).growX().fillY();
        });
    }

    public static Table getForecastTable() {
        return new Table(Tex.buttonEdge4, table -> {
            table.margin(5f, 7f, 6f, 7f);
            table.defaults().growX();
            table.table(header -> {
                header.image(new TextureRegionDrawable(NHWeathers.quantumStorm.uiIcon))
                        .size(24f).scaling(arc.util.Scaling.fit).color(Pal.accent).padRight(6f);
                header.add("@mod.ui.weather-forecast-short").color(Color.white).left().growX();
                header.add("@mod.ui.weather-forecast-window").color(Color.lightGray).right();
            }).height(26f).growX().row();
            table.add(new WeatherForecastBar()).height(44f).growX().pad(2f, 1f, 0f, 1f).row();
            table.table(axis -> {
                axis.add("@mod.ui.weather-forecast-now").color(Color.white).left().growX();
                axis.add("@mod.ui.weather-forecast-5m-short").color(Color.gray).center().growX();
                axis.add("@mod.ui.weather-forecast-10m-short").color(Color.gray).center().growX();
                axis.add("@mod.ui.weather-forecast-15m-short").color(Color.gray).right().growX();
            }).padTop(2f).height(16f).growX().row();
            table.table(legend -> {
                legend.image(new TextureRegionDrawable(NHWeathers.quantumStorm.uiIcon))
                        .size(18f).scaling(arc.util.Scaling.fit).color(weatherColor(0)).padRight(4f);
                legend.add("@mod.ui.weather-quantum-short").color(weatherColor(0)).left();
                legend.add().growX();
                legend.image(new TextureRegionDrawable(NHWeathers.solarStorm.uiIcon))
                        .size(18f).scaling(arc.util.Scaling.fit).color(weatherColor(1)).padRight(4f);
                legend.add("@mod.ui.weather-solar-short").color(weatherColor(1)).right();
            }).height(22f).growX().row();
        });
    }

    private static void refreshUi() {
        boolean displayed = active();
        if (displayed != lastDisplayed || displayed) {
            if (!headless && NHUI.eventList != null) NHUI.rebuildEventList();
        }
        lastDisplayed = displayed;
    }

    public static void writeState(DataOutput out) throws IOException {
        out.writeByte(STATE_VERSION);
        out.writeBoolean(systemEnabled);
        int count = processorEnabled ? Math.min(forecast.size, FORECAST_SIZE) : 0;
        out.writeByte(count);
        for (int i = 0; i < count; i++) {
            Forecast event = forecast.get(i);
            out.writeFloat(event.startTick);
            out.writeByte(event.storm);
            out.writeFloat(event.duration);
        }
        boolean saveActive = active != null && state.tick < active.startTick + active.duration;
        out.writeBoolean(saveActive);
        if (saveActive) {
            out.writeFloat(active.startTick);
            out.writeByte(active.storm);
            out.writeFloat(active.duration);
        }
    }

    public static void readState(DataInput in) throws IOException {
        int version = in.readByte();
        if (version < 1) return;

        boolean savedEnabled = in.readBoolean();
        int count = in.readUnsignedByte();
        if (count > FORECAST_SIZE) throw new IOException("Invalid New Horizon weather forecast count: " + count);

        forecast.clear();
        for (int i = 0; i < count; i++) {
            float startTick = in.readFloat();
            int storm = in.readByte();
            float duration = in.readFloat();
            if (Float.isFinite(startTick) && Float.isFinite(duration) && (storm == 0 || storm == 1)) {
                forecast.add(new Forecast(startTick, storm, duration));
            }
        }
        active = null;
        if (version >= 2 && in.readBoolean()) {
            float startTick = in.readFloat();
            int storm = in.readByte();
            float duration = in.readFloat();
            if (Float.isFinite(startTick) && Float.isFinite(duration) && (storm == 0 || storm == 1)) {
                active = new Forecast(startTick, storm, duration);
            }
        }
        systemEnabled = savedEnabled;
        restored = savedEnabled && forecast.any();
        processorEnabled = false;
    }

    public static void writeSync(arc.util.io.Writes write) {
        write.bool(SpecialEventState.enabled() && !SpecialEventState.defaultEventsExempt()
                && (processorEnabled || active != null));
        int count = Math.min(forecast.size, FORECAST_SIZE);
        write.b((byte) count);
        for (int i = 0; i < count; i++) {
            Forecast event = forecast.get(i);
            write.f(event.startTick);
            write.b((byte) event.storm);
            write.f(event.duration);
        }
        boolean hasActive = active != null && state.tick < active.startTick + active.duration;
        write.bool(hasActive);
        if (hasActive) {
            write.f(active.startTick);
            write.b((byte) active.storm);
            write.f(active.duration);
        }
    }

    public static void applySync(arc.util.io.Reads read) {
        clientEnabled = read.bool();
        int count = read.b();
        if (count < 0 || count > FORECAST_SIZE) return;
        forecast.clear();
        for (int i = 0; i < count; i++) {
            float startTick = read.f();
            int storm = read.b();
            float duration = read.f();
            if (Float.isFinite(startTick) && Float.isFinite(duration) && (storm == 0 || storm == 1)) {
                forecast.add(new Forecast(startTick, storm, duration));
            }
        }
        active = null;
        if (read.bool()) {
            float startTick = read.f();
            int storm = read.b();
            float duration = read.f();
            if (Float.isFinite(startTick) && Float.isFinite(duration) && (storm == 0 || storm == 1)) {
                active = new Forecast(startTick, storm, duration);
            }
        }
        refreshUi();
    }

    public static void pushStateTo(Player player) {
        if (!net.server() || !net.active() || player == null) return;
        NetConnection connection = player.con();
        if (connection != null) connection.send(new WeatherEventSyncPacket(), true);
    }

    private static void syncState() {
        if (!net.server() || !net.active()) return;
        for (Player player : Groups.player) {
            if (!player.isLocal()) pushStateTo(player);
        }
    }

    public static final class Forecast {
        public final float startTick;
        public final int storm;
        public final float duration;

        public Forecast(float startTick, int storm, float duration) {
            this.startTick = startTick;
            this.storm = storm;
            this.duration = duration;
        }
    }
}
