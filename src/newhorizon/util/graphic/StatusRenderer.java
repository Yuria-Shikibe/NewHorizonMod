package newhorizon.util.graphic;

import arc.Core;
import arc.func.Cons3;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.Shader;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Shaders;
import mindustry.type.StatusEffect;
import newhorizon.content.NHColor;
import newhorizon.content.NHShaders;
import newhorizon.content.NHStatusEffects;
import newhorizon.util.MathUtil;
import newhorizon.util.annotation.HeadlessDisabled;

import static mindustry.Vars.content;
import static mindustry.Vars.renderer;

@HeadlessDisabled
//render a screen-space effect for StatusEffect.
public class StatusRenderer{
	public static final float FADE_TIME = 90f;
	public static final float STATUS_RENDER_BEGIN = Layer.space + 0.0001f;
	public static final float STATUS_RENDER_STEP = 0.001f;
	public static final float STATUS_RENDER_RANGE = 0.0001f;

	//registered drawers
	public Seq<StatusEffect> registered = new Seq<>();
	//all status drawers.
	public StatusDrawer[] drawers;
	public Rand rand = new Rand();


	protected LastSeq lastSeq;
	protected IntMap<Runnable> drawTask = new IntMap<>();

	public StatusRenderer(){
		lastSeq = new LastSeq();
		drawers = new StatusDrawer[statusSize()];

		//init drawer with empty renderer
		for(int i = 0; i < statusSize(); i++){
			StatusEffect statusEffect = content.statusEffects().get(i);
			drawers[i] = new StatusDrawer(statusEffect, 0, (warmup, unit, status) -> {});
		}

		registerStatusEffects();
	}

	public int statusSize(){
		return Vars.content.statusEffects().size;
	}

	public float width() {
		return Core.camera.width;
	}
	public float height() {
		return Core.camera.height;
	}
	public float centerX(){
		return Core.camera.position.x;
	}
	public float centerY(){
		return Core.camera.position.y;
	}

	public float left(){return centerX() - width()/2;}
	public float right(){return centerX() + width()/2;}
	public float bottom(){return centerY() - height()/2;}
	public float top(){return centerY() + height()/2;}

	public long statusRandId(StatusEffect status){
		return status.id + status.hashCode();
	}

	public void registerStatusEffects(){
		register(NHStatusEffects.ultFireBurn, 20, (warmup, unit, status) -> {
			Color from = NHColor.lightSkyFront, to = NHColor.deeperBlue;

			float rad = width() / 32f;
			float life = 180f;
			int num = (int)(width() * renderer.getDisplayScale() * height() * renderer.getDisplayScale() / 5000);

			float base = (Time.time / life);
			rand.setSeed(statusRandId(status));

			Draw.blend(Blending.additive);

			Draw.color(from, Color.white, to, MathUtil.timeValue(0.2f * warmup, 0.3f * warmup, 0.5f));
			Draw.alpha(MathUtil.timeValue(0.2f * warmup, 0.3f * warmup, 0.5f));
			Fill.quad(left(), bottom(), left(), top(), right(), top(), right(), bottom());

			for(int i = 0; i < num; i++){
				float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
				Draw.alpha((rand.random(0.8f, 0.875f) + Mathf.absin(2f, 0.15f)) * (Mathf.curve(fout, 0.35f, 0.55f) + 3) / 4 * warmup);
				Draw.color(
					Tmp.c1.set(from).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.2f, 0.3f) * Mathf.curve(fout, 0.8f, 1f)),
					Tmp.c2.set(to).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.2f, 0.3f) * Mathf.curve(fout, 0.8f, 1f)),
					rand.random(0f, 1f)
				);

