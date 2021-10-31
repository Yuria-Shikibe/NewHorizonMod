package newhorizon.feature.cutscene;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Syncc;
import mindustry.io.TypeIO;
import newhorizon.feature.cutscene.packets.EventCompletePacket;
import newhorizon.func.EntityRegister;

import java.nio.FloatBuffer;

public class CutsceneEventEntity implements Cloneable, Entityc, Syncc{
	public static final EntityGroup<CutsceneEventEntity> events = new EntityGroup<>(CutsceneEventEntity.class, false, true);
	
	protected static boolean registeredLoad = false, registeredExit = false;
	
	public static void afterEnter(){
		registeredLoad = registeredExit = false;
	}
	
	public boolean added = false;
	public int id = EntityGroup.nextId();
	public transient long lastUpdated, updateSpacing;
	
	protected boolean inited = false;
	protected Table infoT;
	protected String name;
	protected CutsceneEvent eventType = CutsceneEvent.NULL_EVENT;
	
	public float reload, reload_LAST_, reload_TARGET_;
	public Object data;
	
	public Interval timer = new Interval(6);
	
	public CutsceneEventEntity(){
		name = eventType.name;
	}
	
	public <T> T data(){
		return (T)data;
	}
	
	@Override
	public void update(){
		if(!eventType.exist.get()){
			remove();
			return;
		}
		
		if(eventType.updatable)eventType.updateEvent(this);
	}
	
	@Override
	public void remove(){
		Groups.sync.remove(this);
		Groups.all.remove(this);
		events.remove(this);
		
		if(!Vars.headless && !eventType.isHidden && UIActions.eventTable() != null)eventType.removeTable(this, UIActions.eventTable());
		eventType.onRemove(this);
	}
	
	@Override
	public void add(){
		if(added)return;
		
		Groups.sync.add(this);
		Groups.all.add(this);
		events.add(this);
		
		added = true;
		
		timer = new Interval(6);
		timer.clear();
		
		if(!eventType.initOnce || !inited){
			eventType.onCall(this);
			inited = true;
		}
		
		
		if(!Vars.headless && !eventType.isHidden)show(UIActions.eventTable());
	}
	
	@Override
	public void read(Reads reads){
		if(!registeredLoad){
			String code  = TypeIO.readString(reads);
			if(code == null || code.isEmpty())code = "print('Empty Register');";
			Log.info("READ | " + code);
			CutsceneScript.runJS(code);
			
			registeredLoad = true;
			registeredExit = false;
		}
		
		inited = reads.bool();
		name = reads.str();
		
		eventType = CutsceneEvent.cutsceneEvents.get(name);
		
		
		setType(eventType);
		
		eventType.read(this, reads);
		
		afterRead();
	}
	
	@Override
	public void write(Writes writes){
		Log.info(registeredExit);
		if(!registeredExit){
			String code = CutsceneScript.getScript();
			if(code == null || code.isEmpty())code = "print('Empty Register');";
			Log.info("SAVE | " + code);
			TypeIO.writeString(writes, code);
			
			registeredExit = true;
			registeredLoad = false;
		}
		
		writes.bool(inited);
		writes.str(name);
		
		eventType.write(this, writes);
	}
	
	public void act(){
		eventType.triggered(this);
		
		if(Vars.net.active()){
			EventCompletePacket packet = new EventCompletePacket();
			packet.entity = this;
			Vars.net.send(packet, true);
		}
		
		if(eventType.removeAfterTriggered)remove();
	}
	
	public void netAct(){
		eventType.triggered(this);
		
		if(eventType.removeAfterTriggered)remove();
	}
	
	public void show(Table table){
		eventType.setupTable(this, table);
	}
	
	
	public CutsceneEventEntity copy(){
		try{
			CutsceneEventEntity n = (CutsceneEventEntity)super.clone();
			n.added = false;
			n.id(EntityGroup.nextId());
			return n;
		}catch(CloneNotSupportedException e){
			throw new AssertionError();
		}
	}
	
	public void setType(CutsceneEvent type){
		this.eventType = type == null ? CutsceneEvent.NULL_EVENT : type;
		name = eventType.name;
	}
	
	@Override public void afterRead(){eventType.afterRead(this);}
	@Override public void readSync(Reads reads){
		inited = reads.bool();
		eventType = CutsceneEvent.cutsceneEvents.get(reads.str());
		
		eventType.read(this, reads);
	}
	@Override public void writeSync(Writes writes){
		writes.bool(inited);
		writes.str(name);
		
		eventType.write(this, writes);
	}
	@Override public void afterSync(){eventType.afterSync(this);}
	
	@Override public int id(){return id;}
	@Override public void id(int i){this.id = i;}
	@Override public boolean isAdded(){return added;}
	
	@Override public boolean isLocal(){return !Vars.headless;}
	@Override public boolean isRemote(){return Vars.headless;}
	@Override public boolean isNull(){return this.eventType == CutsceneEvent.NULL_EVENT;}
	@Override public long lastUpdated(){return lastUpdated;}
	@Override public void lastUpdated(long l){lastUpdated = l;}
	@Override public long updateSpacing(){return updateSpacing;}
	@Override public void updateSpacing(long l){updateSpacing = l;}
	@Override public <T extends Entityc> T self(){return (T)this;}
	@Override public <T> T as(){return (T)this;}
	@Override public int classId(){return EntityRegister.getID(CutsceneEventEntity.class);}
	@Override public boolean serialize(){return true;}

	@Override
	public void snapSync(){
		updateSpacing = 16;
		lastUpdated = Time.millis();
		reload_LAST_ = reload_TARGET_;
		reload = reload_TARGET_;
	}
	
	@Override
	public void snapInterpolation(){
		updateSpacing = 16;
		lastUpdated = Time.millis();
		reload_LAST_ = reload;
		reload_TARGET_ = reload;
	}
	
	@Override
	public void readSyncManual(FloatBuffer buffer){
		if(lastUpdated != 0) updateSpacing = Time.timeSinceMillis(lastUpdated);
		lastUpdated = Time.millis();
		reload_LAST_ = reload;
		reload_TARGET_ = buffer.get();
	}
	
	@Override
	public void writeSyncManual(FloatBuffer buffer){
		buffer.put(reload);
	}
	
	@Override
	public void interpolate(){
		if(lastUpdated != 0 && updateSpacing != 0) {
			float timeSinceUpdate = Time.timeSinceMillis(lastUpdated);
			float alpha = Math.min(timeSinceUpdate / updateSpacing, 2f);
			reload = (Mathf.slerp(reload_LAST_, reload_TARGET_, alpha));
		} else if(lastUpdated != 0) {
			reload = reload_TARGET_;
		}
	}
	
	@Override
	public String toString(){
		return Time.millis() + "CutsceneEventEntity{" + "added=" + added + ", id=" + id + '}';
	}
}

