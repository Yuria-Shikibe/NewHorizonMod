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

public class NHShaders{
	public static Shader gravityTrapShader;
	
	public static ModSurfaceShader quantum;
	
	public static void init(){
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
			public String textureName(){
				return "noise";
			}
			
			@Override
			public void loadNoise(){
//				Texture texture = Core.atlas.find(NewHorizon.name("fog")).texture;
//				texture.setFilter(Texture.TextureFilter.linear);
//				texture.setWrap(Texture.TextureWrap.repeat);
//				noiseTex = texture;
				Core.assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
					t.setFilter(Texture.TextureFilter.linear);
					t.setWrap(Texture.TextureWrap.repeat);
				};
			}
		};
	}
	
	public static class ModSurfaceShader extends ModShader{
		protected Texture noiseTex;
		
		public ModSurfaceShader(String frag){
			super("screenspace", frag);
			loadNoise();
		}
		
		public ModSurfaceShader(String vertRaw, String fragRaw){
			super(vertRaw, fragRaw);
			loadNoise();
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
				if(noiseTex == null){
					noiseTex = Core.assets.get("sprites/" + textureName() + ".png", Texture.class);
				}
				
				noiseTex.bind(1);
				renderer.effectBuffer.getTexture().bind(0);
				
				setUniformi("u_noise", 1);
			}
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
