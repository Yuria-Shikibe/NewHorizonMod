package newhorizon.contents.data;

import arc.util.io.*;
import arc.*;
import arc.scene.ui.layout.*;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.type.*;

import newhorizon.*;
import static newhorizon.func.Functions.*;

public class UpgradeBaseData extends UpgradeLevelData {
	public static final float maxReloadReduce = 0.5f;
	public static final float maxDamageReduce = 0.75f;
	
	public static int getPercent(float value, float min, float max){return Mathf.floor(Mathf.clamp(value, min, max) * 100);}
	
	public float timeCostCoefficien = 0f;
	public float speedMPL;
	public float defenceMPL;
	public float itemCostCoefficien;
	public TextureRegion iconLevel;
	
	public UpgradeBaseData(){
		super();
	}
	
	public UpgradeBaseData(
		String name,
		String description,
		float costTime,
		ItemStack... items
	) {
		super(name, description, costTime, items);
	}
	
	@Override
	public void buildUpgradeInfoAll(Table t2) {
		t2.table(Tex.button, t -> {
			t.pane(table -> table.image(iconLevel).size(LEN).left()).size(LEN).left();
			t.pane(table -> {
				table.add("[lightgray]Level: [accent]" + level + "[]", Styles.techLabel).left().pad(OFFSET).row();
				table.image().fillX().pad(OFFSET / 2).height(4f).color(Color.lightGray).left().row();
				table.add("[lightgray]ReloadReduce: [accent]" + getPercent(speedMPL * level, 0f, maxReloadReduce) + "%[]").left().row();
				table.add("[lightgray]DefenceUP: [accent]" + getPercent(defenceMPL * level, 0f, maxDamageReduce) + "%[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			t.table(Tex.button, table -> table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(false)).size(LEN * 2, LEN)).height(LEN + OFFSET).pad(OFFSET);
		}).pad(OFFSET / 2).fillX().height(LEN * 1.5f).row();
	}
	
	@Override
	public float costTime() {
		return costTime * (1 + level * timeCostCoefficien);
	}
	
	@Override
	public ItemStack[] requirements() {
		return ItemStack.mult(this.requirements.toArray(), (itemCostCoefficien * level + 1f) * Vars.state.rules.buildCostMultiplier);
	}
	
	@Override
	public void load() {
		this.icon = Core.atlas.find(NewHorizon.NHNAME + "upgrade2");
		this.iconLevel = Core.atlas.find(NewHorizon.NHNAME + "level-up");
	}

	@Override
	public void buildTableComplete(Table t) {
		t.table(Tex.button, t2 -> {
			t2.pane(table -> table.image(iconLevel).size(LEN).left()).left().size(LEN);

			t2.pane(table -> table.add("[lightgray]Level: [accent]MaxLevel[]").left().row()).size(LEN * 6f, LEN).left().pad(OFFSET);
			
			t2.table(Tex.button, table -> table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo( false)).size(LEN * 2, LEN)).height(LEN + OFFSET).left().pad(OFFSET);
		}).pad(OFFSET / 2).fillX().height(LEN * 1.6f).row();
	}
	
	@Override
	public void infoText(Table table){
		table.image(iconLevel);
	}
	
	@Override
	public void addText(Table table){
		table.add("[lightgray]UpgradeTo: [accent]Level " + level() + "[]").left().row();
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








