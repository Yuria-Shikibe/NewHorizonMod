package newhorizon.interfaces;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Buildingc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import newhorizon.feature.UpgradeData.DataEntity;

import static mindustry.Vars.tilesize;

public interface Upgraderc extends Buildingc{
	default void drawLink(){
		Draw.reset();

		if(!linkValid())return;
		Scalablec target = target();
		float
				sin = Mathf.absin(Time.time, 6f, 1f),
				r1 = block().size / 2f * tilesize + sin,
				r2 = target().block().size / 2f * tilesize + sin,
				x = getX(),
				y = getY();

		Draw.color(getColor());

		Lines.square(target.getX(), target.getY(), target.block().size * tilesize / 2f + 1.0f);

		Tmp.v1.trns(angleTo(target), r1);
		Tmp.v2.trns(target.angleTo(this), r2);
		int sigs = (int)(dst(target) / tilesize);

		Lines.stroke(4, Pal.gray);
		Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, target.getX() + Tmp.v2.x, target.getY() + Tmp.v2.y, sigs);
		Lines.stroke(2, getColor());
		Lines.dashLine(x + Tmp.v1.x, y + Tmp.v1.y, target.getX() + Tmp.v2.x, target.getY() + Tmp.v2.y, sigs);
		Drawf.circles(x, y, r1, getColor());
		Drawf.arrow(x, y, target.getX(), target.getY(), 2 * tilesize + sin, 4 + sin, getColor());

		Drawf.circles(target.getX(), target.getY(), r2, getColor());
		Draw.reset();
	}
	void buildSwitchAmmoTable(Table t, boolean setting);
	Scalablec target();
	Color getColor();
	boolean linkValid();
	boolean isUpgrading();
	boolean canUpgrade(DataEntity data);
	void consumeItems(DataEntity data);
	void updateTarget();
	void upgraderTableBuild();
	void updateUpgrading();
	void completeUpgrade();
	void upgradeData(DataEntity data);
	
}

