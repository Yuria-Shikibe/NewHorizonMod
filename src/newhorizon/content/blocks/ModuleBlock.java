package newhorizon.content.blocks;

import arc.struct.Seq;
import mindustry.world.Block;
import newhorizon.expand.block.inner.ModulePayload;

public class ModuleBlock {
    public static Block armorCast, powerUnit, supraGel, powerCell, crystalDiode,
            coolingUnit, signalCirculator, pulseMutator, bionicsProcessor, resistoArray,
            fusionReactor, quantumConductor, particleModulator, tachyonEmitter, gaussReceptor;

    public static Seq<ModulePayload> modules = new Seq<>();

    public static void load() {
        armorCast = new ModulePayload("armor-cast");
        powerUnit = new ModulePayload("power-unit");
        supraGel = new ModulePayload("supra-gel");
        powerCell = new ModulePayload("power-cell");
        crystalDiode = new ModulePayload("crystal-diode");
        coolingUnit = new ModulePayload("cooling-unit");
        signalCirculator = new ModulePayload("signal-circulator");
        pulseMutator = new ModulePayload("pulse-mutator");
        bionicsProcessor = new ModulePayload("bionics-processor");
        resistoArray = new ModulePayload("resisto-array");
        fusionReactor = new ModulePayload("fusion-reactor");
        particleModulator = new ModulePayload("particle-modulator");
        quantumConductor = new ModulePayload("quantum-conductor");
        tachyonEmitter = new ModulePayload("tachyon-emitter");
        gaussReceptor = new ModulePayload("gauss-receptor");
    }
}
