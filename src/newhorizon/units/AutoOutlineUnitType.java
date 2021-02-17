package newhorizon.units;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
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
	}
	
	@Override
	public void load(){
		super.load();
		shadowRegion = Core.atlas.find(name + "-icon", name);
		Log.info(name + "put");
	}
	
	public TextureRegion icon(Cicon icon) {
		/*if (this.cicons[icon.ordinal()] == null) {
			this.cicons[icon.ordinal()] =
			shadowRegion != null ? shadowRegion : outlineRegion != null ? outlineRegion :
			Core.atlas.find(this.getContentType().name() + "-" + this.name + "-" + icon.name(),
				Core.atlas.find(this.getContentType().name() + "-" + this.name + "-full",
					Core.atlas.find(this.name + "-" + icon.name(),
						Core.atlas.find(this.name + "-full",
							Core.atlas.find(this.name,
								Core.atlas.find(this.getContentType().name() + "-" + this.name,
									Core.atlas.find(this.name + "1")
								)
							)
						)
					)
				)
			);
		}
		return this.cicons[icon.ordinal()];
		*/
		
		
		return shadowRegion;
	}
	
}
