package newhorizon.expand.entities;

import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.io.TypeIO;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.NHGroups;
import newhorizon.NHUI;
import newhorizon.expand.eventsys.types.WorldEventType;
import newhorizon.util.annotation.HeadlessDisabled;

import java.nio.FloatBuffer;

public class WorldEvent extends NHBaseEntity implements Posc, Drawc, Syncc, Teamc{
	public WorldEventType type = WorldEventType.NULL;
	public Team team = Team.derelict;
	
	public transient long lastUpdated;
	public transient long updateSpacing;
	
	protected transient float reload_LAST_;
	protected transient float reload_TARGET_;
	protected transient float x_LAST_;
	protected transient float x_TARGET_;
	protected transient float y_LAST_;
	protected transient float y_TARGET_;
	
	@HeadlessDisabled
	public transient int intData = 0;
	
	public float reload;
	public String name = "";
	public Object data;
	public Element ui;
	
	public Interval timer = new Interval();
	
	@Override
	public int classId(){
		return EntityRegister.getID(getClass());
	}
	
	@Override
	public float clipSize(){
		return type.drawable ? super.clipSize() : -1;
	}
	
	@Override
	public void draw(){
		if(type.drawable)type.draw(this);
	}
	
	@Override
	public void update(){
		if(type.shouldUpdate(this))type.updateEvent(this);
	}
	
	public void buildTable(Table table){
		type.buildTable(this, table);
	}
	
	public void buildDebugTable(Table table){
		type.buildDebugTable(this, table);
	}
	
	@Override
	public Building buildOn(){
		return Vars.world.buildWorld(x, y);
	}
	
	public boolean isSyncHidden(Player player) {
		return inFogTo(player.team());
	}
	
	@Override
	public void afterSync(){
	
	}
	
	@Override
	public boolean serialize(){
		return true;
	}
	
	@Override
	public void handleSyncHidden(){
		remove();
		Vars.netClient.clearRemovedEntity(id);
	}
	
	public void init(){
		type.init(this);
		
		name = type.name + "-" + id;
	}
	
	@Override
	public void add(){
		if(added)return;
		Groups.all.add(this);
		Groups.draw.add(this);
		
		Groups.sync.add(this);
		NHGroups.events.add(this);
		
		added = true;
		type.onAdd(this);
		
		if(!Vars.headless){
			NHUI.eventDialog.buildEvent(this);
		}
	}
	
	@Override
	public void remove(){
		if(!added)return;
		Groups.draw.remove(this);
		Groups.all.remove(this);
		Groups.sync.remove(this);
		NHGroups.events.remove(this);
		type.onRemove(this);
		
		added = false;
		
		if(ui != null){
			NHUI.eventDialog.layout();
			if(ui.parent != null){
				if(type.fadeUI){
					ui.parent.actions(Actions.delay(0.75f), Actions.fadeOut(1f, Interp.fade), Actions.remove());
				}
				else ui.parent.remove();
			}
			ui.remove();
		}
	}
	
	@Override
	public void readSync(Reads read){
		if (lastUpdated != 0L) {
			updateSpacing = Time.timeSinceMillis(lastUpdated);
		}
		
		lastUpdated = Time.millis();
		
		team = TypeIO.readTeam(read);
		name = read.str();
		type = WorldEventType.getStdType(read.str());
		
		if(!isLocal()) {
			x_LAST_ = x;
			x_TARGET_ = read.f();
			y_LAST_ = y;
			y_TARGET_ = read.f();
			reload_LAST_ = reload;
			reload_TARGET_ = read.f();
		}else {
			read.f();
			read.f();
			read.f();
			y_LAST_ = y;
			y_TARGET_ = y;
			x_LAST_ = x;
			x_TARGET_ = x;
			reload_LAST_ = reload;
			reload_TARGET_ = reload;
		}
		
		type.readOnSync(this, read);
		
		afterSync();
	}
	
	public void writeSync(Writes write) {
		TypeIO.writeTeam(write, team);
		write.str(name);
		write.str(type.name);
		write.f(x);
		write.f(y);
		write.f(reload);
		
		type.writeOnSync(this, write);
	}
	
