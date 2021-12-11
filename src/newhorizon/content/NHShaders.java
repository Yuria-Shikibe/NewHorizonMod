package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.graphics.gl.Shader;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import mindustry.graphics.Shaders;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;

public class NHShaders{
	public static GravityTrapShader gravityTrapShader;
	
	public static void init(){
		gravityTrapShader = new GravityTrapShader();
	}
	
	public static class GravityTrapShader extends ModShader{
		public GravityTrapShader(){
			super("screenspace", "gravityTrap");
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
