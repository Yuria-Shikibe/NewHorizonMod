package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.graphics.g2d.Draw;
import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.graphics.Texture;
import arc.graphics.Pixmap;
import arc.graphics.gl.FrameBuffer;
import mindustry.graphics.CacheLayer;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import arc.util.Tmp;
import arc.util.Time;
import mindustry.graphics.Shaders;
import mindustry.mod.Mods;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;

import static mindustry.Vars.renderer;

public class NHShaders {
    public static ModShader gravityTrap, quantum, statusXWave, hexShield;
    public static ModSurfaceShader luminousQuantum;
    public static ModSurfaceShader displaceGlitch;
    public static GalaxyNebulaShader galaxyNebula;
    public static VortexHitShader vortexHit;

    public static float alphaInner, alphaOuter;

    public static void init() {
        alphaInner = NHSetting.getFloat(NHSetting.GRAPHIC_GRAVITY_TRAP_ALPHA, 0.20f);
        alphaOuter = NHSetting.getFloat(NHSetting.GRAPHIC_GRAVITY_TRAP_OUTLINE_ALPHA, 0.60f);

        gravityTrap = new ModShader("VFX_gravityTrap") {
            @Override
            public void apply() {
                setUniformf("u_scale", Scl.scl(1f));
                setUniformf("u_time", Time.time / Scl.scl(1f));
                setUniformf("u_offset",
                        Core.camera.position.x - Core.camera.width / 2,
                        Core.camera.position.y - Core.camera.height / 2);
                setUniformf("u_texsize", Core.camera.width, Core.camera.height);
                setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);

                setUniformf("u_alphaInner", alphaInner);
                setUniformf("u_alphaOuter", alphaOuter);
            }
        };

        statusXWave = new ModShader("VFX_obliqueWave") {
            @Override
            public void apply() {
                setUniformf("u_texsize", Core.camera.width * renderer.getDisplayScale(), Core.camera.height * renderer.getDisplayScale());
                setUniformf("u_invsize", 1f / Core.camera.width * renderer.getDisplayScale(), 1f / Core.camera.height * renderer.getDisplayScale());
                setUniformf("u_time", Time.time);
            }
        };

        hexShield = new ModShader("VFX_hexShield") {
            @Override
            public void apply() {
                setUniformf("u_scale", Scl.scl(1f));
                setUniformf("u_time", Time.time / Scl.scl(1f));
                setUniformf("u_offset",
                        Core.camera.position.x - Core.camera.width / 2,
                        Core.camera.position.y - Core.camera.height / 2);
                setUniformf("u_texsize", Core.camera.width, Core.camera.height);
                setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
            }
        };

        vortexHit = new VortexHitShader();

        displaceGlitch = new ModSurfaceShader("VFX_displaceGlitch") {
            @Override
            public void apply() {
                setUniformf("u_texsize", Core.graphics.getWidth(), Core.graphics.getHeight());
                setUniformf("u_time", Time.time / Scl.scl(1f));
                setUniformf("u_intensity", intensity);

                if (hasUniform("u_noise")) {
                    if (noiseTex1 == null)
                        noiseTex1 = getTexture() == null ? Core.assets.get("sprites/" + textureName() + ".png", Texture.class) : getTexture();

                    noiseTex1.bind(1);
                    texture.bind(0);

                    setUniformi("u_noise", 1);
                }
            }

            @Override
            public String textureName() {
                return super.textureName();
            }

            @Override
            public void loadNoise() {
                super.loadNoise();

                noiseTex1 = NHContent.noise;
            }

            @Override
            public Texture getTexture() {
                return NHContent.noise;
            }
        };

        quantum = new ModSurfaceShader("noise_quantum") {
            @Override
            public String textureName() {
                return super.textureName();
            }

            @Override
            public void loadNoise() {
                super.loadNoise();

                noiseTex2 = NHContent.darkerNoise;
                noiseTex1 = NHContent.smoothNoise;
            }

            @Override
            public Texture getTexture() {
                return NHContent.smoothNoise;
            }
        };

        galaxyNebula = new GalaxyNebulaShader();

