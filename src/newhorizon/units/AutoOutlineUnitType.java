package newhorizon.units;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Cicon;
import newhorizon.content.NHIconGenerator;
import newhorizon.content.NHLoader;

public class AutoOutlineUnitType extends UnitType{
	public AutoOutlineUnitType(String name, Weapon... weapons){
		super(name);
		
		NHLoader.put(
			name + "-leg",
			name + "-joint",
			name + "-joint-base",
			name + "-foot",
			name + "-leg-base",
			name + "-leg",
			name + "-base",
			name + "@-outline"
		);
		
		this.weapons.addAll(weapons);
		NHLoader.put(this.name, new NHIconGenerator.IconSet(this, weapons));
	}
	
	public AutoOutlineUnitType(String name){
		super(name);
		
		NHLoader.put(
			name + "-leg",
			name + "-joint",
			name + "-joint-base",
			name + "-foot",
			name + "-leg-base",
			name + "-leg",
			name + "-base",
			name + "@-outline"
		);
		
		NHLoader.put(this.name, new NHIconGenerator.IconSet(this, null));
	}
	
	@Override
	public void load(){
		super.load();
		shadowRegion = Core.atlas.find(name + "-icon", name);
	}
	
	public TextureRegion icon(Cicon icon) {
		return shadowRegion;
	}
	
}
