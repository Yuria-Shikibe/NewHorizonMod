package newhorizon.expand.block.special;

import arc.struct.EnumSet;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import newhorizon.content.NHDevices;
import newhorizon.expand.type.Device;

public class DeviceBase extends Block {
    public DeviceBase(String name) {
        super(name);

        size = 2;

        update = true;
        solid = true;
        destructible = true;
        rotate = true;

        hasItems = false;
        hasLiquids = false;
        hasPower = false;

        canOverdrive = false;
        drawDisabled = false;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class DeviceBaseBuild extends Building {
        public Seq<Building> targets = new Seq<>();
        public Seq<Device> devices = Seq.with(NHDevices.rapidLoaderLight);

        @Override
        public void updateTile() {
            super.updateTile();

            updateTargets();

            targets.each(target -> devices.each(device -> device.applyBuilding(this, target)));
        }

        public void updateTargets() {
            targets.clear();
            if (front() != null) targets.add(front());
        }
    }
}
