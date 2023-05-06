package newhorizon.expand.eventsys;

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
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NHRegister;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.annotation.Customizable;
import newhorizon.expand.eventsys.annotation.NumberParam;
import newhorizon.expand.eventsys.annotation.Parserable;
import newhorizon.expand.eventsys.annotation.Pos;
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
	
	public static WorldEventType inbuilt(WorldEventType type){
		addInbuilt(type);
		return type;
	}
	
	public static final WorldEventType NULL = new WorldEventType("null");
	
	static{
		allTypes.remove(NULL.name);
	}
	
	public boolean hasCoord = false;
	
	@Customizable public boolean fadeUI = false;
	@Customizable @Parserable(value = String.class, params = {Void.class}) public final String name;
	@Customizable public boolean removeAfterTrigger;
	
	public boolean drawable = false, minimapMarkable = false;
	public boolean warnOnHUD = true;
	
	@Customizable @Pos
	@Parserable(value = Integer.class, params = {Point2.class})
	@NumberParam()
	public int initPos = -1;
	
	public Prov<? extends WorldEvent> eventProv = WorldEvent::new;
	
	public WorldEventType(String name){
		if(allTypes.keys().toSeq().contains(name))throw new IllegalArgumentException("Existed Name For A World Event");
		this.name = name;
		addType(this);
	}
	
	public <T extends WorldEvent> T create(){
		T event = (T)eventProv.get();
		event.type = this;
		event.add();
		event.init();
		
		if(initPos != -1 && hasCoord){
			Tmp.p1.set(Point2.unpack(initPos));
			event.set(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
		}
		
		return event;
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
	
	/**
	 * if the percentage < 0 means that the event doesn't have a progress.
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
		if(warnOnHUD && NHRegister.worldLoaded() && !Vars.headless && event.team != Vars.player.team() && !event.IOed)warnHUD(event);
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
