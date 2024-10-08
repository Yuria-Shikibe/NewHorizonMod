package newhorizon.expand.block.consumer;

import arc.func.Floatf;
import mindustry.gen.Building;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stats;

public class PowerConsumer extends ConsumePower {
    private final Floatf<Building> usage;

    public PowerConsumer(Floatf<Building> usage){
        super(0, 0, false);
        this.usage = usage;
    }

    @Override
    public float requestedPower(Building entity){
        return usage.get(entity);
    }

    @Override
    public void display(Stats stats){

    }

    public float efficiency(Building build){
        return usage.get(build) != 0? build.power.status : 1f;
    }

}
