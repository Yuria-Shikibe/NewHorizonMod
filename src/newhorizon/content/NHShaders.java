package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import mindustry.graphics.Shaders;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;

import static mindustry.Vars.renderer;

public class NHShaders {
    public static ModShader gravityTrap, quantum, statusXWave;
    public static ModSurfaceShader displaceGlitch;

    public static void init() {
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
            }
        };

        statusXWave = new ModShader("VFX_obliqueWave"){
            @Override
            public void apply() {
                setUniformf("u_texsize", Core.camera.width * renderer.getDisplayScale(), Core.camera.height * renderer.getDisplayScale());
                setUniformf("u_invsize", 1f / Core.camera.width * renderer.getDisplayScale(), 1f / Core.camera.height * renderer.getDisplayScale());
                setUniformf("u_time", Time.time);
            }
        };

        displaceGlitch = new ModSurfaceShader("VFX_displaceGlitch") {
            @Override
            public void apply() {
                setUniformf("u_texsize", Core.graphics.getWidth(), Core.graphics.getHeight());
                setUniformf("u_time", Time.time / Scl.scl(1f));
                setUniformf("u_intensity", intensity);

                if (hasUniform("u_noise")) {
                    if (noiseTex1 == null) noiseTex1 = getTexture() == null ? Core.assets.get("sprites/" + textureName() + ".png", Texture.class) : getTexture();

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
    }

    public static Fi getShaderFi(String file) {
        Mods.LoadedMod mod = NewHorizon.MOD;
        Fi shaders = mod.root.child("shaders");
        if (shaders.exists() && shaders.child(file).exists()) return shaders.child(file);
        return Shaders.getShaderFi(file);
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
}
