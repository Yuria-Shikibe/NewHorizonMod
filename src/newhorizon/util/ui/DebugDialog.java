package newhorizon.util.ui;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.geom.Vec2;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Weather;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NewHorizon;
import newhorizon.content.NHSounds;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.vars.TileSortMap;
import newhorizon.util.feature.InternalTools;
import newhorizon.util.feature.ScreenInterferencer;
import newhorizon.util.feature.WarpUnit;
import newhorizon.util.func.NHInterp;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.func.NHSetting;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class DebugDialog extends BaseDialog{
	public DebugDialog(String title){
		this(title, Core.scene.getStyle(Dialog.DialogStyle.class));
		
		cont.pane(t -> {
			t.defaults().size(LEN * 3, LEN).pad(OFFSET / 2).style(Styles.transt);
			
			t.button("Icons", () -> new BaseDialog("Icons"){{
				Class<?> c = Icon.class;
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("TextureRegionDrawable")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									try{
										inner.image((Drawable)f.get(null)).pad(OFFSET / 3);
									}catch(IllegalAccessException err){
										throw new IllegalArgumentException(err);
									}
									inner.add(f.getName());
								}).size(LEN * 3, LEN).pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
				
				addCloseListener();
			}}.show());
			
			t.button("TableTexes", () -> new BaseDialog("TableTexes"){{
				Class<?> c = Tex.class;
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(Drawable.class.isAssignableFrom(f.getType())){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									try{
										inner.table((Drawable)f.get(null), de -> de.add(f.getName())).size(LEN * 3, LEN).pad(OFFSET / 3);
									}catch(IllegalAccessException err){
										throw new IllegalArgumentException(err);
									}
								}).size(LEN * 3, LEN).pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
				addCloseListener();
			}}.show());
			
			t.button("ButtonTexts", () -> new BaseDialog("ButtonTexts"){{
				Class<?> c = Styles.class;
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("TextButtonStyle")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									try{
										inner.button(f.getName(), (TextButton.TextButtonStyle)f.get(null), () -> {}).size(LEN * 3, LEN).row();
										inner.button(f.getName(), (TextButton.TextButtonStyle)f.get(null), () -> {}).size(LEN * 3, LEN).pad(OFFSET / 3).disabled(b -> true).row();
									}catch(IllegalAccessException err){
										throw new IllegalArgumentException(err);
									}
								}).grow().pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
				addCloseListener();
			}}.show());
			
			t.button("ButtonImages", () -> new BaseDialog("ButtonImages"){{
				
				Class<?> c = Styles.class;
				
				Field[] fields = c.getFields();
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(f.getType().getSimpleName().equals("ImageButtonStyle")){
								if(index % 6 == 0) t.row();
								t.table(inner -> {
									inner.table(de ->{
										try{
											de.button(Icon.none, (ImageButton.ImageButtonStyle)f.get(null), () -> {}).size(LEN * 3, LEN).row();
										}catch(IllegalAccessException err){
											throw new IllegalArgumentException(err);
										}
										de.add(f.getName()).pad(OFFSET / 3);
									}).row();
									inner.table(de ->{
										try{
											de.button(Icon.none, (ImageButton.ImageButtonStyle)f.get(null), () -> {}).disabled(b -> true).size(LEN * 3, LEN).row();
										}catch(IllegalAccessException err){
											throw new IllegalArgumentException(err);
										}
										de.add(f.getName()).pad(OFFSET / 3);
									}).row();
								}).grow().pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
				addCloseListener();
			}}.show());
			
			t.button("Unit Icons", () -> new BaseDialog("Units"){{
				cont.pane(table -> {
					AtomicInteger index = new AtomicInteger();
					
					content.units().each( (unit) -> {
						if(!unit.isHidden()){
							if(index.get() % 8 == 0) table.row();
							table.table(Tex.buttonEdge3, t -> {
								t.button(new TextureRegionDrawable(unit.fullIcon), Styles.cleari,LEN * 3,() -> {
									BaseDialog d = new BaseDialog("info"){{
										t.image(unit.fullIcon);
									}};
									d.addCloseListener();
									d.show();
								}).grow().row();
								t.pane(in -> in.add(unit.localizedName).height(LEN).fillX()).height(LEN).growX();
							});
							index.getAndIncrement();
						}
					});
				}).fill();
				addCloseListener();
			}}.show());
			
			t.row();
			
			t.button("Settings", () -> new NHSetting.SettingDialog().show());
			
			t.button("Interps", () -> {
				BaseDialog dialog = new BaseDialog("Interpolation", Styles.fullDialog);
				dialog.setFillParent(true);
				dialog.cont.pane(t1 -> {
					setFillParent(true);
					float unitLength = 2f;
					float offset = 70 * unitLength;
					float len = 100;
					float sigs = 100;
					Seq<Field> fields = new Seq<>();
					fields.addAll(Interp.class.getFields());
					fields.addAll(NHInterp.class.getFields());
					int i = 0;
					for(Field field : fields){
						if(Interp.class.isAssignableFrom(field.getType())){
							if(i++ % 5 == 0)t1.row();
							try{
								Interp interp = (Interp)field.get(null);
								
								Table table = new Table(Tex.pane){{
									fill(Tex.clear, inner -> {
										Label l = inner.add(field.getName()).color(Color.white).align(Align.topLeft).get();
										l.getStyle().background = Styles.black3;
									});
								}
									@Override
									public void draw(){
										background(Tex.pane);
										super.draw();
										
										Lines.stroke(unitLength);
										Draw.color(Color.gray);
										Lines.line(x, y + offset, x + width, y + offset, false);
										Lines.line(x + offset, y, x + offset, y + height, false);
										Drawf.arrow(x, y + offset, x + width, y + offset, width - unitLength * 5.75f, unitLength * 5, Pal.gray);
										Drawf.arrow(x + offset, y, x + offset, y + height, height - unitLength * 5.75f, unitLength * 5, Pal.gray);
										
										Draw.color(Color.gray);
										Lines.line(x + offset, y + offset + unitLength * len, x + offset + unitLength * len, y + offset + unitLength * len, false);
										Lines.line(x + offset + unitLength * len, y + offset, x + offset + unitLength * len, y + offset + unitLength * len, false);
										
										Fill.square(x + offset + unitLength * len, y + offset + unitLength * len, unitLength * 3, 45);
										Draw.color(Pal.accent);
										
										Lines.beginLine();
										
										for(float i = 0; i < len; i += len / sigs){
											Lines.linePoint(x + i * unitLength + offset, y + interp.apply(i / sigs) * len * unitLength + offset);
										}
										
										Lines.endLine(false);
										Draw.reset();
										
										background(Tex.clear);
										super.draw();
									}
								};
								
								t1.table(table1 -> {
									table1.add(table).size(len * unitLength + offset * 2).pad(OFFSET / 2).row();
									//								table1.pane(in -> in.add(field.getName()).fill()).growX().height(LEN / 2 + OFFSET);
								});
							}catch(IllegalAccessException ignored){}
						}
					}
				}).grow();
				dialog.addCloseButton();
				dialog.show();
			});
			
			t.button("Sounds", () -> new BaseDialog("ICONS"){{
				addCloseButton();
				
				Class<?> c = Sounds.class;
				Seq<Field> fields = Seq.with(Sounds.class.getFields()).and(NHSounds.class.getFields());
				
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(Sound.class.isAssignableFrom(f.getType())){
								if(index % 6 == 0) t.row();
								t.table(inner -> inner.table(Tex.pane, de -> {
									de.margin(6f);
									de.add(f.getName()).growX();
									de.button(Icon.play, Styles.clearPartiali, () -> {
										try{
											((Sound)f.get(null)).play();
										}catch(IllegalAccessException e){
											e.printStackTrace();
										}
									}).growY().scaling(Scaling.fit);
								}).size(LEN * 3, LEN).pad(OFFSET / 3)).size(LEN * 3, LEN).pad(OFFSET / 3);
								index++;
							}
						}catch(IllegalArgumentException err){
							throw new IllegalArgumentException(err);
						}
					}
				}).grow();
			}}.show());
			
			t.button("Weathers", () -> {
				BaseDialog dialog = new BaseDialog("");
				dialog.cont.pane(t1 -> {
					t1.image().growX().height(OFFSET / 4).pad(OFFSET / 2).color(Pal.accent).row();
					t1.pane(table -> {
						for(Content tent : content.getBy(ContentType.weather)){
							Weather c = (Weather)tent;
							table.button(c.localizedName, () -> Groups.weather.add(c.create(5f))).growX().fillY().row();
						}
					}).grow().row();
					t1.image().growX().height(OFFSET / 4).pad(OFFSET / 2).color(Pal.accent).row();
					t1.button("Remove", () -> Groups.weather.clear()).growX().height(LEN);
				}).grow();
				dialog.addCloseButton();
				dialog.show();
			});
			
			t.row();
			
			t.button("Unlock ALL", () -> {
				for(UnlockableContent tent : content.items()){
					tent.unlock();
				}
				for(UnlockableContent tent : content.liquids()){
					tent.unlock();
				}
				for(UnlockableContent tent : content.units()){
					tent.unlock();
				}
				for(UnlockableContent tent : content.blocks()){
					tent.unlock();
				}
				for(UnlockableContent tent : content.sectors()){
					tent.unlock();
				}
				for(UnlockableContent tent : content.statusEffects()){
					tent.unlock();
				}
			});
			
			t.button("Unlock Single", () -> new BaseDialog("UNLOCK"){{
				Seq<UnlockableContent> all = new Seq<>().addAll(content.units().select(c -> !c.isHidden())).addAll(content.blocks().select(c -> !c.isHidden())).addAll(content.items().select(c -> !c.isHidden())).addAll(content.liquids()).addAll(content.statusEffects().select(c -> !c.isHidden())).addAll(content.sectors().select(c -> !c.isHidden())).addAll(content.planets().select(c -> !c.isHidden())).as();
				
				cont.pane(t -> {
					for(int i = 0; i < all.size; i++){
						if(i % 4 == 0)t.row();
						
						UnlockableContent c = all.get(i);
						t.table(Tex.pane, table -> {
							table.image(c.fullIcon).size(LEN / 2);
							table.add(c.localizedName).padLeft(OFFSET / 2);
							table.button(Icon.lock, Styles.clearPartiali, () -> {
								if(c.unlocked())c.clearUnlock();
								else c.unlock();
							}).size(LEN / 2).update(b -> {
								if(c.unlocked())b.getStyle().imageUp = Icon.lockOpen;
								else b.getStyle().imageUp = Icon.lock;
							});
						}).fill().pad(OFFSET / 4);
					}
				}).grow();
				
				addCloseButton();
			}}.show());
			
			t.row();
			
			t.button("Hack", () -> ScreenInterferencer.generate(360));
			
			t.row();
			
			t.button("Generate Icon", () -> ui.loadAnd("[accent]Generating", () -> {
				content.units().each(u -> {
					if(u.name.contains(NewHorizon.MOD_NAME)){
						NHPixmap.saveUnitPixmap(Core.atlas.getPixmap(u.fullIcon).crop(), u);
					}
				});
				
				NHPixmap.saveAddProcessed();
			})).disabled(b -> !NHPixmap.isDebugging());
			
			t.button("Fire Tool", InternalTools::fireAnime);
			
			t.button("Bundle Tool", InternalTools::patchBundle);
			
			t.row();
			
			t.button("+10 Wave", () -> state.wave += 10);
			
			t.button("-10 Wave", () -> state.wave = Math.max(0, state.wave - 10));
			
			t.row();
			
			t.button("Warp", () -> Groups.unit.each(u -> WarpUnit.warp(u, 45)));
			
			t.button("Hyper Warp", () -> Groups.unit.each(u -> HyperSpaceWarper.Carrier.create(u, new Vec2().set(player))));
			
			t.row();
			
			t.button("Update Sort Map", () -> TileSortMap.registerTeam(Team.purple));
			
			t.button("Analyses Sort Map", () -> TileSortMap.getTeamMap(Team.purple).analysis());
			
			t.button("Show Sort Map", () -> TileSortMap.getTeamMap(Team.purple).showAsDialog(TileSortMap.ValueCalculator.healthSqrt));
			
			t.row();
			
			t.button("Add Bars", UnitInfo::addBars);
		}).grow();
		
		addCloseButton();
	}
	
	public DebugDialog(String title, Dialog.DialogStyle style) {
		super(title, style);
	}
}































