package newhorizon.expand.interfaces;

import arc.graphics.Color;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.logic.Ranged;

import static mindustry.Vars.world;
import static mindustry.Vars.world;

public interface Linkablec extends Buildingc, Ranged {
    Seq<Building> tmpSeq = new Seq<>(1);

    default boolean onConfigureBuildTapped(Building other) {
        if (this == other || linkPos() == other.pos()) {
            linkPos(-1);
            return false;
        }

        if (((Building)this).dst(other) <= range() && other.team() == ((Building)this).team()) {
            linkPos(((Building)this).pos());
            return false;
        }
        return true;
    }

    default void drawLink(@Nullable Seq<Building> builds) {}

    default void drawLink() {}

    default Building link() {
        return world.build(linkPos());
    }

    default boolean linkValid() {
        return linkValid(link());
    }

    default boolean linkValid(Building building) {
        return building != null;
    }

    default void linkPos(Point2 point2) {
        linkPos(point2.pack());
    }

    int linkPos();

    void linkPos(int value);

    Color getLinkColor();
}