        luminousQuantum = new LuminousQuantumShader();
    }

    public static class LuminousQuantumShader extends ModSurfaceShader {
        public LuminousQuantumShader() {
            super("noise_quantum_luminous");
        }

        public Texture floorTexture;
        public Texture blurTexture;
        public int frame;
        public final Vec2 mouseWorld = new Vec2();
        public final Vec2 previousMouseWorld = new Vec2();
        public Texture mouseHistoryTexture;

        @Override
        public void loadNoise() {
            noiseTex1 = NHContent.smoothNoise;
        }

        @Override
        public Texture getTexture() {
            return NHContent.smoothNoise;
        }

        @Override
        public void apply() {
            setUniformf("u_mouse", mouseWorld.x, mouseWorld.y, previousMouseWorld.x, previousMouseWorld.y);
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_time", Time.time / 1000f);
            setUniformi("u_frame", frame);

            Texture floor = floorTexture == null ? renderer.effectBuffer.getTexture() : floorTexture;
            floor.bind(5);
            floor.bind(4);
            noiseTex1.bind(3);
            mouseHistoryTexture.bind(2);
            blurTexture.bind(1);

            setUniformi("u_mouseTex", 2);
            setUniformi("u_texture", 0);
            setUniformi("u_blur", 1);
            setUniformi("u_noise", 3);
            setUniformi("u_floor", 4);
            setUniformi("u_floorTex", 4);
            setUniformi("u_floorTex", 5);
        }
    }

    public static Fi getShaderFi(String file) {
        Mods.LoadedMod mod = NewHorizon.MOD;
        Fi shaders = mod.root.child("shaders");
        if (shaders.exists() && shaders.child(file).exists()) return shaders.child(file);
        return Shaders.getShaderFi(file);
    }

    public static class LuminousQuantumCacheLayer extends CacheLayer {
        private final FrameBuffer simulationA = new FrameBuffer();
        private final FrameBuffer simulationB = new FrameBuffer();
        private final FrameBuffer blurred = new FrameBuffer();
        private final FrameBuffer blurTemp = new FrameBuffer();
        private final LuminousQuantumShader mainShader = new LuminousQuantumShader();
        private final SimpleSurfaceShader blurHorizontal = new SimpleSurfaceShader("quantum_luminous_blur_h");
        private final SimpleSurfaceShader blurVertical = new SimpleSurfaceShader("quantum_luminous_blur_v");
        private final LuminousCompositeShader compositeShader = new LuminousCompositeShader();
        private final FrameBuffer mouseHistory = new FrameBuffer();
        private boolean usingFirstSimulation;
        private int clearedWidth;
        private int clearedHeight;

        @Override
        public void begin() {
            if (!renderer.animateWater) return;

            int width = Math.max(2, Core.graphics.getWidth());
            int height = Math.max(2, Core.graphics.getHeight());
            simulationA.resize(width, height);
            simulationB.resize(width, height);
            blurred.resize(width, height);
            blurTemp.resize(width, height);
            mouseHistory.resize(width, height);

            if(clearedWidth != width || clearedHeight != height){
                clear(simulationA);
                clear(simulationB);
                clear(blurred);
                clear(blurTemp);
                clear(mouseHistory);
                clearedWidth = width;
                clearedHeight = height;
            }

            renderer.effectBuffer.begin(Color.clear);
            renderer.blocks.floor.beginDraw();
        }

        @Override
        public void end() {
            if (!renderer.animateWater) return;

            renderer.effectBuffer.end();

            FrameBuffer previous = usingFirstSimulation ? simulationA : simulationB;
            FrameBuffer next = usingFirstSimulation ? simulationB : simulationA;

            blit(previous, blurHorizontal, blurTemp);
            blit(blurTemp, blurVertical, blurred);
            mainShader.frame++;
            renderSimulation(previous, blurred, next);
            compositeShader.simulationTexture = next.getTexture();
            compositeShader.floorTexture = renderer.effectBuffer.getTexture();
            renderer.effectBuffer.blit(compositeShader);
            usingFirstSimulation = !usingFirstSimulation;
            renderer.blocks.floor.beginDraw();

        }
        private void renderSimulation(FrameBuffer previous, FrameBuffer blurSource, FrameBuffer target) {
            mainShader.floorTexture = renderer.effectBuffer.getTexture();
            mainShader.blurTexture = blurSource.getTexture();
            mainShader.mouseHistoryTexture = mouseHistory.getTexture();
            Vec2 currentMouse = Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY());
            if(mainShader.frame == 0){
                mainShader.previousMouseWorld.set(currentMouse);
                mainShader.mouseWorld.set(currentMouse);
            }else{
                mainShader.previousMouseWorld.set(mainShader.mouseWorld);
                mainShader.mouseWorld.set(currentMouse);
            }
            mouseHistory.begin();
            mouseStatePixmap.fill(Color.packRgba(
                (int)(currentMouse.x / Core.camera.width * 255f),
                (int)(currentMouse.y / Core.camera.height * 255f),
                (int)(mainShader.mouseWorld.x / Core.camera.width * 255f),
                (int)(mainShader.mouseWorld.y / Core.camera.height * 255f)));
            Texture historyTexture = mouseHistory.getTexture();
            historyTexture.draw(mouseStatePixmap);
            mouseHistory.end();

            target.begin();
            previous.getTexture().bind(0);
            Draw.blit(mainShader);
            target.end();
        }

        private void clear(FrameBuffer buffer) {
            buffer.begin(Color.clear);
            buffer.end();
        }

        private final Pixmap mouseStatePixmap = new Pixmap(1, 1);

        private void blit(FrameBuffer source, Shader shader, FrameBuffer target) {
            target.begin();
            source.blit(shader);
            target.end();
        }
    }

    public static class SimpleSurfaceShader extends ModSurfaceShader {
        public SimpleSurfaceShader(String frag) {
            super(frag);
        }

        @Override
        public void loadNoise() {
        }

        @Override
        public Texture getTexture() {
            return null;
        }

        @Override
        public void apply() {
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_texsize", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniformf("u_time", Time.time);
            setUniformf("u_mouse", Core.input.mouseWorld(Core.input.mouseX(), Core.input.mouseY()));
        }
    }

    public static class LuminousCompositeShader extends SimpleSurfaceShader {
        public Texture simulationTexture;
        public Texture floorTexture;

        public LuminousCompositeShader() {
            super("quantum_luminous_composite");
        }

        @Override
        public void apply() {
            super.apply();

            if(floorTexture != null){
                floorTexture.bind(2);
                setUniformi("u_floorTex", 2);
            }

            if(simulationTexture != null){
                simulationTexture.bind(1);
                setUniformi("u_simulation", 1);
            }
        }
    }

    public static class ModSurfaceShader extends ModShader {
        public float intensity = 0f;

        public Texture texture;
        protected Texture noiseTex1, noiseTex2;

        public ModSurfaceShader(String frag) {
            super("screenspace", frag);
            loadNoise();
        }

        public Texture getTexture() {
            return null;
        }

        public String textureName() {
            return "noise";
        }

        public void loadNoise() {
            Core.assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
                t.setFilter(Texture.TextureFilter.linear);
                t.setWrap(Texture.TextureWrap.repeat);
            };
        }

        @Override
        public void apply() {
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_time", Time.time);

            if (hasUniform("u_noise")) {
                if (noiseTex1 == null) {
                    noiseTex1 = getTexture() == null ? Core.assets.get("sprites/" + textureName() + ".png", Texture.class) : getTexture();
                }

                noiseTex1.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise", 1);
            }

            if (hasUniform("u_noise_2")) {
                if (noiseTex2 == null) {
                    noiseTex2 = Core.assets.get("sprites/" + "noise" + ".png", Texture.class);
                }

                noiseTex2.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise_2", 1);
            }
        }
    }

    public static class ModShader extends Shader {
        public ModShader(String vert, String frag) {
            super(getShaderFi(vert + ".vert"), getShaderFi(frag + ".frag"));
        }

        public ModShader(String frag) {
            super(getShaderFi("screenspace.vert"), getShaderFi(frag + ".frag"));
        }
    }

    public static class VortexHitShader extends ModShader {
        public final float[] hitPositions = new float[48];
        public final float[] hitAngles = new float[48];

        public VortexHitShader() {
            super("VFX_vortexHit");
        }

        @Override
        public void apply() {
            setUniformf("u_offset",
                    Core.camera.position.x - Core.camera.width / 2,
                    Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_texsize", Core.camera.width, Core.camera.height);
            setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
            setUniformf("u_time", Time.time);
            setUniformf("u_scale", Scl.scl(1f));
            setUniform1fv("u_hits[0]", hitPositions, 0, hitPositions.length);
            setUniform1fv("u_hitAngles[0]", hitAngles, 0, hitAngles.length);
            setUniformi("u_hitCount", Math.min(newhorizon.expand.entities.VortexEvent.active.length, 24));
        }
    }

    public static class GalaxyNebulaShader extends ModShader {
        public float seed, alpha, warp, palette;
        public float cameraX, cameraY, cameraZ;

        public GalaxyNebulaShader() {
            super("galaxy_nebula", "galaxy_nebula");
        }

        @Override
        public void apply() {
            setUniformf("u_time", Time.globalTime / 60f);
            setUniformf("u_seed", seed);
            setUniformf("u_alpha", alpha);
            setUniformf("u_warp", warp);
            setUniformf("u_palette", palette);
            setUniformf("u_camera", cameraX, cameraY, cameraZ);
        }
    }
}
