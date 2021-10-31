package newhorizon.feature;

import arc.Core;
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
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import newhorizon.content.NHColor;
import newhorizon.content.NHSounds;

import static mindustry.Vars.state;

public class ScreenHack{
	private static final float coolDown = 30 * Time.toSeconds;
	private static float hackLifetime = 0f;
	private static float hackRemainTime = 0f;
	private static float hackWarmup = 0f;
	private static final Rand rand = new Rand();
	private static int seed = 0;
	
	private static final ObjectMap<String, Float> reloadMap = new ObjectMap<>();
	
	private static final Color from = Pal.heal, to = NHColor.lightSkyBack;
	
	private static int getSeed(){return seed++;}
	
	private static final Label.LabelStyle style = new Label.LabelStyle(Fonts.tech, Color.white){{background = Tex.clear;}};
	
	private static Table hackShowTable;
	
	public static void load(){
		hackShowTable = new Table(Tex.clear){
			{
				setSize(Core.graphics.getWidth(), Core.graphics.getHeight());
				
				table(Tex.pane, table -> {
					table.table(t -> {
						Label label = new Label(">> [red]System Overridden[] <<");
						t.update(() -> {
							int i = (int)(Time.time / 12 % 4);
							switch(i){
								case 0 : {
									label.setText("    [red]SYSTEM OVERLOAD[]    ");
									break;
								}
								case 1 : {
									label.setText("  > [red]SYSTEM OVERLOAD[] <  ");
									break;
								}
								case 2 : {
									label.setText(" >> [red]SYSTEM OVERLOAD[] << ");
									break;
								}
								case 3 : {
									label.setText(">>> [red]SYSTEM OVERLOAD[] <<<");
									break;
								}
							}
						});
						label.setStyle(style);
						table.add(label).row();
					}).center().fill().row();
				}).growX().fillY().center();
				
				update(() -> {
					if(state.isMenu()){
						hackWarmup = hackRemainTime = 0;
						remove();
						setPosition(0, 0);
						setSize(Core.graphics.getWidth(), Core.graphics.getHeight());
					}
					if(!state.isPaused() && !state.isMenu())hackRemainTime = Mathf.approachDelta(hackRemainTime, 0, 1);
					
					if(hackRemainTime > 0){
						hackWarmup = Mathf.approachDelta(hackWarmup, 1, 0.1f);
					}else hackWarmup = Mathf.approachDelta(hackWarmup, 0, 0.02f);
					
					if(Mathf.zero(hackWarmup) && hackLifetime > 0){
						actions(Actions.fadeOut(1f, Interp.fade), Actions.remove());
						hackLifetime = 0;
					}
				});
				visible(() -> !state.isMenu());
			}
			
			@Override
			public void draw(){
				float particleLen = Core.graphics.getWidth() / 8f;
				float stroke = Core.graphics.getHeight() / 40f;
				float life = Core.graphics.getWidth() / 10f;
				Lines.stroke(stroke * hackWarmup);
				
				float base = (Time.time / life);
				rand.setSeed(seed);
				
				Draw.color(from, Color.white, to, Mathf.absin(8f, 1f));
				Draw.alpha((0.3f + Mathf.absin(12f, 0.2f)) * Mathf.curve((hackRemainTime / hackLifetime + hackWarmup) / 2f, 0, 0.2f));
				Draw.getColor().lerp(Color.white, rand.random(0.7f, 0.9f) * Mathf.curve(hackRemainTime / hackLifetime, 0, 0.2f));
				Fill.rect(width / 2, height / 2, width, height);
				
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
				
				Draw.color(Pal.redderDust);
				Lines.lineAngleCenter(width / 2, height / 2, 0, width * Mathf.clamp(hackRemainTime / hackLifetime));
				
				super.draw();
			}
		};
	}
	
	public static void update(){
		if(state.isMenu())reloadMap.clear();
		if(state.isPaused())return;
		reloadMap.each((key, f) -> {
			if(f < 0)reloadMap.remove(key);
			else reloadMap.put(key, f - Time.delta);
		});
	}
	
	public static void generate(Player player, float time){
		if(!player.equals(Vars.player) || reloadMap.containsKey(player.name()))return;
		hackRemainTime = hackLifetime = time;
		
		NHSounds.alarm.play();
		
		hackShowTable.actions(Actions.fadeIn(1f));
		hackShowTable.setPosition(0, 0);
		
		Core.scene.root.addChildAt(1, hackShowTable);
		
		reloadMap.put(player.name(), Math.max(coolDown, time * 2));
	}
	
	public static class ScreenHackEvent{
		public final Player target;
		public final float time;
		
		public ScreenHackEvent(Player target, float time){
			this.target = target;
			this.time = time;
		}
	}
}
