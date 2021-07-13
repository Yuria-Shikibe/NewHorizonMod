package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.graphics.gl.Shader;
import arc.util.Log;
import mindustry.Vars;
import mindustry.mod.Mods;
import newhorizon.NewHorizon;

public class NHShaders{
	public static GravityTrapShader gravityTrapShader;
	
	public static void init(){
		gravityTrapShader = new GravityTrapShader();
	}
	
	public static class GravityTrapShader extends ModShader{
		public GravityTrapShader(){
			super("gravityTrap", "screenspace");
		}
		
		@Override
		public void apply(){
			setUniformf("u_offset",
					Core.camera.position.x - Core.camera.width / 2,
					Core.camera.position.y - Core.camera.height / 2);
			setUniformf("u_texsize", Core.camera.width, Core.camera.height);
			setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
		}
	}
	
	public static class ModShader extends Shader{
		public ModShader(String frag, String vert){
			super(getShaderFi(vert + ".vert"), getShaderFi(frag + ".frag"));
		}
	}
	
	public static Fi getShaderFi(String file){
		Mods.LoadedMod mod = Vars.mods.getMod(NewHorizon.class);
		
		if(mod.root.child("shader").exists()){
			Fi shaders = mod.root.child("shader");
			Log.info(shaders.findAll());
			if(shaders.child(file).exists())return shaders.child(file);
		}
		
		return Core.files.internal("shaders/" + file);
	}
}
