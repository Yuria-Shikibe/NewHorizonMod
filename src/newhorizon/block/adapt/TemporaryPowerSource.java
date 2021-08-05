package newhorizon.block.adapt;

import arc.graphics.g2d.Draw;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.sandbox.PowerSource;
import newhorizon.func.DrawFuncs;

public class TemporaryPowerSource extends PowerSource{
	public TemporaryPowerSource(String name){
		super(name);
		
		saveData = true;
		rebuildable = false;
	}
	
	@Override
	public boolean isHidden(){
		return !Vars.state.rules.editor;
	}
	
	public class TemporaryPowerSourceBuild extends PowerSourceBuild{
		public float delay = 300;
		
		@Override
		public void draw(){
			super.draw();
			
			
			if(isHidden())return;
			
			Draw.z(Layer.overlayUI);
			DrawFuncs.overlayText(delay / 60 + "s", x, y + size * Vars.tilesize / 2f, 0, Pal.accent, true);
		}
		
		@Override
		public void update(){
			super.update();
			
			delay -= Time.delta;
			if(delay < 0)kill();
		}
		
		@Override
		public void buildConfiguration(Table table){
			table.slider(5, 10, 0.5f, 0, f -> delay = f * 60f).growX().row();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.f(delay);
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			delay = read.f();
		}
	}
}
