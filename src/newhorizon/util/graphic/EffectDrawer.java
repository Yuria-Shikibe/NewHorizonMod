package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.math.Interp;
import arc.math.Mat;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec3;
import arc.struct.Bits;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Disposable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.type.StatusEffect;
import newhorizon.NHSetting;
import newhorizon.content.NHColor;
import newhorizon.content.NHShaders;
import newhorizon.content.NHStatusEffects;
import newhorizon.util.annotation.HeadlessDisabled;

import static newhorizon.NHRenderer.height;
import static newhorizon.NHRenderer.width;

@HeadlessDisabled
public class EffectDrawer implements Disposable{
	public static final float FADE_TIME = 90f;
	
	protected static final Mat tmpMat = new Mat();
	
	public static EffectDrawer drawer = new EffectDrawer();
	public final EffectRenderer none = new EffectRenderer();
	
	public FrameBuffer effectBuffer = new FrameBuffer();
	public Seq<StatusEffect> registered = new Seq<>();
	public Seq<EffectRenderer> drawers;
	public IntSeq zOrder;
	
	protected LastSeq lastSeq;
	protected Bits lastStatus = new Bits();
	protected IntMap<Runnable> drawTask = new IntMap<>();
	
	public void init(){
		lastSeq = new LastSeq();
		drawers = new Seq<>(Vars.content.statusEffects().size);
		zOrder = new IntSeq(Vars.content.statusEffects().size);
		
		for(int i = 0; i < Vars.content.statusEffects().size; i++){
			drawers.add(none);
			zOrder.add(-1);
			register(Vars.content.statusEffects().get(i), i, none);
		}
		
		register(NHStatusEffects.scannerDown, Integer.MAX_VALUE, new EffectRenderer(NHShaders.scannerDown){
			{
				cannotSkip = true;
			}
			private final Color from = Pal.heal, to = NHColor.lightSkyBack;
			
			@Override
			public void drawEffect(StatusEffect effect, Unit unit, float f){
				float particleLen = width / 8f;
				float stroke = height / 35f;
				float life = width / 10f;
				Lines.stroke(stroke * f);
				
				float base = (Time.time / life);
				rand.setSeed(seed);
				
				Draw.blend(Blending.additive);
				
				Draw.color(from, Color.white, to, Mathf.absin(4f, 1f));
				Draw.alpha((0.7f + Mathf.absin(2f, 0.2f)) * Mathf.curve(f, 0, 0.5f));
				Fill.quad(0, 0, width, 0, width, height, 0, height);
				
				Draw.blend();
				
				for(int i = -40; i < 40; i++){
					for(int j = -15; j < 15; j++){
						float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
						int angle = Mathf.sign(rand.range(1));
						float len = width / 35f * Interp.pow2Out.apply(fin);
						Draw.alpha((rand.random(0.925f, 1f) + Mathf.absin(2f, 0.05f)) * Mathf.curve(f, 0, 0.5f));
						Draw.tint(
							Tmp.c1.set(from).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.4f, 0.8f) * Mathf.curve(fout, 0.8f, 1f)),
							Tmp.c2.set(to).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.4f, 0.8f) * Mathf.curve(fout, 0.8f, 1f)),
							rand.random(0f, 1f)
						);
						
						Draw.getColor().lerp(Color.white, Mathf.absin(2f, 0.075f));
						
						Lines.lineAngle(width / 2 + rand.range(0.5f) * particleLen + j / 10f * width * fin, height / 2 + rand.range(0.5f) * stroke + i * height / 60, angle * 90 + 90, particleLen * (fout * 0.4f + 0.6f) * f);
					}
				}
			}
		});
		
		register(NHStatusEffects.ultFireBurn, 20, new EffectRenderer(){
			private final Color from = NHColor.lightSkyFront, to = NHColor.deeperBlue;
			
			@Override
			public void drawEffect(StatusEffect effect, Unit unit, float f){
				float rad = width / 22f;
				float life = 180f;
				int num = (int)(width * height / 5000);
				
				float base = (Time.time / life);
				rand.setSeed(seed);
				
				Draw.blend(Blending.additive);
				
				Draw.color(from, Color.white, to, Mathf.absin(4f, 1f));
				Draw.alpha((0.2f + Mathf.absin(1f, 0.15f)) * Mathf.curve(f, 0, 0.5f));
				Fill.quad(0, 0, width, 0, width, height, 0, height);
				
				for(int i = 0; i < num; i++){
					float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
					Draw.alpha((rand.random(0.8f, 0.875f) + Mathf.absin(2f, 0.15f)) * (Mathf.curve(fout, 0.35f, 0.55f) + 3) / 4 * f);
					Draw.tint(
							Tmp.c1.set(from).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.4f, 0.8f) * Mathf.curve(fout, 0.8f, 1f)),
							Tmp.c2.set(to).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.4f, 0.8f) * Mathf.curve(fout, 0.8f, 1f)),
							rand.random(0f, 1f)
					);
					
					Tmp.v1.setToRandomDirection(rand).scl(width / 2.075f, height / 2.05f).scl((rand.random(0.95f, 1.15f)) * Interp.fastSlow.apply(fout) + 0.165f + rand.random(0.05f, 0.125f));
					Fill.square(Tmp.v1.x + width / 2, Tmp.v1.y + height / 2, rad * rand.random(0.8f, 1.8f) * fout * fout * f, 45);
				}
				
				Draw.blend();
			}
		});
		
		register(NHStatusEffects.phased, 25, new EffectRenderer(){
			private final Seq<Vec3> lastCoord = new Seq<>();
			private final Color from = NHColor.lightSkyFront, to = NHColor.deeperBlue;
			
			@Override
			public void drawEffect(StatusEffect effect, Unit unit, float f){
				float rad = width / 1.75f;
				float stroke = width / 27f;
				float life = 125f;
				int itr = 4;
				
				rand.setSeed(seed);
				
				Draw.blend(Blending.additive);
				
				if(lastCoord.isEmpty())for(int i = 0; i < itr; i++){
					lastCoord.add(new Vec3(Core.camera.position.x, Core.camera.position.y, life * (i / (float)itr)));
				}
				
				for(int i = 0; i < itr; i++){
					Vec3 vec3 = lastCoord.get(i);
					
					if(!Vars.state.isPaused()){
						vec3.z -= Time.delta;
						if(vec3.z < -2){
							vec3.set(Core.camera.position.x, Core.camera.position.y, life);
						}
					}
					
					Tmp.v1.set(vec3.x, vec3.y).sub(Core.camera.position).scl(Vars.renderer.getDisplayScale() / 3f);
					float fin = vec3.z / life, fout = 1f - fin;
					
					Draw.alpha(Mathf.curve(fin, 0, 0.55f) * f);
					Lines.stroke(stroke * fout * f);
					Draw.tint(from, to, fout);
					
					Lines.square(width / 2 + Tmp.v1.x, height / 2 + Tmp.v1.y, rad * fout + 0.22f, 45);
				}
				
				Draw.blend();
			}
		});
	}
	
	public void register(StatusEffect effect, int order, EffectRenderer e){
		registered.add(effect);
		drawers.set(effect.id, e);
		zOrder.set(effect.id, order);
	}
	
	public void sort(){
		registered.sortComparing(this::getZ);
	}
	
	public int getZ(StatusEffect statusEffect){
		return -zOrder.get(statusEffect.id);
	}
	
	public EffectRenderer getRenderer(StatusEffect statusEffect){
		return drawers.get(statusEffect.id);
	}
	
	public void draw(){
		Draw.draw(Layer.fogOfWar + 2, () -> {
			effectBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
			
			for(Runnable r : drawTask.values()){
				r.run();
			}
		});
	}
	
	public void update(){
		if(Vars.player.unit() == null || Vars.player.unit().statusBits() == null)return;
		
		registered.each(effect -> {
			if(Vars.player.unit().statusBits().get(effect.id)){
				EffectRenderer d = getRenderer(effect);
				if(d.cannotSkip || NHSetting.enableDetails()){
					lastSeq.add(effect.id);
					if(!drawTask.containsKey(effect.id))drawTask.put(effect.id, () -> d.drawAll(effect, Vars.player.unit(), Mathf.clamp(lastSeq.get(effect.id) / FADE_TIME)));
				}
			}
		});
		
		lastSeq.update();
	}
	
	public void clear(){
		drawTask.clear();
		lastSeq.clear();
	}
	
	/** Releases all resources of this object. */
	@Override
	public void dispose(){
		effectBuffer.dispose();
	}
	
	public class EffectRenderer{
		protected long lastSeed;
		
		public boolean cannotSkip = false;
		public Color initColor = Color.clear;
		public Shader shader = Shaders.screenspace;
		public Rand rand = new Rand();
		public long seed = lastSeed++;
		
		public EffectRenderer(){
		}
		
		public EffectRenderer(Shader shader){
			this.shader = shader;
		}
		
		public void beforeDraw(StatusEffect effect, Unit unit, float f){
			effectBuffer.begin(initColor);
			
			tmpMat.set(Draw.proj());
			Draw.proj().setOrtho(0, 0, width, height);
		}
		
		public void drawEffect(StatusEffect effect, Unit unit, float f){}
		
		public void drawAll(StatusEffect effect, Unit unit, float f){
			beforeDraw(effect, unit, f);
			drawEffect(effect, unit, f);
			afterDraw(effect, unit, f);
		}
		
		public void afterDraw(StatusEffect effect, Unit unit, float f){
			Draw.flush();
			Draw.proj(tmpMat);
			
			effectBuffer.end();
			effectBuffer.blit(shader);
		}
	}
	
	protected class LastSeq{
		public LastEffectTime[] arr;
		
		public LastSeq(){
			this.arr = new LastEffectTime[Vars.content.statusEffects().size];
		}
		
		public boolean contains(int id){
			return arr[id] != null;
		}
		
		public float get(int id){
			LastEffectTime l = arr[id];
			return l == null ? 0 : l.time;
		}
		
		public void clear(){
			for(int i = 0; i < arr.length; i++){
				LastEffectTime l = arr[i];
				if(l != null){
					Pools.free(l);
					arr[i] = null;
				}
			}
		}
		
		public void update(){
			for(int i = 0; i < arr.length; i++){
				LastEffectTime l = arr[i];
				if(l != null){
					if(l.time < -5 && !l.newly){
						arr[i] = null;
						Pools.free(l);
						drawTask.remove(i);
						continue;
					}
					l.newly = false;
					l.time -= Time.delta;
				}
			}
		}
		
		public void add(int id){
			LastEffectTime l = arr[id];
			if(l != null)l.refill();
			else arr[id] = Pools.obtain(LastEffectTime.class, () -> new LastEffectTime(8f));
		}
	}
	
	protected static class LastEffectTime implements Pool.Poolable{
		public float time;
		public boolean newly = true;
		
		public LastEffectTime(float time){
			this.time = time;
		}
		
		public LastEffectTime(){
			this(FADE_TIME);
		}
		
		public void refill(){
			time = Math.min(FADE_TIME + 15f, time + Time.delta * 5f);
		}
		
		@Override
		public void reset(){
			newly = true;
			time = 0;
		}
	}
}
