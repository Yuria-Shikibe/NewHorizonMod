package newhorizon.util.feature.cutscene.actions;

import arc.Core;
import arc.func.Cons4;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.actions.TemporalAction;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import newhorizon.content.NHContent;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;

public class CautionAction extends TemporalAction{
	protected static final Color tmpColor = new Color();
	protected static final Vec2 tmpVec = new Vec2(), tmpVec2 = new Vec2();
	
	public float x, y, size;
	public Color color;
	public Table drawer;
	public MarkStyles style = MarkStyles.defaultStyle;
	
	@Override
	protected void update(float percent){
	
	}
	
	@Override
	protected void begin(){
		if(!UIActions.disabled())drawer = new Table(Tex.pane){
			{
				UIActions.root().addChildAt(0, this);
				
				update(() -> {
					if(Vars.state.isMenu()) remove();
				});
				table().grow();
				
				color.a = 0;
				actions(Actions.alpha(1, 0.45f, NHInterp.bounce5Out));
			}
			
			@Override
			public void draw(){
				float width = UIActions.width_UTD, height = UIActions.height_UTD;
				
				Vec2 screenVec = Core.camera.project(tmpVec.set(CautionAction.this.x, CautionAction.this.y));
				
				boolean outer = screenVec.x < width * 0.05f || screenVec.y < height * 0.05f || screenVec.x > width * 0.95f || screenVec.y > height * 0.95f;
				
				if(outer)screenVec.clamp(width * 0.05f, height * 0.05f, height * 0.95f, width * 0.95f);
				
				tmpColor.set(CautionAction.this.color).lerp(Color.white, Mathf.absin(getTime() * 60f, 5f, 0.4f)).a(color.a);
				
				style.drawer.get(screenVec, tmpColor, outer, CautionAction.this);
			}
		};
	}
	
	@Override
	protected void end(){
		if(!UIActions.disabled())drawer.actions(Actions.fadeOut(1f), Actions.remove());
	}
	
	public enum MarkStyles{
		defaultStyle((pos, color, beyond, action) -> {
			Tmp.c2.set(Pal.gray).a(action.drawer.color.a);
			
			float size = action.size * Vars.renderer.getDisplayScale();
			float width = UIActions.width_UTD, height = UIActions.height_UTD;
			
			float rotationS = 45 + 90 * NHInterp.pow10.apply((action.getTime() * 4 / action.getDuration()) % 1);
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
		
		fixed((pos, color, beyond, action) -> {
			Tmp.c2.set(Pal.gray).a(action.drawer.color.a);
			
			float size = action.size  * Vars.renderer.getDisplayScale();
			float width = UIActions.width_UTD, height = UIActions.height_UTD;
			
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
		
		shake((pos, color, beyond, action) -> {
			Tmp.c2.set(Pal.gray).a(action.drawer.color.a);
			
			Rand rand = NHFunc.rand;
			rand.setSeed((long)(Time.time / 8) + Float.floatToIntBits(action.x) << 8 + Float.floatToIntBits(action.y));
			
			Vec2 v = pos.cpy().add(rand.range(12), rand.range(12));
			float size = action.size  * Vars.renderer.getDisplayScale();
			float width = UIActions.width_UTD, height = UIActions.height_UTD;
			
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
		
		public Cons4<Vec2, Color, Boolean, CautionAction> drawer;
		
		MarkStyles(Cons4<Vec2, Color, Boolean, CautionAction> drawer){
			this.drawer = drawer;
		}
	}
}
