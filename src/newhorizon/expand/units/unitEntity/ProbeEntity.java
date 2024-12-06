package newhorizon.expand.units.unitEntity;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.audio.SoundLoop;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.gen.Sounds;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.UnitType;
import newhorizon.content.NHInbuiltEvents;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.util.func.BuildingConcentration;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHMath;
import newhorizon.util.graphic.DrawFunc;

public class ProbeEntity extends UnitEntity{
	public ObjectSet<Building> scanned = new ObjectSet<>();
	public int scannedSize = 0;
	
	public Team targetTeam = null;
	
	public float scanRange = 240;
	public final float SCAN_WARMUP_SPEED = 0.0075f;
	public final float SCAN_COOL_SPEED = 0.015f;
	public final float SCAN_SHIFT_SPEED = 0.0875f;
	public final float SCAN_SPEED = 0.0075f;
	
	public final float UNSCAN_CLOAK_RELOAD = 180f;
	
	public Color scanColor = Pal.techBlue;
	public float scanRotateScl = 180f;
	public int scanMatWidth = 10; //100
	public float scanMatSpacing = 6f;
	
	public Vec2 scanPos = new Vec2();
	public Building scanTarget = null;
	public boolean scanning = false;
	public float scanWarmup = 0;
	
	public float lastSize = 0;
	
	public int leastScan = 14;
	public int minComplexSize = 16;
	
	public float scanSourceX = 0.5f, scanSourceY = 3;
	
	public float unscanCloakReload = 0;
	
	public Interval timer = new Interval(2);
	
	protected static final Vec2 tmpVec = new Vec2();
	
	public SoundLoop scanSound = new SoundLoop(Sounds.bioLoop, 0.9f);
	
	//[0, 1]
	public float scanProgress = 0;
	
	@Override public int classId(){return EntityRegister.getID(ProbeEntity.class);}
	
	@Override
	public void afterRead(){
		super.afterRead();
		
		scanPos.set(x, y);
		
		if(targetTeam == null)targetTeam = Vars.state.rules.defaultTeam;
	}
	
	@Override
	public void setType(UnitType type){
		super.setType(type);
		
		scanRange = type.maxRange + 160f;
		
		if(targetTeam == null)targetTeam = Vars.state.rules.defaultTeam;
	}
	
	
	@Override
	public float clipSize(){
		return scanWarmup < 0.45f ? super.clipSize() : scanRange * 2f;
	}
	
	@Override
	public void add(){
		super.add();
		
		scanPos.set(x, y);
		scanRange = type.maxRange + 160f;
	}
	
	public float scanSpeedScl(){
		return scanTarget.block.hasPower ? 1.5f / (scanTarget.block.size) * (1 + scanTarget.power.status) / 2f * reloadMultiplier : 2;
	}
	
	@Override
	public boolean checkTarget(boolean targetAir, boolean targetGround){
		return super.checkTarget(targetAir, targetGround) && (scanning || unscanCloakReload >= UNSCAN_CLOAK_RELOAD);
	}
	
	public void updateJudging(){
		if(timer.get(80f)){
			if(scannedSize >= leastScan){
				EventHandler.get().post(() -> {
					Seq<BuildingConcentration.Complex> complexes = BuildingConcentration.getComplexes(scannedSize >= leastScan * 2 ? minComplexSize / 2 : minComplexSize, scanned.toSeq());
					if(complexes.any()){
						EventHandler.get().getSort().sort(complexes);
						BuildingConcentration.Complex complex = complexes.first();
						
						Core.app.post(() -> {
							spawnEvent(complex.priorityCoord.x, complex.priorityCoord.y);
							apply(NHStatusEffects.reinforcements, 120f);
						});
					}
				});
			}
		}
	}
	
	public boolean scanOver(){
		return hasEffect(NHStatusEffects.reinforcements);
	}
	
	public void spawnEvent(float x, float y){
		if(!Vars.net.client()){
			WorldEvent event = NHInbuiltEvents.dynamicRaid(NHInbuiltEvents.dynamicGrowth(targetTeam)).create();
			event.team = team;
			event.set(x, y);
		}
	}
	
