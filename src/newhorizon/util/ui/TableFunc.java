package newhorizon.util.ui;

import arc.Core;
import arc.audio.Sound;
import arc.func.Boolp;
import arc.func.Cons;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.core.UI;
import mindustry.core.World;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Links;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.modules.ItemModule;
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.CutsceneScript;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHSetting;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

import static mindustry.Vars.*;


public class TableFunc{
    private static final int tableZ = 2;
    private static final DecimalFormat df = new DecimalFormat("######0.00");
    private static final Vec2 point = new Vec2(-1, -1);
    private static int spawnNum = 1;
    private static Team selectTeam = Team.sharded;
    private static UnitType selected = UnitTypes.alpha;
    private static long lastToast;

    private static boolean pointValid(){
        return point.x >= 0 && point.y >= 0 && point.x <= world.width() * tilesize && point.y <= world.height() * tilesize;
    }
    
    private static class Inner extends Table{
        Inner(){
            name = "INNER";
            background(Tex.paneSolid);
            
            left();
            table(table -> {
                button(Icon.cancel, Styles.clearTransi, () -> {
                    actions(Actions.touchable(Touchable.disabled), Actions.moveBy(-width, 0, 0.4f, Interp.pow3In), Actions.remove());
                }).width(LEN).growY();
            }).growY().fillX().padRight(OFFSET);
        }
        
        public void init(float width){
            setSize(width, starter.getHeight());
            setPosition(-this.width, starter.originY);
        }
    }
    
