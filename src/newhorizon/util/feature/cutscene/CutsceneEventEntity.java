package newhorizon.util.feature.cutscene;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Drawc;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Syncc;
import mindustry.io.TypeIO;
import newhorizon.expand.entities.NHBaseEntity;
import newhorizon.expand.entities.NHGroups;
import newhorizon.util.annotation.HeadlessDisabled;
import newhorizon.util.feature.cutscene.packets.EventCompletePacket;
import newhorizon.util.func.EntityRegister;

import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * Basic Event Entity that can act many types of things.<p>
 *
 *
 * This is the internal entity of {@link CutsceneEvent}.<p>
 * Define what the event will do in {@link CutsceneEventEntity#eventType} ({@link CutsceneEvent}), not here.<p>
 *
 * Use {@link CutsceneEventEntity#data} to store different types of things, such as {@link arc.struct.Seq}, {@link mindustry.gen.Building} or sth else.
 * In most cases, this class should not be extended to implement extra functions.<p>
 *
 * This event entity is position syncable and reload syncable. And it is savable. <p>
 *
 * If this event can be triggered in a client, {@link CutsceneEventEntity#syncAct()} should be used to act it to sync.
 *
 * @see CutsceneEvent
 * @see Syncc
 * @see Drawc
 * @see Entityc
 *
 * @author Yuria
 * */
public class CutsceneEventEntity extends NHBaseEntity implements Entityc, Syncc, Drawc{
	
	/** Used for js-custom event type register.*/
	protected static boolean registeredLoad = false, registeredExit = false;
	public static void afterIO(){registeredLoad = registeredExit = false;}
	
	public transient long lastUpdated, updateSpacing;
	
	/** Whether this event entity is the first time created.*/
	protected boolean inited = false;
	
	/** What the entity display on your HUD*/
	@HeadlessDisabled public Table infoT;
	
	/** What the entity display on your HUD*/
	protected CutsceneEvent eventType = CutsceneEvent.NULL_EVENT;
	
	/** Used for events that need a reloading time*/
	public float reload;
	
	/** Used for net sync*/
	public transient float reload_LAST_, reload_TARGET_;
	public transient float x_LAST_, x_TARGET_, y_LAST_, y_TARGET_;
	
	public boolean disabled = false;
	
	
	/** Used for special use like {@link mindustry.gen.Bullet#data}, {@link mindustry.entities.Effect.EffectContainer#data}*/
	public Object data;
	
	/** Used for short spacing and IO-Not-Required use*/
	public Interval timer = new Interval(6);
	
	/* Init the draw size*/
	{size = 500f;}
	
	/** Constructor, has nothing to do*/
	public CutsceneEventEntity(){}
	
	public CutsceneEvent eventType(){return eventType;}
	
	public <T> T data(){
		return (T)data;
	}
	
	
	@Override
	public void update(){
		if(!eventType.exist.get(this)){
			remove();
			return;
		}
		
		if(eventType.updatable)eventType.updateEvent(this);
	}
	
	@Override
	public void remove(){
		Groups.draw.remove(this);
		Groups.sync.remove(this);
		Groups.all.remove(this);
		NHGroups.events.remove(this);
		
		if(!UIActions.disabled() && !eventType.isHidden && UIActions.eventTable() != null)eventType.removeTable(this, UIActions.eventTable());
		eventType.onRemove(this);
		
		added = false;
	}
	
	@Override
	public void add(){
		if(added)return;
		
		Groups.draw.add(this);
		Groups.sync.add(this);
		Groups.all.add(this);
		NHGroups.events.add(this);
		
		added = true;
		
		if(!eventType.initOnce || !inited){
			eventType.onCall(this);
			inited = true;
		}
		
		if(!UIActions.disabled() && !eventType.isHidden)show(UIActions.eventTable());
	}
	
	@Override
	public void read(Reads reads){
		super.read(reads);
		
		
		if(!registeredLoad){
			String code  = TypeIO.readString(reads);
			if(code == null || code.isEmpty())code = "print('Empty Register');";
			CutsceneScript.runJS(code);
			
			registeredLoad = true;
			registeredExit = false;
		}
		
		inited = reads.bool();
		eventType = CutsceneEvent.readEvent(reads);
		
		setType(eventType);
		
		eventType.read(this, reads);
		
		afterRead();
	}
	
	@Override
	public void write(Writes writes){
		super.write(writes);
		
		if(!registeredExit){
			String code = CutsceneScript.getScript();
			if(code == null || code.isEmpty())code = "print('Empty Register');";
			TypeIO.writeString(writes, code);
			
			registeredExit = true;
			registeredLoad = false;
		}
		
		writes.bool(inited);
		CutsceneEvent.writeEvent(eventType, writes);
		
		eventType.write(this, writes);
	}
	
	/** Used for generic events that is synchronous.*/
	public void act(){
		eventType.triggered(this);
		
		if(eventType.removeAfterTriggered)remove();
	}
	
	/** Used for events that need synchronously act from client to server.*/
	public void syncAct(){
		if(Vars.net.active()){
			EventCompletePacket packet = new EventCompletePacket();
			packet.entity = this;
			Vars.net.send(packet, true);
		}
		
		act();
	}
	
	@HeadlessDisabled public void show(Table table){
		if(table == null)return;
		table.row();
		
		eventType.setupTable(this, table);
	}
	
	public void setType(CutsceneEvent type){
		eventType = type == null ? CutsceneEvent.NULL_EVENT : type;
		
		eventType.setType(this);
	}
	
	public void setupDebugTable(Table table){
		eventType.debugTable(this, table);
	}
	
	@Override public void afterRead(){
		eventType.afterRead(this);
	}
	
	@Override public void readSync(Reads reads){
		x = reads.f();
		y = reads.f();
		
		inited = reads.bool();
		eventType = CutsceneEvent.get(reads.str());
		
		eventType.read(this, reads);
	}
	@Override public void writeSync(Writes writes){
		writes.f(x);
		writes.f(y);
		
		writes.bool(inited);
		writes.str(eventType.name);
		
		eventType.write(this, writes);
	}
	@Override public void afterSync(){eventType.afterSync(this);}
	
	@Override public boolean isLocal(){return !Vars.headless;}
	@Override public boolean isRemote(){return Vars.headless;}
	@Override public boolean isNull(){return this.eventType == CutsceneEvent.NULL_EVENT;}
	@Override public long lastUpdated(){return lastUpdated;}
	@Override public void lastUpdated(long l){lastUpdated = l;}
	@Override public long updateSpacing(){return updateSpacing;}
	@Override public void updateSpacing(long l){updateSpacing = l;}
	
	@Override public int classId(){return EntityRegister.getID(CutsceneEventEntity.class);}
	@Override public boolean serialize(){return true;}
	
	@Override
	public void snapSync(){
		updateSpacing = 16;
		lastUpdated = Time.millis();
		reload_LAST_ = reload_TARGET_;
		reload = reload_TARGET_;
		
		x_LAST_ = x_TARGET_;
		x = x_TARGET_;
		
		y_LAST_ = y_TARGET_;
		y = y_TARGET_;
	}
	
	@Override
	public void snapInterpolation(){
		updateSpacing = 16;
		lastUpdated = Time.millis();
		reload_LAST_ = reload;
		reload_TARGET_ = reload;
		
		x_LAST_ = x;
		x_TARGET_ = x;
		
		y_LAST_ = y;
		y_TARGET_ = y;
	}
	
	@Override
	public void readSyncManual(FloatBuffer buffer){
		if(lastUpdated != 0) updateSpacing = Time.timeSinceMillis(lastUpdated);
		lastUpdated = Time.millis();
		reload_LAST_ = reload;
		reload_TARGET_ = buffer.get();
		
		x_LAST_ = x;
		x_TARGET_ = buffer.get();
		
		y_LAST_ = y;
		y_TARGET_ = buffer.get();
	}
	
	@Override
	public void writeSyncManual(FloatBuffer buffer){
		buffer.put(reload);
		buffer.put(x);
		buffer.put(y);
	}
	
	@Override
	public void interpolate(){
		if(lastUpdated != 0 && updateSpacing != 0) {
			float timeSinceUpdate = Time.timeSinceMillis(lastUpdated);
			float alpha = Math.min(timeSinceUpdate / updateSpacing, 2f);
			reload = (Mathf.slerp(reload_LAST_, reload_TARGET_, alpha));
			x = (Mathf.slerp(x_LAST_, x_TARGET_, alpha));
			y = (Mathf.slerp(y_LAST_, y_TARGET_, alpha));
		} else if(lastUpdated != 0) {
			reload = reload_TARGET_;
			x = x_TARGET_;
			y = y_TARGET_;
		}
	}
	
	@Override
	public String toString(){
		return "CutsceneEventEntity{" + "type=" + eventType + ", id=" + id + '}';
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		if(!(o instanceof CutsceneEventEntity)) return false;
		CutsceneEventEntity that = (CutsceneEventEntity)o;
		return id == that.id && eventType.equals(that.eventType);
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(id, eventType);
	}
	
	@Override
	public void draw(){
		if(eventType.drawable)eventType.draw(this);
	}
}

