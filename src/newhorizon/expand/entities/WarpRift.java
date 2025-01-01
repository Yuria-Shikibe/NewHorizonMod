package newhorizon.expand.entities;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.CommandAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.units.StatusEntry;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHFx;
import newhorizon.content.NHUnitTypes;
import newhorizon.util.func.GridUtil;
import newhorizon.util.struct.GridData;

import java.nio.FloatBuffer;

import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;
import static mindustry.type.UnitType.shadowTX;
import static mindustry.type.UnitType.shadowTY;
import static newhorizon.util.func.GridUtil.PX_LEN;


public class WarpRift extends NHBaseEntity implements Rotc, Teamc, Syncc {
    public Team team = Team.derelict;
    public UnitType unitType = NHUnitTypes.guardian;

    public float warpChargeTime = 120f, warpBeginTime = 120f, warpTime = 160f, warpEndTime = 120f;

    public float warpTimer = 0f;
    public boolean created;

    public double flagToApply = Double.NaN;

    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public Unit toSpawn = null;

    public Vec2 commandPos = new Vec2(Float.NaN, Float.NaN);

    public float range = 0;

    public static final Rand rand = new Rand();

    public float timer;

    public float rotation = -90f;

    public WarpRift create(Team team, UnitType unitType, float x, float y, float rotation){
        this.team = team;
        this.unitType = unitType;
        this.x = x;
        this.y = y;
        this.rotation = rotation;

        this.range = unitType.clipSize;
        return this;
    }

    @Override
    public void draw() {
        Draw.color(team.color);
        Draw.reset();

        drawUnit();
    }

    @Override
    public void update() {
        warpTimer += Time.delta;
        timer += Time.delta;

        float rad = range * portalProgress();
        int count = (int) (rad / 5f);
        if (timer >= 1f){
            //marginEffect.at(x, y, 0);
            //Angles.randVectors((long) (id + Mathf.random()), count, rad, (ex, ey) -> {
            //    marginEffect.at(ex + x, ey + y, Angles.angle(ex, ey));
            //});
            timer %= 1f;
        }

        if (warpTimer >= spawnTime() && !created){
            dump();
            remove();
        }
    }

    public float portalProgress(){
        if (warpTimer < warpChargeTime) return 0f;
        if (warpTimer < warpBeginTime + warpChargeTime) return (warpTimer - warpChargeTime) / warpBeginTime;
        if (warpTimer < warpTime + warpBeginTime + warpChargeTime) return 1f;
        if (warpTimer < warpEndTime + warpTime + warpBeginTime + warpChargeTime) return 1 -  (warpTimer - (warpTime + warpBeginTime + warpChargeTime)) / warpEndTime;
        return 0f;
    }

    public void dump(){
        toSpawn = unitType.create(team);
        toSpawn.set(x, y);
        toSpawn.rotation = rotation();
        if(!Double.isNaN(flagToApply)){
            toSpawn.flag(flagToApply);
        }
        if(!Vars.net.client()) toSpawn.add();
        toSpawn.apply(StatusEffects.unmoving, Fx.unitSpawn.lifetime);
        toSpawn.apply(statusEntry.effect, statusEntry.time);
        if(commandPos != null && !commandPos.isNaN()){
            if(toSpawn.isCommandable()){
                toSpawn.command().commandPosition(commandPos);
            }else{
                CommandAI ai = new CommandAI();
                ai.commandPosition(commandPos);
                toSpawn.controller(ai);
            }
        }

        created = true;
        Events.fire(new EventType.UnitCreateEvent(toSpawn, null));
    }

    public void effect(){
        if (headless) return;
        GridData grid = GridUtil.unitGridsMap.get(unitType.name);

        for (int gy = grid.height - 1; gy > 0; gy--){
            for (int gx = 0; gx < grid.width; gx++){
                if (grid.getGrid(grid.width - gx - 1, gy) > 0){
                    float sx = grid.xShift / tilesize + gx * PX_LEN - (float) unitType.fullIcon.width / tilesize;
                    float sy = grid.yShift / tilesize + gy * PX_LEN - (float) unitType.fullIcon.height / tilesize;
                    float delay = gy * (285f / grid.height);

                    Tmp.v1.set(sx, sy).rotate(rotation + 90).add(x, y);
                    gridSpark(Tmp.v1.x, Tmp.v1.y, delay);
                }
            }
        }

        createdSpark();
    }

    private void gridSpark(float x, float y, float delay){
        Effect gridSpark = new Effect(120, e -> {
            rand.setSeed(e.id);
            Tmp.v1.trns(rotation + 180, range * rand.random(1f, 5f) * e.fout(Interp.pow3In));
            Draw.color(team.color, Color.white, e.fslope());
            Draw.alpha(1.2f * e.fout(Interp.pow10Out));
            Lines.stroke(PX_LEN);
            Lines.lineAngle(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rotation, PX_LEN / 2f * (e.fslope() * 5 + e.fin()));
        });
        gridSpark.startDelay = delay;
        gridSpark.at(x, y, rotation);
    }

    private void createdSpark(){
        Effect spawn = new Effect(65, e -> {
            Draw.mixcol(team.color, Color.white, e.fout());
            Draw.alpha(1.2f * e.fout(Interp.pow2Out));
            Draw.rect(unitType.fullIcon, e.x, e.y, unitType.fullIcon.width * Draw.scl * (1 + 0.06f * e.fin(Interp.pow2Out)), unitType.fullIcon.height * Draw.scl * (1 + 0.06f * e.fin(Interp.pow2Out)), rotDeg());
            Draw.reset();
        });

        spawn.startDelay = 375f;
        spawn.at(x, y, rotation);
    }

