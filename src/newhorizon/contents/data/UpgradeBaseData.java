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
	public TextureRegion iconLevel;
	
	public UpgradeBaseData(){
		super("UpgradeBuilding", "N/A", none, 460f, new ItemStack(NHItems.emergencyReplace, 0));
	}
	
	public UpgradeBaseData(
		String name,
		String description,
		float costTime,
		ItemStack... items
	) {
		super(name, description, costTime, items);
		unlockLevel = 0;
	}
	
	@Override
	public float costTime() {
		return costTime * (1 + level * timeCostcoefficien);
	}
	
	@Override
	public void load() {
		this.icon = Core.atlas.find(NewHorizon.NHNAME + "upgrade2");
		this.iconLevel = Core.atlas.find(NewHorizon.NHNAME + "level-up");
	}
	
	@Override
	public void buildUpgradeInfoAll(Table t) {
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
		t.pane(t2 -> {
			t2.pane(table -> {
				table.image(iconLevel).size(LEN);
			}).size(LEN);

			t2.pane(table -> {
				table.add("[gray]Level: [accent]" + level + "[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			
			t2.pane(table -> {
				table.button(Icon.infoCircle, () -> {showInfo(this);}).size(LEN);
			}).size(LEN).pad(OFFSET);
		}).size(LEN * 11, LEN * 1.5f).row();
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
	}
	
	@Override
	public void addText(Table table){
		table.add("[gray]UpgradeTo: [accent]Level " + level() + "[]").left().row();
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