	public String info(){
		return type.type() + "\n" + coordText();
	}
	
	public String coordText(){
		return type.coordText(this);
	}
	
	@Override
	public void snapInterpolation() {
		updateSpacing = 16L;
		lastUpdated = Time.millis();
		
		x_LAST_ = x;
		x_TARGET_ = x;
		y_LAST_ = y;
		y_TARGET_ = y;
		reload_LAST_ = reload;
		reload_TARGET_ = reload;
	}
	
	@Override
	public void snapSync() {
		updateSpacing = 16L;
		lastUpdated = Time.millis();
		
		x_LAST_ = x_TARGET_;
		x = x_TARGET_;
		y_LAST_ = y_TARGET_;
		y = y_TARGET_;
		reload_LAST_ = reload_TARGET_;
		reload = reload_TARGET_;
	}
	
	@Override
	public void afterRead(){
	}
	
	@Override
	public void read(Reads read){
		super.read(read);
		reload = read.f();
		name = read.str();
		team = TypeIO.readTeam(read);
		type = WorldEventType.getStdType(read.str());
		type.read(this, read);
		
		afterRead();
	}
	
	@Override
	public void write(Writes write){
		super.write(write);
		write.f(reload);
		write.str(name);
		TypeIO.writeTeam(write, team);
		write.str(type.name);
		type.write(this, write);
	}
	
	@Override public void updateSpacing(long updateSpacing){this.updateSpacing = updateSpacing;}
	@Override public long lastUpdated(){return lastUpdated;}
	@Override public long updateSpacing(){return updateSpacing;}
	@Override public void lastUpdated(long lastUpdated){this.lastUpdated = lastUpdated;}
	
	@Override
	public void interpolate() {
		if (lastUpdated != 0L && updateSpacing != 0L) {
			float timeSinceUpdate = (float)Time.timeSinceMillis(lastUpdated);
			float alpha = Math.min(timeSinceUpdate / (float)updateSpacing, 2.0F);
			x = Mathf.lerp(x_LAST_, x_TARGET_, alpha);
			y = Mathf.lerp(y_LAST_, y_TARGET_, alpha);
			reload = Mathf.lerp(reload_LAST_, reload_TARGET_, alpha);
		} else if (lastUpdated != 0L) {
			x = x_TARGET_;
			y = y_TARGET_;
			reload = reload_TARGET_;
		}
	}
	
	@Override
	public void readSyncManual(FloatBuffer buffer) {
		if (lastUpdated != 0L) {
			updateSpacing = Time.timeSinceMillis(lastUpdated);
		}
		
		lastUpdated = Time.millis();
		x_LAST_ = x;
		x_TARGET_ = buffer.get();
		y_LAST_ = y;
		y_TARGET_ = buffer.get();
		reload_LAST_ = reload;
		reload_TARGET_ = buffer.get();
	}
	
	@Override
	public void writeSyncManual(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(reload);
	}
	
	@Override
	public boolean inFogTo(Team viewer) {
		if (team != viewer && Vars.state.rules.fog) {
			if (drawSize <= 16.0F) {
				return !Vars.fogControl.isVisible(viewer, x, y);
			} else {
				float trns = drawSize / 2.0F;
				
				for(Point2 p : Geometry.d8){
					if(Vars.fogControl.isVisible(viewer, x + (float)p.x * trns, y + (float)p.y * trns)){
						return false;
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public boolean cheating() {
		return team.rules().cheat;
	}
	
	public float ratio(){
		return type.progressRatio(this);
	}
	
	@Override
	public Team team(){
		return team;
	}
	
	@Override
	public CoreBlock.CoreBuild closestCore() {
		return Vars.state.teams.closestCore(x, y, team);
	}
	
	@Override
	public CoreBlock.CoreBuild closestEnemyCore() {
		return Vars.state.teams.closestEnemyCore(x, y, team);
	}
	
	@Override
	public CoreBlock.CoreBuild core(){
		return team.core();
	}
	
	@Override
	public void team(Team team){
		this.team = team;
	}
	
	@Override
	public String toString(){
		return "WorldEvent: " + type.name + " | ID: " + id;
	}
}