    public void drawUnit(){
        if (warpTimer > 420) return;
        drawExtraDrawer();
        if (warpTimer > 400) return;
        float progress = Mathf.clamp((warpTimer - 100) / 285f);
        int height = Mathf.ceil(unitType.fullIcon.height * progress);
        Tmp.tr1.set(unitType.fullIcon, 0, 0, unitType.fullIcon.width, height);
        Tmp.v1.trns(rotation, (float) (unitType.fullIcon.height - height) / tilesize).add(x, y);
        Draw.z(Layer.flyingUnitLow);
        Draw.rect(Tmp.tr1, Tmp.v1.x , Tmp.v1.y, rotation - 90);

        if (unitType.flying){
            Draw.z(Layer.flyingUnitLow - 0.5f);
            float e = unitType.shadowElevation * unitType.shadowElevationScl;
            Drawf.shadow(Tmp.tr1, Tmp.v1.x - shadowTX * e, Tmp.v1.y - shadowTY * e, rotDeg());
        }

        Draw.z(Layer.flyingUnitLow - 1f);
        unitType.drawSoftShadow(Tmp.v1.x , Tmp.v1.y, rotation, progress);
    }

    public void drawExtraDrawer(){
        for (Ability ability: unitType.abilities){
            if (ability instanceof ShieldArcAbility){
                ShieldArcAbility sa = (ShieldArcAbility) ability;

                float progress = Mathf.clamp((warpTimer - 100) / 320f);

                Draw.z(Layer.shields);

                Draw.color(team.color);

                if(!Vars.renderer.animateShields){
                    Draw.alpha(0.4f);
                }

                if(sa.drawArc){
                    Lines.stroke(sa.width * progress);
                    Lines.arc(x, y, sa.radius, sa.angle / 360f, rotation + sa.angleOffset - sa.angle / 2f);
                }
                Draw.reset();            }
        }
    }

    @Override
    public void add(){
        super.add();
        Groups.sync.add(this);

        NHFx.spawnWave.at(x, y, drawSize * 1.1f, team.color);
        //effect();
    }

    @Override
    public void remove(){
        super.remove();
        Groups.sync.remove(this);

        if(Vars.net.client()){
            Vars.netClient.addRemovedEntity(id());
        }
    }

    public float spawnTime(){
        return warpChargeTime + warpBeginTime + warpTime;
    }

    public float rotDeg(){
        return rotation - 90;
    }

    @Override
    public Building buildOn() {
        return null;
    }

    @Override
    public boolean isSyncHidden(Player player) {
        return false;
    }

    @Override
    public long lastUpdated() {
        return 0;
    }

    @Override
    public long updateSpacing() {
        return 0;
    }

    @Override
    public void afterSync() {

    }

    @Override
    public void handleSyncHidden() {

    }

    @Override
    public void interpolate() {

    }

    @Override
    public void lastUpdated(long lastUpdated) {

    }

    @Override
    public void readSyncManual(FloatBuffer buffer) {

    }

    @Override
    public void snapInterpolation() {

    }

    @Override
    public void snapSync() {

    }

    @Override
    public void updateSpacing(long updateSpacing) {

    }

    @Override
    public boolean serialize(){return true;}

    @Override
    public int classId(){
        return EntityRegister.getID(getClass());
    }
    

    @Override
    public void write(Writes write){
        super.write(write);
        write.f(warpTimer);
        write.f(rotation);

        TypeIO.writeUnitType(write, unitType);
        TypeIO.writeTeam(write, team);
        TypeIO.writeStatus(write, statusEntry);

        TypeIO.writeVec2(write, commandPos);
    }

    @Override
    public void read(Reads read){
        super.read(read);
        warpTimer = read.f();
        rotation = read.f();

        unitType = TypeIO.readUnitType(read);
        team = TypeIO.readTeam(read);
        statusEntry = TypeIO.readStatus(read);

        commandPos = TypeIO.readVec2(read);
        afterRead();
    }

    @Override
    public void writeSync(Writes write) {
        write.f(x);
        write.f(y);
        write.f(warpTimer);
        write.f(rotation);

        TypeIO.writeUnitType(write, unitType);
        TypeIO.writeTeam(write, team);
        TypeIO.writeVec2(write, commandPos);
    }

    @Override
    public void readSync(Reads read) {
        x = read.f();
        y = read.f();

        warpTimer = read.f();
        rotation = read.f();

        unitType = TypeIO.readUnitType(read);
        team = TypeIO.readTeam(read);
        statusEntry = TypeIO.readStatus(read);
        commandPos = TypeIO.readVec2(read);

        afterRead();
    }

    @Override
    public void writeSyncManual(FloatBuffer buffer) {

    }

    @Override
    public boolean inFogTo(Team viewer) {
        return false;
    }

    @Override
    public boolean cheating() {
        return false;
    }

    @Override
    public Team team() {
        return null;
    }

    @Override
    public CoreBlock.CoreBuild closestCore() {
        return null;
    }

    @Override
    public CoreBlock.CoreBuild closestEnemyCore() {
        return null;
    }

    @Override
    public CoreBlock.CoreBuild core() {
        return null;
    }

    @Override
    public void team(Team team) {

    }

    @Override
    public float rotation() {
        return rotation;
    }

    @Override
    public void rotation(float rotation) {

    }
}
