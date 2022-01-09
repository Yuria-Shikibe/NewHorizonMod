package newhorizon.util.ui;

import arc.func.Boolf;
import arc.func.Floatf;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import newhorizon.util.func.NHInterp;

public class DelaySlideBar extends Table{
	public static final Color back = Color.black.cpy().lerp(Color.darkGray, 0.35f).a(0.5f);
	
	protected float currentValue, lastValue, stableValue;
	protected float equalizedTime;
	
	protected float stableThreshold = 0.4f;
	
	protected float blink = 0;
	protected boolean blinked = false;
	public boolean blinkable = false;
	
	protected float fontAlpha = 1;
	
	public boolean drawBackground = true;
	public boolean drawShadow = false;
	
	public float approachSpeedScl_Stable = 0.025f;
	public float approachSpeedScl_Real = 0.125f;
	
	public float barMargin = 0;
	public float realStableMargin = 0f;
	
	public boolean snapWhileIncrease = true;
	public float allowedDeviation = Mathf.FLOAT_ROUNDING_ERROR;
	
	public Color rootColor = Color.black;
	public float rootColorLerp = 0.6f;
	
	public Color backgroundColor = back;
	public Color blinkColor = Color.white;
	public Prov<Color> colorStable = () -> Color.lightGray;
	public Prov<Color> colorReal = () -> Pal.accent;
	
	public Boolf<DelaySlideBar> stable = b -> b.equalizedTime >= b.stableThreshold;
	public Prov<CharSequence> info = () -> "null";
	public Floatp valueGetter = () -> 0;
	public Floatp maxValue = () -> 1;
	public Floatf<DelaySlideBar> fontScale = b -> 1;
	
	public DelaySlideBar(){
		color.set(1f, 1f, 1f, 1f);
	}
	
	public DelaySlideBar(Prov<Color> colorReal, Prov<CharSequence> info, Floatp valueGetter, Floatp maxValue){
		this();
		
		this.info = info;
		this.colorReal = colorReal;
		this.valueGetter = valueGetter;
		this.maxValue = maxValue;
		
		snap();
	}
	
	public DelaySlideBar(Prov<Color> colorReal, Prov<CharSequence> info, Floatp valueGetter){
		this();
		
		this.colorReal = colorReal;
		this.info = info;
		this.valueGetter = valueGetter;
		
		snap();
	}
	
	public boolean isStable(){
//		Log.info(equalizedTime + "|" + stableThreshold);
		return stable.get(this);
	}
	
	public void snap(){
		lastValue = currentValue = stableValue = valueGetter.get();
	}
	
	public float getFraction(){
		return lastValue / maxValue.get();
	}
	
	public float getStableFraction(){
		return stableValue / maxValue.get();
	}
	
	@Override
	public void act(float delta){
		super.act(delta);
		
		currentValue = Mathf.clamp(valueGetter.get(), 0 + Mathf.FLOAT_ROUNDING_ERROR, maxValue.get());
		
		if(snapWhileIncrease && lastValue < currentValue && stableValue < lastValue)stableValue = lastValue;
		
		if(lastValue - currentValue < allowedDeviation)equalizedTime += delta;
		else equalizedTime = 0;
		
		if(blinkable && Mathf.equal(currentValue, Mathf.FLOAT_ROUNDING_ERROR) && !blinked)blink();
		
		if(isStable()){
			stableValue = Mathf.lerpDelta(stableValue, lastValue, approachSpeedScl_Stable);
		}
		
		lastValue = Mathf.lerp(lastValue, currentValue, approachSpeedScl_Real);
		
		blink = Mathf.lerp(blink, 0f, 0.075f);
		if(!blinkable || (blink < 0.01f && lastValue > 0.1f && currentValue > 0.01f))blinked = false;
	}
	
	public void blink(){
		if(!blinkable)return;
		blink = 1;
		blinked = true;
	}
	
	@Override
	public void draw(){
		super.draw();
		
		if(drawBackground){
			Draw.color(backgroundColor);
			Draw.alpha(parentAlpha * color.a * backgroundColor.a);
			Fill.quad(
				x, y,
				x + width, y,
				x + width, y + height,
				x, y + height
			);
		}
		
		Color color1 = colorStable.get();
		float c2 = Tmp.c2.set(color1).lerp(Color.black, 0.65f).a(parentAlpha * color.a).toFloatBits();
		float c1 = Tmp.c1.set(color1).lerp(Color.white, 0.1f).lerp(Tmp.c2, 1 - getStableFraction()).a(parentAlpha * color.a).toFloatBits();
		Fill.quad(
			x + barMargin, y + barMargin, c2,
			x - barMargin + width * getStableFraction(), y + barMargin, c1,
			x - barMargin + width * getStableFraction(), y - barMargin + height, c1,
			x + barMargin, y - barMargin + height, c2
		);
		
		color1 = colorReal.get();
		c2 = Tmp.c2.set(color1).lerp(rootColor, 0.65f).a(parentAlpha * color.a).toFloatBits();
		c1 = Tmp.c1.set(color1).lerp(Color.white, 0.1f).lerp(Tmp.c2, 1 - getFraction()).a(parentAlpha * color.a).toFloatBits();
		Fill.quad(
			x + (barMargin + realStableMargin), y + (barMargin + realStableMargin), c2,
			x - (barMargin + realStableMargin) + width * getFraction(), y + (barMargin + realStableMargin), c1,
			x - (barMargin + realStableMargin) + width * getFraction(), y - (barMargin + realStableMargin) + height, c1,
			x + (barMargin + realStableMargin), y - (barMargin + realStableMargin) + height, c2
		);
		
		if(blink > 0.001f){
			Draw.color(blinkColor);
			Draw.blend(Blending.additive);
			Draw.alpha(blink * parentAlpha * color.a);
			float w = width * (NHInterp.parabola4Reversed.apply(blink * 0.95f) / 2.5f + 1);
			Fill.quad(
					x, y,
					x + w, y,
					x + w, y + height,
					x, y + height
			);
			Draw.blend();
		}
		
//
		Font font = Fonts.outline;
		GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
		
		lay.setText(font, info.get());
		
		font.setColor(1f, 1f, 1f, 1f);
		font.getCache().clear();
		
		float scl = fontScale.get(this);
		
		font.getData().setScale(scl);
		
		font.getCache().addText(info.get(), x + width * 0.035f, y + height * 0.8125f);
		
		if(lay.width > width / scaleX)fontAlpha = Mathf.lerp(fontAlpha, 0, 0.2f);
		else fontAlpha = Mathf.lerp(fontAlpha, 1, 0.05f);
		
		font.getCache().draw(parentAlpha * color.a * fontAlpha);
		font.getData().setScale(1.0F);
//		Bar
		Pools.free(lay);
		
	}
}
