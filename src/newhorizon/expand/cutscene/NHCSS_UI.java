package newhorizon.expand.cutscene;

import arc.Core;
import arc.flabel.FConfig;
import arc.flabel.FLabel;
import arc.func.Boolp;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectMap;
import arc.util.Align;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.util.annotation.HeadlessDisabled;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.struct.TimeQueue;

import static arc.Core.settings;
import static newhorizon.NHRenderer.height;
import static newhorizon.NHRenderer.width;

@HeadlessDisabled
public class NHCSS_UI{
	public static NHCSS_Core core(){return NHCSS_Core.core;}
	
	public static WidgetGroup root, customizeLayer, overlay;
	public static Table textTable;
	public static Table paneArea;
	
	public static Pool<MarkBox> markBoxPool = new Pool<MarkBox>(200){
		@Override
		protected MarkBox newObject(){
			return new MarkBox();
		}
	};
	
	public static TimeQueue<TextBox> textQueue = new TimeQueue<>();
	
	public static float curtainProgress = 0;
	public static boolean duringCurtain = false;
	public static boolean fallbackShowUI = true;
	
	public static final Vec2 cameraTarget = new Vec2();
	public static Interp panInterp = Interp.smooth;
	public static float panProgress = 0;
	public static boolean controlOverride = false;
	public static boolean cameraOverride = false;
	
	public static float CURTAIN_HEIGHT_SCL(){
		return Core.graphics.isPortrait() ? 0.22f : 0.1185f;
	}
	public static final float CURTAIN_SPEED = 0.0175f;
	
	public static Interp curtainInterp = Interp.pow2Out;
	
	public static final float OVERLAY_SPEED = 0.0065f;
	public static float targetOverlayAlpha;
	public static float overlayAlphaShiftSpeed = OVERLAY_SPEED;
	
	public static boolean skipFallback = false;
	
	public static float paneAreaCheckReload = 0;
	
	public static final ObjectMap<String, MarkBox> markers = new ObjectMap<>();
	
	//This Should Be Done After the client is launched;
	public static void init(){
		root = new WidgetGroup(){{
			setFillParent(true);
			touchable = Touchable.childrenOnly;
		}};
		
		customizeLayer = new WidgetGroup(){{
			fillParent = true;
			touchable = Touchable.disabled;
		}};
		
		//Overlay Only work for curtain system.
		overlay = new WidgetGroup(){
			{
				color.a = 1;
				fillParent = true;
				touchable = Touchable.disabled;
			}
			
			@Override
			public void draw(){
				super.draw();
			
				float heightC = height * CURTAIN_HEIGHT_SCL() * curtainInterp.apply(curtainProgress);
				
				Draw.color(Color.black);
				Draw.alpha(Interp.pow3Out.apply(Mathf.curve(curtainProgress, 0, 0.75f)));
				Fill.quad(0,  0, 0, heightC, width, heightC, width, 0);
				Fill.quad(0, height, 0, height - heightC, width, height - heightC, width, height);
				Draw.reset();
				
				Draw.color(0, 0, 0, color.a);
				Fill.quad(0, 0, 0, height, width, height, width, 0);
			}
		};
		
		textTable = new Table(Tex.buttonEdge3){{
			touchable(() -> {
				if(color.a > 0.1f){
					return Touchable.childrenOnly;
				}else return Touchable.disabled;
			});
			visible(() -> Vars.state.isGame());
			color.a = 0;
			
			if(Vars.headless){
				paneArea = new Table();
			}else pane(Styles.smallPane, t -> {
				paneArea = t;
				paneArea.defaults().grow().pad(2f);
				t.fillParent = true;
				paneArea.exited(() -> Core.scene.unfocus(paneArea));
			}).grow();
		}
			@Override
			public void act(float delta){
				super.act(delta);
				
				if(!hasActions()){
					if(!paneArea.hasChildren()){
						paneAreaCheckReload += delta;
						if(paneAreaCheckReload > 0.35f){
							actions(Actions.fadeOut(0.2f, Interp.pow2In));
							paneAreaCheckReload = 0;
						}
					}else{
						actions(Actions.fadeIn(0.2f, NHInterp.bounce5Out));
					}
					
				}
			}
		};
		
		if(Vars.mobile){
			textTable.update(() -> {
				textTable.setHeight(height * CURTAIN_HEIGHT_SCL());
				textTable.setWidth(width);
				textTable.setPosition(0, 0);
			});
		}else textTable.update(() -> {
			textTable.setSize(Scl.scl(width * 0.65f), Scl.scl(height * 0.1f));
			textTable.setPosition((width - textTable.getWidth()) / 2, height * 0.14f);
		});
		
		if(!Vars.headless){
			Vars.control.input.addLock(() -> controlOverride);
			Core.scene.root.addChildAt(0, root);
			root.addChild(customizeLayer);
			root.addChild(overlay);
			root.addChild(new Table(){{
				margin(12f);
				visible(() -> !Vars.net.client() && isPlayingMainCutscene());
				setFillParent(true);
				touchable = Touchable.childrenOnly;
				align(Align.topLeft);
				button("Skip Cutscene", Icon.play, () -> {
					NHCSS_Core.core.mainBus.skip();
				}).marginLeft(8f).size(320, 50f).padTop(Vars.mobile ? 60 : 0);
			}});
			root.addChild(textTable);
		}
	}
	
