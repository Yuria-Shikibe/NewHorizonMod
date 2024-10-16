package newhorizon.expand.block.struct;

import newhorizon.expand.block.AdaptBuilding;

public class DirectionGraphEntity<T extends AdaptBuilding> extends GraphEntity<T>{
    public T start;
    public T end;

    @Override
    public void postCalculate() {
        super.postCalculate();
        if (allBuildings.isEmpty()) return;
        allBuildings.each(build -> {
            if (build.front() == null){end = build;}
            if (build.back() == null){start = build;}
        });
    }
}
