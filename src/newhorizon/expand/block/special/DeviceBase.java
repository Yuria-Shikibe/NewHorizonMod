package newhorizon.expand.block.special;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import newhorizon.content.NHDevices;
import newhorizon.expand.type.Device;
import newhorizon.expand.type.DeviceData;
import newhorizon.util.func.MathUtil;

import static mindustry.Vars.tilesize;

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

        configurable = true;
        saveConfig = true;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class DeviceBaseBuild extends Building {
        public Seq<DeviceData> targets = new Seq<>();
        public Seq<Device> devices = Seq.with();

        @Override
        public void updateTile() {
            super.updateTile();

            updateTargets();

            targets.each(target -> devices.each(device -> device.applyBuilding(this, target, 1f)));
        }

        public void updateTargets() {
            targets.clear();
            if (front() != null) targets.add(new DeviceData(front(), 0f));
        }

        @Override
        public void buildConfiguration(Table table) {

        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            Draw.z(Layer.blockOver + 0.01f);
            Draw.color(Pal.gray);
            Lines.stroke(3f);
            Lines.square(x, y, size * tilesize / 2f - 1.5f);

            for (DeviceData target: targets){
                Building b = target.building;

                if (b == null) continue;
                Tmp.v1.set(Geometry.raycastRect(b.x, b.y, x, y, Tmp.r1.setCentered(x, y, size * tilesize).grow(-5f)));
                Tmp.v2.trns(MathUtil.angle(b, this), b.block.size * tilesize / 2f - 2.5f).add(b);

                Draw.z(Layer.blockOver + 0.01f);
                Draw.color(Pal.gray);
                Lines.stroke(3f);
                Lines.circle(b.x, b.y, b.block.size * tilesize / 2f - 2.5f);

                Draw.z(Layer.blockOver + 0.01f);
                Draw.color(Pal.gray);
                Lines.stroke(3f);
                Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);

                Draw.z(Layer.blockOver + 0.02f);
                Draw.color(Pal.accent);
                Lines.stroke(1f);
                Lines.circle(b.x, b.y, b.block.size * tilesize / 2f - 2.5f);

                Draw.z(Layer.blockOver + 0.02f);
                Draw.color(Pal.accent);
                Lines.stroke(1f);
                Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            }

            Draw.z(Layer.blockOver + 0.02f);
            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Lines.square(x, y, size * tilesize / 2f - 2.5f);
        }
    }
}
