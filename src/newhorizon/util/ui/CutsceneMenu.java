package newhorizon.util.ui;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons2;
import arc.func.Func;
import arc.func.Intp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Time;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Displayable;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import newhorizon.util.feature.cutscene.CCS_JsonHandler;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneScript;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.feature.cutscene.events.util.AutoEventTrigger;
import newhorizon.util.func.OV_Pair;

import java.io.IOException;

import static mindustry.Vars.*;
import static newhorizon.util.feature.cutscene.CutsceneScript.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class CutsceneMenu extends BaseDialog{
	public ObjectMap<String, AutoEventTrigger> occasions = new ObjectMap<>();
	
	public String jsonToString;
	public Jval root; //JsonObject/Map
	public Jval triggers; //JsonArray
	public TextArea textArea;
	
	public CutsceneMenu(){
		super("@mod.ui.cutscene-menu");
		
		textArea = new TextArea("");
		
		initRoot();
		
		CCS_JsonHandler.generators(root).each((n, t) -> {
			occasions.put(n, t);
		});
		
		cont.pane(t -> {
			t.top();
			
			t.table(i -> {
				i.image().color(Color.lightGray).padRight(OFFSET).growX().height(OFFSET / 3);
				i.add("@mod.ui.normal-content").color(Color.lightGray);
				i.image().color(Color.lightGray).padLeft(OFFSET).growX().height(OFFSET / 3);
			}).pad(OFFSET).growX().fillY().center().row();
			
			t.table(c -> {
				c.pane(info -> {
					info.defaults().size(LEN * 3, LEN).padTop(OFFSET).align(Align.topLeft);
					
					info.button("@mod.ui.all-triggers", () -> {
						new BaseDialog(""){{
							addCloseButton();
							
							cont.pane(t -> {
								occasions.each((tName, trigger) -> {
									t.table(Tex.sideline, info -> {
										info.add(trigger.toString()).growX().fill().row();
										if(!CutsceneEvent.inValidEvent(trigger.eventType))info.table(Tex.button, show -> {
											trigger.eventType.display(show);
										}).growX().fillY().pad(OFFSET / 2f).row();
										info.defaults().growX().height(LEN).pad(OFFSET / 2f);
										info.button("@mod.ui.config-trigger", Icon.settings, () -> new ConfigDialog(tName, trigger, (name, o) -> {}).show()).row();
										info.button("@waves.remove", Icon.cancel, () -> ui.showConfirm("@mod.ui.trigger-delete.confirm", () -> {
											removeTrigger(tName);
											info.remove();
											updateJson();
										}));
									}).growX().fillY().pad(OFFSET).row();
								});
							}).grow();
							
						}}.show();
					}).disabled(b -> occasions.isEmpty()).row();
					
					info.button("@add", () -> {
						new ConfigDialog(null, new AutoEventTrigger(), (name, o) -> {}).show();
					}).row();
					
					info.button("@clear", () -> {
						ui.showConfirm("@mod.ui.trigger-delete-all.confirm", () -> {
							editor.tags.remove(CUSTOME_EVENTS_KEY);
							initRoot();
							updateJson();
						});
					}).row();
					
					info.button("@editor.export", () -> {
						platform.export(editor.tags.get("name"), "json", file -> file.writeString(jsonToString));
					}).row();
					
					info.button("@editor.import", () -> {
						platform.showMultiFileChooser(fi -> {
							root = Jval.read(fi.readString());
							triggers = CCS_JsonHandler.triggersJval(root);
							updateJson();
						}, "json", "hjson");
					}).row();
					
					info.button("@schematic.copy", () -> {
						Core.app.setClipboardText(jsonToString);
					}).row();
					
					info.button("@schematic.copy.import", () -> {
						String s = Core.app.getClipboardText();
						if(s != null && !s.isEmpty()){
							try{
								root = Jval.read(s);
								triggers = CCS_JsonHandler.triggersJval(root);
								updateJson();
							}catch(Exception e){
								ui.showException(e);
							}
						}
					}).row();
					
					info.button("@mod.ui.copy-import-single", () -> {
						String s = Core.app.getClipboardText();
						if(s != null && !s.isEmpty()){
							try{
								Jval jval = Jval.read(s);
								AutoEventTrigger tg = CCS_JsonHandler.readTrigger(jval);
								addTrigger(jval.get(CCS_JsonHandler.TRIGGER_NAME).asString(), tg);
								updateJson();
							}catch(Exception e){
								ui.showException(e);
							}
						}
					}).row();
				}).padLeft(LEN).growY().fillX();
				
				c.image().color(Color.darkGray).width(OFFSET / 4).growY().pad(OFFSET);
				
				c.pane(Styles.horizontalPane, jsonTable -> {
					jsonTable.background(Tex.sideline);
					jsonTable.marginLeft(LEN).align(Align.topLeft);
					jsonTable.label(() -> jsonToString).self(l -> l.get().setWrap(false)).fill().align(Align.left);
				}).self(p -> {
					p.get().setForceScroll(true, true);
				}).grow().maxWidth(Core.graphics.getWidth() - LEN * 7).padRight(LEN + OFFSET).padBottom(OFFSET / 3).marginBottom(OFFSET / 2).left();
			}).grow().height(LEN * 12).row();
			
			t.table(i -> {
				i.image().color(Color.lightGray).padRight(OFFSET).growX().height(OFFSET / 3);
				i.add("@mod.ui.advanced-content").color(Color.lightGray);
				i.image().color(Color.lightGray).padLeft(OFFSET).growX().height(OFFSET / 3);
			}).pad(OFFSET).growX().fillY().center().row();
			
			t.table(guide -> {
				if(!Vars.mobile)guide.marginLeft(LEN *3);
				guide.defaults().size(LEN * 6, LEN).padTop(OFFSET);
				
				guide.button("@link.mod.ccs.title", Icon.info, () -> {
					if(!Core.app.openURI(CCS_URL)){
						ui.showErrorMessage("@linkfail");
						Core.app.setClipboardText(CCS_URL);
					}
				}).marginLeft(OFFSET).row();
				guide.button("@mod.ui.package-scripts", Icon.download, () -> {
					platform.showMultiFileChooser(file -> {
						editor.tags.put(CUTSCENE_KEY, file.readString());
						ui.showInfo("[accent]" + Core.bundle.get("editor.saved"));
					}, "js");
				}).marginLeft(OFFSET).row();
				guide.button("@mod.ui.delete-scripts", Icon.trash, () -> {
					ui.showConfirm("Are you sure you want to delete it?", () -> {
						if(editor.tags.remove(CUTSCENE_KEY) != null){
							ui.showText("OPERATION STATE", "Delete Successfully");
						}else ui.showErrorMessage("Script is null");
					});
				}).marginLeft(OFFSET).disabled(b -> !editor.tags.containsKey(CUTSCENE_KEY)).row();
				guide.button("@mod.ui.read-scripts", Icon.bookOpen, () -> {
					new BaseDialog(""){{
						addCloseButton();
						
						cont.pane(t -> {
							Label rootScript = new Label(CutsceneScript.getModGlobalJSCode().trim());
							rootScript.setWrap(false);
							Label label = new Label(editor.tags.get(CUTSCENE_KEY).trim());
							label.setWrap(false);
							
							t.left();
							
							t.image().height(OFFSET / 3).growX().color(Pal.heal).padTop(OFFSET / 2).row();
							t.add("[heal]//Package Importer: ").pad(OFFSET / 2).row();
							t.image().height(OFFSET / 3).growX().color(Pal.heal).padBottom(OFFSET / 2).row();
							
							t.add(rootScript).color(Color.gray).growX().padLeft(LEN * 3).row();
							
							t.image().height(OFFSET / 3).growX().color(Pal.heal).padTop(OFFSET / 2).row();
							t.add("[heal]//Custom Cutscene Script: ").pad(OFFSET / 2).row();
							t.image().height(OFFSET / 3).growX().color(Pal.heal).padBottom(OFFSET / 2).row();
							
							t.add(label).growX().padLeft(LEN * 3);
							
						}).grow();
					}}.show();
				}).marginLeft(OFFSET).disabled(b -> !editor.tags.containsKey(CUTSCENE_KEY)).row();
				guide.button("@mod.ui.export-scripts", Icon.export, () -> {
					Fi fi = scriptDirectory.child(editor.tags.get("name") + "-cutscene.js");
					
					if(!fi.exists()){
						try{
							//noinspection ResultOfMethodCallIgnored
							fi.file().createNewFile();
						}catch(IOException ex){
							ui.showErrorMessage(ex.toString());
							return;
						}
					}
					
					fi.writeString(editor.tags.get(CUTSCENE_KEY).trim());
					
					ui.showText("OPERATION STATE", "Export Successfully");
				}).marginLeft(OFFSET).disabled(b -> !editor.tags.containsKey(CUTSCENE_KEY)).row();
				
				guide.left();
			}).growX().fillY().row();
			
			
		}).marginRight(UIActions.width_UTD / 10f * Mathf.num(!mobile)).marginLeft(UIActions.width_UTD / 10f * Mathf.num(!mobile)).grow().row();
		
		cont.table(t -> {
			t.defaults().growX().height(LEN).padTop(OFFSET).marginLeft(OFFSET).marginRight(OFFSET);
			t.button("@back", Icon.left, Styles.cleart, this::hide);
		}).growX();
		
		addCloseListener();
	}
	
	public void initRoot(){
		if(editor.tags.containsKey(CUSTOME_EVENTS_KEY)){
			root = Jval.read(editor.tags.get(CUSTOME_EVENTS_KEY));
			triggers = CCS_JsonHandler.triggersJval(root);
		}
		else{
			root = Jval.newObject();
			root.add(CCS_JsonHandler.KEY_TRIGGERS, (triggers = Jval.newArray()));
		}
		
		updateJson();
	}
	
	public void updateJson(){
		editor.tags.put(CUSTOME_EVENTS_KEY, root.toString());
		jsonToString = root.toString(Jval.Jformat.formatted);
	}
	
	@Override
	public void hide(){
		super.hide();
		
		editor.tags.put(CUSTOME_EVENTS_KEY, root.toString());
	}
	
	public class ConfigDialog extends BaseDialog{
		public AutoEventTrigger newTrigger;
		public String occasionName;
		public String constructor = "//[heal]Empty Constructor.[]\n//[heal]Please Apply JavaScript If You Want To Custom An Event."; //Js Code
		public boolean constructorError = false;
		public Table displayEventTable = new Table();
		
		public ObjectMap<UnlockableContent, Table> added = new ObjectMap<>();
		
		public void findAnd(UnlockableContent content, int value){
			if(content instanceof Item) newTrigger.items.find(s -> s.item == content).value = value;
			if(content instanceof UnitType) newTrigger.units.find(s -> s.item == content).value = value;
			if(content instanceof Block) newTrigger.buildings.find(s -> s.item == content).value = value;
		}
		
		public Table addTable(UnlockableContent content, Intp amount){
			Table table = new Table(){{
				margin(OFFSET / 3);
				defaults().padLeft(OFFSET);
				
				add(new IconNumDisplay.IconImage(content.uiIcon, amount)).size(LEN + OFFSET * 3, LEN - OFFSET);
				
				float scl = content instanceof Item ? 10 : 1;
				
				TextArea textArea = new TextArea("");
				add(textArea).width(LEN * 2.5f);
				button("@editor.apply", () -> {
					try{
						findAnd(content, Integer.parseInt(textArea.getText()));
					}catch(NumberFormatException exception){
						ui.showErrorMessage(textArea.getText() + " is Not A Number!");
					}
				}).disabled(b -> textArea.getText().isEmpty()).size(LEN * 2, LEN - OFFSET);
				
				slider(0, scl * 500, scl, amount.get(), f -> {
					findAnd(content, (int)f);
				}).growX();
			}};
			
			added.put(content, table);
			
			return table;
		}
		
		public boolean inValidName(String curret, String formal){
			return curret == null || !curret.equals(formal) && occasions.containsKey(occasionName);
		}
		
		public ConfigDialog(String name, AutoEventTrigger eventTrigger, Cons2<String, AutoEventTrigger> modifier){
			super("@config");
			
			newTrigger = eventTrigger.copy();
			occasionName = name;
			if(eventTrigger.eventProv != null && !eventTrigger.eventProv.isEmpty()){
				constructor = eventTrigger.eventProv;
				if(checkConstructor()){
					newTrigger.eventType = CutsceneEvent.construct(constructor);
				}
			}
			
			addCloseListener();
			
			Seq<OV_Pair<UnlockableContent>> pairs = new Seq<>(newTrigger.items.size + newTrigger.units.size + newTrigger.buildings.size);
			pairs.addAll(newTrigger.items.as());
			pairs.addAll(newTrigger.items.as());
			pairs.addAll(newTrigger.items.as());
			
			pairs.each(p -> addTable(p.item, () -> p.value));
			
//			//Name Setter
			
			cont.pane(table -> {
				table.table().padTop(LEN + OFFSET).row();
				
				table.table(t -> {
					t.defaults().left().padLeft(LEN);
					t.label(() -> Core.bundle.format("mod.ui.trigger-current-name", occasionName) + " | " + (inValidName(occasionName, name) ? "[#ff7b69]" + Iconc.cancel + " " + Core.bundle.get("mod.ui.trigger-same-name") : "[heal]" + Iconc.ok)).growX().row();
					
					t.row().table(c -> {
						c.left();
						c.defaults().left().padLeft(OFFSET);
						TextArea textArea = new TextArea("name");
						c.add(Core.bundle.get("save.rename") + ": ");
						c.add(textArea).size(LEN * 6, LEN - OFFSET);
						c.button("@editor.apply", () -> {
							occasionName = textArea.getText();
						}).size(LEN + OFFSET, LEN - OFFSET);
					}).growX().fillY().left();
					
				}).growX().fillY().padBottom(LEN).row();
				
				//Constructor Applier
				
				table.table(i -> {
					i.image().color(Color.lightGray).padRight(OFFSET).growX().height(OFFSET / 3);
					i.add("@mod.ui.adjust").color(Color.lightGray);
					i.image().color(Color.lightGray).padLeft(OFFSET).growX().height(OFFSET / 3);
				}).pad(OFFSET).growX().fillY().center().row();
				
				table.table(t -> {
					t.defaults().growX().fillY().padTop(OFFSET / 2f).padLeft(LEN).padRight(LEN);
					t.table(bt -> {
						bt.left();
						bt.button("@mod.ui.disposable", Styles.togglet, () -> {
							newTrigger.disposable = !newTrigger.disposable;
						}).self(b -> b.checked(bu -> newTrigger.disposable)).size(LEN * 6, LEN - OFFSET).row();
					}).row();
					
					t.table(i -> {
						i.left();
						i.table(l -> {
							l.left();
							l.label(() -> Core.bundle.format("mod.ui.spacing-base", TableFunc.format(newTrigger.spacingBase / Time.toMinutes))).growX().row();
						}).growX().pad(OFFSET).row();
						
						i.table(l -> {
							l.left();
							TextArea textArea = new TextArea("");
							l.add(textArea).width(LEN * 2.5f);
							l.button("@editor.apply", () -> {
								try{
									newTrigger.spacingBase = Float.parseFloat(textArea.getText()) * Time.toMinutes;
								}catch(NumberFormatException exception){
									ui.showErrorMessage(textArea.getText() + " is Not A Number!");
								}
							}).disabled(b -> textArea.getText().isEmpty()).size(LEN * 2, LEN - OFFSET).padRight(OFFSET).padLeft(OFFSET);
							
							l.slider(0, 30, 0.5f, newTrigger.spacingBase / Time.toMinutes, f -> newTrigger.spacingBase = f * Time.toMinutes).growX();
						}).growX().fillY().pad(OFFSET);
					}).row();
					
					t.table(i -> {
						i.left();
						i.table(l -> {
							l.left();
							l.label(() -> Core.bundle.format("mod.ui.spacing-rand", TableFunc.format(newTrigger.spacingRand / Time.toMinutes))).growX().row();
						}).growX().pad(OFFSET).row();
						
						i.table(l -> {
							l.left();
							TextArea textArea = new TextArea("");
							l.add(textArea).width(LEN * 2.5f);
							l.button("@editor.apply", () -> {
								try{
									newTrigger.spacingRand = Float.parseFloat(textArea.getText()) * Time.toMinutes;
								}catch(NumberFormatException exception){
									ui.showErrorMessage(textArea.getText() + " is Not A Number!");
								}
							}).disabled(b -> textArea.getText().isEmpty()).size(LEN * 2, LEN - OFFSET).padRight(OFFSET).padLeft(OFFSET);
							
							l.slider(0, 30, 0.5f, newTrigger.spacingRand / Time.toMinutes, f -> newTrigger.spacingRand = f * Time.toMinutes).growX();
						}).growX().fillY().pad(OFFSET);
						
					}).row();
					
					t.table(i -> {
						i.left();
						i.table(l -> {
							l.left();
							l.label(() -> Core.bundle.format("mod.ui.min-trigger-wave", newTrigger.minTriggerWave)).growX().row();
						}).growX().pad(OFFSET).row();
						
						i.table(l -> {
							l.left();
							TextArea textArea = new TextArea("");
							l.add(textArea).width(LEN * 2.5f);
							l.button("@editor.apply", () -> {
								try{
									newTrigger.minTriggerWave = Integer.parseInt(textArea.getText());
								}catch(NumberFormatException exception){
									ui.showErrorMessage(textArea.getText() + " is Not A Number!");
								}
							}).disabled(b -> textArea.getText().isEmpty()).size(LEN * 2, LEN - OFFSET).padRight(OFFSET).padLeft(OFFSET);
							
							l.slider(0, 100, 1, newTrigger.minTriggerWave, f -> newTrigger.minTriggerWave = (int)f).growX();
						}).growX().fillY().pad(OFFSET);
					}).row();
				
				}).growX().fillY().padBottom(LEN).row();
				
				table.table(i -> {
					i.image().color(Color.lightGray).padRight(OFFSET).growX().height(OFFSET / 3);
					i.add("@mod.ui.world-event").color(Color.lightGray);
					i.image().color(Color.lightGray).padLeft(OFFSET).growX().height(OFFSET / 3);
				}).pad(OFFSET).growX().fillY().center().row();
				
				table.table(t -> {
					t.defaults().align(Align.topLeft).padLeft(LEN).padRight(LEN);
					
					Func<Displayable, Table> displayed = d -> new Table(Tex.sideline){{
						marginLeft(OFFSET * 1.5f);
						if(d != null){
							d.display(this);
						}else this.add(Iconc.cancel + " " + Core.bundle.get("nh.cutscene.empty-event")).color(Pal.redderDust);
					}};
					
					displayEventTable = displayed.get(newTrigger.eventType);
					Table shower = t.table(s -> {
						s.align(Align.topLeft);
						s.defaults().align(Align.topLeft).pad(OFFSET * 2);
						s.add(displayEventTable).growX().fillY().margin(OFFSET);
					}).growX().fillY().get().row();
					
					t.row();
					t.table(bu -> {
						bu.button("@mod.ui.select-event", Icon.refresh, () -> {
							new BaseDialog(""){{
								cont.pane(info -> {
									CutsceneEvent.cutsceneEvents.each((eventName, event) -> {
										info.table(Tex.buttonEdge1, c -> {
											c.margin(OFFSET).center();
											c.defaults().growX().fillY().pad(OFFSET);
											c.table(c1 -> {
												c1.add(eventName).fillX().color(Color.lightGray);
												c1.image().growX().pad(OFFSET / 2f).height(OFFSET / 3f).color(Color.lightGray);
											}).growX().fillY().row();
											c.button("@sectors.select", Icon.rightOpen, () -> {
												newTrigger.eventType = event;
												displayEventTable.remove();
												shower.add(displayEventTable = displayed.get(event));
												hide();
											}).growX().height(LEN - OFFSET).disabled(b -> CutsceneEvent.inValidEvent(event)).row();
											c.table(s -> {
												s.align(Align.topLeft);
												s.defaults().align(Align.topLeft);
												s.add(displayed.get(event)).fill();
											}).growX().get().row();
										}).growX().fillY().pad(OFFSET).padLeft(LEN + OFFSET).padRight(LEN + OFFSET).row();
									});
								}).grow();
								
								addCloseButton();
							}}.show();
						}).growX().height(LEN - OFFSET).margin(OFFSET).padRight(OFFSET / 3f);
						bu.button("@clear", Icon.cancel, () -> {
							newTrigger.eventType = CutsceneEvent.NULL_EVENT;
							constructor = "";
							displayEventTable.remove();
							shower.add(displayEventTable = displayed.get(newTrigger.eventType));
						}).growX().height(LEN - OFFSET).margin(OFFSET).padLeft(OFFSET / 3f);
					}).growX().fillY().row();
					t.button("@mod.ui.event-js-constructor", Icon.download, () -> {
						ui.showCustomConfirm("@preparingcontent", "@mod.ui.apply-approach", "@mod.ui.apply-approach.file", "@mod.ui.apply-approach.clipboard", () -> {
							platform.showMultiFileChooser(fi -> {
								constructor = fi.readString();
								if(checkConstructor()){
									displayEventTable.remove();
									shower.add(displayEventTable = displayed.get(newTrigger.eventType));
								};
							}, "js");
						}, () -> {
							constructor = Core.app.getClipboardText();
							if(constructor == null)constructor = "//NULL";
							if(checkConstructor()){
								displayEventTable.remove();
								shower.add(displayEventTable = displayed.get(newTrigger.eventType));
							};
						});
					}).fillX().height(LEN - OFFSET).padTop(OFFSET / 3).margin(OFFSET).disabled(b -> !CutsceneEvent.inValidEvent(newTrigger.eventType)).row();
					t.add("[accent]" + Iconc.warning + " " + Core.bundle.get("mod.ui.handle-event")).growX().fillY().padBottom(OFFSET).left().row();
					t.pane(js -> {
						js.align(Align.topLeft).background(Tex.pane);
						js.label(() -> constructor).fill().pad(OFFSET / 2f).align(Align.topLeft).color(Color.lightGray).get().setWrap(false);
					}).growX().fillY().visible(() -> constructor != null && !constructor.isEmpty());
				}).growX().fillY().padBottom(LEN).row();
				
				table.add("@mod.ui.event-requirement").color(Color.lightGray).growX().fillY().padLeft(LEN * 2).left().padBottom(OFFSET / 2).row();
				
				buildRequirementTable(table, Item.class, "@content.item.name", content.items().removeAll(UnlockableContent::isHidden), newTrigger.items);
				buildRequirementTable(table, UnitType.class, "@content.unit.name", content.units().copy().removeAll(UnlockableContent::isHidden), newTrigger.units);
				buildRequirementTable(table, Block.class, "@content.item.name", content.blocks().copy().filter(Block::isVisible), newTrigger.buildings);
			}).marginRight(UIActions.width_UTD / 10f * Mathf.num(!mobile)).marginLeft(UIActions.width_UTD / 10f * Mathf.num(!mobile)).grow().row();
			
			cont.table(t -> {
				t.defaults().growX().height(LEN).padTop(OFFSET).marginLeft(OFFSET).marginRight(OFFSET);
				t.button("@back", Icon.left, Styles.cleart, this::hide);
				t.button("@editor.apply", Icon.left, Styles.cleart, () -> {
					if(inValidName(occasionName, name)){
						ui.showErrorMessage(Core.bundle.get("mod.ui.trigger-give-name"));
					}if(CutsceneEvent.inValidEvent(newTrigger.eventType)){
						ui.showErrorMessage(Core.bundle.get("mod.ui.invalid-event"));
					}else{
						if(name != null){
							removeTrigger(name);
						}
						if(occasionName != null){
							addTrigger(occasionName, newTrigger);
						}
						
						modifier.get(occasionName, eventTrigger);
						updateJson();
						hide();
					}
				});
			}).growX();
		}
		
		public boolean checkConstructor(){
			boolean ligal = false;
			
			try{
				CutsceneEvent.eventHandled = null;
				CutsceneScript.runJS(constructor);
				if(CutsceneEvent.eventHandled != null && CutsceneEvent.eventHandled != CutsceneEvent.NULL_EVENT)ligal = true;
			}catch(Throwable t){
				Log.err(t.toString());
				Vars.ui.showException(t);
			}
			
			if(ligal){
				newTrigger.eventType = CutsceneEvent.eventHandled;
				CutsceneEvent.cutsceneEvents.remove(CutsceneEvent.eventHandled.name);
				CutsceneEvent.eventHandled = null;
				newTrigger.eventProv = constructor;
			}else{
				ui.showErrorMessage("@mod.ui.apply-constructor-failed");
				constructor = "";
			}
			
			constructorError = !ligal;
			
			return ligal;
		}
		
		//
		public <T extends UnlockableContent> void buildRequirementTable(Table table, Class<T> type, String title, Seq<T> total, Seq<OV_Pair<T>> target){
			table.table(i -> {
				i.image().color(Color.lightGray).padRight(OFFSET).growX().height(OFFSET / 3);
				i.add(title).color(Color.lightGray);
				i.image().color(Color.lightGray).padLeft(OFFSET).growX().height(OFFSET / 3);
			}).pad(OFFSET).growX().fillY().center().row();
			
			table.table(t -> {
				Table cons = new Table(Tex.sideline);
				cons.marginLeft(OFFSET * 2).left();
				cons.defaults().left().growX().fillY();
				
				Seq<T> selected = added.keys().toSeq().filter(c -> type.isAssignableFrom(c.getClass())).as();
				buildTable(t, total, () -> selected, (T c, Boolean add) -> {
					if(add){
						OV_Pair<T> pair = new OV_Pair<>(c, 1);
						target.add(pair);
						selected.add(c);
						cons.add(addTable(c, () -> pair.value)).row();
					}else{
						selected.remove(c);
						added.remove(c).remove();
						target.remove(p -> p.item == c);
					}
				});
				
				t.left();
				
				selected.each(s -> {
					cons.add(added.get(s)).row();
				});
				
				t.pane(p -> {
					p.align(Align.topLeft);
					p.add(cons).growX().fillY().align(Align.topLeft);
				}).growX().height(LEN * 6).padLeft(OFFSET).marginRight(OFFSET).get().setForceScroll(false, true);
			}).growX().height(LEN * 6 + OFFSET * 2).marginLeft(LEN).marginRight(LEN).padBottom(LEN).row();
		}
		
		public <T extends UnlockableContent> void buildTable(Table table, Seq<T> items, Prov<Seq<T>> holder, Cons2<T, Boolean> consumer){
			ButtonGroup<ImageButton> group = new ButtonGroup<>();
			group.setMinCheckCount(0);
			group.setMaxCheckCount(-1);
			Table cont = new Table();
			cont.defaults().size(LEN - OFFSET);
			
			int i = 0;
			
			for(T item : items){
				if(!item.unlockedNow()) continue;
				
				ImageButton button = cont.button(new TextureRegionDrawable(item.uiIcon), Styles.clearToggleTransi, 24, () -> {}).group(group).get();
				button.changed(() -> consumer.get(item, button.isChecked()));
				button.update(() -> button.setChecked(holder.get().contains(item)));
				button.addListener(new Tooltip(t2 -> {
					t2.margin(2f);
					t2.background(Styles.black5);
					t2.add(item.localizedName).fill();
				}));
				
				if(i++ % 4 == 3){
					cont.row();
				}
			}
			
			//add extra blank spaces so it looks nice
			if(i % 4 != 0){
				int remaining = 4 - (i % 4);
				for(int j = 0; j < remaining; j++){
					cont.image(Styles.black6);
				}
			}
			
			ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
			pane.setScrollingDisabled(true, false);
			
			pane.setOverscroll(false, false);
			table.add(pane).height(LEN * 6).fillX().left();
		}
	}
	
	public boolean removeTrigger(String name){
		occasions.remove(name);
		return triggers.asArray().remove(triggers.asArray().find(jval -> jval.asObject().get(CCS_JsonHandler.TRIGGER_NAME).asString().equals(name)));
	}
	
	public void addTrigger(String name, AutoEventTrigger trigger){
		occasions.put(name, trigger);
		CCS_JsonHandler.addTrigger(name, trigger, triggers.asArray());
	}
}
