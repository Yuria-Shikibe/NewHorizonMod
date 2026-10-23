package newhorizon.expand.units.unitEntity;

import arc.math.geom.Vec2;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.CommandAI;
import mindustry.ctype.ContentType;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Hitboxc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.units.ai.ProbeAI;
import newhorizon.expand.units.probe.ProbeColonyManager;
import newhorizon.expand.units.probe.ProbeFactoryCube;
import newhorizon.expand.units.probe.ProbeFactoryRenderer;
import newhorizon.expand.units.probe.ProbeFactoryExitAI;
import newhorizon.expand.units.probe.ProbeFactoryParts;

public class ProbeEntity extends UnitEntity {
    public Team targetTeam;
    public float scanRange = 240f;
    public boolean factory;
    public boolean factoryCandidate;
    public boolean manualFactory;
    public int factoryId = -1;
    public int miningChildren;
    public int scannerChildren;
    public int miningTargetTile = -1;
    public int productionThreatLevel;
    public int lastProducedThreat;
    public float miningWorkProgress;
    public transient float miningTargetRetry;
    public float factoryRadius = 64f;
    public float productionRequirement;
    public float productionContribution;
    public float productionProgress;
    public transient float productionSpeed = 1f;
    public transient int productionAssistIndex = -1;
    public transient int productionAssistCount;
    public float scannerCooldownUntil;
    public UnitType selectedProduction;
    public transient ProbeFactoryRenderer.Labels factoryLabels;
    public transient ProbeFactoryCube factoryCube;
    public transient ProbeFactoryParts factoryParts;

    @Override
    public int classId() {
        return EntityRegister.getID(ProbeEntity.class);
    }

    @Override
    public void afterRead() {
        super.afterRead();
        restoreColonyController();
        if (targetTeam == null || targetTeam == Team.derelict) targetTeam = defaultTargetTeam();
        if (scanRange <= 0f) scanRange = type.maxRange + 160f;
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
        scanRange = type.maxRange + 160f;
        if (targetTeam == null || targetTeam == Team.derelict) targetTeam = defaultTargetTeam();
    }

    @Override
    public void add() {
        super.add();
        ProbeColonyManager.invalidateRoles();
        scanRange = type.maxRange + 160f;
        if (targetTeam == null || targetTeam == Team.derelict) targetTeam = defaultTargetTeam();
    }

    @Override
    public float clipSize() {
        return factory || !ProbeFactoryParts.retracted(this) ? Math.max(super.clipSize(), factoryRadius * 4f + 96f) : super.clipSize();
    }

    @Override
    public int collisionLayer() {
        return factory && !ProbeFactoryParts.retracted(this) ? -1 : super.collisionLayer();
    }

    @Override
    public boolean collides(Hitboxc other) {
        return !(factory && !ProbeFactoryParts.retracted(this) && other instanceof Unit) && super.collides(other);
    }

    public Tile miningTarget() {
        return ProbeColonyManager.targetTile(miningTargetTile);
    }

    public ProbeEntity factoryOwner() {
        Unit unit = Groups.unit.getByID(factoryId);
        return unit instanceof ProbeEntity probe && probe.isValid() && probe.factory && probe.team == team ? probe : null;
    }

    @Override
    public void update() {
        restoreColonyController();
        ProbeFactoryParts.update(this);
        if (!ProbeFactoryParts.retracted(this) && (!Vars.net.client() || isLocal())) vel.setZero();
        super.update();
    }

    @Override
    public void moveAt(Vec2 movement, float acceleration) {
        if (!ProbeFactoryParts.allowMovement(this, movement)) {
            vel.setZero();
            return;
        }
        super.moveAt(movement, acceleration);
    }

    @Override
    public void approach(Vec2 movement) {
        if (!ProbeFactoryParts.allowMovement(this, movement)) {
            vel.setZero();
            return;
        }
        super.approach(movement);
    }

    private void restoreColonyController() {
        if (Vars.net.client() || controller instanceof ProbeFactoryExitAI
                || !(controller instanceof CommandAI command) || command instanceof ProbeAI) return;
        ProbeAI colony = new ProbeAI();
        colony.command = command.command;
        colony.targetPos = command.targetPos;
        colony.attackTarget = command.attackTarget;
        colony.commandQueue.addAll(command.commandQueue);
        colony.stances = command.stances;
        controller(colony);
    }

    @Override
    public void remove() {
        if (isAdded()) ProbeColonyManager.invalidateRoles();
        super.remove();
    }

    @Override
    public void draw() {
        super.draw();
        ProbeFactoryRenderer.drawAssistant(this);
        ProbeColonyManager.drawFactory(this);
        if (!factory && !factoryCandidate) ProbeColonyManager.drawMining(this, miningTargetTile, miningWorkProgress);
    }

    @Override
    public void read(Reads read) {
        readProbeState(read);
        super.read(read);
    }

    @Override
    public void write(Writes write) {
        writeProbeState(write);
        super.write(write);
    }

    @Override
    public void readSync(Reads read) {
        readProbeState(read);
        super.readSync(read);
        productionSpeed = read.f();
        productionAssistIndex = read.i();
        productionAssistCount = read.i();
    }

    @Override
    public void writeSync(Writes write) {
        writeProbeState(write);
        super.writeSync(write);
        write.f(productionSpeed);
        write.i(productionAssistIndex);
        write.i(productionAssistCount);
    }

    @Override
    public void setProp(LAccess prop, Object value) {
        switch (prop) {
            case team:
                if (value instanceof Team team && !Vars.net.client()) {
                    UnitController controller = this.controller;
                    if (controller instanceof Player player) player.team(team);
                    this.team = team;
                }
                break;
            case payloadType:
                if (value instanceof Team team) targetTeam = team;
                break;
            default:
                super.setProp(prop, value);
        }
    }

    private void readProbeState(Reads read) {
        targetTeam = TypeIO.readTeam(read);
        factory = read.bool();
        factoryCandidate = read.bool();
        manualFactory = read.bool();
        factoryId = read.i();
        miningChildren = read.i();
        scannerChildren = read.i();
        miningTargetTile = read.i();
        productionThreatLevel = read.i();
        lastProducedThreat = read.i();
        miningWorkProgress = read.f();
        factoryRadius = read.f();
        productionRequirement = read.f();
        productionContribution = read.f();
        productionProgress = read.f();
        scannerCooldownUntil = read.f();
        short typeId = read.s();
        selectedProduction = typeId < 0 || Vars.content == null ? null
                : (UnitType) Vars.content.getByID(ContentType.unit, typeId);
    }

    private void writeProbeState(Writes write) {
        TypeIO.writeTeam(write, targetTeam);
        write.bool(factory);
        write.bool(factoryCandidate);
        write.bool(manualFactory);
        write.i(factoryId);
        write.i(miningChildren);
        write.i(scannerChildren);
        write.i(miningTargetTile);
        write.i(productionThreatLevel);
        write.i(lastProducedThreat);
        write.f(miningWorkProgress);
        write.f(factoryRadius);
        write.f(productionRequirement);
        write.f(productionContribution);
        write.f(productionProgress);
        write.f(scannerCooldownUntil);
        write.s(selectedProduction == null ? -1 : selectedProduction.id);
    }

    private Team defaultTargetTeam() {
        return Vars.state == null ? Team.derelict : Vars.state.rules.defaultTeam;
    }
}
