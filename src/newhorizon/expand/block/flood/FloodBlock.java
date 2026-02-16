package newhorizon.expand.block.flood;

public interface FloodBlock {
    default float damageReduction() {
        return 0.5f;
    }

    default float damageAbsorption() {
        return 1000f;
    }

    default float healConsumption() {
        return 5 / 60f;
    }

    default float healSpeed() {
        return 50f;
    }
}
