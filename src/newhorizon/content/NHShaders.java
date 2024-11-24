package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.math.Mat;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import mindustry.graphics.Shaders;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;

import static mindustry.Vars.renderer;

public class NHShaders{
	public static MatterStormShader matterStorm;
	public static HyperspaceShader hyperspace;
	public static Tiler tiler;
	
	public static Shader gravityTrapShader, scannerDown;

	public static ShadowShader shadowShader;
	public static ModSurfaceShader quantum;
	public static StatusEffectShader statusAlpha, statusXWave;

	//public static DistortShader distort;

	public static void init(){
		statusAlpha = new StatusEffectShader("screenspace", "overphased");
		statusXWave = new StatusEffectShader("screenspace", "statusXWave");

		//distort = new DistortShader();

		tiler = new Tiler();

		scannerDown = new ModShader("screenspace", "scannerDown");
		
		hyperspace = new HyperspaceShader();
		
		shadowShader = new ShadowShader();
		
		matterStorm = new MatterStormShader("storm");
		
		gravityTrapShader = new ModShader("screenspace", "gravityTrap"){
			@Override
			public void apply(){
				setUniformf("u_dp", Scl.scl(1f));
				setUniformf("u_time", Time.time / Scl.scl(1f));
				setUniformf("u_offset",
						Core.camera.position.x - Core.camera.width / 2,
						Core.camera.position.y - Core.camera.height / 2);
				setUniformf("u_texsize", Core.camera.width, Core.camera.height);
				setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
			}
		};
		
		quantum = new ModSurfaceShader("quantum"){
			@Override
			public String textureName() {
                return super.textureName();
            }
			
			@Override
			public void loadNoise(){
				super.loadNoise();
				
				noiseTex2 = NHContent.darkerNoise;
				noiseTex1 = NHContent.smoothNoise;
			}

			@Override
			public Texture getTexture(){
				return NHContent.smoothNoise;
			}
		};

		//platingSurface = new PlatingSurfaceShader();
	}
	
	public static class Tiler extends ModShader{
		public Texture texture = Core.atlas.white().texture;
		public float scl = 4F;
		
		public Tiler(){
			super("screenspace", "tiler");
		}
		
		@Override
		public void apply(){
			setUniformf("u_offset",
					Core.camera.position.x - Core.camera.width / 2,
					Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);
			setUniformf("u_tiletexsize", (float)texture.width / scl, (float)texture.height / scl);
			
			texture.bind(1);
			renderer.effectBuffer.getTexture().bind(0);
			
			setUniformi("u_tiletex", 1);
		}
	}
	
	public static class OutlineShader extends ModShader{
		public OutlineShader(){
			super("screenspace", "outliner");
		}
		
		public Color color = Color.lightGray;
		
		@Override
		public void apply(){
			setUniformf("u_offset",
					Core.camera.position.x - Core.camera.width / 2,
					Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_dp", Scl.scl(1f));
			setUniformf("u_time", Time.time / Scl.scl(1f));
			setUniformf("u_color", color);
			setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);
		}
	}
	
	public static class HyperspaceShader extends ModShader{
		public Color color = Color.white;
		public float progress = 0;
		public float rotation = 0;
		
		public HyperspaceShader(){
			super("screenspace", "hyperspace");
		}
		
		@Override
		public void apply(){
			setUniformf("u_dp", Scl.scl(1f));
			setUniformf("u_time", Time.time / Scl.scl(1f));
			setUniformf("u_offset",
					Core.camera.position.x - Core.camera.width / 2,
					Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);
			setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
			setUniformf("u_color", color);
		}
	}
	
	public static class ShadowShader extends ModShader{
		public Color color = Color.white;
		
		public ShadowShader set(Color color){
			this.color = color;
			
			return this;
		}
		
		public ShadowShader(){
			super("screenspace", "shadow");
		}
		
		@Override
		public void apply(){
			setUniformf("u_alpha", color);
		}
	}
	
	public static class MatterStormShader extends ModSurfaceShader{
		public Vec2 direction = new Vec2();
		public Color primaryColor = new Color(), secondaryColor = new Color();
		public Mat rotator = new Mat(), scaler = new Mat();
		
		public MatterStormShader(String frag){
			super(frag);
		}
		
		public void applyDirection(Vec2 vec2, float scl){
			direction.set(vec2).scl(scl);
			rotator.setToRotation(vec2.angle());
			scaler.setToScaling(direction);
		}
		
