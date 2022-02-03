package newhorizon.util.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.scene.actions.Actions;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import newhorizon.content.NHColor;
import newhorizon.content.NHSounds;
import newhorizon.util.feature.cutscene.UIActions;

import static mindustry.Vars.state;

public class ScreenInterferencer{
	private static final float coolDown = 30 * Time.toSeconds;
	private static float hackLifetime = 0f;
	private static float hackRemainTime = 0f, reloadTime = 0;
	private static float hackWarmup = 0f;
	private static final Rand rand = new Rand();
	private static long seed = 0;
	
	
	public static final Color from = Pal.heal, to = NHColor.lightSkyBack;
	
	private static long getSeed(){return seed++;}
	
	private static final Label.LabelStyle style = new Label.LabelStyle(Fonts.tech, Color.white){{background = Tex.clear;}};
	
	private static Table hackShowTable;
	
	
	public static void load(){
		hackShowTable = new Table(Tex.clear){
			{
				setSize(UIActions.width_UTD, UIActions.height_UTD);
				
				table(Tex.pane, table -> {
					table.table(t -> {
						Label label = new Label(">> [red]IMAGER OVERLOAD[] <<");
						t.update(() -> {
							int i = (int)(Time.time / 12 % 4);
							switch(i){
								case 0 : {
									label.setText("    [red]IMAGER OVERLOAD[]    ");
									break;
								}
								case 1 : {
									label.setText("  > [red]IMAGER OVERLOAD[] <  ");
									break;
								}
								case 2 : {
									label.setText(" >> [red]IMAGER OVERLOAD[] << ");
									break;
								}
								case 3 : {
									label.setText(">>> [red]IMAGER OVERLOAD[] <<<");
									break;
								}
							}
						});
						label.setStyle(style);
						table.add(label).row();
					}).center().fill().row();
				}).growX().fillY().center();
				
				update(() -> {
					setPosition(0, 0);
					setSize(UIActions.width_UTD, UIActions.height_UTD);
					
					if(state.isMenu()){
						hackWarmup = hackRemainTime = 0;
						remove();
					}
					if(!state.isPaused() && !state.isMenu()){
						hackRemainTime = Mathf.approachDelta(hackRemainTime, 0, 1);
					}
					
					if(hackRemainTime > 0){
						hackWarmup = Mathf.approachDelta(hackWarmup, 1, 0.1f);
					}else hackWarmup = Mathf.approachDelta(hackWarmup, 0, 0.02f);
					
					if(Mathf.zero(hackWarmup) && hackLifetime > 0){
						actions(Actions.fadeOut(0.25f, Interp.fade), Actions.remove());
						hackLifetime = 0;
					}
				});
				visible(() -> !state.isMenu());
			}
			
			@Override
			public void draw(){
				super.draw();
				
				float particleLen = Core.graphics.getWidth() / 8f;
				float stroke = Core.graphics.getHeight() / 40f;
				float life = Core.graphics.getWidth() / 10f;
				Lines.stroke(stroke * hackWarmup);
				
				float base = (Time.time / life);
				rand.setSeed(seed);
				
				Draw.color(from, Color.white, to, Mathf.absin(8f, 1f));
				Draw.alpha((0.3f + Mathf.absin(12f, 0.2f)) * Mathf.curve((hackRemainTime / hackLifetime + hackWarmup) / 2f, 0, 0.2f));
//				Draw.getColor().lerp(Color.white, rand.random(0.7f, 0.9f) * Mathf.curve(hackRemainTime / hackLifetime, 0, 0.2f));
				Fill.rect(width / 2, height / 2, width, height);
				
				Draw.blend(Blending.additive);
				
				for(int i = -40; i < 40; i++){
					for(int j = -15; j < 15; j++){
						float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
						int angle = Mathf.sign(rand.range(1));
						float len = Core.graphics.getWidth() / 35f * Interp.pow2Out.apply(fin);
						Draw.color(
								Tmp.c1.set(from).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.4f, 0.8f) * Mathf.curve(fout, 0.8f, 1f)),
								Tmp.c2.set(to).mul(rand.random(0.9f, 1.1f)).lerp(Color.white, rand.random(0.4f, 0.8f) * Mathf.curve(fout, 0.8f, 1f)),
								rand.random(0f, 1f)
						);
						
						Lines.lineAngle(width / 2 + rand.range(0.5f) * particleLen + j / 10f * width * fin, height / 2 + rand.range(0.5f) * stroke + i * height / 60, angle * 90 + 90, particleLen * (fout * 0.4f + 0.6f) * hackWarmup);
					}
				}
				
				Draw.blend();
				Draw.color(Pal.redderDust);
				Lines.lineAngleCenter(width / 2, height / 2, 0, width * 1.35f * Mathf.clamp(hackRemainTime / hackLifetime));
				
				super.draw();
			}
		};
	}
	
	public static void update(){
		if(Vars.headless)return;
		if(state.isMenu())reloadTime = 0;
		if(state.isPaused())return;
		reloadTime -= Time.delta;
		
		if(UIActions.lockingInput())reloadTime = Mathf.lerpDelta(reloadTime, 0, 0.1f);
	}
	
	public static void generate(float time){
		if(Vars.headless)return;
		if(reloadTime > 0)return;
		hackRemainTime = hackLifetime = time;
		
		NHSounds.alarm.play();
		
		hackShowTable.actions(Actions.fadeIn(1f));
		hackShowTable.setPosition(0, 0);
		
		UIActions.root().addChildAt(0, hackShowTable);
		
		reloadTime = Math.max(coolDown, time * 2);
		
		Events.fire(ScreenInterferencer.ScreenHackEvent.class, new ScreenInterferencer.ScreenHackEvent(time));
	}
	
	public static void continueGenerate(){
		if(Vars.headless)return;
		hackRemainTime = hackLifetime = 30f;
		
		hackShowTable.actions(Actions.fadeIn(1f));
		hackShowTable.setPosition(0, 0);
		
		if(reloadTime <= 0)UIActions.root().addChildAt(0, hackShowTable);
	}
	
	public static class ScreenHackEvent{
		public final float time;
		
		public ScreenHackEvent(float time){
			this.time = time;
		}
	}
}
