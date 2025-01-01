package newhorizon.expand.entities;

import arc.math.geom.Position;
import arc.math.geom.QuadTree.QuadTreeObject;
import arc.math.geom.Rect;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Entityc;
import mindustry.gen.Healthc;
import mindustry.gen.Teamc;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;

//todo this is a class used to create a barrier, similar to shield but with more data
public class BarrierBridge implements Healthc, Teamc, QuadTreeObject{
    @Override
    public void hitbox(Rect out) {

    }

    @Override
    public void damagePierce(float amount) {

    }

    @Override
    public void damagePierce(float amount, boolean withEffect) {

    }

    @Override
    public void healFract(float amount) {

    }

    @Override
    public void heal(float amount) {

    }

    @Override
    public boolean damaged() {
        return false;
    }

    @Override
    public boolean dead() {
        return false;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public float health() {
        return 0;
    }

    @Override
    public float healthf() {
        return 0;
    }

    @Override
    public float hitTime() {
        return 0;
    }

    @Override
    public float maxHealth() {
        return 0;
    }

    @Override
    public void clampHealth() {

    }

    @Override
    public void damage(float amount) {

    }

    @Override
    public void damage(float amount, boolean withEffect) {

    }

    @Override
    public void damageContinuous(float amount) {

    }

    @Override
    public void damageContinuousPierce(float amount) {

    }

    @Override
    public void dead(boolean dead) {

    }

    @Override
    public void heal() {

    }

    @Override
    public void health(float health) {

    }

    @Override
    public void hitTime(float hitTime) {

    }

    @Override
    public void kill() {

    }

    @Override
    public void killed() {

    }

    @Override
    public void maxHealth(float maxHealth) {

    }

    @Override
    public <T extends Entityc> T self() {
        return null;
    }

    @Override
    public <T> T as() {
        return null;
    }

    @Override
    public boolean isAdded() {
        return false;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean serialize() {
        return false;
    }

    @Override
    public int classId() {
        return 0;
    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public void add() {

    }

    @Override
    public void afterRead() {

    }

    @Override
    public void id(int id) {

    }

    @Override
    public void read(Reads read) {

    }

    @Override
    public void remove() {

    }

    @Override
    public void update() {

    }

    @Override
    public void write(Writes write) {

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
    public Floor floorOn() {
        return null;
    }

    @Override
    public Building buildOn() {
        return null;
    }

    @Override
    public boolean onSolid() {
        return false;
    }

    @Override
    public float getX() {
        return 0;
    }

    @Override
    public float getY() {
        return 0;
    }

    @Override
    public float x() {
        return 0;
    }

    @Override
    public float y() {
        return 0;
    }

    @Override
    public int tileX() {
        return 0;
    }

    @Override
    public int tileY() {
        return 0;
    }

    @Override
    public Block blockOn() {
        return null;
    }

    @Override
    public Tile tileOn() {
        return null;
    }

    @Override
    public void set(Position pos) {

    }

    @Override
    public void set(float x, float y) {

    }

    @Override
    public void trns(Position pos) {

    }

    @Override
    public void trns(float x, float y) {

    }

    @Override
    public void x(float x) {

    }

    @Override
    public void y(float y) {

    }
}
