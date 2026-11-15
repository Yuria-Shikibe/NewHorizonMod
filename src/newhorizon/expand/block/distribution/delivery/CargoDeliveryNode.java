package newhorizon.expand.block.distribution.delivery;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class CargoDeliveryNode extends Block {
    public float range = 256f;

    public CargoDeliveryNode(String name) {
        super(name);
        size = 3;
        health = 900;
        armor = 3f;
        update = true;
        solid = true;
        configurable = false;
        hasItems = false;
        hasLiquids = false;
        envEnabled |= Env.space;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize, y * tilesize, range, Pal.accent);
    }

    public class CargoDeliveryNodeBuild extends Building {
        public float pulse;
        public float hoverTime;

        @Override
        public void created() {
            super.created();
            CargoDeliveryGraph.markDirty();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            CargoDeliveryGraph.markDirty();
        }

        @Override
        public void updateTile() {
            pulse = Mathf.lerpDelta(pulse, 0f, 0.04f);
        }

        public void onBoltPass() {
            pulse = 1f;
        }

        @Override
        public void draw() {
            if (!CargoDeliveryDraw.hovered(this)) {
                hoverTime = Math.max(0f, hoverTime - Time.delta * 2f);
            }

            Draw.rect(region, x, y);
            drawTeamTop();

            if (pulse > 0.01f) {
                Draw.z(Layer.effect);
                Draw.color(team.color, pulse * 0.8f);
                Lines.stroke(1.5f + pulse * 1.5f);
                Lines.circle(x, y, 10f + pulse * 5f);
                Draw.reset();
                Draw.z(Layer.block);
            }
        }

        @Override
        public void drawSelect() {
            hoverTime = Math.min(CargoDeliveryDraw.fullHoverTime, hoverTime + Time.delta);
            super.drawSelect();
            CargoDeliveryDraw.drawNetwork(this, hoverTime);
        }

        @Override
        public void drawConfigure() {
            CargoDeliveryDraw.drawNetwork(this, CargoDeliveryDraw.fullHoverTime);
        }
    }
}
