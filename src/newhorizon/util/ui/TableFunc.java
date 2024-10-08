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
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
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
import mindustry.world.Tile;
import mindustry.world.modules.ItemModule;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.PixelArtGenerator;
import newhorizon.util.func.NHFunc;
import newhorizon.util.ui.dialog.DebugDialog;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

import static mindustry.Vars.*;


public class TableFunc{
    private static final Vec2 ctrlVec = new Vec2();
    private static int tmpX = 0, tmpY = 0;
    
    private static final int tableZ = 2;
    private static final DecimalFormat df = new DecimalFormat("######0.0");
    private static final Vec2 point = new Vec2(-1, -1);
    private static int spawnNum = 1;
    private static Team selectTeam = Team.sharded;
    private static UnitType selected = UnitTypes.alpha;
    private static long lastToast;
    
    private static Table pTable = new Table(), floatTable = new Table();
    
    public static final String tabSpace = "    ";
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    public static String format(float value){return df.format(value);}
    public static String judge(boolean value){return value ? "[heal]" + Core.bundle.get("yes") + "[]" : "[#ff7b69]" + Core.bundle.get("no") + "[]";}
    public static String getPercent(float value){return Mathf.floor(value * 100) + "%";}
    
    private static boolean pointValid(){
        return point.x >= 0 && point.y >= 0 && point.x <= world.width() * tilesize && point.y <= world.height() * tilesize;
    }
    
