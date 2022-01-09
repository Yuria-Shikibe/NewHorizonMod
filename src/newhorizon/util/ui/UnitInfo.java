package newhorizon.util.ui;

import arc.Core;
import arc.func.Cons;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import newhorizon.content.NHColor;

public class UnitInfo extends Table{
	public static final IntMap<Table> added = new IntMap<>();
	protected static int lastID = -1, lastSize;
	protected static final Seq<Unit> tmpSeq = new Seq<>();
	
	protected static final Rect viewport = new Rect();
	protected static final Vec2 pos = new Vec2();
	protected static final Group root = new WidgetGroup();
	
	protected static final float hideSclValue = 1.1f;
	
	protected static boolean hidden(){
		return Vars.renderer.getDisplayScale() <= hideSclValue;
	}
	
	public static final float SIZE_SCL = 6;
	
	public static final float DEFAULT_HEIGHT = 24f;
	
	public Unit unit;
	
	public final Vec2 lastPos = new Vec2();
	public Table healthBarTable = new Table();
	
	protected float lastTableSize = -1;
	
	public boolean inBound = false;
	public float backgroundAlpha = 0;
	
	public UnitInfo(Unit unit){
		this.unit = unit;
		
		setup();
	}
	
	public UnitInfo(Unit unit, Drawable background){
		super(background);
		this.unit = unit;
		
		setup();
	}
	
	public UnitInfo(Unit unit, Drawable background, Cons<Table> cons){
		super(background, cons);
		this.unit = unit;
		
		setup();
	}
	
	public UnitInfo(Unit unit, Cons<Table> cons){
		super(cons);
		this.unit = unit;
		
		setup();
	}
//
//	@Override
//	protected void drawBackground(float x, float y){
//		if(getBackground() == null) return;
//		Color color = DelaySlideBar.back;
//		Draw.color(color.r, color.g, color.b, backgroundAlpha);
//		getBackground().draw(x, y, width, height);
//	}
//
//	public Element hit(float x, float y, boolean touchable){
//		if(touchable && this.touchable != Touchable.enabled)return null;
//		Element e = this;
//
//		if(hidden()){
//			float centerX = e.translation.x + width / 2;
//			float centerY = e.translation.y + height / 2 - unit.hitSize() * 1.15f;
//			float size = unit.hitSize() / 2f;
//			return x >= centerX - size && x < centerX + size && y >= centerY - size && y < centerY + size ? this : null;
//		}
//
//		return x >= e.translation.x && x < width / scaleX + e.translation.x && y >= e.translation.y && y < height / scaleY + e.translation.y ? this : null;
//	}
//
	public Table healthTable(){
		healthBarTable = new Table(Tex.pane){{
			margin(4);
			UnitHealthBar bar = new UnitHealthBar(
					unit, () -> NHColor.lightSkyBack, () -> Iconc.commandRally + " : " + (unit.shield() < 0 ? "OFFLINE" : (int)unit.shield()), unit::shield, () -> Math.max(unit.shield(), 100000)
			);
			bar.blinkable = true;
			bar.rootColor = Color.royal;
			bar.blinkColor = Pal.lancerLaser;
			add(bar).grow().padBottom(4f).row();
			
			add(new UnitHealthBar(
					unit, () -> unit.team.color, () -> Iconc.add + " : " + (unit.health() > 0 ? ((int)unit.health() + " / " + (int)unit.maxHealth()) : "Destroyed"), unit::healthf, () -> 1
			)).grow();
		}};
		
		return healthBarTable;
	}
	
	public void setSizeDefault(){
		if(unit.isValid())lastTableSize = unit.hitSize();
		
		setSize(lastTableSize * SIZE_SCL * Vars.renderer.getDisplayScale());
	}
	
