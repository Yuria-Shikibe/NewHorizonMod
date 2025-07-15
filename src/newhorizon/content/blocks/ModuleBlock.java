package newhorizon.content.blocks;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.block.inner.ModulePayload;

public class ModuleBlock {
    public static Block
            wiringKit, powerUnit, bionicsProcessor, memoryRecalibrator, resistoArray,
            crystalDiode, protonCapacitor, hadronBuffers, tachyonEmitter, neutronMembrane,
            armorCast, heatDetector, gaussReceptor, echoCanceller, pulseMutator,
            powerCell, fissionCell, chargeCompensator, fusionReactor, multiphasePropellant,
            supraGel, coolingUnit, signalCirculator, particleModulator, quantumConductor
    ;

    public static Seq<ModulePayload> modules = new Seq<>();

    public static void load() {
        wiringKit = new ModulePayload("wiring-kit");
        powerUnit = new ModulePayload("power-unit");
        bionicsProcessor = new ModulePayload("bionics-processor");
        memoryRecalibrator = new ModulePayload("memory-recalibrator");
        resistoArray = new ModulePayload("resisto-array");

        crystalDiode = new ModulePayload("crystal-diode");
        protonCapacitor = new ModulePayload("proton-capacitor");
        hadronBuffers = new ModulePayload("hadron-buffers");
        tachyonEmitter = new ModulePayload("tachyon-emitter");
        neutronMembrane = new ModulePayload("neutron-membrane");

        armorCast = new ModulePayload("armor-cast");
        heatDetector = new ModulePayload("heat-detector");
        gaussReceptor = new ModulePayload("gauss-receptor");
        echoCanceller = new ModulePayload("echo-canceller");
        pulseMutator = new ModulePayload("pulse-mutator");

        powerCell = new ModulePayload("power-cell");
        fissionCell = new ModulePayload("fission-cell");
        chargeCompensator = new ModulePayload("charge-compensator");
        fusionReactor = new ModulePayload("fusion-reactor");
        multiphasePropellant = new ModulePayload("multiphase-propellant");

        supraGel = new ModulePayload("supra-gel");
        coolingUnit = new ModulePayload("cooling-unit");
        signalCirculator = new ModulePayload("signal-circulator");
        particleModulator = new ModulePayload("particle-modulator");
        quantumConductor = new ModulePayload("quantum-conductor");
    }
}