				Tmp.v1.setToRandomDirection(rand).scl(width() / 2.075f, height() / 2.05f).scl((rand.random(0.95f, 1.15f)) * Interp.fastSlow.apply(fout) + 0.165f + rand.random(0.05f, 0.125f));
				Fill.square(Tmp.v1.x + centerX(), Tmp.v1.y + centerY(), rad * rand.random(0.8f, 1.8f) * fout * fout * warmup, 45);
			}

			Draw.blend();
		});
		register(NHStatusEffects.overphased, 60, Shaders.buildBeam, (warmup, unit, status) -> {
			int step = 32;
			float rad = (width() + height()) / 2 / step;
			float stroke = rad * 0.16f;
			float inner = rad * 0.70f;

			float cx = centerX(), cy = centerY(), dst = rad * Mathf.sqrt3;

			float min = 0.7f;
			float max = 0.85f;
			rand.setSeed(statusRandId(status));
			for (int i = 1; i < step/2; i++){
				for (int j = 0; j < i; j++){
					for (int k = 0; k < 6; k++){
						Tmp.v1.trns(60 * k, dst * i).add(cx, cy);
						Tmp.v2.trns(60 * k + 120, dst * j).add(Tmp.v1);
						if (!(Tmp.v2.x - rad > right() || Tmp.v2.x + rad < left() || Tmp.v2.y - dst > top() || Tmp.v2.y + dst < bottom())){
							float toCenter = Mathf.dst(Tmp.v2.x, Tmp.v2.y, cx, cy);
							float ang = Angles.angle(Tmp.v2.x, Tmp.v2.y, cx, cy);
							float maxDst = Mathf.dst(Mathf.cosDeg(ang) * width()/2, Mathf.sinDeg(ang) * height()/2);
							float frac = toCenter / maxDst;
							float chance = frac > min? frac < max? Mathf.curve(frac, min, max): 1f: 0f;
							if (rand.chance(chance)){
								Tmp.c1.set(NHColor.lightSky).lerp(NHColor.lightSkyFront, warmup * MathUtil.timeValue(0.2f, 0.6f, 1.5f, rand.random(360)));
								Draw.color(Tmp.c1);
								Draw.alpha(0.75f + MathUtil.timeValue(-0.1f, 0.2f, 2f, rand.random(360)) * warmup);
								float innerFrac = Mathf.curve(frac, max, 1.3f);
								float innerWarmup = Interp.exp5In.apply(Mathf.clamp(innerFrac + warmup * 2));;
								Lines.stroke(stroke * innerWarmup);
								Lines.poly(Tmp.v2.x, Tmp.v2.y, 6, rad * innerWarmup, 30);
								Fill.poly(Tmp.v2.x, Tmp.v2.y, 6, inner * innerWarmup, 30);
							}
						}
					}
				}
			}


		});
		/*
		register(NHStatusEffects.scannerDown, 100, new StatusDrawer(NHShaders.scannerDown){
			private final Color from = Pal.heal, to = NHColor.lightSkyBack;

			@Override
			public void drawEffect(float warmup Unit unit){
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
		register(NHStatusEffects.scannerDown, 100, new StatusDrawer(NHShaders.scannerDown){
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
		register(NHStatusEffects.ultFireBurn, 20, new StatusDrawer(){
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
		register(NHStatusEffects.invincible, 25, new StatusDrawer(){
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
					
					Tmp.v1.set(vec3.x, vec3.y).sub(Core.camera.position).scl(renderer.getDisplayScale() / 3f);
					float fin = vec3.z / life, fout = 1f - fin;
					
					Draw.alpha(Mathf.curve(fin, 0, 0.55f) * f);
					Lines.stroke(stroke * fout * f);
					Draw.tint(from, to, fout);
					
					Lines.square(width / 2 + Tmp.v1.x, height / 2 + Tmp.v1.y, rad * fout + 0.22f, 45);
				}
				
				Draw.blend();
			}
		});

		 */
	}
	
	public void register(StatusEffect effect, int priority, Cons3<Float, Unit, StatusEffect> statusRenderer){
		registered.add(effect);
		drawers[effect.id] = new StatusDrawer(effect, priority, statusRenderer);
	}

	public void register(StatusEffect effect, int priority, Shader shader, Cons3<Float, Unit, StatusEffect> statusRenderer){
		registered.add(effect);
		drawers[effect.id] = new StatusDrawer(effect, priority, shader, statusRenderer);
	}

	public StatusDrawer getRenderer(StatusEffect statusEffect){
		return drawers[statusEffect.id];
	}
	
	public void draw(){
		for(Runnable r : drawTask.values()){
			r.run();
		}
	}
	
	public void update(){
		if(Vars.player.unit() == null || Vars.player.unit().statusBits() == null)return;
		
		registered.each(effect -> {
			if(Vars.player.unit().statusBits().get(effect.id)){
				StatusDrawer d = getRenderer(effect);
				lastSeq.add(effect.id);
				if(!drawTask.containsKey(effect.id)){
					drawTask.put(effect.id,
						() -> d.draw(Mathf.clamp(lastSeq.get(effect.id) / FADE_TIME), Vars.player.unit()));
				}
			}
		});
		
		lastSeq.update();
	}
	
	public void clear(){
		drawTask.clear();
		lastSeq.clear();
	}
	
	public class StatusDrawer {
		public StatusEffect status;
		public int priority;
		public Shader shader;
		public Cons3<Float, Unit, StatusEffect> statusRenderer;

		public StatusDrawer(StatusEffect status, int priority, Cons3<Float, Unit, StatusEffect> statusRenderer){
			this.status = status;
			this.priority = priority;
			this.statusRenderer = statusRenderer;
		}

		public StatusDrawer(StatusEffect status, int priority, Shader shader, Cons3<Float, Unit, StatusEffect> statusRenderer){
			this.status = status;
			this.shader = shader;
			this.priority = priority;
			this.statusRenderer = statusRenderer;
		}

		public float drawZLayer(){
			return STATUS_RENDER_BEGIN + STATUS_RENDER_STEP * priority;
		}

		public void drawShader(){
			if (shader != null){
				Draw.drawRange(drawZLayer(), STATUS_RENDER_RANGE,
					() ->
						renderer.effectBuffer.begin(Color.clear),
					() -> {
						renderer.effectBuffer.end();
						renderer.effectBuffer.blit(shader);
					});
			}
		}

		public void draw(float warmup, Unit unit){
			Draw.z(drawZLayer());
			statusRenderer.get(warmup, unit, status);
			drawShader();
		}

	}
	
	protected class LastSeq{
		public LastEffectTime[] arr;
		
		public LastSeq(){
			this.arr = new LastEffectTime[statusSize()];
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
