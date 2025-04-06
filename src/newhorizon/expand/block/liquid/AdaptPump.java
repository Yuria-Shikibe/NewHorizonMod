package newhorizon.expand.block.liquid;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.type.Liquid;
import mindustry.world.blocks.production.Pump;
import newhorizon.expand.block.GraphBuildings;
import newhorizon.expand.block.GraphEntity;

public class AdaptPump extends Pump {
    public AdaptPump(String name) {
        super(name);
    }

    public class AdaptPumpBuild extends PumpBuild{
        public boolean[] drawLink = new boolean[8];

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return super.acceptLiquid(source, liquid) || (isValidPump(source) && liquidDrop == liquid);
        }

        public boolean isValidPump(Building e) {
            if (!(e instanceof AdaptPumpBuild)) return false;
            if (size == 2 && e.block().size == 2) return check(e, 4);
            if (size == 4 && e.block().size == 4) return check(e, 16) || check(e, 20);
            if (size == 2 && e.block().size == 4) return check(e, 10);
            if (size == 4 && e.block().size == 2) return check(e, 10);
            return false;
        }

        public boolean check(Buildingc target, int dst2) {
            int dst = (int) Mathf.dst2(tileX(), tileY(), target.tileX(), target.tileY());
            Log.info(dst);
            return dst == dst2;
        }
    }
}
