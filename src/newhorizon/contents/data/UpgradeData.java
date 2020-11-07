package newhorizon.contents.data;

import arc.util.pooling.Pool.*;
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

import newhorizon.contents.blocks.special.UpgraderBlock.UpgraderBlockBuild;
import newhorizon.NewHorizon;
import java.text.DecimalFormat;

import static mindustry.Vars.*;

public abstract class UpgradeData implements Cloneable{
	public static String getJudge(boolean value){
		return value ? "[green]Yes" : "[red]No";
	}
	public static final String offsetSpace = "    ";
	public static final float LEN = 60f, OFFSET = 12f;
	public static final BulletType none = new BasicBulletType(0, 1, "none") {
		{
			instantDisappear = true;
			trailEffect = smokeEffect = shootEffect = hitEffect = despawnEffect = Fx.none;
		}
	};
	public final Seq<ItemStack> requirements = new Seq<>(ItemStack.class);
	
	protected static final DecimalFormat df = new DecimalFormat("######0.00");
	//
	public int unlockLevel;
	public TextureRegion icon;
	public String name, description;
	public float costTime;
	BulletType selectAmmo;
	public UpgraderBlockBuild from;
	public boolean disable = false;
	

	public UpgradeData(
		String name,
		String description,
		float costTime,
		ItemStack... items
	) {
		this.name = name;
		this.description = description;
		this.costTime = costTime;
		requirements.addAll(items);
	}

	public void load() {
		this.icon = Core.atlas.find(NewHorizon.NHNAME + name);
	}

	public abstract void read(Reads read, byte revision);
	public abstract void write(Writes write);
	
	public float costTime() {
		return costTime;
	}
	
	public ItemStack[] requirements() {
		return this.requirements.toArray();
	}
	
	@Override
	public Object clone (){
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException err){}
		return obj;
	}
	
	public abstract void infoText(Table table);
	public abstract void addText(Table table);
	public abstract void buildUpgradeInfoAll(Table table);
	public void buildTable(Table t) {
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
		t.pane(table -> {
			buildDescriptions(table);
		}).size(LEN * 11, LEN * 1.5f).row();
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
	}

	public void buildDescriptions(Table t) {
		t.pane(table -> {
			table.image(icon).size(LEN).left();
		}).left().size(LEN);

		t.pane(table -> {
			addText(table);
			table.add("[lightgray]NeededTime: [accent]" + df.format(costTime() / 60) + "sec[]").left().row();
		}).size(LEN * 6f, LEN).left().pad(OFFSET);

		t.pane(table -> {
			table.button(Icon.infoCircle, () -> {showInfo(this, true);}).size(LEN);
			table.button(Icon.upgrade, () -> {
				from.upgradeData(this);
			}).size(LEN).disabled(!from.canUpgrade(this));
		}).size(LEN * 2f, LEN).left().pad(OFFSET);
	}
	
	public void showInfo(UpgradeData data, boolean drawCons){
		new Dialog("") {{
			cont.margin(15f);
			cont.pane(table -> {
				table.pane( t -> {t.image(icon);}).size(icon.height + OFFSET / 2).left();
				table.pane( t -> {infoText(t);}).size(icon.height + OFFSET / 2).pad(OFFSET / 2);
			}).row();
			cont.add("<< " + Core.bundle.get(data.name) + " >>").color(Pal.accent).row();
			cont.add("Description: ").color(Pal.accent).left().row();
			cont.add(offsetSpace + Core.bundle.get(data.description)).color(Color.lightGray).left().row();
			if(drawCons){
				cont.pane(table -> {
					int index = 0;
					for(ItemStack stack : requirements()){
						if(index % 5 == 0)table.row();
						table.add(new ItemDisplay(stack.item, stack.amount, false)).padRight(5).left();
						index ++;
					}
				}).left().row();
				if(data.unlockLevel > 0)cont.add("[lightgray]Requires Level: [accent]" + unlockLevel + "[]").left().row();
				cont.add("[lightgray]CanUpgrade?: " + getJudge(data.from.canUpgrade(data)) + "[]").left().row();
			}
			cont.image().width(300f).pad(2).height(4f).color(Pal.accent);
			cont.row();
			cont.button("Leave", this::hide).size(120, 50).pad(4);
		}}.show();
	}

}