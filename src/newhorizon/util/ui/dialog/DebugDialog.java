package newhorizon.util.ui.dialog;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import mindustry.content.TechTree;
import mindustry.core.Logic;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.type.Weather;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NHGroups;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHInbuiltEvents;
import newhorizon.content.NHSounds;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.cutscene.actions.CSSActions;
import newhorizon.util.Tool_Internal;
import newhorizon.util.func.NHInterp;
import newhorizon.util.func.NHPixmap;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class DebugDialog extends BaseDialog{
	public DebugDialog(String title){
		this(title, Core.scene.getStyle(Dialog.DialogStyle.class));
		
		cont.pane(t -> {
			t.defaults().size(LEN * 3, LEN).pad(OFFSET / 2).style(Styles.cleart);
			
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
					t.background(Tex.selection);
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
					t.background(Tex.selection);
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
										cont.image(unit.fullIcon).scaling(Scaling.fit);
									}};
									d.addCloseButton();
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
			
			t.button("Interps", () -> {
				BaseDialog dialog = new BaseDialog("Interpolation", Styles.fullDialog);
				dialog.setFillParent(true);
				dialog.cont.pane(t1 -> {
					setFillParent(true);
					float unitLength = 2f;
					float offset = 50 * unitLength;
					float len = 100;
					float sigs = 100;
					Seq<Field> fields = new Seq<>();
					fields.addAll(Interp.class.getFields());
					fields.addAll(NHInterp.class.getFields());
					int i = 0;
					for(Field field : fields){
						if(Interp.class.isAssignableFrom(field.getType())){
							if(i++ % 4 == 0)t1.row();
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
										
										for(float i = 0; i <= len; i += len / sigs){
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
			
			t.button("Status", () -> new BaseDialog("Status"){{
				addCloseButton();
				
				Seq<StatusEffect> seq = content.statusEffects();
				
				cont.pane(t -> {
					int index = 0;
					for(StatusEffect s : seq){
						if(index % 6 == 0) t.row();
						t.table(inner -> inner.table(Tex.pane, de -> {
							de.margin(6f);
							de.image(s.fullIcon).size(60f);
							de.button(Icon.play, Styles.cleari, () -> {
								player.unit().apply(s, 10 * Time.toSeconds);
							}).growY().scaling(Scaling.fit);
						}).size(LEN * 3, LEN).pad(OFFSET / 3)).size(LEN * 3, LEN).pad(OFFSET / 3);
						index++;
					}
				}).grow();
			}}.show());
			
			t.button("Sounds", () -> new BaseDialog("ICONS"){{
				addCloseButton();
				
				Class<?> c = Sounds.class;
				Seq<Field> fields = Seq.with(Sounds.class.getFields()).addAll(NHSounds.class.getFields());
				
				cont.pane(t -> {
					int index = 0;
					for(Field f : fields){
						try{
							if(Sound.class.isAssignableFrom(f.getType())){
								if(index % 6 == 0) t.row();
								t.table(inner -> inner.table(Tex.pane, de -> {
									de.margin(6f);
									de.add(f.getName()).growX();
									de.button(Icon.play, Styles.cleari, () -> {
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
			}
			
			}.show());
			
			t.button("Weathers", () -> {
				BaseDialog dialog = new BaseDialog("");
				dialog.cont.pane(t1 -> {
					t1.image().growX().height(OFFSET / 4).pad(OFFSET / 2).color(Pal.accent).row();
					t1.pane(table -> {
						for(Content content : content.getBy(ContentType.weather)){
							if(!(content instanceof Weather))continue;
							Weather c = (Weather)content;
							table.button(c.localizedName, new TextureRegionDrawable(c.fullIcon), () -> {
								float intensity = Mathf.random(2, 10);
								c.create(intensity, 600);
							}).growX().padTop(OFFSET / 2f).fillY().row();
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
				TechTree.all.each(c -> c.content.unlock());
			});
			
			t.button("Unlock Single", () -> new BaseDialog("UNLOCK"){{
				Seq<UnlockableContent> all = TechTree.all.map(n -> n.content);
				
				cont.pane(t -> {
					for(int i = 0; i < all.size; i++){
						if(i % 4 == 0)t.row();
						
						UnlockableContent c = all.get(i);
						t.table(Tex.pane, table -> {
							table.image(c.fullIcon).size(LEN / 2);
							table.add(c.localizedName).padLeft(OFFSET / 2);
							table.button(Icon.lock, Styles.cleari, () -> {
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
			
			t.button("Apply Default Waves", () -> {
				state.rules.spawns.clear();
				state.rules.spawns.addAll(waves.get());
			});
			
			t.button("Clear Fog", () -> {
				state.rules.fog = false;
			});
			
			t.button("Remove Limit", () -> {
				state.rules.limitMapArea = false;
			});
			
			
			t.button("Show UI Structure", () -> {
				new BaseDialog(""){{
					addCloseButton();
					
					cont.pane(t -> {
						t.add(ui.hudGroup.toString());
					}).grow();
					
				}}.show();
			});
			
			t.row();
			
			t.button("Generate Icon", () -> ui.loadAnd("[accent]Generating", () -> {
				content.units().each(u -> {
					if(u.name.contains(NewHorizon.MOD_NAME)){
						NHPixmap.saveUnitPixmap(Core.atlas.getPixmap(u.fullIcon).crop(), u);
					}
				});
				
				NHPixmap.saveAddProcessed();
			})).disabled(b -> !NHPixmap.isDebugging());
			
			t.button("Fire Tool", Tool_Internal::fireAnime);
			
			t.button("Texture Lerp Tool", Tool_Internal::textureLerp);
			
			t.button("Texture Pick Tool", Tool_Internal::texturePick);
			
			t.button("Bundle Tool", Tool_Internal::patchBundle);
			
			t.row();
			
			t.button("+10 Wave", () -> state.wave += 10);
			
			t.button("-10 Wave", () -> state.wave = Math.max(0, state.wave - 10));
			
			t.button("capture", Logic::sectorCapture).disabled(b -> !state.isCampaign());
			
			t.row();
			
			t.button("see Tex", () -> Tool_Internal.showTexture(NHContent.smoothNoise));
			
			t.button("move", () -> {
				CSSActions.beginCreateAction();
				
				float x = Mathf.random(0, world.unitWidth()), y = Mathf.random(0, world.unitHeight());
				
				NHCSS_Core.core.applySubBus(CSSActions.caution(x, y, 12, 300, Pal.accent));
				CSSActions.endCreateAction();
			});
			
			t.button("clear", () -> {
//				new CustomUIGen().show();
				Log.info(NHGroups.autoEventTrigger.size());
				
				NHGroups.autoEventTrigger.clear();
				
				Log.info(state.rules.tags.remove("setup-triggers"));
				Log.info(state.rules.tags.remove(NHInbuiltEvents.APPLY_KEY));
				Log.info(NHGroups.autoEventTrigger.size());
			});
			
			t.button("clear", () -> {
				Log.info("TRIGGERS: " + NHGroups.autoEventTrigger.size());
				Log.info("EVENTS: " + NHGroups.events.size());
				Time.run(120f, () -> {
					NHGroups.events.clear();
					NHGroups.autoEventTrigger.clear();
					Log.info("A TRIGGERS: " + NHGroups.autoEventTrigger.size());
					Log.info("A EVENTS: " + NHGroups.events.size());
				});
			});
			
			/*t.button("genT", () -> {
				new BaseDialog(""){{
					addCloseButton();
					
					cont.table(t -> {
						t.image(Draw.wrap(NHModCore.core.renderer.matterStorm));
					}).grow();
				}}.show();
			});*/
		}).grow();
		
		addCloseButton();
	}
	
	public DebugDialog(String title, Dialog.DialogStyle style) {
		super(title, style);
	}
}































