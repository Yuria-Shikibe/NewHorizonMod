package newhorizon.expand.block.flood;

public interface FloodBlock {
    float[] packedData();

    default float healConsumption() {
        return packedData()[0];
    }

    default float healSpeed() {
        return packedData()[1];
    }

    default float damageReduction() {
        return packedData()[2];
    }

    default float damageAbsorption() {
        return packedData()[3];
    }

    default float statMultiplier() {
        return packedData()[4];
    }
}
