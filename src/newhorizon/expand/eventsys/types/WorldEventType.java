package newhorizon.expand.eventsys.types;

import arc.func.Prov;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.scene.ui.Image;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Layer;
import mindustry.graphics.MinimapRenderer;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NHRegister;
import newhorizon.NHUI;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.cutscene.actions.CSSActions;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.util.annotation.ClientDisabled;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class WorldEventType implements Json.JsonSerializable{
	public static final ObjectMap<String, WorldEventType> allTypes = new ObjectMap<>();
	public static final ObjectMap<String, WorldEventType> costumeTypes = new ObjectMap<>();
	
	public static WorldEventType getStdType(String name){
		return allTypes.get(name);
	}
	
	public static <T extends WorldEventType> T getCastType(String name){
		return (T)allTypes.get(name);
	}
	
	public static void addType(WorldEventType type){
		allTypes.put(type.name, type);
		costumeTypes.put(type.name, type);
	}
	
	public static void addInbuilt(WorldEventType type){
		if(NewHorizon.loadedComplete())throw new AssertionError("Inbuilt Should Be Constructed During Loading");
		if(costumeTypes.remove(type.name) == null)throw new IllegalArgumentException("Should Be Already Added To Costume Map");;
	}
	
	public static void clearCustom(){
		for(String key : costumeTypes.keys()){
			allTypes.remove(key);
			costumeTypes.remove(key);
		}
	}
	
	public static WorldEventType inbuilt(WorldEventType type){
		addInbuilt(type);
		return type;
	}
	
	public static final WorldEventType NULL = new WorldEventType("null"){};
	
	static{
		allTypes.remove(NULL.name);
	}
	
	public boolean hasCoord = false;
	
	public boolean fadeUI = false;
	public final String name;
	public boolean removeAfterTrigger;
	
	public boolean drawable = false, minimapMarkable = false;
	public boolean warnOnHUD = true;
	
	public int initPos = -1;
	
	public Prov<? extends WorldEvent> eventProv = WorldEvent::new;
	
	public WorldEventType(String name){
		if(allTypes.containsKey(name))throw new IllegalArgumentException("Existed Name For A World Event");
		this.name = name;
		addType(this);
	}
	
	@ClientDisabled
	public <T extends WorldEvent> T create(){
		T event = (T)eventProv.get();
		event.type = this;
		
		event.init();
		
		if(initPos != -1 && hasCoord){
			Tmp.p1.set(Point2.unpack(initPos));
			event.set(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
		}
		
		event.add();
		
		return event;
	}
	
	public Table buildSimpleTable(WorldEvent e){
		Table button = new Table(){{
			TextButton tb1 = new TextButton("", Styles.cleart);
			tb1.label(e::info).padLeft(4f);
			tb1.marginLeft(4f);
			tb1.stack(/*new Image(Styles.black5),*/ new Image(e.type.icon())).size(38);
			tb1.getCells().reverse();
			tb1.clicked(() -> {
				e.type.showAsDialog(e);
			});
			tb1.setColor(e.team.color);
			
			add(tb1).grow();
			
			if(e.type.hasCoord){
				button(Icon.eyeSmall, Styles.clearNonei, 40f, () -> {
					CSSActions.check(e.x, e.y);
				}).width(40).padLeft(6f).fillX().growY().get().setColor(e.team.color);
			}
		}};
		
		return new Table(t -> {
			t.defaults().growX().fillY().padBottom(6f);
			
			if(e.type.progressRatio(e) >= 0)t.stack(new Bar("", e.team.color, () -> e.type.progressRatio(e)), button).tooltip(NHUI.eventDialog.tooltipModifier.get(e));
			else t.add(button).tooltip(NHUI.eventDialog.tooltipModifier.get(e));
			
			t.update(() -> {
				if(!e.added)t.remove();
			});
		});
	}
	
	public Position source(WorldEvent event){
		return null;
	}
	
	public Position target(WorldEvent event){
		return null;
	}
	
	public TextureRegion icon(){
		return NHContent.objective;
	}
	
	public float range(WorldEvent event){
		return 0;
	}
	
	public void drawMinimap(WorldEvent event, MinimapRenderer minimap){
		minimap.transform(Tmp.v1.set(event.x, event.y));
		
		float rad = minimap.scale(range(event));
		float fin = Interp.pow2Out.apply((Time.globalTime / 100f) % 1f);
		
		Draw.color(Tmp.c1.set(event.team.color).lerp(Color.white, Mathf.absin(Time.globalTime, 4f, 0.4f)));
		
		float size = minimap.scale((float)icon().width / tilesize);
		Draw.rect(icon(), Tmp.v1.x, Tmp.v1.y, size, size);
		
		Lines.stroke(Scl.scl((1f - fin) * 4.5f + 0.15f));
		Lines.circle(Tmp.v1.x, Tmp.v1.y, rad * fin);
		
		fin = Interp.circleOut.apply((Time.globalTime / 50f) % 1f);
		Lines.stroke(Scl.scl((1f - fin) * 2.5f));
		Lines.circle(Tmp.v1.x, Tmp.v1.y, rad);
		
		Draw.reset();
	}
	
	
	public void trigger(WorldEvent event){
		if(removeAfterTrigger)event.remove();
	}
	
	public void editorTable(Table table){
	
	}
	
	public void warnOnTrigger(WorldEvent e){
	
	}
	
	public void buildDebugTable(WorldEvent e, Table table){
		table.table(Tex.sideline, t -> {
			t.table(i -> {
				i.defaults().growX().height(LEN - OFFSET);
				i.button("RUN", Icon.play, Styles.cleart, () -> trigger(e)).get().marginLeft(12f);
				i.button("REMOVE", Icon.cancel, Styles.cleart, e::remove).get().marginLeft(12f);
			}).growX().fillY();
		}).growX().fillY();
	}
	
	public void infoTable(Table table){
	
	}
	
	public void buildTable(WorldEvent event, Table table){
		table.add(name);
	}
	
	public void draw(WorldEvent e){
		Team team = e.team;
		
		float fin = progressRatio(e);
		
		Draw.blend(Blending.additive);
		Draw.z(Layer.legUnit + 1);
		Draw.color(team.color, Color.white, 0.075f);
		Draw.alpha(0.65f);
		
		float f = Interp.pow3Out.apply(Mathf.curve(1 - fin, 0, 0.01f));
		
		Draw.rect(icon(), e, NHContent.fleet.width * f * Draw.scl, NHContent.fleet.height * f * Draw.scl, 0);
		Lines.stroke(5f * f);
		Lines.circle(e.x, e.y, range(e) * (1 + Mathf.absin(4f, 0.055f)));
		
		DrawFunc.circlePercent(e.x, e.y, range(e) * (0.875f), fin, 0);
		
		Draw.reset();
		Draw.blend();
	}
	
	public void init(WorldEvent event){
		if(!hasCoord)event.x = event.y = Float.NaN;
	}
	
	public String coordText(WorldEvent event){
		if(!Float.isNaN(event.x) && !Float.isNaN(event.y))return "[[[accent]" +  (int)(event.x / 8) + ", " + (int)(event.y / 8) + "[]]";
		else return "";
	}
	
	/**
	 * if the percentage < 0 means that the event doesn't have progress.
	 * */
	public float progressRatio(WorldEvent event){
		return -1;
	}
	
	public boolean shouldUpdate(WorldEvent event){
		return true;
	}
	
	public void triggerNet(WorldEvent event){
	
	}
	
	public void updateEvent(WorldEvent event){
	
	}
	
	public void onRemove(WorldEvent event){
	
	}
	
	public void onAdd(WorldEvent event){
		if(drawable)event.drawSize = range(event);
		if(warnOnHUD && NHRegister.worldLoaded() && !Vars.headless){
			warnHUD(event);
			
			if(NHUI.eventSimplePane.visible)NHUI.eventSimplePane.add(NHUI.eventDialog.buildSimpleTable(event)).row();
		}
	}
	
	public void warnHUD(WorldEvent event){
		TableFunc.showToast(Icon.warning, "[#ff7b69]Event Detected", NHSounds.alert2);
	}
	
	public void afterSync(WorldEvent event){
	
	}
	
	public void read(WorldEvent event, Reads read){
	
	}
	
	public void write(WorldEvent event, Writes read){
	
	}
	
	public void readOnSync(WorldEvent event, Reads read){
	
	}
	
	public void writeOnSync(WorldEvent event, Writes read){
	
	}
	
	public String type(){
		return (getClass().getSimpleName().isEmpty() ? getClass().getSuperclass().getSimpleName() : getClass().getSimpleName()).toUpperCase().replaceAll("EVENTTYPE", "");
	}
	
	public void showAsDialog(WorldEvent event){
		new BaseDialog("Event: " + name){{
			cont.pane(t -> {
				t.background(Styles.black5);
				t.table(Tex.sideline, i -> {
					i.left().marginLeft(OFFSET * 1.5f).marginRight(OFFSET * 1.5f);
					i.left();
					infoTable(i);
				}).fill();
			}).grow().margin(LEN);
			
			addCloseButton();
		}}.show();
	}
	
	@Override
	public void write(Json json){
	
	}
	
	@Override
	public void read(Json json, JsonValue jsonData){
	
	}
}