	public static boolean isPlayingMainCutscene(){
		return core().mainBus != null && core().mainBus.valid();
	}
	
	public static void reset(){
		controlOverride = false;
		curtainProgress = 0;
		duringCurtain = false;
		cameraOverride = false;
		targetOverlayAlpha = 0;
		overlayAlphaShiftSpeed = OVERLAY_SPEED;
		
		customizeLayer.clear();
		textQueue.clear();
		paneArea.clear();
		markers.clear();
		
		overlay.color.a = 1;
	}
	
	public static void update(){
		if(Vars.headless){
			reset();
			return;
		}
		
		controlOverride = isPlayingMainCutscene() | cameraOverride;
		
		overlay.color.a = Mathf.approachDelta(overlay.color.a, targetOverlayAlpha, overlayAlphaShiftSpeed);
		
		if(duringCurtain){
			curtainProgress = Mathf.approachDelta(curtainProgress, 1, CURTAIN_SPEED);
			Vars.ui.hudfrag.shown = false;
		}else{
			curtainProgress = Mathf.approachDelta(curtainProgress, 0, CURTAIN_SPEED);
		}
		
		updateText();
	}
	
	public static void add(Element element){
		if(Vars.headless)return;
		
		customizeLayer.addChild(element);
	}
	
	public static float curtainScratchTime(){
		return 1 / CURTAIN_SPEED;
	}
	
	public static float overlayShiftTime(){
		return 1 / overlayAlphaShiftSpeed;
	}
	
	public static void clear(){
		customizeLayer.clear();
	}
	
	public static void skipCoreLanding(){
		skipFallback = settings.getBool("skipcoreanimation");
		settings.put("skipcoreanimation", true);
	}
	
	public static void showCoreLanding(){
		skipFallback = settings.getBool("skipcoreanimation");
		settings.put("skipcoreanimation", false);
	}
	
	public static void setSkippingLandingToDef(){
		settings.put("skipcoreanimation", skipFallback);
	}
	
	public static void opening(){
		setToClear();
		setBlack();
		
	}
	
	public static void setBlack(){
		if(Vars.headless)return;
		overlay.color.a = 2.125f;
	}
	
	public static void setToBlack(){
		targetOverlayAlpha = 1;
	}
	
	public static void setToClear(){
		targetOverlayAlpha = 0;
	}
	
	public static void setOverlayAlphaShiftSpeed(float shiftSpeed){
		overlayAlphaShiftSpeed = shiftSpeed;
	}
	