	public void updateScan(){
		if(team == targetTeam)return;
		
		if(timer.get(1, (scanTarget != null && scanTarget.isValid()) ? 600 : 45))scanTarget = Vars.indexer.findTile(targetTeam, x, y, scanRange, b -> !scanned.contains(b));
		
		scanning = scanTarget != null && !scanOver();
		
		if(scanning){
			unscanCloakReload = 0;
			scanWarmup = Mathf.lerpDelta(scanWarmup, 1, SCAN_WARMUP_SPEED);
			if(scanWarmup > 0.995f){
				scanWarmup = 1;
			}
			
			scanSound.update(x, y, true, scanWarmup);
			
			lastSize = Mathf.lerpDelta(lastSize, scanTarget.block.size * 4 * Mathf.sqrt2, scanWarmup * SCAN_WARMUP_SPEED);
			scanPos.lerp(scanTarget, SCAN_SHIFT_SPEED * Time.delta);
			
			
			
			if(scanTarget.within(scanPos, lastSize / 1.5f))scanProgress = Mathf.approachDelta(scanProgress, 1.005f, SCAN_SPEED * scanSpeedScl());
			
			if(scanProgress >= 1){
				scanProgress = 0;
				scanned.add(scanTarget);
				scannedSize += scanTarget.block.size;
			}
		}else{
			scanSound.update(x, y, false, scanWarmup);
			unscanCloakReload += Time.delta;
			scanProgress = Mathf.approachDelta(scanProgress, 0, SCAN_SPEED * 5);
			scanWarmup = Mathf.lerpDelta(scanWarmup, 0, SCAN_COOL_SPEED);
			if(scanWarmup < 0.005f){
				scanWarmup = 0;
				scanPos.set(x, y);
			}
			
			scanPos.lerp(this, SCAN_SHIFT_SPEED * Time.delta);
		}
		
		updateJudging();
	}
	
	@Override
	public void update(){
		super.update();
		
		updateScan();
	}
	
	@Override
	public void draw(){
		float z = Draw.z();
		
		super.draw();
		
		drawScan();
		
		if(!scanning){
			Draw.z(Layer.buildBeam);
			
			Tmp.c1.set(scanColor).lerp(Color.white, Mathf.absin(5f, 0.325f));
			Fill.light(x, y, (int)(Lines.circleVertices(hitSize) * 0.75f), hitSize * 2f, Tmp.c1.a((0.925f + Mathf.absin(4, 0.07f)) * (1 - scanWarmup) * Mathf.curve(unscanCloakReload / UNSCAN_CLOAK_RELOAD, 0.9f, 1f)), Color.clear);
		}
		
		Draw.z(z);
	}
	
