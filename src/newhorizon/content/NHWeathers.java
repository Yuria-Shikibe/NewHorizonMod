package newhorizon.content;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.WeatherState;
import mindustry.type.Weather;
import mindustry.world.meta.Attribute;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.renderer;

public class NHWeathers {
    public static Weather
            quantumField, quantumStorm, solarStorm;

    private static final Rand particleRand = new Rand();

    private static final float boundMax = 10000 * 8f;
    private static final float particleDensity = 2200f;
    private static final float particleSpeed = 0.35f;
    private static final float particleLife = 480f;
    private static final float fadeIn = 0.2f;
    private static final float fadeOut = 0.25f;
    private static final float sizeMin = 2.8f;
    private static final float sizeMax = 7.5f;

    public static void load() {
        quantumField = new Weather("quantum-weather") {
            {
                duration = 10f * Time.toMinutes;
                statusAir = statusGround = true;
                status = NHStatusEffects.quantization;
                statusDuration = 60f;
                attrs.set(Attribute.light, -0.3f);
                opacityMultiplier = 0.47f;
            }

            @Override
            public void drawOver(WeatherState state) {
                Draw.blend(Blending.additive);
                drawQuantumHexParticles(state.intensity(), state.opacity());
                Draw.blend();
            }
        };

        /*
        quantumStorm = new MatterStorm("quantum-storm") {{
            status = NHStatusEffects.ultFireBurn;
            statusDuration = 15f;
            rotateBullets = true;

            buildingEmp = 0.4f;

            textureColor = primaryColor = NHColor.darkEnrColor;
            secondaryColor = NHColor.lightSkyBack;
            bulletSpawnChance *= 1.5f;
        }};

        solarStorm = new MatterStorm("solar-storm") {
            {
                status = NHStatusEffects.emp2;
                statusDuration = 60f;

                buildingEmp = 0.125f;
                force = 4;
                noise = Sounds.fire;

                primaryColor = Pal.accent;
                textureColor = secondaryColor = Pal.ammo;

                attrs.set(Attribute.heat, 2f);
            }
        };
         */
    }

    static void drawQuantumHexParticles(float intensity, float opacity) {
        if (Core.camera == null || intensity <= 0f || opacity <= 0f) return;

        float invScale = 1f / renderer.minScale();
        float worldSizeMax = sizeMax * invScale;

        Rand rand = particleRand;
        rand.setSeed(0);

        Tmp.r1.setCentered(
                Core.camera.position.x, Core.camera.position.y,
                Core.graphics.getWidth() / renderer.minScale(),
                Core.graphics.getHeight() / renderer.minScale()
        );
        Tmp.r1.grow(worldSizeMax * 1.5f);
        Core.camera.bounds(Tmp.r2);

        int count = (int) (Tmp.r1.area() / particleDensity * intensity);

        for (int i = 0; i < count; i++) {
            float scl = rand.random(0.5f, 1f);
            float scl2 = rand.random(0.5f, 1f);
            float size = rand.random(sizeMin, sizeMax) * invScale;
            float phase = rand.random(1f);

            float dirX = (rand.chance(0.5f) ? 1f : -1f) + rand.range(0.28f);
            float dirY = (rand.chance(0.5f) ? 1f : -1f) + rand.range(0.28f);
            float len = Mathf.len(dirX, dirY);
            float windX = dirX / len * particleSpeed * scl2;
            float windY = dirY / len * particleSpeed * scl;

            float x = rand.random(0f, boundMax) + Time.time * windX;
            float y = rand.random(0f, boundMax) + Time.time * windY;

            float alphaBase = rand.random(0.15f, 0.35f);
            float pulsePeriod = rand.random(1.6f, 3.4f);
            float pulseAngle = rand.random(360f);

            x -= Tmp.r1.x;
            y -= Tmp.r1.y;
            x = Mathf.mod(x, Tmp.r1.width);
            y = Mathf.mod(y, Tmp.r1.height);
            x += Tmp.r1.x;
            y += Tmp.r1.y;

            if (!Tmp.r3.setCentered(x, y, size).overlaps(Tmp.r2)) continue;

            float fin = (phase + Time.time / particleLife) % 1f;
            float fout = 1f - fin;
            float lifeAlpha = Mathf.curve(fin, 0f, fadeIn) * Mathf.curve(fout, 0f, fadeOut);
            float pulse = MathUtil.timeValue(0.16f, 0.78f, pulsePeriod, pulseAngle);
            float alpha = pulse * alphaBase * lifeAlpha * intensity * opacity;

            Draw.color(NHColor.darkEnrFront);
            Draw.alpha(alpha);

            if ((i & 1) == 0) {
                Lines.stroke(Math.max(0.45f, size * 0.17f));
                Lines.poly(x, y, 6, size, 0f);
            } else {
                Fill.poly(x, y, 6, size * 0.72f, 0f);
            }
        }

        Draw.reset();
    }
}
