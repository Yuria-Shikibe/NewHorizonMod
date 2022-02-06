package newhorizon.util.feature.cutscene;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.geom.Position;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.ArcRuntimeException;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Displayable;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.NHGroups;
import newhorizon.util.annotation.HeadlessDisabled;
import newhorizon.util.feature.cutscene.packets.EventUICallPacket;
import org.jetbrains.annotations.NotNull;

import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

/**
 * The shell of {@link CutsceneEventEntity}<p>
 *
 * @author YURIA
 * @since 1.10.0
 *
 * */
public class CutsceneEvent implements Cloneable, Displayable{
	/**
	 * All {@link CutsceneEvent} are stored here. <p>
	 * CutsceneEvent constructed by external javascript will be removed after load another game.
	 *
	 * */
	public static final ObjectMap<String, CutsceneEvent> cutsceneEvents = new ObjectMap<>();
	public static CutsceneEvent eventHandled = null;
	
	/**
	 * Get a {@link CutsceneEvent} by its name.
	 * @return exist ? a specific Event : {@link CutsceneEvent#NULL_EVENT}.
	 * */
	@NotNull
	public static CutsceneEvent get(String name){
		if(has(name))return cutsceneEvents.get(name);
		else return CutsceneEvent.NULL_EVENT;
	}
	public static boolean has(String name){return cutsceneEvents.containsKey(name);}
	
	/** Construct a {@link CutsceneEvent} by using external <b>JavaScript Code</b>.
	 * @param prov JavaScript Code
	 * */
	public static CutsceneEvent construct(String prov){
		eventHandled = null;
		try{
			CutsceneScript.runJS(prov);
		}catch(Exception e){
			Log.err(e);
			Vars.ui.showException(e);
		}
		CutsceneEvent eventType = eventHandled;
		eventHandled = null;
		return eventType == null ? NULL_EVENT : eventType;
	}
	
	/** @param event To be checked event.*/
	public static boolean inValidEvent(CutsceneEvent event){
		return event == null || event == NULL_EVENT;
	}
	
	public static final CutsceneEventEntity NULL_INSTANCE = new CutsceneEventEntity();
	
	/** A Event referred to null*/
	public static final CutsceneEvent NULL_EVENT = new CutsceneEvent("NULL_EVENT"){{
		removeAfterTriggered = true;
		isHidden = true;
		cannotBeRemove = true;
		updatable = false;
		drawable = false;
		exist = e -> false;
	}
		
		@Override
		public CutsceneEventEntity setup(){
			return NULL_INSTANCE;
		}
		
		@Override
		public void display(Table table){
			table.add(Iconc.cancel + " " + Core.bundle.get("nh.cutscene.empty-event")).color(Pal.redderDust).row();
		}
	};
	
	/** Used for builtin events, don't use is external code.*/
	public boolean cannotBeRemove = false;
	
	/** The name of the event*/
	public String name;
	
	/** May will be used*/
	public Position position;
	public float reloadTime = 180;
	
	/** Only run {@link CutsceneEvent#onCall(CutsceneEventEntity)} once.*/
	public boolean initOnce = true;
	
	/** Whether the event will be removed after it is triggered.*/
	public boolean removeAfterTriggered = false;
	
	/** Whether the event will be removed after the sector is captured.*/
	public boolean removeAfterVictory = true;
	
	/** Whether the event will be showed on HUD.*/
	public boolean isHidden = false;
	
	/** Whether the event will be updated.*/
	public boolean updatable = true;
	
	/** Whether the event will be draw.*/
	public boolean drawable = false;
	
	/** Whether the event should exist.*/
	public Func<CutsceneEventEntity, Boolean> exist = e -> true;
	/** Whether the event should update.*/
	public Func<CutsceneEventEntity, Boolean> updatability = e -> true;
	
	protected CutsceneEvent(String name, boolean register){
		this.name = name;
		if(register){
			cutsceneEvents.put(name, this);
		}
		
		cannotBeRemove = !NewHorizon.contentLoadComplete;
	}
	
	public CutsceneEvent(String name){
		this(name, true);
	}
	
	public CutsceneEvent(){this("null", false);}
	
