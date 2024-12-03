package newhorizon.util.struct;

public class WeightedOption {
    public float weight;
    public Runnable option;

    public WeightedOption(float weight, Runnable option) {
        this.weight = weight;
        this.option = option;
    }

    public WeightedOption set(float weight, Runnable option){
        this.weight = weight;
        this.option = option;

        return this;
    }
}
