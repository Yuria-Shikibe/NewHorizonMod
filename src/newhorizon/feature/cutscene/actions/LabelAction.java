package newhorizon.feature.cutscene.actions;

import arc.Core;
import arc.func.Cons;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.actions.Actions;
import arc.scene.actions.TemporalAction;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import newhorizon.feature.cutscene.UIActions;
import newhorizon.func.NHInterp;

import static mindustry.Vars.state;
import static newhorizon.ui.TableFunc.OFFSET;

/**
 * Used to make a pop-up dialog with texts extending out. Use {@code "@@@"} to split the text. The text before it will
 * always be shown, while the latter will have a fade in effect.
 */
public class LabelAction extends TemporalAction{
	public float margin = 0;
	public String text;
	public Label label;
	public Table table;
	public Cons<Table> modifier = null;
	
	public Interp inFunc = NHInterp.bounce5Out;
	
	@Override
	protected void begin(){
		Sounds.press.play(10);
		
		label = new Label("");
		label.setWrap(true);
		
		table = new Table(Tex.buttonEdge3){{
			UIActions.root().addChildAt(2, this);
			color.a = 0;
			
			if(Vars.mobile){
				setSize(Core.graphics.getWidth() / 1.25f, Core.graphics.getHeight() / 3f);
				setPosition(0, 0);
			}else{
				setSize(Core.graphics.getWidth() / 2f, UIActions.yAxis() / 2);
				x = (Core.graphics.getWidth() - width) / 2f;
				y = UIActions.yAxis() * 1.15f;
			}
			
			update(() -> {
				if(state.isMenu()) remove();
				if(Vars.mobile){
					setSize(Core.graphics.getWidth() / 1.25f, Core.graphics.getHeight() / 3f);
					setPosition(0, 0);
				}else{
					setSize(Core.graphics.getWidth() / 2f, UIActions.yAxis() / 2);
					x = (Core.graphics.getWidth() - width) / 2f;
					y = UIActions.yAxis() * 1.15f;
				}
			});
			
			table(inner -> {
				if(Vars.mobile){
					inner.table(table1 -> {
						table1.left();
						if(modifier != null) modifier.get(table1);
					}).fillY().growX().row();
					inner.pane(t -> {
						t.add(label).left().top().growX().fillY().pad(OFFSET).padRight(OFFSET).row();
						t.table(c -> {}).grow();
					}).grow().top();
				}else{
					if(modifier != null)modifier.get(inner);
					inner.add(label).center().grow();
				}
			}).grow().pad(OFFSET);
		}};
		
		table.actions(Actions.alpha(1, 0.45f, inFunc));
	}
	
	@Override
	protected void end(){
		table.actions(Actions.fadeOut(0.45f), Actions.remove());
	}
	
	@Override
	protected void update(float percent){
		String[] s = text.split("@@@");
		String speaker = s[0];
		StringBuilder saying = new StringBuilder();
		for(int i = 1; i < s.length; i++) saying.append(s[i]);
		
		label.setText(speaker + saying.substring(0, (int)(saying.length() * Mathf.curve(percent, 0, margin))));
	}
}
