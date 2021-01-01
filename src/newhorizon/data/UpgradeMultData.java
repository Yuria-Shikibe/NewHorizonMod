package newhorizon.data;

import arc.util.io.*;
import mindustry.type.*;

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

	public UpgradeData cpy() {
		return (UpgradeData)clone();
	}
}