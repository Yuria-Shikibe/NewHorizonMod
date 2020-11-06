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
import newhorizon.contents.data.UpgradeData.*;

import static mindustry.Vars.*;

public class UpgradeAmmoData extends UpgradeData{
	
	public Effect
		chargeEffect = Fx.none,
		chargeBeginEffect = Fx.none;
			
	public float chargeTime = 0f;
	public float reloadTime;
	public float randX;
	public int salvos = 1;
	public float burstSpacing = 5f;
	public BulletType selectAmmo;
	
	public UpgradeAmmoData(
		String name,
		String description,
		BulletType selectAmmo,
		float costTime,
		int unlockLevel,
		ItemStack... items
	) {
		super(name, description, costTime, items);
		this.unlockLevel = unlockLevel;
		this.selectAmmo = selectAmmo;
	}
	public int id;
	public boolean isUnlocked, selected;
	
	@Override
	public void buildUpgradeInfoAll(Table t) {
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
		t.pane(t2 -> {
			t2.pane(table -> {
				table.image(icon).size(LEN);
			}).size(LEN);

			t2.pane(table -> {
				table.add("[gray]IsSelected: [accent]" + selected + "[]").left().row();
				table.add("[gray]IsUnlocked: [accent]" + isUnlocked + "[]").left().row();
			}).size(LEN * 6f, LEN).pad(OFFSET);
			
			t2.pane(table -> {
				table.button(Icon.infoCircle, () -> {showInfo(this);}).size(LEN);
			}).size(LEN).pad(OFFSET);
		}).size(LEN * 11, LEN * 1.5f).row();
		t.image().fillX().pad(OFFSET).height(4f).color(Color.lightGray).row();
	}
		
	
	@Override
	public void addText(Table table){
		table.add("[gray]AmmoType: [accent]" + Core.bundle.get(name) + "[]").left().row();
	}
	public void write(Writes write) {
		write.bool(this.isUnlocked);
		write.bool(this.selected);
	}

	public void read(Reads read, byte revision) {
		this.isUnlocked = read.bool();
		this.selected = read.bool();
	}
	
	
	

}





