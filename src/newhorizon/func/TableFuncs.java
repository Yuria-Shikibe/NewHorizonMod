package newhorizon.func;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.TextArea;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Cicon;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

import static mindustry.Vars.*;

public class TableFuncs {
    private static final int tableZ = 2;
    private static final DecimalFormat df = new DecimalFormat("######0.00");
    private static String sx = "", sy = "";
    private static final TextArea xArea = new TextArea(""), yArea = new TextArea("");
    private static boolean autoMove, onBoost, floatTable, isInner;
    private static final Vec2 point = new Vec2(-1, -1);
    private static int spawnNum = 1;
    private static Team selectTeam = Team.sharded;
    private static UnitType selected = UnitTypes.alpha;
    
    private static void setStr(){
        sx = sy = "";
    }
    private static void setText(){
        xArea.setText(sx);
        yArea.setText(sy);
    }
    private static boolean pointValid(){
        return point.x >= 0 && point.y >= 0 && point.x <= world.width() * tilesize && point.y <= world.height() * tilesize;
    }
    
    private static class Inner extends Table{
        Inner(){
            background(Tex.button);
            isInner = true;
            setSize(LEN * 8.5f, (LEN + OFFSET) * 3);
            button(Icon.cancel, Styles.clearTransi, () -> {
                isInner = false;
                setStr();
                remove();
            }).padRight(OFFSET).size(LEN, getHeight() - OFFSET * 3).left();
            update(() -> {
                if(Vars.state.isMenu()){
                    remove();
                    isInner = false;
                    setStr();
                }
                setPosition(starter.getWidth(), (Core.graphics.getHeight() - getHeight()) / 2f);
            });
            
            new Table(Tex.clear){{
                update(() -> {
                    if(Vars.state.isMenu() || !isInner)remove();
                });
                touchable = Touchable.enabled;
                setFillParent(true);
                addListener(new InputListener(){
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                        point.set(Core.camera.unproject(x, y));
                        sx = String.valueOf(format(point.x / tilesize));
                        sy = String.valueOf(format(point.y / tilesize));
                        setText();
                        setFloatP();
                        return true;
                    }
                });
                Core.scene.add(this);
            }};
            Core.scene.add(this);
        }
    }
    private static class UnitSpawnTable extends Table{
        UnitSpawnTable(){
            Table in = new Table(out -> {
                out.pane(table -> {
                    int num = 0;
                    for(UnitType type : content.units()){
                        if(type.isHidden()) continue;
                        if(num % 5 == 0) table.row();
                        table.button(new TextureRegionDrawable(type.icon(Cicon.xlarge)), Styles.clearTogglei, LEN, () -> selected = type).update(b -> b.setChecked(selected == type)).size(LEN);
                        num++;
                    }
                }).fillX().height(LEN * 5f).row();
                Table t = new Table(tin -> {
                    tin.pane(con -> {
                        con.button("Switch Team", Icon.refresh, () -> {
                            player.team(player.team().id == Team.sharded.id ? state.rules.waveTeam : Team.sharded);
                        }).size(LEN * 4, LEN).update(b -> b.setColor(player.team().color));
                    }).fillX().height(LEN).row();
                    tin.pane(con -> {
                        con.button(Icon.refresh, Styles.clearTransi, () -> selectTeam = selectTeam.id == state.rules.waveTeam.id ? Team.sharded : state.rules.waveTeam).size(LEN);
                        con.button(Icon.cancel, Styles.clearTransi, () -> point.set(-1, -1)).size(LEN);
                        con.slider(1, 100, 2, spawnNum, (f) -> spawnNum = (int)f).fill().height(LEN).row();
                    }).fillX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("SpawnP", Icon.link, Styles.cleart, () -> Functions.spawnUnit(selected, selectTeam, spawnNum, point.x, point.y)).disabled(b -> !pointValid()).size(LEN * 2, LEN);
                        con.button("SpawnC", Icon.add, Styles.cleart, () -> Functions.spawnUnit(selected, selectTeam, spawnNum, player.x, player.y)).size(LEN * 2, LEN);
                    }).fillX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("Remove Units", Styles.cleart, Groups.unit::clear).size(LEN * 2, LEN);
                        con.button("Remove Fires", Styles.cleart, () -> {
                            for(int i = 0; i < 20; i++) Time.run(i * Time.delta * 3, Groups.fire::clear);
                        }).size(LEN * 2, LEN);
                    }).fillX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("Add Items", Styles.cleart, () -> {
                            for(Item item : content.items()) player.team().core().items.add(item, 1000000);
                        }).size(LEN * 2, LEN);
                    }).fillX().height(LEN).row();
                    tin.pane(con -> {
                        con.button("Debug", Styles.cleart, () -> {
                            TableTexDebugDialog d = new TableTexDebugDialog("debug");
                            d.init();
                            d.show();
                        }).disabled(b -> !state.rules.infiniteResources && !NHSetting.getBool("@active.debug")).size(LEN * 2, LEN);
                    }).fillX().height(LEN).row();
                });
                out.pane(t).fillX().height(t.getHeight()).padTop(OFFSET).row();
            });
            pane(in).fillX().height(in.getHeight());
        }
    }
    private static final Table pTable = new Table(Tex.clear){{
        update(() -> {
            if(Vars.state.isMenu()){
                remove();
                floatTable = false;
            }else{
                if(pointValid()){
                    Vec2 v = Core.camera.project(point.x, point.y);
                    setPosition(v.x, v.y, 0);
                }else{
                    remove();
                    floatTable = false;
                }
            }
        });
        button(Icon.upOpen, Styles.emptyi, () -> {
            remove();
            floatTable = false;
        }).center();
    }};
    private static void setFloatP(){
        if(!floatTable){
            Core.scene.root.addChildAt(0, pTable);
            floatTable = true;
        }
    }
    private static final Table starter = new Table(Tex.button);
    public static final TextButton.TextButtonStyle toggletAccent = new TextButton.TextButtonStyle() {{
        this.font = Fonts.def;
        this.fontColor = Color.white;
        this.checked = Tex.buttonOver;
        this.down = Tex.buttonDown;
        this.up = Tex.button;
        this.over = Tex.buttonDown;
        this.disabled = Tex.buttonDisabled;
        this.disabledFontColor = Color.gray;
    }};
    public static final String tabSpace = "    ";
    public static final float LEN = 60f;
    public static final float OFFSET = 12f;
    public static String format(float value){return df.format(value);}
    public static String getJudge(boolean value){return value ? "[green]Yes[]" : "[red]No[]";}
    public static String getPercent(float value){return Mathf.floor(value * 100) + "%";}
    
    public static void disableTable(){
        Core.scene.root.removeChild(starter);
    }
    public static void showTable(){
        Core.scene.root.addChildAt(1, starter);
    }
    
    public static void tableMain(){
        starter.setSize(LEN + OFFSET, (LEN + OFFSET) * 3);
        starter.update(() -> {
            if(Vars.state.isMenu())starter.color.a = 0;
            else {
                if(starter.color.a < 1)starter.color.a = 1;
                starter.setPosition(0, (Core.graphics.getHeight() - starter.getHeight()) / 2f);
                
                Unit u = player.unit();
                if(autoMove && u != null){
                    if(u.dst(point) > tilesize){
                        u.moveAt(u.vel().trns(u.angleTo(point),Mathf.lerp(1.0F, (u.type.canBoost && onBoost) ? u.type.boostMultiplier : 1.0F, u.elevation) * u.speed()));
                        if(u.type.canBoost && onBoost)u.elevation = 1.0F;
                    }else{
                        point.set(-1, -1);
                        setStr();
                        pTable.remove();
                        floatTable = false;
                        setText();
                        autoMove = false;
                        onBoost = false;
                    }
                }
            }
        });
    
        Player player = Vars.player;
        
        starter.table(table -> table.button(Icon.admin, Styles.clearTransi, starter.getWidth() - OFFSET, () -> {
            Table inner = new Inner();
            Table unitTable = new UnitSpawnTable();
            Table uT = new Table(){{
                Label label = new Label("<<-Spawns: [accent]" + spawnNum + "[] ->>");
                Image image = new Image();
                Label p = new Label("");
                update(() -> {
                    image.setColor(selectTeam.color);
                    label.setText(new StringBuilder().append("<<-Spawns: [accent]").append(spawnNum).append("[] ->>"));
                    p.setText(new StringBuilder().append("At: ").append(point.x).append(", ").append(point.y).append(" ->>"));
                });
                table(table1 -> {
                    add(image).growX().height(OFFSET / 3).growY().pad(OFFSET / 2).row();
                    table1.table(t -> {
                        t.add(label).row();
                        t.add(p).row();
                    }).grow().row();
                }).fillX().growY();
            }};
            inner.table(Tex.button, cont -> {
                cont.table(t -> t.add(uT) ).growX().fillY().row();
                cont.table(t -> t.add(unitTable) ).height(mobile ? inner.getHeight() : unitTable.getHeight()).growX();
            }).growX().height(mobile ? inner.getHeight() : Core.graphics.getHeight() / 1.3f);
        }).size(LEN).disabled(b -> isInner || !NHSetting.getBool("@active.admin-panel")).row()).right().padTop(OFFSET).size(LEN).row();
        starter.table(table -> table.button(Icon.move, Styles.clearTransi, starter.getWidth() - OFFSET, () -> {
            Table inner = new Inner();
            inner.table(Tex.button, t -> {
                final float WIDTH = LEN * 2;
                t.table(bt -> {
                    bt.button("@confirm", Icon.export, () -> {
                        try{
                            point.set(Float.parseFloat(sx) * tilesize, Float.parseFloat(sy) * tilesize);
                            setFloatP();
                        }catch(NumberFormatException err){
                            point.set(player.x, player.y);
                        }
                    }).size(WIDTH, LEN).left().padBottom(OFFSET * 5f);
                    bt.button("@cancel", Icon.cancel, () -> {
                        point.set(-1, -1);
                        setStr();
                        pTable.remove();
                        autoMove = false;
                        setText();
                    }).disabled(b -> !pointValid()).size(WIDTH, LEN).padLeft(WIDTH).right().padBottom(OFFSET * 5f);
                }).height(LEN).padTop(OFFSET).row();
    
                t.add("Set Move Target").row();
                t.table(Tex.clear, t2 -> {
                    t2.add("[accent]X: ").left();
                    t2.add(xArea).left();
                    t2.update(() -> sx = xArea.getText());
                }).row();
                t.table(Tex.clear, t2 -> {
                    t2.add("[accent]Y: ").left();
                    t2.add(yArea).left();
                    t2.update(() -> sy = yArea.getText());
                }).row();
                
                t.table(bt -> {
                    bt.button("@move", Icon.rightOpen, toggletAccent, () -> {
                        autoMove = !autoMove;
                        if(player.unit() != null)player.unit().lookAt(point);
                    }).disabled(b -> !pointValid()).update(b -> b.setChecked(autoMove)).size(WIDTH, LEN).left().padTop(OFFSET * 2.75f);
                    bt.button("@boost", Icon.up, toggletAccent, () -> onBoost = !onBoost).disabled(b -> !autoMove || !Vars.player.unit().type.canBoost).update(b -> b.setChecked(onBoost)).size(WIDTH, LEN).padLeft(WIDTH).right().padTop(OFFSET * 2.75f);
                }).height(LEN).padTop(OFFSET);
                
            }).grow().right();
        }).size(LEN).disabled(b -> isInner).row()).right().padTop(OFFSET).size(LEN);
        Core.scene.root.addChildAt(1, starter);
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
                        
                        NHSetting.debug(() -> Log.info(inner));
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
    
    public static void tableImageShrink(TextureRegion tex, float size, Table table){
        float parma = Math.max(tex.height, tex.width);
        float f = Math.min(size, parma);
        table.image(tex).size(tex.width * f / parma, tex.height * f / parma);
    }
}
