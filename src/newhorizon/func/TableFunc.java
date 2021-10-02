package newhorizon.func;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons;
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
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextArea;
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
import mindustry.world.modules.ItemModule;
import newhorizon.feature.CutsceneScript;
import newhorizon.vars.NHVars;

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
                    actions(Actions.touchable(Touchable.disabled), Actions.moveTo(-width, y, 0.7f, Interp.pow4In), Actions.remove());
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
            background(Tex.button);
            Table in = new Table(out -> {
                Label label = new Label("Spawn");
                out.update(() -> {
                    label.setText(Core.bundle.get("waves.perspawn") + ": [accent]" + spawnNum + "[]* | At: " + (int)point.x + ", " + (int)point.y);
                    label.setWidth(out.getWidth());
                });
                out.add(label).growX().fillY().pad(OFFSET).row();
    
                out.pane(con -> {
                    con.button(Icon.leftOpen, Styles.clearPartiali, () -> spawnNum = Mathf.clamp(--spawnNum, 1, 100)).size(LEN - OFFSET * 1.5f);
                    con.slider(1, 100, 2, spawnNum, (f) -> spawnNum = (int)f).growX().height(LEN - OFFSET * 1.5f).padLeft(OFFSET / 2).padRight(OFFSET / 2);
                    con.button(Icon.rightOpen, Styles.clearPartiali, () -> spawnNum = Mathf.clamp(++spawnNum, 1, 100)).size(LEN - OFFSET * 1.5f);
                }).growX().height(LEN).row();
    
                out.table(con -> {
                    con.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
                        pointSelectTable(starter, p -> point.set(World.unconv(p.x), World.unconv(p.y)));
                    }).grow();
                    con.button(Icon.cancel, Styles.clearTransi, () -> point.set(-1, -1)).size(LEN);
                }).growX().height(LEN).row();
    
                out.pane(table -> {
                    int num = 0;
                    for(UnitType type : content.units()){
                        if(type.isHidden()) continue;
                        if(num % 5 == 0) table.row();
                        table.button(new TextureRegionDrawable(type.fullIcon), Styles.clearTogglei, LEN, () -> selected = type).update(b -> b.setChecked(selected == type)).size(LEN);
                        num++;
                    }
                }).fillX().height(LEN * 3f).row();
    
                out.keyDown(c -> {
                    if(c == KeyCode.left)spawnNum = Mathf.clamp(--spawnNum, 1, 100);
                    if(c == KeyCode.right)spawnNum = Mathf.clamp(++spawnNum, 1, 100);
                });
                
                Table t = new Table(tin -> {
                    tin.table(con -> {
                        float size = out.getPrefWidth() / 8;
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
                        float size = out.getPrefWidth() / 8;
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
                    }).growX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("Add Items", Styles.cleart, () -> {
                            for(Item item : content.items()) player.team().core().items.add(item, 1000000);
                        }).size(LEN * 2, LEN);
                    }).grow().row();
                    tin.pane(con -> {
                        con.button("Debug", Styles.cleart, () -> {
                            new TableTexDebugDialog("debug").show();
                        }).disabled(b -> !state.rules.infiniteResources && !NHSetting.getBool("@active.debug")).grow();
                    }).growX().height(LEN).row();
                });
                out.pane(t).fillX().height(t.getHeight()).padTop(OFFSET).row();
            });
            add(in).fillX();
            setSize(Core.graphics.getWidth() / 4f, Core.graphics.getHeight() * 0.75f);
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
    
    public static final TextAreaMod textArea = new TextAreaMod("");
    
    public static class TextAreaMod extends TextArea{
        public TextAreaMod(String text){
            super(text);
        }
    
        @Override
        public void sizeChanged(){
            super.sizeChanged();
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static int getLineNum(String string){
        string.replaceAll("\r", "\n");
        return string.split("\n").length;
    }
    
    public static void disableTable(){
        Core.scene.root.removeChild(starter);
    }
    
    public static void showTable(){
        Core.scene.root.addChildAt(1, starter);
    }
    
    public static void showInner(Table parent, Table children){
        Inner inner = new Inner();
        
        parent.addChildAt(parent.getZIndex() + 1, inner);
        inner.init(parent.getWidth() + children.getWidth() + OFFSET);
    
        children.fill().pack();
        children.setTransform(true);
        inner.addChildAt(parent.getZIndex() + 1, children);
        inner.setScale(parent.scaleX, parent.scaleY);
        children.setScale(parent.scaleX, parent.scaleY);
        
        
        children.setPosition(inner.getWidth() - children.getWidth(), inner.y + (inner.getHeight() - children.getHeight()) / 2);
        
        inner.actions(Actions.moveTo(0, inner.y, 1f, Interp.fade));
    }
    
    public static void tableMain(){
        if(headless)return;
        
        starter.setSize(LEN + OFFSET, (LEN + OFFSET) * 3);

        starter.update(() -> starter.setPosition(0, (Core.graphics.getHeight() - starter.getHeight()) / 2f));
        starter.visible(() -> !state.isMenu() && ui.hudfrag.shown && !net.active());
        starter.touchable(() -> !state.isMenu() && ui.hudfrag.shown && !net.active() ? Touchable.enabled : Touchable.disabled);
        
        Player player = Vars.player;
        
        starter.table(table -> {
            table.button(Icon.settings, Styles.clearTransi, starter.getWidth() - OFFSET, () -> {
                showInner(starter, new ToolTable());
            }).grow().disabled(b -> !NHSetting.getBool("@active.admin-panel") || starter.getChildren().contains(e -> "INNER".equals(e.name))).row();
            table.button(Icon.logic, Styles.clearTransi, starter.getWidth() - OFFSET, () -> {
                showInner(starter, new Table(Tex.button){{
                    setSize(Core.graphics.getWidth() / 3f, Core.graphics.getHeight() * 0.75f);
                    Label label = new Label("");
                    label.setWrap(false);
                    label.setText(textArea.getText());
                    
                    Label liner = new Label("");
                    liner.setWrap(false);
                
                    update(() -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        int linesGlobal = getLineNum(CutsceneScript.getModGlobalJS().readString());
                        int lines = textArea.getLinesShowing();
    
                        for(int i = 0; i < lines; i++)stringBuilder.append(i + linesGlobal + 1).append("\n");
                        liner.setText(stringBuilder.toString());
                    });
                    
                    ScrollPane sp = pane(Styles.horizontalPane, t -> {
                        t.touchable(() -> Touchable.enabled);
                        
                        t.table(cont -> {
                            cont.top();
                            cont.add(liner).fillX().height(label.getPrefHeight()).color(Color.gray);
                            cont.add(textArea).size(label.getPrefWidth(), label.getPrefHeight());
                        }).pad(LEN / 2);
                    }).grow().pad(OFFSET).get();
                    
                    sp.setForceScroll(true, true);
                    
                    row();
                    table(t -> {
                        t.defaults().height(LEN);
                        t.button("@save", Styles.cleart, () -> {
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
                        }).growX().padRight(OFFSET);
                        t.button("Run Selection", Styles.cleart, () -> {
                            CutsceneScript.runJS(textArea.getSelection());
                        }).growX();
                        t.row();
                        t.button("Refresh", Styles.cleart, () -> {
                            textArea.setText(CutsceneScript.currentScriptFile.readString());
                        }).growX().disabled(b -> CutsceneScript.currentScriptFile == null).padTop(OFFSET).padRight(OFFSET);
                        t.button("Read", Styles.cleart, () -> {
                            platform.showMultiFileChooser(file -> {
                                textArea.setText(file.readString());
                            }, "js");
                        }).growX();
                    }).growX().fillY();
                }});
            }).grow().disabled(b -> !NHSetting.getBool("@active.debug") || starter.getChildren().contains(e -> "INNER".equals(e.name))).row();
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