    private static class ToolTable extends Table{
        ToolTable(){
            setSize(Core.graphics.getWidth() / (Vars.mobile ? 2f : 4f), Core.graphics.getHeight() * 0.75f);
            background(Tex.button);
            Table in = new Table(){{
                Label label = new Label("Spawn");
                update(() -> {
                    label.setText(Core.bundle.get("waves.perspawn") + ": [accent]" + spawnNum + "[]* | At: " + (int)point.x + ", " + (int)point.y);
                    label.setWidth(getWidth());
                });
                add(label).growX().fillY().pad(OFFSET).align(Align.topLeft).row();
                button("Copy Coords", Icon.copy, Styles.transt, () -> Core.app.setClipboardText((int)point.x + ", " + (int)point.y)).growX().fillY().row();
    
                pane(con -> {
                    con.button(Icon.leftOpen, Styles.clearPartiali, () -> spawnNum = Mathf.clamp(--spawnNum, 1, 100)).size(LEN - OFFSET * 1.5f);
                    con.slider(1, 100, 2, spawnNum, (f) -> spawnNum = (int)f).growX().height(LEN - OFFSET * 1.5f).padLeft(OFFSET / 2).padRight(OFFSET / 2);
                    con.button(Icon.rightOpen, Styles.clearPartiali, () -> spawnNum = Mathf.clamp(++spawnNum, 1, 100)).size(LEN - OFFSET * 1.5f);
                }).growX().height(LEN).row();
    
                table(con -> {
                    con.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
                        pointSelectTable(starter, p -> point.set(World.unconv(p.x), World.unconv(p.y)));
                    }).grow();
                    con.button(Icon.cancel, Styles.clearTransi, () -> point.set(-1, -1)).size(LEN);
                }).growX().height(LEN).row();
    
                ScrollPane p = pane(table -> {
                    int num = 0;
                    for(UnitType type : content.units()){
                        if(type.isHidden()) continue;
                        if(num % 5 == 0) table.row();
                        table.button(new TextureRegionDrawable(type.fullIcon), Styles.clearTogglei, LEN, () -> selected = type).update(b -> b.setChecked(selected == type)).size(LEN);
                        num++;
                    }
                }).fillX().height(LEN * 3f).get();
                
                row();
                
                p.setFadeScrollBars(true);
                p.setupFadeScrollBars(0.35f, 0.45f);
    
                keyDown(c -> {
                    if(c == KeyCode.left)spawnNum = Mathf.clamp(--spawnNum, 1, 100);
                    if(c == KeyCode.right)spawnNum = Mathf.clamp(++spawnNum, 1, 100);
                });
                
                Table t = new Table(tin -> {
                    tin.table(con -> {
                        float size = getPrefWidth() / 8;
                        con.image(Icon.players).size(size).padRight(size);
                        for(Team team : Team.baseTeams){
                            con.button(Tex.whiteui, Styles.clearTogglei, size - 8f, () -> player.team(team)).update(b -> {
                                b.setChecked(player.team() == team);
                                b.getStyle().imageUpColor = team.color;
                            }).size(size);
                        }
                    }).growX().height(LEN).row();
                    tin.image().color(Pal.gray).height(OFFSET / 3).growX().row();
                    tin.table(con -> {
                        float size = getPrefWidth() / 8;
                        con.image(Icon.units).size(size).padRight(size);
                        for(Team team : Team.baseTeams){
                            con.button(Tex.whiteui, Styles.clearTogglei, size - 8f, () -> selectTeam = team).update(b -> {
                                b.setChecked(selectTeam == team);
                                b.getStyle().imageUpColor = team.color;
                            }).size(size);
                        }
                    }).growX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("SpawnPos", Icon.link, Styles.cleart, () -> NHFunc.spawnSingleUnit(selected, selectTeam, spawnNum, point.x, point.y)).disabled(b -> !pointValid()).grow();
                        con.button("SpawnCur", Icon.add, Styles.cleart, () -> NHFunc.spawnSingleUnit(selected, selectTeam, spawnNum, player.x, player.y)).grow();
                    }).growX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("Remove Units", Styles.cleart, Groups.unit::clear).grow();
                        con.button("Remove Fires", Styles.cleart, () -> {
                            for(int i = 0; i < 20; i++) Time.run(i * Time.delta * 3, Groups.fire::clear);
                        }).grow();
                        con.button("Cathc Fires", Styles.cleart, () -> {
                            Geometry.circle(World.toTile(point.x), World.toTile(point.y), 10, ((x1, y1) -> {
                                Tile tile = world.tile(x1, y1);
                                if(tile != null)UltFire.create(tile);
                            }));
                            
                        }).disabled(b -> !pointValid()).grow();
                    }).growX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("Add Items", Styles.cleart, () -> {
                            for(Item item : content.items()) player.team().core().items.add(item, 1000000);
                        }).size(LEN * 2, LEN);
                    }).grow().row();
                });
                pane(t).fillX().height(t.getHeight()).padTop(OFFSET).row();
                
                table().fill();
            }};
            ScrollPane p = pane(in).grow().get();
            
            p.setStyle(Styles.horizontalPane);
            p.setFadeScrollBars(true);
            p.setupFadeScrollBars(0.35f, 0.45f);
        }
    
        @Override
        public void draw(){
            if(pointValid()){
                Vec2 drawVec = Core.camera.project(Tmp.v1.set(point));
    
                float sX = x + width - 11;
                float sY = y + height - 4;
                float rad = 14 + Mathf.absin(16f, 8f);
                float out = width / 1.8f;
                
                Lines.stroke(5f);
                Draw.color(Pal.gray, Color.white, Mathf.absin(4f, 0.4f));
                Lines.line(sX, sY, sX + out, sY, false);
                Fill.circle(sX + out, sY, Lines.getStroke() / 2);
    
                Tmp.v2.trns(Angles.angle(drawVec.x, drawVec.y, sX + out, sY), rad).add(drawVec);
    
                Lines.line(sX + out, sY, Tmp.v2.x, Tmp.v2.y, false);
                Lines.circle(drawVec.x, drawVec.y, rad);
            }
            
            super.draw();
        }
    }
    
    public static final String tabSpace = "    ";
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    public static String format(float value){return df.format(value);}
    public static String getJudge(boolean value){return value ? "[green]Yes[]" : "[red]No[]";}
    public static String getPercent(float value){return Mathf.floor(value * 100) + "%";}
    
    private static final Table starter = new Table(Tex.paneSolid);
    
    public static final TextArea textArea = Vars.headless ? null : new TextArea("");
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static int getLineNum(String string){
        string.replaceAll("\r", "\n");
        return string.split("\n").length;
    }
    
    public static void disableTable(){
        Core.scene.root.removeChild(starter);
    }
    
    public static void showTable(){
        Core.scene.root.addChildAt(3, starter);
    }
    
    public static void showInner(Table parent, Table children){
        Inner inner = new Inner();
        
        parent.addChildAt(parent.getZIndex() + 1, inner);
        inner.init(parent.getWidth() + children.getWidth() + OFFSET);
    
        children.fill().pack();
        children.setTransform(true);
        inner.addChildAt(parent.getZIndex() + 2, children);
        inner.setScale(parent.scaleX, parent.scaleY);
        children.setScale(parent.scaleX, parent.scaleY);
        
        
        children.setPosition(inner.getWidth() - children.getWidth(), inner.y + (inner.getHeight() - children.getHeight()) / 2);
        
        inner.actions(Actions.moveTo(0, inner.y, 0.35f, Interp.pow3Out));
    }
    
    public static void tableMain(){
        if(headless)return;
        
        starter.setSize(LEN + OFFSET, (LEN + OFFSET) * 3);

        starter.update(() -> starter.setPosition(0, (Core.graphics.getHeight() - starter.getHeight()) / 2f));
        starter.visible(() -> !state.isMenu() && ui.hudfrag.shown && !net.active());
        starter.touchable(() -> !state.isMenu() && ui.hudfrag.shown && !net.active() ? Touchable.enabled : Touchable.disabled);
        
        Player player = Vars.player;
        
        Boolp hasInner = () -> starter.getChildren().contains(e -> "INNER".equals(e.name));
        
        starter.table(table -> {
            table.defaults().size(starter.getWidth() - OFFSET);
            table.button(Icon.settings, Styles.clearTransi, () -> {
                showInner(starter, new ToolTable());
            }).grow().disabled(b -> !NHSetting.getBool("@active.admin-panel") || hasInner.get()).row();
            table.button(Icon.logic, Styles.clearTransi, () -> {
                showInner(starter, new Table(Tex.button){{
                    setSize(Core.graphics.getWidth() / 1.5f, Core.graphics.getHeight() * 0.75f);
                    Label label = new Label("");
                    label.setWrap(false);
                    label.setText(textArea.getText());

                    Label liner = new Label("");
                    liner.setWrap(false);
                    
                    ScrollPane sp = pane(Styles.horizontalPane, t -> {
                        t.align(Align.topLeft);
                        Cell<Label> l = t.add(liner).color(Color.gray).padTop(13.5f).fill();
                        Cell<TextArea> textAreaMod = t.add(textArea).fill();
                        t.update(() -> {
                            label.setText(textArea.getText());
                            StringBuilder stringBuilder = new StringBuilder();
                            
                            int lines = textArea.getLinesShowing();
    
                            for(int i = 0; i < lines; i++)stringBuilder.append(i).append("\n");
                            liner.setText(stringBuilder.toString());
                            
                            l.fillX().height(label.getPrefHeight() + LEN * 4);
                            textAreaMod.size(label.getPrefWidth() + LEN * 4, label.getPrefHeight() + LEN * 4);
                            t.table().fill();
                        });
                    }).grow().pad(OFFSET).get();
                    
                    
                    sp.setForceScroll(true, true);
                    row();
                    table(t -> {
                        t.defaults().height(LEN).growX().pad(OFFSET / 2f);
                        t.button("Try Run From Clipboard", Styles.cleart, () -> {
                            Core.app.post(() -> CutsceneScript.runJS(Core.app.getClipboardText()));
                        }).disabled(b -> Core.app.getClipboardText() == null || Core.app.getClipboardText().isEmpty());
                        t.button("Debug Events", Styles.cleart, () -> {
                            new BaseDialog("Debug"){{
                                addCloseButton();
                                cont.pane(t -> {
                                    CutsceneEventEntity.events.each(e -> {
                                        e.setupDebugTable(t);
                                        t.row();
                                    });
                                }).grow();
                            }
    
                                @Override
                                public void hide(){
                                    super.hide();
                                    CutsceneEventEntity.events.each(e -> !e.eventType().isHidden, e -> e.show(UIActions.eventTable()));
                                }
                            }.show();
                        }).disabled(b -> CutsceneEventEntity.events.isEmpty());
                        t.button("Run Selection", Styles.cleart, () -> {
                            Core.app.post(() -> CutsceneScript.runJS(textArea.getSelection()));
                        }).disabled(b -> textArea.getSelection().isEmpty());
                        t.row();
                        
                        t.button("Reload From Matched File", Styles.cleart, () -> {
                            ui.showConfirm(
                                "Are you sure you want reload the script from matched file: " + CutsceneScript.currentScriptFile.name() + "?",
                                "This will overwrite the script here",
                                () -> textArea.setText(CutsceneScript.currentScriptFile.readString())
                            );
                        }).growX().disabled(b -> CutsceneScript.currentScriptFile == null);
                        t.button("Load Script", Styles.cleart, () -> {
                            platform.showMultiFileChooser(file -> {
                                textArea.setText(file.readString());
                            }, "js");
                        });
                        t.button("Save Script", Styles.cleart, () -> {
                            ui.showConfirm("Are you sure you want save the script?", () -> {
                                if(CutsceneScript.currentScriptFile != null){
                                    int hash = CutsceneScript.currentScriptFile.readString().hashCode();
                                    CutsceneScript.currentScriptFile.writeString(textArea.getText(), false);
                                    ui.showText("Save successfully", hash + " -> " + textArea.getText().hashCode());
                                }else{
                                    ui.showCustomConfirm("Script File Missing", "Copy to clipboard?", "Accept", "@back",
                                        () -> {
                                            Core.app.setClipboardText(textArea.getText());
                                        }, () -> {
                        
                                        }
                                    );
                                }
                            });
                        });
    
                        t.row();
    
                        t.button("Check World Data", Styles.cleart, () -> {
                            ui.showText("Vars.state.rules.tags", state.rules.tags.toString(), Align.left);
                        });
                        t.button("Remove World Data", Styles.cleart, () -> {
                            ui.showConfirm("Are you sure?", state.rules.tags::clear);
                        });
                        t.button("Debug Menu", Styles.cleart, () -> {
                            new DebugDialog("").show();
                        });
    
                    }).growX().fillY();
                }});
            }).grow().disabled(b -> !NHSetting.getBool("@active.debug") || hasInner.get()).row();
            table.button(Icon.play, Styles.clearTransi, () -> {
                Core.app.post(() -> CutsceneScript.runJS(Core.app.getClipboardText()));
            }).disabled(b -> Core.app.getClipboardText() == null || Core.app.getClipboardText().isEmpty() || hasInner.get() || !NHSetting.getBool("@active.debug"));
        }).grow().row();
        
        Core.scene.root.addChildAt(1, starter);
        starter.setScale(Core.scene.table().scaleX, Core.scene.table().scaleY);
    }
    
    public static void buildBulletTypeInfo(Table t, BulletType type){
        t.table(table -> {
            if(type == null)return;
            Class<?> typeClass = type.getClass();
            Field[] fields = typeClass.getFields();
            for(Field field : fields){
                try{
                    if(field.getGenericType().toString().equals("boolean")) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": ").append(getJudge(field.getBoolean(type))).append("[]")).left().row();
                    if(field.getGenericType().toString().equals("float") && field.getFloat(type) > 0) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [accent]").append(field.getFloat(type)).append("[]")).left().row();
                    if(field.getGenericType().toString().equals("int") && field.getInt(type) > 0) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": [accent]").append(field.getInt(type)).append("[]")).left().row();
                    
                    if(field.getType().getSimpleName().equals("BulletType")){
                        BulletType inner = (BulletType)field.get(type);
                        if(inner == null || inner.toString().equals("bullet#0") || inner.toString().equals("bullet#1") || inner.toString().equals("bullet#2"))continue;
                        
                        table.add("[gray]" + field.getName() + "{ ").left().row();
                        table.table(in -> buildBulletTypeInfo(in, inner)).padLeft(LEN).row();
                        table.add("[gray]}").left().row();
                    }
                }catch(IllegalAccessException err){
                    throw new RuntimeException(err);
                }
            }
        }).row();
    }
    
    public static Table tableImageShrink(TextureRegion tex, float size, Table table){
        return tableImageShrink(tex, size, table, c -> {});
    }
    
    public static Table tableImageShrink(TextureRegion tex, float size, Table table, Cons<Image> modifier){
        float parma = Math.max(tex.height, tex.width);
        float f = Math.min(size, parma);
        Image image = new Image(tex);
        modifier.get(image);
        table.add(image).size(tex.width * f / parma, tex.height * f / parma);
        
        return table;
    }
    
    public static void itemStack(Table parent, ItemStack stack, ItemModule itemModule){
        float size = LEN - OFFSET;
        parent.table(t -> {
            t.image(stack.item.fullIcon).size(size).left();
            t.table(n -> {
                Label l = new Label("");
                n.add(stack.item.localizedName + " ").left();
                n.add(l).left();
                n.add("/" + UI.formatAmount(stack.amount)).left().growX();
                n.update(() -> {
                    int amount = itemModule == null ? 0 : itemModule.get(stack.item);
                    l.setText(UI.formatAmount(amount));
                    l.setColor(amount < stack.amount ? Pal.redderDust : Color.white);
                });
            }).growX().height(size).padLeft(OFFSET / 2).left();
        }).growX().height(size).left().row();
    }
    
    public static void link(Table parent, Links.LinkEntry link){
        parent.add(new Tables.LinkTable(link)).size(Tables.LinkTable.w + OFFSET * 2f, Tables.LinkTable.h).padTop(OFFSET / 2f).row();
    }
    
    public static void rectSelectTable(Table parentT, Runnable run){
        NHVars.resetCtrl();
        
        Rect r = NHVars.ctrl.rect;
        
        NHVars.ctrl.isSelecting = true;
        
        NHVars.ctrl.pressDown = false;
        
        Table pTable = new Table(Tex.pane){{
            update(() -> {
                if(Vars.state.isMenu())remove();
                else{
                    Vec2 v = Core.camera.project(r.x + r.width / 2, r.y - OFFSET);
                    setPosition(v.x, v.y, 0);
                }
            });
            table(Tex.paneSolid, t -> {
                t.button(Icon.upOpen, Styles.clearFulli, () -> {
                    run.run();
                    remove();
                    NHVars.ctrl.isSelecting = false;
                }).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.pressDown);
            }).size(LEN * 4, LEN);
        }};
        
        Table floatTable = new Table(Tex.clear){{
            parentT.color.a = 0.3f;
            
            update(() -> {
                r.setSize(Math.abs(NHVars.ctrl.to.x - NHVars.ctrl.from.x), Math.abs(NHVars.ctrl.to.y - NHVars.ctrl.from.y)).setCenter((NHVars.ctrl.from.x + NHVars.ctrl.to.x) / 2f, (NHVars.ctrl.from.y + NHVars.ctrl.to.y) / 2f);
                
                if(Vars.state.isMenu() || !NHVars.ctrl.isSelecting){
                    NHVars.ctrl.from.set(0, 0);
                    NHVars.ctrl.to.set(0, 0);
                    remove();
                }
                
                if(!mobile && NHVars.ctrl.pressDown){
                    NHVars.ctrl.to.set(Core.camera.unproject(Core.input.mouse())).clamp(0, 0, world.unitHeight(), world.unitWidth());
                }
            });
            
            touchable = Touchable.enabled;
            setFillParent(true);
            if(mobile){
                addListener(new InputListener(){
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(!NHVars.ctrl.pressDown){
                        touchable = Touchable.enabled;
                        NHVars.ctrl.from.set(Core.camera.unproject(x, y)).clamp(-finalWorldBounds, -finalWorldBounds, world.unitHeight() + finalWorldBounds, world.unitWidth() + finalWorldBounds);
                        NHVars.ctrl.to.set(NHVars.ctrl.from);
                    }else{
                        NHVars.ctrl.to.set(Core.camera.unproject(x, y)).clamp(-finalWorldBounds, -finalWorldBounds, world.unitHeight() + finalWorldBounds, world.unitWidth() + finalWorldBounds);
                    }
                    NHVars.ctrl.pressDown = !NHVars.ctrl.pressDown;
                    return false;
                    }
                });
            }else addListener(new InputListener(){
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    parentT.touchable = Touchable.childrenOnly;
                    NHVars.ctrl.pressDown = true;
                    NHVars.ctrl.from.set(Core.camera.unproject(x, y)).clamp(-finalWorldBounds, -finalWorldBounds, world.unitHeight() + finalWorldBounds, world.unitWidth() + finalWorldBounds);
                    NHVars.ctrl.to.set(NHVars.ctrl.from);
                    return false;
                }
                
                public void exit(InputEvent event, float x, float y, int pointer, Element toActor) {
                    if(remove()){
                        parentT.touchable = Touchable.enabled;
                        run.run();
                        NHVars.resetCtrl();
                    }
                }
            });
        }
            
            @Override
            public boolean remove(){
                parentT.color.a = 1f;
                return super.remove();
            }
        };
        
        Core.scene.root.addChildAt(9, floatTable);
        if(mobile)Core.scene.root.addChildAt(10, pTable);
    }
    
    public static void pointSelectTable(Table parentT, Cons<Point2> cons){
        Prov<Touchable> original = parentT.touchablility;
        Touchable parentTouchable = parentT.touchable;
        
        parentT.touchablility = () -> Touchable.disabled;
        
        NHVars.resetCtrl();
    
        Table pTable = new Table(Tex.clear){{
            update(() -> {
                if(Vars.state.isMenu()){
                    remove();
                }else{
                    Vec2 v = Core.camera.project(World.toTile(NHVars.ctrl.ctrlVec2.x) * tilesize, World.toTile(NHVars.ctrl.ctrlVec2.y) * tilesize);
                    setPosition(v.x, v.y, 0);
                }
            });
        }};
        
        Table floatTable = new Table(Tex.clear){{
            update(() -> {
                if(Vars.state.isMenu())remove();
            });
            touchable = Touchable.enabled;
            setFillParent(true);
            
            addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                NHVars.ctrl.ctrlVec2.set(Core.camera.unproject(x, y)).clamp(-Vars.finalWorldBounds, -Vars.finalWorldBounds, world.unitHeight() + Vars.finalWorldBounds, world.unitWidth() + Vars.finalWorldBounds);
                return false;
                }
            });
        }};
        
        pTable.button(Icon.cancel, Styles.emptyi, () -> {
            cons.get(Tmp.p1.set(World.toTile(NHVars.ctrl.ctrlVec2.x), World.toTile(NHVars.ctrl.ctrlVec2.y)));
            parentT.touchablility = original;
            parentT.touchable = parentTouchable;
            pTable.remove();
            floatTable.remove();
        }).center();
        
        Core.scene.root.addChildAt(Math.max(parentT.getZIndex() - 1, 0), pTable);
        Core.scene.root.addChildAt(Math.max(parentT.getZIndex() - 2, 0), floatTable);
    }
    
    private static void scheduleToast(Runnable run){
        long duration = (int)(3.5 * 1000);
        long since = Time.timeSinceMillis(lastToast);
        if(since > duration){
            lastToast = Time.millis();
            run.run();
        }else{
            Time.runTask((duration - since) / 1000f * 60f, run);
            lastToast += duration;
        }
    }
    
    public static void countdown(Element e, Floatp remainTime){
        e.addListener(new Tooltip(t2 -> {
            t2.background(Tex.bar);
            t2.color.set(Color.black);
            t2.color.a = 0.35f;
            t2.add("Remain Time: 00:00 ").update(l -> {
                float remain = remainTime.get();
                l.setText("[gray]Remain Time: " + ((remain / Time.toSeconds > 15) ? "[]" : "[accent]") + Mathf.floor(remain / Time.toMinutes) + ":" + Mathf.floor((remain % Time.toMinutes) / Time.toSeconds));
            }).left().fillY().growX().row();
        }));
    }
    
    public static void showToast(Drawable icon, String text, Sound sound){
        if(state.isMenu()) return;
        
        scheduleToast(() -> {
            sound.play();
            
            Table table = new Table(Tex.button);
            table.update(() -> {
                if(state.isMenu() || !ui.hudfrag.shown){
                    table.remove();
                }
            });
            table.margin(12);
            table.image(icon).pad(3);
            table.add(text).wrap().width(280f).get().setAlignment(Align.center, Align.center);
            table.pack();
            
            //create container table which will align and move
            Table container = Core.scene.table();
            container.top().add(table);
            container.setTranslation(0, table.getPrefHeight());
            container.actions(
                    Actions.translateBy(0, -table.getPrefHeight(), 1f, Interp.fade), Actions.delay(2.5f),
                    //nesting actions() calls is necessary so the right prefHeight() is used
                    Actions.run(() -> container.actions(Actions.translateBy(0, table.getPrefHeight(), 1f, Interp.fade), Actions.remove()))
            );
        });
    }
}