	public static void setOverlayAlphaShiftSpeed(){
		overlayAlphaShiftSpeed = OVERLAY_SPEED;
	}
	
	public static void pullCurtain(){
		if(Vars.headless)return;
		fallbackShowUI = Vars.ui.hudfrag.shown;
		duringCurtain = true;
	}
	
	public static void withdrawCurtain(){
		if(Vars.headless)return;
		duringCurtain = false;
		Vars.ui.hudfrag.shown = fallbackShowUI;
	}
	
	public static void updateText(){
		textQueue.update(TextBox::setup);
	}
	
	public static void postText(TextBox textBox){
		textQueue.add(textBox);
	}
	
	public static void postText(String text, float time){
		textQueue.add(new TextBox(text, time));
	}
	
	/**
	 * {@link arc.flabel.FConfig}
	 * */
	public static class TextBox implements TimeQueue.Timed{
		public Table self;
		
		public Cons<Table> modifier = null;
		public String text = "null text";
		public float duration = 90f;
		public float life = 0;
		
		public float fadeTime = 18f;
		
		public TextBox(){
		}
		
		public TextBox(String text){
			this.text = text;
			setDefDuration();
		}
		
		public TextBox(String text, float duration){
			this.text = text;
			this.duration = duration;
		}
		
		public TextBox(String text, float duration, Cons<Table> modifier){
			this.modifier = modifier;
			this.text = text;
			this.duration = duration;
		}
		
		public void setDefDuration(){
			duration = text.length() * FConfig.defaultSpeedPerChar * 60 + 60f;
		}
		
		public void setup(){
			if(Vars.headless)return;
			paneArea.clear();
			self = new Table();
			if(modifier != null)modifier.get(self);
			self.add(new FLabel(text));
			self.margin(2f);
			paneArea.add(self);
		}
		
		@Override
		public void update(){
			life += Time.delta;
			
			if(life >= duration){
				if(life >= duration + fadeTime){
					next();
				}
				if(!self.hasActions())self.actions(Actions.fadeOut(fadeTime / 60f), Actions.remove());
			}
		}
		
		@Override
		public float getDuration(){
			return duration;
		}
		
		@Override
		public void next(){
			if(!textQueue.queue.isEmpty()){
				textQueue.current = textQueue.queue.removeLast();
				textQueue.current.setup();
			}else textQueue.current = null;
		}
	}
	
	public static Interval signalInterval = new Interval();
	
	@HeadlessDisabled
	public static void mark(float x, float y, float radius, float lifetime, Color color, Boolp removeCheck){
		if(Vars.headless)return;
		
		
		MarkBox box = new MarkBox();
		box.init(radius, color, new Vec2(x, y), MarkStyle.defaultStyle);
		box.removeCheck = removeCheck;
		if(lifetime > 0)box.lifetime = lifetime;
		box.addSelf();
	}
	
	@HeadlessDisabled
	public static void mark(float x, float y, float radius, float lifetime, Color color, MarkStyle style, Boolp removeCheck){
		if(Vars.headless)return;
		
		
		MarkBox box = new MarkBox();//markBoxPool.obtain();
		box.init(radius, color, new Vec2(x, y), style);
		box.removeCheck = removeCheck;
		if(lifetime > 0)box.lifetime = lifetime;
		box.addSelf();
	}
	
	@HeadlessDisabled
	public static void markSignal(float x, float y, float maxDst, Color color){
		if(Vars.headless || !signalInterval.get(60f))return;
		float dst = Core.camera.position.dst(x, y);
		
		if(dst > maxDst)return;
		
		float offset = 320;
		float scl = dst / maxDst;
		NHSounds.signal.at(x, y, 1, 0.55f);
		
//		MarkBox box = new MarkBox();
		MarkBox box = new MarkBox();//markBoxPool.obtain();
		Tmp.v1.setToRandomDirection().scl(scl * offset + 40);
		box.init(9, color, new Vec2(x, y).add(Tmp.v1), MarkStyle.shake);
		box.setLife(45f);
		box.addSelf();
	}
	
