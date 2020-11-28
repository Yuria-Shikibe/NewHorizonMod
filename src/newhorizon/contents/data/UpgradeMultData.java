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

public abstract class UpgradeMultData extends UpgradeData{
	public boolean isUnlocked, selected;
	
	public UpgradeMultData(
		String name,
		String description,
		float costTime,
		int unlockLevel,
		ItemStack... items
	) {
		super(name, description, costTime, items);
		this.unlockLevel = unlockLevel;
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