package newhorizon.content;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import newhorizon.NewHorizon;
import newhorizon.func.DrawFuncs;

public class NHIconGenerator extends Block{
	public NHIconGenerator(){
		super("specific-icon-generator");
	}
	
	@Override
	public boolean isHidden(){
		return true;
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		if(Vars.mobile)return;
		NHLoader.fullIconNeeds.each( (name, iconSet) -> {
			TextureAtlas.AtlasRegion t = Core.atlas.find(iconSet.type.name);
			if(t.found()){
				PixmapRegion r = Core.atlas.getPixmap(t);
				Pixmap region = r.crop();
				
				Pixmap base = new Pixmap(r.width, r.height);
				base.drawPixmap(region);
				TextureAtlas.AtlasRegion cell = Core.atlas.find(iconSet.type.name + "-cell");
				if(cell != null && cell.found())base.drawPixmap(DrawFuncs.fillColor(Core.atlas.getPixmap(cell), Team.sharded.color), - 1, 0);
				
				for(Weapon w : iconSet.weapons){
					if(w.top)continue;
					DrawFuncs.drawWeaponPixmap(base, w, false);
				}
				
				base = DrawFuncs.getOutline(base, DrawFuncs.outlineColor);
				for(Weapon w : iconSet.weapons){
					if(!w.top)continue;
					DrawFuncs.drawWeaponPixmap(base, w, true);
				}
				packer.add(MultiPacker.PageType.main, name + "-icon", base);
			}else Log.info("[Create Fail]" + name);
		});
	}
	
	@Override
	public void load(){
		super.load();
		region = Core.atlas.find(NewHorizon.NHNAME + "level-up");
		NHLoader.fullIconNeeds.each( (name, iconSet) -> iconSet.type.shadowRegion = Core.atlas.find(iconSet.type.name + "-icon", iconSet.type.name));
		
		NHLoader.needBeLoad.each( (arg, tex) -> tex = Core.atlas.find(arg));
	}
	
	public static class IconSet{
		public final Seq<Weapon> weapons = new Seq<>();
		public final UnitType type;
		
		public IconSet(UnitType type, Weapon[] weapons){
			this.type = type;
			this.weapons.addAll(weapons);
		}
	}
}
