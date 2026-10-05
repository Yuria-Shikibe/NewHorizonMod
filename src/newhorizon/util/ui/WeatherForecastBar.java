package newhorizon.util.ui;

import arc.graphics.Color;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.math.Mathf;
import arc.util.Time;
import newhorizon.expand.game.WeatherEventState;

import static mindustry.Vars.state;

/** A rolling fifteen-minute timeline for automatic storm events. */
public class WeatherForecastBar extends Element {
    private static final Color background = Color.valueOf("252932");
    private static final Color track = Color.valueOf("151820");
    private static final Color tickColor = Color.white.cpy().a(0.13f);
    private static final Color borderColor = Color.white.cpy().a(0.22f);
    private static final Color currentColor = Color.white.cpy().a(0.92f);

    @Override
    public void draw() {
        super.draw();

        float now = (float) state.tick;
        float horizon = 15f * Time.toMinutes;
        float barHeight = Math.max(1f, height - 10f);
        float barY = y + (height - barHeight) / 2f;
        float trackY = barY + 3f;
        float trackHeight = Math.max(1f, barHeight - 6f);

        Draw.color(background, parentAlpha * color.a);
        Fill.rect(x + width / 2f, barY + barHeight / 2f, width, barHeight);

        Draw.color(track, parentAlpha * color.a);
        Fill.rect(x + width / 2f, trackY + trackHeight / 2f, width - 2f, trackHeight);

        Draw.color(borderColor, parentAlpha * color.a);
        Lines.stroke(1f);
        Lines.rect(x + 0.5f, barY + 0.5f, width - 1f, barHeight - 1f);

        Draw.color(tickColor, parentAlpha * color.a);
        for (int i = 1; i < 3; i++) {
            float tickX = x + width * i / 3f;
            Fill.rect(tickX, trackY + trackHeight / 2f, 1f, trackHeight);
        }

        WeatherEventState.Forecast active = WeatherEventState.activeForecast();
        if (active != null) drawSegment(active, now, horizon, trackY, trackHeight);
        for (WeatherEventState.Forecast event : WeatherEventState.forecast()) {
            drawSegment(event, now, horizon, trackY, trackHeight);
        }

        Draw.color(currentColor, parentAlpha * color.a);
        Fill.rect(x + 1.5f, barY + barHeight / 2f, 2f, barHeight + 4f);
        Draw.reset();
    }

    private void drawSegment(WeatherEventState.Forecast event, float now, float horizon, float barY, float barHeight) {
        float start = event.startTick - now;
        float end = start + event.duration;
        if (end <= 0f || start >= horizon) return;

        float left = Math.max(0f, start / horizon);
        float right = Math.min(1f, end / horizon);
        if (right <= left) return;

        float segmentX = x + width * (left + right) / 2f;
        float segmentWidth = Math.max(2f, width * (right - left));
        Color segmentColor = WeatherEventState.weatherColor(event.storm);
        boolean current = event.startTick <= now && end > now;

        if (current) {
            Draw.blend(Blending.additive);
            Draw.color(segmentColor, parentAlpha * color.a * (0.24f + Mathf.absin(Time.time, 4f, 0.12f)));
            Fill.rect(segmentX, barY + barHeight / 2f, segmentWidth + 4f, barHeight + 4f);
            Draw.blend();
        }

        Draw.color(Color.black, parentAlpha * color.a * 0.72f);
        Fill.rect(segmentX, barY + barHeight / 2f, segmentWidth + 1f, barHeight);
        Draw.color(segmentColor, parentAlpha * color.a);
        Fill.rect(segmentX, barY + barHeight / 2f, segmentWidth, Math.max(1f, barHeight - 2f));
        Draw.color(Color.white, parentAlpha * color.a * (current ? 0.65f : 0.22f));
        Fill.rect(segmentX, barY + barHeight - 1.5f, segmentWidth, 1f);
    }
}
