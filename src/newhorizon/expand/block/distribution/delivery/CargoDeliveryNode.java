package newhorizon.expand.block.distribution.delivery;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class CargoDeliveryNode extends Block {
    public float range = 256f;
    public int maxLinks = 4;

    public CargoDeliveryNode(String name) {
        super(name);
        size = 3;
        health = 900;
        armor = 3f;
        update = true;
        solid = true;
        configurable = true;
        ignoreResizeConfig = true;
        hasItems = false;
        hasLiquids = false;
        envEnabled |= Env.space;

        config(Integer.class, (CargoDeliveryNodeBuild build, Integer value) -> build.toggleLink(world.build(value)));
        config(Point2[].class, (CargoDeliveryNodeBuild build, Point2[] value) -> build.applyLinkPoints(value));
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

    public class CargoDeliveryNodeBuild extends CargoDeliveryBuild {
        public float pulse;

        @Override
        public void updateTile() {
            pulse = Mathf.lerpDelta(pulse, 0f, 0.04f);
        }

        public void onBoltPass() {
            pulse = 1f;
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            drawTeamTop();
            CargoDeliveryDraw.drawNodeLinks(this);

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
            super.drawSelect();

            Draw.color(Pal.accent);
            Lines.stroke(1f);
            Drawf.circles(x, y, range);
            Draw.reset();
        }
    }
}