    private static class Inner extends Table{
        Inner(){
            name = "INNER";
            background(Tex.paneSolid);
            
            left();
            table(table -> {
                table.button(Icon.cancel, Styles.cleari, () -> {
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
                    label.setText(Core.bundle.get("waves.perspawn") + ": [accent]" + spawnNum + "[]* | At: " + tmpX + ", " + tmpY);
                    label.setWidth(getWidth());
                });
                add(label).growX().fillY().pad(OFFSET).align(Align.topLeft).row();
                button("Copy Coords", Icon.copy, Styles.cleart, () -> Core.app.setClipboardText(tmpX + ", " + tmpY)).growX().marginLeft(12f).fillY().row();
                button("Copy Unit Coords", Icon.copy, Styles.cleart, () -> Core.app.setClipboardText((tmpX * 8 + 4) + ", " + (tmpY * 8 + 4))).growX().marginLeft(12f).fillY().row();
    
                pane(con -> {
                    con.button(Icon.leftOpen, Styles.cleari, () -> spawnNum = Mathf.clamp(--spawnNum, 1, 100)).size(LEN - OFFSET * 1.5f);
                    con.slider(1, 100, 2, spawnNum, (f) -> spawnNum = (int)f).growX().height(LEN - OFFSET * 1.5f).padLeft(OFFSET / 2).padRight(OFFSET / 2);
                    con.button(Icon.rightOpen, Styles.cleari, () -> spawnNum = Mathf.clamp(++spawnNum, 1, 100)).size(LEN - OFFSET * 1.5f);
                }).growX().height(LEN).row();
    
                table(con -> {
                    con.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
                        selectPos(starter, p -> {
                            tmpX = p.x;
                            tmpY = p.y;
                            point.set(World.unconv(p.x), World.unconv(p.y));
                        });
                    }).grow();
                    con.button(Icon.cancel, Styles.cleari, () -> point.set(-1, -1)).size(LEN);
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
                        con.button("Debug", Styles.cleart, () -> {
                            new DebugDialog("debug").show();
                        }).size(LEN * 2, LEN);
                        con.button("Pixel Art", Styles.cleart, () -> {
                            selectPos(starter, po -> {
                                PixelArtGenerator.leftDown.set(po.x, po.y);
                                Log.info(po.x + " | " + po.y);
                                Core.app.post(() -> {
                                    selectPos(starter, poi -> {
                                        PixelArtGenerator.rightTop.set(poi.x, poi.y);
                                        Log.info(poi.x + " | " + poi.y);
                                        platform.showMultiFileChooser(fi -> {
                                            PixelArtGenerator.toRead = fi;
                                            boolean b = PixelArtGenerator.process();
                                            if(b)Vars.ui.showInfoToast("Generate Successful", 1);
                                            else Vars.ui.showInfoToast("Generate Failed", 1);
                                        }, "png");
                                    });
                                });
                            });
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
    
    private static final Table starter = new Table(Tex.paneSolid){
    
    };
    
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
        
        starter.update(() -> {
            starter.setPosition(0, (Core.graphics.getHeight() - starter.getHeight()) / 2f);
    
            if(Core.input.mouseX() < 120 && starter.x < -1 && !starter.hasActions()){
                starter.actions(Actions.moveTo(0, (Core.graphics.getHeight() - starter.getHeight()) / 2f, 0.1f));
            }else if(starter.x > -1 && !starter.hasActions())starter.actions(Actions.moveTo(-starter.getWidth(), (Core.graphics.getHeight() - starter.getHeight()) / 2f, 0.1f));
        });
        starter.visible(() -> !state.isMenu() && ui.hudfrag.shown && (!net.client() || NewHorizon.DEBUGGING) && starter.color.a > 0.01f);
        starter.touchable(() -> !state.isMenu() && ui.hudfrag.shown && !net.client() ? Touchable.enabled : Touchable.disabled);
        
        Player player = Vars.player;
        
        Boolp hasInner = () -> starter.getChildren().contains(e -> "INNER".equals(e.name));
        
        starter.table(table -> {
            table.defaults().size(starter.getWidth() - OFFSET);
            table.button(Icon.settings, Styles.cleari, () -> {
                showInner(starter, new ToolTable());
            }).grow().disabled(b -> hasInner.get()).row();
        }).grow().row();
        
        Core.scene.root.addChildAt(1, starter);
    }
    
    public static void buildBulletTypeInfo(Table t, BulletType type){
        t.table(table -> {
            if(type == null)return;
            Class<?> typeClass = type.getClass();
            Field[] fields = typeClass.getFields();
            for(Field field : fields){
                try{
                    if(field.getGenericType().toString().equals("boolean")) table.add(new StringBuilder().append("[gray]").append(field.getName()).append(": ").append(judge(field.getBoolean(type))).append("[]")).left().row();
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
    
    public static void selectPos(Table parentT, Cons<Point2> cons){
        Prov<Touchable> original = parentT.touchablility;
        Touchable parentTouchable = parentT.touchable;
        
        parentT.touchablility = () -> Touchable.disabled;
    
        if(!pTable.hasParent())ctrlVec.set(Core.camera.unproject(Core.input.mouse()));
        
        if(!pTable.hasParent())pTable = new Table(Tex.clear){{
            update(() -> {
                if(Vars.state.isMenu()){
                    remove();
                }else{
                    Vec2 v = Core.camera.project(World.toTile(ctrlVec.x) * tilesize, World.toTile(ctrlVec.y) * tilesize);
                    setPosition(v.x, v.y, 0);
                }
            });
        }
            @Override
            public void draw(){
                super.draw();
        
                Lines.stroke(9, Pal.gray);
                drawLines();
                Lines.stroke(3, Pal.accent);
                drawLines();
//                DrawFunc.overlayText("(" + World.unconv(ctrlVec.x) + ", " + World.unconv(ctrlVec.y) + ")", x + LEN * 1, y + OFFSET, 0, Pal.accent, false);
            }
    
            private void drawLines(){
                Lines.square(x, y, 28, 45);
                Lines.line(x - OFFSET * 4, y, 0, y);
                Lines.line(x + OFFSET * 4, y, Core.graphics.getWidth(), y);
                Lines.line(x, y - OFFSET * 4, x, 0);
                Lines.line(x, y + OFFSET * 4, x, Core.graphics.getHeight());
            }
        };
    
        if(!pTable.hasParent())floatTable = new Table(Tex.clear){{
            update(() -> {
                if(Vars.state.isMenu())remove();
            });
            touchable = Touchable.enabled;
            setFillParent(true);
            
            addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                ctrlVec.set(Core.camera.unproject(x, y));//.clamp(-Vars.finalWorldBounds, -Vars.finalWorldBounds, world.unitHeight() + Vars.finalWorldBounds, world.unitWidth() + Vars.finalWorldBounds);
                return false;
                }
            });
        }};
        
//        ImageButton button = new ImageButton(Icon.cancel, Styles.emptyi){
//
//        };
        
        pTable.button(Icon.cancel, Styles.emptyi, () -> {
            cons.get(Tmp.p1.set(World.toTile(ctrlVec.x), World.toTile(ctrlVec.y)));
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
    
    public static void link(Table parent, Links.LinkEntry link){
        parent.add(new NHUIFunc.LinkTable(link)).size(NHUIFunc.LinkTable.w + OFFSET * 2f, NHUIFunc.LinkTable.h).padTop(OFFSET / 2f).row();
    }
}