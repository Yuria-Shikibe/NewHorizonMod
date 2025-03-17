package newhorizon.expand.cutscene.components;

import arc.Core;
import arc.Events;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectMap;
import arc.util.Align;
import arc.util.Interval;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import newhorizon.content.NHSounds;
import newhorizon.expand.cutscene.components.ui.MarkBox;
import newhorizon.expand.cutscene.components.ui.MarkStyle;
import newhorizon.util.annotation.ClientOnly;
import newhorizon.util.annotation.HeadlessDisabled;

import static mindustry.Vars.headless;
import static newhorizon.NHRenderer.height;
import static newhorizon.NHRenderer.width;
import static newhorizon.NHVars.cutscene;

@HeadlessDisabled
public class CutsceneUI {
	public WidgetGroup root, overlay, curtain;
	public Table textTable, textArea, infoTable, skip;
	public FLabel textLabel, infoLabel;

	public float zoomLock = 1f;
	public boolean controlOverride = false;

	public Interp curtainInterp = Interp.pow2Out;
	public float curtainProgress = 0;
	
	public final float OVERLAY_SPEED = 0.0065f;
	public float targetOverlayAlpha;
	public float overlayAlphaShiftSpeed = OVERLAY_SPEED;

	public final ObjectMap<String, MarkBox> markers = new ObjectMap<>();

	public CutsceneUI() {
		if (headless) return;
		init();
		Events.on(EventType.WorldLoadEvent.class, e -> resetSave());
	}

	public float curtainScl(){
		return Core.graphics.isPortrait() ? 0.22f : 0.1185f;
	}

	public float textTableScl(){
		return Core.graphics.isPortrait() ? 0.22f : 0.1185f;
	}

	public float infoTableScl(){
		return Core.graphics.isPortrait() ? 0.4f : 0.2f;
	}

	public void init(){
		//cutscene root ui, the container of all cutscene ui
		buildRoot();
		//overlay ui, used to add some custom marks like signals.
		buildOverlay();
		//curtain ui, for cutscene curtain and fade in/fade out effect
		buildCurtain();
		//text signal cut-in/cut-out ui, used for dialogs.
		buildTextTable();
		//COD styled info dialog.
		buildInfoTable();
		//skip button for skip current cutscene
		buildSkip();
		//update the text table position according to the layout
		updatePosition();
		//build all cutscene ui, add the elements to root.
		buildCutsceneUI();
	}

	private void buildRoot(){
		root = new WidgetGroup(){{
			setFillParent(true);
			touchable = Touchable.childrenOnly;
		}};
	}

	private void buildOverlay(){
		overlay = new WidgetGroup(){{
			fillParent = true;
			touchable = Touchable.disabled;
		}};
	}

	private void buildCurtain(){
		curtain = new WidgetGroup(){
			{
				color.a = 1;
				fillParent = true;
				touchable = Touchable.disabled;
			}

			@Override
			public void draw(){
				super.draw();

				float heightC = height * curtainScl() * curtainInterp.apply(curtainProgress);

				Draw.color(Color.black);
				Draw.alpha(Interp.pow3Out.apply(Mathf.curve(curtainProgress, 0, 0.75f)));
				Fill.quad(0,  0, 0, heightC, width, heightC, width, 0);
				Fill.quad(0, height, 0, height - heightC, width, height - heightC, width, height);
				Draw.reset();

				Draw.color(0, 0, 0, color.a);
				Fill.quad(0, 0, 0, height, width, height, width, 0);
			}
		};
	}

	private void buildTextTable(){
		textTable = new Table(Tex.buttonEdge3){{
			touchable(() -> Touchable.disabled);
			visible(() -> Vars.state.isGame());
			color.a = 0;

			if(headless){
				textArea = new Table();
			}else {
				pane(Styles.smallPane, t -> {
					textArea = t;
					textArea.defaults().grow().pad(2f);
					textArea.exited(() -> Core.scene.unfocus(textArea));
					t.fillParent = true;
				}).grow();
			}
		}};
	}

	public void buildInfoTable(){
		infoTable = new Table(Tex.clear){{
			touchable(() -> Touchable.disabled);
			visible(() -> Vars.state.isGame());
			color.a = 0;
		}};
	}

	private void buildSkip(){
		skip = new Table(){{
			margin(12f);
			visible(() -> !Vars.net.client() && isPlayingMainCutscene());
			setFillParent(true);
			touchable = Touchable.enabled;
			align(Align.topLeft);
			button("Skip Cutscene", Icon.play, () -> {
				cutscene.mainBus.skip();
			}).marginLeft(8f).size(320, 50f).padTop(Vars.mobile ? 60 : 0);
		}};
	}

	private void updatePosition(){
		if(Vars.mobile){
			textTable.update(() -> {
				textTable.setHeight(height * textTableScl());
				textTable.setWidth(width);
				textTable.setPosition(0, 0);
			});
			infoTable.update(() -> {
				infoTable.setHeight(height * infoTableScl());
				infoTable.setWidth(width);
				infoTable.setPosition(0, 0);
			});
		} else {
			textTable.update(() -> {
				textTable.setSize(Scl.scl(width * 0.65f), Scl.scl(height * 0.1f));
				textTable.setPosition((width - textTable.getWidth()) / 2, height * 0.14f);
			});
			infoTable.update(() -> {
				infoTable.setSize(Scl.scl(width * 0.25f), Scl.scl(height * 0.1f));
				infoTable.setPosition(width * 0.05f, height * 0.1f);
			});
		};
	}

	private void buildCutsceneUI(){
		if(!headless){
			Vars.control.input.addLock(() -> controlOverride);
			Core.scene.root.addChildAt(0, root);
			root.addChild(overlay);
			root.addChild(curtain);
			root.addChild(textTable);
			root.addChild(infoTable);
			root.addChild(skip);
		}
	}
	
	public boolean isPlayingMainCutscene(){
		return cutscene.mainBus != null && !cutscene.mainBus.complete();
	}

	@ClientOnly
	public void reset(){
		if (headless) return;
		controlOverride = false;
		curtainProgress = 0;
		targetOverlayAlpha = 0;
		overlayAlphaShiftSpeed = OVERLAY_SPEED;
		
		overlay.clear();
		textArea.clear();
		markers.clear();
	}

	public void resetSave(){
		reset();
		curtain.color.a = 1;
	}

	public void update(){
		if(headless) return;
		curtain.color.a = Mathf.approachDelta(curtain.color.a, targetOverlayAlpha, overlayAlphaShiftSpeed);
	}

	public void clear(){
		if (headless) return;
		overlay.clear();
	}

	@HeadlessDisabled
	public void mark(float x, float y, float radius, float lifetime, Color color, MarkStyle style){
		if(headless)return;
		MarkBox box = new MarkBox();
		box.init(radius, color, new Vec2(x, y), style);
		if(lifetime > 0)box.lifetime = lifetime;
		box.addSelf();
	}
}
