package newhorizon.expand.entities;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.CommandAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.entities.units.StatusEntry;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.NHFx;
import newhorizon.util.MathUtil;
import newhorizon.util.func.GridUtil;
import newhorizon.util.struct.GridData;

import java.nio.FloatBuffer;

import static mindustry.Vars.tilesize;
import static newhorizon.util.func.GridUtil.PX_LEN;


public class WarpRift extends NHBaseEntity implements Rotc, Teamc, Syncc {
    public Team team = Team.derelict;
    public UnitType unitType = UnitTypes.alpha;

    //warpChargeTime -- time for charge for the portal.
    //warpBeginTime -- time for fully open the warp portal.
    //warpTime -- time for warping units.
    //warpEndTime -- time for close the warp portal.
    //all in ticks.
    public float warpChargeTime = 120f, warpBeginTime = 120f, warpTime = 160f, warpEndTime = 120f;

    public float warpTimer = 0f;
    public boolean created;

    public double flagToApply = Double.NaN;

    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public Unit toSpawn;

    public Vec2 commandPos = new Vec2(Float.NaN, Float.NaN);

    public float range = unitType.clipSize;

    public static final Rand rand = new Rand();

    public float timer;

    public float rotation = -90f;

    public Effect marginEffect = new Effect(12, e -> {
        rand.setSeed(e.id);

        float dst = e.rotation;

        float ang = rand.random(360);

        float len = range * 0.1f;
        float stroke = range * 0.25f;

        Draw.z(Layer.effect - 0.01f);
        Draw.color(Color.white);
        Lines.stroke(stroke * 1.35f);
        Lines.lineAngle(e.x, e.y, e.rotation, len * 1.2f);
        Lines.lineAngle(e.x, e.y, e.rotation + 180, len * 0.4f * 1.2f);

        Draw.z(Layer.effect);
        Draw.color(team.color);
        Lines.stroke(stroke);
        Lines.lineAngle(e.x, e.y, e.rotation, len);
        Lines.lineAngle(e.x, e.y, e.rotation + 180, len * 0.4f);
    });

    public WarpRift create(Team team, UnitType unitType, float x, float y, float rotation){
        this.team = team;
        this.unitType = unitType;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        return this;
    }
    @Override
    public void draw() {
        Draw.color(team.color);

        Draw.reset();
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
        GridData grid = GridUtil.unitGridsMap.get(unitType.name);



        Effect blockSpark = new Effect(400, e -> {
            rand.setSeed(e.id);
            for (int gy = 0; gy < grid.height; gy++){
                for (int gx = grid.width - 1; gx > 0; gx--){
                    if (grid.getGridBottomLeft(gx, gy) > 0){
                        float rx = grid.xShift + gx * PX_LEN - (float) unitType.fullIcon.width / tilesize;
                        float ry = grid.yShift + gy * PX_LEN - (float) unitType.fullIcon.height / tilesize;
                        Angles.randVectors(e.id + (gx + (long) gy * grid.width), 1, range * rand.random(1f, 5f) * e.fout(Interp.pow3In), (ex, ey) -> {
                            Draw.color(team.color, Color.white, e.fout(Interp.pow3Out));
                            Draw.alpha(Mathf.lerp(0.6f, 0.8f, e.fin()));
                            Tmp.v1.set(rx, ry).rotate(e.rotation - 90);
                            Fill.square(e.x + Tmp.v1.x + ex, e.y + Tmp.v1.y + ey, PX_LEN / 2f * e.fin(Interp.pow10Out), e.rotation - 90);
                        });
                    }
                }
            }
        });

        blockSpark.at(x, y, rotation);
    }

    @Override
    public void add(){
        super.add();
        Groups.sync.add(this);

        NHFx.spawnWave.at(x, y, drawSize * 1.1f, team.color);
        effect();
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
    public void readSync(Reads read) {

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
    public void writeSync(Writes write) {

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