	public static void forceMarkSignal(float x, float y, float maxDst, Color color){
		if(Vars.player == null)return;
		float dst = Vars.player.dst(x, y);
		
		if(dst > maxDst)return;
		
		float offset = 320;
		float scl = dst / maxDst;
		NHSounds.signal.at(x, y, 1, 0.55f);
		
		MarkBox box = new MarkBox();//markBoxPool.obtain();
		Tmp.v1.setToRandomDirection().scl(scl * offset + 8);
		box.init(9, color, new Vec2(x, y).add(Tmp.v1), MarkStyle.shake);
		box.setLife(45f);
		box.addSelf();
	}
	
	
	public static class MarkBox extends Table implements Pool.Poolable{
		protected static final Vec2 tmpVec = new Vec2();
		protected static final Color tmpColor = new Color();
		
		public float radius = 24f;
		public Color markColor = Color.white;
		public Interp popUpInterp = NHInterp.bounce5Out;
		public Position markPoint;
		public MarkStyle style = MarkStyle.defaultStyle;
		public Boolp removeCheck = () -> false;
		public int id = lastID++;
		
		protected static int lastID = 0;
		
		public float lifetime = -1;
		
		public MarkBox(){
			touchable = Touchable.childrenOnly;
			fillParent = true;
			
			update(() -> {
				totalProgress += Time.delta;
				if(Vars.state.isMenu()) remove();
			});
			
			color.a = 0;
		}
		
		public void addSelf(){
			customizeLayer.addChild(this);
		}
		
		public MarkBox init(float radius, Color markColor, Position markPoint, MarkStyle style){
			this.radius = radius;
			this.markColor = markColor;
			this.markPoint = markPoint;
			this.style = style;
			
			return this;
		}
		
		public MarkBox setLife(float lifetime){
			this.lifetime = lifetime;
			return this;
		}
		
		protected float totalProgress = 0;
		
		{
			actions(Actions.alpha(1, 0.45f, popUpInterp));
		}
		
		@Override
		public void act(float delta){
			super.act(delta);
			
			if(lifetime > 0){
				if(totalProgress > lifetime){
					removeFromHUD();
				}
			}
			
			if(removeCheck.get())removeFromHUD();
		}
		
		public void removeFromHUD(){
			actions(Actions.fadeOut(0.7f), Actions.parallel(Actions.remove()/*, Actions.run(() -> markBoxPool.free(this))*/));
		}
		
		@Override
		public void draw(){
			super.draw();
			
			if(Vars.headless)return;
			
			Vec2 screenVec = tmpVec.set(Core.camera.project(markPoint.getX(), markPoint.getY()));
			
			boolean outer = screenVec.x < width * 0.05f || screenVec.y < height * 0.05f || screenVec.x > width * 0.95f || screenVec.y > height * 0.95f;
			
//			if(corner)screenVec.clamp(width * 0.05f, height * 0.05f, height * 0.95f, width * 0.95f);
			
			if(outer){
				screenVec.x = Mathf.clamp(screenVec.x, width * 0.05f, width * 0.95f);
				screenVec.y = Mathf.clamp(screenVec.y, height * 0.05f, height * 0.95f);
			}
			
			tmpColor.set(markColor).lerp(Color.white, Mathf.absin(totalProgress, 5f, 0.4f)).a(color.a);
			
			style.drawer.draw(id, totalProgress, radius, screenVec, tmpColor, outer);
		}
		
	}
	
