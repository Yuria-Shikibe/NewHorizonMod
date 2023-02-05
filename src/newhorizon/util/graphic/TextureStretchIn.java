package newhorizon.util.graphic;

import arc.Core;
import arc.Events;
import arc.func.Floatp;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.struct.Seq;
import arc.util.Disposable;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import newhorizon.NHModCore;
import newhorizon.content.NHShaders;

public class TextureStretchIn implements Disposable{
	public Seq<StretchData> stretches = new Seq<>();
	public Seq<StretchData> validStretches = new Seq<>();
	public Texture texture = new Texture(0, 0);
	
	public static void stretchAt(float x, float y, float radius, Floatp f){
		NHModCore.core.renderer.textureStretchIn.add(new StretchData(x, y, radius, f));
	}
	
	public float[] asArr(){
		float[] arr = new float[validStretches.size * 4];
		for(int i = 0; i < validStretches.size; i++){
			StretchData data = validStretches.pop();
			arr[i] = data.x;
			arr[i + 1] = data.y;
			arr[i + 2] = data.radius;
			arr[i + 3] = data.fin;
		}
		
		return arr;
	}
	
	public Color[] asColorArr(){
		Color[] arr = new Color[validStretches.size];
		for(int i = 0; i < validStretches.size; i++){
			StretchData data = validStretches.pop();
			arr[i] = new Color(data.x, data.y, data.radius, data.fin);
		}
		
		return arr;
	}
	
	public void generateTexture2DArr(float[] arr){
		texture = new Texture(arr.length / 4, 4);
		texture.setFilter(Texture.TextureFilter.nearest, Texture.TextureFilter.nearest);
//		texture.draw();
	}
	
	public void clear(){
		stretches.clear();
	}
	
	public void add(StretchData data){
		stretches.add(data);
	}
	
	public void add(float x, float y, float radius, Floatp f){
		stretches.add(new StretchData(x, y, radius, f));
	}
	
	public void update(){
		for(int i = 0; i < stretches.size; i++){
			StretchData d = stretches.get(i);
			d.update();
			if(d.fin >= 0.9995f)stretches.remove(i);
		}
	}
	
	public FrameBuffer buffer = new FrameBuffer();
	public float px, py, pre;
	
	public void load(){
		Events.run(EventType.Trigger.preDraw, () -> {
//			validStretches.clear();
			for(StretchData d : stretches){
				if(NHModCore.core.renderer.viewport.overlaps(d.x - d.radius * 0.5f, d.y - d.radius * 0.5f, d.radius, d.radius)){
					validStretches.add(d);
				}
			}
			
			if(validStretches.any()){
				drawStretch();
			}
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			if(validStretches.any()){
				register();
			}
		});
	}
	
	{
		buffer.getTexture().setFilter(Texture.TextureFilter.nearest, Texture.TextureFilter.nearest);
	}
	
	public void drawStretch(){
//		pre = renderer.getScale();
//		float scale = renderer.getScale();
//
//		px = Core.camera.position.x;
//		py = Core.camera.position.y;
//		Core.camera.position.set((int)px + ((int)(camera.width) % 2 == 0 ? 0 : 0.5f), (int)py + ((int)(camera.height) % 2 == 0 ? 0 : 0.5f));
//
//		int w = (int)Core.camera.width, h = (int)Core.camera.height;
//		if(renderer.isCutscene()){
//			w = (int)(Core.camera.width * renderer.landScale() / renderer.getScale());
//			h = (int)(Core.camera.height * renderer.landScale() / renderer.getScale());
//		}
//		w = Mathf.clamp(w, 2, graphics.getWidth());
//		h = Mathf.clamp(h, 2, graphics.getHeight());
//
//		buffer.resize(w, h);
//
		buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
		buffer.begin(Color.clear);
	}
	
	public void register(){
		Draw.draw(Layer.light + 1, () -> {
			
			Blending.disabled.apply();
//			for(StretchData d : validStretches){
//				Draw.shader();
//				NHShaders.stretch.setColor(d);
//				Draw.shader(NHShaders.stretch);
//			}
//			Draw.shader();
			Draw.shader();
//			buffer.blit(NHShaders.stretch);
			
			buffer.end();
			for(StretchData d : validStretches){
				
				NHShaders.stretch.setColor(d);
				buffer.blit(NHShaders.stretch);
			}
//
			Draw.shader();
//			Core.camera.position.set(px, py);
//			renderer.setScale(pre);
		});
	}
	
	@Override
	public void dispose(){
		buffer.dispose();
		texture.dispose();
	}
	
	public static class StretchData{
		public float x, y, radius, fin;
		public Floatp floatp;
		
		public void update(){
			if(floatp != null)fin = floatp.get();
		}
		
		public StretchData(){
		}
		
		public StretchData(float x, float y, float radius, Floatp floatp){
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.floatp = floatp;
		}
		
		public StretchData(float x, float y, float radius, float fin){
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.fin = fin;
		}
	}
}