	public void setup(){
		background(Styles.black3);
		
		touchable(() -> Touchable.childrenOnly);
		
//		hovered(() -> {
//			inBound = true;
//
//			UIActions.root().swapActor(this, UIActions.root().getChildren().peek());
//			setZIndex(parent.getChildren().size);
//
//			actions(Actions.scaleTo(Math.max(1, 1f / (unit.hitSize() / 80 + 0.25f) / Mathf.clamp(Vars.renderer.getDisplayScale() * 0.8f, 0.75f, 1)), 1.5f, 0.065f, Interp.smoother));
//		});
//
//		exited(() -> {
//			inBound = false;
//			actions(Actions.scaleTo(1f, 1f, 0.1f, Interp.smoother));
//		});
		
		visible(() -> {
			return Vars.ui.hudfrag.shown && (!unit.isValid() || viewport.overlaps(unit.x() - unit.clipSize() / 2f, unit.y() - unit.clipSize() / 2f, unit.clipSize(), unit.clipSize()));
		});
		
		setSizeDefault();
		
		align(Align.topLeft);
		
		add(healthTable().top()).top().growX().fillY().row();
	}
	
	@Override
	public void act(float delta){
		super.act(delta);
		
		healthBarTable.setHeight(UnitInfo.this.height / 12f);
//		if(inBound){
//			backgroundAlpha = Mathf.lerp(backgroundAlpha, 1, 0.1f);
//			parentAlpha = Mathf.approach(parentAlpha, 1, 0.2f);
//		}else{
//			backgroundAlpha = Mathf.lerp(backgroundAlpha, 0, 0.045f);
//			parentAlpha = Mathf.approach(parentAlpha, Mathf.curve(Vars.renderer.getDisplayScale(), hideSclValue, 1.2f), 0.1f);
//		}
//
//		if(!hasActions())color.a = parentAlpha;
//
//		if(hidden()){
//			setScale(Math.max(1, 1f / (unit.hitSize() / 80 + 0.25f)/ 0.75f), 1.5f);
//		}else if(!hasActions() && !inBound)setScale(1, 1);
		
		if(unit.isValid()){
			lastPos.set(unit);
			setSizeDefault();
		}else{
			if(!hasActions()){
				actions(
					Actions.fadeOut(0.35f), Actions.remove()
				);
				
				getChildren().each(e -> e.actions(
					Actions.fadeOut(0.35f), Actions.remove()
				));
			}
		}
		
		pos.set(Core.camera.project(lastPos.x, lastPos.y));
		setPosition(pos.x - width / 2, pos.y - height / 2);
	}
	
	
	public static void init(){
		root.remove();
		Core.scene.root.addChildAt(0, root);
	}
	
	public static void update(){
		Core.camera.bounds(viewport);
		
		if(Groups.unit.isEmpty() || Groups.unit.size() == lastSize)return;
		Groups.unit.copy(tmpSeq);
		tmpSeq.sortComparing(Unit::id);
		for(int i = tmpSeq.size - 1; i >= 0; i--){
			Unit unit = tmpSeq.get(i);
			if(unit.id == lastID)break;
			create(unit);
		}
		
		lastSize = Groups.unit.size();
	}
	
	public static void addBars(){
		init();
		
		Groups.unit.each(e -> !added.keys().toArray().contains(e.id()), UnitInfo::create);
	}
	
	public static void create(Unit unit){
		UnitInfo info = new UnitInfo(unit);
		
		added.put(unit.id, info);
		lastID = unit.id;
		root.addChild(info);
	}
	
	public static class UnitHealthBar extends DelaySlideBar{
		public UnitHealthBar(Unit unit, Prov<Color> colorReal, Prov<CharSequence> info, Floatp valueGetter, Floatp maxValue){
			super(colorReal, info, valueGetter, maxValue);
			
			fontScale = b -> Mathf.clamp(b.getHeight() / Fonts.outline.getData().lineHeight * b.scaleY * 0.85f, 0.001f, b.scaleY);
		}
		
		public UnitHealthBar(Unit unit){
			this(unit, () -> unit.team.color, () -> (int)unit.health() + " / " + (int)unit.maxHealth(), unit::healthf, () -> 1);
		}
	}
}
