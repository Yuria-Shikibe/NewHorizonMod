package newhorizon.contents.data;

import arc.util.pooling.*;
import arc.util.io.*;
import arc.*;
import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.math.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import mindustry.game.*;
import mindustry.ctype.*;
import mindustry.content.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import newhorizon.contents.items.*;
import newhorizon.NewHorizon;

import static mindustry.Vars.*;

public class UpgradeBaseData extends UpgradeData {
	public float timeCostcoefficien = 0f;
	public int level;
	public float speedMPL;
	public float damageMPL;
	public float defenceMPL;
	
	public UpgradeBaseData(){
		super("UpgradeBuilding", "N/A", none, 460f, new ItemStack());
	}
	
	public UpgradeBaseData(
		String name,
		String description,
		float costTime,
		ItemStack... items
	) {
		super(name, description, none, costTime, items);
	}

	@Override
	public void load() {
		this.icon = Core.atlas.find(NewHorizon.NHNAME + "upgrade2");
	}
	
	@Override
	public void addText(Table table){
		table.add("[gray]UpgradeTo: [accent]Level " + level + "[]").left().row();
	}

	public boolean equals(Object obj) {
		if (obj == this)return true;
		if (obj instanceof UpgradeBaseData) {
			UpgradeBaseData data = (UpgradeBaseData)obj;
			return
				data.from.equals(from) &&
				data.level == level &&
				data.speedMPL == speedMPL &&
				data.damageMPL == damageMPL &&
				data.defenceMPL == defenceMPL;
		}
		return false;
	}

	public UpgradeBaseData init() {
		this.level = 0;
		return this;
	}

	public String toString() {
		return
			"    SpeedMultPerLever: " + speedMPL +
			"\n    DamageMultPerLever: " + damageMPL +
			"\n    DefenceMultPerLever: " + defenceMPL;

	}

	public void write(Writes write) {
		write.i(this.level);
	}

	public void read(Reads read, byte revision) {
		this.level = read.i();
	}

	public void plusLevel() {
		this.level ++;
	}

	public int level() {
		return level + 1;
	}


}