	/**
	 * Used to generate an event entity safely.<p>
	 *
	 * Add the event to the {@link mindustry.gen.Groups#all} and {@link NHGroups#event}.
	 *
	 * @return The created {@link CutsceneEventEntity}.
	 * */
	@SuppressWarnings("UnusedReturnValue")
	public CutsceneEventEntity setup(){
		if(name.equals("null"))throw new ArcRuntimeException("Illegal Event #" + System.identityHashCode(this) + "[!]RENAME IT!");
		CutsceneEventEntity entity = Pools.obtain(CutsceneEventEntity.class, CutsceneEventEntity::new);
		entity.setType(this);
		
		if(!Vars.net.client())entity.add();
//		if(!UIActions.disabled()){
//			onCallUINet(entity);
//			onCallUI(entity);
//		}
		
		return entity;
	}
	
	/** What to draw in this event.*/
	public void draw(CutsceneEventEntity e){
	
	}
	
	/** What to update in this event every moment.*/
	public void updateEvent(CutsceneEventEntity e){
	
	}
	
	/** What to do while the event entity is removed.*/
	public void onRemove(CutsceneEventEntity e){
	
	}
	
	/** What to do while the event entity is created.*/
	public void onCall(CutsceneEventEntity e){
	
	}
	
	/**
	 * What to do (with UI) while the event entity is created.
	 * Only act in clients.
	 *
	 * */
	@HeadlessDisabled
	public void onCallUI(CutsceneEventEntity e){
	
	}
	
	public void onCallUINet(CutsceneEventEntity e){
		Vars.net.send(new EventUICallPacket(e), true);
	}
	
	/** What to show about this event.*/
	@HeadlessDisabled
	public void setupTable(CutsceneEventEntity e, Table table){
	
	}
	
	/** Used for special {@link arc.scene.Action} if needed.*/
	@HeadlessDisabled
	public void removeTable(CutsceneEventEntity e, Table table){
		e.infoT.remove();
	}
	
	/** What to do while the event entity is triggered.*/
	public void triggered(CutsceneEventEntity e){
	
	}
	
	/** What to do after {@link mindustry.gen.Entityc#read(Reads)}*/
	public void afterRead(CutsceneEventEntity e){
	
	}
	
	/** What to do after {@link NetClient#sync()}*/
	@SuppressWarnings("JavadocReference")
	public void afterSync(CutsceneEventEntity e){
	
	}
	
	
	public void write(CutsceneEventEntity e, Writes writes){
	
	}
	
	public void read(CutsceneEventEntity e, Reads reads){
	
	}
	
	/** What to do while initializing.*/
	public void setType(CutsceneEventEntity e){
	
	}
	
	/** Used for debug table.*/
	public void debugTable(CutsceneEventEntity e, Table table){
		table.table(Tex.pane, t -> {
			t.add(name + "|" + e.id).growX().fillY().row();
			t.add(e.infoT).growX().fillY();
			t.row().image().growX().height(OFFSET / 3).pad(OFFSET / 2).color(Color.lightGray).row();
			t.table(i -> {
				i.defaults().growX().height(LEN - OFFSET);
				i.button("RUN", Icon.play, Styles.transt, e::act);
				i.button("REMOVE", Icon.cancel, Styles.transt, e::remove);
			}).growX().fillY();
		}).growX().fillY();
	}
	
	@Override
	public String toString(){
		return "EventType: " + name;
	}
	
	public <T extends CutsceneEvent> T copyAnd(Class<T> c, String name, Cons<T> modifier){
		try{
			T clone = (T)super.clone();
			clone.name = name;
			modifier.get(clone);
			cutsceneEvents.put(name, clone);
			
			return clone;
		}catch(CloneNotSupportedException e){
			throw new AssertionError();
		}
	}
	
	public static void writeEvent(CutsceneEvent event, Writes writes){
		writes.str(event.name);
	}
	
	public static CutsceneEvent readEvent(Reads reads){
		return get(reads.str());
	}
	
	@Override
	public void display(Table table){
	
	}
	
	public String type(){
		return getClass().getSimpleName().isEmpty() ? getClass().getSuperclass().getSimpleName() : getClass().getSimpleName();
	}
	
	public void showAsDialog(){
		new BaseDialog("Event: " + name){{
			cont.pane(t -> {
				t.background(Styles.black5);
				t.table(Tex.sideline, i -> {
					i.left().marginLeft(OFFSET * 1.5f).marginRight(OFFSET * 1.5f);
					i.left();
					display(i);
				}).fill();
			}).grow().margin(LEN);
			
			addCloseButton();
		}}.show();
	}
}
