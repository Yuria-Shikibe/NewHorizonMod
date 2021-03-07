package newhorizon.interfaces;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.DrawFuncs.sinScl;

public interface Linkablec extends Buildingc, Ranged{
	default void drawLink(){
		Draw.reset();
		
		if(!linkValid())return;
		float
				sin = Mathf.absin(Time.time * sinScl, 6f, 1f),
				r1 = block().size / 2f * tilesize + sin,
				r2 = link().block().size / 2f * tilesize + sin,
				x = getX(),
				y = getY();
		
		Draw.color(getLinkColor());
		
		Lines.square(link().getX(), link().getY(), link().block().size * tilesize / 2f + 1.0f);
		
		Tmp.v1.trns(angleTo(link()), r1);
		Tmp.v2.trns(link().angleTo(this), r2);
		int sigs = (int)(dst(link()) / tilesize);
		
		Lines.stroke(4, Pal.gray);
		Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, link().getX() + Tmp.v2.x, link().getY() + Tmp.v2.y, sigs);
		Lines.stroke(2, getLinkColor());
		Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, link().getX() + Tmp.v2.x, link().getY() + Tmp.v2.y, sigs);
		Drawf.circles(x, y, r1, getLinkColor());
		Drawf.arrow(x, y, link().getX(), link().getY(), block().size * tilesize / 2f + sin, 4 + sin, getLinkColor());
		
		Drawf.circles(link().getX(), link().getY(), r2, getLinkColor());
		Draw.reset();
	}
	default Building link(){return world.build(linkPos()); }
	default boolean linkValid(){ return link() != null; }
	default void linkPos(Point2 point2){linkPos(point2.pack());}
	int linkPos();
	void linkPos(int value);
	Color getLinkColor();
}
