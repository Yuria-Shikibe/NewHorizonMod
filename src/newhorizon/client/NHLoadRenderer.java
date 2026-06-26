package newhorizon.client;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.graphics.LoadRenderer;
import mindustry.mod.Mods;

import java.lang.reflect.Field;

public class NHLoadRenderer extends LoadRenderer{
    private static final Color bgColor = Color.valueOf("2c3037");
    private static final Color barColor = Color.valueOf("ffd37f").lerp(Color.black, 0.5f);

    private float smoothProgress;
    private TextureRegion logoRegion;

    public static NHLoadRenderer create(){
        // sun.misc.Unsafe — desktop JVM / Android ART
        try{
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafe = f.get(null);
            return (NHLoadRenderer)unsafeClass.getMethod("allocateInstance", Class.class).invoke(unsafe, NHLoadRenderer.class);
        }catch(Throwable ignored){}

        // dalvik.system.VMRuntime — older Android fallback
        try{
            Class<?> vmRuntime = Class.forName("dalvik.system.VMRuntime");
            Object runtime = vmRuntime.getMethod("getRuntime").invoke(null);
            return (NHLoadRenderer)vmRuntime.getMethod("newInstance", Class.class).invoke(runtime, NHLoadRenderer.class);
        }catch(Throwable ignored){}

        throw new RuntimeException("No supported runtime allocation method for this platform.");
    }

    private NHLoadRenderer(){}

    @Override
    public void draw(){
        float progress = Core.assets.getProgress();
        smoothProgress = Mathf.lerpDelta(smoothProgress, progress, 0.15f);

        float w = Core.graphics.getWidth();
        float h = Core.graphics.getHeight();
        float s = Math.min(w, h) / 900f;

        Core.graphics.clear(bgColor);
        Draw.proj().setOrtho(0, 0, w, h);

        // grid
        float space = 60 * s;
        Draw.color(barColor, 0.06f);
        Lines.stroke(1f);
        int cols = (int)(w / space) + 1;
        int rows = (int)(h / space) + 1;
        for(int i = 0; i <= cols; i++) Lines.line(i * space, 0, i * space, h);
        for(int i = 0; i <= rows; i++) Lines.line(0, i * space, w, i * space);

        // progress bar at bottom 5%
        float barWidth = w * 0.5f;
        float barHeight = 14 * s;
        float cx = w / 2f;
        float cy = h * 0.05f + barHeight / 2f;
        float x = cx - barWidth / 2f;
        float y = cy - barHeight / 2f;

        Draw.color(Color.black);
        Fill.rect(cx, cy, barWidth, barHeight);

        float fillWidth = barWidth * smoothProgress;
        if(fillWidth > 0){
            Draw.color(barColor);
            Fill.rect(x + fillWidth / 2f, cy, fillWidth, barHeight);
        }

        Draw.color(barColor, 0.5f);
        Lines.stroke(1.5f * s);
        Lines.rect(x, y, barWidth, barHeight);

        // mod logo above progress bar
        drawModLogo(cx, cy + barHeight + 20 * s);

        Draw.flush();
    }

    private void drawModLogo(float cx, float bottomY){
        if(logoRegion == null){
            try{
                Mods.LoadedMod mod = Vars.mods.getMod("new-horizon");
                if(mod != null){
                    Fi logoFile = mod.root.child("sprites-override/ui/logo.png");
                    if(!logoFile.exists()) logoFile = mod.root.child("icon.png");
                    if(logoFile.exists()){
                        Texture tex = new Texture(logoFile);
                        logoRegion = new TextureRegion(tex, 0, 0, tex.width, tex.height);
                    }
                }
            }catch(Throwable ignored){}
        }

        if(logoRegion == null) return;

        Draw.color(Color.white);
        Draw.rect(logoRegion, cx, bottomY + logoRegion.height / 2f, logoRegion.width, logoRegion.height);
    }

    @Override
    public void dispose(){}
}