		@Override
		public void apply(){
			super.apply();
			
			setUniformf("u_direction", direction.x, direction.y);
			setUniformf("u_color_sec", secondaryColor);
			setUniformf("u_color_pri", primaryColor);
			setUniformMatrix("u_rotator", rotator);
			setUniformMatrix("u_scaler", scaler);
		}
		
		@Override
		public String textureName() {
            return super.textureName();
        }
		
		@Override
		public void loadNoise(){
			super.loadNoise();
			noiseTex2 = NHContent.darkerNoise;
			noiseTex1 = NHContent.smoothNoise;
		}
		
		@Override
		public Texture getTexture(){
			return NHContent.smoothNoise;
		}
	}
	
	public static class ModSurfaceShader extends ModShader{
		protected Texture noiseTex1, noiseTex2;
		
		public ModSurfaceShader(String frag){
			super("screenspace", frag);
			loadNoise();
		}
		
		public ModSurfaceShader(String vertRaw, String fragRaw){
			super(vertRaw, fragRaw);
			loadNoise();
		}
		
		public Texture getTexture(){
			return null;
		}
		
		public String textureName(){
			return "noise";
		}
		
		public void loadNoise(){
			Core.assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
				t.setFilter(Texture.TextureFilter.linear);
				t.setWrap(Texture.TextureWrap.repeat);
			};
		}
		
		@Override
		public void apply(){
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);
			
			if(hasUniform("u_noise")){
				if(noiseTex1 == null){
					noiseTex1 = getTexture() == null ? Core.assets.get("sprites/" + textureName() + ".png", Texture.class) : getTexture();
				}
				
				noiseTex1.bind(1);
				renderer.effectBuffer.getTexture().bind(0);
				
				setUniformi("u_noise", 1);
			}
			
			if(hasUniform("u_noise_2")){
				if(noiseTex2 == null){
					noiseTex2 = Core.assets.get("sprites/" + "noise" + ".png", Texture.class);
				}
				
				noiseTex2.bind(1);
				renderer.effectBuffer.getTexture().bind(0);
				
				setUniformi("u_noise_2", 1);
			}
		}
	}

	public static class DistortShader extends ModShader{
		protected Texture noiseTex1;

		public DistortShader(){
			super("screenspace", "distort");
			loadNoise();
		}

		public Texture getTexture(){
			return null;
		}

		public String textureName(){
			return "noise";
		}

		public void loadNoise(){
			Core.assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
				t.setFilter(Texture.TextureFilter.linear);
				t.setWrap(Texture.TextureWrap.repeat);
			};
		}

		@Override
		public void apply(){
			setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_resolution", Core.camera.width, Core.camera.height);
			setUniformf("u_time", Time.time);
			//setUniformf("u_distort", 0.05f, 0,05f);

			//float[] centers = {
			//	0.5f, 0.5f,
			//	0.3f, 0.7f,
			//	0.8f, 0.2f,
			//	0.2f, 0.4f,
			//	0.6f, 0.8f
			//};
			//float[] ranges = {
			//	0.25f,
			//	0.15f,
			//	0.35f,
			//	0.20f,
			//	0.40f
			//};
			//setUniform2fv("u_centers", centers, 0, centers.length);
			//setUniform1fv("u_ranges", ranges, 0, ranges.length);

			if(hasUniform("u_grayMap")){
				if(noiseTex1 == null){
					noiseTex1 = getTexture() == null ? Core.assets.get("sprites/" + textureName() + ".png", Texture.class) : getTexture();
				}

				noiseTex1.bind(1);
				renderer.effectBuffer.getTexture().bind(0);

				setUniformi("u_grayMap", 1);
			}
		}
	}

	public static class StatusEffectShader extends ModShader{

		public StatusEffectShader(String vert, String frag) {
			super(vert, frag);
		}

		@Override
		public void apply(){
			setUniformf("u_texsize", Core.camera.width * renderer.getDisplayScale(), Core.camera.height * renderer.getDisplayScale());
			setUniformf("u_invsize", 1f/Core.camera.width * renderer.getDisplayScale(), 1f/Core.camera.height * renderer.getDisplayScale());
			setUniformf("u_time", Time.time);
		}
	}
	
	public static class ModShader extends Shader{
		public ModShader(String vert, String frag){
			super(getShaderFi(vert + ".vert"), getShaderFi(frag + ".frag"));
		}
	}
	
	public static Fi getShaderFi(String file){
		Mods.LoadedMod mod = NewHorizon.MOD;
		
		Fi shaders = mod.root.child("shaders");
		if(shaders.exists()){
			if(shaders.child(file).exists())return shaders.child(file);
		}
		
		return Shaders.getShaderFi(file);
	}
}
