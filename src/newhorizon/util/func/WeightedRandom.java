package newhorizon.util.func;

import arc.math.Rand;
import arc.struct.Seq;
import newhorizon.util.struct.WeightedOption;

public class WeightedRandom {
    private static final Rand rand = new Rand();
    /**
     * Selects and executes one option based on weight.
     *
     * @param options Array of WeightedOption to choose from.
     */
    public static void random(WeightedOption... options) {
        float totalWeight = 0;

        // Calculate total weight
        for (WeightedOption option : options) {
            totalWeight += option.weight;
        }

        // Generate a random float between 0 and totalWeight
        float randomValue = rand.nextFloat() * totalWeight;

        // Iterate through options to find the one corresponding to randomValue
        float cumulativeWeight = 0;
        for (WeightedOption option : options) {
            cumulativeWeight += option.weight;
            if (randomValue <= cumulativeWeight) {
                option.option.run();
                return;
            }
        }
    }

    public static void random(Seq<WeightedOption> options){
        float totalWeight = 0;

        // Calculate total weight
        for (WeightedOption option : options) {
            totalWeight += option.weight;
        }

        // Generate a random float between 0 and totalWeight
        float randomValue = rand.nextFloat() * totalWeight;

        // Iterate through options to find the one corresponding to randomValue
        float cumulativeWeight = 0;
        for (WeightedOption option : options) {
            cumulativeWeight += option.weight;
            if (randomValue <= cumulativeWeight) {
                option.option.run();
                return;
            }
        }
    }

    /**
     * Selects and executes one option based on weight.
     *
     * @param objects Array of WeightedOption to choose from.
     */
    public static void random(Object... objects){
        WeightedOption[] options = new WeightedOption[objects.length % 2];
        for(int i = 0; i < objects.length; i += 2){
            options[i / 2] = new WeightedOption((float)objects[i], (Runnable)objects[i + 1]);
        }
        random(options);
    }


}