	public enum MarkStyle{
		defaultStyle((id, time, radius, pos, color, beyond) -> {
			Tmp.c2.set(Pal.gray).a(color.a);
			
			float size = radius * Vars.renderer.getDisplayScale();
			
			float rotationS = 45 + 90 * NHInterp.pow10.apply((time / 120) % 1);
			float angle = beyond ? Angles.angle(width / 2, height / 2, pos.x, pos.y) - 90 : 0;
			Lines.stroke(9f, Tmp.c2);
			Lines.square(pos.x, pos.y, size + 3f, rotationS);
			Lines.stroke(3f, color);
			if(beyond)Draw.rect(NHContent.pointerRegion, pos, size, size, angle);
			Lines.square(pos.x, pos.y, size + 3f, rotationS);
			
			Lines.stroke(9f, Tmp.c2);
			for(int i : Mathf.signs){
				Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
				Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
			}
			
			Lines.stroke(3f, color);
			for(int i : Mathf.signs){
				Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
				Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
			}
		}),
		
		defaultNoLines((id, time, radius, pos, color, beyond) -> {
			Tmp.c2.set(Pal.gray).a(color.a);
			
			float size = radius * Vars.renderer.getDisplayScale();
			
			float rotationS = 45 + 90 * NHInterp.pow10.apply((time / 120) % 1);
			float angle = beyond ? Angles.angle(width / 2, height / 2, pos.x, pos.y) - 90 : 0;
			Lines.stroke(9f, Tmp.c2);
			Lines.square(pos.x, pos.y, size + 3f, rotationS);
			Lines.stroke(3f, color);
			if(beyond)Draw.rect(NHContent.pointerRegion, pos, size, size, angle);
			Lines.square(pos.x, pos.y, size + 3f, rotationS);
		}),
		
		fixed((id, time, radius, pos, color, beyond) -> {
			Tmp.c2.set(Pal.gray).a(color.a);
			
			float size = radius  * Vars.renderer.getDisplayScale();
			
			float rotationS = 45;
			float angle = beyond ? Angles.angle(width / 2, height / 2, pos.x, pos.y) - 90 : 0;
			Lines.stroke(9f, Tmp.c2);
			Lines.square(pos.x, pos.y, size + 3f, rotationS);
			Lines.stroke(3f, color);
			if(beyond)Draw.rect(NHContent.pointerRegion, pos, size, size, angle);
			Lines.square(pos.x, pos.y, size + 3f, rotationS);
			
			Lines.stroke(9f, Tmp.c2);
			for(int i : Mathf.signs){
				Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
				Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
			}
			
			Lines.stroke(3f, color);
			for(int i : Mathf.signs){
				Lines.line(Math.max(0, i) * width, pos.y, pos.x + size * i * 2, pos.y);
				Lines.line(pos.x, Math.max(0, i) * height, pos.x, pos.y + size * i * 2);
			}
		}),
		
		shake((id, time, radius, pos, color, beyond) -> {
			Tmp.c2.set(Pal.gray).a(color.a);
			
			Rand rand = NHFunc.rand;
//			long t = (long)time;
//			t = t - (t % 60);
			rand.setSeed((long)Mathf.round(time, 9f) + id);
			
			Vec2 v = pos.cpy().add(rand.range(12), rand.range(12));
			float size = radius  * Vars.renderer.getDisplayScale();
			
			float rotationS = 45;
			float angle = beyond ? Angles.angle(width / 2, height / 2, v.x, v.y) - 90 : 0;
			Lines.stroke(9f, Tmp.c2);
			Lines.square(v.x, v.y, size + 3f, rotationS);
			Lines.stroke(3f, color);
			Lines.square(v.x, v.y, size + 3f, rotationS);
			
			Lines.stroke(9f, Tmp.c2);
			Lines.spikes(pos.x, pos.y, size * 1.5f + 6f, size / 2, 4, 45);
			
			Lines.stroke(3f, color);
			Lines.spikes(pos.x, pos.y, size * 1.5f + 6f, size / 2, 4, 45);
		});
		
		public final DrawCaution drawer;
		
		MarkStyle(DrawCaution drawer){
			this.drawer = drawer;
		}
	}
	
	public interface DrawCaution{
		void draw(int id, float time, float radius, Vec2 pos, Color color, boolean beyond);
	}
}
