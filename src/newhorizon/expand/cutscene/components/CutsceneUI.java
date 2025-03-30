package newhorizon.expand.cutscene.components;

import arc.Core;
import arc.Events;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectMap;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.cutscene.components.ui.MarkBox;
import newhorizon.expand.cutscene.components.ui.MarkStyle;
import newhorizon.util.annotation.ClientOnly;
import newhorizon.util.annotation.HeadlessDisabled;

import static mindustry.Vars.headless;
import static mindustry.Vars.player;
import static newhorizon.NHRenderer.height;
import static newhorizon.NHRenderer.width;
import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

@HeadlessDisabled
public class CutsceneUI {
	public WidgetGroup root, overlay, curtain;
	public Table textTable, textArea, infoTable, skip;
	public FLabel textLabel, infoLabel;

	public boolean controlOverride = false;

	public Interp curtainInterp = Interp.pow2Out;
	public float curtainProgress = 0;
	
	public final float OVERLAY_SPEED = 0.0065f;
	public float targetOverlayAlpha;
	public float overlayAlphaShiftSpeed = OVERLAY_SPEED;

	public Image killStreak;
	public Vec2 killStreakShake = new Vec2();
	public float killStreakShakeTimer = 0f;
	public float killStreakCountTimer = 0f;
	public int killStreakCount = 0;

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
		//lmao
		buildKillStreak();
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

	private void buildKillStreak(){
		Events.on(EventType.UnitBulletDestroyEvent.class, e -> {
			//if (e.bullet.owner != player) return;
			killStreakCountTimer = 180f;
			killStreakShakeTimer = 60f;
			killStreakCount++;
			killStreakCount = Mathf.clamp(killStreakCount, 0, 8);
			killStreakShake.setToRandomDirection().scl(10f);
			killStreak.setDrawable(getKillStreak());
			killStreak.clearActions();
			killStreak.color.a = 0;
			killStreak.actions(Actions.fadeIn(0.1f), Actions.delay(2f), Actions.fadeOut(0.3f));
		});

		killStreak = new Image(NHContent.khs1){{
			touchable(() -> Touchable.disabled);
			visible(() -> Vars.state.isGame());
			color.a = 0;
		}};

		killStreak.update(() -> {
			if (killStreakCountTimer <= 0) {
				killStreakCount = 0;
				killStreakShakeTimer = 0;
			}else {
				killStreakCountTimer -= Time.delta;
				if (killStreakShakeTimer > 0) {
					killStreakShakeTimer -= Time.delta;
				}
				killStreakShake.setToRandomDirection().scl(killStreakShakeTimer / 9f);
			}

			killStreak.setSize(224, 224);
			killStreak.setPosition((width - killStreak.getWidth()) / 2 + killStreakShake.x, height * 0.15f + killStreakShake.y);
		});
	}

	private TextureRegion getKillStreak(){
		boolean chance = player.team().id % 2 == 0;
		if (killStreakCount == 1 && Mathf.randomBoolean(0.05f)) {
			if (chance) NHSounds.ksah.play();
			else NHSounds.ksbh.play();
			return NHContent.khs1;
		}
		if (killStreakCount == 1 && Mathf.randomBoolean(0.2f)) {
			if (chance) NHSounds.ksah.play();
			else NHSounds.ksbh.play();
			return NHContent.khs0;
		}

        switch (killStreakCount) {
            case 1 -> {
				if (chance) NHSounds.ksa1.play();
				else NHSounds.ksb1.play();
				return NHContent.ks1;
			}
            case 2 -> {
				if (chance) NHSounds.ksa2.play();
				else NHSounds.ksb2.play();
				return NHContent.ks2;
			}
            case 3 -> {
				if (chance) NHSounds.ksa3.play();
				else NHSounds.ksb3.play();
				return NHContent.ks3;
			}
            case 4 -> {
				if (chance) NHSounds.ksa4.play();
				else NHSounds.ksb4.play();
				return NHContent.ks4;
			}
            case 5 -> {
				if (chance) NHSounds.ksa5.play();
				else NHSounds.ksb5.play();
				return NHContent.ks5;
			}
            case 6 -> {
				if (chance) NHSounds.ksa6.play();
				else NHSounds.ksb6.play();
				return NHContent.ks6;
			}
            case 7 -> {
				if (chance) NHSounds.ksa7.play();
				else NHSounds.ksb7.play();
				return NHContent.ks7;
			}
            case 8 -> {
				if (chance) NHSounds.ksa8.play();
				else NHSounds.ksb8.play();
				return NHContent.ks8;
			}
            default -> {
				if (chance) NHSounds.ksah.play();
				else NHSounds.ksbh.play();
				return NHContent.khs1;
			}
        }

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
		}
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
			root.addChild(killStreak);
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

		infoLabel = new FLabel("");
		infoTable.clear();
		infoTable.add(cutsceneUI.infoLabel);
		infoTable.actions(Actions.alpha(0));

		textLabel = new FLabel("");
		textArea.clear();
		textArea.add(cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
		textTable.actions(Actions.alpha(0));

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