	public void drawScan(){
		if(scanWarmup < 0.425f)return;
		
		float z = Draw.z();
		
		Draw.z(Layer.effect);
		
		float rWarmup = Mathf.curve(scanWarmup, 0.425f, 0.95f);
		
		Vec2 drawScanPos = Tmp.v6.set(scanPos).add(Tmp.v1.trns(Time.time * 1.35f, (rWarmup + Mathf.absin(8, rWarmup * 0.65f)) * lastSize / 2f));
		
		float dst = dst(drawScanPos);
		float drawSize = lastSize * rWarmup;
		float ang = NHMath.acosRad(drawSize / dst) * Mathf.radiansToDegrees;
		float angTo = angleTo(drawScanPos);
		float residualAngles = 90 - ang;
		
		Lines.stroke(rWarmup * 1.25f);
		
		Tmp.c1.set(scanColor).lerp(Color.white, Mathf.absin(5f, 0.325f));
		
		Draw.color(Tmp.c1);
		
		//Tangent 1
		Tmp.v1.trns(angTo + ang, drawSize);
		//Tangent 2
		Tmp.v2.trns(angTo - ang, drawSize);
		
		Vec2[] verts = {Tmp.v1, Tmp.v2};
		
		//Scan Source
		//noinspection SuspiciousNameCombination
		Tmp.v3.trns(rotation, scanSourceY, scanSourceX).add(x, y);
		
		float rot = scanProgress * scanRotateScl + Time.time / 4;
		
		Lines.circle(drawScanPos.x, drawScanPos.y, drawSize);
		DrawFunc.circlePercent(drawScanPos.x, drawScanPos.y, drawSize + 5f, scanProgress, rot + DrawFunc.rotator_360() + Time.time);
		Drawf.light(drawScanPos.x, drawScanPos.y, drawSize * 1.5f, Tmp.c1, rWarmup * 0.75f);
		for(int i = 0; i < verts.length; i++){
			Vec2 vert = verts[i];
			
			Lines.line(Tmp.v3.x, Tmp.v3.y, vert.x + drawScanPos.x, vert.y + drawScanPos.y, true);
			DrawFunc.lineAngleLerp(vert.x + drawScanPos.x, vert.y + drawScanPos.y, -Mathf.signs[i] * residualAngles + angTo, drawSize * 6, true, Tmp.c1, Color.clear);
		}
		
		//TODO better implement
		//Worth it?
		
		float maxMatDst = Mathf.sqrt2 * scanMatSpacing * (float)(scanMatWidth / 2) * 0.55f;
		
		Rand rand = NHFunc.rand;
		Lines.stroke(rWarmup * 0.75f);
		for(int sx = -scanMatWidth / 2; sx <= scanMatWidth / 2; sx++){
			for(int sy = -scanMatWidth / 2; sy <= scanMatWidth / 2; sy++){
				NHFunc.rand.setSeed((long)sx + 8 << sy);
				float dstMat = Mathf.dst(sx, sy);
				float scl = Mathf.maxZero(1 - dstMat / maxMatDst);
				
				Draw.mixcol(Color.white, Mathf.absin(rand.random(2, 8), rand.random(0.025f, 0.35f)));
				Draw.alpha(scl * rand.random(scl * 0.25f, scl * 1.125f) * (0.5f + Mathf.absin(rand.random(2, 5), rand.random(0.125f, 0.65f))));
				tmpVec.set(sx * scanMatSpacing, sy * scanMatSpacing).rotate(rot).add(drawScanPos);
				Lines.lineAngleCenter(tmpVec.x, tmpVec.y, rot, scanMatSpacing * 0.8f);
				Lines.lineAngleCenter(tmpVec.x, tmpVec.y, rot - 90, scanMatSpacing * 0.8f);
			}
		}
		
		Draw.mixcol();
		Draw.z(Layer.buildBeam);
		Fill.light(drawScanPos.x, drawScanPos.y, (int)(Lines.circleVertices(drawSize) * 0.75f), drawSize * 1.25f, Tmp.c1.a((0.85f + Mathf.absin(4, 0.15f)) * rWarmup), Color.clear);
		
		Draw.z(z);
	}
	
	@Override
	public boolean isCommandable(){
		return false;
	}
	
	@Override
	public void readSync(Reads read){
		targetTeam = TypeIO.readTeam(read);
		
		super.readSync(read);
	}
	
	@Override
	public void read(Reads read){
		targetTeam = TypeIO.readTeam(read);
		
		super.read(read);
	}
	
	@Override
	public void write(Writes write){
		TypeIO.writeTeam(write, targetTeam);
		
		super.write(write);
	}
	
	@Override
	public void writeSync(Writes write){
		TypeIO.writeTeam(write, targetTeam);
		
		super.writeSync(write);
	}
	
	//borrow it for a use
	public void setProp(LAccess prop, Object value) {
		switch(prop) {
			case team:
				if (value instanceof Team) {
					Team t = (Team)value;
					if (!Vars.net.client()) {
						UnitController var9 = this.controller;
						if (var9 instanceof Player) {
							Player p = (Player)var9;
							p.team(t);
						}
						
						this.team = t;
					}
				}
				break;
			case payloadType:
				if (value instanceof Team) {
					this.targetTeam = (Team)value;
				}
		}
	}
}
