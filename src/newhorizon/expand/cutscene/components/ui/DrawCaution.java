package newhorizon.expand.cutscene.components.ui;

import arc.graphics.Color;
import arc.math.geom.Vec2;

public interface DrawCaution {
    void draw(int id, float time, float radius, Vec2 pos, Color color, boolean beyond);
}
