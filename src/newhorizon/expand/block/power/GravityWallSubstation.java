package newhorizon.expand.block.power;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.util.*;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.meta.StatUnit;
import newhorizon.NHGroups;
import newhorizon.NHVars;
import newhorizon.content.NHContent;
import newhorizon.content.NHStats;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.*;

public class GravityWallSubstation extends PowerNode {
    public float gravityRange = 120f;

    public GravityWallSubstation(String name) {
        super(name);
        update = true;
    }

    public void drawRangeRect(float x, float y, float range){
        Lines.stroke(3, Pal.gray);
        Lines.square(x, y, range + 1);

        Color color = player == null? Pal.techBlue :Vars.player.team().color;
        Lines.stroke(1, color);
        Lines.square(x, y, range);

        Draw.reset();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawRangeRect(x * tilesize, y * tilesize, gravityRange);
        NHVars.renderer.drawGravityTrap();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(NHStats.gravityRange, gravityRange / tilesize, StatUnit.blocks);
    }

    public class GravityWallSubstationBuild extends PowerNodeBuild {
        public transient GravityTrapField field;

        @Override
        public void created() {
            super.created();
            if (field != null) field.setPosition(this);
        }

        @Override
        public void draw(){
            super.draw();
            if (player == null || team != player.team()) return;

            Draw.z(NHContent.POWER_AREA);
            Draw.color(team.color);
            Fill.square(x, y, gravityRange);

            Draw.z(NHContent.POWER_DYNAMIC);
            Draw.color(team.color);
            Fill.square(x, y, gravityRange * 0.8f + gravityRange * 0.2f * Interp.exp5Out.apply(Time.time / 240f % 1f));
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            drawRangeRect(x, y, gravityRange);
        }

        @Override
        public void add(){
            super.add();
            if(field == null)field = new GravityTrapField(this, this::isValid, gravityRange);
            field.add();
        }

        @Override
        public void remove(){
            if(added) NHGroups.gravityTraps.remove(field);
            super.remove();
        }
    }
}
