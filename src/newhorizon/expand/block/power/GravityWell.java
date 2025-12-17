package newhorizon.expand.block.power;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Rand;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.meta.StatUnit;
import newhorizon.content.NHContent;
import newhorizon.content.NHStats;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class GravityWell extends Block{
    public float gravityRange = 80f;
    public final int effect = timers++;

    public static Rand rand = new Rand();

    public GravityWell(String name) {
        super(name);
        update = true;
    }

    @Override
    public void init() {
        super.init();
        clipSize += gravityRange * 2;
    }

    public void drawRangeRect(float x, float y, float range) {
        Lines.stroke(3, Pal.gray);
        Lines.square(x, y, range + 1);

        Color color = player == null ? Pal.techBlue : Vars.player.team().color;
        Lines.stroke(1, color);
        Lines.square(x, y, range);

        Draw.reset();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.gravityRange, gravityRange / tilesize, StatUnit.blocks);
    }

    public class GravityWellBuild extends Building{
        public transient GravityTrapField field;

        @Override
        public void created() {
            super.created();
            field = new GravityTrapField(team, gravityRange);
        }

        public void update(){
            field.update(this);
        }

        @Override
        public void draw() {
            super.draw();
            Draw.z(NHContent.GRAVITY_TRAP_LAYER);
            Draw.color(team.color);
            Draw.alpha(0.25f);
            Fill.square(x, y, gravityRange);
        }

        /*
        @Override
        public void draw() {
            super.draw();
            if (player == null || team != player.team()) return;

            if (isPayload()) return;
            Draw.z(NHContent.POWER_AREA);
            Draw.color(team.color);
            Fill.square(x, y, gravityRange);

            Draw.z(NHContent.POWER_DYNAMIC);
            Draw.color(team.color);
            Fill.square(x, y, gravityRange * 0.8f + gravityRange * 0.2f * Interp.exp5Out.apply(Time.time / 240f % 1f));
        }

         */

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawRangeRect(x, y, gravityRange);
        }

        @Override
        public void remove() {
            super.remove();
            field.remove();
        }
    }
}
