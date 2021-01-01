package newhorizon.data;

import arc.util.io.*;
import arc.*;
import arc.struct.*;
import arc.scene.ui.layout.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.blocks.special.UpgraderBlock.UpgraderBlockBuild;
import newhorizon.*;
import newhorizon.content.NHItems;

import static newhorizon.func.Functions.*;

public abstract class UpgradeData implements Cloneable{
	public Table table;
	public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
	public int unlockLevel = 0;
	public TextureRegion icon;
	public String name, description;
	public float costTime;
	public UpgraderBlockBuild from;
	public int id;

	public UpgradeData(){
		this("null", "null", 0, new ItemStack(NHItems.emergencyReplace, 0));
	}

	public UpgradeData(
		String name,
		String description,
		float costTime,
		ItemStack... items
	){
		this.name = name;
		this.description = description;
		this.costTime = costTime;
		requirements.addAll(items);
	}

	public void load(){this.icon = Core.atlas.find(NewHorizon.NHNAME + name);}
	public void read(Reads read, byte revision){}
	public void write(Writes write){}

	public float costTime() {return costTime;}
	public ItemStack[] requirements() {return ItemStack.mult(requirements.toArray(), 1 * Vars.state.rules.buildCostMultiplier);}

	@Override
	public Object clone(){
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException ignored){

		}
		return obj;
	}
	
	public abstract void infoText(Table table);
	public abstract void addText(Table table);
	
	public void buildUpgradeInfoAll(Table table) {
		table.table(Tex.button, t -> {}).pad(OFFSET / 2).fillX().height(LEN * 1.5f).row();
	}
	
	public void buildTable(Table t) {
		t.table(Tex.button, this::buildDescriptions).pad(OFFSET / 2).fillX().height(LEN * 1.6f).row();
	}

	public void buildDescriptions(Table t) {
		t.pane(table -> table.image(icon).size(LEN).left()).left().size(LEN);

		t.pane(table -> {
			addText(table);
			table.add("[lightgray]NeededTime: [accent]" + format(costTime() / 60) + "[lightgray] sec[]").left().row();
		}).size(LEN * 6f, LEN).left().pad(OFFSET);

		t.table(Tex.button, table -> {
			table.button(Icon.infoCircle, Styles.clearTransi, () -> showInfo(true)).size(LEN);
			table.button(Icon.upOpen, Styles.clearPartiali, () -> from.upgradeData(this)).size(LEN).disabled(b -> !from.canUpgrade(this));
		}).height(LEN + OFFSET).left().pad(OFFSET);
	}
	
	public void showInfo(boolean drawCons){
		BaseDialog dialog = new BaseDialog("");
		dialog.addCloseListener();
		dialog.cont.margin(15f);
		dialog.cont.table(Tex.button, table -> {
			table.pane( t -> t.image(icon)).size(icon.height + OFFSET / 2).left();
			table.pane(this::infoText).size(icon.height + OFFSET / 2).pad(OFFSET / 2);
		}).row();
		dialog.cont.add("<< " + Core.bundle.get(name) + " >>").color(Pal.accent).row();
		dialog.cont.add("Description: ").color(Pal.accent).left().row();
		dialog.cont.add(tabSpace + Core.bundle.get(description)).color(Color.lightGray).left().row();
		if(drawCons){
			dialog.cont.pane(table -> {
				int index = 0;
				for(ItemStack stack : requirements()){
					if(index % 5 == 0)table.row();
					table.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(5).left();
					index ++;
				}
			}).left().row();
			if(this.unlockLevel > 0)dialog.cont.add("[lightgray]Requires Level: [accent]" + unlockLevel + "[]").left().row();
				dialog.cont.add("[lightgray]CanUpgrade?: " + getJudge(this.from.canUpgrade(this)) + "[]").left().row();
		}
		dialog.cont.image().width(300f).pad(2).height(4f).color(Pal.accent);
		dialog.cont.row();
		dialog.cont.button("@back", Icon.left, dialog::hide).size(LEN * 2.5f, LEN).pad(OFFSET / 3);
		dialog.show();
	}
}